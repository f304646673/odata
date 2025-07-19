# OData Tutorial 11 - 批处理(Batch) (p11_batch)

## 概览

`p11_batch` 项目是Apache Olingo OData V4教程的第十一课，专门讲解**OData批处理(Batch)**功能的实现。批处理允许客户端在单个HTTP请求中发送多个操作，从而提高网络效率，减少往返次数，特别适用于移动应用和高延迟网络环境。

## 学习目标

- 理解OData批处理的概念和优势
- 掌握BatchProcessor的实现方法
- 学会处理批处理请求和响应格式
- 了解事务性和非事务性批处理的差异

## 批处理概念

### 批处理优势
- **网络效率**：减少HTTP往返次数
- **性能提升**：批量处理减少延迟影响
- **原子性**：支持事务性操作组
- **错误处理**：统一的错误处理机制

### 批处理类型
| 类型 | 描述 | 特性 |
|------|------|------|
| **批处理请求** | 包含多个子请求的容器 | 非事务性，独立执行 |
| **变更集(ChangeSet)** | 事务性操作组 | 全部成功或全部失败 |

## 核心架构

### 批处理架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                   批处理增强OData服务架构                          │
├─────────────────────────────────────────────────────────────────┤
│                       Client Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   HTTP POST     │  │  Multipart      │  │   Batch         │ │
│  │   /$batch       │  │  Content        │  │  Boundary       │ │
│  │                 │  │                 │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Batch Processing Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ BatchProcessor  │  │  Request        │  │   Response      │ │
│  │                 │  │  Parsing        │  │  Generation     │ │
│  │ processBatch()  │  │                 │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Sub-Request Processing                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Individual    │  │   ChangeSet     │  │   Error         │ │
│  │   Requests      │  │   Transaction   │  │   Handling      │ │
│  │                 │  │                 │  │                 │ │
│  │ GET, POST, PUT  │  │ Atomic Batch    │  │ Sub-Response    │ │
│  │ PATCH, DELETE   │  │                 │  │   Status        │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                Regular Processor Delegation                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │EntityCollection │  │ EntityProcessor │  │ActionProcessor  │ │
│  │   Processor     │  │                 │  │                 │ │
│  │                 │  │                 │  │                 │ │
│  │  (Reuse)        │  │   (Reuse)       │  │    (Reuse)      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Layer                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                      Storage                                │ │
│  │  ┌─────────────┐ ┌─────────────┐                           │ │
│  │  │ Transaction │ │    Data     │                           │ │
│  │  │  Support    │ │  Storage    │                           │ │
│  │  │(ChangeSet)  │ │ (existing)  │                           │ │
│  │  └─────────────┘ └─────────────┘                           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## DemoBatchProcessor 实现

### 1. 处理器注册
```java
public class DemoServlet extends HttpServlet {
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            // ... 现有的初始化代码
            
            // 创建处理器并注册
            ODataHttpHandler handler = odata.createHandler(edm);
            handler.register(new DemoEntityCollectionProcessor(storage));
            handler.register(new DemoEntityProcessor(storage));
            handler.register(new DemoPrimitiveProcessor(storage));
            handler.register(new DemoActionProcessor(storage));
            handler.register(new DemoBatchProcessor(storage)); // 注册批处理器
            
            // 处理请求
            handler.process(req, resp);
        } catch (RuntimeException e) {
            LOG.error("Server Error occurred in ExampleServlet", e);
            throw new ServletException(e);
        }
    }
}
```

