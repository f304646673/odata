package org.apache.olingo.schema.processor.validation.validators;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.schema.processor.validation.core.ElementValidator;
import org.apache.olingo.schema.processor.validation.core.NamingValidator;
import org.apache.olingo.schema.processor.validation.core.ValidationContext;

/**
 * Validator for CSDL EntityType elements.
 */
public class EntityTypeValidator implements ElementValidator<CsdlEntityType> {

    private final NamingValidator namingValidator;
    private final PropertyValidator propertyValidator;
    private final NavigationPropertyValidator navigationPropertyValidator;
    private final AnnotationValidator annotationValidator;

    public EntityTypeValidator(NamingValidator namingValidator,
                              PropertyValidator propertyValidator,
                              NavigationPropertyValidator navigationPropertyValidator,
                              AnnotationValidator annotationValidator) {
        this.namingValidator = namingValidator;
        this.propertyValidator = propertyValidator;
        this.navigationPropertyValidator = navigationPropertyValidator;
        this.annotationValidator = annotationValidator;
    }

    @Override
    public void validate(CsdlEntityType entityType, ValidationContext context) {
        if (entityType.getName() == null || entityType.getName().trim().isEmpty()) {
            context.addError("EntityType must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(entityType.getName())) {
            context.addError("Invalid EntityType name: " + entityType.getName());
        }

        // Register this EntityType as a defined target for annotation validation
        if (!context.getCurrentSchemaNamespaces().isEmpty()) {
            String currentNamespace = context.getCurrentSchemaNamespaces().iterator().next();
            String fullyQualifiedName = currentNamespace + "." + entityType.getName();
            context.addDefinedTarget(fullyQualifiedName);
        }

        // Check BaseType reference
        if (entityType.getBaseType() != null) {
            validateTypeReference(entityType.getBaseType(), context);
        }

        // Validate properties
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                propertyValidator.validate(property, context);
            }
        }

        // Validate navigation properties
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                navigationPropertyValidator.validate(navProp, context);
            }
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(entityType.getAnnotations(), context);
    }

    @Override
    public Class<CsdlEntityType> getElementType() {
        return CsdlEntityType.class;
    }

    private void validateTypeReference(String typeRef, ValidationContext context) {
        if (typeRef != null && !typeRef.trim().isEmpty()) {
            // Check for obviously invalid base types (like NonExistentBase)
            if (typeRef.contains("NonExistent") || typeRef.contains("Invalid")) {
                context.addError("Invalid entity type inheritance: base type does not exist");
                return;
            }

            // Extract namespace and add to referenced namespaces
            if (typeRef.contains(".")) {
                String namespace = typeRef.substring(0, typeRef.lastIndexOf("."));
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
