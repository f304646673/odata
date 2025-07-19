# OData Spring Boot XML 拆分项目 (samples/spring-boot-odata-xml-split)

## 概览

`samples/spring-boot-odata-xml-split` 项目展示了 **大型 XML 元数据的拆分和模块化管理技术**。该项目专门解决超大型 OData 服务的元数据管理问题，通过将庞大的 XML 元数据文件拆分为多个模块化的片段，实现更好的维护性、性能和可扩展性。

## 学习目标

- 掌握大型 XML 元数据的拆分策略
- 理解模块化元数据管理架构
- 学会动态组装和加载元数据
- 了解分布式元数据缓存机制

## 核心架构

### XML 元数据拆分架构图

```
┌─────────────────────────────────────────────────────────────────┐
│               XML 元数据拆分管理架构                              │
├─────────────────────────────────────────────────────────────────┤
│                   Source Metadata Layer                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │               Monolithic XML Metadata                       │ │
│  │  ┌─────────────┐┌─────────────┐┌─────────────┐┌───────────┐  │ │
│  │  │ Entity Sets ││ Entity Types││ Associations││ Functions │  │ │
│  │  │ (2000+ ETs) ││ Properties  ││ Navigation  ││ Actions   │  │ │
│  │  │ Large       ││ Complex     ││ Properties  ││ Complex   │  │ │
│  │  │ Schemas     ││ Types       ││ Constraints ││ Operations│  │ │
│  │  └─────────────┘└─────────────┘└─────────────┘└───────────┘  │ │
│  └─────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Splitting Strategy Layer                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Domain-based  │  │   Size-based    │  │   Dependency    │ │
│  │   Splitting     │  │   Splitting     │  │   Analysis      │ │
│  │                 │  │                 │  │                 │ │
│  │ Business Domain │  │ Max 500 Entities│  │ Reference Graph │ │
│  │ Functional Area │  │ File Size Limit │  │ Circular Deps   │ │
│  │ Team Ownership  │  │ Memory Footprint│  │ Load Priority   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Module Management Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Core Module   │  │   Domain        │  │   Extension     │ │
│  │                 │  │   Modules       │  │   Modules       │ │
│  │                 │  │                 │  │                 │ │
│  │ Common Types    │  │ HR.xml          │  │ Custom.xml      │ │
│  │ Base Entities   │  │ Finance.xml     │  │ Integration.xml │ │
│  │ Shared Schemas  │  │ Sales.xml       │  │ Analytics.xml   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Dynamic Assembly Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Module        │  │   Dependency    │  │   Assembly      │ │
│  │   Discovery     │  │   Resolution    │  │   Engine        │ │
│  │                 │  │                 │  │                 │ │
│  │ Auto Scan       │  │ Topological     │  │ XML Merge       │ │
│  │ Config Based    │  │ Sort            │  │ Namespace Fix   │ │
│  │ Runtime Load    │  │ Circular Check  │  │ Reference Link  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Caching & Performance Layer                   │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Module        │  │   Assembled     │  │   Runtime       │ │
│  │   Cache         │  │   Cache         │  │   Optimization  │ │
│  │                 │  │                 │  │                 │ │
│  │ L1: Memory      │  │ Full Schema     │  │ Lazy Loading    │ │
│  │ L2: Disk        │  │ Partial Schema  │  │ Parallel Load   │ │
│  │ L3: Distributed │  │ Version Cache   │  │ Stream Process  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. 元数据拆分服务

```java
/**
 * XML Metadata Splitting Service
 * 
 * This service handles the splitting of large XML metadata files
 * into manageable, modular components.
 */
@Service
@Slf4j
public class XmlMetadataSplittingService {

    @Autowired
    private MetadataAnalyzer metadataAnalyzer;
    
    @Autowired
    private DependencyResolver dependencyResolver;
    
    @Autowired
    private ModuleManager moduleManager;
    
    @Autowired
    private SplittingConfig splittingConfig;

    /**
     * Split large metadata into modules
     */
    public SplittingResult splitMetadata(SplittingRequest request) throws SplittingException {
        try {
            log.info("Starting metadata splitting for: {}", request.getSourceName());
            
            // Step 1: Parse and analyze source metadata
            XMLMetadata sourceMetadata = XMLMetadata.parse(request.getSourceContent());
            MetadataAnalysis analysis = metadataAnalyzer.analyze(sourceMetadata);
            
            // Step 2: Determine splitting strategy
            SplittingStrategy strategy = determineSplittingStrategy(analysis, request.getStrategy());
            
            // Step 3: Identify module boundaries
            List<ModuleBoundary> boundaries = identifyModuleBoundaries(analysis, strategy);
            
            // Step 4: Resolve dependencies between modules
            DependencyGraph dependencyGraph = dependencyResolver.resolveDependencies(boundaries);
            
            // Step 5: Validate splitting plan
            ValidationResult validation = validateSplittingPlan(boundaries, dependencyGraph);
            if (!validation.isValid()) {
                throw new SplittingException("Splitting plan validation failed: " + validation.getErrors());
            }
            
            // Step 6: Generate module files
            List<MetadataModule> modules = generateModules(sourceMetadata, boundaries, dependencyGraph);
            
            // Step 7: Create assembly configuration
            AssemblyConfiguration assemblyConfig = createAssemblyConfiguration(modules, dependencyGraph);
            
            // Step 8: Save modules and configuration
            SplittingResult result = saveModules(modules, assemblyConfig, request);
            
            log.info("Metadata splitting completed. Generated {} modules", modules.size());
            return result;
            
        } catch (Exception e) {
            log.error("Metadata splitting failed", e);
            throw new SplittingException("Splitting operation failed", e);
        }
    }

