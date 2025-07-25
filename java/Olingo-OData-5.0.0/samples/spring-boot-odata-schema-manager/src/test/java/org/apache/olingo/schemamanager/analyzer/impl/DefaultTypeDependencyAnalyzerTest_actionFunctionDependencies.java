package org.apache.olingo.schemamanager.analyzer.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultTypeDependencyAnalyzerTest_actionFunctionDependencies {
    @Mock
    private SchemaRepository repository;
    private DefaultTypeDependencyAnalyzer analyzer;
    private CsdlSchema testSchema;
    private CsdlAction testAction;
    private CsdlFunction testFunction;
    
    @BeforeEach
    void setUp() throws Exception {
        analyzer = new DefaultTypeDependencyAnalyzer(repository);
        
        // Create test schema with actions and functions
        testSchema = createActionFunctionSchema();
        setupMockRepository();
    }
    
    private CsdlSchema createActionFunctionSchema() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestService");
        
        // Customer EntityType
        CsdlEntityType customerEntity = new CsdlEntityType();
        customerEntity.setName("Customer");
        CsdlProperty customerProp = new CsdlProperty();
        customerProp.setName("Id");
        customerProp.setType("Edm.String");
        customerEntity.setProperties(Arrays.asList(customerProp));
        
        // Address ComplexType
        CsdlComplexType addressComplex = new CsdlComplexType();
        addressComplex.setName("Address");
        CsdlProperty addressProp = new CsdlProperty();
        addressProp.setName("Street");
        addressProp.setType("Edm.String");
        addressComplex.setProperties(Arrays.asList(addressProp));
        
        // Status EnumType
        CsdlEnumType statusEnum = new CsdlEnumType();
        statusEnum.setName("Status");
        
        schema.setEntityTypes(Arrays.asList(customerEntity));
        schema.setComplexTypes(Arrays.asList(addressComplex));
        schema.setEnumTypes(Arrays.asList(statusEnum));
        
        // Action with parameters referencing other types
        testAction = new CsdlAction();
        testAction.setName("DoSomething");
        CsdlParameter customerParam = new CsdlParameter();
        customerParam.setName("customer");
        customerParam.setType("TestService.Customer");
        CsdlParameter addressParam = new CsdlParameter();
        addressParam.setName("address");
        addressParam.setType("TestService.Address");
        testAction.setParameters(Arrays.asList(customerParam, addressParam));
        
        CsdlReturnType actionReturn = new CsdlReturnType();
        actionReturn.setType("TestService.Status");
        testAction.setReturnType(actionReturn);
        
        // Function with parameter and return type
        testFunction = new CsdlFunction();
        testFunction.setName("CalculateSomething");
        CsdlParameter funcParam = new CsdlParameter();
        funcParam.setName("address");
        funcParam.setType("TestService.Address");
        testFunction.setParameters(Arrays.asList(funcParam));
        
        CsdlReturnType funcReturn = new CsdlReturnType();
        funcReturn.setType("TestService.Customer");
        testFunction.setReturnType(funcReturn);
        
        schema.setActions(Arrays.asList(testAction));
        schema.setFunctions(Arrays.asList(testFunction));
        
        return schema;
    }
    
    private void setupMockRepository() {
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", testSchema);
        
        when(repository.getSchema("TestService")).thenReturn(testSchema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        // Mock entity types
        when(repository.getEntityType("TestService.Customer"))
            .thenReturn(testSchema.getEntityType("Customer"));
        
        // Mock complex types
        when(repository.getComplexType("TestService.Address"))
            .thenReturn(testSchema.getComplexType("Address"));
        
        // Mock actions and functions
        when(repository.getAction("TestService.DoSomething"))
            .thenReturn(testAction);
        when(repository.getFunction("TestService.CalculateSomething"))
            .thenReturn(testFunction);
    }
    
    @Test
    void testActionDependencyAnalysis() {
        // Test Action dependencies
        // Action: DoSomething(customer: Customer, address: Address) -> Status
        List<TypeDependencyAnalyzer.TypeReference> deps = analyzer.getDirectDependencies(testAction);
        
        // Should have dependencies on Customer, Address, and Status
        assertEquals(3, deps.size());
        
        List<String> dependencyNames = deps.stream()
            .map(TypeDependencyAnalyzer.TypeReference::getFullQualifiedName)
            .collect(java.util.stream.Collectors.toList());
        
        assertTrue(dependencyNames.contains("TestService.Customer"));
        assertTrue(dependencyNames.contains("TestService.Address"));
        assertTrue(dependencyNames.contains("TestService.Status"));
    }
    
    @Test
    void testFunctionDependencyAnalysis() {
        // Test Function dependencies 
        // Function: CalculateSomething(address: Address) -> Customer
        List<TypeDependencyAnalyzer.TypeReference> deps = analyzer.getDirectDependencies(testFunction);
        
        // Should have dependencies on Address and Customer
        assertEquals(2, deps.size());
        
        List<String> dependencyNames = deps.stream()
            .map(TypeDependencyAnalyzer.TypeReference::getFullQualifiedName)
            .collect(java.util.stream.Collectors.toList());
        
        assertTrue(dependencyNames.contains("TestService.Address"));
        assertTrue(dependencyNames.contains("TestService.Customer"));
    }
    
    @Test
    void testActionWithComplexParameters() {
        // Test action with multiple complex parameter structures
        List<TypeDependencyAnalyzer.TypeReference> deps = analyzer.getDirectDependencies(testAction);
        
        // Verify all parameter types are captured as dependencies
        assertFalse(deps.isEmpty());
        assertTrue(deps.stream().anyMatch(dep -> 
            dep.getFullQualifiedName().equals("TestService.Customer")));
        assertTrue(deps.stream().anyMatch(dep -> 
            dep.getFullQualifiedName().equals("TestService.Address")));
    }
    
    @Test
    void testFunctionWithCollectionReturnType() {
        // Create a function with collection return type for testing
        CsdlFunction collectionFunction = new CsdlFunction();
        collectionFunction.setName("GetMultipleItems");
        
        CsdlReturnType collectionReturn = new CsdlReturnType();
        collectionReturn.setType("Collection(TestService.Customer)");
        collectionFunction.setReturnType(collectionReturn);
        
        List<TypeDependencyAnalyzer.TypeReference> deps = analyzer.getDirectDependencies(collectionFunction);
        
        // Should have dependency on Customer type (from collection)
        assertEquals(1, deps.size());
        assertEquals("TestService.Customer", deps.get(0).getFullQualifiedName());
    }
}
