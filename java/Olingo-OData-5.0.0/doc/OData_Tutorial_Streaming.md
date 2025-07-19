# OData Tutorial - 流式处理实体(Streaming Entity) (pe_streaming)

## 概览

`pe_streaming` 项目是Apache Olingo OData V4教程的流式处理专题，专门讲解**流式处理实体(Streaming Entity)**功能的实现。这个项目演示了如何在OData服务中高效地处理大量数据的流式读取和写入，特别适用于处理大型数据集、实时数据流和批量数据操作的场景。

## 学习目标

- 理解流式处理在OData中的概念和优势
- 掌握EntityCollectionProcessor的流式实现
- 学会处理大数据集的分页和流式响应
- 了解内存优化和性能调优技术

## 流式处理概念

### 流式处理优势
- **内存效率**：避免将大数据集完全加载到内存
- **响应时间**：提供更快的首字节响应时间
- **可扩展性**：支持处理任意大小的数据集
- **用户体验**：客户端可以立即开始处理接收到的数据

### 应用场景
| 场景 | 描述 | 优势 |
|------|------|------|
| **大数据查询** | 查询包含数百万记录的数据集 | 内存占用低，响应快 |
| **实时数据** | 处理实时生成的数据流 | 低延迟，高吞吐 |
| **批量导出** | 导出大量数据到客户端 | 稳定传输，支持断点续传 |
| **ETL操作** | 数据提取、转换、加载 | 高效处理，资源优化 |

## 核心架构

### 流式处理架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                   流式处理增强OData服务架构                        │
├─────────────────────────────────────────────────────────────────┤
│                       Client Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Streaming     │  │   Pagination    │  │   Progressive   │ │
│  │   Request       │  │   Support       │  │   Loading       │ │
│  │                 │  │                 │  │                 │ │
│  │ GET /Products   │  │ $skip, $top     │  │ Real-time UI    │ │
│  │ Accept-Encoding │  │ $orderby        │  │ Updates         │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Streaming Processing Layer                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │StreamingEntity  │  │   Chunk         │  │   Memory        │ │
│  │CollectionProc   │  │   Processing    │  │   Management    │ │
│  │                 │  │                 │  │                 │ │
│  │readEntityColl() │  │ Batch Reader    │  │ Buffer Control  │ │
│  │streamResponse() │  │ Data Chunks     │  │ GC Optimization │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Data Processing Pipeline                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Data Source   │  │   Transform     │  │   Serialization │ │
│  │   Iterator      │  │   Pipeline      │  │   Streaming     │ │
│  │                 │  │                 │  │                 │ │
│  │ Database Cursor │  │ Filter/Sort     │  │ JSON Streaming  │ │
│  │ File Reader     │  │ Map/Reduce      │  │ XML Streaming   │ │
│  │ API Iterator    │  │                 │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Resource Management                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Connection    │  │   Thread        │  │   Error         │ │
│  │   Pooling       │  │   Management    │  │   Recovery      │ │
│  │                 │  │                 │  │                 │ │
│  │ DB Connections  │  │ Async Execution │  │ Circuit Breaker │ │
│  │ HTTP Connections│  │ BackPressure    │  │ Retry Logic     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Enhanced Storage                             │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                   Streaming Storage                         │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │   Data      │ │   Index     │ │   Cache     │           │ │
│  │  │   Iterator  │ │   Support   │ │   Strategy  │           │ │
│  │  │             │ │             │ │             │           │ │
│  │  │ Lazy Load   │ │ B-Tree      │ │ LRU/TTL     │           │ │
│  │  │ Pagination  │ │ Range Query │ │ Write-Back  │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 流式EntityCollectionProcessor

