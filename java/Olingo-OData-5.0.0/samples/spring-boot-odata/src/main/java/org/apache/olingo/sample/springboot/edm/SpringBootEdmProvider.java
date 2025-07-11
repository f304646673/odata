package org.apache.olingo.sample.springboot.edm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

/**
 * Spring Boot EDM Provider
 * 
 * This class defines the Entity Data Model (EDM) for the Spring Boot OData service.
 * It's inspired by the original CarsEdmProvider but adapted for Spring Boot environment.
 */
public class SpringBootEdmProvider extends CsdlAbstractEdmProvider {

    // Namespace
    public static final String NAMESPACE = "org.apache.olingo.sample.springboot";
    public static final String CONTAINER_NAME = "SpringBootContainer";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types
    public static final String ET_CAR_NAME = "Car";
    public static final FullQualifiedName ET_CAR_FQN = new FullQualifiedName(NAMESPACE, ET_CAR_NAME);

    // Entity Sets
    public static final String ES_CARS_NAME = "Cars";

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
        if (entityTypeName.equals(ET_CAR_FQN)) {
            return getCarEntityType();
        }
        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        if (entityContainer.equals(CONTAINER)) {
            if (entitySetName.equals(ES_CARS_NAME)) {
                return getCarEntitySet();
            }
        }
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        // Entity Sets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        entitySets.add(getCarEntitySet());

        // Create entity container
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        // Create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // Add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        entityTypes.add(getCarEntityType());
        schema.setEntityTypes(entityTypes);

        // Add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // Return the schema
        return Arrays.asList(schema);
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        // This method is invoked when displaying the service document at e.g. http://localhost:8080/cars.svc
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }
        return null;
    }

    /**
     * Define Car Entity Type
     */
    private CsdlEntityType getCarEntityType() {
        // Create properties
        CsdlProperty id = new CsdlProperty().setName("Id")
            .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty brand = new CsdlProperty().setName("Brand")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty model = new CsdlProperty().setName("Model")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty color = new CsdlProperty().setName("Color")
            .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty year = new CsdlProperty().setName("Year")
            .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty price = new CsdlProperty().setName("Price")
            .setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName());

        // Create PropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("Id");

        // Configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_CAR_NAME);
        entityType.setProperties(Arrays.asList(id, brand, model, color, year, price));
        entityType.setKey(Collections.singletonList(propertyRef));

        return entityType;
    }

    /**
     * Define Car Entity Set
     */
    private CsdlEntitySet getCarEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_CARS_NAME);
        entitySet.setType(ET_CAR_FQN);
        return entitySet;
    }
}
