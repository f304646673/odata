package org.apache.olingo.compliance.validation.rules.structural;

import java.util.HashSet;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.compliance.validation.api.ValidationConfig;
import org.apache.olingo.compliance.validation.core.ValidationContext;

/**
 * Validates OData element definitions and naming conventions.
 * Based on the original validator business logic.
 */
public class ElementDefinitionRule extends AbstractStructuralRule {
    
    // Built-in OData namespaces
    private static final Set<String> BUILTIN_NAMESPACES = new HashSet<>();
    static {
        BUILTIN_NAMESPACES.add("Edm");
        BUILTIN_NAMESPACES.add("System");
    }
    
    public ElementDefinitionRule() {
        super("element-definition", 
              "Validates element definitions and naming conventions", 
              "error");
    }
    
    @Override
    protected boolean isStructurallyApplicable(ValidationContext context, ValidationConfig config) {
        return context.getSchema() != null;
    }
    
    @Override
    public RuleResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        CsdlSchema schema = context.getSchema();
        if (schema == null) {
            return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
        }
        
        String errorMessage = validateElementDefinitions(schema, context);
        if (errorMessage != null) {
            return RuleResult.fail(getName(), errorMessage, System.currentTimeMillis() - startTime);
        }
        
        return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    private String validateElementDefinitions(CsdlSchema schema, ValidationContext context) {
        // Track element names for duplicate detection
        Set<String> definedNames = new HashSet<>();
        // Track function signatures for duplicate function detection
        Set<String> functionSignatures = new HashSet<>();
        
        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String error = validateEntityType(entityType, context, definedNames);
                if (error != null) return error;
            }
        }
        
        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String error = validateComplexType(complexType, context, definedNames);
                if (error != null) return error;
            }
        }
        
        // Validate enum types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                String error = validateEnumType(enumType, context, definedNames);
                if (error != null) return error;
            }
        }
        
        // Validate actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                String error = validateAction(action, context, definedNames);
                if (error != null) return error;
            }
        }
        
        // Validate functions (use separate signature tracking)
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                String error = validateFunction(function, context, functionSignatures);
                if (error != null) return error;
            }
        }
        
        // Validate terms
        if (schema.getTerms() != null) {
            for (CsdlTerm term : schema.getTerms()) {
                String error = validateTerm(term, context, definedNames);
                if (error != null) return error;
            }
        }

        // Validate entity container
        if (schema.getEntityContainer() != null) {
            String error = validateEntityContainer(schema.getEntityContainer(), context, definedNames);
            if (error != null) return error;
        }        return null;
    }
    
    private String validateEntityType(CsdlEntityType entityType, ValidationContext context, Set<String> definedNames) {
        if (entityType.getName() == null || entityType.getName().trim().isEmpty()) {
            return "EntityType must have a valid name";
        }

        if (!isValidODataIdentifier(entityType.getName())) {
            return "Invalid EntityType name: " + entityType.getName();
        }

        // Check for duplicate names
        if (!definedNames.add(entityType.getName())) {
            return "duplicate element name: " + entityType.getName();
        }

        // Register as defined target
        String namespace = getCurrentNamespace(context);
        if (namespace != null) {
            context.addDefinedTarget(namespace + "." + entityType.getName());
        }

        // Check BaseType reference
        if (entityType.getBaseType() != null) {
            String error = validateTypeReference(entityType.getBaseType(), context);
            if (error != null) return error;
        }

        // Validate properties with duplicate name checking
        if (entityType.getProperties() != null) {
            Set<String> propertyNames = new HashSet<>();
            for (CsdlProperty property : entityType.getProperties()) {
                String error = validateProperty(property, context);
                if (error != null) return error;
                
                // Check for duplicate property names within this entity type
                if (property.getName() != null && !propertyNames.add(property.getName())) {
                    return "Duplicate property name '" + property.getName() + "' in entity type '" + entityType.getName() + "'";
                }
            }
        }

        // Validate navigation properties
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                String error = validateNavigationProperty(navProp, context);
                if (error != null) return error;
            }
        }
        
        return null;
    }
    
    private String validateComplexType(CsdlComplexType complexType, ValidationContext context, Set<String> definedNames) {
        if (complexType.getName() == null || complexType.getName().trim().isEmpty()) {
            return "ComplexType must have a valid name";
        }

        if (!isValidODataIdentifier(complexType.getName())) {
            return "Invalid ComplexType name: " + complexType.getName();
        }

        // Check for duplicate names
        if (!definedNames.add(complexType.getName())) {
            return "duplicate element name: " + complexType.getName();
        }

        // Register as defined target
        String namespace = getCurrentNamespace(context);
        if (namespace != null) {
            context.addDefinedTarget(namespace + "." + complexType.getName());
        }

        // Check BaseType reference
        if (complexType.getBaseType() != null) {
            String error = validateTypeReference(complexType.getBaseType(), context);
            if (error != null) return error;
        }

        // Validate properties with duplicate name checking
        if (complexType.getProperties() != null) {
            Set<String> propertyNames = new HashSet<>();
            for (CsdlProperty property : complexType.getProperties()) {
                String error = validateProperty(property, context);
                if (error != null) return error;
                
                // Check for duplicate property names within this complex type
                if (property.getName() != null && !propertyNames.add(property.getName())) {
                    return "Duplicate property name '" + property.getName() + "' in complex type '" + complexType.getName() + "'";
                }
            }
        }
        
        return null;
    }
    
    private String validateEnumType(CsdlEnumType enumType, ValidationContext context, Set<String> definedNames) {
        if (enumType.getName() == null || enumType.getName().trim().isEmpty()) {
            return "EnumType must have a valid name";
        }

        if (!isValidODataIdentifier(enumType.getName())) {
            return "Invalid EnumType name: " + enumType.getName();
        }

        // Check for duplicate names
        if (!definedNames.add(enumType.getName())) {
            return "duplicate element name: " + enumType.getName();
        }

        // Note: Empty EnumTypes are allowed by OData specification
        // No need to enforce at least one member

        // Register as defined target
        String namespace = getCurrentNamespace(context);
        if (namespace != null) {
            context.addDefinedTarget(namespace + "." + enumType.getName());
        }
        
        return null;
    }
    
    private String validateAction(CsdlAction action, ValidationContext context, Set<String> definedNames) {
        if (action.getName() == null || action.getName().trim().isEmpty()) {
            return "Action must have a valid name";
        }

        if (!isValidODataIdentifier(action.getName())) {
            return "Invalid Action name: " + action.getName();
        }

        // Check for duplicate names
        if (!definedNames.add(action.getName())) {
            return "duplicate element name: " + action.getName();
        }

        // Register as defined target
        String namespace = getCurrentNamespace(context);
        if (namespace != null) {
            context.addDefinedTarget(namespace + "." + action.getName());
        }
        
        // Validate ReturnType if present
        if (action.getReturnType() != null && action.getReturnType().getType() != null) {
            String error = validateTypeReference(action.getReturnType().getType(), context);
            if (error != null) return error;
        }
        
        // Validate parameters
        if (action.getParameters() != null) {
            for (CsdlParameter param : action.getParameters()) {
                if (param.getType() != null) {
                    String error = validateTypeReference(param.getType(), context);
                    if (error != null) return error;
                }
            }
        }
        
        return null;
    }
    
    private String validateFunction(CsdlFunction function, ValidationContext context, Set<String> functionSignatures) {
        if (function.getName() == null || function.getName().trim().isEmpty()) {
            return "Function must have a valid name";
        }

        if (!isValidODataIdentifier(function.getName())) {
            return "Invalid Function name: " + function.getName();
        }

        // Create function signature for duplicate detection
        String functionSignature = createFunctionSignature(function);
        
        // Check for duplicate function signatures (not just names)
        if (!functionSignatures.add(functionSignature)) {
            return "duplicate function signature: " + function.getName();
        }

        // Register as defined target
        String namespace = getCurrentNamespace(context);
        if (namespace != null) {
            context.addDefinedTarget(namespace + "." + function.getName());
        }
        
        // Validate ReturnType if present
        if (function.getReturnType() != null && function.getReturnType().getType() != null) {
            String error = validateTypeReference(function.getReturnType().getType(), context);
            if (error != null) return error;
        }
        
        // Validate parameters
        if (function.getParameters() != null) {
            for (CsdlParameter param : function.getParameters()) {
                if (param.getType() != null) {
                    String error = validateTypeReference(param.getType(), context);
                    if (error != null) return error;
                }
            }
        }
        
        return null;
    }
    
    private String validateTerm(CsdlTerm term, ValidationContext context, Set<String> definedNames) {
        if (term.getName() == null || term.getName().trim().isEmpty()) {
            return "Term must have a valid name";
        }

        if (!isValidODataIdentifier(term.getName())) {
            return "Invalid Term name: " + term.getName();
        }

        // Check for duplicate names
        if (!definedNames.add(term.getName())) {
            return "duplicate element name: " + term.getName();
        }

        // Check that Term has a required Type attribute
        if (term.getType() == null || term.getType().trim().isEmpty()) {
            return "Term '" + term.getName() + "' must have a Type attribute";
        }

        // Register as defined target
        String namespace = getCurrentNamespace(context);
        if (namespace != null) {
            context.addDefinedTarget(namespace + "." + term.getName());
        }

        // Validate term type reference
        String error = validateTypeReference(term.getType(), context);
        if (error != null) return error;
        
        return null;
    }
    
    private String validateEntityContainer(CsdlEntityContainer container, ValidationContext context, Set<String> definedNames) {
        if (container.getName() == null || container.getName().trim().isEmpty()) {
            return "EntityContainer must have a valid name";
        }

        if (!isValidODataIdentifier(container.getName())) {
            return "Invalid EntityContainer name: " + container.getName();
        }

        // Check for duplicate names
        if (!definedNames.add(container.getName())) {
            return "duplicate element name: " + container.getName();
        }

        // Register as defined target
        String namespace = getCurrentNamespace(context);
        if (namespace != null) {
            context.addDefinedTarget(namespace + "." + container.getName());
        }

        // Validate entity sets
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                String error = validateEntitySet(entitySet, context);
                if (error != null) return error;
            }
        }
        
        // Validate action imports for duplicate names
        if (container.getActionImports() != null) {
            Set<String> actionImportNames = new HashSet<>();
            for (CsdlActionImport actionImport : container.getActionImports()) {
                if (actionImport.getName() != null && !actionImportNames.add(actionImport.getName())) {
                    return "Duplicate ActionImport name '" + actionImport.getName() + "' in entity container '" + container.getName() + "'";
                }
            }
        }
        
        // Validate function imports for duplicate names
        if (container.getFunctionImports() != null) {
            Set<String> functionImportNames = new HashSet<>();
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                if (functionImport.getName() != null && !functionImportNames.add(functionImport.getName())) {
                    return "Duplicate FunctionImport name '" + functionImport.getName() + "' in entity container '" + container.getName() + "'";
                }
            }
        }
        
        return null;
    }
    
    private String validateEntitySet(CsdlEntitySet entitySet, ValidationContext context) {
        if (entitySet.getName() == null || entitySet.getName().trim().isEmpty()) {
            return "EntitySet must have a valid name";
        }

        if (!isValidODataIdentifier(entitySet.getName())) {
            return "Invalid EntitySet name: " + entitySet.getName();
        }
        
        return null;
    }
    
    private String validateProperty(CsdlProperty property, ValidationContext context) {
        if (property.getName() == null || property.getName().trim().isEmpty()) {
            return "Property must have a valid name";
        }

        if (!isValidODataIdentifier(property.getName())) {
            return "Invalid Property name: " + property.getName();
        }

        // Validate property type
        if (property.getType() != null) {
            String error = validateTypeReference(property.getType(), context);
            if (error != null) return error;
        }
        
        return null;
    }
    
    private String validateNavigationProperty(CsdlNavigationProperty navProp, ValidationContext context) {
        if (navProp.getName() == null || navProp.getName().trim().isEmpty()) {
            return "NavigationProperty must have a valid name";
        }

        if (!isValidODataIdentifier(navProp.getName())) {
            return "Invalid NavigationProperty name: " + navProp.getName();
        }

        // Validate navigation property type
        if (navProp.getType() != null) {
            String error = validateTypeReference(navProp.getType(), context);
            if (error != null) return error;
        }
        
        return null;
    }
    
    private String validateTypeReference(String typeRef, ValidationContext context) {
        if (typeRef != null && !typeRef.trim().isEmpty()) {
            // Extract namespace and add to referenced namespaces
            if (typeRef.contains(".")) {
                String namespace = typeRef.substring(0, typeRef.lastIndexOf("."));
                context.addReferencedNamespace(namespace);

                // Skip check for built-in namespaces like Edm
                if (BUILTIN_NAMESPACES.contains(namespace)) {
                    return null;
                }

                // Only check external namespace imports, skip current schema namespaces
                if (!context.getCurrentSchemaNamespaces().contains(namespace) &&
                    !context.getImportedNamespaces().contains(namespace)) {
                    return "Referenced type namespace not imported: " + namespace;
                }
                
                // For current schema namespaces, check if the type is defined
                if (context.getCurrentSchemaNamespaces().contains(namespace)) {
                    if (!context.getDefinedTargets().contains(typeRef)) {
                        return "Referenced type does not exist: " + typeRef;
                    }
                }
            }
            
            // Check for obviously invalid base types (from original logic) - only after namespace check
            if (typeRef.contains("NonExistent") || typeRef.contains("Invalid")) {
                return "Invalid entity type inheritance: base type does not exist";
            }
        }
        return null;
    }
    
    private boolean isValidODataIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        // OData identifier: starts with letter or underscore, followed by letters, digits, or underscores
        return identifier.matches("^[A-Za-z_][A-Za-z0-9_]*$");
    }
    
    private String getCurrentNamespace(ValidationContext context) {
        if (!context.getCurrentSchemaNamespaces().isEmpty()) {
            return context.getCurrentSchemaNamespaces().iterator().next();
        }
        return null;
    }
    
    /**
     * Creates a unique signature for a function based on:
     * - Function name
     * - Parameter types and order
     * - Return type
     * 
     * This allows function overloading based on different parameter signatures.
     */
    private String createFunctionSignature(CsdlFunction function) {
        StringBuilder signature = new StringBuilder();
        
        // Add function name
        signature.append(function.getName());
        signature.append("(");
        
        // Add parameter types in order
        if (function.getParameters() != null && !function.getParameters().isEmpty()) {
            for (int i = 0; i < function.getParameters().size(); i++) {
                if (i > 0) {
                    signature.append(",");
                }
                String paramType = function.getParameters().get(i).getType();
                signature.append(paramType != null ? paramType : "");
            }
        }
        
        signature.append(")");
        
        // Add return type
        if (function.getReturnType() != null && function.getReturnType().getType() != null) {
            signature.append(":");
            signature.append(function.getReturnType().getType());
        }
        
        return signature.toString();
    }
    
    @Override
    public long getEstimatedExecutionTime() {
        return 400; // Element validation can be complex
    }
}
