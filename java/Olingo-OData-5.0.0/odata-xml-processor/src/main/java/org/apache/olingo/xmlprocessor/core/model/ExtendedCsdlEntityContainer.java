package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展的CSDL实体容器
 * 继承自CsdlEntityContainer，增加依赖跟踪和扩展功能
 */
public class ExtendedCsdlEntityContainer extends CsdlEntityContainer implements ExtendedCsdlElement {
    
    private String namespace;
    
    // Extended版本的内部元素
    private List<ExtendedCsdlEntitySet> extendedEntitySets;
    private List<ExtendedCsdlSingleton> extendedSingletons;
    private List<ExtendedCsdlActionImport> extendedActionImports;
    private List<ExtendedCsdlFunctionImport> extendedFunctionImports;
    private List<ExtendedCsdlAnnotation> extendedAnnotations;

    /**
     * 默认构造函数
     */
    public ExtendedCsdlEntityContainer() {
        super();
        initializeExtendedCollections();
    }

    /**
     * 从标准CsdlEntityContainer创建ExtendedCsdlEntityContainer
     */
    public static ExtendedCsdlEntityContainer fromCsdlEntityContainer(CsdlEntityContainer source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlEntityContainer extended = new ExtendedCsdlEntityContainer();

        // 复制基本属性
        extended.setName(source.getName());
        // extended.setExtends(source.getExtends()); // 移除不存在的方法调用

        // 转换EntitySets为ExtendedCsdlEntitySet
        if (source.getEntitySets() != null) {
            List<CsdlEntitySet> extendedEntitySets = source.getEntitySets().stream()
                .map(entitySet -> ExtendedCsdlEntitySet.fromCsdlEntitySet(entitySet))
                .collect(Collectors.toList());
            extended.setEntitySets(extendedEntitySets);
        }

        // 转换Singletons为ExtendedCsdlSingleton
        if (source.getSingletons() != null) {
            List<CsdlSingleton> extendedSingletons = source.getSingletons().stream()
                .map(singleton -> ExtendedCsdlSingleton.fromCsdlSingleton(singleton))
                .collect(Collectors.toList());
            extended.setSingletons(extendedSingletons);
        }

        // 转换ActionImports为ExtendedCsdlActionImport
        if (source.getActionImports() != null) {
            List<CsdlActionImport> extendedActionImports = source.getActionImports().stream()
                .map(actionImport -> ExtendedCsdlActionImport.fromCsdlActionImport(actionImport))
                .collect(Collectors.toList());
            extended.setActionImports(extendedActionImports);
        }

        // 转换FunctionImports为ExtendedCsdlFunctionImport
        if (source.getFunctionImports() != null) {
            List<CsdlFunctionImport> extendedFunctionImports = source.getFunctionImports().stream()
                .map(functionImport -> ExtendedCsdlFunctionImport.fromCsdlFunctionImport(functionImport))
                .collect(Collectors.toList());
            extended.setFunctionImports(extendedFunctionImports);
        }

        // 转换Annotations为ExtendedCsdlAnnotation
        if (source.getAnnotations() != null) {
            List<CsdlAnnotation> extendedAnnotations = source.getAnnotations().stream()
                .map(annotation -> ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation))
                .collect(Collectors.toList());
            extended.setAnnotations(extendedAnnotations);
        }

        return extended;
    }

    /**
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedEntitySets = new ArrayList<>();
        this.extendedSingletons = new ArrayList<>();
        this.extendedActionImports = new ArrayList<>();
        this.extendedFunctionImports = new ArrayList<>();
        this.extendedAnnotations = new ArrayList<>();
    }
    
    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "EntityContainer_" + hashCode();
    }
    
    @Override
    public ExtendedCsdlEntityContainer setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public ExtendedCsdlEntityContainer registerElement() {
        ExtendedCsdlElement.super.registerElement();
        return this;
    }
    
    /**
     * 获取元素的完全限定名（如果适用）
     */
    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }
    
    /**
     * 获取元素的依赖类型
     */
    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE; // EntityContainer 使用通用类型引用
    }
    
    @Override
    public String getElementPropertyName() {
        return null; // EntityContainer通常不关联特定属性
    }

    // Extended集合的getter方法
    public List<ExtendedCsdlEntitySet> getExtendedEntitySets() {
        return extendedEntitySets;
    }
    
    public List<ExtendedCsdlSingleton> getExtendedSingletons() {
        return extendedSingletons;
    }

    public List<ExtendedCsdlActionImport> getExtendedActionImports() {
        return extendedActionImports;
    }

    public List<ExtendedCsdlFunctionImport> getExtendedFunctionImports() {
        return extendedFunctionImports;
    }

    public List<ExtendedCsdlAnnotation> getExtendedAnnotations() {
        return extendedAnnotations;
    }
}
