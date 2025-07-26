package org.apache.olingo.schema.processor.model.extended;

import java.util.HashSet;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;

/**
 * 扩展的CsdlAction，增加依赖关系追踪功能
 */
public class ExtendedCsdlAction extends CsdlAction {
    
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
        
        // 分析参数类型依赖
        if (getParameters() != null) {
            getParameters().forEach(parameter -> {
                String typeNamespace = extractNamespace(parameter.getType());
                if (typeNamespace != null) {
                    addDependency(typeNamespace);
                }
            });
        }
        
        // 分析返回类型依赖
        if (getReturnType() != null && getReturnType().getType() != null) {
            String returnTypeNamespace = extractNamespace(getReturnType().getType());
            if (returnTypeNamespace != null) {
                addDependency(returnTypeNamespace);
            }
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
    public ExtendedCsdlAction setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlAction setBound(boolean isBound) {
        super.setBound(isBound);
        return this;
    }
    
    @Override
    public ExtendedCsdlAction setEntitySetPath(String entitySetPath) {
        super.setEntitySetPath(entitySetPath);
        return this;
    }
}
