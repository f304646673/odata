package org.apache.olingo.xmlprocessor.parser.impl;

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
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlSchema;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSDL XML解析器实现
 * 使用Olingo原生解析方法并返回ExtendedCsdlSchema
 */
public class CsdlXmlParserImpl implements ODataXmlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(CsdlXmlParserImpl.class);
    
    /**
     * 构造函数
     */
    public CsdlXmlParserImpl() {
    }
    
    @Override
    public ParseResult parseSchemas(InputStream inputStream, String sourceName) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<ExtendedCsdlSchema> schemas = new ArrayList<>();

        try {
            logger.debug("Parsing XML from source: {}", sourceName);

            // 使用Olingo原生解析器解析
            List<CsdlSchema> parsedSchemas = parseWithOlingoNative(inputStream, sourceName);
            
            if (parsedSchemas.isEmpty()) {
                warnings.add("No valid schemas found in " + sourceName);
            } else {
                // 转换为ExtendedCsdlSchema
                for (CsdlSchema schema : parsedSchemas) {
                    try {
                        ExtendedCsdlSchema extendedSchema = convertToExtendedSchema(schema, sourceName);
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
     * @param resourcePath 资源路径（例如："/test-xml/valid-schema.xml"）
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
            
            // 配置解析器
            parser.parseAnnotations(true);
            parser.useLocalCoreVocabularies(true);
            parser.implicitlyLoadCoreVocabularies(true);
            parser.recursivelyLoadReferences(false);

            // 创建InputStreamReader确保正确的字符编码
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            
            // 使用Olingo解析器构建EdmProvider
            SchemaBasedEdmProvider provider = parser.buildEdmProvider(reader);
            
            // 从EdmProvider中提取Schemas
            List<CsdlSchema> schemas = new ArrayList<>();
            for (CsdlSchema schema : provider.getSchemas()) {
                schemas.add(schema);
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
     * 转换为ExtendedCsdlSchema模型
     */
    private ExtendedCsdlSchema convertToExtendedSchema(CsdlSchema baseSchema, String sourceName) {
        logger.debug("Converting schema: {}", baseSchema.getNamespace());

        // 创建ExtendedCsdlSchema
        ExtendedCsdlSchema extendedSchema = ExtendedCsdlSchema.fromCsdlSchema(baseSchema);

        // 设置源路径
        extendedSchema.setSourcePath(sourceName);

        // 转换所有内部元素为扩展模型（如果需要的话）
        convertSchemaElements(extendedSchema);

        return extendedSchema;
    }

    /**
     * 转换Schema内部元素为扩展模型
     */
    private void convertSchemaElements(ExtendedCsdlSchema schema) {
        // 这里可以进一步转换内部的EntityType、ComplexType等为扩展模型
        // 目前先保持原有的元素类型，如果需要可以进一步扩展

        logger.debug("Schema elements conversion completed for: {}", schema.getNamespace());
    }
}

