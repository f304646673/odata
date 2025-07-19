# OData Tutorial 05-08 - 查询选项 (Query Options)

## 概览

教程p5-p8专门讲解OData查询选项的实现，这些查询选项是OData协议的核心功能，为客户端提供了强大的数据查询和过滤能力。查询选项通过URL参数的形式提供，允许客户端精确控制返回的数据。

## 查询选项分类

| 教程 | 主要查询选项 | 适用范围 | 功能描述 |
|------|-------------|----------|----------|
| **p5_queryoptions-tcs** | $top, $count, $skip | EntityCollection | 分页和计数 |
| **p6_queryoptions-es** | $orderby | EntityCollection | 排序 |
| **p7_queryoptions-o** | $orderby | EntityCollection | 高级排序 |
| **p8_queryoptions-f** | $filter | EntityCollection | 数据过滤 |

## 核心架构

### 查询选项处理架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                 查询选项增强OData服务架构                          │
├─────────────────────────────────────────────────────────────────┤
│                     Query Parameters                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ ?$top=10        │  │ ?$orderby=Name  │  │ ?$filter=Price  │ │
│  │ &$skip=5        │  │ &$count=true    │  │    gt 100       │ │
│  │ &$count=true    │  │                 │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   URI Processing Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ UriInfo         │  │ Query Options   │  │ Expression      │ │
│  │ getTopOption()  │  │ Parsing         │  │ Parsing         │ │
│  │ getSkipOption() │  │ getOrderByOption│  │ getFilterOption │ │
│  │ getCountOption()│  │                 │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│              EntityCollectionProcessor Layer                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │            DemoEntityCollectionProcessor                    │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │ Pagination  │ │   Sorting   │ │  Filtering  │           │ │
│  │  │ Processing  │ │ Processing  │ │ Processing  │           │ │
│  │  │ $top/$skip  │ │ $orderby    │ │ $filter     │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Expression Evaluation                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ Filter          │  │ OrderBy         │  │ Pagination      │ │
│  │ Expression      │  │ Expression      │  │ Logic           │ │
│  │ Evaluation      │  │ Evaluation      │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Layer                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                      Storage                                │ │
│  │  ┌─────────────┐                                            │ │
│  │  │ EntityList  │ ──► 应用查询选项后的数据处理                  │ │
│  │  │ Processing  │                                            │ │
│  │  └─────────────┘                                            │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## P5: $top, $count, $skip 分页选项

### 基础分页查询选项

#### URL示例
```http
# 分页查询
GET /Products?$top=5&$skip=10

# 带计数的分页查询
GET /Products?$top=5&$skip=10&$count=true

# 仅计数查询
GET /Products?$count=true
```

#### 处理器实现
```java
@Override
public void readEntityCollection(ODataRequest request, ODataResponse response,
                                UriInfo uriInfo, ContentType responseFormat)
        throws ODataApplicationException, SerializerException {
    
    // 1. 获取基础数据
    EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
    EntityCollection entityCollection = storage.readEntitySetData(edmEntitySet);
    
    // 2. 处理查询选项
    EntityCollection processedCollection = applyQueryOptions(entityCollection, uriInfo);
    
    // 3. 序列化响应
    serializeEntityCollection(processedCollection, edmEntitySet, uriInfo, response, responseFormat);
}

private EntityCollection applyQueryOptions(EntityCollection entityCollection, UriInfo uriInfo) {
    List<Entity> entityList = entityCollection.getEntities();
    
    // 处理 $skip
    SkipOption skipOption = uriInfo.getSkipOption();
    if (skipOption != null) {
        int skipNumber = skipOption.getValue();
        if (skipNumber >= 0) {
            if (skipNumber <= entityList.size()) {
                entityList = entityList.subList(skipNumber, entityList.size());
            } else {
                entityList.clear(); // skip超过总数，返回空列表
            }
        } else {
            throw new ODataApplicationException("Invalid value for $skip", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ROOT);
        }
    }
    
    // 处理 $top
    TopOption topOption = uriInfo.getTopOption();
    if (topOption != null) {
        int topNumber = topOption.getValue();
        if (topNumber >= 0) {
            if (topNumber <= entityList.size()) {
                entityList = entityList.subList(0, topNumber);
            }
            // 如果top大于剩余数量，返回所有剩余的
        } else {
            throw new ODataApplicationException("Invalid value for $top", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ROOT);
        }
    }
    
    // 创建处理后的集合
    EntityCollection resultCollection = new EntityCollection();
    resultCollection.getEntities().addAll(entityList);
    
    return resultCollection;
}
```

