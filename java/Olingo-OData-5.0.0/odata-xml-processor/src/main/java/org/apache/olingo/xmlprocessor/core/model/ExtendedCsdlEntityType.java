package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 */
public class ExtendedCsdlEntityType implements ExtendedCsdlElement {

    private final CsdlEntityType wrappedEntityType;
    private String namespace;

    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlProperty> extendedProperties = new ArrayList<>();
    private final List<ExtendedCsdlNavigationProperty> extendedNavigationProperties = new ArrayList<>();
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlEntityType() {
        this.wrappedEntityType = new CsdlEntityType();
        initializeExtendedCollections();
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
    }

    /**
     * 从标准CsdlEntityType创建ExtendedCsdlEntityType
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
            extended.setKey(new ArrayList<CsdlPropertyRef>(source.getKey()));
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
     * 获取底层的CsdlEntityType
     */
    public CsdlEntityType asCsdlEntityType() {
        return wrappedEntityType;
    }

    // ==================== CsdlEntityType 方法委托 ====================
    
    public String getName() {
        return wrappedEntityType.getName();
    }

    public ExtendedCsdlEntityType setName(String name) {
        wrappedEntityType.setName(name);
        return this;
    }

    public String getBaseType() {
        return wrappedEntityType.getBaseType();
    }

    public FullQualifiedName getBaseTypeFQN() {
        return wrappedEntityType.getBaseTypeFQN();
    }

    public ExtendedCsdlEntityType setBaseType(FullQualifiedName baseType) {
        wrappedEntityType.setBaseType(baseType);
        return this;
    }

    public ExtendedCsdlEntityType setBaseType(String baseType) {
        // 只有非空值才设置，避免FullQualifiedName构造函数的NullPointerException
        if (baseType != null && !baseType.trim().isEmpty()) {
            wrappedEntityType.setBaseType(baseType);
        }
        return this;
    }

    public boolean isAbstract() {
        return wrappedEntityType.isAbstract();
    }

    public ExtendedCsdlEntityType setAbstract(boolean isAbstract) {
        wrappedEntityType.setAbstract(isAbstract);
        return this;
    }

    public boolean isOpenType() {
        return wrappedEntityType.isOpenType();
    }

    public ExtendedCsdlEntityType setOpenType(boolean isOpenType) {
        wrappedEntityType.setOpenType(isOpenType);
        return this;
    }

    public boolean hasStream() {
        return wrappedEntityType.hasStream();
    }

    public ExtendedCsdlEntityType setHasStream(boolean hasStream) {
        wrappedEntityType.setHasStream(hasStream);
        return this;
    }

    public List<CsdlPropertyRef> getKey() {
        return wrappedEntityType.getKey();
    }

    public ExtendedCsdlEntityType setKey(List<CsdlPropertyRef> key) {
        wrappedEntityType.setKey(key);
        return this;
    }

    public List<CsdlProperty> getProperties() {
        // 返回不可修改的原始数据视图
        return wrappedEntityType.getProperties() != null ?
            Collections.unmodifiableList(wrappedEntityType.getProperties()) : null;
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
        wrappedEntityType.setProperties(csdlProps);
    }

    public CsdlProperty getProperty(String name) {
        return wrappedEntityType.getProperty(name);
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
        wrappedEntityType.setProperties(properties);
        // 同步到Extended对象
        syncPropertiesFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended属性
     */
    private void syncPropertiesFromWrapped() {
        extendedProperties.clear();
        if (wrappedEntityType.getProperties() != null) {
            for (CsdlProperty prop : wrappedEntityType.getProperties()) {
                ExtendedCsdlProperty extProp = ExtendedCsdlProperty.fromCsdlProperty(prop);
                extendedProperties.add(extProp);
            }
        }
    }

    public List<CsdlNavigationProperty> getNavigationProperties() {
        // 返回不可修改的原始数据视图
        return wrappedEntityType.getNavigationProperties() != null ?
            Collections.unmodifiableList(wrappedEntityType.getNavigationProperties()) : null;
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
        wrappedEntityType.setNavigationProperties(csdlNavProps);
    }

    public CsdlNavigationProperty getNavigationProperty(String name) {
        return wrappedEntityType.getNavigationProperty(name);
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
        wrappedEntityType.setNavigationProperties(navigationProperties);
        // 同步到Extended对象
        syncNavigationPropertiesFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended导航属性
     */
    private void syncNavigationPropertiesFromWrapped() {
        extendedNavigationProperties.clear();
        if (wrappedEntityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : wrappedEntityType.getNavigationProperties()) {
                ExtendedCsdlNavigationProperty extNavProp = ExtendedCsdlNavigationProperty.fromCsdlNavigationProperty(navProp);
                extendedNavigationProperties.add(extNavProp);
            }
        }
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedEntityType.getAnnotations();
    }

    public ExtendedCsdlEntityType setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedEntityType.setAnnotations(annotations);
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
        wrappedEntityType.setAnnotations(annotations);
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedEntityType.getName() != null) {
            return wrappedEntityType.getName();
        }
        return "EntityType_" + hashCode();
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

    public String getNamespace() {
        return namespace;
    }

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
