package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlAction，支持依赖关系跟踪
 */
public class ExtendedCsdlAction extends CsdlAction implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;
    
    /**
     * 构造函数
     */
    public ExtendedCsdlAction() {
        this.elementId = null;
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlAction(String elementId) {
        this.elementId = elementId;
    }
    
    @Override
    public String getElementId() {
        if (elementId != null) {
            return elementId;
        }
        if (getName() != null) {
            return getName();
        }
        return "Action_" + hashCode();
    }
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlAction setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Get namespace
     */
    @Override
    public String getNamespace() {
        return this.namespace;
    }
    
    /**
     * Override registerElement to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlAction registerElement() {
        // Call the interface default method but return this instance
        ExtendedCsdlElement.super.registerElement();
        return this;
    }
    
    /**
     * 获取元素的完全限定名（如果适用）
     */
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }
    
    /**
     * 获取元素的依赖类型
     */
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.ACTION;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    public String getElementPropertyName() {
        return null; // Action通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlAction{name='%s', bound=%s}", 
                getName(), isBound());
    }
}
