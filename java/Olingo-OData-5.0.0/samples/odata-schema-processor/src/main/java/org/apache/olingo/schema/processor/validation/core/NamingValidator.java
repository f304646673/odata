package org.apache.olingo.schema.processor.validation.core;

/**
 * Interface for validating OData naming conventions and format rules.
 */
public interface NamingValidator {

    /**
     * Validates if a namespace follows OData 4.0 naming rules.
     *
     * @param namespace the namespace to validate
     * @return true if valid, false otherwise
     */
    boolean isValidODataNamespace(String namespace);

    /**
     * Validates if an identifier follows OData naming rules.
     *
     * @param identifier the identifier to validate
     * @return true if valid, false otherwise
     */
    boolean isValidODataIdentifier(String identifier);

    /**
     * Validates if an annotation term format is valid.
     *
     * @param term the annotation term to validate
     * @return true if valid, false otherwise
     */
    boolean isValidAnnotationTermFormat(String term);

    /**
     * Validates if an annotation target format is valid.
     *
     * @param target the annotation target to validate
     * @return true if valid, false otherwise
     */
    boolean isValidAnnotationTargetFormat(String target);
}
