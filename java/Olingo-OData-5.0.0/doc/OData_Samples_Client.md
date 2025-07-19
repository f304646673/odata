# OData Client 示例项目 (samples/client)

## 概览

`samples/client` 项目是一个完整的 **OData 客户端示例**，演示了如何使用 Apache Olingo 客户端 API 与 OData 服务进行交互。该项目展示了 OData 客户端的核心功能，包括实体的读取、创建、更新、删除以及各种查询选项的使用。

## 学习目标

- 掌握 OData 客户端 API 的使用方法
- 理解客户端与 OData 服务的交互模式
- 学会处理 EDM 元数据和实体操作
- 了解各种 HTTP 客户端定制化技术

## 核心架构

### OData 客户端架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                    OData 客户端架构                              │
├─────────────────────────────────────────────────────────────────┤
│                   Application Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  Business       │  │   Data Access   │  │   Service       │ │
│  │  Logic          │  │   Objects       │  │   Integration   │ │
│  │                 │  │                 │  │                 │ │
│  │ OlingoSampleApp │  │ ClientEntity    │  │ REST Services   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    OData Client API                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   ODataClient   │  │   Request       │  │   Response      │ │
│  │                 │  │   Builders      │  │   Handlers      │ │
│  │ ODataClientFac  │  │                 │  │                 │ │
│  │ tory.getClient()│  │ EntityRequest   │  │ ClientEntity    │ │
│  │                 │  │ MetadataRequest │  │ ClientEntitySet │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   HTTP Communication                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  HTTP Client    │  │   Authentication│  │   Serialization │ │
│  │  Factory        │  │   & Security    │  │   & Format      │ │
│  │                 │  │                 │  │                 │ │
│  │ Custom Factories│  │ OAuth2, Cookie  │  │ JSON, XML, Atom │ │
│  │ Connection Pool │  │ Custom Headers  │  │ Content Types   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   OData Service                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  Remote OData Service                       │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │   Metadata  │ │   Entities  │ │   Actions   │           │ │
│  │  │   $metadata │ │   Cars      │ │   Functions │           │ │
│  │  │             │ │   Manufact. │ │             │           │ │
│  │  │ EDM Schema  │ │ CRUD & Query│ │ Operations  │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. OlingoSampleApp - 主要示例应用

```java
public class OlingoSampleApp {
  private ODataClient client;
  
  public OlingoSampleApp() {
    client = ODataClientFactory.getClient();
  }

  public static void main(String[] params) throws Exception {
    OlingoSampleApp app = new OlingoSampleApp();
    app.perform("http://localhost:8080/cars.svc");
  }

  void perform(String serviceUrl) throws Exception {
    // 1. 读取 EDM 元数据
    print("\\n----- Read Edm ------------------------------");
    Edm edm = readEdm(serviceUrl);
    
    // 2. 检查实体类型和复杂类型
    List<FullQualifiedName> ctFqns = new ArrayList<>();
    List<FullQualifiedName> etFqns = new ArrayList<>();
    for (EdmSchema schema : edm.getSchemas()) {
      for (EdmComplexType complexType : schema.getComplexTypes()) {
        ctFqns.add(complexType.getFullQualifiedName());
      }
      for (EdmEntityType entityType : schema.getEntityTypes()) {
        etFqns.add(entityType.getFullQualifiedName());
      }
    }
    
    // 3. 分析实体属性
    EdmEntityType etype = edm.getEntityType(etFqns.get(0));
    for (String propertyName : etype.getPropertyNames()) {
      EdmProperty property = etype.getStructuralProperty(propertyName);
      FullQualifiedName typeName = property.getType().getFullQualifiedName();
      print("property '" + propertyName + "' " + typeName);
    }
    
    // 4. 读取实体集合
    print("\\n----- Read Entities ------------------------------");
    ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = 
      readEntities(edm, serviceUrl, "Manufacturers");

    while (iterator.hasNext()) {
      ClientEntity ce = iterator.next();
      print("Entry:\\n" + prettyPrint(ce.getProperties(), 0));
    }

    // 5. 读取单个实体
    print("\\n----- Read Entry ------------------------------");
    ClientEntity entry = readEntityWithKey(edm, serviceUrl, "Manufacturers", 1);
    print("Single Entry:\\n" + prettyPrint(entry.getProperties(), 0));

    // 6. 使用 $expand 读取关联数据
    print("\\n----- Read Entity with $expand ------------------------------");
    entry = readEntityWithKeyExpand(edm, serviceUrl, "Manufacturers", 1, "Cars");
    print("Single Entry with expanded Cars relation:\\n" + prettyPrint(entry.getProperties(), 0));

    // 7. 使用 $filter 过滤查询
    print("\\n----- Read Entities with $filter ------------------------------");
    iterator = readEntitiesWithFilter(edm, serviceUrl, "Manufacturers", "Name eq 'Horse Powered Racing'");
    while (iterator.hasNext()) {
      ClientEntity ce = iterator.next();
      print("Entry:\\n" + prettyPrint(ce.getProperties(), 0));
    }
  }
}
```

