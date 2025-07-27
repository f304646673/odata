# Olingo 多层XML Schema聚合示例

## 概述

本示例演示了如何使用Apache Olingo框架从多层文件夹结构中读取、聚合和处理OData XML Schema文件。

## 功能特性

1. **多层文件夹扫描**: 递归扫描指定目录下的所有XML Schema文件
2. **Namespace聚合**: 按照Schema的namespace自动分组和聚合
3. **Schema合并**: 合并相同namespace下的多个Schema文件
4. **依赖分析**: 分析Schema之间的依赖关系
5. **统一输出**: 生成包含所有依赖的完整XML文件

## 项目结构

```
samples/odata-schema-processor/
├── src/main/java/
│   └── org/apache/olingo/schema/processor/
│       └── examples/
│           └── SimpleSchemaAggregatorExample.java
├── examples/
│   ├── schemas/
│   │   ├── core/
│   │   │   └── CoreTypes.xml          # 核心类型Schema
│   │   ├── business/
│   │   │   └── BusinessEntities.xml   # 业务实体Schema
│   │   └── common/
│   │       └── Products.xml           # 通用产品Schema
│   ├── containers/
│   │   └── MainContainer.xml          # 容器定义
│   └── output/
│       └── AggregatedSchema.xml       # 聚合后的完整Schema
```

## 使用方法

### 1. 编译项目

```bash
cd samples/odata-schema-processor
mvn compile
```

### 2. 下载依赖

```bash
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
```

### 3. 运行示例

```bash
java -cp "target/classes;target/dependency/*" org.apache.olingo.schema.processor.examples.SimpleSchemaAggregatorExample
```

### 4. 查看结果

运行完成后，查看生成的文件：
- `examples/output/AggregatedSchema.xml` - 聚合后的完整Schema

## 示例输出

程序运行后会显示类似以下的日志信息：

```
开始多层XML Schema聚合示例...
创建示例XML文件结构...
开始扫描文件夹: examples\schemas
处理Schema文件: examples\schemas\business\BusinessEntities.xml
添加新Schema，namespace: Business.Entities
处理Schema文件: examples\schemas\common\Products.xml
添加新Schema，namespace: Common.Products
处理Schema文件: examples\schemas\core\CoreTypes.xml
添加新Schema，namespace: Core.Types
完成文件夹扫描，共处理 3 个Schema文件

=== Schema分析结果 ===
总共聚合的namespace数量: 3
Namespace: Common.Products
  EntityTypes数量: 1
  ComplexTypes数量: 0
Namespace: Business.Entities
  EntityTypes数量: 2
  ComplexTypes数量: 0
Namespace: Core.Types
  EntityTypes数量: 1
  ComplexTypes数量: 1
```

## Schema结构示例

### Core.Types (核心类型)
- `BaseEntity`: 基础实体类型，包含Id、CreatedAt、UpdatedAt
- `Address`: 地址复杂类型

### Business.Entities (业务实体)
- `Customer`: 客户实体，继承自BaseEntity
- `Order`: 订单实体，继承自BaseEntity

### Common.Products (通用产品)
- `Product`: 产品实体，继承自BaseEntity

## 技术实现

### 核心组件

1. **XML解析器**: 使用`XMLStreamReader`进行高效的XML流式解析
2. **Schema聚合器**: 按namespace组织和合并Schema
3. **依赖分析器**: 分析Schema间的引用关系
4. **XML生成器**: 构建聚合后的完整XML输出

### 关键方法

- `aggregateSchemasFromFolder()`: 递归扫描文件夹
- `parseSchemaFromXml()`: 解析单个XML Schema文件
- `mergeSchemas()`: 合并相同namespace的Schema
- `buildAggregatedSchemaXml()`: 生成聚合的XML输出

## 扩展功能

本示例可以进一步扩展以支持：

1. **导航属性解析**: 完整解析NavigationProperty关系
2. **枚举类型支持**: 解析和聚合EnumType定义
3. **Action/Function处理**: 完整的操作和函数聚合
4. **容器集成**: 将EntityContainer与Schema完全整合
5. **验证机制**: 添加Schema有效性验证
6. **依赖优化**: 智能排序和依赖优化

## 依赖项

- Apache Olingo Commons API
- SLF4J 日志框架
- Java 8+ 标准库

## 注意事项

1. 确保XML文件格式正确且符合OData Schema规范
2. 相同namespace的Schema会被自动合并
3. 程序会自动创建输出目录结构
4. 建议在测试环境中先验证Schema的有效性

## 许可证

本示例遵循Apache License 2.0开源许可证。
