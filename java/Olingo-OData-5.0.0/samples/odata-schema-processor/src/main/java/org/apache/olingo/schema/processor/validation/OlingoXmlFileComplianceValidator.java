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
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
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
            parser.parseAnnotations(true);
            parser.useLocalCoreVocabularies(false);
            
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

            // First, collect all imported namespaces from References
            Set<String> importedNamespaces = new HashSet<>();
            List<EdmxReference> references = edmProvider.getReferences();
            if (references != null) {
                metadata.put("referenceCount", references.size());
                for (EdmxReference reference : references) {
                    if (reference.getIncludes() != null) {
                        reference.getIncludes().forEach(include -> {
                            String namespace = include.getNamespace();
                            importedNamespaces.add(namespace);
                            referencedNamespaces.add(namespace);
                        });
                    }
                }
            }

            // Validate schemas in the main file
            List<CsdlSchema> schemas = edmProvider.getSchemas();
            if (schemas != null && !schemas.isEmpty()) {
                for (CsdlSchema schema : schemas) {
                    validateCsdlSchema(schema, errors, warnings, referencedNamespaces, metadata, importedNamespaces);
                }
                metadata.put("schemaCount", schemas.size());
            } else {
                errors.add("No valid schemas found in the XML file");
            }

            // Global duplicate check across all schemas
            checkGlobalSchemaDuplicates(xmlPath, errors);

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
                    validateCsdlSchema(schema, errors, warnings, referencedNamespaces, metadata, referencedNamespaces);
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

            // Global duplicate check across all schemas - for InputStream we skip this
            // since we don't have file path context for reference resolution
            // checkGlobalSchemaDuplicates(edmProvider, errors); // Removed - only works with file paths

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
                                   Set<String> referencedNamespaces, Map<String, Object> metadata,
                                   Set<String> importedNamespaces) {

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
        
        // Note: We'll check cross-schema duplicates after all schemas are processed
        // checkSchemaDuplicates(schema, errors); // Removed - will be done globally

        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                validateEntityType(entityType, errors, warnings, referencedNamespaces, importedNamespaces);
                // Validate inline annotations on EntityType
                validateInlineAnnotations(entityType.getAnnotations(), errors, importedNamespaces);
            }
            metadata.put("entityTypes_" + namespace, schema.getEntityTypes().size());
        }
        
        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                validateComplexType(complexType, errors, warnings, referencedNamespaces, importedNamespaces);
                // Validate inline annotations on ComplexType
                validateInlineAnnotations(complexType.getAnnotations(), errors, importedNamespaces);
            }
            metadata.put("complexTypes_" + namespace, schema.getComplexTypes().size());
        }
        
        // Validate enum types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                validateEnumType(enumType, errors, warnings, referencedNamespaces, importedNamespaces);
                // Validate inline annotations on EnumType
                validateInlineAnnotations(enumType.getAnnotations(), errors, importedNamespaces);
            }
        }

        // Validate actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                validateAction(action, errors, warnings, referencedNamespaces, importedNamespaces);
                // Validate inline annotations on Action
                validateInlineAnnotations(action.getAnnotations(), errors, importedNamespaces);
            }
        }

        // Validate functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                validateFunction(function, errors, warnings, referencedNamespaces, importedNamespaces);
                // Validate inline annotations on Function
                validateInlineAnnotations(function.getAnnotations(), errors, importedNamespaces);
            }
        }

        // Validate entity container
        if (schema.getEntityContainer() != null) {
            validateEntityContainer(schema.getEntityContainer(), errors, warnings, referencedNamespaces, importedNamespaces);
            // Validate inline annotations on EntityContainer
            validateInlineAnnotations(schema.getEntityContainer().getAnnotations(), errors, importedNamespaces);
        }
        
        // Validate annotations
        if (schema.getAnnotationGroups() != null && !schema.getAnnotationGroups().isEmpty()) {
            validateAnnotations(schema, errors, warnings, referencedNamespaces, importedNamespaces);
        }
    }

    /**
     * Check for duplicate schema elements across ALL schemas BEFORE Olingo processing
     * This addresses the core issue where Olingo uses "override" mode and hides conflicts
     */
    private void checkGlobalSchemaDuplicates(Path xmlPath, List<String> errors) {
        try {
            // Parse main file and all referenced files manually to detect conflicts
            Map<String, List<String>> allSchemaDefinitions = new HashMap<>();
            Set<Path> processedFiles = new HashSet<>();

            // Recursively collect all schema definitions
            collectSchemaDefinitions(xmlPath, allSchemaDefinitions, processedFiles, xmlPath.getParent());

            // Check for conflicts
            checkElementConflicts(allSchemaDefinitions, errors);

        } catch (Exception e) {
            logger.warn("Failed to perform pre-parse duplicate check: {}", e.getMessage());
            // Don't fail validation, just log the issue
        }
    }

    /**
     * Recursively collect schema definitions from XML files before Olingo processing
     */
    private void collectSchemaDefinitions(Path xmlPath, Map<String, List<String>> allDefinitions,
                                        Set<Path> processedFiles, Path baseDir) {
        if (processedFiles.contains(xmlPath.normalize())) {
            return; // Avoid circular references
        }
        processedFiles.add(xmlPath.normalize());

        try {
            if (!Files.exists(xmlPath)) {
                return;
            }

            String xmlContent = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);
            String sourceInfo = "File: " + xmlPath.getFileName();

            // Parse XML content manually to extract schema definitions
            parseSchemaDefinitions(xmlContent, sourceInfo, allDefinitions);

            // Find and process references
            List<String> references = extractReferences(xmlContent);
            for (String refUri : references) {
                try {
                    Path refPath = resolveReference(refUri, baseDir);
                    if (refPath != null) {
                        collectSchemaDefinitions(refPath, allDefinitions, processedFiles, baseDir);
                    }
                } catch (Exception e) {
                    logger.debug("Failed to resolve reference: {}", refUri, e);
                }
            }

        } catch (Exception e) {
            logger.debug("Failed to collect schema definitions from: {}", xmlPath, e);
        }
    }

    /**
     * Parse XML content manually to extract schema element definitions
     */
    private void parseSchemaDefinitions(String xmlContent, String sourceInfo, Map<String, List<String>> allDefinitions) {
        try {
            // Use simple regex patterns to extract schema elements (avoiding full XML parsing)
            // This approach works because we only need to detect duplicates, not full validation

            // Extract namespace from Schema element
            String namespacePattern = "<Schema[^>]*Namespace\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>";
            java.util.regex.Pattern schemaPattern = java.util.regex.Pattern.compile(namespacePattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher schemaMatcher = schemaPattern.matcher(xmlContent);

            while (schemaMatcher.find()) {
                String namespace = schemaMatcher.group(1);

                // Find schema end to limit search scope
                int schemaStart = schemaMatcher.start();
                int schemaEnd = findSchemaEnd(xmlContent, schemaStart);
                String schemaContent = xmlContent.substring(schemaStart, schemaEnd);

                // Extract EntityTypes
                extractElementDefinitions(schemaContent, "EntityType", namespace, sourceInfo, allDefinitions);

                // Extract ComplexTypes
                extractElementDefinitions(schemaContent, "ComplexType", namespace, sourceInfo, allDefinitions);

                // Extract EnumTypes
                extractElementDefinitions(schemaContent, "EnumType", namespace, sourceInfo, allDefinitions);

                // Extract TypeDefinitions
                extractElementDefinitions(schemaContent, "Action", namespace, sourceInfo, allDefinitions);

                // Extract Functions
                extractElementDefinitions(schemaContent, "Function", namespace, sourceInfo, allDefinitions);

                // Extract EntityContainer elements
                extractEntityContainerElements(schemaContent, namespace, sourceInfo, allDefinitions);
            }

        } catch (Exception e) {
            logger.debug("Failed to parse schema definitions", e);
        }
    }

    /**
     * Extract element definitions using regex
     */
    private void extractElementDefinitions(String schemaContent, String elementType, String namespace,
                                         String sourceInfo, Map<String, List<String>> allDefinitions) {
        String pattern = "<" + elementType + "[^>]*Name\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>";
        java.util.regex.Pattern elementPattern = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = elementPattern.matcher(schemaContent);

        while (matcher.find()) {
            String elementName = matcher.group(1);
            String fullName = namespace + "." + elementName;
            String key = elementType + ":" + fullName;

            allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
        }
    }

    /**
     * Extract EntityContainer elements
     */
    private void extractEntityContainerElements(String schemaContent, String namespace, String sourceInfo,
                                              Map<String, List<String>> allDefinitions) {
        // Find EntityContainer
        String containerPattern = "<EntityContainer[^>]*Name\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(containerPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(schemaContent);

        while (matcher.find()) {
            String containerName = matcher.group(1);
            int containerStart = matcher.start();
            int containerEnd = findElementEnd(schemaContent, containerStart, "EntityContainer");
            String containerContent = schemaContent.substring(containerStart, containerEnd);

            // Extract EntitySets
            extractContainerElements(containerContent, "EntitySet", namespace, containerName, sourceInfo, allDefinitions);

            // Extract Singletons
            extractContainerElements(containerContent, "Singleton", namespace, containerName, sourceInfo, allDefinitions);

            // Extract ActionImports
            extractContainerElements(containerContent, "ActionImport", namespace, containerName, sourceInfo, allDefinitions);

            // Extract FunctionImports
            extractContainerElements(containerContent, "FunctionImport", namespace, containerName, sourceInfo, allDefinitions);
        }
    }

    /**
     * Extract elements within EntityContainer
     */
    private void extractContainerElements(String containerContent, String elementType, String namespace,
                                        String containerName, String sourceInfo, Map<String, List<String>> allDefinitions) {
        String pattern = "<" + elementType + "[^>]*Name\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>";
        java.util.regex.Pattern elementPattern = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = elementPattern.matcher(containerContent);

        while (matcher.find()) {
            String elementName = matcher.group(1);
            String fullName = namespace + "." + containerName + "." + elementName;
            String key = elementType + ":" + fullName;

            allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
        }
    }

    /**
     * Find the end of a schema element
     */
    private int findSchemaEnd(String xmlContent, int schemaStart) {
        int depth = 0;
        boolean inTag = false;
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = schemaStart; i < xmlContent.length(); i++) {
            char c = xmlContent.charAt(i);

            if (!inQuote) {
                if (c == '"' || c == '\'') {
                    inQuote = true;
                    quoteChar = c;
                } else if (c == '<') {
                    inTag = true;
                    if (i + 1 < xmlContent.length() && xmlContent.charAt(i + 1) == '/') {
                        // Closing tag
                        if (xmlContent.substring(i).startsWith("</Schema")) {
                            if (depth == 0) {
                                return Math.min(i + 9, xmlContent.length()); // Include </Schema>
                            }
                            depth--;
                        }
                    } else if (xmlContent.substring(i).startsWith("<Schema")) {
                        depth++;
                    }
                } else if (c == '>' && inTag) {
                    inTag = false;
                }
            } else if (c == quoteChar) {
                inQuote = false;
            }
        }

        return xmlContent.length();
    }

    /**
     * Find the end of an element
     */
    private int findElementEnd(String content, int start, String elementName) {
        // Simple implementation - find matching closing tag
        String closingTag = "</" + elementName;
        int pos = content.indexOf(closingTag, start);
        if (pos != -1) {
            return content.indexOf('>', pos) + 1;
        }
        return content.length();
    }

    /**
     * Extract references from XML content
     */
    private List<String> extractReferences(String xmlContent) {
        List<String> references = new ArrayList<>();

        // Updated pattern to handle edmx:Reference (with namespace prefix)
        String pattern = "<(?:edmx:)?Reference[^>]*Uri\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>";
        java.util.regex.Pattern refPattern = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = refPattern.matcher(xmlContent);

        while (matcher.find()) {
            String uri = matcher.group(1);
            references.add(uri);
            logger.debug("Found reference: {}", uri);
        }

        return references;
    }

    /**
     * Resolve reference URI to file path
     */
    private Path resolveReference(String refUri, Path baseDir) {
        try {
            URI uri = URI.create(refUri);
            if (uri.isAbsolute()) {
                return Paths.get(uri);
            } else {
                return baseDir.resolve(refUri).normalize();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check for conflicts in collected definitions
     */
    private void checkElementConflicts(Map<String, List<String>> allDefinitions, List<String> errors) {
        for (Map.Entry<String, List<String>> entry : allDefinitions.entrySet()) {
            if (entry.getValue().size() > 1) {
                String key = entry.getKey();
                List<String> sources = entry.getValue();

                String[] parts = key.split(":", 2);
                String elementType = parts[0];
                String fullName = parts[1];

                errors.add("Conflicting " + elementType + " name: " + fullName +
                          " (defined in " + sources.size() + " locations: " +
                          String.join(", ", sources) + ")");
            }
        }
    }

    /**
     * Validate OData namespace according to OData 4 naming rules.
     * OData 4.0: Namespace = one or more dot-separated identifiers, each identifier must start with a letter or underscore, followed by letters, digits, or underscores.
     * No leading/trailing dot, no consecutive dots, no special chars or spaces.
     */
    private boolean isValidODataNamespace(String namespace) {
        if (namespace == null || namespace.trim().isEmpty()) {
            return false;
        }
        return namespace.matches("^([A-Za-z_][A-ZaZ0-9_]*)(\\.[A-Za-z_][A-ZaZ0-9_]*)*$");
    }

    /**
     * Validate EntityType using Olingo structures
     */
    private void validateEntityType(CsdlEntityType entityType, List<String> errors,
                                   List<String> warnings, Set<String> referencedNamespaces, Set<String> importedNamespaces) {

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
            extractAndValidateTypeReference(baseType, referencedNamespaces, errors, importedNamespaces);
        }

        // Validate properties
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                validateProperty(property, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }

        // Validate navigation properties
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                validateNavigationProperty(navProp, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }
    }

    /**
     * Validate ComplexType using Olingo structures
     */
    private void validateComplexType(CsdlComplexType complexType, List<String> errors,
                                    List<String> warnings, Set<String> referencedNamespaces, Set<String> importedNamespaces) {

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
            extractAndValidateTypeReference(baseType, referencedNamespaces, errors, importedNamespaces);
        }

        // Validate properties
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                validateProperty(property, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }
    }

    /**
     * Validate EnumType using Olingo structures
     */
    private void validateEnumType(CsdlEnumType enumType, List<String> errors, List<String> warnings,
                                 Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (enumType.getName() == null || enumType.getName().trim().isEmpty()) {
            errors.add("EnumType must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(enumType.getName())) {
            errors.add("Invalid EnumType name: " + enumType.getName());
        }

        // Validate underlying type if specified
        if (enumType.getUnderlyingType() != null) {
            extractAndValidateTypeReference(enumType.getUnderlyingType(), referencedNamespaces, errors, importedNamespaces);
        }
    }

    /**
     * Validate Action using Olingo structures
     */
    private void validateAction(CsdlAction action, List<String> errors, List<String> warnings,
                               Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (action.getName() == null || action.getName().trim().isEmpty()) {
            errors.add("Action must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(action.getName())) {
            errors.add("Invalid Action name: " + action.getName());
        }

        // Validate parameters
        if (action.getParameters() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlParameter parameter : action.getParameters()) {
                validateParameter(parameter, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }

        // Validate return type
        if (action.getReturnType() != null) {
            extractAndValidateTypeReference(action.getReturnType().getType(), referencedNamespaces, errors, importedNamespaces);
        }
    }

    /**
     * Validate Function using Olingo structures
     */
    private void validateFunction(CsdlFunction function, List<String> errors, List<String> warnings,
                                 Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (function.getName() == null || function.getName().trim().isEmpty()) {
            errors.add("Function must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(function.getName())) {
            errors.add("Invalid Function name: " + function.getName());
        }

        // Validate parameters
        if (function.getParameters() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlParameter parameter : function.getParameters()) {
                validateParameter(parameter, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }

        // Validate return type (required for functions)
        if (function.getReturnType() == null) {
            errors.add("Function " + function.getName() + " must have a return type");
        } else {
            extractAndValidateTypeReference(function.getReturnType().getType(), referencedNamespaces, errors, importedNamespaces);
        }
    }

    /**
     * Validate Parameter using Olingo structures
     */
    private void validateParameter(org.apache.olingo.commons.api.edm.provider.CsdlParameter parameter,
                                  List<String> errors, List<String> warnings,
                                  Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (parameter.getName() == null || parameter.getName().trim().isEmpty()) {
            errors.add("Parameter must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(parameter.getName())) {
            errors.add("Invalid Parameter name: " + parameter.getName());
        }

        // Validate parameter type
        if (parameter.getType() != null) {
            extractAndValidateTypeReference(parameter.getType(), referencedNamespaces, errors, importedNamespaces);
        } else {
            errors.add("Parameter " + parameter.getName() + " must have a type");
        }

        // Validate inline annotations on Parameter
        validateInlineAnnotations(parameter.getAnnotations(), errors, importedNamespaces);
    }

    /**
     * Validate Property using Olingo structures
     */
    private void validateProperty(CsdlProperty property, List<String> errors, List<String> warnings, Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (property.getName() == null || property.getName().trim().isEmpty()) {
            errors.add("Property must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(property.getName())) {
            errors.add("Invalid Property name: " + property.getName());
        }

        // Validate property type
        if (property.getType() != null) {
            extractAndValidateTypeReference(property.getType(), referencedNamespaces, errors, importedNamespaces);
        } else {
            errors.add("Property " + property.getName() + " must have a type");
        }

        // Validate inline annotations on Property
        validateInlineAnnotations(property.getAnnotations(), errors, importedNamespaces);
    }

    /**
     * Validate Annotations using both Olingo structures and manual XML parsing
     */
    private void validateAnnotations(CsdlSchema schema, List<String> errors, List<String> warnings,
                                   Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        System.out.println("DEBUG: validateAnnotations called for schema: " + schema.getNamespace());

        // Check annotation groups (Annotations elements)
        if (schema.getAnnotationGroups() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlAnnotations annotations : schema.getAnnotationGroups()) {
                String target = annotations.getTarget();
                if (target != null && !target.trim().isEmpty()) {
                    validateAnnotationTarget(target, schema, errors, referencedNamespaces, importedNamespaces);
                }

                // Validate individual annotations within the group
                if (annotations.getAnnotations() != null) {
                    System.out.println("DEBUG: Found " + annotations.getAnnotations().size() + " annotations in group");
                    for (org.apache.olingo.commons.api.edm.provider.CsdlAnnotation annotation : annotations.getAnnotations()) {
                        String termName = annotation.getTerm();
                        System.out.println("DEBUG: Validating annotation term: " + termName);
                        validateAnnotationTerm(annotation.getTerm(), errors, importedNamespaces);
                    }
                }
            }
        }

        // Check inline annotations on entity types
        if (schema.getEntityTypes() != null) {
            System.out.println("DEBUG: Checking inline annotations on " + schema.getEntityTypes().size() + " entity types");
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                System.out.println("DEBUG: Checking entity type: " + entityType.getName());
                validateInlineAnnotations(entityType.getAnnotations(), errors, importedNamespaces);

                // Check annotations on properties
                if (entityType.getProperties() != null) {
                    System.out.println("DEBUG: Checking " + entityType.getProperties().size() + " properties");
                    for (CsdlProperty property : entityType.getProperties()) {
                        System.out.println("DEBUG: Checking property: " + property.getName());
                        validateInlineAnnotations(property.getAnnotations(), errors, importedNamespaces);
                    }
                }

                // Check annotations on navigation properties
                if (entityType.getNavigationProperties() != null) {
                    for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                        validateInlineAnnotations(navProp.getAnnotations(), errors, importedNamespaces);
                    }
                }
            }
        }

        // Check inline annotations on complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                validateInlineAnnotations(complexType.getAnnotations(), errors, importedNamespaces);

                if (complexType.getProperties() != null) {
                    for (CsdlProperty property : complexType.getProperties()) {
                        validateInlineAnnotations(property.getAnnotations(), errors, importedNamespaces);
                    }
                }
            }
        }
    }

    /**
     * Validate inline annotations
     */
    private void validateInlineAnnotations(List<org.apache.olingo.commons.api.edm.provider.CsdlAnnotation> annotations,
                                         List<String> errors, Set<String> importedNamespaces) {
        if (annotations != null && !annotations.isEmpty()) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlAnnotation annotation : annotations) {
                String termName = annotation.getTerm();
                System.out.println("DEBUG: Validating inline annotation term: " + termName);
                validateAnnotationTerm(annotation.getTerm(), errors, importedNamespaces);
            }
        }
    }

    /**
     * Validate annotation term
     */
    private void validateAnnotationTerm(String term, List<String> errors, Set<String> importedNamespaces) {
        if (term == null || term.trim().isEmpty()) {
            errors.add("Annotation term cannot be null or empty");
            return;
        }

        // Check basic format
        if (!isValidAnnotationTermFormat(term)) {
            errors.add("Invalid annotation term format: " + term);
            return;
        }

        // Check if term is from a known vocabulary namespace
        if (!isKnownVocabularyTerm(term, importedNamespaces)) {
            errors.add("Undefined annotation term: " + term);
        }
    }

    /**
     * Validate annotation term format
     */
    private boolean isValidAnnotationTermFormat(String term) {
        if (term == null || term.trim().isEmpty()) {
            return false;
        }

        // Check for invalid characters
        if (term.contains("!") || term.contains("?") || term.contains("<") || term.contains(">")) {
            return false;
        }

        // Term should have at least one dot (namespace.termname)
        if (!term.contains(".")) {
            return false;
        }

        // Validate each segment
        String[] segments = term.split("\\.");
        for (String segment : segments) {
            if (!isValidODataIdentifier(segment)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if term is from a known vocabulary
     */
    private boolean isKnownVocabularyTerm(String term, Set<String> importedNamespaces) {
        // Known OData vocabularies
        Set<String> knownVocabularies = new HashSet<>();
        knownVocabularies.add("Core");
        knownVocabularies.add("Measures");
        knownVocabularies.add("Capabilities");
        knownVocabularies.add("Validation");
        knownVocabularies.add("UI");
        knownVocabularies.add("Common");
        knownVocabularies.add("Communication");
        knownVocabularies.add("PersonalData");
        knownVocabularies.add("Analytics");
        knownVocabularies.add("Aggregation");
        knownVocabularies.add("Authorization");
        knownVocabularies.add("Session");
        knownVocabularies.add("Temporal");

        // Check if term starts with a known vocabulary
        for (String vocab : knownVocabularies) {
            if (term.startsWith(vocab + ".")) {
                return true;
            }
        }

        // Check imported namespaces (assume they contain valid vocabularies)
        for (String namespace : importedNamespaces) {
            if (term.startsWith(namespace + ".")) {
                return true;
            }
        }

        // If not from known vocabularies, it's likely undefined
        return false;
    }

    /**
     * Validate individual annotation target
     */
    private void validateAnnotationTarget(String target, CsdlSchema schema, List<String> errors,
                                        Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        try {
            // Basic format validation
            if (!isValidAnnotationTargetFormat(target)) {
                errors.add("Invalid annotation target format: " + target);
                return;
            }

            // Parse target path
            if (target.contains("/")) {
                // Property or navigation property target (e.g., "Namespace.EntityType/PropertyName")
                String[] parts = target.split("/", 2);
                String entityPath = parts[0];
                String propertyName = parts[1];

                if (!validateEntityTypeExists(entityPath, schema, referencedNamespaces, importedNamespaces)) {
                    errors.add("Annotation target references non-existent entity type: " + entityPath);
                }

                if (!validatePropertyExists(entityPath, propertyName, schema, referencedNamespaces, importedNamespaces)) {
                    errors.add("Annotation target references non-existent property: " + target);
                }
            } else {
                // Entity type or other top-level target
                if (!validateEntityTypeExists(target, schema, referencedNamespaces, importedNamespaces)) {
                    errors.add("Annotation target references non-existent type: " + target);
                }
            }
        } catch (Exception e) {
            errors.add("Error validating annotation target '" + target + "': " + e.getMessage());
        }
    }

    /**
     * Validate annotation target format
     */
    private boolean isValidAnnotationTargetFormat(String target) {
        if (target == null || target.trim().isEmpty()) {
            return false;
        }

        // Check for invalid characters
        if (target.contains("!") || target.contains("?") || target.contains("<") || target.contains(">")) {
            return false;
        }

        // Basic namespace and identifier validation
        String[] parts = target.split("/");
        for (String part : parts) {
            if (!isValidODataPath(part.trim())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validate OData path component
     */
    private boolean isValidODataPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        // Should contain at least one dot for namespace.identifier format
        if (!path.contains(".")) {
            return false;
        }

        String[] segments = path.split("\\.");
        for (String segment : segments) {
            if (!isValidODataIdentifier(segment)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if entity type exists in current schema or imported namespaces
     */
    private boolean validateEntityTypeExists(String typePath, CsdlSchema schema,
                                           Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        // Check current schema
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = schema.getNamespace() + "." + entityType.getName();
                if (fullName.equals(typePath)) {
                    return true;
                }
            }
        }

        // Check complex types as well
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullName = schema.getNamespace() + "." + complexType.getName();
                if (fullName.equals(typePath)) {
                    return true;
                }
            }
        }

        // For now, assume types in imported namespaces exist
        // In a full implementation, we would need to resolve and check those schemas
        for (String namespace : importedNamespaces) {
            if (typePath.startsWith(namespace + ".")) {
                return true; // Assume imported types are valid
            }
        }

        return false;
    }

    /**
     * Check if property exists on the specified entity type
     */
    private boolean validatePropertyExists(String typePath, String propertyName, CsdlSchema schema,
                                         Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        // Find the entity type first
        CsdlEntityType entityType = null;
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType et : schema.getEntityTypes()) {
                String fullName = schema.getNamespace() + "." + et.getName();
                if (fullName.equals(typePath)) {
                    entityType = et;
                    break;
                }
            }
        }

        if (entityType == null) {
            // Check complex types
            CsdlComplexType complexType = null;
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType ct : schema.getComplexTypes()) {
                    String fullName = schema.getNamespace() + "." + ct.getName();
                    if (fullName.equals(typePath)) {
                        complexType = ct;
                        break;
                    }
                }
            }

            if (complexType != null) {
                // Check properties in complex type
                if (complexType.getProperties() != null) {
                    for (CsdlProperty prop : complexType.getProperties()) {
                        if (propertyName.equals(prop.getName())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        // Check regular properties
        if (entityType.getProperties() != null) {
            for (CsdlProperty prop : entityType.getProperties()) {
                if (propertyName.equals(prop.getName())) {
                    return true;
                }
            }
        }

        // Check navigation properties
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                if (propertyName.equals(navProp.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Validate NavigationProperty using Olingo structures
     */
    private void validateNavigationProperty(CsdlNavigationProperty navProp, List<String> errors,
                                          List<String> warnings, Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (navProp.getName() == null || navProp.getName().trim().isEmpty()) {
            errors.add("NavigationProperty must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(navProp.getName())) {
            errors.add("Invalid NavigationProperty name: " + navProp.getName());
        }

        // Validate navigation property type
        if (navProp.getType() != null) {
            extractAndValidateTypeReference(navProp.getType(), referencedNamespaces, warnings, importedNamespaces);
        } else {
            errors.add("NavigationProperty " + navProp.getName() + " must have a type");
        }

        // Validate inline annotations on NavigationProperty
        validateInlineAnnotations(navProp.getAnnotations(), errors, importedNamespaces);
    }

    /**
     * Validate EntityContainer using Olingo structures
     */
    private void validateEntityContainer(org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer container,
                                        List<String> errors, List<String> warnings,
                                        Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (container.getName() == null || container.getName().trim().isEmpty()) {
            errors.add("EntityContainer must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(container.getName())) {
            errors.add("Invalid EntityContainer name: " + container.getName());
        }

        // Validate EntitySets
        if (container.getEntitySets() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEntitySet entitySet : container.getEntitySets()) {
                validateEntitySet(entitySet, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }

        // Validate Singletons
        if (container.getSingletons() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlSingleton singleton : container.getSingletons()) {
                validateSingleton(singleton, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }

        // Validate ActionImports
        if (container.getActionImports() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlActionImport actionImport : container.getActionImports()) {
                validateActionImport(actionImport, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }

        // Validate FunctionImports
        if (container.getFunctionImports() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport functionImport : container.getFunctionImports()) {
                validateFunctionImport(functionImport, errors, warnings, referencedNamespaces, importedNamespaces);
            }
        }
    }

    /**
     * Validate EntitySet using Olingo structures
     */
    private void validateEntitySet(org.apache.olingo.commons.api.edm.provider.CsdlEntitySet entitySet,
                                  List<String> errors, List<String> warnings,
                                  Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (entitySet.getName() == null || entitySet.getName().trim().isEmpty()) {
            errors.add("EntitySet must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(entitySet.getName())) {
            errors.add("Invalid EntitySet name: " + entitySet.getName());
        }

        // Validate EntityType reference
        if (entitySet.getType() != null) {
            extractAndValidateTypeReference(entitySet.getType(), referencedNamespaces, errors, importedNamespaces);
        } else {
            errors.add("EntitySet " + entitySet.getName() + " must have a type");
        }

        // Validate inline annotations on EntitySet
        validateInlineAnnotations(entitySet.getAnnotations(), errors, importedNamespaces);
    }

    /**
     * Validate Singleton using Olingo structures
     */
    private void validateSingleton(org.apache.olingo.commons.api.edm.provider.CsdlSingleton singleton,
                                  List<String> errors, List<String> warnings,
                                  Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (singleton.getName() == null || singleton.getName().trim().isEmpty()) {
            errors.add("Singleton must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(singleton.getName())) {
            errors.add("Invalid Singleton name: " + singleton.getName());
        }

        // Validate EntityType reference
        if (singleton.getType() != null) {
            extractAndValidateTypeReference(singleton.getType(), referencedNamespaces, errors, importedNamespaces);
        } else {
            errors.add("Singleton " + singleton.getName() + " must have a type");
        }

        // Validate inline annotations on Singleton
        validateInlineAnnotations(singleton.getAnnotations(), errors, importedNamespaces);
    }

    /**
     * Validate ActionImport using Olingo structures
     */
    private void validateActionImport(org.apache.olingo.commons.api.edm.provider.CsdlActionImport actionImport,
                                     List<String> errors, List<String> warnings,
                                     Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (actionImport.getName() == null || actionImport.getName().trim().isEmpty()) {
            errors.add("ActionImport must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(actionImport.getName())) {
            errors.add("Invalid ActionImport name: " + actionImport.getName());
        }

        // Validate Action reference
        if (actionImport.getAction() != null) {
            // Action reference should be a qualified name
            if (actionImport.getAction().contains(".")) {
                referencedNamespaces.add(actionImport.getAction().substring(0, actionImport.getAction().lastIndexOf('.')));
            }
        } else {
            errors.add("ActionImport " + actionImport.getName() + " must reference an Action");
        }

        // Validate inline annotations on ActionImport
        validateInlineAnnotations(actionImport.getAnnotations(), errors, importedNamespaces);
    }

    /**
     * Validate FunctionImport using Olingo structures
     */
    private void validateFunctionImport(org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport functionImport,
                                       List<String> errors, List<String> warnings,
                                       Set<String> referencedNamespaces, Set<String> importedNamespaces) {
        if (functionImport.getName() == null || functionImport.getName().trim().isEmpty()) {
            errors.add("FunctionImport must have a valid name");
            return;
        }

        if (!isValidODataIdentifier(functionImport.getName())) {
            errors.add("Invalid FunctionImport name: " + functionImport.getName());
        }

        // Validate Function reference
        if (functionImport.getFunction() != null) {
            // Function reference should be a qualified name
            if (functionImport.getFunction().contains(".")) {
                referencedNamespaces.add(functionImport.getFunction().substring(0, functionImport.getFunction().lastIndexOf('.')));
            }
        } else {
            errors.add("FunctionImport " + functionImport.getName() + " must reference a Function");
        }

        // Validate inline annotations on FunctionImport
        validateInlineAnnotations(functionImport.getAnnotations(), errors, importedNamespaces);
    }

    /**
     * Extract and validate type references (handles Collection(Type) and namespace references)
     */
    private void extractAndValidateTypeReference(String typeRef, Set<String> referencedNamespaces, List<String> errors, Set<String> importedNamespaces) {
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
            validatePrimitiveType(actualType, errors);
            return;
        }

        // Extract namespace from qualified type name
        if (actualType.contains(".")) {
            String namespace = actualType.substring(0, actualType.lastIndexOf('.'));
            referencedNamespaces.add(namespace);

            if (!namespace.equals("Edm") && !importedNamespaces.contains(namespace)) {
                errors.add("Namespace " + namespace + " is referenced but not imported in the schema");
            }
        }
    }

    /**
     * Validate primitive type using Olingo's EdmPrimitiveTypeKind
     */
    private void validatePrimitiveType(String primitiveType, List<String> errors) {
        try {
            String typeName = primitiveType.substring(4); // Remove "Edm." prefix
            EdmPrimitiveTypeKind.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            errors.add("Unknown primitive type: " + primitiveType);
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
}
