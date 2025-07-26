package org.apache.olingo.schemamanager.analyzer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.DependencyStatistics;
import org.apache.olingo.schemamanager.analyzer.DependencyTreeAnalyzer;
import org.apache.olingo.schemamanager.analyzer.DependencyTreeNode;
import org.apache.olingo.schemamanager.analyzer.DependencyTreeNode.ElementType;
import org.apache.olingo.schemamanager.analyzer.ImpactAnalysis;
import org.apache.olingo.schemamanager.analyzer.ImpactAnalysis.ImpactLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation of DependencyTreeAnalyzer
 */
@Component
public class DefaultDependencyTreeAnalyzer implements DependencyTreeAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultDependencyTreeAnalyzer.class);
    
    private final Map<String, DependencyTreeNode> nodeMap = new HashMap<>();
    private DependencyTreeNode rootNode;
    
    @Override
    public DependencyTreeNode buildDependencyTree(List<CsdlSchema> schemas) {
        if (schemas == null || schemas.isEmpty()) {
            return null;
        }
        
        logger.info("Building dependency tree for {} schemas", schemas.size());
        
        // Clear previous state
        nodeMap.clear();
        rootNode = new DependencyTreeNode("ROOT", "ROOT", ElementType.ENTITY_TYPE, null);
        
        // First pass: Create all nodes
        for (CsdlSchema schema : schemas) {
            createNodesForSchema(schema);
        }
        
        // Second pass: Establish dependencies
        for (CsdlSchema schema : schemas) {
            establishDependenciesForSchema(schema);
        }
        
        logger.info("Dependency tree built with {} nodes", nodeMap.size());
        return rootNode;
    }
    
    private void createNodesForSchema(CsdlSchema schema) {
        String namespace = schema.getNamespace();
        
        // Create entity type nodes
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                createEntityTypeNodes(namespace, entityType);
            }
        }
        
        // Create complex type nodes
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                createComplexTypeNodes(namespace, complexType);
            }
        }
        
        // Create enum type nodes
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                createEnumTypeNode(namespace, enumType);
            }
        }
        
        // Create action nodes
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                createActionNodes(namespace, action);
            }
        }
        
        // Create function nodes
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                createFunctionNodes(namespace, function);
            }
        }
    }
    
    private void createEntityTypeNodes(String namespace, CsdlEntityType entityType) {
        String fqn = namespace + "." + entityType.getName();
        DependencyTreeNode entityNode = new DependencyTreeNode(
            entityType.getName(), fqn, ElementType.ENTITY_TYPE, null, entityType);
        nodeMap.put(fqn, entityNode);
        
        // Create property nodes
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                String propFqn = fqn + "." + property.getName();
                DependencyTreeNode propNode = new DependencyTreeNode(
                    property.getName(), propFqn, ElementType.PROPERTY, fqn, property);
                nodeMap.put(propFqn, propNode);
                entityNode.addDependency(propNode);
            }
        }
        
        // Create navigation property nodes
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                String navPropFqn = fqn + "." + navProp.getName();
                DependencyTreeNode navPropNode = new DependencyTreeNode(
                    navProp.getName(), navPropFqn, ElementType.NAVIGATION_PROPERTY, fqn, navProp);
                nodeMap.put(navPropFqn, navPropNode);
                entityNode.addDependency(navPropNode);
            }
        }
        
        // Create key nodes
        if (entityType.getKey() != null && !entityType.getKey().isEmpty()) {
            for (CsdlPropertyRef keyRef : entityType.getKey()) {
                String keyFqn = fqn + ".Key." + keyRef.getName();
                DependencyTreeNode keyNode = new DependencyTreeNode(
                    keyRef.getName(), keyFqn, ElementType.KEY, fqn, keyRef);
                nodeMap.put(keyFqn, keyNode);
                entityNode.addDependency(keyNode);
            }
        }
        
        // Create base type node if exists
        if (entityType.getBaseType() != null) {
            String baseTypeFqn = fqn + ".BaseType";
            DependencyTreeNode baseTypeNode = new DependencyTreeNode(
                "BaseType", baseTypeFqn, ElementType.BASE_TYPE, fqn, entityType.getBaseType());
            nodeMap.put(baseTypeFqn, baseTypeNode);
            entityNode.addDependency(baseTypeNode);
        }
    }
    
    private void createComplexTypeNodes(String namespace, CsdlComplexType complexType) {
        String fqn = namespace + "." + complexType.getName();
        DependencyTreeNode complexNode = new DependencyTreeNode(
            complexType.getName(), fqn, ElementType.COMPLEX_TYPE, null, complexType);
        nodeMap.put(fqn, complexNode);
        
        // Create property nodes
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                String propFqn = fqn + "." + property.getName();
                DependencyTreeNode propNode = new DependencyTreeNode(
                    property.getName(), propFqn, ElementType.PROPERTY, fqn, property);
                nodeMap.put(propFqn, propNode);
                complexNode.addDependency(propNode);
            }
        }
        
        // Create base type node if exists
        if (complexType.getBaseType() != null) {
            String baseTypeFqn = fqn + ".BaseType";
            DependencyTreeNode baseTypeNode = new DependencyTreeNode(
                "BaseType", baseTypeFqn, ElementType.BASE_TYPE, fqn, complexType.getBaseType());
            nodeMap.put(baseTypeFqn, baseTypeNode);
            complexNode.addDependency(baseTypeNode);
        }
    }
    
    private void createEnumTypeNode(String namespace, CsdlEnumType enumType) {
        String fqn = namespace + "." + enumType.getName();
        DependencyTreeNode enumNode = new DependencyTreeNode(
            enumType.getName(), fqn, ElementType.ENUM_TYPE, null, enumType);
        nodeMap.put(fqn, enumNode);
    }
    
    private void createActionNodes(String namespace, CsdlAction action) {
        String fqn = namespace + "." + action.getName();
        DependencyTreeNode actionNode = new DependencyTreeNode(
            action.getName(), fqn, ElementType.ACTION, null, action);
        nodeMap.put(fqn, actionNode);
        
        // Create parameter nodes
        if (action.getParameters() != null) {
            for (CsdlParameter parameter : action.getParameters()) {
                String paramFqn = fqn + "." + parameter.getName();
                DependencyTreeNode paramNode = new DependencyTreeNode(
                    parameter.getName(), paramFqn, ElementType.PARAMETER, fqn, parameter);
                nodeMap.put(paramFqn, paramNode);
                actionNode.addDependency(paramNode);
            }
        }
        
        // Create return type node
        if (action.getReturnType() != null) {
            String returnTypeFqn = fqn + ".ReturnType";
            DependencyTreeNode returnTypeNode = new DependencyTreeNode(
                "ReturnType", returnTypeFqn, ElementType.RETURN_TYPE, fqn, action.getReturnType());
            nodeMap.put(returnTypeFqn, returnTypeNode);
            actionNode.addDependency(returnTypeNode);
        }
    }
    
    private void createFunctionNodes(String namespace, CsdlFunction function) {
        String fqn = namespace + "." + function.getName();
        DependencyTreeNode functionNode = new DependencyTreeNode(
            function.getName(), fqn, ElementType.FUNCTION, null, function);
        nodeMap.put(fqn, functionNode);
        
        // Create parameter nodes
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                String paramFqn = fqn + "." + parameter.getName();
                DependencyTreeNode paramNode = new DependencyTreeNode(
                    parameter.getName(), paramFqn, ElementType.PARAMETER, fqn, parameter);
                nodeMap.put(paramFqn, paramNode);
                functionNode.addDependency(paramNode);
            }
        }
        
        // Create return type node
        if (function.getReturnType() != null) {
            String returnTypeFqn = fqn + ".ReturnType";
            DependencyTreeNode returnTypeNode = new DependencyTreeNode(
                "ReturnType", returnTypeFqn, ElementType.RETURN_TYPE, fqn, function.getReturnType());
            nodeMap.put(returnTypeFqn, returnTypeNode);
            functionNode.addDependency(returnTypeNode);
        }
    }
    
    private void establishDependenciesForSchema(CsdlSchema schema) {
        String namespace = schema.getNamespace();
        
        // Establish entity type dependencies
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                establishEntityTypeDependencies(namespace, entityType);
            }
        }
        
        // Establish complex type dependencies
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                establishComplexTypeDependencies(namespace, complexType);
            }
        }
        
        // Establish action dependencies
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                establishActionDependencies(namespace, action);
            }
        }
        
        // Establish function dependencies
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                establishFunctionDependencies(namespace, function);
            }
        }
    }
    
    private void establishEntityTypeDependencies(String namespace, CsdlEntityType entityType) {
        String fqn = namespace + "." + entityType.getName();
        DependencyTreeNode entityNode = nodeMap.get(fqn);
        
        if (entityNode == null) return;
        
        // Base type dependency
        if (entityType.getBaseType() != null) {
            String baseTypeFqn = fqn + ".BaseType";
            DependencyTreeNode baseTypeNode = nodeMap.get(baseTypeFqn);
            if (baseTypeNode != null) {
                String actualBaseType = extractTypeName(entityType.getBaseType());
                DependencyTreeNode actualBaseTypeNode = nodeMap.get(actualBaseType);
                if (actualBaseTypeNode != null) {
                    baseTypeNode.addDependency(actualBaseTypeNode);
                }
            }
        }
        
        // Property dependencies
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                String propFqn = fqn + "." + property.getName();
                DependencyTreeNode propNode = nodeMap.get(propFqn);
                if (propNode != null) {
                    String typeName = extractTypeName(property.getType());
                    if (!isPrimitiveType(typeName)) {
                        DependencyTreeNode typeNode = nodeMap.get(typeName);
                        if (typeNode != null) {
                            propNode.addDependency(typeNode);
                        }
                    }
                }
            }
        }
        
        // Navigation property dependencies
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                String navPropFqn = fqn + "." + navProp.getName();
                DependencyTreeNode navPropNode = nodeMap.get(navPropFqn);
                if (navPropNode != null) {
                    String typeName = extractTypeName(navProp.getType());
                    DependencyTreeNode typeNode = nodeMap.get(typeName);
                    if (typeNode != null) {
                        navPropNode.addDependency(typeNode);
                    }
                }
            }
        }
    }
    
    private void establishComplexTypeDependencies(String namespace, CsdlComplexType complexType) {
        String fqn = namespace + "." + complexType.getName();
        DependencyTreeNode complexNode = nodeMap.get(fqn);
        
        if (complexNode == null) return;
        
        // Base type dependency
        if (complexType.getBaseType() != null) {
            String baseTypeFqn = fqn + ".BaseType";
            DependencyTreeNode baseTypeNode = nodeMap.get(baseTypeFqn);
            if (baseTypeNode != null) {
                String actualBaseType = extractTypeName(complexType.getBaseType());
                DependencyTreeNode actualBaseTypeNode = nodeMap.get(actualBaseType);
                if (actualBaseTypeNode != null) {
                    baseTypeNode.addDependency(actualBaseTypeNode);
                }
            }
        }
        
        // Property dependencies
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                String propFqn = fqn + "." + property.getName();
                DependencyTreeNode propNode = nodeMap.get(propFqn);
                if (propNode != null) {
                    String typeName = extractTypeName(property.getType());
                    if (!isPrimitiveType(typeName)) {
                        DependencyTreeNode typeNode = nodeMap.get(typeName);
                        if (typeNode != null) {
                            propNode.addDependency(typeNode);
                        }
                    }
                }
            }
        }
    }
    
    private void establishActionDependencies(String namespace, CsdlAction action) {
        String fqn = namespace + "." + action.getName();
        DependencyTreeNode actionNode = nodeMap.get(fqn);
        
        if (actionNode == null) return;
        
        // Parameter dependencies
        if (action.getParameters() != null) {
            for (CsdlParameter parameter : action.getParameters()) {
                String paramFqn = fqn + "." + parameter.getName();
                DependencyTreeNode paramNode = nodeMap.get(paramFqn);
                if (paramNode != null) {
                    String typeName = extractTypeName(parameter.getType());
                    if (!isPrimitiveType(typeName)) {
                        DependencyTreeNode typeNode = nodeMap.get(typeName);
                        if (typeNode != null) {
                            paramNode.addDependency(typeNode);
                        }
                    }
                }
            }
        }
        
        // Return type dependency
        if (action.getReturnType() != null) {
            String returnTypeFqn = fqn + ".ReturnType";
            DependencyTreeNode returnTypeNode = nodeMap.get(returnTypeFqn);
            if (returnTypeNode != null) {
                String typeName = extractTypeName(action.getReturnType().getType());
                if (!isPrimitiveType(typeName)) {
                    DependencyTreeNode typeNode = nodeMap.get(typeName);
                    if (typeNode != null) {
                        returnTypeNode.addDependency(typeNode);
                    }
                }
            }
        }
    }
    
    private void establishFunctionDependencies(String namespace, CsdlFunction function) {
        String fqn = namespace + "." + function.getName();
        DependencyTreeNode functionNode = nodeMap.get(fqn);
        
        if (functionNode == null) return;
        
        // Parameter dependencies
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                String paramFqn = fqn + "." + parameter.getName();
                DependencyTreeNode paramNode = nodeMap.get(paramFqn);
                if (paramNode != null) {
                    String typeName = extractTypeName(parameter.getType());
                    if (!isPrimitiveType(typeName)) {
                        DependencyTreeNode typeNode = nodeMap.get(typeName);
                        if (typeNode != null) {
                            paramNode.addDependency(typeNode);
                        }
                    }
                }
            }
        }
        
        // Return type dependency
        if (function.getReturnType() != null) {
            String returnTypeFqn = fqn + ".ReturnType";
            DependencyTreeNode returnTypeNode = nodeMap.get(returnTypeFqn);
            if (returnTypeNode != null) {
                String typeName = extractTypeName(function.getReturnType().getType());
                if (!isPrimitiveType(typeName)) {
                    DependencyTreeNode typeNode = nodeMap.get(typeName);
                    if (typeNode != null) {
                        returnTypeNode.addDependency(typeNode);
                    }
                }
            }
        }
    }
    
    @Override
    public List<DependencyTreeNode> getAllDependencies(String elementName) {
        DependencyTreeNode node = nodeMap.get(elementName);
        if (node == null) {
            return Collections.emptyList();
        }
        return node.getAllDependencies();
    }
    
    @Override
    public List<DependencyTreeNode> getAllDependents(String elementName) {
        DependencyTreeNode node = nodeMap.get(elementName);
        if (node == null) {
            return Collections.emptyList();
        }
        return node.getAllDependents();
    }
    
    @Override
    public DependencyTreeNode getDependencyTreeNode(String elementName) {
        return nodeMap.get(elementName);
    }
    
    @Override
    public List<List<String>> detectCircularDependencies() {
        List<List<String>> circularDeps = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        
        for (DependencyTreeNode node : nodeMap.values()) {
            if (!visited.contains(node.getFullQualifiedName())) {
                List<String> path = new ArrayList<>();
                if (detectCircularDependencyDFS(node, visited, recStack, path)) {
                    circularDeps.add(new ArrayList<>(path));
                }
            }
        }
        
        return circularDeps;
    }
    
    private boolean detectCircularDependencyDFS(DependencyTreeNode node, Set<String> visited, 
                                               Set<String> recStack, List<String> path) {
        String nodeName = node.getFullQualifiedName();
        visited.add(nodeName);
        recStack.add(nodeName);
        path.add(nodeName);
        
        for (DependencyTreeNode dep : node.getDependencies()) {
            String depName = dep.getFullQualifiedName();
            
            if (!visited.contains(depName)) {
                if (detectCircularDependencyDFS(dep, visited, recStack, path)) {
                    return true;
                }
            } else if (recStack.contains(depName)) {
                path.add(depName);
                return true;
            }
        }
        
        recStack.remove(nodeName);
        path.remove(path.size() - 1);
        return false;
    }
    
    @Override
    public ImpactAnalysis getImpactAnalysis(String elementName) {
        DependencyTreeNode node = nodeMap.get(elementName);
        if (node == null) {
            return new ImpactAnalysis(elementName, Collections.emptySet(), 
                                    Collections.emptySet(), Collections.emptyList(), 
                                    ImpactLevel.LOW, "Element not found");
        }
        
        List<DependencyTreeNode> dependents = node.getAllDependents();
        Set<String> directlyAffected = new HashSet<>();
        Set<String> transitivelyAffected = new HashSet<>();
        List<List<String>> impactPaths = new ArrayList<>();
        
        for (DependencyTreeNode dependent : dependents) {
            if (node.getDependents().contains(dependent)) {
                directlyAffected.add(dependent.getFullQualifiedName());
            } else {
                transitivelyAffected.add(dependent.getFullQualifiedName());
            }
            
            // Find impact path
            List<String> path = findDependencyPath(dependent.getFullQualifiedName(), elementName);
            if (!path.isEmpty()) {
                impactPaths.add(path);
            }
        }
        
        ImpactLevel level = calculateImpactLevel(directlyAffected.size() + transitivelyAffected.size());
        String summary = String.format("Element %s affects %d elements directly and %d transitively", 
                                      elementName, directlyAffected.size(), transitivelyAffected.size());
        
        return new ImpactAnalysis(elementName, directlyAffected, transitivelyAffected, 
                                impactPaths, level, summary);
    }
    
    private ImpactLevel calculateImpactLevel(int affectedCount) {
        if (affectedCount == 0) return ImpactLevel.LOW;
        if (affectedCount <= 5) return ImpactLevel.LOW;
        if (affectedCount <= 15) return ImpactLevel.MEDIUM;
        if (affectedCount <= 30) return ImpactLevel.HIGH;
        return ImpactLevel.CRITICAL;
    }
    
    @Override
    public List<String> findDependencyPath(String fromElement, String toElement) {
        DependencyTreeNode fromNode = nodeMap.get(fromElement);
        DependencyTreeNode toNode = nodeMap.get(toElement);
        
        if (fromNode == null || toNode == null) {
            return Collections.emptyList();
        }
        
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        if (findPathDFS(fromNode, toNode, path, visited)) {
            return path;
        }
        
        return Collections.emptyList();
    }
    
    private boolean findPathDFS(DependencyTreeNode current, DependencyTreeNode target, 
                               List<String> path, Set<String> visited) {
        String currentName = current.getFullQualifiedName();
        
        if (visited.contains(currentName)) {
            return false;
        }
        
        visited.add(currentName);
        path.add(currentName);
        
        if (current.equals(target)) {
            return true;
        }
        
        for (DependencyTreeNode dep : current.getDependencies()) {
            if (findPathDFS(dep, target, path, visited)) {
                return true;
            }
        }
        
        path.remove(path.size() - 1);
        return false;
    }
    
    @Override
    public Set<String> getLeafDependencies(String elementName) {
        DependencyTreeNode node = nodeMap.get(elementName);
        if (node == null) {
            return Collections.emptySet();
        }
        
        Set<String> leafDeps = new HashSet<>();
        Set<String> visited = new HashSet<>();
        collectLeafDependencies(node, leafDeps, visited);
        return leafDeps;
    }
    
    private void collectLeafDependencies(DependencyTreeNode node, Set<String> leafDeps, Set<String> visited) {
        String nodeName = node.getFullQualifiedName();
        if (visited.contains(nodeName)) {
            return;
        }
        visited.add(nodeName);
        
        if (node.isLeaf()) {
            leafDeps.add(nodeName);
            return;
        }
        
        for (DependencyTreeNode dep : node.getDependencies()) {
            collectLeafDependencies(dep, leafDeps, visited);
        }
    }
    
    @Override
    public DependencyStatistics getDependencyStatistics() {
        int totalElements = nodeMap.size();
        int totalDependencies = 0;
        int leafElements = 0;
        int rootElements = 0;
        int maxDepth = 0;
        double totalDepth = 0;
        
        Map<ElementType, Integer> elementTypeCount = new HashMap<>();
        Map<String, Integer> dependencyCountByElement = new HashMap<>();
        
        for (DependencyTreeNode node : nodeMap.values()) {
            int depCount = node.getDependencies().size();
            totalDependencies += depCount;
            
            if (node.isLeaf()) leafElements++;
            if (node.isRoot()) rootElements++;
            
            int depth = node.getDepth();
            maxDepth = Math.max(maxDepth, depth);
            totalDepth += depth;
            
            ElementType type = node.getElementType();
            elementTypeCount.put(type, elementTypeCount.getOrDefault(type, 0) + 1);
            dependencyCountByElement.put(node.getFullQualifiedName(), depCount);
        }
        
        double averageDepth = totalElements > 0 ? totalDepth / totalElements : 0;
        int circularDeps = detectCircularDependencies().size();
        
        return new DependencyStatistics(totalElements, totalDependencies, circularDeps, 
                                       leafElements, rootElements, maxDepth, averageDepth, 
                                       elementTypeCount, dependencyCountByElement);
    }
    
    // Helper methods
    
    private String extractTypeName(String type) {
        if (type == null) return null;
        if (type.startsWith("Collection(")) {
            return type.substring("Collection(".length(), type.length() - 1);
        }
        return type;
    }
    
    private boolean isPrimitiveType(String type) {
        if (type == null) return false;
        return type.startsWith("Edm.");
    }
}
