# OData Server 示例项目 (samples/server)

## 概览

`samples/server` 项目是一个完整的 **OData 服务端示例**，演示了如何使用 Apache Olingo 服务端 API 构建传统的基于 Servlet 的 OData 服务。该项目提供了汽车制造商和汽车信息的标准 OData V4 服务，是学习 OData 服务端开发的经典示例。

## 学习目标

- 掌握基于 Servlet 的 OData 服务开发
- 理解 EDM (Entity Data Model) 的程序化定义
- 学会实现标准的 OData 处理器
- 了解会话管理和数据提供器模式

## 核心架构

### OData Server 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                  传统 Servlet OData 服务架构                      │
├─────────────────────────────────────────────────────────────────┤
│                      HTTP Layer                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   HTTP Request  │  │   Servlet       │  │   HTTP Response │ │
│  │                 │  │   Container     │  │                 │ │
│  │ GET /cars.svc/  │  │                 │  │ JSON/XML Data   │ │
│  │ Manufacturers   │  │ CarsServlet     │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    OData Framework                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   OData         │  │   Service       │  │   ODataHttp     │ │
│  │   Instance      │  │   Metadata      │  │   Handler       │ │
│  │                 │  │                 │  │                 │ │
│  │ OData.newInst() │  │ EDM Provider    │  │ Request Router  │ │
│  │                 │  │ Schema Def      │  │ Processor Mgmt  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Business Logic                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   EDM Provider  │  │   Processors    │  │   Data Provider │ │
│  │                 │  │                 │  │                 │ │
│  │ CarsEdmProvider │  │ CarsProcessor   │  │ DataProvider    │ │
│  │ - Entities      │  │ - Read/Write    │  │ - In-Memory     │ │
│  │ - Relationships │  │ - Query Logic   │  │ - CRUD Ops      │ │
│  │ - Schema        │  │ - Serialization │  │ - Session Mgmt  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Model                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                     Car Domain                              │ │
│  │  ┌─────────────┐          ┌─────────────┐                  │ │
│  │  │Manufacturer │ 1     *  │    Car      │                  │ │
│  │  │  - Id       │<-------->│  - Id       │                  │ │
│  │  │  - Name     │          │  - Model    │                  │ │
│  │  │  - Founded  │          │  - ModelYear│                  │ │
│  │  │  - Address  │          │  - Price    │                  │ │
│  │  │             │          │  - Currency │                  │ │
│  │  └─────────────┘          └─────────────┘                  │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. CarsServlet - 主要服务入口

```java
public class CarsServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(CarsServlet.class);

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      // 1. 获取或创建会话数据提供器
      HttpSession session = req.getSession(true);
      DataProvider dataProvider = (DataProvider) session.getAttribute(DataProvider.class.getName());
      if (dataProvider == null) {
        dataProvider = new DataProvider();
        session.setAttribute(DataProvider.class.getName(), dataProvider);
        LOG.info("Created new data provider.");
      }

      // 2. 初始化 OData 框架
      OData odata = OData.newInstance();
      ServiceMetadata edm = odata.createServiceMetadata(new CarsEdmProvider(), new ArrayList<EdmxReference>());
      
      // 3. 创建 OData HTTP 处理器
      ODataHttpHandler handler = odata.createHandler(edm);
      handler.register(new CarsProcessor(dataProvider));
      
      // 4. 处理请求
      handler.process(req, resp);
      
    } catch (RuntimeException e) {
      LOG.error("Server Error", e);
      throw new ServletException(e);
    }
  }
}
```

**架构说明**：
- **会话管理**：每个用户会话维护独立的数据提供器实例
- **框架初始化**：为每个请求创建完整的 OData 处理环境
- **处理器注册**：注册业务逻辑处理器到 OData 框架
- **请求委托**：将 HTTP 请求委托给 OData 框架处理

### 2. CarsEdmProvider - EDM 元数据定义

