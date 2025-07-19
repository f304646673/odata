# OData Tutorial 04 - 导航属性 (p4_navigation)

## 概览

`p4_navigation` 项目是Apache Olingo OData V4教程的第四课，在前面CRUD功能的基础上添加了**导航属性**支持。导航属性是OData的核心特性之一，允许客户端通过实体之间的关系进行数据导航，实现关联查询和关系数据访问。

## 学习目标

- 理解OData导航属性的概念和作用
- 掌握实体间关系的定义方法
- 学会实现导航查询处理
- 了解一对多和多对一关系的处理差异

## 功能对比

| 功能 | p1-3 | p4_navigation |
|------|------|---------------|
| 基础CRUD操作 | ✅ | ✅ |
| 实体类型定义 | Product | Product + Category |
| 实体关系 | 无 | Product ↔ Category |
| 导航查询 | ❌ | ✅ |
| 关联数据访问 | ❌ | ✅ |

## 数据模型设计

### 实体关系图
```
┌─────────────────┐    1        ∞    ┌─────────────────┐
│    Category     │◄──────────────────┤     Product     │
├─────────────────┤                   ├─────────────────┤
│ ID (Key)        │                   │ ID (Key)        │
│ Name            │                   │ Name            │
│ Description     │                   │ Description     │
└─────────────────┘                   │ ReleaseDate     │
                                      │ DiscontinuedDate│
                                      │ Rating          │
                                      │ Price           │
                                      │ CategoryID (FK) │
                                      └─────────────────┘
```

### 导航属性定义
- **Product → Category**：多对一关系，一个产品属于一个分类
- **Category → Products**：一对多关系，一个分类包含多个产品

## 核心架构

