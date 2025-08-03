package org.apache.olingo.xmlprocessor.parser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.junit.jupiter.api.Test;

public class ReferenceParsingTest {
    
    @Test
    public void testReferenceExtraction() throws Exception {
        File mainFile = new File("target/test-classes/schemas/namespace-conflicts/real-conflict-main.xml");
        
        MetadataParser parser = new MetadataParser();
        
        try (FileInputStream fis = new FileInputStream(mainFile)) {
            SchemaBasedEdmProvider provider = parser.buildEdmProvider(new InputStreamReader(fis));
            
            System.out.println("References found: " + provider.getReferences().size());
            for (EdmxReference ref : provider.getReferences()) {
                System.out.println("Reference URI: " + ref.getUri());
                System.out.println("Reference includes: " + ref.getIncludes().size());
                ref.getIncludes().forEach(include -> 
                    System.out.println("  Include namespace: " + include.getNamespace()));
            }
        }
    }
}
