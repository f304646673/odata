package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的CsdlEnumType，支持依赖关系跟踪
 * 使用组合模式包装CsdlEnumType，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlEnumType extends AbstractExtendedCsdlElement<CsdlEnumType, ExtendedCsdlEnumType> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlEnumType() {
        super(new CsdlEnumType());
    }

    /**
     * 从标准CsdlEnumType创建ExtendedCsdlEnumTypeRefactored
     */
    public static ExtendedCsdlEnumType fromCsdlEnumType(CsdlEnumType source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlEnumType extended = new ExtendedCsdlEnumType();

        // 复制基本属性
        extended.setName(source.getName());
        if (source.getUnderlyingType() != null) {
            extended.setUnderlyingType(source.getUnderlyingType());
        }
        extended.setFlags(source.isFlags());

        // 复制枚举成员
        if (source.getMembers() != null) {
            extended.setMembers(source.getMembers());
        }

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlEnumType
     */
    public CsdlEnumType asCsdlEnumType() {
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

    // ==================== CsdlEnumType 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlEnumType setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getUnderlyingType() {
        return wrappedElement.getUnderlyingType();
    }

    public ExtendedCsdlEnumType setUnderlyingType(String underlyingType) {
        wrappedElement.setUnderlyingType(underlyingType);
        return this;
    }

    public boolean isFlags() {
        return wrappedElement.isFlags();
    }

    public ExtendedCsdlEnumType setFlags(boolean isFlags) {
        wrappedElement.setFlags(isFlags);
        return this;
    }

    @Deprecated
    public List<CsdlEnumMember> getMembers() {
        return wrappedElement.getMembers();
    }

    public ExtendedCsdlEnumType setMembers(List<CsdlEnumMember> members) {
        wrappedElement.setMembers(members);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "EnumType_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.ENUM_TYPE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlEnumType{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", underlyingType=" + getUnderlyingType() +
                ", isFlags=" + isFlags() +
                ", membersCount=" + (getMembers() != null ? getMembers().size() : 0) +
                '}';
    }
}
