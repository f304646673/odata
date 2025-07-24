package org.apache.olingo.schemamanager.analyzer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ODataSchemaAnalyzerTest {

    @Mock
    private ODataXmlLoader xmlLoader;

    @Mock
    private SchemaRepository repository;

    @InjectMocks
    private ODataSchemaAnalyzer analyzer;

    @TempDir
    Path tempDir;

    private CsdlSchema testSchema;
    private CsdlEntityType customerEntity;
    private CsdlComplexType addressComplex;
    private CsdlEnumType orderStatusEnum;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // Customer EntityType
        customerEntity = new CsdlEntityType();
        customerEntity.setName("Customer");
        
        CsdlProperty custIdProp = new CsdlProperty();
        custIdProp.setName("Id");
        custIdProp.setType("Edm.String");
        
        CsdlProperty custAddressProp = new CsdlProperty();
        custAddressProp.setName("Address");
        custAddressProp.setType("TestService.Address");
        
        customerEntity.setProperties(Arrays.asList(custIdProp, custAddressProp));

        // Address ComplexType
        addressComplex = new CsdlComplexType();
        addressComplex.setName("Address");
        
        CsdlProperty streetProp = new CsdlProperty();
        streetProp.setName("Street");
        streetProp.setType("Edm.String");
        
        CsdlProperty statusProp = new CsdlProperty();
        statusProp.setName("Status");
        statusProp.setType("TestService.OrderStatus");
        
        addressComplex.setProperties(Arrays.asList(streetProp, statusProp));

        // OrderStatus EnumType
        orderStatusEnum = new CsdlEnumType();
        orderStatusEnum.setName("OrderStatus");
        
        CsdlEnumMember pending = new CsdlEnumMember();
        pending.setName("Pending");
        pending.setValue("0");
        
        orderStatusEnum.setMembers(Arrays.asList(pending));

        // Test Schema
        testSchema = new CsdlSchema();
        testSchema.setNamespace("TestService");
        testSchema.setEntityTypes(Arrays.asList(customerEntity));
        testSchema.setComplexTypes(Arrays.asList(addressComplex));
        testSchema.setEnumTypes(Arrays.asList(orderStatusEnum));
    }

    @Test
    void testAnalyzeDirectory_Success() {
        // Arrange
        String directoryPath = tempDir.toString();
        
        ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
            2, 2, 0, new ArrayList<>(), new HashMap<>()
        );
        
        when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", testSchema);
        when(repository.getAllSchemas()).thenReturn(schemas);

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
        assertNotNull(result.getDependencies());
        assertNotNull(result.getTypeLocations());
        assertNotNull(result.getImportValidation());
        
        // 验证依赖关系
        Map<String, Set<String>> dependencies = result.getDependencies();
        assertTrue(dependencies.containsKey("TestService.Customer"));
        assertTrue(dependencies.get("TestService.Customer").contains("TestService.Address"));
        
        verify(xmlLoader).loadFromDirectory(directoryPath);
        verify(repository).getAllSchemas();
    }

    @Test
    void testAnalyzeDirectory_LoadFailures() {
        // Arrange
        String directoryPath = tempDir.toString();
        
        List<String> errors = Arrays.asList("Failed to load file1.xml", "Failed to load file2.xml");
        ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
            3, 1, 2, errors, new HashMap<>()
        );
        
        when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", testSchema);
        lenient().when(repository.getAllSchemas()).thenReturn(schemas);

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().containsAll(errors));
        
        verify(xmlLoader).loadFromDirectory(directoryPath);
    }

    @Test
    void testAnalyzeDirectory_Exception() {
        // Arrange
        String directoryPath = "/invalid/path";
        
        when(xmlLoader.loadFromDirectory(directoryPath))
            .thenThrow(new RuntimeException("Directory not found"));

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("Directory not found"));
        
        verify(xmlLoader).loadFromDirectory(directoryPath);
    }

    @Test
    void testGetEntityTypeDetail_Success() {
        // Arrange
        String fullQualifiedName = "TestService.Customer";
        when(repository.getEntityType("TestService.Customer")).thenReturn(customerEntity);

        // Act
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getEntityTypeDetail(fullQualifiedName);

        // Assert
        assertNotNull(detail);
        assertEquals(fullQualifiedName, detail.getFullQualifiedName());
        assertEquals("TestService", detail.getNamespace());
        assertEquals("Customer", detail.getTypeName());
        assertEquals(ODataSchemaAnalyzer.TypeKind.ENTITY_TYPE, detail.getTypeKind());
        assertEquals(customerEntity, detail.getTypeDefinition());
        
        // 验证依赖关系
        Set<String> directDeps = detail.getDirectDependencies();
        assertNotNull(directDeps);
        assertTrue(directDeps.contains("TestService.Address"));
        
        verify(repository).getEntityType("TestService.Customer");
    }

    @Test
    void testGetEntityTypeDetail_NotFound() {
        // Arrange
        String fullQualifiedName = "TestService.NonExistent";
        when(repository.getEntityType("TestService.NonExistent")).thenReturn(null);

        // Act
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getEntityTypeDetail(fullQualifiedName);

        // Assert
        assertNull(detail);
        verify(repository).getEntityType("TestService.NonExistent");
    }

    @Test
    void testGetComplexTypeDetail_Success() {
        // Arrange
        String fullQualifiedName = "TestService.Address";
        when(repository.getComplexType("TestService.Address")).thenReturn(addressComplex);

        // Act
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getComplexTypeDetail(fullQualifiedName);

        // Assert
        assertNotNull(detail);
        assertEquals(fullQualifiedName, detail.getFullQualifiedName());
        assertEquals("TestService", detail.getNamespace());
        assertEquals("Address", detail.getTypeName());
        assertEquals(ODataSchemaAnalyzer.TypeKind.COMPLEX_TYPE, detail.getTypeKind());
        assertEquals(addressComplex, detail.getTypeDefinition());
        
        // 验证依赖关系
        Set<String> directDeps = detail.getDirectDependencies();
        assertNotNull(directDeps);
        assertTrue(directDeps.contains("TestService.OrderStatus"));
        
        verify(repository).getComplexType("TestService.Address");
    }

    @Test
    void testGetComplexTypeDetail_NotFound() {
        // Arrange
        String fullQualifiedName = "TestService.NonExistent";
        when(repository.getComplexType("TestService.NonExistent")).thenReturn(null);

        // Act
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getComplexTypeDetail(fullQualifiedName);

        // Assert
        assertNull(detail);
        verify(repository).getComplexType("TestService.NonExistent");
    }

    @Test
    void testGetEnumTypeDetail_Success() {
        // Arrange
        String fullQualifiedName = "TestService.OrderStatus";
        when(repository.getEnumType("TestService.OrderStatus")).thenReturn(orderStatusEnum);

        // Act
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getEnumTypeDetail(fullQualifiedName);

        // Assert
        assertNotNull(detail);
        assertEquals(fullQualifiedName, detail.getFullQualifiedName());
        assertEquals("TestService", detail.getNamespace());
        assertEquals("OrderStatus", detail.getTypeName());
        assertEquals(ODataSchemaAnalyzer.TypeKind.ENUM_TYPE, detail.getTypeKind());
        assertEquals(orderStatusEnum, detail.getTypeDefinition());
        
        // EnumType没有依赖
        Set<String> directDeps = detail.getDirectDependencies();
        assertNotNull(directDeps);
        assertTrue(directDeps.isEmpty());
        
        verify(repository).getEnumType("TestService.OrderStatus");
    }

    @Test
    void testGetEnumTypeDetail_NotFound() {
        // Arrange
        String fullQualifiedName = "TestService.NonExistent";
        when(repository.getEnumType("TestService.NonExistent")).thenReturn(null);

        // Act
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getEnumTypeDetail(fullQualifiedName);

        // Assert
        assertNull(detail);
        verify(repository).getEnumType("TestService.NonExistent");
    }

    @Test
    void testAnalysisResult_Creation() {
        // Arrange
        boolean success = true;
        List<String> errors = Arrays.asList("Error1", "Error2");
        List<String> warnings = Arrays.asList("Warning1", "Warning2");
        Map<String, Set<String>> dependencies = new HashMap<>();
        Set<String> type1Deps = new HashSet<>();
        type1Deps.add("Type2");
        dependencies.put("Type1", type1Deps);
        Map<String, String> typeLocations = new HashMap<>();
        typeLocations.put("Type1", "file1.xml");
        ODataSchemaAnalyzer.ImportValidationResult importValidation = 
            new ODataSchemaAnalyzer.ImportValidationResult(true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = new ODataSchemaAnalyzer.AnalysisResult(
            success, errors, warnings, dependencies, typeLocations, importValidation
        );

        // Assert
        assertEquals(success, result.isSuccess());
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        assertEquals(dependencies, result.getDependencies());
        assertEquals(typeLocations, result.getTypeLocations());
        assertEquals(importValidation, result.getImportValidation());
    }

    @Test
    void testImportValidationResult_Creation() {
        // Arrange
        boolean valid = false;
        List<String> missingImports = Arrays.asList("Missing1", "Missing2");
        List<String> unusedImports = Arrays.asList("Unused1", "Unused2");
        List<String> circularDependencies = Arrays.asList("Circular1", "Circular2");

        // Act
        ODataSchemaAnalyzer.ImportValidationResult result = new ODataSchemaAnalyzer.ImportValidationResult(
            valid, missingImports, unusedImports, circularDependencies
        );

        // Assert
        assertEquals(valid, result.isValid());
        assertEquals(missingImports, result.getMissingImports());
        assertEquals(unusedImports, result.getUnusedImports());
        assertEquals(circularDependencies, result.getCircularDependencies());
    }

    @Test
    void testTypeDetailInfo_Creation() {
        // Arrange
        String fullQualifiedName = "TestService.Customer";
        String namespace = "TestService";
        String typeName = "Customer";
        ODataSchemaAnalyzer.TypeKind typeKind = ODataSchemaAnalyzer.TypeKind.ENTITY_TYPE;
        Object typeDefinition = customerEntity;
        Set<String> directDependencies = new HashSet<>();
        directDependencies.add("TestService.Address");
        Set<String> allDependencies = new HashSet<>();
        allDependencies.add("TestService.Address");
        allDependencies.add("TestService.Country");
        Set<String> dependents = new HashSet<>();
        dependents.add("TestService.Order");
        String sourceFile = "customer.xml";

        // Act
        ODataSchemaAnalyzer.TypeDetailInfo detail = new ODataSchemaAnalyzer.TypeDetailInfo(
            fullQualifiedName, namespace, typeName, typeKind, typeDefinition,
            directDependencies, allDependencies, dependents, sourceFile
        );

        // Assert
        assertEquals(fullQualifiedName, detail.getFullQualifiedName());
        assertEquals(namespace, detail.getNamespace());
        assertEquals(typeName, detail.getTypeName());
        assertEquals(typeKind, detail.getTypeKind());
        assertEquals(typeDefinition, detail.getTypeDefinition());
        assertEquals(directDependencies, detail.getDirectDependencies());
        assertEquals(allDependencies, detail.getAllDependencies());
        assertEquals(dependents, detail.getDependents());
        assertEquals(sourceFile, detail.getSourceFile());
    }

    @Test
    void testTypeKind_EnumValues() {
        // Test all enum values
        ODataSchemaAnalyzer.TypeKind[] typeKinds = ODataSchemaAnalyzer.TypeKind.values();
        assertEquals(3, typeKinds.length);
        
        assertTrue(Arrays.asList(typeKinds).contains(ODataSchemaAnalyzer.TypeKind.ENTITY_TYPE));
        assertTrue(Arrays.asList(typeKinds).contains(ODataSchemaAnalyzer.TypeKind.COMPLEX_TYPE));
        assertTrue(Arrays.asList(typeKinds).contains(ODataSchemaAnalyzer.TypeKind.ENUM_TYPE));
    }

    @Test
    void testAnalyzeDirectory_WithMissingDependencies() {
        // Arrange
        String directoryPath = tempDir.toString();
        
        // 创建有缺失依赖的实体
        CsdlEntityType entityWithMissingDep = new CsdlEntityType();
        entityWithMissingDep.setName("EntityWithMissingDep");
        
        CsdlProperty propWithMissingType = new CsdlProperty();
        propWithMissingType.setName("MissingTypeRef");
        propWithMissingType.setType("TestService.MissingType");
        
        entityWithMissingDep.setProperties(Arrays.asList(propWithMissingType));

        CsdlSchema schemaWithMissingDep = new CsdlSchema();
        schemaWithMissingDep.setNamespace("TestService");
        schemaWithMissingDep.setEntityTypes(Arrays.asList(entityWithMissingDep));
        
        ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
            1, 1, 0, new ArrayList<>(), new HashMap<>()
        );
        
        when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", schemaWithMissingDep);
        when(repository.getAllSchemas()).thenReturn(schemas);

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess()); // 应该失败，因为有缺失依赖
        assertNotNull(result.getImportValidation());
        assertFalse(result.getImportValidation().isValid());
        assertFalse(result.getImportValidation().getMissingImports().isEmpty());
    }

    @Test
    void testAnalyzeDirectory_WithCircularDependencies() {
        // Arrange
        String directoryPath = tempDir.toString();
        
        // 创建循环依赖：TypeA -> TypeB -> TypeA
        CsdlComplexType typeA = new CsdlComplexType();
        typeA.setName("TypeA");
        
        CsdlProperty propA = new CsdlProperty();
        propA.setName("TypeBRef");
        propA.setType("TestService.TypeB");
        typeA.setProperties(Arrays.asList(propA));

        CsdlComplexType typeB = new CsdlComplexType();
        typeB.setName("TypeB");
        
        CsdlProperty propB = new CsdlProperty();
        propB.setName("TypeARef");
        propB.setType("TestService.TypeA");
        typeB.setProperties(Arrays.asList(propB));

        CsdlSchema circularSchema = new CsdlSchema();
        circularSchema.setNamespace("TestService");
        circularSchema.setComplexTypes(Arrays.asList(typeA, typeB));
        
        ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
            1, 1, 0, new ArrayList<>(), new HashMap<>()
        );
        
        when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("TestService", circularSchema);
        when(repository.getAllSchemas()).thenReturn(schemas);

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess()); // 应该失败，因为有循环依赖
        assertNotNull(result.getImportValidation());
        assertFalse(result.getImportValidation().isValid());
        assertFalse(result.getImportValidation().getCircularDependencies().isEmpty());
    }

    @Test
    void testAnalyzeDirectory_MultipleSchemas() {
        // Arrange
        String directoryPath = tempDir.toString();
        
        // 第二个Schema
        CsdlSchema secondSchema = new CsdlSchema();
        secondSchema.setNamespace("SecondService");
        
        CsdlEntityType secondEntity = new CsdlEntityType();
        secondEntity.setName("SecondEntity");
        
        CsdlProperty prop = new CsdlProperty();
        prop.setName("TestRef");
        prop.setType("TestService.Customer"); // 跨Schema依赖
        
        secondEntity.setProperties(Arrays.asList(prop));
        secondSchema.setEntityTypes(Arrays.asList(secondEntity));
        
        Map<String, CsdlSchema> multipleSchemas = new HashMap<>();
        multipleSchemas.put("TestService", testSchema);
        multipleSchemas.put("SecondService", secondSchema);
        
        ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
            2, 2, 0, new ArrayList<>(), new HashMap<>()
        );
        
        when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);
        when(repository.getAllSchemas()).thenReturn(multipleSchemas);

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        Map<String, Set<String>> dependencies = result.getDependencies();
        assertTrue(dependencies.containsKey("SecondService.SecondEntity"));
        assertTrue(dependencies.get("SecondService.SecondEntity").contains("TestService.Customer"));
    }

    @Test
    void testAnalyzeDirectory_EmptyDirectory() {
        // Arrange
        String directoryPath = tempDir.toString();
        
        ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
            0, 0, 0, new ArrayList<>(), new HashMap<>()
        );
        
        when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);
        lenient().when(repository.getAllSchemas()).thenReturn(new HashMap<>());

        // Act
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getDependencies().isEmpty());
        assertTrue(result.getTypeLocations().isEmpty());
        assertNotNull(result.getImportValidation());
        assertTrue(result.getImportValidation().isValid());
    }

    @Test
    void testGetTypeDetail_InvalidFullQualifiedName() {
        // Test invalid format (no namespace)
        ODataSchemaAnalyzer.TypeDetailInfo detail1 = analyzer.getEntityTypeDetail("InvalidName");
        assertNull(detail1);

        // Test empty string
        ODataSchemaAnalyzer.TypeDetailInfo detail2 = analyzer.getComplexTypeDetail("");
        assertNull(detail2);

        // Test null
        ODataSchemaAnalyzer.TypeDetailInfo detail3 = analyzer.getEnumTypeDetail(null);
        assertNull(detail3);
    }

    @Test
    void testAnalyzeDirectory_PerformanceWithLargeSchema() {
        // Arrange
        String directoryPath = tempDir.toString();
        
        CsdlSchema largeSchema = new CsdlSchema();
        largeSchema.setNamespace("LargeService");
        
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName("Entity" + i);
            
            CsdlProperty prop = new CsdlProperty();
            prop.setName("Id");
            prop.setType("Edm.String");
            entityType.setProperties(Arrays.asList(prop));
            
            entityTypes.add(entityType);
        }
        largeSchema.setEntityTypes(entityTypes);
        
        ODataXmlLoader.LoadResult loadResult = new ODataXmlLoader.LoadResult(
            1, 1, 0, new ArrayList<>(), new HashMap<>()
        );
        
        when(xmlLoader.loadFromDirectory(directoryPath)).thenReturn(loadResult);
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("LargeService", largeSchema);
        when(repository.getAllSchemas()).thenReturn(schemas);

        // Act
        long startTime = System.currentTimeMillis();
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(50, result.getDependencies().size());
        
        // 验证性能（分析应该在合理时间内完成）
        assertTrue(endTime - startTime < 1000, "Analysis took too long: " + (endTime - startTime) + "ms");
    }
}