### 2. 批处理器核心实现
```java
public class DemoBatchProcessor implements BatchProcessor {
    
    private Storage storage;
    
    public DemoBatchProcessor(Storage storage) {
        this.storage = storage;
    }
    
    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        // 初始化（如果需要）
    }
    
    @Override
    public void processBatch(BatchFacade facade, BatchRequestPart request, BatchResponsePart response)
            throws ODataApplicationException, ODataLibraryException {
        
        // 获取批处理请求中的所有子请求
        List<ODataRequest> requests = request.getRequests();
        
        if (request.isChangeSet()) {
            // 处理变更集（事务性）
            processChangeSet(facade, requests, response);
        } else {
            // 处理独立请求（非事务性）
            processIndividualRequests(facade, requests, response);
        }
    }
    
    private void processChangeSet(BatchFacade facade, List<ODataRequest> requests, BatchResponsePart response)
            throws ODataApplicationException, ODataLibraryException {
        
        List<ODataResponse> responses = new ArrayList<>();
        
        try {
            // 开始事务（在真实实现中）
            beginTransaction();
            
            // 处理变更集中的每个请求
            for (ODataRequest request : requests) {
                ODataResponse subResponse = facade.handleODataRequest(request);
                
                // 检查是否有错误
                if (subResponse.getStatusCode() >= 400) {
                    // 如果有错误，回滚事务并返回错误
                    rollbackTransaction();
                    
                    // 创建错误响应
                    ODataResponse errorResponse = new ODataResponse();
                    errorResponse.setStatusCode(subResponse.getStatusCode());
                    errorResponse.setHeader(HttpHeader.CONTENT_TYPE, subResponse.getHeader(HttpHeader.CONTENT_TYPE));
                    errorResponse.setContent(subResponse.getContent());
                    
                    responses.clear();
                    responses.add(errorResponse);
                    break;
                }
                
                responses.add(subResponse);
            }
            
            // 如果所有请求都成功，提交事务
            if (responses.size() == requests.size()) {
                commitTransaction();
            }
            
        } catch (Exception e) {
            // 发生异常时回滚事务
            rollbackTransaction();
            
            // 创建内部错误响应
            ODataResponse errorResponse = new ODataResponse();
            errorResponse.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
            responses.clear();
            responses.add(errorResponse);
        }
        
        // 设置响应
        response.getResponses().addAll(responses);
    }
    
    private void processIndividualRequests(BatchFacade facade, List<ODataRequest> requests, BatchResponsePart response)
            throws ODataApplicationException, ODataLibraryException {
        
        List<ODataResponse> responses = new ArrayList<>();
        
        // 独立处理每个请求
        for (ODataRequest request : requests) {
            try {
                ODataResponse subResponse = facade.handleODataRequest(request);
                responses.add(subResponse);
            } catch (Exception e) {
                // 独立请求的错误不影响其他请求
                ODataResponse errorResponse = new ODataResponse();
                errorResponse.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
                responses.add(errorResponse);
            }
        }
        
        // 设置响应
        response.getResponses().addAll(responses);
    }
    
    // 简化的事务管理方法（实际实现会更复杂）
    private void beginTransaction() {
        // 在真实实现中，这里会开始数据库事务
        storage.beginTransaction();
    }
    
    private void commitTransaction() {
        // 提交事务
        storage.commitTransaction();
    }
    
    private void rollbackTransaction() {
        // 回滚事务
        storage.rollbackTransaction();
    }
}
```

## Storage 事务支持

### 1. 事务状态管理
```java
public class Storage {
    
    // 现有的数据存储
    private List<Entity> productList;
    private List<Entity> categoryList;
    
    // 事务支持
    private List<Entity> productBackup;
    private List<Entity> categoryBackup;
    private boolean inTransaction = false;
    
    public void beginTransaction() {
        if (inTransaction) {
            throw new IllegalStateException("Transaction already in progress");
        }
        
        // 备份当前数据状态
        productBackup = new ArrayList<>();
        for (Entity product : productList) {
            productBackup.add(cloneEntity(product));
        }
        
        categoryBackup = new ArrayList<>();
        for (Entity category : categoryList) {
            categoryBackup.add(cloneEntity(category));
        }
        
        inTransaction = true;
    }
    
    public void commitTransaction() {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        
        // 清除备份数据，提交更改
        productBackup = null;
        categoryBackup = null;
        inTransaction = false;
    }
    
    public void rollbackTransaction() {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        
        // 恢复到事务开始前的状态
        productList.clear();
        productList.addAll(productBackup);
        
        categoryList.clear();
        categoryList.addAll(categoryBackup);
        
        // 清理事务状态
        productBackup = null;
        categoryBackup = null;
        inTransaction = false;
    }
    
    private Entity cloneEntity(Entity original) {
        Entity clone = new Entity();
        
        // 复制所有属性
        for (Property property : original.getProperties()) {
            clone.addProperty(new Property(
                property.getType(),
                property.getName(),
                property.getValueType(),
                property.getValue()
            ));
        }
        
        // 复制ID
        clone.setId(original.getId());
        
        return clone;
    }
    
    // 现有的CRUD方法保持不变，但在事务模式下操作
    public Entity createEntityData(EdmEntitySet edmEntitySet, Entity entityToCreate) 
            throws ODataApplicationException {
        
        // 正常的创建逻辑
        Entity createdEntity = performEntityCreation(edmEntitySet, entityToCreate);
        
        // 如果在事务中，变更会在commit时确认，或在rollback时撤销
        return createdEntity;
    }
    
    // ... 其他CRUD方法类似处理
}
```

## 批处理请求格式

