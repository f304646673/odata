package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlTypeDefinitionRefactored测试类
 */
public class ExtendedCsdlTypeDefinitionRefactoredTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlTypeDefinitionRefactored typeDef = new ExtendedCsdlTypeDefinitionRefactored();
        assertNotNull(typeDef);
        assertNotNull(typeDef.asCsdlTypeDefinition());
    }

    @Test
    public void testFromCsdlTypeDefinition() {
        CsdlTypeDefinition source = new CsdlTypeDefinition()
            .setName("TestTypeDefinition")
            .setUnderlyingType("Edm.String")
            .setMaxLength(255)
            .setPrecision(10)
            .setScale(2);

        ExtendedCsdlTypeDefinitionRefactored extended = ExtendedCsdlTypeDefinitionRefactored.fromCsdlTypeDefinition(source);

        assertNotNull(extended);
        assertEquals("TestTypeDefinition", extended.getName());
        assertEquals("Edm.String", extended.getUnderlyingType());
        assertEquals(Integer.valueOf(255), extended.getMaxLength());
        assertEquals(Integer.valueOf(10), extended.getPrecision());
        assertEquals(Integer.valueOf(2), extended.getScale());
    }

    @Test
    public void testFromCsdlTypeDefinitionNull() {
        ExtendedCsdlTypeDefinitionRefactored extended = ExtendedCsdlTypeDefinitionRefactored.fromCsdlTypeDefinition(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlTypeDefinitionRefactored typeDef = new ExtendedCsdlTypeDefinitionRefactored()
            .setName("FluentTypeDefinition")
            .setUnderlyingType("Edm.Int32")
            .setMaxLength(100)
            .setPrecision(8)
            .setScale(1)
            .setNamespace("TestNamespace");

        assertEquals("FluentTypeDefinition", typeDef.getName());
        assertEquals("Edm.Int32", typeDef.getUnderlyingType());
        assertEquals(Integer.valueOf(100), typeDef.getMaxLength());
        assertEquals(Integer.valueOf(8), typeDef.getPrecision());
        assertEquals(Integer.valueOf(1), typeDef.getScale());
        assertEquals("TestNamespace", typeDef.getNamespace());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlTypeDefinitionRefactored typeDef = new ExtendedCsdlTypeDefinitionRefactored()
            .setName("TestTypeDefinition")
            .setNamespace("TestNamespace");

        assertEquals("TestTypeDefinition", typeDef.getElementId());
        assertEquals("TestTypeDefinition", typeDef.getElementPropertyName());

        FullQualifiedName fqn = typeDef.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestTypeDefinition", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlTypeDefinitionRefactored typeDef = new ExtendedCsdlTypeDefinitionRefactored()
            .setName("AnnotatedTypeDefinition");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        typeDef.addExtendedAnnotation(annotation);

        assertEquals(1, typeDef.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", typeDef.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testUnicodeAttribute() {
        ExtendedCsdlTypeDefinitionRefactored typeDef = new ExtendedCsdlTypeDefinitionRefactored()
            .setName("TestTypeDefinition")
            .setUnderlyingType("Edm.String")
            .setUnicode(true);

        assertTrue(typeDef.isUnicode());

        typeDef.setUnicode(false);
        assertFalse(typeDef.isUnicode());
    }

    @Test
    public void testToString() {
        ExtendedCsdlTypeDefinitionRefactored typeDef = new ExtendedCsdlTypeDefinitionRefactored()
            .setName("TestTypeDefinition")
            .setUnderlyingType("Edm.String")
            .setMaxLength(255)
            .setNamespace("TestNamespace");

        String toString = typeDef.toString();
        assertTrue(toString.contains("TestTypeDefinition"));
        assertTrue(toString.contains("TestNamespace"));
        assertTrue(toString.contains("Edm.String"));
        assertTrue(toString.contains("255"));
    }
}