### 2. EDM 元数据读取

```java
private Edm readEdm(String serviceUrl) throws IOException, ODataDeserializerException {
  EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl);
  ODataRetrieveResponse<Edm> response = request.execute();
  return response.getBody();
}
```

**功能说明**：
- **元数据获取**：从 OData 服务获取完整的 EDM 模型
- **类型发现**：自动发现所有实体类型和复杂类型
- **属性检查**：分析实体的属性结构和类型信息

### 3. 实体集合读取

```java
private ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntities(
    Edm edm, String serviceUrl, String entitySetName) throws IOException, ODataDeserializerException {
  
  URI absoluteUri = client.newURIBuilder(serviceUrl).appendEntitySetSegment(entitySetName).build();
  ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = 
    client.getRetrieveRequestFactory().getEntitySetIteratorRequest(absoluteUri);
  
  request.setFormat(ContentType.JSON);
  ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();
  return response.getBody();
}
```

**功能说明**：
- **URI 构建**：使用客户端 URI 构建器创建请求 URL
- **迭代器模式**：使用迭代器处理大型数据集
- **格式指定**：支持 JSON 和 XML 等多种响应格式

### 4. 单实体读取

```java
private ClientEntity readEntityWithKey(Edm edm, String serviceUrl, String entitySetName, Object keyValue) 
    throws IOException, ODataDeserializerException {
  
  URI absoluteUri = client.newURIBuilder(serviceUrl)
    .appendEntitySetSegment(entitySetName)
    .appendKeySegment(keyValue)
    .build();
    
  ODataEntityRequest<ClientEntity> request = client.getRetrieveRequestFactory().getEntityRequest(absoluteUri);
  request.setFormat(ContentType.JSON);
  
  ODataRetrieveResponse<ClientEntity> response = request.execute();
  return response.getBody();
}
```

**功能说明**：
- **键值访问**：通过主键值直接访问特定实体
- **URL 构建**：自动处理键值的 URL 编码
- **类型安全**：返回强类型的客户端实体对象

### 5. 扩展查询 ($expand)

```java
private ClientEntity readEntityWithKeyExpand(Edm edm, String serviceUrl, String entitySetName, 
    Object keyValue, String expandRelationName) throws IOException, ODataDeserializerException {
  
  URI absoluteUri = client.newURIBuilder(serviceUrl)
    .appendEntitySetSegment(entitySetName)
    .appendKeySegment(keyValue)
    .expand(expandRelationName)
    .build();
    
  ODataEntityRequest<ClientEntity> request = client.getRetrieveRequestFactory().getEntityRequest(absoluteUri);
  request.setFormat(ContentType.JSON);
  
  ODataRetrieveResponse<ClientEntity> response = request.execute();
  return response.getBody();
}
```

**功能说明**：
- **关联加载**：一次请求获取主实体及其关联实体
- **性能优化**：减少往返请求次数
- **灵活扩展**：支持多层级的扩展查询

### 6. 过滤查询 ($filter)

```java
private ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntitiesWithFilter(
    Edm edm, String serviceUrl, String entitySetName, String filterExpression) 
    throws IOException, ODataDeserializerException {
  
  URI absoluteUri = client.newURIBuilder(serviceUrl)
    .appendEntitySetSegment(entitySetName)
    .filter(filterExpression)
    .build();
    
  ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = 
    client.getRetrieveRequestFactory().getEntitySetIteratorRequest(absoluteUri);
  request.setFormat(ContentType.JSON);
  
  ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();
  return response.getBody();
}
```

**功能说明**：
- **服务端过滤**：在服务端执行过滤逻辑，减少网络传输
- **OData 表达式**：支持完整的 OData 过滤表达式语法
- **类型安全**：自动处理不同数据类型的比较