### 1. 核心流式处理器
```java
public class StreamingEntityCollectionProcessor implements EntityCollectionProcessor {
    
    private Storage storage;
    private OData odata;
    private ServiceMetadata serviceMetadata;
    
    // 流式处理配置
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int MAX_CHUNK_SIZE = 10000;
    private static final long DEFAULT_TIMEOUT_MS = 30000; // 30秒
    
    public StreamingEntityCollectionProcessor(Storage storage) {
        this.storage = storage;
    }
    
    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }
    
    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, 
                                   UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        
        // 获取目标实体集
        EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
        
        // 解析查询选项
        QueryOptions queryOptions = parseQueryOptions(uriInfo);
        
        // 检查是否需要流式处理
        if (shouldUseStreaming(queryOptions, request)) {
            processStreamingRequest(request, response, edmEntitySet, queryOptions, responseFormat);
        } else {
            processStandardRequest(request, response, edmEntitySet, queryOptions, responseFormat);
        }
    }
    
    private void processStreamingRequest(ODataRequest request, ODataResponse response, 
                                       EdmEntitySet edmEntitySet, QueryOptions queryOptions, 
                                       ContentType responseFormat) 
            throws ODataApplicationException, ODataLibraryException {
        
        try {
            // 创建流式数据迭代器
            DataIterator<Entity> dataIterator = storage.createStreamingIterator(
                edmEntitySet.getName(), queryOptions);
            
            // 创建流式响应写入器
            StreamingResponseWriter responseWriter = new StreamingResponseWriter(
                response, responseFormat, edmEntitySet, serviceMetadata, odata);
            
            // 开始流式写入
            responseWriter.writeStreamingResponse(dataIterator, queryOptions);
            
        } catch (Exception e) {
            throw new ODataApplicationException("Streaming processing failed",
                                              HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
                                              Locale.ROOT, e);
        }
    }
    
    private void processStandardRequest(ODataRequest request, ODataResponse response, 
                                      EdmEntitySet edmEntitySet, QueryOptions queryOptions, 
                                      ContentType responseFormat) 
            throws ODataApplicationException, ODataLibraryException {
        
        // 标准处理逻辑（与之前的实现相同）
        EntityCollection entityCollection = storage.readEntitySetData(edmEntitySet, queryOptions);
        
        // 序列化并返回
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        
        EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
                .contextURL(ContextURL.with()
                           .entitySet(edmEntitySet)
                           .build())
                .count(queryOptions.getCountOption())
                .build();
        
        SerializerResult serializerResult = serializer.entityCollection(
            serviceMetadata, edmEntitySet.getEntityType(), entityCollection, options);
        
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
    
    // 判断是否使用流式处理的逻辑
    private boolean shouldUseStreaming(QueryOptions queryOptions, ODataRequest request) {
        
        // 1. 检查数据量大小预估
        int estimatedSize = storage.estimateResultSize(queryOptions);
        if (estimatedSize > 10000) { // 超过1万条记录使用流式处理
            return true;
        }
        
        // 2. 检查客户端是否支持流式处理
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (acceptEncoding != null && acceptEncoding.contains("chunked")) {
            return true;
        }
        
        // 3. 检查是否有特殊的流式处理请求头
        String streamingHint = request.getHeader("X-Streaming-Preferred");
        if ("true".equalsIgnoreCase(streamingHint)) {
            return true;
        }
        
        // 4. 检查查询复杂度
        if (queryOptions.hasComplexQuery()) {
            return true;
        }
        
        return false;
    }
    
    private QueryOptions parseQueryOptions(UriInfo uriInfo) {
        
        // 解析各种查询选项
        FilterOption filterOption = uriInfo.getFilterOption();
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        SkipOption skipOption = uriInfo.getSkipOption();
        TopOption topOption = uriInfo.getTopOption();
        CountOption countOption = uriInfo.getCountOption();
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();
        
        return new QueryOptions(filterOption, orderByOption, skipOption, 
                               topOption, countOption, selectOption, expandOption);
    }
}

// 查询选项封装类
class QueryOptions {
    private FilterOption filterOption;
    private OrderByOption orderByOption;
    private SkipOption skipOption;
    private TopOption topOption;
    private CountOption countOption;
    private SelectOption selectOption;
    private ExpandOption expandOption;
    
    public QueryOptions(FilterOption filterOption, OrderByOption orderByOption,
                       SkipOption skipOption, TopOption topOption, CountOption countOption,
                       SelectOption selectOption, ExpandOption expandOption) {
        this.filterOption = filterOption;
        this.orderByOption = orderByOption;
        this.skipOption = skipOption;
        this.topOption = topOption;
        this.countOption = countOption;
        this.selectOption = selectOption;
        this.expandOption = expandOption;
    }
    
    public boolean hasComplexQuery() {
        return (filterOption != null && hasComplexFilter()) ||
               (orderByOption != null && hasComplexOrderBy()) ||
               (expandOption != null);
    }
    
    private boolean hasComplexFilter() {
        // 检查过滤条件的复杂性
        // 这里简化实现，实际可以分析表达式树
        return filterOption.getExpression() != null;
    }
    
    private boolean hasComplexOrderBy() {
        // 检查排序的复杂性
        return orderByOption.getOrders().size() > 1;
    }
    
    public CountOption getCountOption() { return countOption; }
    public SkipOption getSkipOption() { return skipOption; }
    public TopOption getTopOption() { return topOption; }
    public FilterOption getFilterOption() { return filterOption; }
    public OrderByOption getOrderByOption() { return orderByOption; }
    public SelectOption getSelectOption() { return selectOption; }
    public ExpandOption getExpandOption() { return expandOption; }
}
```