```java
public class CarsEdmProvider extends CsdlAbstractEdmProvider {
  
  // 实体容器
  public static final String CONTAINER_NAME = "Container";
  public static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
  
  // 实体集
  public static final String ES_CARS_NAME = "Cars";
  public static final String ES_MANUFACTURERS_NAME = "Manufacturers";
  
  // 实体类型
  public static final String ET_CAR_NAME = "Car";
  public static final String ET_MANUFACTURER_NAME = "Manufacturer";
  public static final FullQualifiedName ET_CAR_FQN = new FullQualifiedName(NAMESPACE, ET_CAR_NAME);
  public static final FullQualifiedName ET_MANUFACTURER_FQN = new FullQualifiedName(NAMESPACE, ET_MANUFACTURER_NAME);

  @Override
  public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
    if (ET_CAR_FQN.equals(entityTypeName)) {
      return getCar();
    } else if (ET_MANUFACTURER_FQN.equals(entityTypeName)) {
      return getManufacturer();
    }
    return null;
  }

  private CsdlEntityType getCar() {
    // 定义 Car 实体的属性
    CsdlProperty id = new CsdlProperty().setName("Id")
        .setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName())
        .setNullable(false);
    
    CsdlProperty model = new CsdlProperty().setName("Model")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(false);
    
    CsdlProperty modelYear = new CsdlProperty().setName("ModelYear")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(true);
    
    CsdlProperty price = new CsdlProperty().setName("Price")
        .setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName())
        .setNullable(true);
    
    CsdlProperty currency = new CsdlProperty().setName("Currency")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(true);

    // 定义导航属性
    CsdlNavigationProperty navProp = new CsdlNavigationProperty()
        .setName("Manufacturer")
        .setType(ET_MANUFACTURER_FQN)
        .setNullable(false)
        .setPartner("Cars");

    // 定义主键
    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
    propertyRef.setName("Id");

    // 组装实体类型
    CsdlEntityType entityType = new CsdlEntityType();
    entityType.setName(ET_CAR_NAME);
    entityType.setProperties(Arrays.asList(id, model, modelYear, price, currency));
    entityType.setNavigationProperties(Arrays.asList(navProp));
    entityType.setKey(Arrays.asList(propertyRef));

    return entityType;
  }

  private CsdlEntityType getManufacturer() {
    // 定义 Manufacturer 实体的属性
    CsdlProperty id = new CsdlProperty().setName("Id")
        .setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName())
        .setNullable(false);
    
    CsdlProperty name = new CsdlProperty().setName("Name")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(false);
    
    CsdlProperty founded = new CsdlProperty().setName("Founded")
        .setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName())
        .setNullable(true);

    // 复杂类型属性
    CsdlProperty address = new CsdlProperty().setName("Address")
        .setType(CT_ADDRESS_FQN)
        .setNullable(true);

    // 导航属性
    CsdlNavigationProperty navProp = new CsdlNavigationProperty()
        .setName("Cars")
        .setType(ET_CAR_FQN)
        .setCollection(true)
        .setPartner("Manufacturer");

    // 主键
    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
    propertyRef.setName("Id");

    // 组装实体类型
    CsdlEntityType entityType = new CsdlEntityType();
    entityType.setName(ET_MANUFACTURER_NAME);
    entityType.setProperties(Arrays.asList(id, name, founded, address));
    entityType.setNavigationProperties(Arrays.asList(navProp));
    entityType.setKey(Arrays.asList(propertyRef));

    return entityType;
  }

  @Override
  public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
    if (CT_ADDRESS_FQN.equals(complexTypeName)) {
      return getComplexTypeAddress();
    }
    return null;
  }

  private CsdlComplexType getComplexTypeAddress() {
    CsdlProperty street = new CsdlProperty().setName("Street")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(true);
    
    CsdlProperty city = new CsdlProperty().setName("City")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(true);
    
    CsdlProperty zipCode = new CsdlProperty().setName("ZipCode")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(true);
    
    CsdlProperty country = new CsdlProperty().setName("Country")
        .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        .setNullable(true);

    CsdlComplexType complexType = new CsdlComplexType();
    complexType.setName("Address");
    complexType.setProperties(Arrays.asList(street, city, zipCode, country));

    return complexType;
  }
}
```

