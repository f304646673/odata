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
package org.apache.olingo.sample.springboot.xmlimport.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.springframework.stereotype.Component;

@Component
public class XmlImportDataProvider {

    private List<Entity> cars;
    private List<Entity> manufacturers;
    
    public XmlImportDataProvider() {
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        initializeManufacturers();
        initializeCars();
    }
    
    private void initializeManufacturers() {
        manufacturers = new ArrayList<>();
        
        // BMW
        Entity bmw = new Entity();
        bmw.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 1));
        bmw.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "BMW"));
        bmw.addProperty(new Property(null, "Address", ValueType.COMPLEX, createAddress("BMW Headquarter", "Munich", "80788", "Germany")));
        manufacturers.add(bmw);
        
        // Mercedes
        Entity mercedes = new Entity();
        mercedes.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 2));
        mercedes.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Mercedes-Benz"));
        mercedes.addProperty(new Property(null, "Address", ValueType.COMPLEX, createAddress("Mercedes-Platz", "Stuttgart", "70546", "Germany")));
        manufacturers.add(mercedes);
        
        // Toyota
        Entity toyota = new Entity();
        toyota.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 3));
        toyota.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Toyota"));
        toyota.addProperty(new Property(null, "Address", ValueType.COMPLEX, createAddress("Toyota City", "Toyota", "471-8571", "Japan")));
        manufacturers.add(toyota);
    }
    
    /**
     * Initialize sample cars
     */
    private void initializeCars() {
        cars = new ArrayList<>();
        
        // BMW cars
        Entity bmwX5 = new Entity();
        bmwX5.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 1));
        bmwX5.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "X5"));
        bmwX5.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, 2023));
        bmwX5.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 65000.00));
        bmwX5.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "EUR"));
        cars.add(bmwX5);
        
        Entity bmw3Series = new Entity();
        bmw3Series.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 2));
        bmw3Series.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "3 Series"));
        bmw3Series.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, 2023));
        bmw3Series.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 45000.00));
        bmw3Series.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "EUR"));
        cars.add(bmw3Series);
        
        // Mercedes cars
        Entity mercedesE = new Entity();
        mercedesE.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 3));
        mercedesE.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "E-Class"));
        mercedesE.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, 2023));
        mercedesE.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 55000.00));
        mercedesE.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "EUR"));
        cars.add(mercedesE);
        
        // Toyota cars
        Entity toyotaCamry = new Entity();
        toyotaCamry.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 4));
        toyotaCamry.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "Camry"));
        toyotaCamry.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, 2023));
        toyotaCamry.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 30000.00));
        toyotaCamry.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "USD"));
        cars.add(toyotaCamry);
    }
    
    /**
     * Create an Address complex value
     */
    private ComplexValue createAddress(String street, String city, String zipCode, String country) {
        ComplexValue address = new ComplexValue();
        address.getValue().add(new Property(null, "Street", ValueType.PRIMITIVE, street));
        address.getValue().add(new Property(null, "City", ValueType.PRIMITIVE, city));
        address.getValue().add(new Property(null, "ZipCode", ValueType.PRIMITIVE, zipCode));
        address.getValue().add(new Property(null, "Country", ValueType.PRIMITIVE, country));
        return address;
    }
    
    /**
     * Get cars data
     */
    public EntityCollection getCars() {
        EntityCollection collection = new EntityCollection();
        collection.getEntities().addAll(cars);
        return collection;
    }
    
    /**
     * Get manufacturers data
     */
    public EntityCollection getManufacturers() {
        EntityCollection collection = new EntityCollection();
        collection.getEntities().addAll(manufacturers);
        return collection;
    }
    
    /**
     * Get car by ID
     */
    public Entity getCar(int id) {
        return cars.stream()
                .filter(car -> ((Integer) car.getProperty("Id").getValue()).equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get manufacturer by ID
     */
    public Entity getManufacturer(int id) {
        return manufacturers.stream()
                .filter(manufacturer -> ((Integer) manufacturer.getProperty("Id").getValue()).equals(id))
                .findFirst()
                .orElse(null);
    }
}