### 2. 流式响应写入器
```java
public class StreamingResponseWriter {
    
    private ODataResponse response;
    private ContentType responseFormat;
    private EdmEntitySet edmEntitySet;
    private ServiceMetadata serviceMetadata;
    private OData odata;
    
    public StreamingResponseWriter(ODataResponse response, ContentType responseFormat,
                                 EdmEntitySet edmEntitySet, ServiceMetadata serviceMetadata,
                                 OData odata) {
        this.response = response;
        this.responseFormat = responseFormat;
        this.edmEntitySet = edmEntitySet;
        this.serviceMetadata = serviceMetadata;
        this.odata = odata;
    }
    
    public void writeStreamingResponse(DataIterator<Entity> dataIterator, QueryOptions queryOptions) 
            throws ODataApplicationException, ODataLibraryException {
        
        // 设置响应头
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        response.setHeader("Transfer-Encoding", "chunked");
        
        try {
            if (ContentType.APPLICATION_JSON.isCompatible(responseFormat)) {
                writeJsonStreamingResponse(dataIterator, queryOptions);
            } else if (ContentType.APPLICATION_XML.isCompatible(responseFormat)) {
                writeXmlStreamingResponse(dataIterator, queryOptions);
            } else {
                throw new ODataApplicationException("Unsupported content type for streaming",
                                                  HttpStatusCode.NOT_ACCEPTABLE.getStatusCode(),
                                                  Locale.ROOT);
            }
        } catch (Exception e) {
            throw new ODataApplicationException("Failed to write streaming response",
                                              HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
                                              Locale.ROOT, e);
        }
    }
    
    private void writeJsonStreamingResponse(DataIterator<Entity> dataIterator, QueryOptions queryOptions) 
            throws Exception {
        
        // 创建管道输出流
        PipedOutputStream pipedOutput = new PipedOutputStream();
        PipedInputStream pipedInput = new PipedInputStream(pipedOutput);
        
        // 设置响应内容
        response.setContent(pipedInput);
        
        // 在独立线程中写入数据
        CompletableFuture.runAsync(() -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(pipedOutput, StandardCharsets.UTF_8);
                 JsonWriter jsonWriter = new JsonWriter(writer)) {
                
                writeJsonStreamContent(jsonWriter, dataIterator, queryOptions);
                
            } catch (Exception e) {
                LOG.error("Error writing streaming JSON response", e);
            }
        });
    }
    
    private void writeJsonStreamContent(JsonWriter jsonWriter, DataIterator<Entity> dataIterator, 
                                      QueryOptions queryOptions) throws Exception {
        
        jsonWriter.beginObject();
        
        // 写入上下文信息
        String contextURL = buildContextURL();
        jsonWriter.name("@odata.context").value(contextURL);
        
        // 写入计数信息（如果请求了）
        if (queryOptions.getCountOption() != null && queryOptions.getCountOption().getValue()) {
            long totalCount = dataIterator.getTotalCount();
            jsonWriter.name("@odata.count").value(totalCount);
        }
        
        // 开始写入实体数组
        jsonWriter.name("value");
        jsonWriter.beginArray();
        
        // 流式写入实体
        int chunkSize = 0;
        while (dataIterator.hasNext()) {
            Entity entity = dataIterator.next();
            writeJsonEntity(jsonWriter, entity);
            
            // 控制块大小，定期刷新
            if (++chunkSize >= DEFAULT_CHUNK_SIZE) {
                jsonWriter.flush();
                chunkSize = 0;
                
                // 检查客户端连接状态
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        }
        
        jsonWriter.endArray();
        jsonWriter.endObject();
        jsonWriter.flush();
    }
    
    private void writeJsonEntity(JsonWriter jsonWriter, Entity entity) throws IOException {
        
        jsonWriter.beginObject();
        
        // 写入实体属性
        for (Property property : entity.getProperties()) {
            jsonWriter.name(property.getName());
            writeJsonValue(jsonWriter, property.getValue());
        }
        
        // 写入导航链接（如果有）
        for (Link link : entity.getNavigationLinks()) {
            jsonWriter.name(link.getTitle());
            if (link.getInlineEntity() != null) {
                writeJsonEntity(jsonWriter, link.getInlineEntity());
            } else if (link.getInlineEntitySet() != null) {
                jsonWriter.beginArray();
                for (Entity relatedEntity : link.getInlineEntitySet().getEntities()) {
                    writeJsonEntity(jsonWriter, relatedEntity);
                }
                jsonWriter.endArray();
            }
        }
        
        jsonWriter.endObject();
    }
    
    private void writeJsonValue(JsonWriter jsonWriter, Object value) throws IOException {
        if (value == null) {
            jsonWriter.nullValue();
        } else if (value instanceof String) {
            jsonWriter.value((String) value);
        } else if (value instanceof Integer) {
            jsonWriter.value((Integer) value);
        } else if (value instanceof Long) {
            jsonWriter.value((Long) value);
        } else if (value instanceof Double) {
            jsonWriter.value((Double) value);
        } else if (value instanceof Boolean) {
            jsonWriter.value((Boolean) value);
        } else if (value instanceof BigDecimal) {
            jsonWriter.value((BigDecimal) value);
        } else if (value instanceof Timestamp) {
            jsonWriter.value(((Timestamp) value).toInstant().toString());
        } else {
            jsonWriter.value(value.toString());
        }
    }
    
    private void writeXmlStreamingResponse(DataIterator<Entity> dataIterator, QueryOptions queryOptions) 
            throws Exception {
        
        // XML流式处理实现（类似JSON但使用XML写入器）
        PipedOutputStream pipedOutput = new PipedOutputStream();
        PipedInputStream pipedInput = new PipedInputStream(pipedOutput);
        
        response.setContent(pipedInput);
        
        CompletableFuture.runAsync(() -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(pipedOutput, StandardCharsets.UTF_8)) {
                
                XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
                writeXmlStreamContent(xmlWriter, dataIterator, queryOptions);
                
            } catch (Exception e) {
                LOG.error("Error writing streaming XML response", e);
            }
        });
    }
    
    private void writeXmlStreamContent(XMLStreamWriter xmlWriter, DataIterator<Entity> dataIterator, 
                                     QueryOptions queryOptions) throws Exception {
        
        // 开始写入XML文档
        xmlWriter.writeStartDocument("UTF-8", "1.0");
        xmlWriter.writeStartElement("feed");
        xmlWriter.writeNamespace("", "http://www.w3.org/2005/Atom");
        xmlWriter.writeNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
        xmlWriter.writeNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
        
        // 写入计数信息
        if (queryOptions.getCountOption() != null && queryOptions.getCountOption().getValue()) {
            xmlWriter.writeStartElement("m", "count");
            xmlWriter.writeCharacters(String.valueOf(dataIterator.getTotalCount()));
            xmlWriter.writeEndElement();
        }
        
        // 流式写入实体
        int chunkSize = 0;
        while (dataIterator.hasNext()) {
            Entity entity = dataIterator.next();
            writeXmlEntity(xmlWriter, entity);
            
            if (++chunkSize >= DEFAULT_CHUNK_SIZE) {
                xmlWriter.flush();
                chunkSize = 0;
                
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        }
        
        xmlWriter.writeEndElement(); // feed
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
    }
    
    private void writeXmlEntity(XMLStreamWriter xmlWriter, Entity entity) throws XMLStreamException {
        
        xmlWriter.writeStartElement("entry");
        
        // 写入实体属性
        xmlWriter.writeStartElement("content");
        xmlWriter.writeAttribute("type", "application/xml");
        xmlWriter.writeStartElement("m", "properties");
        
        for (Property property : entity.getProperties()) {
            xmlWriter.writeStartElement("d", property.getName());
            if (property.getValue() != null) {
                xmlWriter.writeCharacters(property.getValue().toString());
            }
            xmlWriter.writeEndElement();
        }
        
        xmlWriter.writeEndElement(); // properties
        xmlWriter.writeEndElement(); // content
        xmlWriter.writeEndElement(); // entry
    }
    
    private String buildContextURL() {
        return "$metadata#" + edmEntitySet.getName();
    }
}
```

