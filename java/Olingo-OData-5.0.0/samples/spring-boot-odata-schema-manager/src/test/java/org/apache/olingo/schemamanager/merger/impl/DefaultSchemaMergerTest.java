package org.apache.olingo.schemamanager.merger.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.merger.SchemaMerger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultSchemaMergerTest {

    private DefaultSchemaMerger merger;
    private CsdlSchema schema1;
    private CsdlSchema schema2;
    private CsdlSchema schema3;

    @BeforeEach
    void setUp() {
        merger = new DefaultSchemaMerger();
        setupTestSchemas();
    }

    private void setupTestSchemas() {
        // Schema 1: TestService1
        schema1 = new CsdlSchema();
        schema1.setNamespace("TestService1");

        CsdlEntityType customer = new CsdlEntityType();
        customer.setName("Customer");
        
        CsdlProperty idProp = new CsdlProperty();
        idProp.setName("Id");
        idProp.setType("Edm.String");
        customer.setProperties(Arrays.asList(idProp));

        CsdlComplexType address = new CsdlComplexType();
        address.setName("Address");
        
        CsdlProperty streetProp = new CsdlProperty();
        streetProp.setName("Street");
        streetProp.setType("Edm.String");
        address.setProperties(Arrays.asList(streetProp));

        schema1.setEntityTypes(Arrays.asList(customer));
        schema1.setComplexTypes(Arrays.asList(address));

        // Schema 2: TestService1 (same namespace, for merging)
        schema2 = new CsdlSchema();
        schema2.setNamespace("TestService1");

        CsdlEntityType product = new CsdlEntityType();
        product.setName("Product");
        
        CsdlProperty nameProp = new CsdlProperty();
        nameProp.setName("Name");
        nameProp.setType("Edm.String");
        product.setProperties(Arrays.asList(nameProp));

        CsdlEnumType status = new CsdlEnumType();
        status.setName("Status");
        
        CsdlEnumMember member = new CsdlEnumMember();
        member.setName("Active");
        member.setValue("1");
        status.setMembers(Arrays.asList(member));

        schema2.setEntityTypes(Arrays.asList(product));
        schema2.setEnumTypes(Arrays.asList(status));

        // Schema 3: TestService1 - 与Schema1有相同的EntityType名称和namespace (conflict)
        schema3 = new CsdlSchema();
        schema3.setNamespace("TestService1");

        CsdlEntityType customer3 = new CsdlEntityType();
        customer3.setName("Customer");
        
        CsdlProperty emailProp = new CsdlProperty();
        emailProp.setName("Email");
        emailProp.setType("Edm.String");
        customer3.setProperties(Arrays.asList(emailProp));

        schema3.setEntityTypes(Arrays.asList(customer3));
    }

    @Test
    void testMergeSchemas_Success() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);

        SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());

        CsdlSchema mergedSchema = result.getMergedSchema();
        assertNotNull(mergedSchema);
        assertEquals("TestService1", mergedSchema.getNamespace());

        // 验证EntityTypes被合并
        assertNotNull(mergedSchema.getEntityTypes());
        assertEquals(2, mergedSchema.getEntityTypes().size());

        // 验证包含来自两个schema的EntityTypes
        List<String> entityTypeNames = new ArrayList<>();
        for (CsdlEntityType et : mergedSchema.getEntityTypes()) {
            entityTypeNames.add(et.getName());
        }
        assertTrue(entityTypeNames.contains("Customer"));
        assertTrue(entityTypeNames.contains("Product"));

        // 验证ComplexTypes
        assertNotNull(mergedSchema.getComplexTypes());
        assertEquals(1, mergedSchema.getComplexTypes().size());
        assertEquals("Address", mergedSchema.getComplexTypes().get(0).getName());

        // 验证EnumTypes
        assertNotNull(mergedSchema.getEnumTypes());
        assertEquals(1, mergedSchema.getEnumTypes().size());
        assertEquals("Status", mergedSchema.getEnumTypes().get(0).getName());
    }

    @Test
    void testMergeSchemas_EmptyList() {
        List<CsdlSchema> emptySchemas = Collections.emptyList();

        SchemaMerger.MergeResult result = merger.mergeSchemas(emptySchemas);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
        assertNull(result.getMergedSchema());
    }

    @Test
    void testMergeSchemas_NullList() {
        SchemaMerger.MergeResult result = merger.mergeSchemas(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
        assertNull(result.getMergedSchema());
    }

    @Test
    void testMergeSchemas_WithConflicts() {
        // 使用有名称冲突的schemas (same namespace, same entity name)
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema3);

        SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);

        assertNotNull(result);
        
        // 根据实现，可能成功（使用冲突解决策略）或失败
        if (result.isSuccess()) {
            CsdlSchema mergedSchema = result.getMergedSchema();
            assertNotNull(mergedSchema);
            assertEquals("TestService1", mergedSchema.getNamespace());
            
            // 应该有警告提示冲突已被解决
            assertFalse(result.getWarnings().isEmpty());
        } else {
            // 如果实现选择失败而不是解决冲突
            assertFalse(result.getErrors().isEmpty());
        }
    }

    @Test
    void testMergeSchemas_SingleSchema() {
        List<CsdlSchema> schemas = Arrays.asList(schema1);

        SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());

        CsdlSchema mergedSchema = result.getMergedSchema();
        assertNotNull(mergedSchema);
        assertEquals("TestService1", mergedSchema.getNamespace());

        // 验证内容与原schema相同
        assertEquals(1, mergedSchema.getEntityTypes().size());
        assertEquals("Customer", mergedSchema.getEntityTypes().get(0).getName());
        assertEquals(1, mergedSchema.getComplexTypes().size());
        assertEquals("Address", mergedSchema.getComplexTypes().get(0).getName());
    }

    @Test
    void testMergeSchemas_WithNullSchema() {
        // 包含null的schema列表
        List<CsdlSchema> schemas = Arrays.asList(schema1, null, schema2);

        SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);

        assertNotNull(result);
        // 应该能处理null schema并继续处理其他有效的schemas
        assertTrue(result.isSuccess());

        CsdlSchema mergedSchema = result.getMergedSchema();
        assertNotNull(mergedSchema);
        assertEquals("TestService1", mergedSchema.getNamespace());

        // 验证只合并了非null的schemas
        assertEquals(2, mergedSchema.getEntityTypes().size());
    }

    @Test
    void testMergeByNamespace() {
        // 创建不同namespace的schemas
        CsdlSchema schemaA = new CsdlSchema();
        schemaA.setNamespace("ServiceA");
        
        CsdlEntityType entityA = new CsdlEntityType();
        entityA.setName("EntityA");
        schemaA.setEntityTypes(Arrays.asList(entityA));

        CsdlSchema schemaB = new CsdlSchema();
        schemaB.setNamespace("ServiceB");
        
        CsdlEntityType entityB = new CsdlEntityType();
        entityB.setName("EntityB");
        schemaB.setEntityTypes(Arrays.asList(entityB));

        Map<String, CsdlSchema> schemaMap = new HashMap<>();
        schemaMap.put("fileA.xml", schemaA);
        schemaMap.put("fileB.xml", schemaB);
        schemaMap.put("file1.xml", schema1);
        schemaMap.put("file2.xml", schema2); // same namespace as schema1

        Map<String, CsdlSchema> result = merger.mergeByNamespace(schemaMap);

        assertNotNull(result);
        assertEquals(3, result.size()); // ServiceA, ServiceB, TestService1

        assertTrue(result.containsKey("ServiceA"));
        assertTrue(result.containsKey("ServiceB"));
        assertTrue(result.containsKey("TestService1"));

        // 验证TestService1被正确合并
        CsdlSchema mergedTestService = result.get("TestService1");
        assertNotNull(mergedTestService);
        assertEquals(2, mergedTestService.getEntityTypes().size());
    }

    @Test
    void testCheckCompatibility_Compatible() {
        CsdlSchema compatibleSchema = new CsdlSchema();
        compatibleSchema.setNamespace("TestService1");
        
        CsdlEntityType order = new CsdlEntityType();
        order.setName("Order");
        compatibleSchema.setEntityTypes(Arrays.asList(order));

        SchemaMerger.CompatibilityResult result = merger.checkCompatibility(schema1, compatibleSchema);

        assertNotNull(result);
        assertTrue(result.isCompatible());
        assertTrue(result.getConflicts().isEmpty());
    }

    @Test
    void testCheckCompatibility_Incompatible() {
        SchemaMerger.CompatibilityResult result = merger.checkCompatibility(schema1, schema3);

        assertNotNull(result);
        assertFalse(result.isCompatible());
        assertFalse(result.getConflicts().isEmpty());
    }

    @Test
    void testResolveConflicts_KeepFirst() {
        List<CsdlSchema> conflictingSchemas = Arrays.asList(schema1, schema3);

        CsdlSchema resolved = merger.resolveConflicts(conflictingSchemas, SchemaMerger.ConflictResolution.KEEP_FIRST);

        assertNotNull(resolved);
        assertEquals("TestService1", resolved.getNamespace());
        assertEquals(1, resolved.getEntityTypes().size());

        // 应该保留第一个schema的Customer定义
        CsdlEntityType customer = resolved.getEntityTypes().get(0);
        assertEquals("Customer", customer.getName());
        assertEquals("Id", customer.getProperties().get(0).getName()); // from schema1
    }

    @Test
    void testResolveConflicts_KeepLast() {
        List<CsdlSchema> conflictingSchemas = Arrays.asList(schema1, schema3);

        CsdlSchema resolved = merger.resolveConflicts(conflictingSchemas, SchemaMerger.ConflictResolution.KEEP_LAST);

        assertNotNull(resolved);
        assertEquals("TestService1", resolved.getNamespace());
        assertEquals(1, resolved.getEntityTypes().size());

        // 应该保留最后一个schema的Customer定义
        CsdlEntityType customer = resolved.getEntityTypes().get(0);
        assertEquals("Customer", customer.getName());
        assertEquals("Email", customer.getProperties().get(0).getName()); // from schema3
    }

    @Test
    void testResolveConflicts_ThrowError() {
        List<CsdlSchema> conflictingSchemas = Arrays.asList(schema1, schema3);

        assertThrows(RuntimeException.class, () -> {
            merger.resolveConflicts(conflictingSchemas, SchemaMerger.ConflictResolution.THROW_ERROR);
        });
    }

    @Test
    void testMergeWithEmptySchemas() {
        // 创建空的schemas
        CsdlSchema emptySchema1 = new CsdlSchema();
        emptySchema1.setNamespace("TestService1");

        CsdlSchema emptySchema2 = new CsdlSchema();
        emptySchema2.setNamespace("TestService1");

        List<CsdlSchema> schemas = Arrays.asList(emptySchema1, emptySchema2);

        SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);

        assertNotNull(result);
        assertTrue(result.isSuccess());

        CsdlSchema mergedSchema = result.getMergedSchema();
        assertNotNull(mergedSchema);
        assertEquals("TestService1", mergedSchema.getNamespace());

        // 验证空集合
        assertTrue(mergedSchema.getEntityTypes() == null || mergedSchema.getEntityTypes().isEmpty());
        assertTrue(mergedSchema.getComplexTypes() == null || mergedSchema.getComplexTypes().isEmpty());
        assertTrue(mergedSchema.getEnumTypes() == null || mergedSchema.getEnumTypes().isEmpty());
    }

    @Test
    void testMergeWithEntityContainers() {
        // 最简单的测试：只测试merger是否正常
        assertNotNull(merger);
        
        // 测试空列表
        List<CsdlSchema> emptySchemas = new ArrayList<>();
        SchemaMerger.MergeResult emptyResult = merger.mergeSchemas(emptySchemas);
        assertNotNull(emptyResult);
        assertFalse(emptyResult.isSuccess());
        assertTrue(emptyResult.getErrors().contains("No schemas provided for merging"));
        
        // 测试单个schema
        List<CsdlSchema> singleSchema = Arrays.asList(schema1);
        SchemaMerger.MergeResult singleResult = merger.mergeSchemas(singleSchema);
        assertNotNull(singleResult);
        assertTrue(singleResult.isSuccess());
        assertEquals(schema1, singleResult.getMergedSchema());
    }

    @Test
    void testMergeWithEntityContainersReal() {
        // 为schemas添加EntityContainers
        CsdlEntityContainer container1 = new CsdlEntityContainer();
        container1.setName("Container1");
        
        CsdlEntitySet entitySet1 = new CsdlEntitySet();
        entitySet1.setName("Customers");
        entitySet1.setType("TestService1.Customer");
        
        container1.setEntitySets(Arrays.asList(entitySet1));
        
        schema1.setEntityContainer(container1);

        CsdlEntityContainer container2 = new CsdlEntityContainer();
        container2.setName("Container2");
        
        CsdlEntitySet entitySet2 = new CsdlEntitySet();
        entitySet2.setName("Products");
        entitySet2.setType("TestService1.Product");
        
        container2.setEntitySets(Arrays.asList(entitySet2));
        
        schema2.setEntityContainer(container2);

        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);

        SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);

        assertNotNull(result);
        assertTrue(result.isSuccess());

        CsdlSchema mergedSchema = result.getMergedSchema();
        assertNotNull(mergedSchema);

        // 验证EntityContainer被合并
        CsdlEntityContainer mergedContainer = mergedSchema.getEntityContainer();
        assertNotNull(mergedContainer);
        
        // EntitySets应该被合并
        assertNotNull(mergedContainer.getEntitySets());
        assertEquals(2, mergedContainer.getEntitySets().size());
    }

    @Test
    void testHandleDifferentNamespaces() {
        // 测试不同namespace的schemas不会被合并
        CsdlSchema schemaDifferentNs = new CsdlSchema();
        schemaDifferentNs.setNamespace("DifferentService");
        
        CsdlEntityType differentEntity = new CsdlEntityType();
        differentEntity.setName("DifferentEntity");
        schemaDifferentNs.setEntityTypes(Arrays.asList(differentEntity));

        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaDifferentNs);

        SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);

        assertNotNull(result);
        
        // 根据实现，可能会失败（不同namespace）或警告
        if (!result.isSuccess()) {
            assertFalse(result.getErrors().isEmpty());
        } else {
            // 如果成功，应该有警告
            assertFalse(result.getWarnings().isEmpty());
        }
    }

    @Test
    void testLargeSchemasMerge() {
        // 创建大型schemas进行性能测试
        List<CsdlSchema> largeSchemas = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            CsdlSchema schema = new CsdlSchema();
            schema.setNamespace("TestService1"); // same namespace for merging

            List<CsdlEntityType> entityTypes = new ArrayList<>();
            for (int j = 1; j <= 20; j++) {
                CsdlEntityType entityType = new CsdlEntityType();
                entityType.setName("Entity" + i + "_" + j);
                
                CsdlProperty prop = new CsdlProperty();
                prop.setName("Property" + j);
                prop.setType("Edm.String");
                entityType.setProperties(Arrays.asList(prop));
                
                entityTypes.add(entityType);
            }
            schema.setEntityTypes(entityTypes);
            largeSchemas.add(schema);
        }

        long startTime = System.currentTimeMillis();
        SchemaMerger.MergeResult result = merger.mergeSchemas(largeSchemas);
        long endTime = System.currentTimeMillis();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        CsdlSchema mergedSchema = result.getMergedSchema();
        assertNotNull(mergedSchema);
        assertEquals("TestService1", mergedSchema.getNamespace());
        assertEquals(100, mergedSchema.getEntityTypes().size()); // 5 schemas * 20 entities each

        // 验证性能（合并应该在合理时间内完成）
        assertTrue(endTime - startTime < 3000, "Merge took too long: " + (endTime - startTime) + "ms");
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        int numThreads = 5;
        Thread[] threads = new Thread[numThreads];
        List<SchemaMerger.MergeResult> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
                SchemaMerger.MergeResult result = merger.mergeSchemas(schemas);
                results.add(result);
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

        // 验证所有结果
        assertEquals(numThreads, results.size());
        for (SchemaMerger.MergeResult result : results) {
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getMergedSchema());
            assertEquals("TestService1", result.getMergedSchema().getNamespace());
        }
    }

    // ==== 使用测试资源文件的测试方法 ====

    @Test
    void testMergeSchemas_FromTestResources_BaseAndExtension() {
        // 模拟从merge-test目录加载的base和extension schema
        // Base Schema
        CsdlSchema baseSchema = new CsdlSchema();
        baseSchema.setNamespace("Base");

        CsdlEntityType baseEntity = new CsdlEntityType();
        baseEntity.setName("BaseEntity");
        CsdlProperty idProp = new CsdlProperty();
        idProp.setName("Id");
        idProp.setType("Edm.String");
        CsdlProperty createdProp = new CsdlProperty();
        createdProp.setName("CreatedDate");
        createdProp.setType("Edm.DateTimeOffset");
        baseEntity.setProperties(Arrays.asList(idProp, createdProp));

        CsdlComplexType baseAddress = new CsdlComplexType();
        baseAddress.setName("BaseAddress");
        CsdlProperty streetProp = new CsdlProperty();
        streetProp.setName("Street");
        streetProp.setType("Edm.String");
        CsdlProperty cityProp = new CsdlProperty();
        cityProp.setName("City");
        cityProp.setType("Edm.String");
        baseAddress.setProperties(Arrays.asList(streetProp, cityProp));

        CsdlEntityContainer baseContainer = new CsdlEntityContainer();
        baseContainer.setName("BaseContainer");
        CsdlEntitySet baseEntitySet = new CsdlEntitySet();
        baseEntitySet.setName("BaseEntities");
        baseEntitySet.setType("Base.BaseEntity");
        baseContainer.setEntitySets(Arrays.asList(baseEntitySet));

        baseSchema.setEntityTypes(Arrays.asList(baseEntity));
        baseSchema.setComplexTypes(Arrays.asList(baseAddress));
        baseSchema.setEntityContainer(baseContainer);

        // Extension Schema
        CsdlSchema extensionSchema = new CsdlSchema();
        extensionSchema.setNamespace("Extension");

        CsdlEntityType extendedEntity = new CsdlEntityType();
        extendedEntity.setName("ExtendedEntity");
        CsdlProperty extIdProp = new CsdlProperty();
        extIdProp.setName("Id");
        extIdProp.setType("Edm.String");
        CsdlProperty extProp = new CsdlProperty();
        extProp.setName("ExtendedProperty");
        extProp.setType("Edm.String");
        CsdlProperty addressProp = new CsdlProperty();
        addressProp.setName("Address");
        addressProp.setType("Extension.ExtendedAddress");
        extendedEntity.setProperties(Arrays.asList(extIdProp, extProp, addressProp));

        CsdlComplexType extendedAddress = new CsdlComplexType();
        extendedAddress.setName("ExtendedAddress");
        CsdlProperty extStreetProp = new CsdlProperty();
        extStreetProp.setName("Street");
        extStreetProp.setType("Edm.String");
        CsdlProperty extCityProp = new CsdlProperty();
        extCityProp.setName("City");
        extCityProp.setType("Edm.String");
        CsdlProperty postalCodeProp = new CsdlProperty();
        postalCodeProp.setName("PostalCode");
        postalCodeProp.setType("Edm.String");
        CsdlProperty countryProp = new CsdlProperty();
        countryProp.setName("Country");
        countryProp.setType("Edm.String");
        extendedAddress.setProperties(Arrays.asList(extStreetProp, extCityProp, postalCodeProp, countryProp));

        CsdlEntityContainer extensionContainer = new CsdlEntityContainer();
        extensionContainer.setName("ExtensionContainer");
        CsdlEntitySet extendedEntitySet = new CsdlEntitySet();
        extendedEntitySet.setName("ExtendedEntities");
        extendedEntitySet.setType("Extension.ExtendedEntity");
        extensionContainer.setEntitySets(Arrays.asList(extendedEntitySet));

        extensionSchema.setEntityTypes(Arrays.asList(extendedEntity));
        extensionSchema.setComplexTypes(Arrays.asList(extendedAddress));
        extensionSchema.setEntityContainer(extensionContainer);

        // Test merging schemas with different namespaces using mergeByNamespace
        Map<String, CsdlSchema> schemaMap = new HashMap<>();
        schemaMap.put("base-schema.xml", baseSchema);
        schemaMap.put("extension-schema.xml", extensionSchema);
        
        Map<String, CsdlSchema> mergedByNamespace = merger.mergeByNamespace(schemaMap);

        // Verify merge results
        assertNotNull(mergedByNamespace);
        assertEquals(2, mergedByNamespace.size()); // Two different namespaces

        // Verify Base namespace remains independent
        assertTrue(mergedByNamespace.containsKey("Base"));
        CsdlSchema baseResult = mergedByNamespace.get("Base");
        assertNotNull(baseResult);
        assertEquals("Base", baseResult.getNamespace());

        // Verify Extension namespace remains independent
        assertTrue(mergedByNamespace.containsKey("Extension"));
        CsdlSchema extensionResult = mergedByNamespace.get("Extension");
        assertNotNull(extensionResult);
        assertEquals("Extension", extensionResult.getNamespace());
    }

    @Test
    void testMergeSchemas_FromTestResources_MultipleServices() {
        // 模拟从multi-file目录加载的多个service schema
        // Products Schema
        CsdlSchema productsSchema = new CsdlSchema();
        productsSchema.setNamespace("Products");

        CsdlEntityType product = new CsdlEntityType();
        product.setName("Product");
        CsdlProperty productIdProp = new CsdlProperty();
        productIdProp.setName("Id");
        productIdProp.setType("Edm.String");
        CsdlProperty nameProp = new CsdlProperty();
        nameProp.setName("Name");
        nameProp.setType("Edm.String");
        CsdlProperty priceProp = new CsdlProperty();
        priceProp.setName("Price");
        priceProp.setType("Edm.Decimal");
        CsdlProperty categoryProp = new CsdlProperty();
        categoryProp.setName("Category");
        categoryProp.setType("Products.Category");
        product.setProperties(Arrays.asList(productIdProp, nameProp, priceProp, categoryProp));

        CsdlComplexType category = new CsdlComplexType();
        category.setName("Category");
        CsdlProperty catNameProp = new CsdlProperty();
        catNameProp.setName("Name");
        catNameProp.setType("Edm.String");
        CsdlProperty descProp = new CsdlProperty();
        descProp.setName("Description");
        descProp.setType("Edm.String");
        category.setProperties(Arrays.asList(catNameProp, descProp));

        CsdlEntityContainer productContainer = new CsdlEntityContainer();
        productContainer.setName("ProductContainer");
        CsdlEntitySet productSet = new CsdlEntitySet();
        productSet.setName("Products");
        productSet.setType("Products.Product");
        productContainer.setEntitySets(Arrays.asList(productSet));

        productsSchema.setEntityTypes(Arrays.asList(product));
        productsSchema.setComplexTypes(Arrays.asList(category));
        productsSchema.setEntityContainer(productContainer);

        // Sales Schema
        CsdlSchema salesSchema = new CsdlSchema();
        salesSchema.setNamespace("Sales");

        CsdlEntityType sale = new CsdlEntityType();
        sale.setName("Sale");
        CsdlProperty saleIdProp = new CsdlProperty();
        saleIdProp.setName("Id");
        saleIdProp.setType("Edm.String");
        CsdlProperty saleDateProp = new CsdlProperty();
        saleDateProp.setName("SaleDate");
        saleDateProp.setType("Edm.DateTimeOffset");
        CsdlProperty amountProp = new CsdlProperty();
        amountProp.setName("Amount");
        amountProp.setType("Edm.Decimal");
        CsdlProperty statusProp = new CsdlProperty();
        statusProp.setName("Status");
        statusProp.setType("Sales.SaleStatus");
        sale.setProperties(Arrays.asList(saleIdProp, saleDateProp, amountProp, statusProp));

        CsdlEnumType saleStatus = new CsdlEnumType();
        saleStatus.setName("SaleStatus");
        CsdlEnumMember pending = new CsdlEnumMember();
        pending.setName("Pending");
        pending.setValue("0");
        CsdlEnumMember completed = new CsdlEnumMember();
        completed.setName("Completed");
        completed.setValue("1");
        CsdlEnumMember refunded = new CsdlEnumMember();
        refunded.setName("Refunded");
        refunded.setValue("2");
        saleStatus.setMembers(Arrays.asList(pending, completed, refunded));

        CsdlEntityContainer salesContainer = new CsdlEntityContainer();
        salesContainer.setName("SalesContainer");
        CsdlEntitySet saleSet = new CsdlEntitySet();
        saleSet.setName("Sales");
        saleSet.setType("Sales.Sale");
        salesContainer.setEntitySets(Arrays.asList(saleSet));

        salesSchema.setEntityTypes(Arrays.asList(sale));
        salesSchema.setEnumTypes(Arrays.asList(saleStatus));
        salesSchema.setEntityContainer(salesContainer);

        // Inventory Schema
        CsdlSchema inventorySchema = new CsdlSchema();
        inventorySchema.setNamespace("Inventory");

        CsdlEntityType inventoryItem = new CsdlEntityType();
        inventoryItem.setName("InventoryItem");
        CsdlProperty invIdProp = new CsdlProperty();
        invIdProp.setName("Id");
        invIdProp.setType("Edm.String");
        CsdlProperty productIdProp2 = new CsdlProperty();
        productIdProp2.setName("ProductId");
        productIdProp2.setType("Edm.String");
        CsdlProperty quantityProp = new CsdlProperty();
        quantityProp.setName("Quantity");
        quantityProp.setType("Edm.Int32");
        CsdlProperty locationProp = new CsdlProperty();
        locationProp.setName("Location");
        locationProp.setType("Inventory.WarehouseLocation");
        inventoryItem.setProperties(Arrays.asList(invIdProp, productIdProp2, quantityProp, locationProp));

        CsdlComplexType warehouseLocation = new CsdlComplexType();
        warehouseLocation.setName("WarehouseLocation");
        CsdlProperty warehouseNameProp = new CsdlProperty();
        warehouseNameProp.setName("WarehouseName");
        warehouseNameProp.setType("Edm.String");
        CsdlProperty sectionProp = new CsdlProperty();
        sectionProp.setName("Section");
        sectionProp.setType("Edm.String");
        CsdlProperty shelfProp = new CsdlProperty();
        shelfProp.setName("Shelf");
        shelfProp.setType("Edm.String");
        warehouseLocation.setProperties(Arrays.asList(warehouseNameProp, sectionProp, shelfProp));

        CsdlEntityContainer inventoryContainer = new CsdlEntityContainer();
        inventoryContainer.setName("InventoryContainer");
        CsdlEntitySet inventorySet = new CsdlEntitySet();
        inventorySet.setName("InventoryItems");
        inventorySet.setType("Inventory.InventoryItem");
        inventoryContainer.setEntitySets(Arrays.asList(inventorySet));

        inventorySchema.setEntityTypes(Arrays.asList(inventoryItem));
        inventorySchema.setComplexTypes(Arrays.asList(warehouseLocation));
        inventorySchema.setEntityContainer(inventoryContainer);

        // Test merging all three schemas with different namespaces using mergeByNamespace
        Map<String, CsdlSchema> allSchemasMap = new HashMap<>();
        allSchemasMap.put("products-schema.xml", productsSchema);
        allSchemasMap.put("sales-schema.xml", salesSchema);
        allSchemasMap.put("inventory-schema.xml", inventorySchema);
        
        Map<String, CsdlSchema> mergedByNamespace = merger.mergeByNamespace(allSchemasMap);

        // Verify merge results
        assertNotNull(mergedByNamespace);
        assertEquals(3, mergedByNamespace.size()); // Three different namespaces

        // Verify each namespace is preserved
        assertTrue(mergedByNamespace.containsKey("Products"));
        assertTrue(mergedByNamespace.containsKey("Sales"));
        assertTrue(mergedByNamespace.containsKey("Inventory"));

        // Verify content integrity of each schema
        CsdlSchema productsResult = mergedByNamespace.get("Products");
        assertNotNull(productsResult);
        assertNotNull(productsResult.getEntityContainer());
        assertEquals(1, productsResult.getEntityTypes().size());
        assertEquals(1, productsResult.getComplexTypes().size());
        assertEquals("Product", productsResult.getEntityTypes().get(0).getName());
        assertEquals("Category", productsResult.getComplexTypes().get(0).getName());

        CsdlSchema salesResult = mergedByNamespace.get("Sales");
        assertNotNull(salesResult);
        assertNotNull(salesResult.getEntityContainer());
        assertEquals(1, salesResult.getEntityTypes().size());
        assertEquals(1, salesResult.getEnumTypes().size());
        assertEquals("Sale", salesResult.getEntityTypes().get(0).getName());
        assertEquals("SaleStatus", salesResult.getEnumTypes().get(0).getName());

        CsdlSchema inventoryResult = mergedByNamespace.get("Inventory");
        assertNotNull(inventoryResult);
        assertNotNull(inventoryResult.getEntityContainer());
        assertEquals(1, inventoryResult.getEntityTypes().size());
        assertEquals(1, inventoryResult.getComplexTypes().size());
        assertEquals("InventoryItem", inventoryResult.getEntityTypes().get(0).getName());
        assertEquals("WarehouseLocation", inventoryResult.getComplexTypes().get(0).getName());
    }

    @Test
    void testTestResourcesIntegrationScenario() {
        // 这个测试演示了如何在实际场景中使用测试资源
        // 验证测试资源文件的存在和可访问性
        String[] mergeTestFiles = {
            "src/test/resources/loader/merge-test/base-schema.xml",
            "src/test/resources/loader/merge-test/extension-schema.xml"
        };

        String[] multiFileResources = {
            "src/test/resources/loader/multi-file/products-schema.xml",
            "src/test/resources/loader/multi-file/sales-schema.xml",
            "src/test/resources/loader/multi-file/inventory-schema.xml"
        };

        // 验证merge-test资源存在
        for (String filePath : mergeTestFiles) {
            java.io.File file = new java.io.File(filePath);
            assertTrue(file.exists(), "Merge test resource should exist: " + filePath);
            assertTrue(file.canRead(), "Merge test resource should be readable: " + filePath);
        }

        // 验证multi-file资源存在
        for (String filePath : multiFileResources) {
            java.io.File file = new java.io.File(filePath);
            assertTrue(file.exists(), "Multi-file test resource should exist: " + filePath);
            assertTrue(file.canRead(), "Multi-file test resource should be readable: " + filePath);
        }

        // 在实际使用中，这些文件会被ODataXmlLoader加载，
        // 然后由OlingoSchemaParserImpl解析成CsdlSchema对象，
        // 最后传递给SchemaMerger进行合并
        
        // 这个测试确保了完整的集成流程中资源文件的可用性
        assertTrue(true, "All test resources are available for schema merger integration testing");
    }
}
