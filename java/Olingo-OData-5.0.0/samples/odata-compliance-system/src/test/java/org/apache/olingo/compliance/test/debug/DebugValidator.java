package org.apache.olingo.compliance.test.debug;

import org.apache.olingo.compliance.file.ModernXmlFileComplianceValidator;
import org.apache.olingo.compliance.file.ComplianceIssue;
import org.apache.olingo.compliance.file.XmlComplianceResult;
import java.util.List;
import java.io.File;

public class DebugValidator {
    public static void main(String[] args) {
        ModernXmlFileComplianceValidator validator = new ModernXmlFileComplianceValidator();
        
        String testFile = "src/test/resources/validation/single/invalid/missing/invalid-entitycontainer-missing-name/invalid-entitycontainer-missing-name.xml";
        File file = new File(testFile);
        
        System.out.println("Testing file: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        
        XmlComplianceResult result = validator.validateFile(file);
        List<ComplianceIssue> issues = result.getIssues();
        
        System.out.println("Found " + issues.size() + " issues:");
        for (ComplianceIssue issue : issues) {
            System.out.println("- Type: " + issue.getErrorType());
            System.out.println("  Message: " + issue.getMessage());
            System.out.println("  Severity: " + issue.getSeverity());
            System.out.println();
        }
    }
}
