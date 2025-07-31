import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.olingo.compliance.file.ModernXmlFileComplianceValidator;
import org.apache.olingo.compliance.file.XmlComplianceResult;
import org.apache.olingo.compliance.file.ComplianceIssue;

public class DebugDuplicate {
    public static void main(String[] args) {
        ModernXmlFileComplianceValidator validator = new ModernXmlFileComplianceValidator();
        
        // 测试所有的重复错误案例
        List<String> testFiles = Arrays.asList(
            "invalid-action-name-duplicate",
            "invalid-action-parameter-duplicate", 
            "invalid-complextype-name-duplicate",
            "invalid-duplicate-action-name",
            "invalid-duplicate-complextype-name",
            "invalid-duplicate-complextype-navigationproperty-name",
            "invalid-duplicate-complextype-property-name",
            "invalid-duplicate-entitycontainer-name", 
            "invalid-duplicate-entitytype-name",
            "invalid-duplicate-enumtype-member-name",
            "invalid-duplicate-enumtype-name",
            "invalid-duplicate-function-name",
            "invalid-duplicate-namespace",
            "invalid-duplicate-navigationproperty-name",
            "invalid-duplicate-term-name",
            "invalid-duplicate-typedefinition-name",
            "invalid-entitycontainer-actionimport-duplicate",
            "invalid-entitycontainer-entityset-duplicate",
            "invalid-entitycontainer-functionimport-duplicate",
            "invalid-entitycontainer-name-duplicate",
            "invalid-entitycontainer-singleton-duplicate",
            "invalid-entitytype-key-ref-duplicate",
            "invalid-entitytype-name-duplicate",
            "invalid-enumtype-member-duplicate",
            "invalid-enumtype-member-value-duplicate",
            "invalid-enumtype-name-duplicate",
            "invalid-function-name-duplicate",
            "invalid-function-parameter-duplicate",
            "invalid-term-name-duplicate",
            "invalid-typedefinition-name-duplicate"
        );
        
        System.out.println("=== Checking all duplicate error files ===");
        int validFiles = 0;
        int errorFiles = 0;
        
        for (String fileName : testFiles) {
            String filePath = "src/test/resources/validation/single/invalid/duplicate/" + fileName + "/" + fileName + ".xml";
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println(fileName + ": FILE_NOT_FOUND");
                continue;
            }
            
            XmlComplianceResult result = validator.validateFile(file);
            
            if (result.isCompliant() || !result.hasErrors()) {
                System.out.println(fileName + ": VALID (no errors detected)");
                validFiles++;
            } else {
                System.out.println(fileName + ": ERROR - " + result.getIssues().get(0).getErrorType() + 
                    " - " + result.getIssues().get(0).getMessage());
                errorFiles++;
            }
        }
        
        System.out.println("\n=== Summary ===");
        System.out.println("Files with errors: " + errorFiles);
        System.out.println("Files considered valid: " + validFiles);
    }
}
