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
package org.apache.olingo.sample.springboot.xml.processor;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.sample.springboot.xml.data.XmlBasedDataProvider;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * XML-based Entity Processor for Spring Boot OData
 * 
 * This processor handles entity and entity collection requests
 * using the XML-based EDM provider and data provider.
 */
public class XmlBasedEntityProcessor implements EntityCollectionProcessor, EntityProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(XmlBasedEntityProcessor.class);

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private XmlBasedDataProvider dataProvider;

    /**
     * Constructor
     */
    public XmlBasedEntityProcessor(XmlBasedDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        LOG.info("XML-based Entity Processor initialized");
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
        LOG.debug("Processor initialized with OData context");
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, 
                                   UriInfo uriInfo, ContentType responseFormat) 
                                   throws ODataApplicationException, ODataLibraryException {
        
        LOG.debug("Processing entity collection request");

        // Get the entity set from the URI
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        LOG.debug("Reading entity collection: {}", edmEntitySet.getName());

        // Get entity collection from data provider
        EntityCollection entityCollection = dataProvider.getEntityCollection(edmEntitySet);

        // Create context URL
        ContextURL contextUrl = ContextURL.with()
            .entitySet(edmEntitySet)
            .build();

        // Create serializer options
        EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
            .contextURL(contextUrl)
            .build();

        // Serialize the entity collection
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializerResult = serializer.entityCollection(
            serviceMetadata, edmEntitySet.getEntityType(), entityCollection, options);

        // Set response
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        LOG.info("Successfully processed entity collection request for: {}", edmEntitySet.getName());
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, 
                          UriInfo uriInfo, ContentType responseFormat) 
                          throws ODataApplicationException, ODataLibraryException {
        
        LOG.debug("Processing entity request");

        // Get the entity set from the URI
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // Get the key values
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        List<String> keyValues = keyPredicates.stream()
            .map(UriParameter::getText)
            .toList();

        LOG.debug("Reading entity: {} with key: {}", edmEntitySet.getName(), keyValues);

        // Get entity from data provider
        Entity entity = dataProvider.getEntity(edmEntitySet, keyValues);

        if (entity == null) {
            throw new ODataApplicationException("Entity not found.",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        // Create context URL
        ContextURL contextUrl = ContextURL.with()
            .entitySet(edmEntitySet)
            .suffix(ContextURL.Suffix.ENTITY)
            .build();

        // Create serializer options
        EntitySerializerOptions options = EntitySerializerOptions.with()
            .contextURL(contextUrl)
            .build();

        // Serialize the entity
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializerResult = serializer.entity(
            serviceMetadata, edmEntitySet.getEntityType(), entity, options);

        // Set response
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        LOG.info("Successfully processed entity request for: {} with key: {}", edmEntitySet.getName(), keyValues);
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, 
                           UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) 
                           throws ODataApplicationException, ODataLibraryException {
        
        LOG.debug("Processing create entity request");

        // Get the entity set from the URI
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        LOG.debug("Creating entity in set: {}", edmEntitySet.getName());

        // Deserialize the request body
        ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(request.getBody(), edmEntityType);
        Entity requestEntity = result.getEntity();

        // Create entity using data provider
        Entity createdEntity = dataProvider.createEntity(edmEntitySet, requestEntity);

        // Create context URL
        ContextURL contextUrl = ContextURL.with()
            .entitySet(edmEntitySet)
            .suffix(ContextURL.Suffix.ENTITY)
            .build();

        // Create serializer options
        EntitySerializerOptions options = EntitySerializerOptions.with()
            .contextURL(contextUrl)
            .build();

        // Serialize the created entity
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializerResult = serializer.entity(
            serviceMetadata, edmEntityType, createdEntity, options);

        // Set response
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        LOG.info("Successfully created entity in set: {}", edmEntitySet.getName());
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, 
                           UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) 
                           throws ODataApplicationException, ODataLibraryException {
        
        LOG.debug("Processing update entity request");

        // Get the entity set from the URI
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // Get the key values
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        List<String> keyValues = keyPredicates.stream()
            .map(UriParameter::getText)
            .toList();

        LOG.debug("Updating entity: {} with key: {}", edmEntitySet.getName(), keyValues);

        // Deserialize the request body
        ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(request.getBody(), edmEntityType);
        Entity requestEntity = result.getEntity();

        // Update entity using data provider
        dataProvider.updateEntity(edmEntitySet, keyValues, requestEntity);

        // Set response
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

        LOG.info("Successfully updated entity: {} with key: {}", edmEntitySet.getName(), keyValues);
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) 
                           throws ODataApplicationException, ODataLibraryException {
        
        LOG.debug("Processing delete entity request");

        // Get the entity set from the URI
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // Get the key values
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        List<String> keyValues = keyPredicates.stream()
            .map(UriParameter::getText)
            .toList();

        LOG.debug("Deleting entity: {} with key: {}", edmEntitySet.getName(), keyValues);

        // Delete entity using data provider
        dataProvider.deleteEntity(edmEntitySet, keyValues);

        // Set response
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

        LOG.info("Successfully deleted entity: {} with key: {}", edmEntitySet.getName(), keyValues);
    }
}