## 流式数据迭代器

### 1. 数据迭代器接口
```java
public interface DataIterator<T> extends Iterator<T>, AutoCloseable {
    
    /**
     * 获取总数量（如果可用）
     */
    long getTotalCount();
    
    /**
     * 获取当前批次大小
     */
    int getCurrentBatchSize();
    
    /**
     * 预取下一批数据
     */
    void prefetchNext();
    
    /**
     * 检查是否支持重置
     */
    boolean isResetSupported();
    
    /**
     * 重置迭代器到起始位置
     */
    void reset() throws UnsupportedOperationException;
    
    /**
     * 获取迭代器状态信息
     */
    IteratorStatus getStatus();
    
    @Override
    void close() throws Exception;
}

// 迭代器状态枚举
enum IteratorStatus {
    INITIALIZED,
    READING,
    EXHAUSTED,
    ERROR,
    CLOSED
}
```

### 2. 数据库流式迭代器实现
```java
public class DatabaseStreamingIterator implements DataIterator<Entity> {
    
    private final String entitySetName;
    private final QueryOptions queryOptions;
    private final Storage storage;
    
    // 数据库相关
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    
    // 迭代状态
    private IteratorStatus status;
    private Entity nextEntity;
    private boolean hasNextEntity;
    private long totalCount;
    private int currentPosition;
    
    // 配置参数
    private final int fetchSize;
    private final int timeoutSeconds;
    
    public DatabaseStreamingIterator(String entitySetName, QueryOptions queryOptions, 
                                   Storage storage) {
        this.entitySetName = entitySetName;
        this.queryOptions = queryOptions;
        this.storage = storage;
        this.fetchSize = 1000; // 数据库批次大小
        this.timeoutSeconds = 30;
        this.status = IteratorStatus.INITIALIZED;
        
        initialize();
    }
    
    private void initialize() {
        try {
            // 获取数据库连接
            connection = storage.getConnection();
            connection.setAutoCommit(false); // 使用事务确保一致性
            
            // 构建SQL查询
            String sql = buildStreamingQuery();
            statement = connection.prepareStatement(sql, 
                                                   ResultSet.TYPE_FORWARD_ONLY, 
                                                   ResultSet.CONCUR_READ_ONLY);
            
            // 优化游标性能
            statement.setFetchSize(fetchSize);
            statement.setQueryTimeout(timeoutSeconds);
            
            // 执行查询
            resultSet = statement.executeQuery();
            
            // 预读第一条记录
            advanceToNext();
            
            // 获取总数（如果需要）
            if (queryOptions.getCountOption() != null && queryOptions.getCountOption().getValue()) {
                totalCount = calculateTotalCount();
            }
            
            status = IteratorStatus.READING;
            
        } catch (SQLException e) {
            status = IteratorStatus.ERROR;
            throw new RuntimeException("Failed to initialize streaming iterator", e);
        }
    }
    
    private String buildStreamingQuery() {
        StringBuilder sql = new StringBuilder();
        
        // 基础查询
        sql.append("SELECT * FROM ").append(getTableName(entitySetName));
        
        // 添加WHERE条件
        if (queryOptions.getFilterOption() != null) {
            sql.append(" WHERE ").append(translateFilter(queryOptions.getFilterOption()));
        }
        
        // 添加ORDER BY
        if (queryOptions.getOrderByOption() != null) {
            sql.append(" ORDER BY ").append(translateOrderBy(queryOptions.getOrderByOption()));
        } else {
            // 确保有稳定的排序，避免数据重复或遗漏
            sql.append(" ORDER BY ID");
        }
        
        // 添加LIMIT和OFFSET
        if (queryOptions.getSkipOption() != null) {
            sql.append(" OFFSET ").append(queryOptions.getSkipOption().getValue());
        }
        
        if (queryOptions.getTopOption() != null) {
            sql.append(" LIMIT ").append(queryOptions.getTopOption().getValue());
        }
        
        return sql.toString();
    }
    
    @Override
    public boolean hasNext() {
        return hasNextEntity && status == IteratorStatus.READING;
    }
    
    @Override
    public Entity next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more entities available");
        }
        
        Entity currentEntity = nextEntity;
        currentPosition++;
        
        // 预读下一条记录
        advanceToNext();
        
        return currentEntity;
    }
    
    private void advanceToNext() {
        try {
            if (resultSet.next()) {
                nextEntity = mapResultSetToEntity(resultSet);
                hasNextEntity = true;
            } else {
                nextEntity = null;
                hasNextEntity = false;
                status = IteratorStatus.EXHAUSTED;
            }
        } catch (SQLException e) {
            status = IteratorStatus.ERROR;
            throw new RuntimeException("Error reading next entity", e);
        }
    }
    
    private Entity mapResultSetToEntity(ResultSet rs) throws SQLException {
        Entity entity = new Entity();
        
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            
            // 转换数据库类型到OData类型
            Object odataValue = convertDatabaseValue(value, metaData.getColumnType(i));
            
            entity.addProperty(new Property(null, columnName, ValueType.PRIMITIVE, odataValue));
        }
        
        return entity;
    }
    
    private Object convertDatabaseValue(Object dbValue, int sqlType) {
        if (dbValue == null) {
            return null;
        }
        
        switch (sqlType) {
            case Types.INTEGER:
                return ((Number) dbValue).intValue();
            case Types.BIGINT:
                return ((Number) dbValue).longValue();
            case Types.DECIMAL:
            case Types.NUMERIC:
                return (dbValue instanceof BigDecimal) ? dbValue : new BigDecimal(dbValue.toString());
            case Types.VARCHAR:
            case Types.CHAR:
                return dbValue.toString();
            case Types.TIMESTAMP:
                return dbValue; // 保持Timestamp类型
            case Types.BOOLEAN:
                return (Boolean) dbValue;
            default:
                return dbValue.toString();
        }
    }
    
    private long calculateTotalCount() {
        try {
            String countSql = buildCountQuery();
            try (PreparedStatement countStmt = connection.prepareStatement(countSql);
                 ResultSet countRs = countStmt.executeQuery()) {
                
                if (countRs.next()) {
                    return countRs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOG.warn("Failed to calculate total count", e);
        }
        return -1; // 未知总数
    }
    
    private String buildCountQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(getTableName(entitySetName));
        
        if (queryOptions.getFilterOption() != null) {
            sql.append(" WHERE ").append(translateFilter(queryOptions.getFilterOption()));
        }
        
        return sql.toString();
    }
    
    @Override
    public long getTotalCount() {
        return totalCount;
    }
    
    @Override
    public int getCurrentBatchSize() {
        return fetchSize;
    }
    
    @Override
    public void prefetchNext() {
        // 对于数据库游标，预取已经在数据库层面处理
        // 这里可以实现应用层的预取逻辑
    }
    
    @Override
    public boolean isResetSupported() {
        return false; // 数据库游标通常不支持重置
    }
    
    @Override
    public void reset() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Reset not supported for database streaming iterator");
    }
    
    @Override
    public IteratorStatus getStatus() {
        return status;
    }
    
    @Override
    public void close() throws Exception {
        status = IteratorStatus.CLOSED;
        
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOG.warn("Error closing result set", e);
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOG.warn("Error closing statement", e);
            }
        }
        
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.warn("Error closing connection", e);
            }
        }
    }
    
    // 辅助方法
    private String getTableName(String entitySetName) {
        switch (entitySetName) {
            case DemoEdmProvider.ES_PRODUCTS:
                return "PRODUCTS";
            case DemoEdmProvider.ES_CATEGORIES:
                return "CATEGORIES";
            default:
                throw new IllegalArgumentException("Unknown entity set: " + entitySetName);
        }
    }
    
    private String translateFilter(FilterOption filterOption) {
        // 简化的过滤器翻译，实际应用中需要完整的表达式解析
        // 这里只是示例
        return "1=1"; // 占位符
    }
    
    private String translateOrderBy(OrderByOption orderByOption) {
        // 简化的排序翻译
        return orderByOption.getOrders().stream()
                           .map(order -> order.getExpression().toString())
                           .collect(Collectors.joining(", "));
    }
}
```

