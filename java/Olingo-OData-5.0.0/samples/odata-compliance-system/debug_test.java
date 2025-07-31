import org.apache.olingo.compliance.validation.directory.DirectoryValidationManager;
import java.io.IOException;

public class debug_test {
    public static void main(String[] args) throws IOException {
        DirectoryValidationManager validationManager = new DirectoryValidationManager();
        String directoryPath = "src/test/resources/validation/multiple/invalid/scenario2-alias-conflicts";
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        System.out.println("Result: " + result);
        System.out.println("Is valid: " + result.isValid());
        System.out.println("Conflicts: " + result.getConflictIssues().size());
        result.getConflictIssues().forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
}
