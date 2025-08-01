package org.apache.olingo.xmlprocessor.core.model;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlFunctionImport，增加依赖关系追踪功能
 */
public class ExtendedCsdlFunctionImport extends CsdlFunctionImport implements ExtendedCsdlElement {

    private final Set<String> dependencies = new HashSet<>();
    private String fullyQualifiedName;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlFunctionImport() {
        super();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlFunctionImport创建ExtendedCsdlFunctionImport
     */
    public static ExtendedCsdlFunctionImport fromCsdlFunctionImport(CsdlFunctionImport source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlFunctionImport extended = new ExtendedCsdlFunctionImport();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setFunction(source.getFunction());
        extended.setEntitySet(source.getEntitySet());
        extended.setIncludeInServiceDocument(source.isIncludeInServiceDocument());

        // 转换Annotations为ExtendedCsdlAnnotation
        if (source.getAnnotations() != null) {
            List<CsdlAnnotation> extendedAnnotations = source.getAnnotations().stream()
                .map(annotation -> ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation))
                .collect(Collectors.toList());
            extended.setAnnotations(extendedAnnotations);
        }

        return extended;
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<>();
    }

    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "FunctionImport_" + hashCode();
    }

    @Override
    public ExtendedCsdlFunctionImport setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlFunctionImport registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }

    /**
     * 获取元素的完全限定名（如果适用）
     */
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }

    /**
     * 获取元素的依赖类型
     */
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.FUNCTION_IMPORT_REFERENCE;
    }

    /**
     * 添加依赖 - 重写接口方法以匹配签名
     * @param namespace 依赖的命名空间
     * @return 是否成功添加
     */
    @Override
    public boolean addDependency(String namespace) {
        if (namespace != null && !namespace.trim().isEmpty()) {
            dependencies.add(namespace);
            return true;
        }
        return false;
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
     * 获取所有依赖 - 为了兼容性保留的方法
     * @return 依赖的命名空间集合
     */
    public Set<String> getStringDependencies() {
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
        
        // 分析Function依赖
        FullQualifiedName functionFQN = getFunctionFQN();
        if (functionFQN != null) {
            String functionNamespace = extractNamespace(functionFQN.getFullQualifiedNameAsString());
            if (functionNamespace != null) {
                addDependency(functionNamespace);
            }
        }
        
        // 分析EntitySet依赖
        if (getEntitySet() != null) {
            String entitySetNamespace = extractNamespace(getEntitySet());
            if (entitySetNamespace != null) {
                addDependency(entitySetNamespace);
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
        if (fullyQualifiedName != null) {
            return fullyQualifiedName;
        }
        if (namespace != null && getName() != null) {
            return namespace + "." + getName();
        }
        return getName();
    }
    
    // Extended集合的getter方法
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
    
    @Override
    public ExtendedCsdlFunctionImport setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlFunctionImport setFunction(String function) {
        super.setFunction(function);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlFunctionImport setEntitySet(String entitySet) {
        super.setEntitySet(entitySet);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlFunctionImport setIncludeInServiceDocument(boolean includeInServiceDocument) {
        super.setIncludeInServiceDocument(includeInServiceDocument);
        return this;
    }

    @Override
    public String getElementPropertyName() {
        return null; // FunctionImport通常不关联特定属性
    }
}
