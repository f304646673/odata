package org.apache.olingo.schemamanager.testutil;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.parser.impl.OlingoSchemaParserImpl;

/**
 * 测试工具类，用于从XML文件加载Schema
 */
public class XmlSchemaTestUtils {
    
    private static final String TEST_RESOURCES_BASE = "src/test/resources/xml-schemas";
    private static final Map<String, CsdlSchema> schemaCache = new HashMap<>();
    
    /**
     * 从XML文件加载Schema
     * @param relativePath 相对于test/resources/xml-schemas的路径
     * @return CsdlSchema对象
     */
    public static CsdlSchema loadSchemaFromXml(String relativePath) {
        if (schemaCache.containsKey(relativePath)) {
            return schemaCache.get(relativePath);
        }
        
        try {
            Path xmlPath = Paths.get(TEST_RESOURCES_BASE, relativePath);
            OlingoSchemaParserImpl parser = new OlingoSchemaParserImpl();
            
            try (InputStream inputStream = new FileInputStream(xmlPath.toFile())) {
                OlingoSchemaParserImpl.ParseResult result = parser.parseSchema(inputStream, xmlPath.getFileName().toString());
                
                if (result.isSuccess() && result.getSchema() != null) {
                    schemaCache.put(relativePath, result.getSchema());
                    return result.getSchema();
                } else {
                    throw new RuntimeException("Failed to parse schema from " + relativePath + ": " + result.getErrorMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schema from " + relativePath, e);
        }
    }
    
    /**
     * 从多个XML文件加载多个Schema
     * @param relativePaths 相对路径数组
     * @return Schema映射，键为文件路径，值为Schema对象
     */
    public static Map<String, CsdlSchema> loadSchemasFromXml(String... relativePaths) {
        Map<String, CsdlSchema> schemas = new HashMap<>();
        for (String path : relativePaths) {
            schemas.put(path, loadSchemaFromXml(path));
        }
        return schemas;
    }
    
    /**
     * 清空Schema缓存
     */
    public static void clearCache() {
        schemaCache.clear();
    }
    
    /**
     * 获取测试资源基础路径
     */
    public static String getTestResourcesBase() {
        return TEST_RESOURCES_BASE;
    }
    
    /**
     * 加载复杂依赖Schema（用于依赖分析测试）
     */
    public static CsdlSchema loadMultiDependencySchema() {
        return loadSchemaFromXml("loader/complex/multi-dependency-schema.xml");
    }
    
    /**
     * 加载循环依赖Schema（用于循环依赖测试）
     */
    public static CsdlSchema loadCircularDependencySchema() {
        return loadSchemaFromXml("loader/complex/circular-dependency-schema.xml");
    }
    
    /**
     * 加载简单Schema（用于基础测试）
     */
    public static CsdlSchema loadSimpleSchema() {
        return loadSchemaFromXml("loader/valid/simple-schema.xml");
    }
    
    /**
     * 加载完整Schema（用于全功能测试）
     */
    public static CsdlSchema loadFullSchema() {
        return loadSchemaFromXml("loader/valid/full-schema.xml");
    }
    
    /**
     * 加载复杂类型Schema（用于ComplexType测试）
     */
    public static CsdlSchema loadComplexTypesSchema() {
        return loadSchemaFromXml("loader/valid/complex-types-schema.xml");
    }
    
    /**
     * 加载大型Schema（用于性能测试）
     */
    public static CsdlSchema loadLargeSchema() {
        return loadSchemaFromXml("loader/performance/large-schema.xml");
    }
    
    /**
     * 加载包含重复类型的Schema（用于验证测试）
     */
    public static CsdlSchema loadDuplicateTypesSchema() {
        return loadSchemaFromXml("loader/validation/duplicate-types-schema.xml");
    }
    
    /**
     * 加载包含无效类型的Schema（用于验证测试）
     */
    public static CsdlSchema loadInvalidTypesSchema() {
        return loadSchemaFromXml("loader/validation/invalid-types-schema.xml");
    }
    
    /**
     * 加载缺少基类型的Schema（用于验证测试）
     */
    public static CsdlSchema loadMissingBaseTypeSchema() {
        return loadSchemaFromXml("loader/validation/missing-base-type-schema.xml");
    }
    
    /**
     * 加载有效引用的Schema（用于验证测试）
     */
    public static CsdlSchema loadValidReferencesSchema() {
        return loadSchemaFromXml("loader/validation/valid-references-schema.xml");
    }
    
    /**
     * 加载带注解的Schema（用于注解测试）
     */
    public static CsdlSchema loadAnnotatedSchema() {
        return loadSchemaFromXml("loader/annotation/annotated-schema.xml");
    }
}
