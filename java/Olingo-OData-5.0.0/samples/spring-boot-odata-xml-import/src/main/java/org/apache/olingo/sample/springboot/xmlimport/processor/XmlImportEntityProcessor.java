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
package org.apache.olingo.sample.springboot.xmlimport.processor;

import java.util.List;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.sample.springboot.xmlimport.data.XmlImportDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class XmlImportEntityProcessor implements EntityCollectionProcessor, EntityProcessor {

    @Autowired
    private XmlImportDataProvider dataProvider;
    
    private OData odata;
    private ServiceMetadata serviceMetadata;
    
    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }
    
    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        EntityCollection entityCollection = getEntityCollection(edmEntitySet);
        
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        
        ContextURL contextUrl = ContextURL.with()
                .entitySet(edmEntitySet)
                .build();
        
        EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
                .contextURL(contextUrl)
                .build();
        
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, options);
        
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
    
    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        List<org.apache.olingo.server.api.uri.UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        
        Entity entity = getEntity(edmEntitySet, keyPredicates);
        
        if (entity == null) {
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null);
        }
        
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        
        ContextURL contextUrl = ContextURL.with()
                .entitySet(edmEntitySet)
                .build();
        
        EntitySerializerOptions options = EntitySerializerOptions.with()
                .contextURL(contextUrl)
                .build();
        
        SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
        
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
    
    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Create operation not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), null);
    }
    
    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Update operation not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), null);
    }
    
    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Delete operation not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), null);
    }
    
    private EntityCollection getEntityCollection(EdmEntitySet edmEntitySet) {
        String entitySetName = edmEntitySet.getName();
        
        switch (entitySetName) {
            case "Cars":
                return dataProvider.getCars();
            case "Manufacturers":
                return dataProvider.getManufacturers();
            default:
                return new EntityCollection();
        }
    }
    
    private Entity getEntity(EdmEntitySet edmEntitySet, List<org.apache.olingo.server.api.uri.UriParameter> keyPredicates) {
        String entitySetName = edmEntitySet.getName();
        
        int id = Integer.parseInt(keyPredicates.get(0).getText());
        
        switch (entitySetName) {
            case "Cars":
                return dataProvider.getCar(id);
            case "Manufacturers":
                return dataProvider.getManufacturer(id);
            default:
                return null;
        }
    }
}
