package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的CsdlReturnType，支持依赖关系跟踪
 * 使用组合模式包装CsdlReturnType，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlReturnType extends AbstractExtendedCsdlElement<CsdlReturnType, ExtendedCsdlReturnType> implements ExtendedCsdlElement {
    
    // 扩展属性
    private String parentName;

    /**
     * 构造函数
     */
    public ExtendedCsdlReturnType() {
        super(new CsdlReturnType());
    }

    /**
     * 从标准CsdlReturnType创建ExtendedCsdlReturnTypeRefactored
     */
    public static ExtendedCsdlReturnType fromCsdlReturnType(CsdlReturnType source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlReturnType extended = new ExtendedCsdlReturnType();

        // 复制基本属性
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setSrid(source.getSrid());

        // ReturnType通常没有annotations，但为了统一性仍然处理
        if (source.getClass().getName().contains("CsdlReturnType") && 
            java.lang.reflect.Array.getLength(source.getClass().getDeclaredMethods()) > 0) {
            // 简化处理，CsdlReturnType没有getAnnotations方法
        }

        return extended;
    }

    /**
     * 获取底层的CsdlReturnType
     */
    public CsdlReturnType asCsdlReturnType() {
        return wrappedElement;
    }

    // ==================== 基类方法实现 ====================

    @Deprecated
    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        // CsdlReturnType没有annotations，返回null
        return null;
    }

    @Deprecated
    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        // CsdlReturnType没有annotations，不执行任何操作
    }

    // ==================== CsdlReturnType 方法委托 ====================

    public String getType() {
        return wrappedElement.getType();
    }

    public FullQualifiedName getTypeFQN() {
        return wrappedElement.getTypeFQN();
    }

    public ExtendedCsdlReturnType setType(String type) {
        wrappedElement.setType(type);
        return this;
    }

    public ExtendedCsdlReturnType setType(FullQualifiedName type) {
        wrappedElement.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedElement.isCollection();
    }

    public ExtendedCsdlReturnType setCollection(boolean isCollection) {
        wrappedElement.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedElement.isNullable();
    }

    public ExtendedCsdlReturnType setNullable(Boolean nullable) {
        wrappedElement.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedElement.getMaxLength();
    }

    public ExtendedCsdlReturnType setMaxLength(Integer maxLength) {
        wrappedElement.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedElement.getPrecision();
    }

    public ExtendedCsdlReturnType setPrecision(Integer precision) {
        wrappedElement.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedElement.getScale();
    }

    public ExtendedCsdlReturnType setScale(Integer scale) {
        wrappedElement.setScale(scale);
        return this;
    }

    public SRID getSrid() {
        return wrappedElement.getSrid();
    }

    public ExtendedCsdlReturnType setSrid(SRID srid) {
        wrappedElement.setSrid(srid);
        return this;
    }

    // ==================== 扩展属性 ====================

    public String getParentName() {
        return parentName;
    }

    public ExtendedCsdlReturnType setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getType() != null) {
            return "ReturnType_" + wrappedElement.getType();
        }
        return "ReturnType_" + super.hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (namespace != null && getType() != null) {
            return new FullQualifiedName(namespace, getType());
        }
        return null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.RETURN_TYPE_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getType();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlReturnType{" +
                "namespace='" + namespace + '\'' +
                ", type='" + getType() + '\'' +
                ", isCollection=" + isCollection() +
                ", isNullable=" + isNullable() +
                ", parentName='" + parentName + '\'' +
                '}';
    }
}
