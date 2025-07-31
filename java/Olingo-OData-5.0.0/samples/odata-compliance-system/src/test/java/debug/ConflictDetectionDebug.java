package debug;

import org.apache.olingo.compliance.validator.directory.DirectoryValidationManager;
import java.nio.file.Paths;

public class ConflictDetectionDebug {
    public static void main(String[] args) {
        DirectoryValidationManager manager = new DirectoryValidationManager();
        String testDir = "src/test/resources/validation/multiple/invalid/element-conficts/scenario1-typedefinition-conflicts";
        
        try {
            DirectoryValidationManager.DirectoryValidationResult result = 
                manager.validateSingleDirectory(testDir);
            
            System.out.println("Directory: " + testDir);
            System.out.println("Is valid: " + result.isValid());
            System.out.println("Total files: " + result.getTotalFiles());
            System.out.println("Total issues: " + result.getTotalIssueCount());
            
            result.getAllIssues().forEach(issue -> {
                System.out.println("Issue: " + issue.getErrorType() + " - " + issue.getMessage());
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
