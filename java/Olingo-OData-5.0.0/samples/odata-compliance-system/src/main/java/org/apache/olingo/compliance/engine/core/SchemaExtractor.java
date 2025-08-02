package org.apache.olingo.compliance.engine.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.server.core.MetadataParser;

/**
 * Schema信息提取器 - 使用Olingo原生解析器
 * 重构后优先使用Olingo的MetadataParser和CsdlSchema数据结构
 */
public class SchemaExtractor {
    
    private final MetadataParser metadataParser;
    
    public SchemaExtractor() {
        this.metadataParser = new MetadataParser();
        // 配置解析器
        this.metadataParser.useLocalCoreVocabularies(true);
        this.metadataParser.parseAnnotations(true);
    }
    
    /**
     * 从XML文件中提取Schema定义 - 使用Olingo原生解析器
     * @param xmlFile XML文件
     * @return Schema定义列表
     */
    public List<SchemaRegistry.SchemaDefinition> extractSchemas(File xmlFile) {
        List<SchemaRegistry.SchemaDefinition> schemas = new ArrayList<>();
        
        try (InputStream inputStream = new FileInputStream(xmlFile);
             java.io.InputStreamReader reader = new java.io.InputStreamReader(inputStream, "UTF-8")) {
            
            // 使用Olingo的MetadataParser解析
            org.apache.olingo.server.core.SchemaBasedEdmProvider edmProvider = metadataParser.buildEdmProvider(reader);
            
            // 从EdmProvider获取Schema列表
            List<CsdlSchema> csdlSchemas = edmProvider.getSchemas();
            
            for (CsdlSchema csdlSchema : csdlSchemas) {
                // 转换Olingo的CsdlSchema为我们的SchemaDefinition
                List<SchemaRegistry.TypeDefinition> types = extractTypesFromCsdlSchema(csdlSchema);
                
                // 如果Olingo没有找到注解，回退到XML解析获取注解
                // List<SchemaRegistry.TypeDefinition> xmlAnnotations = extractAnnotationsFromXML(xmlFile, csdlSchema.getNamespace());
                // types.addAll(xmlAnnotations);
                
                SchemaRegistry.SchemaDefinition schema = new OlingoSchemaDefinition(
                    csdlSchema.getNamespace(), 
                    csdlSchema.getAlias(), 
                    xmlFile.getAbsolutePath(), 
                    types,
                    csdlSchema // 保留原始Olingo对象
                );
                schemas.add(schema);
            }
            
        } catch (Exception e) {
            // 如果Olingo解析失败，记录错误但不抛出异常，让验证器处理
            System.err.println("Failed to extract schema from " + xmlFile.getName() + " using Olingo parser: " + e.getMessage());
            e.printStackTrace();
            // 这里可以考虑回退到基础XML解析，但通常Olingo解析失败说明文件确实有问题
        }
        
        return schemas;
    }
    
