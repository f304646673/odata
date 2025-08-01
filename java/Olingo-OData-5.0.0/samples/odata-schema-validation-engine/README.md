# OData Schema Validation Engine

## 项目概述

OData Schema Validation Engine 是一个基于Apache Olingo的OData 4.0 XML模式验证和集成框架。它严格使用现有的`samples/odata-compliance-system`和`samples/odata-schema-repository`包的API，提供了统一的验证、冲突检测和模式合并功能。

## 核心特性

- **完全基于现有API**: 不重新实现任何XML解析或验证逻辑
- **迭代验证流程**: 
  1. 使用compliance system验证XML目录
  2. 检测潜在的schema冲突
  3. 合并验证通过的schemas到repository
- **Java 8兼容**: 完全兼容Java 8语法和特性
- **命令行和交互式界面**: 支持批处理和交互模式
- **完整测试覆盖**: JUnit 5测试确保代码质量

## 项目结构

```
odata-schema-validation-engine/
├── pom.xml                              # Maven配置
├── README.md                            # 项目文档
└── src/
    ├── main/java/
    │   └── org/apache/olingo/schema/validation/engine/
    │       ├── ODataSchemaValidationEngine.java      # 核心引擎
    │       ├── IntegrationResult.java                # 结果封装
    │       └── ODataSchemaValidationEngineMain.java  # 主程序
    └── test/java/
        └── org/apache/olingo/schema/validation/engine/
            └── ODataSchemaValidationEngineTest.java  # 测试类
```

## 核心组件

### 1. ODataSchemaValidationEngine
主要的验证引擎，协调不同的验证阶段：
- 使用 `DirectoryValidationManager` 进行目录级别的合规性验证
- 集成 `ODataSchemaRepository` 进行schema管理
- 提供统一的处理接口

### 2. IntegrationResult
封装处理结果，包括：
- 验证状态（成功/失败/冲突/错误）
- 详细的错误信息和冲突报告
- 性能统计（处理时间、文件数量）

### 3. ODataSchemaValidationEngineMain
提供命令行和交互式界面：
- 命令行模式：`java ... ODataSchemaValidationEngineMain <directory>`
- 交互模式：`java ... ODataSchemaValidationEngineMain`

## 使用方法

### 编译项目
```bash
mvn clean compile
```

### 运行测试
```bash
mvn test
```

### 命令行使用
```bash
# 验证指定目录
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) \
     org.apache.olingo.schema.validation.engine.ODataSchemaValidationEngineMain /path/to/xml/directory

# 交互模式
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) \
     org.apache.olingo.schema.validation.engine.ODataSchemaValidationEngineMain
```

## 依赖关系

项目依赖于以下核心组件：
- `odata-compliance-system`: 提供XML验证功能
- `odata-schema-repository`: 提供schema管理功能
- JUnit 5 + Mockito: 测试框架
- SLF4J + Logback: 日志框架

## 处理流程

1. **输入验证**: 检查目录路径有效性
2. **合规性验证**: 使用DirectoryValidationManager验证所有XML文件
3. **冲突检测**: 检查schema之间的潜在冲突（待完善）
4. **Repository集成**: 将验证通过的schemas合并到repository（待完善）
5. **结果报告**: 返回详细的处理结果和统计信息

## 扩展点

当前实现为框架版本，以下功能可以进一步完善：

1. **冲突检测逻辑**: 实现具体的schema冲突检测算法
2. **Repository集成**: 完善schema合并到repository的具体实现
3. **更多验证规则**: 扩展compliance system的验证规则
4. **批处理支持**: 支持多目录批量处理

## 技术决策

- **严格API复用**: 所有XML解析和验证都通过现有包完成
- **Java 8兼容**: 避免使用Java 9+特性（如`List.of()`, `var`）
- **模块化设计**: 每个组件职责清晰，易于测试和扩展
- **错误处理**: 完善的异常处理和错误报告机制

## 成功指标

- ✅ 项目成功编译
- ✅ 所有测试通过
- ✅ 主程序能够处理真实XML文件
- ✅ 严格使用指定包的API
- ✅ Java 8兼容性

这个项目为OData 4.0 XML验证和集成提供了一个可扩展的框架基础。
