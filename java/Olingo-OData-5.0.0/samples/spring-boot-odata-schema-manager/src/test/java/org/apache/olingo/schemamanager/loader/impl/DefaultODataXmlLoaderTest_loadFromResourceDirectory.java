package org.apache.olingo.schemamanager.loader.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultODataXmlLoaderTest_loadFromResourceDirectory {
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
    void testLoadFromResourceDirectory_Success() {
        // 假设xml-schemas/valid下有多个xml文件
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );
        when(parser.parseSchema(any(InputStream.class), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadFromResourceDirectory("loader/valid");
        assertNotNull(result);
        assertTrue(result.getTotalFiles() > 0);
        assertEquals(result.getTotalFiles(), result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
        verify(parser, atLeastOnce()).parseSchema(any(InputStream.class), anyString());
        verify(repository, atLeastOnce()).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromResourceDirectory_EmptyDirectory() {
        ODataXmlLoader.LoadResult result = loader.loadFromResourceDirectory("loader/empty-directory");
        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
        verify(parser, never()).parseSchema(any(InputStream.class), anyString());
        verify(repository, never()).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromResourceDirectory_DirectoryNotExist() {
        ODataXmlLoader.LoadResult result = loader.loadFromResourceDirectory("loader/not-exist");
        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertTrue(result.getFailedFiles() == 0 || result.getFailedFiles() == 1);
        assertTrue(result.getErrorMessages().isEmpty() || result.getErrorMessages().get(0).contains("Resource directory scan error"));
        verify(parser, never()).parseSchema(any(InputStream.class), anyString());
        verify(repository, never()).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromResourceDirectory_WithInvalidXml() {
        // 有效和无效xml混合
        List<ODataSchemaParser.ParseResult> results = new ArrayList<>();
        CsdlSchema validSchema = new CsdlSchema();
        validSchema.setNamespace("Valid");
        results.add(new ODataSchemaParser.ParseResult(validSchema, new ArrayList<>(), true, null));
        results.add(new ODataSchemaParser.ParseResult(null, new ArrayList<>(), false, "Parse error!"));
        when(parser.parseSchema(any(InputStream.class), anyString()))
            .thenReturn(results.get(0))
            .thenReturn(results.get(1));
        ODataXmlLoader.LoadResult result = loader.loadFromResourceDirectory("loader/mixed");
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        verify(parser, times(2)).parseSchema(any(InputStream.class), anyString());
        verify(repository, times(1)).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromResourceDirectory_WithSubDirectory() {
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );
        when(parser.parseSchema(any(InputStream.class), anyString())).thenReturn(mockParseResult);
        ODataXmlLoader.LoadResult result = loader.loadFromResourceDirectory("loader/with-subdir");
        assertNotNull(result);
        assertEquals(5, result.getTotalFiles());
        assertEquals(result.getTotalFiles(), result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
        verify(parser, times(5)).parseSchema(any(InputStream.class), anyString());
        verify(repository, times(5)).addSchema(any(CsdlSchema.class), anyString());
    }
}