## 高级 HTTP 客户端定制

### 1. 连接池定制

```java
public class CustomConnectionsHttpClientFactory implements HttpClientFactory {
  
  @Override
  public CloseableHttpClient create(HttpMethod method, URI uri) {
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    
    // 配置连接池
    cm.setMaxTotal(100);                    // 最大连接数
    cm.setDefaultMaxPerRoute(20);           // 每个路由的最大连接数
    cm.setValidateAfterInactivity(30000);   // 连接验证间隔
    
    // 配置超时
    RequestConfig requestConfig = RequestConfig.custom()
      .setConnectTimeout(5000)              // 连接超时
      .setSocketTimeout(30000)              // 读取超时
      .setConnectionRequestTimeout(3000)    // 从连接池获取连接超时
      .build();
    
    return HttpClients.custom()
      .setConnectionManager(cm)
      .setDefaultRequestConfig(requestConfig)
      .build();
  }
}
```

### 2. OAuth2 认证

```java
public class AzureADOAuth2HttpClientFactory implements HttpClientFactory {
  
  private String accessToken;
  
  public AzureADOAuth2HttpClientFactory(String accessToken) {
    this.accessToken = accessToken;
  }
  
  @Override
  public CloseableHttpClient create(HttpMethod method, URI uri) {
    return HttpClients.custom()
      .addInterceptorFirst(new HttpRequestInterceptor() {
        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
          request.addHeader("Authorization", "Bearer " + accessToken);
        }
      })
      .build();
  }
}
```

### 3. Cookie 管理

```java
public class CookieHttpClientFactory implements HttpClientFactory {
  
  private CookieStore cookieStore = new BasicCookieStore();
  
  @Override
  public CloseableHttpClient create(HttpMethod method, URI uri) {
    return HttpClients.custom()
      .setDefaultCookieStore(cookieStore)
      .build();
  }
}
```

### 4. 请求重试机制

```java
public class RequestRetryHttpClientFactory implements HttpClientFactory {
  
  @Override
  public CloseableHttpClient create(HttpMethod method, URI uri) {
    HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
      @Override
      public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount >= 3) return false;                    // 最多重试3次
        if (exception instanceof InterruptedIOException) return false;  // 超时不重试
        if (exception instanceof UnknownHostException) return false;    // 未知主机不重试
        
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        
        // 只重试幂等请求
        return !(request instanceof HttpEntityEnclosingRequest);
      }
    };
    
    return HttpClients.custom()
      .setRetryHandler(retryHandler)
      .build();
  }
}
```

## 实体操作示例

### 1. 创建实体

```java
private ClientEntity createEntity(Edm edm, String serviceUrl, String entitySetName, ClientEntity entity) 
    throws IOException, ODataDeserializerException {
  
  URI absoluteUri = client.newURIBuilder(serviceUrl).appendEntitySetSegment(entitySetName).build();
  
  ODataEntityCreateRequest<ClientEntity> request = 
    client.getCUDRequestFactory().getEntityCreateRequest(absoluteUri, entity);
  request.setFormat(ContentType.JSON);
  
  ODataEntityCreateResponse<ClientEntity> response = request.execute();
  return response.getBody();
}
```

### 2. 更新实体

```java
private int updateEntity(Edm edm, String serviceUrl, String entitySetName, Object keyValue, ClientEntity entity) 
    throws IOException, ODataDeserializerException {
  
  URI absoluteUri = client.newURIBuilder(serviceUrl)
    .appendEntitySetSegment(entitySetName)
    .appendKeySegment(keyValue)
    .build();
    
  ODataEntityUpdateRequest<ClientEntity> request = 
    client.getCUDRequestFactory().getEntityUpdateRequest(absoluteUri, UpdateType.PATCH, entity);
  request.setFormat(ContentType.JSON);
  
  ODataEntityUpdateResponse<ClientEntity> response = request.execute();
  return response.getStatusCode();
}
```

### 3. 删除实体

```java
private int deleteEntity(String serviceUrl, String entitySetName, Object keyValue) throws IOException {
  
  URI absoluteUri = client.newURIBuilder(serviceUrl)
    .appendEntitySetSegment(entitySetName)
    .appendKeySegment(keyValue)
    .build();
    
  ODataDeleteRequest request = client.getCUDRequestFactory().getDeleteRequest(absoluteUri);
  
  ODataDeleteResponse response = request.execute();
  return response.getStatusCode();
}
```

