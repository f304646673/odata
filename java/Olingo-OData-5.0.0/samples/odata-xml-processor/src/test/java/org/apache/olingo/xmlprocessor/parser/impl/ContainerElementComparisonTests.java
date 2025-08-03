package org.apache.olingo.xmlprocessor.parser.impl;

import java.io.File;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for container element comparison methods in AdvancedMetadataParser.
 * Tests the strict OData 4.0 spec-compliant comparison logic for EntitySets, ActionImports,
 * FunctionImports, Singletons, and NavigationPropertyBindings.
 */
class ContainerElementComparisonTests {

    private AdvancedMetadataParser parser;

    @BeforeEach
    void setUp() {
        parser = new AdvancedMetadataParser();
    }

    @Test
    @DisplayName("Test EntitySet comparison - identical entity sets")
    void testEntitySetComparisonIdentical() {
        String schema1 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.EntitySets\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "        <NavigationProperty Name=\"Category\" Type=\"Test.EntitySets.Category\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Category\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.EntitySets.Product\">\n" +
            "          <NavigationPropertyBinding Path=\"Category\" Target=\"Categories\"/>\n" +
            "        </EntitySet>\n" +
            "        <EntitySet Name=\"Categories\" EntityType=\"Test.EntitySets.Category\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
            
        String schema2 = schema1; // Identical schema
        
        assertDoesNotThrow(() -> {
            File tempFile1 = File.createTempFile("schema1", ".xml");
            File tempFile2 = File.createTempFile("schema2", ".xml");
            tempFile1.deleteOnExit();
            tempFile2.deleteOnExit();
            
            try {
                java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
                java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());
                
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());
                
                assertNotNull(provider1);
                assertNotNull(provider2);
                
                // Get schemas and compare them
                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();
                
                assertEquals(1, schemas1.size());
                assertEquals(1, schemas2.size());
                
                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);
                
                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);
                
