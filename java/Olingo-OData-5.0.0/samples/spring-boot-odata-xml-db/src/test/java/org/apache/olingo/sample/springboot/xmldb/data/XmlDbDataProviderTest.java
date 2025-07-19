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
package org.apache.olingo.sample.springboot.xmldb.data;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.sample.springboot.xmldb.service.XmlDbDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for XmlDbDataProvider class.
 */
@ExtendWith(MockitoExtension.class)
public class XmlDbDataProviderTest {

    @Mock
    private XmlDbDataService dataService;

    @InjectMocks
    private XmlDbDataProvider dataProvider;

    @Test
    public void testGetCars() {
        // Arrange
        org.apache.olingo.sample.springboot.xmldb.entity.CarEntity car1 = 
            new org.apache.olingo.sample.springboot.xmldb.entity.CarEntity("Model S", 2023, new BigDecimal("80000"), "USD", 1);
        car1.setId(1);
        
        org.apache.olingo.sample.springboot.xmldb.entity.CarEntity car2 = 
            new org.apache.olingo.sample.springboot.xmldb.entity.CarEntity("Model 3", 2022, new BigDecimal("45000"), "USD", 1);
        car2.setId(2);

        when(dataService.getAllCars()).thenReturn(Arrays.asList(car1, car2));

        // Act
        EntityCollection result = dataProvider.getCars();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getEntities().size());
        
        Entity entity1 = result.getEntities().get(0);
        assertEquals(1, entity1.getProperty("ID").getValue());
        assertEquals("Model S", entity1.getProperty("Model").getValue());
        assertEquals(2023, entity1.getProperty("ModelYear").getValue());
        assertEquals(new BigDecimal("80000"), entity1.getProperty("Price").getValue());
        assertEquals("USD", entity1.getProperty("Currency").getValue());
        assertEquals(1, entity1.getProperty("ManufacturerID").getValue());

        verify(dataService, times(1)).getAllCars();
    }

    @Test
    public void testGetCar() {
        // Arrange
        org.apache.olingo.sample.springboot.xmldb.entity.CarEntity car = 
            new org.apache.olingo.sample.springboot.xmldb.entity.CarEntity("Model Y", 2023, new BigDecimal("60000"), "USD", 1);
        car.setId(1);

        when(dataService.getCarById(1)).thenReturn(car);

        // Act
        Entity result = dataProvider.getCar(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProperty("ID").getValue());
        assertEquals("Model Y", result.getProperty("Model").getValue());
        assertEquals(2023, result.getProperty("ModelYear").getValue());
        assertEquals(new BigDecimal("60000"), result.getProperty("Price").getValue());
        assertEquals("USD", result.getProperty("Currency").getValue());
        assertEquals(1, result.getProperty("ManufacturerID").getValue());

        verify(dataService, times(1)).getCarById(1);
    }

    @Test
    public void testGetCarNotFound() {
        // Arrange
        when(dataService.getCarById(999)).thenReturn(null);

        // Act
        Entity result = dataProvider.getCar(999);

        // Assert
        assertNull(result);
        verify(dataService, times(1)).getCarById(999);
    }

    @Test
    public void testGetManufacturers() {
        // Arrange
        org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity manufacturer1 = 
            new org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity("Tesla", 2003, "USA");
        manufacturer1.setId(1);
        
        org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity manufacturer2 = 
            new org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity("BMW", 1916, "Germany");
        manufacturer2.setId(2);

        when(dataService.getAllManufacturers()).thenReturn(Arrays.asList(manufacturer1, manufacturer2));

        // Act
        EntityCollection result = dataProvider.getManufacturers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getEntities().size());
        
        Entity entity1 = result.getEntities().get(0);
        assertEquals(1, entity1.getProperty("ID").getValue());
        assertEquals("Tesla", entity1.getProperty("Name").getValue());
        assertEquals(2003, entity1.getProperty("Founded").getValue());
        assertEquals("USA", entity1.getProperty("Headquarters").getValue());

        verify(dataService, times(1)).getAllManufacturers();
    }

    @Test
    public void testGetManufacturer() {
        // Arrange
        org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity manufacturer = 
            new org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity("Ford", 1903, "USA");
        manufacturer.setId(1);

        when(dataService.getManufacturerById(1)).thenReturn(manufacturer);

        // Act
        Entity result = dataProvider.getManufacturer(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProperty("ID").getValue());
        assertEquals("Ford", result.getProperty("Name").getValue());
        assertEquals(1903, result.getProperty("Founded").getValue());
        assertEquals("USA", result.getProperty("Headquarters").getValue());

        verify(dataService, times(1)).getManufacturerById(1);
    }

    @Test
    public void testGetManufacturerNotFound() {
        // Arrange
        when(dataService.getManufacturerById(999)).thenReturn(null);

        // Act
        Entity result = dataProvider.getManufacturer(999);

        // Assert
        assertNull(result);
        verify(dataService, times(1)).getManufacturerById(999);
    }

    @Test
    public void testGetCarsEmptyList() {
        // Arrange
        when(dataService.getAllCars()).thenReturn(Arrays.asList());

        // Act
        EntityCollection result = dataProvider.getCars();

        // Assert
        assertNotNull(result);
        assertTrue(result.getEntities().isEmpty());
        verify(dataService, times(1)).getAllCars();
    }

    @Test
    public void testGetManufacturersEmptyList() {
        // Arrange
        when(dataService.getAllManufacturers()).thenReturn(Arrays.asList());

        // Act
        EntityCollection result = dataProvider.getManufacturers();

        // Assert
        assertNotNull(result);
        assertTrue(result.getEntities().isEmpty());
        verify(dataService, times(1)).getAllManufacturers();
    }
}