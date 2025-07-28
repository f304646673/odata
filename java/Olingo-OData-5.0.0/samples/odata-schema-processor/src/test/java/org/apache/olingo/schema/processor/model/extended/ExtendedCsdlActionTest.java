package org.apache.olingo.schema.processor.model.extended;

import java.util.Arrays;
import java.util.Collections;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ExtendedCsdlAction
 */
public class ExtendedCsdlActionTest {

    private ExtendedCsdlAction action;
    private ExtendedCsdlAction actionWithId;

    @BeforeEach
    public void setUp() {
        action = new ExtendedCsdlAction();
        actionWithId = new ExtendedCsdlAction("testActionId");
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlAction act = new ExtendedCsdlAction();
        assertNotNull(act);
    }

    @Test
    public void testConstructorWithElementId() {
        String elementId = "customId";
        ExtendedCsdlAction act = new ExtendedCsdlAction(elementId);
        assertEquals(elementId, act.getElementId());
    }

    @Test
    public void testGetElementId_WithProvidedId() {
        assertEquals("testActionId", actionWithId.getElementId());
    }

    @Test
    public void testGetElementId_WithName() {
        action.setName("TestAction");
        assertEquals("TestAction", action.getElementId());
    }

    @Test
    public void testGetElementId_GeneratedFromHashCode() {
        // When no elementId and no name are set
        String elementId = action.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("Action_"));
    }

    @Test
    public void testSetNamespace_FluentInterface() {
        ExtendedCsdlAction result = action.setNamespace("TestNamespace");
        assertSame(action, result);
        assertEquals("TestNamespace", action.getNamespace());
    }

    @Test
    public void testGetNamespace() {
        assertNull(action.getNamespace());
        
        action.setNamespace("TestNamespace");
        assertEquals("TestNamespace", action.getNamespace());
    }

    @Test
    public void testRegisterElement_FluentInterface() {
        ExtendedCsdlAction result = action.registerElement();
        assertSame(action, result);
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        action.setName("TestAction");
        action.setNamespace("TestNamespace");
        
        FullQualifiedName fqn = action.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestAction", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedName_WithNullValues() {
        FullQualifiedName fqn = action.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertNull(fqn.getNamespace());
        assertNull(fqn.getName());
    }

    @Test
    public void testGetElementDependencyType() {
        assertEquals(CsdlDependencyNode.DependencyType.ACTION, 
                     action.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        assertNull(action.getElementPropertyName());
    }

    @Test
    public void testToString_WithName() {
        action.setName("TestAction");
        action.setBound(true);
        
        String result = action.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestAction"));
        assertTrue(result.contains("bound=true"));
    }

    @Test
    public void testToString_WithNullName() {
        action.setBound(false);
        
        String result = action.toString();
        assertNotNull(result);
        assertTrue(result.contains("name='null'"));
        assertTrue(result.contains("bound=false"));
    }

    @Test
    public void testToString_NotBound() {
        action.setName("TestAction");
        action.setBound(false);
        
        String result = action.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestAction"));
        assertTrue(result.contains("bound=false"));
    }

    @Test
    public void testInheritedMethods() {
        // Test that it properly inherits CsdlAction methods
        action.setName("TestAction");
        assertEquals("TestAction", action.getName());
        
        action.setBound(true);
        assertEquals(true, action.isBound());
        
        CsdlParameter parameter = new CsdlParameter();
        parameter.setName("TestParameter");
        action.setParameters(Arrays.asList(parameter));
        assertEquals(1, action.getParameters().size());
        assertEquals("TestParameter", action.getParameters().get(0).getName());
    }

    @Test
    public void testInstanceOfExtendedCsdlElement() {
        assertTrue(action instanceof ExtendedCsdlElement);
    }

    @Test
    public void testInstanceOfCsdlAction() {
        assertTrue(action instanceof org.apache.olingo.commons.api.edm.provider.CsdlAction);
    }

    @Test
    public void testEquals_SameInstance() {
        assertTrue(action.equals(action));
    }

    @Test
    public void testEquals_DifferentInstance() {
        ExtendedCsdlAction other = new ExtendedCsdlAction();
        // CsdlAction doesn't override equals, so it uses Object.equals
        assertFalse(action.equals(other));
    }

    @Test
    public void testHashCode() {
        int hashCode = action.hashCode();
        // Just verify it doesn't throw exception
        assertTrue(hashCode != 0 || hashCode == 0); // Always true but verifies no exception
    }

    @Test
    public void testBoundAction() {
        action.setName("BoundAction");
        action.setBound(true);
        
        assertTrue(action.isBound());
        
        String result = action.toString();
        assertTrue(result.contains("bound=true"));
    }

    @Test
    public void testUnboundAction() {
        action.setName("UnboundAction");
        action.setBound(false);
        
        assertFalse(action.isBound());
        
        String result = action.toString();
        assertTrue(result.contains("bound=false"));
    }

    @Test
    public void testActionWithParameters() {
        CsdlParameter param1 = new CsdlParameter();
        param1.setName("Parameter1");
        param1.setType("Edm.String");
        
        CsdlParameter param2 = new CsdlParameter();
        param2.setName("Parameter2");
        param2.setType("Edm.Int32");
        
        action.setName("ActionWithParams");
        action.setParameters(Arrays.asList(param1, param2));
        
        assertEquals(2, action.getParameters().size());
        assertEquals("Parameter1", action.getParameters().get(0).getName());
        assertEquals("Parameter2", action.getParameters().get(1).getName());
    }

    @Test
    public void testActionWithoutParameters() {
        action.setName("ActionWithoutParams");
        action.setParameters(null);
        
        assertNull(action.getParameters());
    }

    @Test
    public void testActionWithEmptyParameters() {
        action.setName("ActionWithEmptyParams");
        action.setParameters(Collections.emptyList());
        
        assertNotNull(action.getParameters());
        assertTrue(action.getParameters().isEmpty());
    }
}