package org.apache.olingo.schema.processor.exporter.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple test for container exporter functionality
 */
public class SimpleContainerExportTest {

    private DefaultContainerExporter exporter;

    @Before
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
