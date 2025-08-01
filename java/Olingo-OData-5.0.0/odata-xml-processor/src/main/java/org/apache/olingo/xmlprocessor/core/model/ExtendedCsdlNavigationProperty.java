package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlNavigationProperty，增加依赖关系追踪功能
 * 内部包含Extended版本的子元素
 */
public class ExtendedCsdlNavigationProperty extends CsdlNavigationProperty implements ExtendedCsdlElement {

    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlNavigationProperty() {
        this.extendedAnnotations = new ArrayList<>();
    }

    /**
     * 从标准CsdlNavigationProperty创建ExtendedCsdlNavigationProperty
     * @param source 源CsdlNavigationProperty
     * @return ExtendedCsdlNavigationProperty实例
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

        // 复制引用约束
        if (source.getReferentialConstraints() != null) {
            extended.setReferentialConstraints(new ArrayList<>(source.getReferentialConstraints()));
        }

        // 复制OnDelete
        if (source.getOnDelete() != null) {
            extended.setOnDelete(source.getOnDelete());
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

    /**
     * 设置namespace
     * @param namespace 命名空间
     * @return 当前实例
     */
    public ExtendedCsdlNavigationProperty setNamespace(String namespace) {
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

    /**
     * 获取目标实体类型的namespace
     * @return 目标实体类型的namespace
     */
    public String getTargetEntityNamespace() {
        String type = getType();
        if (type != null) {
            // 移除Collection()包装
            String cleanTypeName = type.replaceAll("^Collection\\((.*)\\)$", "$1");

            // 提取namespace
            int lastDotIndex = cleanTypeName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return cleanTypeName.substring(0, lastDotIndex);
            }
        }
        return null;
    }

    /**
     * 获取目标实体类型名
     * @return 目标实体类型名
     */
    public String getTargetEntityTypeName() {
        String type = getType();
        if (type != null) {
            // 移除Collection()包装
            String cleanTypeName = type.replaceAll("^Collection\\((.*)\\)$", "$1");

            // 提取类型名
            int lastDotIndex = cleanTypeName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return cleanTypeName.substring(lastDotIndex + 1);
            }
            return cleanTypeName;
        }
        return null;
    }

    // ExtendedCsdlElement接口实现
    @Override
    public String getElementId() {
        return getName() != null ? getName() : "NavigationProperty_" + hashCode();
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
        return CsdlDependencyNode.DependencyType.NAVIGATION_PROPERTY;
    }
    
    @Override
    public String getElementPropertyName() {
        return getName();
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlNavigationProperty{name='%s', type='%s', namespace='%s', partner='%s'}",
                getName(), getType(), namespace, getPartner());
    }
}
