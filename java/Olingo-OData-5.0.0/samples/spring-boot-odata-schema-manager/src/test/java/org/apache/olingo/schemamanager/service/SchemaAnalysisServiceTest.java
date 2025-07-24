package org.apache.olingo.schemamanager.service;

import org.apache.olingo.schemamanager.analyzer.ODataSchemaAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaAnalysisServiceTest {

    @Mock
    private ODataSchemaAnalyzer analyzer;

    @InjectMocks
    private SchemaAnalysisService service;

    private ODataSchemaAnalyzer.AnalysisResult successResult;
    private ODataSchemaAnalyzer.AnalysisResult failureResult;
    private ODataSchemaAnalyzer.TypeDetailInfo sampleTypeInfo;

    // @BeforeEach
    // void setUp() {
    //     setupTestData();
    // }

    // private void setupTestData() {
    //     // Success result
    //     ODataSchemaAnalyzer.ImportValidationResult validImport = 
    //         new ODataSchemaAnalyzer.ImportValidationResult(true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        
    //     successResult = new ODataSchemaAnalyzer.AnalysisResult(
    //         true, new ArrayList<>(), new ArrayList<>(), 
    //         Map.of("TestService.Customer", Set.of("TestService.Address")),
    //         Map.of("TestService.Customer", "customer.xml"),
    //         validImport
    //     );

    //     // Failure result
    //     List<String> errors = Arrays.asList("Parse error in file1.xml", "Missing dependency");
    //     List<String> missingImports = Arrays.asList("TestService.MissingType");
    //     List<String> circularDeps = Arrays.asList("TypeA -> TypeB -> TypeA");
        
    //     ODataSchemaAnalyzer.ImportValidationResult invalidImport = 
    //         new ODataSchemaAnalyzer.ImportValidationResult(false, missingImports, new ArrayList<>(), circularDeps);
        
    //     failureResult = new ODataSchemaAnalyzer.AnalysisResult(
    //         false, errors, new ArrayList<>(), new HashMap<>(), new HashMap<>(), invalidImport
    //     );

    //     // Sample type info
    //     sampleTypeInfo = new ODataSchemaAnalyzer.TypeDetailInfo(
    //         "TestService.Customer",
    //         "TestService",
    //         "Customer", 
    //         ODataSchemaAnalyzer.TypeKind.ENTITY_TYPE,
    //         null, // typeDefinition
    //         Set.of("TestService.Address"),
    //         Set.of("TestService.Address", "TestService.Country"),
    //         Set.of("TestService.Order"),
    //         "customer.xml"
    //     );
    // }

    // @Test
    // void testPerformCompleteAnalysis_Success() {
    //     // Arrange
    //     String directoryPath = "/test/path";
        
    //     when(analyzer.analyzeDirectory(directoryPath)).thenReturn(successResult);
    //     when(analyzer.getStatistics()).thenReturn(Map.of(
    //         "Total Schemas", 2,
    //         "Total EntityTypes", 5,
    //         "Total ComplexTypes", 3
    //     ));
    //     when(analyzer.searchTypes("")).thenReturn(Arrays.asList(sampleTypeInfo));
    //     when(analyzer.searchTypes("Type")).thenReturn(Arrays.asList(sampleTypeInfo));
    //     when(analyzer.searchTypes("Entity")).thenReturn(Arrays.asList(sampleTypeInfo));

    //     // Capture console output
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     System.setOut(new PrintStream(outputStream));

    //     // Act
    //     service.performCompleteAnalysis(directoryPath);

    //     // Assert
    //     String output = outputStream.toString();
    //     assertTrue(output.contains("Analysis completed successfully"));
    //     assertTrue(output.contains("All imports are valid"));
    //     assertTrue(output.contains("Total Schemas: 2"));
    //     assertTrue(output.contains("TestService.Customer"));
        
    //     verify(analyzer).analyzeDirectory(directoryPath);
    //     verify(analyzer).getStatistics();
    //     verify(analyzer, atLeastOnce()).searchTypes(anyString());

    //     // Restore System.out
    //     System.setOut(System.out);
    // }

    // @Test
    // void testPerformCompleteAnalysis_Failure() {
    //     // Arrange
    //     String directoryPath = "/invalid/path";
        
    //     when(analyzer.analyzeDirectory(directoryPath)).thenReturn(failureResult);
    //     when(analyzer.getStatistics()).thenReturn(Map.of("Total Schemas", 0));
    //     when(analyzer.searchTypes("")).thenReturn(new ArrayList<>());
    //     when(analyzer.searchTypes("Type")).thenReturn(new ArrayList<>());
    //     when(analyzer.searchTypes("Entity")).thenReturn(new ArrayList<>());

    //     // Capture console output
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     System.setOut(new PrintStream(outputStream));

    //     // Act
    //     service.performCompleteAnalysis(directoryPath);

    //     // Assert
    //     String output = outputStream.toString();
    //     assertTrue(output.contains("Analysis failed with errors"));
    //     assertTrue(output.contains("Parse error in file1.xml"));
    //     assertTrue(output.contains("Import validation issues found"));
    //     assertTrue(output.contains("Missing: TestService.MissingType"));
    //     assertTrue(output.contains("Circular: TypeA -> TypeB -> TypeA"));
    //     assertTrue(output.contains("No types found in repository"));
        
    //     verify(analyzer).analyzeDirectory(directoryPath);

    //     // Restore System.out
    //     System.setOut(System.out);
    // }

    // @Test
    // void testGenerateDependencyReport_EntityType() {
    //     // Arrange
    //     String typeName = "TestService.Customer";
    //     when(analyzer.getEntityTypeDetail(typeName)).thenReturn(sampleTypeInfo);

    //     // Act
    //     String report = service.generateDependencyReport(typeName);

    //     // Assert
    //     assertNotNull(report);
    //     assertTrue(report.contains("Dependency Report for " + typeName));
    //     assertTrue(report.contains("Type Kind: ENTITY_TYPE"));
    //     assertTrue(report.contains("Namespace: TestService"));
    //     assertTrue(report.contains("Source File: customer.xml"));
    //     assertTrue(report.contains("Direct Dependencies (1)"));
    //     assertTrue(report.contains("TestService.Address"));
    //     assertTrue(report.contains("All Dependencies (2)"));
    //     assertTrue(report.contains("TestService.Country"));
    //     assertTrue(report.contains("Dependent Types (1)"));
    //     assertTrue(report.contains("TestService.Order"));
        
    //     verify(analyzer).getEntityTypeDetail(typeName);
    // }

    // @Test
    // void testGenerateDependencyReport_ComplexType() {
    //     // Arrange
    //     String typeName = "TestService.Address";
    //     when(analyzer.getEntityTypeDetail(typeName)).thenReturn(null);
    //     when(analyzer.getComplexTypeDetail(typeName)).thenReturn(sampleTypeInfo);

    //     // Act
    //     String report = service.generateDependencyReport(typeName);

    //     // Assert
    //     assertNotNull(report);
    //     assertTrue(report.contains("Dependency Report for " + typeName));
        
    //     verify(analyzer).getEntityTypeDetail(typeName);
    //     verify(analyzer).getComplexTypeDetail(typeName);
    // }

    // @Test
    // void testGenerateDependencyReport_EnumType() {
    //     // Arrange
    //     String typeName = "TestService.OrderStatus";
    //     when(analyzer.getEntityTypeDetail(typeName)).thenReturn(null);
    //     when(analyzer.getComplexTypeDetail(typeName)).thenReturn(null);
    //     when(analyzer.getEnumTypeDetail(typeName)).thenReturn(sampleTypeInfo);

    //     // Act
    //     String report = service.generateDependencyReport(typeName);

    //     // Assert
    //     assertNotNull(report);
    //     assertTrue(report.contains("Dependency Report for " + typeName));
        
    //     verify(analyzer).getEntityTypeDetail(typeName);
    //     verify(analyzer).getComplexTypeDetail(typeName);
    //     verify(analyzer).getEnumTypeDetail(typeName);
    // }

    // @Test
    // void testGenerateDependencyReport_TypeNotFound() {
    //     // Arrange
    //     String typeName = "TestService.NonExistent";
    //     when(analyzer.getEntityTypeDetail(typeName)).thenReturn(null);
    //     when(analyzer.getComplexTypeDetail(typeName)).thenReturn(null);
    //     when(analyzer.getEnumTypeDetail(typeName)).thenReturn(null);

    //     // Act
    //     String report = service.generateDependencyReport(typeName);

    //     // Assert
    //     assertNotNull(report);
    //     assertEquals("Type not found: " + typeName, report);
        
    //     verify(analyzer).getEntityTypeDetail(typeName);
    //     verify(analyzer).getComplexTypeDetail(typeName);
    //     verify(analyzer).getEnumTypeDetail(typeName);
    // }

    // @Test
    // void testClearData() {
    //     // Capture console output
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     System.setOut(new PrintStream(outputStream));

    //     // Act
    //     service.clearData();

    //     // Assert
    //     String output = outputStream.toString();
    //     assertTrue(output.contains("Data cleared"));

    //     // Restore System.out
    //     System.setOut(System.out);
    // }

    // @Test
    // void testPerformCompleteAnalysis_EmptyStatistics() {
    //     // Arrange
    //     String directoryPath = "/empty/path";
        
    //     when(analyzer.analyzeDirectory(directoryPath)).thenReturn(successResult);
    //     when(analyzer.getStatistics()).thenReturn(new HashMap<>());
    //     when(analyzer.searchTypes("")).thenReturn(new ArrayList<>());
    //     when(analyzer.searchTypes("Type")).thenReturn(new ArrayList<>());
    //     when(analyzer.searchTypes("Entity")).thenReturn(new ArrayList<>());

    //     // Capture console output
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     System.setOut(new PrintStream(outputStream));

    //     // Act
    //     service.performCompleteAnalysis(directoryPath);

    //     // Assert
    //     String output = outputStream.toString();
    //     assertTrue(output.contains("Analysis completed successfully"));
    //     assertTrue(output.contains("No types found in repository"));
        
    //     verify(analyzer).analyzeDirectory(directoryPath);
    //     verify(analyzer).getStatistics();

    //     // Restore System.out
    //     System.setOut(System.out);
    // }

    // @Test
    // void testPerformCompleteAnalysis_MultipleTypes() {
    //     // Arrange
    //     String directoryPath = "/test/path";
        
    //     ODataSchemaAnalyzer.TypeDetailInfo secondTypeInfo = new ODataSchemaAnalyzer.TypeDetailInfo(
    //         "TestService.Order",
    //         "TestService",
    //         "Order", 
    //         ODataSchemaAnalyzer.TypeKind.ENTITY_TYPE,
    //         null,
    //         Set.of("TestService.Customer"),
    //         Set.of("TestService.Customer"),
    //         new HashSet<>(),
    //         "order.xml"
    //     );

    //     List<ODataSchemaAnalyzer.TypeDetailInfo> multipleTypes = Arrays.asList(sampleTypeInfo, secondTypeInfo);
        
    //     when(analyzer.analyzeDirectory(directoryPath)).thenReturn(successResult);
    //     when(analyzer.getStatistics()).thenReturn(Map.of("Total Types", 2));
    //     when(analyzer.searchTypes("")).thenReturn(multipleTypes);
    //     when(analyzer.searchTypes("Type")).thenReturn(multipleTypes);
    //     when(analyzer.searchTypes("Entity")).thenReturn(multipleTypes);

    //     // Capture console output
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     System.setOut(new PrintStream(outputStream));

    //     // Act
    //     service.performCompleteAnalysis(directoryPath);

    //     // Assert
    //     String output = outputStream.toString();
    //     assertTrue(output.contains("TestService.Customer"));
    //     assertTrue(output.contains("TestService.Order"));
        
    //     verify(analyzer).analyzeDirectory(directoryPath);

    //     // Restore System.out
    //     System.setOut(System.out);
    // }

    // @Test
    // void testGenerateDependencyReport_WithNoDependencies() {
    //     // Arrange
    //     ODataSchemaAnalyzer.TypeDetailInfo noDepsTypeInfo = new ODataSchemaAnalyzer.TypeDetailInfo(
    //         "TestService.SimpleType",
    //         "TestService",
    //         "SimpleType", 
    //         ODataSchemaAnalyzer.TypeKind.ENUM_TYPE,
    //         null,
    //         new HashSet<>(),
    //         new HashSet<>(),
    //         new HashSet<>(),
    //         "simple.xml"
    //     );
        
    //     String typeName = "TestService.SimpleType";
    //     when(analyzer.getEntityTypeDetail(typeName)).thenReturn(null);
    //     when(analyzer.getComplexTypeDetail(typeName)).thenReturn(null);
    //     when(analyzer.getEnumTypeDetail(typeName)).thenReturn(noDepsTypeInfo);

    //     // Act
    //     String report = service.generateDependencyReport(typeName);

    //     // Assert
    //     assertNotNull(report);
    //     assertTrue(report.contains("Direct Dependencies (0)"));
    //     assertTrue(report.contains("All Dependencies (0)"));
    //     assertTrue(report.contains("Dependent Types (0)"));
        
    //     verify(analyzer).getEnumTypeDetail(typeName);
    // }

    // @Test
    // void testPerformCompleteAnalysis_PartialFailure() {
    //     // Arrange
    //     String directoryPath = "/partial/path";
        
    //     List<String> warnings = Arrays.asList("Warning: deprecated type usage");
    //     ODataSchemaAnalyzer.AnalysisResult partialResult = new ODataSchemaAnalyzer.AnalysisResult(
    //         true, new ArrayList<>(), warnings, 
    //         Map.of("TestService.Customer", Set.of("TestService.Address")),
    //         Map.of("TestService.Customer", "customer.xml"),
    //         successResult.getImportValidation()
    //     );
        
    //     when(analyzer.analyzeDirectory(directoryPath)).thenReturn(partialResult);
    //     when(analyzer.getStatistics()).thenReturn(Map.of("Warnings", 1));
    //     when(analyzer.searchTypes("")).thenReturn(new ArrayList<>());
    //     when(analyzer.searchTypes("Type")).thenReturn(new ArrayList<>());
    //     when(analyzer.searchTypes("Entity")).thenReturn(new ArrayList<>());

    //     // Capture console output
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     System.setOut(new PrintStream(outputStream));

    //     // Act
    //     service.performCompleteAnalysis(directoryPath);

    //     // Assert
    //     String output = outputStream.toString();
    //     assertTrue(output.contains("Analysis completed successfully"));
        
    //     verify(analyzer).analyzeDirectory(directoryPath);

    //     // Restore System.out
    //     System.setOut(System.out);
    // }
}
