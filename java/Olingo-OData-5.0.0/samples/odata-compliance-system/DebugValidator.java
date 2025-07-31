import java.io.File;
import org.apache.olingo.compliance.file.ModernXmlFileComplianceValidator;
import org.apache.olingo.compliance.file.XmlComplianceResult;
import org.apache.olingo.compliance.file.ComplianceIssue;

public class DebugValidator {
    public static void main(String[] args) {
        ModernXmlFileComplianceValidator validator = new ModernXmlFileComplianceValidator();
        
        String[] testFiles = {
            "src/test/resources/validation/single/invalid/attribute-error/invalid-parameter-missing-name/invalid-parameter-missing-name.xml",
            "src/test/resources/validation/single/invalid/attribute-error/invalid-enumtype-member-missing-name/invalid-enumtype-member-missing-name.xml",
            "src/test/resources/validation/single/invalid/attribute-error/invalid-name-not-identifier/invalid-name-not-identifier.xml"
        };
        
        for (String filePath : testFiles) {
            System.out.println("\n=== Testing: " + filePath + " ===");
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("File does not exist!");
                continue;
            }
            
            XmlComplianceResult result = validator.validateFile(file);
            System.out.println("Is compliant: " + result.isCompliant());
            System.out.println("Has errors: " + result.hasErrors());
            
            if (result.hasErrors()) {
                System.out.println("Errors:");
                for (ComplianceIssue issue : result.getIssues()) {
                    System.out.println("  - Type: " + issue.getErrorType());
                    System.out.println("    Message: " + issue.getMessage());
                    System.out.println("    Severity: " + issue.getSeverity());
                }
            } else {
                System.out.println("No errors found!");
            }
        }
    }
}
