package org.apache.olingo.schema.processor.model.dependency;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

/**
 * 全局依赖关系管理器
 * 统一管理所有Schema元素的依赖关系，避免分散在各个Extended类中
 */
public class GlobalDependencyManager {
    
    // 单例模式
    private static volatile GlobalDependencyManager instance;
    
    // 全局依赖树
    private final CsdlDependencyTree globalDependencyTree;
    
    // 注册的schema元素映射 - 用于快速查找
    private final ConcurrentMap<String, CsdlDependencyNode> registeredNodes;
    
    // 按类型分组的节点索引
    private final ConcurrentMap<CsdlDependencyNode.DependencyType, Set<CsdlDependencyNode>> nodesByType;
    
    // 按命名空间分组的节点索引
    private final ConcurrentMap<String, Set<CsdlDependencyNode>> nodesByNamespace;
    
    private GlobalDependencyManager() {
        this.globalDependencyTree = new CsdlDependencyTree();
        this.registeredNodes = new ConcurrentHashMap<>();
        this.nodesByType = new ConcurrentHashMap<>();
        this.nodesByNamespace = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取全局依赖管理器实例
     */
    public static GlobalDependencyManager getInstance() {
        if (instance == null) {
            synchronized (GlobalDependencyManager.class) {
                if (instance == null) {
                    instance = new GlobalDependencyManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册Schema元素
     * @param elementId 元素唯一标识（通常是FQN字符串）
     * @param fqn 完全限定名
     * @param dependencyType 依赖类型
     * @param propertyName 属性名称（可为null）
     * @return 注册的依赖节点
     */
    public synchronized CsdlDependencyNode registerElement(String elementId, FullQualifiedName fqn, 
                                                          CsdlDependencyNode.DependencyType dependencyType, String propertyName) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        if (node == null) {
            node = new CsdlDependencyNode(elementId, fqn, dependencyType, propertyName);
            registeredNodes.put(elementId, node);
            globalDependencyTree.addNode(node);
            
            // 更新索引
            updateTypeIndex(dependencyType, node);
            updateNamespaceIndex(fqn, node);
        }
        return node;
    }
    
    /**
     * 获取已注册的元素节点
     * @param elementId 元素ID
     * @return 依赖节点，如果未注册则返回null
     */
    public CsdlDependencyNode getElement(String elementId) {
        return registeredNodes.get(elementId);
    }
    
    /**
     * 添加依赖关系
     * @param sourceElementId 源元素ID
     * @param targetElementId 目标元素ID
     * @return 是否成功添加
     */
    public synchronized boolean addDependency(String sourceElementId, String targetElementId) {
        CsdlDependencyNode sourceNode = registeredNodes.get(sourceElementId);
        CsdlDependencyNode targetNode = registeredNodes.get(targetElementId);
        
        if (sourceNode != null && targetNode != null) {
            globalDependencyTree.addDependency(sourceNode, targetNode);
            return true;
        }
        return false;
    }
    
    /**
     * 添加依赖关系（直接使用节点）
     */
    public synchronized void addDependency(CsdlDependencyNode sourceNode, CsdlDependencyNode targetNode) {
        globalDependencyTree.addDependency(sourceNode, targetNode);
    }
    
    /**
     * 移除依赖关系
     * @param sourceElementId 源元素ID
     * @param targetElementId 目标元素ID
     * @return 是否成功移除
     */
    public synchronized boolean removeDependency(String sourceElementId, String targetElementId) {
        CsdlDependencyNode sourceNode = registeredNodes.get(sourceElementId);
        CsdlDependencyNode targetNode = registeredNodes.get(targetElementId);
        
        if (sourceNode != null && targetNode != null) {
            return globalDependencyTree.removeDependency(sourceNode, targetNode);
        }
        return false;
    }
    
    /**
     * 获取元素的所有直接依赖
     * @param elementId 元素ID
     * @return 直接依赖的节点集合
     */
    public Set<CsdlDependencyNode> getDirectDependencies(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getDependencies() : new HashSet<>();
    }
    
    /**
     * 获取元素的所有直接被依赖者
     * @param elementId 元素ID
     * @return 直接被依赖的节点集合
     */
    public Set<CsdlDependencyNode> getDirectDependents(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getDependents() : new HashSet<>();
    }
    
    /**
     * 递归获取元素的所有依赖（包括间接依赖）
     * @param elementId 元素ID
     * @return 所有依赖的节点集合
     */
    public Set<CsdlDependencyNode> getAllDependencies(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getAllDependencies() : new HashSet<>();
    }
    
    /**
     * 递归获取元素的所有被依赖者（包括间接被依赖者）
     * @param elementId 元素ID
     * @return 所有被依赖的节点集合
     */
    public Set<CsdlDependencyNode> getAllDependents(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getAllDependents() : new HashSet<>();
    }
    
    /**
     * 获取指定类型的所有元素
     * @param dependencyType 依赖类型
     * @return 匹配的节点集合
     */
    public Set<CsdlDependencyNode> getElementsByType(CsdlDependencyNode.DependencyType dependencyType) {
        return nodesByType.getOrDefault(dependencyType, new HashSet<>());
    }
    
    /**
     * 获取指定命名空间的所有元素
     * @param namespace 命名空间
     * @return 匹配的节点集合
     */
    public Set<CsdlDependencyNode> getElementsByNamespace(String namespace) {
        return nodesByNamespace.getOrDefault(namespace, new HashSet<>());
    }
    
    /**
     * 获取从源元素到目标元素的依赖路径
     * @param sourceElementId 源元素ID
     * @param targetElementId 目标元素ID
     * @return 依赖路径，如果不存在则返回null
     */
    public List<CsdlDependencyNode> getDependencyPath(String sourceElementId, String targetElementId) {
        CsdlDependencyNode sourceNode = registeredNodes.get(sourceElementId);
        CsdlDependencyNode targetNode = registeredNodes.get(targetElementId);
        
        if (sourceNode != null && targetNode != null) {
            return sourceNode.getDependencyPath(targetNode);
        }
        return null;
    }
    
    /**
     * 获取所有根节点（没有依赖其他节点的节点）
     * @return 根节点集合
     */
    public Set<CsdlDependencyNode> getRootNodes() {
        return globalDependencyTree.getRootNodes();
    }
    
    /**
     * 获取所有叶子节点（没有被其他节点依赖的节点）
     * @return 叶子节点集合
     */
    public Set<CsdlDependencyNode> getLeafNodes() {
        return globalDependencyTree.getLeafNodes();
    }
    
    /**
     * 获取拓扑排序结果
     * @return 按依赖关系排序的节点列表
     */
    public List<CsdlDependencyNode> getTopologicalOrder() {
        return globalDependencyTree.getTopologicalOrder();
    }
    
    /**
     * 检查是否存在循环依赖
     * @return 如果存在循环依赖则返回true
     */
    public boolean hasCircularDependencies() {
        return globalDependencyTree.hasCircularDependencies();
    }
    
    /**
     * 检查特定元素是否存在循环依赖
     * @param elementId 元素ID
     * @return 如果存在循环依赖则返回true
     */
    public boolean hasCircularDependency(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null && node.hasCircularDependency();
    }
    
    /**
     * 获取全局统计信息
     * @return 统计信息字符串
     */
    public String getStatistics() {
        return globalDependencyTree.getStatistics();
    }
    
    /**
     * 清除所有依赖关系
     */
    public synchronized void clear() {
        globalDependencyTree.clear();
        registeredNodes.clear();
        nodesByType.clear();
        nodesByNamespace.clear();
    }
    
    /**
     * 注销元素
     * @param elementId 元素ID
     * @return 是否成功注销
     */
    public synchronized boolean unregisterElement(String elementId) {
        CsdlDependencyNode node = registeredNodes.remove(elementId);
        if (node != null) {
            globalDependencyTree.removeNode(node);
            
            // 更新索引
            removeFromTypeIndex(node.getDependencyType(), node);
            removeFromNamespaceIndex(node.getFullyQualifiedName(), node);
            return true;
        }
        return false;
    }
    
    /**
     * 获取全局依赖树（只读）
     * @return 全局依赖树
     */
    public CsdlDependencyTree getDependencyTree() {
        return globalDependencyTree;
    }
    
    // === 私有辅助方法 ===
    
    private void updateTypeIndex(CsdlDependencyNode.DependencyType type, CsdlDependencyNode node) {
        nodesByType.computeIfAbsent(type, k -> new HashSet<>()).add(node);
    }
    
    private void updateNamespaceIndex(FullQualifiedName fqn, CsdlDependencyNode node) {
        if (fqn != null && fqn.getNamespace() != null) {
            nodesByNamespace.computeIfAbsent(fqn.getNamespace(), k -> new HashSet<>()).add(node);
        }
    }
    
    private void removeFromTypeIndex(CsdlDependencyNode.DependencyType type, CsdlDependencyNode node) {
        Set<CsdlDependencyNode> nodes = nodesByType.get(type);
        if (nodes != null) {
            nodes.remove(node);
            if (nodes.isEmpty()) {
                nodesByType.remove(type);
            }
        }
    }
    
    private void removeFromNamespaceIndex(FullQualifiedName fqn, CsdlDependencyNode node) {
        if (fqn != null && fqn.getNamespace() != null) {
            Set<CsdlDependencyNode> nodes = nodesByNamespace.get(fqn.getNamespace());
            if (nodes != null) {
                nodes.remove(node);
                if (nodes.isEmpty()) {
                    nodesByNamespace.remove(fqn.getNamespace());
                }
            }
        }
    }
    
    /**
     * 重置单例实例（主要用于测试）
     */
    public static synchronized void resetInstance() {
        instance = null;
    }
}
