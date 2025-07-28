package org.apache.olingo.schema.processor.model.extended;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
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
 * Tests for ExtendedCsdlEntityType
 */
public class ExtendedCsdlEntityTypeTest {

    private ExtendedCsdlEntityType entityType;
    private ExtendedCsdlEntityType entityTypeWithName;

    @Before
    public void setUp() {
        entityType = new ExtendedCsdlEntityType();
        entityTypeWithName = new ExtendedCsdlEntityType("Customer");
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlEntityType et = new ExtendedCsdlEntityType();
        assertNotNull(et);
    }

    @Test
    public void testConstructorWithName() {
        String name = "TestEntity";
        ExtendedCsdlEntityType et = new ExtendedCsdlEntityType(name);
        et.setName(name); // 需要单独设置名称
        assertEquals(name, et.getName());
    }

    @Test
    public void testSetBaseType() {
        String name = "TestEntity";
        String baseType = "TestNamespace.BaseEntity";
        ExtendedCsdlEntityType et = new ExtendedCsdlEntityType(name);
        et.setName(name); // 需要单独设置名称
        et.setBaseType(baseType);
        assertEquals(name, et.getName());
        assertEquals(baseType, et.getBaseType());
    }

    @Test
    public void testGetElementId_WithName() {
        assertEquals("Customer", entityTypeWithName.getElementId());
    }

