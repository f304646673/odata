package org.apache.olingo.sample.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for ODataSpringBootApplication
 * 
 * This test verifies that the Spring Boot application context loads successfully
 * and all beans are properly configured.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ODataSpringBootApplicationTest {

    /**
     * Test that the application context loads without errors
     */
    @Test
    void contextLoads() {
        // This test will fail if the application context cannot be loaded
        // It verifies that all beans are properly configured and dependencies are satisfied
    }
}