### 导航处理架构图
```
┌─────────────────────────────────────────────────────────────────┐
│                    导航增强OData服务架构                           │
├─────────────────────────────────────────────────────────────────┤
│                     Navigation URLs                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ /Products(1)/   │  │ /Categories(1)/ │  │ /Products(1)/   │ │
│  │   Category      │  │   Products      │  │ Category/Name   │ │
│  │ (多对一导航)     │  │ (一对多导航)     │  │ (导航+属性)     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   URI Resolution Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ URI Parsing     │  │ Navigation      │  │ Resource Path   │ │
│  │ UriInfo         │  │ Detection       │  │ Analysis        │ │
│  │ ResourceParts   │  │ UriNavigation   │  │ Segment Count   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Processor Layer                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │EntityCollection │  │ EntityProcessor │  │ Navigation      │ │
│  │   Processor     │  │                 │  │ Handling        │ │
│  │                 │  │                 │  │                 │ │
│  │ + Navigation    │  │ + Navigation    │  │ + Key Resolution│ │
│  │   Collection    │  │   Entity        │  │ + Relationship  │ │
│  │   Support       │  │   Support       │  │   Traversal     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   EDM Provider Layer                            │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  DemoEdmProvider                            │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │ EntityTypes │ │EntitySets + │ │ Navigation  │           │ │
│  │  │  Product    │ │ Navigation  │ │ Properties  │           │ │
│  │  │  Category   │ │ Bindings    │ │ (双向)      │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Layer                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                      Storage                                │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │  Products   │ │ Categories  │ │ Navigation  │           │ │
│  │  │    List     │ │    List     │ │  Methods    │           │ │
│  │  │(+CategoryID)│ │             │ │             │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## EDM Provider 导航扩展

### 1. Category实体类型定义
```java
private CsdlEntityType createCategoryEntityType() {
    // 创建Category的属性
    CsdlProperty id = new CsdlProperty().setName("ID")
                      .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
    CsdlProperty name = new CsdlProperty().setName("Name")
                        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
    CsdlProperty description = new CsdlProperty().setName("Description")
                              .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
    
    // 创建导航属性：Category -> Products (一对多)
    CsdlNavigationProperty navProp = new CsdlNavigationProperty()
                                     .setName("Products")
                                     .setType(ET_PRODUCT_FQN)
                                     .setCollection(true)  // 一对多关系
                                     .setPartner("Category");
    
    // 创建主键
    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
    propertyRef.setName("ID");
    
    // 组装实体类型
    return new CsdlEntityType()
            .setName(ET_CATEGORY_NAME)
            .setProperties(Arrays.asList(id, name, description))
            .setNavigationProperties(Arrays.asList(navProp))
            .setKey(Collections.singletonList(propertyRef));
}
```

### 2. Product实体类型扩展
```java
private CsdlEntityType createProductEntityType() {
    // 原有属性
    CsdlProperty id = new CsdlProperty().setName("ID")
                      .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
    CsdlProperty name = new CsdlProperty().setName("Name")
                        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
    // ... 其他属性
    
    // 新增：外键属性
    CsdlProperty categoryId = new CsdlProperty().setName("CategoryID")
                             .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
    
    // 新增：导航属性 Product -> Category (多对一)
    CsdlNavigationProperty navProp = new CsdlNavigationProperty()
                                     .setName("Category")
                                     .setType(ET_CATEGORY_FQN)
                                     .setNullable(false)   // 多对一关系
                                     .setPartner("Products");
    
    return new CsdlEntityType()
            .setName(ET_PRODUCT_NAME)
            .setProperties(Arrays.asList(id, name, description, releaseDate, 
                                        discontinuedDate, rating, price, categoryId))
            .setNavigationProperties(Arrays.asList(navProp))
            .setKey(Collections.singletonList(propertyRef));
}
```

### 3. 导航属性绑定
```java
@Override
public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
    if (entitySetName.equals(ES_PRODUCTS_NAME)) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_PRODUCTS_NAME);
        entitySet.setType(ET_PRODUCT_FQN);
        
        // 添加导航属性绑定
        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setPath("Category");        // 导航属性名
        navPropBinding.setTarget("Categories");    // 目标实体集
        entitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding));
        
        return entitySet;
    } else if (entitySetName.equals(ES_CATEGORIES_NAME)) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_CATEGORIES_NAME);
        entitySet.setType(ET_CATEGORY_FQN);
        
        // 添加导航属性绑定
        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setPath("Products");       // 导航属性名
        navPropBinding.setTarget("Products");     // 目标实体集
        entitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding));
        
        return entitySet;
    }
    return null;
}
```

## 处理器导航支持

### 1. EntityProcessor 导航查询

#### 导航URL解析流程
```java
public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, 
                      ContentType responseFormat) throws ODataApplicationException, SerializerException {
    
    // 分析URI段数来判断请求类型
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    int segmentCount = resourceParts.size();
    
    if (segmentCount == 1) {
        // 情况1: /Products(1) - 直接实体查询
        handleDirectEntityRead(resourceParts, response, responseFormat);
        
    } else if (segmentCount == 2) {
        // 情况2: /Products(1)/Category - 导航查询
        handleNavigationEntityRead(resourceParts, response, responseFormat);
        
    } else {
        // 不支持更复杂的导航
        throw new ODataApplicationException("Complex navigation not supported", 
                                          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), 
                                          Locale.ROOT);
    }
}
```

#### 导航实体查询实现
```java
private void handleNavigationEntityRead(List<UriResource> resourceParts, 
                                       ODataResponse response, ContentType responseFormat) 
        throws ODataApplicationException, SerializerException {
    
    // 1. 解析源实体信息
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
    
    // 2. 解析导航段信息
    UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourceParts.get(1);
    EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
    EdmEntityType targetEntityType = edmNavigationProperty.getType();
    
    // 3. 获取源实体
    Entity sourceEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);
    if (sourceEntity == null) {
        throw new ODataApplicationException("Source entity not found", 
                                          HttpStatusCode.NOT_FOUND.getStatusCode(), 
                                          Locale.ENGLISH);
    }
    
    // 4. 根据导航属性获取目标实体
    Entity targetEntity = null;
    List<UriParameter> navKeyPredicates = uriResourceNavigation.getKeyPredicates();
    
    if (navKeyPredicates.isEmpty()) {
        // 情况: /Products(1)/Category - 多对一导航
        targetEntity = storage.getRelatedEntity(sourceEntity, targetEntityType);
    } else {
        // 情况: /Categories(1)/Products(5) - 一对多导航带键值
        targetEntity = storage.getRelatedEntity(sourceEntity, targetEntityType, navKeyPredicates);
    }
    
    if (targetEntity == null) {
        throw new ODataApplicationException("Target entity not found", 
                                          HttpStatusCode.NOT_FOUND.getStatusCode(), 
                                          Locale.ENGLISH);
    }
    
    // 5. 确定响应实体集
    EdmEntitySet responseEdmEntitySet;
    if (!edmNavigationProperty.containsTarget()) {
        responseEdmEntitySet = Util.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);
    } else {
        responseEdmEntitySet = startEdmEntitySet;
    }
    
    // 6. 序列化响应
    serializeEntity(targetEntity, targetEntityType, responseEdmEntitySet, response, responseFormat);
}
```

### 2. EntityCollectionProcessor 导航集合查询

#### 导航集合查询实现
```java
public void readEntityCollection(ODataRequest request, ODataResponse response,
                                UriInfo uriInfo, ContentType responseFormat)
        throws ODataApplicationException, SerializerException {
    
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    int segmentCount = resourceParts.size();
    
    if (segmentCount == 1) {
        // 直接实体集查询: /Products
        handleDirectCollectionRead(resourceParts, response, responseFormat);
        
    } else if (segmentCount == 2) {
        // 导航集合查询: /Categories(1)/Products
        handleNavigationCollectionRead(resourceParts, response, responseFormat);
    }
}

