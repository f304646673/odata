package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CSDL术语
 * 继承自CsdlTerm，增加依赖跟踪和扩展功能
 */
public class ExtendedCsdlTerm extends CsdlTerm implements ExtendedCsdlElement {
    
    private String namespace;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlTerm() {
        super();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlTerm创建ExtendedCsdlTerm
     */
    public static ExtendedCsdlTerm fromCsdlTerm(CsdlTerm source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlTerm extended = new ExtendedCsdlTerm();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setBaseTerm(source.getBaseTerm());
        extended.setDefaultValue(source.getDefaultValue());
        extended.setAppliesTo(source.getAppliesTo());
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
        if (getName() != null) {
            return getName();
        }
        return "Term_" + hashCode();
    }
    
    @Override
    public ExtendedCsdlTerm setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlTerm registerElement() {
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
        return CsdlDependencyNode.DependencyType.TERM_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return null; // Term通常不关联特定属性
    }
    
    // Extended集合的getter方法
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
}
