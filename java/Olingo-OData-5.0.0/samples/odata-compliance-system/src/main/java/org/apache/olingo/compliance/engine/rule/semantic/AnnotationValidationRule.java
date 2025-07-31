package org.apache.olingo.compliance.engine.rules.semantic;

import java.util.HashSet;
import java.util.Set;
import org.apache.olingo.compliance.engine.rule.RuleResult;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;

/**
 * Validates annotation usage in OData schemas.
 * Based on the original AnnotationValidator business logic.
 */
public class AnnotationValidationRule extends AbstractSemanticRule {
    
    // Known OData vocabularies (from original implementation)
    private static final Set<String> KNOWN_VOCABULARIES = new HashSet<>();
    static {
        KNOWN_VOCABULARIES.add("Core");
        KNOWN_VOCABULARIES.add("Measures");
        KNOWN_VOCABULARIES.add("Capabilities");
        KNOWN_VOCABULARIES.add("Validation");
        KNOWN_VOCABULARIES.add("UI");
        KNOWN_VOCABULARIES.add("Common");
        KNOWN_VOCABULARIES.add("Communication");
        KNOWN_VOCABULARIES.add("PersonalData");
        KNOWN_VOCABULARIES.add("Analytics");
        KNOWN_VOCABULARIES.add("Aggregation");
        KNOWN_VOCABULARIES.add("Authorization");
        KNOWN_VOCABULARIES.add("Session");
        KNOWN_VOCABULARIES.add("Temporal");
    }
    
    public AnnotationValidationRule() {
        super("annotation-validation", 
              "Validates annotation targets, terms, and format", 
              "error");
    }
    
    @Override
    protected boolean isSemanticApplicable(ValidationContext context, ValidationConfig config) {
        return context.getSchema() != null;
    }
    
    @Override
    public RuleResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        CsdlSchema schema = context.getSchema();
        if (schema == null) {
            return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
        }
        
        // Collect all annotation targets defined in this schema
        Set<String> definedTargets = collectDefinedTargets(schema);
        context.addDefinedTargets(definedTargets);
        
        // Validate annotations
        String errorMessage = validateAnnotations(schema, context);
        if (errorMessage != null) {
            return RuleResult.fail(getName(), errorMessage, System.currentTimeMillis() - startTime);
        }
        
