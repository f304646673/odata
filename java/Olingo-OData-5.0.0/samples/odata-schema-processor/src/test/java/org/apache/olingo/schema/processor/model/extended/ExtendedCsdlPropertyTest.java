package org.apache.olingo.schema.processor.model.extended;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExtendedCsdlProperty class
 */
public class ExtendedCsdlPropertyTest {
    
    private ExtendedCsdlProperty property;
    
    @BeforeEach
    public void setUp() {
        property = new ExtendedCsdlProperty();
    }
    
    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlProperty prop = new ExtendedCsdlProperty();
        assertNotNull(prop);
        assertNull(prop.getName());
        assertNull(prop.getType());
        assertTrue(prop.getDependencies().isEmpty());
    }
    
    @Test
    public void testSetAndGetName() {
        assertNull(property.getName());
        
        ExtendedCsdlProperty result = property.setName("CustomerName");
        assertEquals("CustomerName", property.getName());
        assertSame(property, result); // 测试流式接口
    }
    
    @Test
    public void testSetAndGetType() {
        assertNull(property.getType());
        
        ExtendedCsdlProperty result = property.setType("Edm.String");
        assertEquals("Edm.String", property.getType());
        assertSame(property, result); // 测试流式接口
    }
    
    @Test
    public void testSetAndGetFullyQualifiedName() {
        assertNull(property.getFullyQualifiedName());
        
        property.setFullyQualifiedName("com.example.Customer.Name");
        assertEquals("com.example.Customer.Name", property.getFullyQualifiedName());
    }
    
    @Test
    public void testSetNullable() {
        // 测试setNullable方法
        ExtendedCsdlProperty result1 = property.setNullable(true);
        assertSame(property, result1);
        
        ExtendedCsdlProperty result2 = property.setNullable(false);
        assertSame(property, result2);
    }
    
    @Test
    public void testSetMaxLength() {
        // 测试setMaxLength方法
        ExtendedCsdlProperty result1 = property.setMaxLength(50);
        assertSame(property, result1);
        
        ExtendedCsdlProperty result2 = property.setMaxLength(null);
        assertSame(property, result2);
    }
    
    @Test
    public void testInheritanceFromCsdlProperty() {
        assertTrue(property instanceof org.apache.olingo.commons.api.edm.provider.CsdlProperty);
    }
    
    @Test
    public void testGetDependenciesInitiallyEmpty() {
        assertTrue(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testAddDependency() {
        property.addDependency("com.example.Customer");
        assertTrue(property.getDependencies().contains("com.example.Customer"));
        assertEquals(1, property.getDependencies().size());
        
        property.addDependency("com.other.Order");
        assertTrue(property.getDependencies().contains("com.other.Order"));
        assertEquals(2, property.getDependencies().size());
    }
    
    @Test
    public void testAddDependencyIgnoresNullAndEmpty() {
        property.addDependency(null);
        property.addDependency("");
        property.addDependency("   ");
        
        assertTrue(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesWithSimpleType() {
        property.setType("com.example.Address");
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().contains("com.example"));
        assertEquals(1, property.getDependencies().size());
    }
    
    @Test
    public void testAnalyzeDependenciesWithCollectionType() {
        property.setType("Collection(com.example.items.Item)");
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().contains("com.example.items"));
        assertEquals(1, property.getDependencies().size());
    }
    
    @Test
    public void testAnalyzeDependenciesIgnoresEdmTypes() {
        property.setType("Edm.String");
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().isEmpty());
        
        property.setType("Collection(Edm.Int32)");
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesWithNullType() {
        property.setType((String)null);
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesWithEmptyType() {
        property.setType("");
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesWithWhitespaceType() {
        property.setType("   ");
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesWithSimpleTypeName() {
        // 没有命名空间的类型名
        property.setType("SimpleType");
        property.analyzeDependencies();
        
        assertTrue(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testAnalyzeDependenciesClearsOldDependencies() {
        property.addDependency("com.old.OldType");
        assertFalse(property.getDependencies().isEmpty());
        
        property.setType("com.new.NewType");
        property.analyzeDependencies();
        
        assertFalse(property.getDependencies().contains("com.old"));
        assertTrue(property.getDependencies().contains("com.new"));
        assertEquals(1, property.getDependencies().size());
    }
    
    @Test
    public void testGetDependenciesReturnsImmutableCopy() {
        property.addDependency("com.example.Type1");
        
        Set<String> dependencies1 = property.getDependencies();
        Set<String> dependencies2 = property.getDependencies();
        
        assertNotSame(dependencies1, dependencies2);
        assertEquals(dependencies1, dependencies2);
        
        // 修改返回的集合不应影响原始集合
        dependencies1.clear();
        assertFalse(property.getDependencies().isEmpty());
    }
    
    @Test
    public void testComplexPropertyConfiguration() {
        ExtendedCsdlProperty result = property
            .setName("CustomerAddress")
            .setType("com.example.addresses.Address")
            .setNullable(false)
            .setMaxLength(100);
        
        assertSame(property, result);
        assertEquals("CustomerAddress", property.getName());
        assertEquals("com.example.addresses.Address", property.getType());
        assertTrue(property.getDependencies().contains("com.example.addresses"));
    }
    
    @Test
    public void testAnalyzeDependenciesTriggeredBySetType() {
        // setType应该自动触发analyzeDependencies
        assertTrue(property.getDependencies().isEmpty());
        
        property.setType("com.example.Customer");
        
        // 依赖应该自动被添加
        assertTrue(property.getDependencies().contains("com.example"));
        assertEquals(1, property.getDependencies().size());
    }
}
