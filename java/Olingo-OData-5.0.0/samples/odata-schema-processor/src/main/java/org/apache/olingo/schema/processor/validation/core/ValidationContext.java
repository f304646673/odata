package org.apache.olingo.schema.processor.validation.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context object that holds validation state and results during the validation process.
 */
public class ValidationContext {
    private final List<String> errors;
    private final List<String> warnings;
    private final Set<String> referencedNamespaces;
    private final Set<String> importedNamespaces;
    private final Set<String> currentSchemaNamespaces; // 添加当前Schema的命名空间集合
    private final Set<String> definedTargets; // 新增：跟踪所有已定义的可注解目标
    private final Map<String, Object> metadata;
    private final String fileName;

    public ValidationContext(List<String> errors, List<String> warnings,
                           Set<String> referencedNamespaces, Set<String> importedNamespaces,
                           Map<String, Object> metadata, String fileName) {
        this.errors = errors;
        this.warnings = warnings;
        this.referencedNamespaces = referencedNamespaces;
        this.importedNamespaces = importedNamespaces;
        this.currentSchemaNamespaces = new java.util.HashSet<>(); // 初始化当前Schema命名空间
        this.definedTargets = new java.util.HashSet<>(); // 初始化已定义目标集合
        this.metadata = metadata;
        this.fileName = fileName;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Set<String> getReferencedNamespaces() {
        return referencedNamespaces;
    }

    public Set<String> getImportedNamespaces() {
        return importedNamespaces;
    }

    public Set<String> getCurrentSchemaNamespaces() {
        return currentSchemaNamespaces;
    }

    public Set<String> getDefinedTargets() {
        return definedTargets;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getFileName() {
        return fileName;
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public void addReferencedNamespace(String namespace) {
        referencedNamespaces.add(namespace);
    }

    public void addImportedNamespace(String namespace) {
        importedNamespaces.add(namespace);
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public void addDefinedTarget(String target) {
        definedTargets.add(target);
    }

    public void addCurrentSchemaNamespace(String namespace) {
        currentSchemaNamespaces.add(namespace);
    }
}
