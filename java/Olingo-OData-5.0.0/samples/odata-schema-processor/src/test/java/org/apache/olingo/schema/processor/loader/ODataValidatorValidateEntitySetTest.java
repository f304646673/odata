package org.apache.olingo.schema.processor.loader;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ODataValidator.validateEntitySet() private method
 */
public class ODataValidatorValidateEntitySetTest {
    
    private ODataValidator validator;
    private Method validateEntitySetMethod;
    
    @Before
    public void setUp() throws Exception {
        validator = new ODataValidator();
        // Access private method via reflection
        validateEntitySetMethod = ODataValidator.class.getDeclaredMethod("validateEntitySet", 
                CsdlEntitySet.class, Set.class, List.class, List.class, Set.class, String.class);
        validateEntitySetMethod.setAccessible(true);
    }
    
    @Test
    public void testValidateEntitySetWithValidName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> entitySetNames = new HashSet<>();
        
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName("TestEntitySet");
        entitySet.setType("TestNamespace.TestEntity");
        
        validateEntitySetMethod.invoke(validator, entitySet, entitySetNames, errors, warnings, dependencies, "TestNamespace");
        
        assertTrue("Should have no errors for valid entity set", errors.isEmpty());
        assertTrue("Should add entity set name to set", entitySetNames.contains("TestEntitySet"));
    }
    
    @Test
    public void testValidateEntitySetWithNullName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> entitySetNames = new HashSet<>();
        
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(null);
        entitySet.setType("TestNamespace.TestEntity");
        
        validateEntitySetMethod.invoke(validator, entitySet, entitySetNames, errors, warnings, dependencies, "TestNamespace");
        
        assertFalse("Should have errors for null name", errors.isEmpty());
    }
    
    @Test
    public void testValidateEntitySetWithEmptyName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> entitySetNames = new HashSet<>();
        
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName("");
        entitySet.setType("TestNamespace.TestEntity");
        
        validateEntitySetMethod.invoke(validator, entitySet, entitySetNames, errors, warnings, dependencies, "TestNamespace");
        
        assertFalse("Should have errors for empty name", errors.isEmpty());
    }
    
    @Test
    public void testValidateEntitySetWithDuplicateName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> entitySetNames = new HashSet<>();
        entitySetNames.add("TestEntitySet"); // Pre-add to simulate duplicate
        
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName("TestEntitySet");
        entitySet.setType("TestNamespace.TestEntity");
        
        validateEntitySetMethod.invoke(validator, entitySet, entitySetNames, errors, warnings, dependencies, "TestNamespace");
        
        assertFalse("Should have errors for duplicate name", errors.isEmpty());
    }
}
