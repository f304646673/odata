# Spring Boot OData XML Native Sample

这个项目展示了如何使用 Apache Olingo 的原生 XML 处理功能来创建一个 Spring Boot OData 服务，而无需手动解析 XML 文件。

## 项目特点

### 与 `spring-boot-odata-xml` 的不同

- **手动 XML 解析 vs 原生 API**：
  - `spring-boot-odata-xml` 使用 JAXB 手动解析 XML 元数据文件
  - 本项目使用 Olingo 的原生 EDM API 来定义元数据

- **EDM 提供者**：
  - `spring-boot-odata-xml` 使用 `XmlBasedEdmProvider` 解析 XML
  - 本项目使用 `NativeXmlEdmProvider` 通过程序化方式定义 EDM

- **XML 处理**：
  - `spring-boot-odata-xml` 需要手动解析 XML 文件
  - 本项目使用 Olingo 内置的 XML 序列化/反序列化功能

## 技术架构

### 核心组件

1. **NativeXmlEdmProvider**：
   - 扩展 `CsdlAbstractEdmProvider`
   - 使用 Olingo 的原生 API 定义 EDM
   - 提供 XML 序列化演示功能

2. **NativeXmlDataProvider**：
   - 使用 Olingo 的原生数据结构
   - 提供内存中的 CRUD 操作
   - 使用 `Entity`、`EntityCollection`、`ComplexValue` 等原生类型

3. **NativeXmlEntityProcessor**：
   - 实现 `EntityCollectionProcessor` 和 `EntityProcessor`
   - 使用 Olingo 的原生序列化器
   - 支持实体集合和单个实体请求

4. **NativeXmlODataController**：
   - Spring Boot REST 控制器
   - 集成 Olingo 的 `ODataHttpHandler`
   - 提供健康检查和信息端点

## 项目结构

```
src/main/java/org/apache/olingo/sample/springboot/xmlnative/
├── ODataXmlNativeSpringBootApplication.java  # 主应用程序类
├── controller/
│   └── NativeXmlODataController.java         # OData 控制器
├── data/
│   └── NativeXmlDataProvider.java            # 数据提供者
├── edm/
│   └── NativeXmlEdmProvider.java             # EDM 提供者
└── processor/
    └── NativeXmlEntityProcessor.java         # 实体处理器
```

## 构建和运行

### 构建项目

```bash
mvn clean package -f pom-standalone.xml -DskipTests
```

### 运行应用程序

```bash
java -jar target/odata-spring-boot-xml-native-sample-5.0.0.jar
```

## API 端点

### OData 端点

- **服务文档**: http://localhost:8080/cars.svc/
- **元数据**: http://localhost:8080/cars.svc/$metadata
- **Cars 实体集**: http://localhost:8080/cars.svc/Cars
- **Manufacturers 实体集**: http://localhost:8080/cars.svc/Manufacturers
- **单个实体**: http://localhost:8080/cars.svc/Cars(1)

### 管理端点

- **健康检查**: http://localhost:8080/cars.svc/health
- **服务信息**: http://localhost:8080/cars.svc/info
- **XML 演示**: http://localhost:8080/cars.svc/xml-demo

## 示例数据

### Cars 实体

```json
{
  "Id": 1,
  "Model": "X3",
  "ModelYear": 2020,
  "Price": 45000.00,
  "Currency": "USD"
}
```

### Manufacturers 实体

```json
{
  "Id": 1,
  "Name": "BMW",
  "Address": {
    "Street": "Petuelring 130",
    "City": "Munich",
    "ZipCode": "80809",
    "Country": "Germany"
  }
}
```

## 技术细节

### Olingo 原生 XML 处理

1. **EDM 定义**：
   - 使用 `CsdlEntityType`, `CsdlComplexType`, `CsdlEntityContainer` 等原生类型
   - 通过程序化方式定义元数据，而非解析 XML

2. **数据结构**：
   - 使用 `Entity`, `EntityCollection`, `ComplexValue` 等原生数据类型
   - 直接使用 Olingo 的内部数据表示

3. **序列化**：
   - 使用 `ODataSerializer` 进行 XML/JSON 序列化
   - 利用 Olingo 的内置序列化功能

### 与传统方法的比较

| 特性 | 传统 XML 解析 | Olingo 原生方法 |
|------|---------------|-----------------|
| XML 处理 | 手动解析 JAXB | 内置 API |
| 错误处理 | 手动验证 | 自动验证 |
| 性能 | 解析开销 | 直接内存操作 |
| 维护性 | 复杂 | 简单 |

## 开发说明

### 扩展功能

1. **添加新实体类型**：
   - 在 `NativeXmlEdmProvider` 中定义新的实体类型
   - 在 `NativeXmlDataProvider` 中添加相应的数据

2. **实现 CRUD 操作**：
   - 在 `NativeXmlEntityProcessor` 中实现 `createEntity`, `updateEntity`, `deleteEntity`
   - 使用 Olingo 的反序列化器处理请求体

3. **添加导航属性**：
   - 在 EDM 中定义导航属性
   - 在数据提供者中实现关联数据

### 配置选项

在 `application.properties` 中可以配置：

- 服务端口
- 日志级别
- 执行器端点
- 自定义 OData 配置

## 总结

这个项目展示了如何使用 Apache Olingo 的原生 XML 处理功能来创建 OData 服务，避免了手动 XML 解析的复杂性。通过使用 Olingo 的内置 API，可以获得更好的性能、更简单的维护和更强的类型安全性。

主要优势：
- 使用 Olingo 的原生 EDM API，无需手动解析 XML
- 内置的序列化/反序列化功能
- 更好的错误处理和验证
- 更简洁的代码结构

这种方法特别适合需要高性能和易维护的 OData 服务开发场景。
