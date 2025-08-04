/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.advanced.xmlparser.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotatable;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Utility class to fix missing Qualifier attributes in annotations
 * after parsing with the standard MetadataParser.
 */
public class QualifierFixer {
    
    private final Map<String, String> qualifierCache = new HashMap<>();
    
    /**
     * Fix missing qualifiers in a SchemaBasedEdmProvider by reading the original XML
     * 
     * @param provider The parsed provider to fix
     * @param xmlFilePath Path to the original XML file
     * @throws IOException if the XML file cannot be read
     */
    public void fixQualifiers(SchemaBasedEdmProvider provider, String xmlFilePath) throws IOException {
        // Extract qualifiers from XML
        extractQualifiersFromXml(xmlFilePath);
        
        // Fix qualifiers in the provider
        fixMissingQualifiers(provider);
    }
    
    /**
     * Extract qualifiers from XML file using regex
     */
    private void extractQualifiersFromXml(String xmlFilePath) throws IOException {
        qualifierCache.clear();
        
        // Read XML content
        String xmlContent = readFileContent(xmlFilePath);
        
        // Pattern to match Annotation elements with both Term and Qualifier attributes
        Pattern pattern = Pattern.compile(
            "<Annotation\\s+(?=.*Term=\"([^\"]+)\")(?=.*Qualifier=\"([^\"]+)\")[^>]*>",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = pattern.matcher(xmlContent);
        while (matcher.find()) {
            // Extract Term and Qualifier from the full match
            String fullMatch = matcher.group(0);
            String term = extractAttribute(fullMatch, "Term");
            String qualifier = extractAttribute(fullMatch, "Qualifier");
            
            if (term != null && qualifier != null) {
                qualifierCache.put(term, qualifier);
            }
        }
    }
    
    /**
     * Extract attribute value from XML element string
     */
    private String extractAttribute(String xmlElement, String attributeName) {
        Pattern attrPattern = Pattern.compile(attributeName + "=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = attrPattern.matcher(xmlElement);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * Read file content as string
     */
    private String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
        }
        return content.toString();
    }
    
    /**
     * Fix missing qualifiers in the parsed provider
     */
    private void fixMissingQualifiers(SchemaBasedEdmProvider provider) {
        try {
            if (provider.getSchemas() == null) {
                return;
            }
            
            for (CsdlSchema schema : provider.getSchemas()) {
                // Fix qualifiers in entity types
                if (schema.getEntityTypes() != null) {
                    for (CsdlEntityType entityType : schema.getEntityTypes()) {
                        fixAnnotationQualifiers(entityType);
                    }
                }
                
                // Fix qualifiers in complex types
                if (schema.getComplexTypes() != null) {
                    for (Object complexType : schema.getComplexTypes()) {
                        if (complexType instanceof CsdlAnnotatable) {
                            fixAnnotationQualifiers((CsdlAnnotatable) complexType);
                        }
                    }
                }
                
                // Fix qualifiers in other annotatable elements
                // Add more as needed...
            }
        } catch (Exception e) {
            // Log error but don't fail the entire operation
            System.err.println("Warning: Failed to fix qualifiers: " + e.getMessage());
        }
    }
    
    /**
     * Fix qualifiers for a specific annotatable object
     */
    private void fixAnnotationQualifiers(CsdlAnnotatable annotatable) {
        if (annotatable.getAnnotations() == null) {
            return;
        }
        
        for (CsdlAnnotation annotation : annotatable.getAnnotations()) {
            String term = annotation.getTerm();
            if (term != null && annotation.getQualifier() == null && qualifierCache.containsKey(term)) {
                annotation.setQualifier(qualifierCache.get(term));
            }
        }
    }
    
    /**
     * Get the extracted qualifiers for debugging
     */
    public Map<String, String> getExtractedQualifiers() {
        return new HashMap<>(qualifierCache);
    }
}