                assertTrue(identical, "Identical schemas should be detected as identical");
                
            } finally {
                tempFile1.delete();
                tempFile2.delete();
            }
        });
    }
    
    @Test
    @DisplayName("Test EntitySet comparison - different entity sets")
    void testEntitySetComparisonDifferent() {
        String schema1 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.EntitySets\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.EntitySets.Product\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
            
        String schema2 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.EntitySets\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.EntitySets.Product\"/>\n" +
            "        <EntitySet Name=\"Categories\" EntityType=\"Test.EntitySets.Product\"/>\n" + // Additional entity set
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        assertDoesNotThrow(() -> {
            File tempFile1 = File.createTempFile("schema1", ".xml");
            File tempFile2 = File.createTempFile("schema2", ".xml");
            tempFile1.deleteOnExit();
            tempFile2.deleteOnExit();
            
            try {
                java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
                java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());
                
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());
                
                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();
                
                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);
                
                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);
                
                assertFalse(identical, "Different schemas should not be detected as identical");
                
            } finally {
                tempFile1.delete();
                tempFile2.delete();
            }
        });
    }
    
    @Test
    @DisplayName("Test ActionImport comparison")
    void testActionImportComparison() {
        String schema1 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Actions\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <Action Name=\"CreateProduct\">\n" +
            "        <Parameter Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "        <ReturnType Type=\"Test.Actions.Product\"/>\n" +
            "      </Action>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.Actions.Product\"/>\n" +
            "        <ActionImport Name=\"CreateProduct\" Action=\"Test.Actions.CreateProduct\" EntitySet=\"Products\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
            
        String schema2 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Actions\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <Action Name=\"CreateProduct\">\n" +
            "        <Parameter Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "        <ReturnType Type=\"Test.Actions.Product\"/>\n" +
            "      </Action>\n" +
            "      <Action Name=\"UpdateProduct\">\n" + // Different action
            "        <Parameter Name=\"ID\" Type=\"Edm.Int32\"/>\n" +
            "        <Parameter Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </Action>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.Actions.Product\"/>\n" +
            "        <ActionImport Name=\"CreateProduct\" Action=\"Test.Actions.CreateProduct\" EntitySet=\"Products\"/>\n" +
            "        <ActionImport Name=\"UpdateProduct\" Action=\"Test.Actions.UpdateProduct\"/>\n" + // Additional action import
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        assertDoesNotThrow(() -> {
            File tempFile1 = File.createTempFile("schema1", ".xml");
            File tempFile2 = File.createTempFile("schema2", ".xml");
            tempFile1.deleteOnExit();
            tempFile2.deleteOnExit();
            
            try {
                java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
                java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());
                
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());
                
                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();
                
                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);
                
                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);
                
                assertFalse(identical, "Schemas with different ActionImports should not be identical");
                
            } finally {
                tempFile1.delete();
                tempFile2.delete();
            }
        });
    }
    
    @Test
    @DisplayName("Test FunctionImport comparison")
    void testFunctionImportComparison() {
        String schema1 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Functions\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <Function Name=\"GetProductsByCategory\">\n" +
            "        <Parameter Name=\"CategoryID\" Type=\"Edm.Int32\"/>\n" +
            "        <ReturnType Type=\"Collection(Test.Functions.Product)\"/>\n" +
            "      </Function>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.Functions.Product\"/>\n" +
            "        <FunctionImport Name=\"GetProductsByCategory\" Function=\"Test.Functions.GetProductsByCategory\" EntitySet=\"Products\" IncludeInServiceDocument=\"true\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
            
        String schema2 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Functions\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <Function Name=\"GetProductsByCategory\">\n" +
            "        <Parameter Name=\"CategoryID\" Type=\"Edm.Int32\"/>\n" +
            "        <ReturnType Type=\"Collection(Test.Functions.Product)\"/>\n" +
            "      </Function>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.Functions.Product\"/>\n" +
            "        <FunctionImport Name=\"GetProductsByCategory\" Function=\"Test.Functions.GetProductsByCategory\" EntitySet=\"Products\" IncludeInServiceDocument=\"false\"/>\n" + // Different IncludeInServiceDocument
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        assertDoesNotThrow(() -> {
            File tempFile1 = File.createTempFile("schema1", ".xml");
            File tempFile2 = File.createTempFile("schema2", ".xml");
            tempFile1.deleteOnExit();
            tempFile2.deleteOnExit();
            
            try {
                java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
                java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());
                
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());
                
                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();
                
                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);
                
                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);
                
                assertFalse(identical, "Schemas with different FunctionImport properties should not be identical");
                
            } finally {
                tempFile1.delete();
                tempFile2.delete();
            }
        });
    }
    
    @Test
    @DisplayName("Test Singleton comparison")
    void testSingletonComparison() {
        String schema1 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Singletons\">\n" +
            "      <EntityType Name=\"Company\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "        <NavigationProperty Name=\"CEO\" Type=\"Test.Singletons.Person\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Person\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"People\" EntityType=\"Test.Singletons.Person\"/>\n" +
            "        <Singleton Name=\"MyCompany\" Type=\"Test.Singletons.Company\">\n" +
            "          <NavigationPropertyBinding Path=\"CEO\" Target=\"People\"/>\n" +
            "        </Singleton>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
            
        String schema2 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Singletons\">\n" +
            "      <EntityType Name=\"Company\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "        <NavigationProperty Name=\"CEO\" Type=\"Test.Singletons.Person\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Person\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"People\" EntityType=\"Test.Singletons.Person\"/>\n" +
            "        <Singleton Name=\"MyCompany\" Type=\"Test.Singletons.Company\">\n" +
            "          <NavigationPropertyBinding Path=\"CEO\" Target=\"OtherPeople\"/>\n" + // Different navigation target
            "        </Singleton>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        assertDoesNotThrow(() -> {
            File tempFile1 = File.createTempFile("schema1", ".xml");
            File tempFile2 = File.createTempFile("schema2", ".xml");
            tempFile1.deleteOnExit();
            tempFile2.deleteOnExit();
            
            try {
                java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
                java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());
                
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());
                
                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();
                
                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);
                
                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);
                
                assertFalse(identical, "Schemas with different Singleton NavigationPropertyBindings should not be identical");
                
            } finally {
                tempFile1.delete();
                tempFile2.delete();
            }
        });
    }
    
    @Test
    @DisplayName("Test NavigationPropertyBinding comparison")
    void testNavigationPropertyBindingComparison() {
        String schemaWithBindings = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.NavBindings\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <NavigationProperty Name=\"Category\" Type=\"Test.NavBindings.Category\"/>\n" +
            "        <NavigationProperty Name=\"Supplier\" Type=\"Test.NavBindings.Supplier\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Category\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Supplier\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.NavBindings.Product\">\n" +
            "          <NavigationPropertyBinding Path=\"Category\" Target=\"Categories\"/>\n" +
            "          <NavigationPropertyBinding Path=\"Supplier\" Target=\"Suppliers\"/>\n" +
            "        </EntitySet>\n" +
            "        <EntitySet Name=\"Categories\" EntityType=\"Test.NavBindings.Category\"/>\n" +
            "        <EntitySet Name=\"Suppliers\" EntityType=\"Test.NavBindings.Supplier\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
            
        String schemaWithoutBindings = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.NavBindings\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <NavigationProperty Name=\"Category\" Type=\"Test.NavBindings.Category\"/>\n" +
            "        <NavigationProperty Name=\"Supplier\" Type=\"Test.NavBindings.Supplier\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Category\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Supplier\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"Container\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Test.NavBindings.Product\"/>\n" + // No NavigationPropertyBindings
            "        <EntitySet Name=\"Categories\" EntityType=\"Test.NavBindings.Category\"/>\n" +
            "        <EntitySet Name=\"Suppliers\" EntityType=\"Test.NavBindings.Supplier\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        assertDoesNotThrow(() -> {
            File tempFile1 = File.createTempFile("schema1", ".xml");
            File tempFile2 = File.createTempFile("schema2", ".xml");
            tempFile1.deleteOnExit();
            tempFile2.deleteOnExit();
            
            try {
                java.nio.file.Files.write(tempFile1.toPath(), schemaWithBindings.getBytes());
                java.nio.file.Files.write(tempFile2.toPath(), schemaWithoutBindings.getBytes());
                
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());
                
                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();
                
                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);
                
                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);
                
                assertFalse(identical, "Schemas with different NavigationPropertyBindings should not be identical");
                
            } finally {
                tempFile1.delete();
                tempFile2.delete();
            }
        });
    }
}
