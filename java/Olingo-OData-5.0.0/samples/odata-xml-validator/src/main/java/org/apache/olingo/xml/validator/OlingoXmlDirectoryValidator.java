package org.apache.olingo.xml.validator;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Olingo-based implementation of XmlDirectoryValidator
 * 
 * This implementation uses basic XML parsing capabilities
 * to validate OData 4.0 XML schema files and manage schema dependencies.
 */
public class OlingoXmlDirectoryValidator implements XmlDirectoryValidator {

    private static final Logger logger = LoggerFactory.getLogger(OlingoXmlDirectoryValidator.class);
    
    private final SchemaRepository schemaRepository;

    /**
     * Constructor
     */
    public OlingoXmlDirectoryValidator() {
        this.schemaRepository = new SchemaRepository();
    }

    @Override
    public ValidationResult validateDirectory(Path directoryPath) {
        logger.info("Starting validation of directory: {}", directoryPath);
        
        long startTime = System.currentTimeMillis();
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();

        try {
            // Find all XML files in the directory tree
            List<Path> xmlFiles = findXmlFiles(directoryPath);
            logger.info("Found {} XML files to validate", xmlFiles.size());

            if (xmlFiles.isEmpty()) {
                warnings.add(ValidationWarning.of(ValidationWarning.WarningType.FILE_NAMING_WARNING, "No XML files found in directory", directoryPath));
                return ValidationResult.withErrorsAndWarnings(errors, warnings, directoryPath, 
                    System.currentTimeMillis() - startTime);
            }

            // Phase 1: Parse all schemas and collect them
            Map<Path, SchemaParseResult> parseResults = new HashMap<>();
            for (Path xmlFile : xmlFiles) {
                SchemaParseResult parseResult = parseSchemaFile(xmlFile);
                parseResults.put(xmlFile, parseResult);
                
                if (!parseResult.isValid()) {
                    errors.addAll(parseResult.getErrors());
                } else {
                    // Add successfully parsed schemas to repository
                    for (CsdlSchema schema : parseResult.getSchemas()) {
                        try {
                            schemaRepository.addSchema(schema, xmlFile);
                        } catch (Exception e) {
                            errors.add(ValidationError.of(ValidationError.ErrorType.SCHEMA_STRUCTURE_ERROR,
                                "Failed to add schema to repository: " + e.getMessage(), xmlFile));
                        }
                    }
                }
            }

            // Phase 2: Validate cross-references and dependencies
            for (Map.Entry<Path, SchemaParseResult> entry : parseResults.entrySet()) {
                Path filePath = entry.getKey();
                SchemaParseResult parseResult = entry.getValue();
                
                if (parseResult.isValid()) {
                    for (CsdlSchema schema : parseResult.getSchemas()) {
                        List<ValidationError> referenceErrors = validateSchemaReferences(schema, filePath);
                        errors.addAll(referenceErrors);
                    }
                }
            }

            // Phase 3: Check for circular dependencies
            List<ValidationError> circularErrors = detectCircularDependencies();
            errors.addAll(circularErrors);

            boolean isValid = errors.isEmpty();
            logger.info("Validation completed. Valid: {}, Errors: {}, Warnings: {}", 
                       isValid, errors.size(), warnings.size());

            return ValidationResult.withErrorsAndWarnings(errors, warnings, directoryPath, 
                System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            logger.error("Unexpected error during directory validation", e);
            errors.add(ValidationError.of(ValidationError.ErrorType.FILE_ACCESS_ERROR,
                "Unexpected error: " + e.getMessage(), directoryPath));

            return ValidationResult.withErrorsAndWarnings(errors, warnings, directoryPath, 
                System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public ValidationResult validateXmlFile(Path xmlFilePath) {
        logger.info("Validating single XML file: {}", xmlFilePath);
        
        long startTime = System.currentTimeMillis();
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();

        SchemaParseResult parseResult = parseSchemaFile(xmlFilePath);
        if (!parseResult.isValid()) {
            errors.addAll(parseResult.getErrors());
        }

        return ValidationResult.withErrorsAndWarnings(errors, warnings, xmlFilePath, 
            System.currentTimeMillis() - startTime);
    }

    @Override
    public SchemaRepository buildSchemaRepository(Path directoryPath) {
        logger.info("Building schema repository from directory: {}", directoryPath);
        
        SchemaRepository repository = new SchemaRepository();
        
        try {
            List<Path> xmlFiles = findXmlFiles(directoryPath);
            
            for (Path xmlFile : xmlFiles) {
                SchemaParseResult parseResult = parseSchemaFile(xmlFile);
                
                if (parseResult.isValid()) {
                    for (CsdlSchema schema : parseResult.getSchemas()) {
                        try {
                            repository.addSchema(schema, xmlFile);
                        } catch (Exception e) {
                            logger.warn("Failed to add schema from {} to repository: {}", xmlFile, e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan directory for XML files: {}", e.getMessage());
        }
        
        return repository;
    }

    @Override
    public ValidationResult validateWithRepository(Path xmlFilePath, SchemaRepository repository) {
        logger.info("Validating XML file {} with external repository", xmlFilePath);
        
        long startTime = System.currentTimeMillis();
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();

        SchemaParseResult parseResult = parseSchemaFile(xmlFilePath);
        if (!parseResult.isValid()) {
            errors.addAll(parseResult.getErrors());
        } else {
            // Validate against external repository
            for (CsdlSchema schema : parseResult.getSchemas()) {
                List<ValidationError> referenceErrors = validateSchemaReferencesWithRepository(schema, xmlFilePath, repository);
                errors.addAll(referenceErrors);
            }
        }

        return ValidationResult.withErrorsAndWarnings(errors, warnings, xmlFilePath, 
            System.currentTimeMillis() - startTime);
    }

    /**
     * Find all XML files in the directory tree
     */
    private List<Path> findXmlFiles(Path directoryPath) throws IOException {
        List<Path> xmlFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(directoryPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                 .forEach(xmlFiles::add);
        }
        
        return xmlFiles;
    }

    /**
     * Parse a single schema file using basic XML parsing
     */
    private SchemaParseResult parseSchemaFile(Path filePath) {
        logger.debug("Parsing schema file: {}", filePath);
        
        try {
            // Read file content (Java 8 compatible)
            byte[] bytes = Files.readAllBytes(filePath);
            String content = new String(bytes, StandardCharsets.UTF_8);
            
            // Basic XML syntax validation and parsing
            Document document = parseXmlDocument(content);
            if (document == null) {
                return SchemaParseResult.error(
                    ValidationError.of(ValidationError.ErrorType.XML_FORMAT_ERROR,
                        "Invalid XML syntax", filePath)
                );
            }

            // Extract schema information from XML
            List<CsdlSchema> schemas = extractSchemasFromDocument(document, filePath);
            
            if (schemas.isEmpty()) {
                return SchemaParseResult.error(
                    ValidationError.of(ValidationError.ErrorType.SCHEMA_STRUCTURE_ERROR,
                        "No valid schemas found in file", filePath)
                );
            }
            
            logger.debug("Successfully parsed {} schemas from {}", schemas.size(), filePath);
            return SchemaParseResult.success(schemas);
            
        } catch (IOException e) {
            logger.debug("IO error reading file {}: {}", filePath, e.getMessage());
            return SchemaParseResult.error(
                ValidationError.of(ValidationError.ErrorType.FILE_ACCESS_ERROR,
                    "Failed to read file: " + e.getMessage(), filePath)
            );
        } catch (Exception e) {
            logger.debug("Unexpected error parsing file {}: {}", filePath, e.getMessage());
            return SchemaParseResult.error(
                ValidationError.of(ValidationError.ErrorType.FILE_ACCESS_ERROR,
                    "Unexpected parsing error: " + e.getMessage(), filePath)
            );
        }
    }

    /**
     * Parse XML document using basic DOM parser
     */
    private Document parseXmlDocument(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract schema information from XML document (simplified)
     */
    private List<CsdlSchema> extractSchemasFromDocument(Document document, Path filePath) {
        List<CsdlSchema> schemas = new ArrayList<>();
        
        try {
            Element root = document.getDocumentElement();
            
            // Look for Schema elements (simplified - just checking basic structure)
            NodeList schemaNodes = root.getElementsByTagNameNS("*", "Schema");
            if (schemaNodes.getLength() == 0) {
                // Try without namespace
                schemaNodes = root.getElementsByTagName("Schema");
            }
            
            for (int i = 0; i < schemaNodes.getLength(); i++) {
                Element schemaElement = (Element) schemaNodes.item(i);
                String namespace = schemaElement.getAttribute("Namespace");
                
                if (namespace != null && !namespace.trim().isEmpty()) {
                    // Create a minimal CsdlSchema (this is simplified)
                    // In a real implementation, you'd need to fully parse the schema
                    CsdlSchema schema = new CsdlSchema();
                    schema.setNamespace(namespace.trim());
                    
                    // Note: This is a simplified approach
                    // A full implementation would parse all entity types, complex types, etc.
                    
                    schemas.add(schema);
                    logger.debug("Found schema with namespace: {}", namespace);
                }
            }
        } catch (Exception e) {
            logger.warn("Error extracting schemas from {}: {}", filePath, e.getMessage());
        }
        
        return schemas;
    }

    /**
     * Validate schema references and dependencies
     */
    private List<ValidationError> validateSchemaReferences(CsdlSchema schema, Path filePath) {
        return validateSchemaReferencesWithRepository(schema, filePath, schemaRepository);
    }

    /**
     * Validate schema references against a specific repository
     */
    private List<ValidationError> validateSchemaReferencesWithRepository(CsdlSchema schema, Path filePath, SchemaRepository repository) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Check entity type inheritance
        if (schema.getEntityTypes() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEntityType entityType : schema.getEntityTypes()) {
                if (entityType.getBaseType() != null) {
                    if (!repository.typeExists(entityType.getBaseType())) {
                        errors.add(ValidationError.of(ValidationError.ErrorType.TYPE_REFERENCE_ERROR,
                            "Base type not found: " + entityType.getBaseType(), filePath));
                    }
                }
            }
        }

        // Check complex type inheritance  
        if (schema.getComplexTypes() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlComplexType complexType : schema.getComplexTypes()) {
                if (complexType.getBaseType() != null) {
                    if (!repository.typeExists(complexType.getBaseType())) {
                        errors.add(ValidationError.of(ValidationError.ErrorType.TYPE_REFERENCE_ERROR,
                            "Base complex type not found: " + complexType.getBaseType(), filePath));
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Convert circular dependencies to validation errors
     */
    private List<ValidationError> detectCircularDependencies() {
        List<ValidationError> errors = new ArrayList<>();
        
        if (schemaRepository.hasCircularDependencies()) {
            List<List<String>> chains = schemaRepository.getCircularDependencyChains();
            for (List<String> chain : chains) {
                String chainStr = String.join(" -> ", chain);
                errors.add(ValidationError.of(ValidationError.ErrorType.CIRCULAR_DEPENDENCY,
                    "Circular dependency detected: " + chainStr, 
                    schemaRepository.getFilePath(chain.get(0))));
            }
        }
        
        return errors;
    }

    public SchemaRepository getSchemaRepository() {
        return schemaRepository;
    }

    /**
     * Helper class to hold schema parsing results
     */
    private static class SchemaParseResult {
        private final boolean valid;
        private final List<CsdlSchema> schemas;
        private final List<ValidationError> errors;

        private SchemaParseResult(boolean valid, List<CsdlSchema> schemas, List<ValidationError> errors) {
            this.valid = valid;
            this.schemas = schemas != null ? schemas : Collections.emptyList();
            this.errors = errors != null ? errors : Collections.emptyList();
        }

        public static SchemaParseResult success(List<CsdlSchema> schemas) {
            return new SchemaParseResult(true, schemas, null);
        }

        public static SchemaParseResult error(ValidationError error) {
            return new SchemaParseResult(false, null, Collections.singletonList(error));
        }

        public boolean isValid() {
            return valid;
        }

        public List<CsdlSchema> getSchemas() {
            return schemas;
        }

        public List<ValidationError> getErrors() {
            return errors;
        }
    }
}
