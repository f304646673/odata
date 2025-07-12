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

import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
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
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.sample.springboot.data.SpringBootDataProvider;
import org.apache.olingo.sample.springboot.edm.SpringBootEdmProvider;

/**
 * Spring Boot Cars Processor
 * 
 * This processor handles OData requests for the Car entity set.
 * It's inspired by the original CarsProcessor but adapted for Spring Boot environment.
 */
public class SpringBootCarsProcessor implements EntityCollectionProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private final SpringBootDataProvider dataProvider;

    public SpringBootCarsProcessor(SpringBootDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, 
            UriInfo uriInfo, ContentType responseFormat) 
            throws ODataApplicationException, ODataLibraryException {

        // 1. Retrieve the requested EntitySet from the uriInfo object
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2. Fetch the data from backend for the requested EntitySetName
        EntityCollection entitySet = getData(edmEntitySet);

        // 3. Create a serializer based on the requested format (json)
        ODataSerializer serializer = odata.createSerializer(responseFormat);

        // 4. Configure the serializer
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = ContextURL.with()
                .entitySet(edmEntitySet)
                .build();

        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
                .id(id)
                .contextURL(contextUrl)
                .build();

        // 5. Serialize the content
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, 
                edmEntityType, entitySet, options);

        // 6. Configure the response object
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    /**
     * Fetch data from the data provider and convert to OData format
     */
    private EntityCollection getData(EdmEntitySet edmEntitySet) {
        EntityCollection entityCollection = new EntityCollection();

        // Check which entity set is requested
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
                        
                // Note: setId expects URI, but for simplicity we'll let the framework handle ID generation
                entityCollection.getEntities().add(entity);
            }
        }

        return entityCollection;
    }
}
