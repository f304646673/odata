package org.apache.olingo.advanced.xmlparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.olingo.server.core.ReferenceResolver;
/**
 * File Based Reference Resolver for handling local file references
 */
public  class FileBasedReferenceResolver implements ReferenceResolver {
    private final File baseDirectory;
    
    public FileBasedReferenceResolver(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
    
    @Override
    public InputStream resolveReference(URI referenceUri, String xmlBase) {
        try {
            String referencePath = referenceUri.getPath();
            
            // For absolute URI
            if (referenceUri.isAbsolute()) {
                File resolvedFile = new File(referencePath);
                if (resolvedFile.exists() && resolvedFile.isFile()) {
                    return new FileInputStream(resolvedFile);
                }
            }
            
            // Priority 1: Try as resource from classpath (for schemas/* paths)
            InputStream resourceStream = FileBasedReferenceResolver.class.getClassLoader().getResourceAsStream(referencePath);
            if (resourceStream != null) {
                return resourceStream;
            }
            
            // Priority 2: Try relative to base directory (fallback for relative paths)
            File resolvedFile = new File(baseDirectory, referencePath);
            if (resolvedFile.exists() && resolvedFile.isFile()) {
                return new FileInputStream(resolvedFile);
            }
            
            // Priority 3: Search in test resources directory
            File testResourcesDir = new File("src/test/resources");
            if (!testResourcesDir.exists()) {
                // We're running from target/test-classes
                testResourcesDir = new File("target/test-classes");
            }
            
            if (testResourcesDir.exists()) {
                File candidateFile = new File(testResourcesDir, referencePath);
                if (candidateFile.exists() && candidateFile.isFile()) {
                    return new FileInputStream(candidateFile);
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
}
