package org.apache.olingo.schema.repository.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SchemaRepositoryContext的单元测试
 */
@DisplayName("SchemaRepositoryContext Tests")
class SchemaRepositoryContextTest {
    
    private SchemaRepositoryContext context;
    private CsdlSchema testSchema;
    
    @BeforeEach
    void setUp() {
        context = new SchemaRepositoryContext();
        testSchema = createTestSchema();
    }
    
    private CsdlSchema createTestSchema() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestNamespace");
        schema.setAlias("TN");
        
        // Add EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TestEntity");
        schema.setEntityTypes(List.of(entityType));
        
        // Add ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("TestComplex");
        schema.setComplexTypes(List.of(complexType));
        
        // Add Action
        CsdlAction action = new CsdlAction();
        action.setName("TestAction");
        schema.setActions(List.of(action));
        
        // Add Function
        CsdlFunction function = new CsdlFunction();
        function.setName("TestFunction");
        schema.setFunctions(List.of(function));
        
        // Add TypeDefinition
        CsdlTypeDefinition typeDef = new CsdlTypeDefinition();
        typeDef.setName("TestTypeDef");
        schema.setTypeDefinitions(List.of(typeDef));
        
        // Add Term
        CsdlTerm term = new CsdlTerm();
        term.setName("TestTerm");
        schema.setTerms(List.of(term));
        
        // Add EntityContainer
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName("TestContainer");
        schema.setEntityContainer(container);
        