### 1. 批处理请求示例
```http
POST /DemoService.svc/$batch
Content-Type: multipart/mixed; boundary=batch_boundary

--batch_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary

GET Products HTTP/1.1
Accept: application/json

--batch_boundary
Content-Type: multipart/mixed; boundary=changeset_boundary

--changeset_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 1

POST Products HTTP/1.1
Content-Type: application/json

{
    "Name": "New Product 1",
    "Description": "Description 1"
}

--changeset_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 2

POST Products HTTP/1.1
Content-Type: application/json

{
    "Name": "New Product 2", 
    "Description": "Description 2"
}

--changeset_boundary--

--batch_boundary--
```

### 2. 批处理响应示例
```http
HTTP/1.1 200 OK
Content-Type: multipart/mixed; boundary=response_boundary

--response_boundary
Content-Type: application/http

HTTP/1.1 200 OK
Content-Type: application/json

{
    "@odata.context": "$metadata#Products",
    "value": [
        {
            "ID": 1,
            "Name": "Existing Product",
            "Description": "Existing Description"
        }
    ]
}

--response_boundary
Content-Type: multipart/mixed; boundary=changeset_response_boundary

--changeset_response_boundary
Content-Type: application/http

HTTP/1.1 201 Created
Content-Type: application/json

{
    "ID": 2,
    "Name": "New Product 1",
    "Description": "Description 1"
}

--changeset_response_boundary
Content-Type: application/http

HTTP/1.1 201 Created
Content-Type: application/json

{
    "ID": 3,
    "Name": "New Product 2",
    "Description": "Description 2"
}

--changeset_response_boundary--

--response_boundary--
```

## 错误处理

### 1. 变更集错误处理
```java
private void processChangeSet(BatchFacade facade, List<ODataRequest> requests, BatchResponsePart response)
        throws ODataApplicationException, ODataLibraryException {
    
    List<ODataResponse> responses = new ArrayList<>();
    
    try {
        beginTransaction();
        
        for (int i = 0; i < requests.size(); i++) {
            ODataRequest request = requests.get(i);
            ODataResponse subResponse = facade.handleODataRequest(request);
            
            // 检查错误状态码
            if (subResponse.getStatusCode() >= 400) {
                rollbackTransaction();
                
                // 创建错误响应，包含错误详情
                ODataResponse errorResponse = createErrorResponse(
                    subResponse.getStatusCode(),
                    "Error in changeset at request " + (i + 1),
                    subResponse.getContent()
                );
                
                responses.clear();
                responses.add(errorResponse);
                break;
            }
            
            responses.add(subResponse);
        }
        
        // 所有请求成功时提交
        if (responses.size() == requests.size()) {
            commitTransaction();
        }
        
    } catch (Exception e) {
        rollbackTransaction();
        
        ODataResponse errorResponse = createErrorResponse(
            HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal error during changeset processing",
            null
        );
        
        responses.clear();
        responses.add(errorResponse);
    }
    
    response.getResponses().addAll(responses);
}

private ODataResponse createErrorResponse(int statusCode, String message, InputStream originalContent) {
    ODataResponse errorResponse = new ODataResponse();
    errorResponse.setStatusCode(statusCode);
    errorResponse.setHeader(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON.toContentTypeString());
    
    // 创建错误响应体
    String errorJson = String.format(
        "{\"error\":{\"code\":null,\"message\":\"%s\"}}", 
        message
    );
    
    try {
        errorResponse.setContent(new ByteArrayInputStream(errorJson.getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
        // UTF-8 should always be supported
    }
    
    return errorResponse;
}
```

### 2. 变更集失败响应
```http
--response_boundary
Content-Type: multipart/mixed; boundary=changeset_response_boundary

--changeset_response_boundary
Content-Type: application/http

HTTP/1.1 400 Bad Request
Content-Type: application/json

{
    "error": {
        "code": null,
        "message": "Error in changeset at request 2"
    }
}

--changeset_response_boundary--

--response_boundary--
```

## 批处理优化

### 1. 请求验证
```java
@Override
public void processBatch(BatchFacade facade, BatchRequestPart request, BatchResponsePart response)
        throws ODataApplicationException, ODataLibraryException {
    
    List<ODataRequest> requests = request.getRequests();
    
    // 验证批处理请求
    validateBatchRequest(requests, request.isChangeSet());
    
    if (request.isChangeSet()) {
        processChangeSet(facade, requests, response);
    } else {
        processIndividualRequests(facade, requests, response);
    }
}

private void validateBatchRequest(List<ODataRequest> requests, boolean isChangeSet) 
        throws ODataApplicationException {
    
    if (requests.isEmpty()) {
        throw new ODataApplicationException("Empty batch request", 
                                          HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                          Locale.ROOT);
    }
    
    if (isChangeSet) {
        // 验证变更集中只包含写操作
        for (ODataRequest request : requests) {
            HttpMethod method = request.getMethod();
            if (method == HttpMethod.GET) {
                throw new ODataApplicationException("GET requests not allowed in changeset", 
                                                  HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                                  Locale.ROOT);
            }
        }
    }
    
    // 检查批处理大小限制
    if (requests.size() > 100) { // 假设限制为100个请求
        throw new ODataApplicationException("Batch size exceeds limit", 
                                          HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                          Locale.ROOT);
    }
}
```

