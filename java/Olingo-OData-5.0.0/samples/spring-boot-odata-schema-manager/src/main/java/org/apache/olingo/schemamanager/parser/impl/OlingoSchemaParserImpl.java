package org.apache.olingo.schemamanager.parser.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * OData Schema解析器实现
 * 使用Olingo底层方法解析OData XML
 */
@Component
public class OlingoSchemaParserImpl implements ODataSchemaParser {
    
    private static final Logger logger = LoggerFactory.getLogger(OlingoSchemaParserImpl.class);
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)");
    
    // 存储schema的Using dependencies
    private final Map<String, Set<String>> schemaDependenciesMap = new HashMap<>();
    @Override
    public ParseResult parseSchema(InputStream inputStream, String sourceName) {
        try {
            logger.debug("开始解析Schema: {}", sourceName);
            
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
            factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
            
            // 使用Olingo的MetadataParser解析XML
            // 注意：这里需要根据实际的Olingo API调整
            List<CsdlSchema> schemas = parseWithOlingo(reader, sourceName);
            
            if (schemas.isEmpty()) {
                return ParseResult.failure("未找到有效的Schema定义");
            }
            
            // 验证 namespace 唯一性
            Set<String> namespaces = new HashSet<>();
            for (CsdlSchema schema : schemas) {
                if (schema.getNamespace() != null) {
                    if (!namespaces.add(schema.getNamespace())) {
                        return ParseResult.failure("发现重复的namespace: " + schema.getNamespace());
                    }
                }
            }
            
            // 创建 SchemaWithDependencies 列表
            List<SchemaWithDependencies> schemaWithDependencies = new ArrayList<>();
            for (CsdlSchema schema : schemas) {
                List<String> dependencies = extractDependencies(schema);
                schemaWithDependencies.add(new SchemaWithDependencies(schema, dependencies));
            }
            
            logger.debug("成功解析Schema文件: {}, 包含{}个Schema", sourceName, schemas.size());
            return ParseResult.success(schemaWithDependencies);
            
        } catch (Exception e) {
            logger.error("解析Schema失败: {}", sourceName, e);
            return ParseResult.failure("解析Schema失败: " + e.getMessage());
        }
    }
    
    private List<CsdlSchema> parseWithOlingo(XMLStreamReader reader, String sourceName) throws Exception {
        // 这里实现具体的Olingo解析逻辑
        // 由于Olingo的API可能会变化，这里提供一个基础的解析框架
        
        List<CsdlSchema> schemas = new ArrayList<>();
        CsdlSchema currentSchema = null;
        String currentNamespace = null;
        Set<String> currentSchemaDependencies = new HashSet<>();
        
        while (reader.hasNext()) {
            int eventType = reader.next();
            
            if (eventType == XMLStreamReader.START_ELEMENT) {
                String localName = reader.getLocalName();
                String namespaceURI = reader.getNamespaceURI();
                
                // 解析Schema元素
                if ("Schema".equals(localName) && namespaceURI.contains("edm")) {
                    currentNamespace = reader.getAttributeValue(null, "Namespace");
                    currentSchema = new CsdlSchema();
                    currentSchema.setNamespace(currentNamespace);
                    currentSchemaDependencies = new HashSet<>();
                    
                    logger.debug("发现Schema定义: {}", currentNamespace);
                }
                // 解析Using元素
                else if ("Using".equals(localName) && currentSchema != null) {
                    String namespace = reader.getAttributeValue(null, "Namespace");
                    String alias = reader.getAttributeValue(null, "Alias");
                    if (namespace != null) {
                        // 将Using的namespace添加到dependencies中
                        currentSchemaDependencies.add(namespace);
                        logger.debug("发现Using定义: namespace={}, alias={}", namespace, alias);
                    }
                }
                // 解析EntityType
                else if ("EntityType".equals(localName) && currentSchema != null) {
                    CsdlEntityType entityType = parseEntityType(reader);
                    if (entityType != null) {
                        currentSchema.getEntityTypes().add(entityType);
                        logger.debug("解析EntityType: {}", entityType.getName());
                    }
                }
                // 解析ComplexType
                else if ("ComplexType".equals(localName) && currentSchema != null) {
                    CsdlComplexType complexType = parseComplexType(reader);
                    if (complexType != null) {
                        currentSchema.getComplexTypes().add(complexType);
                        logger.debug("解析ComplexType: {}", complexType.getName());
                    }
                }
                // 解析EnumType
                else if ("EnumType".equals(localName) && currentSchema != null) {
                    CsdlEnumType enumType = parseEnumType(reader);
                    if (enumType != null) {
                        currentSchema.getEnumTypes().add(enumType);
                        logger.debug("解析EnumType: {}", enumType.getName());
                    }
                }
                // 解析EntityContainer
                else if ("EntityContainer".equals(localName) && currentSchema != null) {
                    CsdlEntityContainer container = parseEntityContainer(reader);
                    if (container != null) {
                        currentSchema.setEntityContainer(container);
                        logger.debug("解析EntityContainer: {}", container.getName());
                    }
                }
            }
            else if (eventType == XMLStreamReader.END_ELEMENT) {
                if ("Schema".equals(reader.getLocalName()) && currentSchema != null) {
                    // 存储schema的dependencies
                    if (currentSchema.getNamespace() != null) {
                        schemaDependenciesMap.put(currentSchema.getNamespace(), new HashSet<>(currentSchemaDependencies));
                    }
                    schemas.add(currentSchema);
                    currentSchema = null;
                    currentNamespace = null;
                    currentSchemaDependencies = new HashSet<>();
                }
            }
        }
        
        return schemas;
    }
    
    private CsdlEntityType parseEntityType(XMLStreamReader reader) throws Exception {
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(reader.getAttributeValue(null, "Name"));
        
        String baseType = reader.getAttributeValue(null, "BaseType");
        if (baseType != null) {
            entityType.setBaseType(baseType);
        }
        
        List<CsdlProperty> properties = new ArrayList<>();
        List<CsdlNavigationProperty> navigationProperties = new ArrayList<>();
        List<CsdlPropertyRef> keyProperties = new ArrayList<>();
        
        while (reader.hasNext()) {
            int eventType = reader.next();
            
            if (eventType == XMLStreamReader.START_ELEMENT) {
                String localName = reader.getLocalName();
                
                if ("Property".equals(localName)) {
                    CsdlProperty property = parseProperty(reader);
                    if (property != null) {
                        properties.add(property);
                    }
                } else if ("NavigationProperty".equals(localName)) {
                    CsdlNavigationProperty navProp = parseNavigationProperty(reader);
                    if (navProp != null) {
                        navigationProperties.add(navProp);
                    }
                } else if ("Key".equals(localName)) {
                    keyProperties.addAll(parseKeyProperties(reader));
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT && "EntityType".equals(reader.getLocalName())) {
                break;
            }
        }
        
        entityType.setProperties(properties);
        entityType.setNavigationProperties(navigationProperties);
        if (!keyProperties.isEmpty()) {
            entityType.setKey(keyProperties);
        }
        
        return entityType;
    }
    
    private CsdlComplexType parseComplexType(XMLStreamReader reader) throws Exception {
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(reader.getAttributeValue(null, "Name"));
        
        String baseType = reader.getAttributeValue(null, "BaseType");
        if (baseType != null) {
            complexType.setBaseType(baseType);
        }
        
        List<CsdlProperty> properties = new ArrayList<>();
        
        while (reader.hasNext()) {
            int eventType = reader.next();
            
            if (eventType == XMLStreamReader.START_ELEMENT && "Property".equals(reader.getLocalName())) {
                CsdlProperty property = parseProperty(reader);
                if (property != null) {
                    properties.add(property);
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT && "ComplexType".equals(reader.getLocalName())) {
                break;
            }
        }
        
        complexType.setProperties(properties);
        return complexType;
    }
    
    private CsdlEnumType parseEnumType(XMLStreamReader reader) throws Exception {
        CsdlEnumType enumType = new CsdlEnumType();
        enumType.setName(reader.getAttributeValue(null, "Name"));
        
        String underlyingType = reader.getAttributeValue(null, "UnderlyingType");
        if (underlyingType != null) {
            enumType.setUnderlyingType(underlyingType);
        }
        
        List<CsdlEnumMember> members = new ArrayList<>();
        
        while (reader.hasNext()) {
            int eventType = reader.next();
            
            if (eventType == XMLStreamReader.START_ELEMENT && "Member".equals(reader.getLocalName())) {
                CsdlEnumMember member = new CsdlEnumMember();
                member.setName(reader.getAttributeValue(null, "Name"));
                String value = reader.getAttributeValue(null, "Value");
                if (value != null) {
                    member.setValue(value);
                }
                members.add(member);
            } else if (eventType == XMLStreamReader.END_ELEMENT && "EnumType".equals(reader.getLocalName())) {
                break;
            }
        }
        
        enumType.setMembers(members);
        return enumType;
    }
    
    private CsdlEntityContainer parseEntityContainer(XMLStreamReader reader) throws Exception {
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName(reader.getAttributeValue(null, "Name"));
        
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        
        while (reader.hasNext()) {
            int eventType = reader.next();
            
            if (eventType == XMLStreamReader.START_ELEMENT && "EntitySet".equals(reader.getLocalName())) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(reader.getAttributeValue(null, "Name"));
                entitySet.setType(reader.getAttributeValue(null, "EntityType"));
                entitySets.add(entitySet);
            } else if (eventType == XMLStreamReader.END_ELEMENT && "EntityContainer".equals(reader.getLocalName())) {
                break;
            }
        }
        
        container.setEntitySets(entitySets);
        return container;
    }
    
    private CsdlProperty parseProperty(XMLStreamReader reader) throws Exception {
        CsdlProperty property = new CsdlProperty();
        property.setName(reader.getAttributeValue(null, "Name"));
        property.setType(reader.getAttributeValue(null, "Type"));
        
        String nullable = reader.getAttributeValue(null, "Nullable");
        if (nullable != null) {
            property.setNullable(Boolean.parseBoolean(nullable));
        }
        
        String maxLength = reader.getAttributeValue(null, "MaxLength");
        if (maxLength != null && !"Max".equals(maxLength)) {
            try {
                property.setMaxLength(Integer.parseInt(maxLength));
            } catch (NumberFormatException e) {
                logger.warn("无效的MaxLength值: {}", maxLength);
            }
        }
        
        return property;
    }
    
    private CsdlNavigationProperty parseNavigationProperty(XMLStreamReader reader) throws Exception {
        CsdlNavigationProperty navProp = new CsdlNavigationProperty();
        navProp.setName(reader.getAttributeValue(null, "Name"));
        navProp.setType(reader.getAttributeValue(null, "Type"));
        
        String nullable = reader.getAttributeValue(null, "Nullable");
        if (nullable != null) {
            navProp.setNullable(Boolean.parseBoolean(nullable));
        }
        
        return navProp;
    }
    
    private List<CsdlPropertyRef> parseKeyProperties(XMLStreamReader reader) throws Exception {
        List<CsdlPropertyRef> propertyRefs = new ArrayList<>();
        
        while (reader.hasNext()) {
            int eventType = reader.next();
            
            if (eventType == XMLStreamReader.START_ELEMENT && "PropertyRef".equals(reader.getLocalName())) {
                CsdlPropertyRef propRef = new CsdlPropertyRef();
                propRef.setName(reader.getAttributeValue(null, "Name"));
                propertyRefs.add(propRef);
            } else if (eventType == XMLStreamReader.END_ELEMENT && "Key".equals(reader.getLocalName())) {
                break;
            }
        }
        
        return propertyRefs;
    }
    
    @Override
    public List<String> extractDependencies(CsdlSchema schema) {
        Set<String> dependencies = new HashSet<>();
        
        if (schema == null) {
            return new ArrayList<>(dependencies);
        }
        
        // 首先添加来自Using语句的dependencies
        Set<String> usingDependencies = schemaDependenciesMap.get(schema.getNamespace());
        if (usingDependencies != null) {
            dependencies.addAll(usingDependencies);
        }
        
        // 从EntityType中提取依赖
        for (CsdlEntityType entityType : schema.getEntityTypes()) {
            extractDependenciesFromEntityType(entityType, dependencies);
        }
        
        // 从ComplexType中提取依赖
        for (CsdlComplexType complexType : schema.getComplexTypes()) {
            extractDependenciesFromComplexType(complexType, dependencies);
        }
        
        // 移除当前Schema的namespace
        dependencies.remove(schema.getNamespace());
        
        return new ArrayList<>(dependencies);
    }
    
    private void extractDependenciesFromEntityType(CsdlEntityType entityType, Set<String> dependencies) {
        // BaseType依赖
        if (entityType.getBaseType() != null) {
            String namespace = extractNamespace(entityType.getBaseType());
            if (namespace != null) {
                dependencies.add(namespace);
            }
        }
        
        // Property类型依赖
        for (CsdlProperty property : entityType.getProperties()) {
            String namespace = extractNamespace(property.getType());
            if (namespace != null) {
                dependencies.add(namespace);
            }
        }
        
        // NavigationProperty类型依赖
        for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
            String namespace = extractNamespace(navProp.getType());
            if (namespace != null) {
                dependencies.add(namespace);
            }
        }
    }
    
    private void extractDependenciesFromComplexType(CsdlComplexType complexType, Set<String> dependencies) {
        // BaseType依赖
        if (complexType.getBaseType() != null) {
            String namespace = extractNamespace(complexType.getBaseType());
            if (namespace != null) {
                dependencies.add(namespace);
            }
        }
        
        // Property类型依赖
        for (CsdlProperty property : complexType.getProperties()) {
            String namespace = extractNamespace(property.getType());
            if (namespace != null) {
                dependencies.add(namespace);
            }
        }
    }
    
    private String extractNamespace(String fullQualifiedName) {
        if (fullQualifiedName == null || fullQualifiedName.trim().isEmpty()) {
            return null;
        }
        
        // 处理Collection类型
        String typeName = fullQualifiedName;
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            typeName = typeName.substring(11, typeName.length() - 1);
        }
        
        // 跳过EDM基础类型
        if (typeName.startsWith("Edm.")) {
            return null;
        }
        
        // 提取namespace
        int lastDotIndex = typeName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String namespace = typeName.substring(0, lastDotIndex);
            
            // 验证namespace格式
            Matcher matcher = NAMESPACE_PATTERN.matcher(namespace);
            if (matcher.matches()) {
                return namespace;
            }
        }
        
        return null;
    }
    
    @Override
    public ValidationResult validateSchema(CsdlSchema schema) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (schema == null) {
            errors.add("Schema不能为null");
            return new ValidationResult(false, errors, warnings);
        }
        
        // 验证namespace
        if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
            errors.add("Schema必须定义namespace");
        } else {
            Matcher matcher = NAMESPACE_PATTERN.matcher(schema.getNamespace());
            if (!matcher.matches()) {
                errors.add("无效的namespace格式: " + schema.getNamespace());
            }
        }
        
        // 验证EntityType名称唯一性
        Set<String> entityTypeNames = new HashSet<>();
        for (CsdlEntityType entityType : schema.getEntityTypes()) {
            if (entityType.getName() == null || entityType.getName().trim().isEmpty()) {
                errors.add("EntityType必须有名称");
            } else if (!entityTypeNames.add(entityType.getName())) {
                errors.add("重复的EntityType名称: " + entityType.getName());
            }
        }
        
        // 验证ComplexType名称唯一性
        Set<String> complexTypeNames = new HashSet<>();
        for (CsdlComplexType complexType : schema.getComplexTypes()) {
            if (complexType.getName() == null || complexType.getName().trim().isEmpty()) {
                errors.add("ComplexType必须有名称");
            } else if (!complexTypeNames.add(complexType.getName())) {
                errors.add("重复的ComplexType名称: " + complexType.getName());
            }
        }
        
        // 验证EnumType名称唯一性
        Set<String> enumTypeNames = new HashSet<>();
        for (CsdlEnumType enumType : schema.getEnumTypes()) {
            if (enumType.getName() == null || enumType.getName().trim().isEmpty()) {
                errors.add("EnumType必须有名称");
            } else if (!enumTypeNames.add(enumType.getName())) {
                errors.add("重复的EnumType名称: " + enumType.getName());
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * 验证多个Schema的一致性
     */
    public ValidationResult validateSchemas(List<CsdlSchema> schemas) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (schemas == null || schemas.isEmpty()) {
            errors.add("Schema列表不能为空");
            return new ValidationResult(false, errors, warnings);
        }
        
        // 验证每个Schema
        for (CsdlSchema schema : schemas) {
            ValidationResult result = validateSchema(schema);
            if (!result.isValid()) {
                errors.addAll(result.getErrors());
                warnings.addAll(result.getWarnings());
            }
        }
        
        // 验证namespace唯一性
        Set<String> namespaces = new HashSet<>();
        for (CsdlSchema schema : schemas) {
            if (schema.getNamespace() != null) {
                if (!namespaces.add(schema.getNamespace())) {
                    errors.add("发现重复的namespace: " + schema.getNamespace());
                }
            }
        }
        
        // 验证类型名称在全局的唯一性
        Set<String> globalTypeNames = new HashSet<>();
        for (CsdlSchema schema : schemas) {
            String namespace = schema.getNamespace();
            
            // 检查EntityType
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullTypeName = namespace + "." + entityType.getName();
                if (!globalTypeNames.add(fullTypeName)) {
                    errors.add("全局重复的类型名称: " + fullTypeName);
                }
            }
            
            // 检查ComplexType
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullTypeName = namespace + "." + complexType.getName();
                if (!globalTypeNames.add(fullTypeName)) {
                    errors.add("全局重复的类型名称: " + fullTypeName);
                }
            }
            
            // 检查EnumType
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                String fullTypeName = namespace + "." + enumType.getName();
                if (!globalTypeNames.add(fullTypeName)) {
                    errors.add("全局重复的类型名称: " + fullTypeName);
                }
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
}
