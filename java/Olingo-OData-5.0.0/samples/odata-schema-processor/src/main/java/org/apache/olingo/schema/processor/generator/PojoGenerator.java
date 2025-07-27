package org.apache.olingo.schema.processor.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * POJO类生成器
 * 根据OData Schema中的EntityType生成对应的Java POJO类
 */
public class PojoGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(PojoGenerator.class);
    
    private final Path outputDirectory;
    private final String basePackage;
    private final Map<String, CsdlEntityType> allEntityTypes = new HashMap<>();
    private final Map<String, String> typeMapping = new HashMap<>();
    
    public PojoGenerator(Path outputDirectory, String basePackage) {
        this.outputDirectory = outputDirectory;
        this.basePackage = basePackage;
        initializeTypeMapping();
    }
    
    /**
     * 初始化EDM类型到Java类型的映射
     */
    private void initializeTypeMapping() {
        typeMapping.put("Edm.String", "String");
        typeMapping.put("Edm.Int32", "Integer");
        typeMapping.put("Edm.Int64", "Long");
        typeMapping.put("Edm.Int16", "Short");
        typeMapping.put("Edm.Byte", "Byte");
        typeMapping.put("Edm.SByte", "Byte");
        typeMapping.put("Edm.Boolean", "Boolean");
        typeMapping.put("Edm.Decimal", "java.math.BigDecimal");
        typeMapping.put("Edm.Double", "Double");
        typeMapping.put("Edm.Single", "Float");
        typeMapping.put("Edm.Guid", "java.util.UUID");
        typeMapping.put("Edm.DateTimeOffset", "java.time.OffsetDateTime");
        typeMapping.put("Edm.Date", "java.time.LocalDate");
        typeMapping.put("Edm.TimeOfDay", "java.time.LocalTime");
        typeMapping.put("Edm.Duration", "java.time.Duration");
        typeMapping.put("Edm.Binary", "byte[]");
    }
    
    /**
     * 生成结果
     */
    public static class GenerationResult {
        private final boolean success;
        private final List<String> generatedFiles;
        private final List<String> errors;
        
        public GenerationResult(boolean success, List<String> generatedFiles, List<String> errors) {
            this.success = success;
            this.generatedFiles = new ArrayList<>(generatedFiles);
            this.errors = new ArrayList<>(errors);
        }
        
        public boolean isSuccess() { return success; }
        public List<String> getGeneratedFiles() { return generatedFiles; }
        public List<String> getErrors() { return errors; }
    }
    
    /**
     * 从Schema列表生成POJO类
     */
    public GenerationResult generateFromSchemas(List<CsdlSchema> schemas) {
        List<String> generatedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // 第一步：收集所有EntityType
            collectAllEntityTypes(schemas);
            
            // 第二步：按依赖关系排序
            List<CsdlEntityType> sortedEntityTypes = sortEntityTypesByDependency();
            
            // 第三步：生成POJO类
            for (CsdlEntityType entityType : sortedEntityTypes) {
                try {
                    String fileName = generatePojoClass(entityType);
                    generatedFiles.add(fileName);
                    logger.info("Generated POJO class: {}", fileName);
                } catch (Exception e) {
                    String error = "Failed to generate POJO for " + entityType.getName() + ": " + e.getMessage();
                    errors.add(error);
                    logger.error(error, e);
                }
            }
            
        } catch (Exception e) {
            errors.add("Generation failed: " + e.getMessage());
            logger.error("POJO generation failed", e);
        }
        
        return new GenerationResult(errors.isEmpty(), generatedFiles, errors);
    }
    
    /**
     * 收集所有EntityType
     */
    private void collectAllEntityTypes(List<CsdlSchema> schemas) {
        for (CsdlSchema schema : schemas) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = schema.getNamespace() + "." + entityType.getName();
                allEntityTypes.put(fullName, entityType);
            }
        }
    }
    
    /**
     * 按依赖关系排序EntityType（基类优先）
     */
    private List<CsdlEntityType> sortEntityTypesByDependency() {
        List<CsdlEntityType> sorted = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        for (CsdlEntityType entityType : allEntityTypes.values()) {
            addEntityTypeWithDependencies(entityType, sorted, processed);
        }
        
        return sorted;
    }
    
    /**
     * 递归添加EntityType及其依赖
     */
    private void addEntityTypeWithDependencies(CsdlEntityType entityType, List<CsdlEntityType> sorted, Set<String> processed) {
        String entityTypeName = getEntityTypeFullName(entityType);
        
        if (processed.contains(entityTypeName)) {
            return;
        }
        
        // 如果有基类型，先处理基类型
        if (entityType.getBaseType() != null) {
            CsdlEntityType baseType = allEntityTypes.get(entityType.getBaseType());
            if (baseType != null) {
                addEntityTypeWithDependencies(baseType, sorted, processed);
            }
        }
        
        sorted.add(entityType);
        processed.add(entityTypeName);
    }
    
    /**
     * 获取EntityType的全名
     */
    private String getEntityTypeFullName(CsdlEntityType entityType) {
        // 需要根据Schema的namespace来确定，这里简化处理
        for (Map.Entry<String, CsdlEntityType> entry : allEntityTypes.entrySet()) {
            if (entry.getValue() == entityType) {
                return entry.getKey();
            }
        }
        return entityType.getName();
    }
    
    /**
     * 生成单个POJO类
     */
    private String generatePojoClass(CsdlEntityType entityType) throws IOException {
        String className = entityType.getName();
        String packageName = basePackage + ".entity";
        
        StringBuilder content = new StringBuilder();
        
        // Package声明
        content.append("package ").append(packageName).append(";\n\n");
        
        // Import语句
        addImports(content, entityType);
        
        // 类注释
        content.append("/**\n");
        content.append(" * ").append(className).append(" entity class\n");
        content.append(" * Generated from OData Schema\n");
        content.append(" */\n");
        
        // 类声明
        content.append("public class ").append(className);
        
        if (entityType.getBaseType() != null) {
            String baseTypeName = extractTypeName(entityType.getBaseType());
            content.append(" extends ").append(baseTypeName);
        }
        
        content.append(" {\n\n");
        
        // 属性字段
        generateFields(content, entityType);
        
        // 构造函数
        generateConstructors(content, className, entityType);
        
        // Getter和Setter方法
        generateGettersAndSetters(content, entityType);
        
        // toString方法
        generateToString(content, className, entityType);
        
        content.append("}\n");
        
        // 写入文件
        Path packageDir = outputDirectory.resolve(packageName.replace('.', '/'));
        Files.createDirectories(packageDir);
        
        Path javaFile = packageDir.resolve(className + ".java");
        Files.write(javaFile, content.toString().getBytes(), 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        return javaFile.toString();
    }
    
    /**
     * 添加Import语句
     */
    private void addImports(StringBuilder content, CsdlEntityType entityType) {
        Set<String> imports = new HashSet<>();
        
        // 检查属性中需要的imports
        for (CsdlProperty property : entityType.getProperties()) {
            String javaType = mapToJavaType(property.getType());
            addImportIfNeeded(imports, javaType);
        }
        
        // 检查导航属性中需要的imports
        for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
            if (navProp.isCollection()) {
                imports.add("java.util.List");
                imports.add("java.util.ArrayList");
            }
        }
        
        // 写入imports
        for (String importStr : imports) {
            content.append("import ").append(importStr).append(";\n");
        }
        
        if (!imports.isEmpty()) {
            content.append("\n");
        }
    }
    
    /**
     * 如果需要则添加import
     */
    private void addImportIfNeeded(Set<String> imports, String javaType) {
        if (javaType.contains(".") && !javaType.startsWith("java.lang.")) {
            imports.add(javaType);
        }
    }
    
    /**
     * 生成字段
     */
    private void generateFields(StringBuilder content, CsdlEntityType entityType) {
        // 常规属性
        for (CsdlProperty property : entityType.getProperties()) {
            String javaType = mapToJavaType(property.getType());
            String fieldName = toCamelCase(property.getName());
            
            content.append("    private ").append(getSimpleTypeName(javaType))
                   .append(" ").append(fieldName).append(";\n");
        }
        
        // 导航属性
        for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
            String javaType;
            String fieldName = toCamelCase(navProp.getName());
            
            if (navProp.isCollection()) {
                String entityTypeName = extractTypeName(navProp.getType());
                javaType = "List<" + entityTypeName + ">";
            } else {
                javaType = extractTypeName(navProp.getType());
            }
            
            content.append("    private ").append(javaType)
                   .append(" ").append(fieldName).append(";\n");
        }
        
        content.append("\n");
    }
    
    /**
     * 生成构造函数
     */
    private void generateConstructors(StringBuilder content, String className, CsdlEntityType entityType) {
        // 默认构造函数
        content.append("    public ").append(className).append("() {\n");
        
        // 初始化导航属性集合
        for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
            if (navProp.isCollection()) {
                String fieldName = toCamelCase(navProp.getName());
                content.append("        this.").append(fieldName).append(" = new ArrayList<>();\n");
            }
        }
        
        content.append("    }\n\n");
    }
    
    /**
     * 生成Getter和Setter方法
     */
    private void generateGettersAndSetters(StringBuilder content, CsdlEntityType entityType) {
        // 常规属性的getter/setter
        for (CsdlProperty property : entityType.getProperties()) {
            String javaType = mapToJavaType(property.getType());
            String fieldName = toCamelCase(property.getName());
            String methodName = capitalize(fieldName);
            
            // Getter
            content.append("    public ").append(getSimpleTypeName(javaType))
                   .append(" get").append(methodName).append("() {\n");
            content.append("        return ").append(fieldName).append(";\n");
            content.append("    }\n\n");
            
            // Setter
            content.append("    public void set").append(methodName)
                   .append("(").append(getSimpleTypeName(javaType)).append(" ").append(fieldName).append(") {\n");
            content.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
            content.append("    }\n\n");
        }
        
        // 导航属性的getter/setter
        for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
            String fieldName = toCamelCase(navProp.getName());
            String methodName = capitalize(fieldName);
            String javaType;
            
            if (navProp.isCollection()) {
                String entityTypeName = extractTypeName(navProp.getType());
                javaType = "List<" + entityTypeName + ">";
            } else {
                javaType = extractTypeName(navProp.getType());
            }
            
            // Getter
            content.append("    public ").append(javaType)
                   .append(" get").append(methodName).append("() {\n");
            content.append("        return ").append(fieldName).append(";\n");
            content.append("    }\n\n");
            
            // Setter
            content.append("    public void set").append(methodName)
                   .append("(").append(javaType).append(" ").append(fieldName).append(") {\n");
            content.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
            content.append("    }\n\n");
        }
    }
    
    /**
     * 生成toString方法
     */
    private void generateToString(StringBuilder content, String className, CsdlEntityType entityType) {
        content.append("    @Override\n");
        content.append("    public String toString() {\n");
        content.append("        return \"").append(className).append("{\" +\n");
        
        boolean first = true;
        for (CsdlProperty property : entityType.getProperties()) {
            String fieldName = toCamelCase(property.getName());
            if (!first) {
                content.append(" +\n");
            }
            content.append("                \"").append(first ? "" : ", ").append(fieldName).append("=\" + ").append(fieldName);
            first = false;
        }
        
        content.append(" +\n                '}';\n");
        content.append("    }\n");
    }
    
    /**
     * 将EDM类型映射到Java类型
     */
    private String mapToJavaType(String edmType) {
        // 处理Collection类型
        if (edmType.startsWith("Collection(") && edmType.endsWith(")")) {
            String innerType = edmType.substring(11, edmType.length() - 1);
            return "List<" + mapToJavaType(innerType) + ">";
        }
        
        // 查找预定义映射
        String javaType = typeMapping.get(edmType);
        if (javaType != null) {
            return javaType;
        }
        
        // 处理自定义类型（Entity、Complex、Enum）
        return extractTypeName(edmType);
    }
    
    /**
     * 从完全限定名中提取类型名称
     */
    private String extractTypeName(String fullTypeName) {
        if (fullTypeName.contains(".")) {
            return fullTypeName.substring(fullTypeName.lastIndexOf('.') + 1);
        }
        return fullTypeName;
    }
    
    /**
     * 获取简单类型名称（去掉包名）
     */
    private String getSimpleTypeName(String javaType) {
        if (javaType.contains(".") && !javaType.contains("<")) {
            return javaType.substring(javaType.lastIndexOf('.') + 1);
        }
        return javaType;
    }
    
    /**
     * 转换为驼峰命名
     */
    private String toCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * 首字母大写
     */
    private String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
