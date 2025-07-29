package org.apache.olingo.schema.processor.validation.framework;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.schema.processor.validation.XmlComplianceResult;
import org.apache.olingo.schema.processor.validation.XmlFileComplianceValidator;
import org.apache.olingo.schema.processor.validation.core.*;
import org.apache.olingo.schema.processor.validation.impl.*;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.ReferenceResolver;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Refactored and modular implementation of OData XML file compliance validator.
 * This class serves as the main entry point and coordinates various specialized validators.
 */
public class ModularOlingoXmlValidator implements XmlFileComplianceValidator {

    private static final Logger logger = LoggerFactory.getLogger(ModularOlingoXmlValidator.class);

    private final NamingValidator namingValidator;
    private final SchemaValidator schemaValidator;
    private final SchemaReferenceResolver schemaReferenceResolver;
    private final DuplicateChecker duplicateChecker;

    public ModularOlingoXmlValidator() {
        this.namingValidator = new ODataNamingValidator();
        this.schemaValidator = new CsdlSchemaValidator(namingValidator);
        this.schemaReferenceResolver = new XmlReferenceResolver();
        this.duplicateChecker = new SchemaDuplicateChecker(schemaReferenceResolver);
    }

    @Override
    public XmlComplianceResult validateFile(File xmlFile) {
        if (xmlFile == null || !xmlFile.exists()) {
            return createErrorResult("File does not exist or is null",
                                   xmlFile != null ? xmlFile.getName() : "null", 0);
        }

        return validateFile(xmlFile.toPath());
    }

