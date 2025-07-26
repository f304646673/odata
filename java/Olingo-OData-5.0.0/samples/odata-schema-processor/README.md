# OData 4.0 Schema Processor

一个强大的OData 4.0 XML Schema处理框架，提供完整的Schema解析、验证、依赖分析和管理功能。

## 核心特性

### 1. 扩展的Olingo类
- **ExtendedCsdlProperty**: 扩展了CsdlProperty，增加依赖关系追踪
- **ExtendedCsdlNavigationProperty**: 扩展了CsdlNavigationProperty，支持依赖分析
- **ExtendedCsdlEntityType**: 扩展了CsdlEntityType，提供完整的依赖管理
- **ExtendedCsdlComplexType**: 扩展了CsdlComplexType，支持BaseType和Property的依赖分析
- **ExtendedCsdlAction**: 扩展了CsdlAction，分析参数和返回类型的依赖关系
- **ExtendedCsdlFunction**: 扩展了CsdlFunction，支持参数和返回类型依赖追踪
- **ExtendedCsdlParameter**: 扩展了CsdlParameter，提供类型依赖分析
- **ExtendedCsdlReturnType**: 扩展了CsdlReturnType，支持返回类型依赖追踪
- **ExtendedCsdlEntitySet**: 扩展了CsdlEntitySet，分析EntityType和NavigationPropertyBinding依赖
- **ExtendedCsdlSingleton**: 扩展了CsdlSingleton，支持类型和NavigationPropertyBinding依赖
- **ExtendedCsdlActionImport**: 扩展了CsdlActionImport，分析Action和EntitySet依赖
- **ExtendedCsdlFunctionImport**: 扩展了CsdlFunctionImport，支持Function和EntitySet依赖
- **ExtendedCsdlTypeDefinition**: 扩展了CsdlTypeDefinition，分析UnderlyingType依赖
- **DependencyTracker**: 通用依赖追踪接口

### 2. XML文件仓库管理
- 递归扫描多层目录结构中的XML文件
- 自动提取并映射Schema namespace信息
- 支持文件到namespace和namespace到文件的双向映射
- 提供快速的namespace存在性检查

### 3. Schema仓库管理
- 支持多个Schema的统一管理
- 同名namespace的Schema合并策略
- 冲突解决机制（保留第一个、保留最后一个、抛出异常、尝试合并）
- Schema一致性验证

### 4. 依赖关系分析
- 直接依赖和递归依赖分析
- 反向依赖查询（哪些元素依赖于指定元素）
- 循环依赖检测
- 依赖层级结构分析
- 影响范围分析

### 5. OData 4.0规范验证
- Schema格式验证
- 类型引用完整性检查
- 导入声明验证
- namespace唯一性检查
- 约束条件验证

### 6. 高级功能
- 依赖关系图可视化数据
- 处理统计信息
- 性能监控
- 错误和警告详细报告

## 项目结构

```
odata-schema-processor/
├── src/main/java/org/apache/olingo/schema/processor/
│   ├── model/extended/          # 扩展的Olingo类
│   │   ├── DependencyTracker.java
│   │   ├── AbstractDependencyTracker.java
│   │   ├── ExtendedCsdlProperty.java
│   │   ├── ExtendedCsdlNavigationProperty.java
│   │   └── ExtendedCsdlEntityType.java
│   ├── repository/              # 仓库接口和实现
│   │   ├── XmlFileRepository.java
│   │   ├── SchemaRepository.java
│   │   └── impl/
│   │       └── DefaultXmlFileRepository.java
│   ├── parser/                  # XML解析器
│   │   └── ODataXmlParser.java
│   ├── validator/               # 验证器
│   │   └── ODataValidator.java
│   ├── analyzer/                # 依赖分析器
│   │   └── DependencyAnalyzer.java
│   └── core/                    # 核心协调器
│       └── ODataSchemaProcessor.java
├── src/test/java/               # 单元测试
├── src/test/resources/schemas/  # 测试Schema文件
└── pom.xml
```

## 核心设计理念

### 1. 扩展而非替换
通过继承Olingo的核心类（CsdlProperty、CsdlNavigationProperty等），在保持兼容性的同时增加依赖追踪功能，这样：
- 可以无缝使用现有的Olingo生态
- 支持标准的XML序列化/反序列化
- 保持了类型安全性

### 2. 接口驱动设计
每个主要功能都定义了清晰的接口：
- 降低组件间耦合
- 便于单元测试
- 支持不同的实现策略

