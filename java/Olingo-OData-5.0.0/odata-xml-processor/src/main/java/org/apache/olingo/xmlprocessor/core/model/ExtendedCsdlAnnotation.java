package org.apache.olingo.xmlprocessor.core.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlAnnotation，增加依赖关系追踪功能
 * 使用组合模式包装CsdlAnnotation，保持内部数据联动
 */
public class ExtendedCsdlAnnotation implements ExtendedCsdlElement {

    private final CsdlAnnotation wrappedAnnotation;
    private String namespace;

    /**
     * 构造函数
     */
    public ExtendedCsdlAnnotation() {
        this.wrappedAnnotation = new CsdlAnnotation();
    }

    /**
     * 从标准CsdlAnnotation创建ExtendedCsdlAnnotation
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

        return extended;
    }

    /**
     * 获取底层的CsdlAnnotation
     */
    public CsdlAnnotation asCsdlAnnotation() {
        return wrappedAnnotation;
    }

    // ==================== CsdlAnnotation 方法委托 ====================
    
    public String getTerm() {
        return wrappedAnnotation.getTerm();
    }

    public ExtendedCsdlAnnotation setTerm(FullQualifiedName term) {
        wrappedAnnotation.setTerm(term.getFullQualifiedNameAsString());
        return this;
    }

    public ExtendedCsdlAnnotation setTerm(String term) {
        wrappedAnnotation.setTerm(term);
        return this;
    }

    public String getQualifier() {
        return wrappedAnnotation.getQualifier();
    }

    public ExtendedCsdlAnnotation setQualifier(String qualifier) {
        wrappedAnnotation.setQualifier(qualifier);
        return this;
    }

    public CsdlExpression getExpression() {
        return wrappedAnnotation.getExpression();
    }

    public ExtendedCsdlAnnotation setExpression(CsdlExpression expression) {
        wrappedAnnotation.setExpression(expression);
        return this;
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedAnnotation.getTerm() != null) {
            String qualifier = wrappedAnnotation.getQualifier();
            return wrappedAnnotation.getTerm() + 
                   (qualifier != null ? "#" + qualifier : "");
        }
        return "Annotation_" + hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        String term = getTerm();
        return term != null ? new FullQualifiedName(term) : null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.ANNOTATION_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getQualifier();
    }

    public String getNamespace() {
        return namespace;
    }

    public ExtendedCsdlAnnotation setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlAnnotation{" +
                "term=" + getTerm() +
                ", qualifier='" + getQualifier() + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
