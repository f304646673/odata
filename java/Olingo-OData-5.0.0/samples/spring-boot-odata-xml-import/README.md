# Spring Boot OData XML Import Sample

This sample demonstrates how to create an OData service that automatically resolves and loads XML schema references.

## Key Features

1. **Automatic XML Import Resolution**: The application starts by loading only `main-schema.xml` and automatically discovers and loads referenced XML files.

2. **EDM Reference Support**: Uses `edmx:Reference` elements to declare dependencies between XML files.

3. **Recursive Loading**: Supports recursive reference resolution - referenced files can reference other files.

4. **Duplicate Prevention**: Prevents loading the same XML file multiple times.

5. **Cross-Namespace Types**: Supports using types from different namespaces across XML files.

## Project Structure

```
src/main/
├── java/org/apache/olingo/sample/springboot/xmlimport/
│   ├── XmlImportODataApplication.java      # Main Spring Boot application
│   ├── controller/
│   │   └── XmlImportODataController.java   # OData request handler
│   ├── edm/
│   │   └── AdvancedXmlImportEdmProvider.java       # EDM provider with auto-import
│   ├── processor/
│   │   └── XmlImportEntityProcessor.java   # Entity request processor
│   └── data/
│       └── XmlImportDataProvider.java      # Sample data provider
└── resources/
    ├── main-schema.xml                     # Main schema with references
    ├── address-schema.xml                  # Referenced schema
    └── application.properties              # Application configuration
```

## How It Works

### 1. Schema File Structure

**main-schema.xml** - Contains entity types and references:
```xml
<edmx:Edmx xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx" Version="4.0">
    <edmx:DataServices>
        <!-- Reference to external schema -->
        <edmx:Reference Uri="address-schema.xml">
            <edmx:Include Namespace="OData.Demo.Common"/>
        </edmx:Reference>
        
        <Schema xmlns="http://docs.oasis-open.org/odata/ns/edm" Namespace="OData.Demo">
            <!-- Entity types that use Address complex type -->
            <EntityType Name="Manufacturer">
                <Property Name="Address" Type="OData.Demo.Common.Address"/>
            </EntityType>
        </Schema>
    </edmx:DataServices>
</edmx:Edmx>
```

**address-schema.xml** - Contains complex types:
```xml
<edmx:Edmx xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx" Version="4.0">
    <edmx:DataServices>
        <Schema xmlns="http://docs.oasis-open.org/odata/ns/edm" Namespace="OData.Demo.Common">
            <ComplexType Name="Address">
                <Property Name="Street" Type="Edm.String"/>
                <Property Name="City" Type="Edm.String"/>
                <Property Name="ZipCode" Type="Edm.String"/>
                <Property Name="Country" Type="Edm.String"/>
            </ComplexType>
        </Schema>
    </edmx:DataServices>
</edmx:Edmx>
```

### 2. Automatic Import Process

The `AdvancedXmlImportEdmProvider` class:

1. **Starts with main file**: Loads `main-schema.xml` as the entry point
2. **Parses references**: Uses DOM4J to extract `edmx:Reference` elements
3. **Loads recursively**: For each referenced file, repeats the process
4. **Prevents duplicates**: Tracks loaded files to avoid circular references
5. **Merges schemas**: Combines all schemas into a single EDM model

### 3. Key Components

- **AdvancedXmlImportEdmProvider**: Core logic for automatic XML import resolution
- **XmlImportEntityProcessor**: Handles entity and entity collection requests
- **XmlImportDataProvider**: Provides sample data with complex Address types
- **XmlImportODataController**: Spring MVC controller for OData requests

## Running the Application

1. **Build the project**:
   ```bash
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Access the service**:
   - Service document: http://localhost:8080/cars.svc/
   - Metadata document: http://localhost:8080/cars.svc/$metadata
   - Cars collection: http://localhost:8080/cars.svc/Cars
   - Manufacturers collection: http://localhost:8080/cars.svc/Manufacturers

## Testing the Import Resolution

When the application starts, check the logs for:

```
INFO  - Loading XML file: main-schema.xml
DEBUG - Found reference in main-schema.xml: address-schema.xml
INFO  - Loading XML file: address-schema.xml
DEBUG - Added schema from address-schema.xml: OData.Demo.Common
DEBUG - Added schema from main-schema.xml: OData.Demo
INFO  - Created AdvancedXmlImportEdmProvider with loaded files: [address-schema.xml, main-schema.xml]
```

## Advanced Features

### Adding More References

To add more XML files, simply add `edmx:Reference` elements to any existing XML file:

```xml
<edmx:Reference Uri="product-schema.xml">
    <edmx:Include Namespace="OData.Demo.Products"/>
</edmx:Reference>
```

### Circular Reference Prevention

The system automatically prevents circular references by tracking loaded files.

### Cross-Namespace Usage

Types from any loaded namespace can be used in any other namespace:

```xml
<Property Name="ProductAddress" Type="OData.Demo.Common.Address"/>
<Property Name="ProductInfo" Type="OData.Demo.Products.ProductDetails"/>
```

## Benefits

1. **Modular Design**: Split complex schemas into focused, reusable files
2. **Automatic Discovery**: No need to manually specify all files
3. **Maintainability**: Easy to add new schema files without code changes
4. **Reusability**: Common types can be shared across multiple schemas
5. **Flexibility**: Supports complex reference hierarchies

This approach is ideal for large OData services where schema modularity and maintainability are important.
