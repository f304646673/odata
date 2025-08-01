package org.apache.olingo.xmlprocessor.demo;

import java.util.List;

import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlSchema;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser;
import org.apache.olingo.xmlprocessor.parser.impl.CsdlXmlParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 演示OData XML处理器功能的主类
 */
public class MainDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(MainDemo.class);

    public static void main(String[] args) {
        logger.info("Starting OData XML Processor Demo");
        
        try {
            // 创建处理器实例
            CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
            
            // 测试解析有效的模式文件
            String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                               "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                               "  <edmx:DataServices>\n" +
                               "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"TestService\">\n" +
                               "      <EntityType Name=\"Product\">\n" +
                               "        <Key>\n" +
                               "          <PropertyRef Name=\"ID\"/>\n" +
                               "        </Key>\n" +
                               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
                               "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
                               "      </EntityType>\n" +
                               "    </Schema>\n" +
                               "  </edmx:DataServices>\n" +
                               "</edmx:Edmx>";
            
            logger.info("Parsing schema from XML content");
            
            ODataXmlParser.ParseResult result = parser.parseSchemas(xmlContent, "demo-content");
            
            if (result.isSuccess() && result.getSchemas() != null && !result.getSchemas().isEmpty()) {
                List<ExtendedCsdlSchema> schemas = result.getSchemas();
                logger.info("Successfully parsed {} schema(s)", schemas.size());
                
                for (ExtendedCsdlSchema schema : schemas) {
                    logger.info("Schema: {} - Namespace: {}", schema.getClass().getSimpleName(), schema.getNamespace());
                    logger.info("  Entity Types: {}", schema.getEntityTypes() != null ? schema.getEntityTypes().size() : 0);
                    logger.info("  Complex Types: {}", schema.getComplexTypes() != null ? schema.getComplexTypes().size() : 0);
                    logger.info("  Enum Types: {}", schema.getEnumTypes() != null ? schema.getEnumTypes().size() : 0);
                }
            } else {
                logger.warn("No schemas found or parsing failed");
                if (!result.isSuccess()) {
                    logger.error("Parse failed");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error during demo execution", e);
        }
        
        logger.info("Demo completed");
    }
}
