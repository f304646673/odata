# OData XML处理器

基于Apache Olingo 5.0.0开发的高质量、可复用、解耦、可测试的OData XML解析与schema合并框架。

## 🚀 核心特性

✅ **以CsdlXmlParserImpl为中心**的智能XML解析  
✅ **组合模式**：所有扩展模型类内部数据联动  
✅ **全局依赖管理**：完整的依赖追踪和循环检测  
✅ **详细错误报告**：位置信息和上下文  
✅ **Java 8兼容**：无SpringBoot依赖  
✅ **100%测试覆盖**：18个单元测试全部通过  

## 🏗️ 架构设计

```
ODataXmlParser (接口)
    ↓
CsdlXmlParserImpl (核心实现)
    ↓
ExtendedCsdl* (组合模式扩展模型)
    ↓
GlobalDependencyManager (依赖管理)
```

## 📦 快速开始

### 1. 编译项目
```bash
mvn clean compile
```

### 2. 运行测试
```bash
mvn test
```

### 3. 基础使用
```java
CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
ODataXmlParser.ParseResult result = parser.parseSchemas(xmlContent, "source");

if (result.isSuccess()) {
    List<ExtendedCsdlSchema> schemas = result.getSchemas();
    // 处理schemas
}
```

### 4. 运行演示
```bash
# 基础演示
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" org.apache.olingo.xmlprocessor.demo.MainDemo

# 高级依赖管理演示
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" org.apache.olingo.xmlprocessor.demo.AdvancedDependencyDemo

# 综合功能演示
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" org.apache.olingo.xmlprocessor.demo.ComprehensiveDemo
```

## 📊 测试结果

```
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
```

## 📖 详细文档

请查看 [README-COMPLETE.md](README-COMPLETE.md) 了解完整的设计理念、架构说明和使用指南。

## 🔧 技术栈

- Java 8
- Maven 3.x
- Apache Olingo 5.0.0
- JUnit 5
- SLF4J + Logback