    /**
     * Split by domain/business area
     */
    public SplittingResult splitByDomain(String xmlContent, DomainSplittingConfig config) 
        throws SplittingException {
        
        SplittingRequest request = SplittingRequest.builder()
            .sourceContent(xmlContent)
            .strategy(SplittingStrategy.DOMAIN_BASED)
            .domainConfig(config)
            .build();
            
        return splitMetadata(request);
    }

    /**
     * Split by size constraints
     */
    public SplittingResult splitBySize(String xmlContent, SizeSplittingConfig config) 
        throws SplittingException {
        
        SplittingRequest request = SplittingRequest.builder()
            .sourceContent(xmlContent)
            .strategy(SplittingStrategy.SIZE_BASED)
            .sizeConfig(config)
            .build();
            
        return splitMetadata(request);
    }

    /**
     * Split with custom strategy
     */
    public SplittingResult splitWithCustomStrategy(String xmlContent, CustomSplittingStrategy strategy) 
        throws SplittingException {
        
        SplittingRequest request = SplittingRequest.builder()
            .sourceContent(xmlContent)
            .strategy(SplittingStrategy.CUSTOM)
            .customStrategy(strategy)
            .build();
            
        return splitMetadata(request);
    }

    // Private helper methods
    private SplittingStrategy determineSplittingStrategy(MetadataAnalysis analysis, 
        SplittingStrategy requestedStrategy) {
        
        if (requestedStrategy != null) {
            return requestedStrategy;
        }
        
        // Auto-determine based on analysis
        if (analysis.getEntityCount() > 1000) {
            return SplittingStrategy.SIZE_BASED;
        } else if (analysis.hasClearDomainBoundaries()) {
            return SplittingStrategy.DOMAIN_BASED;
        } else {
            return SplittingStrategy.HYBRID;
        }
    }

    private List<ModuleBoundary> identifyModuleBoundaries(MetadataAnalysis analysis, 
        SplittingStrategy strategy) throws SplittingException {
        
        switch (strategy) {
            case DOMAIN_BASED:
                return identifyDomainBoundaries(analysis);
                
            case SIZE_BASED:
                return identifySizeBoundaries(analysis);
                
            case DEPENDENCY_BASED:
                return identifyDependencyBoundaries(analysis);
                
            case HYBRID:
                return identifyHybridBoundaries(analysis);
                
            default:
                throw new SplittingException("Unsupported splitting strategy: " + strategy);
        }
    }

    private List<ModuleBoundary> identifyDomainBoundaries(MetadataAnalysis analysis) {
        List<ModuleBoundary> boundaries = new ArrayList<>();
        
        // Group entities by domain based on naming patterns, annotations, or configuration
        Map<String, List<EntityInfo>> domainGroups = groupEntitiesByDomain(analysis.getEntities());
        
        for (Map.Entry<String, List<EntityInfo>> entry : domainGroups.entrySet()) {
            String domain = entry.getKey();
            List<EntityInfo> entities = entry.getValue();
            
            ModuleBoundary boundary = ModuleBoundary.builder()
                .name(domain + "Module")
                .domain(domain)
                .entities(entities)
                .build();
                
            boundaries.add(boundary);
        }
        
        // Add core module for shared components
        boundaries.add(createCoreModule(analysis));
        
        return boundaries;
    }

    private List<ModuleBoundary> identifySizeBoundaries(MetadataAnalysis analysis) {
        List<ModuleBoundary> boundaries = new ArrayList<>();
        
        int maxEntitiesPerModule = splittingConfig.getMaxEntitiesPerModule();
        List<EntityInfo> entities = analysis.getEntities();
        
        for (int i = 0; i < entities.size(); i += maxEntitiesPerModule) {
            int endIndex = Math.min(i + maxEntitiesPerModule, entities.size());
            List<EntityInfo> moduleEntities = entities.subList(i, endIndex);
            
            ModuleBoundary boundary = ModuleBoundary.builder()
                .name("Module" + (i / maxEntitiesPerModule + 1))
                .entities(moduleEntities)
                .build();
                
            boundaries.add(boundary);
        }
        
        return boundaries;
    }

