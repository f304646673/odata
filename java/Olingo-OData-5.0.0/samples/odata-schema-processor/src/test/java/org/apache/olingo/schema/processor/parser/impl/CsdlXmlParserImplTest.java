package org.apache.olingo.schema.processor.parser.impl;

import static org.junit.Assert.*;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlEntityType;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlComplexType;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlEnumType;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlTypeDefinition;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlEntityContainer;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlTerm;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlAnnotation;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Comprehensive test class for CsdlXmlParserImpl
 * Tests all parsing functionality and extension class conversions
 */
public class CsdlXmlParserImplTest {

    private CsdlXmlParserImpl parser;

    @Before
    public void setUp() {
        parser = new CsdlXmlParserImpl();
    }

    @Test
    public void testParseValidXmlWithMultipleTypes() throws Exception {
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                "  <edmx:DataServices>\n" +
                "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Model\">\n" +
                "      <EntityType Name=\"Product\">\n" +
                "        <Key>\n" +
                "          <PropertyRef Name=\"ID\"/>\n" +
                "        </Key>\n" +
                "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
                "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
                "      </EntityType>\n" +
                "      <ComplexType Name=\"Address\">\n" +
                "        <Property Name=\"Street\" Type=\"Edm.String\"/>\n" +
                "        <Property Name=\"City\" Type=\"Edm.String\"/>\n" +
                "      </ComplexType>\n" +
                "      <EnumType Name=\"Category\">\n" +
                "        <Member Name=\"Food\" Value=\"0\"/>\n" +
                "        <Member Name=\"Electronics\" Value=\"1\"/>\n" +
                "      </EnumType>\n" +
                "      <TypeDefinition Name=\"ProductId\" UnderlyingType=\"Edm.String\"/>\n" +
                "      <EntityContainer Name=\"Container\">\n" +
                "        <EntitySet Name=\"Products\" EntityType=\"Test.Model.Product\"/>\n" +
                "      </EntityContainer>\n" +
                "    </Schema>\n" +
                "  </edmx:DataServices>\n" +
                "</edmx:Edmx>";

        InputStream inputStream = new ByteArrayInputStream(validXml.getBytes("UTF-8"));
        
        List<CsdlSchema> schemas = parser.parseWithOlingoNative(inputStream, "test.xml");
        
        assertNotNull(schemas);
        assertEquals(1, schemas.size());
        
        CsdlSchema schema = schemas.get(0);
        assertEquals("Test.Model", schema.getNamespace());
        
        // Verify EntityTypes were parsed and extended
        assertNotNull(schema.getEntityTypes());
        assertEquals(1, schema.getEntityTypes().size());
        assertTrue(schema.getEntityTypes().get(0) instanceof ExtendedCsdlEntityType);
        
        ExtendedCsdlEntityType entityType = (ExtendedCsdlEntityType) schema.getEntityTypes().get(0);
        assertEquals("Product", entityType.getName());
        assertNotNull(entityType.getProperties());
        assertEquals(2, entityType.getProperties().size());
        
        // Verify ComplexTypes were parsed and extended
        assertNotNull(schema.getComplexTypes());
        assertEquals(1, schema.getComplexTypes().size());
        assertTrue(schema.getComplexTypes().get(0) instanceof ExtendedCsdlComplexType);
        
        ExtendedCsdlComplexType complexType = (ExtendedCsdlComplexType) schema.getComplexTypes().get(0);
        assertEquals("Address", complexType.getName());
        
        // Verify EnumTypes were parsed and extended
        assertNotNull(schema.getEnumTypes());
        assertEquals(1, schema.getEnumTypes().size());
        assertTrue(schema.getEnumTypes().get(0) instanceof ExtendedCsdlEnumType);
        
        ExtendedCsdlEnumType enumType = (ExtendedCsdlEnumType) schema.getEnumTypes().get(0);
        assertEquals("Category", enumType.getName());
        
        // Verify TypeDefinitions were parsed and extended
        assertNotNull(schema.getTypeDefinitions());
        assertEquals(1, schema.getTypeDefinitions().size());
        assertTrue(schema.getTypeDefinitions().get(0) instanceof ExtendedCsdlTypeDefinition);
        
        ExtendedCsdlTypeDefinition typeDef = (ExtendedCsdlTypeDefinition) schema.getTypeDefinitions().get(0);
        assertEquals("ProductId", typeDef.getName());
        
        // Verify EntityContainer was parsed and extended
        assertNotNull(schema.getEntityContainer());
        assertTrue(schema.getEntityContainer() instanceof ExtendedCsdlEntityContainer);
        
        ExtendedCsdlEntityContainer container = (ExtendedCsdlEntityContainer) schema.getEntityContainer();
        assertEquals("Container", container.getName());
    }

