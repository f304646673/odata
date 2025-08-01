package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlActionImport，支持依赖关系跟踪
 */
public class ExtendedCsdlActionImport extends CsdlActionImport implements ExtendedCsdlElement {
    
    private final String elementId;
    
    /**
     * 构造函数
     */
    public ExtendedCsdlActionImport() {
        this.elementId = null;
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlActionImport(String elementId) {
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
        return "ActionImport_" + hashCode();
    }
    
    /**
     * 获取元素的完全限定名（如果适用）
     */
    public FullQualifiedName getElementFullyQualifiedName() {
        // ActionImport通常在容器级别，不是完全限定名
        return new FullQualifiedName(null, getName());
    }
    
    /**
     * 获取元素的依赖类型
     */
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.ACTION_IMPORT;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    public String getElementPropertyName() {
        return null; // ActionImport通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlActionImport{name='%s', action='%s'}", 
                getName(), getAction());
    }
}
