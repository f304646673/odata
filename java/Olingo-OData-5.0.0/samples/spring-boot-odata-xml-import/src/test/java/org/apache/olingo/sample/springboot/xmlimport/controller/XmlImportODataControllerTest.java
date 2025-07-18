package org.apache.olingo.sample.springboot.xmlimport.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for XmlImportODataController.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class XmlImportODataControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testMetadataEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/$metadata", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("OData.Demo");
        assertThat(response.getBody()).contains("Car");
        assertThat(response.getBody()).contains("Manufacturer");
    }

    @Test
    void testServiceDocument() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCarsEntitySet() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/Cars", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testManufacturersEntitySet() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/Manufacturers", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testSingleCar() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/Cars(1)", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testSingleManufacturer() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/Manufacturers(1)", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCarNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/Cars(999)", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testManufacturerNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/cars.svc/Manufacturers(999)", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
