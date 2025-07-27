package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;

/**
 * Extended CsdlEntityType with dependency tracking capabilities
 * Now implements ExtendedCsdlElement interface directly
 */
public class ExtendedCsdlEntityType extends CsdlEntityType implements ExtendedCsdlElement {
    
    private String namespace;
    
    /**
     * Default constructor
     */
    public ExtendedCsdlEntityType() {
        super();
    }
    
    /**
     * Constructor with name
     * @param name entity type name
     */
    public ExtendedCsdlEntityType(String name) {
        super();
        setName(name);
    }
    
    @Override
    public String getElementId() {
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
    public ExtendedCsdlEntityType registerElement() {
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
        return CsdlDependencyNode.DependencyType.ENTITY_TYPE;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    public String getElementPropertyName() {
        return null; // EntityType通常不关联特定属性
    }
    
    /**
     * Constructor with name and base type
     * @param name entity type name
     * @param baseType base type FQN
     */
    public ExtendedCsdlEntityType(String name, String baseType) {
        super();
        setName(name);
        setBaseType(baseType);
    }
    
    /**
     * Analyze and set dependency relationships
     * Automatically identify and add dependencies based on EntityType properties
     */
    public void analyzeDependencies() {
        // Clear old dependencies
        clearDependencies();
        
        // Analyze BaseType dependency
        if (getBaseType() != null) {
            String baseTypeName = getBaseType();
            String baseTypeNamespace = extractNamespace(baseTypeName);
            if (baseTypeNamespace != null) {
                String baseTypeElement = extractElementName(baseTypeName);
                addDependency("baseType", 
                    new FullQualifiedName(baseTypeNamespace, baseTypeElement),
                    CsdlDependencyNode.DependencyType.ENTITY_TYPE);
            }
        }
        
        // Analyze property type dependencies
        if (getProperties() != null) {
            for (CsdlProperty property : getProperties()) {
                if (property.getType() != null) {
                    String typeName = property.getType();
                    String typeNamespace = extractNamespace(typeName);
                    if (typeNamespace != null) {
                        String typeElement = extractElementName(typeName);
                        CsdlDependencyNode.DependencyType depType = 
                            typeName.contains("ComplexType") ? 
                                CsdlDependencyNode.DependencyType.TYPE_REFERENCE :
                                CsdlDependencyNode.DependencyType.ENTITY_TYPE;
                        addDependency("property:" + property.getName(), 
                            new FullQualifiedName(typeNamespace, typeElement),
                            depType);
                    }
                }
            }
        }
    }
    
    /**
     * Extract namespace from type name
     */
    private String extractNamespace(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return null;
        }
        
        // Handle Collection types
        String actualType = typeName;
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            actualType = typeName.substring(11, typeName.length() - 1);
        }
        
        // Skip EDM basic types
        if (actualType.startsWith("Edm.")) {
            return null;
        }
        
        // Extract namespace
        int lastDotIndex = actualType.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return actualType.substring(0, lastDotIndex);
        }
        
        return null;
    }
    
    /**
     * Extract element name
     * @param fullName fully qualified name
     * @return element name
     */
    private String extractElementName(String fullName) {
        if (fullName == null || !fullName.contains(".")) {
            return fullName;
        }
        int lastDotIndex = fullName.lastIndexOf(".");
        return fullName.substring(lastDotIndex + 1);
    }
}
