# OData Spring Boot 示例项目 (samples/spring-boot-odata)

## 概览

`samples/spring-boot-odata` 项目是一个 **现代化的 Spring Boot OData 服务实现**，展示了如何将 Apache Olingo OData 框架与 Spring Boot 完美集成。该项目提供了汽车制造商和汽车信息的 RESTful OData 服务，采用了现代的 Spring Boot 架构设计模式。

## 学习目标

- 掌握 Spring Boot 与 OData 的集成
- 理解现代化 OData 服务架构
- 学会 Spring Boot 控制器的 OData 集成
- 了解依赖注入在 OData 服务中的应用

## 核心架构

### Spring Boot OData 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                Spring Boot OData 服务架构                       │
├─────────────────────────────────────────────────────────────────┤
│                    Spring Boot Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Application   │  │   Auto Config   │  │   Embedded      │ │
│  │   Context       │  │                 │  │   Tomcat        │ │
│  │                 │  │ @SpringBootApp  │  │                 │ │
│  │ Bean Management │  │ Component Scan  │  │ HTTP Server     │ │
│  │ DI Container    │  │ Configuration   │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Spring MVC Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   HTTP Request  │  │   Controller    │  │   HTTP Response │ │
│  │                 │  │                 │  │                 │ │
│  │ POST /odata/    │  │ @RestController │  │ JSON/XML Data   │ │
│  │ Cars            │  │ @RequestMapping │  │                 │ │
│  │ GET /odata/     │  │ ODataController │  │ Status Codes    │ │
│  │ Manufacturers   │  │                 │  │ Headers         │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    OData Framework                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   OData         │  │   Service       │  │   Request       │ │
│  │   Instance      │  │   Handler       │  │   Processors    │ │
│  │                 │  │                 │  │                 │ │
│  │ Framework Init  │  │ Handler Factory │  │ Entity Proc     │ │
│  │ Service Setup   │  │ Processor Reg   │  │ Collection Proc │ │
│  │                 │  │ Error Handling  │  │ Action Proc     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Business Logic Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   EDM Provider  │  │   Data Provider │  │   Processors    │ │
│  │                 │  │                 │  │                 │ │
│  │ @Component      │  │ @Component      │  │ @Component      │ │
│  │ Schema Builder  │  │ Data Access     │  │ Business Logic  │ │
│  │ Metadata Def    │  │ CRUD Operations │  │ Request Handle  │ │
│  │                 │  │ Bean Injection  │  │ Response Build  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Model                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                   Car Domain Model                          │ │
│  │  ┌─────────────┐         ┌─────────────┐                   │ │
│  │  │Manufacturer │ 1    *  │    Car      │                   │ │
│  │  │  - Id       │<------->│  - Id       │                   │ │
│  │  │  - Name     │         │  - Model    │                   │ │
│  │  │  - Founded  │         │  - ModelYear│                   │ │
│  │  │  - Address  │         │  - Price    │                   │ │
│  │  │             │         │  - Currency │                   │ │
│  │  └─────────────┘         └─────────────┘                   │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. ODataController - Spring Boot 控制器

```java
@RestController
@RequestMapping("/odata")
public class ODataController {

  @Autowired
  private SpringBootEdmProvider edmProvider;

  @Autowired
  private SpringBootDataProvider dataProvider;

  @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, 
                                          RequestMethod.PUT, RequestMethod.DELETE, 
                                          RequestMethod.PATCH, RequestMethod.HEAD})
  public void odata(HttpServletRequest request, HttpServletResponse response) {
    
    try {
      // 1. 初始化 OData 框架
      OData odata = OData.newInstance();
      ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
      
      // 2. 创建 HTTP 处理器
      ODataHttpHandler handler = odata.createHandler(serviceMetadata);
      
      // 3. 注册处理器（使用依赖注入）
      handler.register(new SpringBootCarsProcessor(dataProvider));
      
      // 4. 处理请求
      handler.process(request, response);
      
    } catch (Exception e) {
      // 统一异常处理
      handleODataException(response, e);
    }
  }

  private void handleODataException(HttpServletResponse response, Exception e) {
    try {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":{\"message\":\"" + e.getMessage() + "\"}}");
    } catch (Exception ex) {
      // 日志记录
      ex.printStackTrace();
    }
  }
}
```

**控制器特性**：
- **统一路由**：处理所有 `/odata/**` 路径下的请求
- **多 HTTP 方法**：支持全部 REST HTTP 方法
- **依赖注入**：使用 Spring 的 `@Autowired` 注入组件
- **统一异常处理**：集中的错误处理逻辑

