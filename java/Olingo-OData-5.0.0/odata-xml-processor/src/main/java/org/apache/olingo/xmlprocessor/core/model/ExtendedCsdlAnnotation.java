package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的CsdlAnnotation，支持依赖关系跟踪
 * 使用组合模式包装CsdlAnnotation，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理
 */
public class ExtendedCsdlAnnotation extends AbstractExtendedCsdlElement<CsdlAnnotation, ExtendedCsdlAnnotation> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlAnnotation() {
        super(new CsdlAnnotation());
    }

    /**
     * 从标准CsdlAnnotation创建ExtendedCsdlAnnotationRefactored
     */
    public static ExtendedCsdlAnnotation fromCsdlAnnotation(CsdlAnnotation source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlAnnotation extended = new ExtendedCsdlAnnotation();

        // 复制基本属性
        extended.setTerm(source.getTerm());
        extended.setQualifier(source.getQualifier());
        extended.setExpression(source.getExpression());

        // 注意：CsdlAnnotation本身没有annotations属性，直接返回
        return extended;
    }

    /**
     * 获取底层的CsdlAnnotation
     */
    public CsdlAnnotation asCsdlAnnotation() {
        return wrappedElement;
    }

    // ==================== 基类方法实现 ====================

    @Deprecated
    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        // CsdlAnnotation本身不包含annotations，返回空列表
        return java.util.Collections.emptyList();
    }

    @Deprecated
    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        // CsdlAnnotation本身不包含annotations，无操作
    }

    // ==================== CsdlAnnotation 方法委托 ====================

    public String getTerm() {
        return wrappedElement.getTerm();
    }

    public ExtendedCsdlAnnotation setTerm(FullQualifiedName term) {
        wrappedElement.setTerm(term.getFullQualifiedNameAsString());
        return this;
    }

    public ExtendedCsdlAnnotation setTerm(String term) {
        wrappedElement.setTerm(term);
        return this;
    }

    public String getQualifier() {
        return wrappedElement.getQualifier();
    }

    public ExtendedCsdlAnnotation setQualifier(String qualifier) {
        wrappedElement.setQualifier(qualifier);
        return this;
    }

    @Deprecated
    public CsdlExpression getExpression() {
        return wrappedElement.getExpression();
    }

    public ExtendedCsdlAnnotation setExpression(CsdlExpression expression) {
        wrappedElement.setExpression(expression);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getTerm() != null) {
            String qualifier = wrappedElement.getQualifier();
            return wrappedElement.getTerm() + 
                   (qualifier != null ? "#" + qualifier : "");
        }
        return "Annotation_" + super.hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        String term = getTerm();
        if (term != null) {
            try {
                return new FullQualifiedName(term);
            } catch (IllegalArgumentException e) {
                // 如果term不是合法的FQN格式，返回null
                return null;
            }
        }
        return null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.ANNOTATION_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getQualifier();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlAnnotation{" +
                "term='" + getTerm() + '\'' +
                ", qualifier='" + getQualifier() + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
