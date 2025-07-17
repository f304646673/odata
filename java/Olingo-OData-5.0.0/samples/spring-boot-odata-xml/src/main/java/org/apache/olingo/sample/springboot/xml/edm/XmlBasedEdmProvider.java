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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.ex.ODataException;

import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

/**
 * XML-based EDM Provider for Spring Boot OData
 * 
 * This provider loads the Entity Data Model (EDM) from an XML file
 * instead of programmatically defining it in Java code.
 */
public class XmlBasedEdmProvider extends CsdlAbstractEdmProvider {


    // Constants
    public static final String NAMESPACE = "OData.Demo";
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types
    public static final String ET_CAR_NAME = "Car";
    public static final FullQualifiedName ET_CAR = new FullQualifiedName(NAMESPACE, ET_CAR_NAME);
    public static final String ET_MANUFACTURER_NAME = "Manufacturer";
    public static final FullQualifiedName ET_MANUFACTURER = new FullQualifiedName(NAMESPACE, ET_MANUFACTURER_NAME);

    // Complex Types
    public static final String CT_ADDRESS_NAME = "Address";
    public static final FullQualifiedName CT_ADDRESS = new FullQualifiedName(NAMESPACE, CT_ADDRESS_NAME);

    // Entity Sets
    public static final String ES_CARS_NAME = "Cars";
    public static final String ES_MANUFACTURERS_NAME = "Manufacturers";

    private Document xmlDocument;
    private final String xmlFilePath;

    public XmlBasedEdmProvider(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
        loadXmlMetadata();
    }

    /**
     * Load XML metadata from file
     */
    private void loadXmlMetadata() {
        try {
            
            ClassPathResource resource = new ClassPathResource(xmlFilePath);
            InputStream inputStream = resource.getInputStream();
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            xmlDocument = builder.parse(inputStream);
            xmlDocument.getDocumentElement().normalize();
            
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load XML metadata", e);
        }
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
        
        if (ET_CAR.equals(entityTypeName)) {
            return getCarEntityType();
        } else if (ET_MANUFACTURER.equals(entityTypeName)) {
            return getManufacturerEntityType();
        }
        
        return null;
    }

    /**
     * Get Car entity type from XML
     */
    private CsdlEntityType getCarEntityType() {
        // Create properties
        CsdlProperty id = new CsdlProperty().setName("Id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()).setNullable(false);
        CsdlProperty model = new CsdlProperty().setName("Model").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setMaxLength(60);
        CsdlProperty modelYear = new CsdlProperty().setName("ModelYear").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty price = new CsdlProperty().setName("Price").setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName()).setPrecision(19).setScale(4);
        CsdlProperty currency = new CsdlProperty().setName("Currency").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setMaxLength(3);

        // Create key
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("Id");

        // Create navigation property
        CsdlNavigationProperty manufacturerNavProp = new CsdlNavigationProperty()
            .setName("Manufacturer")
            .setType(ET_MANUFACTURER);

        // Configure entity type
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_CAR_NAME);
        entityType.setProperties(Arrays.asList(id, model, modelYear, price, currency));
        entityType.setKey(Arrays.asList(propertyRef));
        entityType.setNavigationProperties(Arrays.asList(manufacturerNavProp));

