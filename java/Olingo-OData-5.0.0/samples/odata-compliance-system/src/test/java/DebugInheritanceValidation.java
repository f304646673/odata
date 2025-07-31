import org.apache.olingo.compliance.validation.core.*;
import org.apache.olingo.compliance.file.*;
import java.io.File;
import java.util.List;

public class DebugInheritanceValidation {
    public static void main(String[] args) {
        try {
            // 测试invalid inheritance scenario
            File testDir = new File("src/test/resources/validation/multiple/invalid/scenario3-invalid-inheritance");
            
            System.out.println("=== Testing Invalid Inheritance Scenario ===");
            System.out.println("Test directory: " + testDir.getAbsolutePath());
            
            // 创建Schema Extractor和Registry
            SchemaExtractor extractor = new SchemaExtractor();
            SchemaRegistry registry = new SchemaRegistry();
            
            // 扫描目录中的所有XML文件
            File[] xmlFiles = testDir.listFiles((dir, name) -> name.endsWith(".xml"));
            if (xmlFiles == null) {
                System.out.println("No XML files found in test directory");
                return;
            }
            
            System.out.println("Found " + xmlFiles.length + " XML files");
            
            // 构建Schema注册表
            for (File xmlFile : xmlFiles) {
                System.out.println("\n--- Processing file: " + xmlFile.getName() + " ---");
                
                List<SchemaRegistry.SchemaDefinition> schemas = extractor.extractSchemas(xmlFile);
                System.out.println("Extracted " + schemas.size() + " schemas");
                
                for (SchemaRegistry.SchemaDefinition schema : schemas) {
                    System.out.println("Schema namespace: " + schema.getNamespace());
                    System.out.println("Schema alias: " + schema.getAlias());
                    System.out.println("Types in schema: " + schema.getTypes().size());
                    
                    for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                        System.out.println("  Type: " + type.getName() + " (" + type.getKind() + ")");
                        if (type.getBaseType() != null && !type.getBaseType().isEmpty()) {
                            System.out.println("    BaseType: " + type.getBaseType());
                        }
                    }
                    
                    registry.registerSchema(schema);
                }
            }
            
            System.out.println("\n=== Registry Statistics ===");
            SchemaRegistry.RegistryStatistics stats = registry.getStatistics();
            System.out.println("Total schemas: " + stats.getNamespaceCount());
            System.out.println("Total types: " + stats.getTotalTypes());
            System.out.println("Entity types: " + stats.getEntityTypes());
            System.out.println("Complex types: " + stats.getComplexTypes());
            
            // 测试每个文件的验证
            RegistryAwareXmlValidator validator = new RegistryAwareXmlValidator();
            
            for (File xmlFile : xmlFiles) {
                System.out.println("\n--- Validating file: " + xmlFile.getName() + " ---");
                
                XmlComplianceResult result = validator.validateWithRegistry(xmlFile, registry);
                System.out.println("Is compliant: " + result.isCompliant());
                System.out.println("Issues count: " + result.getIssues().size());
                
                if (!result.getIssues().isEmpty()) {
                    for (ComplianceIssue issue : result.getIssues()) {
                        System.out.println("  Issue: " + issue.getErrorType() + " - " + issue.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
