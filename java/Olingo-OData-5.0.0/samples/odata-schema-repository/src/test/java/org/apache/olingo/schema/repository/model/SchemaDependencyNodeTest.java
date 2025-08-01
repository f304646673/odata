package org.apache.olingo.schema.repository.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

/**
 * SchemaDependencyNode的单元测试
 */
@DisplayName("SchemaDependencyNode Tests")
class SchemaDependencyNodeTest {
    
    private FullQualifiedName testFqn;
    private SchemaDependencyNode testNode;
    
    @BeforeEach
    void setUp() {
        testFqn = new FullQualifiedName("TestNamespace", "TestEntity");
        testNode = new SchemaDependencyNode(testFqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create node with full parameters")
        void shouldCreateNodeWithFullParameters() {
            String elementId = "test.element.id";
            FullQualifiedName fqn = new FullQualifiedName("Test.Namespace", "TestType");
            SchemaDependencyNode.DependencyType type = SchemaDependencyNode.DependencyType.COMPLEX_TYPE;
            String propertyName = "testProperty";
            
            SchemaDependencyNode node = new SchemaDependencyNode(elementId, fqn, type, propertyName);
            
            assertThat(node.getElementId()).isEqualTo(elementId);
            assertThat(node.getFullyQualifiedName()).isEqualTo(fqn);
            assertThat(node.getName()).isEqualTo("TestType");
            assertThat(node.getDependencyType()).isEqualTo(type);
            assertThat(node.getPropertyName()).isEqualTo(propertyName);
        }
        
        @Test
        @DisplayName("Should create node with simplified constructor")
        void shouldCreateNodeWithSimplifiedConstructor() {
            FullQualifiedName fqn = new FullQualifiedName("Test.Namespace", "TestType");
            SchemaDependencyNode.DependencyType type = SchemaDependencyNode.DependencyType.ACTION;
            
            SchemaDependencyNode node = new SchemaDependencyNode(fqn, type);
            
            assertThat(node.getElementId()).isEqualTo(fqn.toString());
            assertThat(node.getFullyQualifiedName()).isEqualTo(fqn);
            assertThat(node.getName()).isEqualTo("TestType");
            assertThat(node.getDependencyType()).isEqualTo(type);
            assertThat(node.getPropertyName()).isNull();
        }
        
        @Test
        @DisplayName("Should handle null FullQualifiedName")
        void shouldHandleNullFullQualifiedName() {
            SchemaDependencyNode node = new SchemaDependencyNode(null, SchemaDependencyNode.DependencyType.FUNCTION);
            
            assertThat(node.getFullyQualifiedName()).isNull();
            assertThat(node.getElementId()).isNull();
        }
        
        @Test
        @DisplayName("Should set name from elementId when FQN is null")
        void shouldSetNameFromElementIdWhenFqnIsNull() {
            String elementId = "testElementId";
            SchemaDependencyNode node = new SchemaDependencyNode(elementId, null, 
                SchemaDependencyNode.DependencyType.TYPE_REFERENCE, null);
            
            assertThat(node.getName()).isEqualTo(elementId);
        }
    }
    
    @Nested
    @DisplayName("Dependency Management Tests")
    class DependencyManagementTests {
        
        private SchemaDependencyNode dependencyNode;
        
        @BeforeEach
        void setUp() {
            FullQualifiedName dependencyFqn = new FullQualifiedName("DepNamespace", "DepEntity");
            dependencyNode = new SchemaDependencyNode(dependencyFqn, SchemaDependencyNode.DependencyType.BASE_TYPE);
        }
        
        @Test
        @DisplayName("Should add dependency successfully")
        void shouldAddDependencySuccessfully() {
            testNode.addDependency(dependencyNode);
            
            assertThat(testNode.getDependencies()).contains(dependencyNode);
            assertThat(dependencyNode.getDependents()).contains(testNode);
        }
        
        @Test
        @DisplayName("Should not add null dependency")
        void shouldNotAddNullDependency() {
            testNode.addDependency(null);
            
            assertThat(testNode.getDependencies()).isEmpty();
        }
        
        @Test
        @DisplayName("Should not add self dependency")
        void shouldNotAddSelfDependency() {
            testNode.addDependency(testNode);
            
            assertThat(testNode.getDependencies()).isEmpty();
        }
        
        @Test
        @DisplayName("Should remove dependency successfully")
        void shouldRemoveDependencySuccessfully() {
            testNode.addDependency(dependencyNode);
            
            boolean removed = testNode.removeDependency(dependencyNode);
            
            assertTrue(removed);
            assertThat(testNode.getDependencies()).doesNotContain(dependencyNode);
            assertThat(dependencyNode.getDependents()).doesNotContain(testNode);
        }
        
        @Test
        @DisplayName("Should return false when removing non-existent dependency")
        void shouldReturnFalseWhenRemovingNonExistentDependency() {
            boolean removed = testNode.removeDependency(dependencyNode);
            
            assertFalse(removed);
        }
        
        @Test
        @DisplayName("Should handle removing null dependency")
        void shouldHandleRemovingNullDependency() {
            boolean removed = testNode.removeDependency(null);
            
            assertFalse(removed);
        }
        
        @Test
        @DisplayName("Should get immutable dependencies collection")
        void shouldGetImmutableDependenciesCollection() {
            testNode.addDependency(dependencyNode);
            
            Set<SchemaDependencyNode> dependencies1 = testNode.getDependencies();
            Set<SchemaDependencyNode> dependencies2 = testNode.getDependencies();
            
            // 验证每次调用都返回新的集合实例（不可变性）
            assertThat(dependencies1).isNotSameAs(dependencies2);
            assertThat(dependencies1).contains(dependencyNode);
        }
        
        @Test
        @DisplayName("Should get immutable dependents collection")
        void shouldGetImmutableDependentsCollection() {
            testNode.addDependency(dependencyNode);
            
            Set<SchemaDependencyNode> dependents1 = dependencyNode.getDependents();
            Set<SchemaDependencyNode> dependents2 = dependencyNode.getDependents();
            
            // 验证每次调用都返回新的集合实例（不可变性）
            assertThat(dependents1).isNotSameAs(dependents2);
            assertThat(dependents1).contains(testNode);
        }
    }
    
    @Nested
    @DisplayName("Circular Dependency Detection Tests")
    class CircularDependencyDetectionTests {
        
        @Test
        @DisplayName("Should detect simple circular dependency")
        void shouldDetectSimpleCircularDependency() {
            FullQualifiedName node2Fqn = new FullQualifiedName("Test2", "Entity2");
            SchemaDependencyNode node2 = new SchemaDependencyNode(node2Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            // Create circular dependency: node1 -> node2 -> node1
            testNode.addDependency(node2);
            node2.addDependency(testNode);
            
            assertTrue(testNode.hasCircularDependency());
            assertTrue(node2.hasCircularDependency());
        }
        
        @Test
        @DisplayName("Should detect complex circular dependency")
        void shouldDetectComplexCircularDependency() {
            FullQualifiedName node2Fqn = new FullQualifiedName("Test2", "Entity2");
            FullQualifiedName node3Fqn = new FullQualifiedName("Test3", "Entity3");
            
            SchemaDependencyNode node2 = new SchemaDependencyNode(node2Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            SchemaDependencyNode node3 = new SchemaDependencyNode(node3Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            // Create circular dependency: node1 -> node2 -> node3 -> node1
            testNode.addDependency(node2);
            node2.addDependency(node3);
            node3.addDependency(testNode);
            
            assertTrue(testNode.hasCircularDependency());
            assertTrue(node2.hasCircularDependency());
            assertTrue(node3.hasCircularDependency());
        }
        
        @Test
        @DisplayName("Should not detect circular dependency in valid hierarchy")
        void shouldNotDetectCircularDependencyInValidHierarchy() {
            FullQualifiedName node2Fqn = new FullQualifiedName("Test2", "Entity2");
            FullQualifiedName node3Fqn = new FullQualifiedName("Test3", "Entity3");
            
            SchemaDependencyNode node2 = new SchemaDependencyNode(node2Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            SchemaDependencyNode node3 = new SchemaDependencyNode(node3Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            // Create valid hierarchy: node1 -> node2 -> node3
            testNode.addDependency(node2);
            node2.addDependency(node3);
            
            assertFalse(testNode.hasCircularDependency());
            assertFalse(node2.hasCircularDependency());
            assertFalse(node3.hasCircularDependency());
        }
        
        @Test
        @DisplayName("Should handle empty dependencies")
        void shouldHandleEmptyDependencies() {
            assertFalse(testNode.hasCircularDependency());
        }
        
        @Test
        @DisplayName("Should use provided visited nodes set")
        void shouldUseProvidedVisitedNodesSet() {
            Set<SchemaDependencyNode> visitedNodes = new HashSet<>();
            
            assertFalse(testNode.hasCircularDependency(visitedNodes));
            assertThat(visitedNodes).contains(testNode);
        }
    }
    
    @Nested
    @DisplayName("Dependency Depth Tests")
    class DependencyDepthTests {
        
        @Test
        @DisplayName("Should calculate depth for single node")
        void shouldCalculateDepthForSingleNode() {
            assertEquals(1, testNode.getDependencyDepth());
        }
        
        @Test
        @DisplayName("Should calculate depth for linear dependency chain")
        void shouldCalculateDepthForLinearDependencyChain() {
            FullQualifiedName node2Fqn = new FullQualifiedName("Test2", "Entity2");
            FullQualifiedName node3Fqn = new FullQualifiedName("Test3", "Entity3");
            
            SchemaDependencyNode node2 = new SchemaDependencyNode(node2Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            SchemaDependencyNode node3 = new SchemaDependencyNode(node3Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            // Create chain: node1 -> node2 -> node3
            testNode.addDependency(node2);
            node2.addDependency(node3);
            
            assertEquals(3, testNode.getDependencyDepth());
            assertEquals(2, node2.getDependencyDepth());
            assertEquals(1, node3.getDependencyDepth());
        }
        
        @Test
        @DisplayName("Should calculate depth for branching dependencies")
        void shouldCalculateDepthForBranchingDependencies() {
            FullQualifiedName node2Fqn = new FullQualifiedName("Test2", "Entity2");
            FullQualifiedName node3Fqn = new FullQualifiedName("Test3", "Entity3");
            FullQualifiedName node4Fqn = new FullQualifiedName("Test4", "Entity4");
            
            SchemaDependencyNode node2 = new SchemaDependencyNode(node2Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            SchemaDependencyNode node3 = new SchemaDependencyNode(node3Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            SchemaDependencyNode node4 = new SchemaDependencyNode(node4Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            // Create branching: node1 -> node2, node1 -> node3 -> node4
            testNode.addDependency(node2);
            testNode.addDependency(node3);
            node3.addDependency(node4);
            
            assertEquals(3, testNode.getDependencyDepth()); // Max depth through node3->node4
        }
        
        @Test
        @DisplayName("Should handle circular dependency in depth calculation")
        void shouldHandleCircularDependencyInDepthCalculation() {
            FullQualifiedName node2Fqn = new FullQualifiedName("Test2", "Entity2");
            SchemaDependencyNode node2 = new SchemaDependencyNode(node2Fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            // Create circular dependency
            testNode.addDependency(node2);
            node2.addDependency(testNode);
            
            // Should not cause infinite recursion
            int depth = testNode.getDependencyDepth();
            assertTrue(depth >= 0); // Should return some valid depth
        }
    }
    
    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {
        
        @Test
        @DisplayName("Should get and set elementId")
        void shouldGetAndSetElementId() {
            String newElementId = "newElementId";
            testNode.setElementId(newElementId);
            
            assertEquals(newElementId, testNode.getElementId());
        }
        
        @Test
        @DisplayName("Should get and set name")
        void shouldGetAndSetName() {
            String newName = "NewName";
            testNode.setName(newName);
            
            assertEquals(newName, testNode.getName());
        }
        
        @Test
        @DisplayName("Should get and set fullyQualifiedName")
        void shouldGetAndSetFullyQualifiedName() {
            FullQualifiedName newFqn = new FullQualifiedName("NewNamespace", "NewEntity");
            testNode.setFullyQualifiedName(newFqn);
            
            assertEquals(newFqn, testNode.getFullyQualifiedName());
            assertEquals("NewEntity", testNode.getName()); // Name should be updated
        }
        
        @Test
        @DisplayName("Should handle null fullyQualifiedName")
        void shouldHandleNullFullyQualifiedName() {
            String originalName = testNode.getName();
            testNode.setFullyQualifiedName(null);
            
            assertNull(testNode.getFullyQualifiedName());
            assertEquals(originalName, testNode.getName()); // Name should remain unchanged
        }
        
        @Test
        @DisplayName("Should get and set dependencyType")
        void shouldGetAndSetDependencyType() {
            SchemaDependencyNode.DependencyType newType = SchemaDependencyNode.DependencyType.COMPLEX_TYPE;
            testNode.setDependencyType(newType);
            
            assertEquals(newType, testNode.getDependencyType());
        }
        
        @Test
        @DisplayName("Should get and set propertyName")
        void shouldGetAndSetPropertyName() {
            String propertyName = "testProperty";
            testNode.setPropertyName(propertyName);
            
            assertEquals(propertyName, testNode.getPropertyName());
        }
        
        @Test
        @DisplayName("Should get and set annotations")
        void shouldGetAndSetAnnotations() {
            List<CsdlAnnotation> annotations = new ArrayList<>();
            CsdlAnnotation annotation = new CsdlAnnotation();
            annotation.setTerm("TestTerm");
            annotations.add(annotation);
            
            testNode.setAnnotations(annotations);
            
            assertEquals(annotations, testNode.getAnnotations());
        }
        
        @Test
        @DisplayName("Should handle null annotations")
        void shouldHandleNullAnnotations() {
            testNode.setAnnotations(null);
            
            assertNotNull(testNode.getAnnotations());
            assertTrue(testNode.getAnnotations().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {
        
        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            assertEquals(testNode, testNode);
            assertEquals(testNode.hashCode(), testNode.hashCode());
        }
        
        @Test
        @DisplayName("Should be equal to equivalent node")
        void shouldBeEqualToEquivalentNode() {
            SchemaDependencyNode equivalentNode = new SchemaDependencyNode(testFqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            assertEquals(testNode, equivalentNode);
            assertEquals(testNode.hashCode(), equivalentNode.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal to node with different FQN")
        void shouldNotBeEqualToNodeWithDifferentFqn() {
            FullQualifiedName differentFqn = new FullQualifiedName("DifferentNamespace", "DifferentEntity");
            SchemaDependencyNode differentNode = new SchemaDependencyNode(differentFqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            assertNotEquals(testNode, differentNode);
        }
        
        @Test
        @DisplayName("Should not be equal to node with different dependency type")
        void shouldNotBeEqualToNodeWithDifferentDependencyType() {
            SchemaDependencyNode differentNode = new SchemaDependencyNode(testFqn, SchemaDependencyNode.DependencyType.COMPLEX_TYPE);
            
            assertNotEquals(testNode, differentNode);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(testNode, null);
        }
        
        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            assertNotEquals(testNode, "string");
        }
        
        @Test
        @DisplayName("Should handle null fields in equals")
        void shouldHandleNullFieldsInEquals() {
            SchemaDependencyNode node1 = new SchemaDependencyNode(null, null, SchemaDependencyNode.DependencyType.ENTITY_TYPE, null);
            SchemaDependencyNode node2 = new SchemaDependencyNode(null, null, SchemaDependencyNode.DependencyType.ENTITY_TYPE, null);
            
            assertEquals(node1, node2);
        }
    }
    
    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("Should provide meaningful toString")
        void shouldProvideMeaningfulToString() {
            testNode.setPropertyName("testProperty");
            
            String result = testNode.toString();
            
            assertThat(result).contains("SchemaDependencyNode");
            assertThat(result).contains(testNode.getElementId());
            assertThat(result).contains(testNode.getFullyQualifiedName().toString());
            assertThat(result).contains(testNode.getDependencyType().toString());
            assertThat(result).contains("testProperty");
        }
        
        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            SchemaDependencyNode nodeWithNulls = new SchemaDependencyNode(null, null, SchemaDependencyNode.DependencyType.ENTITY_TYPE, null);
            
            String result = nodeWithNulls.toString();
            
            assertThat(result).contains("SchemaDependencyNode");
            assertThat(result).contains("null");
        }
    }
    
    @Nested
    @DisplayName("DependencyType Enum Tests")
    class DependencyTypeEnumTests {
        
        @Test
        @DisplayName("Should have all expected dependency types")
        void shouldHaveAllExpectedDependencyTypes() {
            SchemaDependencyNode.DependencyType[] types = SchemaDependencyNode.DependencyType.values();
            
            assertThat(types).contains(
                SchemaDependencyNode.DependencyType.TYPE_REFERENCE,
                SchemaDependencyNode.DependencyType.BASE_TYPE,
                SchemaDependencyNode.DependencyType.ENTITY_SET,
                SchemaDependencyNode.DependencyType.ENTITY_TYPE,
                SchemaDependencyNode.DependencyType.COMPLEX_TYPE,
                SchemaDependencyNode.DependencyType.ACTION,
                SchemaDependencyNode.DependencyType.ACTION_IMPORT,
                SchemaDependencyNode.DependencyType.ACTION_REFERENCE,
                SchemaDependencyNode.DependencyType.FUNCTION,
                SchemaDependencyNode.DependencyType.FUNCTION_IMPORT,
                SchemaDependencyNode.DependencyType.FUNCTION_REFERENCE,
                SchemaDependencyNode.DependencyType.NAVIGATION_PROPERTY,
                SchemaDependencyNode.DependencyType.NAVIGATION_TARGET,
                SchemaDependencyNode.DependencyType.PARAMETER,
                SchemaDependencyNode.DependencyType.PARAMETER_TYPE,
                SchemaDependencyNode.DependencyType.PROPERTY,
                SchemaDependencyNode.DependencyType.RETURN_TYPE,
                SchemaDependencyNode.DependencyType.SINGLETON,
                SchemaDependencyNode.DependencyType.TYPE_DEFINITION
            );
        }
        
        @Test
        @DisplayName("Should create node with each dependency type")
        void shouldCreateNodeWithEachDependencyType() {
            for (SchemaDependencyNode.DependencyType type : SchemaDependencyNode.DependencyType.values()) {
                SchemaDependencyNode node = new SchemaDependencyNode(testFqn, type);
                assertEquals(type, node.getDependencyType());
            }
        }
    }
}
