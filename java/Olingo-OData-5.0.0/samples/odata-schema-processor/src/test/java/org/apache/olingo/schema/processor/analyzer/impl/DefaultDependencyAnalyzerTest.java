package org.apache.olingo.schema.processor.analyzer.impl;

import org.apache.olingo.schema.processor.analyzer.DependencyAnalyzer;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Default dependency analyzer test class
 */
public class DefaultDependencyAnalyzerTest {
    
    private DefaultDependencyAnalyzer analyzer;
    
    @Before
    public void setUp() {
        analyzer = new DefaultDependencyAnalyzer();
    }
    
    @Test
    public void testAddAndGetDirectDependencies() {
        analyzer.addDependency("A", "B");
        analyzer.addDependency("A", "C");
        
        Set<String> dependencies = analyzer.getDirectDependencies("A");
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains("B"));
        assertTrue(dependencies.contains("C"));
        
        // Test non-existent element
        assertTrue(analyzer.getDirectDependencies("X").isEmpty());
    }
    
    @Test
    public void testBasicFunctionality() {
        assertNotNull(analyzer);
    }
}
