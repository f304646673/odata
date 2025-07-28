package org.apache.olingo.schema.processor.loader;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

/**
 * Test for validateAction method in ODataValidator
 */
public class ODataValidatorValidateActionTest {
    
    private ODataValidator validator;
    
    @Before
    public void setUp() {
        validator = new ODataValidator();
    }
    
    @Test
    public void testValidateActionWithValidAction() throws Exception {
        CsdlAction action = new CsdlAction();
        action.setName("TestAction");
        
        Method method = ODataValidator.class.getDeclaredMethod("validateAction", CsdlAction.class, String.class);
        method.setAccessible(true);
        
        // Should not throw exception for valid action
        method.invoke(validator, action, "TestNamespace");
    }
    
    @Test
    public void testValidateActionWithNullAction() throws Exception {
        Method method = ODataValidator.class.getDeclaredMethod("validateAction", CsdlAction.class, String.class);
        method.setAccessible(true);
        
        // Should handle null action gracefully
        method.invoke(validator, null, "TestNamespace");
    }
    
    @Test
    public void testValidateActionWithNullNamespace() throws Exception {
        CsdlAction action = new CsdlAction();
        action.setName("TestAction");
        
        Method method = ODataValidator.class.getDeclaredMethod("validateAction", CsdlAction.class, String.class);
        method.setAccessible(true);
        
        // Should handle null namespace gracefully
        method.invoke(validator, action, null);
    }
    
    @Test
    public void testValidateActionWithEmptyNamespace() throws Exception {
        CsdlAction action = new CsdlAction();
        action.setName("TestAction");
        
        Method method = ODataValidator.class.getDeclaredMethod("validateAction", CsdlAction.class, String.class);
        method.setAccessible(true);
        
        // Should handle empty namespace gracefully
        method.invoke(validator, action, "");
    }
}