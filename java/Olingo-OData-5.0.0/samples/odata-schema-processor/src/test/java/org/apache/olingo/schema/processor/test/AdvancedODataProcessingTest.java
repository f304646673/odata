package org.apache.olingo.schema.processor.test;

import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.schema.processor.analyzer.DependencyAnalyzer;
import org.apache.olingo.schema.processor.analyzer.impl.EnhancedDependencyAnalyzer;
import org.apache.olingo.schema.processor.merger.AdvancedSchemaMerger;
import org.apache.olingo.schema.processor.exporter.ContainerExporter;
import org.apache.olingo.schema.processor.exporter.impl.EnhancedContainerExporter;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Enhanced functionality tests for advanced OData 4.0 schema processing
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedODataProcessingTest {

    @Mock
    private SchemaRepository schemaRepository;
    
    @Mock 
    private DependencyAnalyzer dependencyAnalyzer;
    
    private AdvancedSchemaMerger schemaMerger;
    private EnhancedDependencyAnalyzer enhancedDependencyAnalyzer;
    private EnhancedContainerExporter containerExporter;
    
    private CsdlSchema testSchema1;
    private CsdlSchema testSchema2;
    private CsdlEntityContainer testContainer;

    @Before
    public void setUp() {
        schemaMerger = new AdvancedSchemaMerger();
        enhancedDependencyAnalyzer = new EnhancedDependencyAnalyzer(schemaRepository);
        containerExporter = new EnhancedContainerExporter(schemaRepository, dependencyAnalyzer);
        
        setupTestSchemas();
    }

    private void setupTestSchemas() {
        // Create test schema 1
        testSchema1 = new CsdlSchema();
        testSchema1.setNamespace("com.example.schema1");
        
        CsdlEntityType entityType1 = new CsdlEntityType();
        entityType1.setName("Customer");
        entityType1.setProperties(Arrays.asList(
            createProperty("CustomerId", "Edm.String", false),
            createProperty("Name", "Edm.String", true)
        ));
        
        CsdlPropertyRef propertyRef1 = new CsdlPropertyRef();
        propertyRef1.setName("CustomerId");
        entityType1.setKey(Arrays.asList(propertyRef1));
        
        testSchema1.setEntityTypes(Arrays.asList(entityType1));
        
        // Create test schema 2
        testSchema2 = new CsdlSchema();
        testSchema2.setNamespace("com.example.schema2");
        
        CsdlEntityType entityType2 = new CsdlEntityType();
        entityType2.setName("Order");
        entityType2.setProperties(Arrays.asList(
            createProperty("OrderId", "Edm.String", false),
            createProperty("CustomerId", "Edm.String", false),
            createProperty("Amount", "Edm.Decimal", true)
        ));
        
        CsdlPropertyRef propertyRef2 = new CsdlPropertyRef();
        propertyRef2.setName("OrderId");
        entityType2.setKey(Arrays.asList(propertyRef2));
        
        testSchema2.setEntityTypes(Arrays.asList(entityType2));
        
        // Create test container
        testContainer = new CsdlEntityContainer();
        testContainer.setName("TestContainer");
        
        CsdlEntitySet customerSet = new CsdlEntitySet();
        customerSet.setName("Customers");
        customerSet.setType("com.example.schema1.Customer");
        
        CsdlEntitySet orderSet = new CsdlEntitySet();
        orderSet.setName("Orders");
        orderSet.setType("com.example.schema2.Order");
        
        testContainer.setEntitySets(Arrays.asList(customerSet, orderSet));
    }

    private CsdlProperty createProperty(String name, String type, boolean nullable) {
        CsdlProperty property = new CsdlProperty();
        property.setName(name);
        property.setType(type);
        property.setNullable(nullable);
        return property;
    }

    @Test
    public void testAdvancedSchemaMerging() {
        // Test multi-namespace schema merging
        Map<String, List<CsdlSchema>> schemaMap = new HashMap<>();
        schemaMap.put("file1.xml", Arrays.asList(testSchema1));
        schemaMap.put("file2.xml", Arrays.asList(testSchema2));
        
        AdvancedSchemaMerger.MergeResult result = schemaMerger.mergeSchemas(schemaMap);
        
        assertTrue("Merge should be successful", result.isSuccess());
        assertNotNull("Merged schemas should not be null", result.getMergedSchemas());
        assertEquals("Should have 2 namespaces", 2, result.getMergedSchemas().size());
        
        // Check that schemas are properly merged
        assertTrue("Should contain schema1 namespace", 
            result.getMergedSchemas().containsKey("com.example.schema1"));
        assertTrue("Should contain schema2 namespace", 
            result.getMergedSchemas().containsKey("com.example.schema2"));
    }

    @Test
    public void testSchemaMergingWithDuplicates() {
        // Create duplicate entity type
        CsdlEntityType duplicateCustomer = new CsdlEntityType();
        duplicateCustomer.setName("Customer");
        duplicateCustomer.setProperties(Arrays.asList(
            createProperty("CustomerId", "Edm.String", false),
            createProperty("Email", "Edm.String", true)
        ));
        
        CsdlSchema schemaWithDuplicate = new CsdlSchema();
        schemaWithDuplicate.setNamespace("com.example.schema1"); // Same namespace
        schemaWithDuplicate.setEntityTypes(Arrays.asList(duplicateCustomer));
        
        Map<String, List<CsdlSchema>> schemaMap = new HashMap<>();
        schemaMap.put("file1.xml", Arrays.asList(testSchema1));
        schemaMap.put("file2.xml", Arrays.asList(schemaWithDuplicate));
        
        AdvancedSchemaMerger.MergeResult result = schemaMerger.mergeSchemas(schemaMap);
        
        // Should detect duplicates
        assertFalse("Should not be successful due to duplicates", result.isSuccess());
        assertTrue("Should have errors", !result.getErrors().isEmpty());
    }

    @Test
    public void testEnhancedDependencyAnalysis() {
        // Mock schema repository responses
        when(schemaRepository.getSchema("com.example.schema1")).thenReturn(testSchema1);
        when(schemaRepository.getSchema("com.example.schema2")).thenReturn(testSchema2);
        when(schemaRepository.getAllNamespaces()).thenReturn(
            new HashSet<>(Arrays.asList("com.example.schema1", "com.example.schema2")));

        // Test recursive dependency analysis
        Set<String> recursiveDeps = enhancedDependencyAnalyzer.getRecursiveDependencies("com.example.schema1");
        
        assertNotNull("Recursive dependencies should not be null", recursiveDeps);
        assertTrue("Should include the namespace itself", recursiveDeps.contains("com.example.schema1"));
    }

    @Test
    public void testContainerExportToXml() throws IOException {
        // Mock dependencies
        when(schemaRepository.getSchema(anyString())).thenReturn(testSchema1);
        when(dependencyAnalyzer.getRecursiveDependencies(anyString())).thenReturn(new HashSet<>());
        
        Path tempDir = Files.createTempDirectory("odata-test");
        String outputPath = tempDir.resolve("container.xml").toString();
        
        try {
            ContainerExporter.ContainerExportResult result = 
                containerExporter.exportContainer(testContainer, outputPath, "com.example.test");
            
            assertTrue("Export should be successful", result.isSuccess());
            assertTrue("Output file should exist", Files.exists(Paths.get(outputPath)));
            
            String content = new String(Files.readAllBytes(Paths.get(outputPath)));
            assertTrue("Should contain XML declaration", content.contains("<?xml"));
            assertTrue("Should contain edmx namespace", content.contains("edmx:Edmx"));
            assertTrue("Should contain container name", content.contains("TestContainer"));
            
        } finally {
            // Cleanup
            Files.deleteIfExists(Paths.get(outputPath));
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void testContainerExportToJson() throws IOException {
        // Mock dependencies
        when(schemaRepository.getSchema(anyString())).thenReturn(testSchema1);
        when(dependencyAnalyzer.getRecursiveDependencies(anyString())).thenReturn(new HashSet<>());
        
        Path tempDir = Files.createTempDirectory("odata-test");
        String outputPath = tempDir.resolve("container.json").toString();
        
        try {
            ContainerExporter.ContainerExportResult result = 
                containerExporter.exportContainer(testContainer, outputPath, "com.example.test");
            
            assertTrue("Export should be successful", result.isSuccess());
            assertTrue("Output file should exist", Files.exists(Paths.get(outputPath)));
            
            String content = new String(Files.readAllBytes(Paths.get(outputPath)));
            assertTrue("Should be valid JSON with version", content.contains("\"$Version\""));
            assertTrue("Should contain entity container reference", content.contains("\"$EntityContainer\""));
            
        } finally {
            // Cleanup
            Files.deleteIfExists(Paths.get(outputPath));
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void testMergedContainerExport() throws IOException {
        // Mock dependencies
        when(schemaRepository.getSchema(anyString())).thenReturn(testSchema1);
        when(dependencyAnalyzer.getRecursiveDependencies(anyString())).thenReturn(new HashSet<>());
        
        List<CsdlSchema> schemas = Arrays.asList(testSchema1, testSchema2);
        Path tempDir = Files.createTempDirectory("odata-test");
        String outputPath = tempDir.resolve("merged-container.xml").toString();
        
        try {
            ContainerExporter.ContainerExportResult result = 
                containerExporter.exportMergedContainers(schemas, outputPath, "com.example.merged");
            
            assertTrue("Merged export should be successful", result.isSuccess());
            assertTrue("Output file should exist", Files.exists(Paths.get(outputPath)));
            
        } finally {
            // Cleanup
            Files.deleteIfExists(Paths.get(outputPath));
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void testCollectionHandling() {
        // Test collection property handling
        CsdlProperty collectionProp = new CsdlProperty();
        collectionProp.setName("Tags");
        collectionProp.setType("Collection(Edm.String)");
        collectionProp.setNullable(true);
        
        CsdlEntityType entityWithCollection = new CsdlEntityType();
        entityWithCollection.setName("ProductWithTags");
        entityWithCollection.setProperties(Arrays.asList(
            createProperty("ProductId", "Edm.String", false),
            collectionProp
        ));
        
        CsdlSchema schemaWithCollection = new CsdlSchema();
        schemaWithCollection.setNamespace("com.example.collections");
        schemaWithCollection.setEntityTypes(Arrays.asList(entityWithCollection));
        
        Map<String, List<CsdlSchema>> schemaMap = new HashMap<>();
        schemaMap.put("file1.xml", Arrays.asList(schemaWithCollection));
        
        AdvancedSchemaMerger.MergeResult result = schemaMerger.mergeSchemas(schemaMap);
        
        assertTrue("Should handle collection types", result.isSuccess());
        assertTrue("Should preserve collection properties", 
            result.getMergedSchemas().get("com.example.collections")
                .getEntityTypes().get(0).getProperties().stream()
                .anyMatch(p -> p.getType().startsWith("Collection(")));
    }

    @Test
    public void testComplexTypeHandling() {
        // Test complex type support
        CsdlComplexType addressType = new CsdlComplexType();
        addressType.setName("Address");
        addressType.setProperties(Arrays.asList(
            createProperty("Street", "Edm.String", true),
            createProperty("City", "Edm.String", true),
            createProperty("PostalCode", "Edm.String", true)
        ));
        
        CsdlProperty addressProp = new CsdlProperty();
        addressProp.setName("Address");
        addressProp.setType("com.example.schema1.Address");
        addressProp.setNullable(true);
        
        CsdlEntityType personType = new CsdlEntityType();
        personType.setName("Person");
        personType.setProperties(Arrays.asList(
            createProperty("PersonId", "Edm.String", false),
            addressProp
        ));
        
        testSchema1.setComplexTypes(Arrays.asList(addressType));
        testSchema1.setEntityTypes(Arrays.asList(personType));
        
        Map<String, List<CsdlSchema>> schemaMap = new HashMap<>();
        schemaMap.put("file1.xml", Arrays.asList(testSchema1));
        
        AdvancedSchemaMerger.MergeResult result = schemaMerger.mergeSchemas(schemaMap);
        
        assertTrue("Should handle complex types", result.isSuccess());
        assertNotNull("Should have complex types", 
            result.getMergedSchemas().get("com.example.schema1").getComplexTypes());
        assertEquals("Should have 1 complex type", 1, 
            result.getMergedSchemas().get("com.example.schema1").getComplexTypes().size());
    }
}
