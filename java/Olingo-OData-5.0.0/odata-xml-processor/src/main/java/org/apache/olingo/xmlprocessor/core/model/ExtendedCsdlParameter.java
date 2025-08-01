package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的CsdlParameter，支持依赖关系跟踪
 * 使用组合模式包装CsdlParameter，保持内部数据联动
 */
public class ExtendedCsdlParameter implements ExtendedCsdlElement {
    
    private final CsdlParameter wrappedParameter;
    private String namespace;
    private String parentName;
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlAnnotation> extendedAnnotations = new ArrayList<>();

    /**
     * 构造函数
     */
    public ExtendedCsdlParameter() {
        this.wrappedParameter = new CsdlParameter();
    }

    /**
     * 从标准CsdlParameter创建ExtendedCsdlParameter
     */
    public static ExtendedCsdlParameter fromCsdlParameter(CsdlParameter source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlParameter extended = new ExtendedCsdlParameter();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setCollection(source.isCollection());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
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

    /**
     * 获取底层的CsdlParameter
     */
    public CsdlParameter asCsdlParameter() {
        return wrappedParameter;
    }

    // ==================== CsdlParameter 方法委托 ====================

    public String getName() {
        return wrappedParameter.getName();
    }

    public ExtendedCsdlParameter setName(String name) {
        wrappedParameter.setName(name);
        return this;
    }

    public String getType() {
        return wrappedParameter.getType();
    }

    public FullQualifiedName getTypeFQN() {
        return wrappedParameter.getTypeFQN();
    }

    public ExtendedCsdlParameter setType(String type) {
        wrappedParameter.setType(type);
        return this;
    }

    public ExtendedCsdlParameter setType(FullQualifiedName type) {
        wrappedParameter.setType(type);
        return this;
    }

    public boolean isCollection() {
        return wrappedParameter.isCollection();
    }

    public ExtendedCsdlParameter setCollection(boolean isCollection) {
        wrappedParameter.setCollection(isCollection);
        return this;
    }

    public Boolean isNullable() {
        return wrappedParameter.isNullable();
    }

    public ExtendedCsdlParameter setNullable(Boolean nullable) {
        wrappedParameter.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedParameter.getMaxLength();
    }

    public ExtendedCsdlParameter setMaxLength(Integer maxLength) {
        wrappedParameter.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedParameter.getPrecision();
    }

    public ExtendedCsdlParameter setPrecision(Integer precision) {
        wrappedParameter.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedParameter.getScale();
    }

    public ExtendedCsdlParameter setScale(Integer scale) {
        wrappedParameter.setScale(scale);
        return this;
    }

    public SRID getSrid() {
        return wrappedParameter.getSrid();
    }

    public ExtendedCsdlParameter setSrid(SRID srid) {
        wrappedParameter.setSrid(srid);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        // 返回不可修改的原始数据视图
        return wrappedParameter.getAnnotations();
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
    public ExtendedCsdlParameter addExtendedAnnotation(ExtendedCsdlAnnotation extendedAnnotation) {
        if (extendedAnnotation != null) {
            extendedAnnotations.add(extendedAnnotation);
            syncAnnotationsToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended注解列表，同时更新原始数据
     */
    public ExtendedCsdlParameter setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
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
        wrappedParameter.setAnnotations(csdlAnnotations);
    }

    @Deprecated
    public ExtendedCsdlParameter setAnnotations(List<CsdlAnnotation> annotations) {
        // 保留向后兼容，但建议使用setExtendedAnnotations
        wrappedParameter.setAnnotations(annotations);
        // 同步到Extended对象
        syncAnnotationsFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended注解
     */
    private void syncAnnotationsFromWrapped() {
        extendedAnnotations.clear();
        if (wrappedParameter.getAnnotations() != null) {
            for (CsdlAnnotation annotation : wrappedParameter.getAnnotations()) {
                ExtendedCsdlAnnotation extAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extendedAnnotations.add(extAnnotation);
            }
        }
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedParameter.getName() != null) {
            return wrappedParameter.getName();
        }
        return "Parameter_" + hashCode();
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
        return CsdlDependencyNode.DependencyType.PARAMETER_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    // ==================== 扩展属性 ====================

    public String getNamespace() {
        return namespace;
    }

    public ExtendedCsdlParameter setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getParentName() {
        return parentName;
    }

    public ExtendedCsdlParameter setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlParameter{" +
                "name='" + getName() + '\'' +
                ", type='" + getType() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", parentName='" + parentName + '\'' +
                '}';
    }
}
