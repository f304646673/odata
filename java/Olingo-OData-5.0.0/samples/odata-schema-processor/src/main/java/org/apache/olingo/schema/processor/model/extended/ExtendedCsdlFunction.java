package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlFunction，支持依赖关系跟踪
 */
public class ExtendedCsdlFunction extends CsdlFunction implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;
    
    /**
     * 构造函数
     */
    public ExtendedCsdlFunction() {
        this.elementId = null;
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlFunction(String elementId) {
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
        return "Function_" + hashCode();
    }
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlFunction setNamespace(String namespace) {
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
    public ExtendedCsdlFunction registerElement() {
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
        return CsdlDependencyNode.DependencyType.FUNCTION;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    public String getElementPropertyName() {
        return null; // Function通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlFunction{name='%s', bound=%s, parameters=%d}", 
                getName(), isBound(), 
                getParameters() != null ? getParameters().size() : 0);
    }
}
