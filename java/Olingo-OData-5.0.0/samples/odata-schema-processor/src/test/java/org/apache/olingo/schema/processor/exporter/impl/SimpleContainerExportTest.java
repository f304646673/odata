package org.apache.olingo.schema.processor.exporter.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test for container exporter functionality
 */
public class SimpleContainerExportTest {

    private DefaultContainerExporter exporter;

    @BeforeEach
    public void setUp() {
        exporter = new DefaultContainerExporter();
    }

    @Test
    public void testBasicFunctionality() {
        assertNotNull(exporter);
    }

    @Test
    public void testCreateBuilder() {
        assertNotNull(exporter.createBuilder());
    }
}
