package org.apache.olingo.schemamanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot OData Schema Manager Application
 * 
 * 这是一个基于Apache Olingo的OData XML Schema管理系统，提供以下核心功能：
 * 
 * 1. 递归XML加载 - 从目录递归加载所有OData XML文件
 * 2. 命名空间合并 - 将相同namespace的Schema信息组合到一起
 * 3. 依赖分析 - 分析EntityType、ComplexType、EnumType之间的依赖关系
 * 4. Container基础Schema提取 - 从EntityContainer构建完整依赖图
 * 
 * 使用方式：
 * - REST API: 通过HTTP接口操作Schema
 * - 编程API: 直接注入相关服务类使用
 * 
 * 主要组件：
 * - ODataXmlLoader: XML文件加载器
 * - SchemaRepository: Schema存储仓库  
 * - SchemaMerger: Schema合并器
 * - TypeDependencyAnalyzer: 类型依赖分析器
 */
@SpringBootApplication
public class ODataSchemaManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ODataSchemaManagerApplication.class, args);
    }
}
