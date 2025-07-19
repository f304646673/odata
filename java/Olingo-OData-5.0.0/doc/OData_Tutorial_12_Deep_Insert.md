# OData Tutorial 12 - 深度插入(Deep Insert) (p12_deep_insert)

## 概览

`p12_deep_insert` 项目是Apache Olingo OData V4教程的第十二课，专门讲解**深度插入(Deep Insert)**功能的实现。深度插入允许在单个POST请求中创建主实体及其相关的关联实体，是构建复杂数据模型时必不可少的功能。

## 学习目标

- 理解深度插入的概念和应用场景
- 掌握EntityProcessor中深度插入的处理逻辑
- 学会处理一对多和多对一关系的深度插入
- 了解深度插入的数据验证和完整性保证

## 深度插入概念

### 深度插入优势
- **操作简化**：一次请求创建完整的对象图
- **数据一致性**：确保关联数据的原子性创建
- **性能提升**：减少多次请求的开销
- **业务完整性**：保证复杂业务对象的完整创建

### 支持的关系类型
| 关系类型 | 描述 | 示例 |
|----------|------|------|
| **一对多** | 主实体包含多个关联实体 | Order包含多个OrderItem |
| **多对一** | 关联实体引用主实体 | OrderItem引用Product |
| **一对一** | 一对一关联关系 | User包含UserProfile |

## 核心架构

### 深度插入架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                   深度插入增强OData服务架构                        │
├─────────────────────────────────────────────────────────────────┤
│                       Client Request                            │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  POST /Orders                               │ │
│  │  {                                                          │ │
│  │    "CustomerName": "John Doe",                              │ │
│  │    "OrderItems": [                                          │ │
│  │      {                                                      │ │
│  │        "ProductID": 1,                                      │ │
│  │        "Quantity": 2                                        │ │
│  │      },                                                     │ │
│  │      {                                                      │ │
│  │        "ProductID": 2,                                      │ │
│  │        "Quantity": 1                                        │ │
│  │      }                                                      │ │
│  │    ]                                                        │ │
│  │  }                                                          │ │
│  └─────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Deep Insert Processing                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ EntityProcessor │  │   Request       │  │   Navigation    │ │
│  │                 │  │   Parsing       │  │   Property      │ │
│  │ createEntity()  │  │                 │  │   Handling      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Relationship Processing                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Main Entity   │  │   Related       │  │   Validation    │ │
│  │   Creation      │  │   Entities      │  │   & Integrity   │ │
│  │                 │  │   Creation      │  │                 │ │
│  │ Order Object    │  │ OrderItem List  │  │ FK Constraints  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Storage Layer Enhancement                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Transactional │  │   Relationship  │  │   ID           │ │
│  │   Create        │  │   Management    │  │   Generation    │ │
│  │                 │  │                 │  │                 │ │
│  │ Atomic Insert   │  │ FK Assignment   │  │ Auto-increment  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Model                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              Extended Entity Relationships                  │ │
│  │  ┌─────────────┐          ┌─────────────┐                  │ │
│  │  │   Orders    │ 1     *  │ OrderItems  │                  │ │
│  │  │  - ID       │<-------->│  - ID       │                  │ │
│  │  │  - Customer │          │  - OrderID  │                  │ │
│  │  │  - Date     │          │  - ProductID│                  │ │
│  │  │             │          │  - Quantity │                  │ │
│  │  └─────────────┘          └─────────────┘                  │ │
│  │                                    │                       │ │
│  │                                    │ *                     │ │
│  │                                    │                       │ │
│  │                           ┌─────────────┐                  │ │
│  │                           │  Products   │                  │ │
│  │                           │  - ID       │                  │ │
│  │                           │  - Name     │                  │ │
│  │                           │  - Price    │                  │ │
│  │                           └─────────────┘                  │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## EDM 模型扩展

