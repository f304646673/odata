package org.apache.olingo.schema.processor.loader;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ODataValidator.extractNamespace() private method
 */
public class ODataValidatorExtractNamespaceTest {
    
    private ODataValidator validator;
    private Method extractNamespaceMethod;
    
    @BeforeEach
    public void setUp() throws Exception {
        validator = new ODataValidator();
        // Access private method via reflection
        extractNamespaceMethod = ODataValidator.class.getDeclaredMethod("extractNamespace", String.class);
        extractNamespaceMethod.setAccessible(true);
    }
    
    @Test
    public void testExtractNamespaceWithFullyQualifiedName() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "TestNamespace.EntityType");
        
        assertEquals("TestNamespace", result, "Should extract namespace correctly");
    }
    
    @Test
    public void testExtractNamespaceWithNestedNamespace() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "Company.Department.EntityType");
        
        assertEquals("Company.Department", result, "Should extract nested namespace correctly");
    }
    
    @Test
    public void testExtractNamespaceWithSimpleName() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "EntityType");
        
        assertNull(result, "Should return null for simple name");
    }
    
    @Test
    public void testExtractNamespaceWithNullInput() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, (String) null);
        
        assertNull(result, "Should return null for null input");
    }
    
    @Test
    public void testExtractNamespaceWithEmptyInput() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "");
        
        assertNull(result, "Should return null for empty input");
    }
    
    @Test
    public void testExtractNamespaceWithWhitespaceInput() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "   ");
        
        assertNull(result, "Should return null for whitespace input");
    }
    
    @Test
    public void testExtractNamespaceWithDotAtStart() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, ".EntityType");
        
        // The actual implementation returns null, not empty string for ".EntityType"
        assertNull(result, "Should return null for dot at start");
    }
}
