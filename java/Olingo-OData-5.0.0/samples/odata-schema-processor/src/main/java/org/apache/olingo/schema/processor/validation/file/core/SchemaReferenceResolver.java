package org.apache.olingo.schema.processor.validation.file.core;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for resolving and validating schema references.
 */
public interface SchemaReferenceResolver {

    /**
     * Extracts references from XML content.
     *
     * @param xmlContent the XML content to parse
     * @return list of reference URIs
     */
    List<String> extractReferences(String xmlContent);

    /**
     * Resolves a reference URI to a file path.
     *
     * @param refUri the reference URI
     * @param baseDir the base directory for resolution
     * @return resolved path or null if cannot resolve
     */
    Path resolveReference(String refUri, Path baseDir);
}