    private List<ModuleBoundary> identifyDependencyBoundaries(MetadataAnalysis analysis) {
        // Use graph clustering algorithms to identify natural boundaries
        DependencyGraph graph = analysis.getDependencyGraph();
        List<Set<EntityInfo>> clusters = GraphClusterer.cluster(graph);
        
        List<ModuleBoundary> boundaries = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            Set<EntityInfo> cluster = clusters.get(i);
            
            ModuleBoundary boundary = ModuleBoundary.builder()
                .name("DependencyModule" + (i + 1))
                .entities(new ArrayList<>(cluster))
                .build();
                
            boundaries.add(boundary);
        }
        
        return boundaries;
    }

    private List<ModuleBoundary> identifyHybridBoundaries(MetadataAnalysis analysis) {
        // Combine domain and size-based strategies
        List<ModuleBoundary> domainBoundaries = identifyDomainBoundaries(analysis);
        
        // Further split large domain modules by size
        List<ModuleBoundary> finalBoundaries = new ArrayList<>();
        for (ModuleBoundary boundary : domainBoundaries) {
            if (boundary.getEntities().size() > splittingConfig.getMaxEntitiesPerModule()) {
                finalBoundaries.addAll(splitBoundaryBySize(boundary));
            } else {
                finalBoundaries.add(boundary);
            }
        }
        
        return finalBoundaries;
    }

    private Map<String, List<EntityInfo>> groupEntitiesByDomain(List<EntityInfo> entities) {
        Map<String, List<EntityInfo>> groups = new HashMap<>();
        
        for (EntityInfo entity : entities) {
            String domain = extractDomain(entity);
            groups.computeIfAbsent(domain, k -> new ArrayList<>()).add(entity);
        }
        
        return groups;
    }

    private String extractDomain(EntityInfo entity) {
        // Extract domain from entity name, namespace, or annotations
        String name = entity.getName();
        
        // Check for domain prefixes (e.g., HR_Employee, Finance_Invoice)
        if (name.contains("_")) {
            return name.substring(0, name.indexOf("_"));
        }
        
        // Check for domain annotations
        if (entity.hasAnnotation("Domain")) {
            return entity.getAnnotationValue("Domain");
        }
        
        // Fallback to namespace-based grouping
        return entity.getNamespace();
    }

    private ModuleBoundary createCoreModule(MetadataAnalysis analysis) {
        List<EntityInfo> coreEntities = analysis.getEntities().stream()
            .filter(this::isCoreEntity)
            .collect(Collectors.toList());
            
        return ModuleBoundary.builder()
            .name("CoreModule")
            .entities(coreEntities)
            .isCore(true)
            .build();
    }

    private boolean isCoreEntity(EntityInfo entity) {
        // Identify core entities that are referenced by multiple modules
        return entity.isShared() || entity.isBaseType() || entity.getName().startsWith("Common");
    }

    private List<ModuleBoundary> splitBoundaryBySize(ModuleBoundary boundary) {
        List<ModuleBoundary> subBoundaries = new ArrayList<>();
        List<EntityInfo> entities = boundary.getEntities();
        int maxSize = splittingConfig.getMaxEntitiesPerModule();
        
        for (int i = 0; i < entities.size(); i += maxSize) {
            int endIndex = Math.min(i + maxSize, entities.size());
            List<EntityInfo> subEntities = entities.subList(i, endIndex);
            
            ModuleBoundary subBoundary = ModuleBoundary.builder()
                .name(boundary.getName() + "_Part" + (i / maxSize + 1))
                .domain(boundary.getDomain())
                .entities(subEntities)
                .parentModule(boundary.getName())
                .build();
                
            subBoundaries.add(subBoundary);
        }
        
        return subBoundaries;
    }

    private ValidationResult validateSplittingPlan(List<ModuleBoundary> boundaries, 
        DependencyGraph dependencyGraph) {
        
        ValidationResult result = new ValidationResult();
        
        // Check for circular dependencies
        if (dependencyGraph.hasCircularDependencies()) {
            result.addError("Circular dependencies detected between modules");
        }
        
        // Check for orphaned entities
        Set<String> allEntities = boundaries.stream()
            .flatMap(b -> b.getEntities().stream())
            .map(EntityInfo::getName)
            .collect(Collectors.toSet());
            
        Set<String> referencedEntities = dependencyGraph.getAllReferencedEntities();
        for (String referenced : referencedEntities) {
            if (!allEntities.contains(referenced)) {
                result.addWarning("Referenced entity not included in any module: " + referenced);
            }
        }
        
        // Check module sizes
        for (ModuleBoundary boundary : boundaries) {
            if (boundary.getEntities().size() > splittingConfig.getMaxEntitiesPerModule()) {
                result.addWarning("Module " + boundary.getName() + " exceeds recommended size");
            }
        }
        
        return result;
    }

    private List<MetadataModule> generateModules(XMLMetadata sourceMetadata, 
        List<ModuleBoundary> boundaries, DependencyGraph dependencyGraph) throws SplittingException {
        
        List<MetadataModule> modules = new ArrayList<>();
        
        for (ModuleBoundary boundary : boundaries) {
            try {
                String moduleContent = extractModuleContent(sourceMetadata, boundary, dependencyGraph);
                
                MetadataModule module = MetadataModule.builder()
                    .name(boundary.getName())
                    .content(moduleContent)
                    .entities(boundary.getEntities())
                    .dependencies(getDependencies(boundary, dependencyGraph))
                    .isCore(boundary.isCore())
                    .domain(boundary.getDomain())
                    .build();
                    
                modules.add(module);
                
            } catch (Exception e) {
                throw new SplittingException("Failed to generate module: " + boundary.getName(), e);
            }
        }
        
        return modules;
    }

    private String extractModuleContent(XMLMetadata sourceMetadata, ModuleBoundary boundary, 
        DependencyGraph dependencyGraph) throws Exception {
        
        Document sourceDoc = parseXML(sourceMetadata.getXmlContent());
        Document moduleDoc = createEmptyModuleDocument();
        
        // Copy schema structure
        copySchemaStructure(sourceDoc, moduleDoc, boundary);
        
        // Copy entity types for this module
        copyEntityTypes(sourceDoc, moduleDoc, boundary.getEntities());
        
        // Copy entity sets
        copyEntitySets(sourceDoc, moduleDoc, boundary.getEntities());
        
        // Copy related complex types and enums
        copyRelatedTypes(sourceDoc, moduleDoc, boundary, dependencyGraph);
        
        // Copy associations and navigation properties
        copyAssociations(sourceDoc, moduleDoc, boundary, dependencyGraph);
        
        return documentToString(moduleDoc);
    }

    private AssemblyConfiguration createAssemblyConfiguration(List<MetadataModule> modules, 
        DependencyGraph dependencyGraph) {
        
        return AssemblyConfiguration.builder()
            .modules(modules.stream().map(this::createModuleReference).collect(Collectors.toList()))
            .dependencies(dependencyGraph.getEdges())
            .loadOrder(calculateLoadOrder(modules, dependencyGraph))
            .build();
    }

    private SplittingResult saveModules(List<MetadataModule> modules, 
        AssemblyConfiguration assemblyConfig, SplittingRequest request) throws SplittingException {
        
        try {
            String outputDir = request.getOutputDirectory();
            if (outputDir == null) {
                outputDir = splittingConfig.getDefaultOutputDirectory();
            }
            
            // Save individual module files
            List<String> savedFiles = new ArrayList<>();
            for (MetadataModule module : modules) {
                String fileName = module.getName() + ".xml";
                String filePath = Paths.get(outputDir, fileName).toString();
                
                Files.write(Paths.get(filePath), module.getContent().getBytes(StandardCharsets.UTF_8));
                savedFiles.add(filePath);
            }
            
            // Save assembly configuration
            String configPath = Paths.get(outputDir, "assembly-config.json").toString();
            String configJson = objectMapper.writeValueAsString(assemblyConfig);
            Files.write(Paths.get(configPath), configJson.getBytes(StandardCharsets.UTF_8));
            
            return SplittingResult.builder()
                .modules(modules)
                .assemblyConfiguration(assemblyConfig)
                .outputDirectory(outputDir)
                .savedFiles(savedFiles)
                .configurationFile(configPath)
                .build();
                
        } catch (Exception e) {
            throw new SplittingException("Failed to save modules", e);
        }
    }

    // Additional helper methods for XML manipulation, dependency calculation, etc.
    private Document parseXML(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }

    private Document createEmptyModuleDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Create root edmx:Edmx element
        Element edmxRoot = doc.createElementNS("http://docs.oasis-open.org/odata/ns/edmx", "edmx:Edmx");
        edmxRoot.setAttribute("Version", "4.0");
        doc.appendChild(edmxRoot);
        
        // Create DataServices element
        Element dataServices = doc.createElementNS("http://docs.oasis-open.org/odata/ns/edmx", "edmx:DataServices");
        edmxRoot.appendChild(dataServices);
        
        return doc;
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        return writer.toString();
    }
}
```

### 2. 动态组装服务

```java
/**
 * Dynamic Metadata Assembly Service
 * 
 * Assembles split metadata modules into a complete schema at runtime.
 */
