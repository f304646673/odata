# OData Tutorial 09 - 操作(Actions) (p9_action)

## 概览

`p9_action` 项目是Apache Olingo OData V4教程的第九课，专门讲解**OData操作(Actions)**的实现。操作是OData协议中用于执行自定义业务逻辑的重要机制，允许服务端定义和公开不符合标准CRUD模式的业务功能。

## 学习目标

- 理解OData操作(Actions)与函数(Functions)的区别
- 掌握操作的定义、公开和调用方法
- 学会实现ActionProcessor处理器
- 了解操作参数的处理和验证机制

## 操作 vs 函数对比

| 特性 | Actions | Functions |
|------|---------|-----------|
| **副作用** | 有副作用，可修改数据 | 无副作用，只读操作 |
| **HTTP方法** | POST | GET |
| **缓存** | 不可缓存 | 可缓存 |
| **用途** | 业务操作、数据修改 | 计算、查询、转换 |
| **示例** | Reset、Approve、Cancel | Count、Calculate、Search |

## 核心架构

### 操作处理架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                    操作增强OData服务架构                           │
├─────────────────────────────────────────────────────────────────┤
│                       Client Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   HTTP POST     │  │  Action Call    │  │  Parameters     │ │
│  │ /Reset          │  │  Request        │  │  in Body        │ │
│  │                 │  │                 │  │  (JSON)         │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    OData Handler Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  DemoServlet    │  │ODataHttpHandler │  │ ServiceMetadata │ │
│  │                 │  │                 │  │  + Actions      │ │
│  │  (Entry Point)  │  │ (Action Routing)│  │   Metadata      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Processor Layer                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │     CRUD        │  │ ActionProcessor │  │   Action        │ │
│  │   Processors    │  │     (新增)      │  │  Execution      │ │
│  │                 │  │                 │  │                 │ │
│  │  (existing)     │  │ processAction() │  │  Business       │ │
│  │                 │  │                 │  │   Logic         │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   EDM Provider Layer                            │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  DemoEdmProvider                            │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │ EntityTypes │ │ EntitySets  │ │ Actions     │           │ │
│  │  │  (existing) │ │ (existing)  │ │ Definition  │           │ │
│  │  │             │ │             │ │             │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  │  ┌─────────────┐ ┌─────────────┐                           │ │
│  │  │ Action      │ │ Container   │                           │ │
│  │  │ Imports     │ │ + Actions   │                           │ │
│  │  └─────────────┘ └─────────────┘                           │ │
│  └─────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Layer                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                      Storage                                │ │
│  │  ┌─────────────┐ ┌─────────────┐                           │ │
│  │  │   Data      │ │   Action    │                           │ │
│  │  │  Storage    │ │  Methods    │                           │ │
│  │  │ (existing)  │ │  (Reset等)  │                           │ │
│  │  └─────────────┘ └─────────────┘                           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## EDM Provider 操作定义

### 1. 操作定义
```java
public class DemoEdmProvider extends CsdlAbstractEdmProvider {
    
    // 操作名称常量
    public static final String ACTION_RESET = "Reset";
    public static final FullQualifiedName ACTION_RESET_FQN = 
        new FullQualifiedName(NAMESPACE, ACTION_RESET);
    
    // 参数名称常量
    public static final String PARAMETER_AMOUNT = "Amount";
    
    @Override
    public List<CsdlAction> getActions(final FullQualifiedName actionName) {
        if (actionName.equals(ACTION_RESET_FQN)) {
            // 创建参数列表
            final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
            
            // 定义Amount参数
            final CsdlParameter parameter = new CsdlParameter();
            parameter.setName(PARAMETER_AMOUNT);
            parameter.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            parameter.setNullable(false); // 必填参数
            parameters.add(parameter);
            
            // 创建操作定义
            final CsdlAction action = new CsdlAction();
            action.setName(ACTION_RESET_FQN.getName());
            action.setParameters(parameters);
            // 注意：操作没有返回类型定义，表示无返回值
            
            return Arrays.asList(action);
        }
        return null;
    }
    
    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) {
        if (entityContainer.equals(CONTAINER)) {
            if (actionImportName.equals(ACTION_RESET)) {
                return new CsdlActionImport()
                        .setName(actionImportName)
                        .setAction(ACTION_RESET_FQN);
            }
        }
        return null;
    }
    
    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        // 现有的实体集
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.add(getEntitySet(CONTAINER, ES_PRODUCTS_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_CATEGORIES_NAME));
        
        // 添加操作导入
        List<CsdlActionImport> actionImports = new ArrayList<CsdlActionImport>();
        actionImports.add(getActionImport(CONTAINER, ACTION_RESET));
        
        // 创建容器
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);
        entityContainer.setActionImports(actionImports); // 添加操作导入
        
        return entityContainer;
    }
}
```

