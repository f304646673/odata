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
package org.apache.olingo.sample.springboot.xmlimport;

import org.apache.olingo.sample.springboot.xmlimport.edm.XmlImportEdmProvider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "server.port=0",  // Use random port for testing
    "logging.level.org.apache.olingo=DEBUG"
})
class XmlImportODataApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void edmProviderBeanExists() {
        XmlImportEdmProvider edmProvider = applicationContext.getBean(XmlImportEdmProvider.class);
        assertThat(edmProvider).isNotNull();
    }

    @Test
    void applicationCanStart() {
        // This test verifies that the Spring Boot application can start successfully
        // The @SpringBootTest annotation ensures the full application context is loaded
        assertThat(applicationContext.getBean(XmlImportODataApplication.class)).isNotNull();
    }

    @Test
    void allRequiredBeansArePresent() {
        // Verify all critical beans are present in the application context
        assertThat(applicationContext.containsBean("edmProvider")).isTrue();
        assertThat(applicationContext.getBean("edmProvider")).isInstanceOf(XmlImportEdmProvider.class);
    }
}
