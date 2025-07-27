package org.apache.olingo.schema.processor.examples;

import org.apache.olingo.schema.processor.generator.SqlDdlGenerator;
import org.apache.olingo.schema.processor.loader.SchemaDirectoryLoader;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.apache.olingo.schema.processor.parser.impl.CsdlXmlParserImpl;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import org.apache.olingo.schema.processor.repository.impl.InMemorySchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SQL DDL生成示例
 * 演示如何从Schema中的EntityType生成数据库建表SQL语句
 * 支持多层继承和Collection类型处理
 */
public class SqlDdlGenerationExample {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlDdlGenerationExample.class);
    
    public static void main(String[] args) {
        SqlDdlGenerationExample example = new SqlDdlGenerationExample();
        example.runSqlGenerationExample();
    }
    
    public void runSqlGenerationExample() {
        logger.info("=== SQL DDL Generation Example ===");
        
        try {
            // 1. 创建组件
            ODataXmlParser xmlParser = new CsdlXmlParserImpl();
            SchemaRepository repository = new InMemorySchemaRepository();
            SchemaDirectoryLoader loader = new SchemaDirectoryLoader(xmlParser);
            
            // 2. 从resources目录加载Schema
            logger.info("Loading schemas from resources directory...");
            SchemaDirectoryLoader.LoadResult loadResult = loader.loadFromResources("schemas");
            
            if (!loadResult.isSuccess()) {
                logger.error("Failed to load schemas from resources");
                return;
            }
            
            logger.info("Successfully loaded {} schemas", loadResult.getSchemaCount());
            
            // 3. 添加到仓库
            repository.addSchemas(loadResult.getSchemas());
            
            // 4. 为不同数据库生成SQL DDL
            Path outputDir = Paths.get("target/generated-sql");
            
            SqlDdlGenerator.DatabaseDialect[] dialects = {
                SqlDdlGenerator.DatabaseDialect.MYSQL,
                SqlDdlGenerator.DatabaseDialect.POSTGRESQL,
                SqlDdlGenerator.DatabaseDialect.SQL_SERVER,
                SqlDdlGenerator.DatabaseDialect.H2
            };
            
            for (SqlDdlGenerator.DatabaseDialect dialect : dialects) {
                logger.info("Generating SQL DDL for {}...", dialect.name());
                
                SqlDdlGenerator sqlGenerator = new SqlDdlGenerator(outputDir, dialect);
                SqlDdlGenerator.GenerationResult result = 
                    sqlGenerator.generateFromSchemas(loadResult.getSchemas());
                
                displayGenerationResult(dialect.name(), result);
            }
            
            logger.info("SQL DDL output directory: {}", outputDir.toAbsolutePath());
            
        } catch (Exception e) {
            logger.error("Error during SQL DDL generation example", e);
        }
        
        logger.info("=== SQL DDL Generation Example completed ===");
    }
    
    /**
     * 显示生成结果
     */
    private void displayGenerationResult(String dialectName, SqlDdlGenerator.GenerationResult result) {
        logger.info("--- Generation Result for {} ---", dialectName);
        logger.info("Success: {}", result.isSuccess());
        logger.info("Generated files: {}", result.getGeneratedFiles().size());
        
        if (!result.getGeneratedFiles().isEmpty()) {
            logger.info("Generated SQL files:");
            for (String file : result.getGeneratedFiles()) {
                logger.info("  - {}", file);
            }
        }
        
        if (!result.getErrors().isEmpty()) {
            logger.warn("Generation errors:");
            for (String error : result.getErrors()) {
                logger.warn("  - {}", error);
            }
        }
        
        if (!result.getWarnings().isEmpty()) {
            logger.warn("Generation warnings:");
            for (String warning : result.getWarnings()) {
                logger.warn("  - {}", warning);
            }
        }
    }
}
