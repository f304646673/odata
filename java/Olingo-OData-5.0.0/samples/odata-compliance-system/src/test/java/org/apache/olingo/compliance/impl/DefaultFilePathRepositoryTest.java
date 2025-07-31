package org.apache.olingo.compliance.impl;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultFilePathRepository.
 */
class DefaultFilePathRepositoryTest {
    
    private DefaultFilePathRepository repository;
    private Path testFilePath1;
    private Path testFilePath2;
    private CsdlSchema schema1;
    private CsdlSchema schema2;
    private CsdlSchema schema3;
    
    @BeforeEach
    void setUp() {
        repository = new DefaultFilePathRepository();
        testFilePath1 = Paths.get("/test/file1.xml");
        testFilePath2 = Paths.get("/test/file2.xml");
        
        schema1 = new CsdlSchema();
        schema1.setNamespace("Namespace1");
        
        schema2 = new CsdlSchema();
        schema2.setNamespace("Namespace2");
        
        schema3 = new CsdlSchema();
        schema3.setNamespace("Namespace1"); // Same namespace as schema1
    }
    
    @Test
    void testInitialState() {
        assertEquals(0, repository.size());
        assertEquals(0, repository.getTotalSchemaCount());
        assertTrue(repository.getAllFilePaths().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
        assertFalse(repository.contains(testFilePath1));
    }
    
    @Test
    void testStoreSingleSchema() {
        LocalDateTime validationTime = LocalDateTime.now();
        List<CsdlSchema> schemas = Arrays.asList(schema1);
        
        repository.storeSchemas(testFilePath1, schemas, validationTime, 1024L);
        
        assertEquals(1, repository.size());
        assertEquals(1, repository.getTotalSchemaCount());
        assertTrue(repository.contains(testFilePath1));
        
        List<CsdlSchema> retrievedSchemas = repository.getSchemas(testFilePath1);
        assertEquals(1, retrievedSchemas.size());
        assertEquals("Namespace1", retrievedSchemas.get(0).getNamespace());
        
        Optional<LocalDateTime> retrievedTime = repository.getValidationTime(testFilePath1);
        assertTrue(retrievedTime.isPresent());
        assertEquals(validationTime, retrievedTime.get());
        
        Optional<Long> fileSize = repository.getFileSize(testFilePath1);
        assertTrue(fileSize.isPresent());
        assertEquals(1024L, fileSize.get().longValue());
    }
    
    @Test
    void testStoreMultipleSchemas() {
        LocalDateTime validationTime = LocalDateTime.now();
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        repository.storeSchemas(testFilePath1, schemas, validationTime, 2048L);
        
        assertEquals(1, repository.size()); // One file
        assertEquals(2, repository.getTotalSchemaCount()); // Two schemas
        
        List<CsdlSchema> retrievedSchemas = repository.getSchemas(testFilePath1);
        assertEquals(2, retrievedSchemas.size());
        
        Set<String> namespaces = repository.getAllNamespaces();
        assertEquals(2, namespaces.size());
        assertTrue(namespaces.contains("Namespace1"));
        assertTrue(namespaces.contains("Namespace2"));
    }
    
    @Test
    void testGetSchemaByNamespace() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        repository.storeSchemas(testFilePath1, schemas, LocalDateTime.now(), 1024L);
        
        Optional<CsdlSchema> retrievedSchema1 = repository.getSchemaByNamespace(testFilePath1, "Namespace1");
        assertTrue(retrievedSchema1.isPresent());
        assertEquals("Namespace1", retrievedSchema1.get().getNamespace());
        
        Optional<CsdlSchema> retrievedSchema2 = repository.getSchemaByNamespace(testFilePath1, "Namespace2");
        assertTrue(retrievedSchema2.isPresent());
        assertEquals("Namespace2", retrievedSchema2.get().getNamespace());
        
        Optional<CsdlSchema> nonExistent = repository.getSchemaByNamespace(testFilePath1, "NonExistent");
        assertFalse(nonExistent.isPresent());
    }
    
