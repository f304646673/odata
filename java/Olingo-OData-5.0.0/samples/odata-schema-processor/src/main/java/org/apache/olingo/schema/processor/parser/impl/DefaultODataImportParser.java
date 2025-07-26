package org.apache.olingo.schema.processor.parser.impl;

import org.apache.olingo.schema.processor.parser.ODataImportParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认的OData导入解析器实现
 */
public class DefaultODataImportParser implements ODataImportParser {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultODataImportParser.class);
    
    // OData 4.0 XML中可能包含外部引用的属性和元素
    private static final Map<String, ODataImportParser.ReferenceType> REFERENCE_PATTERNS = new HashMap<>();
    
    static {
        // 类型引用模式
        REFERENCE_PATTERNS.put("Type", ODataImportParser.ReferenceType.ENTITY_TYPE); // 可能是EntityType, ComplexType, EnumType等
        REFERENCE_PATTERNS.put("BaseType", ODataImportParser.ReferenceType.ENTITY_TYPE);
        REFERENCE_PATTERNS.put("EntityType", ODataImportParser.ReferenceType.ENTITY_TYPE);
        REFERENCE_PATTERNS.put("Action", ODataImportParser.ReferenceType.ACTION);
        REFERENCE_PATTERNS.put("Function", ODataImportParser.ReferenceType.FUNCTION);
        REFERENCE_PATTERNS.put("EntitySet", ODataImportParser.ReferenceType.ENTITY_SET);
        REFERENCE_PATTERNS.put("Singleton", ODataImportParser.ReferenceType.SINGLETON);
        REFERENCE_PATTERNS.put("Term", ODataImportParser.ReferenceType.TERM);
        REFERENCE_PATTERNS.put("UnderlyingType", ODataImportParser.ReferenceType.TYPE_DEFINITION);
    }
    
    @Override
    public ODataImportParser.ImportParseResult parseImports(String xmlContent, String sourceFile) {
        List<ODataImportParser.ODataImport> imports = new ArrayList<>();
        List<ODataImportParser.ExternalReference> externalReferences = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
            
            // 解析导入声明
            imports.addAll(parseImportDeclarations(document, errors, warnings));
            
            // 提取已声明的namespace
            Set<String> declaredNamespaces = extractDeclaredNamespaces(document);
            
            // 提取外部引用
            externalReferences.addAll(extractExternalReferences(xmlContent, declaredNamespaces));
            
            logger.debug("Parsed {} imports and {} external references from {}", 
                        imports.size(), externalReferences.size(), sourceFile);
            
        } catch (Exception e) {
            logger.error("Failed to parse imports from {}: {}", sourceFile, e.getMessage());
            errors.add("Failed to parse XML: " + e.getMessage());
        }
        
        boolean success = errors.isEmpty();
        return new ODataImportParser.ImportParseResult(imports, externalReferences, success, errors, warnings);
    }
    
    @Override
    public List<ODataImportParser.ExternalReference> extractExternalReferences(String xmlContent, Set<String> declaredNamespaces) {
        List<ODataImportParser.ExternalReference> references = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
            
            // 遍历所有元素和属性，查找外部引用
            extractReferencesFromElement(document.getDocumentElement(), declaredNamespaces, references);
            
        } catch (Exception e) {
            logger.warn("Failed to extract external references: {}", e.getMessage());
        }
        
        return references;
    }
    
    @Override
    public ODataImportParser.ImportValidationResult validateImports(List<ODataImportParser.ODataImport> imports, List<ODataImportParser.ExternalReference> externalReferences) {
        List<String> missingImports = new ArrayList<>();
        List<String> unusedImports = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 构建导入的namespace集合
        Set<String> importedNamespaces = new HashSet<>();
        for (ODataImportParser.ODataImport imp : imports) {
            importedNamespaces.add(imp.getNamespace());
        }
        
        // 检查是否有外部引用但没有相应的导入
        Set<String> referencedNamespaces = new HashSet<>();
        for (ODataImportParser.ExternalReference ref : externalReferences) {
            if (!ref.getNamespace().isEmpty()) {
                referencedNamespaces.add(ref.getNamespace());
            }
        }
        
        for (String namespace : referencedNamespaces) {
            if (!importedNamespaces.contains(namespace)) {
                missingImports.add(namespace);
                errors.add("Missing import for namespace: " + namespace);
            }
        }
        
        // 检查是否有导入但没有被引用（这只是警告）
        for (String namespace : importedNamespaces) {
            if (!referencedNamespaces.contains(namespace)) {
                unusedImports.add(namespace);
                warnings.add("Unused import for namespace: " + namespace);
            }
        }
        
        boolean valid = errors.isEmpty();
        return new ODataImportParser.ImportValidationResult(valid, missingImports, unusedImports, errors, warnings);
    }
    
    /**
     * 解析XML中的导入声明
     */
    private List<ODataImportParser.ODataImport> parseImportDeclarations(Document document, List<String> errors, List<String> warnings) {
        List<ODataImportParser.ODataImport> imports = new ArrayList<>();
        
        try {
            // 查找Reference元素（OData 4.0导入方式）
            NodeList referenceNodes = document.getElementsByTagNameNS("*", "Reference");
            if (referenceNodes.getLength() == 0) {
                // 尝试不使用命名空间
                referenceNodes = document.getElementsByTagName("Reference");
            }
            if (referenceNodes.getLength() == 0) {
                // 尝试带edmx前缀
                referenceNodes = document.getElementsByTagName("edmx:Reference");
            }
            
            for (int i = 0; i < referenceNodes.getLength(); i++) {
                Element referenceElement = (Element) referenceNodes.item(i);
                
                // 获取URI属性
                String uri = referenceElement.getAttribute("Uri");
                if (uri == null || uri.trim().isEmpty()) {
                    warnings.add("Reference element missing Uri attribute");
                    continue;
                }
                
                // 查找Include元素
                NodeList includeNodes = referenceElement.getElementsByTagNameNS("*", "Include");
                if (includeNodes.getLength() == 0) {
                    includeNodes = referenceElement.getElementsByTagName("Include");
                }
                if (includeNodes.getLength() == 0) {
                    includeNodes = referenceElement.getElementsByTagName("edmx:Include");
                }
                
                for (int j = 0; j < includeNodes.getLength(); j++) {
                    Element includeElement = (Element) includeNodes.item(j);
                    
                    String namespace = includeElement.getAttribute("Namespace");
                    String alias = includeElement.getAttribute("Alias");
                    
                    if (namespace == null || namespace.trim().isEmpty()) {
                        errors.add("Include element missing Namespace attribute");
                        continue;
                    }
                    
                    imports.add(new ODataImportParser.ODataImport(namespace, alias, uri, null));
                }
                
                // 查找IncludeAnnotations元素
                NodeList includeAnnotationNodes = referenceElement.getElementsByTagNameNS("*", "IncludeAnnotations");
                if (includeAnnotationNodes.getLength() == 0) {
                    includeAnnotationNodes = referenceElement.getElementsByTagName("IncludeAnnotations");
                }
                if (includeAnnotationNodes.getLength() == 0) {
                    includeAnnotationNodes = referenceElement.getElementsByTagName("edmx:IncludeAnnotations");
                }
                
                for (int j = 0; j < includeAnnotationNodes.getLength(); j++) {
                    Element includeAnnotationElement = (Element) includeAnnotationNodes.item(j);
                    
                    String termNamespace = includeAnnotationElement.getAttribute("TermNamespace");
                    String qualifier = includeAnnotationElement.getAttribute("Qualifier");
                    String targetNamespace = includeAnnotationElement.getAttribute("TargetNamespace");
                    
                    if (termNamespace == null || termNamespace.trim().isEmpty()) {
                        errors.add("IncludeAnnotations element missing TermNamespace attribute");
                        continue;
                    }
                    
                    imports.add(new ODataImportParser.ODataImport(termNamespace, qualifier, uri, targetNamespace));
                }
            }
            
        } catch (Exception e) {
            errors.add("Failed to parse import declarations: " + e.getMessage());
        }
        
        return imports;
    }
    
    /**
     * 提取XML中已声明的namespace
     */
    private Set<String> extractDeclaredNamespaces(Document document) {
        Set<String> namespaces = new HashSet<>();
        
        try {
            NodeList schemaNodes = document.getElementsByTagName("Schema");
            for (int i = 0; i < schemaNodes.getLength(); i++) {
                Element schemaElement = (Element) schemaNodes.item(i);
                String namespace = schemaElement.getAttribute("Namespace");
                if (namespace != null && !namespace.trim().isEmpty()) {
                    namespaces.add(namespace);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract declared namespaces: {}", e.getMessage());
        }
        
        return namespaces;
    }
    
    /**
     * 递归遍历元素，提取外部引用
     */
    private void extractReferencesFromElement(Element element, Set<String> declaredNamespaces, List<ODataImportParser.ExternalReference> references) {
        // 检查元素的属性
        for (Map.Entry<String, ODataImportParser.ReferenceType> entry : REFERENCE_PATTERNS.entrySet()) {
            String attributeName = entry.getKey();
            ODataImportParser.ReferenceType referenceType = entry.getValue();
            
            String attributeValue = element.getAttribute(attributeName);
            if (attributeValue != null && !attributeValue.trim().isEmpty()) {
                processTypeReference(attributeValue, referenceType, declaredNamespaces, references, 
                                   element.getTagName() + "[@" + attributeName + "]");
            }
        }
        
        // 递归处理子元素
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                extractReferencesFromElement((Element) child, declaredNamespaces, references);
            }
        }
    }
    
    /**
     * 处理类型引用
     */
    private void processTypeReference(String typeValue, ODataImportParser.ReferenceType referenceType, Set<String> declaredNamespaces, 
                                    List<ODataImportParser.ExternalReference> references, String location) {
        // 处理Collection包装
        String actualType = typeValue;
        if (typeValue.startsWith("Collection(") && typeValue.endsWith(")")) {
            actualType = typeValue.substring(11, typeValue.length() - 1);
        }
        
        // 跳过EDM基础类型
        if (actualType.startsWith("Edm.")) {
            return;
        }
        
        // 检查是否是外部引用
        int lastDotIndex = actualType.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String namespace = actualType.substring(0, lastDotIndex);
            if (!declaredNamespaces.contains(namespace)) {
                references.add(new ODataImportParser.ExternalReference(actualType, referenceType, location));
            }
        }
    }
}
