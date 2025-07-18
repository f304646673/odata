/*
 * Licensed to the Apache Software Fo    @Test
    void shouldReturnServiceDocument() throws Exception {
        mockMvc.perform(get("/cars.svc/")
                .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }n (ASF) under one
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
package org.apache.olingo.sample.springboot.xml.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for XmlODataController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class XmlODataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnServiceDocument() throws Exception {
        mockMvc.perform(get("/cars.svc/")
                .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void shouldReturnMetadata() throws Exception {
        mockMvc.perform(get("/cars.svc/$metadata")
                .accept("application/xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"));
    }

    @Test
    void shouldReturnCarsCollection() throws Exception {
        mockMvc.perform(get("/cars.svc/Cars")
                .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void shouldReturnManufacturersCollection() throws Exception {
        mockMvc.perform(get("/cars.svc/Manufacturers")
                .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void shouldReturnSpecificCar() throws Exception {
        mockMvc.perform(get("/cars.svc/Cars(1)")
                .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void shouldReturnSpecificManufacturer() throws Exception {
        mockMvc.perform(get("/cars.svc/Manufacturers(1)")
                .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCar() throws Exception {
        mockMvc.perform(get("/cars.svc/Cars(999)")
                .accept("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForNonExistentManufacturer() throws Exception {
        mockMvc.perform(get("/cars.svc/Manufacturers(999)")
                .accept("application/json"))
                .andExpect(status().isNotFound());
    }
}