#### 计数处理
```java
private void serializeEntityCollection(EntityCollection entityCollection, EdmEntitySet edmEntitySet,
                                      UriInfo uriInfo, ODataResponse response, ContentType responseFormat)
        throws SerializerException {
    
    // 检查是否需要计数
    CountOption countOption = uriInfo.getCountOption();
    boolean isCount = countOption != null && countOption.getValue();
    
    // 获取原始总数（在应用$top/$skip之前）
    EntityCollection originalCollection = storage.readEntitySetData(edmEntitySet);
    Integer count = isCount ? originalCollection.getEntities().size() : null;
    
    // 构建序列化选项
    EntityCollectionSerializerOptions.Builder optionsBuilder = EntityCollectionSerializerOptions.with()
        .contextURL(ContextURL.with().entitySet(edmEntitySet).build());
    
    if (isCount) {
        optionsBuilder.count(count);
    }
    
    EntityCollectionSerializerOptions options = optionsBuilder.build();
    
    // 序列化
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, 
                                                                   edmEntitySet.getEntityType(), 
                                                                   entityCollection, options);
    
    // 设置响应
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
}
```

#### 分页响应示例
```json
{
    "@odata.context": "$metadata#Products",
    "@odata.count": 25,
    "value": [
        {
            "ID": 11,
            "Name": "Product 11",
            "Description": "Description 11"
        },
        {
            "ID": 12,
            "Name": "Product 12", 
            "Description": "Description 12"
        }
    ]
}
```

## P6-P7: $orderby 排序选项

### 排序查询选项

#### URL示例
```http
# 单字段升序排序
GET /Products?$orderby=Name

# 单字段降序排序
GET /Products?$orderby=Name desc

# 多字段排序
GET /Products?$orderby=CategoryID,Name desc

# 复合查询
GET /Products?$orderby=Name&$top=5&$count=true
```

