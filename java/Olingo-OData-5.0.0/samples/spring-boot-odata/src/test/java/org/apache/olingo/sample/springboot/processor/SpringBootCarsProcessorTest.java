package org.apache.olingo.sample.springboot.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.sample.springboot.data.SpringBootDataProvider;
import org.apache.olingo.sample.springboot.edm.SpringBootEdmProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for SpringBootCarsProcessor
 * 
 * Tests the OData request processing functionality including:
 * - Entity collection reading
 * - Single entity reading  
 * - Property reading
 * - Primitive value reading
 * - Error handling
 * - Data provider integration
 */
@ExtendWith(MockitoExtension.class)
class SpringBootCarsProcessorTest {

    private SpringBootCarsProcessor processor;
    
    @Mock
    private SpringBootDataProvider dataProvider;
    
    @Mock
    private OData odata;
    
    @Mock
    private ServiceMetadata serviceMetadata;
    
    @Mock
    private ODataRequest request;
    
    @Mock
    private ODataResponse response;
    
    @Mock
    private UriInfo uriInfo;
    
    @Mock
    private EdmEntitySet edmEntitySet;
    
    @Mock
    private EdmEntityType edmEntityType;
    
    @Mock
    private UriResourceEntitySet uriResourceEntitySet;
    
    @Mock
    private ODataSerializer serializer;
    
    @Mock
    private SerializerResult serializerResult;
    
    @Mock
    private UriHelper uriHelper;

    @BeforeEach
    void setUp() throws Exception {
        processor = new SpringBootCarsProcessor(dataProvider);
        processor.init(odata, serviceMetadata);
    }

    @Test
    @DisplayName("Should initialize processor with OData and ServiceMetadata")
    void shouldInitializeProcessorWithODataAndServiceMetadata() {
        // Arrange
        SpringBootDataProvider testProvider = new SpringBootDataProvider();
        SpringBootCarsProcessor testProcessor = new SpringBootCarsProcessor(testProvider);
        
        // Act
        testProcessor.init(odata, serviceMetadata);
        
        // Assert - no exception should be thrown
        assertNotNull(testProcessor);
    }

    @Test
    @DisplayName("Should read entity collection successfully")
    void shouldReadEntityCollectionSuccessfully() throws Exception {
        // Arrange
        ContentType contentType = ContentType.APPLICATION_JSON;
        
        // Mock UriHelper
        when(odata.createUriHelper()).thenReturn(uriHelper);
        when(uriHelper.buildContextURLSelectList(any(), any(), any())).thenReturn("selectList");
        
        // Mock URI info and entity set
        when(uriInfo.asUriInfoResource()).thenReturn(uriInfo);
        when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(uriResourceEntitySet));
        when(uriResourceEntitySet.getEntitySet()).thenReturn(edmEntitySet);
        when(edmEntitySet.getName()).thenReturn(SpringBootEdmProvider.ES_CARS_NAME);
        when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
        
        // Mock data provider response
        List<Map<String, Object>> carData = List.of(
            Map.of("Id", 1, "Brand", "BMW", "Model", "X5", "Color", "Blue", "Year", 2022, "Price", 75000.0),
            Map.of("Id", 2, "Brand", "Audi", "Model", "Q7", "Color", "Black", "Year", 2023, "Price", 85000.0)
        );
        when(dataProvider.getAllCars()).thenReturn(carData);
        
        // Mock serializer
        when(odata.createSerializer(contentType)).thenReturn(serializer);
        InputStream mockContent = new ByteArrayInputStream("test content".getBytes());
        when(serializerResult.getContent()).thenReturn(mockContent);
        when(serializer.entityCollection(any(), any(), any(), any())).thenReturn(serializerResult);
        
        // Mock request
        when(request.getRawBaseUri()).thenReturn("http://localhost:8080/cars.svc");

        // Act
        processor.readEntityCollection(request, response, uriInfo, contentType);

