package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的CsdlSchema，支持依赖关系跟踪
 * 使用组合模式包装CsdlSchema，保持内部数据联动
 * 继承AbstractExtendedCsdlElement以统一管理Annotations
 */
public class ExtendedCsdlSchema extends AbstractExtendedCsdlElement<CsdlSchema, ExtendedCsdlSchema> implements ExtendedCsdlElement {
    
    // 扩展属性
    private String sourcePath;
    private List<String> referencedNamespaces = new ArrayList<>();
    
    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlEntityType> extendedEntityTypes = new ArrayList<>();
    private final List<ExtendedCsdlComplexType> extendedComplexTypes = new ArrayList<>();
    private final List<ExtendedCsdlEnumType> extendedEnumTypes = new ArrayList<>();
    private final List<ExtendedCsdlAction> extendedActions = new ArrayList<>();
    private final List<ExtendedCsdlFunction> extendedFunctions = new ArrayList<>();
    private final List<ExtendedCsdlTypeDefinition> extendedTypeDefinitions = new ArrayList<>();
    private final List<ExtendedCsdlTerm> extendedTerms = new ArrayList<>();
    private ExtendedCsdlEntityContainer extendedEntityContainer;

    /**
     * 构造函数
     */
    public ExtendedCsdlSchema() {
        super(new CsdlSchema());
    }

    /**
     * 构造函数
     */
    public ExtendedCsdlSchema(String namespace) {
        super(new CsdlSchema());
        setNamespace(namespace);
    }

    /**
     * 从标准CsdlSchema创建ExtendedCsdlSchemaRefactored
     */
    public static ExtendedCsdlSchema fromCsdlSchema(CsdlSchema source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlSchema extended = new ExtendedCsdlSchema();

        // 复制基本属性
        extended.setNamespace(source.getNamespace());
        extended.setAlias(source.getAlias());

        // 级联构建EntityTypes
        if (source.getEntityTypes() != null) {
            for (CsdlEntityType entityType : source.getEntityTypes()) {
                ExtendedCsdlEntityType extendedEntityType = ExtendedCsdlEntityType.fromCsdlEntityType(entityType);
                extended.addExtendedEntityType(extendedEntityType);
            }
        }

        // 级联构建ComplexTypes
        if (source.getComplexTypes() != null) {
            for (CsdlComplexType complexType : source.getComplexTypes()) {
                ExtendedCsdlComplexType extendedComplexType = ExtendedCsdlComplexType.fromCsdlComplexType(complexType);
                extended.addExtendedComplexType(extendedComplexType);
            }
        }

        // 级联构建EnumTypes（简化实现）
        if (source.getEnumTypes() != null && !source.getEnumTypes().isEmpty()) {
            // 暂时直接设置到底层对象，不级联构建Extended版本
            extended.wrappedElement.setEnumTypes(new ArrayList<>(source.getEnumTypes()));
        }

        // 级联构建Actions
        if (source.getActions() != null) {
            for (CsdlAction action : source.getActions()) {
                ExtendedCsdlAction extendedAction = ExtendedCsdlAction.fromCsdlAction(action);
                extended.addExtendedAction(extendedAction);
            }
        }

        // 级联构建Functions
        if (source.getFunctions() != null) {
            for (CsdlFunction function : source.getFunctions()) {
                ExtendedCsdlFunction extendedFunction = ExtendedCsdlFunction.fromCsdlFunction(function);
                extended.addExtendedFunction(extendedFunction);
            }
        }

        // 级联构建TypeDefinitions（简化实现）
        if (source.getTypeDefinitions() != null && !source.getTypeDefinitions().isEmpty()) {
            // 暂时直接设置到底层对象，不级联构建Extended版本
            extended.wrappedElement.setTypeDefinitions(new ArrayList<>(source.getTypeDefinitions()));
        }

        // 级联构建Terms（简化实现）
        if (source.getTerms() != null && !source.getTerms().isEmpty()) {
            // 暂时直接设置到底层对象，不级联构建Extended版本
            extended.wrappedElement.setTerms(new ArrayList<>(source.getTerms()));
        }

        // 级联构建EntityContainer
        if (source.getEntityContainer() != null) {
            ExtendedCsdlEntityContainer extendedContainer = ExtendedCsdlEntityContainer.fromCsdlEntityContainer(source.getEntityContainer());
            extended.setExtendedEntityContainer(extendedContainer);
        }

        // 级联构建Annotations（使用基类方法）
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }

