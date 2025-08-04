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
package org.apache.olingo.advanced.xmlparser.validation;

import java.util.List;

import org.apache.olingo.advanced.xmlparser.core.SchemaValidator;
import org.apache.olingo.advanced.xmlparser.statistics.ErrorInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Implementation of schema validation using verified logic
 */
public class SchemaValidatorImpl implements SchemaValidator {
    
    private final SchemaValidator originalValidator;
    
    public SchemaValidatorImpl() {
        // Use the original validator for backward compatibility
        this.originalValidator = new org.apache.olingo.advanced.xmlparser.SchemaValidator();
    }
    
    @Override
    public List<ErrorInfo> validateSchema(CsdlSchema schema) {
        return originalValidator.validateSchema(schema);
    }
    
    @Override
    public boolean isValidSchema(CsdlSchema schema) {
        return originalValidator.isValidSchema(schema);
    }
}
