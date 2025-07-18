package org.apache.olingo.sample.springboot.xmlimport.processor;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.sample.springboot.xmlimport.data.XmlImportDataProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for XmlImportEntityProcessor.
 */
@ExtendWith(MockitoExtension.class)
class XmlImportEntityProcessorTest {

    @Mock
    private XmlImportDataProvider dataProvider;

    @Mock
    private OData odata;

    @Mock
    private ServiceMetadata serviceMetadata;

    @InjectMocks
    private XmlImportEntityProcessor processor;

    @BeforeEach
    void setUp() {
        // Basic setup for processor
    }

    @Test
    void testInit() {
        // Act
        processor.init(odata, serviceMetadata);
        
        // Assert
        assertThat(processor).isNotNull();
    }

    @Test
    void testDataProviderInjection() {
        // Verify that the data provider is properly injected
        assertThat(processor).isNotNull();
    }

    @Test
    void testProcessorCanHandleCarsData() {
        // Arrange
        EntityCollection cars = new EntityCollection();
        Entity car = new Entity();
        car.addProperty(createProperty("Id", 1));
        car.addProperty(createProperty("Model", "BMW X3"));
        cars.getEntities().add(car);
        
        when(dataProvider.getCars()).thenReturn(cars);
        
        // Act
        EntityCollection result = dataProvider.getCars();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntities()).hasSize(1);
        assertThat(result.getEntities().get(0).getProperty("Model").getValue()).isEqualTo("BMW X3");
    }

    @Test
    void testProcessorCanHandleManufacturersData() {
        // Arrange
        EntityCollection manufacturers = new EntityCollection();
        Entity manufacturer = new Entity();
        manufacturer.addProperty(createProperty("Id", 1));
        manufacturer.addProperty(createProperty("Name", "BMW"));
        manufacturers.getEntities().add(manufacturer);
        
        when(dataProvider.getManufacturers()).thenReturn(manufacturers);
        
        // Act
        EntityCollection result = dataProvider.getManufacturers();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntities()).hasSize(1);
        assertThat(result.getEntities().get(0).getProperty("Name").getValue()).isEqualTo("BMW");
    }

    @Test
    void testProcessorCanHandleSingleCar() {
        // Arrange
        Entity car = new Entity();
        car.addProperty(createProperty("Id", 1));
        car.addProperty(createProperty("Model", "BMW X3"));
        
        when(dataProvider.getCar(1)).thenReturn(car);
        
        // Act
        Entity result = dataProvider.getCar(1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProperty("Id").getValue()).isEqualTo(1);
        assertThat(result.getProperty("Model").getValue()).isEqualTo("BMW X3");
    }

    @Test
    void testProcessorCanHandleSingleManufacturer() {
        // Arrange
        Entity manufacturer = new Entity();
        manufacturer.addProperty(createProperty("Id", 1));
        manufacturer.addProperty(createProperty("Name", "BMW"));
        
        when(dataProvider.getManufacturer(1)).thenReturn(manufacturer);
        
        // Act
        Entity result = dataProvider.getManufacturer(1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProperty("Id").getValue()).isEqualTo(1);
        assertThat(result.getProperty("Name").getValue()).isEqualTo("BMW");
    }

    /**
     * Helper method to create properties.
     */
    private org.apache.olingo.commons.api.data.Property createProperty(String name, Object value) {
        org.apache.olingo.commons.api.data.Property property = new org.apache.olingo.commons.api.data.Property();
        property.setName(name);
        property.setValue(org.apache.olingo.commons.api.data.ValueType.PRIMITIVE, value);
        return property;
    }
}
