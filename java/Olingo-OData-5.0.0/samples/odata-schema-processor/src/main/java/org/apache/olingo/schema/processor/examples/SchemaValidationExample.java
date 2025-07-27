package org.apache.olingo.schema.processor.examples;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.loader.SchemaDirectoryLoader;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.apache.olingo.schema.processor.parser.impl.CsdlXmlParserImpl;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import org.apache.olingo.schema.processor.repository.impl.InMemorySchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Schema验证示例
 * 演示如何从resources目录加载Schema并进行验证
 */
public class SchemaValidationExample {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaValidationExample.class);
    
    public static void main(String[] args) {
        SchemaValidationExample example = new SchemaValidationExample();
        example.runValidationExample();
    }
    
    public void runValidationExample() {
        logger.info("=== Schema Validation Example ===");
        
        try {
            // 1. 创建组件  
            ODataXmlParser xmlParser = new CsdlXmlParserImpl();
            SchemaRepository repository = new InMemorySchemaRepository();
            SchemaDirectoryLoader loader = new SchemaDirectoryLoader(xmlParser);
            
            // 2. 从resources目录加载Schema
            logger.info("Loading schemas from resources directory...");
            SchemaDirectoryLoader.LoadResult loadResult = loader.loadFromResources("schemas");
            
            // 3. 显示加载结果
            displayLoadResult(loadResult);
            
            if (loadResult.isSuccess()) {
                // 4. 添加到仓库
                logger.info("Adding schemas to repository...");
                SchemaRepository.AddResult addResult = repository.addSchemas(loadResult.getSchemas());
                displayAddResult(addResult);
                
                // 5. 验证仓库中的所有Schema
                logger.info("Validating all schemas in repository...");
                SchemaRepository.ValidationResult validationResult = repository.validateAll();
                displayValidationResult(validationResult);
                
                // 6. 显示仓库状态
                displayRepositoryStatus(repository);
            } else {
                logger.error("Failed to load schemas from resources");
            }
            
        } catch (Exception e) {
            logger.error("Error during schema validation example", e);
        }
        
        logger.info("=== Example completed ===");
    }
    
    /**
     * 显示加载结果
     */
    private void displayLoadResult(SchemaDirectoryLoader.LoadResult result) {
        logger.info("--- Load Result ---");
        logger.info("Success: {}", result.isSuccess());
        logger.info("Files loaded: {}", result.getFileCount());
        logger.info("Schemas found: {}", result.getSchemaCount());
        
        if (!result.getLoadedFiles().isEmpty()) {
            logger.info("Loaded files:");
            for (Path file : result.getLoadedFiles()) {
                logger.info("  - {}", file.getFileName());
            }
        }
        
        if (!result.getSchemas().isEmpty()) {
            logger.info("Schema namespaces:");
            for (CsdlSchema schema : result.getSchemas()) {
                logger.info("  - {}", schema.getNamespace());
            }
        }
        
        if (!result.getErrors().isEmpty()) {
            logger.warn("Load errors:");
            for (String error : result.getErrors()) {
                logger.warn("  - {}", error);
            }
        }
        
        if (!result.getWarnings().isEmpty()) {
            logger.warn("Load warnings:");
            for (String warning : result.getWarnings()) {
                logger.warn("  - {}", warning);
            }
        }
    }
    
    /**
     * 显示添加结果
     */
    private void displayAddResult(SchemaRepository.AddResult result) {
        logger.info("--- Add Result ---");
        logger.info("Success: {}", result.isSuccess());
        logger.info("Added count: {}", result.getAddedCount());
        logger.info("Conflict count: {}", result.getConflictCount());
        
        if (!result.getErrors().isEmpty()) {
            logger.warn("Add errors:");
            for (String error : result.getErrors()) {
                logger.warn("  - {}", error);
            }
        }
        
        if (!result.getWarnings().isEmpty()) {
            logger.warn("Add warnings:");
            for (String warning : result.getWarnings()) {
                logger.warn("  - {}", warning);
            }
        }
    }
    
    /**
     * 显示验证结果
     */
    private void displayValidationResult(SchemaRepository.ValidationResult result) {
        logger.info("--- Validation Result ---");
        logger.info("Valid: {}", result.isValid());
        
        if (!result.getErrors().isEmpty()) {
            logger.error("Validation errors:");
            for (String error : result.getErrors()) {
                logger.error("  - {}", error);
            }
        }
        
        if (!result.getWarnings().isEmpty()) {
            logger.warn("Validation warnings:");
            for (String warning : result.getWarnings()) {
                logger.warn("  - {}", warning);
            }
        }
    }
    
    /**
     * 显示仓库状态
     */
    private void displayRepositoryStatus(SchemaRepository repository) {
        logger.info("--- Repository Status ---");
        logger.info("Total schemas: {}", repository.getAllSchemas().size());
        logger.info("Available namespaces:");
        for (String namespace : repository.getAllNamespaces()) {
            logger.info("  - {}", namespace);
        }
    }
}
