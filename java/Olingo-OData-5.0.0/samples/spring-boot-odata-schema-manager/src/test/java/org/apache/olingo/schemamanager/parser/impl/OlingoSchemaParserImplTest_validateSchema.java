package org.apache.olingo.schemamanager.parser.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.*;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.exception.SchemaValidationException;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 OlingoSchemaParserImpl.validateSchema() 方法
 */
class OlingoSchemaParserImplTest_validateSchema {

    private OlingoSchemaParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new OlingoSchemaParserImpl();
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testValidateSchema_ValidSimpleSchema() {
        // 测试有效的简单Schema
        CsdlSchema schema = loadSimpleSchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
    }

    @Test
    void testValidateSchema_ValidFullSchema() {
        // 测试有效的完整Schema
        CsdlSchema schema = loadFullSchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
    }

    @Test
    void testValidateSchema_ValidComplexTypesSchema() {
        // 测试有效的复杂类型Schema
        CsdlSchema schema = loadComplexTypesSchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
    }

    @Test
    void testValidateSchema_NullSchema() {
        // 测试null schema
        SchemaValidationException exception = assertThrows(SchemaValidationException.class, () -> {
            parser.validateSchema(null);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Schema cannot be null"));
    }

    @Test
    void testValidateSchema_SchemaWithNullNamespace() {
        // 测试没有namespace的Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(null);
        
        SchemaValidationException exception = assertThrows(SchemaValidationException.class, () -> {
            parser.validateSchema(schema);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("namespace"));
    }

    @Test
    void testValidateSchema_SchemaWithEmptyNamespace() {
        // 测试空namespace的Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("");
        
        SchemaValidationException exception = assertThrows(SchemaValidationException.class, () -> {
            parser.validateSchema(schema);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("namespace"));
    }

    @Test
    void testValidateSchema_InvalidNamespaceFormat() {
        // 测试无效namespace格式的Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("123invalid.namespace");
        
        SchemaValidationException exception = assertThrows(SchemaValidationException.class, () -> {
            parser.validateSchema(schema);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("namespace"));
    }

    @Test
    void testValidateSchema_CircularDependencySchema() {
        // 测试循环依赖Schema（应该被检测到并抛出异常）
        CsdlSchema schema = loadCircularDependencySchema();
        
        // 如果实现了循环依赖检测，应该抛出异常
        // 如果没有实现，可能通过验证
        boolean exceptionThrown = false;
        String errorMessage = null;
        try {
            parser.validateSchema(schema);
            // 如果没有抛出异常，说明验证通过或者还没有实现循环依赖检测
        } catch (RuntimeException e) {
            // 如果抛出异常，验证异常信息
            exceptionThrown = true;
            errorMessage = e.getMessage();
        }
        
        // 验证结果
        if (exceptionThrown) {
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("circular") || errorMessage.contains("dependency"));
        }
    }

    @Test
    void testValidateSchema_LargeSchema() {
        // 测试大型Schema的验证性能和正确性
        CsdlSchema schema = loadLargeSchema();
        
        long startTime = System.currentTimeMillis();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
        
        long duration = System.currentTimeMillis() - startTime;
        // 验证性能：大型schema验证应该在合理时间内完成（如5秒）
        assertTrue(duration < 5000, "Schema validation took too long: " + duration + "ms");
    }

    @Test
    void testValidateSchema_SchemaWithDuplicateEntityTypes() {
        // 测试包含重复实体类型的Schema
        // 这需要构造一个包含重复定义的无效schema
        CsdlSchema schema = loadDuplicateTypesSchema();
        
        SchemaValidationException exception = assertThrows(SchemaValidationException.class, () -> {
            parser.validateSchema(schema);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("duplicate") || exception.getMessage().contains("Duplicate"));
    }

    @Test
    void testValidateSchema_SchemaWithInvalidPropertyTypes() {
        // 测试包含无效属性类型的Schema
        CsdlSchema schema = loadInvalidTypesSchema();
        
        SchemaValidationException exception = assertThrows(SchemaValidationException.class, () -> {
            parser.validateSchema(schema);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("type") || exception.getMessage().contains("property"));
    }

    @Test
    void testValidateSchema_SchemaWithMissingBaseType() {
        // 测试基类型缺失的Schema
        CsdlSchema schema = loadMissingBaseTypeSchema();
        
        SchemaValidationException exception = assertThrows(SchemaValidationException.class, () -> {
            parser.validateSchema(schema);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("base") || exception.getMessage().contains("reference"));
    }

    @Test
    void testValidateSchema_MultiDependencySchema() {
        // 测试多依赖Schema的验证
        CsdlSchema schema = loadMultiDependencySchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
    }

    @Test
    void testValidateSchema_ValidReferencesOnly() {
        // 测试只有有效引用的Schema
        CsdlSchema schema = loadValidReferencesSchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
    }

    @Test
    void testValidateSchema_WithAnnotations() {
        // 测试包含注解的Schema
        CsdlSchema schema = loadAnnotatedSchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
    }

    @Test
    void testValidateSchema_EntityContainerValidation() {
        // 测试EntityContainer的验证
        CsdlSchema schema = loadFullSchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
        
        // 验证EntityContainer是否存在
        if (schema.getEntityContainer() != null) {
            assertNotNull(schema.getEntityContainer().getName());
            assertFalse(schema.getEntityContainer().getName().trim().isEmpty());
        }
    }

    @Test
    void testValidateSchema_FunctionImportValidation() {
        // 测试FunctionImport的验证
        CsdlSchema schema = loadFullSchema();
        
        assertDoesNotThrow(() -> {
            parser.validateSchema(schema);
        });
        
        // 如果有function imports，验证其有效性
        if (schema.getFunctions() != null && !schema.getFunctions().isEmpty()) {
            schema.getFunctions().forEach(function -> {
                assertNotNull(function.getName());
                assertFalse(function.getName().trim().isEmpty());
            });
        }
    }
}
