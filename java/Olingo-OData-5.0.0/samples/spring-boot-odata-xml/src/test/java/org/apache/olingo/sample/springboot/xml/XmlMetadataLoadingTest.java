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
package org.apache.olingo.sample.springboot.xml;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.sample.springboot.xml.edm.XmlEdmProvider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for XML metadata loading
 */
class XmlMetadataLoadingTest {

    private XmlEdmProvider edmProvider;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        edmProvider = new XmlEdmProvider();
    }

    @Test
    void shouldLoadXmlMetadataSuccessfully() throws Exception {
        // When
        List<CsdlSchema> schemas = edmProvider.getSchemas();

        // Then
        assertThat(schemas).isNotEmpty();
        assertThat(schemas).hasSize(1);

        CsdlSchema schema = schemas.get(0);
        assertThat(schema.getNamespace()).isEqualTo("OData.Demo");
    }

    @Test
    void shouldLoadEntityTypesFromXml() throws Exception {
        // Given
        FullQualifiedName carType = new FullQualifiedName("OData.Demo", "Car");
        FullQualifiedName manufacturerType = new FullQualifiedName("OData.Demo", "Manufacturer");

        // When
        CsdlEntityType carEntityType = edmProvider.getEntityType(carType);
        CsdlEntityType manufacturerEntityType = edmProvider.getEntityType(manufacturerType);

        // Then
        assertThat(carEntityType).isNotNull();
        assertThat(carEntityType.getName()).isEqualTo("Car");
        assertThat(carEntityType.getKey()).isNotNull();
        assertThat(carEntityType.getProperties()).isNotEmpty();

        assertThat(manufacturerEntityType).isNotNull();
        assertThat(manufacturerEntityType.getName()).isEqualTo("Manufacturer");
        assertThat(manufacturerEntityType.getKey()).isNotNull();
        assertThat(manufacturerEntityType.getProperties()).isNotEmpty();
    }

    @Test
    void shouldLoadEntityContainerFromXml() throws Exception {
        // When
        CsdlEntityContainer container = edmProvider.getEntityContainer();

        // Then
        assertThat(container).isNotNull();
        assertThat(container.getName()).isEqualTo("Container");
        assertThat(container.getEntitySets()).isNotEmpty();
        assertThat(container.getEntitySets()).hasSize(2);

        // Check that entity sets are configured for service document
        for (CsdlEntitySet entitySet : container.getEntitySets()) {
            assertThat(entitySet.isIncludeInServiceDocument()).isTrue();
        }
    }

    @Test
    void shouldLoadEntitySetsFromXml() throws Exception {
        // Given
        FullQualifiedName containerName = new FullQualifiedName("OData.Demo", "Container");

        // When
        CsdlEntitySet carsEntitySet = edmProvider.getEntitySet(containerName, "Cars");
        CsdlEntitySet manufacturersEntitySet = edmProvider.getEntitySet(containerName, "Manufacturers");

        // Then
        assertThat(carsEntitySet).isNotNull();
        assertThat(carsEntitySet.getName()).isEqualTo("Cars");
        assertThat(carsEntitySet.getType()).isEqualTo("OData.Demo.Car");
        assertThat(carsEntitySet.isIncludeInServiceDocument()).isTrue();

        assertThat(manufacturersEntitySet).isNotNull();
        assertThat(manufacturersEntitySet.getName()).isEqualTo("Manufacturers");
        assertThat(manufacturersEntitySet.getType()).isEqualTo("OData.Demo.Manufacturer");
        assertThat(manufacturersEntitySet.isIncludeInServiceDocument()).isTrue();
    }

    @Test
    void shouldValidateCarEntityStructure() throws Exception {
        // Given
        FullQualifiedName carType = new FullQualifiedName("OData.Demo", "Car");

        // When
        CsdlEntityType carEntityType = edmProvider.getEntityType(carType);

        // Then
        assertThat(carEntityType).isNotNull();
        assertThat(carEntityType.getProperties()).hasSize(4);

        // Verify key properties
        assertThat(carEntityType.getKey()).hasSize(1);
        assertThat(carEntityType.getKey().get(0).getName()).isEqualTo("Id");

        // Verify property names
        List<String> propertyNames = carEntityType.getProperties().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());
        assertThat(propertyNames).containsExactlyInAnyOrder("Id", "Model", "Price", "Year");
    }

    @Test
    void shouldValidateManufacturerEntityStructure() throws Exception {
        // Given
        FullQualifiedName manufacturerType = new FullQualifiedName("OData.Demo", "Manufacturer");

        // When
        CsdlEntityType manufacturerEntityType = edmProvider.getEntityType(manufacturerType);

        // Then
        assertThat(manufacturerEntityType).isNotNull();
        assertThat(manufacturerEntityType.getProperties()).hasSize(4);

        // Verify key properties
        assertThat(manufacturerEntityType.getKey()).hasSize(1);
        assertThat(manufacturerEntityType.getKey().get(0).getName()).isEqualTo("Id");

        // Verify property names
        List<String> propertyNames = manufacturerEntityType.getProperties().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());
        assertThat(propertyNames).containsExactlyInAnyOrder("Id", "Name", "Founded", "Address");
    }
}