    /**
     * 获取底层的CsdlSchema
     */
    public CsdlSchema asCsdlSchema() {
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

    // ==================== CsdlSchema 方法委托 ====================

    @Override
    public String getNamespace() {
        return wrappedElement.getNamespace();
    }

    @Override
    public ExtendedCsdlSchema setNamespace(String namespace) {
        wrappedElement.setNamespace(namespace);
        // 同时设置基类的namespace
        super.setNamespace(namespace);
        return this;
    }

    public String getAlias() {
        return wrappedElement.getAlias();
    }

    public ExtendedCsdlSchema setAlias(String alias) {
        wrappedElement.setAlias(alias);
        return this;
    }

    public List<CsdlEntityType> getEntityTypes() {
        return wrappedElement.getEntityTypes();
    }

    public ExtendedCsdlSchema setEntityTypes(List<CsdlEntityType> entityTypes) {
        wrappedElement.setEntityTypes(entityTypes);
        syncEntityTypesFromWrapped();
        return this;
    }

    public List<CsdlComplexType> getComplexTypes() {
        return wrappedElement.getComplexTypes();
    }

    public ExtendedCsdlSchema setComplexTypes(List<CsdlComplexType> complexTypes) {
        wrappedElement.setComplexTypes(complexTypes);
        syncComplexTypesFromWrapped();
        return this;
    }

    public List<CsdlEnumType> getEnumTypes() {
        return wrappedElement.getEnumTypes();
    }

    public ExtendedCsdlSchema setEnumTypes(List<CsdlEnumType> enumTypes) {
        wrappedElement.setEnumTypes(enumTypes);
        syncEnumTypesFromWrapped();
        return this;
    }

    public List<CsdlAction> getActions() {
        return wrappedElement.getActions();
    }

    public ExtendedCsdlSchema setActions(List<CsdlAction> actions) {
        wrappedElement.setActions(actions);
        syncActionsFromWrapped();
        return this;
    }

    public List<CsdlFunction> getFunctions() {
        return wrappedElement.getFunctions();
    }

    public ExtendedCsdlSchema setFunctions(List<CsdlFunction> functions) {
        wrappedElement.setFunctions(functions);
        syncFunctionsFromWrapped();
        return this;
    }

    public List<CsdlTypeDefinition> getTypeDefinitions() {
        return wrappedElement.getTypeDefinitions();
    }

    public ExtendedCsdlSchema setTypeDefinitions(List<CsdlTypeDefinition> typeDefinitions) {
        wrappedElement.setTypeDefinitions(typeDefinitions);
        syncTypeDefinitionsFromWrapped();
        return this;
    }

    public List<CsdlTerm> getTerms() {
        return wrappedElement.getTerms();
    }

    public ExtendedCsdlSchema setTerms(List<CsdlTerm> terms) {
        wrappedElement.setTerms(terms);
        syncTermsFromWrapped();
        return this;
    }

    public CsdlEntityContainer getEntityContainer() {
        return wrappedElement.getEntityContainer();
    }

    public ExtendedCsdlSchema setEntityContainer(CsdlEntityContainer entityContainer) {
        wrappedElement.setEntityContainer(entityContainer);
        return this;
    }

    // ==================== Extended 集合方法 ====================

    public List<ExtendedCsdlEntityType> getExtendedEntityTypes() {
        return new ArrayList<>(extendedEntityTypes);
    }

    public ExtendedCsdlSchema addExtendedEntityType(ExtendedCsdlEntityType entityType) {
        if (entityType != null) {
            extendedEntityTypes.add(entityType);
            syncEntityTypesToWrapped();
        }
        return this;
    }

    public List<ExtendedCsdlComplexType> getExtendedComplexTypes() {
        return new ArrayList<>(extendedComplexTypes);
    }

    public ExtendedCsdlSchema addExtendedComplexType(ExtendedCsdlComplexType complexType) {
        if (complexType != null) {
            extendedComplexTypes.add(complexType);
            syncComplexTypesToWrapped();
        }
        return this;
    }

    public List<ExtendedCsdlAction> getExtendedActions() {
        return new ArrayList<>(extendedActions);
    }

    public ExtendedCsdlSchema addExtendedAction(ExtendedCsdlAction action) {
        if (action != null) {
            extendedActions.add(action);
            syncActionsToWrapped();
        }
        return this;
    }

    public List<ExtendedCsdlFunction> getExtendedFunctions() {
        return new ArrayList<>(extendedFunctions);
    }

    public ExtendedCsdlSchema addExtendedFunction(ExtendedCsdlFunction function) {
        if (function != null) {
            extendedFunctions.add(function);
            syncFunctionsToWrapped();
        }
        return this;
    }

    public ExtendedCsdlEntityContainer getExtendedEntityContainer() {
        return extendedEntityContainer;
    }

    public ExtendedCsdlSchema setExtendedEntityContainer(ExtendedCsdlEntityContainer container) {
        this.extendedEntityContainer = container;
        if (container != null) {
            wrappedElement.setEntityContainer(container.asCsdlEntityContainer());
        } else {
            wrappedElement.setEntityContainer(null);
        }
        return this;
    }

    // ==================== 扩展属性 ====================

    public String getSourcePath() {
        return sourcePath;
    }

    public ExtendedCsdlSchema setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    public List<String> getReferencedNamespaces() {
        return new ArrayList<>(referencedNamespaces);
    }

    public ExtendedCsdlSchema addReferencedNamespace(String namespace) {
        if (namespace != null && !referencedNamespaces.contains(namespace)) {
            referencedNamespaces.add(namespace);
        }
        return this;
    }

    // ==================== 同步方法 ====================

    private void syncEntityTypesToWrapped() {
        List<CsdlEntityType> csdlTypes = new ArrayList<>();
        for (ExtendedCsdlEntityType extType : extendedEntityTypes) {
            csdlTypes.add(extType.asCsdlEntityType());
        }
        wrappedElement.setEntityTypes(csdlTypes);
    }

    private void syncEntityTypesFromWrapped() {
        extendedEntityTypes.clear();
        if (wrappedElement.getEntityTypes() != null) {
            for (CsdlEntityType type : wrappedElement.getEntityTypes()) {
                ExtendedCsdlEntityType extType = ExtendedCsdlEntityType.fromCsdlEntityType(type);
                extendedEntityTypes.add(extType);
            }
        }
    }

    private void syncComplexTypesToWrapped() {
        List<CsdlComplexType> csdlTypes = new ArrayList<>();
        for (ExtendedCsdlComplexType extType : extendedComplexTypes) {
            csdlTypes.add(extType.asCsdlComplexType());
        }
        wrappedElement.setComplexTypes(csdlTypes);
    }

    private void syncComplexTypesFromWrapped() {
        extendedComplexTypes.clear();
        if (wrappedElement.getComplexTypes() != null) {
            for (CsdlComplexType type : wrappedElement.getComplexTypes()) {
                ExtendedCsdlComplexType extType = ExtendedCsdlComplexType.fromCsdlComplexType(type);
                extendedComplexTypes.add(extType);
            }
        }
    }

    private void syncActionsToWrapped() {
        List<CsdlAction> csdlActions = new ArrayList<>();
        for (ExtendedCsdlAction extAction : extendedActions) {
            csdlActions.add(extAction.asCsdlAction());
        }
        wrappedElement.setActions(csdlActions);
    }

    private void syncActionsFromWrapped() {
        extendedActions.clear();
        if (wrappedElement.getActions() != null) {
            for (CsdlAction action : wrappedElement.getActions()) {
                ExtendedCsdlAction extAction = ExtendedCsdlAction.fromCsdlAction(action);
                extendedActions.add(extAction);
            }
        }
    }

    private void syncFunctionsToWrapped() {
        List<CsdlFunction> csdlFunctions = new ArrayList<>();
        for (ExtendedCsdlFunction extFunction : extendedFunctions) {
            csdlFunctions.add(extFunction.asCsdlFunction());
        }
        wrappedElement.setFunctions(csdlFunctions);
    }

    private void syncFunctionsFromWrapped() {
        extendedFunctions.clear();
        if (wrappedElement.getFunctions() != null) {
            for (CsdlFunction function : wrappedElement.getFunctions()) {
                ExtendedCsdlFunction extFunction = ExtendedCsdlFunction.fromCsdlFunction(function);
                extendedFunctions.add(extFunction);
            }
        }
    }

    // 其他Extended类型的同步方法 - 简化实现
    private void syncEnumTypesFromWrapped() {
        extendedEnumTypes.clear();
        // 简化实现 - 实际应该级联构建ExtendedCsdlEnumType
    }

    private void syncTypeDefinitionsFromWrapped() {
        extendedTypeDefinitions.clear();
        // 简化实现 - 实际应该级联构建ExtendedCsdlTypeDefinition
    }

    private void syncTermsFromWrapped() {
        extendedTerms.clear();
        // 简化实现 - 实际应该级联构建ExtendedCsdlTerm
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedElement.getNamespace() != null) {
            return wrappedElement.getNamespace();
        }
        return "Schema_" + super.hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        if (getNamespace() != null) {
            return new FullQualifiedName(getNamespace(), "Schema");
        }
        return null;
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.TYPE_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getNamespace();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlSchema{" +
                "namespace='" + getNamespace() + '\'' +
                ", alias='" + getAlias() + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", entityTypesCount=" + extendedEntityTypes.size() +
                ", complexTypesCount=" + extendedComplexTypes.size() +
                ", actionsCount=" + extendedActions.size() +
                ", functionsCount=" + extendedFunctions.size() +
                '}';
    }
}