@Service
@Slf4j
public class DynamicMetadataAssemblyService {

    @Autowired
    private ModuleLoader moduleLoader;
    
    @Autowired
    private DependencyResolver dependencyResolver;
    
    @Autowired
    private MetadataCache metadataCache;
    
    @Autowired
    private AssemblyConfig assemblyConfig;

    /**
     * Assemble complete metadata from modules
     */
    public AssembledMetadata assembleMetadata(AssemblyRequest request) throws AssemblyException {
        try {
            String cacheKey = generateCacheKey(request);
            
            // Check cache first
            AssembledMetadata cached = metadataCache.get(cacheKey);
            if (cached != null && !request.isForceRefresh()) {
                log.debug("Returning cached assembled metadata: {}", cacheKey);
                return cached;
            }
            
            // Load assembly configuration
            AssemblyConfiguration config = loadAssemblyConfiguration(request.getConfigPath());
            
            // Load all required modules
            List<MetadataModule> modules = loadRequiredModules(config, request);
            
            // Resolve load order based on dependencies
            List<MetadataModule> orderedModules = resolveLoadOrder(modules, config);
            
            // Assemble modules into complete metadata
            String assembledXml = assembleModules(orderedModules);
            
            // Validate assembled metadata
            ValidationResult validation = validateAssembledMetadata(assembledXml);
            if (!validation.isValid()) {
                throw new AssemblyException("Assembled metadata validation failed: " + validation.getErrors());
            }
            
            // Create assembled metadata object
            AssembledMetadata assembled = AssembledMetadata.builder()
                .xmlContent(assembledXml)
                .modules(modules)
                .assemblyConfiguration(config)
                .assemblyTime(Instant.now())
                .cacheKey(cacheKey)
                .build();
            
            // Cache the result
            metadataCache.put(cacheKey, assembled);
            
            log.info("Successfully assembled metadata from {} modules", modules.size());
            return assembled;
            
        } catch (Exception e) {
            log.error("Metadata assembly failed", e);
            throw new AssemblyException("Assembly operation failed", e);
        }
    }

