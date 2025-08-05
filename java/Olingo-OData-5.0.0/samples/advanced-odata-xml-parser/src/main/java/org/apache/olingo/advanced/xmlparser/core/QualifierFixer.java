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

        // Try to read as absolute path first
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
            return content.toString();
        } catch (Exception e) {
            // If absolute path fails, try as resource
            try {
                String resourcePath = filePath;
                if (!resourcePath.startsWith("schemas/")) {
                    resourcePath = "schemas/" + resourcePath;
                }
                InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (resourceStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = resourceStream.read(buffer)) != -1) {
                        content.append(new String(buffer, 0, bytesRead, "UTF-8"));
                    }
                    resourceStream.close();
                    return content.toString();
                }
            } catch (Exception ex) {
                // Continue with original exception
            }
            throw new IOException("Could not read file: " + filePath, e);
        }
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
                // Fix qualifiers in schema-level annotations
                fixAnnotationQualifiers(schema);

                // Fix qualifiers in entity types
                if (schema.getEntityTypes() != null) {
                    for (CsdlEntityType entityType : schema.getEntityTypes()) {
                        fixAnnotationQualifiers(entityType);

                        // Fix qualifiers in entity type properties
                        if (entityType.getProperties() != null) {
                            for (Object property : entityType.getProperties()) {
                                if (property instanceof CsdlAnnotatable) {
                                    fixAnnotationQualifiers((CsdlAnnotatable) property);
                                }
                            }
                        }

                        // Fix qualifiers in navigation properties
                        if (entityType.getNavigationProperties() != null) {
                            for (Object navProperty : entityType.getNavigationProperties()) {
                                if (navProperty instanceof CsdlAnnotatable) {
                                    fixAnnotationQualifiers((CsdlAnnotatable) navProperty);
                                }
                            }
                        }
                    }
                }
                
                // Fix qualifiers in complex types
                if (schema.getComplexTypes() != null) {
                    for (Object complexType : schema.getComplexTypes()) {
                        if (complexType instanceof CsdlAnnotatable) {
                            fixAnnotationQualifiers((CsdlAnnotatable) complexType);

                            // If complex type has properties, fix them too
                            try {
                                Object properties = complexType.getClass().getMethod("getProperties").invoke(complexType);
                                if (properties instanceof java.util.List) {
                                    for (Object property : (java.util.List<?>) properties) {
                                        if (property instanceof CsdlAnnotatable) {
                                            fixAnnotationQualifiers((CsdlAnnotatable) property);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore reflection errors
                            }
                        }
                    }
                }

                // Fix qualifiers in enum types
                if (schema.getEnumTypes() != null) {
                    for (Object enumType : schema.getEnumTypes()) {
                        if (enumType instanceof CsdlAnnotatable) {
                            fixAnnotationQualifiers((CsdlAnnotatable) enumType);

                            // Fix qualifiers in enum members
                            try {
                                Object members = enumType.getClass().getMethod("getMembers").invoke(enumType);
                                if (members instanceof java.util.List) {
                                    for (Object member : (java.util.List<?>) members) {
                                        if (member instanceof CsdlAnnotatable) {
                                            fixAnnotationQualifiers((CsdlAnnotatable) member);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore reflection errors
                            }
                        }
                    }
                }

                // Fix qualifiers in type definitions
                if (schema.getTypeDefinitions() != null) {
                    for (Object typeDef : schema.getTypeDefinitions()) {
                        if (typeDef instanceof CsdlAnnotatable) {
                            fixAnnotationQualifiers((CsdlAnnotatable) typeDef);
                        }
                    }
                }

                // Fix qualifiers in terms
                if (schema.getTerms() != null) {
                    for (Object term : schema.getTerms()) {
                        if (term instanceof CsdlAnnotatable) {
                            fixAnnotationQualifiers((CsdlAnnotatable) term);
                        }
                    }
                }

                // Fix qualifiers in actions
                if (schema.getActions() != null) {
                    for (Object action : schema.getActions()) {
                        if (action instanceof CsdlAnnotatable) {
                            fixAnnotationQualifiers((CsdlAnnotatable) action);

                            // Fix qualifiers in action parameters
                            try {
                                Object parameters = action.getClass().getMethod("getParameters").invoke(action);
                                if (parameters instanceof java.util.List) {
                                    for (Object parameter : (java.util.List<?>) parameters) {
                                        if (parameter instanceof CsdlAnnotatable) {
                                            fixAnnotationQualifiers((CsdlAnnotatable) parameter);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore reflection errors
                            }
                        }
                    }
                }

                // Fix qualifiers in functions
                if (schema.getFunctions() != null) {
                    for (Object function : schema.getFunctions()) {
                        if (function instanceof CsdlAnnotatable) {
                            fixAnnotationQualifiers((CsdlAnnotatable) function);

                            // Fix qualifiers in function parameters
                            try {
                                Object parameters = function.getClass().getMethod("getParameters").invoke(function);
                                if (parameters instanceof java.util.List) {
                                    for (Object parameter : (java.util.List<?>) parameters) {
                                        if (parameter instanceof CsdlAnnotatable) {
                                            fixAnnotationQualifiers((CsdlAnnotatable) parameter);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore reflection errors
                            }
                        }
                    }
                }

                // Fix qualifiers in entity containers
                if (schema.getEntityContainer() != null) {
                    Object container = schema.getEntityContainer();
                    if (container instanceof CsdlAnnotatable) {
                        fixAnnotationQualifiers((CsdlAnnotatable) container);

                        // Fix qualifiers in entity sets
                        try {
                            Object entitySets = container.getClass().getMethod("getEntitySets").invoke(container);
                            if (entitySets instanceof java.util.List) {
                                for (Object entitySet : (java.util.List<?>) entitySets) {
                                    if (entitySet instanceof CsdlAnnotatable) {
                                        fixAnnotationQualifiers((CsdlAnnotatable) entitySet);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Ignore reflection errors
                        }

                        // Fix qualifiers in singletons
                        try {
                            Object singletons = container.getClass().getMethod("getSingletons").invoke(container);
                            if (singletons instanceof java.util.List) {
                                for (Object singleton : (java.util.List<?>) singletons) {
                                    if (singleton instanceof CsdlAnnotatable) {
                                        fixAnnotationQualifiers((CsdlAnnotatable) singleton);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Ignore reflection errors
                        }

                        // Fix qualifiers in action imports
                        try {
                            Object actionImports = container.getClass().getMethod("getActionImports").invoke(container);
                            if (actionImports instanceof java.util.List) {
                                for (Object actionImport : (java.util.List<?>) actionImports) {
                                    if (actionImport instanceof CsdlAnnotatable) {
                                        fixAnnotationQualifiers((CsdlAnnotatable) actionImport);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Ignore reflection errors
                        }

                        // Fix qualifiers in function imports
                        try {
                            Object functionImports = container.getClass().getMethod("getFunctionImports").invoke(container);
                            if (functionImports instanceof java.util.List) {
                                for (Object functionImport : (java.util.List<?>) functionImports) {
                                    if (functionImport instanceof CsdlAnnotatable) {
                                        fixAnnotationQualifiers((CsdlAnnotatable) functionImport);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Ignore reflection errors
                        }
                    }
                }
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
