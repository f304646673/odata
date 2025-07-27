package org.apache.olingo.schema.processor.model.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

/**
 * 依赖树管理器，用于管理Schema Elements之间的依赖关系树状结构
 */
public class CsdlDependencyTree {
    
    // 所有节点的索引，按照FullQualifiedName分组
    private final Map<FullQualifiedName, Set<CsdlDependencyNode>> nodeRegistry = new HashMap<>();
    
    // 根节点集合（没有被其他节点依赖的节点）
    private final Set<CsdlDependencyNode> rootNodes = new HashSet<>();
    
    // 叶子节点集合（不依赖其他节点的节点）
    private final Set<CsdlDependencyNode> leafNodes = new HashSet<>();
    
    /**
     * 添加节点到树中
     * @param node 要添加的节点
     */
    public void addNode(CsdlDependencyNode node) {
        if (node == null || node.getFullyQualifiedName() == null) {
            return;
        }
        
        FullQualifiedName fqn = node.getFullyQualifiedName();
        nodeRegistry.computeIfAbsent(fqn, k -> new HashSet<>()).add(node);
        
        updateRootAndLeafNodes();
    }
    
    /**
     * 移除节点
     * @param node 要移除的节点
     * @return 是否成功移除
     */
    public boolean removeNode(CsdlDependencyNode node) {
        if (node == null || node.getFullyQualifiedName() == null) {
            return false;
        }
        
        FullQualifiedName fqn = node.getFullyQualifiedName();
        Set<CsdlDependencyNode> nodes = nodeRegistry.get(fqn);
        if (nodes == null) {
            return false;
        }
        
        boolean removed = nodes.remove(node);
        if (removed) {
            // 移除与其他节点的依赖关系
            for (CsdlDependencyNode dependency : node.getDependencies()) {
                node.removeDependency(dependency);
            }
            for (CsdlDependencyNode dependent : node.getDependents()) {
                dependent.removeDependency(node);
            }
            
            // 如果该FullQualifiedName下没有更多节点，移除整个条目
            if (nodes.isEmpty()) {
                nodeRegistry.remove(fqn);
            }
            
            updateRootAndLeafNodes();
        }
        
        return removed;
    }
    
    /**
     * 根据FullQualifiedName查找节点
     * @param fqn 完全限定名
     * @return 匹配的节点集合
     */
    public Set<CsdlDependencyNode> findNodes(FullQualifiedName fqn) {
        Set<CsdlDependencyNode> nodes = nodeRegistry.get(fqn);
        return nodes != null ? new HashSet<>(nodes) : new HashSet<>();
    }
    
