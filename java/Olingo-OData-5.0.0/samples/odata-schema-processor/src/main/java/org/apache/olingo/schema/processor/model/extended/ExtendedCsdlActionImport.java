package org.apache.olingo.schema.processor.model.extended;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlActionImport，增加基于全局依赖管理器的树状依赖关系追踪功能
 */
public class ExtendedCsdlActionImport extends CsdlActionImport {
    
    // 使用基类来管理依赖关系
    private final ExtendedCsdlElement extendedElement = new ExtendedCsdlElement() {
        @Override
        protected CsdlDependencyNode.DependencyType getDependencyType() {
            return CsdlDependencyNode.DependencyType.ACTION_REFERENCE;
        }
        
        @Override
        protected String getName() {
            return ExtendedCsdlActionImport.this.getName();
        }
        
        @Override
        public void analyzeDependencies() {
            ExtendedCsdlActionImport.this.analyzeDependencies();
        }
    };
    
    
    // === 委托给基类的依赖跟踪方法 ===
    
    /**
     * 添加依赖关系到树状结构
     * @param targetNamespace 目标命名空间
     * @param targetElement 目标元素
     * @param dependencyType 依赖类型
     * @param propertyName 产生依赖的属性名
     */
    public void addTreeDependency(String targetNamespace, String targetElement, 
                                 CsdlDependencyNode.DependencyType dependencyType, String propertyName) {
        extendedElement.addTreeDependency(targetNamespace, targetElement, dependencyType, propertyName);
    }
    
    /**
     * 移除依赖关系
     * @param targetNamespace 目标命名空间
     * @param targetElement 目标元素
     * @param dependencyType 依赖类型
     * @return 是否成功移除
     */
    public boolean removeTreeDependency(String targetNamespace, String targetElement, 
                                       CsdlDependencyNode.DependencyType dependencyType) {
        return extendedElement.removeTreeDependency(targetNamespace, targetElement, dependencyType);
    }
    
    /**
     * 获取所有直接依赖的节点
     * @return 直接依赖的节点集合
     */
    public Set<CsdlDependencyNode> getDirectDependencies() {
        return extendedElement.getDirectDependencies();
    }
    
    /**
     * 获取所有直接被依赖的节点
     * @return 直接被依赖的节点集合
     */
    public Set<CsdlDependencyNode> getDirectDependents() {
        return extendedElement.getDirectDependents();
    }
    
    /**
     * 递归获取所有依赖的节点（深度优先）
     * @return 所有直接和间接依赖的节点
     */
    public Set<CsdlDependencyNode> getAllTreeDependencies() {
        return extendedElement.getAllTreeDependencies();
    }
    
    /**
     * 递归获取所有被依赖的节点（深度优先）
     * @return 所有直接和间接依赖当前节点的节点
     */
    public Set<CsdlDependencyNode> getAllTreeDependents() {
        return extendedElement.getAllTreeDependents();
    }
    
    /**
     * 获取到特定目标的依赖路径
     * @param targetNamespace 目标命名空间
     * @param targetElement 目标元素
     * @param dependencyType 依赖类型
     * @return 依赖路径，如果不存在则返回null
     */
    public List<CsdlDependencyNode> getDependencyPath(String targetNamespace, String targetElement, 
                                                     CsdlDependencyNode.DependencyType dependencyType) {
        return extendedElement.getDependencyPath(targetNamespace, targetElement, dependencyType);
    }
    
    /**
     * 检查是否存在循环依赖
     * @return 如果存在循环依赖则返回true
     */
    public boolean hasCircularDependency() {
        return extendedElement.hasCircularDependency();
    }
    
    /**
     * 获取当前ActionImport对应的依赖节点
     * @return 依赖节点
     */
    public CsdlDependencyNode getSelfNode() {
        return extendedElement.getSelfNode();
    }
    
