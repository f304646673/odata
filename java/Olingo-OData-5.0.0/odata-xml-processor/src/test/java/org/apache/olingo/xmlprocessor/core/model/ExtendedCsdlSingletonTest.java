package org.apache.olingo.xmlprocessor.core.model;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlSingletonRefactored测试类
 */
public class ExtendedCsdlSingletonTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlSingleton singleton = new ExtendedCsdlSingleton();
        assertNotNull(singleton);
        assertNotNull(singleton.asCsdlSingleton());
    }

    @Test
    public void testFromCsdlSingleton() {
        CsdlSingleton source = new CsdlSingleton()
            .setName("TestSingleton")
            .setType("TestNamespace.TestEntity");

        // 添加导航属性绑定
        CsdlNavigationPropertyBinding binding = new CsdlNavigationPropertyBinding()
            .setPath("NavProperty")
            .setTarget("TargetEntitySet");
        source.setNavigationPropertyBindings(Arrays.asList(binding));

        ExtendedCsdlSingleton extended = ExtendedCsdlSingleton.fromCsdlSingleton(source);

        assertNotNull(extended);
        assertEquals("TestSingleton", extended.getName());
        assertEquals("TestNamespace.TestEntity", extended.getType());
        assertEquals(1, extended.getNavigationPropertyBindings().size());
        assertEquals("NavProperty", extended.getNavigationPropertyBindings().get(0).getPath());
    }

    @Test
    public void testFromCsdlSingletonNull() {
        ExtendedCsdlSingleton extended = ExtendedCsdlSingleton.fromCsdlSingleton(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlSingleton singleton = new ExtendedCsdlSingleton()
            .setName("FluentSingleton")
            .setType("FluentNamespace.FluentEntity")
            .setNamespace("TestNamespace");

        assertEquals("FluentSingleton", singleton.getName());
        assertEquals("FluentNamespace.FluentEntity", singleton.getType());
        assertEquals("TestNamespace", singleton.getNamespace());
    }

    @Test
    public void testNavigationPropertyBindings() {
        ExtendedCsdlSingleton singleton = new ExtendedCsdlSingleton()
            .setName("TestSingleton");

        CsdlNavigationPropertyBinding binding1 = new CsdlNavigationPropertyBinding()
            .setPath("NavProperty1")
            .setTarget("Target1");

        CsdlNavigationPropertyBinding binding2 = new CsdlNavigationPropertyBinding()
            .setPath("NavProperty2")
            .setTarget("Target2");

        singleton.setNavigationPropertyBindings(Arrays.asList(binding1, binding2));

        assertEquals(2, singleton.getNavigationPropertyBindings().size());
        assertEquals("NavProperty1", singleton.getNavigationPropertyBindings().get(0).getPath());
        assertEquals("Target1", singleton.getNavigationPropertyBindings().get(0).getTarget());
        assertEquals("NavProperty2", singleton.getNavigationPropertyBindings().get(1).getPath());
        assertEquals("Target2", singleton.getNavigationPropertyBindings().get(1).getTarget());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlSingleton singleton = new ExtendedCsdlSingleton()
            .setName("TestSingleton")
            .setNamespace("TestNamespace");

        assertEquals("TestSingleton", singleton.getElementId());
        assertEquals("TestSingleton", singleton.getElementPropertyName());

        FullQualifiedName fqn = singleton.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestSingleton", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlSingleton singleton = new ExtendedCsdlSingleton()
            .setName("AnnotatedSingleton");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        singleton.addExtendedAnnotation(annotation);

        assertEquals(1, singleton.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", singleton.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlSingleton singleton = new ExtendedCsdlSingleton()
            .setName("TestSingleton")
            .setType("TestNamespace.TestEntity")
            .setNamespace("TestNamespace");

        String toString = singleton.toString();
        assertTrue(toString.contains("TestSingleton"));
        assertTrue(toString.contains("TestNamespace.TestEntity"));
        assertTrue(toString.contains("TestNamespace"));
    }
}