### 3. 依赖关系图
构建完整的依赖关系图，支持：
- 递归依赖查询
- 循环依赖检测
- 影响范围分析
- 依赖层级排序

## 使用示例

### 基本使用
```java
// 创建处理器
ODataSchemaProcessor processor = new DefaultODataSchemaProcessor();

// 加载目录中的所有XML文件
Path schemaDir = Paths.get("src/test/resources/schemas");
ODataSchemaProcessor.ProcessingResult result = processor.processDirectory(schemaDir);

if (result.isSuccess()) {
    System.out.println("成功加载 " + result.getTotalSchemas() + " 个Schema");
    System.out.println("命名空间: " + result.getNamespaces());
} else {
    System.out.println("处理失败: " + result.getErrors());
}
```

### 依赖分析
```java
// 分析特定元素的依赖
String entityType = "Microsoft.OData.Core.Test.Common.Person";
ODataSchemaProcessor.DependencyResult deps = processor.analyzeDependencies(entityType);

System.out.println("直接依赖: " + deps.getDirectDependencies());
System.out.println("递归依赖: " + deps.getRecursiveDependencies());
System.out.println("反向依赖: " + deps.getReverseDependencies());
```

### 循环依赖检测
```java
// 检测循环依赖
ODataSchemaProcessor.CircularDependencyResult circularDeps = processor.detectCircularDependencies();

if (circularDeps.hasCircularDependencies()) {
    System.out.println("发现循环依赖:");
    for (List<String> cycle : circularDeps.getCycles()) {
        System.out.println("  " + String.join(" -> ", cycle));
    }
}
```

### Schema验证
```java
// 验证所有Schema
ODataSchemaProcessor.ValidationResult validation = processor.validateAllSchemas();

if (!validation.isValid()) {
    System.out.println("验证错误:");
    validation.getErrors().forEach(System.out::println);
    
    System.out.println("缺失依赖:");
    validation.getMissingDependencies().forEach(System.out::println);
}
```

## 特性详解

### 1. 多层目录支持
系统自动递归扫描指定目录及其子目录中的所有XML文件：
```
schemas/
├── common/
│   ├── Person.xml
│   └── BaseTypes.xml
├── address/
│   └── Address.xml
└── business/
    ├── Order.xml
    └── Product.xml
```

### 2. 同namespace合并
当多个XML文件定义相同namespace的Schema时，提供多种合并策略：
- **KEEP_FIRST**: 保留第一个定义，忽略后续冲突
- **KEEP_LAST**: 使用最后一个定义覆盖之前的
- **THROW_EXCEPTION**: 遇到冲突时抛出异常
- **MERGE**: 尝试智能合并，检测真正的冲突

### 3. Using语句支持
完整支持OData 4.0的Using语句：
```xml
<Schema Namespace="Microsoft.OData.Core.Test.Address">
  <Using Namespace="Microsoft.OData.Core.Test.Common" Alias="Common"/>
  <!-- 现在可以使用 Common.Person 引用其他namespace的类型 -->
</Schema>
```

### 4. 依赖关系可视化数据
提供完整的依赖关系图数据，可用于：
- 生成依赖关系图表
- 依赖层级排序
- 构建打包顺序
- 影响范围评估

## 测试覆盖

项目要求100%的测试覆盖率，包括：
- 单元测试：测试每个组件的独立功能
- 集成测试：测试组件间的协作
- 边界测试：测试异常情况和边界条件
- 性能测试：验证处理大量Schema的性能

## 技术要求

- **Java 8+**: 使用Java 8语法特性
- **Maven**: 构建管理
- **Olingo 5.0.0**: OData核心库
- **JUnit 5**: 单元测试框架
- **SLF4J + Logback**: 日志框架

## 构建和运行

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 生成覆盖率报告
mvn jacoco:report

# 打包
mvn package
```

## 扩展指南

### 添加新的扩展类
1. 继承相应的Olingo类（如CsdlComplexType）
2. 实现或组合DependencyTracker接口
3. 重写相关的setter方法，在修改时调用analyzeDependencies()
4. 编写对应的单元测试

### 自定义验证规则
1. 实现ODataValidator接口
2. 定义新的ErrorType和WarningType
3. 在DefaultODataSchemaProcessor中注册新的验证器

### 自定义依赖分析算法
1. 实现DependencyAnalyzer接口
2. 提供不同的依赖分析策略
3. 支持不同类型的依赖关系（继承、组合、引用等）

这个框架提供了一个完整、可扩展的OData 4.0 Schema处理解决方案，既保持了与Olingo生态的兼容性，又提供了强大的依赖分析和验证功能。
