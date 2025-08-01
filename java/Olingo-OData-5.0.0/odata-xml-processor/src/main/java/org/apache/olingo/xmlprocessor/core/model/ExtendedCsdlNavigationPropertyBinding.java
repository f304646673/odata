package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlNavigationPropertyBinding，支持依赖关系跟踪
 * 使用组合模式包装CsdlNavigationPropertyBinding，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlNavigationPropertyBinding extends AbstractExtendedCsdlElement<CsdlNavigationPropertyBinding, ExtendedCsdlNavigationPropertyBinding> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlNavigationPropertyBinding() {
        super(new CsdlNavigationPropertyBinding());
    }

    /**
     * 从标准CsdlNavigationPropertyBinding创建ExtendedCsdlNavigationPropertyBindingRefactored
     */
    public static ExtendedCsdlNavigationPropertyBinding fromCsdlNavigationPropertyBinding(CsdlNavigationPropertyBinding source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlNavigationPropertyBinding extended = new ExtendedCsdlNavigationPropertyBinding();

        // 复制基本属性
        extended.setPath(source.getPath());
        extended.setTarget(source.getTarget());

        // NavigationPropertyBinding通常没有annotations，但为了统一性保留基类功能
        return extended;
    }

    /**
     * 获取底层的CsdlNavigationPropertyBinding
     */
    public CsdlNavigationPropertyBinding asCsdlNavigationPropertyBinding() {
        return wrappedElement;
    }

    // ==================== 基类方法实现 ====================

    @Deprecated
    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        // CsdlNavigationPropertyBinding没有annotations，返回null
        return null;
    }

    @Deprecated
    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        // CsdlNavigationPropertyBinding没有annotations，不执行任何操作
    }

    // ==================== CsdlNavigationPropertyBinding 方法委托 ====================

    public String getPath() {
        return wrappedElement.getPath();
    }

    public ExtendedCsdlNavigationPropertyBinding setPath(String path) {
        wrappedElement.setPath(path);
        return this;
    }

    public String getTarget() {
        return wrappedElement.getTarget();
    }

    public ExtendedCsdlNavigationPropertyBinding setTarget(String target) {
        wrappedElement.setTarget(target);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getPath() != null) {
            return "Binding_" + wrappedElement.getPath();
        }
        return "NavigationPropertyBinding_" + super.hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (namespace != null && getPath() != null) {
            return new FullQualifiedName(namespace, getPath());
        }
        return null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.NAVIGATION_PROPERTY_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getPath();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlNavigationPropertyBinding{" +
                "path='" + getPath() + '\'' +
                ", target='" + getTarget() + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
