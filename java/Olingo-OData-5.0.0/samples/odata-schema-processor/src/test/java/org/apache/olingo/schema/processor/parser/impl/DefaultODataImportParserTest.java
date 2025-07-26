package org.apache.olingo.schema.processor.parser.impl;

import org.apache.olingo.schema.processor.parser.ODataImportParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试DefaultODataImportParser的功能
 */
public class DefaultODataImportParserTest {
    
    private DefaultODataImportParser parser;
    
    @BeforeEach
    public void setUp() {
        parser = new DefaultODataImportParser();
    }
    
    @Test
    public void testParseImportsWithValidXml() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:Reference Uri=\"https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Core.V1.xml\">\n" +
            "        <edmx:Include Namespace=\"Org.OData.Core.V1\" Alias=\"Core\"/>\n" +
            "    </edmx:Reference>\n" +
            "    <edmx:Reference Uri=\"https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Capabilities.V1.xml\">\n" +
            "        <edmx:Include Namespace=\"Org.OData.Capabilities.V1\" Alias=\"Capabilities\"/>\n" +
            "    </edmx:Reference>\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"SampleService\">\n" +
            "            <EntityType Name=\"Product\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"ID\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"ID\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"Category\" Type=\"SampleService.Category\"/>\n" +
            "            </EntityType>\n" +
            "            <ComplexType Name=\"Category\">\n" +
            "                <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"Description\" Type=\"Edm.String\"/>\n" +
            "            </ComplexType>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        ODataImportParser.ImportParseResult result = parser.parseImports(xmlContent, "test.xml");
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
        
        List<ODataImportParser.ODataImport> imports = result.getImports();
        assertEquals(2, imports.size());
        
        // 验证第一个导入
        ODataImportParser.ODataImport import1 = imports.get(0);
        assertEquals("Org.OData.Core.V1", import1.getNamespace());
        assertEquals("Core", import1.getAlias());
        assertEquals("https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Core.V1.xml", import1.getInclude());
        
