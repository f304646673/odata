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
package org.apache.olingo.sample.springboot.xmldb.edm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.stereotype.Component;

@Component
public class XmlDbEdmProvider extends CsdlAbstractEdmProvider {

    // Service Namespace
    public static final String NAMESPACE = "OData.Sample.XmlDb";

    // EDM Container
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types Names
    public static final String ET_CAR_NAME = "Car";
    public static final FullQualifiedName ET_CAR_FQN = new FullQualifiedName(NAMESPACE, ET_CAR_NAME);

    public static final String ET_MANUFACTURER_NAME = "Manufacturer";
    public static final FullQualifiedName ET_MANUFACTURER_FQN = new FullQualifiedName(NAMESPACE, ET_MANUFACTURER_NAME);

    // Entity Set Names
    public static final String ES_CARS_NAME = "Cars";
    public static final String ES_MANUFACTURERS_NAME = "Manufacturers";

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {

        if (entityTypeName.equals(ET_CAR_FQN)) {
            return getCarEntityType();
        } else if (entityTypeName.equals(ET_MANUFACTURER_FQN)) {
            return getManufacturerEntityType();
        }

        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {

        if (entityContainer.equals(CONTAINER)) {
            if (entitySetName.equals(ES_CARS_NAME)) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(ES_CARS_NAME);
                entitySet.setType(ET_CAR_FQN);
                return entitySet;
            } else if (entitySetName.equals(ES_MANUFACTURERS_NAME)) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(ES_MANUFACTURERS_NAME);
                entitySet.setType(ET_MANUFACTURER_FQN);
                return entitySet;
            }
        }

        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        entitySets.add(getEntitySet(CONTAINER, ES_CARS_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_MANUFACTURERS_NAME));

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {

        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        entityTypes.add(getEntityType(ET_CAR_FQN));
        entityTypes.add(getEntityType(ET_MANUFACTURER_FQN));
        schema.setEntityTypes(entityTypes);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);

        return schemas;
    }

    private CsdlEntityType getCarEntityType() {

        // create EntityType properties
        CsdlProperty id = new CsdlProperty().setName("ID").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()).setNullable(false);
        CsdlProperty model = new CsdlProperty().setName("Model").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setNullable(false);
        CsdlProperty modelYear = new CsdlProperty().setName("ModelYear").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty price = new CsdlProperty().setName("Price").setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName()).setPrecision(10).setScale(2);
        CsdlProperty currency = new CsdlProperty().setName("Currency").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty manufacturerId = new CsdlProperty().setName("ManufacturerID").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

        // create PropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_CAR_NAME);
        entityType.setProperties(Arrays.asList(id, model, modelYear, price, currency, manufacturerId));
        entityType.setKey(Collections.singletonList(propertyRef));

        return entityType;
    }

    private CsdlEntityType getManufacturerEntityType() {

        // create EntityType properties
        CsdlProperty id = new CsdlProperty().setName("ID").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()).setNullable(false);
        CsdlProperty name = new CsdlProperty().setName("Name").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setNullable(false);
        CsdlProperty founded = new CsdlProperty().setName("Founded").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty headquarters = new CsdlProperty().setName("Headquarters").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

        // create PropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_MANUFACTURER_NAME);
        entityType.setProperties(Arrays.asList(id, name, founded, headquarters));
        entityType.setKey(Collections.singletonList(propertyRef));

        return entityType;
    }
}
