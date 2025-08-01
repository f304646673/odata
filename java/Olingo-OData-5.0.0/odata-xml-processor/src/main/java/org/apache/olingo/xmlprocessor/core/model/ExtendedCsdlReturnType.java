package org.apache.olingo.xmlprocessor.core.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlReturnType，增加依赖关系追踪功能
 */
public class ExtendedCsdlReturnType extends CsdlReturnType implements ExtendedCsdlElement {

    private final Set<String> dependencies = new HashSet<>();
    private String fullyQualifiedName;
    private String namespace;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlReturnType() {
        super();
    }

    /**
     * 从标准CsdlReturnType创建ExtendedCsdlReturnType
     */
    public static ExtendedCsdlReturnType fromCsdlReturnType(CsdlReturnType source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlReturnType extended = new ExtendedCsdlReturnType();

        // 复制基本属性
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());

        return extended;
    }

    @Override
    public String getElementId() {
        if (getType() != null) {
            return "ReturnType_" + getType();
        }
        return "ReturnType_" + hashCode();
    }

    @Override
    public ExtendedCsdlReturnType setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlReturnType registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }

    /**
     * 获取元素的完全限定名（如果适用）
     */
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (getType() != null && getType().contains(".")) {
            String[] parts = getType().split("\\.");
            if (parts.length >= 2) {
                String ns = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
                String name = parts[parts.length - 1];
                return new FullQualifiedName(ns, name);
            }
        }
        return null;
    }

    /**
     * 获取元素的依赖类型
     */
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
    }

    /**
     * 添加依赖 - 重写接口方法以匹配签名
     * @param namespace 依赖的命名空间
     * @return 是否成功添加
     */
    @Override
    public boolean addDependency(String namespace) {
        if (namespace != null && !namespace.trim().isEmpty()) {
            dependencies.add(namespace);
            return true;
        }
        return false;
    }

    /**
     * 获取所有依赖 - 重命名以避免与接口方法冲突
     * @return 依赖的命名空间集合
     */
    public Set<String> getStringDependencies() {
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
     * 分析并设置依赖关系
     */
    public void analyzeDependencies() {
        dependencies.clear();
        
        // 分析返回类型依赖
        if (getType() != null) {
            String typeNamespace = extractNamespace(getType());
            if (typeNamespace != null) {
                addDependency(typeNamespace);
            }
        }
    }
    
    /**
     * 从类型名中提取namespace
     */
    private String extractNamespace(String typeName) {
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
    
    /**
     * 获取完全限定名
     */
    public String getFullyQualifiedName() {
        if (fullyQualifiedName != null) {
            return fullyQualifiedName;
        }
        return getType();
    }

    @Override
    public String getElementPropertyName() {
        return null; // ReturnType通常不关联特定属性
    }
}
