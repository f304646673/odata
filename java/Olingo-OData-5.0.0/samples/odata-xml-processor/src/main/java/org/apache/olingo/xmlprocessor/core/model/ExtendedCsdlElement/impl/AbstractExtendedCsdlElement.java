package org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;

/**
 * 抽象基类，提供Extended CSDL元素的通用功能
 * 统一管理Annotations集合和同步机制
 * 
 * @param <T> 包装的原始Csdl类型
 * @param <E> 当前Extended类型（用于流式API）
 */
public abstract class AbstractExtendedCsdlElement<T, E extends AbstractExtendedCsdlElement<T, E>> 
        implements ExtendedCsdlElement {
    
    protected final T wrappedElement;
    protected String namespace;
    
    // Extended版本的注解集合
    protected List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     * @param wrappedElement 被包装的原始Csdl对象
     */
    protected AbstractExtendedCsdlElement(T wrappedElement) {
        this.wrappedElement = wrappedElement;
        // 初始化后再调用方法
        initializeCollections();
    }

    /**
     * 初始化集合 - 在构造函数完成后调用
     */
    private void initializeCollections() {
        this.extendedAnnotations = new ArrayList<>();
    }

    /**
     * 获取包装的原始对象
     */
    public T getWrappedElement() {
        return wrappedElement;
    }

    // ==================== 通用Annotations处理 ====================

    /**
     * 获取Extended注解集合
     */
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }

    /**
     * 设置Extended注解集合
     */
    @SuppressWarnings("unchecked")
    public E setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations = extendedAnnotations;
        syncExtendedAnnotationsToOriginal();
        return (E) this;
    }

    /**
     * 添加Extended注解
     */
    @SuppressWarnings("unchecked")
    public E addExtendedAnnotation(ExtendedCsdlAnnotation extendedAnnotation) {
        if (extendedAnnotation != null) {
            this.extendedAnnotations.add(extendedAnnotation);
            syncExtendedAnnotationsToOriginal();
        }
        return (E) this;
    }

    /**
     * 将原始Annotations同步到Extended集合
     * 子类需要提供获取原始annotations的方法
     */
    protected void syncAnnotationsToExtended() {
        if (this.extendedAnnotations == null) {
            this.extendedAnnotations = new ArrayList<>();
        }
        this.extendedAnnotations.clear();
        
        List<CsdlAnnotation> annotations = getOriginalAnnotations();
        if (annotations != null) {
            for (CsdlAnnotation annotation : annotations) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                if (extendedAnnotation != null) {
                    this.extendedAnnotations.add(extendedAnnotation);
                }
            }
        }
    }

    /**
     * 将Extended Annotations同步到原始集合
     */
    protected void syncExtendedAnnotationsToOriginal() {
        List<CsdlAnnotation> annotations = new ArrayList<>();
        if (this.extendedAnnotations != null) {
            for (ExtendedCsdlAnnotation extendedAnnotation : this.extendedAnnotations) {
                if (extendedAnnotation != null) {
                    annotations.add(extendedAnnotation.asCsdlAnnotation());
                }
            }
        }
        setOriginalAnnotations(annotations);
    }

    /**
     * 处理Extended Annotations的设置（带同步）
     * 子类可以直接调用这个方法来处理setAnnotations
     */
    @SuppressWarnings("unchecked")
    protected E handleSetAnnotations(List<CsdlAnnotation> annotations) {
        setOriginalAnnotations(annotations);
        syncAnnotationsToExtended();
        return (E) this;
    }

    // ==================== 抽象方法 - 子类必须实现 ====================

    /**
     * 获取原始对象的annotations
     * 子类必须实现此方法来返回对应的annotations列表
     */
    @Deprecated
    protected abstract List<CsdlAnnotation> getOriginalAnnotations();

    /**
     * 设置原始对象的annotations
     * 子类必须实现此方法来设置对应的annotations列表
     */
    @Deprecated
    protected abstract void setOriginalAnnotations(List<CsdlAnnotation> annotations);

    // ==================== ExtendedCsdlElement 默认实现 ====================

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E setNamespace(String namespace) {
        this.namespace = namespace;
        return (E) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return (E) this;
    }

    // ==================== 工具方法 ====================

    /**
     * 从原始Annotations列表构建Extended Annotations列表
     */
    protected static List<ExtendedCsdlAnnotation> buildExtendedAnnotations(List<CsdlAnnotation> sourceAnnotations) {
        List<ExtendedCsdlAnnotation> extendedAnnotationsList = new ArrayList<>();
        if (sourceAnnotations != null) {
            for (CsdlAnnotation annotation : sourceAnnotations) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                if (extendedAnnotation != null) {
                    extendedAnnotationsList.add(extendedAnnotation);
                }
            }
        }
        return extendedAnnotationsList;
    }

    /**
     * 复制原始Annotations到Extended对象
     */
    @SuppressWarnings("unchecked")
    protected E copyAnnotationsFrom(List<CsdlAnnotation> sourceAnnotations) {
        if (sourceAnnotations != null) {
            List<ExtendedCsdlAnnotation> extendedAnnotationsList = buildExtendedAnnotations(sourceAnnotations);
            setExtendedAnnotations(extendedAnnotationsList);
            setOriginalAnnotations(new ArrayList<>(sourceAnnotations));
        }
        return (E) this;
    }
}