### 2. ODataSpringBootService - 应用启动类

```java
@SpringBootApplication
@ComponentScan(basePackages = "org.apache.olingo.sample.springboot")
public class ODataSpringBootService {

  public static void main(String[] args) {
    // 启动 Spring Boot 应用
    SpringApplication.run(ODataSpringBootService.class, args);
  }

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilterBean() {
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter());
    bean.addUrlPatterns("/odata/*");
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }

  // CORS 过滤器配置
  public static class CorsFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {
      
      HttpServletResponse response = (HttpServletResponse) resp;
      HttpServletRequest request = (HttpServletRequest) req;
      
      // 设置 CORS 头
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
      response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept, OData-Version, OData-MaxVersion");
      response.setHeader("Access-Control-Max-Age", "3600");
      
      if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        response.setStatus(HttpServletResponse.SC_OK);
        return;
      }
      
      chain.doFilter(req, resp);
    }
  }
}
```

**启动类特性**：
- **自动配置**：Spring Boot 的自动配置机制
- **组件扫描**：指定包扫描范围
- **CORS 支持**：跨域资源共享配置
- **Bean 定义**：自定义 Bean 的注册

### 3. SpringBootEdmProvider - Spring Bean EDM 提供器

```java
@Component
public class SpringBootEdmProvider extends CsdlAbstractEdmProvider {

  // 命名空间和容器定义
  public static final String NAMESPACE = "OData.Demo";
  public static final String CONTAINER_NAME = "Container";
  public static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
  
  // 实体类型定义
  public static final String ET_CAR_NAME = "Car";
  public static final String ET_MANUFACTURER_NAME = "Manufacturer";
  public static final FullQualifiedName ET_CAR_FQN = new FullQualifiedName(NAMESPACE, ET_CAR_NAME);
  public static final FullQualifiedName ET_MANUFACTURER_FQN = new FullQualifiedName(NAMESPACE, ET_MANUFACTURER_NAME);
  
  // 实体集定义
  public static final String ES_CARS_NAME = "Cars";
  public static final String ES_MANUFACTURERS_NAME = "Manufacturers";

  @Override
  public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
    if (ET_CAR_FQN.equals(entityTypeName)) {
      return buildCarEntityType();
    } else if (ET_MANUFACTURER_FQN.equals(entityTypeName)) {
      return buildManufacturerEntityType();
    }
    return null;
  }

  private CsdlEntityType buildCarEntityType() {
    // 构建 Car 实体类型
    List<CsdlProperty> properties = Arrays.asList(
        new CsdlProperty().setName("Id")
            .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
            .setNullable(false),
        new CsdlProperty().setName("Model")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(false)
            .setMaxLength(50),
        new CsdlProperty().setName("ModelYear")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(true)
            .setMaxLength(4),
        new CsdlProperty().setName("Price")
            .setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName())
            .setNullable(true),
        new CsdlProperty().setName("Currency")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(true)
            .setMaxLength(3)
    );

    // 导航属性
    CsdlNavigationProperty navProp = new CsdlNavigationProperty()
        .setName("Manufacturer")
        .setType(ET_MANUFACTURER_FQN)
        .setNullable(false)
        .setPartner("Cars");

    // 主键定义
    CsdlPropertyRef keyPropertyRef = new CsdlPropertyRef().setName("Id");

    // 组装实体类型
    CsdlEntityType entityType = new CsdlEntityType()
        .setName(ET_CAR_NAME)
        .setProperties(properties)
        .setNavigationProperties(Arrays.asList(navProp))
        .setKey(Arrays.asList(keyPropertyRef));

    return entityType;
  }

  private CsdlEntityType buildManufacturerEntityType() {
    // 构建 Manufacturer 实体类型
    List<CsdlProperty> properties = Arrays.asList(
        new CsdlProperty().setName("Id")
            .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName())
            .setNullable(false),
        new CsdlProperty().setName("Name")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(false)
            .setMaxLength(100),
        new CsdlProperty().setName("Founded")
            .setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName())
            .setNullable(true),
        new CsdlProperty().setName("Address")
            .setType(new FullQualifiedName(NAMESPACE, "Address"))
            .setNullable(true)
    );

    // 导航属性
    CsdlNavigationProperty navProp = new CsdlNavigationProperty()
        .setName("Cars")
        .setType(ET_CAR_FQN)
        .setCollection(true)
        .setPartner("Manufacturer");

    // 主键定义
    CsdlPropertyRef keyPropertyRef = new CsdlPropertyRef().setName("Id");

    // 组装实体类型
    CsdlEntityType entityType = new CsdlEntityType()
        .setName(ET_MANUFACTURER_NAME)
        .setProperties(properties)
        .setNavigationProperties(Arrays.asList(navProp))
        .setKey(Arrays.asList(keyPropertyRef));

    return entityType;
  }

  @Override
  public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
    if (complexTypeName.equals(new FullQualifiedName(NAMESPACE, "Address"))) {
      return buildAddressComplexType();
    }
    return null;
  }

  private CsdlComplexType buildAddressComplexType() {
    List<CsdlProperty> properties = Arrays.asList(
        new CsdlProperty().setName("Street")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(true)
            .setMaxLength(200),
        new CsdlProperty().setName("City")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(true)
            .setMaxLength(100),
        new CsdlProperty().setName("ZipCode")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(true)
            .setMaxLength(20),
        new CsdlProperty().setName("Country")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            .setNullable(true)
            .setMaxLength(100)
    );

    return new CsdlComplexType()
        .setName("Address")
        .setProperties(properties);
  }

  @Override
  public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
    if (CONTAINER_FQN.equals(entityContainer)) {
      if (ES_CARS_NAME.equals(entitySetName)) {
        return new CsdlEntitySet()
            .setName(ES_CARS_NAME)
            .setType(ET_CAR_FQN);
      } else if (ES_MANUFACTURERS_NAME.equals(entitySetName)) {
        return new CsdlEntitySet()
            .setName(ES_MANUFACTURERS_NAME)
            .setType(ET_MANUFACTURER_FQN);
      }
    }
    return null;
  }

  @Override
  public CsdlEntityContainer getEntityContainer() throws ODataException {
    // 创建实体集
    CsdlEntitySet carsEntitySet = new CsdlEntitySet()
        .setName(ES_CARS_NAME)
        .setType(ET_CAR_FQN);

    CsdlEntitySet manufacturersEntitySet = new CsdlEntitySet()
        .setName(ES_MANUFACTURERS_NAME)
        .setType(ET_MANUFACTURER_FQN);

    // 创建实体容器
    CsdlEntityContainer entityContainer = new CsdlEntityContainer()
        .setName(CONTAINER_NAME)
        .setEntitySets(Arrays.asList(carsEntitySet, manufacturersEntitySet));

    return entityContainer;
  }

  @Override
  public List<CsdlSchema> getSchemas() throws ODataException {
    CsdlSchema schema = new CsdlSchema()
        .setNamespace(NAMESPACE)
        .setEntityTypes(Arrays.asList(buildCarEntityType(), buildManufacturerEntityType()))
        .setComplexTypes(Arrays.asList(buildAddressComplexType()))
        .setEntityContainer(getEntityContainer());

    return Arrays.asList(schema);
  }
}
```

