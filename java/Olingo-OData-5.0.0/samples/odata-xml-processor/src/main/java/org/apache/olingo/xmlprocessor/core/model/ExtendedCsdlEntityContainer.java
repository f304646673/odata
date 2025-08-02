package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.ExtendedCsdlElement;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的CsdlEntityContainer，支持依赖关系跟踪
 * 使用组合模式包装CsdlEntityContainer，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlEntityContainer extends AbstractExtendedCsdlElement<CsdlEntityContainer, ExtendedCsdlEntityContainer> implements ExtendedCsdlElement {
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlEntitySet> extendedEntitySets = new ArrayList<>();
    private final List<ExtendedCsdlSingleton> extendedSingletons = new ArrayList<>();
    private final List<ExtendedCsdlActionImport> extendedActionImports = new ArrayList<>();
    private final List<ExtendedCsdlFunctionImport> extendedFunctionImports = new ArrayList<>();

    /**
     * 构造函数
     */
    public ExtendedCsdlEntityContainer() {
        super(new CsdlEntityContainer());
    }

    /**
     * 从标准CsdlEntityContainer创建ExtendedCsdlEntityContainerRefactored
     */
    public static ExtendedCsdlEntityContainer fromCsdlEntityContainer(CsdlEntityContainer source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlEntityContainer extended = new ExtendedCsdlEntityContainer();

        // 复制基本属性
        extended.setName(source.getName());

        // 级联构建EntitySets
        if (source.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : source.getEntitySets()) {
                ExtendedCsdlEntitySet extendedEntitySet = ExtendedCsdlEntitySet.fromCsdlEntitySet(entitySet);
                extended.addExtendedEntitySet(extendedEntitySet);
            }
        }

        // 级联构建Singletons
        if (source.getSingletons() != null) {
            for (CsdlSingleton singleton : source.getSingletons()) {
                ExtendedCsdlSingleton extendedSingleton = ExtendedCsdlSingleton.fromCsdlSingleton(singleton);
                extended.addExtendedSingleton(extendedSingleton);
            }
        }

        // 级联构建ActionImports
        if (source.getActionImports() != null) {
            for (CsdlActionImport actionImport : source.getActionImports()) {
                ExtendedCsdlActionImport extendedActionImport = ExtendedCsdlActionImport.fromCsdlActionImport(actionImport);
                extended.addExtendedActionImport(extendedActionImport);
            }
        }

        // 级联构建FunctionImports
        if (source.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : source.getFunctionImports()) {
                ExtendedCsdlFunctionImport extendedFunctionImport = ExtendedCsdlFunctionImport.fromCsdlFunctionImport(functionImport);
                extended.addExtendedFunctionImport(extendedFunctionImport);
            }
        }

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlEntityContainer
     */
    public CsdlEntityContainer asCsdlEntityContainer() {
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

    // ==================== CsdlEntityContainer 方法委托 ====================

    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlEntityContainer setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    // 注意：CsdlEntityContainer可能没有extends相关方法，所以注释掉这些方法
    /*
    public String getExtends() {
        return ((CsdlEntityContainer) wrappedElement).getExtends();
    }

    public FullQualifiedName getExtendsFQN() {
        return ((CsdlEntityContainer) wrappedElement).getExtendsFQN();
    }

    public ExtendedCsdlEntityContainer setExtends(String extendsContainer) {
        ((CsdlEntityContainer) wrappedElement).setExtends(extendsContainer);
        return this;
    }

    public ExtendedCsdlEntityContainer setExtends(FullQualifiedName extendsFQN) {
        ((CsdlEntityContainer) wrappedElement).setExtends(extendsFQN);
        return this;
    }
    */

    @Deprecated
    public List<CsdlEntitySet> getEntitySets() {
        return wrappedElement.getEntitySets();
    }

    public ExtendedCsdlEntityContainer setEntitySets(List<CsdlEntitySet> entitySets) {
        wrappedElement.setEntitySets(entitySets);
        syncEntitySetsFromWrapped();
        return this;
    }

    @Deprecated
    public List<CsdlSingleton> getSingletons() {
        return wrappedElement.getSingletons();
    }

    public ExtendedCsdlEntityContainer setSingletons(List<CsdlSingleton> singletons) {
        wrappedElement.setSingletons(singletons);
        syncSingletonsFromWrapped();
        return this;
    }

    @Deprecated
    public List<CsdlActionImport> getActionImports() {
        return wrappedElement.getActionImports();
    }

    public ExtendedCsdlEntityContainer setActionImports(List<CsdlActionImport> actionImports) {
        wrappedElement.setActionImports(actionImports);
        syncActionImportsFromWrapped();
        return this;
    }

    @Deprecated
    public List<CsdlFunctionImport> getFunctionImports() {
        return wrappedElement.getFunctionImports();
    }

    public ExtendedCsdlEntityContainer setFunctionImports(List<CsdlFunctionImport> functionImports) {
        wrappedElement.setFunctionImports(functionImports);
        syncFunctionImportsFromWrapped();
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlEntitySet> getExtendedEntitySets() {
        return new ArrayList<>(extendedEntitySets);
    }

    public ExtendedCsdlEntityContainer addExtendedEntitySet(ExtendedCsdlEntitySet entitySet) {
        if (entitySet != null) {
            extendedEntitySets.add(entitySet);
            syncEntitySetsToWrapped();
        }
        return this;
    }

    public List<ExtendedCsdlSingleton> getExtendedSingletons() {
        return new ArrayList<>(extendedSingletons);
    }

    public List<ExtendedCsdlActionImport> getExtendedActionImports() {
        return new ArrayList<>(extendedActionImports);
    }

    public ExtendedCsdlEntityContainer addExtendedActionImport(ExtendedCsdlActionImport actionImport) {
        if (actionImport != null) {
            extendedActionImports.add(actionImport);
            syncActionImportsToWrapped();
        }
        return this;
    }

    public List<ExtendedCsdlFunctionImport> getExtendedFunctionImports() {
        return new ArrayList<>(extendedFunctionImports);
    }

    public ExtendedCsdlEntityContainer addExtendedFunctionImport(ExtendedCsdlFunctionImport functionImport) {
        if (functionImport != null) {
            extendedFunctionImports.add(functionImport);
            syncFunctionImportsToWrapped();
        }
        return this;
    }

    public ExtendedCsdlEntityContainer addExtendedSingleton(ExtendedCsdlSingleton singleton) {
        if (singleton != null) {
            extendedSingletons.add(singleton);
            syncSingletonsToWrapped();
        }
        return this;
    }

    // ==================== 同步方法 ====================

    private void syncEntitySetsToWrapped() {
        List<CsdlEntitySet> csdlEntitySets = new ArrayList<>();
        for (ExtendedCsdlEntitySet extEntitySet : extendedEntitySets) {
            csdlEntitySets.add(extEntitySet.asCsdlEntitySet());
        }
        wrappedElement.setEntitySets(csdlEntitySets);
    }

    private void syncEntitySetsFromWrapped() {
        extendedEntitySets.clear();
        if (wrappedElement.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : wrappedElement.getEntitySets()) {
                ExtendedCsdlEntitySet extEntitySet = ExtendedCsdlEntitySet.fromCsdlEntitySet(entitySet);
                extendedEntitySets.add(extEntitySet);
            }
        }
    }

    private void syncActionImportsToWrapped() {
        List<CsdlActionImport> csdlActionImports = new ArrayList<>();
        for (ExtendedCsdlActionImport extActionImport : extendedActionImports) {
            csdlActionImports.add(extActionImport.asCsdlActionImport());
        }
        wrappedElement.setActionImports(csdlActionImports);
    }

    private void syncActionImportsFromWrapped() {
        extendedActionImports.clear();
        if (wrappedElement.getActionImports() != null) {
            for (CsdlActionImport actionImport : wrappedElement.getActionImports()) {
                ExtendedCsdlActionImport extActionImport = ExtendedCsdlActionImport.fromCsdlActionImport(actionImport);
                extendedActionImports.add(extActionImport);
            }
        }
    }

    private void syncFunctionImportsToWrapped() {
        List<CsdlFunctionImport> csdlFunctionImports = new ArrayList<>();
        for (ExtendedCsdlFunctionImport extFunctionImport : extendedFunctionImports) {
            csdlFunctionImports.add(extFunctionImport.asCsdlFunctionImport());
        }
        wrappedElement.setFunctionImports(csdlFunctionImports);
    }

    private void syncFunctionImportsFromWrapped() {
        extendedFunctionImports.clear();
        if (wrappedElement.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : wrappedElement.getFunctionImports()) {
                ExtendedCsdlFunctionImport extFunctionImport = ExtendedCsdlFunctionImport.fromCsdlFunctionImport(functionImport);
                extendedFunctionImports.add(extFunctionImport);
            }
        }
    }

    private void syncSingletonsToWrapped() {
        List<CsdlSingleton> csdlSingletons = new ArrayList<>();
        for (ExtendedCsdlSingleton extSingleton : extendedSingletons) {
            csdlSingletons.add(extSingleton.asCsdlSingleton());
        }
        wrappedElement.setSingletons(csdlSingletons);
    }

    private void syncSingletonsFromWrapped() {
        extendedSingletons.clear();
        if (wrappedElement.getSingletons() != null) {
            for (CsdlSingleton singleton : wrappedElement.getSingletons()) {
                ExtendedCsdlSingleton extSingleton = ExtendedCsdlSingleton.fromCsdlSingleton(singleton);
                extendedSingletons.add(extSingleton);
            }
        }
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getName() != null) {
            return wrappedElement.getName();
        }
        return "EntityContainer_" + super.hashCode();
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
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlEntityContainer{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                ", entitySetsCount=" + extendedEntitySets.size() +
                ", actionImportsCount=" + extendedActionImports.size() +
                ", functionImportsCount=" + extendedFunctionImports.size() +
                '}';
    }
}
