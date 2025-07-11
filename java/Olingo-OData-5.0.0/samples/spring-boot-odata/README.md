# Spring Boot OData Sample

这个项目演示了如何将 Apache Olingo OData 框架与 Spring Boot 集成，提供与传统 HttpServlet 实现相同的功能，但使用现代 Spring Boot 架构。

## 架构对比

### 传统 HttpServlet (CarsServlet)
```
HTTP 请求 → CarsServlet.service() → OData 框架 → 响应
```

### Spring Boot 实现
```
HTTP 请求 → ODataController → ODataSpringBootService → OData 框架 → 响应
```

## 核心组件

### 1. ODataSpringBootService
- **参考**: `CarsServlet.service()` 方法
- **目的**: 核心 OData 处理逻辑
- **功能**:
  - 基于 Session 的数据提供器管理
  - OData 框架初始化
  - 请求委托给 OData 处理器

### 2. SpringBootDataProvider
- **参考**: 原始 `DataProvider`
- **目的**: 内存中汽车数据管理
- **功能**:
  - 线程安全操作
  - Car 实体的 CRUD 操作
  - 示例数据初始化

### 3. SpringBootEdmProvider
- **参考**: 原始 `CarsEdmProvider`
- **目的**: 定义 OData 实体数据模型 (EDM)
- **功能**:
  - Car 实体类型定义
  - Schema 和 metadata 生成

### 4. SpringBootCarsProcessor
- **参考**: 原始 `CarsProcessor`
- **目的**: 处理 OData 实体集合请求
- **功能**:
  - EntityCollectionProcessor 实现
  - 数据序列化和响应格式化

### 5. ODataController
- **Spring Boot 特有**: REST 控制器模式
- **目的**: 使用 Spring MVC 模式暴露 OData 端点
- **功能**:
  - 处理所有 `/cars.svc/*` 下的请求
  - 委托给 ODataSpringBootService 处理

## 快速开始

### 1. 编译项目
```bash
mvn -f pom-standalone.xml clean compile
```

### 2. 运行应用程序
```bash
mvn -f pom-standalone.xml spring-boot:run
```

应用程序将在 `http://localhost:8080` 启动。

### 3. 测试 OData 端点

#### 服务文档
```
GET http://localhost:8080/cars.svc
```

#### 元数据
```
GET http://localhost:8080/cars.svc/$metadata
```

#### 获取所有汽车
```
GET http://localhost:8080/cars.svc/Cars
```

#### 按 ID 获取汽车
```
GET http://localhost:8080/cars.svc/Cars(1)
```

#### OData 查询选项示例
```
GET http://localhost:8080/cars.svc/Cars?$filter=Brand eq 'BMW'
GET http://localhost:8080/cars.svc/Cars?$orderby=Year desc
GET http://localhost:8080/cars.svc/Cars?$top=2
GET http://localhost:8080/cars.svc/Cars?$skip=1
GET http://localhost:8080/cars.svc/Cars?$select=Brand,Model
```

## 项目结构

```
src/main/java/org/apache/olingo/sample/springboot/
├── ODataSpringBootApplication.java     # Spring Boot 主应用类
├── controller/
│   └── ODataController.java            # REST 控制器
├── service/
│   └── ODataSpringBootService.java     # 核心 OData 服务
├── data/
│   └── SpringBootDataProvider.java     # 数据提供器
├── edm/
│   └── SpringBootEdmProvider.java      # EDM 提供器
└── processor/
    └── SpringBootCarsProcessor.java    # OData 处理器
```

## 关键差异

### 与传统 HttpServlet 的对比

1. **依赖注入**: 使用 Spring 的 `@Autowired` 而不是手动实例化
2. **配置管理**: 使用 `application.properties` 而不是 `web.xml`
3. **组件管理**: Spring 容器管理生命周期
4. **嵌入式服务器**: 内置 Tomcat，无需外部容器

### 保持的相似性

1. **OData 处理逻辑**: 与原始 `CarsServlet` 完全相同的处理流程
2. **Session 管理**: 保持相同的会话级数据提供器模式
3. **错误处理**: 相同的异常处理策略
4. **数据模型**: 相同的 Car 实体和 EDM 定义

## 技术栈

- **Spring Boot 3.2.0**: 现代 Java 应用框架
- **Apache Olingo 5.0.0**: OData 协议实现
- **Jakarta Servlet API 6.0.0**: Servlet 规范
- **Java 17**: 最低 Java 版本要求

## 开发特性

- 支持热重载开发
- 集成 Spring Boot DevTools
- 嵌入式 Tomcat 服务器
- 自动配置和依赖管理
- 丰富的日志和监控支持

## 部署

### 构建可执行 JAR
```bash
mvn -f pom-standalone.xml clean package
```

### 运行打包的应用
```bash
java -jar target/Spring\ Boot\ OData\ Sample-5.0.0.jar
```

## 扩展示例

### 添加新的实体类型
1. 在 `SpringBootDataProvider` 中添加数据管理
2. 在 `SpringBootEdmProvider` 中定义 EDM
3. 创建相应的处理器类
4. 在控制器中添加端点映射

### 自定义配置
在 `application.properties` 中添加配置：
```properties
server.port=8081
logging.level.org.apache.olingo=DEBUG
spring.application.name=Custom OData Service
```

这个项目展示了如何将传统的 Servlet 式 OData 服务现代化为 Spring Boot 应用，同时保持核心 OData 功能的完整性。
- **Purpose**: HTTP request handling and routing
- **Features**:
  - `/cars.svc/**` endpoint mapping
  - Error handling and logging

## Running the Application

### 1. Build the project
```bash
mvn clean package
```

### 2. Run with Spring Boot
```bash
mvn spring-boot:run
```

### 3. Run with debugging
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

## Testing the Service

### 1. Service Document
```
GET http://localhost:8080/cars.svc/
```

### 2. Metadata Document
```
GET http://localhost:8080/cars.svc/$metadata
```

### 3. Cars Collection
```
GET http://localhost:8080/cars.svc/Cars
```

### 4. Health Check
```
GET http://localhost:8080/cars.svc/health
```

## Debugging

The application supports remote debugging on port 5005 when started with JVM debug arguments. You can use VS Code or any IDE to attach to the debug session.

## Key Differences from HttpServlet

1. **Dependency Injection**: Uses Spring's @Autowired for dependency management
2. **Configuration**: Uses application.properties instead of web.xml
3. **Error Handling**: Leverages Spring Boot's error handling mechanisms
4. **Logging**: Integrated with Spring Boot's logging framework
5. **Packaging**: JAR with embedded Tomcat instead of WAR deployment

## Benefits of Spring Boot Approach

1. **Simplified Configuration**: No need for web.xml or external container setup
2. **Auto-configuration**: Spring Boot handles many configuration details automatically
3. **Embedded Server**: No need for separate Tomcat installation
4. **Production Ready**: Built-in metrics, health checks, and monitoring
5. **Developer Experience**: Hot reload, better debugging, and development tools
