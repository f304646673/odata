package debug;

import org.apache.olingo.compliance.engine.core.impl.DefaultSchemaRegistryImpl;
import org.apache.olingo.compliance.validator.ComplianceValidator;
import org.apache.olingo.compliance.validator.impl.ComplianceValidatorImpl;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.core.model.ComplianceResult;

public class ConflictDetectionDebug {
    public static void main(String[] args) {
        ComplianceValidator validator = new ComplianceValidatorImpl();
        SchemaRegistry registry = new DefaultSchemaRegistryImpl();
        String testDir = "src/test/resources/validation/multiple/invalid/element-conficts/scenario1-typedefinition-conflicts";
        
        try {
            ComplianceResult result = validator.validateDirectory(testDir, registry, true);

            System.out.println("Directory: " + testDir);
            System.out.println("Is compliant: " + result.isCompliant());
            System.out.println("Total issues: " + result.getIssues().size());
            System.out.println("Validation time: " + result.getValidationTimeMs() + "ms");

            result.getIssues().forEach(issue -> {
                System.out.println("Issue: " + issue.getErrorType() + " - " + issue.getMessage());
                System.out.println("  Severity: " + issue.getSeverity());
                System.out.println("  Location: " + issue.getLocation());
            });

            // 打印元数据信息
            System.out.println("\nMetadata:");
            result.getMetadata().forEach((key, value) -> {
                System.out.println("  " + key + ": " + value);
            });
            
        } catch (Exception e) {
            System.err.println("Error during validation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