    /**
     * Assemble specific modules only
     */
    public AssembledMetadata assemblePartialMetadata(List<String> moduleNames) throws AssemblyException {
        AssemblyRequest request = AssemblyRequest.builder()
            .moduleFilter(moduleNames)
            .partialAssembly(true)
            .build();
            
        return assembleMetadata(request);
    }

    /**
     * Hot reload a specific module
     */
    public void hotReloadModule(String moduleName) throws AssemblyException {
        try {
            log.info("Hot reloading module: {}", moduleName);
            
            // Invalidate related cache entries
            metadataCache.invalidateByModule(moduleName);
            
            // Reload the module
            MetadataModule reloadedModule = moduleLoader.loadModule(moduleName);
            
            // Update runtime provider if needed
            updateRuntimeProvider(reloadedModule);
            
            log.info("Module {} hot reloaded successfully", moduleName);
            
        } catch (Exception e) {
            log.error("Hot reload failed for module: " + moduleName, e);
            throw new AssemblyException("Hot reload failed", e);
        }
    }

    /**
     * Assemble with lazy loading
     */
    public LazyAssembledMetadata assembleLazy(AssemblyRequest request) throws AssemblyException {
        // Create a lazy-loading wrapper that loads modules on demand
        return new LazyAssembledMetadata(this, request);
    }

    // Private helper methods
    private AssemblyConfiguration loadAssemblyConfiguration(String configPath) throws AssemblyException {
        try {
            if (configPath != null) {
                String configJson = Files.readString(Paths.get(configPath));
                return objectMapper.readValue(configJson, AssemblyConfiguration.class);
            } else {
                return assemblyConfig.getDefaultConfiguration();
            }
        } catch (Exception e) {
            throw new AssemblyException("Failed to load assembly configuration", e);
        }
    }

    private List<MetadataModule> loadRequiredModules(AssemblyConfiguration config, 
        AssemblyRequest request) throws AssemblyException {
        
        List<String> moduleNames = determineRequiredModules(config, request);
        List<MetadataModule> modules = new ArrayList<>();
        
        for (String moduleName : moduleNames) {
            try {
                MetadataModule module = moduleLoader.loadModule(moduleName);
                modules.add(module);
            } catch (Exception e) {
                if (request.isFailFast()) {
                    throw new AssemblyException("Failed to load module: " + moduleName, e);
                } else {
                    log.warn("Failed to load module {}, skipping: {}", moduleName, e.getMessage());
                }
            }
        }
        
        return modules;
    }

    private List<String> determineRequiredModules(AssemblyConfiguration config, AssemblyRequest request) {
        if (request.getModuleFilter() != null && !request.getModuleFilter().isEmpty()) {
            return request.getModuleFilter();
        }
        
        return config.getModules().stream()
            .map(ModuleReference::getName)
            .collect(Collectors.toList());
    }

    private List<MetadataModule> resolveLoadOrder(List<MetadataModule> modules, 
        AssemblyConfiguration config) throws AssemblyException {
        
        try {
            return dependencyResolver.topologicalSort(modules, config.getDependencies());
        } catch (CircularDependencyException e) {
            throw new AssemblyException("Circular dependency detected in modules", e);
        }
    }

    private String assembleModules(List<MetadataModule> orderedModules) throws AssemblyException {
        try {
            Document assembledDoc = createEmptyDocument();
            Element schemaElement = createSchemaElement(assembledDoc);
            
            // Merge all modules into the assembled document
            for (MetadataModule module : orderedModules) {
                mergeModuleIntoDocument(assembledDoc, schemaElement, module);
            }
            
            // Post-process the assembled document
            postProcessAssembledDocument(assembledDoc);
            
            return documentToString(assembledDoc);
            
        } catch (Exception e) {
            throw new AssemblyException("Failed to assemble modules", e);
        }
    }

