# Spring Boot OData XML Native Sample

This sample demonstrates how to use **Olingo's native XML parsing capabilities** to load EDM (Entity Data Model) directly from XML files in a Spring Boot application.

## Key Features

- **Native XML Processing**: Uses Olingo's `MetadataParser` to parse XML metadata files directly
- **No Manual Parsing**: No need to write custom XML parsing code or manually define EDM programmatically
- **SchemaBasedEdmProvider**: Leverages Olingo's built-in `SchemaBasedEdmProvider` for XML-based EDM management
- **True "Native" Approach**: This is the proper way to load XML metadata in Olingo

## How It Works

1. **XML Metadata File**: The service metadata is defined in `src/main/resources/service-metadata.xml`
2. **MetadataParser**: Olingo's `MetadataParser` class parses the XML file and creates CSDL objects
3. **SchemaBasedEdmProvider**: The parsed schemas are added to a `SchemaBasedEdmProvider` instance
4. **OData Service**: The provider is used to create OData service metadata and handle requests

## Key Components

### NativeXmlEdmProvider
```java
public class NativeXmlEdmProvider extends SchemaBasedEdmProvider {
    private void loadMetadataFromXml() {
        MetadataParser parser = new MetadataParser();
        SchemaBasedEdmProvider xmlProvider = parser.buildEdmProvider(reader);
        
        // Copy schemas from XML provider to this provider
        List<CsdlSchema> schemas = xmlProvider.getSchemas();
        for (CsdlSchema schema : schemas) {
            this.addSchema(schema);
        }
    }
}
```

### NativeXmlODataController
```java
@RestController
@RequestMapping("/odata")
public class NativeXmlODataController {
    public NativeXmlODataController() {
        OData odata = OData.newInstance();
        NativeXmlEdmProvider edmProvider = new NativeXmlEdmProvider();
        ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
        oDataHttpHandler = odata.createHandler(serviceMetadata);
    }
}
```

## Dependencies Required

```xml
<dependency>
    <groupId>org.apache.olingo</groupId>
    <artifactId>odata-server-core-ext</artifactId>
    <version>5.0.0</version>
</dependency>
```

The `odata-server-core-ext` module contains the `MetadataParser` class.

## Building and Running

```bash
# Compile the project
mvn clean compile -f pom-standalone.xml

# Package the application
mvn clean package -f pom-standalone.xml -DskipTests

# Run the application
mvn spring-boot:run -f pom-standalone.xml
```

## Testing the Service

Once the application is running, you can test the OData service:

```bash
# Get service metadata
curl http://localhost:8080/odata/\$metadata

# Test if the service is accessible
curl http://localhost:8080/odata/
```

## Differences from Other Approaches

| Approach | Description | Complexity |
|----------|-------------|------------|
| **Manual XML Parsing** | Parse XML using DOM/SAX and manually create CSDL objects | High |
| **Programmatic EDM** | Define EDM entirely in Java code | Medium |
| **Native XML (This)** | Use Olingo's MetadataParser to load from XML | Low |

## Advantages

1. **Simple**: Only a few lines of code to load XML metadata
2. **Maintained**: Uses Olingo's official XML parsing capabilities
3. **Robust**: Handles all XML schema features automatically
4. **Efficient**: No need to maintain two representations (XML + Java)

## Example XML Metadata

The `service-metadata.xml` file contains standard OData CSDL:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<edmx:Edmx Version="4.0" xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx">
  <edmx:DataServices>
    <Schema Namespace="OData.Demo" xmlns="http://docs.oasis-open.org/odata/ns/edm">
      <EntityType Name="Car">
        <Key>
          <PropertyRef Name="Id"/>
        </Key>
        <Property Name="Id" Type="Edm.Int32" Nullable="false"/>
        <Property Name="Model" Type="Edm.String" MaxLength="60"/>
        <!-- ... more properties ... -->
      </EntityType>
      <!-- ... more entity types ... -->
    </Schema>
  </edmx:DataServices>
</edmx:Edmx>
```

This approach represents the most efficient and maintainable way to load OData metadata from XML files using Apache Olingo.
