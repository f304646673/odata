package org.apache.olingo.compliance.validator.directory;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schema冲突检测器，用于检测同一命名空间下的元素冲突
 * 参考 samples/odata-schema-processor/src/main/java/org/apache/olingo/schema/processor/validation/directory
 */
public class SchemaConflictDetector {
    
    /**
     * 检测目录中的Schema冲突
     */
    public List<ComplianceIssue> detectConflicts(Map<String, Set<DirectoryValidationManager.SchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> conflicts = new ArrayList<>();
        
        for (Map.Entry<String, Set<DirectoryValidationManager.SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            Set<DirectoryValidationManager.SchemaInfo> schemas = entry.getValue();
            
            // 检测元素冲突
            conflicts.addAll(detectElementConflicts(namespace, schemas));
            
            // 检测同一命名空间内的别名冲突
            conflicts.addAll(detectAliasConflicts(namespace, schemas));
        }
        
        // 检测跨命名空间的别名冲突
        conflicts.addAll(detectCrossNamespaceAliasConflicts(namespaceToSchemas));
        
        return conflicts;
    }    /**
     * 检测元素名称冲突
     */
    private List<ComplianceIssue> detectElementConflicts(String namespace, Set<DirectoryValidationManager.SchemaInfo> schemas) {
        List<ComplianceIssue> conflicts = new ArrayList<>();
        Map<String, Set<String>> elementToFiles = new HashMap<>();
        
        // 收集所有元素名称及其来源文件
        for (DirectoryValidationManager.SchemaInfo schema : schemas) {
            for (String elementName : schema.getElementNames()) {
                elementToFiles.computeIfAbsent(elementName, k -> new HashSet<>())
                             .add(schema.getFilePath());
            }
        }
        
        // 检测冲突
        for (Map.Entry<String, Set<String>> entry : elementToFiles.entrySet()) {
            String elementName = entry.getKey();
            Set<String> files = entry.getValue();
            
            if (files.size() > 1) {
                // 特别处理Function和Action重载情况
                if (elementName.startsWith("Function:") || elementName.startsWith("Action:")) {
                    // Function和Action可以重载，需要检查参数签名
                    if (isValidFunctionOrActionOverload(elementName, files)) {
                        // 这是有效的重载，不是冲突
                        continue;
                    }
                }
                
                String message = String.format(
                    "Element conflict: '%s' in namespace '%s' is defined in multiple files: %s",
                    elementName, namespace, String.join(", ", files)
                );
                
                for (String filePath : files) {
                    ComplianceIssue conflict = new ComplianceIssue(
                        ComplianceErrorType.ELEMENT_CONFLICT,
                        message,
                        elementName,
                        filePath,
                        ComplianceIssue.Severity.ERROR
                    );
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检测别名冲突
     */
    private List<ComplianceIssue> detectAliasConflicts(String namespace, Set<DirectoryValidationManager.SchemaInfo> schemas) {
        List<ComplianceIssue> conflicts = new ArrayList<>();
        Map<String, Set<String>> aliasToFiles = new HashMap<>();
        
        // 收集所有别名及其来源文件
        for (DirectoryValidationManager.SchemaInfo schema : schemas) {
            String alias = schema.getAlias();
            if (alias != null && !alias.isEmpty()) {
                aliasToFiles.computeIfAbsent(alias, k -> new HashSet<>())
                           .add(schema.getFilePath());
            }
        }
        
        // 检测冲突：同一个别名映射到不同的命名空间
        for (Map.Entry<String, Set<String>> entry : aliasToFiles.entrySet()) {
            String alias = entry.getKey();
            Set<String> files = entry.getValue();
            
            if (files.size() > 1) {
                String message = String.format(
                    "Alias conflict: Alias '%s' for namespace '%s' is used in multiple files: %s",
                    alias, namespace, String.join(", ", files)
                );
                
                for (String filePath : files) {
                    ComplianceIssue conflict = new ComplianceIssue(
                        ComplianceErrorType.ALIAS_CONFLICT,
                        message,
                        alias,
                        filePath,
                        ComplianceIssue.Severity.ERROR
                    );
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检测跨命名空间的别名冲突
     * 同一个别名不能用于不同的命名空间
     */
    private List<ComplianceIssue> detectCrossNamespaceAliasConflicts(Map<String, Set<DirectoryValidationManager.SchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> conflicts = new ArrayList<>();
        Map<String, Map<String, Set<String>>> aliasToNamespaceToFiles = new HashMap<>();
        
        // 收集所有别名及其对应的命名空间和文件
        for (Map.Entry<String, Set<DirectoryValidationManager.SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            Set<DirectoryValidationManager.SchemaInfo> schemas = entry.getValue();
            
            for (DirectoryValidationManager.SchemaInfo schema : schemas) {
                String alias = schema.getAlias();
                if (alias != null && !alias.isEmpty()) {
                    aliasToNamespaceToFiles.computeIfAbsent(alias, k -> new HashMap<>())
                                          .computeIfAbsent(namespace, k -> new HashSet<>())
                                          .add(schema.getFilePath());
                }
            }
        }
        
        // 检测跨命名空间的别名冲突
        for (Map.Entry<String, Map<String, Set<String>>> aliasEntry : aliasToNamespaceToFiles.entrySet()) {
            String alias = aliasEntry.getKey();
            Map<String, Set<String>> namespaceToFiles = aliasEntry.getValue();
            
            if (namespaceToFiles.size() > 1) {
                // 同一个别名用于多个命名空间 - 这是冲突
                List<String> namespaces = new ArrayList<>(namespaceToFiles.keySet());
                Set<String> allFiles = new HashSet<>();
                for (Set<String> files : namespaceToFiles.values()) {
                    allFiles.addAll(files);
                }
                
                String message = String.format(
                    "Cross-namespace alias conflict: Alias '%s' is used by multiple namespaces: %s in files: %s",
                    alias, String.join(", ", namespaces), String.join(", ", allFiles)
                );
                
                for (String filePath : allFiles) {
                    ComplianceIssue conflict = new ComplianceIssue(
                        ComplianceErrorType.ALIAS_CONFLICT,
                        message,
                        alias,
                        filePath,
                        ComplianceIssue.Severity.ERROR
                    );
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检查Function或Action的重载是否有效
     * 在OData中，Function和Action可以重载，只要参数签名不同
     * 对于其他元素类型，相同名称视为冲突
     */
    private boolean isValidFunctionOrActionOverload(String elementName, Set<String> files) {
        try {
            // 提取元素类型和名称
            String[] parts = elementName.split(":", 2);
            if (parts.length != 2) {
                return false;
            }
            
            String elementType = parts[0];
            String functionName = parts[1];
            
            // 只有Function和Action才允许重载
            if (!"Function".equals(elementType) && !"Action".equals(elementType)) {
                return false; // 其他类型元素不允许重载
            }
            
            // 收集所有相同名称的Function/Action的参数签名
            Set<String> signatures = new HashSet<>();
            
            for (String filePath : files) {
                String signature = extractFunctionSignature(filePath, functionName, elementType);
                if (signature != null) {
                    if (signatures.contains(signature)) {
                        // 发现相同的参数签名，这是真正的冲突
                        return false;
                    }
                    signatures.add(signature);
                }
            }
            
            // 如果所有签名都不同，则这是有效的重载
            return true;
            
        } catch (Exception e) {
            // 如果解析失败，保守地认为是冲突
            return false;
        }
    }
    
    /**
     * 从XML文件中提取Function或Action的参数签名
     */
    private String extractFunctionSignature(String filePath, String functionName, String elementType) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new java.io.File(filePath));
            
            // 查找指定名称的Function或Action
            org.w3c.dom.NodeList elements = doc.getElementsByTagNameNS("*", elementType);
            
            for (int i = 0; i < elements.getLength(); i++) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) elements.item(i);
                String name = element.getAttribute("Name");
                
                if (functionName.equals(name)) {
                    // 构建参数签名
                    StringBuilder signature = new StringBuilder();
                    signature.append(functionName).append("(");
                    
                    // 获取参数
                    org.w3c.dom.NodeList parameters = element.getElementsByTagNameNS("*", "Parameter");
                    for (int j = 0; j < parameters.getLength(); j++) {
                        org.w3c.dom.Element param = (org.w3c.dom.Element) parameters.item(j);
                        if (j > 0) signature.append(",");
                        signature.append(param.getAttribute("Type"));
                    }
                    signature.append(")");
                    
                    // 对于Function，还需要包含返回类型
                    if ("Function".equals(elementType)) {
                        org.w3c.dom.NodeList returnTypes = element.getElementsByTagNameNS("*", "ReturnType");
                        if (returnTypes.getLength() > 0) {
                            org.w3c.dom.Element returnType = (org.w3c.dom.Element) returnTypes.item(0);
                            signature.append("->").append(returnType.getAttribute("Type"));
                        }
                    }
                    
                    return signature.toString();
                }
            }
            
        } catch (Exception e) {
            // 解析失败，返回null
        }
        
        return null;
    }

    /**
     * 检测循环引用
     */
    public List<ComplianceIssue> detectCircularReferences(Map<String, Set<DirectoryValidationManager.SchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> conflicts = new ArrayList<>();
        
        // 这里可以实现循环引用检测逻辑
        // 需要解析Reference元素来构建依赖图
        // 暂时返回空列表，作为扩展点
        
        return conflicts;
    }
    
    /**
     * 生成冲突检测报告
     */
    public ConflictDetectionReport generateReport(Map<String, Set<DirectoryValidationManager.SchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> elementConflicts = new ArrayList<>();
        List<ComplianceIssue> aliasConflicts = new ArrayList<>();
        List<ComplianceIssue> crossNamespaceAliasConflicts = detectCrossNamespaceAliasConflicts(namespaceToSchemas);
        List<ComplianceIssue> circularReferences = detectCircularReferences(namespaceToSchemas);
        
        // 收集所有冲突
        for (Map.Entry<String, Set<DirectoryValidationManager.SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            Set<DirectoryValidationManager.SchemaInfo> schemas = entry.getValue();
            
            if (schemas.size() > 1) {
                elementConflicts.addAll(detectElementConflicts(namespace, schemas));
                aliasConflicts.addAll(detectAliasConflicts(namespace, schemas));
            }
        }
        
        return new ConflictDetectionReport(
            elementConflicts,
            aliasConflicts,
            crossNamespaceAliasConflicts,
            circularReferences
        );
    }
    
    /**
     * 冲突检测报告
     */
    public static class ConflictDetectionReport {
        private final List<ComplianceIssue> elementConflicts;
        private final List<ComplianceIssue> aliasConflicts;
        private final List<ComplianceIssue> crossNamespaceAliasConflicts;
        private final List<ComplianceIssue> circularReferences;
        
        public ConflictDetectionReport(List<ComplianceIssue> elementConflicts,
                                     List<ComplianceIssue> aliasConflicts,
                                     List<ComplianceIssue> crossNamespaceAliasConflicts,
                                     List<ComplianceIssue> circularReferences) {
            this.elementConflicts = Collections.unmodifiableList(new ArrayList<>(elementConflicts));
            this.aliasConflicts = Collections.unmodifiableList(new ArrayList<>(aliasConflicts));
            this.crossNamespaceAliasConflicts = Collections.unmodifiableList(new ArrayList<>(crossNamespaceAliasConflicts));
            this.circularReferences = Collections.unmodifiableList(new ArrayList<>(circularReferences));
        }
        
        // Getters
        public List<ComplianceIssue> getElementConflicts() { return elementConflicts; }
        public List<ComplianceIssue> getAliasConflicts() { return aliasConflicts; }
        public List<ComplianceIssue> getCrossNamespaceAliasConflicts() { return crossNamespaceAliasConflicts; }
        public List<ComplianceIssue> getCircularReferences() { return circularReferences; }
        
        public List<ComplianceIssue> getAllConflicts() {
            List<ComplianceIssue> allConflicts = new ArrayList<>();
            allConflicts.addAll(elementConflicts);
            allConflicts.addAll(aliasConflicts);
            allConflicts.addAll(crossNamespaceAliasConflicts);
            allConflicts.addAll(circularReferences);
            return allConflicts;
        }
        
        public boolean hasConflicts() {
            return !elementConflicts.isEmpty() || 
                   !aliasConflicts.isEmpty() || 
                   !crossNamespaceAliasConflicts.isEmpty() || 
                   !circularReferences.isEmpty();
        }
        
        public int getTotalConflictCount() {
            return elementConflicts.size() + 
                   aliasConflicts.size() + 
                   crossNamespaceAliasConflicts.size() + 
                   circularReferences.size();
        }
        
        @Override
        public String toString() {
            return String.format(
                "ConflictDetectionReport{elementConflicts=%d, aliasConflicts=%d, " +
                "crossNamespaceAliasConflicts=%d, circularReferences=%d, totalConflicts=%d}",
                elementConflicts.size(), aliasConflicts.size(), 
                crossNamespaceAliasConflicts.size(), circularReferences.size(), getTotalConflictCount()
            );
        }
    }
}
