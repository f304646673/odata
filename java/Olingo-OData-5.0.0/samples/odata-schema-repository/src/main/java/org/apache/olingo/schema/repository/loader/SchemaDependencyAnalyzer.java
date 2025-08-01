package org.apache.olingo.schema.repository.loader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.schema.repository.model.SchemaDependencyNode;
import org.apache.olingo.schema.repository.model.SchemaRepositoryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema依赖关系分析器
 * 分析Schema中元素之间的依赖关系并构建依赖图
 */
public class SchemaDependencyAnalyzer {
    
    private static final Logger LOG = LoggerFactory.getLogger(SchemaDependencyAnalyzer.class);
    
    private final SchemaRepositoryContext context;
    
    public SchemaDependencyAnalyzer(SchemaRepositoryContext context) {
        this.context = context;
    }
    
    /**
     * 分析Schema的所有依赖关系
     */
    public void analyzeSchema(CsdlSchema schema) {
        if (schema == null || schema.getNamespace() == null) {
            return;
        }
        
        LOG.debug("Analyzing dependencies for schema: {}", schema.getNamespace());
        
        // 分析EntityTypes
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                analyzeEntityType(schema.getNamespace(), entityType);
            }
        }
        
        // 分析ComplexTypes
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                analyzeComplexType(schema.getNamespace(), complexType);
            }
        }
        
        // 分析Actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                analyzeAction(schema.getNamespace(), action);
            }
        }
        
        // 分析Functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                analyzeFunction(schema.getNamespace(), function);
            }
        }
        
        // 分析EntityContainer
        if (schema.getEntityContainer() != null) {
            analyzeEntityContainer(schema.getNamespace(), schema.getEntityContainer());
        }
        
        LOG.debug("Completed dependency analysis for schema: {}", schema.getNamespace());
    }
    
    /**
     * 分析EntityType依赖关系
     */
    private void analyzeEntityType(String namespace, CsdlEntityType entityType) {
        FullQualifiedName entityTypeFqn = new FullQualifiedName(namespace, entityType.getName());
        
        SchemaDependencyNode entityTypeNode = getOrCreateDependencyNode(
            entityTypeFqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
        
        // 分析基类型依赖
        if (entityType.getBaseType() != null) {
            FullQualifiedName baseTypeFqn = parseFullQualifiedName(entityType.getBaseType());
            if (baseTypeFqn != null) {
                SchemaDependencyNode baseTypeNode = getOrCreateDependencyNode(
                    baseTypeFqn, SchemaDependencyNode.DependencyType.BASE_TYPE);
                entityTypeNode.addDependency(baseTypeNode);
            }
        }
        
        // 分析Properties依赖
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                analyzePropertyDependencies(entityTypeNode, property);
            }
        }
        
        // 分析NavigationProperties依赖
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                analyzeNavigationPropertyDependencies(entityTypeNode, navProp);
            }
        }
    }
    
    /**
     * 分析ComplexType依赖关系
     */
    private void analyzeComplexType(String namespace, CsdlComplexType complexType) {
        FullQualifiedName complexTypeFqn = new FullQualifiedName(namespace, complexType.getName());
        
        SchemaDependencyNode complexTypeNode = getOrCreateDependencyNode(
            complexTypeFqn, SchemaDependencyNode.DependencyType.COMPLEX_TYPE);
        
        // 分析基类型依赖
        if (complexType.getBaseType() != null) {
            FullQualifiedName baseTypeFqn = parseFullQualifiedName(complexType.getBaseType());
            if (baseTypeFqn != null) {
                SchemaDependencyNode baseTypeNode = getOrCreateDependencyNode(
                    baseTypeFqn, SchemaDependencyNode.DependencyType.BASE_TYPE);
                complexTypeNode.addDependency(baseTypeNode);
            }
        }
        
        // 分析Properties依赖
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                analyzePropertyDependencies(complexTypeNode, property);
            }
        }
        
        // 分析NavigationProperties依赖
        if (complexType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : complexType.getNavigationProperties()) {
                analyzeNavigationPropertyDependencies(complexTypeNode, navProp);
            }
        }
    }
    
    /**
     * 分析Action依赖关系
     */
    private void analyzeAction(String namespace, CsdlAction action) {
        FullQualifiedName actionFqn = new FullQualifiedName(namespace, action.getName());
        
        SchemaDependencyNode actionNode = getOrCreateDependencyNode(
            actionFqn, SchemaDependencyNode.DependencyType.ACTION);
        
        // 分析参数依赖
        if (action.getParameters() != null) {
            for (CsdlParameter parameter : action.getParameters()) {
                analyzeParameterDependencies(actionNode, parameter);
            }
        }
        
        // 分析返回类型依赖
        if (action.getReturnType() != null) {
            analyzeReturnTypeDependencies(actionNode, action.getReturnType());
        }
    }
    
    /**
     * 分析Function依赖关系
     */
    private void analyzeFunction(String namespace, CsdlFunction function) {
        FullQualifiedName functionFqn = new FullQualifiedName(namespace, function.getName());
        
        SchemaDependencyNode functionNode = getOrCreateDependencyNode(
            functionFqn, SchemaDependencyNode.DependencyType.FUNCTION);
        
        // 分析参数依赖
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                analyzeParameterDependencies(functionNode, parameter);
            }
        }
        
        // 分析返回类型依赖
        if (function.getReturnType() != null) {
            analyzeReturnTypeDependencies(functionNode, function.getReturnType());
        }
    }
    
    /**
     * 分析EntityContainer依赖关系
     */
    private void analyzeEntityContainer(String namespace, CsdlEntityContainer container) {
        // 分析EntitySets
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                analyzeEntitySet(namespace, entitySet);
            }
        }
        
        // 分析Singletons
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                analyzeSingleton(namespace, singleton);
            }
        }
        
        // 分析ActionImports
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                analyzeActionImport(namespace, actionImport);
            }
        }
        
        // 分析FunctionImports
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                analyzeFunctionImport(namespace, functionImport);
            }
        }
    }
    
    /**
     * 分析EntitySet依赖关系
     */
    private void analyzeEntitySet(String namespace, CsdlEntitySet entitySet) {
        FullQualifiedName entitySetFqn = new FullQualifiedName(namespace, entitySet.getName());
        
        SchemaDependencyNode entitySetNode = getOrCreateDependencyNode(
            entitySetFqn, SchemaDependencyNode.DependencyType.ENTITY_SET);
        
        // 分析EntityType依赖
        if (entitySet.getType() != null) {
            FullQualifiedName entityTypeFqn = parseFullQualifiedName(entitySet.getType());
            if (entityTypeFqn != null) {
                SchemaDependencyNode entityTypeNode = getOrCreateDependencyNode(
                    entityTypeFqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
                entitySetNode.addDependency(entityTypeNode);
            }
        }
        
        // 分析NavigationPropertyBindings依赖
        if (entitySet.getNavigationPropertyBindings() != null) {
            for (CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
                analyzeNavigationPropertyBinding(entitySetNode, binding);
            }
        }
    }
    
    /**
     * 分析Singleton依赖关系
     */
    private void analyzeSingleton(String namespace, CsdlSingleton singleton) {
        FullQualifiedName singletonFqn = new FullQualifiedName(namespace, singleton.getName());
        
        SchemaDependencyNode singletonNode = getOrCreateDependencyNode(
            singletonFqn, SchemaDependencyNode.DependencyType.SINGLETON);
        
        // 分析EntityType依赖
        if (singleton.getType() != null) {
            FullQualifiedName entityTypeFqn = parseFullQualifiedName(singleton.getType());
            if (entityTypeFqn != null) {
                SchemaDependencyNode entityTypeNode = getOrCreateDependencyNode(
                    entityTypeFqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
                singletonNode.addDependency(entityTypeNode);
            }
        }
        
        // 分析NavigationPropertyBindings依赖
        if (singleton.getNavigationPropertyBindings() != null) {
            for (CsdlNavigationPropertyBinding binding : singleton.getNavigationPropertyBindings()) {
                analyzeNavigationPropertyBinding(singletonNode, binding);
            }
        }
    }
    
    /**
     * 分析Property依赖关系
     */
    private void analyzePropertyDependencies(SchemaDependencyNode parentNode, CsdlProperty property) {
        if (property.getType() != null) {
            FullQualifiedName typeFqn = parseFullQualifiedName(property.getType());
            if (typeFqn != null && !isPrimitiveType(property.getType())) {
                SchemaDependencyNode typeNode = getOrCreateDependencyNode(
                    typeFqn, SchemaDependencyNode.DependencyType.TYPE_REFERENCE);
                typeNode.setPropertyName(property.getName());
                parentNode.addDependency(typeNode);
            }
        }
    }
    
    /**
     * 分析NavigationProperty依赖关系
     */
    private void analyzeNavigationPropertyDependencies(SchemaDependencyNode parentNode, 
                                                      CsdlNavigationProperty navProp) {
        if (navProp.getType() != null) {
            FullQualifiedName typeFqn = parseFullQualifiedName(navProp.getType());
            if (typeFqn != null) {
                SchemaDependencyNode typeNode = getOrCreateDependencyNode(
                    typeFqn, SchemaDependencyNode.DependencyType.NAVIGATION_PROPERTY);
                typeNode.setPropertyName(navProp.getName());
                parentNode.addDependency(typeNode);
            }
        }
    }
    
    /**
     * 分析Parameter依赖关系
     */
    private void analyzeParameterDependencies(SchemaDependencyNode parentNode, CsdlParameter parameter) {
        if (parameter.getType() != null) {
            FullQualifiedName typeFqn = parseFullQualifiedName(parameter.getType());
            if (typeFqn != null && !isPrimitiveType(parameter.getType())) {
                SchemaDependencyNode typeNode = getOrCreateDependencyNode(
                    typeFqn, SchemaDependencyNode.DependencyType.PARAMETER_TYPE);
                typeNode.setPropertyName(parameter.getName());
                parentNode.addDependency(typeNode);
            }
        }
    }
    
    /**
     * 分析ReturnType依赖关系
     */
    private void analyzeReturnTypeDependencies(SchemaDependencyNode parentNode, CsdlReturnType returnType) {
        if (returnType.getType() != null) {
            FullQualifiedName typeFqn = parseFullQualifiedName(returnType.getType());
            if (typeFqn != null && !isPrimitiveType(returnType.getType())) {
                SchemaDependencyNode typeNode = getOrCreateDependencyNode(
                    typeFqn, SchemaDependencyNode.DependencyType.RETURN_TYPE);
                parentNode.addDependency(typeNode);
            }
        }
    }
    
    /**
     * 分析ActionImport依赖关系
     */
    private void analyzeActionImport(String namespace, CsdlActionImport actionImport) {
        FullQualifiedName actionImportFqn = new FullQualifiedName(namespace, actionImport.getName());
        
        SchemaDependencyNode actionImportNode = getOrCreateDependencyNode(
            actionImportFqn, SchemaDependencyNode.DependencyType.ACTION_IMPORT);
        
        // 分析Action依赖
        if (actionImport.getAction() != null) {
            FullQualifiedName actionFqn = parseFullQualifiedName(actionImport.getAction());
            if (actionFqn != null) {
                SchemaDependencyNode actionNode = getOrCreateDependencyNode(
                    actionFqn, SchemaDependencyNode.DependencyType.ACTION_REFERENCE);
                actionImportNode.addDependency(actionNode);
            }
        }
    }
    
    /**
     * 分析FunctionImport依赖关系
     */
    private void analyzeFunctionImport(String namespace, CsdlFunctionImport functionImport) {
        FullQualifiedName functionImportFqn = new FullQualifiedName(namespace, functionImport.getName());
        
        SchemaDependencyNode functionImportNode = getOrCreateDependencyNode(
            functionImportFqn, SchemaDependencyNode.DependencyType.FUNCTION_IMPORT);
        
        // 分析Function依赖
        if (functionImport.getFunction() != null) {
            FullQualifiedName functionFqn = parseFullQualifiedName(functionImport.getFunction());
            if (functionFqn != null) {
                SchemaDependencyNode functionNode = getOrCreateDependencyNode(
                    functionFqn, SchemaDependencyNode.DependencyType.FUNCTION_REFERENCE);
                functionImportNode.addDependency(functionNode);
            }
        }
    }
    
    /**
     * 分析NavigationPropertyBinding依赖关系
     */
    private void analyzeNavigationPropertyBinding(SchemaDependencyNode parentNode, 
                                                 CsdlNavigationPropertyBinding binding) {
        if (binding.getTarget() != null) {
            // NavigationPropertyBinding中的target通常是EntitySet或Singleton的名称
            String target = binding.getTarget();
            // 这里需要根据context来确定target的namespace
            // 暂时假设在同一个namespace中
            FullQualifiedName targetFqn = new FullQualifiedName(
                parentNode.getFullyQualifiedName().getNamespace(), target);
            
            SchemaDependencyNode targetNode = getOrCreateDependencyNode(
                targetFqn, SchemaDependencyNode.DependencyType.NAVIGATION_TARGET);
            parentNode.addDependency(targetNode);
        }
    }
    
    /**
     * 获取或创建依赖节点
     */
    private SchemaDependencyNode getOrCreateDependencyNode(FullQualifiedName fqn, 
                                                          SchemaDependencyNode.DependencyType type) {
        SchemaDependencyNode existingNode = context.getDependencyNode(fqn);
        if (existingNode != null) {
            return existingNode;
        }
        
        SchemaDependencyNode newNode = new SchemaDependencyNode(fqn, type);
        context.addDependencyNode(newNode);
        return newNode;
    }
    
    /**
     * 解析FullQualifiedName（支持别名）
     */
    private FullQualifiedName parseFullQualifiedName(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return null;
        }
        
        // 移除Collection包装
        String cleanType = typeString;
        if (cleanType.startsWith("Collection(") && cleanType.endsWith(")")) {
            cleanType = cleanType.substring("Collection(".length(), cleanType.length() - 1);
        }
        
        // 解析namespace和name
        int lastDotIndex = cleanType.lastIndexOf('.');
        if (lastDotIndex == -1) {
            // 没有namespace，可能是内置类型
            return null;
        }
        
        String namespaceOrAlias = cleanType.substring(0, lastDotIndex);
        String name = cleanType.substring(lastDotIndex + 1);
        
        // 解析namespace（处理别名）
        String namespace = context.resolveNamespace(namespaceOrAlias);
        if (namespace == null) {
            namespace = namespaceOrAlias; // 如果不是别名，使用原值
        }
        
        return new FullQualifiedName(namespace, name);
    }
    
    /**
     * 检查是否是内置类型
     */
    private boolean isPrimitiveType(String typeString) {
        if (typeString == null) {
            return false;
        }
        
        String cleanType = typeString;
        if (cleanType.startsWith("Collection(") && cleanType.endsWith(")")) {
            cleanType = cleanType.substring("Collection(".length(), cleanType.length() - 1);
        }
        
        // OData内置类型都以Edm.开头
        return cleanType.startsWith("Edm.");
    }
    
    /**
     * 检测循环依赖
     */
    public List<SchemaDependencyNode> detectCircularDependencies() {
        List<SchemaDependencyNode> circularNodes = new ArrayList<>();
        
        for (SchemaDependencyNode node : context.getAllDependencyNodes().values()) {
            if (node.hasCircularDependency()) {
                circularNodes.add(node);
            }
        }
        
        return circularNodes;
    }
    
    /**
     * 获取未解析的依赖（指向不存在的元素）
     */
    public List<SchemaDependencyNode> getUnresolvedDependencies() {
        List<SchemaDependencyNode> unresolvedNodes = new ArrayList<>();
        
        for (SchemaDependencyNode node : context.getAllDependencyNodes().values()) {
            FullQualifiedName fqn = node.getFullyQualifiedName();
            if (fqn != null && !isResolved(fqn, node.getDependencyType())) {
                unresolvedNodes.add(node);
            }
        }
        
        return unresolvedNodes;
    }
    
    /**
     * 检查依赖是否已解析
     */
    private boolean isResolved(FullQualifiedName fqn, SchemaDependencyNode.DependencyType type) {
        switch (type) {
            case ENTITY_TYPE:
                return context.getEntityType(fqn) != null;
            case COMPLEX_TYPE:
                return context.getComplexType(fqn) != null;
            case ACTION:
            case ACTION_REFERENCE:
                return context.getAction(fqn) != null;
            case FUNCTION:
            case FUNCTION_REFERENCE:
                return context.getFunction(fqn) != null;
            case TYPE_DEFINITION:
                return context.getTypeDefinition(fqn) != null;
            default:
                // 对于其他类型，暂时认为已解析
                return true;
        }
    }
}
