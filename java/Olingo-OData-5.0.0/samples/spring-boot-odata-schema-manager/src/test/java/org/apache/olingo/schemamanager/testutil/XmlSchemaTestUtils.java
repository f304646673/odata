package org.apache.olingo.schemamanager.testutil;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser.ParseResult;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser.SchemaWithDependencies;
import org.apache.olingo.schemamanager.parser.impl.OlingoSchemaParserImpl;

/**
 * 测试工具类，用于从XML文件加载Schema
 */
public class XmlSchemaTestUtils {
    
    private static final String TEST_RESOURCES_BASE = "src/test/resources/xml-schemas";
    private static final Map<String, ParseResult> schemaCache = new HashMap<>();
    
    /**
     * 从XML文件加载Schema（支持多个Schema）
     * @param relativePath 相对于test/resources/xml-schemas的路径
     * @return ParseResult对象，包含所有Schema
     */
    public static ParseResult loadSchemasFromXml(String relativePath) {
        if (schemaCache.containsKey(relativePath)) {
            return schemaCache.get(relativePath);
        }
        
        try {
            Path xmlPath = Paths.get(TEST_RESOURCES_BASE, relativePath);
            OlingoSchemaParserImpl parser = new OlingoSchemaParserImpl();
            
            try (InputStream inputStream = new FileInputStream(xmlPath.toFile())) {
                ParseResult result = parser.parseSchema(inputStream, xmlPath.getFileName().toString());
                
                if (result.isSuccess()) {
                    schemaCache.put(relativePath, result);
                    return result;
                } else {
                    throw new RuntimeException("Failed to parse schema from " + relativePath + ": " + result.getErrorMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schema from " + relativePath, e);
        }
    }
    
    /**
     * 从XML文件加载单个Schema（向后兼容）
     * @param relativePath 相对于test/resources/xml-schemas的路径
     * @return CsdlSchema对象（如果有多个Schema，返回第一个）
     */
    @Deprecated
    public static CsdlSchema loadSchemaFromXml(String relativePath) {
        ParseResult result = loadSchemasFromXml(relativePath);
        return result.getFirstSchema(); // 使用新的向后兼容方法
    }
    
    /**
     * 从多个XML文件加载多个Schema
     * @param relativePaths 相对路径数组
     * @return Schema映射，键为文件路径，值为ParseResult对象
     */
    public static Map<String, ParseResult> loadMultipleSchemasFromXml(String... relativePaths) {
        Map<String, ParseResult> schemas = new HashMap<>();
        for (String path : relativePaths) {
            schemas.put(path, loadSchemasFromXml(path));
        }
        return schemas;
    }
    
    /**
     * 从多个XML文件加载多个Schema（向后兼容）
     * @param relativePaths 相对路径数组
     * @return Schema映射，键为文件路径，值为Schema对象
     */
    @Deprecated
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
     * 加载多Schema的XML文件（用于多Schema测试）
     */
    public static ParseResult loadMultiSchemaXml() {
        return loadSchemasFromXml("loader/multi-schema/multi-schema.xml");
    }
    
    /**
     * 加载具有重复namespace的多Schema XML（用于错误测试）
     */
    public static ParseResult loadDuplicateNamespaceSchemaXml() {
        try {
            Path xmlPath = Paths.get(TEST_RESOURCES_BASE, "loader/multi-schema/duplicate-namespace-schema.xml");
            OlingoSchemaParserImpl parser = new OlingoSchemaParserImpl();
            
            try (InputStream inputStream = new FileInputStream(xmlPath.toFile())) {
                ParseResult result = parser.parseSchema(inputStream, xmlPath.getFileName().toString());
                return result; // 直接返回结果，不管成功还是失败
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schema from loader/multi-schema/duplicate-namespace-schema.xml", e);
        }
    }
    
    /**
     * 验证XML文件是否包含多个Schema
     */
    public static boolean hasMultipleSchemas(String relativePath) {
        ParseResult result = loadSchemasFromXml(relativePath);
        return result.hasMultipleSchemas();
    }
    
    /**
     * 获取XML文件中的所有Schema的namespace
     */
    public static List<String> getSchemaNamespaces(String relativePath) {
        ParseResult result = loadSchemasFromXml(relativePath);
        List<String> namespaces = new ArrayList<>();
        for (SchemaWithDependencies schemaWithDeps : result.getSchemas()) {
            if (schemaWithDeps.getNamespace() != null) {
                namespaces.add(schemaWithDeps.getNamespace());
            }
        }
        return namespaces;
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