### 2. 操作元数据示例
定义的操作在服务元数据中会显示为：
```xml
<Action Name="Reset">
    <Parameter Name="Amount" Type="Edm.Int32" Nullable="false"/>
</Action>

<EntityContainer Name="Container">
    <EntitySet Name="Products" EntityType="OData.Demo.Product"/>
    <EntitySet Name="Categories" EntityType="OData.Demo.Category"/>
    <ActionImport Name="Reset" Action="OData.Demo.Reset"/>
</EntityContainer>
```

## ActionProcessor 实现

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
            handler.register(new DemoActionProcessor(storage)); // 注册操作处理器
            
            // 处理请求
            handler.process(req, resp);
        } catch (RuntimeException e) {
            LOG.error("Server Error occurred in ExampleServlet", e);
            throw new ServletException(e);
        }
    }
}
```

### 2. DemoActionProcessor 实现
```java
public class DemoActionProcessor implements ActionProcessor {
    
    private OData odata;
    private Storage storage;
    private ServiceMetadata serviceMetadata;
    
    public DemoActionProcessor(Storage storage) {
        this.storage = storage;
    }
    
    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }
    
    @Override
    public void processAction(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                             ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        
        // 1. 获取操作信息
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResourceAction uriResourceAction = (UriResourceAction) resourceParts.get(0);
        EdmAction edmAction = uriResourceAction.getAction();
        
        // 2. 检查操作名称
        if (DemoEdmProvider.ACTION_RESET.equals(edmAction.getName())) {
            processResetAction(request, response, requestFormat, responseFormat);
        } else {
            throw new ODataApplicationException("Unknown action: " + edmAction.getName(), 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ENGLISH);
        }
    }
    
    private void processResetAction(ODataRequest request, ODataResponse response,
                                   ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        
        // 3. 解析操作参数
        Map<String, Parameter> parameters = readActionParameters(request, requestFormat);
        
        // 4. 获取Amount参数
        Parameter amountParameter = parameters.get(DemoEdmProvider.PARAMETER_AMOUNT);
        if (amountParameter == null) {
            throw new ODataApplicationException("Missing required parameter: Amount", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ENGLISH);
        }
        
        Object amountValue = amountParameter.getValue();
        if (!(amountValue instanceof Integer)) {
            throw new ODataApplicationException("Parameter Amount must be an integer", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ENGLISH);
        }
        
        int amount = (Integer) amountValue;
        if (amount < 0) {
            throw new ODataApplicationException("Parameter Amount must be non-negative", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ENGLISH);
        }
        
        // 5. 执行业务逻辑
        storage.resetDataSet(amount);
        
        // 6. 返回成功响应（操作无返回值）
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }
    
    private Map<String, Parameter> readActionParameters(ODataRequest request, ContentType requestFormat)
            throws ODataApplicationException, ODataLibraryException {
        
        if (requestFormat == null) {
            throw new ODataApplicationException("Missing content type for action parameters", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ENGLISH);
        }
        
        // 读取请求体
        InputStream requestInputStream = request.getBody();
        if (requestInputStream == null) {
            return new HashMap<>(); // 无参数
        }
        
        // 反序列化参数
        ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.actionParameters(requestInputStream, 
                                                                 getActionDefinition());
        
        return result.getActionParameters();
    }
    
    private EdmAction getActionDefinition() {
        // 获取操作定义用于参数反序列化
        EdmEntityContainer container = serviceMetadata.getEdm().getEntityContainer();
        EdmActionImport actionImport = container.getActionImport(DemoEdmProvider.ACTION_RESET);
        return actionImport.getUnboundAction();
    }
}
```

## Storage 操作支持

### 1. Reset操作实现
```java
public class Storage {
    
    // 现有的数据存储
    private List<Entity> productList;
    private List<Entity> categoryList;
    
