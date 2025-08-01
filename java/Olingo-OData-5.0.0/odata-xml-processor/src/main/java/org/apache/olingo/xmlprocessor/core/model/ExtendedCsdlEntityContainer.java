package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的CsdlEntityContainer，支持依赖关系跟踪
 * 使用组合模式包装CsdlEntityContainer，保持内部数据联动
 */
public class ExtendedCsdlEntityContainer implements ExtendedCsdlElement {
    
    private final CsdlEntityContainer wrappedEntityContainer;
    private String namespace;

    // Extended版本的内部元素
    private List<ExtendedCsdlAnnotation> extendedAnnotations;
    private List<ExtendedCsdlEntitySet> extendedEntitySets;
    private List<ExtendedCsdlSingleton> extendedSingletons;
    private List<ExtendedCsdlActionImport> extendedActionImports;
    private List<ExtendedCsdlFunctionImport> extendedFunctionImports;

    /**
     * 构造函数
     */
    public ExtendedCsdlEntityContainer() {
        this.wrappedEntityContainer = new CsdlEntityContainer();
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

        // 复制EntitySets并转换为Extended版本
        if (source.getEntitySets() != null) {
            List<ExtendedCsdlEntitySet> extendedEntitySetsList = new ArrayList<ExtendedCsdlEntitySet>();
            for (CsdlEntitySet entitySet : source.getEntitySets()) {
                ExtendedCsdlEntitySet extendedEntitySet = ExtendedCsdlEntitySet.fromCsdlEntitySet(entitySet);
                if (extendedEntitySet != null) {
                    extendedEntitySetsList.add(extendedEntitySet);
                }
            }
            extended.setExtendedEntitySets(extendedEntitySetsList);
            extended.setEntitySets(new ArrayList<CsdlEntitySet>(source.getEntitySets()));
        }

        // 复制Singletons并转换为Extended版本
        if (source.getSingletons() != null) {
            List<ExtendedCsdlSingleton> extendedSingletonsList = new ArrayList<ExtendedCsdlSingleton>();
            for (CsdlSingleton singleton : source.getSingletons()) {
                ExtendedCsdlSingleton extendedSingleton = ExtendedCsdlSingleton.fromCsdlSingleton(singleton);
                if (extendedSingleton != null) {
                    extendedSingletonsList.add(extendedSingleton);
                }
            }
            extended.setExtendedSingletons(extendedSingletonsList);
            extended.setSingletons(new ArrayList<CsdlSingleton>(source.getSingletons()));
        }

        // 复制ActionImports并转换为Extended版本
        if (source.getActionImports() != null) {
            List<ExtendedCsdlActionImport> extendedActionImportsList = new ArrayList<ExtendedCsdlActionImport>();
            for (CsdlActionImport actionImport : source.getActionImports()) {
                ExtendedCsdlActionImport extendedActionImport = ExtendedCsdlActionImport.fromCsdlActionImport(actionImport);
                if (extendedActionImport != null) {
                    extendedActionImportsList.add(extendedActionImport);
                }
            }
            extended.setExtendedActionImports(extendedActionImportsList);
            extended.setActionImports(new ArrayList<CsdlActionImport>(source.getActionImports()));
        }

        // 复制FunctionImports并转换为Extended版本
        if (source.getFunctionImports() != null) {
            List<ExtendedCsdlFunctionImport> extendedFunctionImportsList = new ArrayList<ExtendedCsdlFunctionImport>();
            for (CsdlFunctionImport functionImport : source.getFunctionImports()) {
                ExtendedCsdlFunctionImport extendedFunctionImport = ExtendedCsdlFunctionImport.fromCsdlFunctionImport(functionImport);
                if (extendedFunctionImport != null) {
                    extendedFunctionImportsList.add(extendedFunctionImport);
                }
            }
            extended.setExtendedFunctionImports(extendedFunctionImportsList);
            extended.setFunctionImports(new ArrayList<CsdlFunctionImport>(source.getFunctionImports()));
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
     * 初始化扩展集合
     */
    private void initializeExtendedCollections() {
        this.extendedAnnotations = new ArrayList<ExtendedCsdlAnnotation>();
        this.extendedEntitySets = new ArrayList<ExtendedCsdlEntitySet>();
        this.extendedSingletons = new ArrayList<ExtendedCsdlSingleton>();
        this.extendedActionImports = new ArrayList<ExtendedCsdlActionImport>();
        this.extendedFunctionImports = new ArrayList<ExtendedCsdlFunctionImport>();
    }

    /**
     * 获取底层的CsdlEntityContainer
     */
    public CsdlEntityContainer asCsdlEntityContainer() {
        return wrappedEntityContainer;
    }

    // ==================== CsdlEntityContainer 方法委托 ====================

    public String getName() {
        return wrappedEntityContainer.getName();
    }

    public ExtendedCsdlEntityContainer setName(String name) {
        wrappedEntityContainer.setName(name);
        return this;
    }

    public List<CsdlEntitySet> getEntitySets() {
        return wrappedEntityContainer.getEntitySets();
    }

    public CsdlEntitySet getEntitySet(String name) {
        return wrappedEntityContainer.getEntitySet(name);
    }

    public ExtendedCsdlEntityContainer setEntitySets(List<CsdlEntitySet> entitySets) {
        wrappedEntityContainer.setEntitySets(entitySets);
        syncEntitySetsToExtended();
        return this;
    }

    public List<CsdlSingleton> getSingletons() {
        return wrappedEntityContainer.getSingletons();
    }

    public CsdlSingleton getSingleton(String name) {
        return wrappedEntityContainer.getSingleton(name);
    }

    public ExtendedCsdlEntityContainer setSingletons(List<CsdlSingleton> singletons) {
        wrappedEntityContainer.setSingletons(singletons);
        syncSingletonsToExtended();
        return this;
    }

    public List<CsdlActionImport> getActionImports() {
        return wrappedEntityContainer.getActionImports();
    }

    public CsdlActionImport getActionImport(String name) {
        return wrappedEntityContainer.getActionImport(name);
    }

    public ExtendedCsdlEntityContainer setActionImports(List<CsdlActionImport> actionImports) {
        wrappedEntityContainer.setActionImports(actionImports);
        syncActionImportsToExtended();
        return this;
    }

    public List<CsdlFunctionImport> getFunctionImports() {
        return wrappedEntityContainer.getFunctionImports();
    }

    public CsdlFunctionImport getFunctionImport(String name) {
        return wrappedEntityContainer.getFunctionImport(name);
    }

    public ExtendedCsdlEntityContainer setFunctionImports(List<CsdlFunctionImport> functionImports) {
        wrappedEntityContainer.setFunctionImports(functionImports);
        syncFunctionImportsToExtended();
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedEntityContainer.getAnnotations();
    }

    public ExtendedCsdlEntityContainer setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedEntityContainer.setAnnotations(annotations);
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

    public List<ExtendedCsdlEntitySet> getExtendedEntitySets() {
        return extendedEntitySets;
    }

    public void setExtendedEntitySets(List<ExtendedCsdlEntitySet> extendedEntitySets) {
        this.extendedEntitySets = extendedEntitySets;
        syncExtendedEntitySetsToOriginal();
    }

    public List<ExtendedCsdlSingleton> getExtendedSingletons() {
        return extendedSingletons;
    }

    public void setExtendedSingletons(List<ExtendedCsdlSingleton> extendedSingletons) {
        this.extendedSingletons = extendedSingletons;
        syncExtendedSingletonsToOriginal();
    }

    public List<ExtendedCsdlActionImport> getExtendedActionImports() {
        return extendedActionImports;
    }

    public void setExtendedActionImports(List<ExtendedCsdlActionImport> extendedActionImports) {
        this.extendedActionImports = extendedActionImports;
        syncExtendedActionImportsToOriginal();
    }

    public List<ExtendedCsdlFunctionImport> getExtendedFunctionImports() {
        return extendedFunctionImports;
    }

    public void setExtendedFunctionImports(List<ExtendedCsdlFunctionImport> extendedFunctionImports) {
        this.extendedFunctionImports = extendedFunctionImports;
        syncExtendedFunctionImportsToOriginal();
    }

    // ==================== 同步方法 ====================

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

    private void syncExtendedAnnotationsToOriginal() {
        List<CsdlAnnotation> annotations = new ArrayList<CsdlAnnotation>();
        if (this.extendedAnnotations != null) {
            for (ExtendedCsdlAnnotation extendedAnnotation : this.extendedAnnotations) {
                if (extendedAnnotation != null) {
                    annotations.add(extendedAnnotation.asCsdlAnnotation());
                }
            }
        }
        wrappedEntityContainer.setAnnotations(annotations);
    }

    private void syncEntitySetsToExtended() {
        if (this.extendedEntitySets == null) {
            this.extendedEntitySets = new ArrayList<ExtendedCsdlEntitySet>();
        }
        this.extendedEntitySets.clear();
        
        List<CsdlEntitySet> entitySets = getEntitySets();
        if (entitySets != null) {
            for (CsdlEntitySet entitySet : entitySets) {
                ExtendedCsdlEntitySet extendedEntitySet = ExtendedCsdlEntitySet.fromCsdlEntitySet(entitySet);
                if (extendedEntitySet != null) {
                    this.extendedEntitySets.add(extendedEntitySet);
                }
            }
        }
    }

    private void syncExtendedEntitySetsToOriginal() {
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        if (this.extendedEntitySets != null) {
            for (ExtendedCsdlEntitySet extendedEntitySet : this.extendedEntitySets) {
                if (extendedEntitySet != null) {
                    entitySets.add(extendedEntitySet.asCsdlEntitySet());
                }
            }
        }
        wrappedEntityContainer.setEntitySets(entitySets);
    }

    private void syncSingletonsToExtended() {
        if (this.extendedSingletons == null) {
            this.extendedSingletons = new ArrayList<ExtendedCsdlSingleton>();
        }
        this.extendedSingletons.clear();
        
        List<CsdlSingleton> singletons = getSingletons();
        if (singletons != null) {
            for (CsdlSingleton singleton : singletons) {
                ExtendedCsdlSingleton extendedSingleton = ExtendedCsdlSingleton.fromCsdlSingleton(singleton);
                if (extendedSingleton != null) {
                    this.extendedSingletons.add(extendedSingleton);
                }
            }
        }
    }

    private void syncExtendedSingletonsToOriginal() {
        List<CsdlSingleton> singletons = new ArrayList<CsdlSingleton>();
        if (this.extendedSingletons != null) {
            for (ExtendedCsdlSingleton extendedSingleton : this.extendedSingletons) {
                if (extendedSingleton != null) {
                    singletons.add(extendedSingleton.asCsdlSingleton());
                }
            }
        }
        wrappedEntityContainer.setSingletons(singletons);
    }

    private void syncActionImportsToExtended() {
        if (this.extendedActionImports == null) {
            this.extendedActionImports = new ArrayList<ExtendedCsdlActionImport>();
        }
        this.extendedActionImports.clear();
        
        List<CsdlActionImport> actionImports = getActionImports();
        if (actionImports != null) {
            for (CsdlActionImport actionImport : actionImports) {
                ExtendedCsdlActionImport extendedActionImport = ExtendedCsdlActionImport.fromCsdlActionImport(actionImport);
                if (extendedActionImport != null) {
                    this.extendedActionImports.add(extendedActionImport);
                }
            }
        }
    }

    private void syncExtendedActionImportsToOriginal() {
        List<CsdlActionImport> actionImports = new ArrayList<CsdlActionImport>();
        if (this.extendedActionImports != null) {
            for (ExtendedCsdlActionImport extendedActionImport : this.extendedActionImports) {
                if (extendedActionImport != null) {
                    actionImports.add(extendedActionImport.asCsdlActionImport());
                }
            }
        }
        wrappedEntityContainer.setActionImports(actionImports);
    }

    private void syncFunctionImportsToExtended() {
        if (this.extendedFunctionImports == null) {
            this.extendedFunctionImports = new ArrayList<ExtendedCsdlFunctionImport>();
        }
        this.extendedFunctionImports.clear();
        
        List<CsdlFunctionImport> functionImports = getFunctionImports();
        if (functionImports != null) {
            for (CsdlFunctionImport functionImport : functionImports) {
                ExtendedCsdlFunctionImport extendedFunctionImport = ExtendedCsdlFunctionImport.fromCsdlFunctionImport(functionImport);
                if (extendedFunctionImport != null) {
                    this.extendedFunctionImports.add(extendedFunctionImport);
                }
            }
        }
    }

    private void syncExtendedFunctionImportsToOriginal() {
        List<CsdlFunctionImport> functionImports = new ArrayList<CsdlFunctionImport>();
        if (this.extendedFunctionImports != null) {
            for (ExtendedCsdlFunctionImport extendedFunctionImport : this.extendedFunctionImports) {
                if (extendedFunctionImport != null) {
                    functionImports.add(extendedFunctionImport.asCsdlFunctionImport());
                }
            }
        }
        wrappedEntityContainer.setFunctionImports(functionImports);
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedEntityContainer.getName() != null) {
            return wrappedEntityContainer.getName();
        }
        return "EntityContainer_" + hashCode();
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
        return CsdlDependencyNode.DependencyType.ENTITY_SET_REFERENCE; // 使用最接近的类型
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }

    // ==================== 扩展属性 ====================

    public String getNamespace() {
        return namespace;
    }

    public ExtendedCsdlEntityContainer setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public String toString() {
        return "ExtendedCsdlEntityContainer{" +
                "name='" + getName() + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
