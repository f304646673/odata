package org.apache.olingo.schema.processor.repository.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DefaultXmlFileRepository test class
 */
public class DefaultXmlFileRepositoryTest {
    
    private DefaultXmlFileRepository repository;
    
    @Before
    public void setUp() {
        repository = new DefaultXmlFileRepository();
    }
    
    @Test
    public void testBasicFunctionality() {
        assertNotNull(repository);
    }
}
