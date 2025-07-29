package org.apache.olingo.schema.processor.validation.impl;

import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.schema.processor.validation.core.NamingValidator;
import org.apache.olingo.schema.processor.validation.core.SchemaValidator;
import org.apache.olingo.schema.processor.validation.core.ValidationContext;
import org.apache.olingo.schema.processor.validation.validators.*;

/**
 * Main implementation of schema validator that coordinates all element validators.
 */
public class CsdlSchemaValidator implements SchemaValidator {

    private final NamingValidator namingValidator;
    private final EntityTypeValidator entityTypeValidator;
    private final ComplexTypeValidator complexTypeValidator;
    private final PropertyValidator propertyValidator;
    private final NavigationPropertyValidator navigationPropertyValidator;
    private final AnnotationValidator annotationValidator;

    public CsdlSchemaValidator(NamingValidator namingValidator) {
        this.namingValidator = namingValidator;
        this.annotationValidator = new AnnotationValidator(namingValidator);
        this.propertyValidator = new PropertyValidator(namingValidator, annotationValidator);
        this.navigationPropertyValidator = new NavigationPropertyValidator(namingValidator, annotationValidator);
        this.entityTypeValidator = new EntityTypeValidator(namingValidator, propertyValidator, navigationPropertyValidator, annotationValidator);
        this.complexTypeValidator = new ComplexTypeValidator(namingValidator, propertyValidator, annotationValidator);
    }

    @Override
    public void validate(CsdlSchema schema, ValidationContext context) {
        // Validate schema namespace
        if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
            context.addError("Schema must have a valid namespace");
            return;
        }

        String namespace = schema.getNamespace();
        context.addReferencedNamespace(namespace);
        context.addCurrentSchemaNamespace(namespace); // 记录当前Schema的命名空间

