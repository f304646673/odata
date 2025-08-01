package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CsdlTypeDefinition，增加依赖关系追踪功能
 */
public class ExtendedCsdlTypeDefinition extends CsdlTypeDefinition implements ExtendedCsdlElement {
    
    private String fullyQualifiedName;
    private String namespace;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlTypeDefinition() {
        super();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlTypeDefinition创建ExtendedCsdlTypeDefinition
     */
    public static ExtendedCsdlTypeDefinition fromCsdlTypeDefinition(CsdlTypeDefinition source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlTypeDefinition extended = new ExtendedCsdlTypeDefinition();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setUnderlyingType(source.getUnderlyingType());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setUnicode(source.isUnicode());

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
        return getFullyQualifiedName();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (namespace != null && getName() != null) {
            return new FullQualifiedName(namespace, getName());
        }
        return null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.TYPE_DEFINITION;
    }

    @Override
    public String getElementPropertyName() {
        return null; // TypeDefinitions don't have property dependencies
    }

    @Override
    public ExtendedCsdlTypeDefinition setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null && this.getName() != null) {
            this.fullyQualifiedName = namespace + "." + this.getName();
        }
        return this;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public ExtendedCsdlTypeDefinition registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }

    /**
     * 获取完全限定名
     */
    public String getFullyQualifiedName() {
        if (fullyQualifiedName != null) {
            return fullyQualifiedName;
        }
        if (namespace != null && getName() != null) {
            return namespace + "." + getName();
        }
        return getName();
    }

    // Extended集合的getter方法
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
}
