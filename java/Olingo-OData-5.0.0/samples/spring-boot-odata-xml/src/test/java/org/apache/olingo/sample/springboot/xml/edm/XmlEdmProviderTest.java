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
package org.apache.olingo.sample.springboot.xml.edm;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for XmlEdmProvider
 */
class XmlEdmProviderTest {

    private XmlEdmProvider edmProvider;

    @BeforeEach
    void setUp() {
        edmProvider = new XmlEdmProvider();
    }

    @Test
    void shouldLoadSchemasFromXml() throws Exception {
        // Given & When
        List<CsdlSchema> schemas = edmProvider.getSchemas();

        // Then
        assertThat(schemas).isNotEmpty();
        assertThat(schemas).hasSize(1);
        
        CsdlSchema schema = schemas.get(0);
        assertThat(schema.getNamespace()).isEqualTo("OData.Demo");
    }

    @Test
    void shouldProvideEntityTypes() throws Exception {
        // Given
        FullQualifiedName carType = new FullQualifiedName("OData.Demo", "Car");
        FullQualifiedName manufacturerType = new FullQualifiedName("OData.Demo", "Manufacturer");

        // When
        CsdlEntityType carEntityType = edmProvider.getEntityType(carType);
        CsdlEntityType manufacturerEntityType = edmProvider.getEntityType(manufacturerType);

        // Then
        assertThat(carEntityType).isNotNull();
        assertThat(carEntityType.getName()).isEqualTo("Car");
        assertThat(carEntityType.getProperties()).isNotEmpty();

        assertThat(manufacturerEntityType).isNotNull();
        assertThat(manufacturerEntityType.getName()).isEqualTo("Manufacturer");
        assertThat(manufacturerEntityType.getProperties()).isNotEmpty();
    }

    @Test
    void shouldProvideEntityContainer() throws Exception {
        // Given & When
        CsdlEntityContainer container = edmProvider.getEntityContainer();

        // Then
        assertThat(container).isNotNull();
        assertThat(container.getName()).isEqualTo("Container");
        assertThat(container.getEntitySets()).isNotEmpty();
        assertThat(container.getEntitySets()).hasSize(2);

        // Verify entity sets are included in service document
        for (CsdlEntitySet entitySet : container.getEntitySets()) {
            assertThat(entitySet.isIncludeInServiceDocument()).isTrue();
        }
    }

    @Test
    void shouldProvideEntitySets() throws Exception {
        // Given
        FullQualifiedName containerName = new FullQualifiedName("OData.Demo", "Container");

        // When
        CsdlEntitySet carsEntitySet = edmProvider.getEntitySet(containerName, "Cars");
        CsdlEntitySet manufacturersEntitySet = edmProvider.getEntitySet(containerName, "Manufacturers");

        // Then
        assertThat(carsEntitySet).isNotNull();
        assertThat(carsEntitySet.getName()).isEqualTo("Cars");
        assertThat(carsEntitySet.getType().toString()).isEqualTo("OData.Demo.Car");
        assertThat(carsEntitySet.isIncludeInServiceDocument()).isTrue();

        assertThat(manufacturersEntitySet).isNotNull();
        assertThat(manufacturersEntitySet.getName()).isEqualTo("Manufacturers");
        assertThat(manufacturersEntitySet.getType().toString()).isEqualTo("OData.Demo.Manufacturer");
        assertThat(manufacturersEntitySet.isIncludeInServiceDocument()).isTrue();
    }

    @Test
    void shouldProvideEntityContainerInfo() throws Exception {
        // Given
        FullQualifiedName containerName = new FullQualifiedName("OData.Demo", "Container");

        // When
        CsdlEntityContainerInfo containerInfo = edmProvider.getEntityContainerInfo(containerName);

        // Then
        assertThat(containerInfo).isNotNull();
        assertThat(containerInfo.getContainerName()).isEqualTo(containerName);
    }

    @Test
    void shouldReturnNullForNonExistentEntityType() throws Exception {
        // Given
        FullQualifiedName nonExistentType = new FullQualifiedName("OData.Demo", "NonExistent");

        // When
        CsdlEntityType entityType = edmProvider.getEntityType(nonExistentType);

        // Then
        assertThat(entityType).isNull();
    }

    @Test
    void shouldReturnNullForNonExistentEntitySet() throws Exception {
        // Given
        FullQualifiedName containerName = new FullQualifiedName("OData.Demo", "Container");

        // When
        CsdlEntitySet entitySet = edmProvider.getEntitySet(containerName, "NonExistent");

        // Then
        assertThat(entitySet).isNull();
    }
}
