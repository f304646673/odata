package org.apache.olingo.schemamanager.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * OData Schema分析器
 * 负责递归加载OData XML文件，校验import正确性，分析类型依赖关系
 */
@Component
public class ODataSchemaAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(ODataSchemaAnalyzer.class);
    
    @Autowired
    private ODataXmlLoader xmlLoader;
    
    @Autowired
    private SchemaRepository repository;
    
    /**
     * 分析结果类
     */
    public static class AnalysisResult {
        private final boolean success;
        private final List<String> errors;
        private final List<String> warnings;
        private final Map<String, Set<String>> dependencies;
        private final Map<String, String> typeLocations;
        private final ImportValidationResult importValidation;
        
        public AnalysisResult(boolean success, List<String> errors, List<String> warnings, 
                             Map<String, Set<String>> dependencies, Map<String, String> typeLocations,
                             ImportValidationResult importValidation) {
            this.success = success;
            this.errors = errors;
            this.warnings = warnings;
            this.dependencies = dependencies;
            this.typeLocations = typeLocations;
            this.importValidation = importValidation;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public Map<String, Set<String>> getDependencies() { return dependencies; }
        public Map<String, String> getTypeLocations() { return typeLocations; }
        public ImportValidationResult getImportValidation() { return importValidation; }
    }
    
    /**
     * Import校验结果类
     */
    public static class ImportValidationResult {
        private final boolean valid;
        private final List<String> missingImports;
        private final List<String> unusedImports;
        private final List<String> circularDependencies;
        
        public ImportValidationResult(boolean valid, List<String> missingImports, 
                                    List<String> unusedImports, List<String> circularDependencies) {
            this.valid = valid;
            this.missingImports = missingImports;
            this.unusedImports = unusedImports;
            this.circularDependencies = circularDependencies;
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public List<String> getMissingImports() { return missingImports; }
        public List<String> getUnusedImports() { return unusedImports; }
        public List<String> getCircularDependencies() { return circularDependencies; }
    }
    
    /**
     * 类型详细信息类
     */
    public static class TypeDetailInfo {
        private final String fullQualifiedName;
        private final String namespace;
        private final String typeName;
        private final TypeKind typeKind;
        private final Object typeDefinition; // CsdlEntityType, CsdlComplexType, or CsdlEnumType
        private final Set<String> directDependencies;
        private final Set<String> allDependencies;
        private final Set<String> dependents; // 依赖于此类型的其他类型
        private final String sourceFile;
        
        public TypeDetailInfo(String fullQualifiedName, String namespace, String typeName, 
                             TypeKind typeKind, Object typeDefinition, Set<String> directDependencies,
                             Set<String> allDependencies, Set<String> dependents, String sourceFile) {
            this.fullQualifiedName = fullQualifiedName;
            this.namespace = namespace;
            this.typeName = typeName;
            this.typeKind = typeKind;
            this.typeDefinition = typeDefinition;
            this.directDependencies = directDependencies;
            this.allDependencies = allDependencies;
            this.dependents = dependents;
            this.sourceFile = sourceFile;
        }
        
        // Getters
        public String getFullQualifiedName() { return fullQualifiedName; }
        public String getNamespace() { return namespace; }
        public String getTypeName() { return typeName; }
        public TypeKind getTypeKind() { return typeKind; }
        public Object getTypeDefinition() { return typeDefinition; }
        public Set<String> getDirectDependencies() { return directDependencies; }
        public Set<String> getAllDependencies() { return allDependencies; }
        public Set<String> getDependents() { return dependents; }
        public String getSourceFile() { return sourceFile; }
    }
    
    /**
     * 类型种类枚举
     */
    public enum TypeKind {
        ENTITY_TYPE, COMPLEX_TYPE, ENUM_TYPE
    }
    
    /**
     * 递归加载并分析OData XML文件
     * @param directoryPath 目录路径
     * @return 分析结果
     */
    public AnalysisResult analyzeDirectory(String directoryPath) {
        logger.info("Starting analysis of directory: {}", directoryPath);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // 1. 加载所有XML文件
            ODataXmlLoader.LoadResult loadResult = xmlLoader.loadFromResourceDirectory(directoryPath);
            
            if (loadResult.getFailedFiles() > 0) {
                errors.addAll(loadResult.getErrorMessages());
                logger.warn("Found {} failed files during loading", loadResult.getFailedFiles());
            }
            
            // 2. 校验import正确性
            ImportValidationResult importValidation = validateImports();
            if (!importValidation.isValid()) {
                warnings.add("Import validation found issues");
            }
            
            // 3. 分析类型依赖关系
            Map<String, Set<String>> dependencies = analyzeDependencies();
            
            // 4. 构建类型位置映射
            Map<String, String> typeLocations = buildTypeLocationMap();
            
            boolean success = errors.isEmpty();
            
            logger.info("Analysis completed. Success: {}, Errors: {}, Warnings: {}", 
                       success, errors.size(), warnings.size());
            
            return new AnalysisResult(success, errors, warnings, dependencies, typeLocations, importValidation);
            
        } catch (Exception e) {
            logger.error("Analysis failed with exception", e);
            errors.add("Analysis failed: " + e.getMessage());
            return new AnalysisResult(false, errors, warnings, new HashMap<>(), 
                                    new HashMap<>(), new ImportValidationResult(false, new ArrayList<>(), 
                                    new ArrayList<>(), new ArrayList<>()));
        }
    }
    
    /**
     * 查询EntityType详细信息
     */
    public TypeDetailInfo getEntityTypeDetail(String fullQualifiedName) {
        CsdlEntityType entityType = repository.getEntityType(fullQualifiedName);
        if (entityType == null) {
            return null;
        }
        
        return buildTypeDetailInfo(fullQualifiedName, TypeKind.ENTITY_TYPE, entityType);
    }
    
    /**
     * 查询ComplexType详细信息
     */
    public TypeDetailInfo getComplexTypeDetail(String fullQualifiedName) {
        CsdlComplexType complexType = repository.getComplexType(fullQualifiedName);
        if (complexType == null) {
            return null;
        }
        
        return buildTypeDetailInfo(fullQualifiedName, TypeKind.COMPLEX_TYPE, complexType);
    }
    
    /**
     * 查询EnumType详细信息
     */
    public TypeDetailInfo getEnumTypeDetail(String fullQualifiedName) {
        CsdlEnumType enumType = repository.getEnumType(fullQualifiedName);
        if (enumType == null) {
            return null;
        }
        
        return buildTypeDetailInfo(fullQualifiedName, TypeKind.ENUM_TYPE, enumType);
    }
    
    /**
     * 搜索类型（支持部分匹配）
     */
    public List<TypeDetailInfo> searchTypes(String namePattern) {
        List<TypeDetailInfo> results = new ArrayList<>();
        String lowerPattern = namePattern.toLowerCase();
        
        // 搜索EntityTypes
        for (String namespace : repository.getAllNamespaces()) {
            repository.getEntityTypes(namespace).forEach(entityType -> {
                String fullName = namespace + "." + entityType.getName();
                if (fullName.toLowerCase().contains(lowerPattern)) {
                    results.add(buildTypeDetailInfo(fullName, TypeKind.ENTITY_TYPE, entityType));
                }
            });
            
            // 搜索ComplexTypes
            repository.getComplexTypes(namespace).forEach(complexType -> {
                String fullName = namespace + "." + complexType.getName();
                if (fullName.toLowerCase().contains(lowerPattern)) {
                    results.add(buildTypeDetailInfo(fullName, TypeKind.COMPLEX_TYPE, complexType));
                }
            });
            
            // 搜索EnumTypes
            repository.getEnumTypes(namespace).forEach(enumType -> {
                String fullName = namespace + "." + enumType.getName();
                if (fullName.toLowerCase().contains(lowerPattern)) {
                    results.add(buildTypeDetailInfo(fullName, TypeKind.ENUM_TYPE, enumType));
                }
            });
        }
        
        return results;
    }
    
    /**
     * 获取所有类型的统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        SchemaRepository.RepositoryStatistics repoStats = repository.getStatistics();
        
        stats.put("totalSchemas", repoStats.getTotalSchemas());
        stats.put("totalEntityTypes", repoStats.getTotalEntityTypes());
        stats.put("totalComplexTypes", repoStats.getTotalComplexTypes());
        stats.put("totalEnumTypes", repoStats.getTotalEnumTypes());
        stats.put("totalEntityContainers", repoStats.getTotalEntityContainers());
        stats.put("namespaces", repository.getAllNamespaces());
        
        return stats;
    }
    
    /**
     * 校验import正确性
     */
    private ImportValidationResult validateImports() {
        List<String> missingImports = new ArrayList<>();
        List<String> unusedImports = new ArrayList<>();
        List<String> circularDependencies = new ArrayList<>();
        
        Map<String, Set<String>> dependencies = analyzeDependencies();
        
        // 检测循环依赖
        circularDependencies.addAll(detectCircularDependencies(dependencies));
        
        // 检查缺失的引用
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            String sourceType = entry.getKey();
            for (String depType : entry.getValue()) {
                if (!typeExists(depType)) {
                    missingImports.add("Type " + sourceType + " references missing type: " + depType);
                }
            }
        }
        
        boolean valid = missingImports.isEmpty() && circularDependencies.isEmpty();
        return new ImportValidationResult(valid, missingImports, unusedImports, circularDependencies);
    }
    
    /**
     * 分析类型依赖关系
     */
    private Map<String, Set<String>> analyzeDependencies() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        
        // 分析EntityType依赖
        for (String namespace : repository.getAllNamespaces()) {
            for (CsdlEntityType entityType : repository.getEntityTypes(namespace)) {
                String fullName = namespace + "." + entityType.getName();
                Set<String> deps = extractEntityTypeDependencies(entityType, namespace);
                dependencies.put(fullName, deps);
            }
            
            for (CsdlComplexType complexType : repository.getComplexTypes(namespace)) {
                String fullName = namespace + "." + complexType.getName();
                Set<String> deps = extractComplexTypeDependencies(complexType, namespace);
                dependencies.put(fullName, deps);
            }
        }
        
        return dependencies;
    }
    
    /**
     * 提取EntityType的依赖
     */
    private Set<String> extractEntityTypeDependencies(CsdlEntityType entityType, String currentNamespace) {
        Set<String> dependencies = new HashSet<>();
        
        // 检查BaseType
        if (entityType.getBaseType() != null) {
            dependencies.add(resolveTypeName(entityType.getBaseType(), currentNamespace));
        }
        
        // 检查Properties
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                String typeName = resolveTypeName(property.getType(), currentNamespace);
                if (isCustomType(typeName)) {
                    dependencies.add(typeName);
                }
            }
        }
        
        // 检查Navigation Properties
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                String typeName = resolveTypeName(navProp.getType(), currentNamespace);
                if (isCustomType(typeName)) {
                    dependencies.add(typeName);
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * 提取ComplexType的依赖
     */
    private Set<String> extractComplexTypeDependencies(CsdlComplexType complexType, String currentNamespace) {
        Set<String> dependencies = new HashSet<>();
        
        // 检查BaseType
        if (complexType.getBaseType() != null) {
            dependencies.add(resolveTypeName(complexType.getBaseType(), currentNamespace));
        }
        
        // 检查Properties
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                String typeName = resolveTypeName(property.getType(), currentNamespace);
                if (isCustomType(typeName)) {
                    dependencies.add(typeName);
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * 构建类型详细信息
     */
    private TypeDetailInfo buildTypeDetailInfo(String fullQualifiedName, TypeKind typeKind, Object typeDefinition) {
        String[] parts = fullQualifiedName.split("\\.");
        String namespace = parts[0];
        String typeName = parts[1];
        
        Set<String> directDependencies = new HashSet<>();
        Set<String> allDependencies = new HashSet<>();
        
        // 根据类型种类提取依赖
        if (typeKind == TypeKind.ENTITY_TYPE && typeDefinition instanceof CsdlEntityType) {
            directDependencies = extractEntityTypeDependencies((CsdlEntityType) typeDefinition, namespace);
        } else if (typeKind == TypeKind.COMPLEX_TYPE && typeDefinition instanceof CsdlComplexType) {
            directDependencies = extractComplexTypeDependencies((CsdlComplexType) typeDefinition, namespace);
        }
        
        // 递归计算所有依赖
        allDependencies.addAll(calculateAllDependencies(fullQualifiedName, new HashSet<>()));
        
        // 计算依赖者
        Set<String> dependents = findDependents(fullQualifiedName);
        
        // 获取源文件
        String sourceFile = repository.getSchemaFilePath(namespace);
        
        return new TypeDetailInfo(fullQualifiedName, namespace, typeName, typeKind, typeDefinition,
                                 directDependencies, allDependencies, dependents, sourceFile);
    }
    
    /**
     * 递归计算所有依赖
     */
    private Set<String> calculateAllDependencies(String typeName, Set<String> visited) {
        if (visited.contains(typeName)) {
            return new HashSet<>(); // 避免循环依赖
        }
        
        visited.add(typeName);
        Set<String> allDeps = new HashSet<>();
        
        Map<String, Set<String>> allDependencies = analyzeDependencies();
        Set<String> directDeps = allDependencies.get(typeName);
        
        if (directDeps != null) {
            allDeps.addAll(directDeps);
            for (String dep : directDeps) {
                allDeps.addAll(calculateAllDependencies(dep, new HashSet<>(visited)));
            }
        }
        
        return allDeps;
    }
    
    /**
     * 查找依赖者
     */
    private Set<String> findDependents(String targetType) {
        Set<String> dependents = new HashSet<>();
        Map<String, Set<String>> allDependencies = analyzeDependencies();
        
        for (Map.Entry<String, Set<String>> entry : allDependencies.entrySet()) {
            if (entry.getValue().contains(targetType)) {
                dependents.add(entry.getKey());
            }
        }
        
        return dependents;
    }
    
    /**
     * 构建类型位置映射
     */
    private Map<String, String> buildTypeLocationMap() {
        Map<String, String> typeLocations = new HashMap<>();
        
        for (String namespace : repository.getAllNamespaces()) {
            String filePath = repository.getSchemaFilePath(namespace);
            
            repository.getEntityTypes(namespace).forEach(et -> {
                typeLocations.put(namespace + "." + et.getName(), filePath);
            });
            
            repository.getComplexTypes(namespace).forEach(ct -> {
                typeLocations.put(namespace + "." + ct.getName(), filePath);
            });
            
            repository.getEnumTypes(namespace).forEach(et -> {
                typeLocations.put(namespace + "." + et.getName(), filePath);
            });
        }
        
        return typeLocations;
    }
    
    /**
     * 检测循环依赖
     */
    private List<String> detectCircularDependencies(Map<String, Set<String>> dependencies) {
        List<String> circular = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        
        for (String type : dependencies.keySet()) {
            if (!visited.contains(type)) {
                detectCircularDependenciesHelper(type, dependencies, visiting, visited, circular, new ArrayList<>());
            }
        }
        
        return circular;
    }
    
    private void detectCircularDependenciesHelper(String type, Map<String, Set<String>> dependencies,
                                                 Set<String> visiting, Set<String> visited, 
                                                 List<String> circular, List<String> path) {
        if (visiting.contains(type)) {
            // 找到循环依赖
            int startIndex = path.indexOf(type);
            List<String> cycle = path.subList(startIndex, path.size());
            cycle.add(type);
            circular.add("Circular dependency: " + String.join(" -> ", cycle));
            return;
        }
        
        if (visited.contains(type)) {
            return;
        }
        
        visiting.add(type);
        path.add(type);
        
        Set<String> deps = dependencies.get(type);
        if (deps != null) {
            for (String dep : deps) {
                detectCircularDependenciesHelper(dep, dependencies, visiting, visited, circular, path);
            }
        }
        
        visiting.remove(type);
        visited.add(type);
        path.remove(path.size() - 1);
    }
    
    /**
     * 检查类型是否存在
     */
    private boolean typeExists(String fullQualifiedName) {
        return repository.getEntityType(fullQualifiedName) != null ||
               repository.getComplexType(fullQualifiedName) != null ||
               repository.getEnumType(fullQualifiedName) != null;
    }
    
    /**
     * 解析类型名称
     */
    private String resolveTypeName(String typeName, String currentNamespace) {
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            typeName = typeName.substring(11, typeName.length() - 1);
        }
        
        if (!typeName.contains(".")) {
            typeName = currentNamespace + "." + typeName;
        }
        
        return typeName;
    }
    
    /**
     * 检查是否为自定义类型
     */
    private boolean isCustomType(String typeName) {
        // 排除EDM基础类型
        return !typeName.startsWith("Edm.") && typeName.contains(".");
    }
}
