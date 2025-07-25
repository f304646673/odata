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

@ExtendWith(MockitoExtension.class)
class DefaultODataXmlLoaderTest_loadSingleFile {
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
    void testLoadSingleFile_Success() throws IOException {
        String xmlContent = loadTestResourceAsString("xml-schemas/valid/simple-schema.xml");
        File xmlFile = tempDir.resolve("single.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xmlContent);
        }
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadSingleFile(xmlFile.getAbsolutePath());
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
        verify(repository).addSchema(eq(mockSchema), anyString());
    }
    @Test
    void testLoadSingleFile_FileNotFound() {
        ODataXmlLoader.LoadResult result = loader.loadSingleFile("/non/existent/file.xml");
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
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