private void handleNavigationCollectionRead(List<UriResource> resourceParts, 
                                           ODataResponse response, ContentType responseFormat) 
        throws ODataApplicationException, SerializerException {
    
    // 1. 解析源实体
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
    
    // 2. 解析导航信息
    UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourceParts.get(1);
    EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
    EdmEntityType targetEntityType = edmNavigationProperty.getType();
    
    // 3. 获取源实体
    Entity sourceEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);
    if (sourceEntity == null) {
        throw new ODataApplicationException("Source entity not found", 
                                          HttpStatusCode.NOT_FOUND.getStatusCode(), 
                                          Locale.ENGLISH);
    }
    
    // 4. 获取关联的实体集合
    EntityCollection targetEntityCollection = storage.getRelatedEntityCollection(sourceEntity, targetEntityType);
    
    // 5. 序列化响应
    serializeEntityCollection(targetEntityCollection, targetEntityType, response, responseFormat);
}
```

## Storage 导航数据支持

### 1. 关联实体查询
```java
// 获取单个关联实体 (多对一导航)
public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType) {
    EntityCollection relatedEntityCollection = getRelatedEntityCollection(entity, relatedEntityType);
    if (relatedEntityCollection.getEntities().isEmpty()) {
        return null;
    }
    return relatedEntityCollection.getEntities().get(0);
}

// 获取带键值的关联实体 (一对多导航+键值)
public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType, 
                              List<UriParameter> keyPredicates) {
    EntityCollection relatedEntityCollection = getRelatedEntityCollection(entity, relatedEntityType);
    
    // 从关联集合中查找特定键值的实体
    return findEntityByKey(relatedEntityCollection, keyPredicates, relatedEntityType);
}
```

### 2. 关联实体集合查询
```java
public EntityCollection getRelatedEntityCollection(Entity sourceEntity, EdmEntityType targetEntityType) {
    
    if (targetEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
        // 源实体是Category，查找其下的所有Products
        return getProductsByCategory(sourceEntity);
        
    } else if (targetEntityType.getName().equals(DemoEdmProvider.ET_CATEGORY_NAME)) {
        // 源实体是Product，查找其所属的Category（作为单元素集合返回）
        return getCategoryByProduct(sourceEntity);
    }
    
    return new EntityCollection();
}