**EDM 特性**：
- **实体定义**：完整的实体类型定义，包括属性和类型
- **关系建模**：一对多导航属性定义
- **复杂类型**：地址作为复杂类型的嵌套结构
- **主键约束**：定义实体的唯一标识

### 3. CarsProcessor - 业务逻辑处理器

```java
public class CarsProcessor implements EntityCollectionProcessor, EntityProcessor {

  private OData odata;
  private ServiceMetadata serviceMetadata;
  private DataProvider dataProvider;

  public CarsProcessor(DataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  @Override
  public void init(OData odata, ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  // 处理实体集合请求 (GET /Cars, GET /Manufacturers)
  @Override
  public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 从数据提供器获取数据
    EntityCollection entitySet;
    if (CarsEdmProvider.ES_CARS_NAME.equals(edmEntitySet.getName())) {
      entitySet = dataProvider.readCars();
    } else if (CarsEdmProvider.ES_MANUFACTURERS_NAME.equals(edmEntitySet.getName())) {
      entitySet = dataProvider.readManufacturers();
    } else {
      throw new ODataApplicationException("Unknown entity set", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }

    // 序列化响应
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    
    EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
        .contextURL(ContextURL.with()
                   .entitySet(edmEntitySet)
                   .build())
        .build();
    
    SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntitySet.getEntityType(), entitySet, opts);
    InputStream serializedContent = serializerResult.getContent();

    // 设置响应
    response.setContent(serializedContent);
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }

  // 处理单个实体请求 (GET /Cars(1), GET /Manufacturers(1))
  @Override
  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 提取键值
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
    Entity entity = dataProvider.read(edmEntitySet, keyPredicates);

    if (entity == null) {
      throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }

    // 序列化响应
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    
    EntitySerializerOptions opts = EntitySerializerOptions.with()
        .contextURL(ContextURL.with()
                   .entitySet(edmEntitySet)
                   .suffix(Suffix.ENTITY)
                   .build())
        .build();
    
    SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), entity, opts);
    InputStream serializedContent = serializerResult.getContent();

    // 设置响应
    response.setContent(serializedContent);
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }

  // 其他处理方法...
  @Override
  public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) {
    // 创建实体的实现
  }

  @Override
  public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) {
    // 更新实体的实现
  }

  @Override
  public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) {
    // 删除实体的实现
  }
}
```

**处理器特性**：
- **多接口实现**：同时实现集合和单实体处理器
- **动态路由**：根据请求的实体集动态处理
- **标准序列化**：使用 OData 框架的序列化器
- **错误处理**：标准的 HTTP 状态码和错误响应

### 4. DataProvider - 数据访问层

