package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlEntityContainerRefactored测试类
 */
public class ExtendedCsdlEntityContainerTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlEntityContainer container = new ExtendedCsdlEntityContainer();
        assertNotNull(container);
        assertNotNull(container.asCsdlEntityContainer());
    }

    @Test
    public void testFromCsdlEntityContainer() {
        CsdlEntityContainer source = new CsdlEntityContainer()
            .setName("TestContainer");

        // 添加一个EntitySet
        CsdlEntitySet entitySet = new CsdlEntitySet()
            .setName("TestEntitySet")
            .setType(new FullQualifiedName("TestNamespace", "TestEntity"));
        source.getEntitySets().add(entitySet);

        ExtendedCsdlEntityContainer extended = ExtendedCsdlEntityContainer.fromCsdlEntityContainer(source);

        assertNotNull(extended);
        assertEquals("TestContainer", extended.getName());
        assertEquals(1, extended.getExtendedEntitySets().size());
        assertEquals("TestEntitySet", extended.getExtendedEntitySets().get(0).getName());
    }

    @Test
    public void testFromCsdlEntityContainerNull() {
        ExtendedCsdlEntityContainer extended = ExtendedCsdlEntityContainer.fromCsdlEntityContainer(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlEntityContainer container = new ExtendedCsdlEntityContainer()
            .setName("FluentContainer")
            .setNamespace("TestNamespace");

        assertEquals("FluentContainer", container.getName());
        assertEquals("TestNamespace", container.getNamespace());
    }

    @Test
    public void testEntitySetManagement() {
        ExtendedCsdlEntityContainer container = new ExtendedCsdlEntityContainer()
            .setName("TestContainer");

        ExtendedCsdlEntitySet entitySet = new ExtendedCsdlEntitySet()
            .setName("TestEntitySet")
            .setType("TestNamespace.TestEntity");

        container.addExtendedEntitySet(entitySet);

        assertEquals(1, container.getExtendedEntitySets().size());
        assertEquals("TestEntitySet", container.getExtendedEntitySets().get(0).getName());
        
        // 验证同步到原始数据
        assertEquals(1, container.getEntitySets().size());
        assertEquals("TestEntitySet", container.getEntitySets().get(0).getName());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlEntityContainer container = new ExtendedCsdlEntityContainer()
            .setName("TestContainer")
            .setNamespace("TestNamespace");

        assertEquals("TestContainer", container.getElementId());
        assertEquals("TestContainer", container.getElementPropertyName());

        FullQualifiedName fqn = container.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestContainer", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlEntityContainer container = new ExtendedCsdlEntityContainer()
            .setName("AnnotatedContainer");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        container.addExtendedAnnotation(annotation);

        assertEquals(1, container.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", container.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlEntityContainer container = new ExtendedCsdlEntityContainer()
            .setName("TestContainer")
            .setNamespace("TestNamespace");

        String toString = container.toString();
        assertTrue(toString.contains("TestContainer"));
        assertTrue(toString.contains("TestNamespace"));
    }
}
