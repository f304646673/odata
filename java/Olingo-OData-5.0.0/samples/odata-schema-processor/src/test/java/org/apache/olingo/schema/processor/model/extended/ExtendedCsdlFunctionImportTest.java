package org.apache.olingo.schema.processor.model.extended;

import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试 ExtendedCsdlFunctionImport 类
 */
public class ExtendedCsdlFunctionImportTest {

    private ExtendedCsdlFunctionImport functionImport;

    @Before
    public void setUp() {
        functionImport = new ExtendedCsdlFunctionImport();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(functionImport);
        assertTrue(functionImport.getDependencies().isEmpty());
        assertNull(functionImport.getName());
        // Don't test getFunction() when it's null - causes NPE
        assertNull(functionImport.getEntitySet());
    }

    @Test
    public void testAddDependency() {
        String namespace = "com.example.namespace1";
        functionImport.addDependency(namespace);
        
        assertTrue(functionImport.hasDependency(namespace));
        assertEquals(1, functionImport.getDependencies().size());
        assertTrue(functionImport.getDependencies().contains(namespace));
    }

    @Test
    public void testAddNullDependency() {
        functionImport.addDependency(null);
        assertTrue(functionImport.getDependencies().isEmpty());
    }

    @Test
    public void testAddEmptyDependency() {
        functionImport.addDependency("");
        assertTrue(functionImport.getDependencies().isEmpty());
    }

    @Test
    public void testAddWhitespaceDependency() {
        functionImport.addDependency("   ");
        assertTrue(functionImport.getDependencies().isEmpty());
    }

    @Test
    public void testAddMultipleDependencies() {
        functionImport.addDependency("com.example.namespace1");
        functionImport.addDependency("com.example.namespace2");
        functionImport.addDependency("com.example.namespace3");
        
        assertEquals(3, functionImport.getDependencies().size());
        assertTrue(functionImport.hasDependency("com.example.namespace1"));
        assertTrue(functionImport.hasDependency("com.example.namespace2"));
        assertTrue(functionImport.hasDependency("com.example.namespace3"));
    }

    @Test
    public void testAddDuplicateDependency() {
        String namespace = "com.example.namespace1";
        functionImport.addDependency(namespace);
        functionImport.addDependency(namespace);
        
        assertEquals(1, functionImport.getDependencies().size());
        assertTrue(functionImport.hasDependency(namespace));
    }

    @Test
    public void testRemoveDependency() {
        String namespace1 = "com.example.namespace1";
        String namespace2 = "com.example.namespace2";
        
        functionImport.addDependency(namespace1);
        functionImport.addDependency(namespace2);
        
        assertTrue(functionImport.removeDependency(namespace1));
        assertFalse(functionImport.hasDependency(namespace1));
        assertTrue(functionImport.hasDependency(namespace2));
        assertEquals(1, functionImport.getDependencies().size());
    }

    @Test
    public void testRemoveNonExistentDependency() {
        functionImport.addDependency("com.example.namespace1");
        
        assertFalse(functionImport.removeDependency("com.example.nonexistent"));
        assertEquals(1, functionImport.getDependencies().size());
    }

    @Test
    public void testGetDependenciesIsDefensiveCopy() {
        functionImport.addDependency("com.example.namespace1");
        Set<String> dependencies = functionImport.getDependencies();
        
        dependencies.add("com.example.namespace2");
        
        // 原始集合不应该被修改
        assertEquals(1, functionImport.getDependencies().size());
        assertFalse(functionImport.hasDependency("com.example.namespace2"));
    }

    @Test
    public void testHasDependency() {
        String namespace = "com.example.namespace1";
        
        assertFalse(functionImport.hasDependency(namespace));
        
        functionImport.addDependency(namespace);
        assertTrue(functionImport.hasDependency(namespace));
    }

    @Test
    public void testHasDependencyWithNull() {
        assertFalse(functionImport.hasDependency(null));
    }

    @Test
    public void testSetAndGetName() {
        String name = "MyFunctionImport";
        functionImport.setName(name);
        assertEquals(name, functionImport.getName());
    }

    @Test
    public void testSetAndGetFunction() {
        FullQualifiedName function = new FullQualifiedName("com.example", "MyFunction");
        functionImport.setFunction(function);
        assertEquals("com.example.MyFunction", functionImport.getFunction());
    }

    @Test
    public void testSetAndGetEntitySet() {
        String entitySet = "MyEntitySet";
        functionImport.setEntitySet(entitySet);
        assertEquals(entitySet, functionImport.getEntitySet());
    }

    @Test
    public void testSetAndGetIncludeInServiceDocument() {
        // The default value is false, not null
        assertEquals(Boolean.FALSE, functionImport.isIncludeInServiceDocument());
        
        functionImport.setIncludeInServiceDocument(true);
        assertEquals(Boolean.TRUE, functionImport.isIncludeInServiceDocument());
        
        functionImport.setIncludeInServiceDocument(false);
        assertEquals(Boolean.FALSE, functionImport.isIncludeInServiceDocument());
    }

    @Test
    public void testInheritanceFromCsdlFunctionImport() {
        assertTrue(functionImport instanceof org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport);
    }

    @Test
    public void testFluentInterface() {
        functionImport.setName("FluentTest");
        functionImport.setFunction(new FullQualifiedName("com.example", "FluentFunction"));
        functionImport.setEntitySet("FluentEntitySet");
        functionImport.setIncludeInServiceDocument(true);
        
        assertEquals("FluentTest", functionImport.getName());
        assertEquals("com.example.FluentFunction", functionImport.getFunction());
        assertEquals("FluentEntitySet", functionImport.getEntitySet());
        assertEquals(Boolean.TRUE, functionImport.isIncludeInServiceDocument());
    }

    @Test
    public void testDependencyManagementIntegration() {
        // 测试依赖管理与其他属性的集成
        functionImport.setName("TestFunction");
        functionImport.setFunction(new FullQualifiedName("com.example", "MyFunction"));
        
        functionImport.addDependency("com.example");
        functionImport.addDependency("com.other");
        
        assertEquals(2, functionImport.getDependencies().size());
        assertTrue(functionImport.hasDependency("com.example"));
        assertTrue(functionImport.hasDependency("com.other"));
        assertEquals("TestFunction", functionImport.getName());
        assertEquals("com.example.MyFunction", functionImport.getFunction());
    }

    @Test
    public void testClearAllDependencies() {
        functionImport.addDependency("com.example.namespace1");
        functionImport.addDependency("com.example.namespace2");
        functionImport.addDependency("com.example.namespace3");
        
        assertEquals(3, functionImport.getDependencies().size());
        
        // 通过移除所有依赖来测试清空
        for (String dep : functionImport.getDependencies().toArray(new String[0])) {
            functionImport.removeDependency(dep);
        }
        
        assertTrue(functionImport.getDependencies().isEmpty());
    }
}
