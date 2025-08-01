package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CSDL枚举类型
 * 继承自CsdlEnumType，增加依赖跟踪和扩展功能
 */
public class ExtendedCsdlEnumType extends CsdlEnumType implements ExtendedCsdlElement {
    
    private String namespace;
    
    /**
     * 默认构造函数
     */
    public ExtendedCsdlEnumType() {
        super();
    }
    
    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "EnumType_" + hashCode();
    }
    
    @Override
    public ExtendedCsdlEnumType setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlEnumType registerElement() {
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
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    @Override
    public String getElementPropertyName() {
        return null; // EnumType通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return "ExtendedCsdlEnumType{" +
                "name='" + getName() + '\'' +
                ", namespace='" + getNamespace() + '\'' +
                ", underlyingType=" + getUnderlyingType() +
                ", membersCount=" + (getMembers() != null ? getMembers().size() : 0) +
                ", isFlags=" + isFlags() +
                '}';
    }
}