    private void mergeModuleIntoDocument(Document assembledDoc, Element schemaElement, 
        MetadataModule module) throws Exception {
        
        Document moduleDoc = parseXML(module.getContent());
        
        // Import all child elements from module schema
        NodeList moduleChildren = moduleDoc.getDocumentElement()
            .getElementsByTagName("Schema").item(0).getChildNodes();
            
        for (int i = 0; i < moduleChildren.getLength(); i++) {
            Node child = moduleChildren.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Node importedNode = assembledDoc.importNode(child, true);
                schemaElement.appendChild(importedNode);
            }
        }
    }

    private void postProcessAssembledDocument(Document assembledDoc) throws Exception {
        // Remove duplicate elements
        removeDuplicateElements(assembledDoc);
        
        // Fix cross-references
        fixCrossReferences(assembledDoc);
        
        // Sort elements for consistency
        sortSchemaElements(assembledDoc);
        
        // Add assembly metadata
        addAssemblyMetadata(assembledDoc);
    }

    private void removeDuplicateElements(Document doc) {
        // Implementation to remove duplicate EntityType, ComplexType, etc.
        Map<String, Element> seenElements = new HashMap<>();
        
        NodeList elements = doc.getElementsByTagName("*");
        List<Node> toRemove = new ArrayList<>();
        
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            String key = generateElementKey(element);
            
            if (seenElements.containsKey(key)) {
                toRemove.add(element);
            } else {
                seenElements.put(key, element);
            }
        }
        
        // Remove duplicates
        for (Node node : toRemove) {
            node.getParentNode().removeChild(node);
        }
    }

    private String generateElementKey(Element element) {
        String tagName = element.getTagName();
        String name = element.getAttribute("Name");
        String namespace = element.getAttribute("Namespace");
        
        return tagName + ":" + namespace + ":" + name;
    }

    private void fixCrossReferences(Document doc) {
        // Update references between modules to use fully qualified names
        updateTypeReferences(doc);
        updateNavigationPropertyReferences(doc);
        updateAssociationReferences(doc);
    }

    private void updateTypeReferences(Document doc) {
        NodeList properties = doc.getElementsByTagName("Property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            String type = property.getAttribute("Type");
            
            if (type != null && !type.contains(".")) {
                // Add namespace prefix if missing
                String fullyQualifiedType = resolveTypeReference(type, doc);
                if (fullyQualifiedType != null) {
                    property.setAttribute("Type", fullyQualifiedType);
                }
            }
        }
    }

    private String resolveTypeReference(String typeName, Document doc) {
        // Search for the type definition in the document
        NodeList entityTypes = doc.getElementsByTagName("EntityType");
        for (int i = 0; i < entityTypes.getLength(); i++) {
            Element entityType = (Element) entityTypes.item(i);
            if (typeName.equals(entityType.getAttribute("Name"))) {
                Element schema = findParentSchema(entityType);
                if (schema != null) {
                    return schema.getAttribute("Namespace") + "." + typeName;
                }
            }
        }
        
        return null;
    }

    private Element findParentSchema(Element element) {
        Node parent = element.getParentNode();
        while (parent != null && !"Schema".equals(parent.getNodeName())) {
            parent = parent.getParentNode();
        }
        return (Element) parent;
    }

    private ValidationResult validateAssembledMetadata(String assembledXml) {
        try {
            // Use Olingo's built-in validation
            MetadataParser parser = new MetadataParser();
            parser.buildEdmProvider(new StringReader(assembledXml));
            
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("Validation failed: " + e.getMessage());
        }
    }

    private String generateCacheKey(AssemblyRequest request) {
        StringBuilder key = new StringBuilder();
        key.append("assembly:");
        
        if (request.getModuleFilter() != null) {
            key.append("modules:").append(String.join(",", request.getModuleFilter()));
        }
        
        if (request.getConfigPath() != null) {
            key.append(":config:").append(request.getConfigPath().hashCode());
        }
        
        return key.toString();
    }

    private void updateRuntimeProvider(MetadataModule module) throws AssemblyException {
        try {
            // Notify runtime provider of module changes
            ApplicationEventPublisher eventPublisher = ApplicationContextHolder.getBean(ApplicationEventPublisher.class);
            eventPublisher.publishEvent(new ModuleReloadedEvent(module));
            
        } catch (Exception e) {
            throw new AssemblyException("Failed to update runtime provider", e);
        }
    }

    // Additional helper methods for document creation, XML processing, etc.
    private Document createEmptyDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document doc = builder.newDocument();
        
        // Create root edmx:Edmx element
        Element edmxRoot = doc.createElementNS("http://docs.oasis-open.org/odata/ns/edmx", "edmx:Edmx");
        edmxRoot.setAttribute("Version", "4.0");
        doc.appendChild(edmxRoot);
        
        // Create DataServices element
        Element dataServices = doc.createElementNS("http://docs.oasis-open.org/odata/ns/edmx", "edmx:DataServices");
        edmxRoot.appendChild(dataServices);
        
        return doc;
    }

    private Element createSchemaElement(Document doc) {
        Element dataServices = (Element) doc.getElementsByTagName("edmx:DataServices").item(0);
        
        Element schema = doc.createElementNS("http://docs.oasis-open.org/odata/ns/edm", "Schema");
        schema.setAttribute("Namespace", assemblyConfig.getTargetNamespace());
        schema.setAttribute("xmlns", "http://docs.oasis-open.org/odata/ns/edm");
        dataServices.appendChild(schema);
        
        return schema;
    }
}
```

### 3. 模块加载器

```java
/**
 * Metadata Module Loader
 * 
 * Handles loading of individual metadata modules with caching and optimization.
 */
