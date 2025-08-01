package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CsdlParameter，支持依赖关系跟踪
 */
public class ExtendedCsdlParameter extends CsdlParameter implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;
    private String parentName;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlParameter() {
        this.elementId = null;
        initializeExtendedCollections();
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlParameter(String elementId) {
        this.elementId = elementId;
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlParameter创建ExtendedCsdlParameter
     */
    public static ExtendedCsdlParameter fromCsdlParameter(CsdlParameter source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlParameter extended = new ExtendedCsdlParameter();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());

        // 转换Annotations为ExtendedCsdlAnnotation
        if (source.getAnnotations() != null) {
            List<CsdlAnnotation> extendedAnnotations = source.getAnnotations().stream()
                .map(annotation -> ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation))
                .collect(Collectors.toList());
            extended.setAnnotations(extendedAnnotations);
        }

        return extended;
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<>();
    }
    
    @Override
    public String getElementId() {
        if (elementId != null) {
            return elementId;
        }
        if (getName() != null) {
            return getName();
        }
        return "Parameter_" + hashCode();
    }
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlParameter setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Get namespace
     */
    @Override
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Override registerElement to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlParameter registerElement() {
        // Call the interface default method but return this instance
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
     * 获取元素相关的属性名（如果适用）
     */
    @Override
    public String getElementPropertyName() {
        return getName();
    }

    /**
     * 设置父元素名称
     */
    @Override
    public ExtendedCsdlParameter setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    /**
     * 获取父元素名称
     */
    public String getParentName() {
        return parentName;
    }

    // Extended集合的getter方法
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlParameter{name='%s', type='%s', nullable=%s, parentName='%s'}", 
                getName(), getType(), isNullable(), getParentName());
    }
}
