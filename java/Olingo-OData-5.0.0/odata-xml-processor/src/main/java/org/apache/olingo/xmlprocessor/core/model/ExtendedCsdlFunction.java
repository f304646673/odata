package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 扩展的CsdlFunction，支持依赖关系跟踪
 * 使用组合模式包装CsdlFunction，保持内部数据联动
 */
public class ExtendedCsdlFunction implements ExtendedCsdlElement {
    
    private final CsdlFunction wrappedFunction;
    private String namespace;
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlParameter> extendedParameters = new ArrayList<>();
    private ExtendedCsdlReturnType extendedReturnType;
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 构造函数
     */
    public ExtendedCsdlFunction() {
        this.wrappedFunction = new CsdlFunction();
        initializeExtendedCollections();
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
    }

    /**
     * 从标准CsdlFunction创建ExtendedCsdlFunction
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
            for (CsdlParameter param : source.getParameters()) {
                ExtendedCsdlParameter extendedParam = ExtendedCsdlParameter.fromCsdlParameter(param);
                extended.addExtendedParameter(extendedParam);
            }
        }

        // 级联构建ReturnType
        if (source.getReturnType() != null) {
            ExtendedCsdlReturnType extendedReturnType = ExtendedCsdlReturnType.fromCsdlReturnType(source.getReturnType());
            extended.setExtendedReturnType(extendedReturnType);
        }

        // 复制Annotations并转换为Extended版本
        if (source.getAnnotations() != null) {
            List<ExtendedCsdlAnnotation> extendedAnnotationsList = new ArrayList<ExtendedCsdlAnnotation>();
            for (CsdlAnnotation annotation : source.getAnnotations()) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                if (extendedAnnotation != null) {
                    extendedAnnotationsList.add(extendedAnnotation);
                }
            }
            extended.setExtendedAnnotations(extendedAnnotationsList);
            extended.setAnnotations(new ArrayList<CsdlAnnotation>(source.getAnnotations()));
        }

        return extended;
    }

    /**
     * 获取底层的CsdlFunction
     */
    public CsdlFunction asCsdlFunction() {
        return wrappedFunction;
    }

    // ==================== CsdlFunction 方法委托 ====================

    public String getName() {
        return wrappedFunction.getName();
    }

    public ExtendedCsdlFunction setName(String name) {
        wrappedFunction.setName(name);
        return this;
    }

    public boolean isBound() {
        return wrappedFunction.isBound();
    }

    public ExtendedCsdlFunction setBound(boolean isBound) {
        wrappedFunction.setBound(isBound);
        return this;
    }

    public boolean isComposable() {
        return wrappedFunction.isComposable();
    }

    public ExtendedCsdlFunction setComposable(boolean isComposable) {
        wrappedFunction.setComposable(isComposable);
        return this;
    }

    public String getEntitySetPath() {
        return wrappedFunction.getEntitySetPath();
    }

    public ExtendedCsdlFunction setEntitySetPath(String entitySetPath) {
        wrappedFunction.setEntitySetPath(entitySetPath);
        return this;
    }

