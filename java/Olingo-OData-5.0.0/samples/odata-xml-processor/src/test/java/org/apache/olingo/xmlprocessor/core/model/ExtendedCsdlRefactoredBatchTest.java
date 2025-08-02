package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * 联合测试类，测试多个重构的类
 */
class ExtendedCsdlRefactoredBatchTest {

    @Test
    void testNavigationPropertyRefactored() {
        ExtendedCsdlNavigationProperty navProp = new ExtendedCsdlNavigationProperty();
        navProp.setName("Orders")
               .setType("TestNamespace.Order")
               .setCollection(true)
               .setContainsTarget(false);

        assertEquals("Orders", navProp.getName());
        assertEquals("TestNamespace.Order", navProp.getType());
        assertTrue(navProp.isCollection());
        assertFalse(navProp.isContainsTarget());
    }

    @Test
    void testFunctionImportRefactored() {
        ExtendedCsdlFunctionImport funcImport = new ExtendedCsdlFunctionImport();
        funcImport.setName("GetCustomers")
                  .setFunction("TestNamespace.GetCustomers")
                  .setEntitySet("Customers")
                  .setIncludeInServiceDocument(true);

        assertEquals("GetCustomers", funcImport.getName());
        assertEquals("TestNamespace.GetCustomers", funcImport.getFunction());
        assertEquals("Customers", funcImport.getEntitySet());
        assertTrue(funcImport.isIncludeInServiceDocument());
    }

    @Test
    void testNavigationPropertyBindingRefactored() {
        ExtendedCsdlNavigationPropertyBinding binding = new ExtendedCsdlNavigationPropertyBinding();
        binding.setPath("Orders")
               .setTarget("OrderSet");

        assertEquals("Orders", binding.getPath());
        assertEquals("OrderSet", binding.getTarget());
    }

    @Test
    void testEntityContainerRefactored() {
        ExtendedCsdlEntityContainer container = new ExtendedCsdlEntityContainer();
        container.setName("DefaultContainer");

        assertEquals("DefaultContainer", container.getName());
        assertTrue(container.getExtendedEntitySets().isEmpty());
        assertTrue(container.getExtendedActionImports().isEmpty());
    }

    @Test
    void testFromCsdlMethods() {
        // 测试NavigationProperty的fromCsdl方法
        CsdlNavigationProperty navProp = new CsdlNavigationProperty();
        navProp.setName("Orders");
        navProp.setType("TestNamespace.Order");
        
        ExtendedCsdlNavigationProperty extended =
            ExtendedCsdlNavigationProperty.fromCsdlNavigationProperty(navProp);
        
        assertNotNull(extended);
        assertEquals("Orders", extended.getName());
        assertEquals("TestNamespace.Order", extended.getType());
    }

    @Test
    void testNullHandling() {
        assertNull(ExtendedCsdlNavigationProperty.fromCsdlNavigationProperty(null));
        assertNull(ExtendedCsdlFunctionImport.fromCsdlFunctionImport(null));
        assertNull(ExtendedCsdlNavigationPropertyBinding.fromCsdlNavigationPropertyBinding(null));
        assertNull(ExtendedCsdlEntityContainer.fromCsdlEntityContainer(null));
    }

    @Test
    void testToStringMethods() {
        ExtendedCsdlNavigationProperty navProp = new ExtendedCsdlNavigationProperty();
        navProp.setName("Orders");
        navProp.setNamespace("test");
        
        String result = navProp.toString();
        assertTrue(result.contains("Orders"));
        assertTrue(result.contains("test"));
    }

    @Test
    void testElementInterface() {
        ExtendedCsdlFunctionImport funcImport = new ExtendedCsdlFunctionImport();
        funcImport.setName("TestFunction");
        funcImport.setNamespace("test.namespace");

        assertEquals("TestFunction", funcImport.getElementId());
        assertNotNull(funcImport.getElementFullyQualifiedName());
    }
}
