package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的CsdlTypeDefinition，支持依赖关系跟踪
 * 使用组合模式包装CsdlTypeDefinition，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlTypeDefinition extends AbstractExtendedCsdlElement<CsdlTypeDefinition, ExtendedCsdlTypeDefinition> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlTypeDefinition() {
        super(new CsdlTypeDefinition());
    }

    /**
     * 从标准CsdlTypeDefinition创建ExtendedCsdlTypeDefinitionRefactored
     */
    public static ExtendedCsdlTypeDefinition fromCsdlTypeDefinition(CsdlTypeDefinition source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlTypeDefinition extended = new ExtendedCsdlTypeDefinition();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setUnderlyingType(source.getUnderlyingType());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setUnicode(source.isUnicode());
        extended.setSrid(source.getSrid());

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlTypeDefinition
     */
    public CsdlTypeDefinition asCsdlTypeDefinition() {
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

    // ==================== CsdlTypeDefinition 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlTypeDefinition setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getUnderlyingType() {
        return wrappedElement.getUnderlyingType();
    }

    public ExtendedCsdlTypeDefinition setUnderlyingType(String underlyingType) {
        wrappedElement.setUnderlyingType(underlyingType);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedElement.getMaxLength();
    }

    public ExtendedCsdlTypeDefinition setMaxLength(Integer maxLength) {
        wrappedElement.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedElement.getPrecision();
    }

    public ExtendedCsdlTypeDefinition setPrecision(Integer precision) {
        wrappedElement.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedElement.getScale();
    }

    public ExtendedCsdlTypeDefinition setScale(Integer scale) {
        wrappedElement.setScale(scale);
        return this;
    }

    public Boolean isUnicode() {
        return wrappedElement.isUnicode();
    }

    public ExtendedCsdlTypeDefinition setUnicode(Boolean unicode) {
        wrappedElement.setUnicode(unicode);
        return this;
    }

    public SRID getSrid() {
        return wrappedElement.getSrid();
    }

    public ExtendedCsdlTypeDefinition setSrid(SRID srid) {
        wrappedElement.setSrid(srid);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "TypeDefinition_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.TYPE_DEFINITION;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlTypeDefinition{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", underlyingType=" + getUnderlyingType() +
                ", maxLength=" + getMaxLength() +
                ", precision=" + getPrecision() +
                ", scale=" + getScale() +
                '}';
    }
}
