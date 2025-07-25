package org.apache.olingo.schemamanager.loader.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class DefaultODataXmlLoaderTest_loadFromDirectory {
    @Mock
    private ODataSchemaParser parser;
    @Mock
    private SchemaRepository repository;
    private DefaultODataXmlLoader loader;
    @TempDir
    Path tempDir;
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
    void testLoadFromDirectory_Success() throws IOException {
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadSingleFileFromResource("xml-schemas/valid/simple-schema.xml");
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
        verify(repository).addSchema(eq(mockSchema), anyString());
    }
    @Test
    void testLoadFromDirectory_NonExistentDirectory() {
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory("/non/existent/directory");
        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Directory not found"));
    }
    @Test
    void testLoadFromDirectory_ParseFailure() throws IOException {
        String invalidXmlContent = loadTestResourceAsString("xml-schemas/invalid/malformed-xml.xml");
        File xmlFile = tempDir.resolve("invalid.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(invalidXmlContent);
        }
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            null, new ArrayList<>(), false, "Parse error"
        );
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        verify(repository, never()).addSchema(any(), anyString());
    }
    @Test
    void testLoadFromDirectory_EmptyDirectory() {
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());
        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
    }
    @Test
    void testLoadFromDirectory_RecursiveLoading() throws IOException {
        File subDir = tempDir.resolve("subdir").toFile();
        subDir.mkdir();
        String simpleSchemaContent = loadTestResourceAsString("xml-schemas/valid/simple-schema.xml");
        String complexSchemaContent = loadTestResourceAsString("xml-schemas/valid/complex-types-schema.xml");
        File xmlFile1 = tempDir.resolve("test1.xml").toFile();
        File xmlFile2 = subDir.toPath().resolve("test2.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile1)) {
            writer.write(simpleSchemaContent);
        }
        try (FileWriter writer = new FileWriter(xmlFile2)) {
            writer.write(complexSchemaContent);
        }
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertEquals(2, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        verify(repository, times(2)).addSchema(eq(mockSchema), anyString());
    }
    // ==== 辅助方法 ====
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
