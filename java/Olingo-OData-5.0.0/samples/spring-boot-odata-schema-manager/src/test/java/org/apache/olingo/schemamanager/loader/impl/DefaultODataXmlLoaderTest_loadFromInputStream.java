package org.apache.olingo.schemamanager.loader.impl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultODataXmlLoaderTest_loadFromInputStream {
    @Mock
    private ODataSchemaParser parser;

    @Mock
    private SchemaRepository repository;

    private DefaultODataXmlLoader loader;

    @BeforeEach
    void setUp() throws Exception {
        loader = new DefaultODataXmlLoader();
        java.lang.reflect.Field parserField = DefaultODataXmlLoader.class.getDeclaredField("parser");
        parserField.setAccessible(true);
        parserField.set(loader, parser);
        java.lang.reflect.Field repositoryField = DefaultODataXmlLoader.class.getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(loader, repository);
    }
    
    @Test
    void testLoadFromInputStream_Success() throws IOException {
        String xmlContent = loadTestResourceAsString("loader/valid/simple-schema.xml");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService");
        
        // 使用新的ParseResult设计
        java.util.List<ODataSchemaParser.SchemaWithDependencies> schemaList = new ArrayList<>();
        schemaList.add(new ODataSchemaParser.SchemaWithDependencies(mockSchema, new ArrayList<>()));
        ODataSchemaParser.ParseResult mockParseResult = ODataSchemaParser.ParseResult.success(schemaList);
        
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
        Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = result.getLoadedFiles();
        assertTrue(loadedFiles.containsKey("test-source"));
        
        // 验证新的XmlFileInfo结构
        ODataXmlLoader.XmlFileInfo fileInfo = loadedFiles.get("test-source");
        assertEquals("TestService", fileInfo.getNamespace()); // 向后兼容方法
        assertEquals(1, fileInfo.getSchemaCount());
        assertFalse(fileInfo.hasMultipleSchemas());
        
        verify(repository).addSchema(eq(mockSchema), eq("test-source"));
    }

    @Test
    void testLoadFromInputStream_ParseFailure() throws IOException {
        String xmlContent = loadTestResourceAsString("loader/invalid/malformed-xml.xml");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        
        // 使用新的ParseResult设计
        ODataSchemaParser.ParseResult mockParseResult = ODataSchemaParser.ParseResult.failure("Parse error");
        
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Parse error"));
        verify(repository, never()).addSchema(any(), anyString());
    }

    private String loadTestResourceAsString(String relativePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(relativePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + relativePath);
            }
            return readInputStreamToString(inputStream);
        }
    }
    
    private String readInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytesRead, java.nio.charset.StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
