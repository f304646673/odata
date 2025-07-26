package org.apache.olingo.schema.processor.model.extended;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;

/**
 * 扩展的CsdlActionImport，增加详细依赖关系追踪功能
 */
public class ExtendedCsdlActionImport extends CsdlActionImport {
    
    // 保留旧的简单依赖跟踪（向后兼容）
    private final Set<String> dependencies = new HashSet<>();
    
    // 新的详细依赖跟踪
    private final Set<DetailedDependency> detailedDependencies = new HashSet<>();
    private final List<String> dependencyChains = new ArrayList<>();
    
    private String fullyQualifiedName;
    
    /**
     * 详细依赖信息内部类
     */
    public static class DetailedDependency {
        private final String sourceElement;
        private final String targetNamespace;
        private final String targetElement;
        private final String dependencyType;
        private final String propertyName;
        
        public DetailedDependency(String sourceElement, String targetNamespace, 
                                String targetElement, String dependencyType, String propertyName) {
            this.sourceElement = sourceElement;
            this.targetNamespace = targetNamespace;
            this.targetElement = targetElement;
            this.dependencyType = dependencyType;
            this.propertyName = propertyName;
        }
        
        public String getSourceElement() { return sourceElement; }
        public String getTargetNamespace() { return targetNamespace; }
        public String getTargetElement() { return targetElement; }
        public String getDependencyType() { return dependencyType; }
        public String getPropertyName() { return propertyName; }
        public String getFullTargetName() { return targetNamespace + "." + targetElement; }
        
        @Override
        public String toString() {
            return String.format("%s -[%s:%s]-> %s.%s", 
                sourceElement, dependencyType, propertyName, targetNamespace, targetElement);
        }
    }
    
    // === 新的详细依赖跟踪方法 ===
    
    /**
     * 添加详细依赖信息
     */
    public void addDetailedDependency(String targetNamespace, String targetElement, 
                                    String dependencyType, String propertyName) {
        DetailedDependency dependency = new DetailedDependency(
            this.fullyQualifiedName != null ? this.fullyQualifiedName : this.getName(),
            targetNamespace, targetElement, dependencyType, propertyName
        );
        detailedDependencies.add(dependency);
        
        // 同时更新简单依赖（向后兼容）
        addDependency(targetNamespace);
    }
    
    /**
     * 获取所有详细依赖信息
     */
    public Set<DetailedDependency> getDetailedDependencies() {
        return new HashSet<>(detailedDependencies);
    }
    
    /**
     * 获取指定类型的依赖
     */
    public Set<DetailedDependency> getDependenciesByType(String dependencyType) {
        Set<DetailedDependency> result = new HashSet<>();
        for (DetailedDependency dep : detailedDependencies) {
            if (dependencyType.equals(dep.getDependencyType())) {
                result.add(dep);
            }
        }
        return result;
    }
    
    /**
     * 获取指定命名空间的详细依赖
     */
    public Set<DetailedDependency> getDetailedDependenciesByNamespace(String namespace) {
        Set<DetailedDependency> result = new HashSet<>();
        for (DetailedDependency dep : detailedDependencies) {
            if (namespace.equals(dep.getTargetNamespace())) {
                result.add(dep);
            }
        }
        return result;
    }
    
    /**
     * 构建依赖链字符串
     */
    public List<String> getDependencyChainStrings() {
        return new ArrayList<>(dependencyChains);
    }
    
    /**
     * 添加依赖链
     */
    public void addDependencyChain(String chainString) {
        if (chainString != null && !chainString.trim().isEmpty()) {
            dependencyChains.add(chainString);
        }
    }
    
    /**
     * 清除所有详细依赖信息
     */
    public void clearDetailedDependencies() {
        detailedDependencies.clear();
        dependencyChains.clear();
    }
    
    /**
     * 获取所有被依赖的元素全名
     */
    public Set<String> getAllDependentElementNames() {
        Set<String> result = new HashSet<>();
        for (DetailedDependency dep : detailedDependencies) {
            result.add(dep.getFullTargetName());
        }
        return result;
    }
    
    // === 原有的简单依赖跟踪方法（向后兼容） ===
    
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
     * 分析并设置依赖关系（增强版）
     */
    public void analyzeDependencies() {
        // 清除旧的依赖
        dependencies.clear();
        detailedDependencies.clear();
        dependencyChains.clear();
        
        // 分析Action依赖
        try {
            String actionName = getAction();
            if (actionName != null) {
                String actionNamespace = extractNamespace(actionName);
                String actionElement = extractElementName(actionName);
                if (actionNamespace != null && actionElement != null) {
                    addDetailedDependency(actionNamespace, actionElement, "ACTION_REFERENCE", "action");
                }
            }
        } catch (Exception e) {
            // 忽略错误，可能是因为Action未正确设置
        }
        
        // 分析EntitySet依赖
        try {
            String entitySetName = getEntitySet();
            if (entitySetName != null) {
                String entitySetNamespace = extractNamespace(entitySetName);
                String entitySetElement = extractElementName(entitySetName);
                if (entitySetNamespace != null && entitySetElement != null) {
                    addDetailedDependency(entitySetNamespace, entitySetElement, "ENTITY_SET", "entitySet");
                }
            }
        } catch (Exception e) {
            // 忽略错误，可能是因为EntitySet未正确设置
        }
        
        // 构建依赖链
        buildDependencyChains();
    }
    
    /**
     * 构建依赖链字符串表示
     */
    private void buildDependencyChains() {
        String rootElement = this.fullyQualifiedName != null ? this.fullyQualifiedName : this.getName();
        
        for (DetailedDependency dep : detailedDependencies) {
            String chainString = String.format("%s -> %s (%s:%s)", 
                rootElement, dep.getFullTargetName(), dep.getDependencyType(), dep.getPropertyName());
            addDependencyChain(chainString);
        }
    }
    
    /**
     * 提取元素名称
     * @param fullName 完全限定名
     * @return 元素名称
     */
    private String extractElementName(String fullName) {
        if (fullName == null || !fullName.contains(".")) {
            return fullName;
        }
        int lastDotIndex = fullName.lastIndexOf(".");
        return fullName.substring(lastDotIndex + 1);
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
    public ExtendedCsdlActionImport setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlActionImport setAction(String action) {
        super.setAction(action);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlActionImport setEntitySet(String entitySet) {
        super.setEntitySet(entitySet);
        analyzeDependencies();
        return this;
    }
}
