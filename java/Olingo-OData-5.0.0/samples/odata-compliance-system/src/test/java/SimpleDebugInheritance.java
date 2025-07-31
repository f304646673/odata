import org.apache.olingo.compliance.validation.core.*;
import java.io.File;
import java.util.List;

public class SimpleDebugInheritance {
    public static void main(String[] args) {
        try {
            // 测试invalid inheritance scenario
            File testDir = new File("src/test/resources/validation/multiple/invalid/scenario3-invalid-inheritance");
            
            System.out.println("=== Testing Invalid Inheritance Scenario ===");
            
            // 创建Schema Extractor和Registry
            SchemaExtractor extractor = new SchemaExtractor();
            SchemaRegistry registry = new SchemaRegistry();
            
            // 扫描目录中的所有XML文件
            File[] xmlFiles = testDir.listFiles((dir, name) -> name.endsWith(".xml"));
            if (xmlFiles == null) {
                System.out.println("No XML files found in test directory");
                return;
            }
            
            // 构建Schema注册表
            for (File xmlFile : xmlFiles) {
                List<SchemaRegistry.SchemaDefinition> schemas = extractor.extractSchemas(xmlFile);
                for (SchemaRegistry.SchemaDefinition schema : schemas) {
                    registry.registerSchema(schema);
                }
            }
            
            // 测试特定的继承关系验证
            System.out.println("\n=== Testing Inheritance Validation ===");
            
            // 检查 InvalidComplex -> BaseEntity
            String invalidComplexFullName = "Inheritance.Invalid.InvalidComplex";
            String baseEntityFullName = "Inheritance.Base.BaseEntity";
            
            System.out.println("Testing: " + invalidComplexFullName + " -> " + baseEntityFullName);
            
            SchemaRegistry.TypeDefinition invalidComplex = registry.getTypeDefinition(invalidComplexFullName);
            SchemaRegistry.TypeDefinition baseEntity = registry.getTypeDefinition(baseEntityFullName);
            
            System.out.println("InvalidComplex found: " + (invalidComplex != null));
            if (invalidComplex != null) {
                System.out.println("InvalidComplex kind: " + invalidComplex.getKind());
                System.out.println("InvalidComplex baseType: " + invalidComplex.getBaseType());
            }
            
            System.out.println("BaseEntity found: " + (baseEntity != null));
            if (baseEntity != null) {
                System.out.println("BaseEntity kind: " + baseEntity.getKind());
            }
            
            boolean isValidInheritance = registry.isValidBaseType(invalidComplexFullName, baseEntityFullName);
            System.out.println("Is valid inheritance: " + isValidInheritance);
            
            // 同时检查原始的 BaseType 属性
            if (invalidComplex != null && invalidComplex.getBaseType() != null) {
                boolean isValidOriginal = registry.isValidBaseType(invalidComplexFullName, invalidComplex.getBaseType());
                System.out.println("Original BaseType validation (" + invalidComplex.getBaseType() + "): " + isValidOriginal);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
