package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlProperty，增加依赖关系追踪功能
 * 使用组合模式包装CsdlProperty，保持内部数据联动
 */
public class ExtendedCsdlProperty implements ExtendedCsdlElement {

    private final CsdlProperty wrappedProperty;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlProperty() {
        this.wrappedProperty = new CsdlProperty();
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
    }

    /**
     * 从标准CsdlProperty创建ExtendedCsdlProperty
     */
    public static ExtendedCsdlProperty fromCsdlProperty(CsdlProperty source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlProperty extended = new ExtendedCsdlProperty();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setUnicode(source.isUnicode());
        extended.setDefaultValue(source.getDefaultValue());
        extended.setSrid(source.getSrid());

        // 级联构建Annotations
        if (source.getAnnotations() != null) {
            for (CsdlAnnotation annotation : source.getAnnotations()) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extended.addExtendedAnnotation(extendedAnnotation);
            }
        }

        return extended;
    }

    // ==================== CsdlProperty 方法委托 ====================
    
    public String getName() {
        return wrappedProperty.getName();
    }

    public ExtendedCsdlProperty setName(String name) {
        wrappedProperty.setName(name);
        return this;
    }

    public String getType() {
        return wrappedProperty.getType();
    }

    public ExtendedCsdlProperty setType(String type) {
        wrappedProperty.setType(type);
        return this;
    }

    public ExtendedCsdlProperty setType(FullQualifiedName type) {
        wrappedProperty.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedProperty.isCollection();
    }

    public ExtendedCsdlProperty setCollection(boolean isCollection) {
        wrappedProperty.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedProperty.isNullable();
    }

    public ExtendedCsdlProperty setNullable(Boolean nullable) {
        wrappedProperty.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedProperty.getMaxLength();
    }

    public ExtendedCsdlProperty setMaxLength(Integer maxLength) {
        wrappedProperty.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedProperty.getPrecision();
    }

    public ExtendedCsdlProperty setPrecision(Integer precision) {
        wrappedProperty.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedProperty.getScale();
    }

    public ExtendedCsdlProperty setScale(Integer scale) {
        wrappedProperty.setScale(scale);
        return this;
    }

    public Boolean isUnicode() {
        return wrappedProperty.isUnicode();
    }

    public ExtendedCsdlProperty setUnicode(Boolean unicode) {
        wrappedProperty.setUnicode(unicode);
        return this;
    }

    public String getDefaultValue() {
        return wrappedProperty.getDefaultValue();
    }

    public ExtendedCsdlProperty setDefaultValue(String defaultValue) {
        wrappedProperty.setDefaultValue(defaultValue);
        return this;
    }

    public SRID getSrid() {
        return wrappedProperty.getSrid();
    }

    public ExtendedCsdlProperty setSrid(SRID srid) {
        wrappedProperty.setSrid(srid);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        // 返回不可修改的原始数据视图
        return wrappedProperty.getAnnotations();
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
    public ExtendedCsdlProperty addExtendedAnnotation(ExtendedCsdlAnnotation extendedAnnotation) {
        if (extendedAnnotation != null) {
            extendedAnnotations.add(extendedAnnotation);
            syncAnnotationsToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended注解列表，同时更新原始数据
     */
    public ExtendedCsdlProperty setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
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
        wrappedProperty.setAnnotations(csdlAnnotations);
    }

    @Deprecated
    public ExtendedCsdlProperty setAnnotations(List<CsdlAnnotation> annotations) {
        // 保留向后兼容，但建议使用setExtendedAnnotations
        wrappedProperty.setAnnotations(annotations);
        // 同步到Extended对象
        syncAnnotationsFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended注解
     */
    private void syncAnnotationsFromWrapped() {
        extendedAnnotations.clear();
        if (wrappedProperty.getAnnotations() != null) {
            for (CsdlAnnotation annotation : wrappedProperty.getAnnotations()) {
                ExtendedCsdlAnnotation extAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extendedAnnotations.add(extAnnotation);
            }
        }
    }

    /**
     * 获取包装的CsdlProperty实例
     */
    public CsdlProperty asCsdlProperty() {
        return wrappedProperty;
    }

    // ==================== ExtendedCsdlElement 实现 ====================

    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "Property_" + hashCode();
    }

    @Override
    public ExtendedCsdlProperty setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlProperty registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.PROPERTY_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }
}