        return entityType;
    }

    /**
     * Get Manufacturer entity type from XML
     */
    private CsdlEntityType getManufacturerEntityType() {
        // Create properties
        CsdlProperty id = new CsdlProperty().setName("Id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()).setNullable(false);
        CsdlProperty name = new CsdlProperty().setName("Name").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setMaxLength(60);
        CsdlProperty address = new CsdlProperty().setName("Address").setType(CT_ADDRESS);

        // Create key
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("Id");

        // Create navigation property
        CsdlNavigationProperty carsNavProp = new CsdlNavigationProperty()
            .setName("Cars")
            .setType(ET_CAR)
            .setCollection(true);

        // Configure entity type
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_MANUFACTURER_NAME);
        entityType.setProperties(Arrays.asList(id, name, address));
        entityType.setKey(Arrays.asList(propertyRef));
        entityType.setNavigationProperties(Arrays.asList(carsNavProp));

        return entityType;
    }

    @Override
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
        
        if (CT_ADDRESS.equals(complexTypeName)) {
            return getAddressComplexType();
        }
        
        return null;
    }

    /**
     * Get Address complex type from XML
     */
    private CsdlComplexType getAddressComplexType() {
        // Create properties
        CsdlProperty street = new CsdlProperty().setName("Street").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setMaxLength(60);
        CsdlProperty city = new CsdlProperty().setName("City").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setMaxLength(60);
        CsdlProperty zipCode = new CsdlProperty().setName("ZipCode").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setMaxLength(10);
        CsdlProperty country = new CsdlProperty().setName("Country").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setMaxLength(60);

        // Configure complex type
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(CT_ADDRESS_NAME);
        complexType.setProperties(Arrays.asList(street, city, zipCode, country));

        return complexType;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        
        if (CONTAINER.equals(entityContainer)) {
            if (ES_CARS_NAME.equals(entitySetName)) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(ES_CARS_NAME);
                entitySet.setType(ET_CAR);
                
                // Add navigation property bindings
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setPath("Manufacturer");
                navPropBinding.setTarget(ES_MANUFACTURERS_NAME);
                entitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding));
                
                return entitySet;
                
            } else if (ES_MANUFACTURERS_NAME.equals(entitySetName)) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(ES_MANUFACTURERS_NAME);
                entitySet.setType(ET_MANUFACTURER);
                
                // Add navigation property bindings
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setPath("Cars");
                navPropBinding.setTarget(ES_CARS_NAME);
                entitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding));
                
                return entitySet;
            }
        }
        
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        
        // Create entity sets
        CsdlEntitySet carsEntitySet = new CsdlEntitySet();
        carsEntitySet.setName(ES_CARS_NAME);
        carsEntitySet.setType(ET_CAR);
        
        CsdlEntitySet manufacturersEntitySet = new CsdlEntitySet();
        manufacturersEntitySet.setName(ES_MANUFACTURERS_NAME);
        manufacturersEntitySet.setType(ET_MANUFACTURER);

        // Create entity container
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(Arrays.asList(carsEntitySet, manufacturersEntitySet));

        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        
        // Create schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // Add entity types
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        entityTypes.add(getEntityType(ET_CAR));
        entityTypes.add(getEntityType(ET_MANUFACTURER));
        schema.setEntityTypes(entityTypes);

        // Add complex types
        List<CsdlComplexType> complexTypes = new ArrayList<>();
        complexTypes.add(getComplexType(CT_ADDRESS));
        schema.setComplexTypes(complexTypes);

        // Add entity container
        schema.setEntityContainer(getEntityContainer());

        return Arrays.asList(schema);
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        
        if (entityContainerName == null || CONTAINER.equals(entityContainerName)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }
        
        return null;
    }

    @Override
    public List<CsdlAction> getActions(FullQualifiedName actionName) throws ODataException {
        // No actions defined in XML - return empty list
        return new ArrayList<>();
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName functionName) throws ODataException {
        // No functions defined in XML - return empty list  
        return new ArrayList<>();
    }

    @Override
    public CsdlTerm getTerm(FullQualifiedName termName) throws ODataException {
        // No terms defined in XML - return null
        return null;
    }

    @Override
    public CsdlAnnotations getAnnotationsGroup(FullQualifiedName targetName, String qualifier) throws ODataException {
        // No annotations defined in XML - return null
        return null;
    }

    @Override
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName typeDefinitionName) throws ODataException {
        // No type definitions defined in XML - return null
        return null;
    }

    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) throws ODataException {
        // No enum types defined in XML - return null
        return null;
    }
}
