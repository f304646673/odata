package org.apache.olingo.schema.processor.analyzer.impl;

import org.apache.olingo.schema.processor.analyzer.DependencyAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 依赖分析器默认实现的单元测试
 */
class DefaultDependencyAnalyzerTest {
    
    private DefaultDependencyAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new DefaultDependencyAnalyzer();
    }
    
    @Test
    void testAddAndGetDirectDependencies() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("A", "C");
        
        Set<String> dependencies = analyzer.getDirectDependencies("A");
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains("B"));
        assertTrue(dependencies.contains("C"));
        
        Set<String> noDependencies = analyzer.getDirectDependencies("X");
        assertTrue(noDependencies.isEmpty());
    }
    
    @Test
    void testGetRecursiveDependencies() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("B", "C");
        analyzer.addDependency("C", "D");
        
        Set<String> recursiveDeps = analyzer.getRecursiveDependencies("A");
        assertEquals(3, recursiveDeps.size());
        assertTrue(recursiveDeps.contains("B"));
        assertTrue(recursiveDeps.contains("C"));
        assertTrue(recursiveDeps.contains("D"));
    }
    
    @Test
    void testGetReverseDependencies() {
        analyzer.addDependency("A", "C");
        analyzer.addDependency("B", "C");
        
        Set<String> reverseDeps = analyzer.getReverseDependencies("C");
        assertEquals(2, reverseDeps.size());
        assertTrue(reverseDeps.contains("A"));
        assertTrue(reverseDeps.contains("B"));
    }
    
    @Test
    void testDetectCircularDependencies_NoCycles() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("B", "C");
        
        List<DependencyAnalyzer.DependencyCycle> cycles = analyzer.detectCircularDependencies();
        assertTrue(cycles.isEmpty());
    }
    
    @Test
    void testDetectCircularDependencies_WithCycles() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("B", "C");
        analyzer.addDependency("C", "A"); // 环形依赖: A -> B -> C -> A
        
        List<DependencyAnalyzer.DependencyCycle> cycles = analyzer.detectCircularDependencies();
        assertFalse(cycles.isEmpty());
        assertEquals(1, cycles.size());
        
        List<String> cycle = cycles.get(0).getCycle();
        assertTrue(cycle.contains("A"));
        assertTrue(cycle.contains("B"));
        assertTrue(cycle.contains("C"));
    }
    
    @Test
    void testHasCircularDependency() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("B", "C");
        assertFalse(analyzer.hasCircularDependency("A"));
        
        analyzer.addDependency("C", "A"); // 创建环
        assertTrue(analyzer.hasCircularDependency("A"));
        assertTrue(analyzer.hasCircularDependency("B"));
        assertTrue(analyzer.hasCircularDependency("C"));
    }
    
    @Test
    void testGetDependencyGraph() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("A", "C");
        analyzer.addDependency("B", "D");
        
        Map<String, Set<String>> graph = analyzer.getDependencyGraph();
        assertEquals(2, graph.size());
        
        Set<String> aDeps = graph.get("A");
        assertEquals(2, aDeps.size());
        assertTrue(aDeps.contains("B"));
        assertTrue(aDeps.contains("C"));
        
        Set<String> bDeps = graph.get("B");
        assertEquals(1, bDeps.size());
        assertTrue(bDeps.contains("D"));
    }
    
    @Test
    void testGetReverseDependencyGraph() {
        analyzer.addDependency("A", "C");
        analyzer.addDependency("B", "C");
        analyzer.addDependency("B", "D");
        
        Map<String, Set<String>> reverseGraph = analyzer.getReverseDependencyGraph();
        assertEquals(2, reverseGraph.size());
        
        Set<String> cReverseDeps = reverseGraph.get("C");
        assertEquals(2, cReverseDeps.size());
        assertTrue(cReverseDeps.contains("A"));
        assertTrue(cReverseDeps.contains("B"));
        
        Set<String> dReverseDeps = reverseGraph.get("D");
        assertEquals(1, dReverseDeps.size());
        assertTrue(dReverseDeps.contains("B"));
    }
    
    @Test
    void testGetDependencyLayers() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("A", "C");
        analyzer.addDependency("B", "D");
        analyzer.addDependency("C", "D");
        // D没有依赖，B和C依赖D，A依赖B和C
        
        List<Set<String>> layers = analyzer.getDependencyLayers();
        assertEquals(3, layers.size());
        
        // 第一层：没有依赖的元素
        Set<String> layer0 = layers.get(0);
        assertTrue(layer0.contains("D"));
        
        // 第二层：只依赖第一层的元素
        Set<String> layer1 = layers.get(1);
        assertTrue(layer1.contains("B"));
        assertTrue(layer1.contains("C"));
        
        // 第三层：依赖前面层级的元素
        Set<String> layer2 = layers.get(2);
        assertTrue(layer2.contains("A"));
    }
    
    @Test
    void testGetDependencyStatistics() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("A", "C");
        analyzer.addDependency("B", "D");
        // A依赖2个，B依赖1个，C和D没有依赖
        
        DependencyAnalyzer.DependencyStatistics stats = analyzer.getDependencyStatistics();
        assertEquals(4, stats.getTotalElements());
        assertEquals(2, stats.getElementsWithDependencies()); // A和B有依赖
        assertEquals(2, stats.getElementsWithoutDependencies()); // C和D没有依赖
    }
    
    @Test
    void testAnalyzeImpact() {
        analyzer.addDependency("A", "Core");
        analyzer.addDependency("B", "Core");
        analyzer.addDependency("C", "A"); // C间接依赖Core
        analyzer.addDependency("D", "B"); // D间接依赖Core
        
        DependencyAnalyzer.ImpactAnalysis impact = analyzer.analyzeImpact("Core");
        
        assertEquals("Core", impact.getTargetElement());
        
        // 直接依赖者：A和B
        Set<String> directlyAffected = impact.getDirectlyAffected();
        assertEquals(2, directlyAffected.size());
        assertTrue(directlyAffected.contains("A"));
        assertTrue(directlyAffected.contains("B"));
        
        // 间接依赖者：C和D
        Set<String> indirectlyAffected = impact.getIndirectlyAffected();
        assertEquals(2, indirectlyAffected.size());
        assertTrue(indirectlyAffected.contains("C"));
        assertTrue(indirectlyAffected.contains("D"));
        
        assertEquals(4, impact.getTotalAffected());
    }
    
    @Test
    void testRemoveDependency() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("A", "C");
        
        analyzer.removeDependency("A", "B");
        
        Set<String> dependencies = analyzer.getDirectDependencies("A");
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains("C"));
        assertFalse(dependencies.contains("B"));
        
        // 反向依赖也应该被移除
        Set<String> reverseDeps = analyzer.getReverseDependencies("B");
        assertTrue(reverseDeps.isEmpty());
    }
    
    @Test
    void testCircularDependencyHandling() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("B", "C");
        analyzer.addDependency("C", "A"); // 环形依赖
        
        // 递归依赖应该能处理环形依赖而不死循环
        Set<String> recursiveDeps = analyzer.getRecursiveDependencies("A");
        assertTrue(recursiveDeps.contains("B"));
        assertTrue(recursiveDeps.contains("C"));
        
        // 应该检测到环形依赖
        assertTrue(analyzer.hasCircularDependency("A"));
        
        List<DependencyAnalyzer.DependencyCycle> cycles = analyzer.detectCircularDependencies();
        assertFalse(cycles.isEmpty());
    }
}
