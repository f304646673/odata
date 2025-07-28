package org.apache.olingo.schema.processor.exporter.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.exporter.ContainerExporter.ContainerExportResult;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试DefaultContainerExporter类
 * 该类负责将EntityContainer导出到指定格式
 */
public class DefaultContainerExporterTest {

    private DefaultContainerExporter exporter;
    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        exporter = new DefaultContainerExporter();
        tempDir = Files.createTempDirectory("test-export");
    }

    @After
    public void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                 .sorted((a, b) -> b.compareTo(a))
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch (IOException e) {
                         // Ignore
                     }
                 });
        }
    }

    @Test
    public void testDefaultConstructor() {
        DefaultContainerExporter newExporter = new DefaultContainerExporter();
        assertNotNull("Exporter should not be null", newExporter);
    }

    @Test
    public void testExporterNotNull() {
        assertNotNull("Exporter should not be null", exporter);
    }

    @Test
    public void testExportContainer() throws IOException {
        CsdlEntityContainer container = createTestContainer();
        Path outputPath = tempDir.resolve("test-container.xml");
        
        ContainerExportResult result = exporter.exportContainer(container, outputPath.toString(), "Test.Namespace");
        
        assertNotNull("Export result should not be null", result);
        assertTrue("Output file should exist", Files.exists(outputPath));
    }

    @Test
    public void testExportContainerWithFile() throws IOException {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("Test.Namespace");
        schema.setEntityContainer(createTestContainer());
        
        File outputFile = tempDir.resolve("test-schema.xml").toFile();
        
        exporter.exportContainer(schema, outputFile);
        
        assertTrue("Output file should exist", outputFile.exists());
        assertTrue("Output file should not be empty", outputFile.length() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportNullContainer() throws IOException {
        Path outputPath = tempDir.resolve("test-container.xml");
        exporter.exportContainer(null, outputPath.toString(), "Test.Namespace");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportToNullPath() throws IOException {
        CsdlEntityContainer container = createTestContainer();
        exporter.exportContainer(container, null, "Test.Namespace");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportToEmptyPath() throws IOException {
        CsdlEntityContainer container = createTestContainer();
        exporter.exportContainer(container, "", "Test.Namespace");
    }

    @Test
    public void testExportContainerWithoutEntitySets() throws IOException {
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName("EmptyContainer");
        
        Path outputPath = tempDir.resolve("empty-container.xml");
        
        ContainerExportResult result = exporter.exportContainer(container, outputPath.toString(), "Test.Namespace");
        
        assertNotNull("Export result should not be null", result);
        assertTrue("Output file should exist", Files.exists(outputPath));
    }

    @Test
    public void testExportContainerFromXml() throws IOException {
        // Create a test XML file first
        Path inputPath = tempDir.resolve("input.xml");
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test.Model\">\n" +
                           "      <EntityContainer Name=\"Container\">\n" +
                           "      </EntityContainer>\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        Files.write(inputPath, xmlContent.getBytes());
        
        Path outputPath = tempDir.resolve("output.xml");
        
        ContainerExportResult result = exporter.exportContainerFromXml(
            inputPath.toString(), outputPath.toString(), "TestContainer");
        
        assertNotNull("Export result should not be null", result);
    }

    @Test
    public void testExportMultipleContainers() throws IOException {
        CsdlEntityContainer container1 = createTestContainer();
        CsdlEntityContainer container2 = createTestContainer();
        container2.setName("SecondContainer");
        
        Path outputPath1 = tempDir.resolve("test-container1.xml");
        Path outputPath2 = tempDir.resolve("test-container2.xml");
        
        ContainerExportResult result1 = exporter.exportContainer(container1, outputPath1.toString(), "Test.Namespace1");
        ContainerExportResult result2 = exporter.exportContainer(container2, outputPath2.toString(), "Test.Namespace2");
        
        assertNotNull("First result should not be null", result1);
        assertNotNull("Second result should not be null", result2);
        assertTrue("First file should exist", Files.exists(outputPath1));
        assertTrue("Second file should exist", Files.exists(outputPath2));
    }

    @Test
    public void testExportWithSpecialCharacters() throws IOException {
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName("Container_With-Special.Characters");
        
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName("EntitySet-With_Special.Characters");
        entitySet.setType("Test.Special_Entity-Type");
        
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        entitySets.add(entitySet);
        container.setEntitySets(entitySets);
        
        Path outputPath = tempDir.resolve("special-container.xml");
        
        ContainerExportResult result = exporter.exportContainer(container, outputPath.toString(), "Test.Special_Namespace");
        
        assertNotNull("Export result should not be null", result);
        assertTrue("Output file should exist", Files.exists(outputPath));
        assertTrue("Output file should not be empty", Files.size(outputPath) > 0);
    }

    @Test
    public void testExportLargeContainer() throws IOException {
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName("LargeContainer");
        
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            CsdlEntitySet entitySet = new CsdlEntitySet();
            entitySet.setName("EntitySet" + i);
            entitySet.setType("Test.Entity" + i);
            entitySets.add(entitySet);
        }
        container.setEntitySets(entitySets);
        
        Path outputPath = tempDir.resolve("large-container.xml");
        
        ContainerExportResult result = exporter.exportContainer(container, outputPath.toString(), "Test.Large");
        
        assertNotNull("Export result should not be null", result);
        assertTrue("Output file should exist", Files.exists(outputPath));
        assertTrue("Output file should not be empty", Files.size(outputPath) > 0);
    }

    @Test
    public void testExporterStateConsistency() throws IOException {
        // Test that exporter doesn't maintain state between calls
        CsdlEntityContainer container1 = createTestContainer();
        CsdlEntityContainer container2 = createTestContainer();
        container2.setName("DifferentContainer");
        
        Path outputPath1 = tempDir.resolve("container1.xml");
        Path outputPath2 = tempDir.resolve("container2.xml");
        
        ContainerExportResult result1 = exporter.exportContainer(container1, outputPath1.toString(), "Test.Namespace1");
        ContainerExportResult result2 = exporter.exportContainer(container2, outputPath2.toString(), "Test.Namespace2");
        
        assertNotNull("First result should not be null", result1);
        assertNotNull("Second result should not be null", result2);
        assertTrue("Both files should exist", Files.exists(outputPath1) && Files.exists(outputPath2));
        assertTrue("Both files should have content", Files.size(outputPath1) > 0 && Files.size(outputPath2) > 0);
    }

    @Test
    public void testMultipleExporterInstances() throws IOException {
        DefaultContainerExporter exporter1 = new DefaultContainerExporter();
        DefaultContainerExporter exporter2 = new DefaultContainerExporter();
        
        CsdlEntityContainer container = createTestContainer();
        Path outputPath1 = tempDir.resolve("exporter1.xml");
        Path outputPath2 = tempDir.resolve("exporter2.xml");
        
        ContainerExportResult result1 = exporter1.exportContainer(container, outputPath1.toString(), "Test.Namespace1");
        ContainerExportResult result2 = exporter2.exportContainer(container, outputPath2.toString(), "Test.Namespace2");
        
        assertNotNull("First result should not be null", result1);
        assertNotNull("Second result should not be null", result2);
        assertTrue("Both files should exist", Files.exists(outputPath1) && Files.exists(outputPath2));
        assertTrue("Both files should have content", Files.size(outputPath1) > 0 && Files.size(outputPath2) > 0);
    }

    @Test
    public void testExportResultNotNull() throws IOException {
        CsdlEntityContainer container = createTestContainer();
        Path outputPath = tempDir.resolve("result-test.xml");
        
        ContainerExportResult result = exporter.exportContainer(container, outputPath.toString(), "Test.Namespace");
        
        assertNotNull("Export result should not be null", result);
        // Test basic properties of the result if available
        assertTrue("Output file should exist", Files.exists(outputPath));
    }

    private CsdlEntityContainer createTestContainer() {
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName("TestContainer");
        
        CsdlEntitySet entitySet1 = new CsdlEntitySet();
        entitySet1.setName("Products");
        entitySet1.setType("Test.Product");
        
        CsdlEntitySet entitySet2 = new CsdlEntitySet();
        entitySet2.setName("Categories");
        entitySet2.setType("Test.Category");
        
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        entitySets.add(entitySet1);
        entitySets.add(entitySet2);
        container.setEntitySets(entitySets);
        
        return container;
    }
}