        // 验证第二个导入
        ODataImportParser.ODataImport import2 = imports.get(1);
        assertEquals("Org.OData.Capabilities.V1", import2.getNamespace());
        assertEquals("Capabilities", import2.getAlias());
        assertEquals("https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Capabilities.V1.xml", import2.getInclude());
    }
    
    @Test
    public void testParseImportsWithIncludeAnnotations() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:Reference Uri=\"https://example.com/annotations.xml\">\n" +
            "        <edmx:IncludeAnnotations TermNamespace=\"Org.OData.Core.V1\" Qualifier=\"UI\" TargetNamespace=\"SampleService\"/>\n" +
            "    </edmx:Reference>\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"SampleService\">\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        ODataImportParser.ImportParseResult result = parser.parseImports(xmlContent, "test.xml");
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        List<ODataImportParser.ODataImport> imports = result.getImports();
        assertEquals(1, imports.size());
        
        ODataImportParser.ODataImport import1 = imports.get(0);
        assertEquals("Org.OData.Core.V1", import1.getNamespace());
        assertEquals("UI", import1.getAlias());
        assertEquals("https://example.com/annotations.xml", import1.getInclude());
        assertEquals("SampleService", import1.getIncludeAnnotations());
    }
    
    @Test
    public void testExtractExternalReferences() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"SampleService\">\n" +
            "            <EntityType Name=\"Product\" BaseType=\"ExternalService.BaseProduct\">\n" +
            "                <Property Name=\"Category\" Type=\"ExternalService.Category\"/>\n" +
            "                <NavigationProperty Name=\"Supplier\" Type=\"ExternalService.Supplier\"/>\n" +
            "            </EntityType>\n" +
            "            <Action Name=\"ProcessOrder\" IsBound=\"true\">\n" +
            "                <Parameter Name=\"order\" Type=\"ExternalService.Order\"/>\n" +
            "                <ReturnType Type=\"ExternalService.OrderResult\"/>\n" +
            "            </Action>\n" +
            "            <EntitySet Name=\"Products\" EntityType=\"SampleService.Product\"/>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Set<String> declaredNamespaces = Set.of("SampleService");
        List<ODataImportParser.ExternalReference> references = parser.extractExternalReferences(xmlContent, declaredNamespaces);
        
        assertNotNull(references);
        assertTrue(references.size() >= 4); // BaseType, Category, Supplier, Order, OrderResult
        
        // 验证包含外部引用
        Set<String> referencedTypes = new HashSet<>();
        for (ODataImportParser.ExternalReference ref : references) {
            referencedTypes.add(ref.getFullyQualifiedName());
        }
        
        assertTrue(referencedTypes.contains("ExternalService.BaseProduct"));
        assertTrue(referencedTypes.contains("ExternalService.Category"));
        assertTrue(referencedTypes.contains("ExternalService.Supplier"));
        assertTrue(referencedTypes.contains("ExternalService.Order"));
        assertTrue(referencedTypes.contains("ExternalService.OrderResult"));
    }
    
    @Test
    public void testExtractExternalReferencesWithCollections() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"SampleService\">\n" +
            "            <EntityType Name=\"Product\">\n" +
            "                <Property Name=\"Categories\" Type=\"Collection(ExternalService.Category)\"/>\n" +
            "                <NavigationProperty Name=\"Suppliers\" Type=\"Collection(ExternalService.Supplier)\"/>\n" +
            "            </EntityType>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Set<String> declaredNamespaces = Set.of("SampleService");
        List<ODataImportParser.ExternalReference> references = parser.extractExternalReferences(xmlContent, declaredNamespaces);
        
        assertNotNull(references);
        assertTrue(references.size() >= 2);
        
        Set<String> referencedTypes = new HashSet<>();
        for (ODataImportParser.ExternalReference ref : references) {
            referencedTypes.add(ref.getFullyQualifiedName());
        }
        
        assertTrue(referencedTypes.contains("ExternalService.Category"));
        assertTrue(referencedTypes.contains("ExternalService.Supplier"));
    }
    
    @Test
    public void testValidateImportsSuccess() {
        List<ODataImportParser.ODataImport> imports = Arrays.asList(
            new ODataImportParser.ODataImport("ExternalService", "Ext", "http://example.com/external.xml", null),
            new ODataImportParser.ODataImport("Org.OData.Core.V1", "Core", "http://example.com/core.xml", null)
        );
        
        List<ODataImportParser.ExternalReference> externalReferences = Arrays.asList(
            new ODataImportParser.ExternalReference("ExternalService.Product", ODataImportParser.ReferenceType.ENTITY_TYPE, "EntityType[@BaseType]"),
            new ODataImportParser.ExternalReference("Org.OData.Core.V1.Description", ODataImportParser.ReferenceType.TERM, "Annotation[@Term]")
        );
        
        ODataImportParser.ImportValidationResult result = parser.validateImports(imports, externalReferences);
        
        assertNotNull(result);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getMissingImports().isEmpty());
    }
    
    @Test
    public void testValidateImportsWithMissingImports() {
        List<ODataImportParser.ODataImport> imports = Arrays.asList(
            new ODataImportParser.ODataImport("ExternalService", "Ext", "http://example.com/external.xml", null)
        );
        
        List<ODataImportParser.ExternalReference> externalReferences = Arrays.asList(
            new ODataImportParser.ExternalReference("ExternalService.Product", ODataImportParser.ReferenceType.ENTITY_TYPE, "EntityType[@BaseType]"),
            new ODataImportParser.ExternalReference("MissingService.Category", ODataImportParser.ReferenceType.COMPLEX_TYPE, "Property[@Type]")
        );
        
        ODataImportParser.ImportValidationResult result = parser.validateImports(imports, externalReferences);
        
        assertNotNull(result);
        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertEquals(1, result.getMissingImports().size());
        assertTrue(result.getMissingImports().contains("MissingService"));
    }
    
    @Test
    public void testValidateImportsWithUnusedImports() {
        List<ODataImportParser.ODataImport> imports = Arrays.asList(
            new ODataImportParser.ODataImport("ExternalService", "Ext", "http://example.com/external.xml", null),
            new ODataImportParser.ODataImport("UnusedService", "Unused", "http://example.com/unused.xml", null)
        );
        
        List<ODataImportParser.ExternalReference> externalReferences = Arrays.asList(
            new ODataImportParser.ExternalReference("ExternalService.Product", ODataImportParser.ReferenceType.ENTITY_TYPE, "EntityType[@BaseType]")
        );
        
        ODataImportParser.ImportValidationResult result = parser.validateImports(imports, externalReferences);
        
        assertNotNull(result);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getMissingImports().isEmpty());
        assertEquals(1, result.getUnusedImports().size());
        assertTrue(result.getUnusedImports().contains("UnusedService"));
        assertFalse(result.getWarnings().isEmpty());
    }
    
    @Test
    public void testParseInvalidXml() {
        String invalidXml = "<?xml version='1.0'?><invalid>";
        
        ODataImportParser.ImportParseResult result = parser.parseImports(invalidXml, "invalid.xml");
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getImports().isEmpty());
    }
    
    @Test
    public void testExtractExternalReferencesIgnoresEdmTypes() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"SampleService\">\n" +
            "            <EntityType Name=\"Product\">\n" +
            "                <Property Name=\"ID\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"Price\" Type=\"Edm.Decimal\"/>\n" +
            "                <Property Name=\"Category\" Type=\"ExternalService.Category\"/>\n" +
            "            </EntityType>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Set<String> declaredNamespaces = Set.of("SampleService");
        List<ODataImportParser.ExternalReference> references = parser.extractExternalReferences(xmlContent, declaredNamespaces);
        
        assertNotNull(references);
        
        // 验证只包含外部引用，不包含EDM类型
        for (ODataImportParser.ExternalReference ref : references) {
            assertFalse(ref.getFullyQualifiedName().startsWith("Edm."));
        }
        
        // 验证包含外部引用
        Set<String> referencedTypes = new HashSet<>();
        for (ODataImportParser.ExternalReference ref : references) {
            referencedTypes.add(ref.getFullyQualifiedName());
        }
        
        assertTrue(referencedTypes.contains("ExternalService.Category"));
    }
    
    @Test
    public void testODataImportClass() {
        ODataImportParser.ODataImport import1 = new ODataImportParser.ODataImport(
            "TestNamespace", "TestAlias", "http://example.com/test.xml", "TargetNamespace"
        );
        
        assertEquals("TestNamespace", import1.getNamespace());
        assertEquals("TestAlias", import1.getAlias());
        assertEquals("http://example.com/test.xml", import1.getInclude());
        assertEquals("TargetNamespace", import1.getIncludeAnnotations());
        
        // 测试toString
        String toString = import1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestNamespace"));
        assertTrue(toString.contains("TestAlias"));
    }
    
    @Test
    public void testExternalReferenceClass() {
        ODataImportParser.ExternalReference ref = new ODataImportParser.ExternalReference(
            "ExternalService.Product", ODataImportParser.ReferenceType.ENTITY_TYPE, "EntityType[@BaseType]"
        );
        
        assertEquals("ExternalService.Product", ref.getFullyQualifiedName());
        assertEquals(ODataImportParser.ReferenceType.ENTITY_TYPE, ref.getType());
        assertEquals("EntityType[@BaseType]", ref.getLocation());
        assertEquals("ExternalService", ref.getNamespace());
        
        // 测试toString
        String toString = ref.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ExternalService.Product"));
        assertTrue(toString.contains("ENTITY_TYPE"));
    }
    
    @Test
    public void testImportParseResultClass() {
        List<ODataImportParser.ODataImport> imports = Arrays.asList(
            new ODataImportParser.ODataImport("TestNamespace", "Test", "http://example.com", null)
        );
        List<ODataImportParser.ExternalReference> references = Arrays.asList(
            new ODataImportParser.ExternalReference("Test.Type", ODataImportParser.ReferenceType.ENTITY_TYPE, "location")
        );
        List<String> errors = Arrays.asList("Error 1");
        List<String> warnings = Arrays.asList("Warning 1");
        
        ODataImportParser.ImportParseResult result = new ODataImportParser.ImportParseResult(
            imports, references, false, errors, warnings
        );
        
        assertEquals(imports, result.getImports());
        assertEquals(references, result.getExternalReferences());
        assertFalse(result.isSuccess());
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        
        // 测试toString
        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("false"));
    }
    
    @Test
    public void testImportValidationResultClass() {
        List<String> missingImports = Arrays.asList("MissingNamespace");
        List<String> unusedImports = Arrays.asList("UnusedNamespace");
        List<String> errors = Arrays.asList("Error");
        List<String> warnings = Arrays.asList("Warning");
        
        ODataImportParser.ImportValidationResult result = new ODataImportParser.ImportValidationResult(
            false, missingImports, unusedImports, errors, warnings
        );
        
        assertFalse(result.isValid());
        assertEquals(missingImports, result.getMissingImports());
        assertEquals(unusedImports, result.getUnusedImports());
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
    }
}
