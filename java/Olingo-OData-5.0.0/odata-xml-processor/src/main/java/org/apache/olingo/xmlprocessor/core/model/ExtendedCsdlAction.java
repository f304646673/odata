package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CsdlAction，支持依赖关系跟踪
 */
public class ExtendedCsdlAction extends CsdlAction implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlParameter> extendedParameters;
    private ExtendedCsdlReturnType extendedReturnType;
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlAction() {
        this.elementId = null;
        initializeExtendedCollections();
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlAction(String elementId) {
        this.elementId = elementId;
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlAction创建ExtendedCsdlAction
     */
    public static ExtendedCsdlAction fromCsdlAction(CsdlAction source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlAction extended = new ExtendedCsdlAction();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setBound(source.isBound());
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
        return "Action_" + hashCode();
    }
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlAction setNamespace(String namespace) {
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
    public ExtendedCsdlAction registerElement() {
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
        return CsdlDependencyNode.DependencyType.ACTION_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return null; // Action通常不关联特定属性
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
