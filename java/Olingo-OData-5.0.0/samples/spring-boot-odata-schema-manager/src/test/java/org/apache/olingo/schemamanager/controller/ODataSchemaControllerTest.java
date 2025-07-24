package org.apache.olingo.schemamanager.controller;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ODataSchemaControllerTest {

    @Mock
    private ODataXmlLoader xmlLoader;

    @Mock
    private SchemaRepository repository;

    @InjectMocks
    private ODataSchemaController controller;

    private MockMvc mockMvc;

    private CsdlSchema testSchema;

    // @BeforeEach
    // void setUp() {
    //     mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    //     setupTestData();
    // }

    // private void setupTestData() {
    //     testSchema = new CsdlSchema();
    //     testSchema.setNamespace("TestService");
    // }

    // @Test
    // void testLoadFromDirectory_Success() throws Exception {
    //     // Arrange
    //     String directoryPath = "/test/path";
    //     ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
    //         2, 2, 0, new ArrayList<>(), new HashMap<>()
    //     );
        
    //     when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);

    //     // Act & Assert
    //     mockMvc.perform(post("/api/odata/schema/load")
    //             .param("directoryPath", directoryPath))
    //             .andExpect(status().isOk());

    //     verify(xmlLoader).loadFromDirectory(directoryPath);
    // }

    // @Test
    // void testLoadFromDirectory_DirectCall() {
    //     // Arrange
    //     String directoryPath = "/test/path";
    //     ODataXmlLoader.LoadResult expectedResult = new ODataXmlLoader.LoadResult(
    //         2, 2, 0, new ArrayList<>(), new HashMap<>()
    //     );
        
    //     when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(expectedResult);

    //     // Act
    //     ODataXmlLoader.LoadResult result = controller.loadFromDirectory(directoryPath);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(expectedResult, result);
    //     verify(xmlLoader).loadFromDirectory(directoryPath);
    // }

    // @Test
    // void testGetAllSchemas_Success() throws Exception {
    //     // Arrange
    //     Map<String, CsdlSchema> schemas = Map.of("TestService", testSchema);
    //     when(repository.getAllSchemas()).thenReturn(schemas);

    //     // Act & Assert
    //     mockMvc.perform(get("/api/odata/schema/schemas"))
    //             .andExpect(status().isOk());

    //     verify(repository).getAllSchemas();
    // }

    // @Test
    // void testGetAllSchemas_DirectCall() {
    //     // Arrange
    //     Map<String, CsdlSchema> expectedSchemas = Map.of("TestService", testSchema);
    //     when(repository.getAllSchemas()).thenReturn(expectedSchemas);

    //     // Act
    //     Map<String, CsdlSchema> result = controller.getAllSchemas();

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(expectedSchemas, result);
    //     assertTrue(result.containsKey("TestService"));
    //     assertEquals(testSchema, result.get("TestService"));
    //     verify(repository).getAllSchemas();
    // }

    // @Test
    // void testGetSchema_Success() throws Exception {
    //     // Arrange
    //     String namespace = "TestService";
    //     when(repository.getSchema(namespace)).thenReturn(testSchema);

    //     // Act & Assert
    //     mockMvc.perform(get("/api/odata/schema/schemas/{namespace}", namespace))
    //             .andExpect(status().isOk());

    //     verify(repository).getSchema(namespace);
    // }

    // @Test
    // void testGetSchema_DirectCall() {
    //     // Arrange
    //     String namespace = "TestService";
    //     when(repository.getSchema(namespace)).thenReturn(testSchema);

    //     // Act
    //     CsdlSchema result = controller.getSchema(namespace);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(testSchema, result);
    //     assertEquals("TestService", result.getNamespace());
    //     verify(repository).getSchema(namespace);
    // }

    // @Test
    // void testGetSchema_NotFound() {
    //     // Arrange
    //     String namespace = "NonExistentService";
    //     when(repository.getSchema(namespace)).thenReturn(null);

    //     // Act
    //     CsdlSchema result = controller.getSchema(namespace);

    //     // Assert
    //     assertNull(result);
    //     verify(repository).getSchema(namespace);
    // }

    // @Test
    // void testGetAllNamespaces_Success() throws Exception {
    //     // Arrange
    //     Set<String> namespaces = Set.of("TestService", "AnotherService");
    //     when(repository.getAllNamespaces()).thenReturn(namespaces);

    //     // Act & Assert
    //     mockMvc.perform(get("/api/odata/schema/namespaces"))
    //             .andExpect(status().isOk());

    //     verify(repository).getAllNamespaces();
    // }

    // @Test
    // void testGetAllNamespaces_DirectCall() {
    //     // Arrange
    //     Set<String> expectedNamespaces = Set.of("TestService", "AnotherService");
    //     when(repository.getAllNamespaces()).thenReturn(expectedNamespaces);

    //     // Act
    //     Set<String> result = controller.getAllNamespaces();

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(expectedNamespaces, result);
    //     assertEquals(2, result.size());
    //     assertTrue(result.contains("TestService"));
    //     assertTrue(result.contains("AnotherService"));
    //     verify(repository).getAllNamespaces();
    // }

    // @Test
    // void testGetAllNamespaces_Empty() {
    //     // Arrange
    //     Set<String> emptyNamespaces = new HashSet<>();
    //     when(repository.getAllNamespaces()).thenReturn(emptyNamespaces);

    //     // Act
    //     Set<String> result = controller.getAllNamespaces();

    //     // Assert
    //     assertNotNull(result);
    //     assertTrue(result.isEmpty());
    //     verify(repository).getAllNamespaces();
    // }

    // @Test
    // void testGetStatistics_Success() throws Exception {
    //     // Arrange
    //     SchemaRepository.RepositoryStatistics stats = new SchemaRepository.RepositoryStatistics(
    //         2, 5, 3, 1, System.currentTimeMillis()
    //     );
    //     when(repository.getStatistics()).thenReturn(stats);

    //     // Act & Assert
    //     mockMvc.perform(get("/api/odata/schema/statistics"))
    //             .andExpect(status().isOk());

    //     verify(repository).getStatistics();
    // }

    // @Test
    // void testGetStatistics_DirectCall() {
    //     // Arrange
    //     SchemaRepository.RepositoryStatistics expectedStats = new SchemaRepository.RepositoryStatistics(
    //         2, 5, 3, 1, System.currentTimeMillis()
    //     );
    //     when(repository.getStatistics()).thenReturn(expectedStats);

    //     // Act
    //     SchemaRepository.RepositoryStatistics result = controller.getStatistics();

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(expectedStats, result);
    //     assertEquals(2, result.getSchemaCount());
    //     assertEquals(5, result.getEntityTypeCount());
    //     assertEquals(3, result.getComplexTypeCount());
    //     assertEquals(1, result.getEnumTypeCount());
    //     verify(repository).getStatistics();
    // }

    // @Test
    // void testClearAll_Success() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(delete("/api/odata/schema/clear"))
    //             .andExpect(status().isOk());

    //     verify(xmlLoader).clear();
    //     verify(repository).clear();
    // }

    // @Test
    // void testClearAll_DirectCall() {
    //     // Act
    //     controller.clearAll();

    //     // Assert
    //     verify(xmlLoader).clear();
    //     verify(repository).clear();
    // }

    // @Test
    // void testLoadFromDirectory_WithErrors() {
    //     // Arrange
    //     String directoryPath = "/invalid/path";
    //     List<String> errors = Arrays.asList("File not found", "Parse error");
    //     ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
    //         2, 0, 2, errors, new HashMap<>()
    //     );
        
    //     when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);

    //     // Act
    //     ODataXmlLoader.LoadResult result = controller.loadFromDirectory(directoryPath);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(2, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(2, result.getFailedFiles());
    //     assertEquals(errors, result.getErrorMessages());
    //     verify(xmlLoader).loadFromDirectory(directoryPath);
    // }

    // @Test
    // void testLoadFromDirectory_EmptyDirectory() {
    //     // Arrange
    //     String directoryPath = "/empty/path";
    //     ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
    //         0, 0, 0, new ArrayList<>(), new HashMap<>()
    //     );
        
    //     when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);

    //     // Act
    //     ODataXmlLoader.LoadResult result = controller.loadFromDirectory(directoryPath);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(0, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());
    //     verify(xmlLoader).loadFromDirectory(directoryPath);
    // }

    // @Test
    // void testGetAllSchemas_EmptyRepository() {
    //     // Arrange
    //     Map<String, CsdlSchema> emptySchemas = new HashMap<>();
    //     when(repository.getAllSchemas()).thenReturn(emptySchemas);

    //     // Act
    //     Map<String, CsdlSchema> result = controller.getAllSchemas();

    //     // Assert
    //     assertNotNull(result);
    //     assertTrue(result.isEmpty());
    //     verify(repository).getAllSchemas();
    // }

    // @Test
    // void testGetStatistics_EmptyRepository() {
    //     // Arrange
    //     SchemaRepository.RepositoryStatistics emptyStats = new SchemaRepository.RepositoryStatistics(
    //         0, 0, 0, 0, System.currentTimeMillis()
    //     );
    //     when(repository.getStatistics()).thenReturn(emptyStats);

    //     // Act
    //     SchemaRepository.RepositoryStatistics result = controller.getStatistics();

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(0, result.getSchemaCount());
    //     assertEquals(0, result.getEntityTypeCount());
    //     assertEquals(0, result.getComplexTypeCount());
    //     assertEquals(0, result.getEnumTypeCount());
    //     verify(repository).getStatistics();
    // }

    // @Test
    // void testControllerEndpoints_WithSpecialCharacters() {
    //     // Arrange
    //     String namespaceWithSpecialChars = "Test.Service-v1.0";
    //     when(repository.getSchema(namespaceWithSpecialChars)).thenReturn(testSchema);

    //     // Act
    //     CsdlSchema result = controller.getSchema(namespaceWithSpecialChars);

    //     // Assert
    //     assertNotNull(result);
    //     verify(repository).getSchema(namespaceWithSpecialChars);
    // }

    // @Test
    // void testClearAll_VerifySequence() {
    //     // Act
    //     controller.clearAll();

    //     // Assert - 验证调用顺序
    //     verify(xmlLoader).clear();
    //     verify(repository).clear();
        
    //     // 验证没有其他交互
    //     verifyNoMoreInteractions(xmlLoader);
    //     verifyNoMoreInteractions(repository);
    // }
}
