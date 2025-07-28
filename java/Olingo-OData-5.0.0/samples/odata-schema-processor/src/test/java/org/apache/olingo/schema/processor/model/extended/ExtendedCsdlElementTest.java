package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 ExtendedCsdlElement 接口
 */
public class ExtendedCsdlElementTest {

    private ExtendedCsdlElement element;

    // 简单的测试实现
    private static class TestExtendedCsdlElement implements ExtendedCsdlElement {
        private String elementId;
        private FullQualifiedName fqn;
        private CsdlDependencyNode.DependencyType dependencyType;
        private String propertyName;

        public TestExtendedCsdlElement(String elementId, FullQualifiedName fqn, 
                                     CsdlDependencyNode.DependencyType dependencyType, 
                                     String propertyName) {
            this.elementId = elementId;
            this.fqn = fqn;
            this.dependencyType = dependencyType;
            this.propertyName = propertyName;
        }

        @Override
        public String getElementId() {
            return elementId;
        }

        @Override
        public FullQualifiedName getElementFullyQualifiedName() {
            return fqn;
        }

        @Override
        public CsdlDependencyNode.DependencyType getElementDependencyType() {
            return dependencyType;
        }

        @Override
        public String getElementPropertyName() {
            return propertyName;
        }
    }

    @BeforeEach
    public void setUp() {
        element = new TestExtendedCsdlElement(
            "testElement",
            new FullQualifiedName("com.example", "TestType"),
            CsdlDependencyNode.DependencyType.ENTITY_TYPE,
            "testProperty"
        );
    }

    @Test
    public void testGetElementId() {
        assertEquals("testElement", element.getElementId());
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        FullQualifiedName fqn = element.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("com.example", fqn.getNamespace());
        assertEquals("TestType", fqn.getName());
    }

    @Test
    public void testGetElementDependencyType() {
        assertEquals(CsdlDependencyNode.DependencyType.ENTITY_TYPE, element.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        assertEquals("testProperty", element.getElementPropertyName());
    }

    @Test
    public void testElementWithNullValues() {
        ExtendedCsdlElement nullElement = new TestExtendedCsdlElement(null, null, null, null);
        
        assertNull(nullElement.getElementId());
        assertNull(nullElement.getElementFullyQualifiedName());
        assertNull(nullElement.getElementDependencyType());
        assertNull(nullElement.getElementPropertyName());
    }

    @Test
    public void testElementWithDifferentDependencyTypes() {
        // 测试不同的依赖类
        ExtendedCsdlElement complexType = new TestExtendedCsdlElement(
            "complexElement",
            new FullQualifiedName("com.example", "ComplexType"),
            CsdlDependencyNode.DependencyType.COMPLEX_TYPE,
            null
        );

        assertEquals(CsdlDependencyNode.DependencyType.COMPLEX_TYPE, complexType.getElementDependencyType());

        ExtendedCsdlElement action = new TestExtendedCsdlElement(
            "actionElement",
            new FullQualifiedName("com.example", "Action"),
            CsdlDependencyNode.DependencyType.ACTION,
            null
        );

        assertEquals(CsdlDependencyNode.DependencyType.ACTION, action.getElementDependencyType());
    }

    @Test
    public void testElementWithEmptyValues() {
        ExtendedCsdlElement emptyElement = new TestExtendedCsdlElement(
            "",
            new FullQualifiedName("", ""),
            CsdlDependencyNode.DependencyType.ENTITY_TYPE,
            ""
        );

        assertEquals("", emptyElement.getElementId());
        assertEquals("", emptyElement.getElementFullyQualifiedName().getNamespace());
        assertEquals("", emptyElement.getElementFullyQualifiedName().getName());
        assertEquals("", emptyElement.getElementPropertyName());
    }

    @Test
    public void testFqnWithOnlyNamespace() {
        ExtendedCsdlElement element = new TestExtendedCsdlElement(
            "test",
            new FullQualifiedName("com.example", null),
            CsdlDependencyNode.DependencyType.ENTITY_TYPE,
            null
        );

        FullQualifiedName fqn = element.getElementFullyQualifiedName();
        assertEquals("com.example", fqn.getNamespace());
        assertNull(fqn.getName());
    }

    @Test
    public void testFqnWithOnlyName() {
        ExtendedCsdlElement element = new TestExtendedCsdlElement(
            "test",
            new FullQualifiedName(null, "TestType"),
            CsdlDependencyNode.DependencyType.ENTITY_TYPE,
            null
        );

        FullQualifiedName fqn = element.getElementFullyQualifiedName();
        assertNull(fqn.getNamespace());
        assertEquals("TestType", fqn.getName());
    }

    @Test
    public void testMultipleElementsWithSameInterface() {
        ExtendedCsdlElement element1 = new TestExtendedCsdlElement(
            "element1",
            new FullQualifiedName("com.example", "Type1"),
            CsdlDependencyNode.DependencyType.ENTITY_TYPE,
            "prop1"
        );

        ExtendedCsdlElement element2 = new TestExtendedCsdlElement(
            "element2",
            new FullQualifiedName("com.example", "Type2"),
            CsdlDependencyNode.DependencyType.COMPLEX_TYPE,
            "prop2"
        );

        // 验证两个元素是独立的
        assertNotEquals(element1.getElementId(), element2.getElementId());
        assertNotEquals(element1.getElementFullyQualifiedName().getName(), 
                       element2.getElementFullyQualifiedName().getName());
        assertNotEquals(element1.getElementDependencyType(), element2.getElementDependencyType());
        assertNotEquals(element1.getElementPropertyName(), element2.getElementPropertyName());
    }

    @Test
    public void testInterfaceImplementation() {
        // 验证我们的测试类确实实现了接口
        assertTrue(element instanceof ExtendedCsdlElement);
    }

    @Test
    public void testAllDependencyTypes() {
        // 测试所有依赖类型都可以正确设置和获
        CsdlDependencyNode.DependencyType[] types = CsdlDependencyNode.DependencyType.values();
        
        for (CsdlDependencyNode.DependencyType type : types) {
            ExtendedCsdlElement testElement = new TestExtendedCsdlElement(
                "test_" + type.name(),
                new FullQualifiedName("com.test", type.name()),
                type,
                "property_" + type.name()
            );
            
            assertEquals(type, testElement.getElementDependencyType());
        }
    }
}
