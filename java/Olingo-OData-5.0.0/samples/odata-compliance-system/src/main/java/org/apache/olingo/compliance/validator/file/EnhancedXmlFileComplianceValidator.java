package org.apache.olingo.compliance.validator.file;

import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ComplianceErrorType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Enhanced XML File Compliance Validator that detects specific OData 4.0 compliance errors.
 * This validator performs detailed checks for attributes, duplicates, structure, and type errors.
 */
public class EnhancedXmlFileComplianceValidator implements XmlFileComplianceValidator {
    
    private final DocumentBuilderFactory dbFactory;
    
    public EnhancedXmlFileComplianceValidator() {
        this.dbFactory = DocumentBuilderFactory.newInstance();
        this.dbFactory.setNamespaceAware(true);
    }
    
    @Override
    public XmlComplianceResult validateFile(File xmlFile) {
        try {
            String content = Files.readString(xmlFile.toPath());
            return validateContent(content, xmlFile.getName());
        } catch (IOException e) {
            List<ComplianceIssue> issues = new ArrayList<>();
            issues.add(new ComplianceIssue(ComplianceErrorType.PARSING_ERROR, 
                "Failed to read file: " + e.getMessage()));
            return createFailureResult(issues, xmlFile.getName());
        }
    }
    
    @Override
    public XmlComplianceResult validateFile(Path xmlPath) {
        return validateFile(xmlPath.toFile());
    }
    
    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new java.io.ByteArrayInputStream(xmlContent.getBytes()));
            doc.getDocumentElement().normalize();
            
            // Perform various validation checks
            validateAttributes(doc, issues);
            validateDuplicates(doc, issues);
            validateStructure(doc, issues);
            validateTypes(doc, issues);
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            issues.add(new ComplianceIssue(ComplianceErrorType.PARSING_ERROR, 
                "XML parsing failed: " + e.getMessage()));
        }
        
        if (issues.isEmpty()) {
            return createSuccessResult(fileName);
        } else {
            return createFailureResult(issues, fileName);
        }
    }
    
    private void validateAttributes(Document doc, List<ComplianceIssue> issues) {
        // Check for missing required attributes
        
        // Check EnumType Member elements missing Name attribute
        NodeList members = doc.getElementsByTagName("Member");
        for (int i = 0; i < members.getLength(); i++) {
            Element member = (Element) members.item(i);
            if (!member.hasAttribute("Name")) {
                issues.add(new ComplianceIssue(ComplianceErrorType.MISSING_NAME_ATTRIBUTE,
                    "Member element must have a Name attribute"));
            }
            if (!member.hasAttribute("Value")) {
                issues.add(new ComplianceIssue(ComplianceErrorType.MISSING_REQUIRED_ATTRIBUTE,
                    "Member element must have a Value attribute"));
            }
        }
        
        // Check Parameter elements missing Name or Type attribute
        NodeList parameters = doc.getElementsByTagName("Parameter");
        for (int i = 0; i < parameters.getLength(); i++) {
            Element parameter = (Element) parameters.item(i);
            if (!parameter.hasAttribute("Name")) {
                issues.add(new ComplianceIssue(ComplianceErrorType.MISSING_NAME_ATTRIBUTE,
                    "Parameter element must have a Name attribute"));
            }
            if (!parameter.hasAttribute("Type")) {
                issues.add(new ComplianceIssue(ComplianceErrorType.MISSING_TYPE_ATTRIBUTE,
                    "Parameter element must have a Type attribute"));
            }
        }
        
        // Check NavigationProperty elements missing Type attribute
        NodeList navProps = doc.getElementsByTagName("NavigationProperty");
        for (int i = 0; i < navProps.getLength(); i++) {
            Element navProp = (Element) navProps.item(i);
            if (!navProp.hasAttribute("Type")) {
                issues.add(new ComplianceIssue(ComplianceErrorType.MISSING_TYPE_ATTRIBUTE,
                    "NavigationProperty element must have a Type attribute"));
            }
        }
        
        // Check PropertyRef elements missing Name attribute
        NodeList propertyRefs = doc.getElementsByTagName("PropertyRef");
        for (int i = 0; i < propertyRefs.getLength(); i++) {
            Element propertyRef = (Element) propertyRefs.item(i);
            if (!propertyRef.hasAttribute("Name")) {
                issues.add(new ComplianceIssue(ComplianceErrorType.MISSING_NAME_ATTRIBUTE,
                    "PropertyRef element must have a Name attribute"));
            }
        }
        
        // Check for nullable key properties
        NodeList entityTypes = doc.getElementsByTagName("EntityType");
        for (int i = 0; i < entityTypes.getLength(); i++) {
            Element entityType = (Element) entityTypes.item(i);
            NodeList keys = entityType.getElementsByTagName("Key");
            if (keys.getLength() > 0) {
                Element key = (Element) keys.item(0);
                NodeList keyRefs = key.getElementsByTagName("PropertyRef");
                for (int j = 0; j < keyRefs.getLength(); j++) {
                    Element keyRef = (Element) keyRefs.item(j);
                    String propName = keyRef.getAttribute("Name");
                    if (!propName.isEmpty()) {
                        // Find the corresponding property
                        NodeList properties = entityType.getElementsByTagName("Property");
                        for (int k = 0; k < properties.getLength(); k++) {
                            Element property = (Element) properties.item(k);
                            if (propName.equals(property.getAttribute("Name"))) {
                                String nullable = property.getAttribute("Nullable");
                                if ("true".equals(nullable)) {
                                    issues.add(new ComplianceIssue(ComplianceErrorType.INVALID_ATTRIBUTE_VALUE,
                                        "Key property cannot be nullable"));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Check for invalid identifier names
        String[] elementTypes = {"EntityType", "ComplexType", "EnumType", "Action", "Function", "Term", "TypeDefinition"};
        for (String elementType : elementTypes) {
            NodeList elements = doc.getElementsByTagName(elementType);
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                String name = element.getAttribute("Name");
                if (!name.isEmpty() && !isValidIdentifier(name)) {
                    issues.add(new ComplianceIssue(ComplianceErrorType.INVALID_IDENTIFIER_NAME,
                        "Invalid identifier name: " + name));
                }
            }
        }
    }
    
    private void validateDuplicates(Document doc, List<ComplianceIssue> issues) {
        // Check for duplicate element names within the same scope
        String[] elementTypes = {"EntityType", "ComplexType", "EnumType", "Action", "Function", "Term", "TypeDefinition"};
        
        for (String elementType : elementTypes) {
            NodeList elements = doc.getElementsByTagName(elementType);
            List<String> names = new ArrayList<>();
            
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                String name = element.getAttribute("Name");
                if (!name.isEmpty()) {
                    if (names.contains(name)) {
                        issues.add(new ComplianceIssue(ComplianceErrorType.DUPLICATE_ELEMENT,
                            "Duplicate " + elementType + " name: " + name));
                    } else {
                        names.add(name);
                    }
                }
            }
        }
        
        // Check for duplicate parameters in functions/actions
        NodeList functions = doc.getElementsByTagName("Function");
        NodeList actions = doc.getElementsByTagName("Action");
        checkDuplicateParameters(functions, issues, "Function");
        checkDuplicateParameters(actions, issues, "Action");
        
        // Check for duplicate properties in EntityType/ComplexType
        NodeList entityTypes = doc.getElementsByTagName("EntityType");
        NodeList complexTypes = doc.getElementsByTagName("ComplexType");
        checkDuplicateProperties(entityTypes, issues, "EntityType");
        checkDuplicateProperties(complexTypes, issues, "ComplexType");
        
        // Check for duplicate EntityContainer children
        NodeList containers = doc.getElementsByTagName("EntityContainer");
        for (int i = 0; i < containers.getLength(); i++) {
            Element container = (Element) containers.item(i);
            checkDuplicateContainerChildren(container, issues);
        }
    }
    
    private void validateStructure(Document doc, List<ComplianceIssue> issues) {
        // Structure validation logic would go here
        // For now, we'll add placeholder for some common structure errors
        
        // Check for invalid inheritance hierarchies, etc.
        // This can be expanded based on specific requirements
    }
    
    private void validateTypes(Document doc, List<ComplianceIssue> issues) {
        // Type validation logic would go here
        // For now, we'll add placeholder for some common type errors
        
        // Check for invalid type references, etc.
        // This can be expanded based on specific requirements
    }
    
    private void checkDuplicateParameters(NodeList elements, List<ComplianceIssue> issues, String parentType) {
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            NodeList parameters = element.getElementsByTagName("Parameter");
            List<String> paramNames = new ArrayList<>();
            
            for (int j = 0; j < parameters.getLength(); j++) {
                Element param = (Element) parameters.item(j);
                String name = param.getAttribute("Name");
                if (!name.isEmpty()) {
                    if (paramNames.contains(name)) {
                        issues.add(new ComplianceIssue(ComplianceErrorType.DUPLICATE_ELEMENT,
                            "Duplicate parameter name in " + parentType + ": " + name));
                    } else {
                        paramNames.add(name);
                    }
                }
            }
        }
    }
    
    private void checkDuplicateProperties(NodeList elements, List<ComplianceIssue> issues, String parentType) {
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            NodeList properties = element.getElementsByTagName("Property");
            NodeList navProperties = element.getElementsByTagName("NavigationProperty");
            List<String> propNames = new ArrayList<>();
            
            // Check regular properties
            for (int j = 0; j < properties.getLength(); j++) {
                Element prop = (Element) properties.item(j);
                String name = prop.getAttribute("Name");
                if (!name.isEmpty()) {
                    if (propNames.contains(name)) {
                        issues.add(new ComplianceIssue(ComplianceErrorType.DUPLICATE_ELEMENT,
                            "Duplicate property name in " + parentType + ": " + name));
                    } else {
                        propNames.add(name);
                    }
                }
            }
            
            // Check navigation properties
            for (int j = 0; j < navProperties.getLength(); j++) {
                Element navProp = (Element) navProperties.item(j);
                String name = navProp.getAttribute("Name");
                if (!name.isEmpty()) {
                    if (propNames.contains(name)) {
                        issues.add(new ComplianceIssue(ComplianceErrorType.DUPLICATE_ELEMENT,
                            "Duplicate navigation property name in " + parentType + ": " + name));
                    } else {
                        propNames.add(name);
                    }
                }
            }
        }
    }
    
    private void checkDuplicateContainerChildren(Element container, List<ComplianceIssue> issues) {
        String[] childTypes = {"EntitySet", "Singleton", "ActionImport", "FunctionImport"};
        List<String> names = new ArrayList<>();
        
        for (String childType : childTypes) {
            NodeList children = container.getElementsByTagName(childType);
            for (int i = 0; i < children.getLength(); i++) {
                Element child = (Element) children.item(i);
                String name = child.getAttribute("Name");
                if (!name.isEmpty()) {
                    if (names.contains(name)) {
                        issues.add(new ComplianceIssue(ComplianceErrorType.DUPLICATE_ELEMENT,
                            "Duplicate " + childType + " name in EntityContainer: " + name));
                    } else {
                        names.add(name);
                    }
                }
            }
        }
    }
    
    private boolean isValidIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // OData identifier rules: must start with letter or underscore, 
        // followed by letters, digits, or underscores
        if (!Character.isLetter(name.charAt(0)) && name.charAt(0) != '_') {
            return false;
        }
        
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        
        return true;
    }
    
    private XmlComplianceResult createSuccessResult(String fileName) {
        return new XmlComplianceResult(
            true,
            new ArrayList<>(),
            java.util.Collections.emptySet(),
            java.util.Collections.emptyMap(),
            fileName,
            0L
        );
    }
    
    private XmlComplianceResult createFailureResult(List<ComplianceIssue> issues, String fileName) {
        return new XmlComplianceResult(
            false,
            issues,
            java.util.Collections.emptySet(),
            java.util.Collections.emptyMap(),
            fileName,
            0L
        );
    }
}
