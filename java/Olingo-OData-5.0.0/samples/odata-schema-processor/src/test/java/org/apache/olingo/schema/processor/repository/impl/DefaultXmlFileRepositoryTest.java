package org.apache.olingo.schema.processor.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultXmlFileRepository test class
 */
public class DefaultXmlFileRepositoryTest {
    
    private DefaultXmlFileRepository repository;
    
    @BeforeEach
    public void setUp() {
        repository = new DefaultXmlFileRepository();
    }
    
    @Test
    public void testBasicFunctionality() {
        assertNotNull(repository);
    }
}
