package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CSDL枚举类型
 * 继承自CsdlEnumType，增加依赖跟踪和扩展功能
 */
public class ExtendedCsdlEnumType extends CsdlEnumType implements ExtendedCsdlElement {

    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlEnumType() {
        super();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlEnumType创建ExtendedCsdlEnumType
     */
    public static ExtendedCsdlEnumType fromCsdlEnumType(CsdlEnumType source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlEnumType extended = new ExtendedCsdlEnumType();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setUnderlyingType(source.getUnderlyingType());
        extended.setFlags(source.isFlags());

        // 复制枚举成员
        if (source.getMembers() != null) {
            extended.setMembers(new ArrayList<>(source.getMembers()));
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
        this.extendedAnnotations = new ArrayList<>();
    }

    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "EnumType_" + hashCode();
    }

    @Override
    public ExtendedCsdlEnumType setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlEnumType registerElement() {
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
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return null; // EnumType通常不关联特定属性
    }

    // Extended集合的getter方法
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
}
