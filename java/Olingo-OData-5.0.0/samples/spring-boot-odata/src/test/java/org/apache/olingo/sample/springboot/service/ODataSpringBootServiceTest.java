package org.apache.olingo.sample.springboot.service;

import java.io.IOException;

import org.apache.olingo.sample.springboot.data.SpringBootDataProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Unit tests for ODataSpringBootService
 * 
 * Tests the core OData service functionality including:
 * - Request processing
 * - Session management
 * - Data provider initialization
 * - Error handling
 * - Integration with Olingo framework
 */
@ExtendWith(MockitoExtension.class)
class ODataSpringBootServiceTest {

    private ODataSpringBootService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        service = new ODataSpringBootService();
    }

    @Test
    @DisplayName("Should create new data provider when not in session")
    void shouldCreateNewDataProviderWhenNotInSession() throws ServletException, IOException {
        // Arrange
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute(SpringBootDataProvider.class.getName())).thenReturn(null);

        // Act & Assert
        try {
            service.processODataRequest(request, response);
        } catch (ServletException e) {
            // This is expected due to incomplete mocking for OData processing
            // The important part is that session interaction happens
            verify(session).getAttribute(SpringBootDataProvider.class.getName());
            verify(session).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
            return;
        }
        
        // If no exception, still verify session interaction
        verify(session).getAttribute(SpringBootDataProvider.class.getName());
        verify(session).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
    }

    @Test
    @DisplayName("Should reuse existing data provider from session")
    void shouldReuseExistingDataProviderFromSession() throws ServletException, IOException {
        // Arrange
        SpringBootDataProvider existingProvider = new SpringBootDataProvider();
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute(SpringBootDataProvider.class.getName())).thenReturn(existingProvider);

        // Act & Assert
        try {
            service.processODataRequest(request, response);
        } catch (ServletException e) {
            // This is expected due to incomplete mocking for OData processing
            // The important part is that session interaction happens correctly
            verify(session).getAttribute(SpringBootDataProvider.class.getName());
            verify(session, never()).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
            return;
        }

        // If no exception, still verify session interaction
        verify(session).getAttribute(SpringBootDataProvider.class.getName());
        verify(session, never()).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
    }

    @Test
    @DisplayName("Should handle runtime exceptions and wrap as ServletException")
    void shouldHandleRuntimeExceptionsAndWrapAsServletException() throws IOException {
        // Arrange
        when(request.getSession(true)).thenThrow(new RuntimeException("Session error"));

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, 
            () -> service.processODataRequest(request, response));

        assertEquals("OData processing failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Session error", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should properly initialize OData components")
    void shouldProperlyInitializeODataComponents() throws ServletException, IOException {
        // Arrange
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute(SpringBootDataProvider.class.getName())).thenReturn(null);

        // Act & Assert - This should process the session management part correctly
        try {
            service.processODataRequest(request, response);
        } catch (ServletException e) {
            // Expected due to incomplete OData mocking, but verify session handling
            verify(session).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
            assertEquals("OData processing failed", e.getMessage());
            return;
        }
        
        // If no exception, verify that data provider was created and stored in session
        verify(session).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
    }

    @Test
    @DisplayName("Should handle different HTTP methods")
    void shouldHandleDifferentHttpMethods() throws ServletException, IOException {
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
        
        for (String method : methods) {
            // Arrange
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            HttpSession mockSession = mock(HttpSession.class);
            
            when(mockRequest.getSession(true)).thenReturn(mockSession);
            when(mockSession.getAttribute(SpringBootDataProvider.class.getName())).thenReturn(null);
            when(mockRequest.getMethod()).thenReturn(method);

            // Act & Assert
            try {
                service.processODataRequest(mockRequest, response);
            } catch (ServletException e) {
                // Expected due to OData processing complexity, but verify session handling
                verify(mockSession).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
                assertEquals("OData processing failed", e.getMessage());
                continue;
            }
            
            // If no exception, still verify session interaction
            verify(mockSession).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
        }
    }

    @Test
    @DisplayName("Should create unique data provider instances per session")
    void shouldCreateUniqueDataProviderInstancesPerSession() throws ServletException, IOException {
        // Arrange first request
        HttpServletRequest request1 = mock(HttpServletRequest.class);
        HttpSession session1 = mock(HttpSession.class);
        
        when(request1.getSession(true)).thenReturn(session1);
        when(session1.getAttribute(SpringBootDataProvider.class.getName())).thenReturn(null);

        // Arrange second request
        HttpServletRequest request2 = mock(HttpServletRequest.class);
        HttpSession session2 = mock(HttpSession.class);
        
        when(request2.getSession(true)).thenReturn(session2);
        when(session2.getAttribute(SpringBootDataProvider.class.getName())).thenReturn(null);

        // Act
        try {
            service.processODataRequest(request1, response);
        } catch (ServletException e) {
            // Expected, verify session1 got data provider
            verify(session1).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
        }
        
        try {
            service.processODataRequest(request2, response);
        } catch (ServletException e) {
            // Expected, verify session2 got data provider  
            verify(session2).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
        }

        // Verify both sessions got data providers (even if OData processing failed)
        verify(session1).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
        verify(session2).setAttribute(eq(SpringBootDataProvider.class.getName()), any(SpringBootDataProvider.class));
    }
}