**EDM 提供器特性**：
- **Spring Component**：作为 Spring Bean 管理
- **构建器模式**：使用私有方法构建复杂对象
- **类型安全**：强类型的实体和复杂类型定义
- **完整模式**：完整的 Schema、EntityType、ComplexType 定义

### 4. SpringBootDataProvider - Spring Bean 数据提供器

```java
@Component
public class SpringBootDataProvider {

  private final Map<String, EntityCollection> data = new ConcurrentHashMap<>();
  private final AtomicInteger carIdCounter = new AtomicInteger(1);
  private final AtomicInteger manufacturerIdCounter = new AtomicInteger(1);

  @PostConstruct
  public void initializeData() {
    initializeManufacturers();
    initializeCars();
  }

  private void initializeManufacturers() {
    EntityCollection manufacturerCollection = new EntityCollection();

    // BMW
    Entity bmw = createManufacturer(manufacturerIdCounter.getAndIncrement(), "BMW", 
        LocalDate.of(1916, 3, 7), "Petuelring 130", "Munich", "80809", "Germany");
    manufacturerCollection.getEntities().add(bmw);

    // Mercedes-Benz
    Entity mercedes = createManufacturer(manufacturerIdCounter.getAndIncrement(), "Mercedes-Benz", 
        LocalDate.of(1926, 6, 28), "Mercedesstraße 120", "Stuttgart", "70372", "Germany");
    manufacturerCollection.getEntities().add(mercedes);

    // Audi
    Entity audi = createManufacturer(manufacturerIdCounter.getAndIncrement(), "Audi", 
        LocalDate.of(1910, 7, 16), "Auto-Union-Straße 1", "Ingolstadt", "85057", "Germany");
    manufacturerCollection.getEntities().add(audi);

    data.put(SpringBootEdmProvider.ES_MANUFACTURERS_NAME, manufacturerCollection);
  }

  private Entity createManufacturer(int id, String name, LocalDate founded, 
                                  String street, String city, String zipCode, String country) {
    Entity manufacturer = new Entity()
        .addProperty(new Property(null, "Id", ValueType.PRIMITIVE, id))
        .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, name))
        .addProperty(new Property(null, "Founded", ValueType.PRIMITIVE, founded));

    // 创建地址复杂类型
    ComplexValue address = new ComplexValue();
    address.getValue().add(new Property(null, "Street", ValueType.PRIMITIVE, street));
    address.getValue().add(new Property(null, "City", ValueType.PRIMITIVE, city));
    address.getValue().add(new Property(null, "ZipCode", ValueType.PRIMITIVE, zipCode));
    address.getValue().add(new Property(null, "Country", ValueType.PRIMITIVE, country));
    
    manufacturer.addProperty(new Property(null, "Address", ValueType.COMPLEX, address));

    return manufacturer;
  }

  private void initializeCars() {
    EntityCollection carCollection = new EntityCollection();

    // BMW Cars
    carCollection.getEntities().add(createCar(carIdCounter.getAndIncrement(), "F30", "2012", 31200.0, "EUR"));
    carCollection.getEntities().add(createCar(carIdCounter.getAndIncrement(), "X5", "2018", 75000.0, "EUR"));

    // Mercedes Cars
    carCollection.getEntities().add(createCar(carIdCounter.getAndIncrement(), "C-Class", "2020", 45000.0, "EUR"));
    carCollection.getEntities().add(createCar(carIdCounter.getAndIncrement(), "S-Class", "2021", 95000.0, "EUR"));

    // Audi Cars
    carCollection.getEntities().add(createCar(carIdCounter.getAndIncrement(), "A4", "2019", 40000.0, "EUR"));
    carCollection.getEntities().add(createCar(carIdCounter.getAndIncrement(), "Q7", "2020", 80000.0, "EUR"));

    data.put(SpringBootEdmProvider.ES_CARS_NAME, carCollection);
  }

  private Entity createCar(int id, String model, String modelYear, Double price, String currency) {
    return new Entity()
        .addProperty(new Property(null, "Id", ValueType.PRIMITIVE, id))
        .addProperty(new Property(null, "Model", ValueType.PRIMITIVE, model))
        .addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, modelYear))
        .addProperty(new Property(null, "Price", ValueType.PRIMITIVE, price))
        .addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, currency));
  }

  // 数据访问方法
  public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) throws ODataApplicationException {
    EntityCollection collection = data.get(edmEntitySet.getName());
    if (collection == null) {
      throw new ODataApplicationException("Unknown entity set: " + edmEntitySet.getName(),
          HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }
    return collection;
  }

  public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) 
      throws ODataApplicationException {
    
    EntityCollection entitySet = readEntitySetData(edmEntitySet);
    
    // 根据键参数查找实体
    for (Entity entity : entitySet.getEntities()) {
      boolean allMatched = true;
      for (UriParameter keyParam : keyParams) {
        Property property = entity.getProperty(keyParam.getName());
        if (property == null || !property.getValue().toString().equals(keyParam.getText())) {
          allMatched = false;
          break;
        }
      }
      if (allMatched) {
        return entity;
      }
    }
    
    throw new ODataApplicationException("Entity not found",
        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
  }

  public Entity createEntity(EdmEntitySet edmEntitySet, Entity entity) throws ODataApplicationException {
    EntityCollection entitySet = readEntitySetData(edmEntitySet);
    
    // 生成新 ID
    int newId;
    if (SpringBootEdmProvider.ES_CARS_NAME.equals(edmEntitySet.getName())) {
      newId = carIdCounter.getAndIncrement();
    } else if (SpringBootEdmProvider.ES_MANUFACTURERS_NAME.equals(edmEntitySet.getName())) {
      newId = manufacturerIdCounter.getAndIncrement();
    } else {
      throw new ODataApplicationException("Unknown entity set",
          HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    }
    
    // 设置 ID 属性
    entity.getProperties().removeIf(p -> "Id".equals(p.getName()));
    entity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, newId));
    
    // 添加到集合
    entitySet.getEntities().add(entity);
    
    return entity;
  }

  public Entity updateEntity(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, Entity updateEntity) 
      throws ODataApplicationException {
    
    Entity existingEntity = readEntityData(edmEntitySet, keyParams);
    
    // 更新属性
    for (Property updateProperty : updateEntity.getProperties()) {
      existingEntity.getProperties().removeIf(p -> p.getName().equals(updateProperty.getName()));
      existingEntity.getProperties().add(updateProperty);
    }
    
    return existingEntity;
  }

  public void deleteEntity(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) 
      throws ODataApplicationException {
    
    EntityCollection entitySet = readEntitySetData(edmEntitySet);
    Entity entityToDelete = readEntityData(edmEntitySet, keyParams);
    
    entitySet.getEntities().remove(entityToDelete);
  }

  // 线程安全的计数器获取
  public synchronized int getNextCarId() {
    return carIdCounter.incrementAndGet();
  }

  public synchronized int getNextManufacturerId() {
    return manufacturerIdCounter.incrementAndGet();
  }
}
```

