package org.apache.olingo.schemamanager.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OlingoSchemaParserImplTest {

    private OlingoSchemaParserImpl parser;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new OlingoSchemaParserImpl();
    }

    private String createValidXmlSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"TestService\">\n" +
            "            <EntityType Name=\"Customer\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"Age\" Type=\"Edm.Int32\"/>\n" +
            "            </EntityType>\n" +
            "            \n" +
            "            <ComplexType Name=\"Address\">\n" +
            "                <Property Name=\"Street\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"City\" Type=\"Edm.String\"/>\n" +
            "                <Property Name=\"PostalCode\" Type=\"Edm.String\"/>\n" +
            "            </ComplexType>\n" +
            "            \n" +
            "            <EnumType Name=\"OrderStatus\">\n" +
            "                <Member Name=\"Pending\" Value=\"0\"/>\n" +
            "                <Member Name=\"Processing\" Value=\"1\"/>\n" +
            "                <Member Name=\"Completed\" Value=\"2\"/>\n" +
            "            </EnumType>\n" +
            "            \n" +
            "            <EntityContainer Name=\"DefaultContainer\">\n" +
            "                <EntitySet Name=\"Customers\" EntityType=\"TestService.Customer\"/>\n" +
            "            </EntityContainer>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>\n";
    }

    private String createValidXmlSchemaWithImports() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"MainService\">\n" +
            "            <Using Namespace=\"ExternalService\" Alias=\"Ext\"/>\n" +
            "            <EntityType Name=\"Order\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"CustomerRef\" Type=\"Ext.Customer\"/>\n" +
            "            </EntityType>\n" +
            "            <EntityContainer Name=\"DefaultContainer\">\n" +
            "                <EntitySet Name=\"Orders\" EntityType=\"MainService.Order\"/>\n" +
            "            </EntityContainer>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>\n";
    }

    private String createInvalidXmlSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"TestService\">\n" +
            "            <EntityType Name=\"Customer\">\n" +
            "                <!-- Missing closing tag -->\n" +
            "            <ComplexType Name=\"Address\">\n" +
            "                <Property Name=\"Street\" Type=\"Edm.String\"/>\n" +
            "            </ComplexType>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>\n";
    }

    private String createEmptyXmlSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"EmptyService\">\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>\n";
    }

    @Test
    void testParseSchema_ValidSchema() throws IOException {
        String xmlContent = createValidXmlSchema();
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "test-schema.xml");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
        
        CsdlSchema schema = result.getSchema();
        assertNotNull(schema);
        assertEquals("TestService", schema.getNamespace());
        
        // 验证EntityTypes
        assertNotNull(schema.getEntityTypes());
        assertEquals(1, schema.getEntityTypes().size());
        assertEquals("Customer", schema.getEntityTypes().get(0).getName());
        
        // 验证ComplexTypes
        assertNotNull(schema.getComplexTypes());
        assertEquals(1, schema.getComplexTypes().size());
        assertEquals("Address", schema.getComplexTypes().get(0).getName());
        
        // 验证EnumTypes
        assertNotNull(schema.getEnumTypes());
        assertEquals(1, schema.getEnumTypes().size());
        assertEquals("OrderStatus", schema.getEnumTypes().get(0).getName());
        
        // 验证EntityContainer
        assertNotNull(schema.getEntityContainer());
        assertEquals("DefaultContainer", schema.getEntityContainer().getName());
    }

    @Test
    void testParseSchema_WithImports() throws IOException {
        String xmlContent = createValidXmlSchemaWithImports();
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "test-schema.xml");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        CsdlSchema schema = result.getSchema();
        assertNotNull(schema);
        assertEquals("MainService", schema.getNamespace());
        
        // 验证依赖关系
        List<String> dependencies = result.getDependencies();
        assertNotNull(dependencies);
        assertTrue(dependencies.contains("ExternalService"));
    }

    @Test
    void testParseSchema_EmptySchema() throws IOException {
        String xmlContent = createEmptyXmlSchema();
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "empty-schema.xml");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        CsdlSchema schema = result.getSchema();
        assertNotNull(schema);
        assertEquals("EmptyService", schema.getNamespace());
        
        // 验证空集合
        assertTrue(schema.getEntityTypes() == null || schema.getEntityTypes().isEmpty());
        assertTrue(schema.getComplexTypes() == null || schema.getComplexTypes().isEmpty());
        assertTrue(schema.getEnumTypes() == null || schema.getEnumTypes().isEmpty());
    }

    @Test
    void testParseSchema_InvalidXml() {
        String xmlContent = createInvalidXmlSchema();
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "invalid-schema.xml");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertNull(result.getSchema());
    }

    @Test
    void testParseSchema_NullInputStream() {
        ODataSchemaParser.ParseResult result = parser.parseSchema(null, "null-schema.xml");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertNull(result.getSchema());
    }

    @Test
    void testParseSchema_EmptyInputStream() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        ODataSchemaParser.ParseResult result = parser.parseSchema(emptyStream, "empty-stream.xml");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertNull(result.getSchema());
    }

    @Test
    void testParseSchema_NotXml() {
        String notXml = "This is not XML content";
        InputStream inputStream = new ByteArrayInputStream(notXml.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "not-xml.txt");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertNull(result.getSchema());
    }

    @Test
    void testExtractDependencies_WithImports() throws IOException {
        String xmlContent = createValidXmlSchemaWithImports();
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "test-schema.xml");
        CsdlSchema schema = result.getSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        assertFalse(dependencies.isEmpty());
        assertTrue(dependencies.contains("ExternalService"));
    }

    @Test
    void testExtractDependencies_NoDependencies() throws IOException {
        String xmlContent = createValidXmlSchema();
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "test-schema.xml");
        CsdlSchema schema = result.getSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void testExtractDependencies_NullSchema() {
        List<String> dependencies = parser.extractDependencies(null);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void testValidateSchema_ValidSchema() throws IOException {
        String xmlContent = createValidXmlSchema();
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "test-schema.xml");
        CsdlSchema schema = result.getSchema();
        
        ODataSchemaParser.ValidationResult validationResult = parser.validateSchema(schema);
        
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getErrors().isEmpty());
    }

    @Test
    void testValidateSchema_NullSchema() {
        ODataSchemaParser.ValidationResult validationResult = parser.validateSchema(null);
        
        assertNotNull(validationResult);
        assertFalse(validationResult.isValid());
        assertFalse(validationResult.getErrors().isEmpty());
    }

    @Test
    void testParseComplexEntityType() throws IOException {
        String complexEntityXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"ComplexService\">\n" +
            "            <EntityType Name=\"ComplexEntity\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "                <Property Name=\"Price\" Type=\"Edm.Decimal\" Precision=\"10\" Scale=\"2\"/>\n" +
            "                <Property Name=\"CreatedDate\" Type=\"Edm.DateTimeOffset\"/>\n" +
            "                <Property Name=\"IsActive\" Type=\"Edm.Boolean\" DefaultValue=\"true\"/>\n" +
            "                <NavigationProperty Name=\"Category\" Type=\"ComplexService.Category\"/>\n" +
            "            </EntityType>\n" +
            "            \n" +
            "            <EntityType Name=\"Category\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "            </EntityType>\n" +
            "            \n" +
            "            <EntityContainer Name=\"DefaultContainer\">\n" +
            "                <EntitySet Name=\"ComplexEntities\" EntityType=\"ComplexService.ComplexEntity\">\n" +
            "                    <NavigationPropertyBinding Path=\"Category\" Target=\"Categories\"/>\n" +
            "                </EntitySet>\n" +
            "                <EntitySet Name=\"Categories\" EntityType=\"ComplexService.Category\"/>\n" +
            "            </EntityContainer>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>\n";

        InputStream inputStream = new ByteArrayInputStream(complexEntityXml.getBytes());
        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "complex-schema.xml");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        CsdlSchema schema = result.getSchema();
        assertNotNull(schema);
        assertEquals("ComplexService", schema.getNamespace());
        
        // 验证EntityTypes
        assertEquals(2, schema.getEntityTypes().size());
        
        // 查找ComplexEntity
        org.apache.olingo.commons.api.edm.provider.CsdlEntityType complexEntity = null;
        for (org.apache.olingo.commons.api.edm.provider.CsdlEntityType et : schema.getEntityTypes()) {
            if ("ComplexEntity".equals(et.getName())) {
                complexEntity = et;
                break;
            }
        }
        
        assertNotNull(complexEntity);
        assertEquals(5, complexEntity.getProperties().size()); // 5个Property元素，不包括NavigationProperty
        
        // 验证NavigationProperty
        assertNotNull(complexEntity.getNavigationProperties());
        assertEquals(1, complexEntity.getNavigationProperties().size());
        assertEquals("Category", complexEntity.getNavigationProperties().get(0).getName());
    }

    @Test
    void testParseFromFile() throws IOException {
        // 创建临时文件
        java.io.File xmlFile = tempDir.resolve("schema.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(createValidXmlSchema());
        }

        try (FileInputStream fileInputStream = new FileInputStream(xmlFile)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fileInputStream, xmlFile.getName());

            assertNotNull(result);
            assertTrue(result.isSuccess());
            
            CsdlSchema schema = result.getSchema();
            assertNotNull(schema);
            assertEquals("TestService", schema.getNamespace());
            assertNotNull(schema.getEntityTypes());
            assertEquals(1, schema.getEntityTypes().size());
        }
    }

    @Test
    void testErrorHandling_CorruptedXml() throws IOException {
        // 创建损坏的XML内容
        String corruptedXml = "<?xml version=\"1.0\"?><invalid><unclosed>";
        InputStream inputStream = new ByteArrayInputStream(corruptedXml.getBytes());

        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "corrupted.xml");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertNull(result.getSchema());
    }

    @Test
    void testLargeXmlFile() throws IOException {
        // 创建较大的XML文件（模拟真实场景）
        StringBuilder largeXml = new StringBuilder();
        largeXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
               .append("<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n")
               .append("    <edmx:DataServices>\n")
               .append("        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"LargeService\">\n");

        // 添加多个EntityType
        for (int i = 1; i <= 50; i++) {
            largeXml.append("            <EntityType Name=\"Entity").append(i).append("\">\n")
                   .append("                <Key>\n")
                   .append("                    <PropertyRef Name=\"Id\"/>\n")
                   .append("                </Key>\n")
                   .append("                <Property Name=\"Id\" Type=\"Edm.String\" Nullable=\"false\"/>\n")
                   .append("                <Property Name=\"Name").append(i).append("\" Type=\"Edm.String\"/>\n")
                   .append("            </EntityType>\n");
        }

        largeXml.append("            <EntityContainer Name=\"DefaultContainer\">\n")
               .append("            </EntityContainer>\n")
               .append("        </Schema>\n")
               .append("    </edmx:DataServices>\n")
               .append("</edmx:Edmx>\n");

        InputStream inputStream = new ByteArrayInputStream(largeXml.toString().getBytes());
        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "large-schema.xml");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        CsdlSchema schema = result.getSchema();
        assertNotNull(schema);
        assertEquals("LargeService", schema.getNamespace());
        assertEquals(50, schema.getEntityTypes().size());
    }

    @Test
    void testMultipleNamespaces() throws IOException {
        String multipleNamespacesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Service1\">\n" +
            "            <Using Namespace=\"Service2\" Alias=\"S2\"/>\n" +
            "            <Using Namespace=\"Service3\" Alias=\"S3\"/>\n" +
            "            <EntityType Name=\"Entity1\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"Id\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"Id\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"RelatedEntity2\" Type=\"S2.Entity2\"/>\n" +
            "                <Property Name=\"RelatedEntity3\" Type=\"S3.Entity3\"/>\n" +
            "            </EntityType>\n" +
            "            <EntityContainer Name=\"DefaultContainer\"/>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>\n";

        InputStream inputStream = new ByteArrayInputStream(multipleNamespacesXml.getBytes());
        ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "multiple-namespaces.xml");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        List<String> dependencies = result.getDependencies();
        assertNotNull(dependencies);
        assertTrue(dependencies.contains("Service2"));
        assertTrue(dependencies.contains("Service3"));
    }

    // ==== 使用测试资源文件的测试方法 ====

    @Test
    void testParseSchema_FromTestResources_SimpleSchema() throws IOException {
        // 测试解析简单的Schema
        String resourcePath = "src/test/resources/xml-schemas/loader/valid/simple-schema.xml";
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fis, "simple-schema.xml");

            assertNotNull(result);
            assertTrue(result.isSuccess());

            CsdlSchema schema = result.getSchema();
            assertNotNull(schema);
            assertEquals("TestService", schema.getNamespace());

            // 验证EntityType
            assertNotNull(schema.getEntityTypes());
            assertEquals(1, schema.getEntityTypes().size());
            assertEquals("Customer", schema.getEntityTypes().get(0).getName());

            // 验证EntityContainer
            assertNotNull(schema.getEntityContainer());
            assertEquals("DefaultContainer", schema.getEntityContainer().getName());
        }
    }

    @Test
    void testParseSchema_FromTestResources_ComplexTypesSchema() throws IOException {
        // 测试解析包含ComplexType的Schema
        String resourcePath = "src/test/resources/xml-schemas/loader/valid/complex-types-schema.xml";
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fis, "complex-types-schema.xml");

            assertNotNull(result);
            assertTrue(result.isSuccess());

            CsdlSchema schema = result.getSchema();
            assertNotNull(schema);
            assertEquals("TestService", schema.getNamespace());

            // 验证ComplexTypes
            assertNotNull(schema.getComplexTypes());
            assertEquals(2, schema.getComplexTypes().size());

            // 验证Address ComplexType
            boolean foundAddress = schema.getComplexTypes().stream()
                .anyMatch(ct -> "Address".equals(ct.getName()));
            assertTrue(foundAddress);

            // 验证Country ComplexType
            boolean foundCountry = schema.getComplexTypes().stream()
                .anyMatch(ct -> "Country".equals(ct.getName()));
            assertTrue(foundCountry);

            // 验证EntityType with ComplexType properties
            assertNotNull(schema.getEntityTypes());
            assertEquals(1, schema.getEntityTypes().size());
            assertEquals("Customer", schema.getEntityTypes().get(0).getName());

            // 验证EntityType的properties包含复杂类型
            assertNotNull(schema.getEntityTypes().get(0).getProperties());
            boolean hasAddressProperty = schema.getEntityTypes().get(0).getProperties().stream()
                .anyMatch(p -> "Address".equals(p.getName()) && "TestService.Address".equals(p.getType()));
            assertTrue(hasAddressProperty);
        }
    }

    @Test
    void testParseSchema_FromTestResources_FullSchema() throws IOException {
        // 测试解析完整的Schema（包含EntityType、ComplexType、EnumType、NavigationProperty）
        String resourcePath = "src/test/resources/xml-schemas/loader/valid/full-schema.xml";
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fis, "full-schema.xml");

            assertNotNull(result);
            assertTrue(result.isSuccess());

            CsdlSchema schema = result.getSchema();
            assertNotNull(schema);
            assertEquals("TestService", schema.getNamespace());

            // 验证EnumTypes
            assertNotNull(schema.getEnumTypes());
            assertEquals(2, schema.getEnumTypes().size());

            boolean foundOrderStatus = schema.getEnumTypes().stream()
                .anyMatch(et -> "OrderStatus".equals(et.getName()));
            assertTrue(foundOrderStatus);

            boolean foundCustomerType = schema.getEnumTypes().stream()
                .anyMatch(et -> "CustomerType".equals(et.getName()));
            assertTrue(foundCustomerType);

            // 验证ComplexTypes
            assertNotNull(schema.getComplexTypes());
            assertEquals(2, schema.getComplexTypes().size());

            // 验证EntityTypes
            assertNotNull(schema.getEntityTypes());
            assertEquals(3, schema.getEntityTypes().size()); // Customer, Order, OrderItem

            // 验证NavigationProperties
            boolean foundNavigationProperty = schema.getEntityTypes().stream()
                .anyMatch(et -> et.getNavigationProperties() != null && !et.getNavigationProperties().isEmpty());
            assertTrue(foundNavigationProperty);

            // 验证EntityContainer with NavigationPropertyBindings
            assertNotNull(schema.getEntityContainer());
            assertEquals("DefaultContainer", schema.getEntityContainer().getName());
            assertNotNull(schema.getEntityContainer().getEntitySets());
            assertEquals(3, schema.getEntityContainer().getEntitySets().size());
        }
    }

    @Test
    void testParseSchema_FromTestResources_MalformedXml() throws IOException {
        // 测试解析格式错误的XML
        String resourcePath = "src/test/resources/xml-schemas/loader/invalid/malformed-xml.xml";
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fis, "malformed-xml.xml");
            // 期望解析失败而不是抛出异常
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorMessage());
        }
    }

    @Test
    void testParseSchema_FromTestResources_LargeSchema() throws IOException {
        // 测试解析大型Schema的性能
        String resourcePath = "src/test/resources/xml-schemas/loader/performance/large-schema.xml";
        
        long startTime = System.currentTimeMillis();
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fis, "large-schema.xml");

            assertNotNull(result);
            assertTrue(result.isSuccess());

            CsdlSchema schema = result.getSchema();
            assertNotNull(schema);
            assertEquals("TestService", schema.getNamespace());

            // 验证包含多个EntityType
            assertNotNull(schema.getEntityTypes());
            assertTrue(schema.getEntityTypes().size() >= 5);

            // 验证包含ComplexType
            assertNotNull(schema.getComplexTypes());
            assertTrue(schema.getComplexTypes().size() >= 2);

            // 验证包含EnumType
            assertNotNull(schema.getEnumTypes());
            assertTrue(schema.getEnumTypes().size() >= 1);
        }
        long endTime = System.currentTimeMillis();

        // 验证性能
        assertTrue(endTime - startTime < 3000, "Parsing took too long: " + (endTime - startTime) + "ms");
    }

    @Test
    void testParseSchema_FromTestResources_CircularDependencySchema() throws IOException {
        // 测试解析包含循环依赖的Schema
        String resourcePath = "src/test/resources/xml-schemas/loader/complex/circular-dependency-schema.xml";
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fis, "circular-dependency-schema.xml");

            assertNotNull(result);
            assertTrue(result.isSuccess());

            CsdlSchema schema = result.getSchema();
            assertNotNull(schema);
            assertEquals("TestService", schema.getNamespace());

            // 验证循环依赖的ComplexTypes
            assertNotNull(schema.getComplexTypes());
            assertTrue(schema.getComplexTypes().size() >= 5); // CircularA, CircularB, CircularX, CircularY, CircularZ

            // 验证CircularA和CircularB的相互引用
            boolean foundCircularA = schema.getComplexTypes().stream()
                .anyMatch(ct -> "CircularA".equals(ct.getName()));
            assertTrue(foundCircularA);

            boolean foundCircularB = schema.getComplexTypes().stream()
                .anyMatch(ct -> "CircularB".equals(ct.getName()));
            assertTrue(foundCircularB);

            // 验证EntityType
            assertNotNull(schema.getEntityTypes());
            assertTrue(schema.getEntityTypes().size() >= 1);
            
            boolean foundCircularEntity = schema.getEntityTypes().stream()
                .anyMatch(et -> "CircularEntity".equals(et.getName()));
            assertTrue(foundCircularEntity);
        }
    }

    @Test
    void testParseSchema_FromTestResources_MultiDependencySchema() throws IOException {
        // 测试解析复杂依赖关系的Schema
        String resourcePath = "src/test/resources/xml-schemas/loader/complex/multi-dependency-schema.xml";
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(fis, "multi-dependency-schema.xml");

            assertNotNull(result);
            assertTrue(result.isSuccess());

            CsdlSchema schema = result.getSchema();
            assertNotNull(schema);
            assertEquals("TestService", schema.getNamespace());

            // 验证复杂依赖链的ComplexTypes (A->B->C->D)
            assertNotNull(schema.getComplexTypes());
            assertTrue(schema.getComplexTypes().size() >= 4);

            String[] expectedTypes = {"TypeA", "TypeB", "TypeC", "TypeD"};
            for (String expectedType : expectedTypes) {
                boolean found = schema.getComplexTypes().stream()
                    .anyMatch(ct -> expectedType.equals(ct.getName()));
                assertTrue(found, "Should contain ComplexType: " + expectedType);
            }

            // 验证MultiDependencyEntity
            assertNotNull(schema.getEntityTypes());
            assertTrue(schema.getEntityTypes().size() >= 1);
            
            boolean foundMultiEntity = schema.getEntityTypes().stream()
                .anyMatch(et -> "MultiDependencyEntity".equals(et.getName()));
            assertTrue(foundMultiEntity);

            // 验证EnumType
            assertNotNull(schema.getEnumTypes());
            boolean foundMultiStatus = schema.getEnumTypes().stream()
                .anyMatch(et -> "MultiStatus".equals(et.getName()));
            assertTrue(foundMultiStatus);
        }
    }

    @Test
    void testTestResourcesAvailability() {
        // 验证所有测试资源文件都可用
        String[] testResourcePaths = {
            "src/test/resources/xml-schemas/loader/valid/simple-schema.xml",
            "src/test/resources/xml-schemas/loader/valid/complex-types-schema.xml",
            "src/test/resources/xml-schemas/loader/valid/full-schema.xml",
            "src/test/resources/xml-schemas/loader/invalid/malformed-xml.xml",
            "src/test/resources/xml-schemas/loader/invalid/invalid-types.xml",
            "src/test/resources/xml-schemas/loader/complex/multi-dependency-schema.xml",
            "src/test/resources/xml-schemas/loader/complex/circular-dependency-schema.xml",
            "src/test/resources/xml-schemas/loader/performance/large-schema.xml"
        };

        for (String resourcePath : testResourcePaths) {
            java.io.File file = new java.io.File(resourcePath);
            assertTrue(file.exists(), "Test resource should exist: " + resourcePath);
            assertTrue(file.canRead(), "Test resource should be readable: " + resourcePath);
        }
    }
}
