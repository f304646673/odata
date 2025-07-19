package org.apache.olingo.sample.springboot.controller;

import org.apache.olingo.sample.springboot.service.ODataSpringBootService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for ODataController
 * 
 * Tests the Spring MVC controller functionality including:
 * - Request mapping and routing
 * - HTTP method handling  
 * - Integration with ODataSpringBootService
 * - Path handling and servlet path wrapping
 * - Error handling
 */
@WebMvcTest(ODataController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class ODataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ODataSpringBootService odataService;

    @Test
    @DisplayName("Should handle GET request to root path")
    void shouldHandleGetRequestToRootPath() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle GET request to slash path")
    void shouldHandleGetRequestToSlashPath() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle GET request with path info")
    void shouldHandleGetRequestWithPathInfo() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/Cars"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle GET request to metadata")
    void shouldHandleGetRequestToMetadata() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/$metadata"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle POST request")
    void shouldHandlePostRequest() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(post("/cars.svc/Cars")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle PUT request")
    void shouldHandlePutRequest() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(put("/cars.svc/Cars(1)")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle DELETE request")
    void shouldHandleDeleteRequest() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(delete("/cars.svc/Cars(1)"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle PATCH request")
    void shouldHandlePatchRequest() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(patch("/cars.svc/Cars(1)")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle complex entity queries")
    void shouldHandleComplexEntityQueries() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/Cars?$filter=Brand eq 'BMW'&$top=10&$skip=5"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle entity by key requests")
    void shouldHandleEntityByKeyRequests() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/Cars(1)"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle property requests")
    void shouldHandlePropertyRequests() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/Cars(1)/Brand"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle value requests")
    void shouldHandleValueRequests() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/Cars(1)/Brand/$value"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }

    @Test
    @DisplayName("Should handle count requests")
    void shouldHandleCountRequests() throws Exception {
        // Arrange
        doNothing().when(odataService).processODataRequest(any(), any());

        // Act & Assert
        mockMvc.perform(get("/cars.svc/Cars/$count"))
                .andExpect(status().isOk());

        verify(odataService).processODataRequest(any(), any());
    }
}
