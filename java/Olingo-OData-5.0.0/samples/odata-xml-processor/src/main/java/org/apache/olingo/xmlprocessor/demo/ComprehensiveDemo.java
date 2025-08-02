package org.apache.olingo.xmlprocessor.demo;

import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlEntityType;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlNavigationProperty;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlProperty;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlSchema;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser;
import org.apache.olingo.xmlprocessor.parser.impl.CsdlXmlParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 综合功能演示 - 展示扩展模型的组合模式和联动功能
 */
public class ComprehensiveDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveDemo.class);

    public static void main(String[] args) {
        logger.info("Starting Comprehensive Demo");
        
        try {
            // 演示1：组合模式的数据联动
            demonstrateCompositionPattern();
            
            // 演示2：解析和依赖分析
            demonstrateParsingAndDependencies();
            
            // 演示3：错误处理和验证
            demonstrateErrorHandling();
            
        } catch (Exception e) {
            logger.error("Error during comprehensive demo", e);
        }
        
        logger.info("Comprehensive Demo completed");
    }
    
    private static void demonstrateCompositionPattern() {
        logger.info("=== 组合模式演示 ===");
        
        // 创建一个扩展实体类型
        ExtendedCsdlEntityType extEntityType = new ExtendedCsdlEntityType();
        
        // 直接设置属性，应该同步到内部CsdlEntityType
        extEntityType.setName("DemoEntity");
        extEntityType.setHasStream(true);
        
        logger.info("ExtendedEntityType Name: {}", extEntityType.getName());
        logger.info("ExtendedEntityType HasStream: {}", extEntityType.hasStream());
        
        // 验证内部CsdlEntityType是否同步
        logger.info("Internal CsdlEntityType Name: {}", extEntityType.asCsdlEntityType().getName());
        logger.info("Internal CsdlEntityType HasStream: {}", extEntityType.asCsdlEntityType().hasStream());
        
        // 添加属性测试
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        extProperty.setName("DemoProperty");
        extProperty.setType("Edm.String");
        extProperty.setNullable(false);
        
        extEntityType.getProperties().add(extProperty.asCsdlProperty());
        logger.info("Added property: {} (Type: {}, Nullable: {})", 
                   extProperty.getName(), extProperty.getType(), extProperty.isNullable());
        
        // 验证属性是否正确同步到内部对象
        if (extEntityType.asCsdlEntityType().getProperties() != null) {
            logger.info("Internal properties count: {}", 
                       extEntityType.asCsdlEntityType().getProperties().size());
        }
        
        // 添加导航属性测试
        ExtendedCsdlNavigationProperty extNavProp = new ExtendedCsdlNavigationProperty();
        extNavProp.setName("DemoNavigation");
        extNavProp.setType("Collection(Demo.RelatedEntity)");
        
        extEntityType.getNavigationProperties().add(extNavProp.asCsdlNavigationProperty());
        logger.info("Added navigation property: {} (Type: {})", 
                   extNavProp.getName(), extNavProp.getType());
    }
    
    private static void demonstrateParsingAndDependencies() {
        logger.info("=== 解析和依赖分析演示 ===");
        
        CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
        
        String testXml = createTestSchema();
        
        ODataXmlParser.ParseResult result = parser.parseSchemas(testXml, "comprehensive-test");
        
        if (result.isSuccess() && result.getSchemas() != null) {
            logger.info("成功解析Schema，数量: {}", result.getSchemas().size());
            
            for (ExtendedCsdlSchema schema : result.getSchemas()) {
                logger.info("Schema命名空间: {}", schema.getNamespace());
                
                // 分析实体类型
                if (schema.getEntityTypes() != null) {
                    logger.info("  实体类型数: {}", schema.getEntityTypes().size());
                    
                    schema.getEntityTypes().forEach(entityType -> {
                        logger.info("    Entity: {}", entityType.getName());
                        
                        // 展示属性信息
                        if (entityType.getProperties() != null) {
                            logger.info("      属性数: {}", entityType.getProperties().size());
                            entityType.getProperties().forEach(prop -> {
                                logger.info("        Property: {} (Type: {})", 
                                           prop.getName(), prop.getType());
                            });
                        }
                        
                        // 展示导航属性信息
                        if (entityType.getNavigationProperties() != null) {
                            logger.info("      导航属性数: {}", entityType.getNavigationProperties().size());
                            entityType.getNavigationProperties().forEach(navProp -> {
                                logger.info("        NavProperty: {} (Type: {})", 
                                           navProp.getName(), navProp.getType());
                            });
                        }
                    });
                }
                
                // 分析复杂类型
                if (schema.getComplexTypes() != null) {
                    logger.info("  复杂类型数: {}", schema.getComplexTypes().size());
                }
                
                // 分析枚举类型
                if (schema.getEnumTypes() != null) {
                    logger.info("  枚举类型数: {}", schema.getEnumTypes().size());
                }
                
                // 分析实体容器
                if (schema.getEntityContainer() != null) {
                    logger.info("  实体容器: {}", schema.getEntityContainer().getName());
                }
            }
        } else {
            logger.warn("Schema解析失败");
            if (result.getErrors() != null) {
                result.getErrors().forEach(error -> 
                    logger.error("解析错误: {}", error));
            }
        }
    }
    
    private static void demonstrateErrorHandling() {
        logger.info("=== 错误处理演示 ===");
        
        CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
        
        // 测试无效XML
        String invalidXml = "<?xml version=\"1.0\"?><invalid>test</invalid>";
        
        ODataXmlParser.ParseResult result = parser.parseSchemas(invalidXml, "error-test");
        
        if (!result.isSuccess()) {
            logger.info("预期的解析失败，错误数: {}", result.getErrors().size());
            result.getErrors().forEach(error -> {
                logger.info("  错误: {}", error);
            });
        }
        
        // 测试空内容
        ODataXmlParser.ParseResult emptyResult = parser.parseSchemas("", "empty-test");
        
        if (!emptyResult.isSuccess()) {
            logger.info("空内容解析失败，符合预期");
        }
        
        // 测试null内容 - 明确指定String版本
        ODataXmlParser.ParseResult nullResult = parser.parseSchemas((String)null, "null-test");
        
        if (!nullResult.isSuccess()) {
            logger.info("null内容解析失败，符合预期");
        }
    }
    
    private static String createTestSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
               "  <edmx:DataServices>\n" +
               "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"ComprehensiveTest\">\n" +
               "      \n" +
               "      <!-- 复杂类型 -->\n" +
               "      <ComplexType Name=\"Address\">\n" +
               "        <Property Name=\"Street\" Type=\"Edm.String\"/>\n" +
               "        <Property Name=\"City\" Type=\"Edm.String\"/>\n" +
               "      </ComplexType>\n" +
               "      \n" +
               "      <!-- 枚举类型 -->\n" +
               "      <EnumType Name=\"Status\">\n" +
               "        <Member Name=\"Active\" Value=\"1\"/>\n" +
               "        <Member Name=\"Inactive\" Value=\"2\"/>\n" +
               "      </EnumType>\n" +
               "      \n" +
               "      <!-- 实体类型 -->\n" +
               "      <EntityType Name=\"Person\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
               "        <Property Name=\"Address\" Type=\"ComprehensiveTest.Address\"/>\n" +
               "        <Property Name=\"Status\" Type=\"ComprehensiveTest.Status\"/>\n" +
               "        <NavigationProperty Name=\"Orders\" Type=\"Collection(ComprehensiveTest.Order)\"/>\n" +
               "      </EntityType>\n" +
               "      \n" +
               "      <EntityType Name=\"Order\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"OrderDate\" Type=\"Edm.DateTimeOffset\"/>\n" +
               "        <NavigationProperty Name=\"Customer\" Type=\"ComprehensiveTest.Person\"/>\n" +
               "      </EntityType>\n" +
               "      \n" +
               "      <!-- 实体容器 -->\n" +
               "      <EntityContainer Name=\"Container\">\n" +
               "        <EntitySet Name=\"People\" EntityType=\"ComprehensiveTest.Person\"/>\n" +
               "        <EntitySet Name=\"Orders\" EntityType=\"ComprehensiveTest.Order\"/>\n" +
               "      </EntityContainer>\n" +
               "      \n" +
               "    </Schema>\n" +
               "  </edmx:DataServices>\n" +
               "</edmx:Edmx>";
    }
}
