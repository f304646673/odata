package org.apache.olingo.schema.processor.validation.file.core;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for checking duplicate schema elements across multiple files.
 */
public interface DuplicateChecker {

    /**
     * Checks for duplicate schema elements across all referenced files.
     *
     * @param xmlPath the main XML file path
     * @param errors list to add duplicate errors to
     */
    void checkGlobalSchemaDuplicates(Path xmlPath, List<String> errors);
}
