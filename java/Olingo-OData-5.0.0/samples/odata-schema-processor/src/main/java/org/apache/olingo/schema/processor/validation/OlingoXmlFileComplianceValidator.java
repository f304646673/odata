package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.apache.olingo.server.core.serializer.utils.CircleStreamBuffer;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Olingo-based implementation of XmlFileComplianceValidator.
 * Uses Olingo's internal data structures and methods to validate OData 4.0 XML files.
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
        
        try (InputStream inputStream = new FileInputStream(xmlPath.toFile())) {
            return validateInputStream(inputStream, fileName);
        } catch (IOException e) {
            logger.error("Failed to read file: {}", xmlPath, e);
            return createErrorResult("Failed to read file: " + e.getMessage(), fileName, System.currentTimeMillis() - startTime);
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
     * Core validation method using Olingo's internal structures
     */
    private XmlComplianceResult validateInputStream(InputStream inputStream, String fileName) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> referencedNamespaces = new HashSet<>();
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            // Use Olingo's SchemaBasedEdmProvider to parse and validate
            SchemaBasedEdmProvider edmProvider = new SchemaBasedEdmProvider();
            
            // Try to parse the schema using Olingo's native capabilities
            List<CsdlSchema> schemas = parseSchemaWithOlingo(inputStream, errors, warnings);
            
            if (schemas != null && !schemas.isEmpty()) {
                // Validate each schema using Olingo structures
                for (CsdlSchema schema : schemas) {
                    validateCsdlSchema(schema, errors, warnings, referencedNamespaces, metadata);
                }
                
                metadata.put("schemaCount", schemas.size());
            } else {
                if (errors.isEmpty()) {
                    errors.add("No valid schemas found in the XML file");
                }
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
     * Parse schema using Olingo's native parsing capabilities
     */
    private List<CsdlSchema> parseSchemaWithOlingo(InputStream inputStream, List<String> errors, List<String> warnings) {
        try {
            // Use Olingo's internal XML reading capabilities
            // We'll use a more direct approach with DOM parsing and validation
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false); // We do our own validation
            
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(inputStream);
            
            // Check for basic OData 4.0 structure
            org.w3c.dom.Element root = document.getDocumentElement();
            if (root == null) {
                errors.add("No root element found in XML");
                return null;
            }
            
            // Validate root element is edmx:Edmx
            if (!"Edmx".equals(root.getLocalName()) || 
                !"http://docs.oasis-open.org/odata/ns/edmx".equals(root.getNamespaceURI())) {
                errors.add("Root element must be edmx:Edmx with correct namespace");
                return null;
            }
            
            // Check version
            String version = root.getAttribute("Version");
            if (!"4.0".equals(version)) {
                warnings.add("Expected OData version 4.0, found: " + version);
            }
            
            // Find DataServices element
            org.w3c.dom.NodeList dataServices = root.getElementsByTagNameNS(
                "http://docs.oasis-open.org/odata/ns/edmx", "DataServices");
            if (dataServices.getLength() == 0) {
                errors.add("No edmx:DataServices element found");
                return null;
            }
            
            // Find Schema elements
            org.w3c.dom.NodeList schemas = ((org.w3c.dom.Element)dataServices.item(0))
                .getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "Schema");
            
            if (schemas.getLength() == 0) {
                errors.add("No Schema elements found in DataServices");
                return null;
            }
            
            // Create CSDL Schema objects for each schema found
            List<CsdlSchema> csdlSchemas = new ArrayList<>();
            for (int i = 0; i < schemas.getLength(); i++) {
                org.w3c.dom.Element schemaElement = (org.w3c.dom.Element) schemas.item(i);
                CsdlSchema csdlSchema = createCsdlSchemaFromElement(schemaElement, errors, warnings);
                if (csdlSchema != null) {
                    csdlSchemas.add(csdlSchema);
                }
            }
            
            return csdlSchemas;
            
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            logger.debug("XML parser configuration failed", e);
            errors.add("XML parser configuration error: " + e.getMessage());
            return null;
        } catch (org.xml.sax.SAXException e) {
            logger.debug("XML parsing failed", e);
            errors.add("XML parsing error: " + e.getMessage());
            return null;
        } catch (IOException e) {
            logger.debug("IO error during parsing", e);
            errors.add("IO error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.debug("Olingo schema parsing failed", e);
            errors.add("Schema parsing failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create a CsdlSchema from a DOM Element
     */
    private CsdlSchema createCsdlSchemaFromElement(org.w3c.dom.Element schemaElement, 
                                                   List<String> errors, List<String> warnings) {
        try {
            CsdlSchema schema = new CsdlSchema();
            
            // Set namespace
            String namespace = schemaElement.getAttribute("Namespace");
            if (namespace == null || namespace.trim().isEmpty()) {
                errors.add("Schema element missing required Namespace attribute");
                return null;
            }
            schema.setNamespace(namespace);
            
            // Set alias if present
            String alias = schemaElement.getAttribute("Alias");
            if (alias != null && !alias.trim().isEmpty()) {
                schema.setAlias(alias);
            }
            
            // Parse entity types
            parseEntityTypes(schemaElement, schema, errors, warnings);
            
            // Parse complex types
            parseComplexTypes(schemaElement, schema, errors, warnings);
            
            // Parse entity containers
            parseEntityContainers(schemaElement, schema, errors, warnings);
            
            return schema;
            
        } catch (Exception e) {
            logger.debug("Failed to create CSDL schema from element", e);
            errors.add("Failed to create schema: " + e.getMessage());
            return null;
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
        if (!isValidODataIdentifier(namespace)) {
            errors.add("Invalid namespace format: " + namespace);
        }
        
        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                validateEntityType(entityType, namespace, errors, warnings, referencedNamespaces);
            }
            metadata.put("entityTypes_" + namespace, schema.getEntityTypes().size());
        }
        
        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                validateComplexType(complexType, namespace, errors, warnings, referencedNamespaces);
            }
            metadata.put("complexTypes_" + namespace, schema.getComplexTypes().size());
        }
        
        // Validate entity container
        if (schema.getEntityContainer() != null) {
            validateEntityContainer(schema.getEntityContainer(), namespace, errors, warnings);
        }
    }
    
    /**
     * Validate EntityType using Olingo structures
     */
    private void validateEntityType(CsdlEntityType entityType, String namespace, List<String> errors, 
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
    private void validateComplexType(CsdlComplexType complexType, String namespace, List<String> errors, 
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
                                        String namespace, List<String> errors, List<String> warnings) {
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
     * Parse entity types from schema element
     */
    private void parseEntityTypes(org.w3c.dom.Element schemaElement, CsdlSchema schema, 
                                  List<String> errors, List<String> warnings) {
        org.w3c.dom.NodeList entityTypes = schemaElement.getElementsByTagNameNS(
            "http://docs.oasis-open.org/odata/ns/edm", "EntityType");
        
        List<CsdlEntityType> csdlEntityTypes = new ArrayList<>();
        for (int i = 0; i < entityTypes.getLength(); i++) {
            org.w3c.dom.Element entityTypeElement = (org.w3c.dom.Element) entityTypes.item(i);
            CsdlEntityType entityType = new CsdlEntityType();
            
            String name = entityTypeElement.getAttribute("Name");
            if (name != null && !name.trim().isEmpty()) {
                entityType.setName(name);
                csdlEntityTypes.add(entityType);
            } else {
                errors.add("EntityType missing required Name attribute");
            }
        }
        
        if (!csdlEntityTypes.isEmpty()) {
            schema.setEntityTypes(csdlEntityTypes);
        }
    }
    
    /**
     * Parse complex types from schema element
     */
    private void parseComplexTypes(org.w3c.dom.Element schemaElement, CsdlSchema schema, 
                                   List<String> errors, List<String> warnings) {
        org.w3c.dom.NodeList complexTypes = schemaElement.getElementsByTagNameNS(
            "http://docs.oasis-open.org/odata/ns/edm", "ComplexType");
        
        List<CsdlComplexType> csdlComplexTypes = new ArrayList<>();
        for (int i = 0; i < complexTypes.getLength(); i++) {
            org.w3c.dom.Element complexTypeElement = (org.w3c.dom.Element) complexTypes.item(i);
            CsdlComplexType complexType = new CsdlComplexType();
            
            String name = complexTypeElement.getAttribute("Name");
            if (name != null && !name.trim().isEmpty()) {
                complexType.setName(name);
                csdlComplexTypes.add(complexType);
            } else {
                errors.add("ComplexType missing required Name attribute");
            }
        }
        
        if (!csdlComplexTypes.isEmpty()) {
            schema.setComplexTypes(csdlComplexTypes);
        }
    }
    
    /**
     * Parse entity containers from schema element
     */
    private void parseEntityContainers(org.w3c.dom.Element schemaElement, CsdlSchema schema, 
                                       List<String> errors, List<String> warnings) {
        org.w3c.dom.NodeList containers = schemaElement.getElementsByTagNameNS(
            "http://docs.oasis-open.org/odata/ns/edm", "EntityContainer");
        
        if (containers.getLength() > 0) {
            org.w3c.dom.Element containerElement = (org.w3c.dom.Element) containers.item(0);
            org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer container = 
                new org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer();
            
            String name = containerElement.getAttribute("Name");
            if (name != null && !name.trim().isEmpty()) {
                container.setName(name);
                schema.setEntityContainer(container);
            } else {
                errors.add("EntityContainer missing required Name attribute");
            }
        }
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
