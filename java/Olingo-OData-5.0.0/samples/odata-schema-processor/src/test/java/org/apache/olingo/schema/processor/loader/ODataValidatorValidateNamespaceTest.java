//package org.apache.olingo.schema.processor.loader;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * Tests for ODataValidator.validateNamespace() private method
// */
//public class ODataValidatorValidateNamespaceTest {
//
//    private ODataValidator validator;
//    private Method validateNamespaceMethod;
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        validator = new ODataValidator();
//        // Access private method via reflection
//        validateNamespaceMethod = ODataValidator.class.getDeclaredMethod("validateNamespace",
//                String.class, List.class);
//        validateNamespaceMethod.setAccessible(true);
//    }
//
//    @Test
//    public void testValidateNamespaceWithValidNamespace() throws Exception {
//        List<String> errors = new ArrayList<>();
//
//        validateNamespaceMethod.invoke(validator, "ValidNamespace", errors);
//
//        assertTrue("Should have no errors for valid namespace", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateNamespaceWithNestedNamespace() throws Exception {
//        List<String> errors = new ArrayList<>();
//
//        validateNamespaceMethod.invoke(validator, "Company.Department", errors);
//
//        assertTrue("Should have no errors for nested namespace", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateNamespaceWithNullNamespace() throws Exception {
//        List<String> errors = new ArrayList<>();
//
//        validateNamespaceMethod.invoke(validator, null, errors);
//
//        assertFalse("Should have errors for null namespace", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateNamespaceWithEmptyNamespace() throws Exception {
//        List<String> errors = new ArrayList<>();
//
//        validateNamespaceMethod.invoke(validator, "", errors);
//
//        assertFalse("Should have errors for empty namespace", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateNamespaceWithInvalidCharacters() throws Exception {
//        List<String> errors = new ArrayList<>();
//
//        validateNamespaceMethod.invoke(validator, "Invalid-Namespace", errors);
//
//        assertFalse("Should have errors for namespace with invalid characters", errors.isEmpty());
//    }
//}
