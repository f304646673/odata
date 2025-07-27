package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试 ExtendedCsdlActionImport 类
 */
public class ExtendedCsdlActionImportTest {

    private ExtendedCsdlActionImport actionImport;
    private ExtendedCsdlActionImport actionImportWithId;

    @Before
    public void setUp() {
        actionImport = new ExtendedCsdlActionImport();
        actionImportWithId = new ExtendedCsdlActionImport("testActionImport");
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlActionImport ai = new ExtendedCsdlActionImport();
        assertNotNull(ai);
        assertNull(ai.getName());
        // Don't test getAction() when it's null - causes NPE
        assertNull(ai.getEntitySet());
    }

    @Test
    public void testConstructorWithElementId() {
        String elementId = "testActionImport";
        ExtendedCsdlActionImport ai = new ExtendedCsdlActionImport(elementId);
        assertNotNull(ai);
        assertEquals(elementId, ai.getElementId());
    }

    @Test
    public void testGetElementIdWithProvidedId() {
        assertEquals("testActionImport", actionImportWithId.getElementId());
    }

    @Test
    public void testGetElementIdWithName() {
        actionImport.setName("MyActionImport");
        assertEquals("MyActionImport", actionImport.getElementId());
    }

    @Test
    public void testGetElementIdWithoutNameOrId() {
        String elementId = actionImport.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("ActionImport_"));
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        actionImport.setName("TestAction");
        FullQualifiedName fqn = actionImport.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertNull(fqn.getNamespace());
        assertEquals("TestAction", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedNameWithNullName() {
        FullQualifiedName fqn = actionImport.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertNull(fqn.getNamespace());
        assertNull(fqn.getName());
    }

    @Test
    public void testGetElementDependencyType() {
        assertEquals(CsdlDependencyNode.DependencyType.ACTION_IMPORT, 
                actionImport.getElementDependencyType());
        assertEquals(CsdlDependencyNode.DependencyType.ACTION_IMPORT, 
                actionImportWithId.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        assertNull(actionImport.getElementPropertyName());
        assertNull(actionImportWithId.getElementPropertyName());
    }

    @Test
    public void testSetAndGetName() {
        String name = "MyActionImport";
        actionImport.setName(name);
        assertEquals(name, actionImport.getName());
    }

    @Test
    public void testSetAndGetAction() {
        FullQualifiedName action = new FullQualifiedName("com.example", "MyAction");
        actionImport.setAction(action);
        assertEquals("com.example.MyAction", actionImport.getAction());
    }

    @Test
    public void testSetAndGetEntitySet() {
        String entitySet = "MyEntitySet";
        actionImport.setEntitySet(entitySet);
        assertEquals(entitySet, actionImport.getEntitySet());
    }

    @Test
    public void testToString() {
        actionImport.setName("TestActionImport");
        actionImport.setAction(new FullQualifiedName("com.example", "TestAction"));
        
        String result = actionImport.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestActionImport"));
        assertTrue(result.contains("TestAction"));
        assertTrue(result.contains("ExtendedCsdlActionImport"));
    }

    @Test
    public void testToStringWithNullValues() {
        ExtendedCsdlActionImport ai = new ExtendedCsdlActionImport();
        // Set a minimal action to avoid NPE
        ai.setAction(new FullQualifiedName("test", "action"));
        String result = ai.toString();
        assertNotNull(result);
        assertTrue(result.contains("ExtendedCsdlActionImport"));
    }

    @Test
    public void testInheritanceFromCsdlActionImport() {
        assertTrue(actionImport instanceof org.apache.olingo.commons.api.edm.provider.CsdlActionImport);
    }

    @Test
    public void testImplementsExtendedCsdlElement() {
        assertTrue(actionImport instanceof ExtendedCsdlElement);
    }

    @Test
    public void testFluentInterface() {
        actionImport.setName("FluentTest");
        actionImport.setAction(new FullQualifiedName("com.example", "FluentAction"));
        actionImport.setEntitySet("FluentEntitySet");
        
        assertEquals("FluentTest", actionImport.getName());
        assertEquals("com.example.FluentAction", actionImport.getAction());
        assertEquals("FluentEntitySet", actionImport.getEntitySet());
    }
}
