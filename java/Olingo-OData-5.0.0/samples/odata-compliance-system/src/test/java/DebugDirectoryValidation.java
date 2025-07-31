import org.apache.olingo.compliance.validation.directory.*;
import java.nio.file.Paths;

public class DebugDirectoryValidation {
    public static void main(String[] args) {
        try {
            String testResourcesPath = "src/test/resources/validation/multiple";
            String directoryPath = Paths.get(testResourcesPath, "invalid", "scenario3-invalid-inheritance").toString();
            
            System.out.println("=== Testing Directory Validation ===");
            System.out.println("Directory: " + directoryPath);
            
            DirectoryValidationManager validationManager = new DirectoryValidationManager();
            DirectoryValidationManager.DirectoryValidationResult result = 
                validationManager.validateDirectory(directoryPath);
            
            System.out.println("\n=== Validation Result ===");
            System.out.println("Total files: " + result.getTotalFiles());
            System.out.println("Is valid: " + result.isValid());
            
            System.out.println("\n=== File Results ===");
            result.getValidationResults().forEach((file, vr) -> {
                System.out.println("File: " + file);
                System.out.println("  Is compliant: " + vr.isCompliant());
                System.out.println("  Issues count: " + vr.getIssues().size());
                
                if (!vr.getIssues().isEmpty()) {
                    System.out.println("  Issues:");
                    vr.getIssues().forEach(issue -> {
                        System.out.println("    - " + issue.getErrorType() + ": " + issue.getMessage());
                        System.out.println("      Severity: " + issue.getSeverity());
                    });
                }
            });
            
            System.out.println("\n=== All Issues ===");
            result.getAllIssues().forEach(issue -> {
                System.out.println("- " + issue.getErrorType() + ": " + issue.getMessage());
                System.out.println("  Location: " + issue.getLocation());
                System.out.println("  Severity: " + issue.getSeverity());
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
