package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CsdlComplexType，支持依赖关系跟踪
 * 使用组合模式包装CsdlComplexType，保持内部数据联动
 */
public class ExtendedCsdlComplexType implements ExtendedCsdlElement {
    
    private final CsdlComplexType wrappedComplexType;
    private final String elementId;
    private String namespace;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlProperty> extendedProperties;
    private List<ExtendedCsdlNavigationProperty> extendedNavigationProperties;
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlComplexType() {
        this.wrappedComplexType = new CsdlComplexType();
        this.elementId = null;
        initializeExtendedCollections();
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlComplexType(String elementId) {
        this.wrappedComplexType = new CsdlComplexType();
        this.elementId = elementId;
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlComplexType创建ExtendedCsdlComplexType
     * @param source 源CsdlComplexType
     * @return ExtendedCsdlComplexType实例
     */
    public static ExtendedCsdlComplexType fromCsdlComplexType(CsdlComplexType source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlComplexType extended = new ExtendedCsdlComplexType();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setAbstract(source.isAbstract());
        extended.setOpenType(source.isOpenType());
        if (source.getBaseType() != null) {
            extended.setBaseType(source.getBaseType());
        }

        // 复制Properties
        if (source.getProperties() != null) {
            extended.setProperties(new ArrayList<CsdlProperty>(source.getProperties()));
        }

        // 复制NavigationProperties
        if (source.getNavigationProperties() != null) {
            extended.setNavigationProperties(new ArrayList<CsdlNavigationProperty>(source.getNavigationProperties()));
        }

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
        this.extendedProperties = new ArrayList<ExtendedCsdlProperty>();
        this.extendedNavigationProperties = new ArrayList<ExtendedCsdlNavigationProperty>();
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
    }

    // ==================== CsdlComplexType 方法委托 ====================
    
    public String getName() {
        return wrappedComplexType.getName();
    }

    public ExtendedCsdlComplexType setName(String name) {
        wrappedComplexType.setName(name);
        return this;
    }

    public boolean isAbstract() {
        return wrappedComplexType.isAbstract();
    }

    public ExtendedCsdlComplexType setAbstract(boolean isAbstract) {
        wrappedComplexType.setAbstract(isAbstract);
        return this;
    }

    public String getBaseType() {
        return wrappedComplexType.getBaseType();
    }

    public FullQualifiedName getBaseTypeFQN() {
        return wrappedComplexType.getBaseTypeFQN();
    }

    public ExtendedCsdlComplexType setBaseType(FullQualifiedName baseType) {
        wrappedComplexType.setBaseType(baseType);
        return this;
    }

    public ExtendedCsdlComplexType setBaseType(String baseTypeFQN) {
        wrappedComplexType.setBaseType(baseTypeFQN);
        return this;
    }

    public boolean isOpenType() {
        return wrappedComplexType.isOpenType();
    }

    public ExtendedCsdlComplexType setOpenType(boolean isOpenType) {
        wrappedComplexType.setOpenType(isOpenType);
        return this;
    }

    public List<CsdlProperty> getProperties() {
        return wrappedComplexType.getProperties();
    }

    public ExtendedCsdlComplexType setProperties(List<CsdlProperty> properties) {
        wrappedComplexType.setProperties(properties);
        return this;
    }

    public CsdlProperty getProperty(String name) {
        return wrappedComplexType.getProperty(name);
    }

    public List<CsdlNavigationProperty> getNavigationProperties() {
        return wrappedComplexType.getNavigationProperties();
    }

    public ExtendedCsdlComplexType setNavigationProperties(List<CsdlNavigationProperty> navigationProperties) {
        wrappedComplexType.setNavigationProperties(navigationProperties);
        return this;
    }

    public CsdlNavigationProperty getNavigationProperty(String name) {
        return wrappedComplexType.getNavigationProperty(name);
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedComplexType.getAnnotations();
    }

    public ExtendedCsdlComplexType setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedComplexType.setAnnotations(annotations);
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlProperty> getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(List<ExtendedCsdlProperty> extendedProperties) {
        this.extendedProperties = extendedProperties;
    }

    public List<ExtendedCsdlNavigationProperty> getExtendedNavigationProperties() {
        return extendedNavigationProperties;
    }

    public void setExtendedNavigationProperties(List<ExtendedCsdlNavigationProperty> extendedNavigationProperties) {
        this.extendedNavigationProperties = extendedNavigationProperties;
    }

    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }

    public void setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations = extendedAnnotations;
    }

    /**
     * 获取包装的CsdlComplexType实例
     */
    public CsdlComplexType asCsdlComplexType() {
        return wrappedComplexType;
    }

    // ==================== ExtendedCsdlElement 实现 ====================

    @Override
    public String getElementId() {
        if (elementId != null) {
            return elementId;
        }
        if (getName() != null) {
            return getName();
        }
        return "ComplexType_" + hashCode();
    }

    @Override
    public ExtendedCsdlComplexType setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlComplexType registerElement() {
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