## 使用方式

### 1. 基本使用

```bash
# 编译项目
mvn clean compile

# 运行示例（需要先启动 OData 服务）
mvn exec:java -Dexec.mainClass="org.apache.olingo.samples.client.OlingoSampleApp"
```

### 2. 自定义 HTTP 客户端

```java
public class CustomClientExample {
  public static void main(String[] args) {
    // 创建带连接池的客户端
    ODataClient client = ODataClientFactory.getClient();
    client.getConfiguration().setHttpClientFactory(new CustomConnectionsHttpClientFactory());
    
    // 或者使用 OAuth2 认证
    String accessToken = "your-access-token";
    client.getConfiguration().setHttpClientFactory(new AzureADOAuth2HttpClientFactory(accessToken));
    
    // 使用客户端执行操作
    // ...
  }
}
```

### 3. 处理不同响应格式

```java
// JSON 格式
request.setFormat(ContentType.JSON);

// XML 格式
request.setFormat(ContentType.APPLICATION_XML);

// Atom 格式
request.setFormat(ContentType.APPLICATION_ATOM_XML);
```

## 错误处理

### 1. 网络错误处理

```java
try {
  ODataRetrieveResponse<ClientEntity> response = request.execute();
  ClientEntity entity = response.getBody();
  
} catch (ODataClientErrorException e) {
  // 4xx 客户端错误
  System.err.println("Client error: " + e.getStatusLine().getStatusCode());
  
} catch (ODataServerErrorException e) {
  // 5xx 服务器错误
  System.err.println("Server error: " + e.getStatusLine().getStatusCode());
  
} catch (IOException e) {
  // 网络连接错误
  System.err.println("Network error: " + e.getMessage());
}
```

### 2. 数据解析错误处理

```java
try {
  ClientEntity entity = response.getBody();
  
  // 安全地访问属性
  ClientProperty nameProperty = entity.getProperty("Name");
  if (nameProperty != null && nameProperty.getValue() != null) {
    String name = nameProperty.getValue().toString();
  }
  
} catch (ODataDeserializerException e) {
  System.err.println("Failed to parse response: " + e.getMessage());
}
```

## 性能优化建议

### 1. 连接复用

```java
// 使用单例客户端实例
public class ODataClientManager {
  private static final ODataClient INSTANCE = ODataClientFactory.getClient();
  
  static {
    // 配置连接池
    INSTANCE.getConfiguration().setHttpClientFactory(new CustomConnectionsHttpClientFactory());
  }
  
  public static ODataClient getClient() {
    return INSTANCE;
  }
}
```

### 2. 批量操作

```java
// 使用批量请求减少网络往返
ODataBatchRequest batchRequest = client.getBatchRequestFactory().getBatchRequest(serviceUrl);

// 添加多个操作到批量请求
batchRequest.addRequest(createRequest1);
batchRequest.addRequest(updateRequest2);
batchRequest.addRequest(deleteRequest3);

// 执行批量操作
ODataBatchResponse batchResponse = batchRequest.execute();
```

### 3. 分页处理

```java
// 使用迭代器自动处理分页
ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = 
  client.getRetrieveRequestFactory()
        .getEntitySetIteratorRequest(uri)
        .execute()
        .getBody();

// 迭代器会自动处理 @odata.nextLink
while (iterator.hasNext()) {
  ClientEntity entity = iterator.next();
  // 处理实体
}
```

## 总结

`samples/client` 项目提供了完整的 OData 客户端使用示例：

### 核心特性
- ✅ **完整的 CRUD 操作**：创建、读取、更新、删除实体
- ✅ **高级查询支持**：$filter、$expand、$orderby 等
- ✅ **元数据处理**：自动发现和分析服务元数据
- ✅ **HTTP 客户端定制**：连接池、认证、重试等
- ✅ **多格式支持**：JSON、XML、Atom 等响应格式

### 技术亮点
- **类型安全**：强类型的客户端 API
- **流式处理**：迭代器模式处理大数据集
- **扩展性强**：支持各种 HTTP 客户端定制
- **错误处理**：完善的异常处理机制

### 实际应用
该客户端示例为开发者提供了集成 OData 服务的最佳实践，特别适合：
- **企业应用集成**：与现有 OData 服务集成
- **数据访问层**：构建统一的数据访问接口
- **微服务通信**：服务间的标准化数据交换
- **移动应用**：移动端与后端服务的数据同步
