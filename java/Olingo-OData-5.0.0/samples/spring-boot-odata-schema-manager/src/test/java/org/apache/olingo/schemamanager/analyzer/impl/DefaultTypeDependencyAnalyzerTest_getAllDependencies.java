package org.apache.olingo.schemamanager.analyzer.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultTypeDependencyAnalyzerTest_getAllDependencies {
    @Mock
    private SchemaRepository repository;
    private DefaultTypeDependencyAnalyzer analyzer;
    private CsdlSchema testSchema;
    
    @BeforeEach
    void setUp() throws Exception {
        analyzer = new DefaultTypeDependencyAnalyzer(repository);
        
        // Create test schema with deep dependency chain
        testSchema = createDeepDependencySchema();
        setupMockRepository();
    }
    
    private CsdlSchema createDeepDependencySchema() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestService");
        
        // Create a chain of dependencies: TypeA -> TypeB -> TypeC -> TypeD
        // TypeD (leaf type, no dependencies)
        CsdlComplexType typeD = new CsdlComplexType();
        typeD.setName("TypeD");
        CsdlProperty propD1 = new CsdlProperty();
        propD1.setName("Id");
        propD1.setType("Edm.String");
        CsdlProperty propD2 = new CsdlProperty();
        propD2.setName("Value");
        propD2.setType("Edm.String");
        typeD.setProperties(Arrays.asList(propD1, propD2));
        
        // TypeC -> depends on TypeD
        CsdlComplexType typeC = new CsdlComplexType();
        typeC.setName("TypeC");
        CsdlProperty propC1 = new CsdlProperty();
        propC1.setName("Id");
        propC1.setType("Edm.String");
        CsdlProperty propC2 = new CsdlProperty();
        propC2.setName("TypeDRef");
        propC2.setType("TestService.TypeD");
        typeC.setProperties(Arrays.asList(propC1, propC2));
        
        // TypeB -> depends on TypeC
        CsdlComplexType typeB = new CsdlComplexType();
        typeB.setName("TypeB");
        CsdlProperty propB1 = new CsdlProperty();
        propB1.setName("Id");
        propB1.setType("Edm.String");
        CsdlProperty propB2 = new CsdlProperty();
        propB2.setName("TypeCRef");
        propB2.setType("TestService.TypeC");
        typeB.setProperties(Arrays.asList(propB1, propB2));
        
        // TypeA -> depends on TypeB
        CsdlComplexType typeA = new CsdlComplexType();
        typeA.setName("TypeA");
        CsdlProperty propA1 = new CsdlProperty();
        propA1.setName("Id");
        propA1.setType("Edm.String");
        CsdlProperty propA2 = new CsdlProperty();
        propA2.setName("TypeBRef");
        propA2.setType("TestService.TypeB");
        typeA.setProperties(Arrays.asList(propA1, propA2));
        
        schema.setComplexTypes(Arrays.asList(typeA, typeB, typeC, typeD));
        
        // RootEntity -> depends on TypeA (creates the full chain)
        CsdlEntityType rootEntity = new CsdlEntityType();
        rootEntity.setName("RootEntity");
        CsdlProperty rootProp1 = new CsdlProperty();
        rootProp1.setName("Id");
        rootProp1.setType("Edm.String");
        CsdlProperty rootProp2 = new CsdlProperty();
        rootProp2.setName("TypeARef");
        rootProp2.setType("TestService.TypeA");
        CsdlProperty rootProp3 = new CsdlProperty();
        rootProp3.setName("Status");
        rootProp3.setType("TestService.Status");  // Also depends on enum
        rootEntity.setProperties(Arrays.asList(rootProp1, rootProp2, rootProp3));
        
        schema.setEntityTypes(Arrays.asList(rootEntity));
        
        // Status enum (no dependencies)
        CsdlEnumType statusEnum = new CsdlEnumType();
        statusEnum.setName("Status");
        CsdlEnumMember activeMember = new CsdlEnumMember();
        activeMember.setName("Active");
        activeMember.setValue("0");
        CsdlEnumMember inactiveMember = new CsdlEnumMember();
        inactiveMember.setName("Inactive");
        inactiveMember.setValue("1");
        statusEnum.setMembers(Arrays.asList(activeMember, inactiveMember));
        
        schema.setEnumTypes(Arrays.asList(statusEnum));
        
        return schema;
    }
    
    private void setupMockRepository() {
        when(repository.getSchema("TestService")).thenReturn(testSchema);
    }
    
    @Test
    void testGetAllDependencies_EntityType() {
        // Test transitive dependencies: Customer -> Address -> Country
        CsdlEntityType customerEntity = testSchema.getEntityType("Customer");
        List<TypeDependencyAnalyzer.TypeReference> allDeps = analyzer.getAllDependencies(customerEntity);
        
        // Should include all transitive dependencies
        assertEquals(3, allDeps.size());
        assertTrue(allDeps.stream().anyMatch(dep -> dep.getFullQualifiedName().equals("TestService.Address")));
        assertTrue(allDeps.stream().anyMatch(dep -> dep.getFullQualifiedName().equals("TestService.Country")));
        assertTrue(allDeps.stream().anyMatch(dep -> dep.getFullQualifiedName().equals("TestService.Status")));
    }
    
    @Test
    void testGetAllDependencies_ComplexType() {
        // Test transitive dependencies for complex types
        CsdlComplexType addressComplex = testSchema.getComplexType("Address");
        List<TypeDependencyAnalyzer.TypeReference> allDeps = analyzer.getAllDependencies(addressComplex);
        
        // Should include Country
        assertEquals(1, allDeps.size());
        assertTrue(allDeps.stream().anyMatch(dep -> dep.getFullQualifiedName().equals("TestService.Country")));
    }
    
    @Test
    void testGetAllDependencies_LeafType() {
        // Test leaf type with no dependencies
        CsdlComplexType countryComplex = testSchema.getComplexType("Country");
        List<TypeDependencyAnalyzer.TypeReference> allDeps = analyzer.getAllDependencies(countryComplex);
        
        // Should be empty as Country has no dependencies
        assertTrue(allDeps.isEmpty());
    }
    
    @Test
    void testGetAllDependencies_NullInput() {
        // Test with null input
        CsdlEntityType nullEntity = null;
        List<TypeDependencyAnalyzer.TypeReference> allDeps = analyzer.getAllDependencies(nullEntity);
        
        // Should be empty
        assertTrue(allDeps.isEmpty());
    }
    
    @Test
    void testGetAllDependencies_CompareWithDirectDependencies() {
        // Test that all dependencies include direct dependencies
        CsdlEntityType customerEntity = testSchema.getEntityType("Customer");
        List<TypeDependencyAnalyzer.TypeReference> directDeps = analyzer.getDirectDependencies(customerEntity);
        List<TypeDependencyAnalyzer.TypeReference> allDeps = analyzer.getAllDependencies(customerEntity);
        
        // All dependencies should contain all direct dependencies
        for (TypeDependencyAnalyzer.TypeReference directDep : directDeps) {
            assertTrue(allDeps.stream().anyMatch(allDep -> 
                allDep.getFullQualifiedName().equals(directDep.getFullQualifiedName())));
        }
        
        // For root entity with deep dependencies, all should be greater than or equal to direct
        assertTrue(allDeps.size() >= directDeps.size());
    }
    
    @Test
    void testGetAllDependencies_DeepDependencyChain() {
        // Test deep dependency chain: Entity with properties referencing nested complex types
        CsdlEntityType customerEntity = testSchema.getEntityType("Customer");
        List<TypeDependencyAnalyzer.TypeReference> allDeps = analyzer.getAllDependencies(customerEntity);
        
        // Should include immediate dependency (Address)
        assertTrue(allDeps.stream().anyMatch(dep -> dep.getFullQualifiedName().equals("TestService.Address")));
        
        // Should include transitive dependency through Address (Country)
        assertTrue(allDeps.stream().anyMatch(dep -> dep.getFullQualifiedName().equals("TestService.Country")));
        
        // Verify the deep chain is correctly analyzed
        assertEquals(3, allDeps.size(), "Should include Address, Country, and Status");
    }
}
