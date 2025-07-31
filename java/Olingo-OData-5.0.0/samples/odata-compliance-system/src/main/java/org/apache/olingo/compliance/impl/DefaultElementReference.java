package org.apache.olingo.compliance.impl;

import org.apache.olingo.compliance.api.DependencyTreeManager;

import java.util.Objects;

/**
 * Default implementation of ElementReference.
 */
public class DefaultElementReference implements DependencyTreeManager.ElementReference {
    private final String namespace;
    private final String name;
    private final DependencyTreeManager.ElementType type;
    
    public DefaultElementReference(String namespace, String name, DependencyTreeManager.ElementType type) {
        this.namespace = namespace;
        this.name = name;
        this.type = type;
    }
    
    public DefaultElementReference(String namespace, String name, String typeString) {
        this.namespace = namespace;
        this.name = name;
        this.type = parseElementType(typeString);
    }
    
    private DependencyTreeManager.ElementType parseElementType(String typeString) {
        if (typeString == null) {
            return DependencyTreeManager.ElementType.ENTITY_TYPE; // default
        }
        
        switch (typeString.toLowerCase()) {
            case "entitytype":
                return DependencyTreeManager.ElementType.ENTITY_TYPE;
            case "complextype":
                return DependencyTreeManager.ElementType.COMPLEX_TYPE;
            case "enumtype":
                return DependencyTreeManager.ElementType.ENUM_TYPE;
            case "typedefinition":
                return DependencyTreeManager.ElementType.TYPE_DEFINITION;
            case "entityset":
                return DependencyTreeManager.ElementType.ENTITY_SET;
            case "singleton":
                return DependencyTreeManager.ElementType.SINGLETON;
            case "action":
                return DependencyTreeManager.ElementType.ACTION;
            case "function":
                return DependencyTreeManager.ElementType.FUNCTION;
            case "term":
                return DependencyTreeManager.ElementType.TERM;
            default:
                return DependencyTreeManager.ElementType.ENTITY_TYPE;
        }
    }
    
    @Override
    public String getNamespace() {
        return namespace;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getFullyQualifiedName() {
        return namespace + "." + name;
    }
    
    @Override
    public DependencyTreeManager.ElementType getType() {
        return type;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultElementReference that = (DefaultElementReference) o;
        return Objects.equals(namespace, that.namespace) &&
               Objects.equals(name, that.name) &&
               type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, name, type);
    }
    
    @Override
    public String toString() {
        return String.format("%s.%s (%s)", namespace, name, type);
    }
}
