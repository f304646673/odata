package org.apache.olingo.schema.processor.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ODataValidator的单元测试
 * 测试OData 4.0规范验证功能
 */
public class ODataValidatorTest {
    
    private ODataValidator validator;
    
    @Before
    public void setUp() {
        validator = new ODataValidator();
    }
    
    @Test
    public void testValidateSchema_ValidSchema() {
        CsdlSchema schema = createValidSchema();
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertTrue("Schema should be valid", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
        assertNotNull("Dependencies should not be null", result.getDependencies());
    }
    
    @Test
    public void testValidateSchema_NullSchema() {
        ODataValidator.ValidationResult result = validator.validateSchema(null);
        
        assertFalse("Null schema should be invalid", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Error should mention null schema", 
                  result.getErrors().get(0).toLowerCase().contains("null"));
    }
    
    @Test
    public void testValidateSchema_NullNamespace() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn(null);
        when(schema.getEntityTypes()).thenReturn(Collections.emptyList());
        when(schema.getEntityContainer()).thenReturn(null);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertFalse("Schema with null namespace should be invalid", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Error should mention namespace", 
                  result.getErrors().get(0).toLowerCase().contains("namespace"));
    }
    
    @Test
    public void testValidateSchema_EmptyNamespace() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("   ");
        when(schema.getEntityTypes()).thenReturn(Collections.emptyList());
        when(schema.getEntityContainer()).thenReturn(null);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertFalse("Schema with empty namespace should be invalid", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Error should mention namespace", 
                  result.getErrors().get(0).toLowerCase().contains("namespace"));
    }
    
    @Test
    public void testValidateSchema_InvalidNamespace() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("invalid namespace with spaces");
        when(schema.getEntityTypes()).thenReturn(Collections.emptyList());
        when(schema.getEntityContainer()).thenReturn(null);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertFalse("Schema with invalid namespace should be invalid", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Error should mention namespace format", 
                  result.getErrors().get(0).toLowerCase().contains("namespace"));
    }
    
    @Test
    public void testValidateSchema_WithEntityTypes() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("com.example.test");
        
        CsdlEntityType entityType = createValidEntityType();
        when(schema.getEntityTypes()).thenReturn(Arrays.asList(entityType));
        when(schema.getEntityContainer()).thenReturn(null);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertTrue("Schema with valid entity types should be valid", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchema_WithInvalidEntityType() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("com.example.test");
        
        CsdlEntityType invalidEntityType = mock(CsdlEntityType.class);
        when(invalidEntityType.getName()).thenReturn(null); // Invalid: null name
        when(invalidEntityType.getProperties()).thenReturn(Collections.emptyList());
        when(invalidEntityType.getKey()).thenReturn(Collections.emptyList());
        
        when(schema.getEntityTypes()).thenReturn(Arrays.asList(invalidEntityType));
        when(schema.getEntityContainer()).thenReturn(null);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertFalse("Schema with invalid entity type should be invalid", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchema_WithEntityContainer() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("com.example.test");
        when(schema.getEntityTypes()).thenReturn(Collections.emptyList());
        
        CsdlEntityContainer container = mock(CsdlEntityContainer.class);
        when(container.getName()).thenReturn("TestContainer");
        when(container.getEntitySets()).thenReturn(Collections.emptyList());
        when(schema.getEntityContainer()).thenReturn(container);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertTrue("Schema with valid entity container should be valid", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchema_WithInvalidEntityContainer() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("com.example.test");
        when(schema.getEntityTypes()).thenReturn(Collections.emptyList());
        
        CsdlEntityContainer invalidContainer = mock(CsdlEntityContainer.class);
        when(invalidContainer.getName()).thenReturn(null); // Invalid: null name
        when(invalidContainer.getEntitySets()).thenReturn(Collections.emptyList());
        when(schema.getEntityContainer()).thenReturn(invalidContainer);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertFalse("Schema with invalid entity container should be invalid", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchema_DependencyDetection() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("com.example.test");
        when(schema.getEntityTypes()).thenReturn(Collections.emptyList());
        when(schema.getEntityContainer()).thenReturn(null);
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertNotNull("Dependencies should not be null", result.getDependencies());
        // 注：依赖检测的具体逻辑需要根据实际的Schema内容来测试
    }
    
    @Test
    public void testValidationResult_DefaultConstructor() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        ODataValidator.ValidationResult result = new ODataValidator.ValidationResult(true, errors, warnings, dependencies);
        
        assertTrue("Default result should be valid", result.isValid());
        assertTrue("Default result should have no errors", result.getErrors().isEmpty());
        assertTrue("Default result should have no warnings", result.getWarnings().isEmpty());
        assertTrue("Default result should have no dependencies", result.getDependencies().isEmpty());
    }
    
    @Test
    public void testValidationResult_WithErrorsAndWarnings() {
        List<String> errors = new ArrayList<>();
        errors.add("Test error");
        List<String> warnings = new ArrayList<>();
        warnings.add("Test warning");
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.example.dependency");
        
        ODataValidator.ValidationResult result = new ODataValidator.ValidationResult(false, errors, warnings, dependencies);
        
        assertFalse("Result with errors should be invalid", result.isValid());
        assertEquals("Should have 1 error", 1, result.getErrors().size());
        assertEquals("Should have 1 warning", 1, result.getWarnings().size());
        assertEquals("Should have 1 dependency", 1, result.getDependencies().size());
        
        assertEquals("Error should match", "Test error", result.getErrors().get(0));
        assertEquals("Warning should match", "Test warning", result.getWarnings().get(0));
        assertTrue("Dependencies should contain test dependency", 
                  result.getDependencies().contains("com.example.dependency"));
    }
    
    // 辅助方法
    private CsdlSchema createValidSchema() {
        CsdlSchema schema = mock(CsdlSchema.class);
        when(schema.getNamespace()).thenReturn("com.example.test");
        when(schema.getEntityTypes()).thenReturn(Collections.emptyList());
        when(schema.getEntityContainer()).thenReturn(null);
        return schema;
    }
    
    private CsdlEntityType createValidEntityType() {
        CsdlEntityType entityType = mock(CsdlEntityType.class);
        when(entityType.getName()).thenReturn("TestEntity");
        
        CsdlProperty property = mock(CsdlProperty.class);
        when(property.getName()).thenReturn("Id");
        when(property.getType()).thenReturn("Edm.String");
        
        when(entityType.getProperties()).thenReturn(Arrays.asList(property));
        when(entityType.getKey()).thenReturn(Collections.emptyList());
        
        return entityType;
    }
}
