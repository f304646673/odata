package org.apache.olingo.schemamanager.repository;

import org.apache.olingo.commons.api.edm.provider.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Schema仓储接口
 * 负责管理和查询OData Schema信息
 */
public interface SchemaRepository {
    
    /**
     * 添加Schema到仓储
     * @param schema CSDL Schema
     * @param filePath 文件路径
     */
    void addSchema(CsdlSchema schema, String filePath);
    
    /**
     * 根据namespace获取Schema
     * @param namespace 命名空间
     * @return CSDL Schema
     */
    CsdlSchema getSchema(String namespace);
    
    /**
     * 获取所有Schema
     * @return Schema映射 (namespace -> schema)
     */
    Map<String, CsdlSchema> getAllSchemas();
    
    /**
     * 根据完全限定名查找EntityType
     * @param fullQualifiedName 完全限定名 (namespace.typeName)
     * @return EntityType
     */
    CsdlEntityType getEntityType(String fullQualifiedName);
    
    /**
     * 根据完全限定名查找ComplexType
     * @param fullQualifiedName 完全限定名
     * @return ComplexType
     */
    CsdlComplexType getComplexType(String fullQualifiedName);
    
    /**
     * 根据完全限定名查找EnumType
     * @param fullQualifiedName 完全限定名
     * @return EnumType
     */
    CsdlEnumType getEnumType(String fullQualifiedName);
    
    /**
     * 根据namespace和名称查找EntityType
     * @param namespace 命名空间
     * @param typeName 类型名称
     * @return EntityType
     */
    CsdlEntityType getEntityType(String namespace, String typeName);
    
    /**
     * 根据namespace和名称查找ComplexType
     * @param namespace 命名空间
     * @param typeName 类型名称
     * @return ComplexType
     */
    CsdlComplexType getComplexType(String namespace, String typeName);
    
    /**
     * 根据namespace和名称查找EnumType
     * @param namespace 命名空间
     * @param typeName 类型名称
     * @return EnumType
     */
    CsdlEnumType getEnumType(String namespace, String typeName);
    
    /**
     * 获取指定namespace下的所有EntityType
     * @param namespace 命名空间
     * @return EntityType列表
     */
    List<CsdlEntityType> getEntityTypes(String namespace);
    
    /**
     * 获取指定namespace下的所有ComplexType
     * @param namespace 命名空间
     * @return ComplexType列表
     */
    List<CsdlComplexType> getComplexTypes(String namespace);
    
    /**
     * 获取指定namespace下的所有EnumType
     * @param namespace 命名空间
     * @return EnumType列表
     */
    List<CsdlEnumType> getEnumTypes(String namespace);
    
    /**
     * 获取所有可用的namespace
     * @return namespace集合
     */
    Set<String> getAllNamespaces();
    
    /**
     * 获取Schema的文件路径
     * @param namespace 命名空间
     * @return 文件路径
     */
    String getSchemaFilePath(String namespace);
    
    /**
     * 清空仓储
     */
    void clear();
    
    /**
     * 获取仓储统计信息
     * @return 统计信息
     */
    RepositoryStatistics getStatistics();
    
    /**
     * 仓储统计信息类
     */
    class RepositoryStatistics {
        private final int totalSchemas;
        private final int totalEntityTypes;
        private final int totalComplexTypes;
        private final int totalEnumTypes;
        private final int totalEntityContainers;
        
        public RepositoryStatistics(int totalSchemas, int totalEntityTypes, int totalComplexTypes, 
                                  int totalEnumTypes, int totalEntityContainers) {
            this.totalSchemas = totalSchemas;
            this.totalEntityTypes = totalEntityTypes;
            this.totalComplexTypes = totalComplexTypes;
            this.totalEnumTypes = totalEnumTypes;
            this.totalEntityContainers = totalEntityContainers;
        }
        
        // Getters
        public int getTotalSchemas() { return totalSchemas; }
        public int getTotalEntityTypes() { return totalEntityTypes; }
        public int getTotalComplexTypes() { return totalComplexTypes; }
        public int getTotalEnumTypes() { return totalEnumTypes; }
        public int getTotalEntityContainers() { return totalEntityContainers; }
    }
}