### 2. 性能监控
```java
private void processChangeSet(BatchFacade facade, List<ODataRequest> requests, BatchResponsePart response)
        throws ODataApplicationException, ODataLibraryException {
    
    long startTime = System.currentTimeMillis();
    List<ODataResponse> responses = new ArrayList<>();
    
    try {
        beginTransaction();
        
        for (ODataRequest request : requests) {
            long requestStart = System.currentTimeMillis();
            ODataResponse subResponse = facade.handleODataRequest(request);
            long requestEnd = System.currentTimeMillis();
            
            // 记录单个请求处理时间
            LOG.debug("Sub-request processed in {} ms", requestEnd - requestStart);
            
            if (subResponse.getStatusCode() >= 400) {
                rollbackTransaction();
                responses.clear();
                responses.add(subResponse);
                break;
            }
            
            responses.add(subResponse);
        }
        
        if (responses.size() == requests.size()) {
            commitTransaction();
        }
        
    } catch (Exception e) {
        rollbackTransaction();
        // 处理错误...
    } finally {
        long endTime = System.currentTimeMillis();
        LOG.debug("Changeset processed in {} ms", endTime - startTime);
    }
    
    response.getResponses().addAll(responses);
}
```

## 测试用例

### 1. 简单批处理测试
```bash
# 创建批处理请求文件
cat > batch_request.txt << 'EOF'
--batch_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary

GET Products HTTP/1.1
Accept: application/json

--batch_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary

GET Categories HTTP/1.1
Accept: application/json

--batch_boundary--
EOF

# 发送批处理请求
curl -X POST http://localhost:8080/DemoService.svc/\$batch \
     -H "Content-Type: multipart/mixed; boundary=batch_boundary" \
     --data-binary @batch_request.txt
```

### 2. 变更集测试
```bash
# 创建包含变更集的批处理请求
cat > changeset_request.txt << 'EOF'
--batch_boundary
Content-Type: multipart/mixed; boundary=changeset_boundary

--changeset_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 1

POST Products HTTP/1.1
Content-Type: application/json

{
    "Name": "Batch Product 1",
    "Description": "Created via batch"
}

--changeset_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 2

POST Products HTTP/1.1
Content-Type: application/json

{
    "Name": "Batch Product 2",
    "Description": "Created via batch"
}

--changeset_boundary--

--batch_boundary--
EOF

curl -X POST http://localhost:8080/DemoService.svc/\$batch \
     -H "Content-Type: multipart/mixed; boundary=batch_boundary" \
     --data-binary @changeset_request.txt
```

### 3. 错误处理测试
```bash
# 创建包含错误的变更集
cat > error_changeset.txt << 'EOF'
--batch_boundary
Content-Type: multipart/mixed; boundary=changeset_boundary

--changeset_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 1

POST Products HTTP/1.1
Content-Type: application/json

{
    "Name": "Valid Product"
}

--changeset_boundary
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 2

POST Products HTTP/1.1
Content-Type: application/json

{
    "InvalidField": "This should cause an error"
}

--changeset_boundary--

--batch_boundary--
EOF

# 预期：整个变更集失败，没有产品被创建
curl -X POST http://localhost:8080/DemoService.svc/\$batch \
     -H "Content-Type: multipart/mixed; boundary=batch_boundary" \
     --data-binary @error_changeset.txt
```

## 总结

`p11_batch`教程实现了OData批处理的核心功能：

### 新增能力
- ✅ **批处理支持**：单个HTTP请求处理多个操作
- ✅ **变更集事务**：原子性的写操作组
- ✅ **错误处理**：完善的错误传播和回滚机制
- ✅ **性能优化**：减少网络往返次数
- ✅ **混合操作**：读写操作的灵活组合

### 技术亮点
- **事务管理**：实现了简单但有效的事务支持
- **请求委托**：复用现有处理器处理子请求
- **错误隔离**：独立请求的错误不影响其他请求
- **协议遵循**：严格遵循OData V4批处理规范

### 架构价值
- **网络效率**：显著减少客户端-服务器通信次数
- **原子操作**：提供了事务性操作的支持
- **灵活性**：支持读写操作的任意组合
- **可靠性**：完善的错误处理和回滚机制

批处理功能使OData服务在高延迟网络环境和移动应用中具有更好的性能表现，是企业级应用的重要特性。
