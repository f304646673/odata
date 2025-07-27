package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlParameter，支持依赖关系跟踪
 */
public class ExtendedCsdlParameter extends CsdlParameter implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;
    private String parentName;
    
    /**
     * 构造函数
     */
    public ExtendedCsdlParameter() {
        this.elementId = null;
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlParameter(String elementId) {
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
        return "Parameter_" + hashCode();
    }
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlParameter setNamespace(String namespace) {
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
     * Set parent name
     */
    public ExtendedCsdlParameter setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    /**
     * Get parent name
     */
    public String getParentName() {
        return this.parentName;
    }
    
    /**
     * Override registerElement to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlParameter registerElement() {
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
        return CsdlDependencyNode.DependencyType.PARAMETER;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    public String getElementPropertyName() {
        return getName(); // Parameter本身就是属性
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlParameter{name='%s', type='%s', nullable=%s}", 
                getName(), getType(), isNullable());
    }
}
