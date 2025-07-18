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

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class XmlImportDataProviderTest {

    private final XmlImportDataProvider dataProvider = new XmlImportDataProvider();

    @Test
    void testGetCars() {
        EntityCollection cars = dataProvider.getCars();
        
        assertThat(cars).isNotNull();
        assertThat(cars.getEntities()).isNotNull();
        assertThat(cars.getEntities()).isNotEmpty();
        assertThat(cars.getEntities().size()).isEqualTo(4);
        
        // Check first car
        Entity firstCar = cars.getEntities().get(0);
        assertThat(firstCar.getProperty("Id")).isNotNull();
        assertThat(firstCar.getProperty("Model")).isNotNull();
        assertThat(firstCar.getProperty("ModelYear")).isNotNull();
        assertThat(firstCar.getProperty("Price")).isNotNull();
        assertThat(firstCar.getProperty("Currency")).isNotNull();
    }

    @Test
    void testGetManufacturers() {
        EntityCollection manufacturers = dataProvider.getManufacturers();
        
        assertThat(manufacturers).isNotNull();
        assertThat(manufacturers.getEntities()).isNotNull();
        assertThat(manufacturers.getEntities()).isNotEmpty();
        assertThat(manufacturers.getEntities().size()).isEqualTo(3);
        
        // Check first manufacturer
        Entity firstManufacturer = manufacturers.getEntities().get(0);
        assertThat(firstManufacturer.getProperty("Id")).isNotNull();
        assertThat(firstManufacturer.getProperty("Name")).isNotNull();
        assertThat(firstManufacturer.getProperty("Address")).isNotNull();
        
        // Check Address complex property
        Object address = firstManufacturer.getProperty("Address").getValue();
        assertThat(address).isNotNull();
        assertThat(address).isInstanceOf(ComplexValue.class);
        
        ComplexValue addressComplex = (ComplexValue) address;
        assertThat(addressComplex.getValue()).isNotEmpty();
    }

    @Test
    void testGetCar() {
        Entity car = dataProvider.getCar(1);
        
        assertThat(car).isNotNull();
        assertThat(car.getProperty("Id").getValue()).isEqualTo(1);
        assertThat(car.getProperty("Model")).isNotNull();
        assertThat(car.getProperty("ModelYear")).isNotNull();
        assertThat(car.getProperty("Price")).isNotNull();
        assertThat(car.getProperty("Currency")).isNotNull();
    }

    @Test
    void testGetManufacturer() {
        Entity manufacturer = dataProvider.getManufacturer(1);
        
        assertThat(manufacturer).isNotNull();
        assertThat(manufacturer.getProperty("Id").getValue()).isEqualTo(1);
        assertThat(manufacturer.getProperty("Name")).isNotNull();
        assertThat(manufacturer.getProperty("Address")).isNotNull();
        
        // Verify Address complex type structure
        ComplexValue address = (ComplexValue) manufacturer.getProperty("Address").getValue();
        assertThat(address).isNotNull();
        assertThat(address.getValue()).isNotEmpty();
        
        // Check if Address has the expected properties
        boolean hasStreet = address.getValue().stream()
            .anyMatch(prop -> "Street".equals(prop.getName()));
        boolean hasCity = address.getValue().stream()
            .anyMatch(prop -> "City".equals(prop.getName()));
        boolean hasZipCode = address.getValue().stream()
            .anyMatch(prop -> "ZipCode".equals(prop.getName()));
        boolean hasCountry = address.getValue().stream()
            .anyMatch(prop -> "Country".equals(prop.getName()));
            
        assertThat(hasStreet).isTrue();
        assertThat(hasCity).isTrue();
        assertThat(hasZipCode).isTrue();
        assertThat(hasCountry).isTrue();
    }

    @Test
    void testGetCar_NotFound() {
        Entity car = dataProvider.getCar(999);
        
        assertThat(car).isNull();
    }

    @Test
    void testGetManufacturer_NotFound() {
        Entity manufacturer = dataProvider.getManufacturer(999);
        
        assertThat(manufacturer).isNull();
    }

    @Test
    void testDataConsistency() {
        EntityCollection cars = dataProvider.getCars();
        EntityCollection manufacturers = dataProvider.getManufacturers();
        
        assertThat(cars.getEntities().size()).isGreaterThan(0);
        assertThat(manufacturers.getEntities().size()).isGreaterThan(0);
        
        // Verify that we have the expected number of entities
        assertThat(cars.getEntities().size()).isEqualTo(4);
        assertThat(manufacturers.getEntities().size()).isEqualTo(3);
    }

    @Test
    void testManufacturerNames() {
        EntityCollection manufacturers = dataProvider.getManufacturers();
        
        // Check that we have BMW, Mercedes-Benz, and Toyota
        boolean hasBMW = manufacturers.getEntities().stream()
            .anyMatch(m -> "BMW".equals(m.getProperty("Name").getValue()));
        boolean hasMercedes = manufacturers.getEntities().stream()
            .anyMatch(m -> "Mercedes-Benz".equals(m.getProperty("Name").getValue()));
        boolean hasToyota = manufacturers.getEntities().stream()
            .anyMatch(m -> "Toyota".equals(m.getProperty("Name").getValue()));
            
        assertThat(hasBMW).isTrue();
        assertThat(hasMercedes).isTrue();
        assertThat(hasToyota).isTrue();
    }

    @Test
    void testCarModels() {
        EntityCollection cars = dataProvider.getCars();
        
        // Verify we have some known car models
        boolean hasModel = cars.getEntities().stream()
            .anyMatch(car -> car.getProperty("Model").getValue() != null);
            
        assertThat(hasModel).isTrue();
        
        // Check that all cars have required properties
        for (Entity car : cars.getEntities()) {
            assertThat(car.getProperty("Id")).isNotNull();
            assertThat(car.getProperty("Model")).isNotNull();
            assertThat(car.getProperty("ModelYear")).isNotNull();
            assertThat(car.getProperty("Price")).isNotNull();
            assertThat(car.getProperty("Currency")).isNotNull();
        }
    }
}
