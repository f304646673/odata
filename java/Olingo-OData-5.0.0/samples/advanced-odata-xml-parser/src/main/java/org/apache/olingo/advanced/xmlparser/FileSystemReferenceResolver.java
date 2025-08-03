package org.apache.olingo.advanced.xmlparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.olingo.server.core.ReferenceResolver;
/**
 * File System Reference Resolver
 */
public class FileSystemReferenceResolver implements ReferenceResolver {
    @Override
    public InputStream resolveReference(URI referenceUri, String xmlBase) {
        try {
            File file = new File(referenceUri.getPath());
            if (file.exists() && file.isFile()) {
                return new FileInputStream(file);
            }
        } catch (Exception e) {
            // Ignore and return null
        }
        return null;
    }
}