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
 * 内部包含Extended版本的所有子元素，保持对象树的一致性
 */
public class ExtendedCsdlComplexType extends CsdlComplexType implements ExtendedCsdlElement {
    
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
        this.elementId = null;
        initializeExtendedCollections();
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlComplexType(String elementId) {
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
        extended.setBaseType(source.getBaseType());

        // 转换Properties为Extended版本
        if (source.getProperties() != null) {
            List<ExtendedCsdlProperty> extendedProps = source.getProperties().stream()
                    .map(ExtendedCsdlProperty::fromCsdlProperty)
                    .collect(Collectors.toList());
            extended.setExtendedProperties(extendedProps);

            // 同时设置父类的properties以保持兼容性
            extended.setProperties(new ArrayList<>(source.getProperties()));
        }

        // 转换NavigationProperties为Extended版本
        if (source.getNavigationProperties() != null) {
            List<ExtendedCsdlNavigationProperty> extendedNavProps = source.getNavigationProperties().stream()
                    .map(ExtendedCsdlNavigationProperty::fromCsdlNavigationProperty)
                    .collect(Collectors.toList());
            extended.setExtendedNavigationProperties(extendedNavProps);

            // 同时设置父类的navigationProperties以保持兼容性
            extended.setNavigationProperties(new ArrayList<>(source.getNavigationProperties()));
        }

        // 转换Annotations为Extended版本
        if (source.getAnnotations() != null) {
            List<ExtendedCsdlAnnotation> extendedAnnotations = source.getAnnotations().stream()
                    .map(ExtendedCsdlAnnotation::fromCsdlAnnotation)
                    .collect(Collectors.toList());
            extended.setExtendedAnnotations(extendedAnnotations);

            // 同时设置父类的annotations以保持兼容性
            extended.setAnnotations(new ArrayList<>(source.getAnnotations()));
        }

        return extended;
    }

    private void initializeExtendedCollections() {
        this.extendedProperties = new ArrayList<>();
        this.extendedNavigationProperties = new ArrayList<>();
        this.extendedAnnotations = new ArrayList<>();
    }
    
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
    
    /**
     * Override setNamespace to return the correct type for fluent interface
     */
    @Override
    public ExtendedCsdlComplexType setNamespace(String namespace) {
        this.namespace = namespace;
        // 更新所有子元素的namespace
        updateChildNamespaces(namespace);
        return this;
    }

    /**
     * Get the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * 获取Extended版本的Properties
     * @return Extended Properties列表
     */
    public List<ExtendedCsdlProperty> getExtendedProperties() {
        return extendedProperties;
    }

    /**
     * 设置Extended版本的Properties
     * @param extendedProperties Extended Properties列表
     */
    public void setExtendedProperties(List<ExtendedCsdlProperty> extendedProperties) {
        this.extendedProperties = extendedProperties != null ? extendedProperties : new ArrayList<>();

        // 同步到父类的properties
        if (extendedProperties != null) {
            List<CsdlProperty> standardProperties = new ArrayList<>(extendedProperties);
            setProperties(standardProperties);
        }
    }

    /**
     * 添加Extended Property
     * @param property Extended Property
     */
    public void addExtendedProperty(ExtendedCsdlProperty property) {
        if (property != null) {
            if (extendedProperties == null) {
                extendedProperties = new ArrayList<>();
            }
            extendedProperties.add(property);

            // 同步到父类
            if (getProperties() == null) {
                setProperties(new ArrayList<>());
            }
            getProperties().add(property);
        }
    }

    /**
     * 获取Extended版本的NavigationProperties
     * @return Extended NavigationProperties列表
     */
    public List<ExtendedCsdlNavigationProperty> getExtendedNavigationProperties() {
        return extendedNavigationProperties;
    }

    /**
     * 设置Extended版本的NavigationProperties
     * @param extendedNavigationProperties Extended NavigationProperties列表
     */
    public void setExtendedNavigationProperties(List<ExtendedCsdlNavigationProperty> extendedNavigationProperties) {
        this.extendedNavigationProperties = extendedNavigationProperties != null ? extendedNavigationProperties : new ArrayList<>();

        // 同步到父类的navigationProperties
        if (extendedNavigationProperties != null) {
            List<CsdlNavigationProperty> standardNavProps = new ArrayList<>(extendedNavigationProperties);
            setNavigationProperties(standardNavProps);
        }
    }

