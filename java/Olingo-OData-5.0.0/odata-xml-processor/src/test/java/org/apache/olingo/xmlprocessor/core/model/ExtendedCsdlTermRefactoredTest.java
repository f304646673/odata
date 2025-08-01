package org.apache.olingo.xmlprocessor.core.model;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlTermRefactored测试类
 */
public class ExtendedCsdlTermRefactoredTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlTermRefactored term = new ExtendedCsdlTermRefactored();
        assertNotNull(term);
        assertNotNull(term.asCsdlTerm());
    }

    @Test
    public void testFromCsdlTerm() {
        CsdlTerm source = new CsdlTerm()
            .setName("TestTerm")
            .setType("Edm.String")
            .setBaseTerm("BaseTerm")
            .setNullable(true)
            .setMaxLength(100)
            .setPrecision(10)
            .setScale(2)
            .setDefaultValue("default");

        ExtendedCsdlTermRefactored extended = ExtendedCsdlTermRefactored.fromCsdlTerm(source);

        assertNotNull(extended);
        assertEquals("TestTerm", extended.getName());
        assertEquals("Edm.String", extended.getType());
        assertEquals("BaseTerm", extended.getBaseTerm());
        assertTrue(extended.isNullable());
        assertEquals(Integer.valueOf(100), extended.getMaxLength());
        assertEquals(Integer.valueOf(10), extended.getPrecision());
        assertEquals(Integer.valueOf(2), extended.getScale());
        assertEquals("default", extended.getDefaultValue());
    }

    @Test
    public void testFromCsdlTermNull() {
        ExtendedCsdlTermRefactored extended = ExtendedCsdlTermRefactored.fromCsdlTerm(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlTermRefactored term = new ExtendedCsdlTermRefactored()
            .setName("FluentTerm")
            .setType("Edm.Int32")
            .setBaseTerm("BaseFluentTerm")
            .setNullable(false)
            .setMaxLength(50)
            .setPrecision(5)
            .setScale(1)
            .setDefaultValue("10")
            .setNamespace("TestNamespace");

        assertEquals("FluentTerm", term.getName());
        assertEquals("Edm.Int32", term.getType());
        assertEquals("BaseFluentTerm", term.getBaseTerm());
        assertFalse(term.isNullable());
        assertEquals(Integer.valueOf(50), term.getMaxLength());
        assertEquals(Integer.valueOf(5), term.getPrecision());
        assertEquals(Integer.valueOf(1), term.getScale());
        assertEquals("10", term.getDefaultValue());
        assertEquals("TestNamespace", term.getNamespace());
    }

    @Test
    public void testAppliesTo() {
        ExtendedCsdlTermRefactored term = new ExtendedCsdlTermRefactored();
        term.setAppliesTo(Arrays.asList("EntitySet", "EntityType"));

        assertEquals(2, term.getAppliesTo().size());
        assertTrue(term.getAppliesTo().contains("EntitySet"));
        assertTrue(term.getAppliesTo().contains("EntityType"));
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlTermRefactored term = new ExtendedCsdlTermRefactored()
            .setName("TestTerm")
            .setNamespace("TestNamespace");

        assertEquals("TestTerm", term.getElementId());
        assertEquals("TestTerm", term.getElementPropertyName());

        FullQualifiedName fqn = term.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestTerm", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlTermRefactored term = new ExtendedCsdlTermRefactored()
            .setName("AnnotatedTerm");

        // 使用基类的注解管理方法
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        term.addExtendedAnnotation(annotation);

        assertEquals(1, term.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", term.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlTermRefactored term = new ExtendedCsdlTermRefactored()
            .setName("TestTerm")
            .setType("Edm.String")
            .setBaseTerm("BaseTerm")
            .setNullable(true)
            .setNamespace("TestNamespace");

        String toString = term.toString();
        assertTrue(toString.contains("TestTerm"));
        assertTrue(toString.contains("TestNamespace"));
        assertTrue(toString.contains("Edm.String"));
        assertTrue(toString.contains("BaseTerm"));
    }
}
