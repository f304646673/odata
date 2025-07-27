package org.apache.olingo.schema.processor.model.extended;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import org.apache.olingo.schema.processor.model.dependency.GlobalDependencyManager;

/**
 * Extended CSDL元素的基类
 * 提供统一的依赖关系管理功能
 */
public abstract class ExtendedCsdlElement {
    
    // 保留旧的简单依赖跟踪（向后兼容）
    private final Set<String> dependencies = new HashSet<>();
    
    // 元素的完全限定名
    private String fullyQualifiedName;
    
    // 在全局依赖管理器中的节点引用
    private CsdlDependencyNode selfNode;
    
    /**
     * 获取元素类型，子类必须实现
     */
    protected abstract CsdlDependencyNode.DependencyType getDependencyType();
    
    /**
     * 获取元素的唯一标识，默认使用FQN，子类可以重写
     */
    protected String getElementId() {
        return fullyQualifiedName != null ? fullyQualifiedName : getName();
    }
    
    /**
     * 获取元素名称，子类必须实现
     */
    protected abstract String getName();
    
    /**
     * 初始化当前元素在全局依赖管理器中的节点
     */
    protected void initializeSelfNode() {
        if (selfNode == null && getElementId() != null) {
            GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
            FullQualifiedName fqn = createFullQualifiedName(getElementId());
            selfNode = manager.registerElement(getElementId(), fqn, getDependencyType(), null);
        }
    }
    
    /**
     * 添加依赖关系到全局依赖树
     * @param targetNamespace 目标命名空间
     * @param targetElement 目标元素
     * @param dependencyType 依赖类型
     * @param propertyName 产生依赖的属性名
     */
    public void addTreeDependency(String targetNamespace, String targetElement, 
                                 CsdlDependencyNode.DependencyType dependencyType, String propertyName) {
        // 验证输入参数
        if (targetNamespace == null || targetNamespace.trim().isEmpty() ||
            targetElement == null || targetElement.trim().isEmpty()) {
            return; // 忽略无效输入
        }
        
        initializeSelfNode();
        
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        FullQualifiedName targetFqn = new FullQualifiedName(targetNamespace, targetElement);
        String targetId = targetFqn.getFullQualifiedNameAsString();
        
        // 注册或获取目标节点
        CsdlDependencyNode targetNode = manager.registerElement(targetId, targetFqn, dependencyType, propertyName);
        
        // 建立依赖关系
        manager.addDependency(selfNode, targetNode);
        
        // 同时更新简单依赖（向后兼容）
        addDependency(targetNamespace);
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
        if (selfNode == null) {
            return false;
        }
        
        FullQualifiedName targetFqn = new FullQualifiedName(targetNamespace, targetElement);
        String targetId = targetFqn.getFullQualifiedNameAsString();
        
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        return manager.removeDependency(getElementId(), targetId);
    }
    
    /**
     * 获取所有直接依赖的节点
     * @return 直接依赖的节点集合
     */
    public Set<CsdlDependencyNode> getDirectDependencies() {
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        return manager.getDirectDependencies(getElementId());
    }
    
    /**
     * 获取所有直接被依赖的节点
     * @return 直接被依赖的节点集合
     */
    public Set<CsdlDependencyNode> getDirectDependents() {
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        return manager.getDirectDependents(getElementId());
    }
    
    /**
     * 递归获取所有依赖的节点（深度优先）
     * @return 所有直接和间接依赖的节点
     */
    public Set<CsdlDependencyNode> getAllTreeDependencies() {
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        return manager.getAllDependencies(getElementId());
    }
    
    /**
     * 递归获取所有被依赖的节点（深度优先）
     * @return 所有直接和间接依赖当前节点的节点
     */
    public Set<CsdlDependencyNode> getAllTreeDependents() {
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        return manager.getAllDependents(getElementId());
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
        FullQualifiedName targetFqn = new FullQualifiedName(targetNamespace, targetElement);
        String targetId = targetFqn.getFullQualifiedNameAsString();
        
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        return manager.getDependencyPath(getElementId(), targetId);
    }
    