    // Reset操作：重新初始化数据集
    public void resetDataSet(final int amount) {
        // 清除现有数据
        productList.clear();
        categoryList.clear();
        
        // 重新初始化指定数量的数据
        initCategorySampleData();
        initProductSampleData(amount);
    }
    
    private void initProductSampleData(int amount) {
        // 生成指定数量的产品数据
        for (int i = 1; i <= amount; i++) {
            Entity product = new Entity()
                .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, i))
                .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Product " + i))
                .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, 
                            "Description for Product " + i))
                .addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 
                            new BigDecimal(100 + i * 10)))
                .addProperty(new Property(null, "CategoryID", ValueType.PRIMITIVE, 
                            (i % 3) + 1)); // 循环分配到3个分类
            
            product.setId(createId("Products", i));
            productList.add(product);
        }
    }
    
    private void initCategorySampleData() {
        // 初始化固定的分类数据
        Entity category1 = new Entity()
            .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1))
            .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Electronics"))
            .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "Electronic products"));
        category1.setId(createId("Categories", 1));
        categoryList.add(category1);
        
        Entity category2 = new Entity()
            .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2))
            .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Books"))
            .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "Book products"));
        category2.setId(createId("Categories", 2));
        categoryList.add(category2);
        
        Entity category3 = new Entity()
            .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 3))
            .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Clothing"))
            .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "Clothing products"));
        category3.setId(createId("Categories", 3));
        categoryList.add(category3);
    }
}
```

## 操作调用

### 1. 操作URL模式
```http
# 操作调用URL
POST /DemoService.svc/Reset

# 操作在服务文档中的发现
GET /DemoService.svc/
```

### 2. 操作调用请求
```http
POST /DemoService.svc/Reset
Content-Type: application/json

{
    "Amount": 15
}
```

### 3. 操作调用响应
```http
HTTP/1.1 204 No Content
```

### 4. 验证操作效果
```http
# 调用操作后查询产品数量
GET /DemoService.svc/Products?$count=true

# 响应应显示指定数量的产品
{
    "@odata.context": "$metadata#Products",
    "@odata.count": 15,
    "value": [...]
}
```

## 错误处理

### 1. 参数验证错误
```json
// 请求：缺少必需参数
POST /DemoService.svc/Reset
Content-Type: application/json

{}

// 响应：400 Bad Request
{
    "error": {
        "code": null,
        "message": "Missing required parameter: Amount"
    }
}
```

### 2. 参数类型错误
```json
// 请求：参数类型错误
POST /DemoService.svc/Reset
Content-Type: application/json

{
    "Amount": "invalid"
}

// 响应：400 Bad Request
{
    "error": {
        "code": null,
        "message": "Parameter Amount must be an integer"
    }
}
```

### 3. 参数值验证错误
```json
// 请求：参数值无效
POST /DemoService.svc/Reset
Content-Type: application/json

{
    "Amount": -5
}

