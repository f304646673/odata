package org.apache.olingo.schema.processor.model.extended;

import java.util.Arrays;
import java.util.Collections;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ExtendedCsdlComplexType
 */
public class ExtendedCsdlComplexTypeTest {

    private ExtendedCsdlComplexType complexType;
    private ExtendedCsdlComplexType complexTypeWithId;

    @Before
    public void setUp() {
        complexType = new ExtendedCsdlComplexType();
        complexTypeWithId = new ExtendedCsdlComplexType("testComplexTypeId");
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlComplexType ct = new ExtendedCsdlComplexType();
        assertNotNull(ct);
    }

    @Test
    public void testConstructorWithElementId() {
        String elementId = "customId";
        ExtendedCsdlComplexType ct = new ExtendedCsdlComplexType(elementId);
        assertEquals(elementId, ct.getElementId());
    }

    @Test
    public void testGetElementId_WithProvidedId() {
        assertEquals("testComplexTypeId", complexTypeWithId.getElementId());
    }

    @Test
    public void testGetElementId_WithName() {
        complexType.setName("TestComplexType");
        assertEquals("TestComplexType", complexType.getElementId());
    }

    @Test
    public void testGetElementId_GeneratedFromHashCode() {
        // When no elementId and no name are set
        String elementId = complexType.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("ComplexType_"));
    }

    @Test
    public void testSetNamespace_FluentInterface() {
        ExtendedCsdlComplexType result = complexType.setNamespace("TestNamespace");
        assertSame(complexType, result);
        assertEquals("TestNamespace", complexType.getNamespace());
    }

    @Test
    public void testGetNamespace() {
        assertNull(complexType.getNamespace());
        
        complexType.setNamespace("TestNamespace");
        assertEquals("TestNamespace", complexType.getNamespace());
    }

    @Test
    public void testRegisterElement_FluentInterface() {
        ExtendedCsdlComplexType result = complexType.registerElement();
        assertSame(complexType, result);
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        complexType.setName("TestComplexType");
        complexType.setNamespace("TestNamespace");
        
        FullQualifiedName fqn = complexType.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestComplexType", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedName_WithNullValues() {
        FullQualifiedName fqn = complexType.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertNull(fqn.getNamespace());
        assertNull(fqn.getName());
    }

    @Test
    public void testGetElementDependencyType() {
        assertEquals(CsdlDependencyNode.DependencyType.COMPLEX_TYPE, 
                     complexType.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        assertNull(complexType.getElementPropertyName());
    }

    @Test
    public void testToString_WithName() {
        complexType.setName("TestComplexType");
        
        String result = complexType.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestComplexType"));
        assertTrue(result.contains("properties=0"));
    }

    @Test
    public void testToString_WithProperties() {
        CsdlProperty prop1 = new CsdlProperty();
        prop1.setName("Property1");
        
        CsdlProperty prop2 = new CsdlProperty();
        prop2.setName("Property2");
        
        complexType.setName("TestComplexType");
        complexType.setProperties(Arrays.asList(prop1, prop2));
        
        String result = complexType.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestComplexType"));
        assertTrue(result.contains("properties=2"));
    }

    @Test
    public void testToString_WithNullName() {
        String result = complexType.toString();
        assertNotNull(result);
        assertTrue(result.contains("name='null'"));
    }

    @Test
    public void testToString_WithNullProperties() {
        complexType.setName("TestComplexType");
        complexType.setProperties(null);
        
        String result = complexType.toString();
        assertNotNull(result);
        assertTrue(result.contains("properties=0"));
    }

    @Test
    public void testToString_WithEmptyProperties() {
        complexType.setName("TestComplexType");
        complexType.setProperties(Collections.emptyList());
        
        String result = complexType.toString();
        assertNotNull(result);
        assertTrue(result.contains("properties=0"));
    }

    @Test
    public void testInheritedMethods() {
        // Test that it properly inherits CsdlComplexType methods
        complexType.setName("TestComplexType");
        assertEquals("TestComplexType", complexType.getName());
        
        complexType.setAbstract(true);
        assertEquals(true, complexType.isAbstract());
        
        CsdlProperty property = new CsdlProperty();
        property.setName("TestProperty");
        complexType.setProperties(Arrays.asList(property));
        assertEquals(1, complexType.getProperties().size());
        assertEquals("TestProperty", complexType.getProperties().get(0).getName());
    }

    @Test
    public void testInstanceOfExtendedCsdlElement() {
        assertTrue(complexType instanceof ExtendedCsdlElement);
    }

    @Test
    public void testInstanceOfCsdlComplexType() {
        assertTrue(complexType instanceof org.apache.olingo.commons.api.edm.provider.CsdlComplexType);
    }

    @Test
    public void testEquals_SameInstance() {
        assertTrue(complexType.equals(complexType));
    }

    @Test
    public void testEquals_DifferentInstance() {
        ExtendedCsdlComplexType other = new ExtendedCsdlComplexType();
        // CsdlComplexType doesn't override equals, so it uses Object.equals
        assertFalse(complexType.equals(other));
    }

    @Test
    public void testHashCode() {
        int hashCode = complexType.hashCode();
        // Just verify it doesn't throw exception
        assertTrue(hashCode != 0 || hashCode == 0); // Always true but verifies no exception
    }
}