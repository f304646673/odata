package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlEntityType，增加基于全局依赖管理器的树状依赖关系追踪功能
 */
public class ExtendedCsdlEntityType extends CsdlEntityType {
    
    // 使用基类来管理依赖关系
    private final ExtendedCsdlElement extendedElement = new ExtendedCsdlElement() {
        @Override
        protected CsdlDependencyNode.DependencyType getDependencyType() {
            return CsdlDependencyNode.DependencyType.ENTITY_TYPE;
        }
        
        @Override
        protected String getName() {
            return ExtendedCsdlEntityType.this.getName();
        }
        
        @Override
        public void analyzeDependencies() {
            ExtendedCsdlEntityType.this.analyzeDependencies();
        }
    };
    
    // === 委托给基类的依赖跟踪方法 ===
    
    public void addTreeDependency(String targetNamespace, String targetElement, 
                                 CsdlDependencyNode.DependencyType dependencyType, String propertyName) {
        extendedElement.addTreeDependency(targetNamespace, targetElement, dependencyType, propertyName);
    }
    
    public boolean removeTreeDependency(String targetNamespace, String targetElement, 
                                       CsdlDependencyNode.DependencyType dependencyType) {
        return extendedElement.removeTreeDependency(targetNamespace, targetElement, dependencyType);
    }
    
    public java.util.Set<CsdlDependencyNode> getDirectDependencies() {
        return extendedElement.getDirectDependencies();
    }
    
    public java.util.Set<CsdlDependencyNode> getDirectDependents() {
        return extendedElement.getDirectDependents();
    }
    
    public java.util.Set<CsdlDependencyNode> getAllTreeDependencies() {
        return extendedElement.getAllTreeDependencies();
    }
    
    public java.util.Set<CsdlDependencyNode> getAllTreeDependents() {
        return extendedElement.getAllTreeDependents();
    }
    
    public java.util.List<CsdlDependencyNode> getDependencyPath(String targetNamespace, String targetElement, 
                                                               CsdlDependencyNode.DependencyType dependencyType) {
        return extendedElement.getDependencyPath(targetNamespace, targetElement, dependencyType);
    }
    
    public boolean hasCircularDependency() {
        return extendedElement.hasCircularDependency();
    }
    
    public CsdlDependencyNode getSelfNode() {
        return extendedElement.getSelfNode();
    }
    
    public java.util.Set<CsdlDependencyNode> getDependenciesByType(CsdlDependencyNode.DependencyType dependencyType) {
        return extendedElement.getDependenciesByType(dependencyType);
    }
    
    public java.util.Set<CsdlDependencyNode> getDependenciesByNamespace(String namespace) {
        return extendedElement.getDependenciesByNamespace(namespace);
    }
    
    public void clearTreeDependencies() {
        extendedElement.clearTreeDependencies();
    }
    
    // === 简单依赖跟踪方法（向后兼容） ===
    
    public void addDependency(String namespace) {
        extendedElement.addDependency(namespace);
    }
    
    public boolean removeDependency(String namespace) {
        return extendedElement.removeDependency(namespace);
    }
    
    public java.util.Set<String> getDependencies() {
        return extendedElement.getDependencies();
    }
    
    public boolean hasDependency(String namespace) {
        return extendedElement.hasDependency(namespace);
    }
    
    public void clearDependencies() {
        extendedElement.clearDependencies();
    }
    
    public int getDependencyCount() {
        return extendedElement.getDependencyCount();
    }
    
    /**
     * 分析并设置依赖关系
     */
    public void analyzeDependencies() {
        // 清除旧的依赖
        clearDependencies();
        clearTreeDependencies();
        
        // 初始化自身节点
        getSelfNode();
        
        // 分析BaseType依赖
        try {
            String baseType = getBaseType();
            if (baseType != null) {
                String baseTypeNamespace = extendedElement.extractNamespace(baseType);
                String baseTypeElement = extendedElement.extractElementName(baseType);
                if (baseTypeNamespace != null && baseTypeElement != null) {
                    addTreeDependency(baseTypeNamespace, baseTypeElement, 
                                    CsdlDependencyNode.DependencyType.ENTITY_TYPE, "baseType");
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        
        // 分析属性类型依赖
        try {
            if (getProperties() != null) {
                for (CsdlProperty property : getProperties()) {
                    String propertyType = property.getType();
                    if (propertyType != null) {
                        String propertyTypeNamespace = extendedElement.extractNamespace(propertyType);
                        String propertyTypeElement = extendedElement.extractElementName(propertyType);
                        if (propertyTypeNamespace != null && propertyTypeElement != null) {
                            // 根据类型判断依赖类型
                            CsdlDependencyNode.DependencyType depType = 
                                propertyType.contains("EntityType") ? CsdlDependencyNode.DependencyType.ENTITY_TYPE :
                                propertyType.contains("ComplexType") ? CsdlDependencyNode.DependencyType.COMPLEX_TYPE :
                                CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
                            addTreeDependency(propertyTypeNamespace, propertyTypeElement, 
                                            depType, "property." + property.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        
        // 分析导航属性依赖
        try {
            if (getNavigationProperties() != null) {
                for (org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty navProp : getNavigationProperties()) {
                    String navPropType = navProp.getType();
                    if (navPropType != null) {
                        String navPropTypeNamespace = extendedElement.extractNamespace(navPropType);
                        String navPropTypeElement = extendedElement.extractElementName(navPropType);
                        if (navPropTypeNamespace != null && navPropTypeElement != null) {
                            addTreeDependency(navPropTypeNamespace, navPropTypeElement, 
                                            CsdlDependencyNode.DependencyType.ENTITY_TYPE, "navigationProperty." + navProp.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    public String getFullyQualifiedName() {
        return extendedElement.getFullyQualifiedName();
    }
    
    public void setFullyQualifiedName(String fullyQualifiedName) {
        extendedElement.setFullyQualifiedName(fullyQualifiedName);
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
    public ExtendedCsdlEntityType setProperties(java.util.List<CsdlProperty> properties) {
        super.setProperties(properties);
        analyzeDependencies();
        return this;
    }
}
