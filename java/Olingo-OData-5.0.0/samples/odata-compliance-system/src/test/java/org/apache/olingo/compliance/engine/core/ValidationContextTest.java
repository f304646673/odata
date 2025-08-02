package org.apache.olingo.compliance.engine.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * ValidationContext的单元测试
 */
public class ValidationContextTest {
    
    private ValidationContext context;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        context = new ValidationContext("test-content", "test.xml");
    }
    
    @Test
    void testInitialState() {
        assertNotNull(context);
        assertEquals(0, context.getErrorCount());
        assertEquals(0, context.getWarningCount());
        assertTrue(context.getErrors().isEmpty());
        assertTrue(context.getWarnings().isEmpty());
    }
    
    @Test
    void testAddError() {
        context.addError("Error message");
        
        assertEquals(1, context.getErrorCount());
        assertEquals(0, context.getWarningCount());
        assertEquals(1, context.getErrors().size());
        
        String error = context.getErrors().get(0);
        assertEquals("Error message", error);
    }
    
    @Test
    void testAddErrorWithRule() {
        context.addError("rule.code", "Error message");
        
        assertEquals(1, context.getErrorCount());
        assertEquals(0, context.getWarningCount());
        assertEquals(1, context.getErrors().size());
        
        String error = context.getErrors().get(0);
        assertTrue(error.contains("rule.code"));
        assertTrue(error.contains("Error message"));
    }
    
    @Test
    void testAddWarning() {
        context.addWarning("Warning message");
        
        assertEquals(0, context.getErrorCount());
        assertEquals(1, context.getWarningCount());
        assertEquals(1, context.getWarnings().size());
        
        String warning = context.getWarnings().get(0);
        assertEquals("Warning message", warning);
    }
    
    @Test
    void testAddWarningWithRule() {
        context.addWarning("rule.code", "Warning message");
        
        assertEquals(0, context.getErrorCount());
        assertEquals(1, context.getWarningCount());
        assertEquals(1, context.getWarnings().size());
        
        String warning = context.getWarnings().get(0);
        assertTrue(warning.contains("rule.code"));
        assertTrue(warning.contains("Warning message"));
    }
    
    @Test
    void testMultipleIssues() {
        context.addError("Error 1");
        context.addError("Error 2");
        context.addWarning("Warning 1");
        
        assertEquals(2, context.getErrorCount());
        assertEquals(1, context.getWarningCount());
        assertEquals(2, context.getErrors().size());
        assertEquals(1, context.getWarnings().size());
    }
    
    @Test
    void testHasErrorsAndWarnings() {
        assertFalse(context.hasErrors());
        assertFalse(context.hasWarnings());
        
        context.addWarning("Warning");
        assertFalse(context.hasErrors());
        assertTrue(context.hasWarnings());
        
        context = new ValidationContext("test", "test.xml");
        context.addError("Error");
        assertTrue(context.hasErrors());
        assertFalse(context.hasWarnings());
    }
    
    @Test
    void testValidationServiceDelegation() {
        // 测试ValidationService相关方法
        assertNotNull(context.getValidationService());
        assertNotNull(context.getAllSchemas());
        assertTrue(context.getAllSchemas().isEmpty());
        
        // 测试namespace操作
        context.addReferencedNamespace("Test.Namespace");
        assertTrue(context.getReferencedNamespaces().contains("Test.Namespace"));
        
        context.addImportedNamespace("Imported.Namespace");
        assertTrue(context.getImportedNamespaces().contains("Imported.Namespace"));
        
        context.addCurrentSchemaNamespace("Current.Namespace");
        assertTrue(context.getCurrentSchemaNamespaces().contains("Current.Namespace"));
        
        // 测试metadata操作
        context.addMetadata("test.key", "test.value");
        assertEquals("test.value", context.getMetadata("test.key"));
        
        // 测试cache操作
        context.putCache("cache.key", "cache.value");
        assertEquals("cache.value", context.getCache("cache.key"));
        
        // 测试type kind操作
        context.addTypeKind("Test.Type", "EntityType");
        assertEquals("EntityType", context.getTypeKind("Test.Type"));
    }
    
    @Test
    void testPropertyManagement() {
        context.setProperty("prop1", "value1");
        context.setProperty("prop2", 42);
        
        assertEquals("value1", context.getProperty("prop1"));
        assertEquals(42, context.getProperty("prop2"));
        
        assertEquals("value1", context.getProperty("prop1", String.class));
        assertEquals(Integer.valueOf(42), context.getProperty("prop2", Integer.class));
        
        assertNull(context.getProperty("prop1", Integer.class)); // Wrong type
        assertNull(context.getProperty("nonexistent"));
    }
    
    @Test
    void testProcessingState() {
        assertFalse(context.isProcessingComplete());
        assertTrue(context.getProcessingTime() >= 0);
        
        context.markProcessingComplete();
        assertTrue(context.isProcessingComplete());
    }
    
    @Test
    void testFactoryMethods() throws IOException {
        // Test forFile
        File testFile = tempDir.resolve("test.xml").toFile();
        testFile.createNewFile();
        
        ValidationContext fileContext = ValidationContext.forFile(testFile.toPath());
        assertNotNull(fileContext);
        assertEquals("test.xml", fileContext.getFileName());
        assertEquals(testFile.toPath(), fileContext.getFilePath());
        
        // Test forContent
        ValidationContext contentContext = ValidationContext.forContent("content", "content.xml");
        assertNotNull(contentContext);
        assertEquals("content.xml", contentContext.getFileName());
        assertEquals("content", contentContext.getContent());
    }
    
    @Test
    void testToString() {
        String result = context.toString();
        assertNotNull(result);
        assertTrue(result.contains("test.xml"));
        assertTrue(result.contains("errors=0"));
        assertTrue(result.contains("warnings=0"));
    }
}
