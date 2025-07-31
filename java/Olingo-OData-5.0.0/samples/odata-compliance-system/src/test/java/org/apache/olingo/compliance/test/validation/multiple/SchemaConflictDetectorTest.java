package org.apache.olingo.compliance.test.validation.multiple;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.validator.directory.DirectoryValidationManager;
import org.apache.olingo.compliance.validator.directory.SchemaConflictDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Schema冲突检测器单元测试
 */
@DisplayName("Schema Conflict Detector Tests")
public class SchemaConflictDetectorTest {
    
    private SchemaConflictDetector conflictDetector;
    private Map<String, Set<DirectoryValidationManager.SchemaInfo>> testNamespaceMap;
    
    @BeforeEach
    void setUp() {
        conflictDetector = new SchemaConflictDetector();
        testNamespaceMap = new HashMap<>();
    }
    
    @Test
    @DisplayName("No Conflicts - Different Namespaces")
    void testNoConflictsDifferentNamespaces() {
        // 创建不同命名空间的Schema
        Set<String> customerElements = Set.of("EntityType:Customer", "ComplexType:Address");
        Set<String> productElements = Set.of("EntityType:Product", "ComplexType:ProductInfo");
        
        DirectoryValidationManager.SchemaInfo customerSchema = new DirectoryValidationManager.SchemaInfo(
            "Customer.Service", "CustomerSvc", "/path/customer.xml", customerElements
        );
        
        DirectoryValidationManager.SchemaInfo productSchema = new DirectoryValidationManager.SchemaInfo(
            "Product.Service", "ProductSvc", "/path/product.xml", productElements
        );
        
        testNamespaceMap.put("Customer.Service", Set.of(customerSchema));
        testNamespaceMap.put("Product.Service", Set.of(productSchema));
        
        List<ComplianceIssue> conflicts = conflictDetector.detectConflicts(testNamespaceMap);
        
        assertTrue(conflicts.isEmpty(), "No conflicts should be detected for different namespaces");
    }
    
    @Test
    @DisplayName("No Conflicts - Same Namespace Different Elements")
    void testNoConflictsSameNamespaceDifferentElements() {
        // 创建相同命名空间但不同元素的Schema
        Set<String> schema1Elements = Set.of("EntityType:Customer", "ComplexType:Address");
        Set<String> schema2Elements = Set.of("EntityType:Order", "ComplexType:OrderInfo");
        
        DirectoryValidationManager.SchemaInfo schema1 = new DirectoryValidationManager.SchemaInfo(
            "Shared.Models", null, "/path/file1.xml", schema1Elements
        );
        
        DirectoryValidationManager.SchemaInfo schema2 = new DirectoryValidationManager.SchemaInfo(
            "Shared.Models", null, "/path/file2.xml", schema2Elements
        );
        
        testNamespaceMap.put("Shared.Models", Set.of(schema1, schema2));
        
        List<ComplianceIssue> conflicts = conflictDetector.detectConflicts(testNamespaceMap);
        
        assertTrue(conflicts.isEmpty(), "No conflicts should be detected for different elements in same namespace");
    }
    
    @Test
    @DisplayName("Element Conflicts - Same Namespace Same Elements")
    void testElementConflictsSameNamespaceSameElements() {
        // 创建相同命名空间且有相同元素的Schema
        Set<String> schema1Elements = Set.of("EntityType:Customer", "ComplexType:Address");
        Set<String> schema2Elements = Set.of("EntityType:Customer", "ComplexType:ProductInfo"); // Customer冲突
        
        DirectoryValidationManager.SchemaInfo schema1 = new DirectoryValidationManager.SchemaInfo(
            "Conflicted.Models", null, "/path/file1.xml", schema1Elements
        );
        
        DirectoryValidationManager.SchemaInfo schema2 = new DirectoryValidationManager.SchemaInfo(
            "Conflicted.Models", null, "/path/file2.xml", schema2Elements
        );
        
        testNamespaceMap.put("Conflicted.Models", Set.of(schema1, schema2));
        
        List<ComplianceIssue> conflicts = conflictDetector.detectConflicts(testNamespaceMap);
        
        assertFalse(conflicts.isEmpty(), "Element conflicts should be detected");
        
        // 验证冲突类型
        boolean hasElementConflict = conflicts.stream()
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.ELEMENT_CONFLICT);
        assertTrue(hasElementConflict, "Should detect element conflicts");
        
