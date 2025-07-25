# OData Schema Manager

一套基于Apache Olingo的OData XML Schema管理系统，支持递归加载、命名空间合并、依赖分析等企业级功能。

## 功能特性

### 1. 递归XML加载
- 从指定目录递归加载所有OData XML文件
- 支持多种文件来源（目录、classpath）
- 并行处理提升性能
- 完整的错误处理和报告

### 2. 命名空间合并
- 自动合并相同namespace的Schema
- 冲突检测和解决
- 兼容性验证
- 多种合并策略

### 3. 依赖分析
- EntityType、ComplexType、EnumType之间的依赖关系分析
- 递归依赖追踪
- 循环依赖检测
- 依赖图构建

### 4. Container基础Schema提取
- 从EntityContainer构建完整依赖图
- 手工Container定义支持
- 关联类型自动提取

## 架构设计

### 核心接口

1. **ODataXmlLoader** - XML文件加载器
2. **ODataSchemaParser** - Schema解析器
3. **SchemaRepository** - Schema存储仓库
4. **SchemaMerger** - Schema合并器
5. **TypeDependencyAnalyzer** - 类型依赖分析器

### 实现类

1. **DefaultODataXmlLoader** - 默认XML加载实现
2. **OlingoSchemaParserImpl** - 基于Olingo的解析实现
3. **InMemorySchemaRepository** - 内存存储实现
4. **DefaultSchemaMerger** - 默认合并实现
5. **DefaultTypeDependencyAnalyzer** - 默认依赖分析实现

## 使用示例

### REST API使用

```bash
# 1. 从目录加载XML文件
POST /api/odata/schema/load?directoryPath=/path/to/xml/files

# 2. 获取所有Schema
GET /api/odata/schema/schemas

# 3. 获取指定namespace的Schema  
GET /api/odata/schema/schemas/{namespace}

# 4. 获取所有namespace
GET /api/odata/schema/namespaces

# 5. 获取统计信息
GET /api/odata/schema/statistics

# 6. 清理所有数据
DELETE /api/odata/schema/clear
```

### 编程API使用

```java
@Autowired
private ODataXmlLoader xmlLoader;

@Autowired  
private SchemaRepository repository;

@Autowired
private SchemaMerger schemaMerger;

@Autowired
private TypeDependencyAnalyzer dependencyAnalyzer;

// 1. 加载XML文件
LoadResult result = xmlLoader.loadFromResourceDirectory("/path/to/xml/files");

// 2. 获取所有Schema
Map<String, CsdlSchema> schemas = repository.getAllSchemas();

// 3. 合并Schema
Map<String, CsdlSchema> mergedSchemas = schemaMerger.mergeByNamespace(schemas);

// 4. 分析依赖
CsdlEntityType entityType = repository.getEntityType("Namespace.EntityTypeName");
List<TypeReference> dependencies = dependencyAnalyzer.getAllDependencies(entityType);

// 5. 检测循环依赖
List<CircularDependency> circularDeps = dependencyAnalyzer.detectCircularDependencies();
```

## 配置说明

### Spring Boot配置

所有组件通过`@Component`注解自动注册为Spring Bean，支持依赖注入。

### Maven依赖

```xml
<dependency>
    <groupId>org.apache.olingo</groupId>
    <artifactId>odata-commons-api</artifactId>
    <version>5.0.0</version>
</dependency>
<dependency>
    <groupId>org.apache.olingo</groupId>
    <artifactId>odata-commons-core</artifactId>
    <version>5.0.0</version>
</dependency>
```

## 数据结构

### LoadResult
```java
class LoadResult {
    List<XmlFileInfo> successfulFiles;
    List<XmlFileInfo> failedFiles;
    Map<String, String> summary;
}
```

### XmlFileInfo
```java
class XmlFileInfo {
    String filePath;
    String namespace;
    boolean success;
    String error;
    List<String> entityTypes;
    List<String> complexTypes;
    List<String> enumTypes;
}
```

### TypeReference
```java
class TypeReference {
    String fullQualifiedName;
    TypeKind typeKind;
    String propertyName;
    boolean isCollection;
}
```

## 扩展点

### 自定义解析器
实现`ODataSchemaParser`接口，支持自定义XML解析逻辑。

### 自定义存储
实现`SchemaRepository`接口，支持数据库、缓存等存储方式。

### 自定义合并策略
实现`SchemaMerger`接口，支持自定义Schema合并逻辑。

## 性能特性

- **并行处理**: 支持多文件并行加载
- **内存优化**: 线程安全的并发数据结构
- **增量更新**: 支持Schema增量更新
- **缓存支持**: 解析结果缓存机制

## 错误处理

- 完整的异常捕获和处理
- 详细的错误信息报告
- 部分失败容错机制
- 验证失败详细诊断
