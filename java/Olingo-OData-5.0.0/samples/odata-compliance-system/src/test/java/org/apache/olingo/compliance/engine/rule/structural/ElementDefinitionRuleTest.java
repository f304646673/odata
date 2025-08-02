package org.apache.olingo.compliance.engine.rule.structural;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule.RuleResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for ElementDefinitionRule class.
 * Ensures 100% code coverage for all methods and branches.
 */
class ElementDefinitionRuleTest {

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
    private CsdlEnumType mockEnumType;
    
    @Mock
    private CsdlAction mockAction;
    
    @Mock
    private CsdlFunction mockFunction;
    
    @Mock
    private CsdlTerm mockTerm;
    
    @Mock
    private CsdlEntityContainer mockContainer;
    
    @Mock
    private CsdlEntitySet mockEntitySet;
    
    @Mock
    private CsdlProperty mockProperty;
    
    @Mock
    private CsdlNavigationProperty mockNavProperty;
    
    @Mock
    private CsdlParameter mockParameter;
    
    @Mock
    private CsdlReturnType mockReturnType;
    
    @Mock
    private CsdlActionImport mockActionImport;
    
    @Mock
    private CsdlFunctionImport mockFunctionImport;
    
    private ElementDefinitionRule rule;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rule = new ElementDefinitionRule();
        
