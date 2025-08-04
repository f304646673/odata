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
package org.apache.olingo.advanced.xmlparser.xml;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of XML reference extraction
 */
public class XmlReferenceExtractor implements IXmlReferenceExtractor {
    
    @Override
    public Set<String> extractReferencesFromXml(InputStream inputStream) throws Exception {
        Set<String> references = new HashSet<>();
        
        if (inputStream == null) {
            return references;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            // Find all edmx:Reference elements
            NodeList referenceNodes = doc.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edmx", "Reference");
            
            for (int i = 0; i < referenceNodes.getLength(); i++) {
                Element refElement = (Element) referenceNodes.item(i);
                String uri = refElement.getAttribute("Uri");
                if (uri != null && !uri.trim().isEmpty()) {
                    references.add(uri);
                }
            }
            
        } catch (Exception e) {
            // If XML parsing fails, fall back to empty set
            // Let other parts of the system handle the error
        }
        
        return references;
    }
}
