package org.apache.olingo.advanced.xmlparser.statistics;

/**
 * Comprehensive error information structure
 */
public class ErrorInfo {
    private final ErrorType type;
    private final String description;
    private final String context;
    private final Throwable cause;
    private final long timestamp;
    private final String threadName;

    /**
     * Create error info with type and description
     */
    public ErrorInfo(ErrorType type, String description) {
        this(type, description, null, null);
    }

    /**
     * Create error info with type, description and context
     */
    public ErrorInfo(ErrorType type, String description, String context) {
        this(type, description, context, null);
    }

    /**
     * Create error info with all details
     */
    public ErrorInfo(ErrorType type, String description, String context, Throwable cause) {
        this.type = type;
        this.description = description;
        this.context = context;
        this.cause = cause;
        this.timestamp = System.currentTimeMillis();
        this.threadName = Thread.currentThread().getName();
    }

    // Getters
    public ErrorType getType() { return type; }
    public String getDescription() { return description; }
    public String getContext() { return context; }
    public Throwable getCause() { return cause; }
    public long getTimestamp() { return timestamp; }
    public String getThreadName() { return threadName; }

    /**
     * Get formatted error message
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type.name()).append("] ");
        sb.append(description);
        if (context != null && !context.trim().isEmpty()) {
            sb.append(" (Context: ").append(context).append(")");
        }
        if (cause != null) {
            sb.append(" - Caused by: ").append(cause.getMessage());
        }
        return sb.toString();
    }

    /**
     * Get detailed error information including timestamp and thread
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(new java.util.Date(timestamp)).append("] ");
        sb.append("[Thread: ").append(threadName).append("] ");
        sb.append(getFormattedMessage());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ErrorInfo errorInfo = (ErrorInfo) obj;
        return type == errorInfo.type &&
                java.util.Objects.equals(description, errorInfo.description) &&
                java.util.Objects.equals(context, errorInfo.context);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, description, context);
    }
}
