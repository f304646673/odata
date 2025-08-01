package org.apache.olingo.xmlprocessor.core.model;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlSingleton，增加依赖关系追踪功能
 */
public class ExtendedCsdlSingleton extends CsdlSingleton implements ExtendedCsdlElement {

    private final Set<String> dependencies = new HashSet<>();
    private String fullyQualifiedName;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlSingleton() {
        super();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlSingleton创建ExtendedCsdlSingleton
     */
    public static ExtendedCsdlSingleton fromCsdlSingleton(CsdlSingleton source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlSingleton extended = new ExtendedCsdlSingleton();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());

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
        return "Singleton_" + hashCode();
    }

    @Override
    public ExtendedCsdlSingleton setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlSingleton registerElement() {
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
        return CsdlDependencyNode.DependencyType.SINGLETON_REFERENCE;
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
     * 获取所有依赖 - 重命名以避免与接口方法冲突
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
    
    @Override
    public String getElementPropertyName() {
        return null; // Singleton通常不关联特定属性
    }

    // Extended集合的getter方法
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
}
