package org.apache.olingo.sample.springboot.xml.processor;

import org.apache.olingo.sample.springboot.xml.data.XmlDataProvider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Simplified unit tests for XmlEntityProcessor
 */
@ExtendWith(MockitoExtension.class)
class XmlEntityProcessorTest {

    private XmlEntityProcessor processor;
    private XmlDataProvider dataProvider;

    @BeforeEach
    void setUp() {
        dataProvider = new XmlDataProvider();
        processor = new XmlEntityProcessor(dataProvider);
    }

    @Test
    void testInitialization() {
        assertThat(processor).isNotNull();
        assertThat(dataProvider).isNotNull();
    }

    @Test
    void testDataProviderStatistics() {
        // Test that data provider has statistics
        var stats = dataProvider.getDataStatistics();
        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys("dataProvider", "totalCars", "totalManufacturers");
        assertThat(stats.get("totalCars")).isEqualTo(5);
        assertThat(stats.get("totalManufacturers")).isEqualTo(3);
    }

    @Test
    void testProcessorCreated() {
        // Simple test that processor is properly created
        assertThat(processor).isNotNull();
    }
}
