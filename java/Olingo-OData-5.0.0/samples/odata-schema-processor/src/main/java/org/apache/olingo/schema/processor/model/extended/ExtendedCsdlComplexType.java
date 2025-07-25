package org.apache.olingo.schema.processor.model.extended;

import java.util.HashSet;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;

/**
 * 扩展的CsdlComplexType，增加依赖关系追踪功能
 */
public class ExtendedCsdlComplexType extends CsdlComplexType {
    
    private final Set<String> dependencies = new HashSet<>();
    private String fullyQualifiedName;
    
    /**
     * 添加依赖
     * @param namespace 依赖的命名空间
     */
    public void addDependency(String namespace) {
        if (namespace != null && !namespace.trim().isEmpty()) {
            dependencies.add(namespace);
        }
    }
    
    /**
     * 移除依赖
     * @param namespace 要移除的命名空间
     * @return 是否成功移除
     */
    public boolean removeDependency(String namespace) {
        return dependencies.remove(namespace);
    }
    
    /**
     * 获取所有依赖
     * @return 依赖的命名空间集合
     */
    public Set<String> getDependencies() {
        return new HashSet<>(dependencies);
    }
    
    /**
     * 检查是否有特定依赖
     * @param namespace 要检查的命名空间
     * @return 是否存在该依赖
     */
    public boolean hasDependency(String namespace) {
        return dependencies.contains(namespace);
    }
    
    /**
     * 清除所有依赖
     */
    public void clearDependencies() {
        dependencies.clear();
    }
    
    /**
     * 获取依赖数量
     * @return 依赖数量
     */
    public int getDependencyCount() {
        return dependencies.size();
    }
    
    /**
     * 分析并设置依赖关系
     */
    public void analyzeDependencies() {
        dependencies.clear();
        
        // 分析BaseType依赖
        if (getBaseType() != null) {
            String baseTypeNamespace = extractNamespace(getBaseType());
            if (baseTypeNamespace != null) {
                addDependency(baseTypeNamespace);
            }
        }
        
        // 分析Property类型依赖
        if (getProperties() != null) {
            getProperties().forEach(property -> {
                String typeNamespace = extractNamespace(property.getType());
                if (typeNamespace != null) {
                    addDependency(typeNamespace);
                }
            });
        }
    }
    
    /**
     * 从类型名中提取namespace
     */
    private String extractNamespace(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return null;
        }
        
        // 处理Collection类型
        String actualType = typeName;
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            actualType = typeName.substring(11, typeName.length() - 1);
        }
        
        // 跳过EDM基础类型
        if (actualType.startsWith("Edm.")) {
            return null;
        }
        
        // 提取namespace
        int lastDotIndex = actualType.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return actualType.substring(0, lastDotIndex);
        }
        
        return null;
    }
    
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    
    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }
    
    @Override
    public ExtendedCsdlComplexType setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlComplexType setBaseType(String baseType) {
        super.setBaseType(baseType);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlComplexType setAbstract(boolean isAbstract) {
        super.setAbstract(isAbstract);
        return this;
    }
    
    @Override
    public ExtendedCsdlComplexType setOpenType(boolean isOpenType) {
        super.setOpenType(isOpenType);
        return this;
    }
}
