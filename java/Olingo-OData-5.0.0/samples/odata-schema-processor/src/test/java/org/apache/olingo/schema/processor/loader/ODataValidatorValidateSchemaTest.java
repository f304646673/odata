package org.apache.olingo.schema.processor.loader;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ODataValidator.validateSchema() public method
 */
public class ODataValidatorValidateSchemaTest {
    
    private ODataValidator validator;
    
    @Before
    public void setUp() throws Exception {
        validator = new ODataValidator();
    }
    
    @Test
    public void testValidateSchemaWithValidSchema() throws Exception {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestNamespace");
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertTrue("Should be valid for simple schema", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchemaWithNullSchema() throws Exception {
        ODataValidator.ValidationResult result = validator.validateSchema(null);
        
        assertFalse("Should be invalid for null schema", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should contain null error", result.getErrors().get(0).contains("cannot be null"));
    }
    
    @Test
    public void testValidateSchemaWithInvalidNamespace() throws Exception {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("Invalid-Namespace");
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertFalse("Should be invalid for invalid namespace", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchemaWithEmptyNamespace() throws Exception {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("");
        
        ODataValidator.ValidationResult result = validator.validateSchema(schema);
        
        assertFalse("Should be invalid for empty namespace", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
}
