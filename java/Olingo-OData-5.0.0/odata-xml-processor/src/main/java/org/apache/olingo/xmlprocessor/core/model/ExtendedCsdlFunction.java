package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CsdlFunction，支持依赖关系跟踪
 */
public class ExtendedCsdlFunction extends CsdlFunction implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlParameter> extendedParameters;
    private ExtendedCsdlReturnType extendedReturnType;
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlFunction() {
        this.elementId = null;
        initializeExtendedCollections();
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlFunction(String elementId) {
        this.elementId = elementId;
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlFunction创建ExtendedCsdlFunction
     */
    public static ExtendedCsdlFunction fromCsdlFunction(CsdlFunction source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlFunction extended = new ExtendedCsdlFunction();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setBound(source.isBound());
        extended.setComposable(source.isComposable());
        extended.setEntitySetPath(source.getEntitySetPath());

        // 转换Parameters为ExtendedCsdlParameter
        if (source.getParameters() != null) {
            List<CsdlParameter> extendedParameters = source.getParameters().stream()
                .map(parameter -> ExtendedCsdlParameter.fromCsdlParameter(parameter))
                .collect(Collectors.toList());
            extended.setParameters(extendedParameters);
        }

        // 转换ReturnType为ExtendedCsdlReturnType
        if (source.getReturnType() != null) {
            ExtendedCsdlReturnType extendedReturnType =
                ExtendedCsdlReturnType.fromCsdlReturnType(source.getReturnType());
            extended.setReturnType(extendedReturnType);
        }

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
        this.extendedParameters = new ArrayList<>();
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
        return "Function_" + hashCode();
    }
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlFunction setNamespace(String namespace) {
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

    @Override
    public ExtendedCsdlFunction registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }
    
    /**
     * 获取元素的完全限定名（如果适用）
     */
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }
    
    /**
     * 获取元素的依赖类型
     */
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.FUNCTION_REFERENCE;
    }

    /**
     * 获取元素的属性名称
     */
    @Override
    public String getElementPropertyName() {
        return null; // Function通常不关联特定属性
    }
    
    // Extended集合的getter方法
    public List<ExtendedCsdlParameter> getExtendedParameters() {
        return extendedParameters;
    }

    public ExtendedCsdlReturnType getExtendedReturnType() {
        return extendedReturnType;
    }

    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
}