    /**
     * 检查是否存在循环依赖
     * @return 如果存在循环依赖则返回true
     */
    public boolean hasCircularDependency() {
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        return manager.hasCircularDependency(getElementId());
    }
    
    /**
     * 获取当前元素对应的依赖节点
     * @return 依赖节点
     */
    public CsdlDependencyNode getSelfNode() {
        initializeSelfNode();
        return selfNode;
    }
    
    /**
     * 获取指定类型的依赖节点
     * @param dependencyType 依赖类型
     * @return 匹配的依赖节点集合
     */
    public Set<CsdlDependencyNode> getDependenciesByType(CsdlDependencyNode.DependencyType dependencyType) {
        Set<CsdlDependencyNode> result = new HashSet<>();
        for (CsdlDependencyNode node : getDirectDependencies()) {
            if (node.getDependencyType() == dependencyType) {
                result.add(node);
            }
        }
        return result;
    }
    
    /**
     * 获取指定命名空间的依赖节点
     * @param namespace 命名空间
     * @return 匹配的依赖节点集合
     */
    public Set<CsdlDependencyNode> getDependenciesByNamespace(String namespace) {
        Set<CsdlDependencyNode> result = new HashSet<>();
        for (CsdlDependencyNode node : getDirectDependencies()) {
            if (node.getFullyQualifiedName() != null && 
                namespace.equals(node.getFullyQualifiedName().getNamespace())) {
                result.add(node);
            }
        }
        return result;
    }
    
    /**
     * 清除所有树状依赖信息
     */
    public void clearTreeDependencies() {
        if (selfNode != null) {
            GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
            manager.unregisterElement(getElementId());
            selfNode = null;
        }
    }
    
    /**
     * 创建FullQualifiedName的辅助方法
     */
    protected FullQualifiedName createFullQualifiedName(String fullName) {
        if (fullName == null || !fullName.contains(".")) {
            return new FullQualifiedName(null, fullName);
        }
        int lastDotIndex = fullName.lastIndexOf(".");
        String namespace = fullName.substring(0, lastDotIndex);
        String name = fullName.substring(lastDotIndex + 1);
        return new FullQualifiedName(namespace, name);
    }
    
    // === 原有的简单依赖跟踪方法（向后兼容） ===
    
    /**
     * 添加依赖
     * @param namespace 依赖的命名空间
     */
    public void addDependency(String namespace) {
        if (namespace != null && !namespace.trim().isEmpty()) {
            dependencies.add(namespace);
        }
    }
    
    /**
     * 移除依赖
     * @param namespace 要移除的命名空间
     * @return 是否成功移除
     */
    public boolean removeDependency(String namespace) {
        return dependencies.remove(namespace);
    }
    
    /**
     * 获取所有依赖
     * @return 依赖的命名空间集合
     */
    public Set<String> getDependencies() {
        return new HashSet<>(dependencies);
    }
    
    /**
     * 检查是否有特定依赖
     * @param namespace 要检查的命名空间
     * @return 是否存在该依赖
     */
    public boolean hasDependency(String namespace) {
        return dependencies.contains(namespace);
    }
    
    /**
     * 清除所有依赖
     */
    public void clearDependencies() {
        dependencies.clear();
    }
    
    /**
     * 获取依赖数量
     * @return 依赖数量
     */
    public int getDependencyCount() {
        return dependencies.size();
    }
    
    /**
     * 分析并设置依赖关系，子类应该重写此方法
     */
    public abstract void analyzeDependencies();
    
    /**
     * 提取元素名称
     * @param fullName 完全限定名
     * @return 元素名称
     */
    protected String extractElementName(String fullName) {
        if (fullName == null || !fullName.contains(".")) {
            return fullName;
        }
        int lastDotIndex = fullName.lastIndexOf(".");
        return fullName.substring(lastDotIndex + 1);
    }
    
    /**
     * 从类型名中提取namespace
     */
    protected String extractNamespace(String typeName) {
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
        // 重新初始化节点
        selfNode = null;
        initializeSelfNode();
    }
}
