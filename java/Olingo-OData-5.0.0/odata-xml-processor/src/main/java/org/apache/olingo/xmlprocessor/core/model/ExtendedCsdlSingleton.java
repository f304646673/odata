package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlSingleton，支持依赖关系跟踪
 * 使用组合模式包装CsdlSingleton，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlSingleton extends AbstractExtendedCsdlElement<CsdlSingleton, ExtendedCsdlSingleton> implements ExtendedCsdlElement {
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlNavigationPropertyBinding> extendedNavigationPropertyBindings = new ArrayList<>();

    /**
     * 构造函数
     */
    public ExtendedCsdlSingleton() {
        super(new CsdlSingleton());
    }

    /**
     * 从标准CsdlSingleton创建ExtendedCsdlSingletonRefactored
     */
    public static ExtendedCsdlSingleton fromCsdlSingleton(CsdlSingleton source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlSingleton extended = new ExtendedCsdlSingleton();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());

        // 级联构建NavigationPropertyBindings
        if (source.getNavigationPropertyBindings() != null) {
            for (CsdlNavigationPropertyBinding binding : source.getNavigationPropertyBindings()) {
                ExtendedCsdlNavigationPropertyBinding extendedBinding = 
                    ExtendedCsdlNavigationPropertyBinding.fromCsdlNavigationPropertyBinding(binding);
                extended.addExtendedNavigationPropertyBinding(extendedBinding);
            }
        }

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlSingleton
     */
    public CsdlSingleton asCsdlSingleton() {
        return wrappedElement;
    }

    // ==================== 基类方法实现 ====================

    @Deprecated
    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        return wrappedElement.getAnnotations();
    }

    @Deprecated
    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        wrappedElement.setAnnotations(annotations);
    }

    // ==================== CsdlSingleton 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlSingleton setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getType() {
        return wrappedElement.getType();
    }

    public ExtendedCsdlSingleton setType(String type) {
        wrappedElement.setType(type);
        return this;
    }

    @Deprecated
    public List<CsdlNavigationPropertyBinding> getNavigationPropertyBindings() {
        return wrappedElement.getNavigationPropertyBindings();
    }

    public ExtendedCsdlSingleton setNavigationPropertyBindings(List<CsdlNavigationPropertyBinding> bindings) {
        wrappedElement.setNavigationPropertyBindings(bindings);
        syncNavigationPropertyBindingsFromWrapped();
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlNavigationPropertyBinding> getExtendedNavigationPropertyBindings() {
        return new ArrayList<>(extendedNavigationPropertyBindings);
    }

    public ExtendedCsdlSingleton addExtendedNavigationPropertyBinding(ExtendedCsdlNavigationPropertyBinding binding) {
        if (binding != null) {
            extendedNavigationPropertyBindings.add(binding);
            syncNavigationPropertyBindingsToWrapped();
        }
        return this;
    }

    // ==================== 同步方法 ====================

    private void syncNavigationPropertyBindingsToWrapped() {
        List<CsdlNavigationPropertyBinding> csdlBindings = new ArrayList<>();
        for (ExtendedCsdlNavigationPropertyBinding extBinding : extendedNavigationPropertyBindings) {
            csdlBindings.add(extBinding.asCsdlNavigationPropertyBinding());
        }
        wrappedElement.setNavigationPropertyBindings(csdlBindings);
    }

    private void syncNavigationPropertyBindingsFromWrapped() {
        extendedNavigationPropertyBindings.clear();
        if (wrappedElement.getNavigationPropertyBindings() != null) {
            for (CsdlNavigationPropertyBinding binding : wrappedElement.getNavigationPropertyBindings()) {
                ExtendedCsdlNavigationPropertyBinding extBinding = 
                    ExtendedCsdlNavigationPropertyBinding.fromCsdlNavigationPropertyBinding(binding);
                extendedNavigationPropertyBindings.add(extBinding);
            }
        }
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "Singleton_" + super.hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (namespace != null && getName() != null) {
            return new FullQualifiedName(namespace, getName());
        }
        return null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.SINGLETON_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlSingleton{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", type='" + getType() + '\'' +
                ", navigationPropertyBindingsCount=" + extendedNavigationPropertyBindings.size() +
                '}';
    }
}
