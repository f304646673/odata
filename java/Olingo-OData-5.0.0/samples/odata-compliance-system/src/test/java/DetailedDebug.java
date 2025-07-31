import org.apache.olingo.compliance.validation.core.*;
import org.apache.olingo.compliance.file.*;
import java.io.File;
import java.util.List;

public class DetailedDebug {
    public static void main(String[] args) {
        try {
            // 测试invalid inheritance scenario
            File testDir = new File("src/test/resources/validation/multiple/invalid/scenario3-invalid-inheritance");
            File invalidComplexFile = new File(testDir, "invalid-complex.xml");
            
            System.out.println("=== Testing Invalid Complex File ===");
            System.out.println("File: " + invalidComplexFile.getAbsolutePath());
            System.out.println("File exists: " + invalidComplexFile.exists());
            
            // 创建Schema Extractor和Registry
            SchemaExtractor extractor = new SchemaExtractor();
            SchemaRegistry registry = new SchemaRegistry();
            
            // 扫描目录中的所有XML文件并构建Registry
            File[] xmlFiles = testDir.listFiles((dir, name) -> name.endsWith(".xml"));
            if (xmlFiles != null) {
                for (File xmlFile : xmlFiles) {
                    List<SchemaRegistry.SchemaDefinition> schemas = extractor.extractSchemas(xmlFile);
                    for (SchemaRegistry.SchemaDefinition schema : schemas) {
                        System.out.println("Registering schema: " + schema.getNamespace() + " from " + xmlFile.getName());
                        registry.registerSchema(schema);
                    }
                }
            }
            
            // 创建简化的验证器，避免SLF4J依赖
            SimpleRegistryValidator validator = new SimpleRegistryValidator();
            
            // 验证invalid-complex.xml文件
            List<String> issues = validator.validateInheritance(invalidComplexFile, registry);
            
            System.out.println("\n=== Validation Results ===");
            System.out.println("Issues found: " + issues.size());
            for (String issue : issues) {
                System.out.println("  - " + issue);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SimpleRegistryValidator {
    
    public List<String> validateInheritance(File xmlFile, SchemaRegistry registry) {
        List<String> issues = new java.util.ArrayList<>();
        
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(xmlFile);
            
            // 获取当前文件的Schema命名空间
            org.w3c.dom.NodeList schemas = document.getElementsByTagNameNS("*", "Schema");
            String currentNamespace = "";
            if (schemas.getLength() > 0) {
                org.w3c.dom.Element schema = (org.w3c.dom.Element) schemas.item(0);
                currentNamespace = schema.getAttribute("Namespace");
                System.out.println("Current namespace: " + currentNamespace);
            }
            
            // 检查ComplexType的继承关系
            org.w3c.dom.NodeList complexTypes = document.getElementsByTagNameNS("*", "ComplexType");
            System.out.println("Found " + complexTypes.getLength() + " ComplexType elements");
            
            for (int i = 0; i < complexTypes.getLength(); i++) {
                org.w3c.dom.Element complexType = (org.w3c.dom.Element) complexTypes.item(i);
                String typeName = complexType.getAttribute("Name");
                String baseType = complexType.getAttribute("BaseType");
                
                System.out.println("Processing ComplexType: " + typeName + ", BaseType: " + baseType);
                
                if (baseType != null && !baseType.isEmpty()) {
                    String fullTypeName = currentNamespace + "." + typeName;
                    System.out.println("Full type name: " + fullTypeName);
                    System.out.println("Base type: " + baseType);
                    
                    boolean isValid = registry.isValidBaseType(fullTypeName, baseType);
                    System.out.println("Is valid inheritance: " + isValid);
                    
                    if (!isValid) {
                        String issue = "Invalid inheritance: ComplexType '" + typeName + "' cannot inherit from '" + baseType + "'";
                        issues.add(issue);
                        System.out.println("Added issue: " + issue);
                    }
                }
            }
            
        } catch (Exception e) {
            issues.add("Failed to validate inheritance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return issues;
    }
}
