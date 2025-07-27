package org.apache.olingo.schema.processor.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Schema引用验证器的单元测试
 */
public class SchemaReferenceValidatorTest {
    
    private SchemaReferenceValidator validator;
    private Path testDir;
    
    @Before
    public void setUp() throws IOException {
        validator = new SchemaReferenceValidator();
        testDir = Files.createTempDirectory("schema-test");
    }
    
    @After
    public void tearDown() throws IOException {
        // 清理测试文件
        Files.walk(testDir)
            .sorted((a, b) -> b.compareTo(a)) // 先删除文件，再删除目录
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // 忽略清理错误
                }
            });
    }
    
    @Test
    public void testValidSchemaWithReferences() throws Exception {
        // 创建有正确引用的Schema
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:Reference Uri=\"../core/CoreTypes.xml\">\n" +
            "    <edmx:Include Namespace=\"Core.Types\"/>\n" +
            "  </edmx:Reference>\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Business.Entities\">\n" +
            "      <EntityType Name=\"Customer\" BaseType=\"Core.Types.BaseEntity\">\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Path testFile = testDir.resolve("valid-schema.xml");
        Files.write(testFile, validXml.getBytes());
        
        SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(testFile);
        
        assertTrue("Schema应该是有效的", result.isValid());
        assertTrue("不应该有错误", result.getErrors().isEmpty());
        assertTrue("应该检测到引用的namespace", result.getReferencedNamespaces().contains("Core.Types"));
        assertFalse("应该检测到声明的引用", result.getDeclaredReferences().isEmpty());
    }
    
    @Test
    public void testInvalidSchemaWithoutReferences() throws Exception {
        // 创建没有引用但使用了外部类型的Schema（当前的bug情况）
        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Common.Products\">\n" +
            "      <EntityType Name=\"Product\" BaseType=\"Core.Types.BaseEntity\">\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Price\" Type=\"Edm.Decimal\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Path testFile = testDir.resolve("invalid-schema.xml");
        Files.write(testFile, invalidXml.getBytes());
        
        SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(testFile);
        
        assertFalse("Schema应该是无效的", result.isValid());
        assertFalse("应该有错误", result.getErrors().isEmpty());
        assertTrue("应该检测到引用的namespace", result.getReferencedNamespaces().contains("Core.Types"));
        assertTrue("不应该有声明的引用", result.getDeclaredReferences().isEmpty());
        
        // 检查错误消息
        String errorMessage = result.getErrors().get(0);
        assertTrue("错误消息应该包含缺失的namespace", 
                  errorMessage.contains("Core.Types"));
        assertTrue("错误消息应该提到edmx:Reference", 
                  errorMessage.contains("edmx:Reference"));
    }
    
    @Test
    public void testSchemaWithMissingReferences() throws Exception {
        // 创建缺少部分引用的Schema
        String partialRefXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:Reference Uri=\"../core/CoreTypes.xml\">\n" +
            "    <edmx:Include Namespace=\"Core.Types\"/>\n" +
            "  </edmx:Reference>\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Business.Entities\">\n" +
            "      <EntityType Name=\"Customer\" BaseType=\"Core.Types.BaseEntity\">\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\" Nullable=\"false\"/>\n" +
            "        <NavigationProperty Name=\"Address\" Type=\"Common.Address.AddressInfo\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Path testFile = testDir.resolve("partial-ref-schema.xml");
        Files.write(testFile, partialRefXml.getBytes());
        
        SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(testFile);
        
        assertFalse("Schema应该是无效的", result.isValid());
        assertTrue("应该检测到Core.Types引用", result.getReferencedNamespaces().contains("Core.Types"));
        assertTrue("应该检测到Common.Address引用", result.getReferencedNamespaces().contains("Common.Address"));
        
        Set<String> missingRefs = result.getMissingReferences();
        assertTrue("应该有缺失的引用", missingRefs.contains("Common.Address"));
        assertFalse("Core.Types应该不缺失", missingRefs.contains("Core.Types"));
    }
    
    @Test
    public void testSchemaWithOnlyBuiltInTypes() throws Exception {
        // 创建只使用EDM内置类型的Schema
        String builtInTypesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Simple.Types\">\n" +
            "      <EntityType Name=\"SimpleEntity\">\n" +
            "        <Property Name=\"Id\" Type=\"Edm.Guid\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "        <Property Name=\"Count\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Path testFile = testDir.resolve("builtin-types-schema.xml");
        Files.write(testFile, builtInTypesXml.getBytes());
        
        SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(testFile);
        
        assertTrue("Schema应该是有效的", result.isValid());
        assertTrue("不应该有错误", result.getErrors().isEmpty());
        assertTrue("不应该有引用的namespace", result.getReferencedNamespaces().isEmpty());
    }
    
    @Test
    public void testSchemaWithCollectionTypes() throws Exception {
        // 测试Collection(Type)格式的引用
        String collectionXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Business.Entities\">\n" +
            "      <EntityType Name=\"Order\">\n" +
            "        <Property Name=\"Id\" Type=\"Edm.Guid\" Nullable=\"false\"/>\n" +
            "        <NavigationProperty Name=\"Items\" Type=\"Collection(Order.Items.OrderItem)\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Path testFile = testDir.resolve("collection-schema.xml");
        Files.write(testFile, collectionXml.getBytes());
        
        SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(testFile);
        
        assertFalse("Schema应该是无效的", result.isValid());
        assertTrue("应该检测到Order.Items引用", result.getReferencedNamespaces().contains("Order.Items"));
    }
    
    @Test
    public void testGetMissingReferences() throws Exception {
        // 测试getMissingReferences方法
        String xmlWithMissingRefs = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:Reference Uri=\"../core/CoreTypes.xml\">\n" +
            "    <edmx:Include Namespace=\"Core.Types\"/>\n" +
            "  </edmx:Reference>\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Test.Schema\">\n" +
            "      <EntityType Name=\"TestEntity\" BaseType=\"Core.Types.BaseEntity\">\n" +
            "        <Property Name=\"Address\" Type=\"Address.Types.AddressInfo\"/>\n" +
            "        <Property Name=\"Contact\" Type=\"Contact.Types.ContactInfo\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Path testFile = testDir.resolve("missing-refs-schema.xml");
        Files.write(testFile, xmlWithMissingRefs.getBytes());
        
        SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(testFile);
        
        Set<String> missingRefs = result.getMissingReferences();
        assertEquals("应该有2个缺失的引用", 2, missingRefs.size());
        assertTrue("应该缺失Address.Types", missingRefs.contains("Address.Types"));
        assertTrue("应该缺失Contact.Types", missingRefs.contains("Contact.Types"));
        assertFalse("Core.Types应该不缺失", missingRefs.contains("Core.Types"));
        assertFalse("Core.Types不应该缺失", missingRefs.contains("Core.Types"));
    }
}