### 1. 扩展的实体模型
```java
public class DemoEdmProvider extends CsdlAbstractEdmProvider {
    
    // 实体集名称
    public static final String ES_ORDERS = "Orders";
    public static final String ES_ORDER_ITEMS = "OrderItems";
    public static final String ES_PRODUCTS = "Products";
    
    // 实体类型名称
    public static final String ET_ORDER = "Order";
    public static final String ET_ORDER_ITEM = "OrderItem";
    public static final String ET_PRODUCT = "Product";
    
    // 导航属性名称
    public static final String NAV_TO_ORDER_ITEMS = "OrderItems";
    public static final String NAV_TO_ORDER = "Order";
    public static final String NAV_TO_PRODUCT = "Product";
    
    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        if (entityTypeName.equals(ET_ORDER_FQN)) {
            return getOrderEntityType();
        } else if (entityTypeName.equals(ET_ORDER_ITEM_FQN)) {
            return getOrderItemEntityType();
        } else if (entityTypeName.equals(ET_PRODUCT_FQN)) {
            return getProductEntityType();
        }
        return null;
    }
    
    private CsdlEntityType getOrderEntityType() {
        // 创建Order实体的属性
        CsdlProperty id = new CsdlProperty().setName("ID")
                                           .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
                                           .setNullable(false);
        
        CsdlProperty customerName = new CsdlProperty().setName("CustomerName")
                                                     .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                                                     .setNullable(false);
        
        CsdlProperty orderDate = new CsdlProperty().setName("OrderDate")
                                                  .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName())
                                                  .setNullable(false);
        
        // 创建导航属性
        CsdlNavigationProperty navToOrderItems = new CsdlNavigationProperty()
                .setName(NAV_TO_ORDER_ITEMS)
                .setType(ET_ORDER_ITEM_FQN)
                .setCollection(true)
                .setPartner("Order");
        
        // 创建属性键
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");
        
        // 组装实体类型
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_ORDER);
        entityType.setProperties(Arrays.asList(id, customerName, orderDate));
        entityType.setNavigationProperties(Arrays.asList(navToOrderItems));
        entityType.setKey(Collections.singletonList(propertyRef));
        
        return entityType;
    }
    
    private CsdlEntityType getOrderItemEntityType() {
        // OrderItem属性
        CsdlProperty id = new CsdlProperty().setName("ID")
                                           .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
                                           .setNullable(false);
        
        CsdlProperty orderID = new CsdlProperty().setName("OrderID")
                                                .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
                                                .setNullable(false);
        
        CsdlProperty productID = new CsdlProperty().setName("ProductID")
                                                  .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
                                                  .setNullable(false);
        
        CsdlProperty quantity = new CsdlProperty().setName("Quantity")
                                                 .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
                                                 .setNullable(false);
        
        // 导航属性
        CsdlNavigationProperty navToOrder = new CsdlNavigationProperty()
                .setName(NAV_TO_ORDER)
                .setType(ET_ORDER_FQN)
                .setNullable(false)
                .setPartner("OrderItems");
        
        CsdlNavigationProperty navToProduct = new CsdlNavigationProperty()
                .setName(NAV_TO_PRODUCT)
                .setType(ET_PRODUCT_FQN)
                .setNullable(false);
        
        // 创建实体类型
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_ORDER_ITEM);
        entityType.setProperties(Arrays.asList(id, orderID, productID, quantity));
        entityType.setNavigationProperties(Arrays.asList(navToOrder, navToProduct));
        entityType.setKey(Collections.singletonList(new CsdlPropertyRef().setName("ID")));
        
        return entityType;
    }
    
    private CsdlEntityType getProductEntityType() {
        // Product属性
        CsdlProperty id = new CsdlProperty().setName("ID")
                                           .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
                                           .setNullable(false);
        
        CsdlProperty name = new CsdlProperty().setName("Name")
                                             .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                                             .setNullable(false);
        
        CsdlProperty price = new CsdlProperty().setName("Price")
                                              .setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName())
                                              .setNullable(false);
        
        // 创建实体类型
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_PRODUCT);
        entityType.setProperties(Arrays.asList(id, name, price));
        entityType.setKey(Collections.singletonList(new CsdlPropertyRef().setName("ID")));
        
        return entityType;
    }
}
```

## 深度插入处理器