#### 排序处理实现
```java
private EntityCollection applyOrderBy(EntityCollection entityCollection, UriInfo uriInfo) 
        throws ODataApplicationException {
    
    OrderByOption orderByOption = uriInfo.getOrderByOption();
    if (orderByOption != null) {
        List<Entity> entityList = entityCollection.getEntities();
        
        // 获取排序表达式列表
        List<OrderByItem> orderItemList = orderByOption.getOrders();
        
        // 应用排序
        entityList.sort(new EntityComparator(orderItemList));
        
        EntityCollection sortedCollection = new EntityCollection();
        sortedCollection.getEntities().addAll(entityList);
        return sortedCollection;
    }
    
    return entityCollection;
}

// 实体比较器实现
private static class EntityComparator implements Comparator<Entity> {
    private List<OrderByItem> orderByItems;
    
    public EntityComparator(List<OrderByItem> orderByItems) {
        this.orderByItems = orderByItems;
    }
    
    @Override
    public int compare(Entity entity1, Entity entity2) {
        int result = 0;
        
        // 按顺序应用每个排序条件
        for (OrderByItem orderByItem : orderByItems) {
            try {
                result = compareByProperty(entity1, entity2, orderByItem);
                if (result != 0) {
                    break; // 如果当前条件能区分，则不需要检查后续条件
                }
            } catch (ODataApplicationException e) {
                throw new RuntimeException(e);
            }
        }
        
        return result;
    }
    
    private int compareByProperty(Entity entity1, Entity entity2, OrderByItem orderByItem) 
            throws ODataApplicationException {
        
        // 获取排序表达式
        Expression expression = orderByItem.getExpression();
        if (expression instanceof Member) {
            Member member = (Member) expression;
            UriResourceProperty uriResourceProperty = (UriResourceProperty) member.getResourcePath().getUriResourceParts().get(0);
            String propertyName = uriResourceProperty.getProperty().getName();
            
            // 获取属性值
            Object value1 = getPropertyValue(entity1, propertyName);
            Object value2 = getPropertyValue(entity2, propertyName);
            
            // 处理null值
            if (value1 == null && value2 == null) {
                return 0;
            } else if (value1 == null) {
                return -1;
            } else if (value2 == null) {
                return 1;
            }
            
            // 比较非null值
            int result = compareValues(value1, value2);
            
            // 处理降序
            if (orderByItem.isDescending()) {
                result = -result;
            }
            
            return result;
        }
        
        throw new ODataApplicationException("Unsupported expression in $orderby", 
                                          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), 
                                          Locale.ROOT);
    }
    
    @SuppressWarnings("unchecked")
    private int compareValues(Object value1, Object value2) throws ODataApplicationException {
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            try {
                return ((Comparable<Object>) value1).compareTo(value2);
            } catch (ClassCastException e) {
                throw new ODataApplicationException("Cannot compare values of different types", 
                                                  HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                                  Locale.ROOT);
            }
        } else {
            throw new ODataApplicationException("Property values are not comparable", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ROOT);
        }
    }
    
    private Object getPropertyValue(Entity entity, String propertyName) {
        Property property = entity.getProperty(propertyName);
        return property != null ? property.getValue() : null;
    }
}
```

## P8: $filter 过滤选项

### 过滤查询选项

#### URL示例
```http
# 数值比较
GET /Products?$filter=Price gt 100

# 字符串比较
GET /Products?$filter=startswith(Name,'Note')

# 逻辑运算
GET /Products?$filter=Price gt 100 and CategoryID eq 1

# 复合查询
GET /Products?$filter=Price gt 100&$orderby=Name&$top=5
```