### 3. Storage流式支持
```java
public class Storage {
    
    // 现有实现...
    
    public DataIterator<Entity> createStreamingIterator(String entitySetName, QueryOptions queryOptions) {
        
        // 根据数据源类型选择合适的迭代器
        if (hasDatabaseConnection()) {
            return new DatabaseStreamingIterator(entitySetName, queryOptions, this);
        } else {
            return new MemoryStreamingIterator(entitySetName, queryOptions, this);
        }
    }
    
    public int estimateResultSize(QueryOptions queryOptions) {
        // 估算结果集大小，用于决定是否使用流式处理
        // 这里简化实现
        
        if (queryOptions.getTopOption() != null) {
            return queryOptions.getTopOption().getValue();
        }
        
        // 根据过滤条件估算
        if (queryOptions.getFilterOption() == null) {
            // 没有过滤，返回表的总行数估算
            return getTotalRowCount();
        } else {
            // 有过滤，估算过滤后的行数
            return (int) (getTotalRowCount() * 0.1); // 简化假设过滤后剩余10%
        }
    }
    
    private int getTotalRowCount() {
        // 返回所有实体的总数估算
        return productList.size() + categoryList.size();
    }
    
    private boolean hasDatabaseConnection() {
        // 检查是否有数据库连接可用
        // 这里简化，实际应该检查连接池状态
        return false; // 在这个示例中使用内存数据
    }
    
    public Connection getConnection() throws SQLException {
        // 获取数据库连接
        // 实际实现应该从连接池获取
        throw new UnsupportedOperationException("Database connection not available in this demo");
    }
}
```

