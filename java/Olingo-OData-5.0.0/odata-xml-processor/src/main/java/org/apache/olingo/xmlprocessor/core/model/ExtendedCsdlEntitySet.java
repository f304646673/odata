package org.apache.olingo.xmlprocessor.core.model;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlEntitySet，增加依赖关系追踪功能
 */
public class ExtendedCsdlEntitySet extends CsdlEntitySet implements ExtendedCsdlElement {

    private final Set<String> dependencies = new HashSet<>();
    private String fullyQualifiedName;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlEntitySet() {
        super();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlEntitySet创建ExtendedCsdlEntitySet
     */
    public static ExtendedCsdlEntitySet fromCsdlEntitySet(CsdlEntitySet source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlEntitySet extended = new ExtendedCsdlEntitySet();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setIncludeInServiceDocument(source.isIncludeInServiceDocument());

        // 复制NavigationPropertyBindings
        if (source.getNavigationPropertyBindings() != null) {
            extended.setNavigationPropertyBindings(new ArrayList<>(source.getNavigationPropertyBindings()));
        }

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
        return "EntitySet_" + hashCode();
    }

    @Override
    public ExtendedCsdlEntitySet setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlEntitySet registerElement() {
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
        return CsdlDependencyNode.DependencyType.ENTITY_SET_REFERENCE;
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
     * 移除依赖 - 重写接口方法以匹配签名
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
     * 检查是否有特定依赖 - 为了兼容性保留的方法
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
        
        // 分析EntityType依赖
        FullQualifiedName typeFQN = getTypeFQN();
        if (typeFQN != null) {
            String typeNamespace = extractNamespace(typeFQN.getFullQualifiedNameAsString());
            if (typeNamespace != null) {
                addDependency(typeNamespace);
            }
        }
        
        // 分析NavigationPropertyBinding的Target依赖
        if (getNavigationPropertyBindings() != null) {
            getNavigationPropertyBindings().forEach(binding -> {
                if (binding.getTarget() != null) {
                    // Target可能是另一个EntitySet，可能在不同的namespace
                    String targetNamespace = extractNamespace(binding.getTarget());
                    if (targetNamespace != null) {
                        addDependency(targetNamespace);
                    }
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
    
    /**
     * 获取完全限定名
     */
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
    public ExtendedCsdlEntitySet setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlEntitySet setType(String type) {
        super.setType(type);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlEntitySet setIncludeInServiceDocument(boolean includeInServiceDocument) {
        super.setIncludeInServiceDocument(includeInServiceDocument);
        return this;
    }

    @Override
    public String getElementPropertyName() {
        return null; // EntitySet通常不关联特定属性
    }
}
