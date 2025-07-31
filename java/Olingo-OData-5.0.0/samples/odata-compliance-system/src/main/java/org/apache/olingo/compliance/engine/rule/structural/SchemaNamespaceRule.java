package org.apache.olingo.compliance.engine.rules.structural;

import org.apache.olingo.compliance.engine.rule.RuleResult;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;

/**
 * Validates that schema has a valid namespace.
 */
public class SchemaNamespaceRule extends AbstractStructuralRule {
    
    public SchemaNamespaceRule() {
        super("schema-namespace", 
              "Schema must have a valid namespace", 
              "error");
    }
    
    @Override
    protected boolean isStructurallyApplicable(ValidationContext context, ValidationConfig config) {
        // This rule can work with either parsed schema or raw XML content
        return context.getSchema() != null || 
               context.getContent() != null || 
               context.getFilePath() != null;
    }
    
    @Override
    public RuleResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        CsdlSchema schema = context.getSchema();
        
        // If we don't have a parsed schema, try to validate from raw XML
        if (schema == null) {
            return validateFromRawXml(context, startTime);
        }
        
        String namespace = schema.getNamespace();
        if (namespace == null || namespace.trim().isEmpty()) {
            return RuleResult.fail(getName(), "Schema must have a valid namespace", 
                                 System.currentTimeMillis() - startTime);
        }
        
        // Basic namespace format validation
        if (!isValidNamespaceFormat(namespace)) {
            return RuleResult.fail(getName(), 
                                 String.format("Invalid namespace format: %s", namespace), 
                                 System.currentTimeMillis() - startTime);
        }
        
        // Record the namespace for other rules
        context.addCurrentSchemaNamespace(namespace);
        context.addReferencedNamespace(namespace);
        
        return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    private RuleResult validateFromRawXml(ValidationContext context, long startTime) {
        String content = getXmlContent(context);
        if (content == null) {
            return RuleResult.fail(getName(), "No XML content available for namespace validation", 
                                 System.currentTimeMillis() - startTime);
        }
        
        // Check for Schema element with namespace attribute
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<Schema[^>]+Namespace\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        if (!matcher.find()) {
            return RuleResult.fail(getName(), "Schema must have a valid namespace", 
                                 System.currentTimeMillis() - startTime);
        }
        
        // Extract and validate namespace format
        String namespace = matcher.group(1);
        if (!isValidNamespaceFormat(namespace)) {
            return RuleResult.fail(getName(), 
                                 String.format("Invalid namespace format: %s", namespace), 
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
                byte[] bytes = java.nio.file.Files.readAllBytes(context.getFilePath());
                return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                // Ignore and return null
            }
        }
        
        return null;
    }
    
    private boolean isValidNamespaceFormat(String namespace) {
        // Basic validation: should not contain spaces, should have reasonable structure
        if (namespace.contains(" ") || namespace.contains("\t") || namespace.contains("\n")) {
            return false;
        }
        
        // Should not start or end with dots
        if (namespace.startsWith(".") || namespace.endsWith(".")) {
            return false;
        }
        
        // Should not have consecutive dots
        if (namespace.contains("..")) {
            return false;
        }
        
        // Should not contain invalid characters (like !)
        if (namespace.matches(".*[!@#$%^&*()\\-+=\\[\\]{}|\\\\:;\"'<>,?/~`].*")) {
            return false;
        }
        
        // Should not start with a number
        if (namespace.matches("^\\d.*")) {
            return false;
        }
        
        return true;
    }
}
