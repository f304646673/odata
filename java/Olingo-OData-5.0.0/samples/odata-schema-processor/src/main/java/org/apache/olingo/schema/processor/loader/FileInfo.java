package org.apache.olingo.schema.processor.loader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * 文件信息类
 * 记录单个XML文件的详细信息，包括文件路径、Schema信息、依赖关系等
 */
public class FileInfo {
    
    private final Path filePath;
    private final List<CsdlSchema> schemas;
    private final Set<String> dependencies;
    private final Set<String> dependents;
    private final Set<Path> dependentFiles;
    private final List<String> validationErrors;
    private final List<String> validationWarnings;
    private final boolean isValid;
    
    private FileInfo(Builder builder) {
        this.filePath = builder.filePath;
        this.schemas = new ArrayList<>(builder.schemas);
        this.dependencies = new HashSet<>(builder.dependencies);
        this.dependents = new HashSet<>(builder.dependents);
        this.dependentFiles = new HashSet<>();
        this.validationErrors = new ArrayList<>(builder.validationErrors);
        this.validationWarnings = new ArrayList<>(builder.validationWarnings);
        this.isValid = builder.isValid;
    }
    
    // Getters
    public Path getFilePath() { return filePath; }
    public List<CsdlSchema> getSchemas() { return new ArrayList<>(schemas); }
    public Set<String> getDependencies() { return new HashSet<>(dependencies); }
    public Set<String> getDependents() { return new HashSet<>(dependents); }
    public Set<Path> getDependentFiles() { return new HashSet<>(dependentFiles); }
    public List<String> getValidationErrors() { return new ArrayList<>(validationErrors); }
    public List<String> getValidationWarnings() { return new ArrayList<>(validationWarnings); }
    public boolean isValid() { return isValid; }
    
    /**
     * 获取文件中定义的所有命名空间
     */
    public Set<String> getDefinedNamespaces() {
        Set<String> namespaces = new HashSet<>();
        for (CsdlSchema schema : schemas) {
            if (schema.getNamespace() != null) {
                namespaces.add(schema.getNamespace());
            }
        }
        return namespaces;
    }
    
    /**
     * 添加依赖者（被其他文件依赖）
     */
    public void addDependent(String namespace) {
        dependents.add(namespace);
    }
    
    /**
     * 添加被依赖关系（哪些文件依赖于此文件）
     */
    public void addDependents(Set<Path> dependentFilePaths) {
        this.dependentFiles.addAll(dependentFilePaths);
    }
    
    /**
     * 添加单个被依赖文件
     */
    public void addDependent(Path dependentFile) {
        this.dependentFiles.add(dependentFile);
    }
    
    /**
     * 检查是否有循环依赖
     */
    public boolean hasCircularDependency(Set<String> ownNamespaces) {
        for (String dependency : dependencies) {
            if (ownNamespaces.contains(dependency)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("FileInfo{file='%s', schemas=%d, dependencies=%s, dependents=%s, valid=%s}",
                filePath.getFileName(), schemas.size(), dependencies, dependents, isValid);
    }
    
    /**
     * Builder pattern for FileInfo
     */
    public static class Builder {
        private Path filePath;
        private List<CsdlSchema> schemas = new ArrayList<>();
        private Set<String> dependencies = new HashSet<>();
        private Set<String> dependents = new HashSet<>();
        private List<String> validationErrors = new ArrayList<>();
        private List<String> validationWarnings = new ArrayList<>();
        private boolean isValid = true;
        
        public Builder(Path filePath) {
            this.filePath = filePath;
        }
        
        public Builder addSchema(CsdlSchema schema) {
            this.schemas.add(schema);
            return this;
        }
        
        public Builder addSchemas(List<CsdlSchema> schemas) {
            this.schemas.addAll(schemas);
            return this;
        }
        
        public Builder addDependency(String namespace) {
            this.dependencies.add(namespace);
            return this;
        }
        
        public Builder addDependencies(Set<String> dependencies) {
            this.dependencies.addAll(dependencies);
            return this;
        }
        
        public Builder addDependent(String namespace) {
            this.dependents.add(namespace);
            return this;
        }
        
        public Builder addValidationError(String error) {
            this.validationErrors.add(error);
            this.isValid = false;
            return this;
        }
        
        public Builder addValidationWarning(String warning) {
            this.validationWarnings.add(warning);
            return this;
        }
        
        public Builder setValid(boolean valid) {
            this.isValid = valid;
            return this;
        }
        
        public FileInfo build() {
            return new FileInfo(this);
        }
    }
}
