# Advanced OData 4.0 XML Parser

Advanced OData 4.0 XML Schema Parser with enhanced comparison and validation capabilities.

## Overview

This project provides an advanced OData 4.0 XML schema parser that builds upon Apache Olingo's core functionality with additional features:

- **Enhanced Schema Comparison**: Strict OData 4.0 spec-compliant comparison logic for container elements
- **Advanced Dependency Management**: Robust handling of schema dependencies and circular references
- **Comprehensive Error Reporting**: Detailed error information with multiple error types and contexts
- **Flexible Configuration**: Configurable parsing options and reference resolvers

## Key Features

### Container Element Comparison

The parser provides strict comparison logic for OData 4.0 container elements:

- **EntitySets**: Compares Name, EntityType, IncludeInServiceDocument, and NavigationPropertyBindings
- **ActionImports**: Compares Name, Action, and EntitySet
- **FunctionImports**: Compares Name, Function, EntitySet, and IncludeInServiceDocument  
- **Singletons**: Compares Name, Type, and NavigationPropertyBindings
- **NavigationPropertyBindings**: Compares Path/Target pairs

### Advanced Error Handling

- Multiple error types (parsing, dependency analysis, schema merge conflicts, etc.)
- Contextual error information with timestamps and thread information
- Comprehensive error reporting and statistics

### Dependency Management

- Automatic dependency graph building
- Circular dependency detection
- Configurable dependency resolution strategies
- Topological sorting for optimal loading order

## Usage

```java
import org.apache.olingo.advanced.xmlparser.AdvancedMetadataParser;

// Create parser instance
AdvancedMetadataParser parser = new AdvancedMetadataParser()
    .detectCircularDependencies(true)
    .allowCircularDependencies(false)
    .enableCaching(true)
    .maxDependencyDepth(10);

// Parse schema with advanced features
SchemaBasedEdmProvider provider = parser.buildEdmProvider("path/to/schema.xml");

// Get parsing statistics and error information
ParseStatistics stats = parser.getStatistics();
System.out.println("Files processed: " + stats.getTotalFilesProcessed());
System.out.println("Errors detected: " + stats.getTotalErrorCount());
```

## Testing

The project includes comprehensive unit tests:

- `AdvancedMetadataParserTest`: Tests core parser functionality
- `ContainerElementComparisonTests`: Tests OData 4.0 spec-compliant comparison logic

Run tests with:
```bash
mvn test
```

## Build

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Package the project
mvn package
```

## Dependencies

- Apache Olingo OData 5.0.0
- JUnit 5.10.0 for testing
- SLF4J 1.7.36 for logging
- Jackson 2.15.2 for JSON processing

## License

Licensed under the Apache License, Version 2.0.