### 1. EntityProcessor 扩展
```java
public class DemoEntityProcessor implements EntityProcessor {
    
    private Storage storage;
    
    public DemoEntityProcessor(Storage storage) {
        this.storage = storage;
    }
    
    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, 
                           ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        
        // 获取目标实体集
        EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
        
        // 解析请求体中的实体数据
        Entity requestEntity = null;
        ExpandOption expand = uriInfo.getExpandOption();
        
        try {
            // 读取并解析请求体
            InputStream requestInputStream = request.getBody();
            ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
            
            DeserializerResult result = deserializer.entity(requestInputStream, edmEntitySet.getEntityType());
            requestEntity = result.getEntity();
            
            // 处理深度插入
            Entity createdEntity = storage.createEntityWithDeepInsert(edmEntitySet, requestEntity);
            
            // 构建响应
            buildCreatedEntityResponse(response, edmEntitySet, createdEntity, requestFormat, responseFormat);
            
        } catch (DeserializerException e) {
            throw new ODataApplicationException("Invalid entity format", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ROOT, e);
        }
    }
    
    private void buildCreatedEntityResponse(ODataResponse response, EdmEntitySet edmEntitySet, 
                                          Entity createdEntity, ContentType requestFormat, 
                                          ContentType responseFormat) 
            throws ODataApplicationException, ODataLibraryException {
        
        // 设置响应状态
        response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
        
        // 创建序列化器
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        
        // 序列化创建的实体
        EntitySerializerOptions options = EntitySerializerOptions.with()
                .contextURL(ContextURL.with()
                           .entitySet(edmEntitySet)
                           .suffix(Suffix.ENTITY)
                           .build())
                .build();
        
        SerializerResult serializerResult = serializer.entity(serviceMetadata, 
                                                             edmEntitySet.getEntityType(), 
                                                             createdEntity, 
                                                             options);
        
        // 设置响应内容
        response.setContent(serializerResult.getContent());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        
        // 设置Location头
        String location = request.getRawBaseUri() + "/" + 
                         edmEntitySet.getName() + "(" + 
                         Util.getKeyString(createdEntity, edmEntitySet.getEntityType()) + ")";
        response.setHeader(HttpHeader.LOCATION, location);
    }
}
```

