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
//import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * Tests for ODataValidator.validateEntityContainer() private method
// */
//public class ODataValidatorValidateEntityContainerTest {
//
//    private ODataValidator validator;
//    private Method validateEntityContainerMethod;
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        validator = new ODataValidator();
//        // Access private method via reflection
//        validateEntityContainerMethod = ODataValidator.class.getDeclaredMethod("validateEntityContainer",
//                CsdlEntityContainer.class, String.class, List.class, List.class, Set.class);
//        validateEntityContainerMethod.setAccessible(true);
//    }
//
//    @Test
//    public void testValidateEntityContainerWithValidName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityContainer container = new CsdlEntityContainer();
//        container.setName("TestContainer");
//
//        validateEntityContainerMethod.invoke(validator, container, "TestNamespace", errors, warnings, dependencies);
//
//        assertTrue("Should have no errors for valid entity container", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateEntityContainerWithNullName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityContainer container = new CsdlEntityContainer();
//        container.setName(null);
//
//        validateEntityContainerMethod.invoke(validator, container, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for null name", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateEntityContainerWithEmptyName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityContainer container = new CsdlEntityContainer();
//        container.setName("");
//
//        validateEntityContainerMethod.invoke(validator, container, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for empty name", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateEntityContainerWithInvalidName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityContainer container = new CsdlEntityContainer();
//        container.setName("Invalid-Name");
//
//        validateEntityContainerMethod.invoke(validator, container, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for invalid name format", errors.isEmpty());
//    }
//}
