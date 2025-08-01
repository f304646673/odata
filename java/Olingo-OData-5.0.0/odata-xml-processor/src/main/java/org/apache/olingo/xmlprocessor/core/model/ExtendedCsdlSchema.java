package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的CSDL Schema实现
 * 继承自标准CsdlSchema，添加了扩展功能和元数据追踪
 */
public class ExtendedCsdlSchema extends CsdlSchema {

    private String sourcePath;
    private List<String> referencedNamespaces;
    private boolean isExtended = false;

    public ExtendedCsdlSchema() {
        super();
        this.referencedNamespaces = new ArrayList<>();
        this.isExtended = true;
    }

    public ExtendedCsdlSchema(String namespace) {
        super();
        setNamespace(namespace);
        this.referencedNamespaces = new ArrayList<>();
        this.isExtended = true;
    }

    /**
     * 从标准CsdlSchema创建ExtendedCsdlSchema
     */
    public static ExtendedCsdlSchema fromCsdlSchema(CsdlSchema baseSchema) {
        ExtendedCsdlSchema extended = new ExtendedCsdlSchema();

        // 复制基本属性
        extended.setNamespace(baseSchema.getNamespace());
        extended.setAlias(baseSchema.getAlias());

        // 复制所有集合
        if (baseSchema.getEntityTypes() != null) {
            extended.setEntityTypes(new ArrayList<>(baseSchema.getEntityTypes()));
        }

        if (baseSchema.getComplexTypes() != null) {
            extended.setComplexTypes(new ArrayList<>(baseSchema.getComplexTypes()));
        }

        if (baseSchema.getEnumTypes() != null) {
            extended.setEnumTypes(new ArrayList<>(baseSchema.getEnumTypes()));
        }

        if (baseSchema.getTypeDefinitions() != null) {
            extended.setTypeDefinitions(new ArrayList<>(baseSchema.getTypeDefinitions()));
        }

        if (baseSchema.getActions() != null) {
            extended.setActions(new ArrayList<>(baseSchema.getActions()));
        }

        if (baseSchema.getFunctions() != null) {
            extended.setFunctions(new ArrayList<>(baseSchema.getFunctions()));
        }

        if (baseSchema.getTerms() != null) {
            extended.setTerms(new ArrayList<>(baseSchema.getTerms()));
        }

        if (baseSchema.getAnnotations() != null) {
            extended.setAnnotations(new ArrayList<>(baseSchema.getAnnotations()));
        }

        if (baseSchema.getEntityContainer() != null) {
            extended.setEntityContainer(baseSchema.getEntityContainer());
        }

        return extended;
    }

    /**
     * 获取源文件路径
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * 设置源文件路径
     */
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * 获取引用的命名空间列表
     */
    public List<String> getReferencedNamespaces() {
        return referencedNamespaces;
    }

    /**
     * 设置引用的命名空间列表
     */
    public void setReferencedNamespaces(List<String> referencedNamespaces) {
        this.referencedNamespaces = referencedNamespaces != null ? referencedNamespaces : new ArrayList<>();
    }

    /**
     * 添加引用的命名空间
     */
    public void addReferencedNamespace(String namespace) {
        if (namespace != null && !this.referencedNamespaces.contains(namespace)) {
            this.referencedNamespaces.add(namespace);
        }
    }

    /**
     * 是否为扩展Schema
     */
    public boolean isExtended() {
        return isExtended;
    }

    /**
     * 获取所有EntityType的数量
     */
    public int getEntityTypeCount() {
        return getEntityTypes() != null ? getEntityTypes().size() : 0;
    }

    /**
     * 获取所有ComplexType的数量
     */
    public int getComplexTypeCount() {
        return getComplexTypes() != null ? getComplexTypes().size() : 0;
    }

    /**
     * 获取所有EnumType的数量
     */
    public int getEnumTypeCount() {
        return getEnumTypes() != null ? getEnumTypes().size() : 0;
    }

    /**
     * 获取所有TypeDefinition的数量
     */
    public int getTypeDefinitionCount() {
        return getTypeDefinitions() != null ? getTypeDefinitions().size() : 0;
    }

    /**
     * 获取所有Action的数量
     */
    public int getActionCount() {
        return getActions() != null ? getActions().size() : 0;
    }

    /**
     * 获取所有Function的数量
     */
    public int getFunctionCount() {
        return getFunctions() != null ? getFunctions().size() : 0;
    }

    /**
     * 获取所有Term的数量
     */
    public int getTermCount() {
        return getTerms() != null ? getTerms().size() : 0;
    }

    /**
     * 获取所有Annotation的数量
     */
    public int getAnnotationCount() {
        return getAnnotations() != null ? getAnnotations().size() : 0;
    }

    /**
     * 获取Schema的统计信息
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("ExtendedCsdlSchema Statistics for namespace: ").append(getNamespace()).append("\n");
        stats.append("- EntityTypes: ").append(getEntityTypeCount()).append("\n");
        stats.append("- ComplexTypes: ").append(getComplexTypeCount()).append("\n");
        stats.append("- EnumTypes: ").append(getEnumTypeCount()).append("\n");
        stats.append("- TypeDefinitions: ").append(getTypeDefinitionCount()).append("\n");
        stats.append("- Actions: ").append(getActionCount()).append("\n");
        stats.append("- Functions: ").append(getFunctionCount()).append("\n");
        stats.append("- Terms: ").append(getTermCount()).append("\n");
        stats.append("- Annotations: ").append(getAnnotationCount()).append("\n");
        stats.append("- Referenced Namespaces: ").append(referencedNamespaces.size()).append("\n");
        stats.append("- Source Path: ").append(sourcePath != null ? sourcePath : "N/A");
        return stats.toString();
    }

    @Override
    public String toString() {
        return "ExtendedCsdlSchema{" +
                "namespace='" + getNamespace() + '\'' +
                ", alias='" + getAlias() + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", entityTypes=" + getEntityTypeCount() +
                ", complexTypes=" + getComplexTypeCount() +
                ", enumTypes=" + getEnumTypeCount() +
                ", referencedNamespaces=" + referencedNamespaces.size() +
                '}';
    }
}
