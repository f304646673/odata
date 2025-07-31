package org.apache.olingo.compliance.engine.core;

import org.apache.olingo.compliance.engine.core.SchemaRegistry.SchemaDefinition;
import org.apache.olingo.compliance.engine.core.SchemaRegistry.TypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Schema信息提取器
 * 从XML文件中提取Schema定义信息，不进行验证，只提取结构
 */
public class SchemaExtractor {
    
    /**
     * 从XML文件中提取Schema定义
     * @param xmlFile XML文件
     * @return Schema定义列表
     */
    public List<SchemaDefinition> extractSchemas(File xmlFile) {
        List<SchemaDefinition> schemas = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // 安全配置
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            // 查找所有Schema元素
            NodeList schemaNodes = document.getElementsByTagNameNS("*", "Schema");
            
            for (int i = 0; i < schemaNodes.getLength(); i++) {
                Element schemaElement = (Element) schemaNodes.item(i);
                String namespace = schemaElement.getAttribute("Namespace");
                String alias = schemaElement.getAttribute("Alias");
                
                if (namespace != null && !namespace.isEmpty()) {
                    List<TypeDefinition> types = extractTypes(schemaElement, namespace);
                    SchemaDefinition schema = new SchemaDefinition(
                        namespace, 
                        alias, 
                        xmlFile.getAbsolutePath(), 
                        types
                    );
                    schemas.add(schema);
                }
            }
            
        } catch (Exception e) {
            // 提取失败时返回空列表，不抛出异常
            System.err.println("Warning: Failed to extract schemas from " + xmlFile.getName() + ": " + e.getMessage());
        }
        
        return schemas;
    }
    
    /**
     * 从Schema元素中提取类型定义
     */
    private List<TypeDefinition> extractTypes(Element schemaElement, String namespace) {
        List<TypeDefinition> types = new ArrayList<>();
        
        // 提取EntityType
        NodeList entityTypes = schemaElement.getElementsByTagNameNS("*", "EntityType");
        for (int i = 0; i < entityTypes.getLength(); i++) {
            Element element = (Element) entityTypes.item(i);
            String name = element.getAttribute("Name");
            String baseType = element.getAttribute("BaseType");
            
            if (name != null && !name.isEmpty()) {
                types.add(new TypeDefinition(name, "EntityType", baseType));
            }
        }
        
        // 提取ComplexType
        NodeList complexTypes = schemaElement.getElementsByTagNameNS("*", "ComplexType");
        for (int i = 0; i < complexTypes.getLength(); i++) {
            Element element = (Element) complexTypes.item(i);
            String name = element.getAttribute("Name");
            String baseType = element.getAttribute("BaseType");
            
            if (name != null && !name.isEmpty()) {
                types.add(new TypeDefinition(name, "ComplexType", baseType));
            }
        }
        
        // 提取EnumType
        NodeList enumTypes = schemaElement.getElementsByTagNameNS("*", "EnumType");
        for (int i = 0; i < enumTypes.getLength(); i++) {
            Element element = (Element) enumTypes.item(i);
            String name = element.getAttribute("Name");
            
            if (name != null && !name.isEmpty()) {
                types.add(new TypeDefinition(name, "EnumType", null));
            }
        }
        
        // 提取Action
        NodeList actions = schemaElement.getElementsByTagNameNS("*", "Action");
        for (int i = 0; i < actions.getLength(); i++) {
            Element element = (Element) actions.item(i);
            String name = element.getAttribute("Name");
            
            if (name != null && !name.isEmpty()) {
                types.add(new TypeDefinition(name, "Action", null));
            }
        }
        
        // 提取Function
        NodeList functions = schemaElement.getElementsByTagNameNS("*", "Function");
        for (int i = 0; i < functions.getLength(); i++) {
            Element element = (Element) functions.item(i);
            String name = element.getAttribute("Name");
            
            if (name != null && !name.isEmpty()) {
                types.add(new TypeDefinition(name, "Function", null));
            }
        }
        
        // 提取EntityContainer
        NodeList containers = schemaElement.getElementsByTagNameNS("*", "EntityContainer");
        for (int i = 0; i < containers.getLength(); i++) {
            Element element = (Element) containers.item(i);
            String name = element.getAttribute("Name");
            
            if (name != null && !name.isEmpty()) {
                types.add(new TypeDefinition(name, "EntityContainer", null));
            }
        }
        
        return types;
    }
}
