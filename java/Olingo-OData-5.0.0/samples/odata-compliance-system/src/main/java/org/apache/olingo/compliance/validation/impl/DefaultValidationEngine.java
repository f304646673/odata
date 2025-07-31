package org.apache.olingo.compliance.validation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.olingo.compliance.validation.api.ValidationConfig;
import org.apache.olingo.compliance.validation.api.ValidationResult;
import org.apache.olingo.compliance.validation.core.ValidationContext;
import org.apache.olingo.compliance.validation.core.ValidationEngine;
import org.apache.olingo.compliance.validation.core.ValidationRule;
import org.apache.olingo.compliance.validation.core.ValidationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the validation engine.
 * This engine coordinates validation rules and strategies.
 */
public class DefaultValidationEngine implements ValidationEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultValidationEngine.class);
    
    private final Map<String, ValidationRule> rules = new ConcurrentHashMap<>();
    private final List<ValidationStrategy> strategies = new ArrayList<>();
    private final ExecutorService executorService;
    
    public DefaultValidationEngine() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "ValidationEngine-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });
    }
    
    public DefaultValidationEngine(ExecutorService executorService) {
        this.executorService = executorService;
    }
    
    @Override
    public ValidationResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Find appropriate strategy
            ValidationStrategy strategy = findStrategy(context);
            if (strategy == null) {
                return ValidationResult.error(context.getFileName(), 
                                            "No validation strategy found for context", 
                                            System.currentTimeMillis() - startTime);
            }
            
            // Check timeout
            long maxTime = config.getMaxProcessingTime();
            if (maxTime > 0) {
                return executeWithTimeout(strategy, context, config, maxTime);
            } else {
                return strategy.execute(context, config, this);
            }
            
        } catch (Exception e) {
            logger.error("Validation failed for {}", context.getFileName(), e);
            return ValidationResult.error(context.getFileName(), 
                                        "Validation failed: " + e.getMessage(), 
                                        System.currentTimeMillis() - startTime);
        }
    }
    
    @Override
    public void registerRule(ValidationRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule cannot be null");
        }
        rules.put(rule.getName(), rule);
        logger.debug("Registered validation rule: {}", rule.getName());
    }
    
    @Override
    public void unregisterRule(String ruleName) {
        if (ruleName == null) {
            return;
        }
        ValidationRule removed = rules.remove(ruleName);
        if (removed != null) {
            logger.debug("Unregistered validation rule: {}", ruleName);
        }
    }
    
    @Override
    public List<ValidationRule> getRegisteredRules() {
        return new ArrayList<>(rules.values());
    }
    
    @Override
    public ValidationRule getRule(String ruleName) {
        return rules.get(ruleName);
    }
    
    @Override
    public boolean hasRule(String ruleName) {
        return rules.containsKey(ruleName);
    }
    
    /**
     * Registers a validation strategy.
     */
    public void registerStrategy(ValidationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        strategies.add(strategy);
        logger.debug("Registered validation strategy: {}", strategy.getName());
    }
    
    /**
     * Gets all registered strategies.
     */
    public List<ValidationStrategy> getRegisteredStrategies() {
        return Collections.unmodifiableList(strategies);
    }
    
    /**
     * Finds the appropriate strategy for the given context.
     */
    private ValidationStrategy findStrategy(ValidationContext context) {
        return strategies.stream()
                .filter(strategy -> strategy.canHandle(context))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Executes validation with timeout support.
     */
    private ValidationResult executeWithTimeout(ValidationStrategy strategy, 
                                              ValidationContext context, 
                                              ValidationConfig config, 
                                              long timeoutMs) {
        
        Future<ValidationResult> future = executorService.submit(() -> 
            strategy.execute(context, config, this));
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.warn("Validation timed out for {}", context.getFileName());
            return ValidationResult.error(context.getFileName(), 
                                        "Validation timed out after " + timeoutMs + "ms", 
                                        timeoutMs);
        } catch (Exception e) {
            future.cancel(true);
            logger.error("Validation execution failed for {}", context.getFileName(), e);
            return ValidationResult.error(context.getFileName(), 
                                        "Validation execution failed: " + e.getMessage(), 
                                        System.currentTimeMillis() - System.currentTimeMillis());
        }
    }
    
    /**
     * Executes rules in parallel if enabled.
     */
    public ValidationResult executeRulesParallel(ValidationContext context, ValidationConfig config) {
        List<ValidationRule> applicableRules = getApplicableRules(context, config);
        
        if (!config.isParallelProcessingEnabled() || applicableRules.size() <= 1) {
            return executeRulesSequential(context, config, applicableRules);
        }
        
        List<CompletableFuture<ValidationRule.RuleResult>> futures = applicableRules.stream()
                .map(rule -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return rule.validate(context, config);
                    } catch (Exception e) {
                        logger.warn("Rule execution failed: {}", rule.getName(), e);
                        return ValidationRule.RuleResult.fail(rule.getName(), 
                                                            "Rule execution failed: " + e.getMessage(), 
                                                            0);
                    }
                }, executorService))
                .collect(Collectors.toList());
        
        // Wait for all rules to complete
        CompletableFuture<Void> allRules = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        try {
            allRules.get(config.getMaxProcessingTime(), TimeUnit.MILLISECONDS);
            
            // Collect results
            for (CompletableFuture<ValidationRule.RuleResult> future : futures) {
                ValidationRule.RuleResult result = future.get();
                processRuleResult(result, context);
            }
            
        } catch (Exception e) {
            logger.error("Parallel rule execution failed", e);
            context.addError("Parallel rule execution failed: " + e.getMessage());
        }
        
        return buildValidationResult(context);
    }
    
    private ValidationResult executeRulesSequential(ValidationContext context, 
                                                   ValidationConfig config, 
                                                   List<ValidationRule> rules) {
        for (ValidationRule rule : rules) {
            try {
                ValidationRule.RuleResult result = rule.validate(context, config);
                processRuleResult(result, context);
            } catch (Exception e) {
                logger.warn("Rule execution failed: {}", rule.getName(), e);
                context.addWarning("Rule execution failed: " + rule.getName() + " - " + e.getMessage());
            }
        }
        
        return buildValidationResult(context);
    }
    
    private List<ValidationRule> getApplicableRules(ValidationContext context, ValidationConfig config) {
        return rules.values().stream()
                .filter(rule -> rule.isApplicable(context, config))
                .collect(Collectors.toList());
    }
    
    private void processRuleResult(ValidationRule.RuleResult result, ValidationContext context) {
        if (result.isFailed()) {
            // Find the rule to get its severity
            ValidationRule rule = rules.get(result.getRuleName());
            if (rule != null) {
                switch (rule.getSeverity()) {
                    case "error":
                        context.addError(rule.getName(), result.getMessage());
                        break;
                    case "warning":
                        context.addWarning(rule.getName(), result.getMessage());
                        break;
                    default:
                        context.addInfo(rule.getName(), result.getMessage());
                        break;
                }
            } else {
                context.addError(result.getRuleName(), result.getMessage());
            }
        }
        
        context.recordRuleExecution(result.getRuleName(), result.getExecutionTime());
    }
    
    private ValidationResult buildValidationResult(ValidationContext context) {
        long processingTime = context.getProcessingTime();
        
        ValidationResult.Builder builder = new ValidationResult.Builder()
                .fileName(context.getFileName())
                .processingTime(processingTime)
                .addErrors(context.getErrors())
                .addWarnings(context.getWarnings());
        
        // Add metadata
        builder.addMetadata("ruleExecutionTimes", context.getAllRuleExecutionTimes());
        if (context.getSchema() != null) {
            builder.addMetadata("schemaNamespace", context.getSchema().getNamespace());
        }
        
        return builder.build();
    }
    
    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
