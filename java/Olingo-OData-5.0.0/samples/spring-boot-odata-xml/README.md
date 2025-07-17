# Spring Boot OData XML Service

这是一个基于 Spring Boot 的 OData 服务示例，展示了如何从 XML 文件加载 Entity Data Model (EDM)，实现灵活的 OData API。

## 🎯 项目特点

- **XML-based EDM**: 从 XML 文件加载数据模型定义
- **Spring Boot 集成**: 使用 Spring Boot 3.x 和 Java 17
- **Apache Olingo**: 基于 Olingo 5.0.0 实现 OData 协议
- **完整 CRUD 操作**: 支持创建、读取、更新、删除操作
- **复杂类型支持**: 支持复杂类型（如 Address）
- **多种格式**: 支持 JSON 和 XML 响应格式
- **内存数据提供者**: 使用内存存储演示数据操作

## 📁 项目结构

```
spring-boot-odata-xml/
├── src/main/java/org/apache/olingo/sample/springboot/xml/
│   ├── ODataXmlSpringBootApplication.java     # 主应用程序类
│   ├── controller/
│   │   └── XmlBasedODataController.java       # OData 控制器
│   ├── edm/
│   │   └── XmlBasedEdmProvider.java          # XML-based EDM 提供者
│   ├── data/
│   │   └── XmlBasedDataProvider.java         # 数据提供者
│   └── processor/
│       └── XmlBasedEntityProcessor.java      # 实体处理器
├── src/main/resources/
│   ├── application.properties                # 应用配置
│   ├── service-metadata.xml                  # EDM XML 定义
│   └── static/
│       └── index.html                        # 测试页面
└── pom.xml                                   # Maven 配置
```

## 🚀 快速开始

### 1. 构建项目

```bash
mvn clean compile
```

### 2. 运行应用

```bash
mvn spring-boot:run
```

或者直接运行主类：

```bash
java -jar target/spring-boot-odata-xml-1.0.0.jar
```

### 3. 访问服务

- **测试页面**: http://localhost:8080/
- **OData 服务**: http://localhost:8080/odata/
- **服务元数据**: http://localhost:8080/odata/$metadata
- **健康检查**: http://localhost:8080/odata/health

## 🔧 配置

### application.properties

```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/

# OData 配置
odata.service.name=XMLBasedODataService
odata.service.version=1.0.0
odata.service.namespace=OData.Demo

# 日志配置
logging.level.org.apache.olingo.sample.springboot.xml=DEBUG
logging.level.org.apache.olingo.server=INFO
```

### EDM XML 定义

EDM 在 `src/main/resources/service-metadata.xml` 中定义：

```xml
<edmx:Edmx Version="4.0" xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx">
  <edmx:DataServices>
    <Schema Namespace="OData.Demo">
      <!-- 实体类型定义 -->
      <EntityType Name="Car">
        <Key>
          <PropertyRef Name="Id"/>
        </Key>
        <Property Name="Id" Type="Edm.Int32" Nullable="false"/>
        <Property Name="Model" Type="Edm.String" MaxLength="50"/>
        <Property Name="ModelYear" Type="Edm.Int32"/>
        <Property Name="Price" Type="Edm.Decimal" Precision="10" Scale="2"/>
        <Property Name="Currency" Type="Edm.String" MaxLength="3"/>
      </EntityType>
      
      <!-- 更多实体类型... -->
    </Schema>
  </edmx:DataServices>
</edmx:Edmx>
```

## 📊 数据模型

### 实体类型

1. **Car** (汽车)
   - Id: 主键
   - Model: 车型
   - ModelYear: 年款
   - Price: 价格
   - Currency: 货币

2. **Manufacturer** (制造商)
   - Id: 主键
   - Name: 名称
   - Address: 地址（复杂类型）

3. **Address** (地址 - 复杂类型)
   - Street: 街道
   - City: 城市
   - ZipCode: 邮政编码
   - Country: 国家

### 示例数据

- **汽车**: X3, A4, C-Class, X5, A6
- **制造商**: BMW (慕尼黑), Audi (英戈尔施塔特), Mercedes-Benz (斯图加特)

## 🌐 API 端点