    @Test
    public void testParseXmlWithTermsAndAnnotations() throws Exception {
        String xmlWithTerms = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                "  <edmx:DataServices>\n" +
                "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Annotations\">\n" +
                "      <Term Name=\"Description\" Type=\"Edm.String\" DefaultValue=\"No description\"/>\n" +
                "      <Term Name=\"Priority\" Type=\"Edm.Int32\" MaxLength=\"100\"/>\n" +
                "      <Annotations Target=\"Test.Annotations\">\n" +
                "        <Annotation Term=\"Test.Annotations.Description\" String=\"Sample annotations\"/>\n" +
                "      </Annotations>\n" +
                "    </Schema>\n" +
                "  </edmx:DataServices>\n" +
                "</edmx:Edmx>";

        InputStream inputStream = new ByteArrayInputStream(xmlWithTerms.getBytes("UTF-8"));
        
        List<CsdlSchema> schemas = parser.parseWithOlingoNative(inputStream, "test-terms.xml");
        
        assertNotNull(schemas);
        assertEquals(1, schemas.size());
        
        CsdlSchema schema = schemas.get(0);
        
        // Verify Terms were parsed and extended
        assertNotNull(schema.getTerms());
        assertEquals(2, schema.getTerms().size());
        assertTrue(schema.getTerms().get(0) instanceof ExtendedCsdlTerm);
        
        ExtendedCsdlTerm term = (ExtendedCsdlTerm) schema.getTerms().get(0);
        assertEquals("Description", term.getName());
        assertEquals("Edm.String", term.getType());
        
        // Verify Annotations were parsed
        if (schema.getAnnotationGroups() != null && !schema.getAnnotationGroups().isEmpty()) {
            assertTrue(schema.getAnnotationGroups().get(0).getAnnotations().size() > 0);
        }
    }

