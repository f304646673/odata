package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlActionImportRefactored测试类
 */
public class ExtendedCsdlActionImportTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport();
        assertNotNull(actionImport);
        assertNotNull(actionImport.asCsdlActionImport());
    }

    @Test
    public void testFromCsdlActionImport() {
        CsdlActionImport source = new CsdlActionImport()
            .setName("TestActionImport")
            .setAction(new FullQualifiedName("TestNamespace", "TestAction"))
            .setEntitySet("TestEntitySet");

        ExtendedCsdlActionImport extended = ExtendedCsdlActionImport.fromCsdlActionImport(source);

        assertNotNull(extended);
        assertEquals("TestActionImport", extended.getName());
        assertEquals("TestNamespace.TestAction", extended.getAction());
        assertEquals("TestEntitySet", extended.getEntitySet());
    }

    @Test
    public void testFromCsdlActionImportNull() {
        ExtendedCsdlActionImport extended = ExtendedCsdlActionImport.fromCsdlActionImport(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport()
            .setName("FluentActionImport")
            .setAction("FluentNamespace.FluentAction")
            .setEntitySet("FluentEntitySet")
            .setNamespace("TestNamespace");

        assertEquals("FluentActionImport", actionImport.getName());
        assertEquals("FluentNamespace.FluentAction", actionImport.getAction());
        assertEquals("FluentEntitySet", actionImport.getEntitySet());
        assertEquals("TestNamespace", actionImport.getNamespace());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport()
            .setName("TestActionImport")
            .setNamespace("TestNamespace");

        assertEquals("TestActionImport", actionImport.getElementId());
        assertEquals("TestActionImport", actionImport.getElementPropertyName());

        FullQualifiedName fqn = actionImport.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestActionImport", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport()
            .setName("AnnotatedActionImport");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        actionImport.addExtendedAnnotation(annotation);

        assertEquals(1, actionImport.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", actionImport.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport()
            .setName("TestActionImport")
            .setAction("TestNamespace.TestAction")
            .setNamespace("TestNamespace");

        String toString = actionImport.toString();
        assertTrue(toString.contains("ExtendedCsdlActionImport"));
    }
}
