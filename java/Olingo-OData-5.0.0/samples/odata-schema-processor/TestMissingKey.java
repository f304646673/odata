import org.apache.olingo.schema.processor.validation.OlingoXmlFileComplianceValidator;
import org.apache.olingo.schema.processor.validation.XmlComplianceResult;
import java.io.File;

public class TestMissingKey {
    public static void main(String[] args) {
        OlingoXmlFileComplianceValidator validator = new OlingoXmlFileComplianceValidator();
        File xmlFile = new File("src/test/resources/validator/06-odata-compliance-errors/missing-key-property.xml");

        if (!xmlFile.exists()) {
            System.out.println("Test file does not exist: " + xmlFile.getAbsolutePath());
            return;
        }

        XmlComplianceResult result = validator.validateFile(xmlFile);

        System.out.println("File: " + xmlFile.getName());
        System.out.println("Is Compliant: " + result.isCompliant());
        System.out.println("Has Errors: " + result.hasErrors());
        System.out.println("Error Count: " + result.getErrorCount());
        System.out.println("Errors: " + result.getErrors());
    }
}
