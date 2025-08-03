import java.util.Set;
import org.apache.olingo.advanced.xmlparser.ReferenceResolverManager;

public class debug_test {
    public static void main(String[] args) throws Exception {
        ReferenceResolverManager manager = new ReferenceResolverManager();
        String schemaPath = "src/test/resources/schemas/dependencies/service-layer.xml";
        Set<String> references = manager.extractReferencesFromXml(schemaPath);
        System.out.println("References found: " + references.size());
        for (String ref : references) {
            System.out.println("  " + ref);
        }
    }
}
