package org.apache.olingo.compliance.core.model;

/**
 * Enumeration of OData XML compliance error categories.
 * Each error type represents a specific category of validation failure
 * according to OData 4.0 specification.
 */
public enum ComplianceErrorType {
    
    // Attribute-related errors
    MISSING_REQUIRED_ATTRIBUTE("Missing required attribute"),
    INVALID_ATTRIBUTE_VALUE("Invalid attribute value"),
    INVALID_IDENTIFIER_NAME("Invalid identifier name"),
    
    // Duplicate element errors
    DUPLICATE_ELEMENT("Duplicate element"),
    DUPLICATE_ELEMENT_NAME("Duplicate element name"),
    DUPLICATE_PROPERTY_NAME("Duplicate property name"),
    DUPLICATE_ACTION_IMPORT("Duplicate action import"),
    DUPLICATE_FUNCTION_IMPORT("Duplicate function import"),
    DUPLICATE_ENTITY_SET("Duplicate entity set"),
    DUPLICATE_SINGLETON("Duplicate singleton"),
    DUPLICATE_PARAMETER("Duplicate parameter"),
    DUPLICATE_ENUM_MEMBER("Duplicate enum member"),
    DUPLICATE_KEY_REFERENCE("Duplicate key reference"),
    
    // Missing element errors
    MISSING_REQUIRED_ELEMENT("Missing required element"),
    MISSING_KEY_ELEMENT("Missing key element"),
    MISSING_TYPE_ATTRIBUTE("Missing type attribute"),
    MISSING_NAME_ATTRIBUTE("Missing name attribute"),
    MISSING_NAMESPACE("Missing namespace"),
    MISSING_DATA_SERVICES("Missing data services"),
    MISSING_EDMX_ROOT("Missing EDMX root"),
    
    // Type reference errors
    TYPE_ERROR("Type error"),
    TYPE_NOT_EXIST("Referenced type does not exist"),
    TYPE_WRONG_KIND("Referenced type is wrong kind"),
    INVALID_BASE_TYPE("Invalid base type"),
    CIRCULAR_INHERITANCE("Circular inheritance detected"),
    SELF_INHERITANCE("Self inheritance not allowed"),
    
    // Structure errors
    STRUCTURE_ERROR("Structure error"),
    MALFORMED_XML("Malformed XML structure"),
    INVALID_SCHEMA_STRUCTURE("Invalid schema structure"),
    INVALID_INHERITANCE_HIERARCHY("Invalid inheritance hierarchy"),
    
    // Directory validation errors
    ELEMENT_CONFLICT("Element conflict across files"),
    ALIAS_CONFLICT("Alias conflict across files"),
    NAMESPACE_CONFLICT("Namespace conflict"),
    CIRCULAR_REFERENCE("Circular reference detected"),
    SCHEMA_DEPENDENCY_ERROR("Schema dependency error"),
    MULTIPLE_SCHEMA_SAME_NAMESPACE("Multiple schemas with same namespace"),
    
    // Other errors
    UNKNOWN_ANNOTATION("Unknown annotation"),
    VALIDATION_ERROR("General validation error"),
    PARSING_ERROR("XML parsing error");
    
    private final String description;
    
    ComplianceErrorType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
