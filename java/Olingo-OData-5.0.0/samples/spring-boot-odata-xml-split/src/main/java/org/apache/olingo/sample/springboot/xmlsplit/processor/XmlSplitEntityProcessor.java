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
package org.apache.olingo.sample.springboot.xmlsplit.processor;

import java.util.List;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.sample.springboot.xmlsplit.data.XmlSplitDataProvider;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
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

/**
 * Entity processor for XML Split Sample
 */
public class XmlSplitEntityProcessor extends BaseXmlSplitODataProcessor implements EntityCollectionProcessor, EntityProcessor {
    
    private final XmlSplitDataProvider dataProvider;
    
    public XmlSplitEntityProcessor() {
        this.dataProvider = new XmlSplitDataProvider();
    }
    
    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response,
            UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        
        EdmEntitySet edmEntitySet = getEntitySet(uriInfo);
        
        EntityCollection entityCollection = switch (edmEntitySet.getName()) {
            case "Cars" -> dataProvider.getCars();
            case "Manufacturers" -> dataProvider.getManufacturers();
            default -> throw new ODataApplicationException("Entity set not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null);
        };
        
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = createEntityCollectionContextUrl(edmEntitySet);
        
        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
                .id(id)
                .contextURL(contextUrl)
                .build();
        
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, options);
        
        configureResponse(response, serializerResult, responseFormat);
    }
    
    @Override
    public void readEntity(ODataRequest request, ODataResponse response,
            UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        int id = Integer.parseInt(keyPredicates.get(0).getText());
        
        Entity entity = switch (edmEntitySet.getName()) {
            case "Cars" -> dataProvider.getCar(id);
            case "Manufacturers" -> dataProvider.getManufacturer(id);
            default -> throw new ODataApplicationException("Entity set not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null);
        };
        
        if (entity == null) {
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null);
        }
        
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = createEntityContextUrl(edmEntitySet);
        
        EntitySerializerOptions options = EntitySerializerOptions.with()
                .contextURL(contextUrl)
                .build();
        
        SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
        
        configureResponse(response, serializerResult, responseFormat);
    }
    
    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Create not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), null);
    }
    
    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Update not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), null);
    }
    
    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Delete not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), null);
    }
}
