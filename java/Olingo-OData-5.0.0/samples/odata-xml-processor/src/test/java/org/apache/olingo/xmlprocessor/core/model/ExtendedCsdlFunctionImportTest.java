package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlFunctionImportRefactored测试类
 */
public class ExtendedCsdlFunctionImportTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlFunctionImport functionImport = new ExtendedCsdlFunctionImport();
        assertNotNull(functionImport);
        assertNotNull(functionImport.asCsdlFunctionImport());
    }

    @Test
    public void testFromCsdlFunctionImport() {
        CsdlFunctionImport source = new CsdlFunctionImport()
            .setName("TestFunctionImport")
            .setFunction(new FullQualifiedName("TestNamespace", "TestFunction"))
            .setEntitySet("TestEntitySet")
            .setIncludeInServiceDocument(true);

        ExtendedCsdlFunctionImport extended = ExtendedCsdlFunctionImport.fromCsdlFunctionImport(source);

        assertNotNull(extended);
        assertEquals("TestFunctionImport", extended.getName());
        assertEquals("TestNamespace.TestFunction", extended.getFunction());
        assertEquals("TestEntitySet", extended.getEntitySet());
        assertTrue(extended.isIncludeInServiceDocument());
    }

    @Test
    public void testFromCsdlFunctionImportNull() {
        ExtendedCsdlFunctionImport extended = ExtendedCsdlFunctionImport.fromCsdlFunctionImport(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlFunctionImport functionImport = new ExtendedCsdlFunctionImport()
            .setName("FluentFunctionImport")
            .setFunction("FluentNamespace.FluentFunction")
            .setEntitySet("FluentEntitySet")
            .setIncludeInServiceDocument(false)
            .setNamespace("TestNamespace");

        assertEquals("FluentFunctionImport", functionImport.getName());
        assertEquals("FluentNamespace.FluentFunction", functionImport.getFunction());
        assertEquals("FluentEntitySet", functionImport.getEntitySet());
        assertFalse(functionImport.isIncludeInServiceDocument());
        assertEquals("TestNamespace", functionImport.getNamespace());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlFunctionImport functionImport = new ExtendedCsdlFunctionImport()
            .setName("TestFunctionImport")
            .setNamespace("TestNamespace");

        assertEquals("TestFunctionImport", functionImport.getElementId());
        assertEquals("TestFunctionImport", functionImport.getElementPropertyName());

        FullQualifiedName fqn = functionImport.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestFunctionImport", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlFunctionImport functionImport = new ExtendedCsdlFunctionImport()
            .setName("AnnotatedFunctionImport");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        functionImport.addExtendedAnnotation(annotation);

        assertEquals(1, functionImport.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", functionImport.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlFunctionImport functionImport = new ExtendedCsdlFunctionImport()
            .setName("TestFunctionImport")
            .setFunction("TestNamespace.TestFunction")
            .setNamespace("TestNamespace");

        String toString = functionImport.toString();
        assertTrue(toString.contains("TestFunctionImport"));
        assertTrue(toString.contains("TestNamespace.TestFunction"));
        assertTrue(toString.contains("TestNamespace"));
    }
}
