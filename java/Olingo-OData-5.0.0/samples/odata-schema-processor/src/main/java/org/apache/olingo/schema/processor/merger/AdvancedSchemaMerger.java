package org.apache.olingo.schema.processor.merger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 高级Schema合并器
 * 支持多文件、多namespace的Schema合并，确保元素唯一性
 */
public class AdvancedSchemaMerger {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedSchemaMerger.class);
    
    /**
     * 合并多个Schema文件中的Schema
     * @param schemasByFile 按文件分组的Schema列表
     * @return 合并结果
     */
    public MergeResult mergeSchemas(Map<String, List<CsdlSchema>> schemasByFile) {
        Map<String, CsdlSchema> mergedSchemas = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Set<String>> duplicateElements = new HashMap<>();
        
        // 按namespace收集所有Schema
        Map<String, List<CsdlSchema>> schemasByNamespace = new HashMap<>();
        for (Map.Entry<String, List<CsdlSchema>> entry : schemasByFile.entrySet()) {
            String fileName = entry.getKey();
            for (CsdlSchema schema : entry.getValue()) {
                schemasByNamespace.computeIfAbsent(schema.getNamespace(), k -> new ArrayList<>()).add(schema);
            }
        }
        
        // 合并同名namespace的Schema
        for (Map.Entry<String, List<CsdlSchema>> entry : schemasByNamespace.entrySet()) {
            String namespace = entry.getKey();
            List<CsdlSchema> schemas = entry.getValue();
            
            if (schemas.size() == 1) {
                // 单个Schema，直接使用
                mergedSchemas.put(namespace, schemas.get(0));
            } else {
                // 多个Schema需要合并
                MergeResult singleNamespaceResult = mergeSingleNamespace(namespace, schemas);
                if (singleNamespaceResult.isSuccess()) {
                    mergedSchemas.put(namespace, singleNamespaceResult.getMergedSchemas().get(namespace));
                } else {
                    errors.addAll(singleNamespaceResult.getErrors());
                }
                warnings.addAll(singleNamespaceResult.getWarnings());
                duplicateElements.putAll(singleNamespaceResult.getDuplicateElements());
            }
        }
        
        return new MergeResult(
            errors.isEmpty(),
            mergedSchemas,
            errors,
            warnings,
            duplicateElements
        );
    }
    
    /**
     * 合并单个namespace下的多个Schema
     */
    private MergeResult mergeSingleNamespace(String namespace, List<CsdlSchema> schemas) {
        CsdlSchema mergedSchema = new CsdlSchema();
        mergedSchema.setNamespace(namespace);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Set<String>> duplicateElements = new HashMap<>();
        
        // 合并各类型的元素
        mergeEntityTypes(mergedSchema, schemas, errors, warnings, duplicateElements);
        mergeComplexTypes(mergedSchema, schemas, errors, warnings, duplicateElements);
        mergeEnumTypes(mergedSchema, schemas, errors, warnings, duplicateElements);
        mergeActions(mergedSchema, schemas, errors, warnings, duplicateElements);
        mergeFunctions(mergedSchema, schemas, errors, warnings, duplicateElements);
        mergeEntityContainers(mergedSchema, schemas, errors, warnings, duplicateElements);
        mergeAnnotations(mergedSchema, schemas, errors, warnings, duplicateElements);
        mergeReferences(mergedSchema, schemas, errors, warnings);
        
        Map<String, CsdlSchema> result = new HashMap<>();
        result.put(namespace, mergedSchema);
        
        return new MergeResult(
            errors.isEmpty(),
            result,
            errors,
            warnings,
            duplicateElements
        );
    }
    
    /**
     * 合并EntityType
     */
    private void mergeEntityTypes(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                                 List<String> errors, List<String> warnings,
                                 Map<String, Set<String>> duplicateElements) {
        Map<String, CsdlEntityType> entityTypes = new HashMap<>();
        String namespace = mergedSchema.getNamespace();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String name = entityType.getName();
                    if (entityTypes.containsKey(name)) {
                        // 检查是否完全相同
                        if (!areEntityTypesEquivalent(entityTypes.get(name), entityType)) {
                            errors.add(String.format("Duplicate EntityType '%s' found in namespace '%s' with different definitions", name, namespace));
                            duplicateElements.computeIfAbsent("EntityType", k -> new HashSet<>()).add(name);
                        } else {
                            warnings.add(String.format("Duplicate but identical EntityType '%s' found in namespace '%s'", name, namespace));
                        }
                    } else {
                        entityTypes.put(name, entityType);
                    }
                }
            }
        }
        
        if (!entityTypes.isEmpty()) {
            mergedSchema.setEntityTypes(new ArrayList<>(entityTypes.values()));
        }
    }
    
    /**
     * 合并ComplexType
     */
    private void mergeComplexTypes(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                                  List<String> errors, List<String> warnings,
                                  Map<String, Set<String>> duplicateElements) {
        Map<String, CsdlComplexType> complexTypes = new HashMap<>();
        String namespace = mergedSchema.getNamespace();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    String name = complexType.getName();
                    if (complexTypes.containsKey(name)) {
                        if (!areComplexTypesEquivalent(complexTypes.get(name), complexType)) {
                            errors.add(String.format("Duplicate ComplexType '%s' found in namespace '%s' with different definitions", name, namespace));
                            duplicateElements.computeIfAbsent("ComplexType", k -> new HashSet<>()).add(name);
                        } else {
                            warnings.add(String.format("Duplicate but identical ComplexType '%s' found in namespace '%s'", name, namespace));
                        }
                    } else {
                        complexTypes.put(name, complexType);
                    }
                }
            }
        }
        
        if (!complexTypes.isEmpty()) {
            mergedSchema.setComplexTypes(new ArrayList<>(complexTypes.values()));
        }
    }
    
    /**
     * 合并EnumType
     */
    private void mergeEnumTypes(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                               List<String> errors, List<String> warnings,
                               Map<String, Set<String>> duplicateElements) {
        Map<String, CsdlEnumType> enumTypes = new HashMap<>();
        String namespace = mergedSchema.getNamespace();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getEnumTypes() != null) {
                for (CsdlEnumType enumType : schema.getEnumTypes()) {
                    String name = enumType.getName();
                    if (enumTypes.containsKey(name)) {
                        if (!areEnumTypesEquivalent(enumTypes.get(name), enumType)) {
                            errors.add(String.format("Duplicate EnumType '%s' found in namespace '%s' with different definitions", name, namespace));
                            duplicateElements.computeIfAbsent("EnumType", k -> new HashSet<>()).add(name);
                        } else {
                            warnings.add(String.format("Duplicate but identical EnumType '%s' found in namespace '%s'", name, namespace));
                        }
                    } else {
                        enumTypes.put(name, enumType);
                    }
                }
            }
        }
        
        if (!enumTypes.isEmpty()) {
            mergedSchema.setEnumTypes(new ArrayList<>(enumTypes.values()));
        }
    }
    
    /**
     * 合并Action
     */
    private void mergeActions(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                             List<String> errors, List<String> warnings,
                             Map<String, Set<String>> duplicateElements) {
        Map<String, CsdlAction> actions = new HashMap<>();
        String namespace = mergedSchema.getNamespace();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getActions() != null) {
                for (CsdlAction action : schema.getActions()) {
                    String key = getActionKey(action);
                    if (actions.containsKey(key)) {
                        if (!areActionsEquivalent(actions.get(key), action)) {
                            errors.add(String.format("Duplicate Action '%s' found in namespace '%s' with different definitions", action.getName(), namespace));
                            duplicateElements.computeIfAbsent("Action", k -> new HashSet<>()).add(action.getName());
                        } else {
                            warnings.add(String.format("Duplicate but identical Action '%s' found in namespace '%s'", action.getName(), namespace));
                        }
                    } else {
                        actions.put(key, action);
                    }
                }
            }
        }
        
        if (!actions.isEmpty()) {
            mergedSchema.setActions(new ArrayList<>(actions.values()));
        }
    }
    
    /**
     * 合并Function
     */
    private void mergeFunctions(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                               List<String> errors, List<String> warnings,
                               Map<String, Set<String>> duplicateElements) {
        Map<String, CsdlFunction> functions = new HashMap<>();
        String namespace = mergedSchema.getNamespace();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getFunctions() != null) {
                for (CsdlFunction function : schema.getFunctions()) {
                    String key = getFunctionKey(function);
                    if (functions.containsKey(key)) {
                        if (!areFunctionsEquivalent(functions.get(key), function)) {
                            errors.add(String.format("Duplicate Function '%s' found in namespace '%s' with different definitions", function.getName(), namespace));
                            duplicateElements.computeIfAbsent("Function", k -> new HashSet<>()).add(function.getName());
                        } else {
                            warnings.add(String.format("Duplicate but identical Function '%s' found in namespace '%s'", function.getName(), namespace));
                        }
                    } else {
                        functions.put(key, function);
                    }
                }
            }
        }
        
        if (!functions.isEmpty()) {
            mergedSchema.setFunctions(new ArrayList<>(functions.values()));
        }
    }
    
    /**
     * 合并EntityContainer
     */
    private void mergeEntityContainers(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                                      List<String> errors, List<String> warnings,
                                      Map<String, Set<String>> duplicateElements) {
        CsdlEntityContainer mergedContainer = null;
        String namespace = mergedSchema.getNamespace();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getEntityContainer() != null) {
                if (mergedContainer == null) {
                    mergedContainer = schema.getEntityContainer();
                } else {
                    // 需要合并EntityContainer
                    MergeContainerResult containerResult = mergeEntityContainers(mergedContainer, schema.getEntityContainer(), namespace);
                    mergedContainer = containerResult.getMergedContainer();
                    errors.addAll(containerResult.getErrors());
                    warnings.addAll(containerResult.getWarnings());
                }
            }
        }
        
        if (mergedContainer != null) {
            mergedSchema.setEntityContainer(mergedContainer);
        }
    }
    
    /**
     * 合并Annotations
     */
    private void mergeAnnotations(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                                 List<String> errors, List<String> warnings,
                                 Map<String, Set<String>> duplicateElements) {
        List<CsdlAnnotations> allAnnotations = new ArrayList<>();
        Map<String, CsdlAnnotations> annotationsByTarget = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getAnnotationGroups() != null) {
                for (CsdlAnnotations annotations : schema.getAnnotationGroups()) {
                    String target = annotations.getTarget();
                    if (annotationsByTarget.containsKey(target)) {
                        // 合并同一target的annotations
                        CsdlAnnotations existing = annotationsByTarget.get(target);
                        CsdlAnnotations merged = mergeAnnotationsForTarget(existing, annotations);
                        annotationsByTarget.put(target, merged);
                        warnings.add(String.format("Merged annotations for target '%s' in namespace '%s'", target, mergedSchema.getNamespace()));
                    } else {
                        annotationsByTarget.put(target, annotations);
                    }
                }
            }
        }
        
        if (!annotationsByTarget.isEmpty()) {
            mergedSchema.setAnnotationsGroup(new ArrayList<>(annotationsByTarget.values()));
        }
    }
    
    /**
     * 注意：CsdlSchema不直接支持References，此处为占位符
     * 实际应用中应考虑从XML或其他来源管理References
     */
    private void mergeReferences(CsdlSchema mergedSchema, List<CsdlSchema> schemas,
                                List<String> errors, List<String> warnings) {
        // 暂不支持References合并，因为CsdlSchema没有这个功能
        logger.warn("References merging is not supported in current Olingo version");
    }
    
    // 辅助方法用于检查元素等价性
    private boolean areEntityTypesEquivalent(CsdlEntityType et1, CsdlEntityType et2) {
        if (!Objects.equals(et1.getName(), et2.getName())) return false;
        if (!Objects.equals(et1.getBaseType(), et2.getBaseType())) return false;
        if (!Objects.equals(et1.isAbstract(), et2.isAbstract())) return false;
        if (!Objects.equals(et1.isOpenType(), et2.isOpenType())) return false;
        if (!Objects.equals(et1.hasStream(), et2.hasStream())) return false;
        
        // 比较属性
        if (!arePropertiesEquivalent(et1.getProperties(), et2.getProperties())) return false;
        
        // 比较导航属性
        if (!areNavigationPropertiesEquivalent(et1.getNavigationProperties(), et2.getNavigationProperties())) return false;
        
        // 比较键
        if (!areKeysEquivalent(et1.getKey(), et2.getKey())) return false;
        
        return true;
    }
    
    private boolean areComplexTypesEquivalent(CsdlComplexType ct1, CsdlComplexType ct2) {
        if (!Objects.equals(ct1.getName(), ct2.getName())) return false;
        if (!Objects.equals(ct1.getBaseType(), ct2.getBaseType())) return false;
        if (!Objects.equals(ct1.isAbstract(), ct2.isAbstract())) return false;
        if (!Objects.equals(ct1.isOpenType(), ct2.isOpenType())) return false;
        
        return arePropertiesEquivalent(ct1.getProperties(), ct2.getProperties());
    }
    
    private boolean areEnumTypesEquivalent(CsdlEnumType et1, CsdlEnumType et2) {
        if (!Objects.equals(et1.getName(), et2.getName())) return false;
        if (!Objects.equals(et1.getUnderlyingType(), et2.getUnderlyingType())) return false;
        if (!Objects.equals(et1.isFlags(), et2.isFlags())) return false;
        
        // 比较枚举成员
        List<CsdlEnumMember> members1 = et1.getMembers();
        List<CsdlEnumMember> members2 = et2.getMembers();
        
        if ((members1 == null) != (members2 == null)) return false;
        if (members1 != null && members1.size() != members2.size()) return false;
        
        if (members1 != null) {
            for (int i = 0; i < members1.size(); i++) {
                CsdlEnumMember m1 = members1.get(i);
                CsdlEnumMember m2 = members2.get(i);
                if (!Objects.equals(m1.getName(), m2.getName()) ||
                    !Objects.equals(m1.getValue(), m2.getValue())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean areActionsEquivalent(CsdlAction a1, CsdlAction a2) {
        if (!Objects.equals(a1.getName(), a2.getName())) return false;
        if (!Objects.equals(a1.isBound(), a2.isBound())) return false;
        if (!Objects.equals(a1.getEntitySetPath(), a2.getEntitySetPath())) return false;
        
        // 比较参数
        return areParametersEquivalent(a1.getParameters(), a2.getParameters());
    }
    
    private boolean areFunctionsEquivalent(CsdlFunction f1, CsdlFunction f2) {
        if (!Objects.equals(f1.getName(), f2.getName())) return false;
        if (!Objects.equals(f1.isBound(), f2.isBound())) return false;
        if (!Objects.equals(f1.isComposable(), f2.isComposable())) return false;
        if (!Objects.equals(f1.getEntitySetPath(), f2.getEntitySetPath())) return false;
        
        // 比较参数
        if (!areParametersEquivalent(f1.getParameters(), f2.getParameters())) return false;
        
        // 比较返回类型
        return areReturnTypesEquivalent(f1.getReturnType(), f2.getReturnType());
    }
    
    private boolean arePropertiesEquivalent(List<CsdlProperty> props1, List<CsdlProperty> props2) {
        if ((props1 == null) != (props2 == null)) return false;
        if (props1 != null && props1.size() != props2.size()) return false;
        
        if (props1 != null) {
            Map<String, CsdlProperty> map1 = props1.stream().collect(Collectors.toMap(CsdlProperty::getName, p -> p));
            Map<String, CsdlProperty> map2 = props2.stream().collect(Collectors.toMap(CsdlProperty::getName, p -> p));
            
            if (!map1.keySet().equals(map2.keySet())) return false;
            
            for (String name : map1.keySet()) {
                CsdlProperty p1 = map1.get(name);
                CsdlProperty p2 = map2.get(name);
                if (!arePropertyDefinitionsEquivalent(p1, p2)) return false;
            }
        }
        
        return true;
    }
    
    private boolean areNavigationPropertiesEquivalent(List<CsdlNavigationProperty> navProps1, List<CsdlNavigationProperty> navProps2) {
        if ((navProps1 == null) != (navProps2 == null)) return false;
        if (navProps1 != null && navProps1.size() != navProps2.size()) return false;
        
        if (navProps1 != null) {
            Map<String, CsdlNavigationProperty> map1 = navProps1.stream().collect(Collectors.toMap(CsdlNavigationProperty::getName, p -> p));
            Map<String, CsdlNavigationProperty> map2 = navProps2.stream().collect(Collectors.toMap(CsdlNavigationProperty::getName, p -> p));
            
            if (!map1.keySet().equals(map2.keySet())) return false;
            
            for (String name : map1.keySet()) {
                CsdlNavigationProperty np1 = map1.get(name);
                CsdlNavigationProperty np2 = map2.get(name);
                if (!areNavigationPropertyDefinitionsEquivalent(np1, np2)) return false;
            }
        }
        
        return true;
    }
    
    private boolean areKeysEquivalent(List<CsdlPropertyRef> keys1, List<CsdlPropertyRef> keys2) {
        if ((keys1 == null) != (keys2 == null)) return false;
        if (keys1 != null && keys1.size() != keys2.size()) return false;
        
        if (keys1 != null) {
            for (int i = 0; i < keys1.size(); i++) {
                if (!Objects.equals(keys1.get(i).getName(), keys2.get(i).getName())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean areParametersEquivalent(List<CsdlParameter> params1, List<CsdlParameter> params2) {
        if ((params1 == null) != (params2 == null)) return false;
        if (params1 != null && params1.size() != params2.size()) return false;
        
        if (params1 != null) {
            for (int i = 0; i < params1.size(); i++) {
                CsdlParameter p1 = params1.get(i);
                CsdlParameter p2 = params2.get(i);
                if (!Objects.equals(p1.getName(), p2.getName()) ||
                    !Objects.equals(p1.getType(), p2.getType()) ||
                    !Objects.equals(p1.isCollection(), p2.isCollection()) ||
                    !Objects.equals(p1.isNullable(), p2.isNullable())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean areReturnTypesEquivalent(CsdlReturnType rt1, CsdlReturnType rt2) {
        if ((rt1 == null) != (rt2 == null)) return false;
        if (rt1 == null) return true;
        
        return Objects.equals(rt1.getType(), rt2.getType()) &&
               Objects.equals(rt1.isCollection(), rt2.isCollection()) &&
               Objects.equals(rt1.isNullable(), rt2.isNullable());
    }
    
    private boolean arePropertyDefinitionsEquivalent(CsdlProperty p1, CsdlProperty p2) {
        return Objects.equals(p1.getName(), p2.getName()) &&
               Objects.equals(p1.getType(), p2.getType()) &&
               Objects.equals(p1.isCollection(), p2.isCollection()) &&
               Objects.equals(p1.isNullable(), p2.isNullable()) &&
               Objects.equals(p1.getMaxLength(), p2.getMaxLength()) &&
               Objects.equals(p1.getPrecision(), p2.getPrecision()) &&
               Objects.equals(p1.getScale(), p2.getScale()) &&
               Objects.equals(p1.isUnicode(), p2.isUnicode());
    }
    
    private boolean areNavigationPropertyDefinitionsEquivalent(CsdlNavigationProperty np1, CsdlNavigationProperty np2) {
        return Objects.equals(np1.getName(), np2.getName()) &&
               Objects.equals(np1.getType(), np2.getType()) &&
               Objects.equals(np1.isCollection(), np2.isCollection()) &&
               Objects.equals(np1.isNullable(), np2.isNullable()) &&
               Objects.equals(np1.getPartner(), np2.getPartner()) &&
               Objects.equals(np1.isContainsTarget(), np2.isContainsTarget());
    }
    
    // 注意：由于CsdlSchema不支持References，此方法暂时保留为占位符
    // private boolean areReferencesEquivalent(CsdlReference r1, CsdlReference r2) {
    //     return Objects.equals(r1.getUri(), r2.getUri());
    // }
    
    private String getActionKey(CsdlAction action) {
        // Action重载基于参数类型
        StringBuilder key = new StringBuilder(action.getName());
        if (action.getParameters() != null) {
            for (CsdlParameter param : action.getParameters()) {
                key.append("_").append(param.getType());
            }
        }
        return key.toString();
    }
    
    private String getFunctionKey(CsdlFunction function) {
        // Function重载基于参数类型
        StringBuilder key = new StringBuilder(function.getName());
        if (function.getParameters() != null) {
            for (CsdlParameter param : function.getParameters()) {
                key.append("_").append(param.getType());
            }
        }
        return key.toString();
    }
    
    private MergeContainerResult mergeEntityContainers(CsdlEntityContainer container1, CsdlEntityContainer container2, String namespace) {
        // 实现EntityContainer合并逻辑
        CsdlEntityContainer merged = new CsdlEntityContainer();
        merged.setName(container1.getName() != null ? container1.getName() : container2.getName());
        merged.setExtendsContainer(container1.getExtendsContainer() != null ? container1.getExtendsContainer() : container2.getExtendsContainer());
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 合并EntitySet
        Map<String, CsdlEntitySet> entitySets = new HashMap<>();
        if (container1.getEntitySets() != null) {
            for (CsdlEntitySet es : container1.getEntitySets()) {
                entitySets.put(es.getName(), es);
            }
        }
        if (container2.getEntitySets() != null) {
            for (CsdlEntitySet es : container2.getEntitySets()) {
                if (entitySets.containsKey(es.getName())) {
                    if (!areEntitySetsEquivalent(entitySets.get(es.getName()), es)) {
                        errors.add(String.format("Duplicate EntitySet '%s' with different definitions in namespace '%s'", es.getName(), namespace));
                    }
                } else {
                    entitySets.put(es.getName(), es);
                }
            }
        }
        merged.setEntitySets(new ArrayList<>(entitySets.values()));
        
        // 类似地合并Singleton、ActionImport、FunctionImport等
        
        return new MergeContainerResult(merged, errors, warnings);
    }
    
    private boolean areEntitySetsEquivalent(CsdlEntitySet es1, CsdlEntitySet es2) {
        return Objects.equals(es1.getName(), es2.getName()) &&
               Objects.equals(es1.getType(), es2.getType()) &&
               Objects.equals(es1.isIncludeInServiceDocument(), es2.isIncludeInServiceDocument());
    }
    
    private CsdlAnnotations mergeAnnotationsForTarget(CsdlAnnotations existing, CsdlAnnotations additional) {
        CsdlAnnotations merged = new CsdlAnnotations();
        merged.setTarget(existing.getTarget());
        merged.setQualifier(existing.getQualifier());
        
        List<CsdlAnnotation> allAnnotations = new ArrayList<>();
        if (existing.getAnnotations() != null) {
            allAnnotations.addAll(existing.getAnnotations());
        }
        if (additional.getAnnotations() != null) {
            allAnnotations.addAll(additional.getAnnotations());
        }
        
        merged.setAnnotations(allAnnotations);
        return merged;
    }
    
    /**
     * 合并结果类
     */
    public static class MergeResult {
        private final boolean success;
        private final Map<String, CsdlSchema> mergedSchemas;
        private final List<String> errors;
        private final List<String> warnings;
        private final Map<String, Set<String>> duplicateElements;
        
        public MergeResult(boolean success, Map<String, CsdlSchema> mergedSchemas,
                          List<String> errors, List<String> warnings,
                          Map<String, Set<String>> duplicateElements) {
            this.success = success;
            this.mergedSchemas = mergedSchemas;
            this.errors = errors;
            this.warnings = warnings;
            this.duplicateElements = duplicateElements;
        }
        
        public boolean isSuccess() { return success; }
        public Map<String, CsdlSchema> getMergedSchemas() { return mergedSchemas; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public Map<String, Set<String>> getDuplicateElements() { return duplicateElements; }
    }
    
    /**
     * EntityContainer合并结果
     */
    private static class MergeContainerResult {
        private final CsdlEntityContainer mergedContainer;
        private final List<String> errors;
        private final List<String> warnings;
        
        public MergeContainerResult(CsdlEntityContainer mergedContainer, List<String> errors, List<String> warnings) {
            this.mergedContainer = mergedContainer;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public CsdlEntityContainer getMergedContainer() { return mergedContainer; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }
}