// 响应：400 Bad Request
{
    "error": {
        "code": null,
        "message": "Parameter Amount must be non-negative"
    }
}
```

## 带返回值的操作示例

### 1. 返回实体集合的操作
```java
// EDM Provider中定义
public List<CsdlAction> getActions(final FullQualifiedName actionName) {
    if (actionName.equals(ACTION_GET_EXPENSIVE_PRODUCTS_FQN)) {
        // 定义参数
        final CsdlParameter parameter = new CsdlParameter();
        parameter.setName("MinPrice");
        parameter.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
        
        // 定义返回类型
        CsdlReturnType returnType = new CsdlReturnType();
        returnType.setType(ET_PRODUCT_FQN);
        returnType.setCollection(true); // 返回集合
        
        // 创建操作
        final CsdlAction action = new CsdlAction();
        action.setName("GetExpensiveProducts");
        action.setParameters(Arrays.asList(parameter));
        action.setReturnType(returnType); // 设置返回类型
        
        return Arrays.asList(action);
    }
    return null;
}
```

### 2. 返回值的处理
```java
// ActionProcessor中处理返回值
private void processGetExpensiveProductsAction(ODataRequest request, ODataResponse response,
                                              ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, ODataLibraryException {
    
    // 解析参数
    Map<String, Parameter> parameters = readActionParameters(request, requestFormat);
    BigDecimal minPrice = (BigDecimal) parameters.get("MinPrice").getValue();
    
    // 执行业务逻辑
    EntityCollection expensiveProducts = storage.getProductsAbovePrice(minPrice);
    
    // 序列化返回值
    EdmEntitySet productsEntitySet = serviceMetadata.getEdm()
                                    .getEntityContainer()
                                    .getEntitySet("Products");
    
    ContextURL contextUrl = ContextURL.with().entitySet(productsEntitySet).build();
    EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
                                                .contextURL(contextUrl)
                                                .build();
    
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    SerializerResult serializerResult = serializer.entityCollection(serviceMetadata,
                                                                   productsEntitySet.getEntityType(),
                                                                   expensiveProducts, options);
    
    // 设置响应
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
}
```

## 绑定操作 vs 非绑定操作

### 1. 非绑定操作（当前示例）
```http
# 非绑定操作直接在服务根URL调用
POST /DemoService.svc/Reset
```

### 2. 绑定操作示例
```java
// 绑定到实体的操作定义
public List<CsdlAction> getActions(final FullQualifiedName actionName) {
    if (actionName.equals(ACTION_DISCOUNT_PRODUCT_FQN)) {
        // 绑定参数（隐式）
        CsdlParameter bindingParameter = new CsdlParameter();
        bindingParameter.setName("product");
        bindingParameter.setType(ET_PRODUCT_FQN);
        bindingParameter.setNullable(false);
        
        // 操作参数
        CsdlParameter discountParameter = new CsdlParameter();
        discountParameter.setName("DiscountPercent");
        discountParameter.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
        
        CsdlAction action = new CsdlAction();
        action.setName("DiscountProduct");
        action.setParameters(Arrays.asList(bindingParameter, discountParameter));
        action.setBound(true); // 标记为绑定操作
        
        return Arrays.asList(action);
    }
    return null;
}
```

```http
# 绑定操作在特定实体上调用
POST /DemoService.svc/Products(1)/OData.Demo.DiscountProduct
Content-Type: application/json

{
    "DiscountPercent": 10.0
}
```

## 测试用例

### 1. 基础操作调用
```bash
# 调用Reset操作
curl -X POST http://localhost:8080/DemoService.svc/Reset \
     -H "Content-Type: application/json" \
     -d '{"Amount": 10}'

# 预期：204 No Content
```

### 2. 验证操作效果
```bash
# 检查产品数量
curl "http://localhost:8080/DemoService.svc/Products?\$count=true"

# 预期：返回10个产品
```

### 3. 参数验证测试
```bash
# 无效参数类型
curl -X POST http://localhost:8080/DemoService.svc/Reset \
     -H "Content-Type: application/json" \
     -d '{"Amount": "invalid"}'

# 预期：400 Bad Request

# 负数参数
curl -X POST http://localhost:8080/DemoService.svc/Reset \
     -H "Content-Type: application/json" \
     -d '{"Amount": -5}'

# 预期：400 Bad Request
```

### 4. 操作发现测试
```bash
# 查看服务文档
curl http://localhost:8080/DemoService.svc/

# 查看元数据
curl http://localhost:8080/DemoService.svc/\$metadata

# 两者都应该包含Reset操作的信息
```

## 总结

`p9_action`教程实现了OData操作的核心功能：

### 新增能力
- ✅ **操作定义**：在EDM中定义自定义操作
- ✅ **操作处理**：实现ActionProcessor处理操作调用
- ✅ **参数处理**：操作参数的解析和验证
- ✅ **业务逻辑**：执行自定义业务操作
- ✅ **错误处理**：完善的参数验证和错误响应

### 技术亮点
- **参数反序列化**：正确处理JSON格式的操作参数
- **类型安全**：强类型的参数验证和类型转换
- **元数据集成**：操作在服务元数据中的正确公开
- **HTTP语义**：遵循POST方法和适当的状态码

### 架构价值
- **业务扩展**：为自定义业务逻辑提供了标准化接口
- **操作封装**：将复杂业务操作封装为可调用的服务
- **协议遵循**：严格遵循OData V4操作规范
- **功能完整**：支持参数、返回值、绑定等完整特性

操作功能使OData服务不仅限于CRUD操作，还能提供丰富的业务功能接口，为构建企业级业务服务提供了重要支持。
