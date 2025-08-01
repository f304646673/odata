package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CsdlActionImport，支持依赖关系跟踪
 */
public class ExtendedCsdlActionImport extends CsdlActionImport implements ExtendedCsdlElement {
    
    private final String elementId;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlActionImport() {
        this.elementId = null;
        initializeExtendedCollections();
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlActionImport(String elementId) {
        this.elementId = elementId;
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlActionImport创建ExtendedCsdlActionImport
     */
    public static ExtendedCsdlActionImport fromCsdlActionImport(CsdlActionImport source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlActionImport extended = new ExtendedCsdlActionImport();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setAction(source.getAction());
        extended.setEntitySet(source.getEntitySet());

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
        return "ActionImport_" + hashCode();
    }
    
    @Override
    public ExtendedCsdlActionImport setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlActionImport registerElement() {
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
        return CsdlDependencyNode.DependencyType.ACTION_IMPORT_REFERENCE;
    }
    
    @Override
    public String getElementPropertyName() {
        return null; // ActionImport通常不关联特定属性
    }

    // Extended集合的getter方法
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
}
