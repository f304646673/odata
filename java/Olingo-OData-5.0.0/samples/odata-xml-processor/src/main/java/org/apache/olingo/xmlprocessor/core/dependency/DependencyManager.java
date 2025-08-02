package org.apache.olingo.xmlprocessor.core.dependency;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyTree;

/**
 * 依赖关系管理器接口
 * 定义Schema元素依赖关系管理的标准操作
 */
public interface DependencyManager {

    /**
     * 注册Schema元素
     * @param elementId 元素唯一标识（通常是FQN字符串）
     * @param fqn 完全限定名
     * @param dependencyType 依赖类型
     * @param namespace 命名空间
     * @return 注册的依赖节点
     */
    CsdlDependencyNode registerElement(String elementId, FullQualifiedName fqn,
                                       CsdlDependencyNode.DependencyType dependencyType, String namespace);

    /**
     * 获取已注册的元素节点
     * @param elementId 元素ID
     * @return 依赖节点，如果未注册则返回null
     */
    CsdlDependencyNode getElement(String elementId);

    /**
     * 添加依赖关系
     * @param sourceElementId 源元素ID
     * @param targetElementId 目标元素ID
     * @return 是否成功添加
     */
    boolean addDependency(String sourceElementId, String targetElementId);

    /**
     * 添加依赖关系（直接使用节点）
     * @param sourceNode 源节点
     * @param targetNode 目标节点
     */
    void addDependency(CsdlDependencyNode sourceNode, CsdlDependencyNode targetNode);

    /**
     * 移除依赖关系
     * @param sourceElementId 源元素ID
     * @param targetElementId 目标元素ID
     * @return 是否成功移除
     */
    boolean removeDependency(String sourceElementId, String targetElementId);

    /**
     * 获取元素的所有直接依赖
     * @param elementId 元素ID
     * @return 直接依赖的节点集合
     */
    Set<CsdlDependencyNode> getDirectDependencies(String elementId);

    /**
     * 获取元素的所有直接被依赖者
     * @param elementId 元素ID
     * @return 直接被依赖的节点集合
     */
    Set<CsdlDependencyNode> getDirectDependents(String elementId);

    /**
     * 递归获取元素的所有依赖（包括间接依赖）
     * @param elementId 元素ID
     * @return 所有依赖的节点集合
     */
    Set<CsdlDependencyNode> getAllDependencies(String elementId);

    /**
     * 递归获取元素的所有被依赖者（包括间接被依赖者）
     * @param elementId 元素ID
     * @return 所有被依赖的节点集合
     */
    Set<CsdlDependencyNode> getAllDependents(String elementId);

    /**
     * 获取指定类型的所有元素
     * @param dependencyType 依赖类型
     * @return 匹配的节点集合
     */
    Set<CsdlDependencyNode> getElementsByType(CsdlDependencyNode.DependencyType dependencyType);

    /**
     * 获取指定命名空间的所有元素
     * @param namespace 命名空间
     * @return 匹配的节点集合
     */
    Set<CsdlDependencyNode> getElementsByNamespace(String namespace);

    /**
     * 获取从源元素到目标元素的依赖路径
     * @param sourceElementId 源元素ID
     * @param targetElementId 目标元素ID
     * @return 依赖路径，如果不存在则返回null
     */
    List<CsdlDependencyNode> getDependencyPath(String sourceElementId, String targetElementId);

    /**
     * 获取所有根节点（没有依赖其他节点的节点）
     * @return 根节点集合
     */
    Set<CsdlDependencyNode> getRootNodes();

    /**
     * 获取所有叶子节点（没有被其他节点依赖的节点）
     * @return 叶子节点集合
     */
    Set<CsdlDependencyNode> getLeafNodes();

    /**
     * 获取拓扑排序结果
     * @return 按依赖关系排序的节点列表
     */
    List<CsdlDependencyNode> getTopologicalOrder();

    /**
     * 检查是否存在循环依赖
     * @return 如果存在循环依赖则返回true
     */
    boolean hasCircularDependencies();

    /**
     * 检查特定元素是否存在循环依赖
     * @param elementId 元素ID
     * @return 如果存在循环依赖则返回true
     */
    boolean hasCircularDependency(String elementId);

    /**
     * 获取统计信息
     * @return 统计信息字符串
     */
    String getStatistics();

    /**
     * 清除所有依赖关系
     */
    void clear();

    /**
     * 注销元素
     * @param elementId 元素ID
     * @return 是否成功注销
     */
    boolean unregisterElement(String elementId);

    /**
     * 获取依赖树（只读）
     * @return 依赖树
     */
    CsdlDependencyTree getDependencyTree();

    /**
     * 获取所有已注册的元素
     * @return 所有已注册元素的集合
     */
    Set<CsdlDependencyNode> getAllElements();
}
