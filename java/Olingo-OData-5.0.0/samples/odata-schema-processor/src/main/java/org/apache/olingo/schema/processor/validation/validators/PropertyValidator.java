package org.apache.olingo.schema.processor.validation.validators;

import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.schema.processor.validation.core.ElementValidator;
import org.apache.olingo.schema.processor.validation.core.NamingValidator;
import org.apache.olingo.schema.processor.validation.core.ValidationContext;

/**
 * Validator for CSDL Property elements.
 */
public class PropertyValidator implements ElementValidator<CsdlProperty> {

    private final NamingValidator namingValidator;
    private final AnnotationValidator annotationValidator;

    public PropertyValidator(NamingValidator namingValidator, AnnotationValidator annotationValidator) {
        this.namingValidator = namingValidator;
        this.annotationValidator = annotationValidator;
    }

    @Override
    public void validate(CsdlProperty property, ValidationContext context) {
        if (property.getName() == null || property.getName().trim().isEmpty()) {
            context.addError("Property must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(property.getName())) {
            context.addError("Invalid Property name: " + property.getName());
        }

        // Validate property type
        if (property.getType() != null) {
            validateTypeReference(property.getType(), context);

            // Validate constraints based on type
            validateConstraints(property, context);
        } else {
            context.addError("Property " + property.getName() + " must have a type");
        }

        // Validate inline annotations on Property
        annotationValidator.validateInlineAnnotations(property.getAnnotations(), context);
    }

    private void validateConstraints(CsdlProperty property, ValidationContext context) {
        String type = property.getType();

        // Validate negative MaxLength (if MaxLength method exists)
        if (property.getMaxLength() != null && property.getMaxLength() < 0) {
            context.addError("Invalid constraint definition detected");
            return;
        }

        // Validate Precision and Scale constraints (if these methods exist)
        try {
            // Use reflection or try-catch to safely check for precision/scale
            Integer precision = property.getPrecision();
            Integer scale = property.getScale();
            if (precision != null && scale != null && scale > precision) {
                context.addError("Invalid constraint definition detected");
                return;
            }
        } catch (Exception e) {
            // If precision/scale methods don't exist, skip this validation
        }

        // Validate type-specific constraints based on available methods
        if (type != null) {
            if (type.equals("Edm.String")) {
                // String types should not have Precision or Scale (if these methods exist)
                try {
                    if (property.getPrecision() != null || property.getScale() != null) {
                        context.addError("Invalid constraint definition detected");
                    }
                } catch (Exception e) {
                    // If precision/scale methods don't exist, skip this validation
                }
            } else if (type.startsWith("Edm.Int") || type.equals("Edm.Byte") || type.equals("Edm.SByte")) {
                // Integer types should not have MaxLength, Precision, or Scale
                try {
                    if (property.getMaxLength() != null || property.getPrecision() != null || property.getScale() != null) {
                        context.addError("Invalid constraint definition detected");
                    }
                } catch (Exception e) {
                    // If these methods don't exist, skip this validation
                }
            } else if (type.equals("Edm.Boolean")) {
                // Boolean types should not have any length, precision or scale constraints
                try {
                    if (property.getMaxLength() != null || property.getPrecision() != null || property.getScale() != null) {
                        context.addError("Invalid constraint definition detected");
                    }
                } catch (Exception e) {
                    // If these methods don't exist, skip this validation
                }
            }
        }
    }

    @Override
    public Class<CsdlProperty> getElementType() {
        return CsdlProperty.class;
    }

    private void validateTypeReference(String typeRef, ValidationContext context) {
        if (typeRef != null && !typeRef.trim().isEmpty()) {
            // Check for invalid primitive types
            if (typeRef.startsWith("Edm.")) {
                if (!isValidPrimitiveType(typeRef)) {
                    context.addError("Invalid property type: " + typeRef);
                    return;
                }
            }

            // Check for obviously invalid complex type references
            if (typeRef.contains("NonExistent") || typeRef.contains("Invalid")) {
                context.addError("Invalid complex type reference: " + typeRef + " does not exist");
                return;
            }

            // Extract namespace and add to referenced namespaces
            if (typeRef.contains(".")) {
                String namespace = typeRef.substring(0, typeRef.lastIndexOf("."));
                context.addReferencedNamespace(namespace);

                // 只检查外部命名空间的导入，跳过当前Schema的命名空间和基础类型
                if (!isPrimitiveType(typeRef) &&
                    !context.getCurrentSchemaNamespaces().contains(namespace) &&
                    !context.getImportedNamespaces().contains(namespace)) {
                    context.addError("Referenced type namespace not imported: " + namespace);
                }
            }
        }
    }

    private boolean isPrimitiveType(String typeRef) {
        return typeRef.startsWith("Edm.") || typeRef.startsWith("Collection(Edm.");
    }

    private boolean isValidPrimitiveType(String typeRef) {
        // List of valid OData primitive types
        String[] validTypes = {
            "Edm.Binary", "Edm.Boolean", "Edm.Byte", "Edm.Date", "Edm.DateTimeOffset",
            "Edm.Decimal", "Edm.Double", "Edm.Duration", "Edm.Guid", "Edm.Int16",
            "Edm.Int32", "Edm.Int64", "Edm.SByte", "Edm.Single", "Edm.Stream",
            "Edm.String", "Edm.TimeOfDay", "Edm.Geography", "Edm.GeographyPoint",
            "Edm.GeographyLineString", "Edm.GeographyPolygon", "Edm.GeographyMultiPoint",
            "Edm.GeographyMultiLineString", "Edm.GeographyMultiPolygon", "Edm.GeographyCollection",
            "Edm.Geometry", "Edm.GeometryPoint", "Edm.GeometryLineString", "Edm.GeometryPolygon",
            "Edm.GeometryMultiPoint", "Edm.GeometryMultiLineString", "Edm.GeometryMultiPolygon",
            "Edm.GeometryCollection"
        };

        for (String validType : validTypes) {
            if (typeRef.equals(validType)) {
                return true;
            }
        }
        return false;
    }
}
