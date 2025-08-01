package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlTerm，支持依赖关系跟踪
 * 使用组合模式包装CsdlTerm，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlTermRefactored extends AbstractExtendedCsdlElement<CsdlTerm, ExtendedCsdlTermRefactored> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlTermRefactored() {
        super(new CsdlTerm());
    }

    /**
     * 从标准CsdlTerm创建ExtendedCsdlTermRefactored
     */
    public static ExtendedCsdlTermRefactored fromCsdlTerm(CsdlTerm source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlTermRefactored extended = new ExtendedCsdlTermRefactored();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setType(source.getType());
        extended.setBaseTerm(source.getBaseTerm());
        extended.setAppliesTo(source.getAppliesTo());
        extended.setNullable(source.isNullable());
        extended.setMaxLength(source.getMaxLength());
        extended.setPrecision(source.getPrecision());
        extended.setScale(source.getScale());
        extended.setDefaultValue(source.getDefaultValue());

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlTerm
     */
    public CsdlTerm asCsdlTerm() {
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

    // ==================== CsdlTerm 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlTermRefactored setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getType() {
        return wrappedElement.getType();
    }

    public ExtendedCsdlTermRefactored setType(String type) {
        wrappedElement.setType(type);
        return this;
    }

    public String getBaseTerm() {
        return wrappedElement.getBaseTerm();
    }

    public ExtendedCsdlTermRefactored setBaseTerm(String baseTerm) {
        wrappedElement.setBaseTerm(baseTerm);
        return this;
    }

    public List<String> getAppliesTo() {
        return wrappedElement.getAppliesTo();
    }

    public ExtendedCsdlTermRefactored setAppliesTo(List<String> appliesTo) {
        wrappedElement.setAppliesTo(appliesTo);
        return this;
    }

    public Boolean isNullable() {
        return wrappedElement.isNullable();
    }

    public ExtendedCsdlTermRefactored setNullable(Boolean nullable) {
        wrappedElement.setNullable(nullable);
        return this;
    }

    public Integer getMaxLength() {
        return wrappedElement.getMaxLength();
    }

    public ExtendedCsdlTermRefactored setMaxLength(Integer maxLength) {
        wrappedElement.setMaxLength(maxLength);
        return this;
    }

    public Integer getPrecision() {
        return wrappedElement.getPrecision();
    }

    public ExtendedCsdlTermRefactored setPrecision(Integer precision) {
        wrappedElement.setPrecision(precision);
        return this;
    }

    public Integer getScale() {
        return wrappedElement.getScale();
    }

    public ExtendedCsdlTermRefactored setScale(Integer scale) {
        wrappedElement.setScale(scale);
        return this;
    }

    public String getDefaultValue() {
        return wrappedElement.getDefaultValue();
    }

    public ExtendedCsdlTermRefactored setDefaultValue(String defaultValue) {
        wrappedElement.setDefaultValue(defaultValue);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "Term_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.TERM_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlTermRefactored{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", type='" + getType() + '\'' +
                ", baseTerm='" + getBaseTerm() + '\'' +
                ", nullable=" + isNullable() +
                '}';
    }
}
