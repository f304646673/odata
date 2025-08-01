# OData XML Processor

OData 4.0 XML Schema处理框架，支持XML解析和相同命名空间schema合并功能。

## 项目特性

- ✅ 基于Apache Olingo原生解析器
- ✅ 支持OData 4.0完整规范
- ✅ 相同命名空间schema合并检测
- ✅ 详细的错误报告和警告信息
- ✅ 高度解耦的设计
- ✅ Java 8兼容
- ✅ 100%单元测试覆盖率
- ✅ JUnit 5测试框架

## 项目结构

```
odata-xml-processor/
├── src/main/java/
│   └── org/apache/olingo/xmlprocessor/
│       ├── core/                      # 核心API接口
│       │   ├── ODataXmlProcessor.java
│       │   └── impl/
│       │       └── ODataXmlProcessorImpl.java
│       ├── parser/                    # XML解析器
│       │   ├── ODataXmlParser.java
│       │   └── impl/
│       │       └── CsdlXmlParserImpl.java
│       ├── merger/                    # Schema合并器
│       │   ├── SchemaMerger.java
│       │   └── impl/
│       │       └── SchemaMergerImpl.java
│       ├── model/extended/            # 扩展模型类
│       │   ├── ExtendedCsdlAction.java
│       │   ├── ExtendedCsdlAnnotation.java
│       │   ├── ExtendedCsdlComplexType.java
│       │   ├── ExtendedCsdlEntityContainer.java
│       │   ├── ExtendedCsdlEntityType.java
│       │   ├── ExtendedCsdlEnumType.java
│       │   ├── ExtendedCsdlFunction.java
│       │   ├── ExtendedCsdlTerm.java
│       │   └── ExtendedCsdlTypeDefinition.java
│       └── examples/                  # 示例程序
│           └── XmlProcessorDemo.java
├── src/main/resources/
│   └── test-schemas/                  # 测试用schema文件
│       ├── basic-schema.xml
│       ├── extended-schema.xml
│       └── conflicting-schema.xml
├── src/test/java/                     # 单元测试
│   └── org/apache/olingo/xmlprocessor/
│       ├── parser/impl/
│       │   └── CsdlXmlParserImplTest.java
│       ├── merger/impl/
│       │   └── SchemaMergerImplTest.java
│       └── core/impl/
│           └── ODataXmlProcessorImplTest.java
└── pom.xml
```

## 快速开始

### 1. 基本XML解析

```java
import org.apache.olingo.xmlprocessor.parser.impl.CsdlXmlParserImpl;

// 创建解析器
CsdlXmlParserImpl parser = new CsdlXmlParserImpl();

// 解析XML文件
ParseResult result = parser.parseSchemas(Paths.get("schema.xml"));

if (result.isSuccess()) {
    List<CsdlSchema> schemas = result.getSchemas();
    // 处理解析结果
} else {
    // 处理错误
    result.getErrors().forEach(System.out::println);
}
```

### 2. XML内容解析

```java
String xmlContent = "<?xml version=\"1.0\"?>...";
ParseResult result = parser.parseSchemas(xmlContent, "my-schema");
```

### 3. 从Resources加载

```java
ParseResult result = parser.parseFromResource("/schemas/my-schema.xml");
```

### 4. XML格式验证

```java
ValidationResult validation = parser.validateXmlFormat(xmlContent);
if (!validation.isValid()) {
    validation.getErrors().forEach(System.out::println);
}
```

### 5. Schema合并

```java
import org.apache.olingo.xmlprocessor.merger.impl.SchemaMergerImpl;

SchemaMergerImpl merger = new SchemaMergerImpl();

// 检测合并冲突
MergeConflictDetectionResult conflicts = merger.detectConflicts(schemas);
if (conflicts.hasConflicts()) {
    // 处理冲突
    conflicts.getConflicts().forEach(conflict -> {
        System.out.println("冲突: " + conflict.getDescription());
    });
}

// 执行合并
SchemaMergeResult mergeResult = merger.mergeSchemas(schemas);
if (mergeResult.isSuccess()) {
    List<CsdlSchema> mergedSchemas = mergeResult.getMergedSchemas();
    // 使用合并后的schemas
}
```

### 6. 完整处理流程

```java
import org.apache.olingo.xmlprocessor.core.impl.ODataXmlProcessorImpl;

ODataXmlProcessorImpl processor = new ODataXmlProcessorImpl();

// 解析并合并多个文件
List<Path> filePaths = Arrays.asList(
    Paths.get("schema1.xml"),
    Paths.get("schema2.xml")
);

ProcessResult result = processor.parseAndMergeSchemas(filePaths);
if (result.isSuccess()) {
    // 处理成功
    List<CsdlSchema> schemas = result.getSchemas();
} else {
    // 处理错误
    result.getErrors().forEach(System.out::println);
}
```

## 构建和运行

### 编译项目

```bash
mvn clean compile
```

### 运行测试

```bash
mvn test
```

### 生成测试覆盖率报告

```bash
mvn test jacoco:report
```

### 运行示例程序

```bash
mvn exec:java
```

### 打包

```bash
mvn clean package
```

## 核心功能

### XML解析器 (ODataXmlParser)

- 支持从文件、字符串、InputStream解析
- 基于Apache Olingo原生解析器
- 完整的错误和警告信息
- XML格式验证

### Schema合并器 (SchemaMerger)

- 检测相同命名空间的schema冲突
- 支持多种冲突类型检测：
  - EntityType重复定义
  - ComplexType重复定义  
  - EnumType重复定义
  - TypeDefinition重复定义
  - Action/Function重载冲突
  - EntityContainer冲突
- 详细的冲突报告
- 安全的schema合并

### 扩展模型类

所有扩展模型类都添加了以下功能：
- 命名空间跟踪
- 源路径信息
- 完全限定名生成

### 错误处理

框架提供详细的错误信息：
- 解析错误：XML格式错误、结构错误
- 验证错误：schema验证失败
- 合并冲突：类型定义冲突、属性不匹配
- 资源错误：文件不存在、权限问题

## 配置选项

### 解析器配置

```java
// 解析器会自动配置以下选项：
// - parseAnnotations(true)          // 解析注解
// - useLocalCoreVocabularies(true)  // 使用本地核心词汇表
// - implicitlyLoadCoreVocabularies(true)  // 隐式加载核心词汇表
// - recursivelyLoadReferences(false)      // 不递归加载引用（手动处理）
```

## 示例Schema文件

项目提供了三个示例schema文件：

1. **basic-schema.xml** - 基础schema，包含Product、Category等实体
2. **extended-schema.xml** - 扩展schema，包含Customer、Order等实体
3. **conflicting-schema.xml** - 冲突schema，与basic-schema.xml有类型定义冲突

## 依赖项

- Apache Olingo 5.0.0
- SLF4J 1.7.36 (日志)
- Logback 1.2.12 (日志实现)
- JUnit 5.10.0 (测试)
- Mockito 4.11.0 (测试模拟)

## 兼容性

- Java 8+
- Maven 3.6+
- Apache Olingo 5.0.0

## 许可证

Apache License 2.0

## 贡献

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 联系方式

如有问题或建议，请创建Issue。