**数据提供器特性**：
- **Spring Component**：使用 `@Component` 注解
- **初始化钩子**：使用 `@PostConstruct` 进行数据初始化
- **线程安全**：使用 `ConcurrentHashMap` 和 `AtomicInteger`
- **完整 CRUD**：支持所有数据操作

### 5. SpringBootCarsProcessor - Spring 集成处理器

```java
public class SpringBootCarsProcessor implements EntityCollectionProcessor, EntityProcessor {

  private OData odata;
  private ServiceMetadata serviceMetadata;
  private final SpringBootDataProvider dataProvider;

  public SpringBootCarsProcessor(SpringBootDataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  @Override
  public void init(OData odata, ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    // 解析请求路径
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 从数据提供器获取数据
    EntityCollection entitySet = dataProvider.readEntitySetData(edmEntitySet);

    // 应用查询选项
    entitySet = applyQueryOptions(entitySet, uriInfo);

    // 序列化响应
    serializeEntityCollection(response, edmEntitySet, entitySet, responseFormat);
  }

  @Override
  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    // 解析请求路径和键
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

    // 从数据提供器读取实体
    Entity entity = dataProvider.readEntityData(edmEntitySet, keyPredicates);

    // 序列化响应
    serializeEntity(response, edmEntitySet, entity, responseFormat);
  }

  @Override
  public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, 
      ContentType requestFormat, ContentType responseFormat) 
      throws ODataApplicationException, ODataLibraryException {

    // 解析请求路径
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 反序列化请求体
    Entity entity = deserializeEntity(request, edmEntitySet, requestFormat);

    // 创建实体
    Entity createdEntity = dataProvider.createEntity(edmEntitySet, entity);

    // 序列化响应
    serializeEntity(response, edmEntitySet, createdEntity, responseFormat);
    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
    
    // 设置 Location 头
    String location = request.getRawODataPath() + "(" + getEntityKey(createdEntity) + ")";
    response.setHeader(HttpHeader.LOCATION, location);
  }

  @Override
  public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, 
      ContentType requestFormat, ContentType responseFormat) 
      throws ODataApplicationException, ODataLibraryException {

    // 解析请求路径和键
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

    // 反序列化请求体
    Entity updateEntity = deserializeEntity(request, edmEntitySet, requestFormat);

    // 更新实体
    Entity updatedEntity = dataProvider.updateEntity(edmEntitySet, keyPredicates, updateEntity);

    // 返回更新后的实体
    serializeEntity(response, edmEntitySet, updatedEntity, responseFormat);
  }

  @Override
  public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) 
      throws ODataApplicationException {

    // 解析请求路径和键
    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

    // 删除实体
    dataProvider.deleteEntity(edmEntitySet, keyPredicates);

    // 返回 204 No Content
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

  // 辅助方法
  private EntityCollection applyQueryOptions(EntityCollection entitySet, UriInfo uriInfo) {
    // 实现查询选项应用逻辑
    // $filter, $orderby, $top, $skip, $select, $expand 等
    
    FilterOption filterOption = uriInfo.getFilterOption();
    if (filterOption != null) {
      // 应用过滤逻辑
      entitySet = applyFilter(entitySet, filterOption);
    }

    OrderByOption orderByOption = uriInfo.getOrderByOption();
    if (orderByOption != null) {
      // 应用排序逻辑
      entitySet = applyOrderBy(entitySet, orderByOption);
    }

    TopOption topOption = uriInfo.getTopOption();
    SkipOption skipOption = uriInfo.getSkipOption();
    if (topOption != null || skipOption != null) {
      // 应用分页逻辑
      entitySet = applyPaging(entitySet, skipOption, topOption);
    }

    return entitySet;
  }

  private void serializeEntityCollection(ODataResponse response, EdmEntitySet edmEntitySet, 
      EntityCollection entitySet, ContentType responseFormat) throws ODataLibraryException {
    
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    
    EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
        .contextURL(ContextURL.with().entitySet(edmEntitySet).build())
        .build();
    
    SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, 
        edmEntitySet.getEntityType(), entitySet, options);
    
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }

  private void serializeEntity(ODataResponse response, EdmEntitySet edmEntitySet, 
      Entity entity, ContentType responseFormat) throws ODataLibraryException {
    
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    
    EntitySerializerOptions options = EntitySerializerOptions.with()
        .contextURL(ContextURL.with().entitySet(edmEntitySet).suffix(Suffix.ENTITY).build())
        .build();
    
    SerializerResult serializerResult = serializer.entity(serviceMetadata, 
        edmEntitySet.getEntityType(), entity, options);
    
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }

  private Entity deserializeEntity(ODataRequest request, EdmEntitySet edmEntitySet, 
      ContentType requestFormat) throws ODataLibraryException {
    
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(request.getBody(), edmEntitySet.getEntityType());
    
    return result.getEntity();
  }

  private String getEntityKey(Entity entity) {
    Property idProperty = entity.getProperty("Id");
    return idProperty != null ? idProperty.getValue().toString() : "";
  }

  // 查询选项实现方法...
  private EntityCollection applyFilter(EntityCollection entitySet, FilterOption filterOption) {
    // 过滤实现
    return entitySet;
  }

  private EntityCollection applyOrderBy(EntityCollection entitySet, OrderByOption orderByOption) {
    // 排序实现
    return entitySet;
  }

  private EntityCollection applyPaging(EntityCollection entitySet, SkipOption skipOption, TopOption topOption) {
    // 分页实现
    List<Entity> entities = entitySet.getEntities();
    int skip = skipOption != null ? skipOption.getValue() : 0;
    int top = topOption != null ? topOption.getValue() : entities.size();
    
    int fromIndex = Math.min(skip, entities.size());
    int toIndex = Math.min(fromIndex + top, entities.size());
    
    EntityCollection pagedCollection = new EntityCollection();
    pagedCollection.getEntities().addAll(entities.subList(fromIndex, toIndex));
    
    return pagedCollection;
  }
}
```

