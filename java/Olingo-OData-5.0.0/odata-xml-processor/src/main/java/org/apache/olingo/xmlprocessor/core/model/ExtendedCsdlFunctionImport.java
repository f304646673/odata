package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的 CsdlFunctionImport，采用组合模式
 * 使用组合模式包装CsdlFunctionImport，保持内部数据联动
 */
public class ExtendedCsdlFunctionImport {
    
    private final CsdlFunctionImport wrappedFunctionImport;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;
    
    public ExtendedCsdlFunctionImport() {
        this.wrappedFunctionImport = new CsdlFunctionImport();
        initializeExtendedCollections();
    }
    
    public ExtendedCsdlFunctionImport(CsdlFunctionImport csdlFunctionImport) {
        this.wrappedFunctionImport = csdlFunctionImport != null ? csdlFunctionImport : new CsdlFunctionImport();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlFunctionImport创建ExtendedCsdlFunctionImport
     */
    public static ExtendedCsdlFunctionImport fromCsdlFunctionImport(CsdlFunctionImport source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlFunctionImport extended = new ExtendedCsdlFunctionImport();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setFunction(source.getFunction());
        extended.setEntitySet(source.getEntitySet());
        extended.setIncludeInServiceDocument(source.isIncludeInServiceDocument());

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
    public CsdlFunctionImport asCsdlFunctionImport() {
        return wrappedFunctionImport;
    }
    
    // ==================== CsdlFunctionImport 方法委托 ====================
    
    public String getName() {
        return wrappedFunctionImport.getName();
    }

    public ExtendedCsdlFunctionImport setName(String name) {
        wrappedFunctionImport.setName(name);
        return this;
    }

    public String getFunction() {
        return wrappedFunctionImport.getFunction();
    }

    public ExtendedCsdlFunctionImport setFunction(String function) {
        wrappedFunctionImport.setFunction(function);
        return this;
    }

    public String getEntitySet() {
        return wrappedFunctionImport.getEntitySet();
    }

    public ExtendedCsdlFunctionImport setEntitySet(String entitySet) {
        wrappedFunctionImport.setEntitySet(entitySet);
        return this;
    }

    public boolean isIncludeInServiceDocument() {
        return wrappedFunctionImport.isIncludeInServiceDocument();
    }

    public ExtendedCsdlFunctionImport setIncludeInServiceDocument(boolean includeInServiceDocument) {
        wrappedFunctionImport.setIncludeInServiceDocument(includeInServiceDocument);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedFunctionImport.getAnnotations();
    }

    public ExtendedCsdlFunctionImport setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedFunctionImport.setAnnotations(annotations);
        syncAnnotationsToExtended();
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }

    public void setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations = extendedAnnotations;
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
        wrappedFunctionImport.setAnnotations(annotations);
    }
}
