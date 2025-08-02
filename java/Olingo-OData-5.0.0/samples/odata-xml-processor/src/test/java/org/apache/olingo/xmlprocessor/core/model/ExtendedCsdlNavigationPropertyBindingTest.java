package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlNavigationPropertyBindingRefactored测试类
 */
public class ExtendedCsdlNavigationPropertyBindingTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlNavigationPropertyBinding binding = new ExtendedCsdlNavigationPropertyBinding();
        assertNotNull(binding);
        assertNotNull(binding.asCsdlNavigationPropertyBinding());
    }

    @Test
    public void testFromCsdlNavigationPropertyBinding() {
        CsdlNavigationPropertyBinding source = new CsdlNavigationPropertyBinding()
            .setPath("NavProperty")
            .setTarget("TargetEntitySet");

        ExtendedCsdlNavigationPropertyBinding extended = ExtendedCsdlNavigationPropertyBinding.fromCsdlNavigationPropertyBinding(source);

        assertNotNull(extended);
        assertEquals("NavProperty", extended.getPath());
        assertEquals("TargetEntitySet", extended.getTarget());
    }

    @Test
    public void testFromCsdlNavigationPropertyBindingNull() {
        ExtendedCsdlNavigationPropertyBinding extended = ExtendedCsdlNavigationPropertyBinding.fromCsdlNavigationPropertyBinding(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlNavigationPropertyBinding binding = new ExtendedCsdlNavigationPropertyBinding()
            .setPath("FluentNavProperty")
            .setTarget("FluentTargetEntitySet")
            .setNamespace("TestNamespace");

        assertEquals("FluentNavProperty", binding.getPath());
        assertEquals("FluentTargetEntitySet", binding.getTarget());
        assertEquals("TestNamespace", binding.getNamespace());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlNavigationPropertyBinding binding = new ExtendedCsdlNavigationPropertyBinding()
            .setPath("TestNavProperty")
            .setTarget("TestTarget")
            .setNamespace("TestNamespace");

        assertEquals("Binding_TestNavProperty", binding.getElementId());
        assertEquals("TestNavProperty", binding.getElementPropertyName());

        FullQualifiedName fqn = binding.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestNavProperty", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlNavigationPropertyBinding binding = new ExtendedCsdlNavigationPropertyBinding()
            .setPath("AnnotatedNavProperty")
            .setTarget("AnnotatedTarget");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        binding.addExtendedAnnotation(annotation);

        assertEquals(1, binding.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", binding.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlNavigationPropertyBinding binding = new ExtendedCsdlNavigationPropertyBinding()
            .setPath("TestNavProperty")
            .setTarget("TestTarget")
            .setNamespace("TestNamespace");

        String toString = binding.toString();
        assertTrue(toString.contains("TestNavProperty"));
        assertTrue(toString.contains("TestTarget"));
        assertTrue(toString.contains("TestNamespace"));
    }
}