#### 过滤处理实现
```java
private EntityCollection applyFilter(EntityCollection entityCollection, UriInfo uriInfo) 
        throws ODataApplicationException {
    
    FilterOption filterOption = uriInfo.getFilterOption();
    if (filterOption != null) {
        List<Entity> filteredEntities = new ArrayList<>();
        
        // 获取过滤表达式
        Expression filterExpression = filterOption.getExpression();
        
        // 对每个实体应用过滤条件
        for (Entity entity : entityCollection.getEntities()) {
            try {
                Object result = evaluateExpression(filterExpression, entity);
                if (result instanceof Boolean && (Boolean) result) {
                    filteredEntities.add(entity);
                }
            } catch (Exception e) {
                throw new ODataApplicationException("Error evaluating filter expression", 
                                                  HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
                                                  Locale.ROOT);
            }
        }
        
        EntityCollection filteredCollection = new EntityCollection();
        filteredCollection.getEntities().addAll(filteredEntities);
        return filteredCollection;
    }
    
    return entityCollection;
}

// 表达式求值器
private Object evaluateExpression(Expression expression, Entity entity) throws ODataApplicationException {
    
    if (expression instanceof BinaryOperatorExpression) {
        return evaluateBinaryExpression((BinaryOperatorExpression) expression, entity);
    } else if (expression instanceof Member) {
        return evaluateMemberExpression((Member) expression, entity);
    } else if (expression instanceof Literal) {
        return evaluateLiteralExpression((Literal) expression);
    } else if (expression instanceof MethodCallExpression) {
        return evaluateMethodCallExpression((MethodCallExpression) expression, entity);
    }
    
    throw new ODataApplicationException("Unsupported expression type: " + expression.getClass().getSimpleName(), 
                                      HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), 
                                      Locale.ROOT);
}

private Object evaluateBinaryExpression(BinaryOperatorExpression binaryExpression, Entity entity) 
        throws ODataApplicationException {
    
    Object left = evaluateExpression(binaryExpression.getLeftOperand(), entity);
    Object right = evaluateExpression(binaryExpression.getRightOperand(), entity);
    
    BinaryOperatorKind operator = binaryExpression.getOperator();
    
    switch (operator) {
        case EQ:
            return Objects.equals(left, right);
        case NE:
            return !Objects.equals(left, right);
        case GT:
            return compareValues(left, right) > 0;
        case GE:
            return compareValues(left, right) >= 0;
        case LT:
            return compareValues(left, right) < 0;
        case LE:
            return compareValues(left, right) <= 0;
        case AND:
            return (Boolean) left && (Boolean) right;
        case OR:
            return (Boolean) left || (Boolean) right;
        default:
            throw new ODataApplicationException("Unsupported binary operator: " + operator, 
                                              HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), 
                                              Locale.ROOT);
    }
}

private Object evaluateMethodCallExpression(MethodCallExpression methodCall, Entity entity) 
        throws ODataApplicationException {
    
    MethodKind methodKind = methodCall.getMethod();
    List<Expression> parameters = methodCall.getParameters();
    
    switch (methodKind) {
        case STARTSWITH:
            if (parameters.size() == 2) {
                String str = (String) evaluateExpression(parameters.get(0), entity);
                String prefix = (String) evaluateExpression(parameters.get(1), entity);
                return str != null && str.startsWith(prefix);
            }
            break;
        case ENDSWITH:
            if (parameters.size() == 2) {
                String str = (String) evaluateExpression(parameters.get(0), entity);
                String suffix = (String) evaluateExpression(parameters.get(1), entity);
                return str != null && str.endsWith(suffix);
            }
            break;
        case CONTAINS:
            if (parameters.size() == 2) {
                String str = (String) evaluateExpression(parameters.get(0), entity);
                String substr = (String) evaluateExpression(parameters.get(1), entity);
                return str != null && str.contains(substr);
            }
            break;
        case LENGTH:
            if (parameters.size() == 1) {
                String str = (String) evaluateExpression(parameters.get(0), entity);
                return str != null ? str.length() : null;
            }
            break;
        default:
            throw new ODataApplicationException("Unsupported method: " + methodKind, 
                                              HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), 
                                              Locale.ROOT);
    }
    
    throw new ODataApplicationException("Invalid parameters for method: " + methodKind, 
                                      HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                      Locale.ROOT);
}

@SuppressWarnings("unchecked")
private int compareValues(Object left, Object right) throws ODataApplicationException {
    if (left == null && right == null) {
        return 0;
    } else if (left == null) {
        return -1;
    } else if (right == null) {
        return 1;
    }
    
    if (left instanceof Comparable && right instanceof Comparable) {
        try {
            return ((Comparable<Object>) left).compareTo(right);
        } catch (ClassCastException e) {
            throw new ODataApplicationException("Cannot compare values of different types", 
                                              HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                              Locale.ROOT);
        }
    }
    
    throw new ODataApplicationException("Values are not comparable", 
                                      HttpStatusCode.BAD_REQUEST.getStatusCode(), 
                                      Locale.ROOT);
}
```

## 综合查询选项处理

### 完整的查询选项处理流程
```java
private EntityCollection processQueryOptions(EntityCollection entityCollection, UriInfo uriInfo) 
        throws ODataApplicationException {
    
    EntityCollection result = entityCollection;
    
    // 1. 应用过滤 (在分页之前)
    result = applyFilter(result, uriInfo);
    
    // 2. 应用排序 (在分页之前)
    result = applyOrderBy(result, uriInfo);
    
    // 3. 获取计数 (在分页之前，但在过滤和排序之后)
    int totalCount = result.getEntities().size();
    
    // 4. 应用分页
    result = applyPagination(result, uriInfo);
    
    // 5. 设置计数信息
    CountOption countOption = uriInfo.getCountOption();
    if (countOption != null && countOption.getValue()) {
        result.setCount(totalCount);
    }
    
    return result;
}
```