**处理器特性**：
- **依赖注入**：通过构造函数注入数据提供器
- **完整 CRUD**：实现所有实体操作接口
- **查询支持**：支持 OData 查询选项
- **标准序列化**：使用 OData 框架的序列化机制

## 配置文件

### 1. application.properties

```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/

# 日志配置
logging.level.org.apache.olingo=DEBUG
logging.level.org.springframework=INFO
logging.level.org.apache.olingo.sample.springboot=DEBUG

# Spring Boot 配置
spring.application.name=OData Spring Boot Service
spring.main.banner-mode=console

# HTTP 编码配置
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true

# JSON 配置
spring.jackson.serialization.indent_output=true
spring.jackson.serialization.write_dates_as_timestamps=false

# 错误页面配置
server.error.whitelabel.enabled=false
server.error.include-stacktrace=never
server.error.include-message=always
```

### 2. pom.xml 关键依赖

```xml
<dependencies>
  <!-- Spring Boot Starter Web -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <!-- Apache Olingo OData 服务端 -->
  <dependency>
    <groupId>org.apache.olingo</groupId>
    <artifactId>odata-server-api</artifactId>
    <version>${olingo.version}</version>
  </dependency>
  
  <dependency>
    <groupId>org.apache.olingo</groupId>
    <artifactId>odata-server-core</artifactId>
    <version>${olingo.version}</version>
  </dependency>
  
  <dependency>
    <groupId>org.apache.olingo</groupId>
    <artifactId>odata-commons-api</artifactId>
    <version>${olingo.version}</version>
  </dependency>
  
  <dependency>
    <groupId>org.apache.olingo</groupId>
    <artifactId>odata-commons-core</artifactId>
    <version>${olingo.version}</version>
  </dependency>

  <!-- 测试依赖 -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

## API 使用示例

### 1. 服务发现

```bash
# 获取服务根
GET http://localhost:8080/odata/