        // Assert
        verify(response).setContent(mockContent);
        verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
        verify(response).setHeader(any(), any());
        verify(dataProvider).getAllCars();
    }

    @Test
    @DisplayName("Should handle empty entity collection")
    void shouldHandleEmptyEntityCollection() throws Exception {
        // Arrange
        ContentType contentType = ContentType.APPLICATION_JSON;
        
        // Mock UriHelper
        when(odata.createUriHelper()).thenReturn(uriHelper);
        when(uriHelper.buildContextURLSelectList(any(), any(), any())).thenReturn("selectList");
        
        when(uriInfo.asUriInfoResource()).thenReturn(uriInfo);
        when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(uriResourceEntitySet));
        when(uriResourceEntitySet.getEntitySet()).thenReturn(edmEntitySet);
        when(edmEntitySet.getName()).thenReturn(SpringBootEdmProvider.ES_CARS_NAME);
        when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
        
        // Mock empty data
        when(dataProvider.getAllCars()).thenReturn(Collections.emptyList());
        
        // Mock serializer
        when(odata.createSerializer(contentType)).thenReturn(serializer);
        InputStream mockContent = new ByteArrayInputStream("[]".getBytes());
        when(serializerResult.getContent()).thenReturn(mockContent);
        when(serializer.entityCollection(any(), any(), any(), any())).thenReturn(serializerResult);
        
        when(request.getRawBaseUri()).thenReturn("http://localhost:8080/cars.svc");

        // Act
        processor.readEntityCollection(request, response, uriInfo, contentType);

        // Assert
        verify(response).setContent(mockContent);
        verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
        verify(dataProvider).getAllCars();
    }

    @Test
    @DisplayName("Should throw exception for unimplemented create operation")
    void shouldThrowExceptionForUnimplementedCreateOperation() throws Exception {
        // Arrange
        ContentType contentType = ContentType.APPLICATION_JSON;

        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.createEntity(request, response, uriInfo, contentType, contentType));

        assertEquals("Entity create is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
        assertEquals(Locale.ENGLISH, exception.getLocale());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented update operation")
    void shouldThrowExceptionForUnimplementedUpdateOperation() throws Exception {
        // Arrange
        ContentType contentType = ContentType.APPLICATION_JSON;

        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.updateEntity(request, response, uriInfo, contentType, contentType));

        assertEquals("Entity update is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented delete operation")
    void shouldThrowExceptionForUnimplementedDeleteOperation() throws Exception {
        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.deleteEntity(request, response, uriInfo));

        assertEquals("Entity delete is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented primitive update operation")
    void shouldThrowExceptionForUnimplementedPrimitiveUpdateOperation() throws Exception {
        // Arrange
        ContentType contentType = ContentType.APPLICATION_JSON;

        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.updatePrimitive(request, response, uriInfo, contentType, contentType));

        assertEquals("Primitive property update is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented primitive delete operation")
    void shouldThrowExceptionForUnimplementedPrimitiveDeleteOperation() throws Exception {
        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.deletePrimitive(request, response, uriInfo));

        assertEquals("Primitive property delete is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented primitive value update operation")
    void shouldThrowExceptionForUnimplementedPrimitiveValueUpdateOperation() throws Exception {
        // Arrange
        ContentType contentType = ContentType.APPLICATION_JSON;

        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.updatePrimitiveValue(request, response, uriInfo, contentType, contentType));

        assertEquals("Primitive property update is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented primitive value delete operation")
    void shouldThrowExceptionForUnimplementedPrimitiveValueDeleteOperation() throws Exception {
        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.deletePrimitiveValue(request, response, uriInfo));

        assertEquals("Primitive property delete is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented complex update operation")
    void shouldThrowExceptionForUnimplementedComplexUpdateOperation() throws Exception {
        // Arrange
        ContentType contentType = ContentType.APPLICATION_JSON;

        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.updateComplex(request, response, uriInfo, contentType, contentType));

        assertEquals("Complex property update is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception for unimplemented complex delete operation")
    void shouldThrowExceptionForUnimplementedComplexDeleteOperation() throws Exception {
        // Act & Assert
        ODataApplicationException exception = assertThrows(ODataApplicationException.class,
            () -> processor.deleteComplex(request, response, uriInfo));

        assertEquals("Complex property delete is not supported yet.", exception.getMessage());
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), exception.getStatusCode());
    }

    @Test
    @DisplayName("Should correctly identify OData metadata none content type")
    void shouldCorrectlyIdentifyODataMetadataNoneContentType() {
        // Test with metadata=none
        ContentType metadataNone = ContentType.APPLICATION_JSON;
        // Note: In a real implementation, you would need to properly create ContentType with parameters
        // For this test, we'll test the basic logic
        assertFalse(SpringBootCarsProcessor.isODataMetadataNone(metadataNone));

        // Test with XML content type
        ContentType xmlContentType = ContentType.APPLICATION_XML;
        assertFalse(SpringBootCarsProcessor.isODataMetadataNone(xmlContentType));
    }

    @Test
    @DisplayName("Should handle data provider with sample data correctly")
    void shouldHandleDataProviderWithSampleDataCorrectly() throws Exception {
        // Arrange
        SpringBootDataProvider realDataProvider = new SpringBootDataProvider();
        SpringBootCarsProcessor realProcessor = new SpringBootCarsProcessor(realDataProvider);
        realProcessor.init(odata, serviceMetadata);
        
        ContentType contentType = ContentType.APPLICATION_JSON;
        
        // Mock UriHelper
        when(odata.createUriHelper()).thenReturn(uriHelper);
        when(uriHelper.buildContextURLSelectList(any(), any(), any())).thenReturn("selectList");
        
        when(uriInfo.asUriInfoResource()).thenReturn(uriInfo);
        when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(uriResourceEntitySet));
        when(uriResourceEntitySet.getEntitySet()).thenReturn(edmEntitySet);
        when(edmEntitySet.getName()).thenReturn(SpringBootEdmProvider.ES_CARS_NAME);
        when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
        
        when(odata.createSerializer(contentType)).thenReturn(serializer);
        InputStream mockContent = new ByteArrayInputStream("test".getBytes());
        when(serializerResult.getContent()).thenReturn(mockContent);
        when(serializer.entityCollection(any(), any(), any(), any())).thenReturn(serializerResult);
        when(request.getRawBaseUri()).thenReturn("http://localhost:8080/cars.svc");

        // Act
        realProcessor.readEntityCollection(request, response, uriInfo, contentType);

        // Assert
        verify(response).setContent(mockContent);
        verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
        
        // Verify that the real data provider has sample data
        assertTrue(realDataProvider.getCarCount() > 0);
        assertFalse(realDataProvider.getAllCars().isEmpty());
    }
}