        // 验证冲突消息包含Customer
        boolean hasCustomerConflict = conflicts.stream()
            .anyMatch(issue -> issue.getMessage().contains("Customer"));
        assertTrue(hasCustomerConflict, "Should detect Customer element conflict");
        
        System.out.println("Element Conflicts:");
        conflicts.forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
    
    @Test
    @DisplayName("Multiple Element Conflicts")
    void testMultipleElementConflicts() {
        // 创建多个元素冲突的场景
        Set<String> schema1Elements = Set.of("EntityType:Customer", "ComplexType:Address", "EntityType:Product");
        Set<String> schema2Elements = Set.of("EntityType:Customer", "ComplexType:Address", "EntityType:Order");
        
        DirectoryValidationManager.SchemaInfo schema1 = new DirectoryValidationManager.SchemaInfo(
            "Multi.Conflicted", null, "/path/file1.xml", schema1Elements
        );
        
        DirectoryValidationManager.SchemaInfo schema2 = new DirectoryValidationManager.SchemaInfo(
            "Multi.Conflicted", null, "/path/file2.xml", schema2Elements
        );
        
        testNamespaceMap.put("Multi.Conflicted", Set.of(schema1, schema2));
        
        List<ComplianceIssue> conflicts = conflictDetector.detectConflicts(testNamespaceMap);
        
        assertFalse(conflicts.isEmpty(), "Multiple element conflicts should be detected");
        
        // 应该检测到Customer和Address的冲突（每个冲突有2个issue，一个文件一个）
        long conflictCount = conflicts.stream()
            .map(issue -> issue.getMessage())
            .map(msg -> {
                if (msg.contains("Customer")) return "Customer";
                if (msg.contains("Address")) return "Address";
                return "Other";
            })
            .distinct()
            .count();
        
        assertTrue(conflictCount >= 2, "Should detect conflicts for both Customer and Address");
        
        System.out.println("Multiple Element Conflicts:");
        conflicts.forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
    
    @Test
    @DisplayName("Cross-Namespace Alias Conflicts")
    void testCrossNamespaceAliasConflicts() {
        // 创建跨命名空间别名冲突
        DirectoryValidationManager.SchemaInfo schema1 = new DirectoryValidationManager.SchemaInfo(
            "Service.V1", "SVC", "/path/service-v1.xml", Set.of("EntityType:Product")
        );
        
        DirectoryValidationManager.SchemaInfo schema2 = new DirectoryValidationManager.SchemaInfo(
            "Service.V2", "SVC", "/path/service-v2.xml", Set.of("EntityType:Customer") // 相同别名SVC
        );
        
        testNamespaceMap.put("Service.V1", Set.of(schema1));
        testNamespaceMap.put("Service.V2", Set.of(schema2));
        
        List<ComplianceIssue> conflicts = conflictDetector.detectCrossNamespaceAliasConflicts(testNamespaceMap);
        
        assertFalse(conflicts.isEmpty(), "Cross-namespace alias conflicts should be detected");
        
        // 验证冲突类型
        boolean hasAliasConflict = conflicts.stream()
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.ALIAS_CONFLICT);
        assertTrue(hasAliasConflict, "Should detect alias conflicts");
        
        // 验证冲突消息包含SVC别名信息
        boolean hasSVCAliasConflict = conflicts.stream()
            .anyMatch(issue -> issue.getMessage().contains("SVC"));
        assertTrue(hasSVCAliasConflict, "Should detect SVC alias conflict");
        
        System.out.println("Cross-Namespace Alias Conflicts:");
        conflicts.forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
    
    @Test
    @DisplayName("Generate Conflict Detection Report")
    void testGenerateConflictReport() {
        // 创建包含多种冲突的复杂场景
        
        // 元素冲突
        Set<String> schema1Elements = Set.of("EntityType:Customer", "ComplexType:Address");
        Set<String> schema2Elements = Set.of("EntityType:Customer", "ComplexType:ProductInfo");
        
        DirectoryValidationManager.SchemaInfo conflictSchema1 = new DirectoryValidationManager.SchemaInfo(
            "Conflict.Test", "CT1", "/path/conflict1.xml", schema1Elements
        );
        
        DirectoryValidationManager.SchemaInfo conflictSchema2 = new DirectoryValidationManager.SchemaInfo(
            "Conflict.Test", "CT2", "/path/conflict2.xml", schema2Elements
        );
        
        // 跨命名空间别名冲突
        DirectoryValidationManager.SchemaInfo aliasSchema1 = new DirectoryValidationManager.SchemaInfo(
            "Different.NS1", "ALIAS", "/path/alias1.xml", Set.of("EntityType:Type1")
        );
        
        DirectoryValidationManager.SchemaInfo aliasSchema2 = new DirectoryValidationManager.SchemaInfo(
            "Different.NS2", "ALIAS", "/path/alias2.xml", Set.of("EntityType:Type2")
        );
        
        testNamespaceMap.put("Conflict.Test", Set.of(conflictSchema1, conflictSchema2));
        testNamespaceMap.put("Different.NS1", Set.of(aliasSchema1));
        testNamespaceMap.put("Different.NS2", Set.of(aliasSchema2));
        
        SchemaConflictDetector.ConflictDetectionReport report = 
            conflictDetector.generateReport(testNamespaceMap);
        
        assertNotNull(report);
        assertTrue(report.hasConflicts(), "Report should indicate conflicts exist");
        assertTrue(report.getTotalConflictCount() > 0, "Report should show conflict count");
        
        // 验证报告包含不同类型的冲突
        assertFalse(report.getElementConflicts().isEmpty(), "Should have element conflicts");
        assertFalse(report.getCrossNamespaceAliasConflicts().isEmpty(), "Should have alias conflicts");
        
        System.out.println("Conflict Detection Report:");
        System.out.println(report.toString());
        System.out.println("All Conflicts:");
        report.getAllConflicts().forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
    
    @Test
    @DisplayName("Performance Test with Large Schema Set")
    void testPerformanceWithLargeSchemaSet() {
        // 创建大量Schema进行性能测试
        int namespaceCount = 100;
        int schemasPerNamespace = 5;
        int elementsPerSchema = 20;
        
        Random random = new Random(12345); // 固定种子确保可重复性
        
        for (int nsIndex = 0; nsIndex < namespaceCount; nsIndex++) {
            String namespace = "Performance.Test.NS" + nsIndex;
            Set<DirectoryValidationManager.SchemaInfo> schemas = new HashSet<>();
            
            for (int schemaIndex = 0; schemaIndex < schemasPerNamespace; schemaIndex++) {
                Set<String> elements = new HashSet<>();
                
                for (int elemIndex = 0; elemIndex < elementsPerSchema; elemIndex++) {
                    String elementType = random.nextBoolean() ? "EntityType" : "ComplexType";
                    String elementName = "Element" + elemIndex + "_Schema" + schemaIndex;
                    elements.add(elementType + ":" + elementName);
                }
                
                DirectoryValidationManager.SchemaInfo schema = new DirectoryValidationManager.SchemaInfo(
                    namespace,
                    "Alias" + nsIndex + "_" + schemaIndex,
                    "/path/ns" + nsIndex + "/schema" + schemaIndex + ".xml",
                    elements
                );
                
                schemas.add(schema);
            }
            
            testNamespaceMap.put(namespace, schemas);
        }
        
        long startTime = System.currentTimeMillis();
        List<ComplianceIssue> conflicts = conflictDetector.detectConflicts(testNamespaceMap);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        int totalSchemas = namespaceCount * schemasPerNamespace;
        
        System.out.println("Performance Test Results:");
        System.out.println("Total namespaces: " + namespaceCount);
        System.out.println("Total schemas: " + totalSchemas);
        System.out.println("Total elements: " + (totalSchemas * elementsPerSchema));
        System.out.println("Conflicts detected: " + conflicts.size());
        System.out.println("Detection time: " + duration + "ms");
        System.out.println("Average time per schema: " + (duration / (double) totalSchemas) + "ms");
        
        // 性能断言：检测时间应该合理（小于5秒）
        assertTrue(duration < 5000, "Conflict detection should complete within 5 seconds");
        
        // 应该没有冲突，因为每个Schema的元素名称都是唯一的
        assertTrue(conflicts.isEmpty(), "No conflicts should be detected in performance test");
    }
}
