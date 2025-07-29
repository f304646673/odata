package org.apache.olingo.schema.processor.validation.validators;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.schema.processor.validation.core.ElementValidator;
import org.apache.olingo.schema.processor.validation.core.NamingValidator;
import org.apache.olingo.schema.processor.validation.core.ValidationContext;

/**
 * Validator for CSDL ComplexType elements.
 */
public class ComplexTypeValidator implements ElementValidator<CsdlComplexType> {

    private final NamingValidator namingValidator;
    private final PropertyValidator propertyValidator;
    private final AnnotationValidator annotationValidator;

    public ComplexTypeValidator(NamingValidator namingValidator,
                               PropertyValidator propertyValidator,
                               AnnotationValidator annotationValidator) {
        this.namingValidator = namingValidator;
        this.propertyValidator = propertyValidator;
        this.annotationValidator = annotationValidator;
    }

    @Override
    public void validate(CsdlComplexType complexType, ValidationContext context) {
        if (complexType.getName() == null || complexType.getName().trim().isEmpty()) {
            context.addError("ComplexType must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(complexType.getName())) {
            context.addError("Invalid ComplexType name: " + complexType.getName());
        }

        // Check BaseType reference
        if (complexType.getBaseType() != null) {
            validateTypeReference(complexType.getBaseType(), context);
        }

        // Validate properties
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                propertyValidator.validate(property, context);
            }
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(complexType.getAnnotations(), context);
    }

    @Override
    public Class<CsdlComplexType> getElementType() {
        return CsdlComplexType.class;
    }

    private void validateTypeReference(String typeRef, ValidationContext context) {
        if (typeRef != null && !typeRef.trim().isEmpty()) {
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
