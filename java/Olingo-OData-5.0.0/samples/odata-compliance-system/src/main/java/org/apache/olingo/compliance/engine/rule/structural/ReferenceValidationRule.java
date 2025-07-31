package org.apache.olingo.compliance.engine.rules.structural;

import org.apache.olingo.compliance.engine.rule.RuleResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;

/**
 * Rule for validating external references in OData schema files.
 * Checks if referenced files exist and are accessible.
 */
public class ReferenceValidationRule extends AbstractStructuralRule {
    
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(
        "<edmx:Reference\\s+Uri\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>", 
        Pattern.CASE_INSENSITIVE
    );
    
    public ReferenceValidationRule() {
        super("reference-validation", 
              "Validates external references in OData schema files", 
              "error");
    }
    
    @Override
    protected boolean isStructurallyApplicable(ValidationContext context, ValidationConfig config) {
        // This rule needs XML content to look for references
        return context.getContent() != null || context.getFilePath() != null;
    }
    
    @Override
    public RuleResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        // Get XML content
        String xmlContent = getXmlContent(context);
        if (xmlContent == null) {
            return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
        }
        
        // Find all edmx:Reference elements
        Matcher matcher = REFERENCE_PATTERN.matcher(xmlContent);
        
        while (matcher.find()) {
            String referencedUri = matcher.group(1);
            
            // Validate the referenced file
            RuleResult result = validateReference(referencedUri, context, startTime);
            if (!result.isPassed()) {
                return result;
            }
        }
        
        return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    private RuleResult validateReference(String uri, ValidationContext context, long startTime) {
        // Skip HTTP/HTTPS URLs as they are external references
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
        }
        
        // For relative file references, check if file exists
        if (context.getFilePath() != null) {
            Path currentDir = context.getFilePath().getParent();
            Path referencedFile = currentDir != null ? currentDir.resolve(uri) : Paths.get(uri);
            
            if (!Files.exists(referencedFile)) {
                return RuleResult.fail(getName(), 
                    String.format("Referenced file does not exist: %s", uri),
                    System.currentTimeMillis() - startTime);
            }
            
            if (!Files.isReadable(referencedFile)) {
                return RuleResult.fail(getName(), 
                    String.format("Referenced file is not readable: %s", uri),
                    System.currentTimeMillis() - startTime);
            }
        }
        
        return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    private String getXmlContent(ValidationContext context) {
        if (context.getContent() != null) {
            return context.getContent();
        }
        
        if (context.getFilePath() != null) {
            try {
                byte[] bytes = Files.readAllBytes(context.getFilePath());
                return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                // Ignore and return null
            }
        }
        
        return null;
    }
}
