# OData Tutorial 03 - 写入操作 (p3_write)

## 概览

`p3_write` 项目是Apache Olingo OData V4教程的第三课，在前两课读取功能的基础上添加了完整的**写入操作**支持，包括创建、更新和删除实体。这是构建完整CRUD (Create, Read, Update, Delete) OData服务的关键一步。

## 学习目标

- 掌握HTTP POST/PUT/PATCH/DELETE方法处理
- 学会实体的反序列化技术
- 理解OData写入操作的规范和约定
- 掌握数据验证和错误处理机制

## 功能对比

| 功能 | p1_read | p2_readep | p3_write |
|------|---------|-----------|----------|
| 读取实体集合 | ✅ | ✅ | ✅ |
| 读取单个实体 | ❌ | ✅ | ✅ |
| 读取实体属性 | ❌ | ✅ | ✅ |
| 创建实体 (POST) | ❌ | ❌ | ✅ |
| 更新实体 (PUT/PATCH) | ❌ | ❌ | ✅ |
| 删除实体 (DELETE) | ❌ | ❌ | ✅ |
| 更新属性 (PUT/PATCH) | ❌ | ❌ | ✅ |

## 核心架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                    完整CRUD OData服务架构                         │
├─────────────────────────────────────────────────────────────────┤
│                       Client Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   HTTP GET      │  │  HTTP POST      │  │ HTTP PUT/PATCH  │ │
│  │ 读取操作         │  │  创建实体       │  │ 更新操作        │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│  ┌─────────────────┐                                           │
│  │ HTTP DELETE     │                                           │
│  │ 删除操作        │                                           │
│  └─────────────────┘                                           │
├─────────────────────────────────────────────────────────────────┤
│                    OData Handler Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  DemoServlet    │  │ODataHttpHandler │  │ ServiceMetadata │ │
│  │                 │  │                 │  │                 │ │
│  │  (Entry Point)  │  │ (URL Routing)   │  │ (Schema Info)   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Processor Layer                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │EntityCollection │  │ EntityProcessor │  │PrimitiveProcessor│ │
│  │   Processor     │  │    (扩展)       │  │    (扩展)       │ │
│  │                 │  │                 │  │                 │ │
│  │  readCollection │  │ create/update/  │  │ update/delete   │ │
│  │                 │  │ delete Entity   │  │ Property        │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   EDM Provider Layer                            │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  DemoEdmProvider                            │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │ EntityType  │ │ EntitySet   │ │ Container   │           │ │
│  │  │  Product    │ │  Products   │ │             │           │ │
│  │  │             │ │             │ │             │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Layer                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                      Storage                                │ │
│  │  ┌─────────────┐                                            │ │
│  │  │  Products   │  ← 支持CRUD操作                             │ │
│  │  │    List     │  ← ID生成机制                              │ │
│  │  │(In-Memory)  │  ← 数据验证                                │ │
│  │  └─────────────┘                                            │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件扩展

### 1. EntityProcessor 写入方法扩展

在`p2_readep`的基础上，`DemoEntityProcessor`新增了三个关键方法：

#### createEntity - 创建实体
```java
@Override
public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                        ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, DeserializerException, SerializerException {
    
    // 1. 获取目标实体集
    EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
    
    // 2. 反序列化请求体
    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
    Entity requestEntity = result.getEntity();
    
    // 3. 在存储中创建实体
    Entity createdEntity = storage.createEntityData(edmEntitySet, requestEntity);
    
    // 4. 序列化响应
    ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
    
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, 
                                                          createdEntity, options);
    
    // 5. 设置响应
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
}
```

#### updateEntity - 更新实体
```java
@Override
public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                        ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, DeserializerException, SerializerException {
    
    // 1. 获取目标实体
    EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
    
    // 2. 提取键值
    List<UriParameter> keyPredicates = Util.getKeyPredicates(uriInfo);
    
    // 3. 反序列化更新数据
    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
    Entity requestEntity = result.getEntity();
    
    // 4. 执行更新
    HttpMethod httpMethod = request.getMethod();
    storage.updateEntityData(edmEntitySet, keyPredicates, requestEntity, httpMethod);
    
    // 5. 返回204 No Content（更新成功）
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
}
```

