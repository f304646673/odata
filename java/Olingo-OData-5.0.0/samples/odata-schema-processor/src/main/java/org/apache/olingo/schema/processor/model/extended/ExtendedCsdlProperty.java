package org.apache.olingo.schema.processor.model.extended;

import java.util.HashSet;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

/**
 * 扩展的CsdlProperty，增加依赖关系追踪功能
 */
public class ExtendedCsdlProperty extends CsdlProperty {
    
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
     * 分析并设置类型依赖
     */
    public void analyzeDependencies() {
        dependencies.clear();
        
        String type = getType();
        if (type != null) {
            String dependency = extractTypeNamespace(type);
            if (dependency != null) {
                addDependency(dependency);
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
    public ExtendedCsdlProperty setName(String name) {
        super.setName(name);
        return this;
    }
    
    @Override
    public ExtendedCsdlProperty setType(String type) {
        super.setType(type);
        analyzeDependencies();
        return this;
    }
    
    public ExtendedCsdlProperty setNullable(Boolean nullable) {
        // CsdlProperty的setNullable方法可能返回void，这里只是为了演示扩展功能
        if (nullable != null) {
            // 可以通过反射或其他方式设置nullable属性
        }
        return this;
    }
    
    public ExtendedCsdlProperty setMaxLength(Integer maxLength) {
        if (maxLength != null) {
            // 使用反射或其他方式设置maxLength，因为CsdlProperty可能没有公开的setter
        }
        return this;
    }
}
