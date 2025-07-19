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
package org.apache.olingo.sample.springboot.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.sample.springboot.data.SpringBootDataProvider;
import org.apache.olingo.sample.springboot.edm.SpringBootEdmProvider;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

/**
 * Spring Boot Cars Processor
 * 
 * This processor handles OData requests for the Car entity set in a Spring Boot environment.
 * It implements multiple processor interfaces to support:
 * - Entity collections (readEntityCollection)
 * - Single entities (readEntity)  
 * - Primitive properties (readPrimitive, readPrimitiveValue)
 * - Complex properties (readComplex)
 * 
 * This implementation is based on the original CarsProcessor but adapted for Spring Boot
 * with the SpringBootDataProvider for data access.
 */
public class SpringBootCarsProcessor extends BaseODataProcessor implements EntityProcessor,
        PrimitiveValueProcessor, ComplexProcessor, CountEntityCollectionProcessor {

    private final SpringBootDataProvider dataProvider;

    /**
     * Constructor with SpringBootDataProvider injection
     * @param dataProvider The data provider for car data
     */
    public SpringBootCarsProcessor(final SpringBootDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    // ==================== EntityCollectionProcessor Methods ====================
    
    @Override
    public void readEntityCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
            final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
        
        // First we have to figure out which entity set to use
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

        // Second we fetch the data for this specific entity set from the data provider
        EntityCollection entityCollection = readEntityCollectionData(edmEntitySet);

        // Next we create a serializer based on the requested format
        ODataSerializer serializer = odata.createSerializer(requestedContentType);

        // Now the content is serialized using the serializer with query options support
        final ExpandOption expand = uriInfo.getExpandOption();
        final SelectOption select = uriInfo.getSelectOption();
        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        
        InputStream serializedContent = serializer.entityCollection(serviceMetadata, edmEntitySet.getEntityType(), entityCollection,
                EntityCollectionSerializerOptions.with()
                        .id(id)
                        .contextURL(isODataMetadataNone(requestedContentType) ? null :
                                getContextUrl(edmEntitySet, false, expand, select, null))
                        .count(uriInfo.getCountOption())
                        .expand(expand).select(select)
                        .build()).getContent();

        // Finally we set the response data, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
    }

    @Override
    public void countEntityCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
            throws ODataApplicationException, SerializerException {
        
        // First we have to figure out which entity set to use
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

        // Second we fetch the data for this specific entity set from the data provider and count it
        EntityCollection entityCollection = readEntityCollectionData(edmEntitySet);
        int count = entityCollection.getEntities().size();

        // Return the count as plain text
        InputStream serializedContent = new ByteArrayInputStream(String.valueOf(count).getBytes(Charset.forName("UTF-8")));
        
        // Set the response
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN.toContentTypeString());
    }

    // ==================== EntityProcessor Methods ====================
    
    @Override
    public void readEntity(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
            final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
        
        // First we have to figure out which entity set the requested entity is in
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

        // Next we fetch the requested entity from the database
        Entity entity;
        try {
            entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
        }

        if (entity == null) {
            // If no entity was found for the given key we throw an exception.
            throw new ODataApplicationException("No entity found for this key", HttpStatusCode.NOT_FOUND
                    .getStatusCode(), Locale.ENGLISH);
        } else {
            // If an entity was found we proceed by serializing it and sending it to the client.
            ODataSerializer serializer = odata.createSerializer(requestedContentType);
            final ExpandOption expand = uriInfo.getExpandOption();
            final SelectOption select = uriInfo.getSelectOption();
            InputStream serializedContent = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), entity,
                    EntitySerializerOptions.with()
                            .contextURL(isODataMetadataNone(requestedContentType) ? null :
                                    getContextUrl(edmEntitySet, true, expand, select, null))
                            .expand(expand).select(select)
                            .build()).getContent();
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
        }
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                             ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Entity create is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void updateEntity(final ODataRequest request, final ODataResponse response,
                             final UriInfo uriInfo, final ContentType requestFormat,
                             final ContentType responseFormat)
            throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Entity update is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException {
        throw new ODataApplicationException("Entity delete is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    // ==================== PrimitiveProcessor Methods ====================
    
    @Override
    public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
            throws ODataApplicationException, SerializerException {
        readProperty(response, uriInfo, format, false);
    }

    @Override
    public void updatePrimitive(final ODataRequest request, final ODataResponse response,
                                final UriInfo uriInfo, final ContentType requestFormat,
                                final ContentType responseFormat)
            throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Primitive property update is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws
            ODataApplicationException {
        throw new ODataApplicationException("Primitive property delete is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    // ==================== PrimitiveValueProcessor Methods ====================
    
    @Override
    public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
            throws ODataApplicationException, SerializerException {
        
        // First we have to figure out which entity set the requested entity is in
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
        
        // Next we fetch the requested entity from the database
        final Entity entity;
        try {
            entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
        }
        
        if (entity == null) {
            // If no entity was found for the given key we throw an exception.
            throw new ODataApplicationException("No entity found for this key", HttpStatusCode.NOT_FOUND
                    .getStatusCode(), Locale.ENGLISH);
        } else {
            // Next we get the property value from the entity and pass the value to serialization
            UriResourceProperty uriProperty = (UriResourceProperty) uriInfo
                    .getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
            EdmProperty edmProperty = uriProperty.getProperty();
            Property property = entity.getProperty(edmProperty.getName());
            if (property == null) {
                throw new ODataApplicationException("No property found", HttpStatusCode.NOT_FOUND
                        .getStatusCode(), Locale.ENGLISH);
            } else {
                if (property.getValue() == null) {
                    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
                } else {
                    String value = String.valueOf(property.getValue());
                    ByteArrayInputStream serializerContent = new ByteArrayInputStream(
                            value.getBytes(Charset.forName("UTF-8")));
                    response.setContent(serializerContent);
                    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
                    response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN.toContentTypeString());
                }
            }
        }
    }

    @Override
    public void updatePrimitiveValue(final ODataRequest request, ODataResponse response,
            final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Primitive property update is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void deletePrimitiveValue(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Primitive property delete is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    // ==================== ComplexProcessor Methods ====================
    
    @Override
    public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
            throws ODataApplicationException, SerializerException {
        readProperty(response, uriInfo, format, true);
    }

    @Override
    public void updateComplex(final ODataRequest request, final ODataResponse response,
                              final UriInfo uriInfo, final ContentType requestFormat,
                              final ContentType responseFormat)
            throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Complex property update is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
            throws ODataApplicationException {
        throw new ODataApplicationException("Complex property delete is not supported yet.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    // ==================== Private Helper Methods ====================
    
    /**
     * Read entity collection data from SpringBootDataProvider and convert to OData format
     */
    private EntityCollection readEntityCollectionData(EdmEntitySet edmEntitySet) {
        EntityCollection entityCollection = new EntityCollection();

        // Check which entity set is requested (in our case, it should be Cars)
        if (SpringBootEdmProvider.ES_CARS_NAME.equals(edmEntitySet.getName())) {
            List<Map<String, Object>> cars = dataProvider.getAllCars();
            
            for (Map<String, Object> carData : cars) {
                Entity entity = new Entity()
                        .addProperty(new Property(null, "Id", ValueType.PRIMITIVE, carData.get("Id")))
                        .addProperty(new Property(null, "Brand", ValueType.PRIMITIVE, carData.get("Brand")))
                        .addProperty(new Property(null, "Model", ValueType.PRIMITIVE, carData.get("Model")))
                        .addProperty(new Property(null, "Color", ValueType.PRIMITIVE, carData.get("Color")))
                        .addProperty(new Property(null, "Year", ValueType.PRIMITIVE, carData.get("Year")))
                        .addProperty(new Property(null, "Price", ValueType.PRIMITIVE, carData.get("Price")));
                        
                entityCollection.getEntities().add(entity);
            }
        }

        return entityCollection;
    }

    /**
     * Read property value (primitive or complex) from entity
     */
    private void readProperty(ODataResponse response, UriInfo uriInfo, ContentType contentType,
            boolean complex) throws ODataApplicationException, SerializerException {
        
        // To read a property we have to first get the entity out of the entity set
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
        Entity entity;
        try {
            entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(),
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
        }

        if (entity == null) {
            // If no entity was found for the given key we throw an exception.
            throw new ODataApplicationException("No entity found for this key",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        } else {
            // Next we get the property value from the entity and pass the value to serialization
            UriResourceProperty uriProperty = (UriResourceProperty) uriInfo
                    .getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
            EdmProperty edmProperty = uriProperty.getProperty();
            Property property = entity.getProperty(edmProperty.getName());
            if (property == null) {
                throw new ODataApplicationException("No property found",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
            } else {
                if (property.getValue() == null) {
                    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
                } else {
                    ODataSerializer serializer = odata.createSerializer(contentType);
                    final ContextURL contextURL = isODataMetadataNone(contentType) ? null :
                            getContextUrl(edmEntitySet, true, null, null, edmProperty.getName());
                    InputStream serializerContent = complex ?
                            serializer.complex(serviceMetadata, (EdmComplexType) edmProperty.getType(), property,
                                    ComplexSerializerOptions.with().contextURL(contextURL).build()).getContent() :
                            serializer.primitive(serviceMetadata, (EdmPrimitiveType) edmProperty.getType(), property,
                                    PrimitiveSerializerOptions.with()
                                            .contextURL(contextURL)
                                            .scale(edmProperty.getScale())
                                            .nullable(edmProperty.isNullable())
                                            .precision(edmProperty.getPrecision())
                                            .maxLength(edmProperty.getMaxLength())
                                            .unicode(edmProperty.isUnicode()).build()).getContent();
                    response.setContent(serializerContent);
                    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
                    response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
                }
            }
        }
    }

    /**
     * Read entity internal helper method
     * This method extracts key values and retrieves the specific entity
     */
    private Entity readEntityInternal(final UriInfoResource uriInfo, final EdmEntitySet entitySet) throws Exception {
        // Extract the entity set resource and key predicates
        final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        
        // For demonstration purposes, we'll return the first entity if no key predicates
        // In a real implementation, you would use resourceEntitySet.getKeyPredicates() 
        // to find the specific entity by ID
        if (resourceEntitySet.getKeyPredicates().isEmpty()) {
            // Return first entity for demo purposes
            EntityCollection entityCollection = readEntityCollectionData(entitySet);
            if (!entityCollection.getEntities().isEmpty()) {
                return entityCollection.getEntities().get(0);
            }
            return null;
        } else {
            // TODO: Implement proper key predicate handling
            // For now, return first entity as placeholder
            EntityCollection entityCollection = readEntityCollectionData(entitySet);
            if (!entityCollection.getEntities().isEmpty()) {
                return entityCollection.getEntities().get(0);
            }
            return null;
        }
    }
}
