package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的CsdlAction，支持依赖关系跟踪
 * 使用组合模式包装CsdlAction，保持内部数据联动
 */
public class ExtendedCsdlAction implements ExtendedCsdlElement {
    
    private final CsdlAction wrappedAction;
    private String namespace;
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlParameter> extendedParameters = new ArrayList<>();
    private final List<ExtendedCsdlAnnotation> extendedAnnotations = new ArrayList<>();
    private ExtendedCsdlReturnType extendedReturnType;

    /**
     * 构造函数
     */
    public ExtendedCsdlAction() {
        this.wrappedAction = new CsdlAction();
    }

    /**
     * 从标准CsdlAction创建ExtendedCsdlAction
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
     * 获取底层的CsdlAction
     */
    public CsdlAction asCsdlAction() {
        return wrappedAction;
    }

    // ==================== CsdlAction 方法委托 ====================

    public String getName() {
        return wrappedAction.getName();
    }

    public ExtendedCsdlAction setName(String name) {
        wrappedAction.setName(name);
        return this;
    }

    public boolean isBound() {
        return wrappedAction.isBound();
    }

    public ExtendedCsdlAction setBound(boolean isBound) {
        wrappedAction.setBound(isBound);
        return this;
    }

    public String getEntitySetPath() {
        return wrappedAction.getEntitySetPath();
    }

    public ExtendedCsdlAction setEntitySetPath(String entitySetPath) {
        wrappedAction.setEntitySetPath(entitySetPath);
        return this;
    }

    public List<CsdlParameter> getParameters() {
        // 返回不可修改的原始数据视图
        return wrappedAction.getParameters();
    }

    /**
     * 获取Extended参数列表
     */
    public List<ExtendedCsdlParameter> getExtendedParameters() {
        return new ArrayList<>(extendedParameters);
    }

    /**
     * 添加Extended参数，同时更新原始数据
     */
    public ExtendedCsdlAction addExtendedParameter(ExtendedCsdlParameter extendedParameter) {
        if (extendedParameter != null) {
            extendedParameters.add(extendedParameter);
            syncParametersToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended参数列表，同时更新原始数据
     */
    public ExtendedCsdlAction setExtendedParameters(List<ExtendedCsdlParameter> extendedParameters) {
        this.extendedParameters.clear();
        if (extendedParameters != null) {
            this.extendedParameters.addAll(extendedParameters);
        }
        syncParametersToWrapped();
        return this;
    }

    /**
     * 同步Extended参数到原始数据
     */
    private void syncParametersToWrapped() {
        List<CsdlParameter> csdlParameters = new ArrayList<>();
        for (ExtendedCsdlParameter extParameter : extendedParameters) {
            csdlParameters.add(extParameter.asCsdlParameter());
        }
        wrappedAction.setParameters(csdlParameters);
    }

    public CsdlParameter getParameter(String name) {
        return wrappedAction.getParameter(name);
    }

    @Deprecated
    public ExtendedCsdlAction setParameters(List<CsdlParameter> parameters) {
        // 保留向后兼容，但建议使用setExtendedParameters
        wrappedAction.setParameters(parameters);
        // 同步到Extended对象
        syncParametersFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended参数
     */
    private void syncParametersFromWrapped() {
        extendedParameters.clear();
        if (wrappedAction.getParameters() != null) {
            for (CsdlParameter parameter : wrappedAction.getParameters()) {
                ExtendedCsdlParameter extParameter = ExtendedCsdlParameter.fromCsdlParameter(parameter);
                extendedParameters.add(extParameter);
            }
        }
    }

    public CsdlReturnType getReturnType() {
        return wrappedAction.getReturnType();
    }

    /**
     * 获取Extended返回类型
     */
    public ExtendedCsdlReturnType getExtendedReturnType() {
        return extendedReturnType;
    }

    /**
     * 设置Extended返回类型，同时更新原始数据
     */
    public ExtendedCsdlAction setExtendedReturnType(ExtendedCsdlReturnType extendedReturnType) {
        this.extendedReturnType = extendedReturnType;
        if (extendedReturnType != null) {
            wrappedAction.setReturnType(extendedReturnType.asCsdlReturnType());
        } else {
            wrappedAction.setReturnType(null);
        }
        return this;
    }

    @Deprecated
    public ExtendedCsdlAction setReturnType(CsdlReturnType returnType) {
        // 保留向后兼容，但建议使用setExtendedReturnType
        wrappedAction.setReturnType(returnType);
        // 同步到Extended对象
        if (returnType != null) {
            this.extendedReturnType = ExtendedCsdlReturnType.fromCsdlReturnType(returnType);
        } else {
            this.extendedReturnType = null;
        }
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        // 返回不可修改的原始数据视图
        return wrappedAction.getAnnotations();
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
    public ExtendedCsdlAction addExtendedAnnotation(ExtendedCsdlAnnotation extendedAnnotation) {
        if (extendedAnnotation != null) {
            extendedAnnotations.add(extendedAnnotation);
            syncAnnotationsToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended注解列表，同时更新原始数据
     */
    public ExtendedCsdlAction setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
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
        wrappedAction.setAnnotations(csdlAnnotations);
    }

    @Deprecated
    public ExtendedCsdlAction setAnnotations(List<CsdlAnnotation> annotations) {
        // 保留向后兼容，但建议使用setExtendedAnnotations
        wrappedAction.setAnnotations(annotations);
        // 同步到Extended对象
        syncAnnotationsFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended注解
     */
    private void syncAnnotationsFromWrapped() {
        extendedAnnotations.clear();
        if (wrappedAction.getAnnotations() != null) {
            for (CsdlAnnotation annotation : wrappedAction.getAnnotations()) {
                ExtendedCsdlAnnotation extAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                extendedAnnotations.add(extAnnotation);
            }
        }
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedAction.getName() != null) {
            return wrappedAction.getName();
        }
        return "Action_" + hashCode();
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

    // ==================== 扩展属性 ====================

    public String getNamespace() {
        return namespace;
    }

    public ExtendedCsdlAction setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlAction{" +
                "name='" + getName() + '\'' +
                ", isBound=" + isBound() +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
