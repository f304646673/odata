package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CSDL实体容器
 * 继承自CsdlEntityContainer，增加依赖跟踪和扩展功能
 */
public class ExtendedCsdlEntityContainer extends CsdlEntityContainer implements ExtendedCsdlElement {
    
    private String namespace;
    
    /**
     * 默认构造函数
     */
    public ExtendedCsdlEntityContainer() {
        super();
    }
    
    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "EntityContainer_" + hashCode();
    }
    
    @Override
    public ExtendedCsdlEntityContainer setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlEntityContainer registerElement() {
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
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE; // EntityContainer 使用通用类型引用
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    @Override
    public String getElementPropertyName() {
        return null; // EntityContainer通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return "ExtendedCsdlEntityContainer{" +
                "name='" + getName() + '\'' +
                ", namespace='" + getNamespace() + '\'' +
                ", entitySetsCount=" + (getEntitySets() != null ? getEntitySets().size() : 0) +
                ", singletonsCount=" + (getSingletons() != null ? getSingletons().size() : 0) +
                ", actionImportsCount=" + (getActionImports() != null ? getActionImports().size() : 0) +
                ", functionImportsCount=" + (getFunctionImports() != null ? getFunctionImports().size() : 0) +
                '}';
    }
}
