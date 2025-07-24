package org.apache.olingo.schemamanager.analyzer;

import org.apache.olingo.commons.api.edm.provider.*;
import java.util.List;
import java.util.Set;

/**
 * 类型依赖分析器接口
 * 负责分析EntityType、ComplexType、EnumType之间的依赖关系
 */
public interface TypeDependencyAnalyzer {
    
    /**
     * 获取EntityType的所有直接依赖
     * @param entityType EntityType
     * @return 依赖的类型信息列表
     */
    List<TypeReference> getDirectDependencies(CsdlEntityType entityType);
    
    /**
     * 获取ComplexType的所有直接依赖
     * @param complexType ComplexType
     * @return 依赖的类型信息列表
     */
    List<TypeReference> getDirectDependencies(CsdlComplexType complexType);
    
    /**
     * 获取EntityType的所有递归依赖（包括间接依赖）
     * @param entityType EntityType
     * @return 依赖的类型信息列表
     */
    List<TypeReference> getAllDependencies(CsdlEntityType entityType);
    
    /**
     * 获取ComplexType的所有递归依赖（包括间接依赖）
     * @param complexType ComplexType
     * @return 依赖的类型信息列表
     */
    List<TypeReference> getAllDependencies(CsdlComplexType complexType);
    
    /**
     * 获取指定类型的所有依赖者（哪些类型依赖于它）
     * @param fullQualifiedName 完全限定名
     * @return 依赖者的类型信息列表
     */
    List<TypeReference> getDependents(String fullQualifiedName);
    
    /**
     * 检查两个类型之间是否存在依赖关系
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 是否存在依赖关系
     */
    boolean hasDependency(String sourceType, String targetType);
    
    /**
     * 获取类型的依赖路径
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 依赖路径，如果不存在依赖返回空列表
     */
    List<String> getDependencyPath(String sourceType, String targetType);
    
    /**
     * 检测循环依赖
     * @return 循环依赖信息列表
     */
    List<CircularDependency> detectCircularDependencies();
    
    /**
     * 根据Container构建完整的依赖图
     * @param entityContainer EntityContainer
     * @return 依赖图
     */
    DependencyGraph buildDependencyGraph(CsdlEntityContainer entityContainer);
    
    /**
     * 手工构建Container并提取所有关联类型
     * @param entitySetDefinitions EntitySet定义
     * @return 手工构建的依赖图
     */
    DependencyGraph buildCustomDependencyGraph(List<EntitySetDefinition> entitySetDefinitions);
    
    /**
     * 类型引用类
     */
    class TypeReference {
        private final String fullQualifiedName;
        private final TypeKind typeKind;
        private final String propertyName;
        private final boolean isCollection;
        
        public TypeReference(String fullQualifiedName, TypeKind typeKind, String propertyName, boolean isCollection) {
            this.fullQualifiedName = fullQualifiedName;
            this.typeKind = typeKind;
            this.propertyName = propertyName;
            this.isCollection = isCollection;
        }
        
        // Getters
        public String getFullQualifiedName() { return fullQualifiedName; }
        public TypeKind getTypeKind() { return typeKind; }
        public String getPropertyName() { return propertyName; }
        public boolean isCollection() { return isCollection; }
    }
    
    /**
     * 类型种类枚举
     */
    enum TypeKind {
        ENTITY_TYPE,
        COMPLEX_TYPE,
        ENUM_TYPE,
        PRIMITIVE_TYPE
    }
    
    /**
     * 循环依赖信息类
     */
    class CircularDependency {
        private final List<String> dependencyChain;
        
        public CircularDependency(List<String> dependencyChain) {
            this.dependencyChain = dependencyChain;
        }
        
        public List<String> getDependencyChain() { return dependencyChain; }
    }
    
    /**
     * 依赖图类
     */
    class DependencyGraph {
        private final Set<String> allTypes;
        private final List<TypeReference> allDependencies;
        private final CsdlEntityContainer container;
        
        public DependencyGraph(Set<String> allTypes, List<TypeReference> allDependencies, CsdlEntityContainer container) {
            this.allTypes = allTypes;
            this.allDependencies = allDependencies;
            this.container = container;
        }
        
        // Getters
        public Set<String> getAllTypes() { return allTypes; }
        public List<TypeReference> getAllDependencies() { return allDependencies; }
        public CsdlEntityContainer getContainer() { return container; }
    }
    
    /**
     * EntitySet定义类
     */
    class EntitySetDefinition {
        private final String entitySetName;
        private final String entityTypeName;
        
        public EntitySetDefinition(String entitySetName, String entityTypeName) {
            this.entitySetName = entitySetName;
            this.entityTypeName = entityTypeName;
        }
        
        // Getters
        public String getEntitySetName() { return entitySetName; }
        public String getEntityTypeName() { return entityTypeName; }
    }
}