```java
public class DataProvider {

  private Map<String, EntityCollection> data;

  public DataProvider() {
    data = new HashMap<String, EntityCollection>();
    initializeSampleData();
  }

  private void initializeSampleData() {
    // 初始化制造商数据
    EntityCollection manufacturerCollection = new EntityCollection();
    
    Entity manufacturer1 = new Entity()
        .addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 1))
        .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "BMW"))
        .addProperty(new Property(null, "Founded", ValueType.PRIMITIVE, getCalendar(1916, 2, 7)));
    
    // 添加复杂类型属性
    ComplexValue addressValue = new ComplexValue();
    addressValue.getValue().add(new Property(null, "Street", ValueType.PRIMITIVE, "Petuelring 130"));
    addressValue.getValue().add(new Property(null, "City", ValueType.PRIMITIVE, "Munich"));
    addressValue.getValue().add(new Property(null, "ZipCode", ValueType.PRIMITIVE, "80809"));
    addressValue.getValue().add(new Property(null, "Country", ValueType.PRIMITIVE, "Germany"));
    manufacturer1.addProperty(new Property(null, "Address", ValueType.COMPLEX, addressValue));
    
    manufacturerCollection.getEntities().add(manufacturer1);

    // 类似地添加更多制造商...
    
    data.put(CarsEdmProvider.ES_MANUFACTURERS_NAME, manufacturerCollection);

    // 初始化汽车数据
    EntityCollection carCollection = new EntityCollection();
    
    Entity car1 = new Entity()
        .addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 1))
        .addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "F30"))
        .addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, "2012"))
        .addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 31200.0))
        .addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "EUR"));
    
    carCollection.getEntities().add(car1);
    
    // 类似地添加更多汽车...
    
    data.put(CarsEdmProvider.ES_CARS_NAME, carCollection);
  }

  public EntityCollection readCars() {
    return data.get(CarsEdmProvider.ES_CARS_NAME);
  }

  public EntityCollection readManufacturers() {
    return data.get(CarsEdmProvider.ES_MANUFACTURERS_NAME);
  }

  public Entity read(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) throws ODataApplicationException {
    
    String entitySetName = edmEntitySet.getName();
    EntityCollection entitySet;
    
    if (CarsEdmProvider.ES_CARS_NAME.equals(entitySetName)) {
      entitySet = readCars();
    } else if (CarsEdmProvider.ES_MANUFACTURERS_NAME.equals(entitySetName)) {
      entitySet = readManufacturers();
    } else {
      return null;
    }

    // 根据键值查找实体
    Entity requestedEntity = Util.findEntity(edmEntitySet.getEntityType(), entitySet, keyParams);
    
    if (requestedEntity == null) {
      throw new ODataApplicationException("Entity for requested key doesn't exist",
          HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }

    return requestedEntity;
  }

  // CRUD 操作方法
  public Entity createCar(Entity entity) throws ODataApplicationException {
    // 实现汽车创建逻辑
    EntityCollection carCollection = readCars();
    
    // 生成新的 ID
    int newId = getNextId(carCollection);
    entity.getProperties().add(new Property(null, "Id", ValueType.PRIMITIVE, newId));
    
    carCollection.getEntities().add(entity);
    return entity;
  }

  public Entity updateCar(List<UriParameter> keyParams, Entity updateEntity) throws ODataApplicationException {
    // 实现汽车更新逻辑
    Entity existingEntity = findCar(keyParams);
    if (existingEntity == null) {
      throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }
    
    // 更新属性
    for (Property updateProperty : updateEntity.getProperties()) {
      existingEntity.getProperties().removeIf(p -> p.getName().equals(updateProperty.getName()));
      existingEntity.getProperties().add(updateProperty);
    }
    
    return existingEntity;
  }

  public void deleteCar(List<UriParameter> keyParams) throws ODataApplicationException {
    // 实现汽车删除逻辑
    Entity existingEntity = findCar(keyParams);
    if (existingEntity == null) {
      throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }
    
    EntityCollection carCollection = readCars();
    carCollection.getEntities().remove(existingEntity);
  }

  private Entity findCar(List<UriParameter> keyParams) {
    EntityCollection carCollection = readCars();
    for (Entity entity : carCollection.getEntities()) {
      boolean matches = true;
      for (UriParameter keyParam : keyParams) {
        Property property = entity.getProperty(keyParam.getName());
        if (property == null || !property.getValue().toString().equals(keyParam.getText())) {
          matches = false;
          break;
        }
      }
      if (matches) {
        return entity;
      }
    }
    return null;
  }

  private int getNextId(EntityCollection collection) {
    int maxId = 0;
    for (Entity entity : collection.getEntities()) {
      Property idProperty = entity.getProperty("Id");
      if (idProperty != null && idProperty.getValue() instanceof Integer) {
        maxId = Math.max(maxId, (Integer) idProperty.getValue());
      }
    }
    return maxId + 1;
  }

  private static Calendar getCalendar(int year, int month, int day) {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(year, month, day);
    return cal;
  }
}
```

**数据提供器特性**：
- **内存存储**：使用 Map 和 EntityCollection 进行内存数据管理
- **CRUD 支持**：完整的创建、读取、更新、删除操作
- **键值查找**：基于 OData 键值参数的实体查找
- **数据初始化**：预填充示例数据

