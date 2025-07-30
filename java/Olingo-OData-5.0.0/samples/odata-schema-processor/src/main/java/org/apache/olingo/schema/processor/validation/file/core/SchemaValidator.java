package org.apache.olingo.schema.processor.validation.file.core;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Interface for validating individual CSDL schema elements.
 */
public interface SchemaValidator {

    /**
     * Validates a CSDL schema and updates the validation context.
     *
     * @param schema the CSDL schema to validate
     * @param context the validation context to update
     */
    void validate(CsdlSchema schema, ValidationContext context);
}
