# OData Tutorial P0_ALL - 完整功能演示

## 项目概述

这是Apache Olingo OData V4的完整功能演示项目，展示了OData服务的所有核心特性，包括：

- 🔧 基础CRUD操作
- 🔍 查询选项 ($filter, $select, $orderby, $top, $skip, $count)
- 🧭 导航属性
- ⚡ Actions和Functions
- 📦 批处理 (Batch Processing)
- 📊 媒体实体 (Media Entities)

## 环境要求

- **Java**: JDK 8或更高版本
- **Maven**: 3.6+
- **内存**: 建议设置 `MAVEN_OPTS=-Xmx1024m`

## 🚀 快速启动

### 方法1: 使用Jetty插件 (推荐)

```bash
# 1. 进入项目目录
cd d:\Users\Liang\Documents\GitHub\odata\java\Olingo-OData-5.0.0\samples\tutorials\p0_all

# 2. 清理并编译
mvn clean compile

# 3. 启动Jetty服务器
mvn jetty:run
```

### 方法2: 使用Tomcat插件

```bash
# 启动Tomcat服务器
mvn tomcat7:run
```

### 方法3: 打包并部署到外部容器

```bash
# 1. 打包WAR文件
mvn clean package

# 2. 将target/DemoService.war部署到Tomcat/Jetty等Servlet容器
# WAR文件位置: target/DemoService.war
```

## 🌐 服务访问

启动成功后，可以通过以下URL访问OData服务：

### 基础服务端点
```
http://localhost:8080/DemoService/DemoService.svc
```

### 服务文档
```
http://localhost:8080/DemoService/DemoService.svc/$metadata
```

### 实体集合
```
# 获取所有产品
http://localhost:8080/DemoService/DemoService.svc/Products

# 获取所有分类
http://localhost:8080/DemoService/DemoService.svc/Categories

# 获取所有广告
http://localhost:8080/DemoService/DemoService.svc/Advertisements
```

## 📋 API示例

### 基础查询

```http
# 获取所有产品
GET /DemoService/DemoService.svc/Products

# 获取特定产品
GET /DemoService/DemoService.svc/Products(1)

# 获取产品的分类（导航）
GET /DemoService/DemoService.svc/Products(1)/Category

# 获取分类下的所有产品（导航）
GET /DemoService/DemoService.svc/Categories(1)/Products
```

### 查询选项

```http
# 分页查询 - 前5个产品
GET /DemoService/DemoService.svc/Products?$top=5&$skip=0

# 排序 - 按名称排序
GET /DemoService/DemoService.svc/Products?$orderby=Name asc

# 过滤 - 名称包含"Bread"的产品
GET /DemoService/DemoService.svc/Products?$filter=contains(Name,'Bread')

# 选择字段 - 只返回ID和Name
GET /DemoService/DemoService.svc/Products?$select=ID,Name

# 展开导航属性 - 包含分类信息
GET /DemoService/DemoService.svc/Products?$expand=Category

# 计数
GET /DemoService/DemoService.svc/Products/$count
```

### CRUD操作

#### 创建产品
```http
POST /DemoService/DemoService.svc/Products
Content-Type: application/json

{
  "ID": 100,
  "Name": "New Product",
  "Description": "A new product description"
}
```

#### 更新产品
```http
PUT /DemoService/DemoService.svc/Products(100)
Content-Type: application/json

{
  "ID": 100,
  "Name": "Updated Product",
  "Description": "Updated description"
}
```

#### 删除产品
```http
DELETE /DemoService/DemoService.svc/Products(100)
```

### Actions和Functions

#### 重置数据 (Action)
```http
POST /DemoService/DemoService.svc/Reset
Content-Type: application/json

{
  "Amount": 10
}
```

#### 计数分类 (Function)
```http
GET /DemoService/DemoService.svc/CountCategories(Amount=5)
```

## 🏗️ 架构组件

### 核心类文件

```
src/main/java/myservice/mynamespace/
├── web/
│   └── DemoServlet.java              # HTTP请求入口点
├── service/
│   ├── DemoEdmProvider.java          # 元数据提供者
│   ├── FilterExpressionVisitor.java  # 查询过滤器
│   └── Util.java                     # 工具类
├── data/
│   └── Storage.java                   # 内存数据存储
└── processor/
    ├── DemoEntityCollectionProcessor.java  # 实体集合处理器
    ├── DemoEntityProcessor.java            # 单实体处理器
    ├── DemoPrimitiveProcessor.java         # 基本类型处理器
    ├── DemoActionProcessor.java            # Action处理器
    └── DemoBatchProcessor.java             # 批处理器
```

### 数据模型

- **Product**: 产品实体 (ID, Name, Description)
- **Category**: 分类实体 (ID, Name)
- **Advertisement**: 广告实体 (ID, Name, AirDate) - 支持媒体流

### 关系映射
- Product ↔ Category (多对一关系)
- Products ← Categories (一对多关系)

## 🛠️ 开发说明

### 修改数据模型
编辑 `DemoEdmProvider.java` 来：
- 添加新的实体类型
- 定义属性和关系
- 配置导航属性

### 添加业务逻辑
在相应的Processor类中实现：
- 数据验证
- 业务规则
- 自定义操作

### 扩展存储
修改 `Storage.java` 来：
- 连接数据库
- 实现持久化
- 添加缓存机制

## 🔧 故障排除

### 常见问题

1. **端口冲突**
   ```bash
   # 使用其他端口启动
   mvn jetty:run -Djetty.port=9090
   ```

2. **内存不足**
   ```bash
   # 设置环境变量
   set MAVEN_OPTS=-Xmx1024m -XX:MaxPermSize=256m
   ```

3. **编译错误**
   ```bash
   # 清理并重新编译
   mvn clean compile
   ```

4. **UUID字符串错误**
   - 如果遇到 "UUID string too large" 错误，说明示例数据中的UUID格式不正确
   - 已修复：Storage.java中的无效UUID字符串（多了一个字符的问题）

### 日志查看
服务运行时的日志会显示在控制台，包括：
- HTTP请求信息
- OData操作日志
- 错误堆栈信息

## 📚 相关资源

- [Apache Olingo官方文档](http://olingo.apache.org/)
- [OData V4规范](http://www.odata.org/documentation/)
- [OData教程系列](../README.md)

## 🎯 下一步

完成基础学习后，可以继续学习专题教程：
- **p1_read**: 实体读取操作
- **p2_readep**: 实体属性读取
- **p3_write**: 写入操作
- **p4_navigation**: 导航属性
- **p5-p8**: 查询选项详解
- **p9_action**: Actions详解
- **p10_media**: 媒体实体
- **p11_batch**: 批处理
- **p12_deep_insert**: 深度插入

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个教程项目！