#### deleteEntity - 删除实体
```java
@Override
public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
        throws ODataApplicationException {
    
    // 1. 获取目标实体集
    EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
    
    // 2. 提取键值
    List<UriParameter> keyPredicates = Util.getKeyPredicates(uriInfo);
    
    // 3. 执行删除
    storage.deleteEntityData(edmEntitySet, keyPredicates);
    
    // 4. 返回204 No Content（删除成功）
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
}
```

### 2. PrimitiveProcessor 属性写入扩展

#### updatePrimitive - 更新属性
```java
@Override
public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                           ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, DeserializerException, SerializerException {
    
    // 1. 解析URI获取实体和属性信息
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(resourceParts.size() - 1);
    
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
    EdmProperty edmProperty = uriProperty.getProperty();
    
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
    
    // 2. 反序列化属性值
    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.property(requestInputStream, edmProperty);
    Property property = result.getProperty();
    
    // 3. 更新属性
    storage.updatePropertyData(edmEntitySet, keyPredicates, property, request.getMethod());
    
    // 4. 返回204 No Content
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
}
```

#### deletePrimitive - 删除属性值 (设为null)
```java
@Override
public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo)
        throws ODataApplicationException {
    
    // 解析URI信息
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(resourceParts.size() - 1);
    
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
    EdmProperty edmProperty = uriProperty.getProperty();
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
    
    // 删除属性值（设为null）
    storage.deletePropertyData(edmEntitySet, keyPredicates, edmProperty);
    
    // 返回204 No Content
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
}
```

### 3. Storage类的CRUD扩展

#### 创建实体
```java
public Entity createEntityData(EdmEntitySet edmEntitySet, Entity entityToCreate) 
        throws ODataApplicationException {
    
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
    
    if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
        return createProduct(entityToCreate);
    }
    
    return null;
}

private Entity createProduct(Entity entity) throws ODataApplicationException {
    // 1. 生成新的ID
    int newId = productList.size() + 1;
    
    // 2. 创建新实体
    Entity newEntity = new Entity();
    newEntity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, newId));
    
    // 3. 复制其他属性
    Property nameProperty = entity.getProperty("Name");
    if (nameProperty != null) {
        newEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, 
                                          nameProperty.getValue()));
    }
    
    Property descProperty = entity.getProperty("Description");
    if (descProperty != null) {
        newEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, 
                                          descProperty.getValue()));
    }
    
    // 4. 设置ID并添加到列表
    newEntity.setId(createId("Products", newId));
    productList.add(newEntity);
    
    return newEntity;
}
```

#### 更新实体
```java
public void updateEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, 
                            Entity updateEntity, HttpMethod httpMethod) 
        throws ODataApplicationException {
    
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
    
    if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
        updateProduct(keyParams, updateEntity, httpMethod);
    }
}

private void updateProduct(List<UriParameter> keyParams, Entity updateEntity, HttpMethod httpMethod) 
        throws ODataApplicationException {
    
    // 1. 找到要更新的实体
    Entity productEntity = getProduct(keyParams);
    if (productEntity == null) {
        throw new ODataApplicationException("Entity not found", 
                                          HttpStatusCode.NOT_FOUND.getStatusCode(), 
                                          Locale.ENGLISH);
    }
    
    // 2. 根据HTTP方法执行不同的更新策略
    if (httpMethod == HttpMethod.PUT) {
        // PUT: 替换整个实体
        productEntity.getProperties().clear();
        productEntity.getProperties().addAll(updateEntity.getProperties());
    } else if (httpMethod == HttpMethod.PATCH) {
        // PATCH: 只更新提供的属性
        for (Property updateProperty : updateEntity.getProperties()) {
            Property existingProperty = productEntity.getProperty(updateProperty.getName());
            if (existingProperty != null) {
                existingProperty.setValue(updateProperty.getValueType(), updateProperty.getValue());
            } else {
                productEntity.getProperties().add(updateProperty);
            }
        }
    }
}
```