        // Default setup for context
        when(mockContext.getCurrentSchemaNamespaces()).thenReturn(Collections.singleton("com.example"));
        when(mockContext.getImportedNamespaces()).thenReturn(new HashSet<>());
        when(mockContext.getDefinedTargets()).thenReturn(new HashSet<>());
    }
    
    @Test
    void testGetName() {
        assertEquals("element-definition", rule.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Validates element definitions and naming conventions", rule.getDescription());
    }
    
    @Test
    void testIsStructurallyApplicable_WithSchemas() {
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        assertTrue(rule.isStructurallyApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsStructurallyApplicable_NullSchemas() {
        when(mockContext.getAllSchemas()).thenReturn(null);
        
        assertFalse(rule.isStructurallyApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsStructurallyApplicable_EmptySchemas() {
        when(mockContext.getAllSchemas()).thenReturn(Collections.emptyList());
        
        assertFalse(rule.isStructurallyApplicable(mockContext, mockConfig));
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
    void testValidate_ValidSchema() {
        setupValidEntityType();
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidateEntityType_NullName() {
        when(mockEntityType.getName()).thenReturn(null);
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("EntityType must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateEntityType_EmptyName() {
        when(mockEntityType.getName()).thenReturn("  ");
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("EntityType must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateEntityType_InvalidName() {
        when(mockEntityType.getName()).thenReturn("123InvalidName");
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid EntityType name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateEntityType_DuplicateName() {
        CsdlEntityType mockEntityType2 = mock(CsdlEntityType.class);
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType2.getName()).thenReturn("TestEntity");
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType, mockEntityType2));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("duplicate element name: TestEntity", result.getMessage());
    }
    
    @Test
    void testValidateEntityType_DuplicatePropertyName() {
        CsdlProperty mockProperty2 = mock(CsdlProperty.class);
        setupValidEntityType();
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("Edm.String");
        when(mockProperty2.getName()).thenReturn("TestProperty");
        when(mockProperty2.getType()).thenReturn("Edm.Int32");
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty, mockProperty2));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Duplicate property name 'TestProperty' in entity type 'TestEntity'", result.getMessage());
    }
    
    @Test
    void testValidateComplexType_NullName() {
        when(mockComplexType.getName()).thenReturn(null);
        when(mockSchema.getComplexTypes()).thenReturn(Arrays.asList(mockComplexType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("ComplexType must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateComplexType_InvalidName() {
        when(mockComplexType.getName()).thenReturn("123InvalidName");
        when(mockSchema.getComplexTypes()).thenReturn(Arrays.asList(mockComplexType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid ComplexType name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateComplexType_DuplicatePropertyName() {
        CsdlProperty mockProperty2 = mock(CsdlProperty.class);
        when(mockComplexType.getName()).thenReturn("TestComplexType");
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("Edm.String");
        when(mockProperty2.getName()).thenReturn("TestProperty");
        when(mockProperty2.getType()).thenReturn("Edm.Int32");
        when(mockComplexType.getProperties()).thenReturn(Arrays.asList(mockProperty, mockProperty2));
        when(mockSchema.getComplexTypes()).thenReturn(Arrays.asList(mockComplexType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Duplicate property name 'TestProperty' in complex type 'TestComplexType'", result.getMessage());
    }
    
    @Test
    void testValidateEnumType_NullName() {
        when(mockEnumType.getName()).thenReturn(null);
        when(mockSchema.getEnumTypes()).thenReturn(Arrays.asList(mockEnumType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("EnumType must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateEnumType_InvalidName() {
        when(mockEnumType.getName()).thenReturn("123InvalidName");
        when(mockSchema.getEnumTypes()).thenReturn(Arrays.asList(mockEnumType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid EnumType name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateEnumType_Valid() {
        when(mockEnumType.getName()).thenReturn("TestEnumType");
        when(mockSchema.getEnumTypes()).thenReturn(Arrays.asList(mockEnumType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidateAction_NullName() {
        when(mockAction.getName()).thenReturn(null);
        when(mockSchema.getActions()).thenReturn(Arrays.asList(mockAction));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Action must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateAction_InvalidName() {
        when(mockAction.getName()).thenReturn("123InvalidName");
        when(mockSchema.getActions()).thenReturn(Arrays.asList(mockAction));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid Action name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateAction_WithValidReturnType() {
        when(mockAction.getName()).thenReturn("TestAction");
        when(mockAction.getReturnType()).thenReturn(mockReturnType);
        when(mockReturnType.getType()).thenReturn("Edm.String");
        when(mockSchema.getActions()).thenReturn(Arrays.asList(mockAction));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidateAction_WithParameters() {
        when(mockAction.getName()).thenReturn("TestAction");
        when(mockParameter.getType()).thenReturn("Edm.String");
        when(mockAction.getParameters()).thenReturn(Arrays.asList(mockParameter));
        when(mockSchema.getActions()).thenReturn(Arrays.asList(mockAction));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidateFunction_NullName() {
        when(mockFunction.getName()).thenReturn(null);
        when(mockSchema.getFunctions()).thenReturn(Arrays.asList(mockFunction));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Function must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateFunction_InvalidName() {
        when(mockFunction.getName()).thenReturn("123InvalidName");
        when(mockSchema.getFunctions()).thenReturn(Arrays.asList(mockFunction));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid Function name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateFunction_DuplicateSignature() {
        CsdlFunction mockFunction2 = mock(CsdlFunction.class);
        when(mockFunction.getName()).thenReturn("TestFunction");
        when(mockFunction.getParameters()).thenReturn(Arrays.asList(mockParameter));
        when(mockParameter.getType()).thenReturn("Edm.String");
        when(mockFunction.getReturnType()).thenReturn(mockReturnType);
        when(mockReturnType.getType()).thenReturn("Edm.String");
        
        when(mockFunction2.getName()).thenReturn("TestFunction");
        when(mockFunction2.getParameters()).thenReturn(Arrays.asList(mockParameter));
        when(mockFunction2.getReturnType()).thenReturn(mockReturnType);
        
        when(mockSchema.getFunctions()).thenReturn(Arrays.asList(mockFunction, mockFunction2));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("duplicate function signature: TestFunction", result.getMessage());
    }
    
    @Test
    void testValidateTerm_NullName() {
        when(mockTerm.getName()).thenReturn(null);
        when(mockSchema.getTerms()).thenReturn(Arrays.asList(mockTerm));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Term must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateTerm_InvalidName() {
        when(mockTerm.getName()).thenReturn("123InvalidName");
        when(mockSchema.getTerms()).thenReturn(Arrays.asList(mockTerm));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid Term name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateTerm_NullType() {
        when(mockTerm.getName()).thenReturn("TestTerm");
        when(mockTerm.getType()).thenReturn(null);
        when(mockSchema.getTerms()).thenReturn(Arrays.asList(mockTerm));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Term 'TestTerm' must have a Type attribute", result.getMessage());
    }
    
    @Test
    void testValidateTerm_EmptyType() {
        when(mockTerm.getName()).thenReturn("TestTerm");
        when(mockTerm.getType()).thenReturn("  ");
        when(mockSchema.getTerms()).thenReturn(Arrays.asList(mockTerm));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Term 'TestTerm' must have a Type attribute", result.getMessage());
    }
    
    @Test
    void testValidateEntityContainer_NullName() {
        when(mockContainer.getName()).thenReturn(null);
        when(mockSchema.getEntityContainer()).thenReturn(mockContainer);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("EntityContainer must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateEntityContainer_InvalidName() {
        when(mockContainer.getName()).thenReturn("123InvalidName");
        when(mockSchema.getEntityContainer()).thenReturn(mockContainer);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid EntityContainer name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateEntityContainer_DuplicateActionImports() {
        CsdlActionImport mockActionImport2 = mock(CsdlActionImport.class);
        when(mockContainer.getName()).thenReturn("TestContainer");
        when(mockActionImport.getName()).thenReturn("TestActionImport");
        when(mockActionImport2.getName()).thenReturn("TestActionImport");
        when(mockContainer.getActionImports()).thenReturn(Arrays.asList(mockActionImport, mockActionImport2));
        when(mockSchema.getEntityContainer()).thenReturn(mockContainer);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Duplicate ActionImport name 'TestActionImport' in entity container 'TestContainer'", result.getMessage());
    }
    
    @Test
    void testValidateEntityContainer_DuplicateFunctionImports() {
        CsdlFunctionImport mockFunctionImport2 = mock(CsdlFunctionImport.class);
        when(mockContainer.getName()).thenReturn("TestContainer");
        when(mockFunctionImport.getName()).thenReturn("TestFunctionImport");
        when(mockFunctionImport2.getName()).thenReturn("TestFunctionImport");
        when(mockContainer.getFunctionImports()).thenReturn(Arrays.asList(mockFunctionImport, mockFunctionImport2));
        when(mockSchema.getEntityContainer()).thenReturn(mockContainer);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Duplicate FunctionImport name 'TestFunctionImport' in entity container 'TestContainer'", result.getMessage());
    }
    
    @Test
    void testValidateEntitySet_NullName() {
        when(mockContainer.getName()).thenReturn("TestContainer");
        when(mockEntitySet.getName()).thenReturn(null);
        when(mockContainer.getEntitySets()).thenReturn(Arrays.asList(mockEntitySet));
        when(mockSchema.getEntityContainer()).thenReturn(mockContainer);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("EntitySet must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateEntitySet_InvalidName() {
        when(mockContainer.getName()).thenReturn("TestContainer");
        when(mockEntitySet.getName()).thenReturn("123InvalidName");
        when(mockContainer.getEntitySets()).thenReturn(Arrays.asList(mockEntitySet));
        when(mockSchema.getEntityContainer()).thenReturn(mockContainer);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid EntitySet name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateProperty_NullName() {
        setupValidEntityType();
        when(mockProperty.getName()).thenReturn(null);
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Property must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateProperty_InvalidName() {
        setupValidEntityType();
        when(mockProperty.getName()).thenReturn("123InvalidName");
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid Property name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateNavigationProperty_NullName() {
        setupValidEntityType();
        when(mockNavProperty.getName()).thenReturn(null);
        when(mockEntityType.getNavigationProperties()).thenReturn(Arrays.asList(mockNavProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("NavigationProperty must have a valid name", result.getMessage());
    }
    
    @Test
    void testValidateNavigationProperty_InvalidName() {
        setupValidEntityType();
        when(mockNavProperty.getName()).thenReturn("123InvalidName");
        when(mockEntityType.getNavigationProperties()).thenReturn(Arrays.asList(mockNavProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Invalid NavigationProperty name: 123InvalidName", result.getMessage());
    }
    
    @Test
    void testValidateTypeReference_UnknownNamespace() {
        setupValidEntityType();
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("Unknown.Type");
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Referenced type namespace not imported: Unknown", result.getMessage());
    }
    
    @Test
    void testValidateTypeReference_InvalidType() {
        setupValidEntityType();
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("com.example.NonExistent");
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        Set<String> definedTargets = new HashSet<>();
        when(mockContext.getDefinedTargets()).thenReturn(definedTargets);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Referenced type does not exist: com.example.NonExistent", result.getMessage());
    }
    
    @Test
    void testValidateTypeReference_UndefinedType() {
        setupValidEntityType();
        when(mockProperty.getName()).thenReturn("TestProperty");
        when(mockProperty.getType()).thenReturn("com.example.UndefinedType");
        when(mockEntityType.getProperties()).thenReturn(Arrays.asList(mockProperty));
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        Set<String> definedTargets = new HashSet<>();
        when(mockContext.getDefinedTargets()).thenReturn(definedTargets);
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Referenced type does not exist: com.example.UndefinedType", result.getMessage());
    }
    
    @Test
    void testGetEstimatedExecutionTime() {
        assertEquals(400, rule.getEstimatedExecutionTime());
    }
    
    @Test
    void testValidateEntityTypeBaseType_WrongKind() {
        when(mockEntityType.getName()).thenReturn("TestEntity");
        when(mockEntityType.getBaseType()).thenReturn("com.example.TestComplexType");
        when(mockSchema.getEntityTypes()).thenReturn(Arrays.asList(mockEntityType));
        
        Set<String> definedTargets = new HashSet<>();
        definedTargets.add("com.example.TestComplexType");
        when(mockContext.getDefinedTargets()).thenReturn(definedTargets);
        when(mockContext.getTypeKind("com.example.TestComplexType")).thenReturn("ComplexType");
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("EntityType BaseType must reference another EntityType"));
    }
    
    @Test
    void testValidateComplexTypeBaseType_WrongKind() {
        when(mockComplexType.getName()).thenReturn("TestComplexType");
        when(mockComplexType.getBaseType()).thenReturn("com.example.TestEntityType");
        when(mockSchema.getComplexTypes()).thenReturn(Arrays.asList(mockComplexType));
        
        Set<String> definedTargets = new HashSet<>();
        definedTargets.add("com.example.TestEntityType");
        when(mockContext.getDefinedTargets()).thenReturn(definedTargets);
        when(mockContext.getTypeKind("com.example.TestEntityType")).thenReturn("EntityType");
        
        List<CsdlSchema> schemas = Arrays.asList(mockSchema);
        when(mockContext.getAllSchemas()).thenReturn(schemas);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("ComplexType BaseType must reference another ComplexType"));
    }
    
    private void setupValidEntityType() {
        when(mockEntityType.getName()).thenReturn("TestEntity");
    }
}
