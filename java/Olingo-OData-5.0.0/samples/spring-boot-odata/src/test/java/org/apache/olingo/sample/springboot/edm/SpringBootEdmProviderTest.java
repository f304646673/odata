package org.apache.olingo.sample.springboot.edm;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SpringBootEdmProvider
 * 
 * Tests the Entity Data Model (EDM) definitions including:
 * - Entity types and their properties
 * - Entity sets configuration
 * - Entity container setup
 * - Schema generation
 * - Namespace and naming conventions
 */
class SpringBootEdmProviderTest {

    private SpringBootEdmProvider edmProvider;

    @BeforeEach
    void setUp() {
        edmProvider = new SpringBootEdmProvider();
    }

    @Test
    @DisplayName("Should return correct namespace and container constants")
    void shouldReturnCorrectNamespaceAndContainerConstants() {
        assertEquals("org.apache.olingo.sample.springboot", SpringBootEdmProvider.NAMESPACE);
        assertEquals("SpringBootContainer", SpringBootEdmProvider.CONTAINER_NAME);
        assertEquals(new FullQualifiedName(SpringBootEdmProvider.NAMESPACE, SpringBootEdmProvider.CONTAINER_NAME), 
                     SpringBootEdmProvider.CONTAINER);
    }

    @Test
    @DisplayName("Should return correct Car entity type constants")
    void shouldReturnCorrectCarEntityTypeConstants() {
        assertEquals("Car", SpringBootEdmProvider.ET_CAR_NAME);
        assertEquals(new FullQualifiedName(SpringBootEdmProvider.NAMESPACE, SpringBootEdmProvider.ET_CAR_NAME), 
                     SpringBootEdmProvider.ET_CAR_FQN);
    }

    @Test
    @DisplayName("Should return correct Car entity set name")
    void shouldReturnCorrectCarEntitySetName() {
        assertEquals("Cars", SpringBootEdmProvider.ES_CARS_NAME);
    }

    @Test
    @DisplayName("Should return Car entity type for valid FQN")
    void shouldReturnCarEntityTypeForValidFQN() throws ODataException {
        CsdlEntityType entityType = edmProvider.getEntityType(SpringBootEdmProvider.ET_CAR_FQN);

        assertNotNull(entityType);
        assertEquals(SpringBootEdmProvider.ET_CAR_NAME, entityType.getName());
        
        // Verify properties
        List<CsdlProperty> properties = entityType.getProperties();
        assertNotNull(properties);
        assertEquals(6, properties.size());
        
        // Check each property
        assertPropertyExists(properties, "Id", EdmPrimitiveTypeKind.Int32.getFullQualifiedName().toString());
        assertPropertyExists(properties, "Brand", EdmPrimitiveTypeKind.String.getFullQualifiedName().toString());
        assertPropertyExists(properties, "Model", EdmPrimitiveTypeKind.String.getFullQualifiedName().toString());
        assertPropertyExists(properties, "Color", EdmPrimitiveTypeKind.String.getFullQualifiedName().toString());
        assertPropertyExists(properties, "Year", EdmPrimitiveTypeKind.Int32.getFullQualifiedName().toString());
        assertPropertyExists(properties, "Price", EdmPrimitiveTypeKind.Double.getFullQualifiedName().toString());
        
        // Verify key
        List<CsdlPropertyRef> key = entityType.getKey();
        assertNotNull(key);
        assertEquals(1, key.size());
        assertEquals("Id", key.get(0).getName());
    }

    @Test
    @DisplayName("Should return null for invalid entity type FQN")
    void shouldReturnNullForInvalidEntityTypeFQN() throws ODataException {
        FullQualifiedName invalidFQN = new FullQualifiedName("invalid.namespace", "InvalidType");
        CsdlEntityType entityType = edmProvider.getEntityType(invalidFQN);
        assertNull(entityType);
    }

    @Test
    @DisplayName("Should return Car entity set for valid container and name")
    void shouldReturnCarEntitySetForValidContainerAndName() throws ODataException {
        CsdlEntitySet entitySet = edmProvider.getEntitySet(SpringBootEdmProvider.CONTAINER, SpringBootEdmProvider.ES_CARS_NAME);

        assertNotNull(entitySet);
        assertEquals(SpringBootEdmProvider.ES_CARS_NAME, entitySet.getName());
        assertEquals(SpringBootEdmProvider.ET_CAR_FQN.getFullQualifiedNameAsString(), entitySet.getType());
    }

    @Test
    @DisplayName("Should return null for invalid container")
    void shouldReturnNullForInvalidContainer() throws ODataException {
        FullQualifiedName invalidContainer = new FullQualifiedName("invalid.namespace", "InvalidContainer");
        CsdlEntitySet entitySet = edmProvider.getEntitySet(invalidContainer, SpringBootEdmProvider.ES_CARS_NAME);
        assertNull(entitySet);
    }

    @Test
    @DisplayName("Should return null for invalid entity set name")
    void shouldReturnNullForInvalidEntitySetName() throws ODataException {
        CsdlEntitySet entitySet = edmProvider.getEntitySet(SpringBootEdmProvider.CONTAINER, "InvalidEntitySet");
        assertNull(entitySet);
    }

