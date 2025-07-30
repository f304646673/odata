package org.apache.olingo.schema.processor.validation.file.validators;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.schema.processor.validation.file.core.NamingValidator;
import org.apache.olingo.schema.processor.validation.file.core.ValidationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validator for CSDL Annotation elements.
 */
public class AnnotationValidator {

    private final NamingValidator namingValidator;

    public AnnotationValidator(NamingValidator namingValidator) {
        this.namingValidator = namingValidator;
    }

    /**
     * Validates inline annotations
     */
    public void validateInlineAnnotations(List<CsdlAnnotation> annotations, ValidationContext context) {
        if (annotations != null && !annotations.isEmpty()) {
            for (CsdlAnnotation annotation : annotations) {
                validateAnnotationTerm(annotation.getTerm(), context);
            }
        }
    }

    /**
     * Validates annotation term
     */
    public void validateAnnotationTerm(String term, ValidationContext context) {
        if (term == null || term.trim().isEmpty()) {
            context.addError("Annotation term cannot be null or empty");
            return;
        }

        // Check basic format
        if (!namingValidator.isValidAnnotationTermFormat(term)) {
            context.addError("Invalid annotation term format: " + term);
            return;
        }

        // Check if term is from a known vocabulary namespace
        if (!isKnownVocabularyTerm(term, context.getImportedNamespaces(), context.getCurrentSchemaNamespaces())) {
            context.addError("Undefined annotation term: " + term);
        }
    }

    /**
     * Validates annotation target
     */
    public void validateAnnotationTarget(String target, ValidationContext context) {
        if (target == null || target.trim().isEmpty()) {
            context.addError("Annotation target cannot be null or empty");
            return;
        }

        // Basic format validation
        if (!namingValidator.isValidAnnotationTargetFormat(target)) {
            context.addError("Invalid annotation target format: " + target);
            return;
        }

        // Check if target exists in defined schema elements
        if (!isValidAnnotationTarget(target, context)) {
            context.addError("Annotation target does not exist: " + target);
        }
    }

    /**
     * Check if term is from a known vocabulary
     */
    private boolean isKnownVocabularyTerm(String term, Set<String> importedNamespaces, Set<String> currentSchemaNamespaces) {
        // Known OData vocabularies
        Set<String> knownVocabularies = new HashSet<>();
        knownVocabularies.add("Core");
        knownVocabularies.add("Measures");
        knownVocabularies.add("Capabilities");
        knownVocabularies.add("Validation");
        knownVocabularies.add("UI");
        knownVocabularies.add("Common");
        knownVocabularies.add("Communication");
        knownVocabularies.add("PersonalData");
        knownVocabularies.add("Analytics");
        knownVocabularies.add("Aggregation");
        knownVocabularies.add("Authorization");
        knownVocabularies.add("Session");
        knownVocabularies.add("Temporal");

        // Check if term starts with a known vocabulary
        for (String vocab : knownVocabularies) {
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

    /**
     * Check if annotation target is valid (exists in schema)
     */
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
}
