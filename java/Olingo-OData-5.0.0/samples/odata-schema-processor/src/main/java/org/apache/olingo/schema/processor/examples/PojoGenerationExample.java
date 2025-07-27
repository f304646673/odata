package org.apache.olingo.schema.processor.examples;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.generator.PojoGenerator;
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
 * POJO生成示例
 * 演示如何从Schema中的EntityType生成对应的Java POJO类
 */
public class PojoGenerationExample {
    
    private static final Logger logger = LoggerFactory.getLogger(PojoGenerationExample.class);
    
    public static void main(String[] args) {
        PojoGenerationExample example = new PojoGenerationExample();
        example.runPojoGenerationExample();
    }
    
    public void runPojoGenerationExample() {
        logger.info("=== POJO Generation Example ===");
        
        try {
            // 1. 创建组件
            ODataXmlParser xmlParser = new CsdlXmlParserImpl();
            SchemaRepository repository = new InMemorySchemaRepository();
            SchemaDirectoryLoader loader = new SchemaDirectoryLoader(xmlParser);
            
            // 设置输出目录和基础包名
            Path outputDir = Paths.get("target/generated-sources/odata-pojos");
            String basePackage = "org.example.generated";
            PojoGenerator pojoGenerator = new PojoGenerator(outputDir, basePackage);
            
            // 2. 从resources目录加载Schema
            logger.info("Loading schemas from resources directory...");
            SchemaDirectoryLoader.LoadResult loadResult = loader.loadFromResources("schemas");
            
            if (!loadResult.isSuccess()) {
                logger.error("Failed to load schemas:");
                for (String error : loadResult.getErrors()) {
                    logger.error("  - {}", error);
                }
                return;
            }
            
            logger.info("Successfully loaded {} schemas", loadResult.getSchemaCount());
            
            // 3. 添加到仓库
            repository.addSchemas(loadResult.getSchemas());
            
            logger.info("POJO output directory: {}", outputDir.toAbsolutePath());
            
            // 4. 生成所有Schema的POJO（考虑继承关系）
            logger.info("Generating POJOs with inheritance support...");
            try {
                PojoGenerator.GenerationResult result = 
                    pojoGenerator.generateFromSchemas(loadResult.getSchemas());
                
                displayGenerationResult("All Schemas", result);
                
            } catch (Exception e) {
                logger.error("Failed to generate POJOs: {}", e.getMessage(), e);
            }
            
        } catch (Exception e) {
            logger.error("Error during POJO generation example", e);
        }
        
        logger.info("=== POJO Generation Example completed ===");
    }
    
    /**
     * 显示生成结果
     */
    private void displayGenerationResult(String schemaName, PojoGenerator.GenerationResult result) {
        logger.info("--- Generation Result for {} ---", schemaName);
        logger.info("Success: {}", result.isSuccess());
        logger.info("Generated files: {}", result.getGeneratedFiles().size());
        
        if (!result.getGeneratedFiles().isEmpty()) {
            logger.info("Generated POJO files:");
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
    }
}
