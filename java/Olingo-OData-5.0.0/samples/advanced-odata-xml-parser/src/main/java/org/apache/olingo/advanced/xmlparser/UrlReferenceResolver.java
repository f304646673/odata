package org.apache.olingo.advanced.xmlparser;

import java.io.InputStream;
import java.net.URI;

import org.apache.olingo.server.core.ReferenceResolver;
/**
 * URL Reference Resolver
 */
public class UrlReferenceResolver implements ReferenceResolver {
    @Override
    public InputStream resolveReference(URI referenceUri, String xmlBase) {
        try {
            if (referenceUri.isAbsolute()) {
                return referenceUri.toURL().openStream();
            } else if (xmlBase != null) {
                URI baseUri = URI.create(xmlBase);
                URI resolvedUri = baseUri.resolve(referenceUri);
                return resolvedUri.toURL().openStream();
            }
        } catch (Exception e) {
            // Ignore and return null
        }
        return null;
    }
}