package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlComplexType，支持依赖关系跟踪
 * 使用组合模式包装CsdlComplexType，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlComplexTypeRefactored extends AbstractExtendedCsdlElement<CsdlComplexType, ExtendedCsdlComplexTypeRefactored> implements ExtendedCsdlElement {
    
    private final String elementId;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlProperty> extendedProperties;
    private List<ExtendedCsdlNavigationProperty> extendedNavigationProperties;

    /**
     * 构造函数
     */
    public ExtendedCsdlComplexTypeRefactored() {
        super(new CsdlComplexType());
        this.elementId = null;
        initializeExtendedCollections();
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlComplexTypeRefactored(String elementId) {
        super(new CsdlComplexType());
        this.elementId = elementId;
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlComplexType创建ExtendedCsdlComplexTypeRefactored
     * @param source 源CsdlComplexType
     * @return ExtendedCsdlComplexTypeRefactored实例
     */
    public static ExtendedCsdlComplexTypeRefactored fromCsdlComplexType(CsdlComplexType source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlComplexTypeRefactored extended = new ExtendedCsdlComplexTypeRefactored();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setAbstract(source.isAbstract());
        extended.setOpenType(source.isOpenType());
        if (source.getBaseType() != null) {
            extended.setBaseType(source.getBaseType());
        }

        // 复制Properties
        if (source.getProperties() != null) {
            extended.setProperties(new ArrayList<>(source.getProperties()));
        }

        // 复制NavigationProperties
        if (source.getNavigationProperties() != null) {
            extended.setNavigationProperties(new ArrayList<>(source.getNavigationProperties()));
        }

        // 复制Annotations（使用基类方法）
        if (source.getAnnotations() != null) {
            extended.setOriginalAnnotations(source.getAnnotations());
            extended.syncAnnotationsToExtended();
        }

        return extended;
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedProperties = new ArrayList<>();
        this.extendedNavigationProperties = new ArrayList<>();
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

    // ==================== CsdlComplexType 方法委托 ====================
    
    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlComplexTypeRefactored setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getBaseType() {
        return wrappedElement.getBaseType();
    }

    public FullQualifiedName getBaseTypeFQN() {
        return wrappedElement.getBaseTypeFQN();
    }

    public ExtendedCsdlComplexTypeRefactored setBaseType(FullQualifiedName baseType) {
        wrappedElement.setBaseType(baseType);
        return this;
    }

    public ExtendedCsdlComplexTypeRefactored setBaseType(String baseType) {
        // 避免空值或空字符串导致异常
        if (baseType != null && !baseType.trim().isEmpty()) {
            wrappedElement.setBaseType(baseType);
        }
        return this;
    }

    public boolean isAbstract() {
        return wrappedElement.isAbstract();
    }

    public ExtendedCsdlComplexTypeRefactored setAbstract(boolean isAbstract) {
        wrappedElement.setAbstract(isAbstract);
        return this;
    }

    public boolean isOpenType() {
        return wrappedElement.isOpenType();
    }

    public ExtendedCsdlComplexTypeRefactored setOpenType(boolean isOpenType) {
        wrappedElement.setOpenType(isOpenType);
        return this;
    }

    public List<CsdlProperty> getProperties() {
        return wrappedElement.getProperties();
    }

    public ExtendedCsdlComplexTypeRefactored setProperties(List<CsdlProperty> properties) {
        wrappedElement.setProperties(properties);
        return this;
    }

    public List<CsdlNavigationProperty> getNavigationProperties() {
        return wrappedElement.getNavigationProperties();
    }

    public ExtendedCsdlComplexTypeRefactored setNavigationProperties(List<CsdlNavigationProperty> navigationProperties) {
        wrappedElement.setNavigationProperties(navigationProperties);
        return this;
    }

    /**
     * 获取底层的CsdlComplexType
     */
    public CsdlComplexType asCsdlComplexType() {
        return wrappedElement;
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (elementId != null) {
            return elementId;
        }
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "ComplexType_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.COMPLEX_TYPE;
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
    public ExtendedCsdlComplexTypeRefactored setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlComplexTypeRefactored{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", baseType=" + getBaseTypeFQN() +
                '}';
    }
}
