package org.apache.olingo.compliance.engine.rule.semantic;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.olingo.compliance.engine.rule.RuleResult;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validates OData compliance requirements for all schemas.
 */
public class ComplianceRule extends AbstractSemanticRule {
    
    public ComplianceRule() {
        super("odata-compliance", 
              "Validates OData compliance requirements", 
              "error");
    }
    
    @Override
    protected boolean isSemanticApplicable(ValidationContext context, ValidationConfig config) {
        return context.getAllSchemas() != null && !context.getAllSchemas().isEmpty();
    }
    
    @Override
    public RuleResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        List<CsdlSchema> schemas = context.getAllSchemas();
        if (schemas == null || schemas.isEmpty()) {
            return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
        }

        // Validate each schema for compliance
        for (CsdlSchema schema : schemas) {
            String errorMessage = validateSchemaCompliance(schema, context);
            if (errorMessage != null) {
                return RuleResult.fail(getName(), errorMessage, System.currentTimeMillis() - startTime);
            }
        }
        
        return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    private String validateSchemaCompliance(CsdlSchema schema, ValidationContext context) {
        if (schema == null) {
            return "Schema cannot be null";
        }
        
        if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
            return "Schema must have a valid namespace";
        }
        
        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String error = validateEntityType(entityType);
                if (error != null) return error;
                
                if (entityType.getProperties() != null) {
                    for (CsdlProperty property : entityType.getProperties()) {
                        String propertyError = validatePropertyType(property, entityType.getName());
                        if (propertyError != null) return propertyError;
                    }
                }
                
                if (entityType.getNavigationProperties() != null) {
                    for (CsdlNavigationProperty navProperty : entityType.getNavigationProperties()) {
                        String navError = validateNavigationProperty(context, navProperty, entityType.getName());
                        if (navError != null) return navError;
                    }
                }
            }
        }
        
        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                if (complexType.getProperties() != null) {
                    for (CsdlProperty property : complexType.getProperties()) {
                        String propertyError = validatePropertyType(property, complexType.getName());
                        if (propertyError != null) return propertyError;
                    }
                }
            }
        }
        
        return null;
    }
    
    private String validateEntityType(CsdlEntityType entityType) {
        if (entityType.getName() == null || entityType.getName().trim().isEmpty()) {
            return "Entity type must have a valid name";
        }
        
        // Entity types should have a key
        if (entityType.getKey() == null || entityType.getKey().isEmpty()) {
            return String.format("Entity type '%s' must have a key", entityType.getName());
        }
        
        return null;
    }
    
    private String validatePropertyType(CsdlProperty property, String parentTypeName) {
        if (property == null) {
            return String.format("Null property found in type '%s'", parentTypeName);
        }
        
        if (property.getName() == null || property.getName().trim().isEmpty()) {
            return String.format("Property in type '%s' must have a valid name", parentTypeName);
        }
        
        if (property.getType() == null || property.getType().trim().isEmpty()) {
            return String.format("Property '%s' in type '%s' must have a valid type", 
                property.getName(), parentTypeName);
        }
        
        String propertyType = property.getType();
        
        // Validate EDM types or check if it's a valid complex/entity type reference
        if (propertyType.startsWith("Edm.")) {
            String edmType = propertyType.substring(4);
            if (!isValidEdmType(edmType)) {
                return String.format("Property '%s' in type '%s' has invalid EDM type '%s'",
                    property.getName(), parentTypeName, propertyType);
            }
        } else if (propertyType.startsWith("Collection(") && propertyType.endsWith(")")) {
            // Validate collection type
            String innerType = propertyType.substring(11, propertyType.length() - 1);
            if (innerType.startsWith("Edm.")) {
                String edmType = innerType.substring(4);
                if (!isValidEdmType(edmType)) {
                    return String.format("Property '%s' in type '%s' has invalid collection EDM type '%s'",
                        property.getName(), parentTypeName, innerType);
                }
            }
        }
        
        // Validate property constraints
        String constraintError = validatePropertyConstraints(property, parentTypeName);
        if (constraintError != null) {
            return constraintError;
        }
        
        return null;
    }
    
    private String validateNavigationProperty(ValidationContext context, CsdlNavigationProperty navProperty, String parentTypeName) {
        if (navProperty == null) {
            return String.format("Null navigation property found in type '%s'", parentTypeName);
        }
        
        if (navProperty.getName() == null || navProperty.getName().trim().isEmpty()) {
            return String.format("Navigation property in type '%s' must have a valid name", parentTypeName);
        }
        
        String typeName = navProperty.getType();
        if (typeName == null || typeName.trim().isEmpty()) {
            return String.format("Navigation property '%s' in type '%s' must have a valid type",
                navProperty.getName(), parentTypeName);
        }
        
        return null;
    }
    
    private String validatePropertyConstraints(CsdlProperty property, String parentTypeName) {
        String propertyType = property.getType();
        
        // Validate Decimal precision/scale constraints
        if ("Edm.Decimal".equals(propertyType)) {
            Integer precision = property.getPrecision();
            Integer scale = property.getScale();
            
            if (precision != null && precision < 1) {
                return String.format("Property '%s' in type '%s' has invalid precision %d (must be >= 1)",
                    property.getName(), parentTypeName, precision);
            }
            
            if (scale != null && precision != null && scale > precision) {
                return String.format("Property '%s' in type '%s' has scale %d greater than precision %d",
                    property.getName(), parentTypeName, scale, precision);
            }
        }
        
        // Validate String maxLength constraint
        if ("Edm.String".equals(propertyType)) {
            Integer maxLength = property.getMaxLength();
            if (maxLength != null && maxLength < 1) {
                return String.format("Property '%s' in type '%s' has invalid maxLength %d (must be >= 1)",
                    property.getName(), parentTypeName, maxLength);
            }
        }
        
        return null;
    }
    
    private boolean isValidEdmType(String edmType) {
        String[] validTypes = {
            "Binary", "Boolean", "Byte", "Date", "DateTimeOffset", 
            "Decimal", "Double", "Duration", "Guid", "Int16", 
            "Int32", "Int64", "SByte", "Single", "Stream", 
            "String", "TimeOfDay", "Geography", "GeographyPoint",
            "GeographyLineString", "GeographyPolygon", "GeographyMultiPoint",
            "GeographyMultiLineString", "GeographyMultiPolygon", "GeographyCollection",
            "Geometry", "GeometryPoint", "GeometryLineString", "GeometryPolygon",
            "GeometryMultiPoint", "GeometryMultiLineString", "GeometryMultiPolygon",
            "GeometryCollection"
        };
        
        for (String validType : validTypes) {
            if (validType.equals(edmType)) {
                return true;
            }
        }
        return false;
    }
}
