# OData 4 XML 合规性检测系统 - FilePathRepository 重新设计

## 重要改进

### 问题描述
原来的 `FilePathRepository` 设计有一个重要问题：**一个 XML 文件可以包含多个不同 namespace 的 schema**，但原设计假设一个文件只对应一个 schema。

### 解决方案

#### 新的 FilePathRepository 接口设计

```java
public interface FilePathRepository {
    
    // 新增 FileEntry 内部接口
    interface FileEntry {
        Path getFilePath();
        List<CsdlSchema> getSchemas();     // 支持多个 schema
        LocalDateTime getValidationTime();
        long getFileSize();
        Set<String> getNamespaces();       // 文件中包含的所有 namespace
    }
    
    // 核心方法 - 存储多个 schema
    void storeSchemas(Path filePath, List<CsdlSchema> schemas, 
                     LocalDateTime validationTime, long fileSize);
    
    // 获取文件中的所有 schema
    List<CsdlSchema> getSchemas(Path filePath);
    
    // 根据 namespace 获取特定 schema
    Optional<CsdlSchema> getSchemaByNamespace(Path filePath, String namespace);
    
    // 根据 namespace 查找包含该 namespace 的所有文件
    List<Path> getFilePathsByNamespace(String namespace);
    
    // 获取所有 namespace
    Set<String> getAllNamespaces();
    
    // 删除文件时返回受影响的 namespace 集合
    Set<String> remove(Path filePath);
    
    // 统计信息
    int getTotalSchemaCount();  // 总 schema 数量
}
```

#### 主要改进点

1. **支持多 Schema 文件**: 
   - `storeSchemas()` 方法接受 `List<CsdlSchema>` 而不是单个 schema
   - `FileEntry` 包含多个 schema 和所有相关的 namespace

2. **Namespace 导向的查询**:
   - `getFilePathsByNamespace()` - 根据 namespace 查找包含该 namespace 的所有文件
   - `getSchemaByNamespace()` - 从特定文件中获取特定 namespace 的 schema
   - `getAllNamespaces()` - 获取系统中所有的 namespace

3. **更好的数据完整性**:
   - `remove()` 方法返回受影响的 namespace 集合，便于连锁更新
   - `FileEntry` 自动维护 namespace 集合

4. **统计信息增强**:
   - `getTotalSchemaCount()` - 区分文件数量和 schema 数量

#### 实际应用场景

一个 XML 文件可能包含：
```xml
<edmx:Edmx xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx" Version="4.0">
  <edmx:DataServices>
    <Schema Namespace="Microsoft.OData.SampleService.Models.TripPin" xmlns="http://docs.oasis-open.org/odata/ns/edm">
      <!-- TripPin schema 内容 -->
    </Schema>
    <Schema Namespace="Microsoft.OData.Core.V1" xmlns="http://docs.oasis-open.org/odata/ns/edm">
      <!-- 核心注解 schema 内容 -->
    </Schema>
  </edmx:DataServices>
</edmx:Edmx>
```

使用新设计：
```java
// 文件解析后存储
List<CsdlSchema> schemas = parseFile(xmlFile);  // 返回两个 schema
repository.storeSchemas(xmlFile, schemas, LocalDateTime.now(), fileSize);

// 查询特定 namespace
Optional<CsdlSchema> tripPinSchema = repository.getSchemaByNamespace(xmlFile, "Microsoft.OData.SampleService.Models.TripPin");
Optional<CsdlSchema> coreSchema = repository.getSchemaByNamespace(xmlFile, "Microsoft.OData.Core.V1");

// 查找包含特定 namespace 的所有文件
List<Path> filesWithCore = repository.getFilePathsByNamespace("Microsoft.OData.Core.V1");
```

#### 与其他组件的集成

1. **ComplianceValidator**: 现在正确处理多 schema 文件的验证和存储
2. **NamespaceSchemaRepository**: 接收来自多文件、多 schema 的合并操作
3. **DependencyTreeManager**: 可以正确分析跨文件、跨 namespace 的依赖关系

## 当前状态

✅ **编译**: 所有代码编译通过  
✅ **测试**: 基础功能测试全部通过  
🔄 **TODO**: 实现基于 Olingo 的实际 XML 解析和 schema 验证逻辑

这个重新设计解决了多 namespace 文件的核心问题，为后续实现完整的 OData schema 分析和依赖管理奠定了坚实的基础。
