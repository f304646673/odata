package org.apache.olingo.schema.processor.model.extended;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 DependencyTracker 接口的基本实现
 */
public class DependencyTrackerTest {

    private DependencyTracker dependencyTracker;

    // 简单的测试实现
    private static class TestDependencyTracker implements DependencyTracker {
        private final Set<String> dependencies = new HashSet<>();
        private String fullyQualifiedName;

        @Override
        public Set<String> getDirectDependencies() {
            return new HashSet<>(dependencies);
        }

        @Override
        public Set<String> getRecursiveDependencies() {
            return getDirectDependencies(); // 简化实现
        }

        @Override
        public void addDependency(String fullyQualifiedTypeName) {
            if (fullyQualifiedTypeName != null) {
                dependencies.add(fullyQualifiedTypeName);
            }
        }

        @Override
        public void removeDependency(String fullyQualifiedTypeName) {
            dependencies.remove(fullyQualifiedTypeName);
        }

        @Override
        public void clearDependencies() {
            dependencies.clear();
        }

        @Override
        public boolean hasCircularDependencies() {
            return dependencies.contains(fullyQualifiedName);
        }

        @Override
        public String getFullyQualifiedName() {
            return fullyQualifiedName;
        }

        @Override
        public void setFullyQualifiedName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
        }
    }

    @BeforeEach
    public void setUp() {
        dependencyTracker = new TestDependencyTracker();
    }

    @Test
    public void testAddDependency() {
        String dependency = "com.example.Type1";
        dependencyTracker.addDependency(dependency);
        
        assertTrue(dependencyTracker.getDirectDependencies().contains(dependency));
        assertEquals(1, dependencyTracker.getDirectDependencies().size());
    }

    @Test
    public void testAddNullDependency() {
        dependencyTracker.addDependency(null);
        assertTrue(dependencyTracker.getDirectDependencies().isEmpty());
    }

    @Test
    public void testAddMultipleDependencies() {
        dependencyTracker.addDependency("com.example.Type1");
        dependencyTracker.addDependency("com.example.Type2");
        dependencyTracker.addDependency("com.example.Type3");
        
        assertEquals(3, dependencyTracker.getDirectDependencies().size());
        assertTrue(dependencyTracker.getDirectDependencies().contains("com.example.Type1"));
        assertTrue(dependencyTracker.getDirectDependencies().contains("com.example.Type2"));
        assertTrue(dependencyTracker.getDirectDependencies().contains("com.example.Type3"));
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
        dependencyTracker.removeDependency("com.example.NonExistent");
        
        assertEquals(1, dependencyTracker.getDirectDependencies().size());
        assertTrue(dependencyTracker.getDirectDependencies().contains("com.example.Type1"));
    }

    @Test
    public void testClearDependencies() {
        dependencyTracker.addDependency("com.example.Type1");
        dependencyTracker.addDependency("com.example.Type2");
        
        dependencyTracker.clearDependencies();
        
        assertTrue(dependencyTracker.getDirectDependencies().isEmpty());
    }

    @Test
    public void testGetRecursiveDependencies() {
        dependencyTracker.addDependency("com.example.Type1");
        dependencyTracker.addDependency("com.example.Type2");
        
        Set<String> recursive = dependencyTracker.getRecursiveDependencies();
        assertEquals(dependencyTracker.getDirectDependencies(), recursive);
    }

    @Test
    public void testSetAndGetFullyQualifiedName() {
        String fqn = "com.example.MyType";
        dependencyTracker.setFullyQualifiedName(fqn);
        
        assertEquals(fqn, dependencyTracker.getFullyQualifiedName());
    }

    @Test
    public void testHasCircularDependencies() {
        String selfType = "com.example.SelfType";
        dependencyTracker.setFullyQualifiedName(selfType);
        
        assertFalse(dependencyTracker.hasCircularDependencies());
        
        dependencyTracker.addDependency(selfType);
        assertTrue(dependencyTracker.hasCircularDependencies());
    }

    @Test
    public void testHasCircularDependenciesWithoutSelfReference() {
        dependencyTracker.setFullyQualifiedName("com.example.Type1");
        dependencyTracker.addDependency("com.example.Type2");
        dependencyTracker.addDependency("com.example.Type3");
        
        assertFalse(dependencyTracker.hasCircularDependencies());
    }

    @Test
    public void testGetDirectDependenciesIsDefensiveCopy() {
        dependencyTracker.addDependency("com.example.Type1");
        Set<String> dependencies = dependencyTracker.getDirectDependencies();
        
        dependencies.add("com.example.Type2");
        
        // 原始集合不应该被修改
        assertEquals(1, dependencyTracker.getDirectDependencies().size());
        assertFalse(dependencyTracker.getDirectDependencies().contains("com.example.Type2"));
    }
}
