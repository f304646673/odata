package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的 CsdlEntitySet，采用组合模式
 * 使用组合模式包装CsdlEntitySet，保持内部数据联动
 */
public class ExtendedCsdlEntitySet {
    
    private final CsdlEntitySet wrappedEntitySet;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;
    private List<ExtendedCsdlNavigationPropertyBinding> extendedNavigationPropertyBindings;
    
    public ExtendedCsdlEntitySet() {
        this.wrappedEntitySet = new CsdlEntitySet();
        initializeExtendedCollections();
    }
    
    public ExtendedCsdlEntitySet(CsdlEntitySet csdlEntitySet) {
        this.wrappedEntitySet = csdlEntitySet != null ? csdlEntitySet : new CsdlEntitySet();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlEntitySet创建ExtendedCsdlEntitySet
     */
    public static ExtendedCsdlEntitySet fromCsdlEntitySet(CsdlEntitySet source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlEntitySet extended = new ExtendedCsdlEntitySet();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setIncludeInServiceDocument(source.isIncludeInServiceDocument());

        // 复制NavigationPropertyBindings并转换为Extended版本
        if (source.getNavigationPropertyBindings() != null) {
            List<ExtendedCsdlNavigationPropertyBinding> extendedBindingsList = new ArrayList<ExtendedCsdlNavigationPropertyBinding>();
            for (CsdlNavigationPropertyBinding binding : source.getNavigationPropertyBindings()) {
                ExtendedCsdlNavigationPropertyBinding extendedBinding = ExtendedCsdlNavigationPropertyBinding.fromCsdlNavigationPropertyBinding(binding);
                if (extendedBinding != null) {
                    extendedBindingsList.add(extendedBinding);
                }
            }
            extended.setExtendedNavigationPropertyBindings(extendedBindingsList);
            extended.setNavigationPropertyBindings(new ArrayList<CsdlNavigationPropertyBinding>(source.getNavigationPropertyBindings()));
        }

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
        this.extendedNavigationPropertyBindings = new ArrayList<ExtendedCsdlNavigationPropertyBinding>();
    }
    
    // 获取内部包装的对象
    public CsdlEntitySet asCsdlEntitySet() {
        return wrappedEntitySet;
    }
    
    // ==================== CsdlEntitySet 方法委托 ====================
    
    public String getName() {
        return wrappedEntitySet.getName();
    }

    public ExtendedCsdlEntitySet setName(String name) {
        wrappedEntitySet.setName(name);
        return this;
    }

    public String getType() {
        return wrappedEntitySet.getType();
    }

    public ExtendedCsdlEntitySet setType(String type) {
        wrappedEntitySet.setType(type);
        return this;
    }

    public boolean isIncludeInServiceDocument() {
        return wrappedEntitySet.isIncludeInServiceDocument();
    }

    public ExtendedCsdlEntitySet setIncludeInServiceDocument(boolean includeInServiceDocument) {
        wrappedEntitySet.setIncludeInServiceDocument(includeInServiceDocument);
        return this;
    }

    public List<CsdlNavigationPropertyBinding> getNavigationPropertyBindings() {
        return wrappedEntitySet.getNavigationPropertyBindings();
    }

    public ExtendedCsdlEntitySet setNavigationPropertyBindings(List<CsdlNavigationPropertyBinding> navigationPropertyBindings) {
        wrappedEntitySet.setNavigationPropertyBindings(navigationPropertyBindings);
        syncNavigationPropertyBindingsToExtended();
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedEntitySet.getAnnotations();
    }

    public ExtendedCsdlEntitySet setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedEntitySet.setAnnotations(annotations);
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

    public List<ExtendedCsdlNavigationPropertyBinding> getExtendedNavigationPropertyBindings() {
        return extendedNavigationPropertyBindings;
    }

    public void setExtendedNavigationPropertyBindings(List<ExtendedCsdlNavigationPropertyBinding> extendedNavigationPropertyBindings) {
        this.extendedNavigationPropertyBindings = extendedNavigationPropertyBindings;
        syncExtendedNavigationPropertyBindingsToOriginal();
    }

    // ==================== 同步方法 ====================

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

    private void syncExtendedAnnotationsToOriginal() {
        List<CsdlAnnotation> annotations = new ArrayList<CsdlAnnotation>();
        if (this.extendedAnnotations != null) {
            for (ExtendedCsdlAnnotation extendedAnnotation : this.extendedAnnotations) {
                if (extendedAnnotation != null) {
                    annotations.add(extendedAnnotation.asCsdlAnnotation());
                }
            }
        }
        wrappedEntitySet.setAnnotations(annotations);
    }

    private void syncNavigationPropertyBindingsToExtended() {
        if (this.extendedNavigationPropertyBindings == null) {
            this.extendedNavigationPropertyBindings = new ArrayList<ExtendedCsdlNavigationPropertyBinding>();
        }
        this.extendedNavigationPropertyBindings.clear();
        
        List<CsdlNavigationPropertyBinding> bindings = getNavigationPropertyBindings();
        if (bindings != null) {
            for (CsdlNavigationPropertyBinding binding : bindings) {
                ExtendedCsdlNavigationPropertyBinding extendedBinding = ExtendedCsdlNavigationPropertyBinding.fromCsdlNavigationPropertyBinding(binding);
                if (extendedBinding != null) {
                    this.extendedNavigationPropertyBindings.add(extendedBinding);
                }
            }
        }
    }

    private void syncExtendedNavigationPropertyBindingsToOriginal() {
        List<CsdlNavigationPropertyBinding> bindings = new ArrayList<CsdlNavigationPropertyBinding>();
        if (this.extendedNavigationPropertyBindings != null) {
            for (ExtendedCsdlNavigationPropertyBinding extendedBinding : this.extendedNavigationPropertyBindings) {
                if (extendedBinding != null) {
                    bindings.add(extendedBinding.asCsdlNavigationPropertyBinding());
                }
            }
        }
        wrappedEntitySet.setNavigationPropertyBindings(bindings);
    }
}