### OData 标准端点

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/odata/` | 服务文档 |
| GET | `/odata/$metadata` | 服务元数据 |
| GET | `/odata/Cars` | 所有汽车 |
| GET | `/odata/Cars(1)` | 指定汽车 |
| GET | `/odata/Manufacturers` | 所有制造商 |
| GET | `/odata/Manufacturers(1)` | 指定制造商 |
| POST | `/odata/Cars` | 创建汽车 |
| PUT | `/odata/Cars(1)` | 更新汽车 |
| DELETE | `/odata/Cars(1)` | 删除汽车 |

### 管理端点

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/odata/health` | 健康检查 |
| GET | `/odata/info` | 服务信息 |

## 🧪 测试示例

### 获取所有汽车

```bash
curl -X GET "http://localhost:8080/odata/Cars"
```

### 获取指定汽车

```bash
curl -X GET "http://localhost:8080/odata/Cars(1)"
```

### 创建新汽车

```bash
curl -X POST "http://localhost:8080/odata/Cars" \
  -H "Content-Type: application/json" \
  -d '{
    "Model": "A8",
    "ModelYear": 2023,
    "Price": 75000.00,
    "Currency": "USD"
  }'
```

### 更新汽车

```bash
curl -X PUT "http://localhost:8080/odata/Cars(1)" \
  -H "Content-Type: application/json" \
  -d '{
    "Model": "X3 Updated",
    "ModelYear": 2024,
    "Price": 50000.00,
    "Currency": "USD"
  }'
```

### 删除汽车

```bash
curl -X DELETE "http://localhost:8080/odata/Cars(1)"
```

## 🔍 查询选项

OData 支持多种查询选项：

```bash
# 格式化为 JSON
curl "http://localhost:8080/odata/Cars?$format=json"

# 过滤
curl "http://localhost:8080/odata/Cars?$filter=ModelYear gt 2020"

# 排序
curl "http://localhost:8080/odata/Cars?$orderby=Price desc"

# 分页
curl "http://localhost:8080/odata/Cars?$top=2&$skip=1"

# 选择字段
curl "http://localhost:8080/odata/Cars?$select=Model,Price"
```

## 🏗️ 架构设计

### 核心组件

1. **XmlBasedEdmProvider**: 从 XML 文件加载 EDM 定义
2. **XmlBasedDataProvider**: 管理内存数据存储
3. **XmlBasedEntityProcessor**: 处理 OData 实体操作
4. **XmlBasedODataController**: Spring Boot 控制器

### 设计模式

- **Provider Pattern**: EDM 和数据提供者
- **MVC Pattern**: Spring Boot 控制器架构
- **Template Pattern**: OData 处理器模板

## 🔧 自定义扩展

### 添加新实体类型

1. 在 `service-metadata.xml` 中定义新的实体类型
2. 在 `XmlBasedDataProvider` 中添加数据管理逻辑
3. 重启应用程序

### 连接数据库

替换 `XmlBasedDataProvider` 中的内存存储：

```java
// 注入数据库访问层
@Autowired
private CarRepository carRepository;

// 修改数据访问方法
public EntityCollection getEntityCollection(EdmEntitySet edmEntitySet) {
    if ("Cars".equals(edmEntitySet.getName())) {
        List<Car> cars = carRepository.findAll();
        return convertToEntityCollection(cars);
    }
    // ...
}
```

## 🐛 故障排除

### 常见问题

1. **端口冲突**: 修改 `application.properties` 中的 `server.port`
2. **XML 解析错误**: 检查 `service-metadata.xml` 格式
3. **类路径问题**: 确保 XML 文件在 `src/main/resources` 目录中

### 调试模式

启用调试日志：

```properties
logging.level.org.apache.olingo.sample.springboot.xml=DEBUG
logging.level.org.apache.olingo.server=DEBUG
```

## 📚 相关资源

- [Apache Olingo 官方文档](https://olingo.apache.org/)
- [OData 规范](https://www.odata.org/documentation/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

## 📄 许可证

本项目基于 Apache License 2.0 开源许可证。

## 🤝 贡献

欢迎提交问题报告和功能请求！

---

**注意**: 这是一个演示项目，生产环境使用时请考虑安全性、性能和扩展性要求。
