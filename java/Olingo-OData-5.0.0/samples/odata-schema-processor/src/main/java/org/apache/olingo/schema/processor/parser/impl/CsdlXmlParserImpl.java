package org.apache.olingo.schema.processor.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlAction;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlAnnotation;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlComplexType;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlEntityContainer;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlEntityType;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlEnumType;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlFunction;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlTerm;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlTypeDefinition;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重构的CSDL XML解析器实现
 * 使用Olingo原生解析方法和扩展模型类
 * 
 * 重构前问题：
 * - 使用字符串搜索手动解析XML
 * - 没有利用Olingo的原生解析能力
 * - 错误处理不够健壮
 * 
 * 重构后优势：
 * - 使用Olingo原生MetadataParser
 * - 使用扩展模型类提供统一结构
 * - 更好的错误处理和验证
 * - 支持完整的CSDL规范
 */
public class CsdlXmlParserImpl implements ODataXmlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(CsdlXmlParserImpl.class);
    
    private final MetadataParser metadataParser;
    
    /**
     * 构造函数 - 初始化Olingo原生解析器
     */
    public CsdlXmlParserImpl() {
        this.metadataParser = new MetadataParser();
        
        // 配置MetadataParser
        this.metadataParser.useLocalCoreVocabularies(true);
        this.metadataParser.recursivelyLoadReferences(false); // 我们手动处理引用
    }
    
    @Override
    public ParseResult parseSchemas(InputStream inputStream, String sourceName) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<CsdlSchema> schemas = new ArrayList<>();
        
        try {
            logger.debug("Parsing XML from source using Olingo native parser: {}", sourceName);
            
            // 使用Olingo原生解析器解析
            List<CsdlSchema> parsedSchemas = parseWithOlingoNative(inputStream, sourceName);
            
            if (parsedSchemas.isEmpty()) {
                warnings.add("No valid schemas found in " + sourceName);
            } else {
                // 转换为扩展模型并添加到结果
                for (CsdlSchema schema : parsedSchemas) {
                    try {
                        CsdlSchema extendedSchema = convertToExtendedSchema(schema);
                        schemas.add(extendedSchema);
                        logger.debug("Successfully converted schema: {}", schema.getNamespace());
                    } catch (Exception e) {
                        errors.add("Failed to convert schema '" + schema.getNamespace() + "': " + e.getMessage());
                        logger.warn("Schema conversion failed for {}", schema.getNamespace(), e);
                    }
                }
            }
            
        } catch (Exception e) {
            errors.add("Failed to parse XML from " + sourceName + ": " + e.getMessage());
            logger.error("XML parsing error for {}", sourceName, e);
        }
        
        return new ParseResult(errors.isEmpty(), schemas, errors, warnings, sourceName);
    }
    
    @Override
    public ParseResult parseSchemas(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                List<String> errors = new ArrayList<>();
                errors.add("File does not exist: " + filePath);
                return new ParseResult(false, new ArrayList<>(), errors, new ArrayList<>(), filePath.toString());
            }
            
            logger.debug("Reading file: {}", filePath);
            
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                return parseSchemas(inputStream, filePath.toString());
            }
            
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Failed to read or parse file " + filePath + ": " + e.getMessage());
            logger.error("File parsing error for {}", filePath, e);
            return new ParseResult(false, new ArrayList<>(), errors, new ArrayList<>(), filePath.toString());
        }
    }
    
    @Override
    public ParseResult parseSchemas(String xmlContent, String sourceName) {
        try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"))) {
            return parseSchemas(inputStream, sourceName);
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Failed to parse XML content from " + sourceName + ": " + e.getMessage());
            return new ParseResult(false, new ArrayList<>(), errors, new ArrayList<>(), sourceName);
        }
    }
    
    /**
     * 从resources目录解析XML文件
     * 
     * @param resourcePath 资源路径（例如："/test-xml/valid-multiple-types.xml"）
     * @return 解析结果
     */
    public ParseResult parseFromResource(String resourcePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                List<String> errors = new ArrayList<>();
                errors.add("Resource not found: " + resourcePath);
                return new ParseResult(false, new ArrayList<>(), errors, new ArrayList<>(), resourcePath);
            }
            return parseSchemas(inputStream, resourcePath);
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Failed to parse resource " + resourcePath + ": " + e.getMessage());
            return new ParseResult(false, new ArrayList<>(), errors, new ArrayList<>(), resourcePath);
        }
    }
    
    @Override
    public ValidationResult validateXmlFormat(String xmlContent) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            if (xmlContent == null || xmlContent.trim().isEmpty()) {
                errors.add("XML content is null or empty");
                return new ValidationResult(false, errors, warnings);
            }
            
            if (!xmlContent.contains("<?xml")) {
                warnings.add("Missing XML declaration");
            }
            
            if (!xmlContent.contains("<edmx:Edmx") && !xmlContent.contains("<Schema")) {
                errors.add("Missing edmx:Edmx root element or Schema element");
            }
            
            // 使用Olingo原生解析器进行更严格的验证
            try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"))) {
                parseWithOlingoNative(inputStream, "validation");
            } catch (Exception e) {
                errors.add("XML parsing validation failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            errors.add("XML validation error: " + e.getMessage());
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    
    /**
     * 使用Olingo原生解析器解析XML
     */
    protected List<CsdlSchema> parseWithOlingoNative(InputStream inputStream, String sourceName) throws Exception {
        try {
            // 使用Olingo的MetadataParser来解析XML
            MetadataParser parser = new MetadataParser();
            
            // 配置解析器以支持annotations和扩展元素
            parser.parseAnnotations(true);
            parser.useLocalCoreVocabularies(true);
            parser.implicitlyLoadCoreVocabularies(true);
            parser.recursivelyLoadReferences(false); // 我们单独处理引用
            
            // 创建InputStreamReader确保正确的字符编码
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            
            // 使用Olingo解析器构建EdmProvider
            SchemaBasedEdmProvider provider = parser.buildEdmProvider(reader);
            
            // 从EdmProvider中提取Schemas
            List<CsdlSchema> schemas = new ArrayList<>();
            for (CsdlSchema schema : provider.getSchemas()) {
                // 创建扩展Schema并复制内容
                CsdlSchema extendedSchema = createExtendedSchema(schema);
                schemas.add(extendedSchema);
            }
            
            logger.debug("Successfully parsed {} schemas using Olingo native parser from {}", 
                        schemas.size(), sourceName);
            
            return schemas;
            
        } catch (Exception e) {
            logger.error("Failed to parse with Olingo native parser from {}: {}", sourceName, e.getMessage());
            throw new RuntimeException("Olingo native parsing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建扩展Schema，包含所有扩展模型元素
     */
    private CsdlSchema createExtendedSchema(CsdlSchema originalSchema) {
        // 创建新的Schema实例
        CsdlSchema extendedSchema = new CsdlSchema();
        
        // 复制基本属性
        extendedSchema.setNamespace(originalSchema.getNamespace());
        extendedSchema.setAlias(originalSchema.getAlias());
        
        // 转换EntityTypes为扩展模型
        if (originalSchema.getEntityTypes() != null) {
            List<CsdlEntityType> extendedEntityTypes = new ArrayList<>();
            for (CsdlEntityType entityType : originalSchema.getEntityTypes()) {
                ExtendedCsdlEntityType extendedEntityType = createExtendedEntityType(entityType);
                extendedEntityType.setNamespace(originalSchema.getNamespace()); // 设置namespace
                extendedEntityTypes.add(extendedEntityType);
            }
            extendedSchema.setEntityTypes(extendedEntityTypes);
        }
        
        // 转换ComplexTypes为扩展模型
        if (originalSchema.getComplexTypes() != null) {
            List<CsdlComplexType> extendedComplexTypes = new ArrayList<>();
            for (CsdlComplexType complexType : originalSchema.getComplexTypes()) {
                ExtendedCsdlComplexType extendedComplexType = createExtendedComplexType(complexType);
                extendedComplexType.setNamespace(originalSchema.getNamespace()); // 设置namespace
                extendedComplexTypes.add(extendedComplexType);
            }
            extendedSchema.setComplexTypes(extendedComplexTypes);
        }
        
        // 转换EntityContainer为扩展模型
        if (originalSchema.getEntityContainer() != null) {
            ExtendedCsdlEntityContainer extendedEntityContainer = createExtendedEntityContainer(originalSchema.getEntityContainer());
            extendedSchema.setEntityContainer(extendedEntityContainer);
        }
        
        // 转换EnumTypes为扩展模型
        if (originalSchema.getEnumTypes() != null) {
            List<CsdlEnumType> extendedEnumTypes = new ArrayList<>();
            for (CsdlEnumType enumType : originalSchema.getEnumTypes()) {
                ExtendedCsdlEnumType extendedEnumType = createExtendedEnumType(enumType);
                extendedEnumTypes.add(extendedEnumType);
            }
            extendedSchema.setEnumTypes(extendedEnumTypes);
        }
        
        // 转换TypeDefinitions为扩展模型
        if (originalSchema.getTypeDefinitions() != null) {
            List<org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition> extendedTypeDefinitions = new ArrayList<>();
            for (org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition typeDef : originalSchema.getTypeDefinitions()) {
                ExtendedCsdlTypeDefinition extendedTypeDef = createExtendedTypeDefinition(typeDef);
                extendedTypeDefinitions.add(extendedTypeDef);
            }
            extendedSchema.setTypeDefinitions(extendedTypeDefinitions);
        }
        
        // 复制Actions和Functions（如果有）
        if (originalSchema.getActions() != null) {
            extendedSchema.setActions(new ArrayList<>(originalSchema.getActions()));
        }
        
        if (originalSchema.getFunctions() != null) {
            extendedSchema.setFunctions(new ArrayList<>(originalSchema.getFunctions()));
        }
        
        // 转换Terms为扩展模型
        if (originalSchema.getTerms() != null) {
            List<CsdlTerm> extendedTerms = new ArrayList<>();
            for (CsdlTerm term : originalSchema.getTerms()) {
                ExtendedCsdlTerm extendedTerm = createExtendedTerm(term);
                extendedTerms.add(extendedTerm);
            }
            extendedSchema.setTerms(extendedTerms);
        }
        
        // 转换Annotations为扩展模型
        if (originalSchema.getAnnotations() != null) {
            List<CsdlAnnotation> extendedAnnotations = new ArrayList<>();
            for (CsdlAnnotation annotation : originalSchema.getAnnotations()) {
                ExtendedCsdlAnnotation extendedAnnotation = createExtendedAnnotation(annotation);
                extendedAnnotations.add(extendedAnnotation);
            }
            extendedSchema.setAnnotations(extendedAnnotations);
        }
        
        logger.debug("Created extended schema for namespace: {}", originalSchema.getNamespace());
        
        return extendedSchema;
    }
    
    /**
     * 创建扩展EntityType
     */
    private ExtendedCsdlEntityType createExtendedEntityType(CsdlEntityType originalEntityType) {
        ExtendedCsdlEntityType extended = new ExtendedCsdlEntityType();
        
        // 复制基本属性
        extended.setName(originalEntityType.getName());
        extended.setAbstract(originalEntityType.isAbstract());
        extended.setOpenType(originalEntityType.isOpenType());
        
        // 安全地设置BaseType
        if (originalEntityType.getBaseType() != null) {
            extended.setBaseType(originalEntityType.getBaseType());
        }
        
        // 复制属性
        if (originalEntityType.getProperties() != null) {
            extended.setProperties(new ArrayList<>(originalEntityType.getProperties()));
        }
        
        // 复制导航属性
        if (originalEntityType.getNavigationProperties() != null) {
            extended.setNavigationProperties(new ArrayList<>(originalEntityType.getNavigationProperties()));
        }
        
        // 复制键
        if (originalEntityType.getKey() != null) {
            extended.setKey(originalEntityType.getKey());
        }
        
        // 复制注解
        if (originalEntityType.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(originalEntityType.getAnnotations()));
        }
        
        // 注册扩展元素
        extended.registerElement();
        
        logger.debug("Created extended entity type: {}", originalEntityType.getName());
        
        return extended;
    }
    
    /**
     * 创建扩展ComplexType
     */
    private ExtendedCsdlComplexType createExtendedComplexType(CsdlComplexType originalComplexType) {
        ExtendedCsdlComplexType extended = new ExtendedCsdlComplexType();
        
        // 复制基本属性
        extended.setName(originalComplexType.getName());
        extended.setAbstract(originalComplexType.isAbstract());
        extended.setOpenType(originalComplexType.isOpenType());
        if (originalComplexType.getBaseType() != null) {
            extended.setBaseType(originalComplexType.getBaseType());
        }
        
        // 复制属性
        if (originalComplexType.getProperties() != null) {
            extended.setProperties(new ArrayList<>(originalComplexType.getProperties()));
        }
        
        // 复制导航属性
        if (originalComplexType.getNavigationProperties() != null) {
            extended.setNavigationProperties(new ArrayList<>(originalComplexType.getNavigationProperties()));
        }
        
        // 复制注解
        if (originalComplexType.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(originalComplexType.getAnnotations()));
        }
        
        // 注册扩展元素
        extended.registerElement();
        
        logger.debug("Created extended complex type: {}", originalComplexType.getName());
        
        return extended;
    }
    
    /**
     * 创建扩展TypeDefinition
     */
    private ExtendedCsdlTypeDefinition createExtendedTypeDefinition(org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition originalTypeDef) {
        ExtendedCsdlTypeDefinition extended = new ExtendedCsdlTypeDefinition();
        
        // 复制基本属性
        extended.setName(originalTypeDef.getName());
        if (originalTypeDef.getUnderlyingType() != null) {
            extended.setUnderlyingType(originalTypeDef.getUnderlyingType());
        }
        extended.setMaxLength(originalTypeDef.getMaxLength());
        extended.setPrecision(originalTypeDef.getPrecision());
        extended.setScale(originalTypeDef.getScale());
        extended.setSrid(originalTypeDef.getSrid());
        extended.setUnicode(originalTypeDef.isUnicode());
        
        // 复制注解
        if (originalTypeDef.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(originalTypeDef.getAnnotations()));
        }
        
        logger.debug("Created extended type definition: {}", originalTypeDef.getName());
        
        return extended;
    }
    
    /**
     * 创建扩展EntityContainer
     */
    private ExtendedCsdlEntityContainer createExtendedEntityContainer(CsdlEntityContainer originalEntityContainer) {
        ExtendedCsdlEntityContainer extended = new ExtendedCsdlEntityContainer();
        
        // 复制基本属性
        extended.setName(originalEntityContainer.getName());
        // EntityContainer 没有 extends 属性
        
        // 复制EntitySets
        if (originalEntityContainer.getEntitySets() != null) {
            extended.setEntitySets(new ArrayList<>(originalEntityContainer.getEntitySets()));
        }
        
        // 复制Singletons
        if (originalEntityContainer.getSingletons() != null) {
            extended.setSingletons(new ArrayList<>(originalEntityContainer.getSingletons()));
        }
        
        // 复制ActionImports
        if (originalEntityContainer.getActionImports() != null) {
            extended.setActionImports(new ArrayList<>(originalEntityContainer.getActionImports()));
        }
        
        // 复制FunctionImports
        if (originalEntityContainer.getFunctionImports() != null) {
            extended.setFunctionImports(new ArrayList<>(originalEntityContainer.getFunctionImports()));
        }
        
        // 复制注解
        if (originalEntityContainer.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(originalEntityContainer.getAnnotations()));
        }
        
        // 注册扩展元素
        extended.registerElement();
        
        logger.debug("Created extended entity container: {}", originalEntityContainer.getName());
        
        return extended;
    }
    
    /**
     * 创建扩展EnumType
     */
    private ExtendedCsdlEnumType createExtendedEnumType(CsdlEnumType originalEnumType) {
        ExtendedCsdlEnumType extended = new ExtendedCsdlEnumType();
        
        // 复制基本属性
        extended.setName(originalEnumType.getName());
        if (originalEnumType.getUnderlyingType() != null) {
            extended.setUnderlyingType(originalEnumType.getUnderlyingType());
        }
        extended.setFlags(originalEnumType.isFlags());
        
        // 复制成员
        if (originalEnumType.getMembers() != null) {
            extended.setMembers(new ArrayList<>(originalEnumType.getMembers()));
        }
        
        // 复制注解
        if (originalEnumType.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(originalEnumType.getAnnotations()));
        }
        
        // 注册扩展元素
        extended.registerElement();
        
        logger.debug("Created extended enum type: {}", originalEnumType.getName());
        
        return extended;
    }
    
    /**
     * 创建扩展Term
     */
    private ExtendedCsdlTerm createExtendedTerm(CsdlTerm originalTerm) {
        ExtendedCsdlTerm extended = new ExtendedCsdlTerm();
        
        // 复制基本属性
        extended.setName(originalTerm.getName());
        if (originalTerm.getType() != null) {
            extended.setType(originalTerm.getType());
        }
        extended.setBaseTerm(originalTerm.getBaseTerm());
        extended.setMaxLength(originalTerm.getMaxLength());
        extended.setPrecision(originalTerm.getPrecision());
        extended.setScale(originalTerm.getScale());
        extended.setAppliesTo(originalTerm.getAppliesTo());
        extended.setDefaultValue(originalTerm.getDefaultValue());
        
        // 复制注解
        if (originalTerm.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(originalTerm.getAnnotations()));
        }
        
        // 注册扩展元素
        extended.registerElement();
        
        logger.debug("Created extended term: {}", originalTerm.getName());
        
        return extended;
    }
    
    /**
     * 创建扩展Annotation
     */
    private ExtendedCsdlAnnotation createExtendedAnnotation(CsdlAnnotation originalAnnotation) {
        ExtendedCsdlAnnotation extended = new ExtendedCsdlAnnotation();
        
        // 复制基本属性
        extended.setTerm(originalAnnotation.getTerm());
        extended.setQualifier(originalAnnotation.getQualifier());
        extended.setExpression(originalAnnotation.getExpression());
        
        // 复制注解（如果Annotation支持嵌套注解）
        if (originalAnnotation.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(originalAnnotation.getAnnotations()));
        }
        
        // 注册扩展元素
        extended.registerElement();
        
        logger.debug("Created extended annotation for term: {}", originalAnnotation.getTerm());
        
        return extended;
    }
    
    /**
     * 转换为扩展Schema模型
     */
    private CsdlSchema convertToExtendedSchema(CsdlSchema baseSchema) {
        // 目前直接返回基础Schema，因为扩展模型主要用于特定类型
        // 如果需要Schema级别的扩展，可以创建ExtendedCsdlSchema类
        logger.debug("Converting schema: {}", baseSchema.getNamespace());
        
        // 如果有EntityTypes，转换为扩展模型
        if (baseSchema.getEntityTypes() != null && !baseSchema.getEntityTypes().isEmpty()) {
            List<CsdlEntityType> extendedEntityTypes = baseSchema.getEntityTypes().stream()
                    .map(this::convertToExtendedEntityType)
                    .collect(Collectors.toList());
            baseSchema.setEntityTypes(extendedEntityTypes);
        }
        
        // 如果有ComplexTypes，转换为扩展模型
        if (baseSchema.getComplexTypes() != null && !baseSchema.getComplexTypes().isEmpty()) {
            List<CsdlComplexType> extendedComplexTypes = baseSchema.getComplexTypes().stream()
                    .map(this::convertToExtendedComplexType)
                    .collect(Collectors.toList());
            baseSchema.setComplexTypes(extendedComplexTypes);
        }
        
        // 转换其他类型
        convertActions(baseSchema);
        convertFunctions(baseSchema);
        convertEntityContainer(baseSchema);
        
        return baseSchema;
    }
    
    /**
     * 转换为扩展EntityType模型
     */
    private CsdlEntityType convertToExtendedEntityType(CsdlEntityType baseEntityType) {
        ExtendedCsdlEntityType extendedEntityType = new ExtendedCsdlEntityType();
        
        // 复制基础属性
        extendedEntityType.setName(baseEntityType.getName());
        
        // 只有当BaseType不为null时才设置
        if (baseEntityType.getBaseType() != null) {
            extendedEntityType.setBaseType(baseEntityType.getBaseType());
        }
        
        extendedEntityType.setAbstract(baseEntityType.isAbstract());
        extendedEntityType.setOpenType(baseEntityType.isOpenType());
        extendedEntityType.setHasStream(baseEntityType.hasStream());
        
        // 复制属性
        if (baseEntityType.getProperties() != null) {
            extendedEntityType.setProperties(new ArrayList<>(baseEntityType.getProperties()));
        }
        
        // 复制Key
        if (baseEntityType.getKey() != null) {
            extendedEntityType.setKey(baseEntityType.getKey());
        }
        
        // 复制导航属性
        if (baseEntityType.getNavigationProperties() != null) {
            extendedEntityType.setNavigationProperties(new ArrayList<>(baseEntityType.getNavigationProperties()));
        }
        
        // 复制Annotations
        if (baseEntityType.getAnnotations() != null) {
            extendedEntityType.setAnnotations(new ArrayList<>(baseEntityType.getAnnotations()));
        }
        
        logger.debug("Converted EntityType: {}", extendedEntityType.getName());
        return extendedEntityType;
    }
    
    /**
     * 转换为扩展ComplexType模型
     */
    private CsdlComplexType convertToExtendedComplexType(CsdlComplexType baseComplexType) {
        ExtendedCsdlComplexType extendedComplexType = new ExtendedCsdlComplexType();
        
        // 复制基础属性
        extendedComplexType.setName(baseComplexType.getName());
        
        // 只有当BaseType不为null时才设置
        if (baseComplexType.getBaseType() != null) {
            extendedComplexType.setBaseType(baseComplexType.getBaseType());
        }
        
        extendedComplexType.setAbstract(baseComplexType.isAbstract());
        extendedComplexType.setOpenType(baseComplexType.isOpenType());
        
        // 复制属性
        if (baseComplexType.getProperties() != null) {
            extendedComplexType.setProperties(new ArrayList<>(baseComplexType.getProperties()));
        }
        
        // 复制导航属性
        if (baseComplexType.getNavigationProperties() != null) {
            extendedComplexType.setNavigationProperties(new ArrayList<>(baseComplexType.getNavigationProperties()));
        }
        
        // 复制Annotations
        if (baseComplexType.getAnnotations() != null) {
            extendedComplexType.setAnnotations(new ArrayList<>(baseComplexType.getAnnotations()));
        }
        
        logger.debug("Converted ComplexType: {}", extendedComplexType.getName());
        return extendedComplexType;
    }
    
    /**
     * 转换Actions
     */
    private void convertActions(CsdlSchema schema) {
        if (schema.getActions() != null && !schema.getActions().isEmpty()) {
            List<CsdlAction> extendedActions = schema.getActions().stream()
                    .map(this::convertToExtendedAction)
                    .collect(Collectors.toList());
            schema.setActions(extendedActions);
        }
    }
    
    /**
     * 转换Functions
     */
    private void convertFunctions(CsdlSchema schema) {
        if (schema.getFunctions() != null && !schema.getFunctions().isEmpty()) {
            List<CsdlFunction> extendedFunctions = schema.getFunctions().stream()
                    .map(this::convertToExtendedFunction)
                    .collect(Collectors.toList());
            schema.setFunctions(extendedFunctions);
        }
    }
    
    /**
     * 转换EntityContainer
     */
    private void convertEntityContainer(CsdlSchema schema) {
        if (schema.getEntityContainer() != null) {
            // EntityContainer通常不需要扩展，保持原样
            logger.debug("EntityContainer preserved: {}", schema.getEntityContainer().getName());
        }
    }
    
    private CsdlAction convertToExtendedAction(CsdlAction baseAction) {
        ExtendedCsdlAction extendedAction = new ExtendedCsdlAction();
        
        // 复制基础属性
        extendedAction.setName(baseAction.getName());
        extendedAction.setBound(baseAction.isBound());
        extendedAction.setEntitySetPath(baseAction.getEntitySetPath());
        
        // 复制参数
        if (baseAction.getParameters() != null) {
            extendedAction.setParameters(new ArrayList<>(baseAction.getParameters()));
        }
        
        // 复制返回类型
        if (baseAction.getReturnType() != null) {
            extendedAction.setReturnType(baseAction.getReturnType());
        }
        
        // 复制Annotations
        if (baseAction.getAnnotations() != null) {
            extendedAction.setAnnotations(new ArrayList<>(baseAction.getAnnotations()));
        }
        
        return extendedAction;
    }
    
    private CsdlFunction convertToExtendedFunction(CsdlFunction baseFunction) {
        ExtendedCsdlFunction extendedFunction = new ExtendedCsdlFunction();
        
        // 复制基础属性
        extendedFunction.setName(baseFunction.getName());
        extendedFunction.setBound(baseFunction.isBound());
        extendedFunction.setComposable(baseFunction.isComposable());
        extendedFunction.setEntitySetPath(baseFunction.getEntitySetPath());
        
        // 复制参数
        if (baseFunction.getParameters() != null) {
            extendedFunction.setParameters(new ArrayList<>(baseFunction.getParameters()));
        }
        
        // 复制返回类型
        if (baseFunction.getReturnType() != null) {
            extendedFunction.setReturnType(baseFunction.getReturnType());
        }
        
        // 复制Annotations
        if (baseFunction.getAnnotations() != null) {
            extendedFunction.setAnnotations(new ArrayList<>(baseFunction.getAnnotations()));
        }
        
        return extendedFunction;
    }
}