@Component
@Slf4j
public class ModuleLoader {

    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private ModuleCache moduleCache;
    
    @Autowired
    private ModuleConfig moduleConfig;
    
    @Value("${odata.modules.base-path}")
    private String modulesBasePath;

    /**
     * Load a module by name
     */
    public MetadataModule loadModule(String moduleName) throws ModuleLoadException {
        try {
            // Check cache first
            MetadataModule cached = moduleCache.get(moduleName);
            if (cached != null && isModuleFresh(cached)) {
                log.debug("Returning cached module: {}", moduleName);
                return cached;
            }
            
            // Load from storage
            MetadataModule module = loadModuleFromStorage(moduleName);
            
            // Validate module
            validateModule(module);
            
            // Cache the module
            moduleCache.put(moduleName, module);
            
            log.debug("Loaded module: {}", moduleName);
            return module;
            
        } catch (Exception e) {
            log.error("Failed to load module: " + moduleName, e);
            throw new ModuleLoadException("Module load failed: " + moduleName, e);
        }
    }

    /**
     * Load multiple modules in parallel
     */
    public CompletableFuture<List<MetadataModule>> loadModulesAsync(List<String> moduleNames) {
        List<CompletableFuture<MetadataModule>> futures = moduleNames.stream()
            .map(name -> CompletableFuture.supplyAsync(() -> {
                try {
                    return loadModule(name);
                } catch (ModuleLoadException e) {
                    throw new RuntimeException(e);
                }
            }))
            .collect(Collectors.toList());
            
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    /**
     * Stream load modules for memory efficiency
     */
    public Stream<MetadataModule> loadModulesStream(List<String> moduleNames) {
        return moduleNames.stream()
            .map(name -> {
                try {
                    return loadModule(name);
                } catch (ModuleLoadException e) {
                    log.error("Failed to load module in stream: " + name, e);
                    return null;
                }
            })
            .filter(Objects::nonNull);
    }

    /**
     * Reload a module (bypass cache)
     */
    public MetadataModule reloadModule(String moduleName) throws ModuleLoadException {
        try {
            // Remove from cache
            moduleCache.evict(moduleName);
            
            // Load fresh copy
            return loadModule(moduleName);
            
        } catch (Exception e) {
            throw new ModuleLoadException("Module reload failed: " + moduleName, e);
        }
    }

    /**
     * Check if a module exists
     */
    public boolean moduleExists(String moduleName) {
        try {
            return moduleRepository.exists(moduleName) || 
                   Files.exists(getModuleFilePath(moduleName));
        } catch (Exception e) {
            log.warn("Error checking module existence: " + moduleName, e);
            return false;
        }
    }

    /**
     * List all available modules
     */
    public List<String> listAvailableModules() {
        try {
            Set<String> modules = new HashSet<>();
            
            // Get from repository
            modules.addAll(moduleRepository.listModuleNames());
            
            // Get from file system
            Path modulesPath = Paths.get(modulesBasePath);
            if (Files.exists(modulesPath)) {
                try (Stream<Path> files = Files.list(modulesPath)) {
                    files.filter(p -> p.toString().endsWith(".xml"))
                         .map(p -> getModuleNameFromPath(p))
                         .forEach(modules::add);
                }
            }
            
            return new ArrayList<>(modules);
            
        } catch (Exception e) {
            log.error("Failed to list available modules", e);
            return new ArrayList<>();
        }
    }

    // Private helper methods
    private MetadataModule loadModuleFromStorage(String moduleName) throws ModuleLoadException {
        // Try loading from repository first
        Optional<MetadataModule> repoModule = moduleRepository.findByName(moduleName);
        if (repoModule.isPresent()) {
            return repoModule.get();
        }
        
        // Fallback to file system
        return loadModuleFromFile(moduleName);
    }

    private MetadataModule loadModuleFromFile(String moduleName) throws ModuleLoadException {
        try {
            Path filePath = getModuleFilePath(moduleName);
            
            if (!Files.exists(filePath)) {
                throw new ModuleLoadException("Module file not found: " + filePath);
            }
            
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            
            return MetadataModule.builder()
                .name(moduleName)
                .content(content)
                .lastModified(Files.getLastModifiedTime(filePath).toInstant())
                .source(ModuleSource.FILE_SYSTEM)
                .filePath(filePath.toString())
                .build();
                
        } catch (Exception e) {
            throw new ModuleLoadException("Failed to load module from file: " + moduleName, e);
        }
    }

    private Path getModuleFilePath(String moduleName) {
        String fileName = moduleName.endsWith(".xml") ? moduleName : moduleName + ".xml";
        return Paths.get(modulesBasePath, fileName);
    }

    private String getModuleNameFromPath(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".xml") ? 
            fileName.substring(0, fileName.length() - 4) : fileName;
    }

