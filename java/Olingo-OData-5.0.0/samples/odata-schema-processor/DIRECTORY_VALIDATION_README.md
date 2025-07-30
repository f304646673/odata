# 目录级别 OData XML Schema 验证器

## 概述

本模块扩展了现有的单文件XML验证功能，提供了目录级别的OData XML Schema验证能力。新的验证器可以：

1. **批量验证** - 验证目录中的所有XML文件
2. **冲突检测** - 检测不同文件之间的schema冲突
3. **命名空间管理** - 确保命名空间的一致性
4. **全面报告** - 提供详细的验证报告和冲突分析

## 主要特性

### 支持的验证规则

- ✅ **单文件验证** - 重用现有的单文件验证逻辑
- ✅ **重复元素检测** - 检测同一命名空间中的重复元素定义
- ✅ **重复命名空间检测** - 检测多个文件中重复的完整命名空间定义
- ✅ **不兼容定义检测** - 检测同一元素的不兼容定义
- ✅ **并发处理** - 支持多文件并发验证提高性能

### 核心组件

#### 1. DirectorySchemaValidator
主要的目录级别验证器，协调整个验证过程。

```java
DirectorySchemaValidator validator = new DirectorySchemaValidator(fileValidator);
DirectoryValidationResult result = validator.validateDirectory(directoryPath);
```

#### 2. SchemaConflictDetector
专门用于检测schema冲突的组件。

支持的冲突类型：
- `DUPLICATE_ELEMENT` - 重复元素定义
- `DUPLICATE_NAMESPACE_SCHEMA` - 重复命名空间schema
- `INCOMPATIBLE_DEFINITION` - 不兼容的定义
- `CIRCULAR_REFERENCE` - 循环引用
- `MISSING_REFERENCE` - 缺失引用

#### 3. DirectoryValidationResult
包含完整验证结果的容器类，提供：
- 个别文件的验证结果
- 跨文件冲突列表
- 命名空间到文件的映射
- 全局错误和警告
- 性能统计

## 使用示例

### 基本用法

```java
// 创建验证器
XmlFileComplianceValidator fileValidator = new OlingoXmlFileComplianceValidator();
DirectorySchemaValidator validator = new DirectorySchemaValidator(fileValidator);

// 验证目录
Path schemaDirectory = Paths.get("/path/to/schemas");
DirectoryValidationResult result = validator.validateDirectory(schemaDirectory);

// 检查结果
if (result.isCompliant()) {
    System.out.println("所有文件都符合规范，无冲突");
} else {
    System.out.println("发现问题:");
    result.getConflicts().forEach(System.out::println);
    result.getGlobalErrors().forEach(System.out::println);
}

// 清理资源
validator.shutdown();
```

### 使用文件模式

```java
// 只验证特定模式的文件
DirectoryValidationResult result = validator.validateDirectory(directoryPath, "schema_*.xml");
```

### 命令行工具

```bash
# 验证目录中的所有XML文件
java -cp ... DirectoryValidatorDemo /path/to/schemas

# 验证特定模式的文件
java -cp ... DirectoryValidatorDemo /path/to/schemas "schema_*.xml"
```

## 验证规则详解

### 1. 命名空间规则
- **允许**: 不同文件可以定义不同的命名空间
- **允许**: 同一命名空间可以在多个文件中定义不同的元素
- **禁止**: 同一命名空间在多个文件中定义完整的schema
- **禁止**: 同一命名空间中定义相同的元素

### 2. 元素定义规则
- **禁止**: 在同一命名空间中定义相同名称的元素
- **禁止**: 对同一元素有不兼容的定义

### 3. 文件组织最佳实践
```
schemas/
├── common/
│   ├── types.xml        # 通用类型定义 (CommonTypes namespace)
│   └── enums.xml        # 枚举定义 (CommonEnums namespace)
├── customer/
│   └── customer.xml     # 客户相关定义 (Customer namespace)
└── product/
    └── product.xml      # 产品相关定义 (Product namespace)
```

## 测试用例

项目包含全面的测试用例，位于 `src/test/resources/directory-validation/`：

### 1. 有效案例 (`valid-separate-namespaces/`)
- 不同文件定义不同命名空间
- 所有文件都符合OData规范

### 2. 冲突案例 (`conflict-duplicate-elements/`)
- 相同命名空间中定义重复元素
- 演示元素冲突检测

### 3. 命名空间冲突案例 (`conflict-duplicate-namespace/`)
- 多个文件定义相同命名空间的完整schema
- 演示命名空间级别冲突检测

### 4. 混合案例 (`mixed-valid-invalid/`)
- 包含有效和无效文件
- 演示错误处理能力

## 性能特性

- **并发验证**: 支持多文件并发处理
- **可配置并发度**: 默认4个并发线程，可自定义
- **增量处理**: 大量文件时采用流式处理
- **内存优化**: 避免加载所有文件到内存

## 扩展性

### 自定义冲突检测规则
```java
public class CustomConflictDetector extends SchemaConflictDetector {
    @Override
    public List<SchemaConflict> detectConflicts() {
        List<SchemaConflict> conflicts = super.detectConflicts();
        // 添加自定义检测逻辑
        conflicts.addAll(detectCustomConflicts());
        return conflicts;
    }
}
```

### 自定义验证结果处理
```java
public class CustomResultProcessor {
    public void processResult(DirectoryValidationResult result) {
        // 生成自定义报告
        // 发送通知
        // 持久化结果
    }
}
```

## 注意事项

1. **Java版本**: 需要Java 8或更高版本
2. **依赖**: 基于Apache Olingo 5.0.0
3. **线程安全**: DirectorySchemaValidator是线程安全的
4. **资源管理**: 使用完毕后请调用 `shutdown()` 方法清理线程池

## 未来改进

- [ ] 支持远程schema引用解析
- [ ] 增加更多schema兼容性检查
- [ ] 支持增量验证（只验证变更的文件）
- [ ] 添加图形化报告生成
- [ ] 支持自定义验证规则插件
