package org.apache.olingo.xmlprocessor.core.dependency;

import java.util.Objects;

/**
 * 表示一个具体的依赖关系
 */
public class DependencyInfo {
    
    private final String sourceElement;
    private final String targetNamespace;
    private final String targetElement;
    private final DependencyType dependencyType;
    private final String propertyName; // 哪个属性产生的依赖
    
    public enum DependencyType {
        TYPE_REFERENCE,      // 类型引用
        BASE_TYPE,          // 基类型
        ENTITY_SET,         // EntitySet引用
        ACTION_REFERENCE,   // Action引用
        FUNCTION_REFERENCE, // Function引用
        NAVIGATION_TARGET,  // 导航目标
        PARAMETER_TYPE,     // 参数类型
        RETURN_TYPE         // 返回类型
    }
    
    public DependencyInfo(String sourceElement, String targetNamespace, 
                         String targetElement, DependencyType dependencyType, 
                         String propertyName) {
        this.sourceElement = sourceElement;
        this.targetNamespace = targetNamespace;
        this.targetElement = targetElement;
        this.dependencyType = dependencyType;
        this.propertyName = propertyName;
    }
    
    public String getSourceElement() {
        return sourceElement;
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    public String getTargetElement() {
        return targetElement;
    }
    
    public DependencyType getDependencyType() {
        return dependencyType;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public String getFullTargetName() {
        return targetNamespace + "." + targetElement;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DependencyInfo that = (DependencyInfo) obj;
        return Objects.equals(sourceElement, that.sourceElement) &&
               Objects.equals(targetNamespace, that.targetNamespace) &&
               Objects.equals(targetElement, that.targetElement) &&
               dependencyType == that.dependencyType &&
               Objects.equals(propertyName, that.propertyName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceElement, targetNamespace, targetElement, dependencyType, propertyName);
    }
    
    @Override
    public String toString() {
        return String.format("%s.%s -[%s:%s]-> %s.%s", 
            extractNamespace(sourceElement), extractElementName(sourceElement),
            dependencyType, propertyName,
            targetNamespace, targetElement);
    }
    
    private String extractNamespace(String fullName) {
        if (fullName == null) return "";
        int lastDot = fullName.lastIndexOf('.');
        return lastDot > 0 ? fullName.substring(0, lastDot) : "";
    }
    
    private String extractElementName(String fullName) {
        if (fullName == null) return "";
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
    }
}