    @Test
    @DisplayName("Should return properly configured entity container")
    void shouldReturnProperlyConfiguredEntityContainer() throws ODataException {
        CsdlEntityContainer container = edmProvider.getEntityContainer();

        assertNotNull(container);
        assertEquals(SpringBootEdmProvider.CONTAINER_NAME, container.getName());
        
        // Verify entity sets
        List<CsdlEntitySet> entitySets = container.getEntitySets();
        assertNotNull(entitySets);
        assertEquals(1, entitySets.size());
        
        CsdlEntitySet carEntitySet = entitySets.get(0);
        assertEquals(SpringBootEdmProvider.ES_CARS_NAME, carEntitySet.getName());
        assertEquals(SpringBootEdmProvider.ET_CAR_FQN.getFullQualifiedNameAsString(), carEntitySet.getType());
    }

    @Test
    @DisplayName("Should return complete schema with all components")
    void shouldReturnCompleteSchemaWithAllComponents() throws ODataException {
        List<CsdlSchema> schemas = edmProvider.getSchemas();

        assertNotNull(schemas);
        assertEquals(1, schemas.size());
        
        CsdlSchema schema = schemas.get(0);
        assertEquals(SpringBootEdmProvider.NAMESPACE, schema.getNamespace());
        
        // Verify entity types
        List<CsdlEntityType> entityTypes = schema.getEntityTypes();
        assertNotNull(entityTypes);
        assertEquals(1, entityTypes.size());
        assertEquals(SpringBootEdmProvider.ET_CAR_NAME, entityTypes.get(0).getName());
        
        // Verify entity container
        CsdlEntityContainer container = schema.getEntityContainer();
        assertNotNull(container);
        assertEquals(SpringBootEdmProvider.CONTAINER_NAME, container.getName());
    }

    @Test
    @DisplayName("Should return entity container info for valid container name")
    void shouldReturnEntityContainerInfoForValidContainerName() throws ODataException {
        CsdlEntityContainerInfo containerInfo = edmProvider.getEntityContainerInfo(SpringBootEdmProvider.CONTAINER);

        assertNotNull(containerInfo);
        assertEquals(SpringBootEdmProvider.CONTAINER, containerInfo.getContainerName());
    }

    @Test
    @DisplayName("Should return entity container info for null container name")
    void shouldReturnEntityContainerInfoForNullContainerName() throws ODataException {
        CsdlEntityContainerInfo containerInfo = edmProvider.getEntityContainerInfo(null);

        assertNotNull(containerInfo);
        assertEquals(SpringBootEdmProvider.CONTAINER, containerInfo.getContainerName());
    }

    @Test
    @DisplayName("Should return null for invalid container name")
    void shouldReturnNullForInvalidContainerName() throws ODataException {
        FullQualifiedName invalidContainer = new FullQualifiedName("invalid.namespace", "InvalidContainer");
        CsdlEntityContainerInfo containerInfo = edmProvider.getEntityContainerInfo(invalidContainer);
        assertNull(containerInfo);
    }

    @Test
    @DisplayName("Should maintain consistency across all EDM components")
    void shouldMaintainConsistencyAcrossAllEDMComponents() throws ODataException {
        // Test that all components reference each other correctly
        
        // Get entity type
        CsdlEntityType entityType = edmProvider.getEntityType(SpringBootEdmProvider.ET_CAR_FQN);
        assertNotNull(entityType);
        
        // Get entity set
        CsdlEntitySet entitySet = edmProvider.getEntitySet(SpringBootEdmProvider.CONTAINER, SpringBootEdmProvider.ES_CARS_NAME);
        assertNotNull(entitySet);
        assertEquals(SpringBootEdmProvider.ET_CAR_FQN.getFullQualifiedNameAsString(), entitySet.getType());
        
        // Get container
        CsdlEntityContainer container = edmProvider.getEntityContainer();
        assertNotNull(container);
        assertTrue(container.getEntitySets().stream()
                  .anyMatch(es -> es.getName().equals(SpringBootEdmProvider.ES_CARS_NAME)));
        
        // Get schema
        List<CsdlSchema> schemas = edmProvider.getSchemas();
        assertNotNull(schemas);
        CsdlSchema schema = schemas.get(0);
        
        // Verify schema contains the entity type
        assertTrue(schema.getEntityTypes().stream()
                  .anyMatch(et -> et.getName().equals(SpringBootEdmProvider.ET_CAR_NAME)));
        
        // Verify schema contains the container
        assertEquals(SpringBootEdmProvider.CONTAINER_NAME, schema.getEntityContainer().getName());
    }

    /**
     * Helper method to assert that a property exists with the correct name and type
     */
    private void assertPropertyExists(List<CsdlProperty> properties, String expectedName, String expectedType) {
        assertTrue(properties.stream()
                  .anyMatch(prop -> expectedName.equals(prop.getName()) && expectedType.equals(prop.getType())),
                  "Property '" + expectedName + "' with type '" + expectedType + "' not found");
    }
}
