package org.apache.olingo.schemamanager.repository.impl;

import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySchemaRepositoryTest {

    private InMemorySchemaRepository repository;
    private CsdlSchema testSchema;
    private CsdlEntityType testEntityType;
    private CsdlComplexType testComplexType;
    private CsdlEnumType testEnumType;
    private CsdlEntityContainer testContainer;

    @BeforeEach
    void setUp() {
        repository = new InMemorySchemaRepository();
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试Schema
        testSchema = new CsdlSchema();
        testSchema.setNamespace("TestNamespace");

        // 创建测试EntityType
        testEntityType = new CsdlEntityType();
        testEntityType.setName("Customer");
        
        CsdlProperty idProperty = new CsdlProperty();
        idProperty.setName("Id");
        idProperty.setType("Edm.String");
        testEntityType.setProperties(Collections.singletonList(idProperty));

        // 创建测试ComplexType
        testComplexType = new CsdlComplexType();
        testComplexType.setName("Address");
        
        CsdlProperty streetProperty = new CsdlProperty();
        streetProperty.setName("Street");
        streetProperty.setType("Edm.String");
        testComplexType.setProperties(Collections.singletonList(streetProperty));

        // 创建测试EnumType
        testEnumType = new CsdlEnumType();
        testEnumType.setName("OrderStatus");
        
        CsdlEnumMember member1 = new CsdlEnumMember();
        member1.setName("Pending");
        member1.setValue("0");
        testEnumType.setMembers(Collections.singletonList(member1));

        // 创建测试EntityContainer
        testContainer = new CsdlEntityContainer();
        testContainer.setName("DefaultContainer");

        // 设置到Schema中
        testSchema.setEntityTypes(Collections.singletonList(testEntityType));
        testSchema.setComplexTypes(Collections.singletonList(testComplexType));
        testSchema.setEnumTypes(Collections.singletonList(testEnumType));
        testSchema.setEntityContainer(testContainer);
    }

    @Test
    void testAddSchema_Success() {
        // 添加Schema
        repository.addSchema(testSchema, "/test/path/schema.xml");

        // 验证Schema被添加
        CsdlSchema retrievedSchema = repository.getSchema("TestNamespace");
        assertNotNull(retrievedSchema);
        assertEquals("TestNamespace", retrievedSchema.getNamespace());

        // 验证文件路径被记录
        String filePath = repository.getSchemaFilePath("TestNamespace");
        assertEquals("/test/path/schema.xml", filePath);
    }

    @Test
    void testAddSchema_NullSchema() {
        // 添加null Schema应该不会抛出异常
        assertDoesNotThrow(() -> repository.addSchema(null, "/test/path"));
        
        // 验证没有内容被添加
        assertTrue(repository.getAllSchemas().isEmpty());
    }

    @Test
    void testAddSchema_NullNamespace() {
        CsdlSchema schemaWithNullNamespace = new CsdlSchema();
        schemaWithNullNamespace.setNamespace(null);

        // 添加namespace为null的Schema应该不会抛出异常
        assertDoesNotThrow(() -> repository.addSchema(schemaWithNullNamespace, "/test/path"));
        
        // 验证没有内容被添加
        assertTrue(repository.getAllSchemas().isEmpty());
    }

    @Test
    void testGetAllSchemas() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        Map<String, CsdlSchema> allSchemas = repository.getAllSchemas();
        assertNotNull(allSchemas);
        assertEquals(1, allSchemas.size());
        assertTrue(allSchemas.containsKey("TestNamespace"));
        assertEquals(testSchema, allSchemas.get("TestNamespace"));
    }

    @Test
    void testGetEntityType_ByFullQualifiedName() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        CsdlEntityType retrievedEntityType = repository.getEntityType("TestNamespace.Customer");
        assertNotNull(retrievedEntityType);
        assertEquals("Customer", retrievedEntityType.getName());
    }

    @Test
    void testGetEntityType_ByNamespaceAndName() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        CsdlEntityType retrievedEntityType = repository.getEntityType("TestNamespace", "Customer");
        assertNotNull(retrievedEntityType);
        assertEquals("Customer", retrievedEntityType.getName());
    }

    @Test
    void testGetEntityType_NotFound() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        CsdlEntityType notFound = repository.getEntityType("TestNamespace.NonExistent");
        assertNull(notFound);
    }

    @Test
    void testGetComplexType_ByFullQualifiedName() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        CsdlComplexType retrievedComplexType = repository.getComplexType("TestNamespace.Address");
        assertNotNull(retrievedComplexType);
        assertEquals("Address", retrievedComplexType.getName());
    }

    @Test
    void testGetComplexType_ByNamespaceAndName() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        CsdlComplexType retrievedComplexType = repository.getComplexType("TestNamespace", "Address");
        assertNotNull(retrievedComplexType);
        assertEquals("Address", retrievedComplexType.getName());
    }

    @Test
    void testGetEnumType_ByFullQualifiedName() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        CsdlEnumType retrievedEnumType = repository.getEnumType("TestNamespace.OrderStatus");
        assertNotNull(retrievedEnumType);
        assertEquals("OrderStatus", retrievedEnumType.getName());
    }

    @Test
    void testGetEnumType_ByNamespaceAndName() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        CsdlEnumType retrievedEnumType = repository.getEnumType("TestNamespace", "OrderStatus");
        assertNotNull(retrievedEnumType);
        assertEquals("OrderStatus", retrievedEnumType.getName());
    }

    @Test
    void testGetEntityTypes_ByNamespace() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        List<CsdlEntityType> entityTypes = repository.getEntityTypes("TestNamespace");
        assertNotNull(entityTypes);
        assertEquals(1, entityTypes.size());
        assertEquals("Customer", entityTypes.get(0).getName());
    }

    @Test
    void testGetComplexTypes_ByNamespace() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        List<CsdlComplexType> complexTypes = repository.getComplexTypes("TestNamespace");
        assertNotNull(complexTypes);
        assertEquals(1, complexTypes.size());
        assertEquals("Address", complexTypes.get(0).getName());
    }

    @Test
    void testGetEnumTypes_ByNamespace() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        List<CsdlEnumType> enumTypes = repository.getEnumTypes("TestNamespace");
        assertNotNull(enumTypes);
        assertEquals(1, enumTypes.size());
        assertEquals("OrderStatus", enumTypes.get(0).getName());
    }

    @Test
    void testGetAllNamespaces() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        // 添加另一个Schema
        CsdlSchema anotherSchema = new CsdlSchema();
        anotherSchema.setNamespace("AnotherNamespace");
        repository.addSchema(anotherSchema, "/test/path/another.xml");

        Set<String> namespaces = repository.getAllNamespaces();
        assertNotNull(namespaces);
        assertEquals(2, namespaces.size());
        assertTrue(namespaces.contains("TestNamespace"));
        assertTrue(namespaces.contains("AnotherNamespace"));
    }

    @Test
    void testGetStatistics() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        SchemaRepository.RepositoryStatistics stats = repository.getStatistics();
        assertNotNull(stats);
        assertEquals(1, stats.getTotalSchemas());
        assertEquals(1, stats.getTotalEntityTypes());
        assertEquals(1, stats.getTotalComplexTypes());
        assertEquals(1, stats.getTotalEnumTypes());
        assertEquals(1, stats.getTotalEntityContainers());
    }

    @Test
    void testClear() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        // 验证数据已添加
        assertFalse(repository.getAllSchemas().isEmpty());
        assertFalse(repository.getAllNamespaces().isEmpty());

        // 清理
        repository.clear();

        // 验证数据已清理
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
        assertNull(repository.getSchema("TestNamespace"));
        assertNull(repository.getEntityType("TestNamespace.Customer"));
        assertNull(repository.getComplexType("TestNamespace.Address"));
        assertNull(repository.getEnumType("TestNamespace.OrderStatus"));
    }

    @Test
    void testMultipleSchemas() {
        // 添加多个Schema
        repository.addSchema(testSchema, "/test/path/schema1.xml");

        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("Schema2");
        
        CsdlEntityType entityType2 = new CsdlEntityType();
        entityType2.setName("Product");
        schema2.setEntityTypes(Collections.singletonList(entityType2));
        
        repository.addSchema(schema2, "/test/path/schema2.xml");

        // 验证两个Schema都存在
        assertEquals(2, repository.getAllSchemas().size());
        assertNotNull(repository.getSchema("TestNamespace"));
        assertNotNull(repository.getSchema("Schema2"));

        // 验证各自的EntityType
        assertNotNull(repository.getEntityType("TestNamespace.Customer"));
        assertNotNull(repository.getEntityType("Schema2.Product"));
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // 基本的线程安全测试
        int numThreads = 10;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                CsdlSchema schema = new CsdlSchema();
                schema.setNamespace("Namespace" + threadId);
                
                CsdlEntityType entityType = new CsdlEntityType();
                entityType.setName("Entity" + threadId);
                schema.setEntityTypes(Collections.singletonList(entityType));
                
                repository.addSchema(schema, "/test/path/schema" + threadId + ".xml");
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证所有Schema都被添加
        assertEquals(numThreads, repository.getAllSchemas().size());
        assertEquals(numThreads, repository.getAllNamespaces().size());

        // 验证每个EntityType都存在
        for (int i = 0; i < numThreads; i++) {
            assertNotNull(repository.getEntityType("Namespace" + i + ".Entity" + i));
        }
    }

    @Test
    void testEmptyCollections() {
        // 测试空集合的处理
        CsdlSchema emptySchema = new CsdlSchema();
        emptySchema.setNamespace("EmptyNamespace");
        // 不设置EntityTypes, ComplexTypes, EnumTypes
        
        repository.addSchema(emptySchema, "/test/path/empty.xml");

        // 验证空集合不会导致异常
        List<CsdlEntityType> entityTypes = repository.getEntityTypes("EmptyNamespace");
        assertTrue(entityTypes.isEmpty());

        List<CsdlComplexType> complexTypes = repository.getComplexTypes("EmptyNamespace");
        assertTrue(complexTypes.isEmpty());

        List<CsdlEnumType> enumTypes = repository.getEnumTypes("EmptyNamespace");
        assertTrue(enumTypes.isEmpty());
    }

    @Test
    void testNonExistentNamespace() {
        repository.addSchema(testSchema, "/test/path/schema.xml");

        // 查询不存在的namespace
        assertNull(repository.getSchema("NonExistent"));
        assertNull(repository.getSchemaFilePath("NonExistent"));
        assertTrue(repository.getEntityTypes("NonExistent").isEmpty());
        assertTrue(repository.getComplexTypes("NonExistent").isEmpty());
        assertTrue(repository.getEnumTypes("NonExistent").isEmpty());
    }
}
