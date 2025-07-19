/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more         assertEquals(Integer.valueOf(1), testCar.getId());ontributor license agreements.  See the NOTICE file
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
package org.apache.olingo.sample.springboot.xmldb.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CarEntity class.
 */
public class CarEntityTest {

    private CarEntity carEntity;

    @BeforeEach
    public void setUp() {
        carEntity = new CarEntity();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(carEntity);
        assertNull(carEntity.getId());
        assertNull(carEntity.getModel());
        assertNull(carEntity.getModelYear());
        assertNull(carEntity.getPrice());
        assertNull(carEntity.getCurrency());
        assertNull(carEntity.getManufacturerId());
    }

    @Test
    public void testParameterizedConstructor() {
        BigDecimal price = new BigDecimal("35000.00");
        CarEntity testCar = new CarEntity("Camry", 2023, price, "USD", 1);

        assertEquals("Camry", testCar.getModel());
        assertEquals(Integer.valueOf(2023), testCar.getModelYear());
        assertEquals(price, testCar.getPrice());
        assertEquals("USD", testCar.getCurrency());
        assertEquals(Integer.valueOf(1), testCar.getManufacturerId());
    }

    @Test
    public void testSettersAndGetters() {
        Integer id = 1;
        String model = "Corolla";
        Integer modelYear = 2022;
        BigDecimal price = new BigDecimal("25000.00");
        String currency = "EUR";
        Integer manufacturerId = 2;

        carEntity.setId(id);
        carEntity.setModel(model);
        carEntity.setModelYear(modelYear);
        carEntity.setPrice(price);
        carEntity.setCurrency(currency);
        carEntity.setManufacturerId(manufacturerId);

        assertEquals(id, carEntity.getId());
        assertEquals(model, carEntity.getModel());
        assertEquals(modelYear, carEntity.getModelYear());
        assertEquals(price, carEntity.getPrice());
        assertEquals(currency, carEntity.getCurrency());
        assertEquals(manufacturerId, carEntity.getManufacturerId());
    }

    @Test
    public void testEqualsAndHashCode() {
        CarEntity car1 = new CarEntity("Model 3", 2023, new BigDecimal("40000"), "USD", 1);
        CarEntity car2 = new CarEntity("Model 3", 2023, new BigDecimal("40000"), "USD", 1);
        CarEntity car3 = new CarEntity("Model X", 2023, new BigDecimal("80000"), "USD", 1);

        car1.setId(1);
        car2.setId(1);
        car3.setId(2);

        // Test basic object equality (same instance)
        assertEquals(car1, car1);
        assertNotEquals(car1, null);
        assertNotEquals(car1, "not a car");
        
        // Test different objects with different data are not equal
        assertNotEquals(car1, car3);
        
        // Note: CarEntity doesn't override equals/hashCode, so different instances are not equal
        // This test checks the default Object behavior
        assertNotEquals(car1, car2); // Different instances, even with same data
        assertNotEquals(car1.hashCode(), car2.hashCode()); // Different hash codes
    }

    @Test
    public void testToString() {
        carEntity.setId(1);
        carEntity.setModel("Prius");
        carEntity.setModelYear(2021);
        carEntity.setPrice(new BigDecimal("30000"));
        carEntity.setCurrency("USD");
        carEntity.setManufacturerId(3);

        String toString = carEntity.toString();
        assertNotNull(toString);
        // CarEntity uses the default Object.toString() format: ClassName@hashCode
        assertTrue(toString.contains("CarEntity"));
        // The default toString doesn't contain field values, so we test for class name only
    }

    @Test
    public void testPriceValidation() {
        BigDecimal negativePrice = new BigDecimal("-1000");
        carEntity.setPrice(negativePrice);
        assertEquals(negativePrice, carEntity.getPrice());
        
        BigDecimal zeroPrice = BigDecimal.ZERO;
        carEntity.setPrice(zeroPrice);
        assertEquals(zeroPrice, carEntity.getPrice());
    }

    @Test
    public void testYearValidation() {
        Integer futureYear = 2030;
        carEntity.setModelYear(futureYear);
        assertEquals(futureYear, carEntity.getModelYear());
        
        Integer pastYear = 1900;
        carEntity.setModelYear(pastYear);
        assertEquals(pastYear, carEntity.getModelYear());
    }
}
