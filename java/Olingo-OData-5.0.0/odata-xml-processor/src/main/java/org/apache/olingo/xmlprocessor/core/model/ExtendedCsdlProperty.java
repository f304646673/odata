package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlProperty，增加依赖关系追踪功能
 * 使用组合模式包装CsdlProperty，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlProperty extends AbstractExtendedCsdlElement<CsdlProperty, ExtendedCsdlProperty> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlProperty() {
        super(new CsdlProperty());
    }

    /**
     * 从标准CsdlProperty创建ExtendedCsdlPropertyRefactored
     */
    public static ExtendedCsdlProperty fromCsdlProperty(CsdlProperty source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlProperty extended = new ExtendedCsdlProperty();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setUnicode(source.isUnicode());
        extended.setDefaultValue(source.getDefaultValue());
        extended.setSrid(source.getSrid());

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlProperty
     */
    public CsdlProperty asCsdlProperty() {
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

    // ==================== CsdlProperty 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlProperty setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getType() {
        return wrappedElement.getType();
    }

    public FullQualifiedName getTypeFQN() {
        return wrappedElement.getTypeAsFQNObject();
    }

    public ExtendedCsdlProperty setType(String type) {
        wrappedElement.setType(type);
        return this;
    }

    public ExtendedCsdlProperty setType(FullQualifiedName type) {
        wrappedElement.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedElement.isCollection();
    }

    public ExtendedCsdlProperty setCollection(boolean isCollection) {
        wrappedElement.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedElement.isNullable();
    }

    public ExtendedCsdlProperty setNullable(Boolean nullable) {
        wrappedElement.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedElement.getMaxLength();
    }

    public ExtendedCsdlProperty setMaxLength(Integer maxLength) {
        wrappedElement.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedElement.getPrecision();
    }

    public ExtendedCsdlProperty setPrecision(Integer precision) {
        wrappedElement.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedElement.getScale();
    }

    public ExtendedCsdlProperty setScale(Integer scale) {
        wrappedElement.setScale(scale);
        return this;
    }

    public Boolean isUnicode() {
        return wrappedElement.isUnicode();
    }

    public ExtendedCsdlProperty setUnicode(Boolean unicode) {
        wrappedElement.setUnicode(unicode);
        return this;
    }

    public String getDefaultValue() {
        return wrappedElement.getDefaultValue();
    }

    public ExtendedCsdlProperty setDefaultValue(String defaultValue) {
        wrappedElement.setDefaultValue(defaultValue);
        return this;
    }

    public SRID getSrid() {
        return wrappedElement.getSrid();
    }

    public ExtendedCsdlProperty setSrid(SRID srid) {
        wrappedElement.setSrid(srid);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "Property_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.PROPERTY_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlProperty{" +
                "name='" + getName() + '\'' +
                ", type=" + getTypeFQN() +
                ", isCollection=" + isCollection() +
                ", nullable=" + isNullable() +
                '}';
    }
}
