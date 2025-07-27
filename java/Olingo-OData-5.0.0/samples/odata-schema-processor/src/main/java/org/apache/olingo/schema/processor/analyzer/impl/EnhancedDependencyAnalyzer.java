package org.apache.olingo.schema.processor.analyzer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.analyzer.DependencyAnalyzer;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 增强的依赖分析器实现
 * 支持递归依赖分析、继承链分析、annotation处理等高级功能
 */
public class EnhancedDependencyAnalyzer implements DependencyAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedDependencyAnalyzer.class);
    
    private final SchemaRepository repository;
    private final Map<String, Set<String>> dependencyCache = new HashMap<>();
    private final Map<String, Set<String>> reverseDependencyCache = new HashMap<>();
    
    public EnhancedDependencyAnalyzer(SchemaRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public Set<String> getDirectDependencies(String fullyQualifiedName) {
        logger.debug("Analyzing direct dependencies for: {}", fullyQualifiedName);
        
        Set<String> dependencies = new HashSet<>();
        
        // 解析类型和namespace
        String[] parts = fullyQualifiedName.split("\\.");
        if (parts.length < 2) {
            logger.warn("Invalid fully qualified name: {}", fullyQualifiedName);
            return dependencies;
        }
        
        String typeName = parts[parts.length - 1];
        String namespace = String.join(".", Arrays.copyOf(parts, parts.length - 1));
        
        CsdlSchema schema = repository.getSchema(namespace);
        if (schema == null) {
            logger.warn("Schema not found for namespace: {}", namespace);
            return dependencies;
        }
        
        // 检查EntityType
        dependencies.addAll(analyzeEntityTypeDependencies(schema, typeName));
        
        // 检查ComplexType
        dependencies.addAll(analyzeComplexTypeDependencies(schema, typeName));
        
        // 检查EnumType (EnumType通常没有依赖，但可能有annotation)
        dependencies.addAll(analyzeEnumTypeDependencies(schema, typeName));
        
        // 检查Action
        dependencies.addAll(analyzeActionDependencies(schema, typeName));
        
        // 检查Function
        dependencies.addAll(analyzeFunctionDependencies(schema, typeName));
        
        // 检查EntitySet (在EntityContainer中)
        dependencies.addAll(analyzeEntitySetDependencies(schema, typeName));
        
        return dependencies;
    }
    
    @Override
    public Set<String> getRecursiveDependencies(String fullyQualifiedName) {
        if (dependencyCache.containsKey(fullyQualifiedName)) {
            return new HashSet<>(dependencyCache.get(fullyQualifiedName));
        }
        
        Set<String> allDependencies = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> toProcess = new LinkedList<>();
        
        // 包含自身
        allDependencies.add(fullyQualifiedName);
        
        toProcess.add(fullyQualifiedName);
        visited.add(fullyQualifiedName);
        
        while (!toProcess.isEmpty()) {
            String current = toProcess.poll();
            Set<String> directDeps = getDirectDependencies(current);
            
            for (String dep : directDeps) {
                if (!visited.contains(dep)) {
                    visited.add(dep);
                    toProcess.add(dep);
                    allDependencies.add(dep);
                }
            }
        }
        
        dependencyCache.put(fullyQualifiedName, allDependencies);
        return new HashSet<>(allDependencies);
    }
    
    @Override
    public Set<String> getReverseDependencies(String fullyQualifiedName) {
        if (reverseDependencyCache.containsKey(fullyQualifiedName)) {
            return new HashSet<>(reverseDependencyCache.get(fullyQualifiedName));
        }
        
        Set<String> reverseDeps = new HashSet<>();
        
        // 遍历所有schema中的所有元素，查找依赖于指定元素的
        for (CsdlSchema schema : repository.getAllSchemas()) {
            String namespace = schema.getNamespace();
            
            // 检查EntityType
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String entityFqn = namespace + "." + entityType.getName();
                    Set<String> deps = getDirectDependencies(entityFqn);
                    if (deps.contains(fullyQualifiedName)) {
                        reverseDeps.add(entityFqn);
                    }
                }
            }
            
            // 检查ComplexType
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    String complexFqn = namespace + "." + complexType.getName();
                    Set<String> deps = getDirectDependencies(complexFqn);
                    if (deps.contains(fullyQualifiedName)) {
                        reverseDeps.add(complexFqn);
                    }
                }
            }
            
            // 检查Action和Function
            if (schema.getActions() != null) {
                for (CsdlAction action : schema.getActions()) {
                    String actionFqn = namespace + "." + action.getName();
                    Set<String> deps = getDirectDependencies(actionFqn);
                    if (deps.contains(fullyQualifiedName)) {
                        reverseDeps.add(actionFqn);
                    }
                }
            }
            
            if (schema.getFunctions() != null) {
                for (CsdlFunction function : schema.getFunctions()) {
                    String functionFqn = namespace + "." + function.getName();
                    Set<String> deps = getDirectDependencies(functionFqn);
                    if (deps.contains(fullyQualifiedName)) {
                        reverseDeps.add(functionFqn);
                    }
                }
            }
        }
        
        reverseDependencyCache.put(fullyQualifiedName, reverseDeps);
        return new HashSet<>(reverseDeps);
    }
    
    @Override
    public List<DependencyCycle> detectCircularDependencies() {
        List<DependencyCycle> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        // 获取所有元素
        Set<String> allElements = getAllElements();
        
        for (String element : allElements) {
            if (!visited.contains(element)) {
                List<String> path = new ArrayList<>();
                detectCycleDFS(element, visited, recursionStack, path, cycles);
            }
        }
        
        return cycles;
    }
    
    @Override
    public boolean hasCircularDependency(String fullyQualifiedName) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> path = new ArrayList<>();
        List<DependencyCycle> cycles = new ArrayList<>();
        
        return detectCycleDFS(fullyQualifiedName, visited, recursionStack, path, cycles);
    }
    
    @Override
    public Map<String, Set<String>> getDependencyGraph() {
        Map<String, Set<String>> graph = new HashMap<>();
        Set<String> allElements = getAllElements();
        
        for (String element : allElements) {
            graph.put(element, getDirectDependencies(element));
        }
        
        return graph;
    }
    
    @Override
    public Map<String, Set<String>> getReverseDependencyGraph() {
        Map<String, Set<String>> reverseGraph = new HashMap<>();
        Set<String> allElements = getAllElements();
        
        for (String element : allElements) {
            reverseGraph.put(element, getReverseDependencies(element));
        }
        
        return reverseGraph;
    }
    
    @Override
    public List<Set<String>> getDependencyLayers() {
        List<Set<String>> layers = new ArrayList<>();
        Set<String> allElements = getAllElements();
        Set<String> processed = new HashSet<>();
        
        while (processed.size() < allElements.size()) {
            Set<String> currentLayer = new HashSet<>();
            
            for (String element : allElements) {
                if (!processed.contains(element)) {
                    Set<String> deps = getDirectDependencies(element);
                    deps.removeAll(processed); // 移除已处理的依赖
                    
                    if (deps.isEmpty()) {
                        currentLayer.add(element);
                    }
                }
            }
            
            if (currentLayer.isEmpty()) {
                // 存在循环依赖，强制添加剩余元素到当前层
                for (String element : allElements) {
                    if (!processed.contains(element)) {
                        currentLayer.add(element);
                    }
                }
            }
            
            layers.add(currentLayer);
            processed.addAll(currentLayer);
        }
        
        return layers;
    }
    
    @Override
    public DependencyStatistics getDependencyStatistics() {
        Set<String> allElements = getAllElements();
        int totalElements = allElements.size();
        int elementsWithDependencies = 0;
        int maxDepth = 0;
        double totalDependencies = 0;
        Map<String, Integer> dependencyCountByNamespace = new HashMap<>();
        
        for (String element : allElements) {
            Set<String> deps = getDirectDependencies(element);
            if (!deps.isEmpty()) {
                elementsWithDependencies++;
            }
            
            totalDependencies += deps.size();
            
            // 计算最大深度
            Set<String> recursiveDeps = getRecursiveDependencies(element);
            maxDepth = Math.max(maxDepth, recursiveDeps.size());
            
            // 按namespace统计
            String namespace = getNamespace(element);
            dependencyCountByNamespace.merge(namespace, deps.size(), Integer::sum);
        }
        
        double averageDependencies = totalElements > 0 ? totalDependencies / totalElements : 0;
        int elementsWithoutDependencies = totalElements - elementsWithDependencies;
        
        return new DependencyStatistics(
            totalElements,
            elementsWithDependencies,
            elementsWithoutDependencies,
            maxDepth,
            averageDependencies,
            dependencyCountByNamespace
        );
    }
    
    @Override
    public ImpactAnalysis analyzeImpact(String fullyQualifiedName) {
        Set<String> directlyAffected = getReverseDependencies(fullyQualifiedName);
        Set<String> indirectlyAffected = new HashSet<>();
        
        // 递归查找间接影响
        Set<String> toProcess = new HashSet<>(directlyAffected);
        Set<String> processed = new HashSet<>();
        
        while (!toProcess.isEmpty()) {
            String current = toProcess.iterator().next();
            toProcess.remove(current);
            processed.add(current);
            
            Set<String> currentReverseDeps = getReverseDependencies(current);
            for (String dep : currentReverseDeps) {
                if (!processed.contains(dep) && !directlyAffected.contains(dep)) {
                    indirectlyAffected.add(dep);
                    toProcess.add(dep);
                }
            }
        }
        
        int totalAffected = directlyAffected.size() + indirectlyAffected.size();
        
        // 按namespace统计影响
        Map<String, Integer> affectedByNamespace = new HashMap<>();
        Set<String> allAffected = new HashSet<>();
        allAffected.addAll(directlyAffected);
        allAffected.addAll(indirectlyAffected);
        
        for (String affected : allAffected) {
            String namespace = getNamespace(affected);
            affectedByNamespace.merge(namespace, 1, Integer::sum);
        }
        
        return new ImpactAnalysis(
            fullyQualifiedName,
            directlyAffected,
            indirectlyAffected,
            totalAffected,
            affectedByNamespace
        );
    }
    
    /**
     * 获取EntityType的继承链
     */
    public List<String> getInheritanceChain(String entityTypeFqn) {
        List<String> chain = new ArrayList<>();
        String current = entityTypeFqn;
        Set<String> visited = new HashSet<>();
        
        while (current != null && !visited.contains(current)) {
            visited.add(current);
            chain.add(current);
            
            String namespace = getNamespace(current);
            String typeName = getTypeName(current);
            
            CsdlSchema schema = repository.getSchema(namespace);
            if (schema != null && schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    if (entityType.getName().equals(typeName)) {
                        current = entityType.getBaseType();
                        break;
                    }
                }
            } else {
                break;
            }
        }
        
        return chain;
    }
    
    /**
     * 获取ComplexType的继承链
     */
    public List<String> getComplexTypeInheritanceChain(String complexTypeFqn) {
        List<String> chain = new ArrayList<>();
        String current = complexTypeFqn;
        Set<String> visited = new HashSet<>();
        
        while (current != null && !visited.contains(current)) {
            visited.add(current);
            chain.add(current);
            
            String namespace = getNamespace(current);
            String typeName = getTypeName(current);
            
            CsdlSchema schema = repository.getSchema(namespace);
            if (schema != null && schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    if (complexType.getName().equals(typeName)) {
                        current = complexType.getBaseType();
                        break;
                    }
                }
            } else {
                break;
            }
        }
        
        return chain;
    }
    
    // 私有辅助方法
    private Set<String> analyzeEntityTypeDependencies(CsdlSchema schema, String typeName) {
        Set<String> dependencies = new HashSet<>();
        
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                if (entityType.getName().equals(typeName)) {
                    // 基类型依赖
                    if (entityType.getBaseType() != null) {
                        dependencies.add(entityType.getBaseType());
                    }
                    
                    // 属性类型依赖
                    if (entityType.getProperties() != null) {
                        for (CsdlProperty property : entityType.getProperties()) {
                            String propType = extractTypeFromEdmType(property.getType());
                            if (!isPrimitiveType(propType)) {
                                dependencies.add(propType);
                            }
                        }
                    }
                    
                    // 导航属性依赖
                    if (entityType.getNavigationProperties() != null) {
                        for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                            String navType = extractTypeFromEdmType(navProp.getType());
                            dependencies.add(navType);
                        }
                    }
                    
                    // Annotation依赖
                    dependencies.addAll(analyzeAnnotationDependencies(entityType.getAnnotations()));
                    
                    break;
                }
            }
        }
        
        return dependencies;
    }
    
    private Set<String> analyzeComplexTypeDependencies(CsdlSchema schema, String typeName) {
        Set<String> dependencies = new HashSet<>();
        
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                if (complexType.getName().equals(typeName)) {
                    // 基类型依赖
                    if (complexType.getBaseType() != null) {
                        dependencies.add(complexType.getBaseType());
                    }
                    
                    // 属性类型依赖
                    if (complexType.getProperties() != null) {
                        for (CsdlProperty property : complexType.getProperties()) {
                            String propType = extractTypeFromEdmType(property.getType());
                            if (!isPrimitiveType(propType)) {
                                dependencies.add(propType);
                            }
                        }
                    }
                    
                    // 导航属性依赖
                    if (complexType.getNavigationProperties() != null) {
                        for (CsdlNavigationProperty navProp : complexType.getNavigationProperties()) {
                            String navType = extractTypeFromEdmType(navProp.getType());
                            dependencies.add(navType);
                        }
                    }
                    
                    // Annotation依赖
                    dependencies.addAll(analyzeAnnotationDependencies(complexType.getAnnotations()));
                    
                    break;
                }
            }
        }
        
        return dependencies;
    }
    
    private Set<String> analyzeEnumTypeDependencies(CsdlSchema schema, String typeName) {
        Set<String> dependencies = new HashSet<>();
        
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                if (enumType.getName().equals(typeName)) {
                    // EnumType主要检查annotation依赖
                    dependencies.addAll(analyzeAnnotationDependencies(enumType.getAnnotations()));
                    break;
                }
            }
        }
        
        return dependencies;
    }
    
    private Set<String> analyzeActionDependencies(CsdlSchema schema, String actionName) {
        Set<String> dependencies = new HashSet<>();
        
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                if (action.getName().equals(actionName)) {
                    // 参数类型依赖
                    if (action.getParameters() != null) {
                        for (CsdlParameter parameter : action.getParameters()) {
                            String paramType = extractTypeFromEdmType(parameter.getType());
                            if (!isPrimitiveType(paramType)) {
                                dependencies.add(paramType);
                            }
                        }
                    }
                    
                    // 返回类型依赖
                    if (action.getReturnType() != null) {
                        String returnType = extractTypeFromEdmType(action.getReturnType().getType());
                        if (!isPrimitiveType(returnType)) {
                            dependencies.add(returnType);
                        }
                    }
                    
                    // Annotation依赖
                    dependencies.addAll(analyzeAnnotationDependencies(action.getAnnotations()));
                    
                    break;
                }
            }
        }
        
        return dependencies;
    }
    
    private Set<String> analyzeFunctionDependencies(CsdlSchema schema, String functionName) {
        Set<String> dependencies = new HashSet<>();
        
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                if (function.getName().equals(functionName)) {
                    // 参数类型依赖
                    if (function.getParameters() != null) {
                        for (CsdlParameter parameter : function.getParameters()) {
                            String paramType = extractTypeFromEdmType(parameter.getType());
                            if (!isPrimitiveType(paramType)) {
                                dependencies.add(paramType);
                            }
                        }
                    }
                    
                    // 返回类型依赖
                    if (function.getReturnType() != null) {
                        String returnType = extractTypeFromEdmType(function.getReturnType().getType());
                        if (!isPrimitiveType(returnType)) {
                            dependencies.add(returnType);
                        }
                    }
                    
                    // Annotation依赖
                    dependencies.addAll(analyzeAnnotationDependencies(function.getAnnotations()));
                    
                    break;
                }
            }
        }
        
        return dependencies;
    }
    
    private Set<String> analyzeEntitySetDependencies(CsdlSchema schema, String entitySetName) {
        Set<String> dependencies = new HashSet<>();
        
        if (schema.getEntityContainer() != null && schema.getEntityContainer().getEntitySets() != null) {
            for (CsdlEntitySet entitySet : schema.getEntityContainer().getEntitySets()) {
                if (entitySet.getName().equals(entitySetName)) {
                    // EntityType依赖
                    dependencies.add(entitySet.getType());
                    
                    // Annotation依赖
                    dependencies.addAll(analyzeAnnotationDependencies(entitySet.getAnnotations()));
                    
                    break;
                }
            }
        }
        
        return dependencies;
    }
    
    private Set<String> analyzeAnnotationDependencies(List<CsdlAnnotation> annotations) {
        Set<String> dependencies = new HashSet<>();
        
        if (annotations != null) {
            for (CsdlAnnotation annotation : annotations) {
                // Annotation的Term可能引用其他namespace的定义
                if (annotation.getTerm() != null) {
                    String term = annotation.getTerm();
                    if (term.contains(".") && !isPrimitiveType(term)) {
                        dependencies.add(term);
                    }
                }
                
                // 递归分析annotation值中的类型引用
                dependencies.addAll(analyzeAnnotationValueDependencies(annotation));
            }
        }
        
        return dependencies;
    }
    
    private Set<String> analyzeAnnotationValueDependencies(CsdlAnnotation annotation) {
        Set<String> dependencies = new HashSet<>();
        
        // 这里需要根据annotation的值类型进行分析
        // 例如Path表达式可能引用属性或导航属性
        // Record类型可能包含PropertyValue引用其他类型
        
        // 简化实现：检查常见的类型引用模式
        if (annotation.getExpression() != null) {
            // 这里可以扩展以支持更复杂的表达式分析
            // 目前返回空集合
        }
        
        return dependencies;
    }
    
    private boolean detectCycleDFS(String element, Set<String> visited, Set<String> recursionStack,
                                  List<String> path, List<DependencyCycle> cycles) {
        if (recursionStack.contains(element)) {
            // 发现循环
            int cycleStart = path.indexOf(element);
            List<String> cycle = new ArrayList<>(path.subList(cycleStart, path.size()));
            cycle.add(element);
            
            String description = String.format("Circular dependency detected: %s", 
                String.join(" -> ", cycle));
            cycles.add(new DependencyCycle(cycle, description));
            return true;
        }
        
        if (visited.contains(element)) {
            return false;
        }
        
        visited.add(element);
        recursionStack.add(element);
        path.add(element);
        
        Set<String> dependencies = getDirectDependencies(element);
        for (String dep : dependencies) {
            if (detectCycleDFS(dep, visited, recursionStack, path, cycles)) {
                return true;
            }
        }
        
        recursionStack.remove(element);
        path.remove(path.size() - 1);
        
        return false;
    }
    
    private Set<String> getAllElements() {
        Set<String> allElements = new HashSet<>();
        
        for (CsdlSchema schema : repository.getAllSchemas()) {
            String namespace = schema.getNamespace();
            
            // EntityType
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    allElements.add(namespace + "." + entityType.getName());
                }
            }
            
            // ComplexType
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    allElements.add(namespace + "." + complexType.getName());
                }
            }
            
            // EnumType
            if (schema.getEnumTypes() != null) {
                for (CsdlEnumType enumType : schema.getEnumTypes()) {
                    allElements.add(namespace + "." + enumType.getName());
                }
            }
            
            // Action
            if (schema.getActions() != null) {
                for (CsdlAction action : schema.getActions()) {
                    allElements.add(namespace + "." + action.getName());
                }
            }
            
            // Function
            if (schema.getFunctions() != null) {
                for (CsdlFunction function : schema.getFunctions()) {
                    allElements.add(namespace + "." + function.getName());
                }
            }
            
            // EntitySet
            if (schema.getEntityContainer() != null && schema.getEntityContainer().getEntitySets() != null) {
                for (CsdlEntitySet entitySet : schema.getEntityContainer().getEntitySets()) {
                    allElements.add(namespace + "." + entitySet.getName());
                }
            }
        }
        
        return allElements;
    }
    
    private String extractTypeFromEdmType(String edmType) {
        if (edmType == null) {
            return null;
        }
        
        // 处理Collection类型: Collection(Namespace.Type) -> Namespace.Type
        if (edmType.startsWith("Collection(") && edmType.endsWith(")")) {
            return edmType.substring(11, edmType.length() - 1);
        }
        
        return edmType;
    }
    
    private boolean isPrimitiveType(String type) {
        return type != null && type.startsWith("Edm.");
    }
    
    private String getNamespace(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return null;
        }
        
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot > 0 ? fullyQualifiedName.substring(0, lastDot) : fullyQualifiedName;
    }
    
    private String getTypeName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return null;
        }
        
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        dependencyCache.clear();
        reverseDependencyCache.clear();
    }
}