    @Override
    public XmlComplianceResult validateFile(Path xmlPath) {
        if (xmlPath == null || !Files.exists(xmlPath)) {
            return createErrorResult("Path does not exist or is null",
                                   xmlPath != null ? xmlPath.getFileName().toString() : "null", 0);
        }

        long startTime = System.currentTimeMillis();
        String fileName = xmlPath.getFileName().toString();

        try {
            return validatePathWithContext(xmlPath);
        } catch (Exception e) {
            logger.error("Failed to read file: {}", xmlPath, e);
            return createErrorResult("Failed to read file: " + e.getMessage(),
                                   fileName, System.currentTimeMillis() - startTime);
        }
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
            return validateInputStream(inputStream, fileName);
        } catch (Exception e) {
            logger.error("Failed to validate content for file: {}", fileName, e);
            return createErrorResult("Failed to validate content: " + e.getMessage(), fileName, 0);
        }
    }

    /**
     * Main validation method using file path to ensure proper reference resolution
     */
    private XmlComplianceResult validatePathWithContext(Path xmlPath) {
        long startTime = System.currentTimeMillis();
        String fileName = xmlPath.getFileName().toString();

        // Initialize validation context
        ValidationContext context = createValidationContext(fileName);

        try {
            // Step 1: Configure Olingo MetadataParser with reference resolution
            SchemaBasedEdmProvider edmProvider = buildEdmProvider(xmlPath, context);

            // Step 2: Collect imported namespaces from references
            collectImportedNamespaces(edmProvider, context);

            // Step 3: Validate all schemas using modular validators
            validateSchemas(edmProvider, context);

            // Step 4: Perform global duplicate checking across all referenced files
            duplicateChecker.checkGlobalSchemaDuplicates(xmlPath, context.getErrors());

            // Step 5: Finalize results
            return createResult(context, startTime);

        } catch (Exception e) {
            logger.error("Validation failed for file: {}", fileName, e);
            categorizeAndAddError(e, context);
            return createResult(context, startTime);
        }
    }

    /**
     * Validation method for input streams (without file context)
     */
    private XmlComplianceResult validateInputStream(InputStream inputStream, String fileName) {
        long startTime = System.currentTimeMillis();

        // Initialize validation context
        ValidationContext context = createValidationContext(fileName);

        try {
            // Configure MetadataParser
            MetadataParser parser = new MetadataParser();
            parser.recursivelyLoadReferences(true);
            parser.parseAnnotations(true);

            // Parse the XML file
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            SchemaBasedEdmProvider edmProvider = parser.buildEdmProvider(reader);

            // Collect imported namespaces and validate schemas
            collectImportedNamespaces(edmProvider, context);
            validateSchemas(edmProvider, context);

            // Note: Skip global duplicate check for input streams since we don't have file path context

            return createResult(context, startTime);

        } catch (Exception e) {
            logger.error("Validation failed for file: {}", fileName, e);
            context.addError("Validation error: " + e.getMessage());
            return createResult(context, startTime);
        }
    }

    /**
     * Build EDM provider with proper reference resolution
     */
    private SchemaBasedEdmProvider buildEdmProvider(Path xmlPath, ValidationContext context) throws Exception {
        MetadataParser parser = new MetadataParser();
        parser.recursivelyLoadReferences(true);
        parser.parseAnnotations(true);
        parser.useLocalCoreVocabularies(false);

        // Get base URI for reference resolution
        URI baseUri = xmlPath.getParent().toUri();

        // Create custom reference resolver
        ReferenceResolver resolver = (uri, baseURI) -> {
            try {
                if (!uri.isAbsolute()) {
                    uri = baseUri.resolve(uri);
                }

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

        // Read XML content and perform structural validation
        String fileContent = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);
        performBasicStructuralValidation(fileContent, context);

        String modifiedContent = addXmlBase(fileContent, baseUri.toString());

        try (StringReader reader = new StringReader(modifiedContent)) {
            return parser.buildEdmProvider(reader);
        }
    }

    /**
     * Perform basic structural validation that Olingo doesn't cover
     */
    private void performBasicStructuralValidation(String xmlContent, ValidationContext context) {
        // Check for missing Key elements in EntityTypes
        String[] lines = xmlContent.split("\n");
        boolean inEntityType = false;
        boolean hasKey = false;
        boolean hasBaseType = false;
        String entityTypeName = "";

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("<EntityType")) {
                inEntityType = true;
                hasKey = false;
                hasBaseType = false;

                // Extract entity type name
                if (trimmedLine.contains("Name=\"")) {
                    int start = trimmedLine.indexOf("Name=\"") + 6;
                    int end = trimmedLine.indexOf("\"", start);
                    if (end > start) {
                        entityTypeName = trimmedLine.substring(start, end);
                    }
                }

                // Check if it has a BaseType
                if (trimmedLine.contains("BaseType=\"")) {
                    hasBaseType = true;
                }
            } else if (inEntityType && trimmedLine.startsWith("<Key>")) {
                hasKey = true;
            } else if (inEntityType && (trimmedLine.startsWith("</EntityType>") || trimmedLine.contains("/>"))) {
                // End of EntityType - check if it's missing a Key
                if (!hasKey && !hasBaseType && !entityTypeName.isEmpty()) {
                    context.addError("EntityType '" + entityTypeName + "' is missing required Key element");
                }
                inEntityType = false;
                entityTypeName = "";
            }
        }
    }

    /**
     * Collect imported namespaces from EDM references
     */
    private void collectImportedNamespaces(SchemaBasedEdmProvider edmProvider, ValidationContext context) {
        List<EdmxReference> references = edmProvider.getReferences();
        if (references != null) {
            context.addMetadata("referenceCount", references.size());
            for (EdmxReference reference : references) {
                if (reference.getIncludes() != null) {
                    reference.getIncludes().forEach(include -> {
                        String namespace = include.getNamespace();
                        context.addImportedNamespace(namespace);
                        context.addReferencedNamespace(namespace);
                    });
                }
            }
        }
    }

    /**
     * Validate all schemas using the modular schema validator
     */
    private void validateSchemas(SchemaBasedEdmProvider edmProvider, ValidationContext context) {
        try {
            List<CsdlSchema> schemas = edmProvider.getSchemas();
            if (schemas != null && !schemas.isEmpty()) {
                for (CsdlSchema schema : schemas) {
                    schemaValidator.validate(schema, context);
                }
                context.addMetadata("schemaCount", schemas.size());
            } else {
                context.addError("No valid schemas found in the XML file");
            }
        } catch (Exception e) {
            context.addError("Failed to retrieve schemas: " + e.getMessage());
            logger.error("Error retrieving schemas from EDM provider", e);
        }
    }

    /**
     * Create validation context with initialized collections
     */
    private ValidationContext createValidationContext(String fileName) {
        return new ValidationContext(
            new ArrayList<>(),      // errors
            new ArrayList<>(),      // warnings
            new HashSet<>(),        // referencedNamespaces
            new HashSet<>(),        // importedNamespaces
            new HashMap<>(),        // metadata
            fileName
        );
    }

    /**
     * Create final validation result
     */
    private XmlComplianceResult createResult(ValidationContext context, long startTime) {
        long validationTime = System.currentTimeMillis() - startTime;
        context.addMetadata("validationTimeMs", validationTime);

        boolean isCompliant = context.getErrors().isEmpty();
        return new XmlComplianceResult(
            isCompliant,
            context.getErrors(),
            context.getWarnings(),
            context.getReferencedNamespaces(),
            context.getMetadata(),
            context.getFileName(),
            validationTime
        );
    }

    /**
     * Create error result for exceptional cases
     */
    private XmlComplianceResult createErrorResult(String errorMessage, String fileName, long validationTime) {
        List<String> errors = Arrays.asList(errorMessage);
        return new XmlComplianceResult(
            false,
            errors,
            new ArrayList<>(),
            new HashSet<>(),
            new HashMap<>(),
            fileName,
            validationTime
        );
    }

    /**
     * Add xml:base attribute to XML content for proper reference resolution
     */
    private String addXmlBase(String xmlContent, String baseUri) {
        String pattern = "(<edmx:Edmx[^>]*)(>)";
        String replacement = "$1 xml:base=\"" + baseUri + "\"$2";
        return xmlContent.replaceFirst(pattern, replacement);
    }


    /**
     * Categorize and add error to the validation context
     */
    private void categorizeAndAddError(Exception e, ValidationContext context) {
        String message = e.getMessage();

        if (message != null) {
            // OData specific errors
            if (message.contains("Malformed FullQualifiedName")) {
                context.addError("Invalid type definition: " + message);
            }
            // XML parsing errors
            else if (message.contains("ParseError") || message.contains("XML")) {
                if (message.contains("元素类型") && message.contains("终止")) {
                    context.addError("Validation error: " + message);
                } else if (message.contains("前言中不允许有内容")) {
                    context.addError("Validation error: " + message);
                } else if (message.contains("字符引用") && message.contains("无效")) {
                    context.addError("Validation error: " + message);
                } else {
                    context.addError("Validation error: " + message);
                }
            }
            // Metadata parsing errors
            else if (message.contains("Failed to read complete metadata file")) {
                context.addError("Validation error: " + message);
            }
            // Namespace or type errors
            else if (message.contains("Cannot invoke") && message.contains("namespaceAndName")) {
                context.addError("Validation error: Missing required attributes");
            }
            // General validation errors
            else {
                context.addError("Validation error: " + message);
            }
        } else {
            // Handle NullPointerException and other exceptions without messages
            if (e instanceof NullPointerException) {
                // Check the stack trace for specific NPE causes
                StackTraceElement[] stackTrace = e.getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    if (element.toString().contains("FullQualifiedName") && element.toString().contains("namespaceAndName")) {
                        context.addError("Validation error: Missing required attributes - namespaceAndName is null");
                        return;
                    }
                }
                context.addError("Validation error: Missing required attributes");
            } else {
                context.addError("Unexpected error: " + e.getClass().getSimpleName());
            }
        }
    }
}
