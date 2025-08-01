package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlNavigationProperty，支持依赖关系跟踪
 * 使用组合模式包装CsdlNavigationProperty，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlNavigationProperty extends AbstractExtendedCsdlElement<CsdlNavigationProperty, ExtendedCsdlNavigationProperty> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlNavigationProperty() {
        super(new CsdlNavigationProperty());
    }

    /**
     * 从标准CsdlNavigationProperty创建ExtendedCsdlNavigationPropertyRefactored
     */
    public static ExtendedCsdlNavigationProperty fromCsdlNavigationProperty(CsdlNavigationProperty source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlNavigationProperty extended = new ExtendedCsdlNavigationProperty();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setPartner(source.getPartner());
        extended.setContainsTarget(source.isContainsTarget());

        // 复制ReferentialConstraints
        if (source.getReferentialConstraints() != null) {
            extended.setReferentialConstraints(new ArrayList<>(source.getReferentialConstraints()));
        }

        // 复制OnDelete
        if (source.getOnDelete() != null) {
            extended.setOnDelete(source.getOnDelete());
        }

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlNavigationProperty
     */
    public CsdlNavigationProperty asCsdlNavigationProperty() {
        return wrappedElement;
    }

    // ==================== 基类方法实现 ====================

    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        return wrappedElement.getAnnotations();
    }

    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        wrappedElement.setAnnotations(annotations);
    }

    // ==================== CsdlNavigationProperty 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlNavigationProperty setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getType() {
        return wrappedElement.getType();
    }

    public FullQualifiedName getTypeFQN() {
        return wrappedElement.getTypeFQN();
    }

    public ExtendedCsdlNavigationProperty setType(String type) {
        wrappedElement.setType(type);
        return this;
    }

    public ExtendedCsdlNavigationProperty setType(FullQualifiedName type) {
        wrappedElement.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedElement.isCollection();
    }

    public ExtendedCsdlNavigationProperty setCollection(boolean isCollection) {
        wrappedElement.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedElement.isNullable();
    }

    public ExtendedCsdlNavigationProperty setNullable(Boolean nullable) {
        wrappedElement.setNullable(nullable);
        return this;
    }

    public String getPartner() {
        return wrappedElement.getPartner();
    }

    public ExtendedCsdlNavigationProperty setPartner(String partner) {
        wrappedElement.setPartner(partner);
        return this;
    }

    public boolean isContainsTarget() {
        return wrappedElement.isContainsTarget();
    }

    public ExtendedCsdlNavigationProperty setContainsTarget(boolean containsTarget) {
        wrappedElement.setContainsTarget(containsTarget);
        return this;
    }

    @Deprecated
    public List<CsdlReferentialConstraint> getReferentialConstraints() {
        return wrappedElement.getReferentialConstraints();
    }

    public ExtendedCsdlNavigationProperty setReferentialConstraints(List<CsdlReferentialConstraint> referentialConstraints) {
        wrappedElement.setReferentialConstraints(referentialConstraints);
        return this;
    }

    public CsdlOnDelete getOnDelete() {
        return wrappedElement.getOnDelete();
    }

    public ExtendedCsdlNavigationProperty setOnDelete(CsdlOnDelete onDelete) {
        wrappedElement.setOnDelete(onDelete);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "NavigationProperty_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.NAVIGATION_PROPERTY_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlNavigationProperty{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", type='" + getType() + '\'' +
                ", isCollection=" + isCollection() +
                ", containsTarget=" + isContainsTarget() +
                '}';
    }
}