## Web.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee 
         http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd" version="3.0">

  <display-name>Apache Olingo OData Server Sample</display-name>

  <servlet>
    <servlet-name>CarsServlet</servlet-name>
    <servlet-class>org.apache.olingo.server.sample.CarsServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <servlet-mapping>
    <servlet-name>CarsServlet</servlet-name>
    <url-pattern>/cars.svc/*</url-pattern>
  </servlet-mapping>

</web-app>
```

## API 使用示例

### 1. 获取服务元数据

```bash
# 获取服务根
GET http://localhost:8080/cars.svc/

# 获取元数据
GET http://localhost:8080/cars.svc/$metadata

# 预期响应：完整的 CSDL XML 元数据
```

### 2. 实体集合操作

```bash
# 获取所有制造商
GET http://localhost:8080/cars.svc/Manufacturers

# 预期响应
{
  "@odata.context": "$metadata#Manufacturers",
  "value": [
    {
      "Id": 1,
      "Name": "BMW",
      "Founded": "1916-03-07",
      "Address": {
        "Street": "Petuelring 130",
        "City": "Munich",
        "ZipCode": "80809",
        "Country": "Germany"
      }
    }
  ]
}

# 获取所有汽车
GET http://localhost:8080/cars.svc/Cars
```

### 3. 单个实体操作

```bash
# 获取特定制造商
GET http://localhost:8080/cars.svc/Manufacturers(1)

# 获取特定汽车
GET http://localhost:8080/cars.svc/Cars(1)

# 创建新汽车
POST http://localhost:8080/cars.svc/Cars
Content-Type: application/json

{
  "Model": "X5",
  "ModelYear": "2023",
  "Price": 75000.0,
  "Currency": "EUR"
}

# 更新汽车
PUT http://localhost:8080/cars.svc/Cars(1)
Content-Type: application/json

{
  "Model": "X5 Updated",
  "ModelYear": "2023",
  "Price": 80000.0,
  "Currency": "EUR"
}

# 删除汽车
DELETE http://localhost:8080/cars.svc/Cars(1)
```

### 4. 导航属性

```bash
# 获取制造商的所有汽车
GET http://localhost:8080/cars.svc/Manufacturers(1)/Cars

# 获取汽车的制造商
GET http://localhost:8080/cars.svc/Cars(1)/Manufacturer

# 使用 $expand 扩展查询
GET http://localhost:8080/cars.svc/Manufacturers?$expand=Cars
```

### 5. 查询选项

```bash
# 过滤查询
GET http://localhost:8080/cars.svc/Cars?$filter=Price gt 30000

# 排序查询
GET http://localhost:8080/cars.svc/Cars?$orderby=Price desc

# 分页查询
GET http://localhost:8080/cars.svc/Cars?$top=10&$skip=20

# 选择特定字段
GET http://localhost:8080/cars.svc/Cars?$select=Model,Price

# 计数查询
GET http://localhost:8080/cars.svc/Cars?$count=true
```

## 部署和运行

### 1. Maven 构建

```bash
# 清理并编译
mvn clean compile

# 打包为 WAR 文件
mvn package

# 生成的 WAR 文件位于 target/ 目录
```

### 2. Servlet 容器部署

```bash
# 部署到 Tomcat
cp target/odata-server-sample.war $TOMCAT_HOME/webapps/

# 启动 Tomcat
$TOMCAT_HOME/bin/startup.sh

# 服务访问地址
http://localhost:8080/odata-server-sample/cars.svc/
```

### 3. 开发模式运行

```bash
# 使用 Maven Jetty 插件
mvn jetty:run

# 或使用 Tomcat 插件
mvn tomcat7:run

# 本地开发访问地址
http://localhost:8080/cars.svc/
```

## 会话管理特性

### 1. 每用户独立数据

```java
// 每个 HTTP 会话维护独立的数据实例
HttpSession session = req.getSession(true);
DataProvider dataProvider = (DataProvider) session.getAttribute(DataProvider.class.getName());
if (dataProvider == null) {
  dataProvider = new DataProvider();
  session.setAttribute(DataProvider.class.getName(), dataProvider);
}
```

**优势**：
- **数据隔离**：不同用户的操作互不影响
- **状态保持**：用户的数据修改在会话期间保持
- **简单实现**：无需复杂的多用户数据管理

### 2. 会话数据持久化

```java
// 可以扩展为将会话数据持久化到数据库
public class PersistentDataProvider extends DataProvider {
  
  @Override
  public Entity createCar(Entity entity) throws ODataApplicationException {
    Entity newCar = super.createCar(entity);
    
    // 持久化到数据库
    persistToDatabase(newCar);
    
    return newCar;
  }
  
  private void persistToDatabase(Entity entity) {
    // 数据库持久化逻辑
  }
}
```

## 扩展和定制

### 1. 添加新的实体类型

```java
// 在 CarsEdmProvider 中添加新实体
public static final String ET_DEALER_NAME = "Dealer";
public static final FullQualifiedName ET_DEALER_FQN = new FullQualifiedName(NAMESPACE, ET_DEALER_NAME);

private CsdlEntityType getDealer() {
  CsdlProperty id = new CsdlProperty().setName("Id")
      .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
      .setNullable(false);
  
  CsdlProperty name = new CsdlProperty().setName("Name")
      .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
      .setNullable(false);
  
  // 更多属性...
  
  CsdlEntityType entityType = new CsdlEntityType();
  entityType.setName(ET_DEALER_NAME);
  entityType.setProperties(Arrays.asList(id, name));
  entityType.setKey(Arrays.asList(new CsdlPropertyRef().setName("Id")));
  
  return entityType;
}
```

### 2. 添加自定义操作

```java
// 添加函数和操作
@Override
public CsdlFunction getFunction(FullQualifiedName functionName) {
  if (FUNCTION_COUNT_CARS.equals(functionName)) {
    return getFunctionCountCars();
  }
  return null;
}

private CsdlFunction getFunctionCountCars() {
  CsdlFunction function = new CsdlFunction();
  function.setName("CountCars");
  function.setReturnType(new CsdlReturnType().setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()));
  return function;
}
```

### 3. 高级查询支持

```java
// 在处理器中添加高级查询支持
public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) {
  
  // 获取查询选项
  FilterOption filterOption = uriInfo.getFilterOption();
  OrderByOption orderByOption = uriInfo.getOrderByOption();
  SkipOption skipOption = uriInfo.getSkipOption();
  TopOption topOption = uriInfo.getTopOption();
  
  // 应用查询选项
  EntityCollection entitySet = dataProvider.readCars();
  
  if (filterOption != null) {
    entitySet = applyFilter(entitySet, filterOption);
  }
  
  if (orderByOption != null) {
    entitySet = applyOrderBy(entitySet, orderByOption);
  }
  
  if (skipOption != null || topOption != null) {
    entitySet = applyPaging(entitySet, skipOption, topOption);
  }
  
  // 序列化和响应...
}
```

## 总结

`samples/server` 项目提供了完整的传统 Servlet OData 服务实现：

### 核心特性
- ✅ **标准 Servlet 架构**：基于传统 Java EE Servlet 实现
- ✅ **完整 EDM 定义**：程序化的实体数据模型定义
- ✅ **CRUD 操作支持**：完整的增删改查功能
- ✅ **会话管理**：基于 HTTP 会话的数据隔离
- ✅ **导航属性**：实体间关系的完整支持

### 技术亮点
- **传统架构**：适合传统 Java EE 环境
- **内存数据**：快速原型和演示
- **模块化设计**：清晰的层次分离
- **标准兼容**：完全符合 OData V4 规范

### 适用场景
该服务端示例为开发者提供了构建 OData 服务的经典模式，特别适合：
- **学习 OData**：理解 OData 服务端的核心概念
- **快速原型**：快速构建 OData 服务原型
- **传统环境**：在传统 Java EE 环境中集成 OData
- **教学演示**：作为 OData 教学的标准示例