    /**
     * 添加Extended NavigationProperty
     * @param navigationProperty Extended NavigationProperty
     */
    public void addExtendedNavigationProperty(ExtendedCsdlNavigationProperty navigationProperty) {
        if (navigationProperty != null) {
            if (extendedNavigationProperties == null) {
                extendedNavigationProperties = new ArrayList<>();
            }
            extendedNavigationProperties.add(navigationProperty);

            // 同步到父类
            if (getNavigationProperties() == null) {
                setNavigationProperties(new ArrayList<>());
            }
            getNavigationProperties().add(navigationProperty);
        }
    }

    /**
     * 获取Extended版本的Annotations
     * @return Extended Annotations列表
     */
    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }

    /**
     * 设置Extended版本的Annotations
     * @param extendedAnnotations Extended Annotations列表
     */
    public void setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations = extendedAnnotations != null ? extendedAnnotations : new ArrayList<>();

        // 同步到父类的annotations
        if (extendedAnnotations != null) {
            List<CsdlAnnotation> standardAnnotations = new ArrayList<>(extendedAnnotations);
            setAnnotations(standardAnnotations);
        }
    }

    /**
     * 添加Extended Annotation
     * @param annotation Extended Annotation
     */
    public void addExtendedAnnotation(ExtendedCsdlAnnotation annotation) {
        if (annotation != null) {
            if (extendedAnnotations == null) {
                extendedAnnotations = new ArrayList<>();
            }
            extendedAnnotations.add(annotation);

            // 同步到父类
            if (getAnnotations() == null) {
                setAnnotations(new ArrayList<>());
            }
            getAnnotations().add(annotation);
        }
    }

    /**
     * 更新所有子元素的namespace
     * @param namespace 新的namespace
     */
    private void updateChildNamespaces(String namespace) {
        if (extendedProperties != null) {
            extendedProperties.forEach(prop -> {
                if (prop instanceof ExtendedCsdlProperty) {
                    ((ExtendedCsdlProperty) prop).setNamespace(namespace);
                }
            });
        }

        if (extendedNavigationProperties != null) {
            extendedNavigationProperties.forEach(navProp -> {
                if (navProp instanceof ExtendedCsdlNavigationProperty) {
                    ((ExtendedCsdlNavigationProperty) navProp).setNamespace(namespace);
                }
            });
        }

        if (extendedAnnotations != null) {
            extendedAnnotations.forEach(annotation -> {
                if (annotation instanceof ExtendedCsdlAnnotation) {
                    ((ExtendedCsdlAnnotation) annotation).setNamespace(namespace);
                }
            });
        }
    }
    
    /**
     * 获取元素的完全限定名
     */
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (namespace != null && getName() != null) {
            return new FullQualifiedName(namespace, getName());
        }
        return null;
    }
    
    /**
     * 获取元素的依赖类型
     */
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.COMPLEX_TYPE;
    }
    
    /**
     * 获取元素的属性名称（如果适用）
     */
    @Override
    public String getElementPropertyName() {
        return null; // ComplexType通常不关联特定属性
    }
    
    /**
     * 注册扩展元素 - 用于依赖关系跟踪
     */
    @Override
    public ExtendedCsdlComplexType registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }

    /**
     * 获取所有依赖的类型名称列表（自定义方法）
     */
    public List<String> getDependencyTypeNames() {
        List<String> dependencies = new ArrayList<>();

        if (extendedProperties != null) {
            extendedProperties.forEach(prop -> {
                if (prop instanceof ExtendedCsdlElement) {
                    // 可以调用子元素的注册方法
                    ((ExtendedCsdlElement) prop).registerElement();
                }
            });
        }

        if (extendedNavigationProperties != null) {
            extendedNavigationProperties.forEach(navProp -> {
                if (navProp instanceof ExtendedCsdlElement) {
                    // 可以调用子元素的注册方法
                    ((ExtendedCsdlElement) navProp).registerElement();
                }
            });
        }

        return dependencies;
    }
}
