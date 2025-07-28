package org.apache.olingo.schema.processor.model.extended;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 AbstractDependencyTracker
 */
public class AbstractDependencyTrackerTest {

    private AbstractDependencyTracker dependencyTracker;

    // 用于测试的具体实现
    private static class TestAbstractDependencyTracker extends AbstractDependencyTracker {
        // 为了测试抽象类，提供一个空的具体实现
    }

    @BeforeEach
    public void setUp() {
        dependencyTracker = new TestAbstractDependencyTracker();
    }

    @Test
    public void testInitialState() {
        assertTrue(dependencyTracker.getDirectDependencies().isEmpty());
        assertNull(dependencyTracker.getFullyQualifiedName());
        assertFalse(dependencyTracker.hasCircularDependencies());
    }

    @Test
    public void testAddDependency() {
        String dependency = "com.example.Type1";
        dependencyTracker.addDependency(dependency);
        
        Set<String> dependencies = dependencyTracker.getDirectDependencies();
        assertTrue(dependencies.contains(dependency));
        assertEquals(1, dependencies.size());
    }

    @Test
    public void testAddNullDependency() {
        dependencyTracker.addDependency(null);
        assertTrue(dependencyTracker.getDirectDependencies().isEmpty());
    }

    @Test
    public void testAddEmptyDependency() {
        dependencyTracker.addDependency("");
        assertTrue(dependencyTracker.getDirectDependencies().isEmpty());
    }

    @Test
    public void testAddWhitespaceDependency() {
        dependencyTracker.addDependency("   ");
        assertTrue(dependencyTracker.getDirectDependencies().isEmpty());
    }

    @Test
    public void testAddValidDependencyWithWhitespace() {
        dependencyTracker.addDependency("  com.example.Type1  ");
        assertTrue(dependencyTracker.getDirectDependencies().contains("  com.example.Type1  "));
    }

    @Test
    public void testRemoveDependency() {
        String dependency = "com.example.Type1";
        dependencyTracker.addDependency(dependency);
        dependencyTracker.addDependency("com.example.Type2");
        
        dependencyTracker.removeDependency(dependency);
        
        assertFalse(dependencyTracker.getDirectDependencies().contains(dependency));
        assertTrue(dependencyTracker.getDirectDependencies().contains("com.example.Type2"));
        assertEquals(1, dependencyTracker.getDirectDependencies().size());
    }

    @Test
    public void testRemoveNonExistentDependency() {
        dependencyTracker.addDependency("com.example.Type1");
        int sizeBefore = dependencyTracker.getDirectDependencies().size();
        
        dependencyTracker.removeDependency("com.example.NonExistent");
        
        assertEquals(sizeBefore, dependencyTracker.getDirectDependencies().size());
    }

    @Test
    public void testClearDependencies() {
        dependencyTracker.addDependency("com.example.Type1");
        dependencyTracker.addDependency("com.example.Type2");
        dependencyTracker.addDependency("com.example.Type3");
        
        dependencyTracker.clearDependencies();
        
        assertTrue(dependencyTracker.getDirectDependencies().isEmpty());
    }

    @Test
    public void testGetDirectDependenciesIsUnmodifiable() {
        dependencyTracker.addDependency("com.example.Type1");
        Set<String> dependencies = dependencyTracker.getDirectDependencies();
        
        try {
            dependencies.add("com.example.Type2");
            fail("应该抛出UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // 期望的行
        }
        
        // 确保原始集合没有被修
        assertEquals(1, dependencyTracker.getDirectDependencies().size());
        assertTrue(dependencyTracker.getDirectDependencies().contains("com.example.Type1"));
    }

    @Test
    public void testGetRecursiveDependencies() {
        dependencyTracker.addDependency("com.example.Type1");
        dependencyTracker.addDependency("com.example.Type2");
        
        Set<String> recursive = dependencyTracker.getRecursiveDependencies();
        Set<String> direct = dependencyTracker.getDirectDependencies();
        
        assertEquals(direct, recursive);
    }

    @Test
    public void testGetRecursiveDependenciesIsUnmodifiable() {
        dependencyTracker.addDependency("com.example.Type1");
        Set<String> recursive = dependencyTracker.getRecursiveDependencies();
        
        try {
            recursive.add("com.example.Type2");
            fail("应该抛出UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // 期望的行
        }
    }

    @Test
    public void testSetAndGetFullyQualifiedName() {
        String fqn = "com.example.MyType";
        dependencyTracker.setFullyQualifiedName(fqn);
        
        assertEquals(fqn, dependencyTracker.getFullyQualifiedName());
    }

    @Test
    public void testSetNullFullyQualifiedName() {
        dependencyTracker.setFullyQualifiedName("com.example.Type");
        dependencyTracker.setFullyQualifiedName(null);
        
        assertNull(dependencyTracker.getFullyQualifiedName());
    }

    @Test
    public void testHasCircularDependenciesWithoutSelfReference() {
        dependencyTracker.setFullyQualifiedName("com.example.Type1");
        dependencyTracker.addDependency("com.example.Type2");
        dependencyTracker.addDependency("com.example.Type3");
        
        assertFalse(dependencyTracker.hasCircularDependencies());
    }

    @Test
    public void testHasCircularDependenciesWithSelfReference() {
        String selfType = "com.example.SelfType";
        dependencyTracker.setFullyQualifiedName(selfType);
        dependencyTracker.addDependency(selfType);
        
        assertTrue(dependencyTracker.hasCircularDependencies());
    }

    @Test
    public void testHasCircularDependenciesWithNullFQN() {
        dependencyTracker.setFullyQualifiedName(null);
        dependencyTracker.addDependency("com.example.Type1");
        
        assertFalse(dependencyTracker.hasCircularDependencies());
    }

    @Test
    public void testHasCircularDependenciesAfterRemovingSelfReference() {
        String selfType = "com.example.SelfType";
        dependencyTracker.setFullyQualifiedName(selfType);
        dependencyTracker.addDependency(selfType);
        
        assertTrue(dependencyTracker.hasCircularDependencies());
        
        dependencyTracker.removeDependency(selfType);
        assertFalse(dependencyTracker.hasCircularDependencies());
    }

    @Test
    public void testAddDuplicateDependency() {
        String dependency = "com.example.Type1";
        dependencyTracker.addDependency(dependency);
        dependencyTracker.addDependency(dependency);
        
        assertEquals(1, dependencyTracker.getDirectDependencies().size());
        assertTrue(dependencyTracker.getDirectDependencies().contains(dependency));
    }
}