    /**
     * 从Olingo的CsdlSchema中提取类型定义
     */
    private List<SchemaRegistry.TypeDefinition> extractTypesFromCsdlSchema(CsdlSchema csdlSchema) {
        List<SchemaRegistry.TypeDefinition> types = new ArrayList<>();
        
        // 提取EntityType
        if (csdlSchema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : csdlSchema.getEntityTypes()) {
                types.add(new OlingoTypeDefinition(
                    entityType.getName(), 
                    "EntityType", 
                    entityType.getBaseType(),
                    entityType
                ));
                
                // 提取NavigationProperty
                if (entityType.getNavigationProperties() != null) {
                    for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                        types.add(new OlingoEntityElementDefinition(
                            navProp.getName(),
                            "NavigationProperty",
                            entityType.getName(),
                            navProp.getType(),
                            navProp
                        ));
                        
                        // 提取ReferentialConstraint
                        if (navProp.getReferentialConstraints() != null) {
                            for (CsdlReferentialConstraint refConstraint : navProp.getReferentialConstraints()) {
                                String constraintKey = refConstraint.getProperty() + "->" + refConstraint.getReferencedProperty();
                                types.add(new OlingoEntityElementDefinition(
                                    constraintKey,
                                    "ReferentialConstraint",
                                    entityType.getName(),
                                    navProp.getName(),
                                    refConstraint
                                ));
                            }
                        }
                    }
                }
            }
        }
        
        // 提取ComplexType
        if (csdlSchema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : csdlSchema.getComplexTypes()) {
                types.add(new OlingoTypeDefinition(
                    complexType.getName(),
                    "ComplexType",
                    complexType.getBaseType(),
                    complexType
                ));
            }
        }
        
        // 提取EnumType
        if (csdlSchema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : csdlSchema.getEnumTypes()) {
                types.add(new OlingoTypeDefinition(
                    enumType.getName(),
                    "EnumType",
                    null,
                    enumType
                ));
            }
        }
        
        // 提取TypeDefinition
        if (csdlSchema.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : csdlSchema.getTypeDefinitions()) {
                types.add(new OlingoTypeDefinition(
                    typeDef.getName(),
                    "TypeDefinition",
                    typeDef.getUnderlyingType(),
                    typeDef
                ));
            }
        }
        
        // 提取Function
        if (csdlSchema.getFunctions() != null) {
            for (CsdlFunction function : csdlSchema.getFunctions()) {
                types.add(new OlingoTypeDefinition(
                    function.getName(),
                    "Function",
                    null,
                    function
                ));
            }
        }
        
        // 提取Action
        if (csdlSchema.getActions() != null) {
            for (CsdlAction action : csdlSchema.getActions()) {
                types.add(new OlingoTypeDefinition(
                    action.getName(),
                    "Action",
                    null,
                    action
                ));
            }
        }
        
        // 提取Term
        if (csdlSchema.getTerms() != null) {
            for (CsdlTerm term : csdlSchema.getTerms()) {
                types.add(new OlingoTypeDefinition(
                    term.getName(),
                    "Term",
                    term.getType(),
                    term
                ));
            }
        }
        
        // 提取EntityContainer
        if (csdlSchema.getEntityContainer() != null) {
            CsdlEntityContainer container = csdlSchema.getEntityContainer();
            types.add(new OlingoTypeDefinition(
                container.getName(),
                "EntityContainer",
                null,
                container
            ));
            
            // 提取EntitySet
            if (container.getEntitySets() != null) {
                for (CsdlEntitySet entitySet : container.getEntitySets()) {
                    types.add(new OlingoContainerElementDefinition(
                        entitySet.getName(),
                        "EntitySet",
                        container.getName(),
                        entitySet.getType(),
                        entitySet
                    ));
                }
            }
            
            // 提取Singleton
            if (container.getSingletons() != null) {
                for (CsdlSingleton singleton : container.getSingletons()) {
                    types.add(new OlingoContainerElementDefinition(
                        singleton.getName(),
                        "Singleton",
                        container.getName(),
                        singleton.getType(),
                        singleton
                    ));
                }
            }
            
            // 提取FunctionImport
            if (container.getFunctionImports() != null) {
                for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                    types.add(new OlingoContainerElementDefinition(
                        functionImport.getName(),
                        "FunctionImport",
                        container.getName(),
                        functionImport.getFunction(),
                        functionImport
                    ));
                }
            }
            
            // 提取ActionImport
            if (container.getActionImports() != null) {
                for (CsdlActionImport actionImport : container.getActionImports()) {
                    types.add(new OlingoContainerElementDefinition(
                        actionImport.getName(),
                        "ActionImport",
                        container.getName(),
                        actionImport.getAction(),
                        actionImport
                    ));
                }
            }
        }
        
        // 提取Annotations
        if (csdlSchema.getAnnotationGroups() != null) {
            for (CsdlAnnotations annotations : csdlSchema.getAnnotationGroups()) {
                if (annotations.getAnnotations() != null) {
                    for (CsdlAnnotation annotation : annotations.getAnnotations()) {
                        types.add(new OlingoAnnotationDefinition(
                            annotations.getTarget(),
                            annotation.getTerm(),
                            "Annotation",
                            annotation
                        ));
                    }
                }
            }
        }
        
        return types;
    }
    
    /**
     * 当Olingo无法正确提取注解时，回退到XML解析
     */
    private List<SchemaRegistry.TypeDefinition> extractAnnotationsFromXML(File xmlFile, String namespace) {
        List<SchemaRegistry.TypeDefinition> annotations = new ArrayList<>();
        
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(xmlFile);
            
            // 查找所有Annotations元素
            org.w3c.dom.NodeList annotationsNodes = doc.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "Annotations");
            
            for (int i = 0; i < annotationsNodes.getLength(); i++) {
                org.w3c.dom.Element annotationsElement = (org.w3c.dom.Element) annotationsNodes.item(i);
                String target = annotationsElement.getAttribute("Target");
                
                // 查找该Annotations下的所有Annotation元素
                org.w3c.dom.NodeList annotationNodes = annotationsElement.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edm", "Annotation");
                
                for (int j = 0; j < annotationNodes.getLength(); j++) {
                    org.w3c.dom.Element annotationElement = (org.w3c.dom.Element) annotationNodes.item(j);
                    String term = annotationElement.getAttribute("Term");
                    
                    annotations.add(new OlingoAnnotationDefinition(
                        target,
                        term,
                        "Annotation",
                        annotationElement // 存储XML Element
                    ));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to extract annotations from XML: " + e.getMessage());
        }
        
        return annotations;
    }    
    /**
     * 基于Olingo CsdlSchema的Schema定义实现
     */
    private static class OlingoSchemaDefinition implements SchemaRegistry.SchemaDefinition {
        private final String namespace;
        private final String alias;
        private final String filePath;
        private final List<SchemaRegistry.TypeDefinition> types;
        private final CsdlSchema csdlSchema; // 保留原始Olingo对象
        
        public OlingoSchemaDefinition(String namespace, String alias, String filePath, 
                                    List<SchemaRegistry.TypeDefinition> types, CsdlSchema csdlSchema) {
            this.namespace = namespace;
            this.alias = alias != null ? alias : "";
            this.filePath = filePath != null ? filePath : "";
            this.types = types != null ? new ArrayList<>(types) : new ArrayList<>();
            this.csdlSchema = csdlSchema;
        }
        
        @Override
        public String getNamespace() { return namespace; }
        
        @Override
        public String getAlias() { return alias; }
        
        @Override
        public String getFilePath() { return filePath; }
        
        @Override
        public List<SchemaRegistry.TypeDefinition> getTypes() {
            return Collections.unmodifiableList(types);
        }
        
        /**
         * 获取原始的Olingo CsdlSchema对象
         */
        public CsdlSchema getCsdlSchema() { return csdlSchema; }
    }
    
    /**
     * 基于Olingo对象的类型定义实现
     */
    private static class OlingoTypeDefinition implements SchemaRegistry.TypeDefinition {
        private final String name;
        private final String kind;
        private final String baseType;
        private final Object csdlObject; // 保留原始Olingo对象
        
        public OlingoTypeDefinition(String name, String kind, String baseType, Object csdlObject) {
            this.name = name != null ? name : "";
            this.kind = kind != null ? kind : "";
            this.baseType = baseType;
            this.csdlObject = csdlObject;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getKind() { return kind; }
        
        @Override
        public String getBaseType() { return baseType; }
        
        /**
         * 获取原始的Olingo CSDL对象
         */
        public Object getCsdlObject() { return csdlObject; }
    }
    
    /**
     * 容器元素类型定义实现（基于Olingo对象）
     */
    private static class OlingoContainerElementDefinition implements SchemaRegistry.TypeDefinition {
        private final String name;
        private final String kind;
        private final String containerName;
        private final String targetType;
        private final Object csdlObject;
        
        public OlingoContainerElementDefinition(String name, String kind, String containerName, 
                                              String targetType, Object csdlObject) {
            this.name = name != null ? name : "";
            this.kind = kind != null ? kind : "";
            this.containerName = containerName != null ? containerName : "";
            this.targetType = targetType;
            this.csdlObject = csdlObject;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getKind() { return kind; }
        
        @Override
        public String getBaseType() { return targetType; }
        
        public String getContainerName() { return containerName; }
        
        public Object getCsdlObject() { return csdlObject; }
    }
    
    /**
     * 实体元素类型定义实现（基于Olingo对象）
     */
    private static class OlingoEntityElementDefinition implements SchemaRegistry.TypeDefinition {
        private final String name;
        private final String kind;
        private final String entityName;
        private final String targetType;
        private final Object csdlObject;
        
        public OlingoEntityElementDefinition(String name, String kind, String entityName, 
                                           String targetType, Object csdlObject) {
            this.name = name != null ? name : "";
            this.kind = kind != null ? kind : "";
            this.entityName = entityName != null ? entityName : "";
            this.targetType = targetType;
            this.csdlObject = csdlObject;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getKind() { return kind; }
        
        @Override
        public String getBaseType() { return targetType; }
        
        public String getEntityName() { return entityName; }
        
        public Object getCsdlObject() { return csdlObject; }
    }

    /**
     * 注解定义类（基于Olingo对象） - 设为公开
     */
    public static class OlingoAnnotationDefinition implements SchemaRegistry.TypeDefinition {
        private final String target;
        private final String term;
        private final String kind;
        private final Object csdlObject;

        public OlingoAnnotationDefinition(String target, String term, String kind, Object csdlObject) {
            this.target = target;
            this.term = term;
            this.kind = kind;
            this.csdlObject = csdlObject;
        }

        @Override
        public String getName() { return target + ":" + term; }

        @Override
        public String getKind() { return kind; }

        @Override
        public String getBaseType() { return null; }

        public String getTarget() { return target; }

        public String getTerm() { return term; }
        
        public Object getCsdlObject() { return csdlObject; }
    }
}