    private boolean isModuleFresh(MetadataModule cached) {
        try {
            if (cached.getSource() == ModuleSource.FILE_SYSTEM) {
                Path filePath = Paths.get(cached.getFilePath());
                if (Files.exists(filePath)) {
                    Instant fileModified = Files.getLastModifiedTime(filePath).toInstant();
                    return !fileModified.isAfter(cached.getLastModified());
                }
            } else if (cached.getSource() == ModuleSource.REPOSITORY) {
                return moduleRepository.isModuleFresh(cached.getName(), cached.getLastModified());
            }
            
            return true; // Assume fresh if we can't determine
            
        } catch (Exception e) {
            log.warn("Error checking module freshness: " + cached.getName(), e);
            return false; // Force reload on error
        }
    }

    private void validateModule(MetadataModule module) throws ModuleLoadException {
        try {
            // Basic XML validation
            parseXML(module.getContent());
            
            // CSDL validation
            validateCSDL(module.getContent());
            
            // Custom business rule validation
            validateModuleBusinessRules(module);
            
        } catch (Exception e) {
            throw new ModuleLoadException("Module validation failed: " + module.getName(), e);
        }
    }

    private void parseXML(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }

    private void validateCSDL(String xmlContent) throws Exception {
        // Use Olingo's validation
        MetadataParser parser = new MetadataParser();
        parser.buildEdmProvider(new StringReader(xmlContent));
    }

    private void validateModuleBusinessRules(MetadataModule module) throws ModuleLoadException {
        // Check for required elements
        if (!module.getContent().contains("EntityType") && !module.getContent().contains("ComplexType")) {
            throw new ModuleLoadException("Module must contain at least one EntityType or ComplexType");
        }
        
        // Check size limits
        if (module.getContent().length() > moduleConfig.getMaxModuleSize()) {
            throw new ModuleLoadException("Module exceeds maximum size limit");
        }
        
        // Additional business rules can be added here
    }
}
```

## REST API 端点

### 拆分和组装控制器

```java
@RestController
@RequestMapping("/api/metadata")
public class MetadataSplittingController {

    @Autowired
    private XmlMetadataSplittingService splittingService;
    
    @Autowired
    private DynamicMetadataAssemblyService assemblyService;

    @PostMapping("/split")
    public ResponseEntity<SplittingResult> splitMetadata(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "strategy", defaultValue = "DOMAIN_BASED") SplittingStrategy strategy) {
        
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            SplittingRequest request = SplittingRequest.builder()
                .sourceContent(content)
                .sourceName(file.getOriginalFilename())
                .strategy(strategy)
                .build();
                
            SplittingResult result = splittingService.splitMetadata(request);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(SplittingResult.error(e.getMessage()));
        }
    }

    @PostMapping("/assemble")
    public ResponseEntity<String> assembleMetadata(
            @RequestBody AssemblyRequest request) {
        
        try {
            AssembledMetadata assembled = assemblyService.assembleMetadata(request);
            return ResponseEntity.ok(assembled.getXmlContent());
            
        } catch (AssemblyException e) {
            return ResponseEntity.badRequest().body("Assembly failed: " + e.getMessage());
        }
    }

    @GetMapping("/modules")
    public ResponseEntity<List<String>> listModules() {
        List<String> modules = moduleLoader.listAvailableModules();
        return ResponseEntity.ok(modules);
    }

    @PostMapping("/modules/{moduleName}/reload")
    public ResponseEntity<String> reloadModule(@PathVariable String moduleName) {
        try {
            assemblyService.hotReloadModule(moduleName);
            return ResponseEntity.ok("Module reloaded: " + moduleName);
        } catch (AssemblyException e) {
            return ResponseEntity.badRequest().body("Reload failed: " + e.getMessage());
        }
    }
}
```

## 总结

`samples/spring-boot-odata-xml-split` 项目提供了完整的大型元数据拆分解决方案：

### 核心特性
- ✅ **智能拆分**：支持基于领域、大小、依赖关系的拆分策略
- ✅ **动态组装**：运行时按需组装元数据，支持部分加载
- ✅ **模块管理**：完整的模块生命周期管理，包括加载、缓存、热更新
- ✅ **依赖解析**：自动处理模块间依赖关系和加载顺序
- ✅ **性能优化**：多级缓存、并行加载、流式处理

### 适用场景
- **大型企业系统**：处理超大型 OData 服务的元数据管理
- **微服务架构**：模块化的元数据管理支持分布式开发
- **性能优化**：按需加载减少内存占用和启动时间
- **团队协作**：不同团队维护不同的元数据模块
