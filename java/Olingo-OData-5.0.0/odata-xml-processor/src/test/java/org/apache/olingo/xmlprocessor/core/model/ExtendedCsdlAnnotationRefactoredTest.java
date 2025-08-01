package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlAnnotationRefactored测试类
 */
public class ExtendedCsdlAnnotationRefactoredTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored();
        assertNotNull(annotation);
        assertNotNull(annotation.asCsdlAnnotation());
    }

    @Test
    public void testFromCsdlAnnotation() {
        CsdlExpression expression = new CsdlConstantExpression(CsdlConstantExpression.ConstantExpressionType.String, "test");
        CsdlAnnotation source = new CsdlAnnotation()
            .setTerm("TestTerm")
            .setQualifier("TestQualifier")
            .setExpression(expression);

        ExtendedCsdlAnnotationRefactored extended = ExtendedCsdlAnnotationRefactored.fromCsdlAnnotation(source);

        assertNotNull(extended);
        assertEquals("TestTerm", extended.getTerm());
        assertEquals("TestQualifier", extended.getQualifier());
        assertNotNull(extended.getExpression());
    }

    @Test
    public void testFromCsdlAnnotationNull() {
        ExtendedCsdlAnnotationRefactored extended = ExtendedCsdlAnnotationRefactored.fromCsdlAnnotation(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        CsdlExpression expression = new CsdlConstantExpression(CsdlConstantExpression.ConstantExpressionType.Int, "42");
        
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored()
            .setTerm("FluentTerm")
            .setQualifier("FluentQualifier")
            .setExpression(expression)
            .setNamespace("TestNamespace");

        assertEquals("FluentTerm", annotation.getTerm());
        assertEquals("FluentQualifier", annotation.getQualifier());
        assertEquals(expression, annotation.getExpression());
        assertEquals("TestNamespace", annotation.getNamespace());
    }

    @Test
    public void testSetTermWithFullQualifiedName() {
        FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestTerm");
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored()
            .setTerm(fqn);

        assertEquals("TestNamespace.TestTerm", annotation.getTerm());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored()
            .setTerm("TestNamespace.TestTerm")
            .setQualifier("TestQualifier");

        assertEquals("TestNamespace.TestTerm#TestQualifier", annotation.getElementId());
        assertEquals("TestQualifier", annotation.getElementPropertyName());

        FullQualifiedName fqn = annotation.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace.TestTerm", fqn.getFullQualifiedNameAsString());
    }

    @Test
    public void testElementIdWithoutQualifier() {
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored()
            .setTerm("TestTerm");

        assertEquals("TestTerm", annotation.getElementId());
    }

    @Test
    public void testElementIdWithoutTerm() {
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored();
        String elementId = annotation.getElementId();
        assertTrue(elementId.startsWith("Annotation_"));
    }

    @Test
    public void testToString() {
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored()
            .setTerm("TestTerm")
            .setQualifier("TestQualifier")
            .setNamespace("TestNamespace");

        String toString = annotation.toString();
        assertTrue(toString.contains("TestTerm"));
        assertTrue(toString.contains("TestQualifier"));
        assertTrue(toString.contains("TestNamespace"));
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlAnnotationRefactored annotation = new ExtendedCsdlAnnotationRefactored()
            .setTerm("TestTerm");

        // CsdlAnnotation本身不包含annotations，所以应该返回空列表
        assertEquals(0, annotation.getExtendedAnnotations().size());
    }
}
