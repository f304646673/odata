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
package org.apache.olingo.sample.springboot.xmlnative.data;

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

/**
 * Native XML Data Provider for Spring Boot OData
 * 
 * This provider manages data for the XML-native EDM provider.
 * It uses Olingo's native data structures and APIs.
 */
public class NativeXmlDataProvider {


    private final List<Entity> carList;
    private final List<Entity> manufacturerList;

    /**
     * Constructor - initializes sample data
     */
    public NativeXmlDataProvider() {
        
        carList = new ArrayList<>();
        manufacturerList = new ArrayList<>();
        
        createSampleData();
    }

    /**
     * Creates sample data using Olingo's native data structures
     */
    private void createSampleData() {
        
        // Create manufacturers first
        createManufacturer(1, "BMW", "Petuelring 130", "Munich", "80809", "Germany");
        createManufacturer(2, "Audi", "Audi-Ring 1", "Ingolstadt", "85057", "Germany");
        createManufacturer(3, "Mercedes-Benz", "Mercedesstraße 120", "Stuttgart", "70546", "Germany");
        
        // Create cars
        createCar(1, "X3", 2020, new BigDecimal("45000.00"), "USD");
        createCar(2, "A4", 2021, new BigDecimal("42000.00"), "USD");
        createCar(3, "C-Class", 2019, new BigDecimal("48000.00"), "USD");
        createCar(4, "X5", 2022, new BigDecimal("65000.00"), "USD");
        createCar(5, "A6", 2020, new BigDecimal("55000.00"), "USD");
    }

    /**
     * Creates a car entity using Olingo's native data structures
     */
    private void createCar(int id, String model, int modelYear, BigDecimal price, String currency) {
        Entity car = new Entity();
        car.setId(createId("Cars", id));
        car.setType("OData.Demo.Car");
        
        // Basic properties
        car.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, id));
        car.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, model));
        car.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, modelYear));
        car.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, price));
        car.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, currency));
        
        carList.add(car);
    }

    /**
     * Creates a manufacturer entity using Olingo's native data structures
     */
    private void createManufacturer(int id, String name, String street, String city, String zipCode, String country) {
        Entity manufacturer = new Entity();
        manufacturer.setId(createId("Manufacturers", id));
        manufacturer.setType("OData.Demo.Manufacturer");
        
        // Basic properties
        manufacturer.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, id));
        manufacturer.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, name));

        // Complex property - Address using Olingo's native ComplexValue
        ComplexValue address = new ComplexValue();
        address.getValue().add(new Property(null, "Street", ValueType.PRIMITIVE, street));
        address.getValue().add(new Property(null, "City", ValueType.PRIMITIVE, city));
        address.getValue().add(new Property(null, "ZipCode", ValueType.PRIMITIVE, zipCode));
        address.getValue().add(new Property(null, "Country", ValueType.PRIMITIVE, country));

        manufacturer.addProperty(new Property(null, "Address", ValueType.COMPLEX, address));

        manufacturerList.add(manufacturer);
    }

    /**
     * Creates entity ID URI using Olingo's native approach
     */
    private URI createId(String entitySetName, Object... keyValues) {
        try {
            StringBuilder idBuilder = new StringBuilder();
            idBuilder.append(entitySetName).append("(");
            for (int i = 0; i < keyValues.length; i++) {
                if (i > 0) {
                    idBuilder.append(",");
                }
                idBuilder.append(keyValues[i]);
            }
            idBuilder.append(")");
            return new URI(idBuilder.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error creating entity ID URI", e);
        }
    }

    /**
     * Get entity collection for entity set using Olingo's native approach
     */
    public EntityCollection getEntityCollection(EdmEntitySet edmEntitySet) throws ODataApplicationException {

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

        return entityCollection;
    }

    /**
     * Get single entity by key using Olingo's native approach
     */
    public Entity getEntity(EdmEntitySet edmEntitySet, List<String> keyValues) throws ODataApplicationException {

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

        Entity entity = entityList.stream()
            .filter(e -> {
                Property idProperty = e.getProperty("Id");
                return idProperty != null && idProperty.getValue().equals(keyValue);
            })
            .findFirst()
            .orElse(null);

        if (entity == null) {
            throw new ODataApplicationException("Entity with key " + keyValue + " not found.",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        return entity;
    }

    /**
     * Get data statistics using Olingo's native approach
     */
    public Map<String, Object> getDataStatistics() {
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCars", carList.size());
        stats.put("totalManufacturers", manufacturerList.size());
        stats.put("dataProvider", "Native XML Data Provider");
        
        return stats;
    }

    /**
     * Creates a new entity using Olingo's native data structures
     */
    public Entity createEntity(EdmEntitySet edmEntitySet, Entity entity) throws ODataApplicationException {

        String entitySetName = edmEntitySet.getName();
        
        // Generate new ID
        int newId = getNextId(entitySetName);
        entity.getProperty("Id").setValue(ValueType.PRIMITIVE, newId);
        entity.setId(createId(entitySetName, newId));

        // Add to appropriate list
        switch (entitySetName) {
            case "Cars":
                carList.add(entity);
                break;
            case "Manufacturers":
                manufacturerList.add(entity);
                break;
            default:
                throw new ODataApplicationException("Entity set " + entitySetName + " is not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

        return entity;
    }

    /**
     * Updates an existing entity using Olingo's native data structures
     */
    public void updateEntity(EdmEntitySet edmEntitySet, List<String> keyValues, Entity updateEntity) throws ODataApplicationException {

        Entity existingEntity = getEntity(edmEntitySet, keyValues);
        
        // Update properties
        for (Property property : updateEntity.getProperties()) {
            if (!"Id".equals(property.getName())) {
                existingEntity.getProperty(property.getName()).setValue(property.getValueType(), property.getValue());
            }
        }

    }

    /**
     * Deletes an entity using Olingo's native data structures
     */
    public void deleteEntity(EdmEntitySet edmEntitySet, List<String> keyValues) throws ODataApplicationException {

        Entity entityToDelete = getEntity(edmEntitySet, keyValues);
        
        String entitySetName = edmEntitySet.getName();
        switch (entitySetName) {
            case "Cars":
                carList.remove(entityToDelete);
                break;
            case "Manufacturers":
                manufacturerList.remove(entityToDelete);
                break;
            default:
                throw new ODataApplicationException("Entity set " + entitySetName + " is not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

    }

    /**
     * Gets next available ID for entity set
     */
    private int getNextId(String entitySetName) {
        switch (entitySetName) {
            case "Cars":
                return carList.stream()
                    .mapToInt(e -> (Integer) e.getProperty("Id").getValue())
                    .max()
                    .orElse(0) + 1;
            case "Manufacturers":
                return manufacturerList.stream()
                    .mapToInt(e -> (Integer) e.getProperty("Id").getValue())
                    .max()
                    .orElse(0) + 1;
            default:
                return 1;
        }
    }
}
