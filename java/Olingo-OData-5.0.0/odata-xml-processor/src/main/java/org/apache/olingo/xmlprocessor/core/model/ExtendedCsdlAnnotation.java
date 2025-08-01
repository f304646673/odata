package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlAnnotation，增加依赖关系追踪功能
 */
public class ExtendedCsdlAnnotation extends CsdlAnnotation implements ExtendedCsdlElement {
    
    private String namespace;
    private String elementId;

    /**
     * 构造函数
     */
    public ExtendedCsdlAnnotation() {
    }
    
    /**
     * 构造函数，使用指定的elementId
     * @param elementId 元素唯一标识
     */
    public ExtendedCsdlAnnotation(String elementId) {
        this.elementId = elementId;
    }

    /**
     * 从标准CsdlAnnotation创建ExtendedCsdlAnnotation
     * @param source 源CsdlAnnotation
     * @return ExtendedCsdlAnnotation实例
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

        // 复制嵌套Annotations（如果支持）
        if (source.getAnnotations() != null) {
            extended.setAnnotations(source.getAnnotations());
        }

        return extended;
    }
    
    /**
     * 设置namespace
     * @param namespace 命名空间
     * @return 当前实例
     */
    public ExtendedCsdlAnnotation setNamespace(String namespace) {
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
     * 获取Term的namespace
     * @return Term的namespace
     */
    public String getTermNamespace() {
        String term = getTerm();
        if (term != null) {
            int lastDotIndex = term.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return term.substring(0, lastDotIndex);
            }
        }
        return null;
    }

    /**
     * 获取Term的名称
     * @return Term的名称
     */
    public String getTermName() {
        String term = getTerm();
        if (term != null) {
            int lastDotIndex = term.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return term.substring(lastDotIndex + 1);
            }
            return term;
        }
        return null;
    }

    // ExtendedCsdlElement接口实现
    @Override
    public String getElementId() {
        if (elementId != null) {
            return elementId;
        }
        String term = getTerm();
        String qualifier = getQualifier();
        if (term != null) {
            return qualifier != null ? term + "#" + qualifier : term;
        }
        return "Annotation_" + hashCode();
    }
    
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        String termNamespace = getTermNamespace();
        String termName = getTermName();
        if (termNamespace != null && termName != null) {
            return new FullQualifiedName(termNamespace, termName);
        }
        return null;
    }
    
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE; // 使用TYPE_REFERENCE代替ANNOTATION
    }
    
    @Override
    public String getElementPropertyName() {
        return getTerm();
    }
    
    @Override
    public String toString() {
        return String.format("ExtendedCsdlAnnotation{term='%s', qualifier='%s', namespace='%s'}",
                getTerm(), getQualifier(), namespace);
    }
}
