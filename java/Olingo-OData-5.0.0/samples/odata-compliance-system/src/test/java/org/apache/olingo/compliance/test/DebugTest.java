package org.apache.olingo.compliance.test;

import org.apache.olingo.compliance.validation.directory.DirectoryValidationManager;
import org.apache.olingo.compliance.validation.directory.SchemaConflictDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DebugTest {
    public static void main(String[] args) throws Exception {
        DirectoryValidationManager validationManager = new DirectoryValidationManager();
        String directoryPath = "src/test/resources/validation/multiple/invalid/scenario2-alias-conflicts";
        
        // 手动解析 Schema 信息
        File dir = new File(directoryPath);
        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml"));
        
        System.out.println("=== 手动解析Schema信息 ===");
        for (File xmlFile : xmlFiles) {
            System.out.println("File: " + xmlFile.getName());
            List<DirectoryValidationManager.SchemaInfo> schemas = parseSchemaInfo(xmlFile);
            for (DirectoryValidationManager.SchemaInfo schema : schemas) {
                System.out.println("  Namespace: " + schema.getNamespace());
                System.out.println("  Alias: " + schema.getAlias());
                System.out.println("  FilePath: " + schema.getFilePath());
            }
        }
        
        System.out.println("\n=== 运行目录验证 ===");
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        System.out.println("Result: " + result);
        System.out.println("Is valid: " + result.isValid());
        System.out.println("Total files: " + result.getTotalFiles());
        System.out.println("Conflicts: " + result.getConflictIssues().size());
        result.getConflictIssues().forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
    
    private static List<DirectoryValidationManager.SchemaInfo> parseSchemaInfo(File xmlFile) throws Exception {
        List<DirectoryValidationManager.SchemaInfo> schemas = new ArrayList<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        
        // 查找所有Schema元素
        NodeList schemaNodes = document.getElementsByTagNameNS("*", "Schema");
        
        for (int i = 0; i < schemaNodes.getLength(); i++) {
            Element schemaElement = (Element) schemaNodes.item(i);
            String namespace = schemaElement.getAttribute("Namespace");
            String alias = schemaElement.getAttribute("Alias");
            
            if (namespace != null && !namespace.isEmpty()) {
                DirectoryValidationManager.SchemaInfo schemaInfo = new DirectoryValidationManager.SchemaInfo(
                    namespace,
                    alias,
                    xmlFile.getAbsolutePath(),
                    new HashSet<>()
                );
                schemas.add(schemaInfo);
            }
        }
        
        return schemas;
    }
}
