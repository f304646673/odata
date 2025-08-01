package org.apache.olingo.xmlprocessor.core.model;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlProperty，增加依赖关系追踪功能
 * 内部包含Extended版本的子元素
 */
public class ExtendedCsdlProperty extends CsdlProperty implements ExtendedCsdlElement {

    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlProperty() {
        this.extendedAnnotations = new ArrayList<>();
    }
    
    /**
     * 从标准CsdlProperty创建ExtendedCsdlProperty
     * @param source 源CsdlProperty
     * @return ExtendedCsdlProperty实例
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
        extended.setSrid(source.getSrid());
        extended.setUnicode(source.isUnicode());
        extended.setDefaultValue(source.getDefaultValue());

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

    /**
     * 设置namespace
     * @param namespace 命名空间
     * @return 当前实例
     */
    public ExtendedCsdlProperty setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * 获取namespace
     * @return 命名空间
     */
    public String getNamespace() {
        return namespace;
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

    // ExtendedCsdlElement接口实现
    @Override
    public String getElementId() {
        return getName() != null ? getName() : "Property_" + hashCode();
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
        return CsdlDependencyNode.DependencyType.PROPERTY;
    }
    
    @Override
    public String getElementPropertyName() {
        return getName();
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlProperty{name='%s', type='%s', namespace='%s'}",
                getName(), getType(), namespace);
    }
}
