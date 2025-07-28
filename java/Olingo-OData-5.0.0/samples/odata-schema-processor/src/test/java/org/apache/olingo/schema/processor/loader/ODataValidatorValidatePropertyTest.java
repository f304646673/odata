package org.apache.olingo.schema.processor.loader;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ODataValidator.validateProperty() private method
 */
public class ODataValidatorValidatePropertyTest {
    
    private ODataValidator validator;
    private Method validatePropertyMethod;
    
    @Before
    public void setUp() throws Exception {
        validator = new ODataValidator();
        // Access private method via reflection
        validatePropertyMethod = ODataValidator.class.getDeclaredMethod("validateProperty", 
                CsdlProperty.class, String.class, String.class, Set.class, List.class, List.class, Set.class, String.class);
        validatePropertyMethod.setAccessible(true);
    }
    
    @Test
    public void testValidatePropertyWithValidNameAndType() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> propertyNames = new HashSet<>();
        
        CsdlProperty property = new CsdlProperty();
        property.setName("TestProperty");
        property.setType("Edm.String");
        
        validatePropertyMethod.invoke(validator, property, "TestEntity", "EntityType", 
                propertyNames, errors, warnings, dependencies, "TestNamespace");
        
        assertTrue("Should have no errors for valid property", errors.isEmpty());
        assertTrue("Should add property name to set", propertyNames.contains("TestProperty"));
    }
    
    @Test
    public void testValidatePropertyWithNullName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> propertyNames = new HashSet<>();
        
        CsdlProperty property = new CsdlProperty();
        property.setName(null);
        property.setType("Edm.String");
        
        validatePropertyMethod.invoke(validator, property, "TestEntity", "EntityType", 
                propertyNames, errors, warnings, dependencies, "TestNamespace");
        
        assertFalse("Should have errors for null name", errors.isEmpty());
    }
    
    @Test
    public void testValidatePropertyWithEmptyName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> propertyNames = new HashSet<>();
        
        CsdlProperty property = new CsdlProperty();
        property.setName("");
        property.setType("Edm.String");
        
        validatePropertyMethod.invoke(validator, property, "TestEntity", "EntityType", 
                propertyNames, errors, warnings, dependencies, "TestNamespace");
        
        assertFalse("Should have errors for empty name", errors.isEmpty());
    }
    
    @Test
    public void testValidatePropertyWithDuplicateName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        Set<String> propertyNames = new HashSet<>();
        propertyNames.add("TestProperty"); // Pre-add to simulate duplicate
        
        CsdlProperty property = new CsdlProperty();
        property.setName("TestProperty");
        property.setType("Edm.String");
        
        validatePropertyMethod.invoke(validator, property, "TestEntity", "EntityType", 
                propertyNames, errors, warnings, dependencies, "TestNamespace");
        
        assertFalse("Should have errors for duplicate name", errors.isEmpty());
    }
}