        // Validate namespace format
        if (!namingValidator.isValidODataNamespace(namespace)) {
            context.addError("Invalid namespace format: " + namespace);
        }

        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                entityTypeValidator.validate(entityType, context);
            }
            context.addMetadata("entityTypes_" + namespace, schema.getEntityTypes().size());
        }

        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                complexTypeValidator.validate(complexType, context);
                // Register ComplexType as annotation target
                String fullyQualifiedName = namespace + "." + complexType.getName();
                context.addDefinedTarget(fullyQualifiedName);
            }
            context.addMetadata("complexTypes_" + namespace, schema.getComplexTypes().size());
        }

        // Validate enum types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                validateEnumType(enumType, context);
                // Register EnumType as annotation target
                String fullyQualifiedName = namespace + "." + enumType.getName();
                context.addDefinedTarget(fullyQualifiedName);
            }
        }

        // Validate actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                validateAction(action, context);
                // Register Action as annotation target
                String fullyQualifiedName = namespace + "." + action.getName();
                context.addDefinedTarget(fullyQualifiedName);
            }
        }

        // Validate functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                validateFunction(function, context);
                // Register Function as annotation target
                String fullyQualifiedName = namespace + "." + function.getName();
                context.addDefinedTarget(fullyQualifiedName);
            }
        }

        // Validate entity container
        if (schema.getEntityContainer() != null) {
            validateEntityContainer(schema.getEntityContainer(), context);
            // Register EntityContainer as annotation target
            String fullyQualifiedName = namespace + "." + schema.getEntityContainer().getName();
            context.addDefinedTarget(fullyQualifiedName);
        }

        // Validate annotations
        if (schema.getAnnotationGroups() != null && !schema.getAnnotationGroups().isEmpty()) {
            validateAnnotations(schema, context);
        }
    }

    private void validateEnumType(CsdlEnumType enumType, ValidationContext context) {
        if (enumType.getName() == null || enumType.getName().trim().isEmpty()) {
            context.addError("EnumType must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(enumType.getName())) {
            context.addError("Invalid EnumType name: " + enumType.getName());
        }

        // Validate underlying type if specified
        if (enumType.getUnderlyingType() != null) {
            validateTypeReference(enumType.getUnderlyingType(), context);
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(enumType.getAnnotations(), context);
    }

    private void validateAction(CsdlAction action, ValidationContext context) {
        if (action.getName() == null || action.getName().trim().isEmpty()) {
            context.addError("Action must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(action.getName())) {
            context.addError("Invalid Action name: " + action.getName());
        }

        // Validate parameters
        if (action.getParameters() != null) {
            for (CsdlParameter parameter : action.getParameters()) {
                validateParameter(parameter, context);
            }
        }

        // Validate return type
        if (action.getReturnType() != null) {
            validateTypeReference(action.getReturnType().getType(), context);
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(action.getAnnotations(), context);
    }

    private void validateFunction(CsdlFunction function, ValidationContext context) {
        if (function.getName() == null || function.getName().trim().isEmpty()) {
            context.addError("Function must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(function.getName())) {
            context.addError("Invalid Function name: " + function.getName());
        }

        // Validate parameters
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                validateParameter(parameter, context);
            }
        }

        // Validate return type (required for functions)
        if (function.getReturnType() == null) {
            context.addError("Function " + function.getName() + " must have a return type");
        } else {
            validateTypeReference(function.getReturnType().getType(), context);
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(function.getAnnotations(), context);
    }

    private void validateParameter(CsdlParameter parameter, ValidationContext context) {
        if (parameter.getName() == null || parameter.getName().trim().isEmpty()) {
            context.addError("Parameter must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(parameter.getName())) {
            context.addError("Invalid Parameter name: " + parameter.getName());
        }

        // Validate parameter type
        if (parameter.getType() != null) {
            validateTypeReference(parameter.getType(), context);
        } else {
            context.addError("Parameter " + parameter.getName() + " must have a type");
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(parameter.getAnnotations(), context);
    }

    private void validateEntityContainer(CsdlEntityContainer container, ValidationContext context) {
        if (container.getName() == null || container.getName().trim().isEmpty()) {
            context.addError("EntityContainer must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(container.getName())) {
            context.addError("Invalid EntityContainer name: " + container.getName());
        }

        // Validate EntitySets
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                validateEntitySet(entitySet, context);
            }
        }

        // Validate Singletons
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                validateSingleton(singleton, context);
            }
        }

        // Validate ActionImports
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                validateActionImport(actionImport, context);
            }
        }

        // Validate FunctionImports
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                validateFunctionImport(functionImport, context);
            }
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(container.getAnnotations(), context);
    }

    private void validateEntitySet(CsdlEntitySet entitySet, ValidationContext context) {
        if (entitySet.getName() == null || entitySet.getName().trim().isEmpty()) {
            context.addError("EntitySet must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(entitySet.getName())) {
            context.addError("Invalid EntitySet name: " + entitySet.getName());
        }

        if (entitySet.getType() != null) {
            validateTypeReference(entitySet.getType(), context);
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(entitySet.getAnnotations(), context);
    }

    private void validateSingleton(CsdlSingleton singleton, ValidationContext context) {
        if (singleton.getName() == null || singleton.getName().trim().isEmpty()) {
            context.addError("Singleton must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(singleton.getName())) {
            context.addError("Invalid Singleton name: " + singleton.getName());
        }

        if (singleton.getType() != null) {
            validateTypeReference(singleton.getType(), context);
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(singleton.getAnnotations(), context);
    }

    private void validateActionImport(CsdlActionImport actionImport, ValidationContext context) {
        if (actionImport.getName() == null || actionImport.getName().trim().isEmpty()) {
            context.addError("ActionImport must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(actionImport.getName())) {
            context.addError("Invalid ActionImport name: " + actionImport.getName());
        }

        if (actionImport.getAction() != null) {
            validateTypeReference(actionImport.getAction(), context);
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(actionImport.getAnnotations(), context);
    }

    private void validateFunctionImport(CsdlFunctionImport functionImport, ValidationContext context) {
        if (functionImport.getName() == null || functionImport.getName().trim().isEmpty()) {
            context.addError("FunctionImport must have a valid name");
            return;
        }

        if (!namingValidator.isValidODataIdentifier(functionImport.getName())) {
            context.addError("Invalid FunctionImport name: " + functionImport.getName());
        }

        if (functionImport.getFunction() != null) {
            validateTypeReference(functionImport.getFunction(), context);
        }

        // Validate inline annotations
        annotationValidator.validateInlineAnnotations(functionImport.getAnnotations(), context);
    }

    private void validateAnnotations(CsdlSchema schema, ValidationContext context) {
        // Check annotation groups (Annotations elements)
        if (schema.getAnnotationGroups() != null) {
            for (CsdlAnnotations annotations : schema.getAnnotationGroups()) {
                String target = annotations.getTarget();
                if (target != null && !target.trim().isEmpty()) {
                    annotationValidator.validateAnnotationTarget(target, context);
                }

                // Validate individual annotations within the group
                if (annotations.getAnnotations() != null) {
                    for (CsdlAnnotation annotation : annotations.getAnnotations()) {
                        annotationValidator.validateAnnotationTerm(annotation.getTerm(), context);
                    }
                }
            }
        }
    }

    private void validateTypeReference(String typeRef, ValidationContext context) {
        if (typeRef != null && !typeRef.trim().isEmpty()) {
            // Handle Collection types
            String actualType = typeRef;
            if (typeRef.startsWith("Collection(") && typeRef.endsWith(")")) {
                actualType = typeRef.substring(11, typeRef.length() - 1);
            }

            // Extract namespace and add to referenced namespaces
            if (actualType.contains(".")) {
                String namespace = actualType.substring(0, actualType.lastIndexOf("."));
                context.addReferencedNamespace(namespace);

                // 只检查外部命名空间的导入，跳过当前Schema的命名空间和基础类型
                if (!isPrimitiveType(actualType) &&
                    !context.getCurrentSchemaNamespaces().contains(namespace) &&
                    !context.getImportedNamespaces().contains(namespace)) {
                    context.addError("Referenced type namespace not imported: " + namespace);
                }
            }
        }
    }

    private boolean isPrimitiveType(String typeRef) {
        return typeRef.startsWith("Edm.");
    }
}
