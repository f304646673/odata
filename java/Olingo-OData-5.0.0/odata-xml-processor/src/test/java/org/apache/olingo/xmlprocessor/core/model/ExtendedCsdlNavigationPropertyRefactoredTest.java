package org.apache.olingo.xmlprocessor.core.model;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlNavigationPropertyRefactored测试类
 */
public class ExtendedCsdlNavigationPropertyRefactoredTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlNavigationPropertyRefactored navProperty = new ExtendedCsdlNavigationPropertyRefactored();
        assertNotNull(navProperty);
        assertNotNull(navProperty.asCsdlNavigationProperty());
    }

    @Test
    public void testFromCsdlNavigationProperty() {
        CsdlNavigationProperty source = new CsdlNavigationProperty()
            .setName("TestNavProperty")
            .setType("TestNamespace.TestEntity")
            .setNullable(false)
            .setCollection(true)
            .setPartner("BackReference");

        ExtendedCsdlNavigationPropertyRefactored extended = ExtendedCsdlNavigationPropertyRefactored.fromCsdlNavigationProperty(source);

        assertNotNull(extended);
        assertEquals("TestNavProperty", extended.getName());
        assertEquals("TestNamespace.TestEntity", extended.getType());
        assertFalse(extended.isNullable());
        assertTrue(extended.isCollection());
        assertEquals("BackReference", extended.getPartner());
    }

    @Test
    public void testFromCsdlNavigationPropertyNull() {
        ExtendedCsdlNavigationPropertyRefactored extended = ExtendedCsdlNavigationPropertyRefactored.fromCsdlNavigationProperty(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlNavigationPropertyRefactored navProperty = new ExtendedCsdlNavigationPropertyRefactored()
            .setName("FluentNavProperty")
            .setType("FluentNamespace.FluentEntity")
            .setNullable(true)
            .setCollection(false)
            .setPartner("FluentPartner")
            .setNamespace("TestNamespace");

        assertEquals("FluentNavProperty", navProperty.getName());
        assertEquals("FluentNamespace.FluentEntity", navProperty.getType());
        assertTrue(navProperty.isNullable());
        assertFalse(navProperty.isCollection());
        assertEquals("FluentPartner", navProperty.getPartner());
        assertEquals("TestNamespace", navProperty.getNamespace());
    }

    @Test
    public void testReferentialConstraints() {
        ExtendedCsdlNavigationPropertyRefactored navProperty = new ExtendedCsdlNavigationPropertyRefactored()
            .setName("TestNavProperty");

        CsdlReferentialConstraint constraint1 = new CsdlReferentialConstraint()
            .setProperty("Property1")
            .setReferencedProperty("RefProperty1");

        CsdlReferentialConstraint constraint2 = new CsdlReferentialConstraint()
            .setProperty("Property2")
            .setReferencedProperty("RefProperty2");

        navProperty.setReferentialConstraints(Arrays.asList(constraint1, constraint2));

        assertEquals(2, navProperty.getReferentialConstraints().size());
        assertEquals("Property1", navProperty.getReferentialConstraints().get(0).getProperty());
        assertEquals("RefProperty1", navProperty.getReferentialConstraints().get(0).getReferencedProperty());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlNavigationPropertyRefactored navProperty = new ExtendedCsdlNavigationPropertyRefactored()
            .setName("TestNavProperty")
            .setNamespace("TestNamespace");

        assertEquals("TestNavProperty", navProperty.getElementId());
        assertEquals("TestNavProperty", navProperty.getElementPropertyName());

        FullQualifiedName fqn = navProperty.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestNavProperty", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlNavigationPropertyRefactored navProperty = new ExtendedCsdlNavigationPropertyRefactored()
            .setName("AnnotatedNavProperty");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        navProperty.addExtendedAnnotation(annotation);

        assertEquals(1, navProperty.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", navProperty.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlNavigationPropertyRefactored navProperty = new ExtendedCsdlNavigationPropertyRefactored()
            .setName("TestNavProperty")
            .setType("TestNamespace.TestEntity")
            .setPartner("Partner")
            .setNamespace("TestNamespace");

        String toString = navProperty.toString();
        assertTrue(toString.contains("TestNavProperty"));
        assertTrue(toString.contains("TestNamespace.TestEntity"));
        assertTrue(toString.contains("TestNamespace"));
    }
}
