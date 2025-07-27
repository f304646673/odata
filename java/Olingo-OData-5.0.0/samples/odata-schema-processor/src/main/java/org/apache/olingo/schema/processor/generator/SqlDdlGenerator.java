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
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL DDL生成器
 * 根据OData Schema中的EntityType生成对应的数据库建表SQL语句
 * 支持多层继承和Collection类型处理
 */
public class SqlDdlGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlDdlGenerator.class);
    
    private final Path outputDirectory;
    private final DatabaseDialect dialect;
    private final Map<String, CsdlEntityType> allEntityTypes = new HashMap<>();
    private final Map<String, String> typeMapping = new HashMap<>();
    private final Map<String, Set<String>> inheritanceTree = new HashMap<>();
    
    public enum DatabaseDialect {
        MYSQL,
        POSTGRESQL,
        SQL_SERVER,
        ORACLE,
        H2
    }
    
    public SqlDdlGenerator(Path outputDirectory, DatabaseDialect dialect) {
        this.outputDirectory = outputDirectory;
        this.dialect = dialect;
        initializeTypeMapping();
    }
    
    /**
     * 初始化EDM类型到SQL类型的映射
     */
    private void initializeTypeMapping() {
        switch (dialect) {
            case MYSQL:
                initializeMySQLTypeMapping();
                break;
            case POSTGRESQL:
                initializePostgreSQLTypeMapping();
                break;
            case SQL_SERVER:
                initializeSqlServerTypeMapping();
                break;
            case ORACLE:
                initializeOracleTypeMapping();
                break;
            case H2:
                initializeH2TypeMapping();
                break;
        }
    }
    
    private void initializeMySQLTypeMapping() {
        typeMapping.put("Edm.String", "VARCHAR");
        typeMapping.put("Edm.Int32", "INT");
        typeMapping.put("Edm.Int64", "BIGINT");
        typeMapping.put("Edm.Int16", "SMALLINT");
        typeMapping.put("Edm.Byte", "TINYINT");
        typeMapping.put("Edm.SByte", "TINYINT");
        typeMapping.put("Edm.Boolean", "BOOLEAN");
        typeMapping.put("Edm.Decimal", "DECIMAL");
        typeMapping.put("Edm.Double", "DOUBLE");
        typeMapping.put("Edm.Single", "FLOAT");
        typeMapping.put("Edm.DateTime", "DATETIME");
        typeMapping.put("Edm.DateTimeOffset", "TIMESTAMP");
        typeMapping.put("Edm.Time", "TIME");
        typeMapping.put("Edm.Date", "DATE");
        typeMapping.put("Edm.Guid", "CHAR(36)");
        typeMapping.put("Edm.Binary", "BLOB");
    }
    
    private void initializePostgreSQLTypeMapping() {
        typeMapping.put("Edm.String", "VARCHAR");
        typeMapping.put("Edm.Int32", "INTEGER");
        typeMapping.put("Edm.Int64", "BIGINT");
        typeMapping.put("Edm.Int16", "SMALLINT");
        typeMapping.put("Edm.Byte", "SMALLINT");
        typeMapping.put("Edm.SByte", "SMALLINT");
        typeMapping.put("Edm.Boolean", "BOOLEAN");
        typeMapping.put("Edm.Decimal", "DECIMAL");
        typeMapping.put("Edm.Double", "DOUBLE PRECISION");
        typeMapping.put("Edm.Single", "REAL");
        typeMapping.put("Edm.DateTime", "TIMESTAMP");
        typeMapping.put("Edm.DateTimeOffset", "TIMESTAMP WITH TIME ZONE");
        typeMapping.put("Edm.Time", "TIME");
        typeMapping.put("Edm.Date", "DATE");
        typeMapping.put("Edm.Guid", "UUID");
        typeMapping.put("Edm.Binary", "BYTEA");
    }
    
    private void initializeSqlServerTypeMapping() {
        typeMapping.put("Edm.String", "NVARCHAR");
        typeMapping.put("Edm.Int32", "INT");
        typeMapping.put("Edm.Int64", "BIGINT");
        typeMapping.put("Edm.Int16", "SMALLINT");
        typeMapping.put("Edm.Byte", "TINYINT");
        typeMapping.put("Edm.SByte", "TINYINT");
        typeMapping.put("Edm.Boolean", "BIT");
        typeMapping.put("Edm.Decimal", "DECIMAL");
        typeMapping.put("Edm.Double", "FLOAT");
        typeMapping.put("Edm.Single", "REAL");
        typeMapping.put("Edm.DateTime", "DATETIME2");
        typeMapping.put("Edm.DateTimeOffset", "DATETIMEOFFSET");
        typeMapping.put("Edm.Time", "TIME");
        typeMapping.put("Edm.Date", "DATE");
        typeMapping.put("Edm.Guid", "UNIQUEIDENTIFIER");
        typeMapping.put("Edm.Binary", "VARBINARY(MAX)");
    }
    
    private void initializeOracleTypeMapping() {
        typeMapping.put("Edm.String", "VARCHAR2");
        typeMapping.put("Edm.Int32", "NUMBER(10)");
        typeMapping.put("Edm.Int64", "NUMBER(19)");
        typeMapping.put("Edm.Int16", "NUMBER(5)");
        typeMapping.put("Edm.Byte", "NUMBER(3)");
        typeMapping.put("Edm.SByte", "NUMBER(3)");
        typeMapping.put("Edm.Boolean", "NUMBER(1)");
        typeMapping.put("Edm.Decimal", "NUMBER");
        typeMapping.put("Edm.Double", "BINARY_DOUBLE");
        typeMapping.put("Edm.Single", "BINARY_FLOAT");
        typeMapping.put("Edm.DateTime", "TIMESTAMP");
        typeMapping.put("Edm.DateTimeOffset", "TIMESTAMP WITH TIME ZONE");
        typeMapping.put("Edm.Time", "TIMESTAMP");
        typeMapping.put("Edm.Date", "DATE");
        typeMapping.put("Edm.Guid", "RAW(16)");
        typeMapping.put("Edm.Binary", "BLOB");
    }
    
    private void initializeH2TypeMapping() {
        typeMapping.put("Edm.String", "VARCHAR");
        typeMapping.put("Edm.Int32", "INT");
        typeMapping.put("Edm.Int64", "BIGINT");
        typeMapping.put("Edm.Int16", "SMALLINT");
        typeMapping.put("Edm.Byte", "TINYINT");
        typeMapping.put("Edm.SByte", "TINYINT");
        typeMapping.put("Edm.Boolean", "BOOLEAN");
        typeMapping.put("Edm.Decimal", "DECIMAL");
        typeMapping.put("Edm.Double", "DOUBLE");
        typeMapping.put("Edm.Single", "REAL");
        typeMapping.put("Edm.DateTime", "TIMESTAMP");
        typeMapping.put("Edm.DateTimeOffset", "TIMESTAMP WITH TIME ZONE");
        typeMapping.put("Edm.Time", "TIME");
        typeMapping.put("Edm.Date", "DATE");
        typeMapping.put("Edm.Guid", "UUID");
        typeMapping.put("Edm.Binary", "BLOB");
    }
    
    /**
     * 生成结果
     */
    public static class GenerationResult {
        private final boolean success;
        private final List<String> generatedFiles;
        private final List<String> errors;
        private final List<String> warnings;
        
        public GenerationResult(boolean success, List<String> generatedFiles, 
                               List<String> errors, List<String> warnings) {
            this.success = success;
            this.generatedFiles = new ArrayList<>(generatedFiles);
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isSuccess() { return success; }
        public List<String> getGeneratedFiles() { return generatedFiles; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }
    
    /**
     * 从Schema列表生成SQL DDL
     */
    public GenerationResult generateFromSchemas(List<CsdlSchema> schemas) {
        List<String> generatedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // 第一步：收集所有EntityType和继承关系
            collectAllEntityTypes(schemas);
            buildInheritanceTree();
            
            // 第二步：验证继承关系的一致性
            validateInheritanceTree(warnings);
            
            // 第三步：按依赖关系排序EntityType
            List<CsdlEntityType> sortedEntityTypes = sortEntityTypesByDependency();
            
            // 第四步：生成SQL文件
            String fileName = generateSqlFile(sortedEntityTypes, schemas);
            generatedFiles.add(fileName);
            
            logger.info("Generated SQL DDL file: {}", fileName);
            
        } catch (IOException | RuntimeException e) {
            errors.add("SQL generation failed: " + e.getMessage());
            logger.error("SQL DDL generation failed", e);
        }
        
        return new GenerationResult(errors.isEmpty(), generatedFiles, errors, warnings);
    }
    
    /**
     * 收集所有EntityType
     */
    private void collectAllEntityTypes(List<CsdlSchema> schemas) {
        logger.info("Starting to collect EntityTypes from {} schemas", schemas.size());
        for (CsdlSchema schema : schemas) {
            logger.info("Processing schema: {}", schema.getNamespace());
            if (schema.getEntityTypes() != null) {
                logger.info("Found {} EntityTypes in schema {}", schema.getEntityTypes().size(), schema.getNamespace());
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String fullName = schema.getNamespace() + "." + entityType.getName();
                    allEntityTypes.put(fullName, entityType);
                    logger.info("Collected EntityType: {}", fullName);
                }
            } else {
                logger.warn("No EntityTypes found in schema: {}", schema.getNamespace());
            }
        }
        logger.info("Total EntityTypes collected: {}", allEntityTypes.size());
    }
    
    /**
     * 构建继承树
     */
    private void buildInheritanceTree() {
        for (Map.Entry<String, CsdlEntityType> entry : allEntityTypes.entrySet()) {
            String entityName = entry.getKey();
            CsdlEntityType entityType = entry.getValue();
            
            if (entityType.getBaseType() != null) {
                String baseType = entityType.getBaseType();
                inheritanceTree.computeIfAbsent(baseType, k -> new HashSet<>()).add(entityName);
                logger.debug("Inheritance: {} extends {}", entityName, baseType);
            }
        }
    }
    
    /**
     * 验证继承树的一致性
     */
    private void validateInheritanceTree(List<String> warnings) {
        for (Map.Entry<String, CsdlEntityType> entry : allEntityTypes.entrySet()) {
            CsdlEntityType entityType = entry.getValue();
            if (entityType.getBaseType() != null) {
                String baseType = entityType.getBaseType();
                if (!allEntityTypes.containsKey(baseType)) {
                    warnings.add("Base type not found: " + baseType + " for entity " + entry.getKey());
                }
            }
        }
        
        // 检查循环继承
        for (String entityName : allEntityTypes.keySet()) {
            if (hasCircularInheritance(entityName, new HashSet<>())) {
                warnings.add("Circular inheritance detected for entity: " + entityName);
            }
        }
    }
    
    /**
     * 检查循环继承
     */
    private boolean hasCircularInheritance(String entityName, Set<String> visited) {
        if (visited.contains(entityName)) {
            return true;
        }
        
        CsdlEntityType entityType = allEntityTypes.get(entityName);
        if (entityType == null || entityType.getBaseType() == null) {
            return false;
        }
        
        visited.add(entityName);
        return hasCircularInheritance(entityType.getBaseType(), visited);
    }
    
    /**
     * 按继承依赖关系排序EntityType
     */
    private List<CsdlEntityType> sortEntityTypesByDependency() {
        List<CsdlEntityType> sorted = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        // 首先处理没有基类的实体
        for (Map.Entry<String, CsdlEntityType> entry : allEntityTypes.entrySet()) {
            if (entry.getValue().getBaseType() == null) {
                sorted.add(entry.getValue());
                processed.add(entry.getKey());
            }
        }
        
        // 然后按层次处理有继承关系的实体
        boolean hasChanges = true;
        while (hasChanges) {
            hasChanges = false;
            for (Map.Entry<String, CsdlEntityType> entry : allEntityTypes.entrySet()) {
                String entityName = entry.getKey();
                CsdlEntityType entityType = entry.getValue();
                
                if (!processed.contains(entityName) && 
                    entityType.getBaseType() != null && 
                    processed.contains(entityType.getBaseType())) {
                    sorted.add(entityType);
                    processed.add(entityName);
                    hasChanges = true;
                }
            }
        }
        
        // 添加剩余的实体（可能有循环依赖）
        for (Map.Entry<String, CsdlEntityType> entry : allEntityTypes.entrySet()) {
            if (!processed.contains(entry.getKey())) {
                sorted.add(entry.getValue());
            }
        }
        
        return sorted;
    }
    
    /**
     * 生成SQL文件
     */
    private String generateSqlFile(List<CsdlEntityType> entityTypes, List<CsdlSchema> schemas) throws IOException {
        Files.createDirectories(outputDirectory);
        
        String fileName = "create_tables_" + dialect.name().toLowerCase() + ".sql";
        Path sqlFile = outputDirectory.resolve(fileName);
        
        StringBuilder sql = new StringBuilder();
        
        // 添加文件头注释
        sql.append("-- ").append("=".repeat(60)).append("\n");
        sql.append("-- OData Schema DDL for ").append(dialect.name()).append("\n");
        sql.append("-- Generated on: ").append(new java.util.Date()).append("\n");
        sql.append("-- Schemas: ").append(schemas.stream()
                .map(CsdlSchema::getNamespace)
                .collect(Collectors.joining(", "))).append("\n");
        sql.append("-- ").append("=".repeat(60)).append("\n\n");
        
        // 生成每个实体的表
        for (CsdlEntityType entityType : entityTypes) {
            sql.append(generateCreateTableStatement(entityType));
            sql.append("\n");
        }
        
        // 生成外键约束
        sql.append(generateForeignKeyConstraints(entityTypes));
        
        // 生成索引
        sql.append(generateIndexes(entityTypes));
        
        Files.write(sqlFile, sql.toString().getBytes("UTF-8"), 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        return fileName;
    }
    
    /**
     * 生成CREATE TABLE语句
     */
    private String generateCreateTableStatement(CsdlEntityType entityType) {
        StringBuilder sql = new StringBuilder();
        
        String tableName = getTableName(entityType.getName());
        
        sql.append("-- Entity: ").append(entityType.getName());
        if (entityType.getBaseType() != null) {
            sql.append(" (extends ").append(entityType.getBaseType()).append(")");
        }
        sql.append("\n");
        
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        List<String> columns = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        
        // 添加继承的列
        if (entityType.getBaseType() != null) {
            CsdlEntityType baseType = allEntityTypes.get(entityType.getBaseType());
            if (baseType != null) {
                addInheritedColumns(baseType, columns, primaryKeys, new HashSet<>());
            }
        }
        
        // 添加自己的属性列
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                columns.add(generateColumnDefinition(property));
            }
        }
        
        // 添加主键
        if (entityType.getKey() != null && !entityType.getKey().isEmpty()) {
            for (CsdlPropertyRef keyRef : entityType.getKey()) {
                primaryKeys.add(keyRef.getName());
            }
        }
        
        // 添加导航属性的外键列
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                if (!isCollectionType(navProp.getType())) {
                    // 单值导航属性，添加外键列
                    String fkColumn = navProp.getName() + "_Id";
                    columns.add("    " + fkColumn + " " + getSqlType("Edm.String", null, null));
                }
            }
        }
        
        // 如果是继承的实体，添加鉴别器列
        if (entityType.getBaseType() != null || hasSubTypes(entityType.getName())) {
            columns.add("    EntityType VARCHAR(100) NOT NULL -- Discriminator column");
        }
        
        sql.append(String.join(",\n", columns));
        
        // 添加主键约束
        if (!primaryKeys.isEmpty()) {
            sql.append(",\n    CONSTRAINT PK_").append(tableName)
               .append(" PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }
        
        sql.append("\n);\n\n");
        
        return sql.toString();
    }
    
    /**
     * 添加继承的列
     */
    private void addInheritedColumns(CsdlEntityType baseType, List<String> columns, 
                                   List<String> primaryKeys, Set<String> processedTypes) {
        String baseTypeName = getEntityTypeFullName(baseType);
        if (processedTypes.contains(baseTypeName)) {
            return; // 避免循环继承
        }
        processedTypes.add(baseTypeName);
        
        // 递归添加祖先类的列
        if (baseType.getBaseType() != null) {
            CsdlEntityType grandParent = allEntityTypes.get(baseType.getBaseType());
            if (grandParent != null) {
                addInheritedColumns(grandParent, columns, primaryKeys, processedTypes);
            }
        }
        
        // 添加基类的属性
        if (baseType.getProperties() != null) {
            for (CsdlProperty property : baseType.getProperties()) {
                columns.add(generateColumnDefinition(property) + " -- Inherited from " + baseTypeName);
            }
        }
        
        // 添加基类的主键
        if (baseType.getKey() != null) {
            for (CsdlPropertyRef keyRef : baseType.getKey()) {
                if (!primaryKeys.contains(keyRef.getName())) {
                    primaryKeys.add(keyRef.getName());
                }
            }
        }
    }
    
    /**
     * 生成列定义
     */
    private String generateColumnDefinition(CsdlProperty property) {
        StringBuilder column = new StringBuilder();
        
        column.append("    ").append(property.getName()).append(" ");
        
        // 处理Collection类型
        if (isCollectionType(property.getType())) {
            String elementType = getCollectionElementType(property.getType());
            // Collection类型通常存储为JSON或者单独的表，这里用JSON表示
            switch (dialect) {
                case MYSQL:
                    column.append("JSON");
                    break;
                case POSTGRESQL:
                    column.append("JSONB");
                    break;
                case SQL_SERVER:
                    column.append("NVARCHAR(MAX)");
                    break;
                case ORACLE:
                    column.append("CLOB CHECK (").append(property.getName()).append(" IS JSON)");
                    break;
                case H2:
                    column.append("CLOB");
                    break;
            }
            column.append(" -- Collection of ").append(elementType);
        } else {
            column.append(getSqlType(property.getType(), property.getMaxLength(), property.getPrecision()));
        }
        
        // 处理可空性
        if (!property.isNullable()) {
            column.append(" NOT NULL");
        }
        
        // 处理默认值
        if (property.getDefaultValue() != null) {
            column.append(" DEFAULT ");
            if (property.getType().equals("Edm.String")) {
                column.append("'").append(property.getDefaultValue()).append("'");
            } else {
                column.append(property.getDefaultValue());
            }
        }
        
        return column.toString();
    }
    
    /**
     * 获取SQL数据类型
     */
    private String getSqlType(String edmType, Integer maxLength, Integer precision) {
        String baseType = typeMapping.getOrDefault(edmType, "VARCHAR");
        
        // 处理长度和精度
        if (maxLength != null && maxLength > 0) {
            if (baseType.contains("VARCHAR") || baseType.contains("CHAR")) {
                return baseType + "(" + maxLength + ")";
            }
        }
        
        if (precision != null && precision > 0) {
            if (baseType.equals("DECIMAL") || baseType.equals("NUMBER")) {
                return baseType + "(" + precision + ")";
            }
        }
        
        // 默认长度
        if (baseType.contains("VARCHAR") && !baseType.contains("(")) {
            return baseType + "(255)";
        }
        
        return baseType;
    }
    
    /**
     * 检查是否为Collection类型
     */
    private boolean isCollectionType(String type) {
        return type != null && type.startsWith("Collection(");
    }
    
    /**
     * 获取Collection元素类型
     */
    private String getCollectionElementType(String collectionType) {
        if (collectionType.startsWith("Collection(") && collectionType.endsWith(")")) {
            return collectionType.substring(11, collectionType.length() - 1);
        }
        return collectionType;
    }
    
    /**
     * 检查实体是否有子类型
     */
    private boolean hasSubTypes(String entityName) {
        String fullName = getEntityTypeFullName(allEntityTypes.values().stream()
                .filter(et -> et.getName().equals(entityName))
                .findFirst().orElse(null));
        return inheritanceTree.containsKey(fullName);
    }
    
    /**
     * 获取实体类型的完整名称
     */
    private String getEntityTypeFullName(CsdlEntityType entityType) {
        // 由于我们存储的key就是完整名称，这里简化处理
        for (Map.Entry<String, CsdlEntityType> entry : allEntityTypes.entrySet()) {
            if (entry.getValue() == entityType) {
                return entry.getKey();
            }
        }
        return entityType.getName();
    }
    
    /**
     * 获取表名
     */
    private String getTableName(String entityName) {
        // 简单的命名策略：实体名转换为表名
        return entityName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
    
    /**
     * 生成外键约束
     */
    private String generateForeignKeyConstraints(List<CsdlEntityType> entityTypes) {
        StringBuilder sql = new StringBuilder();
        sql.append("-- Foreign Key Constraints\n");
        
        for (CsdlEntityType entityType : entityTypes) {
            if (entityType.getNavigationProperties() != null) {
                for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                    if (!isCollectionType(navProp.getType())) {
                        String tableName = getTableName(entityType.getName());
                        String fkColumn = navProp.getName() + "_Id";
                        String refTable = getTableName(getEntityNameFromType(navProp.getType()));
                        
                        sql.append("ALTER TABLE ").append(tableName)
                           .append(" ADD CONSTRAINT FK_").append(tableName).append("_").append(navProp.getName())
                           .append(" FOREIGN KEY (").append(fkColumn).append(")")
                           .append(" REFERENCES ").append(refTable).append("(Id);\n");
                    }
                }
            }
        }
        
        sql.append("\n");
        return sql.toString();
    }
    
    /**
     * 生成索引
     */
    private String generateIndexes(List<CsdlEntityType> entityTypes) {
        StringBuilder sql = new StringBuilder();
        sql.append("-- Indexes\n");
        
        for (CsdlEntityType entityType : entityTypes) {
            String tableName = getTableName(entityType.getName());
            
            // 为外键创建索引
            if (entityType.getNavigationProperties() != null) {
                for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                    if (!isCollectionType(navProp.getType())) {
                        String fkColumn = navProp.getName() + "_Id";
                        sql.append("CREATE INDEX IX_").append(tableName).append("_").append(navProp.getName())
                           .append(" ON ").append(tableName).append("(").append(fkColumn).append(");\n");
                    }
                }
            }
            
            // 为鉴别器列创建索引
            if (entityType.getBaseType() != null || hasSubTypes(entityType.getName())) {
                sql.append("CREATE INDEX IX_").append(tableName).append("_EntityType")
                   .append(" ON ").append(tableName).append("(EntityType);\n");
            }
        }
        
        sql.append("\n");
        return sql.toString();
    }
    
    /**
     * 从类型字符串获取实体名称
     */
    private String getEntityNameFromType(String type) {
        if (type.contains(".")) {
            return type.substring(type.lastIndexOf('.') + 1);
        }
        return type;
    }
}
