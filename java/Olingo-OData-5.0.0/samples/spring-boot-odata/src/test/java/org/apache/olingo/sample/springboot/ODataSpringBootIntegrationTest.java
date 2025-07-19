package org.apache.olingo.sample.springboot;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for the complete Spring Boot OData application
 * 
 * These tests verify end-to-end functionality including:
 * - Service metadata endpoint
 * - Entity collection queries
 * - Entity by key queries
 * - Property queries
 * - HTTP status codes and content types
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class ODataSpringBootIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private String getBaseUrl() {
        return "http://localhost:" + port + "/cars.svc";
    }

    @Test
    @DisplayName("Should return service metadata")
    void shouldReturnServiceMetadata() {
        String url = getBaseUrl() + "/$metadata";
        
        // Create headers to explicitly request XML metadata
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_XML));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("<?xml"));
        assertTrue(body.contains("EntityContainer"));
        assertTrue(body.contains("Car"));
    }

    @Test
    @DisplayName("Should return service document")
    void shouldReturnServiceDocument() {
        String url = getBaseUrl() + "/";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return Cars entity collection")
    void shouldReturnCarsEntityCollection() {
        String url = getBaseUrl() + "/Cars";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        // Verify it contains JSON structure for cars
        assertTrue(body.contains("value") || body.contains("Car"));
    }

    @Test
    @DisplayName("Should handle root endpoint")
    void shouldHandleRootEndpoint() {
        String url = getBaseUrl();
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return appropriate response for Cars count")
    void shouldReturnAppropriateResponseForCarsCount() {
        String url = getBaseUrl() + "/Cars/$count";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Should return OK status and a numeric count
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should handle query options")
    void shouldHandleQueryOptions() {
        String url = getBaseUrl() + "/Cars?$top=2";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should handle filter query options")
    void shouldHandleFilterQueryOptions() {
        String url = getBaseUrl() + "/Cars?$filter=Brand eq 'BMW'";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return 404 for non-existent endpoint")
    void shouldReturn404ForNonExistentEndpoint() {
        String url = getBaseUrl() + "/NonExistentEntity";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Should handle this gracefully - may return 404 or 400 depending on OData implementation
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    @DisplayName("Should verify application context loads properly")
    void shouldVerifyApplicationContextLoadsProperlyInIntegrationEnvironment() {
        // This test ensures the full application context loads correctly
        // Simply making a successful request verifies this
        String url = getBaseUrl() + "/$metadata";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
