package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的 CsdlTerm，采用组合模式
 */
public class ExtendedCsdlTerm {
    
    private final CsdlTerm wrappedTerm;
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlAnnotation> extendedAnnotations = new ArrayList<>();
    
    public ExtendedCsdlTerm() {
        this.wrappedTerm = new CsdlTerm();
    }
    
    public ExtendedCsdlTerm(CsdlTerm csdlTerm) {
        this.wrappedTerm = csdlTerm != null ? csdlTerm : new CsdlTerm();
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
        extended.setAppliesTo(source.getAppliesTo());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setDefaultValue(source.getDefaultValue());

        // 级联构建Annotations
        if (source.getAnnotations() != null) {
            for (CsdlAnnotation annotation : source.getAnnotations()) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extended.addExtendedAnnotation(extendedAnnotation);
            }
        }

        return extended;
    }
    
    // 获取内部包装的对象
    public CsdlTerm asCsdlTerm() {
        return wrappedTerm;
    }
    
    // 基本属性和方法
    public String getName() {
        return wrappedTerm.getName();
    }

    public ExtendedCsdlTerm setName(String name) {
        wrappedTerm.setName(name);
        return this;
    }

    public String getType() {
        return wrappedTerm.getType();
    }

    public ExtendedCsdlTerm setType(String type) {
        wrappedTerm.setType(type);
        return this;
    }

    public String getBaseTerm() {
        return wrappedTerm.getBaseTerm();
    }

    public ExtendedCsdlTerm setBaseTerm(String baseTerm) {
        wrappedTerm.setBaseTerm(baseTerm);
        return this;
    }

    public List<String> getAppliesTo() {
        return wrappedTerm.getAppliesTo();
    }

    public ExtendedCsdlTerm setAppliesTo(List<String> appliesTo) {
        wrappedTerm.setAppliesTo(appliesTo);
        return this;
    }

    public boolean isNullable() {
        return wrappedTerm.isNullable();
    }

    public ExtendedCsdlTerm setNullable(boolean nullable) {
        wrappedTerm.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedTerm.getMaxLength();
    }

    public ExtendedCsdlTerm setMaxLength(Integer maxLength) {
        wrappedTerm.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedTerm.getPrecision();
    }

    public ExtendedCsdlTerm setPrecision(Integer precision) {
        wrappedTerm.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedTerm.getScale();
    }

    public ExtendedCsdlTerm setScale(Integer scale) {
        wrappedTerm.setScale(scale);
        return this;
    }

    public String getDefaultValue() {
        return wrappedTerm.getDefaultValue();
    }

    public ExtendedCsdlTerm setDefaultValue(String defaultValue) {
        wrappedTerm.setDefaultValue(defaultValue);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        // 返回不可修改的原始数据视图
        return wrappedTerm.getAnnotations();
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
    public ExtendedCsdlTerm addExtendedAnnotation(ExtendedCsdlAnnotation extendedAnnotation) {
        if (extendedAnnotation != null) {
            extendedAnnotations.add(extendedAnnotation);
            syncAnnotationsToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended注解列表，同时更新原始数据
     */
    public ExtendedCsdlTerm setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
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
        wrappedTerm.setAnnotations(csdlAnnotations);
    }

    @Deprecated
    public ExtendedCsdlTerm setAnnotations(List<CsdlAnnotation> annotations) {
        // 保留向后兼容，但建议使用setExtendedAnnotations
        wrappedTerm.setAnnotations(annotations);
        // 同步到Extended对象
        syncAnnotationsFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended注解
     */
    private void syncAnnotationsFromWrapped() {
        extendedAnnotations.clear();
        if (wrappedTerm.getAnnotations() != null) {
            for (CsdlAnnotation annotation : wrappedTerm.getAnnotations()) {
                ExtendedCsdlAnnotation extAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extendedAnnotations.add(extAnnotation);
            }
        }
    }
}