#### 删除实体
```java
public void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) 
        throws ODataApplicationException {
    
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
    
    if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
        deleteProduct(keyParams);
    }
}

private void deleteProduct(List<UriParameter> keyParams) throws ODataApplicationException {
    // 1. 找到要删除的实体
    Entity productEntity = getProduct(keyParams);
    if (productEntity == null) {
        throw new ODataApplicationException("Entity not found", 
                                          HttpStatusCode.NOT_FOUND.getStatusCode(), 
                                          Locale.ENGLISH);
    }
    
    // 2. 从列表中移除
    productList.remove(productEntity);
}
```

## HTTP方法和OData操作映射

### CRUD操作映射表

| HTTP方法 | OData操作 | URL示例 | 用途 | 响应状态码 |
|----------|-----------|---------|------|-------------|
| GET | 读取 | `/Products` | 获取实体集合 | 200 OK |
| GET | 读取 | `/Products(1)` | 获取单个实体 | 200 OK / 404 Not Found |
| POST | 创建 | `/Products` | 创建新实体 | 201 Created |
| PUT | 替换 | `/Products(1)` | 完全替换实体 | 204 No Content |
| PATCH | 更新 | `/Products(1)` | 部分更新实体 | 204 No Content |
| DELETE | 删除 | `/Products(1)` | 删除实体 | 204 No Content |
| PUT | 更新 | `/Products(1)/Name` | 更新属性值 | 204 No Content |
| DELETE | 删除 | `/Products(1)/Name` | 删除属性值(设为null) | 204 No Content |

### 请求体格式

#### 创建实体请求 (POST)
```http
POST /DemoService.svc/Products
Content-Type: application/json

{
    "Name": "New Product",
    "Description": "This is a new product"
}
```

#### 更新实体请求 (PUT)
```http
PUT /DemoService.svc/Products(1)
Content-Type: application/json

{
    "ID": 1,
    "Name": "Updated Product Name",
    "Description": "Updated description"
}
```

#### 部分更新请求 (PATCH)
```http
PATCH /DemoService.svc/Products(1)
Content-Type: application/json

{
    "Name": "Partially Updated Name"
}
```

#### 更新属性请求 (PUT)
```http
PUT /DemoService.svc/Products(1)/Name
Content-Type: application/json

{
    "value": "New Product Name"
}
```

## 数据验证和错误处理

### 验证流程图
```
客户端请求
    │
    ▼
┌─────────────────┐
│   请求验证      │
│ Content-Type    │ ──► 检查请求格式是否支持
│ HTTP Method     │
└─────────────────┘
    │
    ▼
┌─────────────────┐
│   数据反序列化  │
│ JSON/XML        │ ──► 将请求体转换为实体对象
│ Schema验证      │
└─────────────────┘
    │
    ▼
┌─────────────────┐
│   业务验证      │
│ 实体存在性      │ ──► 检查要操作的实体是否存在
│ 数据完整性      │
└─────────────────┘
    │
    ▼
┌─────────────────┐
│   执行操作      │
│ CRUD操作        │ ──► 执行实际的数据操作
│ 状态更新        │
└─────────────────┘
    │
    ▼
┌─────────────────┐
│   响应生成      │
│ 状态码设置      │ ──► 返回适当的HTTP响应
│ 内容序列化      │
└─────────────────┘
```

### 常见错误处理

```java
// 1. 实体不存在
if (entity == null) {
    throw new ODataApplicationException("Entity not found", 
                                      HttpStatusCode.NOT_FOUND.getStatusCode(), 
                                      Locale.ENGLISH);
}

// 2. 无效的请求格式
if (requestFormat == null) {
    throw new ODataApplicationException("Unsupported media type", 
                                      HttpStatusCode.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), 
                                      Locale.ENGLISH);
}

// 3. 必填字段缺失
if (nameProperty == null || nameProperty.getValue() == null) {
    throw new ODataApplicationException("Name is required", 
                                      HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                      Locale.ENGLISH);
}

// 4. 数据类型错误
try {
    Integer.valueOf(idValue.toString());
} catch (NumberFormatException e) {
    throw new ODataApplicationException("Invalid ID format", 
                                      HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                      Locale.ENGLISH);
}
```

## PUT vs PATCH 语义差异

