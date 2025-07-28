package org.apache.olingo.schema.processor.model.extended;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ExtendedCsdlReturnType class
 */
public class ExtendedCsdlReturnTypeTest {
    
    private ExtendedCsdlReturnType returnType;
    
    @BeforeEach
    public void setUp() {
        returnType = new ExtendedCsdlReturnType();
    }
    
    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlReturnType rt = new ExtendedCsdlReturnType();
        assertNotNull(rt);
        assertTrue(rt.getDependencies().isEmpty());
    }
    
    @Test
    public void testInheritanceFromCsdlReturnType() {
        assertTrue(returnType instanceof org.apache.olingo.commons.api.edm.provider.CsdlReturnType);
    }
    
    @Test
    public void testGetDependenciesInitiallyEmpty() {
        assertTrue(returnType.getDependencies().isEmpty());
    }
    
    @Test
    public void testAddDependency() {
        returnType.addDependency("com.example");
        assertTrue(returnType.getDependencies().contains("com.example"));
        assertEquals(1, returnType.getDependencies().size());
        
        returnType.addDependency("com.other");
        assertTrue(returnType.getDependencies().contains("com.other"));
        assertEquals(2, returnType.getDependencies().size());
    }
    
    @Test
    public void testAddDependencyIgnoresNullAndEmpty() {
        returnType.addDependency(null);
        returnType.addDependency("");
        returnType.addDependency("   ");
        
        assertTrue(returnType.getDependencies().isEmpty());
    }
    
    @Test
    public void testRemoveDependency() {
        returnType.addDependency("com.example");
        returnType.addDependency("com.other");
        assertEquals(2, returnType.getDependencies().size());
        
        boolean removed = returnType.removeDependency("com.example");
        assertTrue(removed);
        assertFalse(returnType.getDependencies().contains("com.example"));
        assertEquals(1, returnType.getDependencies().size());
        
        boolean notRemoved = returnType.removeDependency("com.nonexistent");
        assertFalse(notRemoved);
        assertEquals(1, returnType.getDependencies().size());
    }
    
    @Test
    public void testHasDependency() {
        assertFalse(returnType.hasDependency("com.example"));
        
        returnType.addDependency("com.example");
        assertTrue(returnType.hasDependency("com.example"));
        assertFalse(returnType.hasDependency("com.other"));
    }
    
    @Test
    public void testGetDependenciesReturnsImmutableCopy() {
        returnType.addDependency("com.example");
        
        java.util.Set<String> dependencies1 = returnType.getDependencies();
        java.util.Set<String> dependencies2 = returnType.getDependencies();
        
        assertNotSame(dependencies1, dependencies2);
        assertEquals(dependencies1, dependencies2);
        
        // 修改返回的集合不应影响原始集合
        dependencies1.clear();
        assertFalse(returnType.getDependencies().isEmpty());
    }
    
    @Test
    public void testClearDependencies() {
        returnType.addDependency("com.example");
        returnType.addDependency("com.other");
        assertFalse(returnType.getDependencies().isEmpty());
        
        returnType.clearDependencies();
        assertTrue(returnType.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependencies() {
        // 测试基础功能
        returnType.setType("com.example.CustomerType");
        returnType.analyzeDependencies();
        
        assertTrue(returnType.getDependencies().contains("com.example"));
        assertEquals(1, returnType.getDependencies().size());
    }
    
    @Test
    public void testAnalyzeDependenciesWithCollectionType() {
        returnType.setType("Collection(com.example.items.Item)");
        returnType.analyzeDependencies();
        
        assertTrue(returnType.getDependencies().contains("com.example.items"));
        assertEquals(1, returnType.getDependencies().size());
    }
    
    @Test
    public void testAnalyzeDependenciesIgnoresEdmTypes() {
        returnType.setType("Edm.String");
        returnType.analyzeDependencies();
        
        assertTrue(returnType.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesWithNullType() {
        returnType.setType((String)null);
        returnType.analyzeDependencies();
        
        assertTrue(returnType.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesWithEmptyType() {
        returnType.setType("");
        returnType.analyzeDependencies();
        
        assertTrue(returnType.getDependencies().isEmpty());
    }
    
    @Test
    public void testSetFullyQualifiedName() {
        assertNull(returnType.getFullyQualifiedName());
        
        returnType.setFullyQualifiedName("com.example.function.ReturnType");
        assertEquals("com.example.function.ReturnType", returnType.getFullyQualifiedName());
    }
    
    @Test
    public void testFluentInterface() {
        ExtendedCsdlReturnType result1 = returnType.setType("com.example.Type");
        assertSame(returnType, result1);
        
        ExtendedCsdlReturnType result2 = returnType.setNullable(false);
        assertSame(returnType, result2);
        
        ExtendedCsdlReturnType result3 = returnType.setCollection(true);
        assertSame(returnType, result3);
    }
    
    @Test
    public void testToString() {
        returnType.setType("com.example.Type");
        returnType.addDependency("com.example");
        
        String toString = returnType.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ExtendedCsdlReturnType"));
    }
}