        return RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    private Set<String> collectDefinedTargets(CsdlSchema schema) {
        Set<String> targets = new HashSet<>();
        String namespace = schema.getNamespace();
        
        // Entity types and their properties
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                targets.add(namespace + "." + entityType.getName());
                
                // Properties
                if (entityType.getProperties() != null) {
                    for (CsdlProperty property : entityType.getProperties()) {
                        targets.add(namespace + "." + entityType.getName() + "/" + property.getName());
                    }
                }
                
                // Navigation properties
                if (entityType.getNavigationProperties() != null) {
                    for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                        targets.add(namespace + "." + entityType.getName() + "/" + navProp.getName());
                    }
                }
            }
        }
        
        // Complex types and their properties
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                targets.add(namespace + "." + complexType.getName());
                
                if (complexType.getProperties() != null) {
                    for (CsdlProperty property : complexType.getProperties()) {
                        targets.add(namespace + "." + complexType.getName() + "/" + property.getName());
                    }
                }
            }
        }
        
        // Enum types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                targets.add(namespace + "." + enumType.getName());
            }
        }
        
        // Actions and Functions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                targets.add(namespace + "." + action.getName());
            }
        }
        
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                targets.add(namespace + "." + function.getName());
            }
        }
        
        // Entity container and entity sets
        if (schema.getEntityContainer() != null) {
            CsdlEntityContainer container = schema.getEntityContainer();
            targets.add(namespace + "." + container.getName());
            
            if (container.getEntitySets() != null) {
                for (CsdlEntitySet entitySet : container.getEntitySets()) {
                    targets.add(namespace + "." + container.getName() + "/" + entitySet.getName());
                }
            }
        }
        
        return targets;
    }
    
    private String validateAnnotations(CsdlSchema schema, ValidationContext context) {
        // Validate annotation groups
        if (schema.getAnnotationGroups() != null) {
            for (CsdlAnnotations annotations : schema.getAnnotationGroups()) {
                String error = validateAnnotationGroup(annotations, context);
                if (error != null) {
                    return error;
                }
            }
        }
        
        // Validate inline annotations on entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String error = validateInlineAnnotations(entityType.getAnnotations(), context);
                if (error != null) return error;
                
                if (entityType.getProperties() != null) {
                    for (CsdlProperty property : entityType.getProperties()) {
                        error = validateInlineAnnotations(property.getAnnotations(), context);
                        if (error != null) return error;
                    }
                }
                
                if (entityType.getNavigationProperties() != null) {
                    for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                        error = validateInlineAnnotations(navProp.getAnnotations(), context);
                        if (error != null) return error;
                    }
                }
            }
        }
        
        // Validate inline annotations on complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String error = validateInlineAnnotations(complexType.getAnnotations(), context);
                if (error != null) return error;
                
                if (complexType.getProperties() != null) {
                    for (CsdlProperty property : complexType.getProperties()) {
                        error = validateInlineAnnotations(property.getAnnotations(), context);
                        if (error != null) return error;
                    }
                }
            }
        }
        
        // Validate inline annotations on enum types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                String error = validateInlineAnnotations(enumType.getAnnotations(), context);
                if (error != null) return error;
            }
        }
        
        // Validate inline annotations on actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                String error = validateInlineAnnotations(action.getAnnotations(), context);
                if (error != null) return error;
            }
        }
        
        // Validate inline annotations on functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                String error = validateInlineAnnotations(function.getAnnotations(), context);
                if (error != null) return error;
            }
        }
        
        // Validate inline annotations on entity container and entity sets
        if (schema.getEntityContainer() != null) {
            CsdlEntityContainer container = schema.getEntityContainer();
            String error = validateInlineAnnotations(container.getAnnotations(), context);
            if (error != null) return error;
            
            if (container.getEntitySets() != null) {
                for (CsdlEntitySet entitySet : container.getEntitySets()) {
                    error = validateInlineAnnotations(entitySet.getAnnotations(), context);
                    if (error != null) return error;
                }
            }
            
            if (container.getSingletons() != null) {
                for (CsdlSingleton singleton : container.getSingletons()) {
                    error = validateInlineAnnotations(singleton.getAnnotations(), context);
                    if (error != null) return error;
                }
            }
            
            if (container.getActionImports() != null) {
                for (CsdlActionImport actionImport : container.getActionImports()) {
                    error = validateInlineAnnotations(actionImport.getAnnotations(), context);
                    if (error != null) return error;
                }
            }
            
            if (container.getFunctionImports() != null) {
                for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                    error = validateInlineAnnotations(functionImport.getAnnotations(), context);
                    if (error != null) return error;
                }
            }
        }
        
        return null;
    }
    
    private String validateAnnotationGroup(CsdlAnnotations annotations, ValidationContext context) {
        String target = annotations.getTarget();
        
        // Validate target format
        if (target == null || target.trim().isEmpty()) {
            return "Annotation target cannot be null or empty";
        }
        
        if (!isValidAnnotationTargetFormat(target)) {
            return "Invalid annotation target format: " + target;
        }
        
        // Check if target exists
        if (!isValidAnnotationTarget(target, context)) {
            return "Annotation target does not exist: " + target;
        }
        
        // Validate individual annotations
        if (annotations.getAnnotations() != null) {
            for (CsdlAnnotation annotation : annotations.getAnnotations()) {
                String error = validateAnnotation(annotation, context);
                if (error != null) {
                    return error;
                }
            }
        }
        
        return null;
    }
    
    private String validateInlineAnnotations(java.util.List<CsdlAnnotation> annotations, ValidationContext context) {
        if (annotations != null && !annotations.isEmpty()) {
            for (CsdlAnnotation annotation : annotations) {
                String error = validateAnnotation(annotation, context);
                if (error != null) {
                    return error;
                }
            }
        }
        return null;
    }
    
    private String validateAnnotation(CsdlAnnotation annotation, ValidationContext context) {
        String term = annotation.getTerm();
        
        // Validate term format and content
        return validateAnnotationTerm(term, context);
    }
    
    private String validateAnnotationTerm(String term, ValidationContext context) {
        if (term == null || term.trim().isEmpty()) {
            return "Annotation term cannot be null or empty";
        }

        // Check basic format (from original ODataNamingValidator)
        if (!isValidAnnotationTermFormat(term)) {
            return "Invalid annotation term format: " + term;
        }

        // Check if term is from a known vocabulary namespace
        if (!isKnownVocabularyTerm(term, context.getImportedNamespaces(), context.getCurrentSchemaNamespaces())) {
            return "Undefined annotation term: " + term;
        }
        
        return null;
    }
    
    private boolean isValidAnnotationTermFormat(String term) {
        if (term == null || term.trim().isEmpty()) {
            return false;
        }

        // Check for invalid characters (from original implementation)
        if (term.contains("!") || term.contains("?") || term.contains("<") || term.contains(">")) {
            return false;
        }

        // Term should have at least one dot (namespace.termname)
        if (!term.contains(".")) {
            return false;
        }

        // Validate each segment
        String[] segments = term.split("\\.");
        for (String segment : segments) {
            if (!isValidODataIdentifier(segment)) {
                return false;
            }
        }

        return true;
    }
    
    private boolean isValidODataIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        // OData identifier: starts with letter or underscore, followed by letters, digits, or underscores
        return identifier.matches("^[A-Za-z_][A-Za-z0-9_]*$");
    }
    
    private boolean isValidAnnotationTargetFormat(String target) {
        if (target == null || target.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation - target should contain valid identifiers and dots
        return target.matches("^[A-Za-z_][A-Za-z0-9_.]*$");
    }
    
    private boolean isKnownVocabularyTerm(String term, Set<String> importedNamespaces, Set<String> currentSchemaNamespaces) {
        // Check if term starts with a known vocabulary
        for (String vocab : KNOWN_VOCABULARIES) {
            if (term.startsWith(vocab + ".")) {
                return true;
            }
        }

        // Check imported namespaces (assume they contain valid vocabularies)
        for (String namespace : importedNamespaces) {
            if (term.startsWith(namespace + ".")) {
                return true;
            }
        }

        // Check current schema namespaces (terms defined in current file)
        for (String namespace : currentSchemaNamespaces) {
            if (term.startsWith(namespace + ".")) {
                return true;
            }
        }

        // If not from known vocabularies, it's likely undefined
        return false;
    }
    
    private boolean isValidAnnotationTarget(String target, ValidationContext context) {
        // Check if target is in the defined targets set
        if (context.getDefinedTargets().contains(target)) {
            return true;
        }

        // For qualified targets, also check property/navigation property paths
        if (target.contains("/")) {
            String basePath = target.substring(0, target.lastIndexOf("/"));
            if (context.getDefinedTargets().contains(basePath)) {
                return true; // Assume property paths are valid if base entity exists
            }
        }

        // Check if it's a container-qualified target
        String[] parts = target.split("\\.");
        if (parts.length >= 2) {
            // Try to match container.entityset pattern
            String containerPart = String.join(".", java.util.Arrays.copyOfRange(parts, 0, parts.length - 1));
            if (context.getDefinedTargets().contains(containerPart)) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public long getEstimatedExecutionTime() {
        return 300; // Annotation validation can be more complex
    }
}
