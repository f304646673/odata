package org.apache.olingo.schema.processor.loader;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ODataValidator.extractNamespace() private method
 */
public class ODataValidatorExtractNamespaceTest {
    
    private ODataValidator validator;
    private Method extractNamespaceMethod;
    
    @Before
    public void setUp() throws Exception {
        validator = new ODataValidator();
        // Access private method via reflection
        extractNamespaceMethod = ODataValidator.class.getDeclaredMethod("extractNamespace", String.class);
        extractNamespaceMethod.setAccessible(true);
    }
    
    @Test
    public void testExtractNamespaceWithFullyQualifiedName() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "TestNamespace.EntityType");
        
        assertEquals("Should extract namespace correctly", "TestNamespace", result);
    }
    
    @Test
    public void testExtractNamespaceWithNestedNamespace() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "Company.Department.EntityType");
        
        assertEquals("Should extract nested namespace correctly", "Company.Department", result);
    }
    
    @Test
    public void testExtractNamespaceWithSimpleName() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "EntityType");
        
        assertNull("Should return null for simple name", result);
    }
    
    @Test
    public void testExtractNamespaceWithNullInput() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, (String) null);
        
        assertNull("Should return null for null input", result);
    }
    
    @Test
    public void testExtractNamespaceWithEmptyInput() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "");
        
        assertNull("Should return null for empty input", result);
    }
    
    @Test
    public void testExtractNamespaceWithWhitespaceInput() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, "   ");
        
        assertNull("Should return null for whitespace input", result);
    }
    
    @Test
    public void testExtractNamespaceWithDotAtStart() throws Exception {
        String result = (String) extractNamespaceMethod.invoke(validator, ".EntityType");
        
        // The actual implementation returns null, not empty string for ".EntityType"
        assertNull("Should return null for dot at start", result);
    }
}
