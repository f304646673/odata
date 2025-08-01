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
import java.util.stream.Collectors;

/**
 * 扩展的CSDL Schema实现
 * 继承自标准CsdlSchema，添加了扩展功能和元数据追踪
 */
public class ExtendedCsdlSchema extends CsdlSchema {

    private String sourcePath;
    private List<String> referencedNamespaces;
    private boolean isExtended = true;

    public ExtendedCsdlSchema() {
        super();
        this.referencedNamespaces = new ArrayList<>();
    }

    public ExtendedCsdlSchema(String namespace) {
        super();
        setNamespace(namespace);
        this.referencedNamespaces = new ArrayList<>();
    }

    /**
     * 从标准CsdlSchema创建ExtendedCsdlSchema
     */
    public static ExtendedCsdlSchema fromCsdlSchema(CsdlSchema baseSchema) {
        ExtendedCsdlSchema extended = new ExtendedCsdlSchema();

        // 复制基本属性
        extended.setNamespace(baseSchema.getNamespace());
        extended.setAlias(baseSchema.getAlias());

        // 转换EntityTypes为ExtendedCsdlEntityType
        if (baseSchema.getEntityTypes() != null) {
            List<CsdlEntityType> extendedEntityTypes = baseSchema.getEntityTypes().stream()
                .map(entityType -> ExtendedCsdlEntityType.fromCsdlEntityType(entityType))
                .collect(Collectors.toList());
            extended.setEntityTypes(extendedEntityTypes);
        }

        // 转换ComplexTypes为ExtendedCsdlComplexType
        if (baseSchema.getComplexTypes() != null) {
            List<CsdlComplexType> extendedComplexTypes = baseSchema.getComplexTypes().stream()
                .map(complexType -> ExtendedCsdlComplexType.fromCsdlComplexType(complexType))
                .collect(Collectors.toList());
            extended.setComplexTypes(extendedComplexTypes);
        }

        // 转换EnumTypes为ExtendedCsdlEnumType
        if (baseSchema.getEnumTypes() != null) {
            List<CsdlEnumType> extendedEnumTypes = baseSchema.getEnumTypes().stream()
                .map(enumType -> ExtendedCsdlEnumType.fromCsdlEnumType(enumType))
                .collect(Collectors.toList());
            extended.setEnumTypes(extendedEnumTypes);
        }

        // 转换TypeDefinitions为ExtendedCsdlTypeDefinition
        if (baseSchema.getTypeDefinitions() != null) {
            List<CsdlTypeDefinition> extendedTypeDefinitions = baseSchema.getTypeDefinitions().stream()
                .map(typeDef -> ExtendedCsdlTypeDefinition.fromCsdlTypeDefinition(typeDef))
                .collect(Collectors.toList());
            extended.setTypeDefinitions(extendedTypeDefinitions);
        }

        // 转换Actions为ExtendedCsdlAction
        if (baseSchema.getActions() != null) {
            List<CsdlAction> extendedActions = baseSchema.getActions().stream()
                .map(action -> ExtendedCsdlAction.fromCsdlAction(action))
                .collect(Collectors.toList());
            extended.setActions(extendedActions);
        }

        // 转换Functions为ExtendedCsdlFunction
        if (baseSchema.getFunctions() != null) {
            List<CsdlFunction> extendedFunctions = baseSchema.getFunctions().stream()
                .map(function -> ExtendedCsdlFunction.fromCsdlFunction(function))
                .collect(Collectors.toList());
            extended.setFunctions(extendedFunctions);
        }

        // 转换Terms为ExtendedCsdlTerm
        if (baseSchema.getTerms() != null) {
            List<CsdlTerm> extendedTerms = baseSchema.getTerms().stream()
                .map(term -> ExtendedCsdlTerm.fromCsdlTerm(term))
                .collect(Collectors.toList());
            extended.setTerms(extendedTerms);
        }

        // 转换Annotations为ExtendedCsdlAnnotation
        if (baseSchema.getAnnotations() != null) {
            List<CsdlAnnotation> extendedAnnotations = baseSchema.getAnnotations().stream()
                .map(annotation -> ExtendedCsdlAnnotation.fromCsdlAnnotation(annotation))
                .collect(Collectors.toList());
            extended.setAnnotations(extendedAnnotations);
        }

        // 转换EntityContainer为ExtendedCsdlEntityContainer
        if (baseSchema.getEntityContainer() != null) {
            ExtendedCsdlEntityContainer extendedContainer =
                ExtendedCsdlEntityContainer.fromCsdlEntityContainer(baseSchema.getEntityContainer());
            extended.setEntityContainer(extendedContainer);
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

    @Override
    public String toString() {
        return String.format("ExtendedCsdlSchema{namespace='%s', alias='%s', sourcePath='%s', " +
                "entityTypes=%d, complexTypes=%d, enumTypes=%d, typeDefinitions=%d, " +
                "actions=%d, functions=%d, terms=%d, annotations=%d, referencedNamespaces=%s}",
                getNamespace(), getAlias(), sourcePath,
                getEntityTypeCount(), getComplexTypeCount(), getEnumTypeCount(), getTypeDefinitionCount(),
                getActionCount(), getFunctionCount(), getTermCount(), getAnnotationCount(),
                referencedNamespaces);
    }
}
