package org.apache.olingo.compliance.engine.rule.crossfile;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 跨文件引用验证规则的单元测试
 * 验证SchemaRegistry机制的正确性
 */
class CrossFileReferenceValidationRuleTest {
    
    @TempDir
    java.nio.file.Path tempDir;
    
    private CrossFileReferenceValidationRule rule;
    private ValidationContext mockContext;
    private ValidationConfig mockConfig;
    private SchemaRegistry mockRegistry;
    
    @BeforeEach
    void setUp() {
        rule = new CrossFileReferenceValidationRule();
        mockContext = mock(ValidationContext.class);
        mockConfig = mock(ValidationConfig.class);
        mockRegistry = mock(SchemaRegistry.class);
        
        when(mockContext.getSchemaRegistry()).thenReturn(mockRegistry);
        when(mockContext.getFilePath()).thenReturn(tempDir.resolve("test.xml"));
    }
    
    @Test
    void testValidate_NoSchemaRegistry() {
        when(mockContext.getSchemaRegistry()).thenReturn(null);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("SchemaRegistry is required"));
    }
    
    @Test
    void testValidate_NoXmlContent() {
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(null);
        when(mockContext.getAllSchemas()).thenReturn(null);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed()); // 没有内容可验证，跳过
    }
    
    @Test
    void testValidate_ValidHttpReference() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"http://example.com/schema.xml\">\n" +
                           "    <edmx:Include Namespace=\"External\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockRegistry.hasNamespace("External")).thenReturn(true);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_InvalidLocalReference() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"missing.xml\">\n" +
                           "    <edmx:Include Namespace=\"Missing\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockRegistry.hasSchemaForFile("missing.xml")).thenReturn(false);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("missing.xml"));
        assertTrue(result.getMessage().contains("not registered"));
    }
    
    @Test
    void testValidate_ValidLocalReference() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"valid.xml\">\n" +
                           "    <edmx:Include Namespace=\"Valid\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockRegistry.hasSchemaForFile("valid.xml")).thenReturn(true);
        when(mockRegistry.hasNamespace("Valid")).thenReturn(true);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_MissingIncludedNamespace() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"http://example.com/schema.xml\">\n" +
                           "    <edmx:Include Namespace=\"Missing\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockRegistry.hasNamespace("Missing")).thenReturn(false);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Missing"));
        assertTrue(result.getMessage().contains("not found"));
    }
    
    @Test
    void testValidate_ValidTypeReferences() {
        // 创建模拟的Schema和类型
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("CurrentNamespace");
        
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TestEntity");
        entityType.setBaseType("External.BaseEntity");
        
        CsdlProperty property = new CsdlProperty();
        property.setName("TestProperty");
        property.setType("External.ComplexType");
        entityType.setProperties(Arrays.asList(property));
        
        schema.setEntityTypes(Arrays.asList(entityType));
        
        when(mockContext.getAllSchemas()).thenReturn(Arrays.asList(schema));
        when(mockRegistry.isTypeExists("External.BaseEntity")).thenReturn(true);
        when(mockRegistry.isTypeExists("External.ComplexType")).thenReturn(true);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_InvalidTypeReferences() {
        // 创建模拟的Schema和类型
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("CurrentNamespace");
        
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("TestComplex");
        complexType.setBaseType("Missing.BaseComplex");
        
        schema.setComplexTypes(Arrays.asList(complexType));
        
        when(mockContext.getAllSchemas()).thenReturn(Arrays.asList(schema));
        when(mockRegistry.isTypeExists("Missing.BaseComplex")).thenReturn(false);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Missing.BaseComplex"));
        assertTrue(result.getMessage().contains("undefined type"));
    }
    
    @Test
    void testValidate_SkipEdmNamespace() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"http://example.com/schema.xml\">\n" +
                           "    <edmx:Include Namespace=\"Edm\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        // 不设置hasNamespace("Edm")的返回值，应该被跳过
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed()); // EDM命名空间应该被跳过
    }
    
    @Test
    void testValidate_SkipCurrentNamespaceTypes() {
        // 创建模拟的Schema和类型
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("CurrentNamespace");
        
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TestEntity");
        entityType.setBaseType("CurrentNamespace.BaseEntity"); // 引用同一命名空间的类型
        
        schema.setEntityTypes(Arrays.asList(entityType));
        
        when(mockContext.getAllSchemas()).thenReturn(Arrays.asList(schema));
        // 不设置isTypeExists的返回值，因为应该跳过当前命名空间的类型
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed()); // 当前命名空间的类型应该被跳过
    }
    
    @Test
    void testValidate_CollectionTypeHandling() {
        // 创建模拟的Schema和类型
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("CurrentNamespace");
        
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TestEntity");
        
        CsdlProperty property = new CsdlProperty();
        property.setName("TestProperty");
        property.setType("Collection(External.ComplexType)"); // 集合类型
        entityType.setProperties(Arrays.asList(property));
        
        schema.setEntityTypes(Arrays.asList(entityType));
        
        when(mockContext.getAllSchemas()).thenReturn(Arrays.asList(schema));
        when(mockRegistry.isTypeExists("External.ComplexType")).thenReturn(true);
        
        ValidationRule.RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
}
