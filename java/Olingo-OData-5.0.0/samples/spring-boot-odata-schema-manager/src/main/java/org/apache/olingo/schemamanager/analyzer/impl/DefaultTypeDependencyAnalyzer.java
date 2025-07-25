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
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of TypeDependencyAnalyzer
 */
@Component
public class DefaultTypeDependencyAnalyzer implements TypeDependencyAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultTypeDependencyAnalyzer.class);
    
    private final SchemaRepository repository;
    
    @Autowired
    public DefaultTypeDependencyAnalyzer(SchemaRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public List<TypeReference> getDirectDependencies(CsdlEntityType entityType) {
        if (entityType == null) {
            return Collections.emptyList();
        }
        
        List<TypeReference> dependencies = new ArrayList<>();
        
        // 检查基类型
        if (entityType.getBaseType() != null) {
            String baseType = entityType.getBaseType();
            if (!isPrimitiveType(baseType)) {
                dependencies.add(createTypeReference(baseType, "baseType"));
            }
        }
        
        // 检查属性
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                String type = extractTypeName(property.getType());
                if (!isPrimitiveType(type)) {
                    dependencies.add(createTypeReference(type, property.getName()));
                }
            }
        }
        
        // 检查导航属性
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                String type = extractTypeName(navProp.getType());
                if (!isPrimitiveType(type)) {
                    dependencies.add(createTypeReference(type, navProp.getName()));
                }
            }
        }
        
        return dependencies;
    }
    
    @Override
    public List<TypeReference> getDirectDependencies(CsdlComplexType complexType) {
        if (complexType == null) {
            return Collections.emptyList();
        }
        
        List<TypeReference> dependencies = new ArrayList<>();
        
        // 检查基类型
        if (complexType.getBaseType() != null) {
            String baseType = complexType.getBaseType();
            if (!isPrimitiveType(baseType)) {
                dependencies.add(createTypeReference(baseType, "baseType"));
            }
        }
        
        // 检查属性
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                String type = extractTypeName(property.getType());
                if (!isPrimitiveType(type)) {
                    dependencies.add(createTypeReference(type, property.getName()));
                }
            }
        }
        
        return dependencies;
    }
    
    @Override
    public List<TypeReference> getAllDependencies(CsdlEntityType entityType) {
        if (entityType == null) {
            return Collections.emptyList();
        }
        
        List<TypeReference> allDependencies = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectAllDependencies(entityType, visited, allDependencies);
        return allDependencies;
    }
    
    @Override
    public List<TypeReference> getAllDependencies(CsdlComplexType complexType) {
        if (complexType == null) {
            return Collections.emptyList();
        }
        
        List<TypeReference> allDependencies = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectAllDependencies(complexType, visited, allDependencies);
        return allDependencies;
    }
    
    @Override
    public List<TypeReference> getDirectDependencies(CsdlAction action) {
        if (action == null) {
            return Collections.emptyList();
        }
        
        List<TypeReference> dependencies = new ArrayList<>();
        
        // 检查参数
        if (action.getParameters() != null) {
            for (CsdlParameter parameter : action.getParameters()) {
                String type = extractTypeName(parameter.getType());
                if (!isPrimitiveType(type)) {
                    dependencies.add(createTypeReference(type, parameter.getName()));
                }
            }
        }
        
        // 检查返回类型
        if (action.getReturnType() != null) {
            String type = extractTypeName(action.getReturnType().getType());
            if (!isPrimitiveType(type)) {
                dependencies.add(createTypeReference(type, "returnType"));
            }
        }
        
        return dependencies;
    }
    
    @Override
    public List<TypeReference> getDirectDependencies(CsdlFunction function) {
        if (function == null) {
            return Collections.emptyList();
        }
        
        List<TypeReference> dependencies = new ArrayList<>();
        
        // 检查参数
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                String type = extractTypeName(parameter.getType());
                if (!isPrimitiveType(type)) {
                    dependencies.add(createTypeReference(type, parameter.getName()));
                }
            }
        }
        
        // 检查返回类型
        if (function.getReturnType() != null) {
            String type = extractTypeName(function.getReturnType().getType());
            if (!isPrimitiveType(type)) {
                dependencies.add(createTypeReference(type, "returnType"));
            }
        }
        
        return dependencies;
    }
    
    @Override
    public List<TypeReference> getAllDependencies(CsdlAction action) {
        return getDirectDependencies(action);
    }
    
    @Override
    public List<TypeReference> getAllDependencies(CsdlFunction function) {
        return getDirectDependencies(function);
    }
    
    @Override
    public boolean hasDependency(String sourceType, String targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }
        
        List<TypeReference> dependencies = getAllDependencies(sourceType);
        for (TypeReference dep : dependencies) {
            if (targetType.equals(dep.getFullQualifiedName())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public List<TypeReference> getDependents(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TypeReference> dependents = new ArrayList<>();
        
        // 遍历所有schema，查找依赖于指定类型的类型
        for (CsdlSchema schema : repository.getAllSchemas().values()) {
            // 检查实体类型
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String entityTypeName = schema.getNamespace() + "." + entityType.getName();
                    if (hasDependency(entityTypeName, typeName)) {
                        dependents.add(new TypeReference(entityTypeName, TypeKind.ENTITY_TYPE, "entity", false));
                    }
                }
            }
            
            // 检查复杂类型
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    String complexTypeName = schema.getNamespace() + "." + complexType.getName();
                    if (hasDependency(complexTypeName, typeName)) {
                        dependents.add(new TypeReference(complexTypeName, TypeKind.COMPLEX_TYPE, "complex", false));
                    }
                }
            }
        }
        
        return dependents;
    }
    
    @Override
    public List<String> getDependencyPath(String sourceType, String targetType) {
        if (sourceType == null || targetType == null) {
            return Collections.emptyList();
        }
        
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        path.add(sourceType); // 先添加源类型
        if (findDependencyPath(sourceType, targetType, path, visited)) {
            return path;
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public List<CircularDependency> detectCircularDependencies() {
        List<CircularDependency> circularDeps = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        
        for (CsdlSchema schema : repository.getAllSchemas().values()) {
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String typeName = schema.getNamespace() + "." + entityType.getName();
                    if (!visited.contains(typeName)) {
                        List<String> path = new ArrayList<>();
                        if (detectCircularDependencyDFS(typeName, visited, recStack, path)) {
                            circularDeps.add(new CircularDependency(new ArrayList<>(path)));
                        }
                    }
                }
            }
        }
        
        return circularDeps;
    }
    
    @Override
    public DependencyGraph buildDependencyGraph(CsdlEntityContainer entityContainer) {
        Set<String> allTypes = new HashSet<>();
        List<TypeReference> allDependencies = new ArrayList<>();
        
        if (entityContainer != null && entityContainer.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : entityContainer.getEntitySets()) {
                String entityTypeName = entitySet.getTypeFQN().getFullQualifiedNameAsString();
                allTypes.add(entityTypeName);
                
                // 获取该实体类型的所有依赖
                List<TypeReference> deps = getAllDependencies(entityTypeName);
                for (TypeReference dep : deps) {
                    if (!allDependencies.contains(dep)) {
                        allDependencies.add(dep);
                        allTypes.add(dep.getFullQualifiedName());
                    }
                }
            }
        }
        
        return new DependencyGraph(allTypes, allDependencies, entityContainer);
    }
    
    @Override
    public DependencyGraph buildCustomDependencyGraph(List<EntitySetDefinition> entitySetDefinitions) {
        Set<String> allTypes = new HashSet<>();
        List<TypeReference> allDependencies = new ArrayList<>();
        
        if (entitySetDefinitions != null) {
            for (EntitySetDefinition definition : entitySetDefinitions) {
                String entityTypeName = definition.getEntityTypeName();
                allTypes.add(entityTypeName);
                
                // 获取该实体类型的所有依赖
                List<TypeReference> deps = getAllDependencies(entityTypeName);
                for (TypeReference dep : deps) {
                    if (!allDependencies.contains(dep)) {
                        allDependencies.add(dep);
                        allTypes.add(dep.getFullQualifiedName());
                    }
                }
            }
        }
        
        return new DependencyGraph(allTypes, allDependencies, null);
    }
    
    // 辅助方法
    
    /**
     * 根据类型名获取所有依赖（字符串参数版本）
     */
    public List<TypeReference> getAllDependencies(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Type name cannot be null or empty");
        }
        
        CsdlEntityType entityType = repository.getEntityType(typeName);
        if (entityType != null) {
            return getAllDependencies(entityType);
        }
        
        CsdlComplexType complexType = repository.getComplexType(typeName);
        if (complexType != null) {
            return getAllDependencies(complexType);
        }
        
        return Collections.emptyList();
    }
    
    /**
     * 递归查找依赖路径
     */
    private boolean findDependencyPath(String current, String target, List<String> path, Set<String> visited) {
        if (visited.contains(current)) {
            return false;
        }
        
        visited.add(current);
        
        List<TypeReference> directDeps = getDirectDependenciesByName(current);
        for (TypeReference dep : directDeps) {
            path.add(dep.getFullQualifiedName());
            
            if (target.equals(dep.getFullQualifiedName())) {
                return true;
            }
            
            if (findDependencyPath(dep.getFullQualifiedName(), target, path, visited)) {
                return true;
            }
            
            path.remove(path.size() - 1);
        }
        
        visited.remove(current);
        return false;
    }
    
    /**
     * 根据类型名获取直接依赖
     */
    private List<TypeReference> getDirectDependenciesByName(String typeName) {
        CsdlEntityType entityType = repository.getEntityType(typeName);
        if (entityType != null) {
            return getDirectDependencies(entityType);
        }
        
        CsdlComplexType complexType = repository.getComplexType(typeName);
        if (complexType != null) {
            return getDirectDependencies(complexType);
        }
        
        return Collections.emptyList();
    }
    
    /**
     * 递归收集依赖
     */
    private void collectAllDependencies(CsdlEntityType entityType, Set<String> visited, List<TypeReference> allDeps) {
        if (entityType == null) return;
        
        String entityTypeName = entityType.getName();
        if (visited.contains(entityTypeName)) return;
        visited.add(entityTypeName);
        
        List<TypeReference> deps = getDirectDependencies(entityType);
        for (TypeReference dep : deps) {
            if (!allDeps.contains(dep)) {
                allDeps.add(dep);
            }
            
            String depType = dep.getFullQualifiedName();
            if (dep.getKind() == TypeKind.ENTITY_TYPE) {
                CsdlEntityType et = repository.getEntityType(depType);
                collectAllDependencies(et, visited, allDeps);
            } else if (dep.getKind() == TypeKind.COMPLEX_TYPE) {
                CsdlComplexType ct = repository.getComplexType(depType);
                collectAllDependencies(ct, visited, allDeps);
            }
        }
    }

    /**
     * 递归收集依赖
     */
    private void collectAllDependencies(CsdlComplexType complexType, Set<String> visited, List<TypeReference> allDeps) {
        if (complexType == null) return;
        
        String complexTypeName = complexType.getName();
        if (visited.contains(complexTypeName)) return;
        visited.add(complexTypeName);
        
        List<TypeReference> deps = getDirectDependencies(complexType);
        for (TypeReference dep : deps) {
            if (!allDeps.contains(dep)) {
                allDeps.add(dep);
            }
            
            String depType = dep.getFullQualifiedName();
            if (dep.getKind() == TypeKind.ENTITY_TYPE) {
                CsdlEntityType et = repository.getEntityType(depType);
                collectAllDependencies(et, visited, allDeps);
            } else if (dep.getKind() == TypeKind.COMPLEX_TYPE) {
                CsdlComplexType ct = repository.getComplexType(depType);
                collectAllDependencies(ct, visited, allDeps);
            }
        }
    }
    
    /**
     * 检测循环依赖的DFS
     */
    private boolean detectCircularDependencyDFS(String typeName, Set<String> visited, Set<String> recStack, List<String> path) {
        visited.add(typeName);
        recStack.add(typeName);
        path.add(typeName);
        
        List<TypeReference> deps = getDirectDependenciesByName(typeName);
        for (TypeReference dep : deps) {
            String depName = dep.getFullQualifiedName();
            
            if (!visited.contains(depName)) {
                if (detectCircularDependencyDFS(depName, visited, recStack, path)) {
                    return true;
                }
            } else if (recStack.contains(depName)) {
                path.add(depName);
                return true;
            }
        }
        
        recStack.remove(typeName);
        path.remove(path.size() - 1);
        return false;
    }
    
    /**
     * 提取类型名（去除Collection()包装）
     */
    private String extractTypeName(String type) {
        if (type == null) return null;
        if (type.startsWith("Collection(")) {
            return type.substring("Collection(".length(), type.length() - 1);
        }
        return type;
    }

    /**
     * 判断是否为Edm原始类型
     */
    private boolean isPrimitiveType(String type) {
        if (type == null) return false;
        return type.startsWith("Edm.");
    }
    
    /**
     * 创建TypeReference
     */
    private TypeReference createTypeReference(String typeName, String propertyName) {
        if (typeName == null) return null;
        
        boolean isCollection = typeName.startsWith("Collection(");
        String actualType = extractTypeName(typeName);
        
        // 判断类型种类
        CsdlEntityType entityType = repository.getEntityType(actualType);
        if (entityType != null) {
            return new TypeReference(actualType, TypeKind.ENTITY_TYPE, propertyName, isCollection);
        }
        
        CsdlComplexType complexType = repository.getComplexType(actualType);
        if (complexType != null) {
            return new TypeReference(actualType, TypeKind.COMPLEX_TYPE, propertyName, isCollection);
        }
        
        // 默认作为复杂类型处理
        return new TypeReference(actualType, TypeKind.COMPLEX_TYPE, propertyName, isCollection);
    }
}
