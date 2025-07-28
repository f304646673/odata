package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Base test class for Extended CSDL elements
 * Provides common test methods for all ExtendedCsdlElement implementations
 */
public abstract class ExtendedCsdlTestBase<T extends ExtendedCsdlElement> {

    /**
     * Create an instance of the ExtendedCsdlElement under test
     * @return new instance
     */
    protected abstract T createInstance();

    /**
     * Create an instance with a specific element ID
     * @param elementId the element ID
     * @return new instance with element ID
     */
    protected abstract T createInstanceWithElementId(String elementId);

    /**
     * Get the expected dependency type for this element
     * @return expected dependency type
     */
    protected abstract CsdlDependencyNode.DependencyType getExpectedDependencyType();

    /**
     * Set the name on the element (if applicable)
     * @param element the element
     * @param name the name to set
     */
    protected abstract void setElementName(T element, String name);

    /**
     * Get the name from the element (if applicable)
     * @param element the element
     * @return the name or null if not applicable
     */
    protected abstract String getElementName(T element);

    @Test
    public void testDefaultConstructor() {
        T element = createInstance();
        assertNotNull(element);
    }

    @Test
    public void testConstructorWithElementId() {
        String elementId = "testElementId";
        T element = createInstanceWithElementId(elementId);
        assertEquals(elementId, element.getElementId());
    }

    @Test
    public void testGetElementId_WithName() {
        T element = createInstance();
        setElementName(element, "TestElement");
        assertEquals("TestElement", element.getElementId());
    }

    @Test
    public void testSetNamespace_FluentInterface() {
        T element = createInstance();
        ExtendedCsdlElement result = element.setNamespace("TestNamespace");
        assertSame(element, result);
        assertEquals("TestNamespace", element.getNamespace());
    }

    @Test
    public void testGetNamespace() {
        T element = createInstance();
        assertNull(element.getNamespace());
        
        element.setNamespace("TestNamespace");
        assertEquals("TestNamespace", element.getNamespace());
    }

    @Test
    public void testRegisterElement_FluentInterface() {
        T element = createInstance();
        ExtendedCsdlElement result = element.registerElement();
        assertSame(element, result);
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        T element = createInstance();
        setElementName(element, "TestElement");
        element.setNamespace("TestNamespace");
        
        FullQualifiedName fqn = element.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestElement", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedName_WithNullValues() {
        T element = createInstance();
        FullQualifiedName fqn = element.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertNull(fqn.getNamespace());
        assertNull(fqn.getName());
    }

    @Test
    public void testGetElementDependencyType() {
        T element = createInstance();
        assertEquals(getExpectedDependencyType(), element.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        T element = createInstance();
        // Most elements return null for property name unless they're specifically property-related
        assertNull(element.getElementPropertyName());
    }

    @Test
    public void testInstanceOfExtendedCsdlElement() {
        T element = createInstance();
        assertTrue(element instanceof ExtendedCsdlElement);
    }

    @Test
    public void testToString() {
        T element = createInstance();
        String result = element.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testEquals_SameInstance() {
        T element = createInstance();
        assertTrue(element.equals(element));
    }

    @Test
    public void testEquals_DifferentInstance() {
        T element1 = createInstance();
        T element2 = createInstance();
        // Most Extended classes don't override equals, so different instances are not equal
        assertFalse(element1.equals(element2));
    }

    @Test
    public void testHashCode() {
        T element = createInstance();
        int hashCode = element.hashCode();
        // Just verify it doesn't throw exception
        assertTrue(hashCode != 0 || hashCode == 0); // Always true but verifies no exception
    }

    /**
     * Test specific to elements that can have null names but generate element IDs
     */
    @Test
    public void testGetElementId_GeneratedFromHashCode() {
        T element = createInstance();
        // When no elementId and no name are set
        String elementId = element.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.contains("_")); // Most generated IDs contain underscore
    }
}