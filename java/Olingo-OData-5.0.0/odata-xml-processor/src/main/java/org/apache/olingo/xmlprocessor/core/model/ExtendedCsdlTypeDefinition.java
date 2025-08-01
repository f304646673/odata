package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlTypeDefinition，增加依赖关系追踪功能
 */
public class ExtendedCsdlTypeDefinition extends CsdlTypeDefinition implements ExtendedCsdlElement {
    
    private String fullyQualifiedName;
    private String namespace;
    
    @Override
    public String getElementId() {
        return getFullyQualifiedName();
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
        return CsdlDependencyNode.DependencyType.TYPE_DEFINITION;
    }

    @Override
    public String getElementPropertyName() {
        return null; // TypeDefinitions don't have property dependencies
    }

    @Override
    public ExtendedCsdlElement setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null && this.getName() != null) {
            this.fullyQualifiedName = namespace + "." + this.getName();
        }
        return this;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * 获取完全限定名称
     * @return 完全限定名称
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlTypeDefinition{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", underlyingType='" + getUnderlyingType() + '\'' +
                '}';
    }
}