### 2. Storage 深度插入实现
```java
public class Storage {
    
    // 现有的数据存储
    private List<Entity> orderList = new ArrayList<>();
    private List<Entity> orderItemList = new ArrayList<>();
    private List<Entity> productList = new ArrayList<>();
    
    // ID生成器
    private int nextOrderID = 1;
    private int nextOrderItemID = 1;
    private int nextProductID = 1;
    
    public Entity createEntityWithDeepInsert(EdmEntitySet edmEntitySet, Entity entityToCreate) 
            throws ODataApplicationException {
        
        String entitySetName = edmEntitySet.getName();
        
        switch (entitySetName) {
            case DemoEdmProvider.ES_ORDERS:
                return createOrderWithItems(entityToCreate);
            case DemoEdmProvider.ES_ORDER_ITEMS:
                return createOrderItem(entityToCreate);
            case DemoEdmProvider.ES_PRODUCTS:
                return createProduct(entityToCreate);
            default:
                throw new ODataApplicationException("Unknown entity set: " + entitySetName,
                                                  HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                  Locale.ROOT);
        }
    }
    
    private Entity createOrderWithItems(Entity orderToCreate) throws ODataApplicationException {
        // 1. 创建主订单实体
        Entity newOrder = new Entity();
        
        // 生成订单ID
        newOrder.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, nextOrderID++));
        
        // 复制订单基本属性
        copyBasicProperties(orderToCreate, newOrder, Arrays.asList("CustomerName", "OrderDate"));
        
        // 如果没有指定OrderDate，使用当前时间
        if (newOrder.getProperty("OrderDate") == null) {
            newOrder.addProperty(new Property(null, "OrderDate", ValueType.PRIMITIVE, 
                                            Timestamp.valueOf(LocalDateTime.now())));
        }
        
        // 2. 处理关联的OrderItems
        Link orderItemsLink = orderToCreate.getNavigationLink(DemoEdmProvider.NAV_TO_ORDER_ITEMS);
        if (orderItemsLink != null && orderItemsLink.getInlineEntitySet() != null) {
            EntityCollection inlineOrderItems = orderItemsLink.getInlineEntitySet();
            
            List<Entity> createdOrderItems = new ArrayList<>();
            
            for (Entity orderItemToCreate : inlineOrderItems.getEntities()) {
                Entity createdOrderItem = createOrderItemForOrder(orderItemToCreate, 
                                                                 (Integer) newOrder.getProperty("ID").getValue());
                createdOrderItems.add(createdOrderItem);
            }
            
            // 将创建的OrderItems关联到Order
            EntityCollection orderItemsCollection = new EntityCollection();
            orderItemsCollection.getEntities().addAll(createdOrderItems);
            
            Link createdItemsLink = new Link();
            createdItemsLink.setTitle(DemoEdmProvider.NAV_TO_ORDER_ITEMS);
            createdItemsLink.setInlineEntitySet(orderItemsCollection);
            newOrder.getNavigationLinks().add(createdItemsLink);
        }
        
        // 3. 保存到存储
        orderList.add(newOrder);
        
        return newOrder;
    }
    
    private Entity createOrderItemForOrder(Entity orderItemToCreate, Integer orderID) 
            throws ODataApplicationException {
        
        Entity newOrderItem = new Entity();
        
        // 生成OrderItem ID
        newOrderItem.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, nextOrderItemID++));
        
        // 设置OrderID外键
        newOrderItem.addProperty(new Property(null, "OrderID", ValueType.PRIMITIVE, orderID));
        
        // 复制OrderItem属性
        copyBasicProperties(orderItemToCreate, newOrderItem, Arrays.asList("ProductID", "Quantity"));
        
        // 验证ProductID是否存在
        Integer productID = (Integer) newOrderItem.getProperty("ProductID").getValue();
        if (!isProductExists(productID)) {
            throw new ODataApplicationException("Product with ID " + productID + " does not exist",
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                              Locale.ROOT);
        }
        
        // 验证Quantity
        Integer quantity = (Integer) newOrderItem.getProperty("Quantity").getValue();
        if (quantity == null || quantity <= 0) {
            throw new ODataApplicationException("Quantity must be greater than 0",
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                              Locale.ROOT);
        }
        
        // 保存到存储
        orderItemList.add(newOrderItem);
        
        return newOrderItem;
    }
    
    private Entity createOrderItem(Entity orderItemToCreate) throws ODataApplicationException {
        Entity newOrderItem = new Entity();
        
        // 生成ID
        newOrderItem.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, nextOrderItemID++));
        
        // 复制属性
        copyBasicProperties(orderItemToCreate, newOrderItem, 
                          Arrays.asList("OrderID", "ProductID", "Quantity"));
        
        // 验证OrderID存在
        Integer orderID = (Integer) newOrderItem.getProperty("OrderID").getValue();
        if (!isOrderExists(orderID)) {
            throw new ODataApplicationException("Order with ID " + orderID + " does not exist",
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                              Locale.ROOT);
        }
        
        // 验证ProductID存在
        Integer productID = (Integer) newOrderItem.getProperty("ProductID").getValue();
        if (!isProductExists(productID)) {
            throw new ODataApplicationException("Product with ID " + productID + " does not exist",
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                              Locale.ROOT);
        }
        
        // 保存到存储
        orderItemList.add(newOrderItem);
        
        return newOrderItem;
    }
    
    private Entity createProduct(Entity productToCreate) throws ODataApplicationException {
        Entity newProduct = new Entity();
        
        // 生成ID
        newProduct.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, nextProductID++));
        
        // 复制属性
        copyBasicProperties(productToCreate, newProduct, Arrays.asList("Name", "Price"));
        
        // 验证必需属性
        if (newProduct.getProperty("Name") == null || 
            newProduct.getProperty("Name").getValue() == null ||
            ((String) newProduct.getProperty("Name").getValue()).trim().isEmpty()) {
            throw new ODataApplicationException("Product name is required",
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                              Locale.ROOT);
        }
        
        BigDecimal price = (BigDecimal) newProduct.getProperty("Price").getValue();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ODataApplicationException("Product price must be non-negative",
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                              Locale.ROOT);
        }
        
        // 保存到存储
        productList.add(newProduct);
        
        return newProduct;
    }
    
    // 辅助方法
    private void copyBasicProperties(Entity source, Entity target, List<String> propertyNames) {
        for (String propertyName : propertyNames) {
            Property property = source.getProperty(propertyName);
            if (property != null) {
                target.addProperty(new Property(property.getType(), 
                                              property.getName(), 
                                              property.getValueType(), 
                                              property.getValue()));
            }
        }
    }
    
    private boolean isOrderExists(Integer orderID) {
        return orderList.stream()
                       .anyMatch(order -> orderID.equals(order.getProperty("ID").getValue()));
    }
    
    private boolean isProductExists(Integer productID) {
        return productList.stream()
                         .anyMatch(product -> productID.equals(product.getProperty("ID").getValue()));
    }
    
    // 初始化示例数据
    public void initializeData() {
        // 创建示例产品
        Entity product1 = new Entity();
        product1.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, nextProductID++));
        product1.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Laptop"));
        product1.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, new BigDecimal("999.99")));
        productList.add(product1);
        
        Entity product2 = new Entity();
        product2.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, nextProductID++));
        product2.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Mouse"));
        product2.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, new BigDecimal("25.50")));
        productList.add(product2);
        
        Entity product3 = new Entity();
        product3.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, nextProductID++));
        product3.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Keyboard"));
        product3.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, new BigDecimal("75.00")));
        productList.add(product3);
    }
}
```

