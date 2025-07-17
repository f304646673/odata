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
package org.apache.olingo.sample.springboot.xml.data;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML-based Data Provider for Spring Boot OData
 * 
 * This provider manages the data for entities defined in the XML EDM.
 * It provides in-memory storage and basic CRUD operations.
 */
public class XmlBasedDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(XmlBasedDataProvider.class);

    // In-memory storage
    private List<Entity> carList;
    private List<Entity> manufacturerList;

    /**
     * Constructor - initializes sample data
     */
    public XmlBasedDataProvider() {
        LOG.info("Initializing XML-based data provider");
        initSampleData();
    }

    /**
     * Initialize sample data
     */
    private void initSampleData() {
        LOG.debug("Creating sample data");

        // Initialize lists
        carList = new ArrayList<>();
        manufacturerList = new ArrayList<>();

        // Create manufacturers
        Entity bmw = createManufacturer(1, "BMW", "Munich", "Germany", "80809", "Petuelring 130");
        Entity audi = createManufacturer(2, "Audi", "Ingolstadt", "Germany", "85057", "Audi-Ring 1");
        Entity mercedes = createManufacturer(3, "Mercedes-Benz", "Stuttgart", "Germany", "70546", "Mercedesstraße 120");

        manufacturerList.add(bmw);
        manufacturerList.add(audi);
        manufacturerList.add(mercedes);

        // Create cars
        Entity car1 = createCar(1, "X3", 2020, new BigDecimal("45000.00"), "USD");
        Entity car2 = createCar(2, "A4", 2021, new BigDecimal("42000.00"), "USD");
        Entity car3 = createCar(3, "C-Class", 2019, new BigDecimal("48000.00"), "USD");
        Entity car4 = createCar(4, "X5", 2022, new BigDecimal("65000.00"), "USD");
        Entity car5 = createCar(5, "A6", 2020, new BigDecimal("55000.00"), "USD");

        carList.add(car1);
        carList.add(car2);
        carList.add(car3);
        carList.add(car4);
        carList.add(car5);

        LOG.info("Sample data initialized - {} cars, {} manufacturers", carList.size(), manufacturerList.size());
    }

    /**
     * Create a manufacturer entity
     */
    private Entity createManufacturer(int id, String name, String city, String country, String zipCode, String street) {
        Entity manufacturer = new Entity();
        manufacturer.setId(createId("Manufacturers", id));
        manufacturer.setType("OData.Demo.Manufacturer");

        // Basic properties
        manufacturer.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, id));
        manufacturer.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, name));

        // Complex property - Address
        ComplexValue address = new ComplexValue();
        address.getValue().add(new Property(null, "Street", ValueType.PRIMITIVE, street));
        address.getValue().add(new Property(null, "City", ValueType.PRIMITIVE, city));
        address.getValue().add(new Property(null, "ZipCode", ValueType.PRIMITIVE, zipCode));
        address.getValue().add(new Property(null, "Country", ValueType.PRIMITIVE, country));

        manufacturer.addProperty(new Property(null, "Address", ValueType.COMPLEX, address));

        return manufacturer;
    }

    /**
     * Create a car entity
     */
    private Entity createCar(int id, String model, int modelYear, BigDecimal price, String currency) {
        Entity car = new Entity();
        car.setId(createId("Cars", id));
        car.setType("OData.Demo.Car");

        car.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, id));
        car.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, model));
        car.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, modelYear));
        car.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, price));
        car.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, currency));

        return car;
    }

    /**
     * Create entity ID URI
     */
    private URI createId(String entitySetName, Object... keys) {
        try {
            StringBuilder idBuilder = new StringBuilder();
            idBuilder.append(entitySetName).append("(");
            for (int i = 0; i < keys.length; i++) {
                if (i > 0) {
                    idBuilder.append(",");
                }
                idBuilder.append(keys[i].toString());
            }
            idBuilder.append(")");
            return new URI(idBuilder.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error creating entity ID URI", e);
        }
    }

    /**
     * Get entity collection for entity set
     */
    public EntityCollection getEntityCollection(EdmEntitySet edmEntitySet) throws ODataApplicationException {
        LOG.debug("Getting entity collection for: {}", edmEntitySet.getName());

        EntityCollection entityCollection = new EntityCollection();

        String entitySetName = edmEntitySet.getName();
        switch (entitySetName) {
            case "Cars":
                entityCollection.getEntities().addAll(carList);
                break;
            case "Manufacturers":
                entityCollection.getEntities().addAll(manufacturerList);
                break;
            default:
                throw new ODataApplicationException("Entity set " + entitySetName + " is not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

        LOG.debug("Returning {} entities for entity set: {}", entityCollection.getEntities().size(), entitySetName);
        return entityCollection;
    }

    /**
     * Get single entity by key
     */
    public Entity getEntity(EdmEntitySet edmEntitySet, List<String> keyValues) throws ODataApplicationException {
        LOG.debug("Getting entity from set: {} with key: {}", edmEntitySet.getName(), keyValues);

        if (keyValues.isEmpty()) {
            throw new ODataApplicationException("Key values cannot be empty.",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }

        String entitySetName = edmEntitySet.getName();
        int keyValue = Integer.parseInt(keyValues.get(0));

        List<Entity> entityList;
        switch (entitySetName) {
            case "Cars":
                entityList = carList;
                break;
            case "Manufacturers":
                entityList = manufacturerList;
                break;
            default:
                throw new ODataApplicationException("Entity set " + entitySetName + " is not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

        // Find entity by ID
        for (Entity entity : entityList) {
            Property idProperty = entity.getProperty("Id");
            if (idProperty != null && idProperty.getValue().equals(keyValue)) {
                LOG.debug("Found entity with ID: {}", keyValue);
                return entity;
            }
        }

        LOG.debug("Entity with ID {} not found in set: {}", keyValue, entitySetName);
        return null;
    }

    /**
     * Create new entity
     */
    public Entity createEntity(EdmEntitySet edmEntitySet, Entity entity) throws ODataApplicationException {
        LOG.debug("Creating entity in set: {}", edmEntitySet.getName());

        String entitySetName = edmEntitySet.getName();
        List<Entity> entityList;

        switch (entitySetName) {
            case "Cars":
                entityList = carList;
                break;
            case "Manufacturers":
                entityList = manufacturerList;
                break;
            default:
                throw new ODataApplicationException("Entity set " + entitySetName + " is not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

        // Generate new ID
        int newId = entityList.size() + 1;
        entity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, newId));
        entity.setId(createId(entitySetName, newId));
        entity.setType("OData.Demo." + edmEntitySet.getEntityType().getName());

        entityList.add(entity);

        LOG.info("Created new entity with ID: {} in set: {}", newId, entitySetName);
        return entity;
    }

    /**
     * Update existing entity
     */
    public void updateEntity(EdmEntitySet edmEntitySet, List<String> keyValues, Entity updateEntity) throws ODataApplicationException {
        LOG.debug("Updating entity in set: {} with key: {}", edmEntitySet.getName(), keyValues);

        Entity existingEntity = getEntity(edmEntitySet, keyValues);
        if (existingEntity == null) {
            throw new ODataApplicationException("Entity not found.",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        // Update properties
        for (Property property : updateEntity.getProperties()) {
            existingEntity.addProperty(property);
        }

        LOG.info("Updated entity with key: {} in set: {}", keyValues, edmEntitySet.getName());
    }

    /**
     * Delete entity
     */
    public void deleteEntity(EdmEntitySet edmEntitySet, List<String> keyValues) throws ODataApplicationException {
        LOG.debug("Deleting entity from set: {} with key: {}", edmEntitySet.getName(), keyValues);

        Entity entityToDelete = getEntity(edmEntitySet, keyValues);
        if (entityToDelete == null) {
            throw new ODataApplicationException("Entity not found.",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        String entitySetName = edmEntitySet.getName();
        List<Entity> entityList;

        switch (entitySetName) {
            case "Cars":
                entityList = carList;
                break;
            case "Manufacturers":
                entityList = manufacturerList;
                break;
            default:
                throw new ODataApplicationException("Entity set " + entitySetName + " is not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

        entityList.remove(entityToDelete);
        LOG.info("Deleted entity with key: {} from set: {}", keyValues, edmEntitySet.getName());
    }

    /**
     * Get data statistics
     */
    public Map<String, Integer> getDataStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Cars", carList.size());
        stats.put("Manufacturers", manufacturerList.size());
        return stats;
    }
}