    @Test
    public void testParseInvalidXml() {
        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<InvalidRoot>This is not valid OData XML</InvalidRoot>";

        try {
            InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes("UTF-8"));
            parser.parseWithOlingoNative(inputStream, "invalid.xml");
            fail("Expected RuntimeException for invalid XML");
        } catch (Exception e) {
            // Expected exception
            assertTrue(true);
        }
    }

    @Test
    public void testParseNullInputStream() {
        try {
            parser.parseWithOlingoNative(null, "null-test.xml");
            fail("Expected IllegalArgumentException for null input stream");
        } catch (Exception e) {
            // Expected exception
            assertTrue(true);
        }
    }

    @Test
    public void testParseEmptyXml() {
        String emptyXml = "";

        try {
            InputStream inputStream = new ByteArrayInputStream(emptyXml.getBytes("UTF-8"));
            parser.parseWithOlingoNative(inputStream, "empty.xml");
            fail("Expected RuntimeException for empty XML");
        } catch (Exception e) {
            // Expected exception
            assertTrue(true);
        }
    }

    @Test
    public void testCreateExtendedSchemaConversions() throws Exception {
        String xmlWithAllTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                "  <edmx:DataServices>\n" +
                "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.All\">\n" +
                "      <EntityType Name=\"Entity1\">\n" +
                "        <Key><PropertyRef Name=\"ID\"/></Key>\n" +
                "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
                "      </EntityType>\n" +
                "      <ComplexType Name=\"Complex1\">\n" +
                "        <Property Name=\"Prop1\" Type=\"Edm.String\"/>\n" +
                "      </ComplexType>\n" +
                "      <EnumType Name=\"Enum1\">\n" +
                "        <Member Name=\"Value1\" Value=\"0\"/>\n" +
                "      </EnumType>\n" +
                "      <TypeDefinition Name=\"TypeDef1\" UnderlyingType=\"Edm.String\"/>\n" +
                "      <Term Name=\"Term1\" Type=\"Edm.String\"/>\n" +
                "      <EntityContainer Name=\"Container1\">\n" +
                "        <EntitySet Name=\"Set1\" EntityType=\"Test.All.Entity1\"/>\n" +
                "      </EntityContainer>\n" +
                "    </Schema>\n" +
                "  </edmx:DataServices>\n" +
                "</edmx:Edmx>";

        InputStream inputStream = new ByteArrayInputStream(xmlWithAllTypes.getBytes("UTF-8"));
        
        List<CsdlSchema> schemas = parser.parseWithOlingoNative(inputStream, "test-all-types.xml");
        
        assertNotNull(schemas);
        CsdlSchema schema = schemas.get(0);
        
        // Test EntityType extension
        ExtendedCsdlEntityType entityType = (ExtendedCsdlEntityType) schema.getEntityTypes().get(0);
        assertNotNull(entityType.getElementId());
        assertNotNull(entityType.getElementFullyQualifiedName());
        assertNotNull(entityType.getElementDependencyType());
        
        // Test ComplexType extension  
        ExtendedCsdlComplexType complexType = (ExtendedCsdlComplexType) schema.getComplexTypes().get(0);
        assertNotNull(complexType.getElementId());
        assertNotNull(complexType.getElementFullyQualifiedName());
        assertNotNull(complexType.getElementDependencyType());
        
        // Test EnumType extension
        ExtendedCsdlEnumType enumType = (ExtendedCsdlEnumType) schema.getEnumTypes().get(0);
        assertNotNull(enumType.getElementId());
        assertNotNull(enumType.getElementFullyQualifiedName());
        assertNotNull(enumType.getElementDependencyType());
        
        // Test TypeDefinition extension - simplified test
        ExtendedCsdlTypeDefinition typeDef = (ExtendedCsdlTypeDefinition) schema.getTypeDefinitions().get(0);
        assertNotNull(typeDef.toString());
        assertTrue(typeDef.toString().contains("TypeDef1"));
        
        // Test Term extension  
        ExtendedCsdlTerm term = (ExtendedCsdlTerm) schema.getTerms().get(0);
        assertNotNull(term.getElementId());
        assertNotNull(term.getElementFullyQualifiedName());
        assertNotNull(term.getElementDependencyType());
        
        // Test EntityContainer extension
        ExtendedCsdlEntityContainer container = (ExtendedCsdlEntityContainer) schema.getEntityContainer();
        assertNotNull(container.getElementId());
        assertNotNull(container.getElementFullyQualifiedName());
        assertNotNull(container.getElementDependencyType());
    }

    @Test
    public void testToStringMethods() throws Exception {
        String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                "  <edmx:DataServices>\n" +
                "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.ToString\">\n" +
                "      <EntityType Name=\"TestEntity\">\n" +
                "        <Key><PropertyRef Name=\"ID\"/></Key>\n" +
                "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
                "      </EntityType>\n" +
                "      <ComplexType Name=\"TestComplex\">\n" +
                "        <Property Name=\"Prop\" Type=\"Edm.String\"/>\n" +
                "      </ComplexType>\n" +
                "      <EnumType Name=\"TestEnum\">\n" +
                "        <Member Name=\"Value\" Value=\"0\"/>\n" +
                "      </EnumType>\n" +
                "      <TypeDefinition Name=\"TestTypeDef\" UnderlyingType=\"Edm.String\"/>\n" +
                "      <Term Name=\"TestTerm\" Type=\"Edm.String\"/>\n" +
                "      <EntityContainer Name=\"TestContainer\">\n" +
                "        <EntitySet Name=\"TestSet\" EntityType=\"Test.ToString.TestEntity\"/>\n" +
                "      </EntityContainer>\n" +
                "    </Schema>\n" +
                "  </edmx:DataServices>\n" +
                "</edmx:Edmx>";

        InputStream inputStream = new ByteArrayInputStream(testXml.getBytes("UTF-8"));
        List<CsdlSchema> schemas = parser.parseWithOlingoNative(inputStream, "test-tostring.xml");
        
        CsdlSchema schema = schemas.get(0);
        
        // Test toString methods for all extension classes
        ExtendedCsdlEntityType entityType = (ExtendedCsdlEntityType) schema.getEntityTypes().get(0);
        assertNotNull(entityType.toString());
        assertTrue(entityType.toString().contains("TestEntity"));
        
        ExtendedCsdlComplexType complexType = (ExtendedCsdlComplexType) schema.getComplexTypes().get(0);
        assertNotNull(complexType.toString());
        System.out.println("ComplexType toString: " + complexType.toString());
        assertTrue("Expected 'TestComplex' in toString: " + complexType.toString(), 
                   complexType.toString().contains("TestComplex"));
        
        ExtendedCsdlEnumType enumType = (ExtendedCsdlEnumType) schema.getEnumTypes().get(0);
        assertNotNull(enumType.toString());
        assertTrue(enumType.toString().contains("TestEnum"));
        
        ExtendedCsdlTypeDefinition typeDef = (ExtendedCsdlTypeDefinition) schema.getTypeDefinitions().get(0);
        assertNotNull(typeDef.toString());
        assertTrue(typeDef.toString().contains("TestTypeDef"));
        
        ExtendedCsdlTerm term = (ExtendedCsdlTerm) schema.getTerms().get(0);
        assertNotNull(term.toString());
        assertTrue(term.toString().contains("TestTerm"));
        
        ExtendedCsdlEntityContainer container = (ExtendedCsdlEntityContainer) schema.getEntityContainer();
        assertNotNull(container.toString());
        assertTrue(container.toString().contains("TestContainer"));
    }

    @Test
    public void testComplexSchemaWithAllElements() throws Exception {
        String complexXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                "  <edmx:DataServices>\n" +
                "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Complex\">\n" +
                "      <EntityType Name=\"Order\">\n" +
                "        <Key><PropertyRef Name=\"ID\"/></Key>\n" +
                "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
                "        <Property Name=\"CustomerID\" Type=\"Edm.String\"/>\n" +
                "      </EntityType>\n" +
                "      <EntityType Name=\"Customer\">\n" +
                "        <Key><PropertyRef Name=\"ID\"/></Key>\n" +
                "        <Property Name=\"ID\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
                "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
                "      </EntityType>\n" +
                "      <ComplexType Name=\"Address\">\n" +
                "        <Property Name=\"Street\" Type=\"Edm.String\"/>\n" +
                "        <Property Name=\"City\" Type=\"Edm.String\"/>\n" +
                "        <Property Name=\"Country\" Type=\"Edm.String\"/>\n" +
                "      </ComplexType>\n" +
                "      <ComplexType Name=\"PhoneNumber\">\n" +
                "        <Property Name=\"Number\" Type=\"Edm.String\"/>\n" +
                "        <Property Name=\"Type\" Type=\"Test.Complex.PhoneType\"/>\n" +
                "      </ComplexType>\n" +
                "      <EnumType Name=\"PhoneType\">\n" +
                "        <Member Name=\"Home\" Value=\"0\"/>\n" +
                "        <Member Name=\"Work\" Value=\"1\"/>\n" +
                "        <Member Name=\"Mobile\" Value=\"2\"/>\n" +
                "      </EnumType>\n" +
                "      <EnumType Name=\"OrderStatus\">\n" +
                "        <Member Name=\"Pending\" Value=\"0\"/>\n" +
                "        <Member Name=\"Shipped\" Value=\"1\"/>\n" +
                "        <Member Name=\"Delivered\" Value=\"2\"/>\n" +
                "      </EnumType>\n" +
                "      <TypeDefinition Name=\"CustomerID\" UnderlyingType=\"Edm.String\"/>\n" +
                "      <TypeDefinition Name=\"OrderID\" UnderlyingType=\"Edm.Int32\"/>\n" +
                "      <EntityContainer Name=\"Service\">\n" +
                "        <EntitySet Name=\"Orders\" EntityType=\"Test.Complex.Order\"/>\n" +
                "        <EntitySet Name=\"Customers\" EntityType=\"Test.Complex.Customer\"/>\n" +
                "      </EntityContainer>\n" +
                "    </Schema>\n" +
                "  </edmx:DataServices>\n" +
                "</edmx:Edmx>";

        InputStream inputStream = new ByteArrayInputStream(complexXml.getBytes("UTF-8"));
        
        List<CsdlSchema> schemas = parser.parseWithOlingoNative(inputStream, "complex-test.xml");
        
        assertNotNull(schemas);
        assertEquals(1, schemas.size());
        
        CsdlSchema schema = schemas.get(0);
        assertEquals("Test.Complex", schema.getNamespace());
        
        // Verify EntityTypes
        assertEquals(2, schema.getEntityTypes().size());
        assertTrue(schema.getEntityTypes().get(0) instanceof ExtendedCsdlEntityType);
        
        // Verify ComplexTypes
        assertEquals(2, schema.getComplexTypes().size());
        assertTrue(schema.getComplexTypes().get(0) instanceof ExtendedCsdlComplexType);
        
        // Verify EnumTypes
        assertEquals(2, schema.getEnumTypes().size());
        assertTrue(schema.getEnumTypes().get(0) instanceof ExtendedCsdlEnumType);
        
        // Verify TypeDefinitions
        assertEquals(2, schema.getTypeDefinitions().size());
        assertTrue(schema.getTypeDefinitions().get(0) instanceof ExtendedCsdlTypeDefinition);
        
        // Verify EntityContainer
        assertTrue(schema.getEntityContainer() instanceof ExtendedCsdlEntityContainer);
        
        assertEquals(2, schema.getEntityContainer().getEntitySets().size());
    }
}