    /**
     * 获取指定类型的依赖节点
     * @param dependencyType 依赖类型
     * @return 匹配的依赖节点集合
     */
    public Set<CsdlDependencyNode> getDependenciesByType(CsdlDependencyNode.DependencyType dependencyType) {
        return extendedElement.getDependenciesByType(dependencyType);
    }
    
    /**
     * 获取指定命名空间的依赖节点
     * @param namespace 命名空间
     * @return 匹配的依赖节点集合
     */
    public Set<CsdlDependencyNode> getDependenciesByNamespace(String namespace) {
        return extendedElement.getDependenciesByNamespace(namespace);
    }
    
    /**
     * 清除所有树状依赖信息
     */
    public void clearTreeDependencies() {
        extendedElement.clearTreeDependencies();
    }
    
    // === 委托给基类的简单依赖跟踪方法（向后兼容） ===
    
    /**
     * 添加依赖
     * @param namespace 依赖的命名空间
     */
    public void addDependency(String namespace) {
        extendedElement.addDependency(namespace);
    }
    
    /**
     * 移除依赖
     * @param namespace 要移除的命名空间
     * @return 是否成功移除
     */
    public boolean removeDependency(String namespace) {
        return extendedElement.removeDependency(namespace);
    }
    
    /**
     * 获取所有依赖
     * @return 依赖的命名空间集合
     */
    public Set<String> getDependencies() {
        return extendedElement.getDependencies();
    }
    
    /**
     * 检查是否有特定依赖
     * @param namespace 要检查的命名空间
     * @return 是否存在该依赖
     */
    public boolean hasDependency(String namespace) {
        return extendedElement.hasDependency(namespace);
    }
    
    /**
     * 清除所有依赖
     */
    public void clearDependencies() {
        extendedElement.clearDependencies();
    }
    
    /**
     * 获取依赖数量
     * @return 依赖数量
     */
    public int getDependencyCount() {
        return extendedElement.getDependencyCount();
    }
    
    /**
     * 分析并设置依赖关系（增强版，使用全局依赖管理器）
     */
    public void analyzeDependencies() {
        // 清除旧的依赖
        clearDependencies();
        clearTreeDependencies();
        
        // 初始化自身节点
        getSelfNode();
        
        // 分析Action依赖
        try {
            String actionName = getAction();
            if (actionName != null) {
                String actionNamespace = extendedElement.extractNamespace(actionName);
                String actionElement = extendedElement.extractElementName(actionName);
                if (actionNamespace != null && actionElement != null) {
                    addTreeDependency(actionNamespace, actionElement, 
                                    CsdlDependencyNode.DependencyType.ACTION_REFERENCE, "action");
                }
            }
        } catch (Exception e) {
            // 忽略错误，可能是因为Action未正确设置
        }
        
        // 分析EntitySet依赖
        try {
            String entitySetName = getEntitySet();
            if (entitySetName != null) {
                String entitySetNamespace = extendedElement.extractNamespace(entitySetName);
                String entitySetElement = extendedElement.extractElementName(entitySetName);
                if (entitySetNamespace != null && entitySetElement != null) {
                    addTreeDependency(entitySetNamespace, entitySetElement, 
                                    CsdlDependencyNode.DependencyType.ENTITY_SET, "entitySet");
                }
            }
        } catch (Exception e) {
            // 忽略错误，可能是因为EntitySet未正确设置
        }
    }
    
    public String getFullyQualifiedName() {
        return extendedElement.getFullyQualifiedName();
    }
    
    public void setFullyQualifiedName(String fullyQualifiedName) {
        extendedElement.setFullyQualifiedName(fullyQualifiedName);
    }
    
    @Override
    public ExtendedCsdlActionImport setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlActionImport setAction(String action) {
        super.setAction(action);
        analyzeDependencies();
        return this;
    }
    
    @Override
    public ExtendedCsdlActionImport setEntitySet(String entitySet) {
        super.setEntitySet(entitySet);
        analyzeDependencies();
        return this;
    }
}