## 内存流式迭代器

### 1. 内存数据流式处理
```java
public class MemoryStreamingIterator implements DataIterator<Entity> {
    
    private final String entitySetName;
    private final QueryOptions queryOptions;
    private final Storage storage;
    
    private List<Entity> sourceData;
    private Iterator<Entity> dataIterator;
    private IteratorStatus status;
    private long totalCount;
    private int currentPosition;
    
    public MemoryStreamingIterator(String entitySetName, QueryOptions queryOptions, Storage storage) {
        this.entitySetName = entitySetName;
        this.queryOptions = queryOptions;
        this.storage = storage;
        this.status = IteratorStatus.INITIALIZED;
        
        initialize();
    }
    
    private void initialize() {
        try {
            // 获取源数据
            sourceData = getSourceData(entitySetName);
            
            // 应用过滤
            if (queryOptions.getFilterOption() != null) {
                sourceData = applyFilter(sourceData, queryOptions.getFilterOption());
            }
            
            // 计算总数
            totalCount = sourceData.size();
            
            // 应用排序
            if (queryOptions.getOrderByOption() != null) {
                sourceData = applyOrderBy(sourceData, queryOptions.getOrderByOption());
            }
            
            // 应用分页
            sourceData = applyPaging(sourceData, queryOptions);
            
            // 创建迭代器
            dataIterator = sourceData.iterator();
            
            status = IteratorStatus.READING;
            
        } catch (Exception e) {
            status = IteratorStatus.ERROR;
            throw new RuntimeException("Failed to initialize memory streaming iterator", e);
        }
    }
    
    private List<Entity> getSourceData(String entitySetName) {
        switch (entitySetName) {
            case DemoEdmProvider.ES_PRODUCTS:
                return new ArrayList<>(storage.getProductList());
            case DemoEdmProvider.ES_CATEGORIES:
                return new ArrayList<>(storage.getCategoryList());
            default:
                throw new IllegalArgumentException("Unknown entity set: " + entitySetName);
        }
    }
    
    private List<Entity> applyFilter(List<Entity> data, FilterOption filterOption) {
        // 简化的过滤器实现
        // 实际应该解析和执行过滤表达式
        return data.stream()
                  .filter(entity -> matchesFilter(entity, filterOption))
                  .collect(Collectors.toList());
    }
    
    private boolean matchesFilter(Entity entity, FilterOption filterOption) {
        // 简化的过滤匹配逻辑
        // 实际应该解析ComplexFilterExpression
        return true; // 占位符
    }
    
    private List<Entity> applyOrderBy(List<Entity> data, OrderByOption orderByOption) {
        // 简化的排序实现
        return data.stream()
                  .sorted((e1, e2) -> compareEntities(e1, e2, orderByOption))
                  .collect(Collectors.toList());
    }
    
    private int compareEntities(Entity e1, Entity e2, OrderByOption orderByOption) {
        // 简化的实体比较逻辑
        // 实际应该处理多个排序字段和升序/降序
        
        for (OrderByItem orderItem : orderByOption.getOrders()) {
            String propertyName = orderItem.getExpression().toString();
            
            Object value1 = getPropertyValue(e1, propertyName);
            Object value2 = getPropertyValue(e2, propertyName);
            
            int comparison = compareValues(value1, value2);
            
            if (comparison != 0) {
                return orderItem.isDescending() ? -comparison : comparison;
            }
        }
        
        return 0;
    }
    
    private Object getPropertyValue(Entity entity, String propertyName) {
        Property property = entity.getProperty(propertyName);
        return property != null ? property.getValue() : null;
    }
    
    @SuppressWarnings("unchecked")
    private int compareValues(Object value1, Object value2) {
        if (value1 == null && value2 == null) return 0;
        if (value1 == null) return -1;
        if (value2 == null) return 1;
        
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            return ((Comparable<Object>) value1).compareTo(value2);
        }
        
        return value1.toString().compareTo(value2.toString());
    }
    
    private List<Entity> applyPaging(List<Entity> data, QueryOptions queryOptions) {
        int skip = 0;
        int top = data.size();
        
        if (queryOptions.getSkipOption() != null) {
            skip = queryOptions.getSkipOption().getValue();
        }
        
        if (queryOptions.getTopOption() != null) {
            top = queryOptions.getTopOption().getValue();
        }
        
        int fromIndex = Math.min(skip, data.size());
        int toIndex = Math.min(fromIndex + top, data.size());
        
        return data.subList(fromIndex, toIndex);
    }
    
    @Override
    public boolean hasNext() {
        return dataIterator.hasNext() && status == IteratorStatus.READING;
    }
    
    @Override
    public Entity next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more entities available");
        }
        
        currentPosition++;
        Entity entity = dataIterator.next();
        
        if (!dataIterator.hasNext()) {
            status = IteratorStatus.EXHAUSTED;
        }
        
        return entity;
    }
    
    @Override
    public long getTotalCount() {
        return totalCount;
    }
    
    @Override
    public int getCurrentBatchSize() {
        return sourceData.size();
    }
    
    @Override
    public void prefetchNext() {
        // 内存迭代器不需要预取
    }
    
    @Override
    public boolean isResetSupported() {
        return true;
    }
    
    @Override
    public void reset() {
        dataIterator = sourceData.iterator();
        currentPosition = 0;
        status = IteratorStatus.READING;
    }
    
    @Override
    public IteratorStatus getStatus() {
        return status;
    }
    
    @Override
    public void close() throws Exception {
        status = IteratorStatus.CLOSED;
        sourceData = null;
        dataIterator = null;
    }
}
```

