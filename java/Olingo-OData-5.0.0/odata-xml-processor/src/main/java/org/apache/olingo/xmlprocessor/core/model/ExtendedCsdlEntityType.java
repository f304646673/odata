package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlEntityType，增加依赖关系追踪功能
 * 使用组合模式包装CsdlEntityType，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlEntityType extends AbstractExtendedCsdlElement<CsdlEntityType, ExtendedCsdlEntityType> implements ExtendedCsdlElement {

    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlProperty> extendedProperties = new ArrayList<>();
    private final List<ExtendedCsdlNavigationProperty> extendedNavigationProperties = new ArrayList<>();

    /**
     * 构造函数
     */
    public ExtendedCsdlEntityType() {
        super(new CsdlEntityType());
    }

    /**
     * 从标准CsdlEntityType创建ExtendedCsdlEntityTypeRefactored
     */
    public static ExtendedCsdlEntityType fromCsdlEntityType(CsdlEntityType source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlEntityType extended = new ExtendedCsdlEntityType();

        // 复制基本属性
        extended.setName(source.getName());
        // 安全设置BaseType，避免null值导致的异常
        if (source.getBaseType() != null) {
            extended.setBaseType(source.getBaseType());
        }
        extended.setAbstract(source.isAbstract());
        extended.setHasStream(source.hasStream());
        extended.setOpenType(source.isOpenType());

        // 复制Key属性
        if (source.getKey() != null) {
            extended.setKey(new ArrayList<>(source.getKey()));
        }

        // 级联构建Properties
        if (source.getProperties() != null) {
            for (CsdlProperty prop : source.getProperties()) {
                ExtendedCsdlProperty extendedProp = ExtendedCsdlProperty.fromCsdlProperty(prop);
                extended.addExtendedProperty(extendedProp);
            }
        }

        // 级联构建NavigationProperties
        if (source.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : source.getNavigationProperties()) {
                ExtendedCsdlNavigationProperty extendedNavProp = ExtendedCsdlNavigationProperty.fromCsdlNavigationProperty(navProp);
                extended.addExtendedNavigationProperty(extendedNavProp);
            }
        }

        // 复制Annotations并转换为Extended版本（使用基类方法）
        if (source.getAnnotations() != null) {
            extended.setOriginalAnnotations(source.getAnnotations());
            extended.syncAnnotationsToExtended();
        }

        return extended;
    }

    /**
     * 获取底层的CsdlEntityType
     */
    public CsdlEntityType asCsdlEntityType() {
        return wrappedElement;
    }

    // ==================== 基类方法实现 ====================

    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        return wrappedElement.getAnnotations();
    }

    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        wrappedElement.setAnnotations(annotations);
    }

    // ==================== CsdlEntityType 方法委托 ====================
    
    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlEntityType setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getBaseType() {
        return wrappedElement.getBaseType();
    }

    public FullQualifiedName getBaseTypeFQN() {
        return wrappedElement.getBaseTypeFQN();
    }

    public ExtendedCsdlEntityType setBaseType(FullQualifiedName baseType) {
        wrappedElement.setBaseType(baseType);
        return this;
    }

    public ExtendedCsdlEntityType setBaseType(String baseType) {
        // 只有非空值才设置，避免FullQualifiedName构造函数的NullPointerException
        if (baseType != null && !baseType.trim().isEmpty()) {
            wrappedElement.setBaseType(baseType);
        }
        return this;
    }

    public boolean isAbstract() {
        return wrappedElement.isAbstract();
    }

    public ExtendedCsdlEntityType setAbstract(boolean isAbstract) {
        wrappedElement.setAbstract(isAbstract);
        return this;
    }

    public boolean isOpenType() {
        return wrappedElement.isOpenType();
    }

    public ExtendedCsdlEntityType setOpenType(boolean isOpenType) {
        wrappedElement.setOpenType(isOpenType);
        return this;
    }

    public boolean hasStream() {
        return wrappedElement.hasStream();
    }

    public ExtendedCsdlEntityType setHasStream(boolean hasStream) {
        wrappedElement.setHasStream(hasStream);
        return this;
    }

    @Deprecated
    public List<CsdlPropertyRef> getKey() {
        return wrappedElement.getKey();
    }

    public ExtendedCsdlEntityType setKey(List<CsdlPropertyRef> key) {
        wrappedElement.setKey(key);
        return this;
    }

    @Deprecated
    public List<CsdlProperty> getProperties() {
        // 返回不可修改的原始数据视图
        return wrappedElement.getProperties() != null ?
            Collections.unmodifiableList(wrappedElement.getProperties()) : null;
    }

    /**
     * 获取Extended属性列表
     */
    public List<ExtendedCsdlProperty> getExtendedProperties() {
        return new ArrayList<>(extendedProperties);
    }

    /**
     * 添加Extended属性，同时更新原始数据
     */
    public ExtendedCsdlEntityType addExtendedProperty(ExtendedCsdlProperty extendedProperty) {
        if (extendedProperty != null) {
            extendedProperties.add(extendedProperty);
            syncPropertiesToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended属性列表，同时更新原始数据
     */
    public ExtendedCsdlEntityType setExtendedProperties(List<ExtendedCsdlProperty> extendedProperties) {
        this.extendedProperties.clear();
        if (extendedProperties != null) {
            this.extendedProperties.addAll(extendedProperties);
        }
        syncPropertiesToWrapped();
        return this;
    }

    /**
     * 同步Extended属性到原始数据
     */
    private void syncPropertiesToWrapped() {
        List<CsdlProperty> csdlProps = new ArrayList<>();
        for (ExtendedCsdlProperty extProp : extendedProperties) {
            csdlProps.add(extProp.asCsdlProperty());
        }
        wrappedElement.setProperties(csdlProps);
    }

    public CsdlProperty getProperty(String name) {
        return wrappedElement.getProperty(name);
    }

    /**
     * 获取Extended属性
     */
    public ExtendedCsdlProperty getExtendedProperty(String name) {
        return extendedProperties.stream()
            .filter(p -> name.equals(p.getName()))
            .findFirst()
            .orElse(null);
    }

    @Deprecated
    public ExtendedCsdlEntityType setProperties(List<CsdlProperty> properties) {
        // 保留向后兼容，但建议使用setExtendedProperties
        wrappedElement.setProperties(properties);
        // 同步到Extended对象
        syncPropertiesFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended属性
     */
    private void syncPropertiesFromWrapped() {
        extendedProperties.clear();
        if (wrappedElement.getProperties() != null) {
            for (CsdlProperty prop : wrappedElement.getProperties()) {
                ExtendedCsdlProperty extProp = ExtendedCsdlProperty.fromCsdlProperty(prop);
                extendedProperties.add(extProp);
            }
        }
    }

    @Deprecated
    public List<CsdlNavigationProperty> getNavigationProperties() {
        // 返回不可修改的原始数据视图
        return wrappedElement.getNavigationProperties() != null ?
            Collections.unmodifiableList(wrappedElement.getNavigationProperties()) : null;
    }

    /**
     * 获取Extended导航属性列表
     */
    public List<ExtendedCsdlNavigationProperty> getExtendedNavigationProperties() {
        return new ArrayList<>(extendedNavigationProperties);
    }

    /**
     * 添加Extended导航属性，同时更新原始数据
     */
    public ExtendedCsdlEntityType addExtendedNavigationProperty(ExtendedCsdlNavigationProperty extendedNavProperty) {
        if (extendedNavProperty != null) {
            extendedNavigationProperties.add(extendedNavProperty);
            syncNavigationPropertiesToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended导航属性列表，同时更新原始数据
     */
    public ExtendedCsdlEntityType setExtendedNavigationProperties(List<ExtendedCsdlNavigationProperty> extendedNavProperties) {
        this.extendedNavigationProperties.clear();
        if (extendedNavProperties != null) {
            this.extendedNavigationProperties.addAll(extendedNavProperties);
        }
        syncNavigationPropertiesToWrapped();
        return this;
    }

    /**
     * 同步Extended导航属性到原始数据
     */
    private void syncNavigationPropertiesToWrapped() {
        List<CsdlNavigationProperty> csdlNavProps = new ArrayList<>();
        for (ExtendedCsdlNavigationProperty extNavProp : extendedNavigationProperties) {
            csdlNavProps.add(extNavProp.asCsdlNavigationProperty());
        }
        wrappedElement.setNavigationProperties(csdlNavProps);
    }

    public CsdlNavigationProperty getNavigationProperty(String name) {
        return wrappedElement.getNavigationProperty(name);
    }

    /**
     * 获取Extended导航属性
     */
    public ExtendedCsdlNavigationProperty getExtendedNavigationProperty(String name) {
        return extendedNavigationProperties.stream()
            .filter(p -> name.equals(p.getName()))
            .findFirst()
            .orElse(null);
    }

    @Deprecated
    public ExtendedCsdlEntityType setNavigationProperties(List<CsdlNavigationProperty> navigationProperties) {
        // 保留向后兼容，但建议使用setExtendedNavigationProperties
        wrappedElement.setNavigationProperties(navigationProperties);
        // 同步到Extended对象
        syncNavigationPropertiesFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended导航属性
     */
    private void syncNavigationPropertiesFromWrapped() {
        extendedNavigationProperties.clear();
        if (wrappedElement.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : wrappedElement.getNavigationProperties()) {
                ExtendedCsdlNavigationProperty extNavProp = ExtendedCsdlNavigationProperty.fromCsdlNavigationProperty(navProp);
                extendedNavigationProperties.add(extNavProp);
            }
        }
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "EntityType_" + super.hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (namespace != null && getName() != null) {
            return new FullQualifiedName(namespace, getName());
        }
        return null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.ENTITY_TYPE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public ExtendedCsdlEntityType setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlEntityType{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", baseType=" + getBaseTypeFQN() +
                '}';
    }
}
