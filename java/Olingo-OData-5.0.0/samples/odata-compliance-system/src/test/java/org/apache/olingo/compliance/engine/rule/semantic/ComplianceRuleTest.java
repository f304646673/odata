package org.apache.olingo.compliance.engine.rule.semantic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule.RuleResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for ComplianceRule class.
 * Ensures 100% code coverage for all methods and branches.
 */
class ComplianceRuleTest {

    @Mock
    private ValidationContext mockContext;
    
    @Mock
    private ValidationConfig mockConfig;
    
    @Mock
    private CsdlSchema mockSchema;
    
    @Mock
    private CsdlEntityType mockEntityType;
    
    @Mock
    private CsdlComplexType mockComplexType;
    
    @Mock
    private CsdlProperty mockProperty;
    
    @Mock
    private CsdlNavigationProperty mockNavProperty;
    
    @Mock
    private CsdlPropertyRef mockPropertyRef;
    
    private ComplianceRule rule;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rule = new ComplianceRule();
    }
    
    @Test
    void testGetName() {
        assertEquals("odata-compliance", rule.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Validates OData compliance requirements", rule.getDescription());
    }
    
    @Test
    void testIsSemanticApplicable_WithSchemas() {
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        assertTrue(rule.isSemanticApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsSemanticApplicable_WithoutSchemas() {
        when(mockContext.getAllSchemas()).thenReturn(null);
        
        assertFalse(rule.isSemanticApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsSemanticApplicable_EmptySchemas() {
        when(mockContext.getAllSchemas()).thenReturn(Collections.emptyList());
        
        assertFalse(rule.isSemanticApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testValidate_NullSchemas() {
        when(mockContext.getAllSchemas()).thenReturn(null);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_EmptySchemas() {
        when(mockContext.getAllSchemas()).thenReturn(Collections.emptyList());
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_NullSchema() {
        List<CsdlSchema> schemas = Arrays.asList((CsdlSchema) null);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Schema cannot be null", result.getMessage());
    }
    
    @Test
    void testValidate_SchemaWithoutNamespace() {
        when(mockSchema.getNamespace()).thenReturn(null);
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Schema must have a valid namespace", result.getMessage());
    }
    
    @Test
    void testValidate_SchemaWithEmptyNamespace() {
        when(mockSchema.getNamespace()).thenReturn("  ");
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Schema must have a valid namespace", result.getMessage());
    }
    
    @Test
    void testValidate_EntityTypeWithoutName() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn(null);
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Entity type must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidate_EntityTypeWithEmptyName() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("  ");
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Entity type must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidate_EntityTypeWithoutKey() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(null);
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Entity type 'TestEntity' must have a key", result.getMessage());
    }
    
    @Test
    void testValidate_EntityTypeWithEmptyKey() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Collections.emptyList());
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Entity type 'TestEntity' must have a key", result.getMessage());
    }
    
    @Test
    void testValidate_PropertyWithoutName() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn(null);
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Property in type 'TestEntity' must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidate_PropertyWithoutType() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn(null);
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Property 'TestProperty' in type 'TestEntity' must have a valid type", result.getMessage());
    }
    
    @Test
    void testValidate_PropertyWithInvalidEdmType() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("Edm.InvalidType");
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Property 'TestProperty' in type 'TestEntity' has invalid EDM type 'Edm.InvalidType'", result.getMessage());
    }
    
    @Test
    void testValidate_PropertyWithInvalidCollectionType() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("Collection(Edm.InvalidType)");
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Property 'TestProperty' in type 'TestEntity' has invalid collection EDM type 'Edm.InvalidType'", result.getMessage());
    }
    
    @Test
    void testValidate_ValidEntityType() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("Edm.String");
        // Mock valid maxLength for string properties
        when(mockProperty.getMaxLength()).thenReturn(100);
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        // Don't return navigation properties to keep test simple
        when(mockEntityType.getNavigationProperties()).thenReturn(null);
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        if (!result.isPassed()) {
            System.out.println("FAILURE MESSAGE: " + result.getMessage());
        }
        assertTrue(result.isPassed(), "Expected validation to pass, but got: " + result.getMessage());
    }
    
    @Test
    void testValidate_NavigationPropertyWithoutName() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockNavProperty.getName()).thenReturn(null);
        when(mockEntityType.getNavigationProperties()).thenReturn(Arrays.asList(mockNavProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Navigation property in type 'TestEntity' must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidate_NavigationPropertyWithoutType() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockNavProperty.getName()).thenReturn("NavProperty");
        when(mockNavProperty.getType()).thenReturn(null);
        when(mockEntityType.getNavigationProperties()).thenReturn(Arrays.asList(mockNavProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Navigation property 'NavProperty' in type 'TestEntity' must have a valid type", result.getMessage());
    }
    
    @Test
    void testValidate_ComplexTypeProperty() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockComplexType.getName()).thenReturn("TestComplexType");
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("Edm.String");
        // Mock valid maxLength for string properties
        when(mockProperty.getMaxLength()).thenReturn(100);
        when(mockComplexType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getComplexTypes()).thenReturn(Arrays.asList(mockComplexType));
        // Ensure entityTypes returns empty list instead of null
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList());
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed(), "Expected validation to pass, but got: " + result.getMessage());
    }
    
    @Test
    void testValidate_DecimalPropertyConstraints() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn("DecimalProperty");
        when(mockProperty.getType()).thenReturn("Edm.Decimal");
        when(mockProperty.getPrecision()).thenReturn(0); // Invalid precision
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("invalid precision"));
    }
    
    @Test
    void testValidate_DecimalPropertyScaleGreaterThanPrecision() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn("DecimalProperty");
        when(mockProperty.getType()).thenReturn("Edm.Decimal");
        when(mockProperty.getPrecision()).thenReturn(5);
        when(mockProperty.getScale()).thenReturn(10); // Scale > precision
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("scale 10 greater than precision 5"));
    }
    
    @Test
    void testValidate_StringPropertyInvalidMaxLength() {
        when(mockSchema.getNamespace()).thenReturn("com.example");
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getKey()).thenReturn(Arrays.asList(mockPropertyRef));
        when(mockProperty.getName()).thenReturn("StringProperty");
        when(mockProperty.getType()).thenReturn("Edm.String");
        when(mockProperty.getMaxLength()).thenReturn(0); // Invalid maxLength
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("invalid maxLength"));
    }
}
