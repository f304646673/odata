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
package org.apache.olingo.sample.springboot.xmlsplit.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;

/**
 * Data Provider for XML Split Sample
 * 
 * Provides sample data for Cars and Manufacturers with Address complex type
 */
public class XmlSplitDataProvider {
    
    /**
     * Get Cars entity collection
     */
    public EntityCollection getCars() {
        EntityCollection cars = new EntityCollection();
        
        // Car 1
        Entity car1 = new Entity();
        car1.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 1));
        car1.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "X3"));
        car1.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, 2020));
        car1.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 45000.00));
        car1.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "USD"));
        cars.getEntities().add(car1);
        
        // Car 2
        Entity car2 = new Entity();
        car2.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 2));
        car2.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "A4"));
        car2.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, 2021));
        car2.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 42000.00));
        car2.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "USD"));
        cars.getEntities().add(car2);
        
        // Car 3
        Entity car3 = new Entity();
        car3.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 3));
        car3.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, "C-Class"));
        car3.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, 2022));
        car3.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, 48000.00));
        car3.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, "USD"));
        cars.getEntities().add(car3);
        
        return cars;
    }
    
    /**
     * Get Manufacturers entity collection
     */
    public EntityCollection getManufacturers() {
        EntityCollection manufacturers = new EntityCollection();
        
        // BMW
        Entity bmw = new Entity();
        bmw.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 1));
        bmw.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "BMW"));
        bmw.addProperty(new Property(null, "Address", ValueType.COMPLEX, createAddress("BMW Headquarters", "Munich", "80788", "Germany")));
        manufacturers.getEntities().add(bmw);
        
        // Audi
        Entity audi = new Entity();
        audi.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 2));
        audi.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Audi"));
        audi.addProperty(new Property(null, "Address", ValueType.COMPLEX, createAddress("Audi Headquarters", "Ingolstadt", "85045", "Germany")));
        manufacturers.getEntities().add(audi);
        
        // Mercedes
        Entity mercedes = new Entity();
        mercedes.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, 3));
        mercedes.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Mercedes-Benz"));
        mercedes.addProperty(new Property(null, "Address", ValueType.COMPLEX, createAddress("Mercedes Headquarters", "Stuttgart", "70546", "Germany")));
        manufacturers.getEntities().add(mercedes);
        
        return manufacturers;
    }
    
    /**
     * Create Address complex value
     */
    private ComplexValue createAddress(String street, String city, String zipCode, String country) {
        ComplexValue address = new ComplexValue();
        List<Property> properties = new ArrayList<>();
        
        properties.add(new Property(null, "Street", ValueType.PRIMITIVE, street));
        properties.add(new Property(null, "City", ValueType.PRIMITIVE, city));
        properties.add(new Property(null, "ZipCode", ValueType.PRIMITIVE, zipCode));
        properties.add(new Property(null, "Country", ValueType.PRIMITIVE, country));
        
        address.getValue().addAll(properties);
        return address;
    }
    
    /**
     * Get single Car entity by ID
     */
    public Entity getCar(int id) {
        EntityCollection cars = getCars();
        for (Entity car : cars.getEntities()) {
            if (car.getProperty("Id").getValue().equals(id)) {
                return car;
            }
        }
        return null;
    }
    
    /**
     * Get single Manufacturer entity by ID
     */
    public Entity getManufacturer(int id) {
        EntityCollection manufacturers = getManufacturers();
        for (Entity manufacturer : manufacturers.getEntities()) {
            if (manufacturer.getProperty("Id").getValue().equals(id)) {
                return manufacturer;
            }
        }
        return null;
    }
}
