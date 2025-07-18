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
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for XmlDataProvider
 */
class XmlDataProviderTest {

    private XmlDataProvider dataProvider;

    @BeforeEach
    void setUp() {
        dataProvider = new XmlDataProvider();
    }

    @Test
    void shouldReturnCarsCollection() throws ODataApplicationException {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Cars");

        // When
        EntityCollection result = dataProvider.getEntityCollection(edmEntitySet);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEntities()).isNotEmpty();
        assertThat(result.getEntities()).hasSize(5);

        // Verify first car
        Entity firstCar = result.getEntities().get(0);
        assertThat(firstCar.getType()).isEqualTo("OData.Demo.Car");
        
        Property idProperty = firstCar.getProperty("Id");
        assertThat(idProperty).isNotNull();
        assertThat(idProperty.getValue()).isEqualTo(1);

        Property modelProperty = firstCar.getProperty("Model");
        assertThat(modelProperty).isNotNull();
        assertThat(modelProperty.getValue()).isEqualTo("X3");
    }

    @Test
    void shouldReturnManufacturersCollection() throws ODataApplicationException {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Manufacturers");

        // When
        EntityCollection result = dataProvider.getEntityCollection(edmEntitySet);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEntities()).isNotEmpty();
        assertThat(result.getEntities()).hasSize(3);

        // Verify first manufacturer
        Entity firstManufacturer = result.getEntities().get(0);
        assertThat(firstManufacturer.getType()).isEqualTo("OData.Demo.Manufacturer");
        
        Property idProperty = firstManufacturer.getProperty("Id");
        assertThat(idProperty).isNotNull();
        assertThat(idProperty.getValue()).isEqualTo(1);

        Property nameProperty = firstManufacturer.getProperty("Name");
        assertThat(nameProperty).isNotNull();
        assertThat(nameProperty.getValue()).isEqualTo("BMW");

        // Verify complex property (Address)
        Property addressProperty = firstManufacturer.getProperty("Address");
        assertThat(addressProperty).isNotNull();
        assertThat(addressProperty.getValue()).isNotNull();
    }

    @Test
    void shouldReturnSpecificCar() throws ODataApplicationException {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Cars");

        // When
        Entity result = dataProvider.getEntity(edmEntitySet, List.of("1"));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("OData.Demo.Car");
        
        Property idProperty = result.getProperty("Id");
        assertThat(idProperty.getValue()).isEqualTo(1);

        Property modelProperty = result.getProperty("Model");
        assertThat(modelProperty.getValue()).isEqualTo("X3");

        Property priceProperty = result.getProperty("Price");
        assertThat(priceProperty.getValue()).isEqualTo(new BigDecimal("45000.00"));
    }

    @Test
    void shouldReturnSpecificManufacturer() throws ODataApplicationException {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Manufacturers");

        // When
        Entity result = dataProvider.getEntity(edmEntitySet, List.of("2"));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("OData.Demo.Manufacturer");
        
        Property idProperty = result.getProperty("Id");
        assertThat(idProperty.getValue()).isEqualTo(2);

        Property nameProperty = result.getProperty("Name");
        assertThat(nameProperty.getValue()).isEqualTo("Audi");
    }

    @Test
    void shouldThrowExceptionForNonExistentCar() {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Cars");

        // When & Then
        assertThatThrownBy(() -> dataProvider.getEntity(edmEntitySet, List.of("999")))
            .isInstanceOf(ODataApplicationException.class)
            .hasMessageContaining("Entity with key 999 not found");
    }

    @Test
    void shouldThrowExceptionForNonExistentManufacturer() {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Manufacturers");

        // When & Then
        assertThatThrownBy(() -> dataProvider.getEntity(edmEntitySet, List.of("999")))
            .isInstanceOf(ODataApplicationException.class)
            .hasMessageContaining("Entity with key 999 not found");
    }

    @Test
    void shouldThrowExceptionForUnknownEntitySet() {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("UnknownEntitySet");

        // When & Then
        assertThatThrownBy(() -> dataProvider.getEntityCollection(edmEntitySet))
            .isInstanceOf(ODataApplicationException.class)
            .hasMessageContaining("Entity set UnknownEntitySet is not supported");
    }

    @Test
    void shouldThrowExceptionForUnknownEntitySetInGetEntity() {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("UnknownEntitySet");

        // When & Then
        assertThatThrownBy(() -> dataProvider.getEntity(edmEntitySet, List.of("1")))
            .isInstanceOf(ODataApplicationException.class)
            .hasMessageContaining("Entity set UnknownEntitySet is not supported");
    }

    @Test
    void shouldVerifyAllCarsHaveRequiredProperties() throws ODataApplicationException {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Cars");

        // When
        EntityCollection result = dataProvider.getEntityCollection(edmEntitySet);

        // Then
        for (Entity car : result.getEntities()) {
            assertThat(car.getProperty("Id")).isNotNull();
            assertThat(car.getProperty("Model")).isNotNull();
            assertThat(car.getProperty("ModelYear")).isNotNull();
            assertThat(car.getProperty("Price")).isNotNull();
            assertThat(car.getProperty("Currency")).isNotNull();
            
            // Verify data types
            assertThat(car.getProperty("Id").getValue()).isInstanceOf(Integer.class);
            assertThat(car.getProperty("Model").getValue()).isInstanceOf(String.class);
            assertThat(car.getProperty("ModelYear").getValue()).isInstanceOf(Integer.class);
            assertThat(car.getProperty("Price").getValue()).isInstanceOf(BigDecimal.class);
            assertThat(car.getProperty("Currency").getValue()).isInstanceOf(String.class);
        }
    }

    @Test
    void shouldVerifyAllManufacturersHaveRequiredProperties() throws ODataApplicationException {
        // Given
        EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
        when(edmEntitySet.getName()).thenReturn("Manufacturers");

        // When
        EntityCollection result = dataProvider.getEntityCollection(edmEntitySet);

        // Then
        for (Entity manufacturer : result.getEntities()) {
            assertThat(manufacturer.getProperty("Id")).isNotNull();
            assertThat(manufacturer.getProperty("Name")).isNotNull();
            assertThat(manufacturer.getProperty("Address")).isNotNull();
            
            // Verify data types
            assertThat(manufacturer.getProperty("Id").getValue()).isInstanceOf(Integer.class);
            assertThat(manufacturer.getProperty("Name").getValue()).isInstanceOf(String.class);
        }
    }
}
