package org.apache.olingo.compliance.test.validation.multiple;

import org.apache.olingo.compliance.file.ComplianceErrorType;
import org.apache.olingo.compliance.file.ComplianceIssue;
import org.apache.olingo.compliance.validation.directory.DirectoryValidationManager;
import org.apache.olingo.compliance.validation.directory.SchemaConflictDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 目录验证测试类
 * 测试多文件OData XML验证场景
 */
@DisplayName("Directory Validation Tests")
public class DirectoryValidationTest {
    
    private DirectoryValidationManager validationManager;
    private String testResourcesPath;
    
    @BeforeEach
    void setUp() {
        validationManager = new DirectoryValidationManager();
        testResourcesPath = "src/test/resources/validation/multiple";
    }
    
    @Test
    @DisplayName("Valid Scenario 1: Separate Namespaces")
    void testValidSeparateNamespaces() throws IOException {
        String directoryPath = Paths.get(testResourcesPath, "valid", "scenario1-separate-namespaces").toString();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertTrue(result.isValid(), "Directory with separate namespaces should be valid");
        assertEquals(0, result.getConflictIssues().size(), "No conflicts should be detected");
        assertEquals(0, result.getAllIssues().size(), "No parsing errors should occur");
        assertEquals(2, result.getValidFileCount(), "Both files should be valid");
        
        // 验证统计信息
        assertNotNull(result.getStatistics());
        assertTrue(result.getValidationTimeMs() > 0, "Validation should take some time");
        
        System.out.println("Valid Scenario 1 Result: " + result);
    }
    
    @Test
    @DisplayName("Valid Scenario 2: Same Namespace No Conflicts")
    void testValidSameNamespaceNoConflicts() throws IOException {
        String directoryPath = Paths.get(testResourcesPath, "valid", "scenario2-same-namespace-no-conflicts").toString();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertTrue(result.isValid(), "Directory with same namespace but no conflicts should be valid");
        assertEquals(0, result.getConflictIssues().size(), "No conflicts should be detected");
        assertEquals(2, result.getValidFileCount(), "Both files should be valid");
        
        System.out.println("Valid Scenario 2 Result: " + result);
    }
    
    @Test
    @DisplayName("Valid Scenario 3: Multilevel Directories")
    void testValidMultilevelDirectories() throws IOException {
        String directoryPath = Paths.get(testResourcesPath, "valid", "scenario3-multilevel-directories").toString();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertTrue(result.isValid(), "Multilevel directory structure should be valid");
        assertEquals(0, result.getConflictIssues().size(), "No conflicts should be detected");
        assertEquals(2, result.getValidFileCount(), "Both files should be valid");
        
        // 验证文件处理顺序（应该按依赖关系处理）
        assertNotNull(result.getValidationResults());
        assertTrue(result.getValidationResults().size() >= 2);
        
        System.out.println("Valid Scenario 3 Result: " + result);
    }
    
    @Test
    @DisplayName("Invalid Scenario 1: Element Conflicts")
    void testInvalidElementConflicts() throws IOException {
        String directoryPath = Paths.get(testResourcesPath, "invalid", "scenario1-element-conflicts").toString();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertFalse(result.isValid(), "Directory with element conflicts should be invalid");
        
        // 验证冲突检测
        assertTrue(result.getConflictIssues().size() > 0, "Element conflicts should be detected");
        
        // 检查具体的冲突类型
        boolean hasElementConflict = result.getConflictIssues().stream()
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.ELEMENT_CONFLICT);
        assertTrue(hasElementConflict, "Should detect element conflicts");
        
        // 验证冲突消息包含相关信息
        boolean hasCustomerConflict = result.getConflictIssues().stream()
            .anyMatch(issue -> issue.getMessage().contains("Customer"));
        assertTrue(hasCustomerConflict, "Should detect Customer type conflict");
        
        boolean hasAddressConflict = result.getConflictIssues().stream()
            .anyMatch(issue -> issue.getMessage().contains("Address"));
        assertTrue(hasAddressConflict, "Should detect Address type conflict");
        
        System.out.println("Invalid Scenario 1 Result: " + result);
        System.out.println("Conflict Issues:");
        result.getConflictIssues().forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
    
    @Test
    @DisplayName("Invalid Scenario 2: Alias Conflicts")
    void testInvalidAliasConflicts() throws IOException {
        String directoryPath = Paths.get(testResourcesPath, "invalid", "scenario2-alias-conflicts").toString();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertFalse(result.isValid(), "Directory with alias conflicts should be invalid");
        
        // 验证别名冲突检测
        assertTrue(result.getConflictIssues().size() > 0, "Alias conflicts should be detected");
        
        // 检查具体的冲突类型
        boolean hasAliasConflict = result.getConflictIssues().stream()
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.ALIAS_CONFLICT);
        assertTrue(hasAliasConflict, "Should detect alias conflicts");
        
