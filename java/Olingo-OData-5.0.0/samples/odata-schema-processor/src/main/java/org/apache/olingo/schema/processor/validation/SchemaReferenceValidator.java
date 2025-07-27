package org.apache.olingo.schema.processor.validation;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OData Schema依赖验证器
 * 
 * 验证XML Schema文件中的类型引用是否有对应的edmx:Reference导入
 */
public class SchemaReferenceValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaReferenceValidator.class);
    
    // 匹配namespace.TypeName格式的正则表达式
    private static final Pattern NAMESPACE_TYPE_PATTERN = Pattern.compile("^([A-Za-z0-9_\\.]+)\\.([A-Za-z0-9_]+)$");
    
    // EDM内置类型
    private static final Set<String> EDM_BUILT_IN_TYPES = new HashSet<>(Arrays.asList(
        "Edm.String", "Edm.Int32", "Edm.Int64", "Edm.Boolean", "Edm.Decimal", 
        "Edm.Double", "Edm.Single", "Edm.Guid", "Edm.DateTimeOffset", "Edm.Date",
        "Edm.TimeOfDay", "Edm.Duration", "Edm.Binary", "Edm.Byte", "Edm.SByte",
        "Edm.Int16", "Edm.Stream", "Edm.Geography", "Edm.GeographyPoint",
        "Edm.GeographyLineString", "Edm.GeographyPolygon", "Edm.GeographyMultiPoint",
        "Edm.GeographyMultiLineString", "Edm.GeographyMultiPolygon", "Edm.GeographyCollection",
        "Edm.Geometry", "Edm.GeometryPoint", "Edm.GeometryLineString", "Edm.GeometryPolygon",
        "Edm.GeometryMultiPoint", "Edm.GeometryMultiLineString", "Edm.GeometryMultiPolygon",
        "Edm.GeometryCollection"
    ));
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final Set<String> referencedNamespaces;
        private final Set<String> declaredReferences;
        private final Set<String> includedNamespaces;
        
        public ValidationResult(boolean isValid, List<String> errors, 
                              Set<String> referencedNamespaces, Set<String> declaredReferences,
                              Set<String> includedNamespaces) {
            this.isValid = isValid;
            this.errors = new ArrayList<>(errors);
            this.referencedNamespaces = new HashSet<>(referencedNamespaces);
            this.declaredReferences = new HashSet<>(declaredReferences);
            this.includedNamespaces = new HashSet<>(includedNamespaces);
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return errors; }
        public Set<String> getReferencedNamespaces() { return referencedNamespaces; }
        public Set<String> getDeclaredReferences() { return declaredReferences; }
        public Set<String> getIncludedNamespaces() { return includedNamespaces; }
        public Set<String> getMissingReferences() {
            Set<String> missing = new HashSet<>(referencedNamespaces);
            missing.removeAll(includedNamespaces);
            return missing;
        }
    }
    
    /**
     * 验证XML文件的依赖引用
     */
    public ValidationResult validateSchemaReferences(Path xmlFile) throws Exception {
        List<String> errors = new ArrayList<>();
        Set<String> referencedNamespaces = new HashSet<>();
        Set<String> declaredReferences = new HashSet<>();
        Set<String> includedNamespaces = new HashSet<>();
        String currentNamespace = null;
        
        try (FileInputStream fis = new FileInputStream(xmlFile.toFile())) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(fis);
            
            while (reader.hasNext()) {
                int event = reader.next();
                
                if (event == XMLStreamReader.START_ELEMENT) {
                    String localName = reader.getLocalName();
                    
                    switch (localName) {
                        case "Reference":
                            // 解析edmx:Reference
                            String uri = reader.getAttributeValue(null, "Uri");
                            if (uri != null) {
                                declaredReferences.add(uri);
                                logger.debug("Found reference: {}", uri);
                            }
                            break;
                            
                        case "Include":
                            // 解析edmx:Include
                            String includeNamespace = reader.getAttributeValue(null, "Namespace");
                            if (includeNamespace != null) {
                                includedNamespaces.add(includeNamespace);
                                logger.debug("Found included namespace: {}", includeNamespace);
                            }
                            break;
                            
                        case "Schema":
                            // 获取当前Schema的namespace
                            currentNamespace = reader.getAttributeValue(null, "Namespace");
                            logger.debug("Current schema namespace: {}", currentNamespace);
                            break;
                            
                        case "EntityType":
                        case "ComplexType":
                            // 检查BaseType引用
                            String baseType = reader.getAttributeValue(null, "BaseType");
                            if (baseType != null) {
                                checkTypeReference(baseType, currentNamespace, referencedNamespaces);
                            }
                            break;
                            
                        case "Property":
                        case "NavigationProperty":
                            // 检查Type引用
                            String type = reader.getAttributeValue(null, "Type");
                            if (type != null) {
                                // 处理Collection(Type)格式
                                String actualType = extractActualType(type);
                                checkTypeReference(actualType, currentNamespace, referencedNamespaces);
                            }
                            break;
                            
                        case "EntitySet":
                            // 检查EntityType引用
                            String entityType = reader.getAttributeValue(null, "EntityType");
                            if (entityType != null) {
                                checkTypeReference(entityType, currentNamespace, referencedNamespaces);
                            }
                            break;
                            
                        case "ActionImport":
                            // 检查Action引用
                            String action = reader.getAttributeValue(null, "Action");
                            if (action != null) {
                                checkTypeReference(action, currentNamespace, referencedNamespaces);
                            }
                            break;
                            
                        case "FunctionImport":
                            // 检查Function引用
                            String function = reader.getAttributeValue(null, "Function");
                            if (function != null) {
                                checkTypeReference(function, currentNamespace, referencedNamespaces);
                            }
                            break;
                    }
                }
            }
            
            reader.close();
        }
        
        // 检查是否有未声明的引用
        for (String referencedNs : referencedNamespaces) {
            if (!includedNamespaces.contains(referencedNs)) {
                errors.add("Missing edmx:Reference for namespace: " + referencedNs + 
                          ". Referenced namespaces must be explicitly imported via edmx:Reference.");
            }
        }
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, referencedNamespaces, declaredReferences, includedNamespaces);
    }
    
    /**
     * 检查类型引用
     */
    private void checkTypeReference(String typeReference, String currentNamespace, 
                                   Set<String> referencedNamespaces) {
        if (typeReference == null || typeReference.trim().isEmpty()) {
            return;
        }
        
        // 跳过EDM内置类型
        if (EDM_BUILT_IN_TYPES.contains(typeReference)) {
            return;
        }
        
        // 解析namespace
        Matcher matcher = NAMESPACE_TYPE_PATTERN.matcher(typeReference);
        if (matcher.matches()) {
            String namespace = matcher.group(1);
            String typeName = matcher.group(2);
            
            // 跳过当前namespace
            if (!namespace.equals(currentNamespace)) {
                referencedNamespaces.add(namespace);
                logger.debug("Found external type reference: {} in namespace: {}", typeName, namespace);
            }
        }
    }
    
    /**
     * 提取实际类型（处理Collection(Type)格式）
     */
    private String extractActualType(String type) {
        if (type.startsWith("Collection(") && type.endsWith(")")) {
            return type.substring(11, type.length() - 1);
        }
        return type;
    }
}
