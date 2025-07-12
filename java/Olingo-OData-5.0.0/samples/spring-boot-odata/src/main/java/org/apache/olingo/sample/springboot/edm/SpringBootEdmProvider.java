/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
        // Create entity container
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        
        // Entity Sets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        entitySets.add(getEntitySet(CONTAINER, ES_CARS_NAME));
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        // Create Schema list
        List<CsdlSchema> schemas = new ArrayList<>();
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // Add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        entityTypes.add(getEntityType(ET_CAR_FQN));
        schema.setEntityTypes(entityTypes);

        // Add EntityContainer
        schema.setEntityContainer(getEntityContainer());
        
        schemas.add(schema);

        // Return the schemas list
        return schemas;
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
