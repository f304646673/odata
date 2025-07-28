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
//import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * Tests for ODataValidator.validateEntityType() private method
// */
//public class ODataValidatorValidateEntityTypeTest {
//
//    private ODataValidator validator;
//    private Method validateEntityTypeMethod;
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        validator = new ODataValidator();
//        // Access private method via reflection
//        validateEntityTypeMethod = ODataValidator.class.getDeclaredMethod("validateEntityType",
//                CsdlEntityType.class, String.class, List.class, List.class, Set.class);
//        validateEntityTypeMethod.setAccessible(true);
//    }
//
//    @Test
//    public void testValidateEntityTypeWithValidName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityType entityType = new CsdlEntityType();
//        entityType.setName("TestEntity");
//
//        validateEntityTypeMethod.invoke(validator, entityType, "TestNamespace", errors, warnings, dependencies);
//
//        assertTrue("Should have no errors for valid entity type", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateEntityTypeWithNullName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityType entityType = new CsdlEntityType();
//        entityType.setName(null);
//
//        validateEntityTypeMethod.invoke(validator, entityType, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for null name", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateEntityTypeWithEmptyName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityType entityType = new CsdlEntityType();
//        entityType.setName("");
//
//        validateEntityTypeMethod.invoke(validator, entityType, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for empty name", errors.isEmpty());
//    }
//
//    @Test
//    public void testValidateEntityTypeWithInvalidName() throws Exception {
//        List<String> errors = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        Set<String> dependencies = new HashSet<>();
//
//        CsdlEntityType entityType = new CsdlEntityType();
//        entityType.setName("Invalid-Name");
//
//        validateEntityTypeMethod.invoke(validator, entityType, "TestNamespace", errors, warnings, dependencies);
//
//        assertFalse("Should have errors for invalid name format", errors.isEmpty());
//    }
//}
