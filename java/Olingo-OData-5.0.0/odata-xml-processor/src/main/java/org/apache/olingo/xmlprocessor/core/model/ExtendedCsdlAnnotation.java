package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CSDL注解
 * 继承自CsdlAnnotation，增加依赖跟踪和扩展功能
 */
public class ExtendedCsdlAnnotation extends CsdlAnnotation implements ExtendedCsdlElement {
    
    private String namespace;
    
    /**
     * 默认构造函数
     */
    public ExtendedCsdlAnnotation() {
        super();
    }
    
    @Override
    public String getElementId() {
        if (getTerm() != null) {
            return getTerm();
        }
        return "Annotation_" + hashCode();
    }
    
    // 为ExtendedCsdlElement接口提供getName方法实现
    public String getName() {
        return getTerm(); // Annotation的名称是其term
    }
    
    @Override
    public ExtendedCsdlAnnotation setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlAnnotation registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }
    
    /**
     * 获取元素的完全限定名（如果适用）
     */
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getTerm());
    }
    
    /**
     * 获取元素的依赖类型
     */
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
    }
    
    /**
     * 获取元素相关的属性名（如果适用）
     */
    @Override
    public String getElementPropertyName() {
        return null; // Annotation通常不关联特定属性
    }
    
    @Override
    public String toString() {
        return "ExtendedCsdlAnnotation{" +
                "term='" + getTerm() + '\'' +
                ", namespace='" + getNamespace() + '\'' +
                ", qualifier='" + getQualifier() + '\'' +
                ", hasExpression=" + (getExpression() != null) +
                '}';
    }
}
