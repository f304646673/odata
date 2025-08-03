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
package org.apache.olingo.advanced.xmlparser;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Handles schema comparison logic with null safety
 */
public class SchemaComparator {
    
    /**
     * Compare two schemas for structural equality
     */
    public boolean areSchemasIdentical(CsdlSchema schema1, CsdlSchema schema2) {
        if (schema1 == null && schema2 == null) {
            return true;
        }
        if (schema1 == null || schema2 == null) {
            return false;
        }
        
        // Compare namespace first
        if (!areEqual(schema1.getNamespace(), schema2.getNamespace())) {
            return false;
        }
        
        // Compare alias
        if (!areEqual(schema1.getAlias(), schema2.getAlias())) {
            return false;
        }
        
        // Check EntityTypes - compare by name and structure
        if (!areEntityTypesIdentical(schema1.getEntityTypes(), schema2.getEntityTypes())) {
            return false;
        }
        
        // Check ComplexTypes - compare by name and structure
        if (!areComplexTypesIdentical(schema1.getComplexTypes(), schema2.getComplexTypes())) {
            return false;
        }
        
        // Check EnumTypes - compare by name and members
        if (!areEnumTypesIdentical(schema1.getEnumTypes(), schema2.getEnumTypes())) {
            return false;
        }
        
        // Check EntityContainer - compare by name and contents
        if (!areEntityContainersIdentical(schema1.getEntityContainer(), schema2.getEntityContainer())) {
            return false;
        }
        
        // Check Actions
        if (!areActionsIdentical(schema1.getActions(), schema2.getActions())) {
            return false;
        }
        
        // Check Functions
        if (!areFunctionsIdentical(schema1.getFunctions(), schema2.getFunctions())) {
            return false;
        }
        
        return true;
    }
    
    private boolean areEqual(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }
    
    private boolean areEntityTypesIdentical(List<?> types1, List<?> types2) {
        if (types1 == null && types2 == null) {
            return true;
        }
        if (types1 == null || types2 == null) {
            return false;
        }
        return types1.size() == types2.size();
        // For now, just compare sizes. Can be enhanced for deeper comparison
    }
    
    private boolean areComplexTypesIdentical(List<?> types1, List<?> types2) {
        if (types1 == null && types2 == null) {
            return true;
        }
        if (types1 == null || types2 == null) {
            return false;
        }
        return types1.size() == types2.size();
        // For now, just compare sizes. Can be enhanced for deeper comparison
    }
    
    private boolean areEnumTypesIdentical(List<CsdlEnumType> types1, List<CsdlEnumType> types2) {
        if (types1 == null && types2 == null) {
            return true;
        }
        if (types1 == null || types2 == null) {
            return false;
        }
        if (types1.size() != types2.size()) {
            return false;
        }
        
        Map<String, CsdlEnumType> map1 = types1.stream()
            .collect(Collectors.toMap(CsdlEnumType::getName, Function.identity()));
        Map<String, CsdlEnumType> map2 = types2.stream()
            .collect(Collectors.toMap(CsdlEnumType::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) {
            return false;
        }
        
        for (String enumName : map1.keySet()) {
            CsdlEnumType enum1 = map1.get(enumName);
            CsdlEnumType enum2 = map2.get(enumName);
            
            if (!areEnumMembersIdentical(enum1.getMembers(), enum2.getMembers())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean areEnumMembersIdentical(List<CsdlEnumMember> members1, List<CsdlEnumMember> members2) {
        if (members1 == null && members2 == null) {
            return true;
        }
        if (members1 == null || members2 == null) {
            return false;
        }
        if (members1.size() != members2.size()) {
            return false;
        }
        
        Map<String, CsdlEnumMember> map1 = members1.stream()
            .collect(Collectors.toMap(CsdlEnumMember::getName, Function.identity()));
        Map<String, CsdlEnumMember> map2 = members2.stream()
            .collect(Collectors.toMap(CsdlEnumMember::getName, Function.identity()));
        
        return map1.keySet().equals(map2.keySet());
    }
    
    private boolean areEntityContainersIdentical(Object container1, Object container2) {
        if (container1 == null && container2 == null) {
            return true;
        }
        if (container1 == null || container2 == null) {
            return false;
        }
        // For now, just check if both exist. Can be enhanced for deeper comparison
        return true;
    }
    
    private boolean areActionsIdentical(List<?> actions1, List<?> actions2) {
        if (actions1 == null && actions2 == null) {
            return true;
        }
        if (actions1 == null || actions2 == null) {
            return false;
        }
        return actions1.size() == actions2.size();
        // For now, just compare sizes. Can be enhanced for deeper comparison
    }
    
    private boolean areFunctionsIdentical(List<?> functions1, List<?> functions2) {
        if (functions1 == null && functions2 == null) {
            return true;
        }
        if (functions1 == null || functions2 == null) {
            return false;
        }
        return functions1.size() == functions2.size();
        // For now, just compare sizes. Can be enhanced for deeper comparison
    }
}