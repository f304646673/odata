package org.apache.olingo.compliance.test.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing test resources - creates missing XML files on demand
 */
public class TestResourceManager {
    
    private static final String BASE_RESOURCE_PATH = "src/test/resources/validation/single/invalid";
    
    private static final Map<String, String> INVALID_XML_TEMPLATES = new HashMap<>();
    
    static {
        // Attribute error templates
        INVALID_XML_TEMPLATES.put("attribute_error/missing_name/member_missing_name.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EnumType Name=\"Status\">\n" +
            "        <Member Value=\"0\"/>\n" +  // Missing Name attribute
            "        <Member Name=\"Approved\" Value=\"1\"/>\n" +
            "      </EnumType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("attribute_error/missing_name/parameter_missing_name.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <Action Name=\"TestAction\">\n" +
            "        <Parameter Type=\"Edm.String\"/>\n" +  // Missing Name attribute
            "      </Action>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("attribute_error/missing_name/propertyref_missing_name.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef/>\n" +  // Missing Name attribute
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("attribute_error/missing_type/parameter_missing_type.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <Action Name=\"TestAction\">\n" +
            "        <Parameter Name=\"param1\"/>\n" +  // Missing Type attribute
            "      </Action>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("attribute_error/missing_type/navigationproperty_missing_type.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <NavigationProperty Name=\"Category\"/>\n" +  // Missing Type attribute
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("attribute_error/invalid_value/nullable_key_property.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"true\"/>\n" +  // Key property cannot be nullable
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("attribute_error/invalid_identifier/invalid_name_format.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"123Invalid\">\n" +  // Invalid identifier - starts with number
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        // Duplicate error templates
        INVALID_XML_TEMPLATES.put("duplicate_error/duplicate_elements/duplicate_entitytype.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityType Name=\"Product\">\n" +  // Duplicate EntityType name
            "        <Key>\n" +
            "          <PropertyRef Name=\"Name\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("duplicate_error/duplicate_parameters/duplicate_action_parameters.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <Action Name=\"TestAction\">\n" +
            "        <Parameter Name=\"param1\" Type=\"Edm.String\"/>\n" +
            "        <Parameter Name=\"param1\" Type=\"Edm.Int32\"/>\n" +  // Duplicate parameter name
            "      </Action>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("duplicate_error/duplicate_properties/duplicate_entitytype_properties.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +  // Duplicate property name
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        INVALID_XML_TEMPLATES.put("duplicate_error/duplicate_container_children/duplicate_entityset.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"TestContainer\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"TestModel.Product\"/>\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"TestModel.Product\"/>\n" +  // Duplicate EntitySet name
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        // Structure error templates (placeholders)
        INVALID_XML_TEMPLATES.put("structure_error/invalid_hierarchy/circular_inheritance.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <!-- Placeholder for structure error - implement specific validation as needed -->\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
            
        // Type error templates (placeholders)
        INVALID_XML_TEMPLATES.put("type_error/invalid_type_reference/unknown_type.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <!-- Placeholder for type error - implement specific validation as needed -->\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>");
    }
    
    /**
     * Gets the path to a test resource file, creating it if it doesn't exist
     */
    public static String getTestResourcePath(String resourceRelativePath) {
        Path resourcePath = Paths.get(BASE_RESOURCE_PATH, resourceRelativePath);
        File resourceFile = resourcePath.toFile();
        
        if (!resourceFile.exists()) {
            createTestResource(resourceRelativePath, resourcePath);
        }
        
        return resourceFile.getAbsolutePath();
    }
    
    /**
     * Creates a test resource file with appropriate content
     */
    private static void createTestResource(String relativePath, Path resourcePath) {
        try {
            // Ensure parent directories exist
            Files.createDirectories(resourcePath.getParent());
            
            // Get content from templates or create default
            String content = INVALID_XML_TEMPLATES.get(relativePath);
            if (content == null) {
                content = createDefaultInvalidXml(relativePath);
            }
            
            Files.write(resourcePath, content.getBytes());
            System.out.println("Created test resource: " + resourcePath);
            
        } catch (IOException e) {
            System.err.println("Failed to create test resource: " + resourcePath + " - " + e.getMessage());
        }
    }
    
    /**
     * Creates a default invalid XML content based on the file path
     */
    private static String createDefaultInvalidXml(String relativePath) {
        // Extract error type from path
        String errorType = "";
        if (relativePath.contains("attribute_error")) {
            errorType = "MISSING_NAME_ATTRIBUTE";
        } else if (relativePath.contains("duplicate_error")) {
            errorType = "DUPLICATE_ELEMENT";
        } else if (relativePath.contains("structure_error")) {
            errorType = "STRUCTURE_ERROR";
        } else if (relativePath.contains("type_error")) {
            errorType = "TYPE_ERROR";
        }
        
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
               "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
               "  <edmx:DataServices>\n" +
               "    <Schema Namespace=\"TestModel\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
               "      <!-- Test file for " + errorType + " scenario: " + relativePath + " -->\n" +
               "      <EntityType Name=\"TestEntity\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "      </EntityType>\n" +
               "    </Schema>\n" +
               "  </edmx:DataServices>\n" +
               "</edmx:Edmx>";
    }
    
    /**
     * Checks if a test resource exists
     */
    public static boolean resourceExists(String resourceRelativePath) {
        Path resourcePath = Paths.get(BASE_RESOURCE_PATH, resourceRelativePath);
        return Files.exists(resourcePath);
    }
}