## 深度插入请求示例

### 1. 创建Order及其OrderItems
```http
POST /DemoService.svc/Orders
Content-Type: application/json

{
    "CustomerName": "John Doe",
    "OrderDate": "2024-01-15T10:30:00Z",
    "OrderItems": [
        {
            "ProductID": 1,
            "Quantity": 2
        },
        {
            "ProductID": 2,
            "Quantity": 1
        },
        {
            "ProductID": 3,
            "Quantity": 3
        }
    ]
}
```

### 2. 预期响应
```json
{
    "@odata.context": "$metadata#Orders/$entity",
    "ID": 1,
    "CustomerName": "John Doe",
    "OrderDate": "2024-01-15T10:30:00Z",
    "OrderItems": [
        {
            "ID": 1,
            "OrderID": 1,
            "ProductID": 1,
            "Quantity": 2
        },
        {
            "ID": 2,
            "OrderID": 1,
            "ProductID": 2,
            "Quantity": 1
        },
        {
            "ID": 3,
            "OrderID": 1,
            "ProductID": 3,
            "Quantity": 3
        }
    ]
}
```

## 错误处理和验证

### 1. 数据验证
```java
private void validateOrderCreation(Entity orderToCreate) throws ODataApplicationException {
    
    // 验证必需字段
    Property customerName = orderToCreate.getProperty("CustomerName");
    if (customerName == null || customerName.getValue() == null ||
        ((String) customerName.getValue()).trim().isEmpty()) {
        throw new ODataApplicationException("CustomerName is required",
                                          HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                          Locale.ROOT);
    }
    
    // 验证OrderItems
    Link orderItemsLink = orderToCreate.getNavigationLink(DemoEdmProvider.NAV_TO_ORDER_ITEMS);
    if (orderItemsLink != null && orderItemsLink.getInlineEntitySet() != null) {
        EntityCollection orderItems = orderItemsLink.getInlineEntitySet();
        
        if (orderItems.getEntities().isEmpty()) {
            throw new ODataApplicationException("Order must contain at least one item",
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                              Locale.ROOT);
        }
        
        // 验证每个OrderItem
        for (Entity orderItem : orderItems.getEntities()) {
            validateOrderItem(orderItem);
        }
    }
}

private void validateOrderItem(Entity orderItem) throws ODataApplicationException {
    
    // 验证ProductID
    Property productIDProp = orderItem.getProperty("ProductID");
    if (productIDProp == null || productIDProp.getValue() == null) {
        throw new ODataApplicationException("ProductID is required for order item",
                                          HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                          Locale.ROOT);
    }
    
    Integer productID = (Integer) productIDProp.getValue();
    if (!isProductExists(productID)) {
        throw new ODataApplicationException("Product with ID " + productID + " does not exist",
                                          HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                          Locale.ROOT);
    }
    
    // 验证Quantity
    Property quantityProp = orderItem.getProperty("Quantity");
    if (quantityProp == null || quantityProp.getValue() == null) {
        throw new ODataApplicationException("Quantity is required for order item",
                                          HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                          Locale.ROOT);
    }
    
    Integer quantity = (Integer) quantityProp.getValue();
    if (quantity <= 0) {
        throw new ODataApplicationException("Quantity must be greater than 0",
                                          HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                          Locale.ROOT);
    }
}
```