    @Test
    void testGetFilePathsByNamespace() {
        repository.storeSchemas(testFilePath1, Arrays.asList(schema1), LocalDateTime.now(), 1024L);
        repository.storeSchemas(testFilePath2, Arrays.asList(schema2, schema3), LocalDateTime.now(), 2048L);
        
        List<Path> pathsWithNamespace1 = repository.getFilePathsByNamespace("Namespace1");
        assertEquals(2, pathsWithNamespace1.size()); // Both files contain Namespace1
        assertTrue(pathsWithNamespace1.contains(testFilePath1));
        assertTrue(pathsWithNamespace1.contains(testFilePath2));
        
        List<Path> pathsWithNamespace2 = repository.getFilePathsByNamespace("Namespace2");
        assertEquals(1, pathsWithNamespace2.size()); // Only file2 contains Namespace2
        assertTrue(pathsWithNamespace2.contains(testFilePath2));
        
        List<Path> pathsWithNonExistent = repository.getFilePathsByNamespace("NonExistent");
        assertTrue(pathsWithNonExistent.isEmpty());
    }
    
    @Test
    void testGetFileEntry() {
        LocalDateTime validationTime = LocalDateTime.now();
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        repository.storeSchemas(testFilePath1, schemas, validationTime, 1024L);
        
        Optional<DefaultFilePathRepository.FileEntry> entry = repository.getFileEntry(testFilePath1);
        assertTrue(entry.isPresent());
        
        DefaultFilePathRepository.FileEntry fileEntry = entry.get();
        assertEquals(testFilePath1, fileEntry.getFilePath());
        assertEquals(2, fileEntry.getSchemas().size());
        assertEquals(validationTime, fileEntry.getValidationTime());
        assertEquals(1024L, fileEntry.getFileSize());
        
        Set<String> namespaces = fileEntry.getNamespaces();
        assertEquals(2, namespaces.size());
        assertTrue(namespaces.contains("Namespace1"));
        assertTrue(namespaces.contains("Namespace2"));
    }
    
    @Test
    void testRemove() {
        repository.storeSchemas(testFilePath1, Arrays.asList(schema1), LocalDateTime.now(), 1024L);
        repository.storeSchemas(testFilePath2, Arrays.asList(schema2), LocalDateTime.now(), 2048L);
        
        Set<String> affectedNamespaces = repository.remove(testFilePath1);
        assertEquals(1, affectedNamespaces.size());
        assertTrue(affectedNamespaces.contains("Namespace1"));
        
        assertEquals(1, repository.size());
        assertFalse(repository.contains(testFilePath1));
        assertTrue(repository.contains(testFilePath2));
        
        // Test removing non-existent file
        Set<String> emptyResult = repository.remove(testFilePath1);
        assertTrue(emptyResult.isEmpty());
    }
    
    @Test
    void testClear() {
        repository.storeSchemas(testFilePath1, Arrays.asList(schema1), LocalDateTime.now(), 1024L);
        repository.storeSchemas(testFilePath2, Arrays.asList(schema2), LocalDateTime.now(), 2048L);
        
        repository.clear();
        
        assertEquals(0, repository.size());
        assertEquals(0, repository.getTotalSchemaCount());
        assertTrue(repository.getAllFilePaths().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
    }
    
    @Test
    void testGetNonExistentFile() {
        List<CsdlSchema> schemas = repository.getSchemas(testFilePath1);
        assertTrue(schemas.isEmpty());
        
        Optional<DefaultFilePathRepository.FileEntry> entry = repository.getFileEntry(testFilePath1);
        assertFalse(entry.isPresent());
        
        Optional<LocalDateTime> validationTime = repository.getValidationTime(testFilePath1);
        assertFalse(validationTime.isPresent());
        
        Optional<Long> fileSize = repository.getFileSize(testFilePath1);
        assertFalse(fileSize.isPresent());
    }


}