    /**
     * 根据FullQualifiedName和依赖类型查找节点
     * @param fqn 完全限定名
     * @param dependencyType 依赖类型
     * @return 匹配的节点，如果没有找到则返回null
     */
    public CsdlDependencyNode findNode(FullQualifiedName fqn, CsdlDependencyNode.DependencyType dependencyType) {
        Set<CsdlDependencyNode> nodes = findNodes(fqn);
        for (CsdlDependencyNode node : nodes) {
            if (node.getDependencyType() == dependencyType) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * 建立依赖关系
     * @param sourceNode 源节点
     * @param targetNode 目标节点
     */
    public void addDependency(CsdlDependencyNode sourceNode, CsdlDependencyNode targetNode) {
        if (sourceNode != null && targetNode != null) {
            sourceNode.addDependency(targetNode);
            updateRootAndLeafNodes();
        }
    }
    
    /**
     * 移除依赖关系
     * @param sourceNode 源节点
     * @param targetNode 目标节点
     * @return 是否成功移除
     */
    public boolean removeDependency(CsdlDependencyNode sourceNode, CsdlDependencyNode targetNode) {
        if (sourceNode != null && targetNode != null) {
            boolean removed = sourceNode.removeDependency(targetNode);
            if (removed) {
                updateRootAndLeafNodes();
            }
            return removed;
        }
        return false;
    }
    
    /**
     * 获取所有根节点
     * @return 根节点集合
     */
    public Set<CsdlDependencyNode> getRootNodes() {
        return new HashSet<>(rootNodes);
    }
    
    /**
     * 获取所有叶子节点
     * @return 叶子节点集合
     */
    public Set<CsdlDependencyNode> getLeafNodes() {
        return new HashSet<>(leafNodes);
    }
    
    /**
     * 获取所有节点
     * @return 所有节点的集合
     */
    public Set<CsdlDependencyNode> getAllNodes() {
        Set<CsdlDependencyNode> allNodes = new HashSet<>();
        for (Set<CsdlDependencyNode> nodes : nodeRegistry.values()) {
            allNodes.addAll(nodes);
        }
        return allNodes;
    }
    
    /**
     * 获取拓扑排序结果
     * @return 按依赖关系排序的节点列表（被依赖的节点在前）
     */
    public List<CsdlDependencyNode> getTopologicalOrder() {
        List<CsdlDependencyNode> result = new ArrayList<>();
        Set<CsdlDependencyNode> visited = new HashSet<>();
        Set<CsdlDependencyNode> visiting = new HashSet<>();
        
        for (CsdlDependencyNode node : getAllNodes()) {
            if (!visited.contains(node)) {
                if (!topologicalSortHelper(node, visited, visiting, result)) {
                    // 存在循环依赖，返回空列表
                    return new ArrayList<>();
                }
            }
        }
        
        return result;
    }
    
    /**
     * 拓扑排序的递归实现
     */
    private boolean topologicalSortHelper(CsdlDependencyNode node, 
                                        Set<CsdlDependencyNode> visited,
                                        Set<CsdlDependencyNode> visiting,
                                        List<CsdlDependencyNode> result) {
        if (visiting.contains(node)) {
            return false; // 发现循环依赖
        }
        
        if (visited.contains(node)) {
            return true; // 已经处理过
        }
        
        visiting.add(node);
        
        // 先处理依赖的节点
        for (CsdlDependencyNode dependency : node.getDependencies()) {
            if (!topologicalSortHelper(dependency, visited, visiting, result)) {
                return false;
            }
        }
        
        visiting.remove(node);
        visited.add(node);
        result.add(node);
        
        return true;
    }
    
    /**
     * 检查整个树是否存在循环依赖
     * @return 如果存在循环依赖则返回true
     */
    public boolean hasCircularDependencies() {
        for (CsdlDependencyNode node : getAllNodes()) {
            if (node.hasCircularDependency()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 查找从源节点到目标节点的所有路径
     * @param source 源节点
     * @param target 目标节点
     * @return 所有可能的路径
     */
    public List<List<CsdlDependencyNode>> findAllPaths(CsdlDependencyNode source, CsdlDependencyNode target) {
        List<List<CsdlDependencyNode>> allPaths = new ArrayList<>();
        List<CsdlDependencyNode> currentPath = new ArrayList<>();
        Set<CsdlDependencyNode> visited = new HashSet<>();
        
        findAllPathsHelper(source, target, currentPath, visited, allPaths);
        return allPaths;
    }
    
    /**
     * 查找所有路径的递归实现
     */
    private void findAllPathsHelper(CsdlDependencyNode current, CsdlDependencyNode target,
                                  List<CsdlDependencyNode> currentPath, Set<CsdlDependencyNode> visited,
                                  List<List<CsdlDependencyNode>> allPaths) {
        if (visited.contains(current)) {
            return; // 避免循环
        }
        
        visited.add(current);
        currentPath.add(current);
        
        if (current.equals(target)) {
            // 找到目标，保存路径
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            // 继续搜索
            for (CsdlDependencyNode dependency : current.getDependencies()) {
                findAllPathsHelper(dependency, target, currentPath, visited, allPaths);
            }
        }
        
        // 回溯
        currentPath.remove(currentPath.size() - 1);
        visited.remove(current);
    }
    
    /**
     * 更新根节点和叶子节点集合
     */
    private void updateRootAndLeafNodes() {
        rootNodes.clear();
        leafNodes.clear();
        
        for (CsdlDependencyNode node : getAllNodes()) {
            // 根节点：没有依赖其他节点的节点
            if (node.getDependencies().isEmpty()) {
                rootNodes.add(node);
            }
            // 叶子节点：没有被其他节点依赖的节点
            if (node.getDependents().isEmpty()) {
                leafNodes.add(node);
            }
        }
    }
    
    /**
     * 清空整个树
     */
    public void clear() {
        // 清除所有依赖关系
        for (CsdlDependencyNode node : getAllNodes()) {
            for (CsdlDependencyNode dependency : new HashSet<>(node.getDependencies())) {
                node.removeDependency(dependency);
            }
        }
        
        nodeRegistry.clear();
        rootNodes.clear();
        leafNodes.clear();
    }
    
    /**
     * 获取树的统计信息
     * @return 统计信息字符串
     */
    public String getStatistics() {
        int totalNodes = getAllNodes().size();
        int totalDependencies = 0;
        for (CsdlDependencyNode node : getAllNodes()) {
            totalDependencies += node.getDependencies().size();
        }
        
        return String.format("DependencyTree Statistics: %d nodes, %d dependencies, %d roots, %d leaves, circular=%s",
            totalNodes, totalDependencies, rootNodes.size(), leafNodes.size(), hasCircularDependencies());
    }
}
