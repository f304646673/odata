package org.apache.olingo.schema.processor.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简化的CSDL XML解析器实现，用于演示目的
 */
public class CsdlXmlParserImpl implements ODataXmlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(CsdlXmlParserImpl.class);
    
    @Override
    public ParseResult parseSchemas(InputStream inputStream, String sourceName) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<CsdlSchema> schemas = new ArrayList<>();
        
        try {
            logger.debug("Parsing XML from source: {}", sourceName);
            
            // 读取输入流内容
            byte[] buffer = new byte[8192];
            StringBuilder content = new StringBuilder();
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
            String xmlContent = content.toString();
            
            // 简单解析：查找Schema元素
            if (xmlContent.contains("<Schema")) {
                CsdlSchema schema = parseSchemaFromContent(xmlContent, sourceName);
                if (schema != null) {
                    schemas.add(schema);
                    logger.debug("Parsed schema: {}", schema.getNamespace());
                }
            } else {
                warnings.add("No Schema elements found in " + sourceName);
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
            byte[] content = Files.readAllBytes(filePath);
            
            try (InputStream inputStream = new ByteArrayInputStream(content)) {
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
            
        } catch (Exception e) {
            errors.add("XML validation error: " + e.getMessage());
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * 从XML内容解析Schema（简化实现）
     */
    private CsdlSchema parseSchemaFromContent(String xmlContent, String sourceName) {
        try {
            CsdlSchema schema = new CsdlSchema();
            
            // 提取namespace
            String namespace = extractAttribute(xmlContent, "Schema", "Namespace");
            if (namespace != null) {
                schema.setNamespace(namespace);
                logger.debug("Found schema namespace: {}", namespace);
            } else {
                // 如果没有找到namespace，使用文件名生成一个
                String fileName = sourceName.substring(sourceName.lastIndexOf('/') + 1);
                if (fileName.endsWith(".xml")) {
                    fileName = fileName.substring(0, fileName.length() - 4);
                }
                namespace = "Generated." + fileName;
                schema.setNamespace(namespace);
                logger.debug("Generated namespace: {}", namespace);
            }
            
            // 解析EntityTypes
            List<CsdlEntityType> entityTypes = parseEntityTypesFromContent(xmlContent, namespace);
            schema.setEntityTypes(entityTypes);
            
            logger.debug("Parsed {} EntityTypes from {}", entityTypes.size(), sourceName);
            
            return schema;
            
        } catch (Exception e) {
            logger.error("Error parsing schema from content", e);
            return null;
        }
    }
    
    /**
     * 从XML内容解析EntityTypes（简化实现）
     */
    private List<CsdlEntityType> parseEntityTypesFromContent(String xmlContent, String namespace) {
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        
        try {
            // 查找所有EntityType元素
            int index = 0;
            while ((index = xmlContent.indexOf("<EntityType", index)) != -1) {
                int endIndex = xmlContent.indexOf(">", index);
                if (endIndex == -1) break;
                
                String entityTypeTag = xmlContent.substring(index, endIndex + 1);
                String name = extractAttributeFromTag(entityTypeTag, "Name");
                String baseType = extractAttributeFromTag(entityTypeTag, "BaseType");
                
                if (name != null) {
                    CsdlEntityType entityType = new CsdlEntityType();
                    entityType.setName(name);
                    
                    if (baseType != null) {
                        entityType.setBaseType(new FullQualifiedName(baseType));
                    }
                    
                    // 查找EntityType的结束标签
                    int entityEndIndex = xmlContent.indexOf("</EntityType>", endIndex);
                    if (entityEndIndex == -1) {
                        entityEndIndex = xmlContent.indexOf("/>", index);
                    }
                    
                    if (entityEndIndex != -1) {
                        String entityTypeContent = xmlContent.substring(endIndex + 1, entityEndIndex);
                        List<CsdlProperty> properties = parsePropertiesFromContent(entityTypeContent);
                        entityType.setProperties(properties);
                    }
                    
                    entityTypes.add(entityType);
                    logger.debug("Parsed EntityType: {}", name);
                }
                
                index = endIndex + 1;
            }
            
        } catch (Exception e) {
            logger.error("Error parsing EntityTypes", e);
        }
        
        return entityTypes;
    }
    
    /**
     * 从XML内容解析Properties（简化实现）
     */
    private List<CsdlProperty> parsePropertiesFromContent(String entityContent) {
        List<CsdlProperty> properties = new ArrayList<>();
        
        try {
            int index = 0;
            while ((index = entityContent.indexOf("<Property", index)) != -1) {
                int endIndex = entityContent.indexOf("/>", index);
                if (endIndex == -1) {
                    endIndex = entityContent.indexOf(">", index);
                }
                if (endIndex == -1) break;
                
                String propertyTag = entityContent.substring(index, endIndex + 1);
                String name = extractAttributeFromTag(propertyTag, "Name");
                String type = extractAttributeFromTag(propertyTag, "Type");
                String nullable = extractAttributeFromTag(propertyTag, "Nullable");
                
                if (name != null && type != null) {
                    CsdlProperty property = new CsdlProperty();
                    property.setName(name);
                    property.setType(new FullQualifiedName(type));
                    
                    if ("false".equalsIgnoreCase(nullable)) {
                        property.setNullable(false);
                    }
                    
                    properties.add(property);
                }
                
                index = endIndex + 1;
            }
            
        } catch (Exception e) {
            logger.error("Error parsing Properties", e);
        }
        
        return properties;
    }
    
    /**
     * 从XML内容中提取指定元素的属性
     */
    private String extractAttribute(String xmlContent, String elementName, String attributeName) {
        String searchPattern = "<" + elementName;
        int index = xmlContent.indexOf(searchPattern);
        if (index == -1) return null;
        
        int endIndex = xmlContent.indexOf(">", index);
        if (endIndex == -1) return null;
        
        String elementTag = xmlContent.substring(index, endIndex + 1);
        return extractAttributeFromTag(elementTag, attributeName);
    }
    
    /**
     * 从XML标签中提取属性值
     */
    private String extractAttributeFromTag(String tag, String attributeName) {
        String searchPattern = attributeName + "=\"";
        int index = tag.indexOf(searchPattern);
        if (index == -1) return null;
        
        int startIndex = index + searchPattern.length();
        int endIndex = tag.indexOf("\"", startIndex);
        if (endIndex == -1) return null;
        
        return tag.substring(startIndex, endIndex);
    }
}