## 性能优化和监控

### 1. 性能监控
```java
public class StreamingPerformanceMonitor {
    
    private static final Logger PERF_LOG = LoggerFactory.getLogger("performance");
    
    public static class StreamingMetrics {
        private long startTime;
        private long totalEntities;
        private long totalBytes;
        private int chunkCount;
        private long lastChunkTime;
        
        public StreamingMetrics() {
            this.startTime = System.currentTimeMillis();
            this.lastChunkTime = startTime;
        }
        
        public void recordChunk(int entityCount, long bytes) {
            long currentTime = System.currentTimeMillis();
            totalEntities += entityCount;
            totalBytes += bytes;
            chunkCount++;
            
            long chunkDuration = currentTime - lastChunkTime;
            PERF_LOG.debug("Chunk {}: {} entities, {} bytes, {}ms", 
                          chunkCount, entityCount, bytes, chunkDuration);
            
            lastChunkTime = currentTime;
        }
        
        public void recordCompletion() {
            long totalDuration = System.currentTimeMillis() - startTime;
            double entitiesPerSecond = totalEntities * 1000.0 / totalDuration;
            double mbPerSecond = (totalBytes / 1024.0 / 1024.0) * 1000.0 / totalDuration;
            
            PERF_LOG.info("Streaming completed: {} entities, {} bytes, {}ms, {:.2f} entities/sec, {:.2f} MB/sec",
                         totalEntities, totalBytes, totalDuration, entitiesPerSecond, mbPerSecond);
        }
    }
    
    public static StreamingMetrics createMetrics() {
        return new StreamingMetrics();
    }
}
```

