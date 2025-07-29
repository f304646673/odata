package org.apache.olingo.schema.processor.validation.impl;

import org.apache.olingo.schema.processor.validation.core.SchemaReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of reference resolver for OData XML files.
 */
public class XmlReferenceResolver implements SchemaReferenceResolver {

    private static final Logger logger = LoggerFactory.getLogger(XmlReferenceResolver.class);

    @Override
    public List<String> extractReferences(String xmlContent) {
        List<String> references = new ArrayList<>();

        // Pattern to handle edmx:Reference (with namespace prefix)
        String pattern = "<(?:edmx:)?Reference[^>]*Uri\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>";
        Pattern refPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = refPattern.matcher(xmlContent);

        while (matcher.find()) {
            String uri = matcher.group(1);
            references.add(uri);
            logger.debug("Found reference: {}", uri);
        }

        return references;
    }

    @Override
    public Path resolveReference(String refUri, Path baseDir) {
        try {
            URI uri = URI.create(refUri);
            if (uri.isAbsolute()) {
                return Paths.get(uri);
            } else {
                return baseDir.resolve(refUri).normalize();
            }
        } catch (Exception e) {
            logger.debug("Failed to resolve reference: {}", refUri, e);
            return null;
        }
    }
}
