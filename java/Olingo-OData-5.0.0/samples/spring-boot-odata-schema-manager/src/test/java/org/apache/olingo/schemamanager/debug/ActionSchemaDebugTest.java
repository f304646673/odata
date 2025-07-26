package org.apache.olingo.schemamanager.debug;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActionSchemaDebugTest {
    
    @Test
    void testSchemaLoading() throws Exception {
        System.out.println("Loading schema...");
        
        try {
            CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema1.xml");
            System.out.println("Schema loaded successfully");
            
            System.out.println("Schema namespace: " + schema.getNamespace());
            System.out.println("Schema actions: " + schema.getActions());
            
            if (schema.getActions() != null) {
                System.out.println("Actions count: " + schema.getActions().size());
                schema.getActions().forEach(action -> {
                    System.out.println("Action name: " + action.getName());
                });
            } else {
                System.out.println("Actions is null");
            }
            
            assertNotNull(schema.getActions(), "Actions should not be null");
            assertFalse(schema.getActions().isEmpty(), "Actions should not be empty");
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
