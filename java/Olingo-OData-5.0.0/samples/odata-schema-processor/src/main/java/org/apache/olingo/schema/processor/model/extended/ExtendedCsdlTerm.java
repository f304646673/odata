package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;

/**
 * 扩展的CSDL术语
 * 继承自CsdlTerm，增加依赖跟踪和扩展功能
 */
public class ExtendedCsdlTerm extends CsdlTerm implements ExtendedCsdlElement {
    
    private String namespace;
    
    /**
     * 默认构造函数
     */
    public ExtendedCsdlTerm() {
        super();
    }
    
    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "Term_" + hashCode();
    }
    
    @Override
    public ExtendedCsdlTerm setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlTerm registerElement() {
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
        return null; // Term通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return "ExtendedCsdlTerm{" +
                "name='" + getName() + '\'' +
                ", namespace='" + getNamespace() + '\'' +
                ", type='" + getType() + '\'' +
                ", baseTerm='" + getBaseTerm() + '\'' +
                ", maxLength=" + getMaxLength() +
                ", precision=" + getPrecision() +
                ", scale=" + getScale() +
                ", appliesToCount=" + (getAppliesTo() != null ? getAppliesTo().size() : 0) +
                ", defaultValue='" + getDefaultValue() + '\'' +
                '}';
    }
}