### 2. 错误响应示例
```json
{
    "error": {
        "code": null,
        "message": "Product with ID 999 does not exist"
    }
}
```

## 测试用例

### 1. 成功的深度插入测试
```bash
# 测试创建包含多个OrderItems的Order
curl -X POST http://localhost:8080/DemoService.svc/Orders \
     -H "Content-Type: application/json" \
     -d '{
       "CustomerName": "Alice Smith",
       "OrderDate": "2024-01-20T14:30:00Z",
       "OrderItems": [
         {
           "ProductID": 1,
           "Quantity": 1
         },
         {
           "ProductID": 2,
           "Quantity": 2
         }
       ]
     }'
```

### 2. 验证创建的数据
```bash
# 查看创建的Order
curl -X GET "http://localhost:8080/DemoService.svc/Orders(1)?\$expand=OrderItems"

# 查看所有OrderItems
curl -X GET "http://localhost:8080/DemoService.svc/OrderItems"
```

### 3. 错误情况测试
```bash
# 测试无效的ProductID
curl -X POST http://localhost:8080/DemoService.svc/Orders \
     -H "Content-Type: application/json" \
     -d '{
       "CustomerName": "Bob Wilson",
       "OrderItems": [
         {
           "ProductID": 999,
           "Quantity": 1
         }
       ]
     }'

# 测试无效的Quantity
curl -X POST http://localhost:8080/DemoService.svc/Orders \
     -H "Content-Type: application/json" \
     -d '{
       "CustomerName": "Carol Johnson",
       "OrderItems": [
         {
           "ProductID": 1,
           "Quantity": 0
         }
       ]
     }'
```

## 高级特性

### 1. 事务性深度插入
```java
public Entity createOrderWithItemsTransactional(Entity orderToCreate) 
        throws ODataApplicationException {
    
    // 开始事务
    beginTransaction();
    
    try {
        // 创建主实体
        Entity createdOrder = createOrderWithItems(orderToCreate);
        
        // 提交事务
        commitTransaction();
        
        return createdOrder;
        
    } catch (Exception e) {
        // 回滚事务
        rollbackTransaction();
        
        if (e instanceof ODataApplicationException) {
            throw e;
        } else {
            throw new ODataApplicationException("Failed to create order with items",
                                              HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
                                              Locale.ROOT, e);
        }
    }
}
```

### 2. 扩展的导航链接
```java
private void addNavigationLinks(Entity createdOrder) {
    
    Integer orderID = (Integer) createdOrder.getProperty("ID").getValue();
    
    // 查找关联的OrderItems
    List<Entity> relatedOrderItems = orderItemList.stream()
            .filter(item -> orderID.equals(item.getProperty("OrderID").getValue()))
            .collect(Collectors.toList());
    
    if (!relatedOrderItems.isEmpty()) {
        EntityCollection orderItemsCollection = new EntityCollection();
        orderItemsCollection.getEntities().addAll(relatedOrderItems);
        
        Link orderItemsLink = new Link();
        orderItemsLink.setTitle(DemoEdmProvider.NAV_TO_ORDER_ITEMS);
        orderItemsLink.setInlineEntitySet(orderItemsCollection);
        
        createdOrder.getNavigationLinks().add(orderItemsLink);
    }
}
```

## 总结

`p12_deep_insert`教程实现了OData深度插入的核心功能：

### 新增能力
- ✅ **深度插入**：单个请求创建主实体及关联实体
- ✅ **关系管理**：自动处理一对多关系的外键设置
- ✅ **数据验证**：完整的业务规则验证
- ✅ **错误处理**：详细的错误信息和回滚机制
- ✅ **导航属性**：响应中包含创建的关联实体

### 技术亮点
- **原子性操作**：确保主实体和关联实体的一致性创建
- **外键管理**：自动设置和验证外键关系
- **数据完整性**：严格的业务规则验证
- **响应完整性**：包含关联数据的完整响应

### 架构价值
- **开发效率**：减少客户端的多次请求
- **数据一致性**：保证复杂对象的原子性创建
- **业务完整性**：支持复杂业务场景的数据创建
- **用户体验**：简化客户端的数据操作逻辑

深度插入功能是构建复杂业务应用时的重要特性，它使得客户端能够以自然的方式创建包含关联关系的复杂对象。
