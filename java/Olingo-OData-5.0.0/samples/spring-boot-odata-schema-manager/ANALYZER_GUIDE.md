# OData Schema Analyzer 使用指南

## 概述

`ODataSchemaAnalyzer` 是一个强大的OData Schema分析工具，基于现有的 `InMemorySchemaRepository` 构建，提供以下核心功能：

1. **递归加载**: 从指定路径递归加载所有OData XML文件
2. **Import校验**: 验证文件间的import依赖正确性，检测循环依赖
3. **类型分析**: 深度分析EntityType、ComplexType、EnumType之间的依赖关系
4. **智能查询**: 提供多种查询方式获取类型详细信息和关联关系

## 核心类设计

### ODataSchemaAnalyzer

主要分析器类，提供以下方法：

```java
// 分析整个目录
AnalysisResult analyzeDirectory(String directoryPath)

// 查询具体类型详情
TypeDetailInfo getEntityTypeDetail(String fullQualifiedName)
TypeDetailInfo getComplexTypeDetail(String fullQualifiedName) 
TypeDetailInfo getEnumTypeDetail(String fullQualifiedName)

// 搜索类型
List<TypeDetailInfo> searchTypes(String namePattern)

// 获取统计信息
Map<String, Object> getStatistics()
```

## 数据结构

### AnalysisResult - 分析结果

```java
class AnalysisResult {
    boolean success;                    // 分析是否成功
    List<String> errors;               // 错误列表
    List<String> warnings;             // 警告列表
    Map<String, Set<String>> dependencies;  // 依赖关系图
    Map<String, String> typeLocations;      // 类型位置映射
    ImportValidationResult importValidation; // Import校验结果
}
```

### TypeDetailInfo - 类型详细信息

```java
class TypeDetailInfo {
    String fullQualifiedName;          // 完全限定名
    String namespace;                  // 命名空间
    String typeName;                   // 类型名
    TypeKind typeKind;                 // 类型种类(ENTITY_TYPE/COMPLEX_TYPE/ENUM_TYPE)
    Object typeDefinition;             // 类型定义对象
    Set<String> directDependencies;    // 直接依赖
    Set<String> allDependencies;       // 所有依赖(递归)
    Set<String> dependents;            // 依赖者(谁依赖我)
    String sourceFile;                 // 源文件路径
}
```

### ImportValidationResult - Import校验结果

```java
class ImportValidationResult {
    boolean valid;                     // 校验是否通过
    List<String> missingImports;       // 缺失的引用
    List<String> unusedImports;        // 未使用的引用
    List<String> circularDependencies; // 循环依赖
}
```

## 使用示例

### 1. 编程API使用

```java
@Autowired
private ODataSchemaAnalyzer analyzer;

// 分析目录
AnalysisResult result = analyzer.analyzeDirectory("/path/to/odata/xml/files");

if (result.isSuccess()) {
    System.out.println("分析成功!");
    
    // 检查Import校验结果
    ImportValidationResult importResult = result.getImportValidation();
    if (!importResult.isValid()) {
        System.out.println("发现Import问题:");
        importResult.getMissingImports().forEach(System.out::println);
        importResult.getCircularDependencies().forEach(System.out::println);
    }
    
    // 查询特定类型
    TypeDetailInfo customerInfo = analyzer.getEntityTypeDetail("MyNamespace.Customer");
    if (customerInfo != null) {
        System.out.println("Customer类型的直接依赖: " + customerInfo.getDirectDependencies());
        System.out.println("Customer类型的所有依赖: " + customerInfo.getAllDependencies());
        System.out.println("依赖Customer的类型: " + customerInfo.getDependents());
    }
    
    // 搜索类型
    List<TypeDetailInfo> searchResults = analyzer.searchTypes("Address");
    searchResults.forEach(info -> 
        System.out.println("找到类型: " + info.getFullQualifiedName())
    );
    
} else {
    System.out.println("分析失败:");
    result.getErrors().forEach(System.out::println);
}
```

### 2. REST API使用

```bash
# 分析目录
POST /api/odata/analyzer/analyze?directoryPath=/path/to/xml/files

# 获取EntityType详情
GET /api/odata/analyzer/entitytype/MyNamespace.Customer

# 获取ComplexType详情  
GET /api/odata/analyzer/complextype/MyNamespace.Address

# 获取EnumType详情
GET /api/odata/analyzer/enumtype/MyNamespace.OrderStatus

# 搜索类型
GET /api/odata/analyzer/search?namePattern=Customer

# 获取统计信息
GET /api/odata/analyzer/statistics
```

### 3. 示例Service使用

```java
@Autowired
private SchemaAnalysisService analysisService;

// 执行完整分析流程
analysisService.performCompleteAnalysis("/path/to/xml/files");

// 生成依赖报告
String report = analysisService.generateDependencyReport("MyNamespace.Customer");
System.out.println(report);
```

## 功能特性

### 1. 智能依赖分析

- **直接依赖**: 分析BaseType、Property类型、Navigation Property等直接引用
- **递归依赖**: 深度追踪所有间接依赖关系
- **反向依赖**: 查找依赖当前类型的其他类型

### 2. Import校验

- **缺失引用检测**: 发现引用了不存在的类型
- **循环依赖检测**: 检测类型间的循环引用
- **未使用引用**: 标识可能的冗余引用

### 3. 类型解析

- **完全限定名解析**: 正确处理命名空间
- **Collection类型**: 支持Collection(Type)语法
- **基础类型过滤**: 排除Edm.*基础类型

### 4. 搜索功能

- **模糊匹配**: 支持类型名称部分匹配
- **多类型搜索**: 同时搜索EntityType、ComplexType、EnumType
- **结果排序**: 按相关性排序搜索结果

## 集成说明

### Spring Boot集成

所有组件都通过Spring注解自动注册：

```java
@Component
public class ODataSchemaAnalyzer {
    @Autowired
    private ODataXmlLoader xmlLoader;
    
    @Autowired
    private SchemaRepository repository;
}
```

### 与现有组件的关系

```
ODataSchemaAnalyzer
├── 使用 ODataXmlLoader 加载XML文件
├── 使用 SchemaRepository 存储和查询数据
├── 集成 InMemorySchemaRepository 的所有功能
└── 提供高级分析和查询功能
```

## 性能特性

- **线程安全**: 基于ConcurrentHashMap的并发安全设计
- **增量分析**: 支持增量添加和分析新的Schema
- **内存优化**: 高效的索引结构和查询算法
- **缓存机制**: 依赖关系计算结果缓存

## 错误处理

- **完整异常捕获**: 处理XML解析、文件IO等异常
- **详细错误报告**: 提供具体的错误位置和原因
- **部分失败容错**: 单个文件失败不影响其他文件处理
- **验证失败诊断**: 提供详细的校验失败信息

## 扩展点

1. **自定义校验规则**: 扩展ImportValidationResult添加更多校验
2. **自定义依赖分析**: 扩展依赖关系提取逻辑
3. **自定义搜索**: 实现更复杂的搜索算法
4. **结果格式化**: 自定义分析结果的输出格式

这个设计完全满足了您的需求，提供了一个强大而灵活的OData Schema分析解决方案！