private EntityCollection getProductsByCategory(Entity categoryEntity) {
    // 从Category实体获取ID
    Integer categoryId = (Integer) categoryEntity.getProperty("ID").getValue();
    
    EntityCollection productsCollection = new EntityCollection();
    
    // 查找所有CategoryID匹配的Product
    for (Entity product : productList) {
        Property categoryIdProperty = product.getProperty("CategoryID");
        if (categoryIdProperty != null && categoryId.equals(categoryIdProperty.getValue())) {
            productsCollection.getEntities().add(product);
        }
    }
    
    return productsCollection;
}

private EntityCollection getCategoryByProduct(Entity productEntity) {
    // 从Product实体获取CategoryID
    Property categoryIdProperty = productEntity.getProperty("CategoryID");
    if (categoryIdProperty == null) {
        return new EntityCollection();
    }
    
    Integer categoryId = (Integer) categoryIdProperty.getValue();
    
    // 查找对应的Category
    for (Entity category : categoryList) {
        Integer catId = (Integer) category.getProperty("ID").getValue();
        if (categoryId.equals(catId)) {
            EntityCollection result = new EntityCollection();
            result.getEntities().add(category);
            return result;
        }
    }
    
    return new EntityCollection();
}
```

### 3. 数据初始化扩展
```java
private void initCategorySampleData() {
    Entity category1 = new Entity()
        .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1))
        .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Notebooks"))
        .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "Notebook computers"));
    category1.setId(createId("Categories", 1));
    categoryList.add(category1);
    
    Entity category2 = new Entity()
        .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2))
        .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Monitors"))
        .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "Computer monitors"));
    category2.setId(createId("Categories", 2));
    categoryList.add(category2);
}

private void initProductSampleData() {
    Entity product1 = new Entity()
        .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1))
        .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Notebook Basic 15"))
        .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "Basic notebook"))
        .addProperty(new Property(null, "CategoryID", ValueType.PRIMITIVE, 1));  // 关联到Notebooks
    product1.setId(createId("Products", 1));
    productList.add(product1);
    
    Entity product2 = new Entity()
        .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2))
        .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Ergo Screen"))
        .addProperty(new Property(null, "Description", ValueType.PRIMITIVE, "Ergonomic monitor"))
        .addProperty(new Property(null, "CategoryID", ValueType.PRIMITIVE, 2));  // 关联到Monitors
    product2.setId(createId("Products", 2));
    productList.add(product2);
}
```

## 支持的导航URL模式

### 1. 多对一导航 (Product → Category)
```http
# 获取产品的分类
GET /DemoService.svc/Products(1)/Category

# 获取产品分类的名称
GET /DemoService.svc/Products(1)/Category/Name
```

### 2. 一对多导航 (Category → Products)
```http
# 获取分类下的所有产品
GET /DemoService.svc/Categories(1)/Products

# 获取分类下的特定产品
GET /DemoService.svc/Categories(1)/Products(2)
```

### 3. 导航响应示例

#### 多对一导航响应
```json
{
    "@odata.context": "$metadata#Categories/$entity",
    "ID": 1,
    "Name": "Notebooks",
    "Description": "Notebook computers"
}
```

#### 一对多导航响应
```json
{
    "@odata.context": "$metadata#Products",
    "value": [
        {
            "ID": 1,
            "Name": "Notebook Basic 15",
            "Description": "Basic notebook",
            "CategoryID": 1
        },
        {
            "ID": 3,
            "Name": "Notebook Professional 17",
            "Description": "Professional notebook",
            "CategoryID": 1
        }
    ]
}
```

## 工具类支持

### Util类导航支持
```java
public class Util {
    
