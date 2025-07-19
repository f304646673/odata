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

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.sample.springboot.xml.data.XmlDataProvider;
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
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

/**
 *  XML Entity Processor for Spring Boot OData
 * 
 * This processor handles entity and entity collection requests using Olingo's native APIs.
 */
public class XmlEntityProcessor extends BaseXmlODataProcessor implements EntityCollectionProcessor, EntityProcessor {

    private final XmlDataProvider dataProvider;

    public XmlEntityProcessor(XmlDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        EdmEntitySet edmEntitySet = getEntitySetFromUri(uriInfo);
        EntityCollection entityCollection = dataProvider.getEntityCollection(edmEntitySet);

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        ContextURL contextUrl = createContextUrl(edmEntitySet);
        
        EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
            .contextURL(contextUrl)
            .build();
        
        SerializerResult serializerResult = serializer.entityCollection(
            serviceMetadata, edmEntitySet.getEntityType(), entityCollection, options);

        configureResponse(response, serializerResult, responseFormat, HttpStatusCode.OK);
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        EdmEntitySet edmEntitySet = getEntitySetFromUri(uriInfo);
        
        // Get the key values
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        List<String> keyValues = uriResourceEntitySet.getKeyPredicates().stream()
            .map(keyPredicate -> keyPredicate.getText())
            .toList();

        Entity entity = dataProvider.getEntity(edmEntitySet, keyValues);

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        ContextURL contextUrl = createEntityContextUrl(edmEntitySet);
        
        EntitySerializerOptions options = EntitySerializerOptions.with()
            .contextURL(contextUrl)
            .build();
        
        SerializerResult serializerResult = serializer.entity(
            serviceMetadata, edmEntitySet.getEntityType(), entity, options);

        configureResponse(response, serializerResult, responseFormat, HttpStatusCode.OK);
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("CREATE operations are not yet implemented in this sample.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("UPDATE operations are not yet implemented in this sample.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("DELETE operations are not yet implemented in this sample.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }
}