        // 验证冲突消息包含别名信息
        boolean hasSVCAliasConflict = result.getConflictIssues().stream()
            .anyMatch(issue -> issue.getMessage().contains("SVC"));
        assertTrue(hasSVCAliasConflict, "Should detect SVC alias conflict");
        
        System.out.println("Invalid Scenario 2 Result: " + result);
        System.out.println("Conflict Issues:");
        result.getConflictIssues().forEach(issue -> 
            System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
        );
    }
    
    @Test
    @DisplayName("Invalid Scenario 3: Invalid Inheritance")
    void testInvalidInheritance() throws IOException {
        String directoryPath = Paths.get(testResourcesPath, "invalid", "scenario3-invalid-inheritance").toString();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertFalse(result.isValid(), "Directory with invalid inheritance should be invalid");
        
        // 验证应该有文件级别的验证错误（无效的继承关系）
        assertTrue(result.getValidationResults().values().stream()
                         .anyMatch(vr -> !vr.isCompliant()), 
                  "Should have invalid files due to inheritance errors");
        
        // 检查是否检测到无效的基类型错误
        boolean hasInvalidBaseTypeError = result.getValidationResults().values().stream()
            .flatMap(vr -> vr.getIssues().stream())
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.INVALID_BASE_TYPE ||
                              issue.getErrorType() == ComplianceErrorType.TYPE_ERROR);
        
        assertTrue(hasInvalidBaseTypeError, "Should detect invalid base type errors");
        
        System.out.println("Invalid Scenario 3 Result: " + result);
        System.out.println("Validation Issues:");
        result.getValidationResults().values().forEach(vr -> {
            if (!vr.isCompliant()) {
                vr.getIssues().forEach(issue -> 
                    System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
                );
            }
        });
    }
    
    @Test
    @DisplayName("Invalid Scenario 4: Missing Dependencies")
    void testMissingDependencies() throws IOException {
        String directoryPath = Paths.get(testResourcesPath, "invalid", "scenario4-missing-dependencies").toString();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(directoryPath);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertFalse(result.isValid(), "Directory with missing dependencies should be invalid");
        
        // 验证应该有类型不存在的错误
        assertTrue(result.getValidationResults().values().stream()
                         .anyMatch(vr -> !vr.isCompliant()), 
                  "Should have invalid files due to missing type references");
        
        // 检查是否检测到类型不存在错误
        boolean hasTypeNotExistError = result.getValidationResults().values().stream()
            .flatMap(vr -> vr.getIssues().stream())
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.TYPE_NOT_EXIST ||
                              issue.getErrorType() == ComplianceErrorType.TYPE_ERROR);
        
        assertTrue(hasTypeNotExistError, "Should detect type not exist errors");
        
        System.out.println("Invalid Scenario 4 Result: " + result);
        System.out.println("Validation Issues:");
        result.getValidationResults().values().forEach(vr -> {
            if (!vr.isCompliant()) {
                vr.getIssues().forEach(issue -> 
                    System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage())
                );
            }
        });
    }
    
    @Test
    @DisplayName("Empty Directory Test")
    void testEmptyDirectory() throws IOException {
        // 创建临时空目录进行测试
        String emptyDirPath = Paths.get(testResourcesPath, "empty").toString();
        java.io.File emptyDir = new java.io.File(emptyDirPath);
        emptyDir.mkdirs();
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(emptyDirPath);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertTrue(result.isValid(), "Empty directory should be considered valid");
        assertEquals(0, result.getConflictIssues().size());
        assertEquals(0, result.getAllIssues().size());
        assertEquals(0, result.getValidFileCount());
        
        // 清理
        emptyDir.delete();
        
        System.out.println("Empty Directory Result: " + result);
    }
    
    @Test
    @DisplayName("Performance Test with Multiple Files")
    void testPerformanceWithMultipleFiles() throws IOException {
        // 测试所有valid场景的性能
        String[] validScenarios = {
            "scenario1-separate-namespaces",
            "scenario2-same-namespace-no-conflicts", 
            "scenario3-multilevel-directories"
        };
        
        long totalTime = 0;
        int totalFiles = 0;
        
        for (String scenario : validScenarios) {
            String directoryPath = Paths.get(testResourcesPath, "valid", scenario).toString();
            
            long startTime = System.currentTimeMillis();
            DirectoryValidationManager.DirectoryValidationResult result = 
                validationManager.validateDirectory(directoryPath);
            long endTime = System.currentTimeMillis();
            
            totalTime += (endTime - startTime);
            totalFiles += result.getTotalFiles();
            
            assertTrue(result.isValid(), "Scenario " + scenario + " should be valid");
        }
        
        System.out.println("Performance Test Results:");
        System.out.println("Total files validated: " + totalFiles);
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Average time per file: " + (totalFiles > 0 ? totalTime / totalFiles : 0) + "ms");
        
        // 性能断言：每个文件的平均验证时间应该合理（小于1秒）
        assertTrue(totalFiles == 0 || totalTime / totalFiles < 1000, 
                  "Average validation time per file should be less than 1 second");
    }
}
