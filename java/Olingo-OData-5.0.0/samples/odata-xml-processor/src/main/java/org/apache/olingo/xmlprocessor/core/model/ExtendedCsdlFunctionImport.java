package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的CsdlFunctionImport，支持依赖关系跟踪
 * 使用组合模式包装CsdlFunctionImport，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlFunctionImport extends AbstractExtendedCsdlElement<CsdlFunctionImport, ExtendedCsdlFunctionImport> implements ExtendedCsdlElement {

    /**
     * 构造函数
     */
    public ExtendedCsdlFunctionImport() {
        super(new CsdlFunctionImport());
    }

    /**
     * 从标准CsdlFunctionImport创建ExtendedCsdlFunctionImportRefactored
     */
    public static ExtendedCsdlFunctionImport fromCsdlFunctionImport(CsdlFunctionImport source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlFunctionImport extended = new ExtendedCsdlFunctionImport();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setFunction(source.getFunction());
        extended.setEntitySet(source.getEntitySet());
        extended.setIncludeInServiceDocument(source.isIncludeInServiceDocument());

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlFunctionImport
     */
    public CsdlFunctionImport asCsdlFunctionImport() {
        return wrappedElement;
    }

    // ==================== 基类方法实现 ====================

    @Deprecated
    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        return wrappedElement.getAnnotations();
    }

    @Deprecated
    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        wrappedElement.setAnnotations(annotations);
    }

    // ==================== CsdlFunctionImport 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlFunctionImport setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getFunction() {
        return wrappedElement.getFunction();
    }

    public FullQualifiedName getFunctionFQN() {
        return wrappedElement.getFunctionFQN();
    }

    public ExtendedCsdlFunctionImport setFunction(String function) {
        wrappedElement.setFunction(function);
        return this;
    }

    public ExtendedCsdlFunctionImport setFunction(FullQualifiedName functionFQN) {
        wrappedElement.setFunction(functionFQN);
        return this;
    }

    public String getEntitySet() {
        return wrappedElement.getEntitySet();
    }

    public ExtendedCsdlFunctionImport setEntitySet(String entitySet) {
        wrappedElement.setEntitySet(entitySet);
        return this;
    }

    public boolean isIncludeInServiceDocument() {
        return wrappedElement.isIncludeInServiceDocument();
    }

    public ExtendedCsdlFunctionImport setIncludeInServiceDocument(boolean includeInServiceDocument) {
        wrappedElement.setIncludeInServiceDocument(includeInServiceDocument);
        return this;
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "FunctionImport_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.FUNCTION_IMPORT_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlFunctionImport{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", function='" + getFunction() + '\'' +
                ", entitySet='" + getEntitySet() + '\'' +
                ", includeInServiceDocument=" + isIncludeInServiceDocument() +
                '}';
    }
}
