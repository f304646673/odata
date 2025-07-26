package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 扩展的CsdlEntityType，增加依赖关系追踪功能
 */
public class ExtendedCsdlEntityType extends CsdlEntityType {
    
    private final Set<String> dependencies = new HashSet<>();
    private String fullyQualifiedName;
    
    /**
     * 获取依赖的类型全限定名集合
     * @return 依赖的类型全限定名集合
     */
    public Set<String> getDependencies() {
        return new HashSet<>(dependencies);
    }
    
    /**
     * 添加依赖
     * @param fullyQualifiedTypeName 依赖的类型全限定名
     */
    public void addDependency(String fullyQualifiedTypeName) {
        if (fullyQualifiedTypeName != null && !fullyQualifiedTypeName.trim().isEmpty()) {
            dependencies.add(fullyQualifiedTypeName);
        }
    }
    
    /**
     * 分析并设置所有依赖
     */
    public void analyzeDependencies() {
        dependencies.clear();
        
        // 分析BaseType依赖
        String baseTypeValue = getBaseType();
        if (baseTypeValue != null) {
            String dependency = extractTypeNamespace(baseTypeValue);
            if (dependency != null) {
                addDependency(dependency);
            }
        }
        
        // 分析Property依赖
        if (getProperties() != null) {
            for (CsdlProperty property : getProperties()) {
                if (property instanceof ExtendedCsdlProperty) {
                    ExtendedCsdlProperty extProperty = (ExtendedCsdlProperty) property;
                    extProperty.analyzeDependencies();
                    dependencies.addAll(extProperty.getDependencies());
                } else {
                    String type = property.getType();
                    if (type != null) {
                        String dependency = extractTypeNamespace(type);
                        if (dependency != null) {
                            addDependency(dependency);
                        }
                    }
                }
            }
        }
        
        // 分析NavigationProperty依赖
        if (getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProperty : getNavigationProperties()) {
                if (navProperty instanceof ExtendedCsdlNavigationProperty) {
                    ExtendedCsdlNavigationProperty extNavProperty = (ExtendedCsdlNavigationProperty) navProperty;
                    extNavProperty.analyzeDependencies();
                    dependencies.addAll(extNavProperty.getDependencies());
                } else {
                    String type = navProperty.getType();
                    if (type != null) {
                        String dependency = extractTypeNamespace(type);
                        if (dependency != null) {
                            addDependency(dependency);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 从类型名中提取namespace
     * @param typeName 类型名
     * @return namespace，如果是基础类型返回null
     */
    private String extractTypeNamespace(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return null;
        }
        
        // 处理Collection类型
        String actualType = typeName;
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            actualType = typeName.substring(11, typeName.length() - 1);
        }
        
        // 跳过EDM基础类型
        if (actualType.startsWith("Edm.")) {
            return null;
        }
        
        // 提取namespace
        int lastDotIndex = actualType.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return actualType.substring(0, lastDotIndex);
        }
        
        return null;
    }
    
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    
    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }
    
    @Override
    public ExtendedCsdlEntityType setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlEntityType setBaseType(String baseType) {
        super.setBaseType(baseType);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlEntityType setProperties(List<CsdlProperty> properties) {
        super.setProperties(properties);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlEntityType setNavigationProperties(List<CsdlNavigationProperty> navigationProperties) {
        super.setNavigationProperties(navigationProperties);
        analyzeDependencies();
        return this;
    }
    
    /**
     * 添加扩展属性
     * @param property 扩展属性
     * @return this
     */
    public ExtendedCsdlEntityType addProperty(ExtendedCsdlProperty property) {
        if (getProperties() == null) {
            setProperties(new ArrayList<>());
        }
        getProperties().add(property);
        analyzeDependencies();
        return this;
    }
    
    /**
     * 添加扩展导航属性
     * @param navigationProperty 扩展导航属性
     * @return this
     */
    public ExtendedCsdlEntityType addNavigationProperty(ExtendedCsdlNavigationProperty navigationProperty) {
        if (getNavigationProperties() == null) {
            setNavigationProperties(new ArrayList<>());
        }
        getNavigationProperties().add(navigationProperty);
        analyzeDependencies();
        return this;
    }
}
