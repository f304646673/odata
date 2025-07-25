# 测试XML资源文件说明

此目录包含用于OData Schema Manager测试的各种XML Schema文件。

## 目录结构

### valid/ - 有效的Schema文件
- `simple-schema.xml` - 基础的EntityType和EntityContainer定义
- `complex-types-schema.xml` - 包含ComplexType的Schema
- `full-schema.xml` - 完整的Schema，包含EntityType、ComplexType、EnumType和NavigationProperty

### invalid/ - 无效的Schema文件（用于错误处理测试）
- `malformed-xml.xml` - 格式错误的XML文件（缺少闭合标签）
- `invalid-types.xml` - 包含无效类型引用的Schema
- `not-xml.txt` - 非XML格式的文件

### complex/ - 复杂场景的Schema文件
- `multi-dependency-schema.xml` - 复杂的类型依赖关系 (A->B->C->D)
- `circular-dependency-schema.xml` - 包含循环依赖的Schema

### performance/ - 性能测试的Schema文件
- `large-schema.xml` - 大型Schema，包含多个EntityType和ComplexType

### multi-file/ - 多文件测试目录
- `products-schema.xml` - 产品相关的Schema
- `sales-schema.xml` - 销售相关的Schema  
- `inventory-schema.xml` - 库存相关的Schema

### merge-test/ - Schema合并测试文件
- `base-schema.xml` - 基础Schema
- `extension-schema.xml` - 扩展Schema

### empty-directory/ - 空目录（用于空目录处理测试）

## 使用说明

### 在测试中使用资源文件
```java
// 获取资源文件路径
String resourcePath = getClass().getClassLoader()
    .getResource("loader/valid/simple-schema.xml")
    .getPath();

// 或者使用相对路径
Path testResourcePath = Paths.get("src/test/resources/xml-schemas/loader/valid");
```

### 目录扫描测试
```java
// 测试多文件目录扫描
String multiFileDir = "src/test/resources/xml-schemas/loader/multi-file";

// 测试空目录处理
String emptyDir = "src/test/resources/xml-schemas/loader/empty-directory";
```

### 错误处理测试
```java
// 测试格式错误的XML文件
String malformedXml = "src/test/resources/xml-schemas/loader/invalid/malformed-xml.xml";

// 测试非XML文件
String notXmlFile = "src/test/resources/xml-schemas/loader/invalid/not-xml.txt";
```

### 依赖分析测试
```java
// 测试复杂依赖关系
String complexDeps = "src/test/resources/xml-schemas/loader/complex/multi-dependency-schema.xml";

// 测试循环依赖检测
String circularDeps = "src/test/resources/xml-schemas/loader/complex/circular-dependency-schema.xml";
```

### 性能测试
```java
// 测试大型Schema处理性能
String largeSchema = "src/test/resources/xml-schemas/loader/performance/large-schema.xml";
```

### Schema合并测试
```java
// 测试Schema合并功能
String baseSchema = "src/test/resources/xml-schemas/loader/merge-test/base-schema.xml";
String extensionSchema = "src/test/resources/xml-schemas/loader/merge-test/extension-schema.xml";
```

## 注意事项

1. 所有有效的XML文件都遵循OData 4.0 EDMX格式
2. 无效文件专门用于测试错误处理逻辑
3. 复杂场景文件用于测试高级功能如依赖分析
4. 文件路径使用相对于`src/test/resources`的路径
5. 在单元测试中使用时，注意区分不同的测试场景和目录
