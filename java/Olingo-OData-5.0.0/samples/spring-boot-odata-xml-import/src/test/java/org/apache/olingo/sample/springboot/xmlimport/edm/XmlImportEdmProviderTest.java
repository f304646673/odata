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
package org.apache.olingo.sample.springboot.xmlimport.edm;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XmlImportEdmProviderTest {

    private XmlImportEdmProvider edmProvider;

    @BeforeEach
    void setUp() {
        edmProvider = new XmlImportEdmProvider();
    }

    @Test
    void testProviderInitialization() {
        assertThat(edmProvider).isNotNull();
    }

    @Test
    void testGetSchemas() throws ODataException {
        List<CsdlSchema> schemas = edmProvider.getSchemas();
        
        assertThat(schemas).isNotNull();
        assertThat(schemas).isNotEmpty();
        
        // Should have at least two schemas: Common and Demo
        assertThat(schemas.size()).isGreaterThanOrEqualTo(2);
        
        // Check if we have the expected namespaces
        boolean hasCommonNamespace = schemas.stream()
            .anyMatch(schema -> "OData.Demo.Common".equals(schema.getNamespace()));
        boolean hasDemoNamespace = schemas.stream()
            .anyMatch(schema -> "OData.Demo".equals(schema.getNamespace()));
            
        assertThat(hasCommonNamespace).isTrue();
        assertThat(hasDemoNamespace).isTrue();
    }

    @Test
    void testGetEntityContainer() throws ODataException {
        CsdlEntityContainer container = edmProvider.getEntityContainer();
        
        assertThat(container).isNotNull();
        assertThat(container.getName()).isEqualTo("Container");
        assertThat(container.getEntitySets()).isNotNull();
        assertThat(container.getEntitySets()).isNotEmpty();
        
        // Check if entity sets are included in service document
        for (CsdlEntitySet entitySet : container.getEntitySets()) {
            assertThat(entitySet.isIncludeInServiceDocument()).isTrue();
        }
    }

    @Test
    void testGetEntityContainerInfo() throws ODataException {
        CsdlEntityContainerInfo containerInfo = edmProvider.getEntityContainerInfo(null);
        
        assertThat(containerInfo).isNotNull();
        assertThat(containerInfo.getContainerName()).isNotNull();
        assertThat(containerInfo.getContainerName().getName()).isEqualTo("Container");
        assertThat(containerInfo.getContainerName().getNamespace()).isEqualTo("OData.Demo");
    }

    @Test
    void testGetEntityType() throws ODataException {
        // Test Car entity type
        FullQualifiedName carTypeName = new FullQualifiedName("OData.Demo", "Car");
        CsdlEntityType carType = edmProvider.getEntityType(carTypeName);
        
        assertThat(carType).isNotNull();
        assertThat(carType.getName()).isEqualTo("Car");
        assertThat(carType.getKey()).isNotNull();
        assertThat(carType.getProperties()).isNotNull();
        assertThat(carType.getProperties()).isNotEmpty();
        
        // Test Manufacturer entity type
        FullQualifiedName manufacturerTypeName = new FullQualifiedName("OData.Demo", "Manufacturer");
        CsdlEntityType manufacturerType = edmProvider.getEntityType(manufacturerTypeName);
        
        assertThat(manufacturerType).isNotNull();
        assertThat(manufacturerType.getName()).isEqualTo("Manufacturer");
        assertThat(manufacturerType.getKey()).isNotNull();
        assertThat(manufacturerType.getProperties()).isNotNull();
        assertThat(manufacturerType.getProperties()).isNotEmpty();
    }

    @Test
    void testGetComplexType() throws ODataException {
        // Test Address complex type
        FullQualifiedName addressTypeName = new FullQualifiedName("OData.Demo.Common", "Address");
        CsdlComplexType addressType = edmProvider.getComplexType(addressTypeName);
        
        assertThat(addressType).isNotNull();
        assertThat(addressType.getName()).isEqualTo("Address");
        assertThat(addressType.getProperties()).isNotNull();
        assertThat(addressType.getProperties()).isNotEmpty();
    }

    @Test
    void testGetEntitySet() throws ODataException {
        // Test Cars entity set
        CsdlEntitySet carsEntitySet = edmProvider.getEntitySet(
            new FullQualifiedName("OData.Demo", "Container"), "Cars");
        
        assertThat(carsEntitySet).isNotNull();
        assertThat(carsEntitySet.getName()).isEqualTo("Cars");
        assertThat(carsEntitySet.getType()).isNotNull();
        assertThat(carsEntitySet.isIncludeInServiceDocument()).isTrue();
        
        // Test Manufacturers entity set
        CsdlEntitySet manufacturersEntitySet = edmProvider.getEntitySet(
            new FullQualifiedName("OData.Demo", "Container"), "Manufacturers");
        
        assertThat(manufacturersEntitySet).isNotNull();
        assertThat(manufacturersEntitySet.getName()).isEqualTo("Manufacturers");
        assertThat(manufacturersEntitySet.getType()).isNotNull();
        assertThat(manufacturersEntitySet.isIncludeInServiceDocument()).isTrue();
    }

    @Test
    void testXmlFileLoading() {
        // This test verifies that the XML files can be loaded without exceptions
        assertDoesNotThrow(() -> {
            XmlImportEdmProvider testProvider = new XmlImportEdmProvider();
            testProvider.getSchemas();
        });
    }

    @Test
    void testCrossNamespaceReferences() throws ODataException {
        // Test that types from different namespaces can reference each other
        FullQualifiedName manufacturerTypeName = new FullQualifiedName("OData.Demo", "Manufacturer");
        CsdlEntityType manufacturerType = edmProvider.getEntityType(manufacturerTypeName);
        
        assertThat(manufacturerType).isNotNull();
        
        // Check if Manufacturer has a property that references Address complex type
        boolean hasAddressProperty = manufacturerType.getProperties().stream()
            .anyMatch(property -> property.getType().equals("OData.Demo.Common.Address"));
            
        assertThat(hasAddressProperty).isTrue();
    }

    @Test
    void testInvalidEntityType() throws ODataException {
        // Test with non-existent entity type
        FullQualifiedName invalidTypeName = new FullQualifiedName("Invalid", "Type");
        CsdlEntityType invalidType = edmProvider.getEntityType(invalidTypeName);
        
        assertThat(invalidType).isNull();
    }

    @Test
    void testInvalidComplexType() throws ODataException {
        // Test with non-existent complex type
        FullQualifiedName invalidTypeName = new FullQualifiedName("Invalid", "ComplexType");
        CsdlComplexType invalidType = edmProvider.getComplexType(invalidTypeName);
        
        assertThat(invalidType).isNull();
    }
}
