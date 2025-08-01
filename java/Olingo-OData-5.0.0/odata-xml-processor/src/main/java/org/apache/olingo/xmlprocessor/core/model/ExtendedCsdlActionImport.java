package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的 CsdlActionImport，采用组合模式
 * 使用组合模式包装CsdlActionImport，保持内部数据联动
 */
public class ExtendedCsdlActionImport {
    
    private final CsdlActionImport wrappedActionImport;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;
    
    public ExtendedCsdlActionImport() {
        this.wrappedActionImport = new CsdlActionImport();
        initializeExtendedCollections();
    }
    
    public ExtendedCsdlActionImport(CsdlActionImport csdlActionImport) {
        this.wrappedActionImport = csdlActionImport != null ? csdlActionImport : new CsdlActionImport();
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

        // 复制Annotations并转换为Extended版本
        if (source.getAnnotations() != null) {
            List<ExtendedCsdlAnnotation> extendedAnnotationsList = new ArrayList<ExtendedCsdlAnnotation>();
            for (CsdlAnnotation annotation : source.getAnnotations()) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                if (extendedAnnotation != null) {
                    extendedAnnotationsList.add(extendedAnnotation);
                }
            }
            extended.setExtendedAnnotations(extendedAnnotationsList);
            
            // 同时设置原始Annotations以保持数据联动
            extended.setAnnotations(new ArrayList<CsdlAnnotation>(source.getAnnotations()));
        }

        return extended;
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
    }
    
    // 获取内部包装的对象
    public CsdlActionImport asCsdlActionImport() {
        return wrappedActionImport;
    }
    
    // ==================== CsdlActionImport 方法委托 ====================
    
    public String getName() {
        return wrappedActionImport.getName();
    }

    public ExtendedCsdlActionImport setName(String name) {
        wrappedActionImport.setName(name);
        return this;
    }

    public String getAction() {
        return wrappedActionImport.getAction();
    }

    public ExtendedCsdlActionImport setAction(String action) {
        wrappedActionImport.setAction(action);
        return this;
    }

    public String getEntitySet() {
        return wrappedActionImport.getEntitySet();
    }

    public ExtendedCsdlActionImport setEntitySet(String entitySet) {
        wrappedActionImport.setEntitySet(entitySet);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedActionImport.getAnnotations();
    }

    public ExtendedCsdlActionImport setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedActionImport.setAnnotations(annotations);
        // 同步到Extended集合
        syncAnnotationsToExtended();
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }

    public void setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations = extendedAnnotations;
        // 同步到原始集合
        syncExtendedAnnotationsToOriginal();
    }

    /**
     * 将原始Annotations同步到Extended集合
     */
    private void syncAnnotationsToExtended() {
        if (this.extendedAnnotations == null) {
            this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
        }
        this.extendedAnnotations.clear();
        
        List<CsdlAnnotation> annotations = getAnnotations();
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
    private void syncExtendedAnnotationsToOriginal() {
        List<CsdlAnnotation> annotations = new ArrayList<CsdlAnnotation>();
        if (this.extendedAnnotations != null) {
            for (ExtendedCsdlAnnotation extendedAnnotation : this.extendedAnnotations) {
                if (extendedAnnotation != null) {
                    annotations.add(extendedAnnotation.asCsdlAnnotation());
                }
            }
        }
        wrappedActionImport.setAnnotations(annotations);
    }
}
