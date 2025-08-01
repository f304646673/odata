package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的CsdlTypeDefinition，增加依赖关系追踪功能
 * 使用组合模式包装CsdlTypeDefinition，保持内部数据联动
 */
public class ExtendedCsdlTypeDefinition implements ExtendedCsdlElement {
    
    private final CsdlTypeDefinition wrappedTypeDefinition;
    private String namespace;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlTypeDefinition() {
        this.wrappedTypeDefinition = new CsdlTypeDefinition();
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
        extended.setSrid(source.getSrid());

        // 复制Annotations
        if (source.getAnnotations() != null) {
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

    // ==================== CsdlTypeDefinition 方法委托 ====================
    
    public String getName() {
        return wrappedTypeDefinition.getName();
    }

    public ExtendedCsdlTypeDefinition setName(String name) {
        wrappedTypeDefinition.setName(name);
        return this;
    }

    public String getUnderlyingType() {
        return wrappedTypeDefinition.getUnderlyingType();
    }

    public ExtendedCsdlTypeDefinition setUnderlyingType(String underlyingType) {
        wrappedTypeDefinition.setUnderlyingType(underlyingType);
        return this;
    }

    public ExtendedCsdlTypeDefinition setUnderlyingType(FullQualifiedName underlyingType) {
        wrappedTypeDefinition.setUnderlyingType(underlyingType);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedTypeDefinition.getMaxLength();
    }

    public ExtendedCsdlTypeDefinition setMaxLength(Integer maxLength) {
        wrappedTypeDefinition.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedTypeDefinition.getPrecision();
    }

    public ExtendedCsdlTypeDefinition setPrecision(Integer precision) {
        wrappedTypeDefinition.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedTypeDefinition.getScale();
    }

    public ExtendedCsdlTypeDefinition setScale(Integer scale) {
        wrappedTypeDefinition.setScale(scale);
        return this;
    }

    public Boolean isUnicode() {
        return wrappedTypeDefinition.isUnicode();
    }

    public ExtendedCsdlTypeDefinition setUnicode(Boolean unicode) {
        wrappedTypeDefinition.setUnicode(unicode);
        return this;
    }

    public SRID getSrid() {
        return wrappedTypeDefinition.getSrid();
    }

    public ExtendedCsdlTypeDefinition setSrid(SRID srid) {
        wrappedTypeDefinition.setSrid(srid);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedTypeDefinition.getAnnotations();
    }

    public ExtendedCsdlTypeDefinition setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedTypeDefinition.setAnnotations(annotations);
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }

    public void setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations = extendedAnnotations;
    }

    /**
     * 获取包装的CsdlTypeDefinition实例
     */
    public CsdlTypeDefinition asCsdlTypeDefinition() {
        return wrappedTypeDefinition;
    }

    // ==================== ExtendedCsdlElement 实现 ====================

    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "TypeDefinition_" + hashCode();
    }

    @Override
    public ExtendedCsdlTypeDefinition setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlTypeDefinition registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return null;
    }
}
