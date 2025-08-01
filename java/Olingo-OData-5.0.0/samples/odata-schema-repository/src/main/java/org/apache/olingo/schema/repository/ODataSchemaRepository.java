package org.apache.olingo.schema.repository;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.schema.repository.loader.SchemaDependencyAnalyzer;
import org.apache.olingo.schema.repository.loader.SchemaRepositoryLoader;
import org.apache.olingo.schema.repository.loader.SchemaRepositoryLoader.SchemaLoadException;
import org.apache.olingo.schema.repository.model.SchemaDependencyNode;
import org.apache.olingo.schema.repository.model.SchemaRepositoryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OData 4 Schema Repository 主服务类
 * 提供Schema的加载、管理、查询和依赖分析功能
 */
public class ODataSchemaRepository implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory.getLogger(ODataSchemaRepository.class);
    
    private final SchemaRepositoryContext context;
    private final SchemaRepositoryLoader loader;
    private final SchemaDependencyAnalyzer dependencyAnalyzer;
    
    /**
     * 构造函数
     */
    public ODataSchemaRepository() {
        this.context = new SchemaRepositoryContext();
        this.loader = new SchemaRepositoryLoader(context);
        this.dependencyAnalyzer = new SchemaDependencyAnalyzer(context);
        
        LOG.info("OData Schema Repository initialized");
    }
    
    /**
     * 自定义验证器的构造函数
     */
    public ODataSchemaRepository(SchemaRepositoryLoader.SchemaValidator validator) {
        this.context = new SchemaRepositoryContext();
        this.loader = new SchemaRepositoryLoader(context, validator);
        this.dependencyAnalyzer = new SchemaDependencyAnalyzer(context);
        
        LOG.info("OData Schema Repository initialized with custom validator");
    }
    
    // ==================== Schema Loading Methods ====================
    
    /**
     * 从文件加载Schema
     */
    public void loadSchema(File file) throws SchemaLoadException {
        loader.loadFromFile(file);
        analyzeLoadedSchema();
    }
    
    /**
     * 从文件路径加载Schema
     */
    public void loadSchema(String filePath) throws SchemaLoadException {
        loader.loadFromFile(filePath);
        analyzeLoadedSchema();
    }
    
    /**
     * 从Path加载Schema
     */
    public void loadSchema(Path path) throws SchemaLoadException {
        loader.loadFromPath(path);
        analyzeLoadedSchema();
    }
    
    /**
     * 从InputStream加载Schema
     */
    public void loadSchema(InputStream inputStream, String sourceName) throws SchemaLoadException {
        loader.loadFromInputStream(inputStream, sourceName);
        analyzeLoadedSchema();
    }
    
    /**
     * 从目录批量加载Schema
     */
    public void loadSchemasFromDirectory(Path directory, boolean recursive) throws SchemaLoadException {
        loader.loadFromDirectory(directory, recursive);
        analyzeAllSchemas();
    }
    
    /**
     * 并行加载多个文件
     */
    public void loadSchemasInParallel(List<Path> files) throws SchemaLoadException {
        loader.loadFilesInParallel(files);
        analyzeAllSchemas();
    }
    
    /**
     * 重新加载所有Schema
     */
    public void reloadAll(List<Path> files) throws SchemaLoadException {
        loader.reloadAll(files);
        analyzeAllSchemas();
    }
    
    // ==================== Schema Query Methods ====================
    
    /**
     * 获取所有Schema
     */
    public Map<String, CsdlSchema> getAllSchemas() {
        return context.getAllSchemas();
    }
    
    /**
     * 获取指定namespace的Schema
     */
    public CsdlSchema getSchema(String namespace) {
        return context.getSchema(namespace);
    }
    
    /**
     * 检查是否包含指定namespace的Schema
     */
    public boolean containsSchema(String namespace) {
        return context.containsSchema(namespace);
    }
    
    /**
     * 获取所有namespace
     */
    public Set<String> getAllNamespaces() {
        return context.getAllNamespaces();
    }
    
    /**
     * 移除Schema
     */
    public boolean removeSchema(String namespace) {
        boolean removed = context.removeSchema(namespace);
        if (removed) {
            LOG.info("Removed schema: {}", namespace);
        }
        return removed;
    }
    
    // ==================== Element Query Methods ====================
    
    /**
     * 获取EntityType
     */
    public CsdlEntityType getEntityType(FullQualifiedName fqn) {
        return context.getEntityType(fqn);
    }
    
    /**
     * 获取EntityType（支持别名）
     */
    public CsdlEntityType getEntityType(String namespace, String name) {
        String resolvedNamespace = context.resolveNamespace(namespace);
        if (resolvedNamespace != null) {
            return context.getEntityType(new FullQualifiedName(resolvedNamespace, name));
        }
        return null;
    }
    
    /**
     * 获取ComplexType
     */
    public CsdlComplexType getComplexType(FullQualifiedName fqn) {
        return context.getComplexType(fqn);
    }
    
    /**
     * 获取ComplexType（支持别名）
     */
    public CsdlComplexType getComplexType(String namespace, String name) {
        String resolvedNamespace = context.resolveNamespace(namespace);
        if (resolvedNamespace != null) {
            return context.getComplexType(new FullQualifiedName(resolvedNamespace, name));
        }
        return null;
    }
    
    /**
     * 获取Action
     */
    public CsdlAction getAction(FullQualifiedName fqn) {
        return context.getAction(fqn);
    }
    
    /**
     * 获取Action（支持别名）
     */
    public CsdlAction getAction(String namespace, String name) {
        String resolvedNamespace = context.resolveNamespace(namespace);
        if (resolvedNamespace != null) {
            return context.getAction(new FullQualifiedName(resolvedNamespace, name));
        }
        return null;
    }
    
    /**
     * 获取Function
     */
    public CsdlFunction getFunction(FullQualifiedName fqn) {
        return context.getFunction(fqn);
    }
    
    /**
     * 获取Function（支持别名）
     */
    public CsdlFunction getFunction(String namespace, String name) {
        String resolvedNamespace = context.resolveNamespace(namespace);
        if (resolvedNamespace != null) {
            return context.getFunction(new FullQualifiedName(resolvedNamespace, name));
        }
        return null;
    }
    
    /**
     * 获取TypeDefinition
     */
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName fqn) {
        return context.getTypeDefinition(fqn);
    }
    
    /**
     * 获取TypeDefinition（支持别名）
     */
    public CsdlTypeDefinition getTypeDefinition(String namespace, String name) {
        String resolvedNamespace = context.resolveNamespace(namespace);
        if (resolvedNamespace != null) {
            return context.getTypeDefinition(new FullQualifiedName(resolvedNamespace, name));
        }
        return null;
    }
    
    /**
     * 获取Term
     */
    public CsdlTerm getTerm(FullQualifiedName fqn) {
        return context.getTerm(fqn);
    }
    
    /**
     * 获取Term（支持别名）
     */
    public CsdlTerm getTerm(String namespace, String name) {
        String resolvedNamespace = context.resolveNamespace(namespace);
        if (resolvedNamespace != null) {
            return context.getTerm(new FullQualifiedName(resolvedNamespace, name));
        }
        return null;
    }
    
    /**
     * 获取EntityContainer
     */
    public CsdlEntityContainer getEntityContainer(String namespace) {
        String resolvedNamespace = context.resolveNamespace(namespace);
        if (resolvedNamespace != null) {
            return context.getEntityContainer(resolvedNamespace);
        }
        return null;
    }
    
    // ==================== Dependency Analysis Methods ====================
    
    /**
     * 获取依赖节点
     */
    public SchemaDependencyNode getDependencyNode(FullQualifiedName fqn) {
        return context.getDependencyNode(fqn);
    }
    
    /**
     * 获取所有依赖节点
     */
    public Map<FullQualifiedName, SchemaDependencyNode> getAllDependencyNodes() {
        return context.getAllDependencyNodes();
    }
    
    /**
     * 检测循环依赖
     */
    public List<SchemaDependencyNode> detectCircularDependencies() {
        return dependencyAnalyzer.detectCircularDependencies();
    }
    
    /**
     * 获取未解析的依赖
     */
    public List<SchemaDependencyNode> getUnresolvedDependencies() {
        return dependencyAnalyzer.getUnresolvedDependencies();
    }
    
    // ==================== Alias Management Methods ====================
    
    /**
     * 解析namespace（支持别名）
     */
    public String resolveNamespace(String aliasOrNamespace) {
        return context.resolveNamespace(aliasOrNamespace);
    }
    
    /**
     * 获取别名到namespace的映射
     */
    public Map<String, String> getAliasToNamespaceMap() {
        return context.getAliasToNamespaceMap();
    }
    
    /**
     * 获取namespace到别名的映射
     */
    public Map<String, String> getNamespaceToAliasMap() {
        return context.getNamespaceToAliasMap();
    }
    
    // ==================== Statistics and Management Methods ====================
    
    /**
     * 获取Repository统计信息
     */
    public SchemaRepositoryContext.RepositoryStatistics getStatistics() {
        return context.getStatistics();
    }
    
    /**
     * 清空所有数据
     */
    public void clear() {
        context.clear();
        LOG.info("Schema repository cleared");
    }
    
    /**
     * 验证Repository完整性
     */
    public RepositoryValidationResult validateIntegrity() {
        List<SchemaDependencyNode> circularDependencies = detectCircularDependencies();
        List<SchemaDependencyNode> unresolvedDependencies = getUnresolvedDependencies();
        
        boolean isValid = circularDependencies.isEmpty() && unresolvedDependencies.isEmpty();
        
        return new RepositoryValidationResult(isValid, circularDependencies, unresolvedDependencies);
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * 分析最新加载的Schema
     */
    private void analyzeLoadedSchema() {
        // 获取最新的Schema并分析其依赖关系
        Map<String, CsdlSchema> allSchemas = context.getAllSchemas();
        for (CsdlSchema schema : allSchemas.values()) {
            dependencyAnalyzer.analyzeSchema(schema);
        }
    }
    
    /**
     * 分析所有Schema的依赖关系
     */
    private void analyzeAllSchemas() {
        Map<String, CsdlSchema> allSchemas = context.getAllSchemas();
        for (CsdlSchema schema : allSchemas.values()) {
            dependencyAnalyzer.analyzeSchema(schema);
        }
        
        LOG.info("Analyzed dependencies for {} schemas", allSchemas.size());
    }
    
    // ==================== AutoCloseable Implementation ====================
    
    @Override
    public void close() {
        if (loader != null) {
            loader.shutdown();
        }
        LOG.info("OData Schema Repository closed");
    }
    
    // ==================== Validation Result Class ====================
    
    /**
     * Repository验证结果
     */
    public static class RepositoryValidationResult {
        private final boolean valid;
        private final List<SchemaDependencyNode> circularDependencies;
        private final List<SchemaDependencyNode> unresolvedDependencies;
        
        public RepositoryValidationResult(boolean valid, 
                                        List<SchemaDependencyNode> circularDependencies,
                                        List<SchemaDependencyNode> unresolvedDependencies) {
            this.valid = valid;
            this.circularDependencies = circularDependencies;
            this.unresolvedDependencies = unresolvedDependencies;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<SchemaDependencyNode> getCircularDependencies() {
            return circularDependencies;
        }
        
        public List<SchemaDependencyNode> getUnresolvedDependencies() {
            return unresolvedDependencies;
        }
        
        public boolean hasCircularDependencies() {
            return circularDependencies != null && !circularDependencies.isEmpty();
        }
        
        public boolean hasUnresolvedDependencies() {
            return unresolvedDependencies != null && !unresolvedDependencies.isEmpty();
        }
    }
}
