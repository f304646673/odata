package org.apache.olingo.schemamanager.loader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultODataXmlLoaderTest_loadSingleFileFromResource {
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
    void testLoadSingleFileFromResource_Success() {
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
    void testLoadSingleFileFromResource_ResourceNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            loader.loadSingleFileFromResource("xml-schemas/not-exist.xml");
        });
        assertTrue(ex.getMessage().contains("Resource not found"));
        verify(repository, never()).addSchema(any(), anyString());
    }

    @Test
    void testLoadSingleFileFromResource_ParseFailure() {
        ODataSchemaParser.ParseResult failResult = new ODataSchemaParser.ParseResult(
            null, new ArrayList<>(), false, "Parse error!"
        );
        when(parser.parseSchema(any(), anyString())).thenReturn(failResult);
        ODataXmlLoader.LoadResult result = loader.loadSingleFileFromResource("xml-schemas/invalid/malformed-xml.xml");
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Parse error"));
        verify(repository, never()).addSchema(any(), anyString());
    }

    @Test
    void testLoadSingleFileFromResource_ParserThrowsException() {
        when(parser.parseSchema(any(), anyString())).thenThrow(new RuntimeException("Mock parser exception"));
        ODataXmlLoader.LoadResult result = loader.loadSingleFileFromResource("xml-schemas/valid/simple-schema.xml");
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Mock parser exception"));
        verify(repository, never()).addSchema(any(), anyString());
    }
}