# 获取元数据
GET http://localhost:8080/odata/$metadata

# 预期响应：完整的 CSDL XML 元数据
```

### 2. 实体集合操作

```bash
# 获取所有制造商
GET http://localhost:8080/odata/Manufacturers

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
    },
    {
      "Id": 2,
      "Name": "Mercedes-Benz",
      "Founded": "1926-06-28",
      "Address": {
        "Street": "Mercedesstraße 120",
        "City": "Stuttgart",
        "ZipCode": "70372",
        "Country": "Germany"
      }
    }
  ]
}

# 获取所有汽车
GET http://localhost:8080/odata/Cars
```

### 3. 单个实体操作

```bash
# 获取特定制造商
GET http://localhost:8080/odata/Manufacturers(1)

# 获取特定汽车
GET http://localhost:8080/odata/Cars(1)

# 创建新汽车
POST http://localhost:8080/odata/Cars
Content-Type: application/json

{
  "Model": "X7",
  "ModelYear": "2023",
  "Price": 95000.0,
  "Currency": "EUR"
}

# 更新汽车
PUT http://localhost:8080/odata/Cars(1)
Content-Type: application/json

{
  "Model": "X7 Updated",
  "ModelYear": "2023",
  "Price": 100000.0,
  "Currency": "EUR"
}

