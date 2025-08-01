package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的CsdlAction，支持依赖关系跟踪
 * 使用组合模式包装CsdlAction，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlAction extends AbstractExtendedCsdlElement<CsdlAction, ExtendedCsdlAction> implements ExtendedCsdlElement {
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlParameter> extendedParameters = new ArrayList<>();
    private ExtendedCsdlReturnType extendedReturnType;

    /**
     * 构造函数
     */
    public ExtendedCsdlAction() {
        super(new CsdlAction());
    }

    /**
     * 从标准CsdlAction创建ExtendedCsdlActionRefactored
     */
    public static ExtendedCsdlAction fromCsdlAction(CsdlAction source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlAction extended = new ExtendedCsdlAction();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setBound(source.isBound());
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
     * 获取底层的CsdlAction
     */
    public CsdlAction asCsdlAction() {
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

    // ==================== CsdlAction 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlAction setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public boolean isBound() {
        return wrappedElement.isBound();
    }

    public ExtendedCsdlAction setBound(boolean isBound) {
        wrappedElement.setBound(isBound);
        return this;
    }

    public String getEntitySetPath() {
        return wrappedElement.getEntitySetPath();
    }

    public ExtendedCsdlAction setEntitySetPath(String entitySetPath) {
        wrappedElement.setEntitySetPath(entitySetPath);
        return this;
    }

    @Deprecated
    public List<CsdlParameter> getParameters() {
        return wrappedElement.getParameters();
    }

    public ExtendedCsdlAction setParameters(List<CsdlParameter> parameters) {
        wrappedElement.setParameters(parameters);
        syncParametersFromWrapped();
        return this;
    }

    @Deprecated
    public CsdlReturnType getReturnType() {
        return wrappedElement.getReturnType();
    }

    public ExtendedCsdlAction setReturnType(CsdlReturnType returnType) {
        wrappedElement.setReturnType(returnType);
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlParameter> getExtendedParameters() {
        return new ArrayList<>(extendedParameters);
    }

    public ExtendedCsdlAction addExtendedParameter(ExtendedCsdlParameter parameter) {
        if (parameter != null) {
            extendedParameters.add(parameter);
            syncParametersToWrapped();
        }
        return this;
    }

    public ExtendedCsdlAction setExtendedParameters(List<ExtendedCsdlParameter> parameters) {
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

    public ExtendedCsdlAction setExtendedReturnType(ExtendedCsdlReturnType returnType) {
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
        return "Action_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.ACTION_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlAction{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", isBound=" + isBound() +
                ", parametersCount=" + extendedParameters.size() +
                '}';
    }
}
