package org.apache.olingo.schema.processor.examples;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.validation.SchemaReferenceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简化的多层XML Schema聚合示例
 * 
 * 功能：
 * 1. 从多层文件夹递归读取XML Schema文件
 * 2. 按namespace聚合不同的Schema
 * 3. 构建包含所有依赖的完整XML
 * 4. 使用Olingo标准API处理OData Schema
 */
public class SimpleSchemaAggregatorExample {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleSchemaAggregatorExample.class);
    
    // Schema聚合器
    private final Map<String, CsdlSchema> schemasByNamespace = new HashMap<>();
    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private final Set<Path> processedFiles = new HashSet<>();
    
    /**
     * 获取资源路径
     */
    private Path getResourcePath(String resourceName) {
        try {
            return Paths.get(getClass().getClassLoader().getResource(resourceName).toURI());
        } catch (Exception e) {
            throw new RuntimeException("无法找到资源: " + resourceName, e);
        }
    }
    
    /**
     * 主要示例方法
     */
    public void demonstrateMultiLayerSchemaAggregation() {
        try {
            logger.info("开始多层XML Schema聚合示例...");
            
            // 第1步：创建示例XML文件结构
            createExampleXmlStructure();
            
            // 第2步：递归扫描并聚合所有Schema
            Path rootSchemaFolder = getResourcePath("examples/schemas");
            aggregateSchemasFromFolder(rootSchemaFolder);
            
            // 第3步：加载容器XML并分析依赖
            Path containerFile = getResourcePath("examples/containers/MainContainer.xml");
            CsdlEntityContainer container = loadContainer(containerFile);
            
            // 第4步：构建完整的聚合XML
            String aggregatedXml = buildAggregatedSchemaXml(container);
            
            // 第5步：输出结果到target目录
            Path outputPath = Paths.get("target/examples/output");
            Files.createDirectories(outputPath);
            saveAggregatedXml(aggregatedXml, outputPath.resolve("AggregatedSchema.xml").toString());
            
            // 第6步：展示依赖分析结果
            displaySchemaAnalysis();
            
            logger.info("多层XML Schema聚合示例完成！");
            
        } catch (Exception e) {
            logger.error("示例执行失败", e);
        }
    }
    
    /**
     * 递归扫描文件夹并聚合Schema
     */
    private void aggregateSchemasFromFolder(Path rootFolder) throws Exception {
        logger.info("开始扫描文件夹: " + rootFolder);
        
        if (!Files.exists(rootFolder)) {
            logger.warn("文件夹不存在: " + rootFolder);
            return;
        }
        
        // 递归遍历所有XML文件
        Files.walk(rootFolder)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
            .forEach(this::processSchemaFile);
        
        logger.info("完成文件夹扫描，共处理 " + processedFiles.size() + " 个Schema文件");
    }
    
    /**
     * 处理单个Schema文件
     */
    private void processSchemaFile(Path xmlFile) {
        if (processedFiles.contains(xmlFile)) {
            logger.debug("文件已处理，跳过: " + xmlFile);
            return;
        }
        
        try {
            logger.info("处理Schema文件: " + xmlFile);
            
            // 首先验证Schema的引用
            validateSchemaReferences(xmlFile);
            
            CsdlSchema schema = parseSchemaFromXml(xmlFile);
            if (schema != null) {
                String namespace = schema.getNamespace();
                
                if (schemasByNamespace.containsKey(namespace)) {
                    // 合并相同namespace的Schema
                    mergeSchemas(schemasByNamespace.get(namespace), schema);
                    logger.info("合并Schema到现有namespace: " + namespace);
                } else {
                    // 添加新的Schema
                    schemasByNamespace.put(namespace, schema);
                    logger.info("添加新Schema，namespace: " + namespace);
                }
                
                processedFiles.add(xmlFile);
            }
            
        } catch (Exception e) {
            logger.error("处理Schema文件失败: " + xmlFile, e);
        }
    }
    
    /**
     * 验证Schema引用
     */
    private void validateSchemaReferences(Path xmlFile) {
        try {
            SchemaReferenceValidator validator = new SchemaReferenceValidator();
            SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(xmlFile);
            
            if (!result.isValid()) {
                logger.warn("Schema文件 {} 存在引用问题:", xmlFile.getFileName());
                for (String error : result.getErrors()) {
                    logger.warn("  - {}", error);
                }
                
                if (!result.getMissingReferences().isEmpty()) {
                    logger.info("缺失的引用: {}", result.getMissingReferences());
                    logger.info("建议在文件开头添加相应的edmx:Reference声明");
                }
            } else {
                logger.debug("Schema文件 {} 引用验证通过", xmlFile.getFileName());
            }
            
        } catch (Exception e) {
            logger.error("验证Schema引用时发生错误: " + xmlFile, e);
        }
    }
    
    /**
     * 从XML解析Schema（简化版本）
     */
    private CsdlSchema parseSchemaFromXml(Path xmlFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(xmlFile.toFile())) {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(fis);
            
            CsdlSchema schema = new CsdlSchema();
            List<CsdlEntityType> entityTypes = new ArrayList<>();
            List<CsdlComplexType> complexTypes = new ArrayList<>();
            List<CsdlAction> actions = new ArrayList<>();
            List<CsdlFunction> functions = new ArrayList<>();
            
            while (reader.hasNext()) {
                int event = reader.next();
                
                if (event == XMLStreamReader.START_ELEMENT) {
                    String localName = reader.getLocalName();
                    
                    switch (localName) {
                        case "Schema":
                            String namespace = reader.getAttributeValue(null, "Namespace");
                            schema.setNamespace(namespace);
                            break;
                            
                        case "EntityType":
                            CsdlEntityType entityType = parseEntityType(reader);
                            if (entityType != null) {
                                entityTypes.add(entityType);
                            }
                            break;
                            
                        case "ComplexType":
                            CsdlComplexType complexType = parseComplexType(reader);
                            if (complexType != null) {
                                complexTypes.add(complexType);
                            }
                            break;
                            
                        case "Action":
                            CsdlAction action = parseAction(reader);
                            if (action != null) {
                                actions.add(action);
                            }
                            break;
                            
                        case "Function":
                            CsdlFunction function = parseFunction(reader);
                            if (function != null) {
                                functions.add(function);
                            }
                            break;
                    }
                }
            }
            
            schema.setEntityTypes(entityTypes);
            schema.setComplexTypes(complexTypes);
            schema.setActions(actions);
            schema.setFunctions(functions);
            
            reader.close();
            return schema;
        }
    }
    
    /**
     * 解析EntityType
     */
    private CsdlEntityType parseEntityType(XMLStreamReader reader) throws Exception {
        CsdlEntityType entityType = new CsdlEntityType();
        String name = reader.getAttributeValue(null, "Name");
        String baseType = reader.getAttributeValue(null, "BaseType");
        
        entityType.setName(name);
        if (baseType != null) {
            entityType.setBaseType(baseType);
        }
        
        List<CsdlProperty> properties = new ArrayList<>();
        
        while (reader.hasNext()) {
            int event = reader.next();
            
            if (event == XMLStreamReader.START_ELEMENT) {
                String localName = reader.getLocalName();
                
                if ("Property".equals(localName)) {
                    CsdlProperty property = parseProperty(reader);
                    if (property != null) {
                        properties.add(property);
                    }
                }
            } else if (event == XMLStreamReader.END_ELEMENT && 
                      "EntityType".equals(reader.getLocalName())) {
                break;
            }
        }
        
        entityType.setProperties(properties);
        return entityType;
    }
    
    /**
     * 解析ComplexType
     */
    private CsdlComplexType parseComplexType(XMLStreamReader reader) throws Exception {
        CsdlComplexType complexType = new CsdlComplexType();
        String name = reader.getAttributeValue(null, "Name");
        
        complexType.setName(name);
        
        List<CsdlProperty> properties = new ArrayList<>();
        
        while (reader.hasNext()) {
            int event = reader.next();
            
            if (event == XMLStreamReader.START_ELEMENT) {
                String localName = reader.getLocalName();
                
                if ("Property".equals(localName)) {
                    CsdlProperty property = parseProperty(reader);
                    if (property != null) {
                        properties.add(property);
                    }
                }
            } else if (event == XMLStreamReader.END_ELEMENT && 
                      "ComplexType".equals(reader.getLocalName())) {
                break;
            }
        }
        
        complexType.setProperties(properties);
        return complexType;
    }
    
    /**
     * 解析Property
     */
    private CsdlProperty parseProperty(XMLStreamReader reader) throws Exception {
        CsdlProperty property = new CsdlProperty();
        
        String name = reader.getAttributeValue(null, "Name");
        String type = reader.getAttributeValue(null, "Type");
        String nullable = reader.getAttributeValue(null, "Nullable");
        String maxLength = reader.getAttributeValue(null, "MaxLength");
        
        property.setName(name);
        property.setType(type);
        
        if (nullable != null) {
            property.setNullable(Boolean.parseBoolean(nullable));
        }
        
        if (maxLength != null) {
            property.setMaxLength(Integer.valueOf(maxLength));
        }
        
        return property;
    }
    
    /**
     * 解析Action
     */
    private CsdlAction parseAction(XMLStreamReader reader) throws Exception {
        CsdlAction action = new CsdlAction();
        String name = reader.getAttributeValue(null, "Name");
        action.setName(name);
        return action;
    }
    
    /**
     * 解析Function
     */
    private CsdlFunction parseFunction(XMLStreamReader reader) throws Exception {
        CsdlFunction function = new CsdlFunction();
        String name = reader.getAttributeValue(null, "Name");
        function.setName(name);
        return function;
    }
    
    /**
     * 合并两个Schema
     */
    private void mergeSchemas(CsdlSchema target, CsdlSchema source) {
        // 合并EntityTypes
        List<CsdlEntityType> targetEntityTypes = new ArrayList<>(target.getEntityTypes());
        for (CsdlEntityType sourceEntityType : source.getEntityTypes()) {
            if (!containsEntityType(targetEntityTypes, sourceEntityType.getName())) {
                targetEntityTypes.add(sourceEntityType);
            }
        }
        target.setEntityTypes(targetEntityTypes);
        
        // 合并ComplexTypes
        List<CsdlComplexType> targetComplexTypes = new ArrayList<>(target.getComplexTypes());
        for (CsdlComplexType sourceComplexType : source.getComplexTypes()) {
            if (!containsComplexType(targetComplexTypes, sourceComplexType.getName())) {
                targetComplexTypes.add(sourceComplexType);
            }
        }
        target.setComplexTypes(targetComplexTypes);
        
        // 合并Actions
        List<CsdlAction> targetActions = new ArrayList<>(target.getActions());
        for (CsdlAction sourceAction : source.getActions()) {
            if (!containsAction(targetActions, sourceAction.getName())) {
                targetActions.add(sourceAction);
            }
        }
        target.setActions(targetActions);
        
        // 合并Functions
        List<CsdlFunction> targetFunctions = new ArrayList<>(target.getFunctions());
        for (CsdlFunction sourceFunction : source.getFunctions()) {
            if (!containsFunction(targetFunctions, sourceFunction.getName())) {
                targetFunctions.add(sourceFunction);
            }
        }
        target.setFunctions(targetFunctions);
    }
    
    private boolean containsEntityType(List<CsdlEntityType> entityTypes, String name) {
        return entityTypes.stream().anyMatch(et -> name.equals(et.getName()));
    }
    
    private boolean containsComplexType(List<CsdlComplexType> complexTypes, String name) {
        return complexTypes.stream().anyMatch(ct -> name.equals(ct.getName()));
    }
    
    private boolean containsAction(List<CsdlAction> actions, String name) {
        return actions.stream().anyMatch(a -> name.equals(a.getName()));
    }
    
    private boolean containsFunction(List<CsdlFunction> functions, String name) {
        return functions.stream().anyMatch(f -> name.equals(f.getName()));
    }
    
    /**
     * 加载容器文件
     */
    private CsdlEntityContainer loadContainer(Path containerFile) throws Exception {
        logger.info("加载容器文件: " + containerFile);
        
        if (!Files.exists(containerFile)) {
            logger.warn("容器文件不存在，创建示例文件");
            createExampleContainerFile(containerFile);
        }
        
        // 这里简化处理，返回一个基本的容器
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName("MainContainer");
        
        return container;
    }
    
    /**
     * 构建聚合的Schema XML
     */
    private String buildAggregatedSchemaXml(CsdlEntityContainer container) {
        logger.info("构建聚合Schema XML...");
        logger.info("容器名称: " + container.getName());
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n");
        xml.append("  <edmx:DataServices>\n");
        
        // 添加所有Schema
        for (Map.Entry<String, CsdlSchema> entry : schemasByNamespace.entrySet()) {
            String namespace = entry.getKey();
            CsdlSchema schema = entry.getValue();
            
            xml.append("    <Schema Namespace=\"").append(namespace).append("\">\n");
            
            // 添加EntityTypes
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                xml.append("      <EntityType Name=\"").append(entityType.getName()).append("\"");
                if (entityType.getBaseType() != null) {
                    xml.append(" BaseType=\"").append(entityType.getBaseType()).append("\"");
                }
                xml.append(">\n");
                
                // 添加Properties
                for (CsdlProperty property : entityType.getProperties()) {
                    xml.append("        <Property Name=\"").append(property.getName())
                       .append("\" Type=\"").append(property.getType()).append("\"");
                    
                    if (!property.isNullable()) {
                        xml.append(" Nullable=\"false\"");
                    }
                    
                    if (property.getMaxLength() != null) {
                        xml.append(" MaxLength=\"").append(property.getMaxLength()).append("\"");
                    }
                    
                    xml.append("/>\n");
                }
                
                xml.append("      </EntityType>\n");
            }
            
            // 添加ComplexTypes
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                xml.append("      <ComplexType Name=\"").append(complexType.getName()).append("\">\n");
                
                for (CsdlProperty property : complexType.getProperties()) {
                    xml.append("        <Property Name=\"").append(property.getName())
                       .append("\" Type=\"").append(property.getType()).append("\"");
                    
                    if (!property.isNullable()) {
                        xml.append(" Nullable=\"false\"");
                    }
                    
                    if (property.getMaxLength() != null) {
                        xml.append(" MaxLength=\"").append(property.getMaxLength()).append("\"");
                    }
                    
                    xml.append("/>\n");
                }
                
                xml.append("      </ComplexType>\n");
            }
            
            xml.append("    </Schema>\n");
        }
        
        xml.append("  </edmx:DataServices>\n");
        xml.append("</edmx:Edmx>");
        
        return xml.toString();
    }
    
    /**
     * 保存聚合的XML
     */
    private void saveAggregatedXml(String xml, String outputPath) throws Exception {
        Path outputFile = Paths.get(outputPath);
        Files.createDirectories(outputFile.getParent());
        
        try (FileWriter writer = new FileWriter(outputFile.toFile())) {
            writer.write(xml);
        }
        
        logger.info("聚合Schema已保存到: " + outputPath);
    }
    
    /**
     * 显示Schema分析结果
     */
    private void displaySchemaAnalysis() {
        logger.info("=== Schema分析结果 ===");
        logger.info("总共聚合的namespace数量: " + schemasByNamespace.size());
        
        for (Map.Entry<String, CsdlSchema> entry : schemasByNamespace.entrySet()) {
            String namespace = entry.getKey();
            CsdlSchema schema = entry.getValue();
            
            logger.info("Namespace: " + namespace);
            logger.info("  EntityTypes数量: " + schema.getEntityTypes().size());
            logger.info("  ComplexTypes数量: " + schema.getComplexTypes().size());
            logger.info("  Actions数量: " + schema.getActions().size());
            logger.info("  Functions数量: " + schema.getFunctions().size());
        }
        
        logger.info("  处理的文件数量: " + processedFiles.size());
    }
    
    /**
     * 创建示例XML文件结构
     */
    private void createExampleXmlStructure() throws Exception {
        logger.info("创建示例XML文件结构...");
        
        // 创建目录结构
        Files.createDirectories(Paths.get("target/examples/schemas/core"));
        Files.createDirectories(Paths.get("target/examples/schemas/business"));
        Files.createDirectories(Paths.get("target/examples/schemas/common"));
        Files.createDirectories(Paths.get("target/examples/containers"));
        Files.createDirectories(Paths.get("target/examples/output"));
        
        // 创建示例Schema文件
        createCoreSchema();
        createBusinessSchema();
        createCommonSchema();
        
        logger.info("示例XML文件结构创建完成");
    }
    
    /**
     * 创建Core Schema示例
     */
    private void createCoreSchema() throws Exception {
        String coreSchema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Core.Types\">\n" +
            "      <EntityType Name=\"BaseEntity\">\n" +
            "        <Property Name=\"Id\" Type=\"Edm.Guid\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"CreatedAt\" Type=\"Edm.DateTimeOffset\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"UpdatedAt\" Type=\"Edm.DateTimeOffset\" Nullable=\"true\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <ComplexType Name=\"Address\">\n" +
            "        <Property Name=\"Street\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "        <Property Name=\"City\" Type=\"Edm.String\" MaxLength=\"50\"/>\n" +
            "        <Property Name=\"Country\" Type=\"Edm.String\" MaxLength=\"50\"/>\n" +
            "        <Property Name=\"PostalCode\" Type=\"Edm.String\" MaxLength=\"20\"/>\n" +
            "      </ComplexType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Files.write(Paths.get("target/examples/schemas/core/CoreTypes.xml"), coreSchema.getBytes());
    }
    
    /**
     * 创建Business Schema示例
     */
    private void createBusinessSchema() throws Exception {
        String businessSchema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Business.Entities\">\n" +
            "      <EntityType Name=\"Customer\" BaseType=\"Core.Types.BaseEntity\">\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"CustomerCode\" Type=\"Edm.String\" MaxLength=\"20\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityType Name=\"Order\" BaseType=\"Core.Types.BaseEntity\">\n" +
            "        <Property Name=\"OrderNumber\" Type=\"Edm.String\" MaxLength=\"50\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"TotalAmount\" Type=\"Edm.Decimal\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Files.write(Paths.get("target/examples/schemas/business/BusinessEntities.xml"), businessSchema.getBytes());
    }
    
    /**
     * 创建Common Schema示例
     */
    private void createCommonSchema() throws Exception {
        String commonSchema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Common.Products\">\n" +
            "      <EntityType Name=\"Product\" BaseType=\"Core.Types.BaseEntity\">\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Price\" Type=\"Edm.Decimal\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Files.write(Paths.get("target/examples/schemas/common/Products.xml"), commonSchema.getBytes());
    }
    
    /**
     * 创建示例容器文件
     */
    private void createExampleContainerFile(Path containerFile) throws Exception {
        String containerXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"Main.Container\">\n" +
            "      <EntityContainer Name=\"MainContainer\">\n" +
            "        <EntitySet Name=\"Customers\" EntityType=\"Business.Entities.Customer\"/>\n" +
            "        <EntitySet Name=\"Orders\" EntityType=\"Business.Entities.Order\"/>\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"Common.Products.Product\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Files.createDirectories(containerFile.getParent());
        Files.write(containerFile, containerXml.getBytes());
    }
    
    /**
     * 主方法 - 运行示例
     */
    public static void main(String[] args) {
        SimpleSchemaAggregatorExample example = new SimpleSchemaAggregatorExample();
        example.demonstrateMultiLayerSchemaAggregation();
    }
}
