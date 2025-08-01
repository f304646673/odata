package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
 * 扩展的CSDL Schema实现
 * 使用组合模式包装CsdlSchema，保持内部数据联动
 */
public class ExtendedCsdlSchema implements ExtendedCsdlElement {

    private final CsdlSchema wrappedSchema;
    private String sourcePath;
    private List<String> referencedNamespaces;
    private boolean isExtended = true;

    // Extended子对象集合，与原始数据保持同步
    private final List<ExtendedCsdlEntityType> extendedEntityTypes = new ArrayList<>();
    private final List<ExtendedCsdlComplexType> extendedComplexTypes = new ArrayList<>();
    private final List<ExtendedCsdlEnumType> extendedEnumTypes = new ArrayList<>();
    private final List<ExtendedCsdlAction> extendedActions = new ArrayList<>();
    private final List<ExtendedCsdlFunction> extendedFunctions = new ArrayList<>();
    private final List<ExtendedCsdlTypeDefinition> extendedTypeDefinitions = new ArrayList<>();
    private final List<ExtendedCsdlTerm> extendedTerms = new ArrayList<>();
    private ExtendedCsdlEntityContainer extendedEntityContainer;

    public ExtendedCsdlSchema() {
        this.wrappedSchema = new CsdlSchema();
        this.referencedNamespaces = new ArrayList<>();
    }

    public ExtendedCsdlSchema(String namespace) {
        this.wrappedSchema = new CsdlSchema();
        this.wrappedSchema.setNamespace(namespace);
        this.referencedNamespaces = new ArrayList<>();
    }

    /**
     * 从标准CsdlSchema创建ExtendedCsdlSchema
     */
    public static ExtendedCsdlSchema fromCsdlSchema(CsdlSchema baseSchema) {
        if (baseSchema == null) {
            return null;
        }

        ExtendedCsdlSchema extended = new ExtendedCsdlSchema();

        // 复制基本属性
        extended.setNamespace(baseSchema.getNamespace());
        extended.setAlias(baseSchema.getAlias());

        // 级联构建EntityTypes
        if (baseSchema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : baseSchema.getEntityTypes()) {
                ExtendedCsdlEntityType extendedEntityType = ExtendedCsdlEntityType.fromCsdlEntityType(entityType);
                extended.addExtendedEntityType(extendedEntityType);
            }
        }

