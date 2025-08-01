package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CSDL枚举类型
 * 使用组合模式包装CsdlEnumType，保持内部数据联动
 */
public class ExtendedCsdlEnumType implements ExtendedCsdlElement {

    private final CsdlEnumType wrappedEnumType;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlEnumType() {
        this.wrappedEnumType = new CsdlEnumType();
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
        if (source.getUnderlyingType() != null) {
            extended.setUnderlyingType(source.getUnderlyingType());
        }
        extended.setFlags(source.isFlags());

        // 复制枚举成员
        if (source.getMembers() != null) {
            extended.setMembers(new ArrayList<CsdlEnumMember>(source.getMembers()));
        }

        // 级联构建Annotations
        if (source.getAnnotations() != null) {
            for (CsdlAnnotation annotation : source.getAnnotations()) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extended.addExtendedAnnotation(extendedAnnotation);
            }
        }

        return extended;
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
    }

    // ==================== CsdlEnumType 方法委托 ====================
    
    public String getName() {
        return wrappedEnumType.getName();
    }

    public ExtendedCsdlEnumType setName(String name) {
        wrappedEnumType.setName(name);
        return this;
    }

    public String getUnderlyingType() {
        return wrappedEnumType.getUnderlyingType();
    }

    public ExtendedCsdlEnumType setUnderlyingType(String underlyingType) {
        wrappedEnumType.setUnderlyingType(underlyingType);
        return this;
    }

    public boolean isFlags() {
        return wrappedEnumType.isFlags();
    }

    public ExtendedCsdlEnumType setFlags(boolean isFlags) {
        wrappedEnumType.setFlags(isFlags);
        return this;
    }

    public List<CsdlEnumMember> getMembers() {
        return wrappedEnumType.getMembers();
    }

    public ExtendedCsdlEnumType setMembers(List<CsdlEnumMember> members) {
        wrappedEnumType.setMembers(members);
        return this;
    }

    public CsdlEnumMember getMember(String name) {
        return wrappedEnumType.getMember(name);
    }

    public List<CsdlAnnotation> getAnnotations() {
        // 返回不可修改的原始数据视图
        return wrappedEnumType.getAnnotations();
    }

    /**
     * 获取Extended注解列表
     */
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return new ArrayList<>(extendedAnnotations);
    }

    /**
     * 添加Extended注解，同时更新原始数据
     */
    public ExtendedCsdlEnumType addExtendedAnnotation(ExtendedCsdlAnnotation extendedAnnotation) {
        if (extendedAnnotation != null) {
            extendedAnnotations.add(extendedAnnotation);
            syncAnnotationsToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended注解列表，同时更新原始数据
     */
    public ExtendedCsdlEnumType setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations.clear();
        if (extendedAnnotations != null) {
            this.extendedAnnotations.addAll(extendedAnnotations);
        }
        syncAnnotationsToWrapped();
        return this;
    }

    /**
     * 同步Extended注解到原始数据
     */
    private void syncAnnotationsToWrapped() {
        List<CsdlAnnotation> csdlAnnotations = new ArrayList<>();
        for (ExtendedCsdlAnnotation extAnnotation : extendedAnnotations) {
            csdlAnnotations.add(extAnnotation.asCsdlAnnotation());
        }
        wrappedEnumType.setAnnotations(csdlAnnotations);
    }

    @Deprecated
    public ExtendedCsdlEnumType setAnnotations(List<CsdlAnnotation> annotations) {
        // 保留向后兼容，但建议使用setExtendedAnnotations
        wrappedEnumType.setAnnotations(annotations);
        // 同步到Extended对象
        syncAnnotationsFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended注解
     */
    private void syncAnnotationsFromWrapped() {
        extendedAnnotations.clear();
        if (wrappedEnumType.getAnnotations() != null) {
            for (CsdlAnnotation annotation : wrappedEnumType.getAnnotations()) {
                ExtendedCsdlAnnotation extAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extendedAnnotations.add(extAnnotation);
            }
        }
    }

    /**
     * 获取包装的CsdlEnumType实例
     */
    public CsdlEnumType asCsdlEnumType() {
        return wrappedEnumType;
    }

    // ==================== ExtendedCsdlElement 实现 ====================

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
}