### 查询选项优先级
1. **$filter** - 首先过滤数据
2. **$orderby** - 然后排序数据  
3. **$count** - 计算过滤排序后的总数
4. **$skip** - 跳过指定数量的记录
5. **$top** - 取指定数量的记录

## 支持的查询选项总结

### P5: 分页选项
| 选项 | 语法 | 示例 | 说明 |
|------|------|------|------|
| $top | $top=n | $top=10 | 返回前n条记录 |
| $skip | $skip=n | $skip=20 | 跳过前n条记录 |
| $count | $count=true/false | $count=true | 返回总记录数 |

### P6-P7: 排序选项
| 选项 | 语法 | 示例 | 说明 |
|------|------|------|------|
| $orderby | $orderby=property [asc\|desc] | $orderby=Name desc | 按属性排序 |
| 多字段排序 | $orderby=prop1,prop2 desc | $orderby=CategoryID,Name desc | 多级排序 |

### P8: 过滤选项
| 操作类型 | 语法 | 示例 | 说明 |
|----------|------|------|------|
| 比较运算 | eq, ne, gt, ge, lt, le | Price gt 100 | 数值/字符串比较 |
| 逻辑运算 | and, or, not | Price gt 100 and CategoryID eq 1 | 逻辑组合 |
| 字符串函数 | startswith, endswith, contains | startswith(Name,'Note') | 字符串操作 |

## 测试用例

### 分页测试
```bash
# 基础分页
curl "http://localhost:8080/DemoService.svc/Products?\$top=3&\$skip=1"

# 带计数的分页
curl "http://localhost:8080/DemoService.svc/Products?\$top=3&\$count=true"
```

### 排序测试
```bash
# 单字段排序
curl "http://localhost:8080/DemoService.svc/Products?\$orderby=Name"

# 多字段排序
curl "http://localhost:8080/DemoService.svc/Products?\$orderby=CategoryID,Name%20desc"
```

### 过滤测试
```bash
# 数值过滤
curl "http://localhost:8080/DemoService.svc/Products?\$filter=Price%20gt%20100"

# 字符串过滤
curl "http://localhost:8080/DemoService.svc/Products?\$filter=startswith(Name,'Note')"

# 复合过滤
curl "http://localhost:8080/DemoService.svc/Products?\$filter=Price%20gt%20100%20and%20CategoryID%20eq%201"
```

### 综合查询测试
```bash
# 完整查询
curl "http://localhost:8080/DemoService.svc/Products?\$filter=Price%20gt%2050&\$orderby=Name&\$top=5&\$count=true"
```

## 总结

查询选项教程(p5-p8)实现了OData的核心查询功能：

### 新增能力
- ✅ **分页控制**：$top/$skip实现数据分页
- ✅ **记录计数**：$count返回总记录数
- ✅ **数据排序**：$orderby实现单/多字段排序
- ✅ **数据过滤**：$filter实现复杂条件过滤
- ✅ **查询组合**：多个查询选项的正确组合

### 技术亮点
- **表达式求值**：实现了完整的OData表达式解析和求值
- **类型安全**：正确处理不同数据类型的比较和运算
- **性能优化**：合理的查询选项执行顺序
- **错误处理**：完整的查询选项验证和错误处理

### 架构价值
- **查询能力**：提供了企业级的数据查询功能
- **标准遵循**：严格遵循OData V4查询规范
- **扩展性**：为更复杂的查询功能奠定基础
- **用户体验**：提供了灵活强大的数据访问接口

这些查询选项使OData服务具备了完整的数据查询能力，为构建企业级数据服务提供了必要的功能基础。
