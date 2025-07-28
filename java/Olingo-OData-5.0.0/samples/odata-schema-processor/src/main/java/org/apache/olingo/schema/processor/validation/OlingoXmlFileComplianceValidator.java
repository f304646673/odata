package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.ReferenceResolver;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Olingo-based implementation of XmlFileComplianceValidator.
 * Uses Olingo's native MetadataParser to validate OData 4.0 XML files.
 */
public class OlingoXmlFileComplianceValidator implements XmlFileComplianceValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(OlingoXmlFileComplianceValidator.class);
    
    @Override
    public XmlComplianceResult validateFile(File xmlFile) {
        if (xmlFile == null || !xmlFile.exists()) {
            return createErrorResult("File does not exist or is null", xmlFile != null ? xmlFile.getName() : "null", 0);
        }
        
        return validateFile(xmlFile.toPath());
    }
    
    @Override
    public XmlComplianceResult validateFile(Path xmlPath) {
        if (xmlPath == null || !Files.exists(xmlPath)) {
            return createErrorResult("Path does not exist or is null", xmlPath != null ? xmlPath.getFileName().toString() : "null", 0);
        }
        
        long startTime = System.currentTimeMillis();
        String fileName = xmlPath.getFileName().toString();
        
        try {
            return validatePath(xmlPath);
        } catch (Exception e) {
            logger.error("Failed to read file: {}", xmlPath, e);
            return createErrorResult("Failed to read file: " + e.getMessage(), fileName, System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * Validate using file path to ensure proper reference resolution
     */
    private XmlComplianceResult validatePath(Path xmlPath) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> referencedNamespaces = new HashSet<>();
        Map<String, Object> metadata = new HashMap<>();
        String fileName = xmlPath.getFileName().toString();

        try {
            // Configure MetadataParser with base URI for reference resolution
            MetadataParser parser = new MetadataParser();
            parser.recursivelyLoadReferences(true);
            
            // 获取文件的父目录作为基本 URI
            URI baseUri = xmlPath.getParent().toUri();
            
            // 创建自定义引用解析器
            ReferenceResolver resolver = (uri, baseURI) -> {
                try {
                    // 如果是相对路径，基于当前文件目录解析
                    if (!uri.isAbsolute()) {
                        uri = baseUri.resolve(uri);
                    }
                    
                    // 转换为文件路径
                    Path referencePath = Paths.get(uri);
                    if (Files.exists(referencePath)) {
                        return Files.newInputStream(referencePath);
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            };
            
            parser.referenceResolver(resolver);

            // Parse with base URI context
            SchemaBasedEdmProvider edmProvider;
            String fileContent = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);
            
            // 为 XML 内容添加 xml:base 属性以确保正确的引用解析
            String modifiedContent = addXmlBase(fileContent, baseUri.toString());
            
            try (StringReader reader = new StringReader(modifiedContent)) {
                edmProvider = parser.buildEdmProvider(reader);
            }

            // Validate schemas in the main file
            List<CsdlSchema> schemas = edmProvider.getSchemas();
            if (schemas != null && !schemas.isEmpty()) {
                for (CsdlSchema schema : schemas) {
                    validateCsdlSchema(schema, errors, warnings, referencedNamespaces, metadata);
                }
                metadata.put("schemaCount", schemas.size());
                
                // Check references
                List<EdmxReference> references = edmProvider.getReferences();
                if (references != null) {
                    metadata.put("referenceCount", references.size());
                    for (EdmxReference reference : references) {
                        if (reference.getIncludes() != null) {
                            reference.getIncludes().forEach(include -> 
                                referencedNamespaces.add(include.getNamespace()));
                        }
                    }
                }
            } else {
                errors.add("No valid schemas found in the XML file");
            }

            long validationTime = System.currentTimeMillis() - startTime;
            metadata.put("validationTimeMs", validationTime);

            boolean isCompliant = errors.isEmpty();
            return new XmlComplianceResult(isCompliant, errors, warnings, referencedNamespaces, metadata, fileName, validationTime);

        } catch (Exception e) {
            logger.error("Validation failed for file: {}", fileName, e);
            errors.add("Validation error: " + e.getMessage());
            return new XmlComplianceResult(false, errors, warnings, referencedNamespaces, metadata, fileName, System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 为 XML 内容添加 xml:base 属性
     */
    private String addXmlBase(String xmlContent, String baseUri) {
        // 在根元素中添加 xml:base 属性
        String pattern = "(<edmx:Edmx[^>]*)(>)";
        String replacement = "$1 xml:base=\"" + baseUri + "\"$2";
        return xmlContent.replaceFirst(pattern, replacement);
    }
    
    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName) {
        if (fileName == null) {
            fileName = "unknown";
        }
        
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return createErrorResult("XML content is null or empty", fileName, 0);
        }
        
        try (InputStream inputStream = new java.io.ByteArrayInputStream(xmlContent.getBytes("UTF-8"))) {
            return validateInputStream(inputStream, fileName, null);
        } catch (Exception e) {
            logger.error("Failed to validate content for file: {}", fileName, e);
            return createErrorResult("Failed to validate content: " + e.getMessage(), fileName, 0);
        }
    }
    
    /**
     * Core validation method using Olingo's native MetadataParser
     */
    private XmlComplianceResult validateInputStream(InputStream inputStream, String fileName, Path basePath) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> referencedNamespaces = new HashSet<>();
        Map<String, Object> metadata = new HashMap<>();

        try {
            // Configure MetadataParser with reference resolution capability
            MetadataParser parser = new MetadataParser();
            
            // Enable reference loading
            parser.recursivelyLoadReferences(true);

            // Parse the XML file
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            SchemaBasedEdmProvider edmProvider = parser.buildEdmProvider(reader);

            // Validate schemas in the main file
            List<CsdlSchema> schemas = edmProvider.getSchemas();
            if (schemas != null && !schemas.isEmpty()) {
                for (CsdlSchema schema : schemas) {
                    validateCsdlSchema(schema, errors, warnings, referencedNamespaces, metadata);
                }
                metadata.put("schemaCount", schemas.size());
                
                // Check references
                List<EdmxReference> references = edmProvider.getReferences();
                if (references != null) {
                    metadata.put("referenceCount", references.size());
                    for (EdmxReference reference : references) {
                        if (reference.getIncludes() != null) {
                            reference.getIncludes().forEach(include -> 
                                referencedNamespaces.add(include.getNamespace()));
                        }
                    }
                }
            } else {
                errors.add("No valid schemas found in the XML file");
            }

            long validationTime = System.currentTimeMillis() - startTime;
            metadata.put("validationTimeMs", validationTime);

            boolean isCompliant = errors.isEmpty();
            return new XmlComplianceResult(isCompliant, errors, warnings, referencedNamespaces, metadata, fileName, validationTime);

        } catch (Exception e) {
            logger.error("Validation failed for file: {}", fileName, e);
            errors.add("Validation error: " + e.getMessage());
            return new XmlComplianceResult(false, errors, warnings, referencedNamespaces, metadata, fileName, System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * Validate a CSDL Schema using Olingo's data structures
     */
    private void validateCsdlSchema(CsdlSchema schema, List<String> errors, List<String> warnings, 
                                   Set<String> referencedNamespaces, Map<String, Object> metadata) {
        
        // Validate schema namespace
        if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
            errors.add("Schema must have a valid namespace");
            return;
        }
        
        String namespace = schema.getNamespace();
        referencedNamespaces.add(namespace);
        
        // Validate namespace format using Olingo's validation logic
        if (!isValidODataNamespace(namespace)) {
            errors.add("Invalid namespace format: " + namespace);
        }
        
        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                validateEntityType(entityType, errors, warnings, referencedNamespaces);
            }
            metadata.put("entityTypes_" + namespace, schema.getEntityTypes().size());
        }
        
        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                validateComplexType(complexType, errors, warnings, referencedNamespaces);
            }
            metadata.put("complexTypes_" + namespace, schema.getComplexTypes().size());
        }
        
        // Validate entity container
        if (schema.getEntityContainer() != null) {
            validateEntityContainer(schema.getEntityContainer(), errors);
        }
    }

    /**
     * Validate OData namespace according to OData 4 naming rules.
     */
    private boolean isValidODataNamespace(String namespace) {
        if (namespace == null || namespace.trim().isEmpty()) {
            return false;
        }

        // Ensure it starts and ends with a valid character and does not contain consecutive dots
        String namespacePattern = "^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*$";
        return namespace.matches(namespacePattern);
    }

    /**
     * Validate EntityType using Olingo structures
     */
    private void validateEntityType(CsdlEntityType entityType, List<String> errors, 
                                   List<String> warnings, Set<String> referencedNamespaces) {
        
        if (entityType.getName() == null || entityType.getName().trim().isEmpty()) {
            errors.add("EntityType must have a valid name");
            return;
        }
        
        if (!isValidODataIdentifier(entityType.getName())) {
            errors.add("Invalid EntityType name: " + entityType.getName());
        }
        
        // Check BaseType reference
        if (entityType.getBaseType() != null) {
            String baseType = entityType.getBaseType();
            extractAndValidateTypeReference(baseType, referencedNamespaces, warnings);
        }
        
        // Validate properties
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                validateProperty(property, errors, warnings, referencedNamespaces);
            }
        }
        
        // Validate navigation properties
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                validateNavigationProperty(navProp, errors, warnings, referencedNamespaces);
            }
        }
    }
    
    /**
     * Validate ComplexType using Olingo structures
     */
    private void validateComplexType(CsdlComplexType complexType, List<String> errors, 
                                    List<String> warnings, Set<String> referencedNamespaces) {
        
        if (complexType.getName() == null || complexType.getName().trim().isEmpty()) {
            errors.add("ComplexType must have a valid name");
            return;
        }
        
        if (!isValidODataIdentifier(complexType.getName())) {
            errors.add("Invalid ComplexType name: " + complexType.getName());
        }
        
        // Check BaseType reference
        if (complexType.getBaseType() != null) {
            String baseType = complexType.getBaseType();
            extractAndValidateTypeReference(baseType, referencedNamespaces, warnings);
        }
        
        // Validate properties
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                validateProperty(property, errors, warnings, referencedNamespaces);
            }
        }
    }
    
    /**
     * Validate Property using Olingo structures
     */
    private void validateProperty(CsdlProperty property, List<String> errors, List<String> warnings, Set<String> referencedNamespaces) {
        if (property.getName() == null || property.getName().trim().isEmpty()) {
            errors.add("Property must have a valid name");
            return;
        }
        
        if (!isValidODataIdentifier(property.getName())) {
            errors.add("Invalid Property name: " + property.getName());
        }
        
        // Validate property type
        if (property.getType() != null) {
            extractAndValidateTypeReference(property.getType(), referencedNamespaces, warnings);
        } else {
            errors.add("Property " + property.getName() + " must have a type");
        }
    }
    
    /**
     * Validate NavigationProperty using Olingo structures
     */
    private void validateNavigationProperty(CsdlNavigationProperty navProp, List<String> errors, 
                                          List<String> warnings, Set<String> referencedNamespaces) {
        if (navProp.getName() == null || navProp.getName().trim().isEmpty()) {
            errors.add("NavigationProperty must have a valid name");
            return;
        }
        
        if (!isValidODataIdentifier(navProp.getName())) {
            errors.add("Invalid NavigationProperty name: " + navProp.getName());
        }
        
        // Validate navigation property type
        if (navProp.getType() != null) {
            extractAndValidateTypeReference(navProp.getType(), referencedNamespaces, warnings);
        } else {
            errors.add("NavigationProperty " + navProp.getName() + " must have a type");
        }
    }
    
    /**
     * Validate EntityContainer using Olingo structures
     */
    private void validateEntityContainer(org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer container, 
                                        List<String> errors) {
        if (container.getName() == null || container.getName().trim().isEmpty()) {
            errors.add("EntityContainer must have a valid name");
            return;
        }
        
        if (!isValidODataIdentifier(container.getName())) {
            errors.add("Invalid EntityContainer name: " + container.getName());
        }
    }
    
    /**
     * Extract and validate type references (handles Collection(Type) and namespace references)
     */
    private void extractAndValidateTypeReference(String typeRef, Set<String> referencedNamespaces, List<String> warnings) {
        if (typeRef == null || typeRef.trim().isEmpty()) {
            return;
        }
        
        // Handle Collection(Type) format
        String actualType = typeRef;
        if (typeRef.startsWith("Collection(") && typeRef.endsWith(")")) {
            actualType = typeRef.substring(11, typeRef.length() - 1);
        }
        
        // Check if it's a primitive type
        if (actualType.startsWith("Edm.")) {
            validatePrimitiveType(actualType, warnings);
            return;
        }
        
        // Extract namespace from qualified type name
        if (actualType.contains(".")) {
            String namespace = actualType.substring(0, actualType.lastIndexOf('.'));
            referencedNamespaces.add(namespace);
        }
    }
    
    /**
     * Validate primitive type using Olingo's EdmPrimitiveTypeKind
     */
    private void validatePrimitiveType(String primitiveType, List<String> warnings) {
        try {
            String typeName = primitiveType.substring(4); // Remove "Edm." prefix
            EdmPrimitiveTypeKind.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            warnings.add("Unknown primitive type: " + primitiveType);
        }
    }
    
    /**
     * Validate OData identifier using Olingo's naming conventions
     */
    private boolean isValidODataIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        // OData identifier rules: start with letter or underscore, followed by letters, digits, or underscores
        return identifier.matches("^[A-Za-z_][A-Za-z0-9_]*$");
    }
    
    /**
     * Create an error result
     */
    private XmlComplianceResult createErrorResult(String errorMessage, String fileName, long validationTime) {
        List<String> errors = new ArrayList<>();
        errors.add(errorMessage);
        return new XmlComplianceResult(false, errors, new ArrayList<>(), new HashSet<>(), new HashMap<>(), fileName, validationTime);
    }
    
    /**
     * File System Reference Resolver for handling relative references
     */
    private static class FileSystemReferenceResolver implements ReferenceResolver {
        private final Path basePath;
        
        public FileSystemReferenceResolver(Path basePath) {
            this.basePath = basePath;
        }
        
        @Override
        public InputStream resolveReference(URI referenceUri, String xmlBase) {
            try {
                Path resolvedPath;
                if (referenceUri.isAbsolute()) {
                    resolvedPath = basePath.getFileSystem().getPath(referenceUri.getPath());
                } else {
                    resolvedPath = basePath.resolve(referenceUri.getPath()).normalize();
                }
                
                if (Files.exists(resolvedPath)) {
                    return new FileInputStream(resolvedPath.toFile());
                }
            } catch (Exception e) {
                // Ignore and return null
            }
            return null;
        }
    }
}
