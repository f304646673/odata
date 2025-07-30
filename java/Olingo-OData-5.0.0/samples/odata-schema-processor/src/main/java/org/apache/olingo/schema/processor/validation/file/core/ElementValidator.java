package org.apache.olingo.schema.processor.validation.file.core;

/**
 * Interface for validating specific OData elements.
 */
public interface ElementValidator<T> {

    /**
     * Validates a specific element and updates the validation context.
     *
     * @param element the element to validate
     * @param context the validation context to update
     */
    void validate(T element, ValidationContext context);

    /**
     * Returns the type of element this validator handles.
     *
     * @return the element type class
     */
    Class<T> getElementType();
}
