package org.apache.olingo.compliance.validation.rules.security;

import java.util.regex.Pattern;

import org.apache.olingo.compliance.validation.api.ValidationConfig;
import org.apache.olingo.compliance.validation.core.ValidationContext;

/**
 * Validates XML content for potential XXE (XML External Entity) vulnerabilities.
 */
public class XxeAttackRule extends AbstractSecurityRule {
    
    // Patterns for detecting XXE attacks
    private static final Pattern EXTERNAL_ENTITY_PATTERN = Pattern.compile(
        "<!ENTITY\\s+\\w+\\s+(SYSTEM|PUBLIC).*?>", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern PARAMETER_ENTITY_PATTERN = Pattern.compile(
        "<!ENTITY\\s+%\\s*\\w+.*?>", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern ENTITY_REFERENCE_PATTERN = Pattern.compile(
        "&\\w+;|%\\w+;"
    );
    
    public XxeAttackRule() {
        super("xxe-attack", 
              "Detects potential XXE (XML External Entity) attacks", 
              "error");
    }
    
    @Override
    protected boolean isSecurityApplicable(ValidationContext context, ValidationConfig config) {
        return context.getContent() != null || context.getFilePath() != null;
    }
    
    @Override
    public RuleResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        String content = getXmlContent(context);
        if (content == null) {
            return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
        }
        
        // Check for external entity declarations
        if (EXTERNAL_ENTITY_PATTERN.matcher(content).find()) {
            return RuleResult.fail(getName(), 
                                 "External entity declaration detected - potential XXE attack", 
                                 "External entities can be used to read local files or make network requests",
                                 System.currentTimeMillis() - startTime);
        }
        
        // Check for parameter entity declarations
        if (PARAMETER_ENTITY_PATTERN.matcher(content).find()) {
            return RuleResult.fail(getName(), 
                                 "Parameter entity declaration detected - potential XXE attack", 
                                 "Parameter entities can be used for recursive entity expansion attacks",
                                 System.currentTimeMillis() - startTime);
        }
        
        // Check for entity references that might not be declared
        if (hasUndeclaredEntityReferences(content)) {
            return RuleResult.fail(getName(), 
                                 "Undeclared entity references detected", 
                                 "Entity references without proper declarations can indicate XXE attempts",
                                 System.currentTimeMillis() - startTime);
        }
        
        return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    private String getXmlContent(ValidationContext context) {
        if (context.getContent() != null) {
            return context.getContent();
        }
        
        if (context.getFilePath() != null) {
            try {
                return new String(java.nio.file.Files.readAllBytes(context.getFilePath()), 
                                java.nio.charset.StandardCharsets.UTF_8);
            } catch (java.io.IOException e) {
                // Log error but don't fail validation
                context.addWarning(getName(), "Could not read file content for XXE analysis: " + e.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    private boolean hasUndeclaredEntityReferences(String content) {
        // Look for entity references
        java.util.regex.Matcher matcher = ENTITY_REFERENCE_PATTERN.matcher(content);
        while (matcher.find()) {
            String entityRef = matcher.group();
            // Skip standard XML entities
            if (isStandardXmlEntity(entityRef)) {
                continue;
            }
            
            // Check if entity is declared in the document
            String entityName = entityRef.substring(1, entityRef.length() - 1); // Remove & and ;
            if (!isEntityDeclared(content, entityName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isStandardXmlEntity(String entityRef) {
        return "&lt;".equals(entityRef) || 
               "&gt;".equals(entityRef) || 
               "&amp;".equals(entityRef) || 
               "&quot;".equals(entityRef) || 
               "&apos;".equals(entityRef);
    }
    
    private boolean isEntityDeclared(String content, String entityName) {
        // Simple check for entity declaration
        Pattern declarationPattern = Pattern.compile(
            "<!ENTITY\\s+" + Pattern.quote(entityName) + "\\s+.*?>", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        return declarationPattern.matcher(content).find();
    }
    
    @Override
    public long getEstimatedExecutionTime() {
        return 500; // XXE analysis can be more expensive
    }
}
