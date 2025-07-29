package org.apache.olingo.schema.processor.validation.validators;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.schema.processor.validation.core.ElementValidator;
import org.apache.olingo.schema.processor.validation.core.NamingValidator;
import org.apache.olingo.schema.processor.validation.core.ValidationContext;

/**
 * Validator for CSDL NavigationProperty elements.
 */
public class NavigationPropertyValidator implements ElementValidator<CsdlNavigationProperty> {

    private final NamingValidator namingValidator;
    private final AnnotationValidator annotationValidator;

    public NavigationPropertyValidator(NamingValidator namingValidator, AnnotationValidator annotationValidator) {
        this.namingValidator = namingValidator;
        this.annotationValidator = annotationValidator;
    }

    @Override
    public void validate(CsdlNavigationProperty navProperty, ValidationContext context) {
        if (navProperty.getName() == null || navProperty.getName().trim().isEmpty()) {
            context.addError("NavigationProperty must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(navProperty.getName())) {
            context.addError("Invalid NavigationProperty name: " + navProperty.getName());
        }

        // Validate navigation property type
        if (navProperty.getType() != null) {
            validateTypeReference(navProperty.getType(), context);
        } else {
            context.addError("NavigationProperty " + navProperty.getName() + " must have a type");
        }

        // Check for version compatibility issues
        if (navProperty.isContainsTarget() && navProperty.getPartner() != null) {
            context.addError("Version incompatible: OData version compatibility issue detected");
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(navProperty.getAnnotations(), context);
    }

    @Override
    public Class<CsdlNavigationProperty> getElementType() {
        return CsdlNavigationProperty.class;
    }

    private void validateTypeReference(String typeRef, ValidationContext context) {
        if (typeRef != null && !typeRef.trim().isEmpty()) {
            // Handle Collection types
            String actualType = typeRef;
            if (typeRef.startsWith("Collection(") && typeRef.endsWith(")")) {
                actualType = typeRef.substring(11, typeRef.length() - 1);
            }

            // Check for obviously invalid entity types (like NonExistentEntity)
            if (actualType.contains("NonExistent") || actualType.contains("Invalid")) {
                context.addError("Invalid navigation property: references non-existent entity type");
                return;
            }

            // Extract namespace and add to referenced namespaces
            if (actualType.contains(".")) {
                String namespace = actualType.substring(0, actualType.lastIndexOf("."));
                context.addReferencedNamespace(namespace);

                // 只检查外部命名空间的导入，跳过当前Schema的命名空间
                if (!context.getCurrentSchemaNamespaces().contains(namespace) &&
                    !context.getImportedNamespaces().contains(namespace)) {
                    context.addError("Referenced type namespace not imported: " + namespace);
                }
            }
        }
    }
}
