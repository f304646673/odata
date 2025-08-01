package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlFunction，支持依赖关系跟踪
 * 使用组合模式包装CsdlFunction，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlFunction extends AbstractExtendedCsdlElement<CsdlFunction, ExtendedCsdlFunction> implements ExtendedCsdlElement {
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlParameter> extendedParameters = new ArrayList<>();
    private ExtendedCsdlReturnType extendedReturnType;

    /**
     * 构造函数
     */
    public ExtendedCsdlFunction() {
        super(new CsdlFunction());
    }

    /**
     * 从标准CsdlFunction创建ExtendedCsdlFunctionRefactored
     */
    public static ExtendedCsdlFunction fromCsdlFunction(CsdlFunction source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlFunction extended = new ExtendedCsdlFunction();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setBound(source.isBound());
        extended.setComposable(source.isComposable());
        extended.setEntitySetPath(source.getEntitySetPath());

        // 级联构建Parameters
        if (source.getParameters() != null) {
            for (CsdlParameter parameter : source.getParameters()) {
                ExtendedCsdlParameter extendedParameter = ExtendedCsdlParameter.fromCsdlParameter(parameter);
                extended.addExtendedParameter(extendedParameter);
            }
        }

        // 级联构建ReturnType
        if (source.getReturnType() != null) {
            ExtendedCsdlReturnType extendedReturnType = ExtendedCsdlReturnType.fromCsdlReturnType(source.getReturnType());
            extended.setExtendedReturnType(extendedReturnType);
        }

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlFunction
     */
    public CsdlFunction asCsdlFunction() {
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

    // ==================== CsdlFunction 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlFunction setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public boolean isBound() {
        return wrappedElement.isBound();
    }

    public ExtendedCsdlFunction setBound(boolean isBound) {
        wrappedElement.setBound(isBound);
        return this;
    }

    public boolean isComposable() {
        return wrappedElement.isComposable();
    }

    public ExtendedCsdlFunction setComposable(boolean isComposable) {
        wrappedElement.setComposable(isComposable);
        return this;
    }

    public String getEntitySetPath() {
        return wrappedElement.getEntitySetPath();
    }

    public ExtendedCsdlFunction setEntitySetPath(String entitySetPath) {
        wrappedElement.setEntitySetPath(entitySetPath);
        return this;
    }

    @Deprecated
    public List<CsdlParameter> getParameters() {
        return wrappedElement.getParameters();
    }

    public ExtendedCsdlFunction setParameters(List<CsdlParameter> parameters) {
        wrappedElement.setParameters(parameters);
        syncParametersFromWrapped();
        return this;
    }

    @Deprecated
    public CsdlReturnType getReturnType() {
        return wrappedElement.getReturnType();
    }

    public ExtendedCsdlFunction setReturnType(CsdlReturnType returnType) {
        wrappedElement.setReturnType(returnType);
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlParameter> getExtendedParameters() {
        return new ArrayList<>(extendedParameters);
    }

    public ExtendedCsdlFunction addExtendedParameter(ExtendedCsdlParameter parameter) {
        if (parameter != null) {
            extendedParameters.add(parameter);
            syncParametersToWrapped();
        }
        return this;
    }

    public ExtendedCsdlFunction setExtendedParameters(List<ExtendedCsdlParameter> parameters) {
        extendedParameters.clear();
        if (parameters != null) {
            extendedParameters.addAll(parameters);
        }
        syncParametersToWrapped();
        return this;
    }

    public ExtendedCsdlReturnType getExtendedReturnType() {
        return extendedReturnType;
    }

    public ExtendedCsdlFunction setExtendedReturnType(ExtendedCsdlReturnType returnType) {
        this.extendedReturnType = returnType;
        if (returnType != null) {
            wrappedElement.setReturnType(returnType.asCsdlReturnType());
        } else {
            wrappedElement.setReturnType(null);
        }
        return this;
    }

    // ==================== 同步方法 ====================

    private void syncParametersToWrapped() {
        List<CsdlParameter> csdlParameters = new ArrayList<>();
        for (ExtendedCsdlParameter extParam : extendedParameters) {
            csdlParameters.add(extParam.asCsdlParameter());
        }
        wrappedElement.setParameters(csdlParameters);
    }

    private void syncParametersFromWrapped() {
        extendedParameters.clear();
        if (wrappedElement.getParameters() != null) {
            for (CsdlParameter param : wrappedElement.getParameters()) {
                ExtendedCsdlParameter extParam = ExtendedCsdlParameter.fromCsdlParameter(param);
                extendedParameters.add(extParam);
            }
        }
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "Function_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.FUNCTION_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlFunction{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", isBound=" + isBound() +
                ", isComposable=" + isComposable() +
                ", parametersCount=" + extendedParameters.size() +
                '}';
    }
}