    public List<CsdlParameter> getParameters() {
        // 返回不可修改的原始数据视图
        return wrappedFunction.getParameters() != null ? 
            Collections.unmodifiableList(wrappedFunction.getParameters()) : null;
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
    public ExtendedCsdlFunction addExtendedParameter(ExtendedCsdlParameter extendedParameter) {
        if (extendedParameter != null) {
            extendedParameters.add(extendedParameter);
            syncParametersToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended参数列表，同时更新原始数据
     */
    public ExtendedCsdlFunction setExtendedParameters(List<ExtendedCsdlParameter> extendedParameters) {
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
        List<CsdlParameter> csdlParams = new ArrayList<>();
        for (ExtendedCsdlParameter extParam : extendedParameters) {
            csdlParams.add(extParam.asCsdlParameter());
        }
        wrappedFunction.setParameters(csdlParams);
    }

    public CsdlParameter getParameter(String name) {
        return wrappedFunction.getParameter(name);
    }

    /**
     * 获取Extended参数
     */
    public ExtendedCsdlParameter getExtendedParameter(String name) {
        return extendedParameters.stream()
            .filter(p -> name.equals(p.getName()))
            .findFirst()
            .orElse(null);
    }

    @Deprecated
    public ExtendedCsdlFunction setParameters(List<CsdlParameter> parameters) {
        // 保留向后兼容，但建议使用setExtendedParameters
        wrappedFunction.setParameters(parameters);
        // 同步到Extended对象
        syncParametersFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended参数
     */
    private void syncParametersFromWrapped() {
        extendedParameters.clear();
        if (wrappedFunction.getParameters() != null) {
            for (CsdlParameter param : wrappedFunction.getParameters()) {
                ExtendedCsdlParameter extParam = ExtendedCsdlParameter.fromCsdlParameter(param);
                extendedParameters.add(extParam);
            }
        }
    }

    public CsdlReturnType getReturnType() {
        // 返回不可修改的原始数据
        return wrappedFunction.getReturnType();
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
    public ExtendedCsdlFunction setExtendedReturnType(ExtendedCsdlReturnType extendedReturnType) {
        this.extendedReturnType = extendedReturnType;
        if (extendedReturnType != null) {
            wrappedFunction.setReturnType(extendedReturnType.asCsdlReturnType());
        } else {
            wrappedFunction.setReturnType(null);
        }
        return this;
    }

    @Deprecated
    public ExtendedCsdlFunction setReturnType(CsdlReturnType returnType) {
        // 保留向后兼容，但建议使用setExtendedReturnType
        wrappedFunction.setReturnType(returnType);
        if (returnType != null) {
            this.extendedReturnType = ExtendedCsdlReturnType.fromCsdlReturnType(returnType);
        } else {
            this.extendedReturnType = null;
        }
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedFunction.getAnnotations();
    }

    public ExtendedCsdlFunction setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedFunction.setAnnotations(annotations);
        syncAnnotationsToExtended();
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }

    public void setExtendedAnnotations(List<ExtendedCsdlAnnotation> extendedAnnotations) {
        this.extendedAnnotations = extendedAnnotations;
        syncExtendedAnnotationsToOriginal();
    }

    /**
     * 将原始Annotations同步到Extended集合
     */
    private void syncAnnotationsToExtended() {
        if (this.extendedAnnotations == null) {
            this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
        }
        this.extendedAnnotations.clear();
        
        List<CsdlAnnotation> annotations = getAnnotations();
        if (annotations != null) {
            for (CsdlAnnotation annotation : annotations) {
                ExtendedCsdlAnnotation extendedAnnotation = ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation);
                if (extendedAnnotation != null) {
                    this.extendedAnnotations.add(extendedAnnotation);
                }
            }
        }
    }

    /**
     * 将Extended Annotations同步到原始集合
     */
    private void syncExtendedAnnotationsToOriginal() {
        List<CsdlAnnotation> annotations = new ArrayList<CsdlAnnotation>();
        if (this.extendedAnnotations != null) {
            for (ExtendedCsdlAnnotation extendedAnnotation : this.extendedAnnotations) {
                if (extendedAnnotation != null) {
                    annotations.add(extendedAnnotation.asCsdlAnnotation());
                }
            }
        }
        wrappedFunction.setAnnotations(annotations);
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedFunction.getName() != null) {
            return wrappedFunction.getName();
        }
        return "Function_" + hashCode();
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

    // ==================== 扩展属性 ====================

    public String getNamespace() {
        return namespace;
    }

    public ExtendedCsdlFunction setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlFunction{" +
                "name='" + getName() + '\'' +
                ", isBound=" + isBound() +
                ", isComposable=" + isComposable() +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
