package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlEntityType，支持依赖关系跟踪
 */
public class ExtendedCsdlEntityType extends CsdlEntityType implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;
    
    /**
     * 构造函数
     */
    public ExtendedCsdlEntityType() {
        this.elementId = null;
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlEntityType(String elementId) {
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
        return "EntityType_" + hashCode();
    }
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlEntityType setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Get the namespace
     */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * 获取元素的完全限定名
     */
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (namespace != null && getName() != null) {
            return new FullQualifiedName(namespace, getName());
        }
        if (getName() != null) {
            // 如果没有namespace，使用默认的namespace或者不返回FullQualifiedName
            return null;
        }
        return null;
    }
    
    /**
     * 获取元素的依赖类型
     */
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.ENTITY_TYPE;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    @Override
    public String getElementPropertyName() {
        return null; // EntityType通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlEntityType{name='%s', properties=%d}", 
                getName(), getProperties() != null ? getProperties().size() : 0);
    }
}