### 2. 内存管理
```java
public class StreamingMemoryManager {
    
    private static final long MAX_MEMORY_THRESHOLD = Runtime.getRuntime().maxMemory() * 8 / 10; // 80%
    private static final int GC_CHECK_INTERVAL = 100; // 每100个实体检查一次
    
    private int entityCount = 0;
    
    public boolean shouldTriggerGC() {
        return ++entityCount % GC_CHECK_INTERVAL == 0 && 
               Runtime.getRuntime().totalMemory() > MAX_MEMORY_THRESHOLD;
    }
    
    public void suggestGC() {
        long beforeGC = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        long afterGC = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        LOG.debug("GC suggested: memory before {}MB, after {}MB", 
                 beforeGC / 1024 / 1024, afterGC / 1024 / 1024);
    }
    
    public MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long max = runtime.maxMemory();
        long used = total - free;
        
        return new MemoryInfo(used, free, total, max);
    }
    
    public static class MemoryInfo {
        public final long used;
        public final long free;
        public final long total;
        public final long max;
        
        public MemoryInfo(long used, long free, long total, long max) {
            this.used = used;
            this.free = free;
            this.total = total;
            this.max = max;
        }
        
        public double getUsagePercentage() {
            return (double) used / max * 100;
        }
    }
}
```

## 流式处理测试

### 1. 基本流式处理测试
```bash
# 测试大数据集的流式获取
curl -X GET "http://localhost:8080/DemoService.svc/Products?\$top=100000" \
     -H "Accept: application/json" \
     -H "X-Streaming-Preferred: true"

# 测试带过滤的流式处理
curl -X GET "http://localhost:8080/DemoService.svc/Products?\$filter=Price gt 100&\$top=50000" \
     -H "Accept: application/json" \
     -H "Accept-Encoding: chunked"
```

### 2. 性能测试
```bash
# 测试响应时间
time curl -X GET "http://localhost:8080/DemoService.svc/Products?\$top=10000" \
          -H "Accept: application/json" \
          --output /dev/null

# 测试内存使用情况（需要JVM参数 -XX:+PrintGCDetails）
curl -X GET "http://localhost:8080/DemoService.svc/Products?\$top=100000" \
     -H "X-Streaming-Preferred: true" \
     --output large_dataset.json
```

### 3. 错误恢复测试
```bash
# 测试连接中断恢复
curl -X GET "http://localhost:8080/DemoService.svc/Products?\$top=50000" \
     -H "Accept: application/json" \
     --max-time 10 # 10秒后超时

# 测试内存压力下的行为
curl -X GET "http://localhost:8080/DemoService.svc/Products?\$top=1000000" \
     -H "Accept: application/json"
```

## 总结

`pe_streaming`教程实现了OData流式处理的核心功能：

### 新增能力
- ✅ **流式响应**：支持大数据集的流式传输
- ✅ **内存优化**：避免大数据集的内存溢出
- ✅ **性能监控**：实时监控流式处理性能
- ✅ **多数据源**：支持数据库和内存数据的流式处理
- ✅ **格式支持**：JSON和XML的流式序列化

### 技术亮点
- **惰性加载**：按需加载数据，减少内存占用
- **异步处理**：使用管道和异步写入提高并发性能
- **自适应策略**：根据数据量自动选择处理策略
- **资源管理**：完善的连接和内存资源管理

### 架构价值
- **可扩展性**：支持任意大小数据集的处理
- **响应性能**：提供更快的首字节响应时间
- **稳定性**：内存和资源的有效管理
- **监控能力**：完整的性能监控和指标收集

流式处理功能是构建高性能、大规模数据服务的关键技术，特别适用于数据分析、报表生成和ETL场景。