### PUT (完全替换)
```java
// PUT语义：替换整个实体
if (httpMethod == HttpMethod.PUT) {
    // 清除所有现有属性
    productEntity.getProperties().clear();
    
    // 设置新的属性集合
    productEntity.getProperties().addAll(updateEntity.getProperties());
    
    // 如果某个属性在请求中没有提供，它将被移除/重置为默认值
}
```

### PATCH (部分更新)
```java
// PATCH语义：只更新提供的属性
if (httpMethod == HttpMethod.PATCH) {
    // 只更新请求中提供的属性
    for (Property updateProperty : updateEntity.getProperties()) {
        Property existingProperty = productEntity.getProperty(updateProperty.getName());
        if (existingProperty != null) {
            // 更新现有属性
            existingProperty.setValue(updateProperty.getValueType(), updateProperty.getValue());
        } else {
            // 添加新属性
            productEntity.getProperties().add(updateProperty);
        }
    }
    
    // 未在请求中提供的属性保持不变
}
```

### 示例对比

**原始实体**：
```json
{
    "ID": 1,
    "Name": "Original Name",
    "Description": "Original Description"
}
```

**PUT请求**：
```json
{
    "Name": "New Name"
}
```

**PUT结果**：
```json
{
    "ID": 1,
    "Name": "New Name"
    // Description 被移除了
}
```

**PATCH请求**：
```json
{
    "Name": "New Name"
}
```

**PATCH结果**：
```json
{
    "ID": 1,
    "Name": "New Name",
    "Description": "Original Description"  // 保持不变
}
```

## 测试用例

### 创建实体测试
```bash
# 创建新产品
curl -X POST http://localhost:8080/DemoService.svc/Products \
     -H "Content-Type: application/json" \
     -d '{
       "Name": "Test Product",
       "Description": "This is a test product"
     }'

# 预期：201 Created，返回创建的实体
```

### 更新实体测试
```bash
# 完全替换产品
curl -X PUT http://localhost:8080/DemoService.svc/Products(1) \
     -H "Content-Type: application/json" \
     -d '{
       "ID": 1,
       "Name": "Updated Product",
       "Description": "Updated description"
     }'

# 部分更新产品
curl -X PATCH http://localhost:8080/DemoService.svc/Products(1) \
     -H "Content-Type: application/json" \
     -d '{
       "Name": "Partially Updated"
     }'

# 预期：204 No Content
```

### 删除实体测试
```bash
# 删除产品
curl -X DELETE http://localhost:8080/DemoService.svc/Products(1)

# 预期：204 No Content

# 再次尝试获取已删除的产品
curl http://localhost:8080/DemoService.svc/Products(1)

# 预期：404 Not Found
```

### 属性操作测试
```bash
# 更新产品名称
curl -X PUT http://localhost:8080/DemoService.svc/Products(1)/Name \
     -H "Content-Type: application/json" \
     -d '{"value": "New Product Name"}'

# 删除产品描述
curl -X DELETE http://localhost:8080/DemoService.svc/Products(1)/Description

# 预期：204 No Content
```

## 总结

`p3_write`教程完成了OData服务的CRUD功能：

### 新增能力
- ✅ **实体创建**：支持POST方法创建新实体
- ✅ **实体更新**：支持PUT/PATCH方法更新实体
- ✅ **实体删除**：支持DELETE方法删除实体
- ✅ **属性操作**：支持对单个属性的更新和删除
- ✅ **数据验证**：添加了完整的数据验证机制
- ✅ **错误处理**：实现了标准的HTTP错误响应

### 技术亮点
- **反序列化支持**：实现了JSON/XML到实体对象的转换
- **HTTP语义遵循**：正确实现了PUT vs PATCH的语义差异
- **ID生成机制**：自动为新创建的实体分配唯一ID
- **并发安全**：基于内存的简单并发控制

### 架构完善
- **处理器扩展**：EntityProcessor和PrimitiveProcessor支持写入操作
- **存储层完善**：Storage类支持完整的CRUD操作
- **错误处理标准化**：统一的异常处理和HTTP状态码

这为后续的导航、查询选项等高级功能奠定了坚实的基础。
