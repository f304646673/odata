package org.apache.olingo.xmlprocessor.demo;

import java.util.List;
import java.util.Set;

import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.dependency.GlobalDependencyManager;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlSchema;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser;
import org.apache.olingo.xmlprocessor.parser.impl.CsdlXmlParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 演示完整依赖管理功能的高级示例
 */
public class AdvancedDependencyDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedDependencyDemo.class);

    public static void main(String[] args) {
        logger.info("Starting Advanced Dependency Management Demo");
        
        try {
            // 创建处理器实例
            CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
            
            // 创建一个更复杂的schema，包含依赖关系
            String complexXmlContent = createComplexSchema();
            
            logger.info("Parsing complex schema with dependencies");
            
            ODataXmlParser.ParseResult result = parser.parseSchemas(complexXmlContent, "complex-demo");
            
            if (result.isSuccess() && result.getSchemas() != null && !result.getSchemas().isEmpty()) {
                List<ExtendedCsdlSchema> schemas = result.getSchemas();
                logger.info("Successfully parsed {} schema(s)", schemas.size());
                
                // 展示依赖管理功能
                demonstrateDependencyAnalysis(schemas);
                
                // 展示全局依赖管理器功能
                demonstrateGlobalDependencyManager();
                
            } else {
                logger.warn("Schema parsing failed");
            }
            
        } catch (Exception e) {
            logger.error("Error during advanced demo execution", e);
        }
        
        logger.info("Advanced Demo completed");
    }
    
    private static String createComplexSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
               "  <edmx:DataServices>\n" +
               "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"ComplexService\">\n" +
               "      \n" +
               "      <!-- 基础复杂类型 -->\n" +
               "      <ComplexType Name=\"Address\">\n" +
               "        <Property Name=\"Street\" Type=\"Edm.String\"/>\n" +
               "        <Property Name=\"City\" Type=\"Edm.String\"/>\n" +
               "        <Property Name=\"PostalCode\" Type=\"Edm.String\"/>\n" +
               "      </ComplexType>\n" +
               "      \n" +
               "      <!-- 枚举类型 -->\n" +
               "      <EnumType Name=\"ProductStatus\">\n" +
               "        <Member Name=\"Active\" Value=\"1\"/>\n" +
               "        <Member Name=\"Inactive\" Value=\"2\"/>\n" +
               "        <Member Name=\"Discontinued\" Value=\"3\"/>\n" +
               "      </EnumType>\n" +
               "      \n" +
               "      <!-- 实体类型 - 具有复杂依赖关系 -->\n" +
               "      <EntityType Name=\"Customer\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
               "        <Property Name=\"Address\" Type=\"ComplexService.Address\"/>\n" +
               "        <NavigationProperty Name=\"Orders\" Type=\"Collection(ComplexService.Order)\"/>\n" +
               "      </EntityType>\n" +
               "      \n" +
               "      <EntityType Name=\"Order\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"OrderDate\" Type=\"Edm.DateTimeOffset\"/>\n" +
               "        <Property Name=\"TotalAmount\" Type=\"Edm.Decimal\"/>\n" +
               "        <NavigationProperty Name=\"Customer\" Type=\"ComplexService.Customer\"/>\n" +
               "        <NavigationProperty Name=\"OrderItems\" Type=\"Collection(ComplexService.OrderItem)\"/>\n" +
               "      </EntityType>\n" +
               "      \n" +
               "      <EntityType Name=\"Product\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
               "        <Property Name=\"Price\" Type=\"Edm.Decimal\"/>\n" +
               "        <Property Name=\"Status\" Type=\"ComplexService.ProductStatus\"/>\n" +
               "        <NavigationProperty Name=\"OrderItems\" Type=\"Collection(ComplexService.OrderItem)\"/>\n" +
               "      </EntityType>\n" +
               "      \n" +
               "      <EntityType Name=\"OrderItem\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"Quantity\" Type=\"Edm.Int32\"/>\n" +
               "        <Property Name=\"UnitPrice\" Type=\"Edm.Decimal\"/>\n" +
               "        <NavigationProperty Name=\"Order\" Type=\"ComplexService.Order\"/>\n" +
               "        <NavigationProperty Name=\"Product\" Type=\"ComplexService.Product\"/>\n" +
               "      </EntityType>\n" +
               "      \n" +
               "      <!-- 实体容器 -->\n" +
               "      <EntityContainer Name=\"Container\">\n" +
               "        <EntitySet Name=\"Customers\" EntityType=\"ComplexService.Customer\"/>\n" +
               "        <EntitySet Name=\"Orders\" EntityType=\"ComplexService.Order\"/>\n" +
               "        <EntitySet Name=\"Products\" EntityType=\"ComplexService.Product\"/>\n" +
               "        <EntitySet Name=\"OrderItems\" EntityType=\"ComplexService.OrderItem\"/>\n" +
               "      </EntityContainer>\n" +
               "      \n" +
               "    </Schema>\n" +
               "  </edmx:DataServices>\n" +
               "</edmx:Edmx>";
    }
    
    private static void demonstrateDependencyAnalysis(List<ExtendedCsdlSchema> schemas) {
        logger.info("=== 依赖关系分析 ===");
        
        for (ExtendedCsdlSchema schema : schemas) {
            logger.info("Schema: {} (Namespace: {})", schema.getClass().getSimpleName(), schema.getNamespace());
            
            // 分析实体类型依赖
            if (schema.getEntityTypes() != null) {
                logger.info("  实体类型依赖分析:");
                schema.getEntityTypes().forEach(entityType -> {
                    logger.info("    Entity: {}", entityType.getName());
                    
                    // 分析属性依赖（复杂类型、枚举类型等）
                    if (entityType.getProperties() != null) {
                        entityType.getProperties().forEach(property -> {
                            String propertyType = property.getType();
                            if (propertyType != null && propertyType.contains(".")) {
                                logger.info("      Property '{}' depends on type: {}", 
                                           property.getName(), propertyType);
                            }
                        });
                    }
                    
                    // 分析导航属性依赖
                    if (entityType.getNavigationProperties() != null) {
                        entityType.getNavigationProperties().forEach(navProp -> {
                            logger.info("      Navigation '{}' depends on: {}", 
                                       navProp.getName(), navProp.getType());
                        });
                    }
                });
            }
            
            // 分析复杂类型
            if (schema.getComplexTypes() != null) {
                logger.info("  复杂类型: {}", schema.getComplexTypes().size());
                schema.getComplexTypes().forEach(complexType -> {
                    logger.info("    Complex Type: {}", complexType.getName());
                });
            }
            
            // 分析枚举类型
            if (schema.getEnumTypes() != null) {
                logger.info("  枚举类型: {}", schema.getEnumTypes().size());
                schema.getEnumTypes().forEach(enumType -> {
                    logger.info("    Enum Type: {}", enumType.getName());
                });
            }
        }
    }
    
    private static void demonstrateGlobalDependencyManager() {
        logger.info("=== 全局依赖管理器演示 ===");
        
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        
        // 获取所有已注册的节点
        Set<CsdlDependencyNode> allNodes = manager.getAllElements();
        logger.info("已注册的依赖节点总数: {}", allNodes.size());
        
        // 展示部分节点的依赖关系
        allNodes.stream().limit(5).forEach(node -> {
            String nodeId = node.getElementId();
            Set<CsdlDependencyNode> directDeps = manager.getDirectDependencies(nodeId);
            Set<CsdlDependencyNode> directDependents = manager.getDirectDependents(nodeId);
            
            logger.info("节点: {} (类型: {})", nodeId, node.getDependencyType());
            logger.info("  直接依赖数: {}", directDeps.size());
            logger.info("  被依赖数: {}", directDependents.size());
            
            // 展示循环依赖检测
            boolean hasCycles = manager.hasCircularDependency(nodeId);
            if (hasCycles) {
                logger.warn("  检测到循环依赖!");
            }
        });
        
        // 展示依赖图统计
        logger.info("依赖图统计:");
        logger.info("  根节点数: {}", manager.getRootNodes().size());
        logger.info("  叶子节点数: {}", manager.getLeafNodes().size());
        logger.info("  有循环依赖: {}", manager.hasCircularDependencies());
        
        // 展示统计信息
        logger.info("详细统计: {}", manager.getStatistics());
        
        // 清理（演示用）
        logger.info("清理依赖管理器...");
        manager.clear();
        logger.info("清理完成，剩余节点数: {}", manager.getAllElements().size());
    }
}
