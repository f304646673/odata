package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlReturnType，支持依赖关系跟踪
 * 使用组合模式包装CsdlReturnType，保持内部数据联动
 */
public class ExtendedCsdlReturnType implements ExtendedCsdlElement {
    
    private final CsdlReturnType wrappedReturnType;
    private String namespace;
    private String parentName;

    /**
     * 构造函数
     */
    public ExtendedCsdlReturnType() {
        this.wrappedReturnType = new CsdlReturnType();
    }

    /**
     * 从标准CsdlReturnType创建ExtendedCsdlReturnType
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

        return extended;
    }

    /**
     * 获取底层的CsdlReturnType
     */
    public CsdlReturnType asCsdlReturnType() {
        return wrappedReturnType;
    }

    // ==================== CsdlReturnType 方法委托 ====================

    public String getType() {
        return wrappedReturnType.getType();
    }

    public FullQualifiedName getTypeFQN() {
        return wrappedReturnType.getTypeFQN();
    }

    public ExtendedCsdlReturnType setType(String type) {
        wrappedReturnType.setType(type);
        return this;
    }

    public ExtendedCsdlReturnType setType(FullQualifiedName type) {
        wrappedReturnType.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedReturnType.isCollection();
    }

    public ExtendedCsdlReturnType setCollection(boolean isCollection) {
        wrappedReturnType.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedReturnType.isNullable();
    }

    public ExtendedCsdlReturnType setNullable(Boolean nullable) {
        wrappedReturnType.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedReturnType.getMaxLength();
    }

    public ExtendedCsdlReturnType setMaxLength(Integer maxLength) {
        wrappedReturnType.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedReturnType.getPrecision();
    }

    public ExtendedCsdlReturnType setPrecision(Integer precision) {
        wrappedReturnType.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedReturnType.getScale();
    }

    public ExtendedCsdlReturnType setScale(Integer scale) {
        wrappedReturnType.setScale(scale);
        return this;
    }

    public SRID getSrid() {
        return wrappedReturnType.getSrid();
    }

    public ExtendedCsdlReturnType setSrid(SRID srid) {
        wrappedReturnType.setSrid(srid);
        return this;
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedReturnType.getType() != null) {
            return "ReturnType_" + wrappedReturnType.getType();
        }
        return "ReturnType_" + hashCode();
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

    // ==================== 扩展属性 ====================

    public String getNamespace() {
        return namespace;
    }

    public ExtendedCsdlReturnType setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getParentName() {
        return parentName;
    }

    public ExtendedCsdlReturnType setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlReturnType{" +
                "type='" + getType() + '\'' +
                ", isCollection=" + isCollection() +
                ", namespace='" + namespace + '\'' +
                ", parentName='" + parentName + '\'' +
                '}';
    }
}