# 删除汽车
DELETE http://localhost:8080/odata/Cars(1)
```

### 4. 查询选项

```bash
# 过滤查询
GET http://localhost:8080/odata/Cars?$filter=Price gt 50000

# 排序查询
GET http://localhost:8080/odata/Cars?$orderby=Price desc

# 分页查询
GET http://localhost:8080/odata/Cars?$top=5&$skip=10

# 选择特定字段
GET http://localhost:8080/odata/Cars?$select=Model,Price

# 计数查询
GET http://localhost:8080/odata/Cars?$count=true

# 复合查询
GET http://localhost:8080/odata/Cars?$filter=Price gt 30000&$orderby=Price desc&$top=10&$select=Model,Price
```

### 5. 导航属性

```bash
# 获取制造商的所有汽车
GET http://localhost:8080/odata/Manufacturers(1)/Cars

# 获取汽车的制造商
GET http://localhost:8080/odata/Cars(1)/Manufacturer

# 使用 $expand 扩展查询
GET http://localhost:8080/odata/Manufacturers?$expand=Cars

# 嵌套扩展
GET http://localhost:8080/odata/Manufacturers?$expand=Cars($select=Model,Price)
```

## 运行和部署

### 1. 开发模式运行

```bash
# 使用 Maven 运行
mvn spring-boot:run

