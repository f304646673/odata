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
        
        // 1. 检测同一命名空间下的冲突
        for (Map.Entry<String, Set<DirectoryValidationManager.SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            Set<DirectoryValidationManager.SchemaInfo> schemas = entry.getValue();
            
            if (schemas.size() > 1) {
                // 检测同一命名空间下的元素冲突
                conflicts.addAll(detectElementConflicts(namespace, schemas));
                
                // 检测Schema别名冲突
                conflicts.addAll(detectAliasConflicts(namespace, schemas));
            }
        }
        
        // 2. 检测跨命名空间的别名冲突
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
     */
    public List<ComplianceIssue> detectCrossNamespaceAliasConflicts(Map<String, Set<DirectoryValidationManager.SchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> conflicts = new ArrayList<>();
        Map<String, Map<String, String>> aliasToNamespaceAndFile = new HashMap<>(); // alias -> {namespace: file}
        
        // 收集所有别名映射
        for (Map.Entry<String, Set<DirectoryValidationManager.SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            Set<DirectoryValidationManager.SchemaInfo> schemas = entry.getValue();
            
            for (DirectoryValidationManager.SchemaInfo schema : schemas) {
                String alias = schema.getAlias();
                if (alias != null && !alias.isEmpty()) {
                    Map<String, String> namespaceFileMap = aliasToNamespaceAndFile.computeIfAbsent(alias, k -> new HashMap<>());
                    
                    // 检查是否已经有不同的命名空间使用了相同的别名
                    for (Map.Entry<String, String> existing : namespaceFileMap.entrySet()) {
                        String existingNamespace = existing.getKey();
                        String existingFile = existing.getValue();
                        
                        if (!existingNamespace.equals(namespace)) {
                            String message = String.format(
                                "Cross-namespace alias conflict: Alias '%s' is used for both namespace '%s' (in %s) and namespace '%s' (in %s)",
                                alias, existingNamespace, existingFile, namespace, schema.getFilePath()
                            );
                            
                            ComplianceIssue conflict1 = new ComplianceIssue(
                                ComplianceErrorType.ALIAS_CONFLICT,
                                message,
                                alias,
                                existingFile,
                                ComplianceIssue.Severity.ERROR
                            );
                            
                            ComplianceIssue conflict2 = new ComplianceIssue(
                                ComplianceErrorType.ALIAS_CONFLICT,
                                message,
                                alias,
                                schema.getFilePath(),
                                ComplianceIssue.Severity.ERROR
                            );
                            
                            conflicts.add(conflict1);
                            conflicts.add(conflict2);
                        }
                    }
                    
                    namespaceFileMap.put(namespace, schema.getFilePath());
                }
            }
        }
        
        return conflicts;
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
