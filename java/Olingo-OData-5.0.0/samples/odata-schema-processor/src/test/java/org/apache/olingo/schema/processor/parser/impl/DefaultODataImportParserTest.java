package org.apache.olingo.schema.processor.parser.impl;

import org.apache.olingo.schema.processor.parser.ODataImportParser;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * DefaultODataImportParser test class
 */
public class DefaultODataImportParserTest {
    
    private DefaultODataImportParser parser;
    
    @Before
    public void setUp() {
        parser = new DefaultODataImportParser();
    }
    
    @Test
    public void testBasicFunctionality() {
        assertNotNull(parser);
    }
}
