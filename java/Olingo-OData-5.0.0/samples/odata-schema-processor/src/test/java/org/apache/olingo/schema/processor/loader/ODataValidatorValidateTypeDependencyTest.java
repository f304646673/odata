package org.apache.olingo.schema.processor.loader;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ODataValidator.validateTypeDependency() private method
 */
public class ODataValidatorValidateTypeDependencyTest {
    
    private ODataValidator validator;
    private Method validateTypeDependencyMethod;
    
    @Before
    public void setUp() throws Exception {
        validator = new ODataValidator();
        // Access private method via reflection
        validateTypeDependencyMethod = ODataValidator.class.getDeclaredMethod("validateTypeDependency", 
                String.class, String.class, Set.class);
        validateTypeDependencyMethod.setAccessible(true);
    }
    
    @Test
    public void testValidateTypeDependencyWithPrimitiveType() throws Exception {
        Set<String> dependencies = new HashSet<>();
        
        validateTypeDependencyMethod.invoke(validator, "Edm.String", "TestNamespace", dependencies);
        
        assertTrue("Should have no dependencies for primitive type", dependencies.isEmpty());
    }
    
    @Test
    public void testValidateTypeDependencyWithSameNamespace() throws Exception {
        Set<String> dependencies = new HashSet<>();
        
        validateTypeDependencyMethod.invoke(validator, "TestNamespace.EntityType", "TestNamespace", dependencies);
        
        assertTrue("Should have no dependencies for same namespace", dependencies.isEmpty());
    }
    
    @Test
    public void testValidateTypeDependencyWithDifferentNamespace() throws Exception {
        Set<String> dependencies = new HashSet<>();
        
        validateTypeDependencyMethod.invoke(validator, "OtherNamespace.EntityType", "TestNamespace", dependencies);
        
        assertTrue("Should add dependency for different namespace", dependencies.contains("OtherNamespace"));
    }
    
    @Test
    public void testValidateTypeDependencyWithCollectionType() throws Exception {
        Set<String> dependencies = new HashSet<>();
        
        validateTypeDependencyMethod.invoke(validator, "Collection(OtherNamespace.EntityType)", "TestNamespace", dependencies);
        
        assertTrue("Should add dependency for collection type", dependencies.contains("OtherNamespace"));
    }
    
    @Test
    public void testValidateTypeDependencyWithNullType() throws Exception {
        Set<String> dependencies = new HashSet<>();
        
        validateTypeDependencyMethod.invoke(validator, null, "TestNamespace", dependencies);
        
        assertTrue("Should have no dependencies for null type", dependencies.isEmpty());
    }
    
    @Test
    public void testValidateTypeDependencyWithEmptyType() throws Exception {
        Set<String> dependencies = new HashSet<>();
        
        validateTypeDependencyMethod.invoke(validator, "", "TestNamespace", dependencies);
        
        assertTrue("Should have no dependencies for empty type", dependencies.isEmpty());
    }
}
