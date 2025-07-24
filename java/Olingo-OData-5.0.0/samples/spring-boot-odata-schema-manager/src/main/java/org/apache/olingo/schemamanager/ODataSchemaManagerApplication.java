package org.apache.olingo.schemamanager;

import org.apache.olingo.schemamanager.service.SchemaAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot OData Schema Manager Application
 * 
 * 现在包含强大的ODataSchemaAnalyzer功能：
 * 
 * 1. 递归XML加载 - 从目录递归扫描所有OData XML文件
 * 2. Import校验 - 验证文件间依赖关系，检测循环依赖
 * 3. 类型分析 - 深度分析EntityType、ComplexType、EnumType依赖
 * 4. 智能查询 - 提供类型详情和关联关系查询
 * 
 * 使用方式：
 * - REST API: /api/odata/analyzer/* 接口
 * - 编程API: 注入ODataSchemaAnalyzer直接使用
 * - 示例Service: SchemaAnalysisService演示完整流程
 */
@SpringBootApplication
public class ODataSchemaManagerApplication implements CommandLineRunner {

    @Autowired(required = false)
    private SchemaAnalysisService analysisService;

    public static void main(String[] args) {
        SpringApplication.run(ODataSchemaManagerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== OData Schema Manager Started ===");
        System.out.println("Available endpoints:");
        System.out.println("  POST /api/odata/analyzer/analyze?directoryPath=<path>");
        System.out.println("  GET  /api/odata/analyzer/entitytype/{fullQualifiedName}");
        System.out.println("  GET  /api/odata/analyzer/complextype/{fullQualifiedName}");
        System.out.println("  GET  /api/odata/analyzer/enumtype/{fullQualifiedName}");
        System.out.println("  GET  /api/odata/analyzer/search?namePattern=<pattern>");
        System.out.println("  GET  /api/odata/analyzer/statistics");
        System.out.println("");
        System.out.println("Example usage:");
        System.out.println("  curl -X POST 'http://localhost:8080/api/odata/analyzer/analyze?directoryPath=/path/to/xml/files'");
        System.out.println("  curl 'http://localhost:8080/api/odata/analyzer/search?namePattern=Customer'");
        System.out.println("");
        
        // 如果提供了命令行参数，执行分析
        if (args.length > 0 && analysisService != null) {
            String directoryPath = args[0];
            System.out.println("Analyzing directory from command line: " + directoryPath);
            try {
                analysisService.performCompleteAnalysis(directoryPath);
            } catch (Exception e) {
                System.err.println("Analysis failed: " + e.getMessage());
            }
        }
    }
}
