package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlNavigationProperty，增加依赖关系追踪功能
 * 使用组合模式包装CsdlNavigationProperty，保持内部数据联动
 */
public class ExtendedCsdlNavigationProperty implements ExtendedCsdlElement {

    private final CsdlNavigationProperty wrappedNavProperty;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlNavigationProperty() {
        this.wrappedNavProperty = new CsdlNavigationProperty();
        initializeExtendedCollections();
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
    }

    /**
     * 从标准CsdlNavigationProperty创建ExtendedCsdlNavigationProperty
     */
    public static ExtendedCsdlNavigationProperty fromCsdlNavigationProperty(CsdlNavigationProperty source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlNavigationProperty extended = new ExtendedCsdlNavigationProperty();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setPartner(source.getPartner());
        extended.setContainsTarget(source.isContainsTarget());

        // 复制ReferentialConstraints
        if (source.getReferentialConstraints() != null) {
            extended.setReferentialConstraints(new ArrayList<>(source.getReferentialConstraints()));
        }

        // 复制OnDelete
        if (source.getOnDelete() != null) {
            extended.setOnDelete(source.getOnDelete());
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

    // ==================== CsdlNavigationProperty 方法委托 ====================
    
    public String getName() {
        return wrappedNavProperty.getName();
    }

    public ExtendedCsdlNavigationProperty setName(String name) {
        wrappedNavProperty.setName(name);
        return this;
    }

    public String getType() {
        return wrappedNavProperty.getType();
    }

    public ExtendedCsdlNavigationProperty setType(String type) {
        wrappedNavProperty.setType(type);
        return this;
    }

    public ExtendedCsdlNavigationProperty setType(FullQualifiedName type) {
        wrappedNavProperty.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedNavProperty.isCollection();
    }

    public ExtendedCsdlNavigationProperty setCollection(boolean isCollection) {
        wrappedNavProperty.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedNavProperty.isNullable();
    }

    public ExtendedCsdlNavigationProperty setNullable(Boolean nullable) {
        wrappedNavProperty.setNullable(nullable);
        return this;
    }

    public String getPartner() {
        return wrappedNavProperty.getPartner();
    }

    public ExtendedCsdlNavigationProperty setPartner(String partner) {
        wrappedNavProperty.setPartner(partner);
        return this;
    }

    public boolean isContainsTarget() {
        return wrappedNavProperty.isContainsTarget();
    }

    public ExtendedCsdlNavigationProperty setContainsTarget(boolean containsTarget) {
        wrappedNavProperty.setContainsTarget(containsTarget);
        return this;
    }

    public List<CsdlReferentialConstraint> getReferentialConstraints() {
        return wrappedNavProperty.getReferentialConstraints();
    }

    public ExtendedCsdlNavigationProperty setReferentialConstraints(List<CsdlReferentialConstraint> referentialConstraints) {
        wrappedNavProperty.setReferentialConstraints(referentialConstraints);
        return this;
    }

    public org.apache.olingo.commons.api.edm.provider.CsdlOnDelete getOnDelete() {
        return wrappedNavProperty.getOnDelete();
    }

    public ExtendedCsdlNavigationProperty setOnDelete(org.apache.olingo.commons.api.edm.provider.CsdlOnDelete onDelete) {
        wrappedNavProperty.setOnDelete(onDelete);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedNavProperty.getAnnotations();
    }

    public ExtendedCsdlNavigationProperty setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedNavProperty.setAnnotations(annotations);
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
        wrappedNavProperty.setAnnotations(annotations);
    }

    /**
     * 获取包装的CsdlNavigationProperty实例
     */
    public CsdlNavigationProperty asCsdlNavigationProperty() {
        return wrappedNavProperty;
    }

    // ==================== ExtendedCsdlElement 实现 ====================

    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "NavigationProperty_" + hashCode();
    }

    @Override
    public ExtendedCsdlNavigationProperty setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlNavigationProperty registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.NAVIGATION_PROPERTY_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }
}
