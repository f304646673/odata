//package org.apache.olingo.schema.processor.loader;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.HashSet;
//
//import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * Tests for ODataValidator.validateFunction() private method
// */
//public class ODataValidatorValidateFunctionTest {
//
//    private ODataValidator validator;
//    private Method validateFunctionMethod;
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        validator = new ODataValidator();
//        // Access private method via reflection
//        validateFunctionMethod = ODataValidator.class.getDeclaredMethod("validateFunction",
//                CsdlFunction.class, String.class, List.class, List.class, Set.class);
//        validateFunctionMethod.setAccessible(true);
//    }
//
//    @Test
//    public void testValidateFunctionWithValidName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlFunction function = new CsdlFunction();
//        function.setName("TestFunction");
//
//        validateFunctionMethod.invoke(validator, function, "TestNamespace", errors, warnings, dependencies);
//
//        assertTrue("Should have no errors for valid function", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateFunctionWithNullName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlFunction function = new CsdlFunction();
//        function.setName(null);
//
//        validateFunctionMethod.invoke(validator, function, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for null name", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateFunctionWithEmptyName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlFunction function = new CsdlFunction();
//        function.setName("");
//
//        validateFunctionMethod.invoke(validator, function, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for empty name", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateFunctionWithInvalidName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlFunction function = new CsdlFunction();
//        function.setName("Invalid-Name");
//
//        validateFunctionMethod.invoke(validator, function, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for invalid name format", errors.isEmpty());
//    }
//}
