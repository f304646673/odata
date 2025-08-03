package org.apache.olingo.advanced.xmlparser;

import java.io.InputStream;
import java.net.URI;

import org.apache.olingo.server.core.ReferenceResolver;
/**
 * ClassPath Reference Resolver
 */
public class ClassPathReferenceResolver implements ReferenceResolver {
    @Override
    public InputStream resolveReference(URI referenceUri, String xmlBase) {
        try {
            String path = referenceUri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(path);
            if (resourceStream != null) {
                return resourceStream;
            }
        } catch (Exception e) {
            // Ignore and return null
        }
        return null;
    }
}
    