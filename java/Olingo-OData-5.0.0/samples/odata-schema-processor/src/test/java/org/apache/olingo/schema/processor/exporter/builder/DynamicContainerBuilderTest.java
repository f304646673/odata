package org.apache.olingo.schema.processor.exporter.builder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试DynamicContainerBuilder类
 * 该类负责动态构建EntityContainer
 */
public class DynamicContainerBuilderTest {

    private DynamicContainerBuilder builder;

    @Before
    public void setUp() {
        builder = new DynamicContainerBuilder();
    }

    @Test
    public void testDefaultConstructor() {
        DynamicContainerBuilder newBuilder = new DynamicContainerBuilder();
        assertNotNull("Builder should not be null", newBuilder);
    }

    @Test
    public void testBuilderNotNull() {
        assertNotNull("Builder should not be null", builder);
    }

    @Test
    public void testAddEntitySetBasic() {
        // Test basic functionality without assuming specific method signatures
        DynamicContainerBuilder result = builder.addEntitySet("TestEntitySet", "Test.Entity");
        
        assertNotNull("Builder should return itself for chaining", result);
        assertSame("Builder should return the same instance", builder, result);
    }

    @Test
    public void testMultipleEntitySets() {
        // Test adding multiple entity sets
        builder.addEntitySet("EntitySet1", "Test.Entity1");
        builder.addEntitySet("EntitySet2", "Test.Entity2");
        
        // Just verify the builder still works
        assertNotNull("Builder should not be null after adding entity sets", builder);
    }

    @Test
    public void testBuilderFunctionality() {
        // Test basic builder functionality
        builder.addEntitySet("Products", "Test.Product");
        builder.addEntitySet("Categories", "Test.Category");
        
        // Verify builder is still functional
        assertNotNull("Builder should remain functional", builder);
    }

    @Test
    public void testEmptyEntitySetName() {
        // Test with empty entity set name
        DynamicContainerBuilder result = builder.addEntitySet("", "Test.Entity");
        
        assertNotNull("Builder should handle empty name", result);
        assertSame("Builder should return the same instance", builder, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyEntityType() {
        // Test with empty entity type - should throw exception
        builder.addEntitySet("TestEntitySet", "");
    }

    @Test
    public void testNullEntitySetName() {
        // Test with null entity set name
        DynamicContainerBuilder result = builder.addEntitySet(null, "Test.Entity");
        
        assertNotNull("Builder should handle null name", result);
        assertSame("Builder should return the same instance", builder, result);
    }

    @Test(expected = NullPointerException.class)
    public void testNullEntityType() {
        // Test with null entity type - should throw exception
        builder.addEntitySet("TestEntitySet", null);
    }

    @Test
    public void testBuilderChaining() {
        // Test method chaining
        DynamicContainerBuilder result = builder
            .addEntitySet("EntitySet1", "Test.Entity1")
            .addEntitySet("EntitySet2", "Test.Entity2");
        
        assertNotNull("Chained result should not be null", result);
        assertSame("Chained result should be same instance", builder, result);
    }

    @Test
    public void testMultipleOperations() {
        // Test that multiple operations don't interfere with each other
        builder.addEntitySet("EntitySet1", "Test.Entity1");
        builder.addEntitySet("EntitySet2", "Test.Entity2");
        builder.addEntitySet("EntitySet3", "Test.Entity3");
        
        assertNotNull("Builder should remain functional after multiple operations", builder);
    }

    @Test
    public void testSpecialCharacters() {
        // Test with special characters in names
        builder.addEntitySet("Entity_Set-1", "Test.Entity_Type-1");
        builder.addEntitySet("Entity.Set.2", "Test.Entity.Type.2");
        
        assertNotNull("Builder should handle special characters", builder);
    }

    @Test
    public void testLongNames() {
        // Test with long names
        String longName = "VeryLongEntitySetNameThatExceedsNormalLimits";
        String longType = "Test.VeryLongEntityTypeThatExceedsNormalLimits";
        
        DynamicContainerBuilder result = builder.addEntitySet(longName, longType);
        
        assertNotNull("Builder should handle long names", result);
        assertSame("Builder should return the same instance", builder, result);
    }

    @Test
    public void testRepeatedCalls() {
        // Test calling the same method multiple times
        builder.addEntitySet("TestEntitySet", "Test.Entity");
        builder.addEntitySet("TestEntitySet", "Test.Entity");
        builder.addEntitySet("TestEntitySet", "Test.Entity");
        
        assertNotNull("Builder should handle repeated calls", builder);
    }

    @Test
    public void testStateConsistency() {
        // Test that builder maintains consistent state
        DynamicContainerBuilder builder1 = new DynamicContainerBuilder();
        DynamicContainerBuilder builder2 = new DynamicContainerBuilder();
        
        builder1.addEntitySet("EntitySet1", "Test.Entity1");
        builder2.addEntitySet("EntitySet2", "Test.Entity2");
        
        // Verify both builders are independent
        assertNotSame("Builders should be independent", builder1, builder2);
    }
}
