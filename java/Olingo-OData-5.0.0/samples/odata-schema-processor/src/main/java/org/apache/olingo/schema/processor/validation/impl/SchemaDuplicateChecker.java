package org.apache.olingo.schema.processor.validation.impl;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.schema.processor.validation.core.DuplicateChecker;
import org.apache.olingo.schema.processor.validation.core.SchemaReferenceResolver;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Implementation of duplicate checker for schema elements across multiple files.
 * Uses Olingo's native parsing mechanisms instead of manual XML parsing.
 */
public class SchemaDuplicateChecker implements DuplicateChecker {

    private static final Logger logger = LoggerFactory.getLogger(SchemaDuplicateChecker.class);
    private final SchemaReferenceResolver referenceResolver;

    public SchemaDuplicateChecker(SchemaReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public void checkGlobalSchemaDuplicates(Path xmlPath, List<String> errors) {
        try {
            // 检查并收集所有schema定义，使用Olingo机制
            Map<String, List<String>> allSchemaDefinitions = new HashMap<>();
            Set<Path> processedFiles = new HashSet<>();

            // 递归收集所有schema定义
            collectSchemaDefinitionsUsingOlingo(xmlPath, allSchemaDefinitions, processedFiles, xmlPath.getParent());

            // 检查冲突
            checkElementConflicts(allSchemaDefinitions, errors);

        } catch (Exception e) {
            logger.warn("Failed to perform pre-parse duplicate check using Olingo: {}", e.getMessage());
            // 不让验证失败，只记录问题
        }
    }

    /**
     * 使用Olingo机制递归收集schema定义
     */
    private void collectSchemaDefinitionsUsingOlingo(Path xmlPath, Map<String, List<String>> allDefinitions,
                                                    Set<Path> processedFiles, Path baseDir) {
        if (processedFiles.contains(xmlPath.normalize())) {
            return; // 避免循环引用
        }
        processedFiles.add(xmlPath.normalize());

        try {
            if (!Files.exists(xmlPath)) {
                return;
            }

            String xmlContent = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);
            String sourceInfo = "File: " + xmlPath.getFileName();

            // 使用Olingo解析schema定义
            parseSchemaDefinitionsUsingOlingo(xmlContent, sourceInfo, allDefinitions);

            // 处理引用文件
            List<String> references = referenceResolver.extractReferences(xmlContent);
            for (String refUri : references) {
                try {
                    Path refPath = referenceResolver.resolveReference(refUri, baseDir);
                    if (refPath != null) {
                        collectSchemaDefinitionsUsingOlingo(refPath, allDefinitions, processedFiles, baseDir);
                    }
                } catch (Exception e) {
                    logger.debug("Failed to resolve reference: {}", refUri, e);
                }
            }

        } catch (Exception e) {
            logger.debug("Failed to collect schema definitions from: {}", xmlPath, e);
        }
    }

    /**
     * 使用Olingo机制解析schema定义
     */
    private void parseSchemaDefinitionsUsingOlingo(String xmlContent, String sourceInfo,
                                                  Map<String, List<String>> allDefinitions) {
        try {
            // 使用Olingo的MetadataParser解析
            MetadataParser parser = new MetadataParser();
            parser.recursivelyLoadReferences(false); // 不加载引用，避免循环

            try (StringReader reader = new StringReader(xmlContent)) {
                SchemaBasedEdmProvider edmProvider = parser.buildEdmProvider(reader);

                // 获取所有schema
                List<CsdlSchema> schemas = edmProvider.getSchemas();
                if (schemas != null) {
                    for (CsdlSchema schema : schemas) {
                        extractSchemaElementsUsingOlingo(schema, sourceInfo, allDefinitions);
                    }
                }
            }

        } catch (Exception e) {
            logger.debug("Failed to parse schema definitions using Olingo for {}: {}", sourceInfo, e.getMessage());
        }
    }

    /**
     * 使用Olingo的CsdlSchema对象提取元素定义
     */
    private void extractSchemaElementsUsingOlingo(CsdlSchema schema, String sourceInfo,
                                                Map<String, List<String>> allDefinitions) {
        String namespace = schema.getNamespace();
        if (namespace == null || namespace.trim().isEmpty()) {
            return;
        }

        // 提取EntityTypes
        if (schema.getEntityTypes() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = namespace + "." + entityType.getName();
                String key = "EntityType:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取ComplexTypes
        if (schema.getComplexTypes() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullName = namespace + "." + complexType.getName();
                String key = "ComplexType:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取EnumTypes
        if (schema.getEnumTypes() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEnumType enumType : schema.getEnumTypes()) {
                String fullName = namespace + "." + enumType.getName();
                String key = "EnumType:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取Actions
        if (schema.getActions() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlAction action : schema.getActions()) {
                String fullName = namespace + "." + action.getName();
                String key = "Action:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取Functions
        if (schema.getFunctions() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlFunction function : schema.getFunctions()) {
                String fullName = namespace + "." + function.getName();
                String key = "Function:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取EntityContainer元素
        if (schema.getEntityContainer() != null) {
            extractEntityContainerElementsUsingOlingo(schema.getEntityContainer(), namespace, sourceInfo, allDefinitions);
        }
    }

    /**
     * 使用Olingo的CsdlEntityContainer对象提取容器元素
     */
    private void extractEntityContainerElementsUsingOlingo(org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer container,
                                                          String namespace, String sourceInfo,
                                                          Map<String, List<String>> allDefinitions) {
        String containerName = container.getName();
        if (containerName == null || containerName.trim().isEmpty()) {
            return;
        }

        // 提取EntitySets
        if (container.getEntitySets() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEntitySet entitySet : container.getEntitySets()) {
                String fullName = namespace + "." + containerName + "." + entitySet.getName();
                String key = "EntitySet:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取Singletons
        if (container.getSingletons() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlSingleton singleton : container.getSingletons()) {
                String fullName = namespace + "." + containerName + "." + singleton.getName();
                String key = "Singleton:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取ActionImports
        if (container.getActionImports() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlActionImport actionImport : container.getActionImports()) {
                String fullName = namespace + "." + containerName + "." + actionImport.getName();
                String key = "ActionImport:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }

        // 提取FunctionImports
        if (container.getFunctionImports() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport functionImport : container.getFunctionImports()) {
                String fullName = namespace + "." + containerName + "." + functionImport.getName();
                String key = "FunctionImport:" + fullName;
                allDefinitions.computeIfAbsent(key, k -> new ArrayList<>()).add(sourceInfo);
            }
        }
    }

    /**
     * 检查元素定义冲突
     */
    private void checkElementConflicts(Map<String, List<String>> allDefinitions, List<String> errors) {
        for (Map.Entry<String, List<String>> entry : allDefinitions.entrySet()) {
            if (entry.getValue().size() > 1) {
                String key = entry.getKey();
                List<String> sources = entry.getValue();

                String[] parts = key.split(":", 2);
                String elementType = parts[0];
                String fullName = parts[1];

                errors.add("Conflicting " + elementType + " name: " + fullName +
                          " (defined in " + sources.size() + " locations: " +
                          String.join(", ", sources) + ")");
            }
        }
    }
}
