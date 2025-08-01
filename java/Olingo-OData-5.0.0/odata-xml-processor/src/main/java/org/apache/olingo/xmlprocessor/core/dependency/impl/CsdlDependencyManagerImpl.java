package org.apache.olingo.xmlprocessor.core.dependency.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyTree;
import org.apache.olingo.xmlprocessor.core.dependency.DependencyManager;

/**
 * CSDL依赖关系管理器实现
 * 管理Schema元素的依赖关系，支持多实例使用
 */
public class CsdlDependencyManagerImpl implements DependencyManager {

    // 依赖树
    private final CsdlDependencyTree dependencyTree;

    // 注册的schema元素映射 - 用于快速查找
    private final ConcurrentMap<String, CsdlDependencyNode> registeredNodes;
    
    // 按类型分组的节点索引
    private final ConcurrentMap<CsdlDependencyNode.DependencyType, Set<CsdlDependencyNode>> nodesByType;
    
    // 按命名空间分组的节点索引
    private final ConcurrentMap<String, Set<CsdlDependencyNode>> nodesByNamespace;
    
    /**
     * 构造函数
     */
    public CsdlDependencyManagerImpl() {
        this.dependencyTree = new CsdlDependencyTree();
        this.registeredNodes = new ConcurrentHashMap<>();
        this.nodesByType = new ConcurrentHashMap<>();
        this.nodesByNamespace = new ConcurrentHashMap<>();
    }
    
    @Override
    public synchronized CsdlDependencyNode registerElement(String elementId, FullQualifiedName fqn,
                                                          CsdlDependencyNode.DependencyType dependencyType, String namespace) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        if (node == null) {
            node = new CsdlDependencyNode(elementId, fqn, dependencyType, namespace, null);
            registeredNodes.put(elementId, node);
            dependencyTree.addNode(node);

            // 更新索引
            updateTypeIndex(dependencyType, node);
            updateNamespaceIndex(fqn, node);
        }
        return node;
    }
    
    @Override
    public CsdlDependencyNode getElement(String elementId) {
        return registeredNodes.get(elementId);
    }
    
    @Override
    public synchronized boolean addDependency(String sourceElementId, String targetElementId) {
        CsdlDependencyNode sourceNode = registeredNodes.get(sourceElementId);
        CsdlDependencyNode targetNode = registeredNodes.get(targetElementId);
        
        if (sourceNode != null && targetNode != null) {
            dependencyTree.addDependency(sourceNode, targetNode);
            return true;
        }
        return false;
    }
    
    @Override
    public synchronized void addDependency(CsdlDependencyNode sourceNode, CsdlDependencyNode targetNode) {
        dependencyTree.addDependency(sourceNode, targetNode);
    }
    
    @Override
    public synchronized boolean removeDependency(String sourceElementId, String targetElementId) {
        CsdlDependencyNode sourceNode = registeredNodes.get(sourceElementId);
        CsdlDependencyNode targetNode = registeredNodes.get(targetElementId);
        
        if (sourceNode != null && targetNode != null) {
            return dependencyTree.removeDependency(sourceNode, targetNode);
        }
        return false;
    }
    
    @Override
    public Set<CsdlDependencyNode> getDirectDependencies(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getDependencies() : new HashSet<>();
    }
    
    @Override
    public Set<CsdlDependencyNode> getDirectDependents(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getDependents() : new HashSet<>();
    }
    
    @Override
    public Set<CsdlDependencyNode> getAllDependencies(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getAllDependencies() : new HashSet<>();
    }
    
    @Override
    public Set<CsdlDependencyNode> getAllDependents(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null ? node.getAllDependents() : new HashSet<>();
    }
    
    @Override
    public Set<CsdlDependencyNode> getElementsByType(CsdlDependencyNode.DependencyType dependencyType) {
        return nodesByType.getOrDefault(dependencyType, new HashSet<>());
    }
    
    @Override
    public Set<CsdlDependencyNode> getElementsByNamespace(String namespace) {
        return nodesByNamespace.getOrDefault(namespace, new HashSet<>());
    }
    
    @Override
    public List<CsdlDependencyNode> getDependencyPath(String sourceElementId, String targetElementId) {
        CsdlDependencyNode sourceNode = registeredNodes.get(sourceElementId);
        CsdlDependencyNode targetNode = registeredNodes.get(targetElementId);
        
        if (sourceNode != null && targetNode != null) {
            return sourceNode.getDependencyPath(targetNode);
        }
        return null;
    }
    
    @Override
    public Set<CsdlDependencyNode> getRootNodes() {
        return dependencyTree.getRootNodes();
    }
    
    @Override
    public Set<CsdlDependencyNode> getLeafNodes() {
        return dependencyTree.getLeafNodes();
    }
    
    @Override
    public List<CsdlDependencyNode> getTopologicalOrder() {
        return dependencyTree.getTopologicalOrder();
    }
    
    @Override
    public boolean hasCircularDependencies() {
        return dependencyTree.hasCircularDependencies();
    }
    
    @Override
    public boolean hasCircularDependency(String elementId) {
        CsdlDependencyNode node = registeredNodes.get(elementId);
        return node != null && node.hasCircularDependency();
    }
    
    @Override
    public String getStatistics() {
        return dependencyTree.getStatistics();
    }
    
    @Override
    public synchronized void clear() {
        dependencyTree.clear();
        registeredNodes.clear();
        nodesByType.clear();
        nodesByNamespace.clear();
    }
    
    @Override
    public synchronized boolean unregisterElement(String elementId) {
        CsdlDependencyNode node = registeredNodes.remove(elementId);
        if (node != null) {
            dependencyTree.removeNode(node);

            // 更新索引
            removeFromTypeIndex(node.getDependencyType(), node);
            removeFromNamespaceIndex(node.getFullyQualifiedName(), node);
            return true;
        }
        return false;
    }
    
    @Override
    public CsdlDependencyTree getDependencyTree() {
        return dependencyTree;
    }
    
    @Override
    public Set<CsdlDependencyNode> getAllElements() {
        return new HashSet<>(registeredNodes.values());
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
}
