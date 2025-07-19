# Spring Boot OData XML Database Sample

这个项目演示了如何创建一个基于Spring Boot的OData服务，该服务：
1. 连接PostgreSQL数据库
2. 使用XML Schema生成POJO模型
3. 提供OData REST API接口
4. 实现数据库和OData服务之间的数据映射

## 项目特性

- **数据库连接**: 连接到PostgreSQL数据库 (172.31.107.222)
- **XML Schema支持**: 使用JAXB从XSD生成Java POJO类
- **JPA集成**: 使用Spring Data JPA进行数据库操作
- **OData服务**: 基于Apache Olingo的OData v4服务
- **自动初始化**: 应用启动时自动清空并重新创建数据库表

## 数据库配置

```properties
spring.datasource.url=jdbc:postgresql://172.31.107.222:5432/odata_xmldb
spring.datasource.username=fangliang
spring.datasource.password=fangliang
```

## 项目结构

```
src/main/java/org/apache/olingo/sample/springboot/xmldb/
├── XmlDbODataApplication.java          # 主应用类
├── controller/
│   └── XmlDbODataController.java       # OData控制器
├── data/
│   └── XmlDbDataProvider.java          # 数据提供者
├── edm/
│   └── XmlDbEdmProvider.java           # EDM元数据提供者
├── entity/
│   ├── CarEntity.java                  # Car JPA实体
│   └── ManufacturerEntity.java         # Manufacturer JPA实体
├── processor/
│   ├── BaseXmlDbODataProcessor.java    # 基础OData处理器
│   └── XmlDbEntityProcessor.java       # 实体处理器
├── repository/
│   ├── CarRepository.java              # Car数据仓库
│   └── ManufacturerRepository.java     # Manufacturer数据仓库
└── service/
    └── XmlDbDataService.java           # 数据服务层

src/main/resources/
├── xsd/
│   └── entities.xsd                    # XML Schema定义
└── application.properties              # 应用配置
```

## 构建和运行

### 使用独立POM构建

```bash
# 生成JAXB类并编译
mvn -f pom-standalone.xml clean compile

# 运行应用
mvn -f pom-standalone.xml spring-boot:run
```

### 访问OData服务

应用启动后，可以通过以下URL访问OData服务：

- **服务文档**: http://localhost:8080/xmldb-odata/
- **元数据**: http://localhost:8080/xmldb-odata/$metadata
- **所有汽车**: http://localhost:8080/xmldb-odata/Cars
- **特定汽车**: http://localhost:8080/xmldb-odata/Cars(1)
- **所有制造商**: http://localhost:8080/xmldb-odata/Manufacturers
- **特定制造商**: http://localhost:8080/xmldb-odata/Manufacturers(1)

## XML Schema到POJO

项目使用JAXB Maven插件从`src/main/resources/xsd/entities.xsd`生成Java POJO类。生成的类将位于：
```
target/generated-sources/jaxb/org/apache/olingo/sample/springboot/xmldb/model/
```

## 数据初始化

应用启动时会自动：
1. 清空现有数据库表
2. 重新创建表结构
3. 插入示例数据（BMW、Audi、Toyota制造商及相关汽车数据）

## 技术栈

- **Spring Boot 3.2.0**: 应用框架
- **Spring Data JPA**: 数据访问层
- **PostgreSQL**: 数据库
- **Apache Olingo 5.0.0**: OData框架
- **JAXB**: XML绑定
- **Maven**: 构建工具

## API示例

### 获取所有汽车
```
GET http://localhost:8080/xmldb-odata/Cars
```

### 获取特定汽车
```
GET http://localhost:8080/xmldb-odata/Cars(1)
```

### 获取所有制造商
```
GET http://localhost:8080/xmldb-odata/Manufacturers
```

### OData查询选项支持
```
# 过滤
GET http://localhost:8080/xmldb-odata/Cars?$filter=Price gt 40000

# 排序
GET http://localhost:8080/xmldb-odata/Cars?$orderby=Price desc

# 分页
GET http://localhost:8080/xmldb-odata/Cars?$top=5&$skip=10
```
