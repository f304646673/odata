# OData 4.0 目录验证系统

## 概述

这是一个完整的OData 4.0 XML目录验证系统，支持多层目录结构的Schema验证、跨文件冲突检测和依赖关系管理。

## 核心功能

### 1. 多文件验证
- ✅ 支持多层目录结构
- ✅ 自动发现所有XML文件
- ✅ 按依赖关系排序处理
- ✅ 批量验证和报告

### 2. Schema冲突检测
- 🔍 同命名空间元素冲突检测
- 🔍 跨命名空间别名冲突检测
- 🔍 循环依赖检测
- 🔍 详细冲突报告生成

### 3. 合规性验证
- 📋 继承关系验证（ComplexType不能继承EntityType）
- 📋 类型引用完整性检查
- 📋 命名空间一致性验证
- 📋 OData 4.0规范合规检查

## 项目结构

```
src/main/java/org/apache/olingo/compliance/validation/
├── core/                           # 核心验证框架
│   ├── ComplianceKnowledgeBase.java    # 只读知识库
│   ├── ComplianceContext.java          # 可更新上下文
│   ├── ComplianceValidationManager.java # 验证管理器
│   └── ComplianceUsageExample.java     # 使用示例
└── directory/                      # 目录验证专用
    ├── DirectoryValidationManager.java # 目录验证管理器
    ├── SchemaConflictDetector.java     # 冲突检测器
    └── DirectoryValidationExample.java # 目录验证示例

src/test/resources/validation/multiple/
├── valid/                          # 有效场景测试数据
│   ├── scenario1-separate-namespaces/     # 分离命名空间
│   ├── scenario2-same-namespace-no-conflicts/  # 同命名空间无冲突
│   └── scenario3-multilevel-directories/  # 多层目录结构
└── invalid/                        # 无效场景测试数据
    ├── scenario1-element-conflicts/        # 元素冲突
    ├── scenario2-alias-conflicts/          # 别名冲突
    ├── scenario3-invalid-inheritance/      # 无效继承
    └── scenario4-missing-dependencies/     # 缺失依赖
```

## 快速开始

### 1. 基本使用

```java
// 创建目录验证管理器
DirectoryValidationManager validationManager = new DirectoryValidationManager();

// 验证整个目录
DirectoryValidationResult result = validationManager.validateDirectory("path/to/schemas");

// 检查结果
System.out.println("验证结果: " + result.isValid());
System.out.println("总文件数: " + result.getTotalFiles());
System.out.println("有效文件数: " + result.getValidFileCount());
System.out.println("冲突数量: " + result.getConflictIssues().size());
```

### 2. 详细冲突分析

```java
if (!result.isValid()) {
    // 显示冲突详情
    result.getConflictIssues().forEach(issue -> {
        System.out.println(issue.getErrorType() + ": " + issue.getMessage());
        System.out.println("文件: " + issue.getFilePath());
    });
    
    // 显示文件级验证错误
    result.getValidationResults().forEach((filePath, validationResult) -> {
        if (!validationResult.isValid()) {
            validationResult.getIssues().forEach(issue -> {
                System.out.println(issue.getErrorType() + ": " + issue.getMessage());
            });
        }
    });
}
```

### 3. 生成冲突报告

```java
SchemaConflictDetector conflictDetector = new SchemaConflictDetector();
SchemaConflictDetector.ConflictDetectionReport report = 
    conflictDetector.generateReport(namespaceToSchemas);

System.out.println("冲突报告: " + report.toString());
System.out.println("元素冲突: " + report.getElementConflicts().size());
System.out.println("别名冲突: " + report.getAliasConflicts().size());
```

## 测试场景

### 有效场景

1. **分离命名空间** (`scenario1-separate-namespaces`)
   - 不同文件使用不同命名空间
   - 无元素冲突
   - 验证通过

2. **同命名空间无冲突** (`scenario2-same-namespace-no-conflicts`)
   - 多个文件共享相同命名空间
   - 不同元素名称
   - 验证通过

3. **多层目录结构** (`scenario3-multilevel-directories`)
   - 包含基础类型和扩展类型
   - 正确的继承关系（EntityType继承EntityType）
   - 多层目录组织
   - 验证通过

### 无效场景

1. **元素冲突** (`scenario1-element-conflicts`)
   - 同命名空间下重复定义相同元素
   - 检测到ELEMENT_CONFLICT错误
   - 验证失败

2. **别名冲突** (`scenario2-alias-conflicts`)
   - 不同命名空间使用相同别名
   - 检测到ALIAS_CONFLICT错误
   - 验证失败

3. **无效继承** (`scenario3-invalid-inheritance`)
   - ComplexType试图继承EntityType
   - 检测到INVALID_BASE_TYPE错误
   - 验证失败

4. **缺失依赖** (`scenario4-missing-dependencies`)
   - 引用不存在的类型
   - 检测到TYPE_NOT_EXIST错误
   - 验证失败

## 运行测试

```bash
# 运行目录验证测试
mvn test -Dtest=DirectoryValidationTest

# 运行冲突检测测试
mvn test -Dtest=SchemaConflictDetectorTest

# 运行所有多文件验证测试
mvn test -Dtest="**.*multiple*"
```

## 性能特征

- ✅ 支持大规模Schema集合（测试过100个命名空间，500个Schema）
- ✅ 并发安全的数据结构
- ✅ 缓存机制提升性能
- ✅ 内存高效的冲突检测算法

## 扩展点

### 1. 自定义冲突检测规则

```java
public class CustomConflictDetector extends SchemaConflictDetector {
    @Override
    public List<ComplianceIssue> detectConflicts(
        Map<String, Set<DirectoryValidationManager.SchemaInfo>> namespaceToSchemas) {
        // 自定义检测逻辑
    }
}
```

### 2. 自定义验证规则

```java
public class CustomValidationManager extends DirectoryValidationManager {
    @Override
    protected List<File> orderFilesByDependencies(List<File> xmlFiles, 
        Map<String, List<SchemaInfo>> fileSchemas) {
        // 自定义排序逻辑
    }
}
```

## 最佳实践

1. **目录组织**
   - 按功能模块组织子目录
   - 使用有意义的文件名
   - 基础类型文件放在依赖文件之前

2. **命名空间管理**
   - 确保命名空间唯一性
   - 避免跨文件元素冲突
   - 谨慎使用别名

3. **验证策略**
   - 先单文件验证，再目录验证
   - 关注冲突检测结果
   - 定期运行完整验证

4. **性能优化**
   - 大型目录分批验证
   - 利用缓存机制
   - 监控验证时间

## 错误类型参考

| 错误类型 | 描述 | 检测级别 |
|---------|------|---------|
| ELEMENT_CONFLICT | 跨文件元素冲突 | 目录级 |
| ALIAS_CONFLICT | 别名冲突 | 目录级 |
| INVALID_BASE_TYPE | 无效继承关系 | 文件级 |
| TYPE_NOT_EXIST | 引用类型不存在 | 文件级 |
| NAMESPACE_CONFLICT | 命名空间冲突 | 目录级 |
| CIRCULAR_REFERENCE | 循环引用 | 目录级 |

## 版本信息

- OData版本: 4.0
- Java版本: 8+
- 测试框架: JUnit 5
- 构建工具: Maven
