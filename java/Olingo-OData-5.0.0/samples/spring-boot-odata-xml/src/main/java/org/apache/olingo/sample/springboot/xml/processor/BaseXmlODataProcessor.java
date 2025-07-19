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

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

/**
 * Base class for XML OData processors to reduce code duplication
 */
public abstract class BaseXmlODataProcessor {
    
    protected OData odata;
    protected ServiceMetadata serviceMetadata;
    
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }
    
    /**
     * Helper method to extract entity set from URI
     */
    protected EdmEntitySet getEntitySetFromUri(UriInfo uriInfo) {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        return uriResourceEntitySet.getEntitySet();
    }
    
    /**
     * Helper method to configure the response
     */
    protected void configureResponse(ODataResponse response, SerializerResult serializerResult, 
                                   ContentType responseFormat, HttpStatusCode statusCode) {
        response.setContent(serializerResult.getContent());
        response.setStatusCode(statusCode.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
    
    /**
     * Helper method to create a context URL for entity sets
     */
    protected ContextURL createContextUrl(EdmEntitySet edmEntitySet) {
        return ContextURL.with().entitySet(edmEntitySet).build();
    }
    
    /**
     * Helper method to create a context URL for entities
     */
    protected ContextURL createEntityContextUrl(EdmEntitySet edmEntitySet) {
        return ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
    }
}