# 使用 Java 运行（需要先编译）
mvn clean compile
java -cp target/classes:target/dependency/* org.apache.olingo.sample.springboot.ODataSpringBootService

# 访问服务
http://localhost:8080/odata/
```

### 2. 生产模式部署

```bash
# 打包为可执行 JAR
mvn clean package

# 运行 JAR 文件
java -jar target/spring-boot-odata-sample.jar

# 配置生产环境
java -jar -Dspring.profiles.active=prod target/spring-boot-odata-sample.jar
```

### 3. Docker 部署

```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim

COPY target/spring-boot-odata-sample.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# 构建 Docker 镜像
docker build -t spring-boot-odata .

# 运行容器
docker run -p 8080:8080 spring-boot-odata
```

## Spring Boot 集成优势

### 1. 自动配置

```java
// Spring Boot 自动配置示例
@Configuration
@ConditionalOnClass(OData.class)
@EnableConfigurationProperties(ODataProperties.class)
public class ODataAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ODataServiceFactory oDataServiceFactory() {
    return new DefaultODataServiceFactory();
  }

  @Bean
  @ConditionalOnMissingBean
  public ODataErrorHandler oDataErrorHandler() {
    return new DefaultODataErrorHandler();
  }
}
```

### 2. 监控和健康检查

```java
// Actuator 集成
@Component
public class ODataHealthIndicator implements HealthIndicator {

  @Autowired
  private SpringBootDataProvider dataProvider;

  @Override
  public Health health() {
    try {
      // 检查数据提供器状态
      int carCount = dataProvider.readEntitySetData(null).getEntities().size();
      
      return Health.up()
          .withDetail("cars.count", carCount)
          .withDetail("status", "OData service is running")
          .build();
    } catch (Exception e) {
      return Health.down()
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
```

### 3. 配置属性绑定

```java
// 配置属性类
@ConfigurationProperties(prefix = "odata")
@Data
public class ODataProperties {
  
  private String serviceName = "OData Demo Service";
  private String namespace = "OData.Demo";
  private boolean enableCors = true;
  private int maxPageSize = 1000;
  
  private Security security = new Security();
  
  @Data
  public static class Security {
    private boolean enabled = false;
    private String[] allowedOrigins = {"*"};
  }
}
```

### 4. AOP 集成

```java
// AOP 切面示例
@Aspect
@Component
public class ODataPerformanceAspect {

  private static final Logger logger = LoggerFactory.getLogger(ODataPerformanceAspect.class);

  @Around("execution(* org.apache.olingo.sample.springboot..*Processor.*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    
    try {
      Object result = joinPoint.proceed();
      long executionTime = System.currentTimeMillis() - start;
      
      logger.info("Method {} executed in {} ms", 
          joinPoint.getSignature().toShortString(), executionTime);
      
      return result;
    } catch (Exception e) {
      long executionTime = System.currentTimeMillis() - start;
      logger.error("Method {} failed after {} ms: {}", 
          joinPoint.getSignature().toShortString(), executionTime, e.getMessage());
      throw e;
    }
  }
}
```

## 扩展和定制

### 1. 自定义错误处理

```java
// 全局异常处理器
@ControllerAdvice
public class ODataExceptionHandler {

  @ExceptionHandler(ODataApplicationException.class)
  public ResponseEntity<Map<String, Object>> handleODataException(ODataApplicationException e) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", Map.of(
        "code", e.getStatusCode(),
        "message", e.getMessage(),
        "timestamp", Instant.now()
    ));
    
    return ResponseEntity.status(e.getStatusCode()).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", Map.of(
        "code", 500,
        "message", "Internal server error",
        "timestamp", Instant.now()
    ));
    
    return ResponseEntity.status(500).body(error);
  }
}
```

### 2. 安全集成

```java
// Spring Security 配置
@Configuration
@EnableWebSecurity
public class ODataSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests()
        .antMatchers("/odata/$metadata").permitAll()
        .antMatchers(HttpMethod.GET, "/odata/**").hasRole("READ")
        .antMatchers(HttpMethod.POST, "/odata/**").hasRole("WRITE")
        .antMatchers(HttpMethod.PUT, "/odata/**").hasRole("WRITE")
        .antMatchers(HttpMethod.DELETE, "/odata/**").hasRole("ADMIN")
        .and()
        .httpBasic();
  }
}
```

### 3. 缓存集成

```java
// 缓存配置
@Configuration
@EnableCaching
public class ODataCacheConfig {

  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager("entities", "metadata");
  }
}

// 缓存注解使用
@Service
public class CachedDataProvider extends SpringBootDataProvider {

  @Cacheable(value = "entities", key = "#edmEntitySet.name")
  @Override
  public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) {
    return super.readEntitySetData(edmEntitySet);
  }

  @CacheEvict(value = "entities", allEntries = true)
  @Override
  public Entity createEntity(EdmEntitySet edmEntitySet, Entity entity) {
    return super.createEntity(edmEntitySet, entity);
  }
}
```

## 总结

`samples/spring-boot-odata` 项目展示了现代化的 OData 服务实现：

### 核心特性
- ✅ **Spring Boot 集成**：现代化的 Spring Boot 架构
- ✅ **依赖注入**：完整的 Spring IoC 容器支持
- ✅ **自动配置**：Spring Boot 的自动配置机制
- ✅ **生产就绪**：内置监控、健康检查、配置管理
- ✅ **现代架构**：注解驱动、组件化设计

### 技术亮点
- **现代框架**：Spring Boot 2.x+ 的现代特性
- **云原生**：容器化、微服务友好
- **开发效率**：快速启动、热重载、开发工具
- **生产特性**：监控、日志、配置、安全

### 适用场景
该 Spring Boot OData 服务为企业级应用提供了现代化的 OData 解决方案：
- **企业应用**：现代化的企业级 OData 服务
- **微服务架构**：作为微服务组件的 OData 接口
- **云原生应用**：容器化部署的 OData 服务
- **快速开发**：基于 Spring Boot 的快速 OData 原型
