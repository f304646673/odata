package org.apache.olingo.schema.processor.exporter;

/**
 * Result of an export operation
 */
public class ExportResult {
    private final boolean success;
    private final String message;
    private final int dependenciesProcessed;
    private final int schemasProcessed;

    public ExportResult(boolean success, String message, int dependenciesProcessed, int schemasProcessed) {
        this.success = success;
        this.message = message;
        this.dependenciesProcessed = dependenciesProcessed;
        this.schemasProcessed = schemasProcessed;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getDependenciesProcessed() {
        return dependenciesProcessed;
    }

    public int getSchemasProcessed() {
        return schemasProcessed;
    }

    @Override
    public String toString() {
        return String.format("ExportResult{success=%s, message='%s', dependencies=%d, schemas=%d}",
                success, message, dependenciesProcessed, schemasProcessed);
    }
}
