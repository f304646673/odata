package org.apache.olingo.schema.processor.validation.file.impl;

import org.apache.olingo.schema.processor.validation.file.core.NamingValidator;

/**
 * Implementation of naming validation rules for OData 4.0.
 */
public class ODataNamingValidator implements NamingValidator {

    @Override
    public boolean isValidODataNamespace(String namespace) {
        if (namespace == null || namespace.trim().isEmpty()) {
            return false;
        }
        // OData 4.0: Namespace = one or more dot-separated identifiers
        // Each identifier must start with a letter or underscore, followed by letters, digits, or underscores
        return namespace.matches("^([A-Za-z_][A-Za-z0-9_]*)(\\.[A-Za-z_][A-Za-z0-9_]*)*$");
    }

    @Override
    public boolean isValidODataIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        // OData identifier: starts with letter or underscore, followed by letters, digits, or underscores
        return identifier.matches("^[A-Za-z_][A-Za-z0-9_]*$");
    }

    @Override
    public boolean isValidAnnotationTermFormat(String term) {
        if (term == null || term.trim().isEmpty()) {
            return false;
        }

        // Check for invalid characters
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

    @Override
    public boolean isValidAnnotationTargetFormat(String target) {
        if (target == null || target.trim().isEmpty()) {
            return false;
        }

        // Basic validation - target should contain valid identifiers and dots
        // More complex validation would require schema context
        return target.matches("^[A-Za-z_][A-Za-z0-9_.]*$");
    }
}
