package org.apache.olingo.schemamanager.analyzer.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultTypeDependencyAnalyzerTest_detectCircularDependencies {
    @Mock
    private SchemaRepository repository;
    private DefaultTypeDependencyAnalyzer analyzer;
    
    @BeforeEach
    void setUp() throws Exception {
        analyzer = new DefaultTypeDependencyAnalyzer(repository);
    }
    
    private CsdlSchema loadSchemaFromResource(String resourcePath) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            
            CsdlSchema schema = new CsdlSchema();
            schema.setNamespace("TestService");
            return schema;
        }
    }
    
    @Test
    void testDetectCircularDependencies_NoCircular() {
        // Create non-circular dependency schema: A -> B -> C
        CsdlSchema schema = createNonCircularSchema();
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", schema);
        when(repository.getSchema("TestService")).thenReturn(schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        setupMockRepository(schema);
        
        List<TypeDependencyAnalyzer.CircularDependency> circularDependencies = analyzer.detectCircularDependencies();
        
        // Should find no circular dependencies
        assertTrue(circularDependencies.isEmpty());
    }
    
    @Test
    void testDetectCircularDependencies_WithCircular() {
        // Create circular dependency schema: A -> B -> A
        CsdlSchema schema = createCircularSchema();
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", schema);
        when(repository.getSchema("TestService")).thenReturn(schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        setupMockRepository(schema);
        
        List<TypeDependencyAnalyzer.CircularDependency> circularDependencies = analyzer.detectCircularDependencies();
        
        // Should find circular dependencies
        assertFalse(circularDependencies.isEmpty());
        assertTrue(circularDependencies.size() >= 1);
        
        // Verify the circular dependency involves TypeA and TypeB
        boolean foundABCircular = circularDependencies.stream().anyMatch(cd -> {
            List<String> cycle = cd.getDependencyChain();
            return cycle.contains("TestService.TypeA") && cycle.contains("TestService.TypeB");
        });
        assertTrue(foundABCircular);
    }
    
    @Test
    void testDetectCircularDependencies_ComplexCircular() {
        // Create complex circular dependency: A -> B -> C -> A
        CsdlSchema schema = createComplexCircularSchema();
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", schema);
        when(repository.getSchema("TestService")).thenReturn(schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        setupMockRepository(schema);
        
        List<TypeDependencyAnalyzer.CircularDependency> circularDependencies = analyzer.detectCircularDependencies();
        
        // Should find circular dependencies
        assertFalse(circularDependencies.isEmpty());
        
        // Verify the circular dependency involves TypeA, TypeB, and TypeC
        boolean foundComplexCircular = circularDependencies.stream().anyMatch(cd -> {
            List<String> cycle = cd.getDependencyChain();
            return cycle.contains("TestService.TypeA") && 
                   cycle.contains("TestService.TypeB") && 
                   cycle.contains("TestService.TypeC");
        });
        assertTrue(foundComplexCircular);
    }
    
    private CsdlSchema createNonCircularSchema() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestService");
        
        // TypeA -> TypeB
        CsdlComplexType typeA = new CsdlComplexType();
        typeA.setName("TypeA");
        CsdlProperty propA = new CsdlProperty();
        propA.setName("TypeBRef");
        propA.setType("TestService.TypeB");
        typeA.setProperties(Arrays.asList(propA));
        
        // TypeB -> TypeC
        CsdlComplexType typeB = new CsdlComplexType();
        typeB.setName("TypeB");
        CsdlProperty propB = new CsdlProperty();
        propB.setName("TypeCRef");
        propB.setType("TestService.TypeC");
        typeB.setProperties(Arrays.asList(propB));
        
        // TypeC (leaf type)
        CsdlComplexType typeC = new CsdlComplexType();
        typeC.setName("TypeC");
        CsdlProperty propC = new CsdlProperty();
        propC.setName("Value");
        propC.setType("Edm.String");
        typeC.setProperties(Arrays.asList(propC));
        
        schema.setComplexTypes(Arrays.asList(typeA, typeB, typeC));
        return schema;
    }
    
    private CsdlSchema createCircularSchema() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestService");
        
        // TypeA -> TypeB
        CsdlComplexType typeA = new CsdlComplexType();
        typeA.setName("TypeA");
        CsdlProperty propA = new CsdlProperty();
        propA.setName("TypeBRef");
        propA.setType("TestService.TypeB");
        typeA.setProperties(Arrays.asList(propA));
        
        // TypeB -> TypeA (creates circular dependency)
        CsdlComplexType typeB = new CsdlComplexType();
        typeB.setName("TypeB");
        CsdlProperty propB = new CsdlProperty();
        propB.setName("TypeARef");
        propB.setType("TestService.TypeA");
        typeB.setProperties(Arrays.asList(propB));
        
        schema.setComplexTypes(Arrays.asList(typeA, typeB));
        return schema;
    }
    
    private CsdlSchema createComplexCircularSchema() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestService");
        
        // TypeA -> TypeB
        CsdlComplexType typeA = new CsdlComplexType();
        typeA.setName("TypeA");
        CsdlProperty propA = new CsdlProperty();
        propA.setName("TypeBRef");
        propA.setType("TestService.TypeB");
        typeA.setProperties(Arrays.asList(propA));
        
        // TypeB -> TypeC
        CsdlComplexType typeB = new CsdlComplexType();
        typeB.setName("TypeB");
        CsdlProperty propB = new CsdlProperty();
        propB.setName("TypeCRef");
        propB.setType("TestService.TypeC");
        typeB.setProperties(Arrays.asList(propB));
        
        // TypeC -> TypeA (creates circular dependency A -> B -> C -> A)
        CsdlComplexType typeC = new CsdlComplexType();
        typeC.setName("TypeC");
        CsdlProperty propC = new CsdlProperty();
        propC.setName("TypeARef");
        propC.setType("TestService.TypeA");
        typeC.setProperties(Arrays.asList(propC));
        
        schema.setComplexTypes(Arrays.asList(typeA, typeB, typeC));
        return schema;
    }
    
    private void setupMockRepository(CsdlSchema schema) {
        // Setup mock returns for each complex type
        for (CsdlComplexType complexType : schema.getComplexTypes()) {
            String fqn = schema.getNamespace() + "." + complexType.getName();
            when(repository.getComplexType(fqn)).thenReturn(complexType);
        }
    }
}