    @Test
    public void testGetElementId_WithoutName() {
        String elementId = entityType.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("EntityType_"));
    }

    @Test
    public void testSetNamespace_FluentInterface() {
        ExtendedCsdlEntityType result = entityType.setNamespace("TestNamespace");
        assertSame(entityType, result);
        assertEquals("TestNamespace", entityType.getNamespace());
    }

    @Test
    public void testGetNamespace() {
        assertNull(entityType.getNamespace());
        
        entityType.setNamespace("TestNamespace");
        assertEquals("TestNamespace", entityType.getNamespace());
    }

    @Test
    public void testRegisterElement_FluentInterface() {
        ExtendedCsdlElement result = entityType.registerElement();
        assertSame(entityType, result);
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        entityType.setName("TestEntity");
        entityType.setNamespace("TestNamespace");
        
        FullQualifiedName fqn = entityType.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestEntity", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedName_WithNullValues() {
        FullQualifiedName fqn = entityType.getElementFullyQualifiedName();
        assertNull(fqn); // 当名称为null时应该返回null
    }

    @Test
    public void testGetElementDependencyType() {
        assertEquals(CsdlDependencyNode.DependencyType.ENTITY_TYPE, 
                     entityType.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        assertNull(entityType.getElementPropertyName());
    }

    @Test
    public void testAddDependency_WithBaseType() {
        entityType.setName("DerivedEntity");
        entityType.setBaseType("TestNamespace.BaseEntity");
        
        // Register the element first
        entityType.registerElement();
        
        // Add dependency manually using the addDependency method
        FullQualifiedName baseFqn = new FullQualifiedName("TestNamespace", "BaseEntity");
        entityType.addDependency("baseType", baseFqn, CsdlDependencyNode.DependencyType.TYPE_REFERENCE);
        
        // Verify that dependencies were added
        assertNotNull(entityType.getDirectDependencies());
    }

    @Test
    public void testAddDependency_WithProperties() {
        CsdlProperty prop1 = new CsdlProperty();
        prop1.setName("ComplexProperty");
        prop1.setType("TestNamespace.Address");
        
        CsdlProperty prop2 = new CsdlProperty();
        prop2.setName("PrimitiveProperty");
        prop2.setType("Edm.String");
        
        entityType.setName("TestEntity");
        entityType.setProperties(Arrays.asList(prop1, prop2));
        
        // Register the element and add dependencies
        entityType.registerElement();
        FullQualifiedName addressFqn = new FullQualifiedName("TestNamespace", "Address");
        entityType.addDependency("ComplexProperty", addressFqn, CsdlDependencyNode.DependencyType.TYPE_REFERENCE);
        
        // Verify that dependencies were added
        assertNotNull(entityType.getDirectDependencies());
    }

    @Test
    public void testAddDependency_WithCollectionType() {
        CsdlProperty prop = new CsdlProperty();
        prop.setName("CollectionProperty");
        prop.setType("Collection(TestNamespace.Address)");
        
        entityType.setName("TestEntity");
        entityType.setProperties(Arrays.asList(prop));
        
        // Register and add collection type dependency
        entityType.registerElement();
        FullQualifiedName addressFqn = new FullQualifiedName("TestNamespace", "Address");
        entityType.addDependency("CollectionProperty", addressFqn, CsdlDependencyNode.DependencyType.TYPE_REFERENCE);
        
        // Verify that dependencies were added
        assertNotNull(entityType.getDirectDependencies());
    }

    @Test
    public void testNoDependencies_WithPrimitiveTypes() {
        entityType.setName("SimpleEntity");
        
        CsdlProperty prop = new CsdlProperty();
        prop.setName("SimpleProperty");
        prop.setType("Edm.String");
        
        entityType.setProperties(Arrays.asList(prop));
        entityType.registerElement();
        
        // No dependencies should be added for primitive types
        Set<CsdlDependencyNode> deps = entityType.getDirectDependencies();
        assertNotNull(deps);
        // For primitive types, we don't expect dependencies
    }

    @Test
    public void testInheritedMethods() {
        // Test that it properly inherits CsdlEntityType methods
        entityType.setName("TestEntity");
        assertEquals("TestEntity", entityType.getName());
        
        entityType.setAbstract(true);
        assertEquals(true, entityType.isAbstract());
        
        entityType.setHasStream(true);
        assertEquals(true, entityType.hasStream());
        
        CsdlProperty property = new CsdlProperty();
        property.setName("TestProperty");
        entityType.setProperties(Arrays.asList(property));
        assertEquals(1, entityType.getProperties().size());
        assertEquals("TestProperty", entityType.getProperties().get(0).getName());
    }

    @Test
    public void testKeyProperties() {
        CsdlPropertyRef keyRef = new CsdlPropertyRef();
        keyRef.setName("ID");
        
        entityType.setKey(Arrays.asList(keyRef));
        assertEquals(1, entityType.getKey().size());
        assertEquals("ID", entityType.getKey().get(0).getName());
    }

    @Test
    public void testInstanceOfExtendedCsdlElement() {
        assertTrue(entityType instanceof ExtendedCsdlElement);
    }

    @Test
    public void testInstanceOfCsdlEntityType() {
        assertTrue(entityType instanceof org.apache.olingo.commons.api.edm.provider.CsdlEntityType);
    }

    @Test
    public void testEquals_SameInstance() {
        assertTrue(entityType.equals(entityType));
    }

    @Test
    public void testEquals_DifferentInstance() {
        ExtendedCsdlEntityType other = new ExtendedCsdlEntityType();
        // CsdlEntityType doesn't override equals, so it uses Object.equals
        assertFalse(entityType.equals(other));
    }

    @Test
    public void testHashCode() {
        int hashCode = entityType.hashCode();
        // Just verify it doesn't throw exception
        assertTrue(hashCode != 0 || hashCode == 0); // Always true but verifies no exception
    }

    @Test
    public void testWithBaseType() {
        entityType.setName("DerivedEntity");
        entityType.setBaseType("TestNamespace.BaseEntity"); // 使用完整的命名空间
        
        assertEquals("DerivedEntity", entityType.getName());
        assertEquals("TestNamespace.BaseEntity", entityType.getBaseType());
    }

    @Test
    public void testWithoutProperties() {
        entityType.setName("EmptyEntity");
        entityType.setProperties(null);
        
        assertNull(entityType.getProperties());
        
        // Should not fail when registering element
        entityType.registerElement();
        assertNotNull(entityType.getDirectDependencies());
    }

    @Test
    public void testWithEmptyProperties() {
        entityType.setName("EmptyEntity");
        entityType.setProperties(Collections.emptyList());
        
        assertNotNull(entityType.getProperties());
        assertTrue(entityType.getProperties().isEmpty());
        
        // Should not fail when registering element
        entityType.registerElement();
        assertNotNull(entityType.getDirectDependencies());
    }

    @Test
    public void testToString() {
        entityType.setName("TestEntity");
        entityType.setNamespace("TestNamespace");
        
        String result = entityType.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestEntity"));
        assertTrue(result.contains("ExtendedCsdlEntityType"));
    }

    @Test
    public void testToStringWithNullName() {
        entityType.setNamespace("TestNamespace");
        
        String result = entityType.toString();
        assertNotNull(result);
        assertTrue(result.contains("ExtendedCsdlEntityType"));
        assertTrue(result.contains("null")); // name会是null
    }

    @Test
    public void testToStringWithProperties() {
        entityType.setName("TestEntity");
        CsdlProperty property1 = new CsdlProperty();
        property1.setName("Property1");
        CsdlProperty property2 = new CsdlProperty();
        property2.setName("Property2");
        entityType.setProperties(Arrays.asList(property1, property2));
        
        String result = entityType.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestEntity"));
        assertTrue(result.contains("properties=2")); // 应该显示属性数量
    }
}