        return schema;
    }
    
    @Nested
    @DisplayName("Schema Management Tests")
    class SchemaManagementTests {
        
        @Test
        @DisplayName("Should add schema successfully")
        void shouldAddSchemaSuccessfully() {
            context.addSchema(testSchema);
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertEquals(testSchema, context.getSchema("TestNamespace"));
            assertThat(context.getAllNamespaces()).contains("TestNamespace");
        }
        
        @Test
        @DisplayName("Should throw exception for null schema")
        void shouldThrowExceptionForNullSchema() {
            assertThrows(IllegalArgumentException.class, () -> context.addSchema(null));
        }
        
        @Test
        @DisplayName("Should throw exception for schema with null namespace")
        void shouldThrowExceptionForSchemaWithNullNamespace() {
            CsdlSchema invalidSchema = new CsdlSchema();
            invalidSchema.setNamespace(null);
            
            assertThrows(IllegalArgumentException.class, () -> context.addSchema(invalidSchema));
        }
        
        @Test
        @DisplayName("Should remove schema successfully")
        void shouldRemoveSchemaSuccessfully() {
            context.addSchema(testSchema);
            
            boolean removed = context.removeSchema("TestNamespace");
            
            assertTrue(removed);
            assertFalse(context.containsSchema("TestNamespace"));
            assertNull(context.getSchema("TestNamespace"));
        }
        
        @Test
        @DisplayName("Should return false when removing non-existent schema")
        void shouldReturnFalseWhenRemovingNonExistentSchema() {
            boolean removed = context.removeSchema("NonExistentNamespace");
            
            assertFalse(removed);
        }
        
        @Test
        @DisplayName("Should replace existing schema")
        void shouldReplaceExistingSchema() {
            context.addSchema(testSchema);
            
            CsdlSchema newSchema = new CsdlSchema();
            newSchema.setNamespace("TestNamespace");
            newSchema.setAlias("TN2");
            
            context.addSchema(newSchema);
            
            assertEquals(newSchema, context.getSchema("TestNamespace"));
            assertEquals("TestNamespace", context.resolveNamespace("TN2"));
        }
        
        @Test
        @DisplayName("Should get all schemas")
        void shouldGetAllSchemas() {
            context.addSchema(testSchema);
            
            CsdlSchema anotherSchema = new CsdlSchema();
            anotherSchema.setNamespace("AnotherNamespace");
            context.addSchema(anotherSchema);
            
            Map<String, CsdlSchema> allSchemas = context.getAllSchemas();
            
            assertEquals(2, allSchemas.size());
            assertThat(allSchemas).containsKeys("TestNamespace", "AnotherNamespace");
        }
    }
    
    @Nested
    @DisplayName("Alias Management Tests")
    class AliasManagementTests {
        
        @Test
        @DisplayName("Should register aliases correctly")
        void shouldRegisterAliasesCorrectly() {
            context.addSchema(testSchema);
            
            assertEquals("TestNamespace", context.resolveNamespace("TN"));
            assertEquals("TestNamespace", context.resolveNamespace("TestNamespace"));
            
            Map<String, String> aliasToNamespace = context.getAliasToNamespaceMap();
            Map<String, String> namespaceToAlias = context.getNamespaceToAliasMap();
            
            assertEquals("TestNamespace", aliasToNamespace.get("TN"));
            assertEquals("TN", namespaceToAlias.get("TestNamespace"));
        }
        
        @Test
        @DisplayName("Should handle schema without alias")
        void shouldHandleSchemaWithoutAlias() {
            CsdlSchema schemaWithoutAlias = new CsdlSchema();
            schemaWithoutAlias.setNamespace("NoAliasNamespace");
            
            context.addSchema(schemaWithoutAlias);
            
            assertEquals("NoAliasNamespace", context.resolveNamespace("NoAliasNamespace"));
            assertNull(context.resolveNamespace("SomeAlias"));
        }
        
        @Test
        @DisplayName("Should return null for unknown alias or namespace")
        void shouldReturnNullForUnknownAliasOrNamespace() {
            assertNull(context.resolveNamespace("UnknownAlias"));
            assertNull(context.resolveNamespace("Unknown.Namespace"));
        }
        
        @Test
        @DisplayName("Should clean up aliases when removing schema")
        void shouldCleanUpAliasesWhenRemovingSchema() {
            context.addSchema(testSchema);
            context.removeSchema("TestNamespace");
            
            assertNull(context.resolveNamespace("TN"));
            assertThat(context.getAliasToNamespaceMap()).doesNotContainKey("TN");
            assertThat(context.getNamespaceToAliasMap()).doesNotContainKey("TestNamespace");
        }
    }
    
    @Nested
    @DisplayName("Element Query Tests")
    class ElementQueryTests {
        
        @BeforeEach
        void setUp() {
            context.addSchema(testSchema);
        }
        
        @Test
        @DisplayName("Should get EntityType by FQN")
        void shouldGetEntityTypeByFqn() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestEntity");
            
            CsdlEntityType entityType = context.getEntityType(fqn);
            
            assertNotNull(entityType);
            assertEquals("TestEntity", entityType.getName());
        }
        
        @Test
        @DisplayName("Should return null for non-existent EntityType")
        void shouldReturnNullForNonExistentEntityType() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "NonExistentEntity");
            
            CsdlEntityType entityType = context.getEntityType(fqn);
            
            assertNull(entityType);
        }
        
        @Test
        @DisplayName("Should get ComplexType by FQN")
        void shouldGetComplexTypeByFqn() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestComplex");
            
            CsdlComplexType complexType = context.getComplexType(fqn);
            
            assertNotNull(complexType);
            assertEquals("TestComplex", complexType.getName());
        }
        
        @Test
        @DisplayName("Should get Action by FQN")
        void shouldGetActionByFqn() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestAction");
            
            CsdlAction action = context.getAction(fqn);
            
            assertNotNull(action);
            assertEquals("TestAction", action.getName());
        }
        
        @Test
        @DisplayName("Should get Function by FQN")
        void shouldGetFunctionByFqn() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestFunction");
            
            CsdlFunction function = context.getFunction(fqn);
            
            assertNotNull(function);
            assertEquals("TestFunction", function.getName());
        }
        
        @Test
        @DisplayName("Should get TypeDefinition by FQN")
        void shouldGetTypeDefinitionByFqn() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestTypeDef");
            
            CsdlTypeDefinition typeDef = context.getTypeDefinition(fqn);
            
            assertNotNull(typeDef);
            assertEquals("TestTypeDef", typeDef.getName());
        }
        
        @Test
        @DisplayName("Should get Term by FQN")
        void shouldGetTermByFqn() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestTerm");
            
            CsdlTerm term = context.getTerm(fqn);
            
            assertNotNull(term);
            assertEquals("TestTerm", term.getName());
        }
        
        @Test
        @DisplayName("Should get EntityContainer by namespace")
        void shouldGetEntityContainerByNamespace() {
            CsdlEntityContainer container = context.getEntityContainer("TestNamespace");
            
            assertNotNull(container);
            assertEquals("TestContainer", container.getName());
        }
        
        @Test
        @DisplayName("Should handle null FQN gracefully")
        void shouldHandleNullFqnGracefully() {
            assertNull(context.getEntityType(null));
            assertNull(context.getComplexType(null));
            assertNull(context.getAction(null));
            assertNull(context.getFunction(null));
            assertNull(context.getTypeDefinition(null));
            assertNull(context.getTerm(null));
        }
        
        @Test
        @DisplayName("Should handle wrong namespace gracefully")
        void shouldHandleWrongNamespaceGracefully() {
            FullQualifiedName fqn = new FullQualifiedName("WrongNamespace", "TestEntity");
            
            assertNull(context.getEntityType(fqn));
        }
    }
    
    @Nested
    @DisplayName("Dependency Node Management Tests")
    class DependencyNodeManagementTests {
        
        @Test
        @DisplayName("Should add and get dependency node")
        void shouldAddAndGetDependencyNode() {
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestEntity");
            SchemaDependencyNode node = new SchemaDependencyNode(fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            context.addDependencyNode(node);
            
            SchemaDependencyNode retrievedNode = context.getDependencyNode(fqn);
            assertEquals(node, retrievedNode);
        }
        
        @Test
        @DisplayName("Should get all dependency nodes")
        void shouldGetAllDependencyNodes() {
            FullQualifiedName fqn1 = new FullQualifiedName("TestNamespace", "Entity1");
            FullQualifiedName fqn2 = new FullQualifiedName("TestNamespace", "Entity2");
            
            SchemaDependencyNode node1 = new SchemaDependencyNode(fqn1, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            SchemaDependencyNode node2 = new SchemaDependencyNode(fqn2, SchemaDependencyNode.DependencyType.COMPLEX_TYPE);
            
            context.addDependencyNode(node1);
            context.addDependencyNode(node2);
            
            Map<FullQualifiedName, SchemaDependencyNode> allNodes = context.getAllDependencyNodes();
            
            assertEquals(2, allNodes.size());
            assertThat(allNodes).containsKeys(fqn1, fqn2);
        }
        
        @Test
        @DisplayName("Should handle null dependency node")
        void shouldHandleNullDependencyNode() {
            context.addDependencyNode(null);
            
            // Should not throw exception and should not add anything
            assertTrue(context.getAllDependencyNodes().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle dependency node with null FQN")
        void shouldHandleDependencyNodeWithNullFqn() {
            SchemaDependencyNode nodeWithNullFqn = new SchemaDependencyNode(null, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            
            context.addDependencyNode(nodeWithNullFqn);
            
            // Should not throw exception and should not add anything
            assertTrue(context.getAllDependencyNodes().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Index Management Tests")
    class IndexManagementTests {
        
        @Test
        @DisplayName("Should build indexes when adding schema")
        void shouldBuildIndexesWhenAddingSchema() {
            long initialUpdateTime = context.getStatistics().getLastUpdateTime();
            
            // Add slight delay to ensure timestamp difference
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            context.addSchema(testSchema);
            
            SchemaRepositoryContext.RepositoryStatistics stats = context.getStatistics();
            assertTrue(stats.getLastUpdateTime() > initialUpdateTime);
            assertEquals(1, stats.getSchemaCount());
            assertTrue(stats.getTotalElements() > 0);
        }
        
        @Test
        @DisplayName("Should remove indexes when removing schema")
        void shouldRemoveIndexesWhenRemovingSchema() {
            context.addSchema(testSchema);
            
            FullQualifiedName entityFqn = new FullQualifiedName("TestNamespace", "TestEntity");
            assertNotNull(context.getEntityType(entityFqn));
            
            context.removeSchema("TestNamespace");
            
            assertNull(context.getEntityType(entityFqn));
        }
        
        @Test
        @DisplayName("Should handle schema with null collections")
        void shouldHandleSchemaWithNullCollections() {
            CsdlSchema minimalSchema = new CsdlSchema();
            minimalSchema.setNamespace("MinimalNamespace");
            // All collections are null by default
            
            // Should not throw exception
            context.addSchema(minimalSchema);
            
            assertTrue(context.containsSchema("MinimalNamespace"));
        }
        
        @Test
        @DisplayName("Should handle schema with empty collections")
        void shouldHandleSchemaWithEmptyCollections() {
            CsdlSchema emptySchema = new CsdlSchema();
            emptySchema.setNamespace("EmptyNamespace");
            emptySchema.setEntityTypes(new ArrayList<>());
            emptySchema.setComplexTypes(new ArrayList<>());
            emptySchema.setActions(new ArrayList<>());
            emptySchema.setFunctions(new ArrayList<>());
            emptySchema.setTypeDefinitions(new ArrayList<>());
            emptySchema.setTerms(new ArrayList<>());
            
            // Should not throw exception
            context.addSchema(emptySchema);
            
            assertTrue(context.containsSchema("EmptyNamespace"));
        }
    }
    
    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {
        
        @Test
        @DisplayName("Should provide accurate statistics")
        void shouldProvideAccurateStatistics() {
            long beforeTime = System.currentTimeMillis();
            
            context.addSchema(testSchema);
            
            long afterTime = System.currentTimeMillis();
            
            SchemaRepositoryContext.RepositoryStatistics stats = context.getStatistics();
            
            assertEquals(1, stats.getSchemaCount());
            assertTrue(stats.getTotalElements() >= 7); // At least 7 elements in test schema
            assertTrue(stats.getLastUpdateTime() >= beforeTime);
            assertTrue(stats.getLastUpdateTime() <= afterTime);
        }
        
        @Test
        @DisplayName("Should update statistics when adding multiple schemas")
        void shouldUpdateStatisticsWhenAddingMultipleSchemas() {
            context.addSchema(testSchema);
            
            CsdlSchema anotherSchema = new CsdlSchema();
            anotherSchema.setNamespace("AnotherNamespace");
            CsdlEntityType anotherEntity = new CsdlEntityType();
            anotherEntity.setName("AnotherEntity");
            anotherSchema.setEntityTypes(List.of(anotherEntity));
            
            context.addSchema(anotherSchema);
            
            SchemaRepositoryContext.RepositoryStatistics stats = context.getStatistics();
            
            assertEquals(2, stats.getSchemaCount());
            assertTrue(stats.getTotalElements() >= 8); // Original 7 + 1 new
        }
        
        @Test
        @DisplayName("Should provide statistics for empty context")
        void shouldProvideStatisticsForEmptyContext() {
            SchemaRepositoryContext.RepositoryStatistics stats = context.getStatistics();
            
            assertEquals(0, stats.getSchemaCount());
            assertEquals(0, stats.getTotalElements());
            assertEquals(0, stats.getDependencyNodeCount());
            assertTrue(stats.getLastUpdateTime() > 0);
        }
    }
    
    @Nested
    @DisplayName("Clear and Cleanup Tests")
    class ClearAndCleanupTests {
        
        @Test
        @DisplayName("Should clear all data")
        void shouldClearAllData() {
            context.addSchema(testSchema);
            
            FullQualifiedName fqn = new FullQualifiedName("TestNamespace", "TestEntity");
            SchemaDependencyNode node = new SchemaDependencyNode(fqn, SchemaDependencyNode.DependencyType.ENTITY_TYPE);
            context.addDependencyNode(node);
            
            context.clear();
            
            assertTrue(context.getAllSchemas().isEmpty());
            assertTrue(context.getAllDependencyNodes().isEmpty());
            assertTrue(context.getAllNamespaces().isEmpty());
            assertTrue(context.getAliasToNamespaceMap().isEmpty());
            assertTrue(context.getNamespaceToAliasMap().isEmpty());
            
            SchemaRepositoryContext.RepositoryStatistics stats = context.getStatistics();
            assertEquals(0, stats.getSchemaCount());
            assertEquals(0, stats.getTotalElements());
            assertEquals(0, stats.getDependencyNodeCount());
        }
        
        @Test
        @DisplayName("Should be reusable after clear")
        void shouldBeReusableAfterClear() {
            context.addSchema(testSchema);
            context.clear();
            
            // Should be able to add schema again
            context.addSchema(testSchema);
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertEquals(1, context.getStatistics().getSchemaCount());
        }
    }
    
    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {
        
        @Test
        @DisplayName("Should handle concurrent schema additions")
        void shouldHandleConcurrentSchemaAdditions() throws InterruptedException {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    CsdlSchema schema = new CsdlSchema();
                    schema.setNamespace("Namespace" + index);
                    context.addSchema(schema);
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            assertEquals(threadCount, context.getAllSchemas().size());
        }
        
        @Test
        @DisplayName("Should handle concurrent read and write operations")
        void shouldHandleConcurrentReadAndWriteOperations() throws InterruptedException {
            context.addSchema(testSchema);
            
            Thread writerThread = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    CsdlSchema schema = new CsdlSchema();
                    schema.setNamespace("TempNamespace" + i);
                    context.addSchema(schema);
                    context.removeSchema("TempNamespace" + i);
                }
            });
            
            Thread readerThread = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    context.getSchema("TestNamespace");
                    context.getAllSchemas();
                    context.getStatistics();
                }
            });
            
            writerThread.start();
            readerThread.start();
            
            writerThread.join();
            readerThread.join();
            
            // Should still contain the original test schema
            assertTrue(context.containsSchema("TestNamespace"));
        }
    }
}
