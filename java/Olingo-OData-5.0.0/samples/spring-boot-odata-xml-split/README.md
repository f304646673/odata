# Spring Boot OData XML Split Sample

此示例展示了如何使用Apache Olingo 5.0.0在Spring Boot中实现从拆分XML文件加载EDM元数据的OData服务。

## 项目概述

该项目演示了如何使用Apache Olingo的原生API从**多个分体XML文件**加载EDM元数据：

- `address-schema.xml`：包含Address复合类型定义（OData.Demo.Common命名空间）
- `main-schema.xml`：包含主要实体类型和容器（OData.Demo命名空间）

## 技术特点

### 1. 分体XML架构
- **分离关注点**：共享类型（Address）单独定义在独立文件中
- **真正的拆分**：使用两个独立的XML文件，不是合并文件
- **命名空间隔离**：不同类型定义在不同的命名空间中

### 2. 原生Olingo XML解析
- 使用Olingo的`MetadataParser`和`SchemaBasedEdmProvider`
- 顺序加载多个XML文件并合并Schema
- 完全基于Olingo原生API，不需要手动解析XML

### 3. 数据模型
- **Cars**：汽车实体，包含ID、型号、年份、价格等属性
- **Manufacturers**：制造商实体，包含ID、名称和Address复合类型
- **Address**：地址复合类型，包含街道、城市、邮编、国家等属性

### 4. Spring Boot集成
- 使用Spring Boot 3.2.0和Java 17
- 集成Tomcat嵌入式Web服务器
- 支持Jakarta EE规范

## 快速开始

### 构建项目
```bash
mvn clean package -DskipTests
```

### 运行应用
```bash
java -jar target/odata-spring-boot-xml-split-sample-5.0.0.jar
```

### 测试端点

#### 1. 服务元数据
```
GET http://localhost:8080/cars.svc/$metadata
```

#### 2. 汽车数据
```
GET http://localhost:8080/cars.svc/Cars
```

#### 3. 制造商数据（包含Address复合类型）
```
GET http://localhost:8080/cars.svc/Manufacturers
```

## 核心文件结构

### XML元数据文件
- `src/main/resources/address-schema.xml` - Address复合类型定义
- `src/main/resources/main-schema.xml` - 主要实体类型和容器定义

### Java源代码
- `XmlSplitEdmProvider` - 基于Olingo原生XML解析的分体XML EDM提供者
- `XmlSplitDataProvider` - 示例数据提供者
- `XmlSplitEntityProcessor` - OData实体处理器
- `XmlSplitODataController` - Spring Boot控制器

## 技术亮点

1. **真正的分体XML**：Address类型定义在独立的address-schema.xml文件中
2. **多文件加载**：EDM provider顺序加载两个XML文件并合并Schema
3. **命名空间分离**：Address在OData.Demo.Common命名空间，实体在OData.Demo命名空间
4. **复合类型处理**：Manufacturer实体正确引用OData.Demo.Common.Address
5. **原生Olingo集成**：完全使用Olingo的原生API进行分体XML解析和EDM构建
6. **生产就绪**：代码简洁，无调试信息，适合生产环境使用

## 示例数据

### 汽车数据
- BMW X3 (2020) - $45,000
- Audi A4 (2021) - $42,000  
- Mercedes C-Class (2022) - $48,000

### 制造商数据
- BMW: 慕尼黑总部
- Audi: 英戈尔施塔特总部
- Mercedes-Benz: 斯图加特总部

## 技术栈

- Apache Olingo 5.0.0
- Spring Boot 3.2.0
- Java 17
- Maven 3.9+

## 相关项目

- `samples/spring-boot-odata` - 基础Spring Boot OData示例
- `samples/spring-boot-odata-xml` - 单文件XML EDM示例
