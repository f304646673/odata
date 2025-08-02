package org.apache.olingo.compliance.engine.core.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.core.api.ValidationResult;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.core.ValidationEngine;
import org.apache.olingo.compliance.engine.core.ValidationStrategy;
import org.apache.olingo.compliance.engine.rule.ValidationRule;
import org.apache.olingo.server.core.MetadataParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation strategy for single file validation.
 */
public class FileValidationStrategyImpl implements ValidationStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationStrategyImpl.class);
    
    @Override
    public String getName() {
        return "file-validation";
    }
    
    @Override
    public boolean canHandle(ValidationContext context) {
        return context.getFilePath() != null || 
               context.getContent() != null || 
               context.getInputStream() != null ||
               (context.getAllSchemas() != null && !context.getAllSchemas().isEmpty());
    }
    
    @Override
    public ValidationResult execute(ValidationContext context, ValidationConfig config, ValidationEngine engine) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute pre-parsing rules (for structural validation that doesn't require parsed schema)
            List<ValidationRule> rules = engine.getRegisteredRules();
            for (ValidationRule rule : rules) {
                if (rule.getName().equals("schema-namespace") && rule.isApplicable(context, config)) {
                    executeRule(rule, context, config);
                }
            }
            
            // Parse schema if not already parsed
            if (context.getAllSchemas() == null || context.getAllSchemas().isEmpty()) {
                parseSchema(context);
            }
            
            // Execute remaining validation rules regardless of parsing status
            for (ValidationRule rule : rules) {
                if (!rule.getName().equals("schema-namespace") && rule.isApplicable(context, config)) {
                    executeRule(rule, context, config);
                }
            }
            
            // Build final result
            return buildResult(context, startTime);
            
        } catch (Exception e) {
            logger.error("File validation failed for {}", context.getFileName(), e);
            return ValidationResult.error(context.getFileName(), 
                                        "Validation failed: " + e.getMessage(), 
                                        System.currentTimeMillis() - startTime);
        }
    }
    
    @Override
    public long getEstimatedExecutionTime(ValidationContext context) {
        // Base time + estimated time based on content size
        long baseTime = 1000; // 1 second base
        
        if (context.getFilePath() != null) {
            try {
                long fileSize = Files.size(context.getFilePath());
                return baseTime + (fileSize / 1024); // Add 1ms per KB
            } catch (Exception e) {
                // Ignore and use base time
            }
        }
        
        if (context.getContent() != null) {
            return baseTime + (context.getContent().length() / 1024);
        }
        
        return baseTime;
    }
    
    private void parseSchema(ValidationContext context) {
        try {
            InputStream inputStream = getInputStream(context);
            if (inputStream == null) {
                context.addError("No input source available for parsing");
                return;
            }
            
            // Use Olingo's MetadataParser to parse the XML
            MetadataParser parser = new MetadataParser();
            
            // For now, disable recursive reference loading to avoid xml:base issues
            // TODO: Implement proper ReferenceResolver when Olingo API allows it
            parser.recursivelyLoadReferences(false);
            parser.parseAnnotations(true);
            
            // Parse the XML
            java.io.InputStreamReader reader = new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8);
            org.apache.olingo.server.core.SchemaBasedEdmProvider edmProvider = parser.buildEdmProvider(reader);
            
            if (edmProvider == null || edmProvider.getSchemas().isEmpty()) {
                context.addError("No valid schema found in the XML");
                return;
            }
            
            // Get schemas from the EDM provider
            List<CsdlSchema> schemas = edmProvider.getSchemas();
            context.setAllSchemas(schemas);
            
            // Add current schema namespaces to context
            for (CsdlSchema schema : schemas) {
                if (schema.getNamespace() != null) {
                    context.addCurrentSchemaNamespace(schema.getNamespace());
                }
            }
            
        } catch (Exception e) {
            context.addError("Validation error: " + e.getMessage());
            logger.debug("Schema parsing failed for {}", context.getFileName(), e);
        }
    }
    
    private InputStream getInputStream(ValidationContext context) throws Exception {
        if (context.getInputStream() != null) {
            return context.getInputStream();
        }
        
        if (context.getContent() != null) {
            return new ByteArrayInputStream(context.getContent().getBytes(StandardCharsets.UTF_8));
        }
        
        if (context.getFilePath() != null) {
            return Files.newInputStream(context.getFilePath());
        }
        
        return null;
    }
    
    private void executeRule(ValidationRule rule, ValidationContext context, ValidationConfig config) {
        try {
            long ruleStart = System.currentTimeMillis();
            ValidationRule.RuleResult result = rule.validate(context, config);
            long ruleTime = System.currentTimeMillis() - ruleStart;
            
            context.recordRuleExecution(rule.getName(), ruleTime);
            
            if (result.isFailed()) {
                if ("error".equals(rule.getSeverity())) {
                    context.addError(rule.getName(), result.getMessage());
                } else if ("warning".equals(rule.getSeverity())) {
                    context.addWarning(rule.getName(), result.getMessage());
                } else {
                    context.addInfo(rule.getName(), result.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.warn("Rule execution failed: {}", rule.getName(), e);
            context.addWarning("Rule execution failed: " + rule.getName() + " - " + e.getMessage());
        }
    }
    
    private ValidationResult buildResult(ValidationContext context, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        
        ValidationResult.Builder builder = new ValidationResult.Builder()
                .fileName(context.getFileName())
                .processingTime(processingTime)
                .addErrors(context.getErrors())
                .addWarnings(context.getWarnings());
        
        // Add metadata
        builder.addMetadata("ruleExecutionTimes", context.getAllRuleExecutionTimes());
        if (context.getAllSchemas() != null && !context.getAllSchemas().isEmpty()) {
            // Add all schema namespaces
            List<String> namespaces = new ArrayList<>();
            for (CsdlSchema schema : context.getAllSchemas()) {
                if (schema.getNamespace() != null) {
                    namespaces.add(schema.getNamespace());
                }
            }
            builder.addMetadata("schemaNamespaces", namespaces);
        }
        
        // Add file size if available
        if (context.getFilePath() != null) {
            try {
                builder.fileSize(Files.size(context.getFilePath()));
            } catch (Exception e) {
                // Ignore
            }
        }
        
        return builder.build();
    }
    
    private ValidationResult buildErrorResult(ValidationContext context, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        
        return new ValidationResult.Builder()
                .fileName(context.getFileName())
                .processingTime(processingTime)
                .addErrors(context.getErrors())
                .addWarnings(context.getWarnings())
                .compliant(false)
                .build();
    }
}
