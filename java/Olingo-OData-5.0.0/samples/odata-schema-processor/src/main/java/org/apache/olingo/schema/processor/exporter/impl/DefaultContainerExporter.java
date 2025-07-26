package org.apache.olingo.schema.processor.exporter.impl;

import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.schema.processor.exporter.ContainerExporter;
import org.apache.olingo.schema.processor.exporter.builder.DynamicContainerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 默认的Container导出器实现
 * 使用Olingo底层方法进行容器导出
 */
public class DefaultContainerExporter implements ContainerExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultContainerExporter.class);
    
    // OData和EDMX命名空间常量
    private static final String EDMX_NAMESPACE = "http://docs.oasis-open.org/odata/ns/edmx";
    private static final String EDM_NAMESPACE = "http://docs.oasis-open.org/odata/ns/edm";
    private static final String EDMX_VERSION = "4.0";
    
    @Override
    public ContainerExportResult exportContainer(CsdlEntityContainer container, String outputPath, String containerNamespace) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> exportedElements = new ArrayList<>();
        
        try {
            // 创建包含容器的Schema
            CsdlSchema schema = createSchemaWithContainer(container, containerNamespace);
            
            // 导出到XML
            Document document = createEdmxDocument(Arrays.asList(schema), errors, warnings);
            writeDocumentToFile(document, outputPath);
            
            // 统计导出的元素
            int elementCount = countContainerElements(container, exportedElements);
            
            logger.info("Successfully exported container '{}' with {} elements to {}", 
                       container.getName(), elementCount, outputPath);
            
            return new ContainerExportResult(true, outputPath, elementCount, exportedElements, errors, warnings);
            
        } catch (Exception e) {
            logger.error("Failed to export container: {}", e.getMessage(), e);
            errors.add("Export failed: " + e.getMessage());
            return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
        }
    }
    
    @Override
    public ContainerExportResult exportContainerFromXml(String inputXmlPath, String outputPath, String containerName) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> exportedElements = new ArrayList<>();
        
        try {
            // 从XML文件加载Schema
            List<CsdlSchema> schemas = loadSchemasFromXml(inputXmlPath, errors, warnings);
            
            if (schemas.isEmpty()) {
                errors.add("No schemas found in input XML file");
                return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
            }
            
            // 查找指定的容器
            CsdlEntityContainer targetContainer = null;
            CsdlSchema targetSchema = null;
            
            for (CsdlSchema schema : schemas) {
                if (schema.getEntityContainer() != null) {
                    if (containerName == null || containerName.equals(schema.getEntityContainer().getName())) {
                        targetContainer = schema.getEntityContainer();
                        targetSchema = schema;
                        break;
                    }
                }
            }
            
            if (targetContainer == null) {
                errors.add("Container '" + containerName + "' not found in input XML");
                return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
            }
            
            // 创建新的Schema只包含容器及其依赖的元素
            CsdlSchema exportSchema = createSchemaWithContainerAndDependencies(targetContainer, targetSchema);
            
            // 导出到XML
            Document document = createEdmxDocument(Arrays.asList(exportSchema), errors, warnings);
            writeDocumentToFile(document, outputPath);
            
            // 统计导出的元素
            int elementCount = countContainerElements(targetContainer, exportedElements);
            
            logger.info("Successfully exported container '{}' from {} to {} with {} elements", 
                       targetContainer.getName(), inputXmlPath, outputPath, elementCount);
            
            return new ContainerExportResult(true, outputPath, elementCount, exportedElements, errors, warnings);
            
        } catch (Exception e) {
            logger.error("Failed to export container from XML: {}", e.getMessage(), e);
            errors.add("Export failed: " + e.getMessage());
            return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
        }
    }
    
    @Override
    public ContainerExportResult exportDynamicContainer(ContainerBuilder containerBuilder, String outputPath) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> exportedElements = new ArrayList<>();
        
        try {
            // 使用构建器创建容器
            ContainerBuilder.ContainerBuildResult buildResult = containerBuilder.build();
            CsdlEntityContainer container = buildResult.getContainer();
            CsdlSchema schema = buildResult.getSchema();
            
            if (container == null) {
                errors.add("Container builder returned null container");
                return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
            }
            
            // 如果没有提供Schema，创建一个默认的
            if (schema == null) {
                schema = createSchemaWithContainer(container, buildResult.getNamespace());
            }
            
            // 导出到XML
            Document document = createEdmxDocument(Arrays.asList(schema), errors, warnings);
            writeDocumentToFile(document, outputPath);
            
            // 统计导出的元素
            int elementCount = countContainerElements(container, exportedElements);
            
            logger.info("Successfully exported dynamic container '{}' with {} elements to {}", 
                       container.getName(), elementCount, outputPath);
            
            return new ContainerExportResult(true, outputPath, elementCount, exportedElements, errors, warnings);
            
        } catch (Exception e) {
            logger.error("Failed to export dynamic container: {}", e.getMessage(), e);
            errors.add("Export failed: " + e.getMessage());
            return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
        }
    }
    
    @Override
    public ContainerExportResult exportMergedContainers(List<CsdlSchema> schemas, String outputPath, String targetNamespace) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> exportedElements = new ArrayList<>();
        
        try {
            // 合并所有容器
            CsdlEntityContainer mergedContainer = mergeContainers(schemas, errors, warnings);
            
            if (mergedContainer == null) {
                errors.add("No containers found to merge");
                return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
            }
            
            // 创建合并后的Schema
            CsdlSchema mergedSchema = createMergedSchema(schemas, mergedContainer, targetNamespace);
            
            // 导出到XML
            Document document = createEdmxDocument(Arrays.asList(mergedSchema), errors, warnings);
            writeDocumentToFile(document, outputPath);
            
            // 统计导出的元素
            int elementCount = countContainerElements(mergedContainer, exportedElements);
            
            logger.info("Successfully exported merged container with {} elements to {}", elementCount, outputPath);
            
            return new ContainerExportResult(true, outputPath, elementCount, exportedElements, errors, warnings);
            
        } catch (Exception e) {
            logger.error("Failed to export merged containers: {}", e.getMessage(), e);
            errors.add("Export failed: " + e.getMessage());
            return new ContainerExportResult(false, outputPath, 0, exportedElements, errors, warnings);
        }
    }
    
    /**
     * 创建包含容器的Schema
     */
    private CsdlSchema createSchemaWithContainer(CsdlEntityContainer container, String namespace) {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(namespace != null ? namespace : "DefaultNamespace");
        schema.setEntityContainer(container);
        return schema;
    }
    
    /**
     * 创建包含容器及其依赖元素的Schema
     */
    private CsdlSchema createSchemaWithContainerAndDependencies(CsdlEntityContainer container, CsdlSchema sourceSchema) {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(sourceSchema.getNamespace());
        schema.setAlias(sourceSchema.getAlias());
        
        // 设置容器
        schema.setEntityContainer(container);
        
        // 收集容器依赖的类型
        Set<String> requiredTypes = collectRequiredTypes(container);
        
        // 添加依赖的EntityTypes
        if (sourceSchema.getEntityTypes() != null) {
            List<CsdlEntityType> requiredEntityTypes = new ArrayList<>();
            for (CsdlEntityType entityType : sourceSchema.getEntityTypes()) {
                if (requiredTypes.contains(entityType.getName())) {
                    requiredEntityTypes.add(entityType);
                }
            }
            schema.setEntityTypes(requiredEntityTypes);
        }
        
        // 添加依赖的ComplexTypes
        if (sourceSchema.getComplexTypes() != null) {
            List<CsdlComplexType> requiredComplexTypes = new ArrayList<>();
            for (CsdlComplexType complexType : sourceSchema.getComplexTypes()) {
                if (requiredTypes.contains(complexType.getName())) {
                    requiredComplexTypes.add(complexType);
                }
            }
            schema.setComplexTypes(requiredComplexTypes);
        }
        
        // 添加依赖的Actions
        if (sourceSchema.getActions() != null) {
            List<CsdlAction> requiredActions = new ArrayList<>();
            for (CsdlAction action : sourceSchema.getActions()) {
                if (requiredTypes.contains(action.getName())) {
                    requiredActions.add(action);
                }
            }
            schema.setActions(requiredActions);
        }
        
        // 添加依赖的Functions
        if (sourceSchema.getFunctions() != null) {
            List<CsdlFunction> requiredFunctions = new ArrayList<>();
            for (CsdlFunction function : sourceSchema.getFunctions()) {
                if (requiredTypes.contains(function.getName())) {
                    requiredFunctions.add(function);
                }
            }
            schema.setFunctions(requiredFunctions);
        }
        
        return schema;
    }
    
    /**
     * 收集容器依赖的类型名称
     */
    private Set<String> collectRequiredTypes(CsdlEntityContainer container) {
        Set<String> requiredTypes = new HashSet<>();
        
        // 从EntitySets收集EntityType
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                String typeName = extractLocalTypeName(entitySet.getType());
                if (typeName != null) {
                    requiredTypes.add(typeName);
                }
            }
        }
        
        // 从Singletons收集EntityType
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                String typeName = extractLocalTypeName(singleton.getType());
                if (typeName != null) {
                    requiredTypes.add(typeName);
                }
            }
        }
        
        // 从ActionImports收集Action
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                String actionName = extractLocalTypeName(actionImport.getAction());
                if (actionName != null) {
                    requiredTypes.add(actionName);
                }
            }
        }
        
        // 从FunctionImports收集Function
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                String functionName = extractLocalTypeName(functionImport.getFunction());
                if (functionName != null) {
                    requiredTypes.add(functionName);
                }
            }
        }
        
        return requiredTypes;
    }
    
    /**
     * 提取本地类型名称（去除命名空间前缀）
     */
    private String extractLocalTypeName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return null;
        }
        
        int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return lastDotIndex > 0 ? fullyQualifiedName.substring(lastDotIndex + 1) : fullyQualifiedName;
    }
    
    /**
     * 从XML文件加载Schemas
     */
    private List<CsdlSchema> loadSchemasFromXml(String xmlPath, List<String> errors, List<String> warnings) throws Exception {
        List<CsdlSchema> schemas = new ArrayList<>();
        
        String xmlContent = new String(Files.readAllBytes(Paths.get(xmlPath)), StandardCharsets.UTF_8);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        
        // 查找Schema元素
        NodeList schemaNodes = document.getElementsByTagNameNS(EDM_NAMESPACE, "Schema");
        if (schemaNodes.getLength() == 0) {
            schemaNodes = document.getElementsByTagName("Schema");
        }
        
        for (int i = 0; i < schemaNodes.getLength(); i++) {
            Element schemaElement = (Element) schemaNodes.item(i);
            CsdlSchema schema = parseSchemaFromElement(schemaElement, errors, warnings);
            if (schema != null) {
                schemas.add(schema);
            }
        }
        
        return schemas;
    }
    
    /**
     * 从XML元素解析Schema
     */
    private CsdlSchema parseSchemaFromElement(Element schemaElement, List<String> errors, List<String> warnings) {
        try {
            CsdlSchema schema = new CsdlSchema();
            
            // 设置基本属性
            schema.setNamespace(schemaElement.getAttribute("Namespace"));
            String alias = schemaElement.getAttribute("Alias");
            if (!alias.isEmpty()) {
                schema.setAlias(alias);
            }
            
            // 解析EntityContainer
            NodeList containerNodes = schemaElement.getElementsByTagNameNS(EDM_NAMESPACE, "EntityContainer");
            if (containerNodes.getLength() == 0) {
                containerNodes = schemaElement.getElementsByTagName("EntityContainer");
            }
            
            if (containerNodes.getLength() > 0) {
                Element containerElement = (Element) containerNodes.item(0);
                CsdlEntityContainer container = parseEntityContainerFromElement(containerElement, errors, warnings);
                schema.setEntityContainer(container);
            }
            
            // 这里可以继续解析其他元素（EntityTypes, ComplexTypes等）
            // 为了简化，现在只解析Container
            
            return schema;
            
        } catch (Exception e) {
            errors.add("Failed to parse schema: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从XML元素解析EntityContainer
     */
    private CsdlEntityContainer parseEntityContainerFromElement(Element containerElement, List<String> errors, List<String> warnings) {
        CsdlEntityContainer container = new CsdlEntityContainer();
        
        // 设置名称
        container.setName(containerElement.getAttribute("Name"));
        
        // 解析EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        NodeList entitySetNodes = containerElement.getElementsByTagNameNS(EDM_NAMESPACE, "EntitySet");
        if (entitySetNodes.getLength() == 0) {
            entitySetNodes = containerElement.getElementsByTagName("EntitySet");
        }
        
        for (int i = 0; i < entitySetNodes.getLength(); i++) {
            Element entitySetElement = (Element) entitySetNodes.item(i);
            CsdlEntitySet entitySet = new CsdlEntitySet();
            entitySet.setName(entitySetElement.getAttribute("Name"));
            entitySet.setType(entitySetElement.getAttribute("EntityType"));
            entitySets.add(entitySet);
        }
        container.setEntitySets(entitySets);
        
        // 解析Singletons
        List<CsdlSingleton> singletons = new ArrayList<>();
        NodeList singletonNodes = containerElement.getElementsByTagNameNS(EDM_NAMESPACE, "Singleton");
        if (singletonNodes.getLength() == 0) {
            singletonNodes = containerElement.getElementsByTagName("Singleton");
        }
        
        for (int i = 0; i < singletonNodes.getLength(); i++) {
            Element singletonElement = (Element) singletonNodes.item(i);
            CsdlSingleton singleton = new CsdlSingleton();
            singleton.setName(singletonElement.getAttribute("Name"));
            singleton.setType(singletonElement.getAttribute("Type"));
            singletons.add(singleton);
        }
        container.setSingletons(singletons);
        
        // 解析ActionImports
        List<CsdlActionImport> actionImports = new ArrayList<>();
        NodeList actionImportNodes = containerElement.getElementsByTagNameNS(EDM_NAMESPACE, "ActionImport");
        if (actionImportNodes.getLength() == 0) {
            actionImportNodes = containerElement.getElementsByTagName("ActionImport");
        }
        
        for (int i = 0; i < actionImportNodes.getLength(); i++) {
            Element actionImportElement = (Element) actionImportNodes.item(i);
            CsdlActionImport actionImport = new CsdlActionImport();
            actionImport.setName(actionImportElement.getAttribute("Name"));
            actionImport.setAction(actionImportElement.getAttribute("Action"));
            actionImports.add(actionImport);
        }
        container.setActionImports(actionImports);
        
        // 解析FunctionImports
        List<CsdlFunctionImport> functionImports = new ArrayList<>();
        NodeList functionImportNodes = containerElement.getElementsByTagNameNS(EDM_NAMESPACE, "FunctionImport");
        if (functionImportNodes.getLength() == 0) {
            functionImportNodes = containerElement.getElementsByTagName("FunctionImport");
        }
        
        for (int i = 0; i < functionImportNodes.getLength(); i++) {
            Element functionImportElement = (Element) functionImportNodes.item(i);
            CsdlFunctionImport functionImport = new CsdlFunctionImport();
            functionImport.setName(functionImportElement.getAttribute("Name"));
            functionImport.setFunction(functionImportElement.getAttribute("Function"));
            functionImports.add(functionImport);
        }
        container.setFunctionImports(functionImports);
        
        return container;
    }
    
    /**
     * 合并多个容器
     */
    private CsdlEntityContainer mergeContainers(List<CsdlSchema> schemas, List<String> errors, List<String> warnings) {
        CsdlEntityContainer mergedContainer = new CsdlEntityContainer();
        mergedContainer.setName("MergedContainer");
        
        List<CsdlEntitySet> allEntitySets = new ArrayList<>();
        List<CsdlSingleton> allSingletons = new ArrayList<>();
        List<CsdlActionImport> allActionImports = new ArrayList<>();
        List<CsdlFunctionImport> allFunctionImports = new ArrayList<>();
        
        Set<String> usedNames = new HashSet<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getEntityContainer() != null) {
                CsdlEntityContainer container = schema.getEntityContainer();
                
                // 合并EntitySets
                if (container.getEntitySets() != null) {
                    for (CsdlEntitySet entitySet : container.getEntitySets()) {
                        String uniqueName = ensureUniqueName(entitySet.getName(), usedNames);
                        if (!uniqueName.equals(entitySet.getName())) {
                            warnings.add("Renamed EntitySet '" + entitySet.getName() + "' to '" + uniqueName + "' to avoid conflicts");
                        }
                        
                        CsdlEntitySet newEntitySet = new CsdlEntitySet();
                        newEntitySet.setName(uniqueName);
                        newEntitySet.setType(entitySet.getType());
                        allEntitySets.add(newEntitySet);
                    }
                }
                
                // 合并Singletons
                if (container.getSingletons() != null) {
                    for (CsdlSingleton singleton : container.getSingletons()) {
                        String uniqueName = ensureUniqueName(singleton.getName(), usedNames);
                        if (!uniqueName.equals(singleton.getName())) {
                            warnings.add("Renamed Singleton '" + singleton.getName() + "' to '" + uniqueName + "' to avoid conflicts");
                        }
                        
                        CsdlSingleton newSingleton = new CsdlSingleton();
                        newSingleton.setName(uniqueName);
                        newSingleton.setType(singleton.getType());
                        allSingletons.add(newSingleton);
                    }
                }
                
                // 合并ActionImports
                if (container.getActionImports() != null) {
                    for (CsdlActionImport actionImport : container.getActionImports()) {
                        String uniqueName = ensureUniqueName(actionImport.getName(), usedNames);
                        if (!uniqueName.equals(actionImport.getName())) {
                            warnings.add("Renamed ActionImport '" + actionImport.getName() + "' to '" + uniqueName + "' to avoid conflicts");
                        }
                        
                        CsdlActionImport newActionImport = new CsdlActionImport();
                        newActionImport.setName(uniqueName);
                        newActionImport.setAction(actionImport.getAction());
                        allActionImports.add(newActionImport);
                    }
                }
                
                // 合并FunctionImports
                if (container.getFunctionImports() != null) {
                    for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                        String uniqueName = ensureUniqueName(functionImport.getName(), usedNames);
                        if (!uniqueName.equals(functionImport.getName())) {
                            warnings.add("Renamed FunctionImport '" + functionImport.getName() + "' to '" + uniqueName + "' to avoid conflicts");
                        }
                        
                        CsdlFunctionImport newFunctionImport = new CsdlFunctionImport();
                        newFunctionImport.setName(uniqueName);
                        newFunctionImport.setFunction(functionImport.getFunction());
                        allFunctionImports.add(newFunctionImport);
                    }
                }
            }
        }
        
        mergedContainer.setEntitySets(allEntitySets);
        mergedContainer.setSingletons(allSingletons);
        mergedContainer.setActionImports(allActionImports);
        mergedContainer.setFunctionImports(allFunctionImports);
        
        return mergedContainer;
    }
    
    /**
     * 确保名称唯一
     */
    private String ensureUniqueName(String baseName, Set<String> usedNames) {
        String uniqueName = baseName;
        int counter = 1;
        
        while (usedNames.contains(uniqueName)) {
            uniqueName = baseName + "_" + counter;
            counter++;
        }
        
        usedNames.add(uniqueName);
        return uniqueName;
    }
    
    /**
     * 创建合并后的Schema
     */
    private CsdlSchema createMergedSchema(List<CsdlSchema> schemas, CsdlEntityContainer mergedContainer, String targetNamespace) {
        CsdlSchema mergedSchema = new CsdlSchema();
        mergedSchema.setNamespace(targetNamespace != null ? targetNamespace : "MergedNamespace");
        mergedSchema.setEntityContainer(mergedContainer);
        
        // 可以在这里合并其他Schema元素（EntityTypes, ComplexTypes等）
        // 为了简化，现在只处理Container
        
        return mergedSchema;
    }
    
    /**
     * 创建EDMX文档
     */
    private Document createEdmxDocument(List<CsdlSchema> schemas, List<String> errors, List<String> warnings) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        
        // 创建根元素
        Element edmxRoot = document.createElementNS(EDMX_NAMESPACE, "edmx:Edmx");
        edmxRoot.setAttribute("Version", EDMX_VERSION);
        document.appendChild(edmxRoot);
        
        // 创建DataServices元素
        Element dataServices = document.createElementNS(EDMX_NAMESPACE, "edmx:DataServices");
        edmxRoot.appendChild(dataServices);
        
        // 添加Schema元素
        for (CsdlSchema schema : schemas) {
            Element schemaElement = createSchemaElement(document, schema);
            dataServices.appendChild(schemaElement);
        }
        
        return document;
    }
    
    /**
     * 创建Schema元素
     */
    private Element createSchemaElement(Document document, CsdlSchema schema) {
        Element schemaElement = document.createElementNS(EDM_NAMESPACE, "Schema");
        schemaElement.setAttribute("Namespace", schema.getNamespace());
        
        if (schema.getAlias() != null) {
            schemaElement.setAttribute("Alias", schema.getAlias());
        }
        
        // 添加EntityContainer
        if (schema.getEntityContainer() != null) {
            Element containerElement = createEntityContainerElement(document, schema.getEntityContainer());
            schemaElement.appendChild(containerElement);
        }
        
        // 这里可以添加其他Schema元素（EntityTypes, ComplexTypes等）
        
        return schemaElement;
    }
    
    /**
     * 创建EntityContainer元素
     */
    private Element createEntityContainerElement(Document document, CsdlEntityContainer container) {
        Element containerElement = document.createElementNS(EDM_NAMESPACE, "EntityContainer");
        containerElement.setAttribute("Name", container.getName());
        
        // 添加EntitySets
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                Element entitySetElement = document.createElementNS(EDM_NAMESPACE, "EntitySet");
                entitySetElement.setAttribute("Name", entitySet.getName());
                entitySetElement.setAttribute("EntityType", entitySet.getType());
                containerElement.appendChild(entitySetElement);
            }
        }
        
        // 添加Singletons
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                Element singletonElement = document.createElementNS(EDM_NAMESPACE, "Singleton");
                singletonElement.setAttribute("Name", singleton.getName());
                singletonElement.setAttribute("Type", singleton.getType());
                containerElement.appendChild(singletonElement);
            }
        }
        
        // 添加ActionImports
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                Element actionImportElement = document.createElementNS(EDM_NAMESPACE, "ActionImport");
                actionImportElement.setAttribute("Name", actionImport.getName());
                actionImportElement.setAttribute("Action", actionImport.getAction());
                containerElement.appendChild(actionImportElement);
            }
        }
        
        // 添加FunctionImports
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                Element functionImportElement = document.createElementNS(EDM_NAMESPACE, "FunctionImport");
                functionImportElement.setAttribute("Name", functionImport.getName());
                functionImportElement.setAttribute("Function", functionImport.getFunction());
                containerElement.appendChild(functionImportElement);
            }
        }
        
        return containerElement;
    }
    
    /**
     * 将文档写入文件
     */
    private void writeDocumentToFile(Document document, String outputPath) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        // 设置输出格式
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        // 写入文件
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(outputPath));
        transformer.transform(source, result);
    }
    
    /**
     * 统计容器元素数量
     */
    private int countContainerElements(CsdlEntityContainer container, List<String> exportedElements) {
        int count = 0;
        
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                exportedElements.add("EntitySet: " + entitySet.getName());
                count++;
            }
        }
        
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                exportedElements.add("Singleton: " + singleton.getName());
                count++;
            }
        }
        
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                exportedElements.add("ActionImport: " + actionImport.getName());
                count++;
            }
        }
        
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                exportedElements.add("FunctionImport: " + functionImport.getName());
                count++;
            }
        }
        
        return count;
    }

    /**
     * 导出Schema到文件 (为了向后兼容)
     */
    public void exportContainer(CsdlSchema schema, File outputFile) throws IOException {
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }
        
        // 使用容器导出的方法
        if (schema.getEntityContainer() != null) {
            exportContainer(schema.getEntityContainer(), 
                           outputFile.getAbsolutePath(), 
                           schema.getNamespace());
        } else {
            // 如果没有容器，创建一个空的
            CsdlEntityContainer emptyContainer = new CsdlEntityContainer().setName("DefaultContainer");
            schema.setEntityContainer(emptyContainer);
            exportContainer(emptyContainer, 
                           outputFile.getAbsolutePath(), 
                           schema.getNamespace());
        }
    }

    /**
     * 从XML文件加载容器
     */
    public CsdlSchema loadContainerFromXml(File xmlFile) throws IOException {
        if (xmlFile == null) {
            throw new IllegalArgumentException("XML file cannot be null");
        }
        if (!xmlFile.exists()) {
            throw new IOException("XML file does not exist: " + xmlFile.getAbsolutePath());
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            return parseSchemaFromDocument(document);
        } catch (Exception e) {
            throw new IOException("Failed to parse XML file: " + e.getMessage(), e);
        }
    }

    /**
     * 从XML文件导出到新的XML文件
     */
    public void exportFromXmlToXml(File inputFile, File outputFile) throws IOException {
        CsdlSchema schema = loadContainerFromXml(inputFile);
        exportContainer(schema, outputFile);
    }

    /**
     * 创建构建器
     */
    public DynamicContainerBuilder createBuilder() {
        return new org.apache.olingo.schema.processor.exporter.builder.DynamicContainerBuilder();
    }

    /**
     * 从XML文档解析Schema
     */
    private CsdlSchema parseSchemaFromDocument(Document document) throws Exception {
        CsdlSchema schema = new CsdlSchema();
        
        // 查找Schema元素
        NodeList schemaNodes = document.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "Schema");
        if (schemaNodes.getLength() == 0) {
            throw new Exception("No Schema element found in XML document");
        }
        
        Element schemaElement = (Element) schemaNodes.item(0);
        
        // 设置namespace
        String namespace = schemaElement.getAttribute("Namespace");
        if (namespace != null && !namespace.isEmpty()) {
            schema.setNamespace(namespace);
        }
        
        // 设置alias
        String alias = schemaElement.getAttribute("Alias");
        if (alias != null && !alias.isEmpty()) {
            schema.setAlias(alias);
        }
        
        // 解析EntityContainer
        NodeList containerNodes = schemaElement.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "EntityContainer");
        if (containerNodes.getLength() > 0) {
            Element containerElement = (Element) containerNodes.item(0);
            CsdlEntityContainer container = parseEntityContainer(containerElement);
            schema.setEntityContainer(container);
        }
        
        // 解析EntityTypes
        NodeList entityTypeNodes = schemaElement.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "EntityType");
        if (entityTypeNodes.getLength() > 0) {
            List<CsdlEntityType> entityTypes = new ArrayList<>();
            for (int i = 0; i < entityTypeNodes.getLength(); i++) {
                Element entityTypeElement = (Element) entityTypeNodes.item(i);
                CsdlEntityType entityType = parseEntityType(entityTypeElement);
                entityTypes.add(entityType);
            }
            schema.setEntityTypes(entityTypes);
        }
        
        return schema;
    }
    
    /**
     * 解析EntityContainer
     */
    private CsdlEntityContainer parseEntityContainer(Element containerElement) {
        CsdlEntityContainer container = new CsdlEntityContainer();
        
        String name = containerElement.getAttribute("Name");
        if (name != null && !name.isEmpty()) {
            container.setName(name);
        }
        
        // 解析EntitySets
        NodeList entitySetNodes = containerElement.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "EntitySet");
        if (entitySetNodes.getLength() > 0) {
            List<CsdlEntitySet> entitySets = new ArrayList<>();
            for (int i = 0; i < entitySetNodes.getLength(); i++) {
                Element entitySetElement = (Element) entitySetNodes.item(i);
                CsdlEntitySet entitySet = parseEntitySet(entitySetElement);
                entitySets.add(entitySet);
            }
            container.setEntitySets(entitySets);
        }
        
        return container;
    }
    
    /**
     * 解析EntitySet
     */
    private CsdlEntitySet parseEntitySet(Element entitySetElement) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        
        String name = entitySetElement.getAttribute("Name");
        if (name != null && !name.isEmpty()) {
            entitySet.setName(name);
        }
        
        String entityType = entitySetElement.getAttribute("EntityType");
        if (entityType != null && !entityType.isEmpty()) {
            entitySet.setType(entityType);
        }
        
        return entitySet;
    }
    
    /**
     * 解析EntityType
     */
    private CsdlEntityType parseEntityType(Element entityTypeElement) {
        CsdlEntityType entityType = new CsdlEntityType();
        
        String name = entityTypeElement.getAttribute("Name");
        if (name != null && !name.isEmpty()) {
            entityType.setName(name);
        }
        
        // 解析Properties
        NodeList propertyNodes = entityTypeElement.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "Property");
        if (propertyNodes.getLength() > 0) {
            List<CsdlProperty> properties = new ArrayList<>();
            for (int i = 0; i < propertyNodes.getLength(); i++) {
                Element propertyElement = (Element) propertyNodes.item(i);
                CsdlProperty property = parseProperty(propertyElement);
                properties.add(property);
            }
            entityType.setProperties(properties);
        }
        
        // 解析Key
        NodeList keyNodes = entityTypeElement.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "Key");
        if (keyNodes.getLength() > 0) {
            Element keyElement = (Element) keyNodes.item(0);
            NodeList propertyRefNodes = keyElement.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "PropertyRef");
            if (propertyRefNodes.getLength() > 0) {
                List<CsdlPropertyRef> keys = new ArrayList<>();
                for (int i = 0; i < propertyRefNodes.getLength(); i++) {
                    Element propertyRefElement = (Element) propertyRefNodes.item(i);
                    String propertyRefName = propertyRefElement.getAttribute("Name");
                    if (propertyRefName != null && !propertyRefName.isEmpty()) {
                        keys.add(new CsdlPropertyRef().setName(propertyRefName));
                    }
                }
                entityType.setKey(keys);
            }
        }
        
        return entityType;
    }
    
    /**
     * 解析Property
     */
    private CsdlProperty parseProperty(Element propertyElement) {
        CsdlProperty property = new CsdlProperty();
        
        String name = propertyElement.getAttribute("Name");
        if (name != null && !name.isEmpty()) {
            property.setName(name);
        }
        
        String type = propertyElement.getAttribute("Type");
        if (type != null && !type.isEmpty()) {
            property.setType(type);
        }
        
        String nullable = propertyElement.getAttribute("Nullable");
        if (nullable != null && !nullable.isEmpty()) {
            property.setNullable(Boolean.parseBoolean(nullable));
        }
        
        String maxLength = propertyElement.getAttribute("MaxLength");
        if (maxLength != null && !maxLength.isEmpty()) {
            try {
                property.setMaxLength(Integer.parseInt(maxLength));
            } catch (NumberFormatException e) {
                // 忽略无效的MaxLength值
            }
        }
        
        return property;
    }
}
