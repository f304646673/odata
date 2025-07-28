package org.apache.olingo.schema.processor.loader;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ODataValidator.validateComplexType() private method
 */
public class ODataValidatorValidateComplexTypeTest {
    
    private ODataValidator validator;
    private Method validateComplexTypeMethod;
    
    @Before
    public void setUp() throws Exception {
        validator = new ODataValidator();
        // Access private method via reflection
        validateComplexTypeMethod = ODataValidator.class.getDeclaredMethod("validateComplexType", 
                CsdlComplexType.class, String.class, List.class, List.class, Set.class);
        validateComplexTypeMethod.setAccessible(true);
    }
    
    @Test
    public void testValidateComplexTypeWithValidName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("TestComplexType");
        
        validateComplexTypeMethod.invoke(validator, complexType, "TestNamespace", errors, warnings, dependencies);
        
        assertTrue("Should have no errors for valid complex type", errors.isEmpty());
    }
    
    @Test
    public void testValidateComplexTypeWithNullName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(null);
        
        validateComplexTypeMethod.invoke(validator, complexType, "TestNamespace", errors, warnings, dependencies);
        
        assertFalse("Should have errors for null name", errors.isEmpty());
    }
    
    @Test
    public void testValidateComplexTypeWithEmptyName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("");
        
        validateComplexTypeMethod.invoke(validator, complexType, "TestNamespace", errors, warnings, dependencies);
        
        assertFalse("Should have errors for empty name", errors.isEmpty());
    }
    
    @Test
    public void testValidateComplexTypeWithInvalidName() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Invalid-Name");
        
        validateComplexTypeMethod.invoke(validator, complexType, "TestNamespace", errors, warnings, dependencies);
        
        assertFalse("Should have errors for invalid name format", errors.isEmpty());
    }
}
