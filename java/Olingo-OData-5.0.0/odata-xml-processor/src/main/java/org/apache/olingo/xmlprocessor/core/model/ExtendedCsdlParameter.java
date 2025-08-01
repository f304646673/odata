package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlParameter，支持依赖关系跟踪
 * 使用组合模式包装CsdlParameter，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlParameter extends AbstractExtendedCsdlElement<CsdlParameter, ExtendedCsdlParameter> implements ExtendedCsdlElement {
    
    // 扩展属性
    private String parentName;

    /**
     * 构造函数
     */
    public ExtendedCsdlParameter() {
        super(new CsdlParameter());
    }

    /**
     * 从标准CsdlParameter创建ExtendedCsdlParameterRefactored
     */
    public static ExtendedCsdlParameter fromCsdlParameter(CsdlParameter source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlParameter extended = new ExtendedCsdlParameter();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setSrid(source.getSrid());

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlParameter
     */
    public CsdlParameter asCsdlParameter() {
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

    // ==================== CsdlParameter 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlParameter setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getType() {
        return wrappedElement.getType();
    }

    public FullQualifiedName getTypeFQN() {
        return wrappedElement.getTypeFQN();
    }

    public ExtendedCsdlParameter setType(String type) {
        wrappedElement.setType(type);
        return this;
    }

    public ExtendedCsdlParameter setType(FullQualifiedName type) {
        wrappedElement.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedElement.isCollection();
    }

    public ExtendedCsdlParameter setCollection(boolean isCollection) {
        wrappedElement.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedElement.isNullable();
    }

    public ExtendedCsdlParameter setNullable(Boolean nullable) {
        wrappedElement.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedElement.getMaxLength();
    }

    public ExtendedCsdlParameter setMaxLength(Integer maxLength) {
        wrappedElement.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedElement.getPrecision();
    }

    public ExtendedCsdlParameter setPrecision(Integer precision) {
        wrappedElement.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedElement.getScale();
    }

    public ExtendedCsdlParameter setScale(Integer scale) {
        wrappedElement.setScale(scale);
        return this;
    }

    public SRID getSrid() {
        return wrappedElement.getSrid();
    }

    public ExtendedCsdlParameter setSrid(SRID srid) {
        wrappedElement.setSrid(srid);
        return this;
    }

    // ==================== 扩展属性 ====================

    public String getParentName() {
        return parentName;
    }

    public ExtendedCsdlParameter setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "Parameter_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.PARAMETER_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlParameter{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", type='" + getType() + '\'' +
                ", isCollection=" + isCollection() +
                ", isNullable=" + isNullable() +
                ", parentName='" + parentName + '\'' +
                '}';
    }
}