    // 获取导航目标实体集
    public static EdmEntitySet getNavigationTargetEntitySet(EdmEntitySet startEntitySet, 
                                                           EdmNavigationProperty edmNavigationProperty) 
            throws ODataApplicationException {
        
        EdmEntityContainer entityContainer = startEntitySet.getEntityContainer();
        String navPropName = edmNavigationProperty.getName();
        
        // 通过导航属性绑定查找目标实体集
        EdmEntitySet targetEntitySet = null;
        for (EdmEntitySet entitySet : entityContainer.getEntitySets()) {
            for (EdmNavigationPropertyBinding binding : startEntitySet.getNavigationPropertyBindings()) {
                if (binding.getPath().equals(navPropName)) {
                    String targetName = binding.getTarget();
                    targetEntitySet = entityContainer.getEntitySet(targetName);
                    break;
                }
            }
        }
        
        if (targetEntitySet == null) {
            throw new ODataApplicationException("Cannot find target EntitySet for navigation property: " + navPropName, 
                                              HttpStatusCode.NOT_FOUND.getStatusCode(), 
                                              Locale.ENGLISH);
        }
        
        return targetEntitySet;
    }
    
    // 获取键值谓词字符串表示
    public static String getKeyPredicatesAsString(List<UriParameter> keyPredicates) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < keyPredicates.size(); i++) {
            if (i > 0) {
                result.append(",");
            }
            UriParameter keyPredicate = keyPredicates.get(i);
            result.append(keyPredicate.getName()).append("=").append(keyPredicate.getText());
        }
        return result.toString();
    }
}
```

## 测试用例

### 基础导航测试
```bash
# 多对一导航
curl http://localhost:8080/DemoService.svc/Products(1)/Category

# 一对多导航
curl http://localhost:8080/DemoService.svc/Categories(1)/Products

# 导航+键值
curl http://localhost:8080/DemoService.svc/Categories(1)/Products(1)

# 导航+属性
curl http://localhost:8080/DemoService.svc/Products(1)/Category/Name
```

### 错误情况测试
```bash
# 源实体不存在
curl http://localhost:8080/DemoService.svc/Products(999)/Category
# 预期：404 Not Found

# 目标实体不存在
curl http://localhost:8080/DemoService.svc/Products(1)/Category/InvalidProperty
# 预期：404 Not Found

# 复杂导航不支持
curl http://localhost:8080/DemoService.svc/Products(1)/Category/Products(1)/Category
# 预期：501 Not Implemented
```

## 性能考虑

### 1. 懒加载 vs 预加载
```java
// 懒加载实现（按需查询）
public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType) {
    // 每次导航时才查询关联数据
    return queryRelatedEntity(entity, relatedEntityType);
}

// 预加载实现（可选）
public Entity getEntityWithNavigation(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, 
                                     List<String> expandPaths) {
    Entity entity = readEntityData(edmEntitySet, keyParams);
    if (entity != null && expandPaths != null) {
        // 预加载指定的导航属性
        for (String expandPath : expandPaths) {
            loadNavigationProperty(entity, expandPath);
        }
    }
    return entity;
}
```

### 2. 循环引用处理
```java
// 防止循环引用的序列化选项
EntitySerializerOptions options = EntitySerializerOptions.with()
    .contextURL(contextUrl)
    .expand(expandInfo)  // 控制展开深度
    .build();
```

## 总结

`p4_navigation`教程实现了OData导航的核心功能：

### 新增能力
- ✅ **实体关系定义**：Product-Category多对一/一对多关系
- ✅ **导航属性支持**：双向导航属性定义
- ✅ **导航查询**：通过导航属性访问关联实体
- ✅ **导航集合查询**：获取关联实体集合
- ✅ **复合导航**：导航+属性访问

### 技术亮点
- **URI解析增强**：支持多段URI路径解析
- **关系数据处理**：实现了外键关联查询
- **导航绑定**：正确配置了导航属性绑定
- **错误处理完善**：添加了导航相关的错误处理

### 架构价值
- **数据关联**：实现了实体间的关系建模
- **查询能力**：提供了关联数据查询功能
- **URL语义**：遵循OData导航URL约定
- **扩展基础**：为查询选项中的$expand功能奠定基础

这为后续的查询选项（$expand, $select等）和更复杂的数据访问模式提供了必要的基础设施。