        // 级联构建ComplexTypes
        if (baseSchema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : baseSchema.getComplexTypes()) {
                ExtendedCsdlComplexType extendedComplexType = ExtendedCsdlComplexType.fromCsdlComplexType(complexType);
                extended.addExtendedComplexType(extendedComplexType);
            }
        }

        // 级联构建EnumTypes
        if (baseSchema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : baseSchema.getEnumTypes()) {
                ExtendedCsdlEnumType extendedEnumType = ExtendedCsdlEnumType.fromCsdlEnumType(enumType);
                extended.addExtendedEnumType(extendedEnumType);
            }
        }

        // 级联构建TypeDefinitions
        if (baseSchema.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : baseSchema.getTypeDefinitions()) {
                ExtendedCsdlTypeDefinition extendedTypeDef = ExtendedCsdlTypeDefinition.fromCsdlTypeDefinition(typeDef);
                extended.addExtendedTypeDefinition(extendedTypeDef);
            }
        }

        // 级联构建Actions
        if (baseSchema.getActions() != null) {
            for (CsdlAction action : baseSchema.getActions()) {
                ExtendedCsdlAction extendedAction = ExtendedCsdlAction.fromCsdlAction(action);
                extended.addExtendedAction(extendedAction);
            }
        }

        // 级联构建Functions
        if (baseSchema.getFunctions() != null) {
            for (CsdlFunction function : baseSchema.getFunctions()) {
                ExtendedCsdlFunction extendedFunction = ExtendedCsdlFunction.fromCsdlFunction(function);
                extended.addExtendedFunction(extendedFunction);
            }
        }

        // 级联构建Terms
        if (baseSchema.getTerms() != null) {
            for (CsdlTerm term : baseSchema.getTerms()) {
                ExtendedCsdlTerm extendedTerm = ExtendedCsdlTerm.fromCsdlTerm(term);
                extended.addExtendedTerm(extendedTerm);
            }
        }

        // 级联构建EntityContainer
        if (baseSchema.getEntityContainer() != null) {
            ExtendedCsdlEntityContainer extendedContainer = ExtendedCsdlEntityContainer.fromCsdlEntityContainer(baseSchema.getEntityContainer());
            extended.setExtendedEntityContainer(extendedContainer);
        }

        // 复制Annotations
        if (baseSchema.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(baseSchema.getAnnotations()));
        }

        return extended;
    }

    /**
     * 获取底层的CsdlSchema
     */
    public CsdlSchema asCsdlSchema() {
        return wrappedSchema;
    }

    // ==================== CsdlSchema 方法委托 ====================

    public String getNamespace() {
        return wrappedSchema.getNamespace();
    }

    public ExtendedCsdlSchema setNamespace(String namespace) {
        wrappedSchema.setNamespace(namespace);
        return this;
    }

    public String getAlias() {
        return wrappedSchema.getAlias();
    }

    public ExtendedCsdlSchema setAlias(String alias) {
        wrappedSchema.setAlias(alias);
        return this;
    }

    public List<CsdlEntityType> getEntityTypes() {
        // 返回不可修改的原始数据视图
        return wrappedSchema.getEntityTypes() != null ?
            Collections.unmodifiableList(wrappedSchema.getEntityTypes()) : null;
    }

    /**
     * 获取Extended实体类型列表
     */
    public List<ExtendedCsdlEntityType> getExtendedEntityTypes() {
        return new ArrayList<>(extendedEntityTypes);
    }

    /**
     * 添加Extended实体类型，同时更新原始数据
     */
    public ExtendedCsdlSchema addExtendedEntityType(ExtendedCsdlEntityType extendedEntityType) {
        if (extendedEntityType != null) {
            extendedEntityTypes.add(extendedEntityType);
            syncEntityTypesToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended实体类型列表，同时更新原始数据
     */
    public ExtendedCsdlSchema setExtendedEntityTypes(List<ExtendedCsdlEntityType> extendedEntityTypes) {
        this.extendedEntityTypes.clear();
        if (extendedEntityTypes != null) {
            this.extendedEntityTypes.addAll(extendedEntityTypes);
        }
        syncEntityTypesToWrapped();
        return this;
    }

    /**
     * 同步Extended实体类型到原始数据
     */
    private void syncEntityTypesToWrapped() {
        List<CsdlEntityType> csdlEntityTypes = new ArrayList<>();
        for (ExtendedCsdlEntityType extEntityType : extendedEntityTypes) {
            csdlEntityTypes.add(extEntityType.asCsdlEntityType());
        }
        wrappedSchema.setEntityTypes(csdlEntityTypes);
    }

    @Deprecated
    public ExtendedCsdlSchema setEntityTypes(List<CsdlEntityType> entityTypes) {
        // 保留向后兼容，但建议使用setExtendedEntityTypes
        wrappedSchema.setEntityTypes(entityTypes);
        // 同步到Extended对象
        syncEntityTypesFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended实体类型
     */
    private void syncEntityTypesFromWrapped() {
        extendedEntityTypes.clear();
        if (wrappedSchema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : wrappedSchema.getEntityTypes()) {
                ExtendedCsdlEntityType extEntityType = ExtendedCsdlEntityType.fromCsdlEntityType(entityType);
                extendedEntityTypes.add(extEntityType);
            }
        }
    }

    public List<CsdlComplexType> getComplexTypes() {
        // 返回不可修改的原始数据视图
        return wrappedSchema.getComplexTypes() != null ?
            Collections.unmodifiableList(wrappedSchema.getComplexTypes()) : null;
    }

    /**
     * 获取Extended复杂类型列表
     */
    public List<ExtendedCsdlComplexType> getExtendedComplexTypes() {
        return new ArrayList<>(extendedComplexTypes);
    }

    /**
     * 添加Extended复杂类型，同时更新原始数据
     */
    public ExtendedCsdlSchema addExtendedComplexType(ExtendedCsdlComplexType extendedComplexType) {
        if (extendedComplexType != null) {
            extendedComplexTypes.add(extendedComplexType);
            syncComplexTypesToWrapped();
        }
        return this;
    }

    /**
     * 设置Extended复杂类型列表，同时更新原始数据
     */
    public ExtendedCsdlSchema setExtendedComplexTypes(List<ExtendedCsdlComplexType> extendedComplexTypes) {
        this.extendedComplexTypes.clear();
        if (extendedComplexTypes != null) {
            this.extendedComplexTypes.addAll(extendedComplexTypes);
        }
        syncComplexTypesToWrapped();
        return this;
    }

    /**
     * 同步Extended复杂类型到原始数据
     */
    private void syncComplexTypesToWrapped() {
        List<CsdlComplexType> csdlComplexTypes = new ArrayList<>();
        for (ExtendedCsdlComplexType extComplexType : extendedComplexTypes) {
            csdlComplexTypes.add(extComplexType.asCsdlComplexType());
        }
        wrappedSchema.setComplexTypes(csdlComplexTypes);
    }

    @Deprecated
    public ExtendedCsdlSchema setComplexTypes(List<CsdlComplexType> complexTypes) {
        // 保留向后兼容，但建议使用setExtendedComplexTypes
        wrappedSchema.setComplexTypes(complexTypes);
        // 同步到Extended对象
        syncComplexTypesFromWrapped();
        return this;
    }

    /**
     * 从原始数据同步到Extended复杂类型
     */
    private void syncComplexTypesFromWrapped() {
        extendedComplexTypes.clear();
        if (wrappedSchema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : wrappedSchema.getComplexTypes()) {
                ExtendedCsdlComplexType extComplexType = ExtendedCsdlComplexType.fromCsdlComplexType(complexType);
                extendedComplexTypes.add(extComplexType);
            }
        }
    }

    public List<CsdlEnumType> getEnumTypes() {
        return wrappedSchema.getEnumTypes();
    }

    /**
     * 获取Extended枚举类型列表
     */
    public List<ExtendedCsdlEnumType> getExtendedEnumTypes() {
        return new ArrayList<>(extendedEnumTypes);
    }

    /**
     * 添加Extended枚举类型，同时更新原始数据
     */
    public ExtendedCsdlSchema addExtendedEnumType(ExtendedCsdlEnumType extendedEnumType) {
        if (extendedEnumType != null) {
            extendedEnumTypes.add(extendedEnumType);
            syncEnumTypesToWrapped();
        }
        return this;
    }

    /**
     * 同步Extended枚举类型到原始数据
     */
    private void syncEnumTypesToWrapped() {
        List<CsdlEnumType> csdlEnumTypes = new ArrayList<>();
        for (ExtendedCsdlEnumType extEnumType : extendedEnumTypes) {
            csdlEnumTypes.add(extEnumType.asCsdlEnumType());
        }
        wrappedSchema.setEnumTypes(csdlEnumTypes);
    }

    @Deprecated
    public ExtendedCsdlSchema setEnumTypes(List<CsdlEnumType> enumTypes) {
        wrappedSchema.setEnumTypes(enumTypes);
        return this;
    }

    public List<CsdlTypeDefinition> getTypeDefinitions() {
        return wrappedSchema.getTypeDefinitions();
    }

    /**
     * 获取Extended类型定义列表
     */
    public List<ExtendedCsdlTypeDefinition> getExtendedTypeDefinitions() {
        return new ArrayList<>(extendedTypeDefinitions);
    }

    /**
     * 添加Extended类型定义，同时更新原始数据
     */
    public ExtendedCsdlSchema addExtendedTypeDefinition(ExtendedCsdlTypeDefinition extendedTypeDefinition) {
        if (extendedTypeDefinition != null) {
            extendedTypeDefinitions.add(extendedTypeDefinition);
            syncTypeDefinitionsToWrapped();
        }
        return this;
    }

    /**
     * 同步Extended类型定义到原始数据
     */
    private void syncTypeDefinitionsToWrapped() {
        List<CsdlTypeDefinition> csdlTypeDefinitions = new ArrayList<>();
        for (ExtendedCsdlTypeDefinition extTypeDefinition : extendedTypeDefinitions) {
            csdlTypeDefinitions.add(extTypeDefinition.asCsdlTypeDefinition());
        }
        wrappedSchema.setTypeDefinitions(csdlTypeDefinitions);
    }

    @Deprecated
    public ExtendedCsdlSchema setTypeDefinitions(List<CsdlTypeDefinition> typeDefinitions) {
        wrappedSchema.setTypeDefinitions(typeDefinitions);
        return this;
    }

    public List<CsdlAction> getActions() {
        return wrappedSchema.getActions();
    }

    /**
     * 获取Extended操作列表
     */
    public List<ExtendedCsdlAction> getExtendedActions() {
        return new ArrayList<>(extendedActions);
    }

    /**
     * 添加Extended操作，同时更新原始数据
     */
    public ExtendedCsdlSchema addExtendedAction(ExtendedCsdlAction extendedAction) {
        if (extendedAction != null) {
            extendedActions.add(extendedAction);
            syncActionsToWrapped();
        }
        return this;
    }

    /**
     * 同步Extended操作到原始数据
     */
    private void syncActionsToWrapped() {
        List<CsdlAction> csdlActions = new ArrayList<>();
        for (ExtendedCsdlAction extAction : extendedActions) {
            csdlActions.add(extAction.asCsdlAction());
        }
        wrappedSchema.setActions(csdlActions);
    }

    @Deprecated
    public ExtendedCsdlSchema setActions(List<CsdlAction> actions) {
        wrappedSchema.setActions(actions);
        return this;
    }

    public List<CsdlFunction> getFunctions() {
        return wrappedSchema.getFunctions();
    }

    /**
     * 获取Extended函数列表
     */
    public List<ExtendedCsdlFunction> getExtendedFunctions() {
        return new ArrayList<>(extendedFunctions);
    }

    /**
     * 添加Extended函数，同时更新原始数据
     */
    public ExtendedCsdlSchema addExtendedFunction(ExtendedCsdlFunction extendedFunction) {
        if (extendedFunction != null) {
            extendedFunctions.add(extendedFunction);
            syncFunctionsToWrapped();
        }
        return this;
    }

    /**
     * 同步Extended函数到原始数据
     */
    private void syncFunctionsToWrapped() {
        List<CsdlFunction> csdlFunctions = new ArrayList<>();
        for (ExtendedCsdlFunction extFunction : extendedFunctions) {
            csdlFunctions.add(extFunction.asCsdlFunction());
        }
        wrappedSchema.setFunctions(csdlFunctions);
    }

    @Deprecated
    public ExtendedCsdlSchema setFunctions(List<CsdlFunction> functions) {
        wrappedSchema.setFunctions(functions);
        return this;
    }

    public List<CsdlTerm> getTerms() {
        return wrappedSchema.getTerms();
    }

    /**
     * 获取Extended术语列表
     */
    public List<ExtendedCsdlTerm> getExtendedTerms() {
        return new ArrayList<>(extendedTerms);
    }

    /**
     * 添加Extended术语，同时更新原始数据
     */
    public ExtendedCsdlSchema addExtendedTerm(ExtendedCsdlTerm extendedTerm) {
        if (extendedTerm != null) {
            extendedTerms.add(extendedTerm);
            syncTermsToWrapped();
        }
        return this;
    }

    /**
     * 同步Extended术语到原始数据
     */
    private void syncTermsToWrapped() {
        List<CsdlTerm> csdlTerms = new ArrayList<>();
        for (ExtendedCsdlTerm extTerm : extendedTerms) {
            csdlTerms.add(extTerm.asCsdlTerm());
        }
        wrappedSchema.setTerms(csdlTerms);
    }

    @Deprecated
    public ExtendedCsdlSchema setTerms(List<CsdlTerm> terms) {
        wrappedSchema.setTerms(terms);
        return this;
    }

    public CsdlEntityContainer getEntityContainer() {
        return wrappedSchema.getEntityContainer();
    }

    /**
     * 获取Extended实体容器
     */
    public ExtendedCsdlEntityContainer getExtendedEntityContainer() {
        return extendedEntityContainer;
    }

    /**
     * 设置Extended实体容器，同时更新原始数据
     */
    public ExtendedCsdlSchema setExtendedEntityContainer(ExtendedCsdlEntityContainer extendedEntityContainer) {
        this.extendedEntityContainer = extendedEntityContainer;
        if (extendedEntityContainer != null) {
            wrappedSchema.setEntityContainer(extendedEntityContainer.asCsdlEntityContainer());
        } else {
            wrappedSchema.setEntityContainer(null);
        }
        return this;
    }

    @Deprecated
    public ExtendedCsdlSchema setEntityContainer(CsdlEntityContainer entityContainer) {
        wrappedSchema.setEntityContainer(entityContainer);
        // 同步到Extended对象
        if (entityContainer != null) {
            this.extendedEntityContainer = ExtendedCsdlEntityContainer.fromCsdlEntityContainer(entityContainer);
        } else {
            this.extendedEntityContainer = null;
        }
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedSchema.getAnnotations();
    }

    public ExtendedCsdlSchema setAnnotations(List<CsdlAnnotation> annotations) {
        wrappedSchema.setAnnotations(annotations);
        return this;
    }

    // ==================== Extended Element 接口实现 ====================

    @Override
    public String getElementId() {
        if (wrappedSchema.getNamespace() != null) {
            return wrappedSchema.getNamespace();
        }
        return "Schema_" + hashCode();
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

    // ==================== 扩展功能 ====================

    public String getSourcePath() {
        return sourcePath;
    }

    public ExtendedCsdlSchema setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    public List<String> getReferencedNamespaces() {
        return referencedNamespaces;
    }

    public ExtendedCsdlSchema setReferencedNamespaces(List<String> referencedNamespaces) {
        this.referencedNamespaces = referencedNamespaces != null ? referencedNamespaces : new ArrayList<>();
        return this;
    }

    public ExtendedCsdlSchema addReferencedNamespace(String namespace) {
        if (namespace != null && !referencedNamespaces.contains(namespace)) {
            referencedNamespaces.add(namespace);
        }
        return this;
    }

    public boolean isExtended() {
        return isExtended;
    }

    public ExtendedCsdlSchema setExtended(boolean extended) {
        isExtended = extended;
        return this;
    }

    // ==================== 便利方法 ====================

    @Override
    public String toString() {
        return "ExtendedCsdlSchema{" +
                "namespace='" + getNamespace() + '\'' +
                ", alias='" + getAlias() + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", isExtended=" + isExtended +
                '}';
    }
}
