package org.apache.olingo.advanced.xmlparser.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一的操作结果类，支持错误分类和详细信息
 */
public class OperationResult {
    private final List<ResultItem> items;
    private final OperationType operationType;
    private final Map<String, Object> metadata;
    private String context;

    public OperationResult() {
        this.items = new ArrayList<>();
        this.operationType = OperationType.VALIDATION;
        this.metadata = new HashMap<>();
    }

    public OperationResult(OperationType operationType) {
        this.items = new ArrayList<>();
        this.operationType = operationType;
        this.metadata = new HashMap<>();
    }

    public OperationResult(OperationType operationType, String context) {
        this.items = new ArrayList<>();
        this.operationType = operationType;
        this.metadata = new HashMap<>();
        this.context = context;
    }

    public static class ResultItem {
        private final ResultType type;
        private final String message;
        private String context;
        private final Map<String, Object> metadata;

        public ResultItem(ResultType type, String message) {
            this.type = type;
            this.message = message;
            this.metadata = new HashMap<>();
        }

        public ResultItem(ResultType type, String message, String context) {
            this.type = type;
            this.message = message;
            this.context = context;
            this.metadata = new HashMap<>();
        }

        public ResultType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void addMetadata(String key, Object value) {
            this.metadata.put(key, value);
        }

        public boolean isError() {
            return type.isError();
        }

        public boolean isWarning() {
            return type.isWarning();
        }

        public boolean isInfo() {
            return type.isInfo();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s%s", type, message, 
                context != null ? " (Context: " + context + ")" : "");
        }
    }

    // 添加不同类型的结果项
    public void addError(ResultType type, String message) {
        items.add(new ResultItem(type, message));
    }

    public void addError(ResultType type, String message, String context) {
        items.add(new ResultItem(type, message, context));
    }

    public void addWarning(ResultType type, String message) {
        items.add(new ResultItem(type, message));
    }

    public void addWarning(ResultType type, String message, String context) {
        items.add(new ResultItem(type, message, context));
    }

    public void addInfo(ResultType type, String message) {
        items.add(new ResultItem(type, message));
    }

    public void addInfo(ResultType type, String message, String context) {
        items.add(new ResultItem(type, message, context));
    }

    // 兼容旧方法
    public void addError(String message) {
        addError(ResultType.VALIDATION_FAILED, message);
    }

    public void addWarning(String message) {
        addWarning(ResultType.SCHEMA_WARNING, message);
    }

    public void addInfo(String message) {
        addInfo(ResultType.SUCCESS, message);
    }

    // 检查方法
    public boolean hasErrors() {
        return items.stream().anyMatch(item -> item.getType().isError());
    }

    public boolean hasWarnings() {
        return items.stream().anyMatch(item -> item.getType().isWarning());
    }

    public boolean hasResultType(ResultType type) {
        return items.stream().anyMatch(item -> item.getType() == type);
    }

    public boolean isSuccessful() {
        return !hasErrors();
    }

    // 获取不同类型的结果
    public List<ResultItem> getErrors() {
        return items.stream()
                .filter(item -> item.getType().isError())
                .collect(Collectors.toList());
    }

    public List<ResultItem> getWarnings() {
        return items.stream()
                .filter(item -> item.getType().isWarning())
                .collect(Collectors.toList());
    }

    public List<ResultItem> getInfos() {
        return items.stream()
                .filter(item -> item.getType().isInfo())
                .collect(Collectors.toList());
    }

    public List<ResultItem> getItems() {
        return new ArrayList<>(items);
    }

    public List<ResultItem> getResultsByType(ResultType type) {
        return items.stream()
                .filter(item -> item.getType() == type)
                .collect(Collectors.toList());
    }

    // 获取器和设置器
    public OperationType getOperationType() {
        return operationType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    // 合并操作
    public void merge(OperationResult other) {
        if (other != null) {
            this.items.addAll(other.items);
            this.metadata.putAll(other.metadata);
        }
    }

    // 兼容方法 - 用于替代 MergeResult 和 ValidationResult
    public List<String> getErrorMessages() {
        return getErrors().stream()
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
    }

    public List<String> getWarningMessages() {
        return getWarnings().stream()
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
    }

    public List<String> getMessages() {
        return getInfos().stream()
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
    }

    // 为了向后兼容 ValidationResult 的构造和方法
    public static OperationResult createValidationResult() {
        return new OperationResult(OperationType.VALIDATION);
    }

    // 为了向后兼容 MergeResult 的构造和方法
    public static OperationResult createMergeResult() {
        return new OperationResult(OperationType.MERGE);
    }

    // 支持 ValidationResult 的特定功能
    private OperationResult validationResult;

    public void setValidationResult(OperationResult validationResult) {
        this.validationResult = validationResult;
        if (validationResult != null && validationResult.hasErrors()) {
            addError(ResultType.MERGE_VALIDATION_FAILED, "Pre-merge validation failed");
        }
    }

    public OperationResult getValidationResult() {
        return validationResult;
    }

    // 摘要信息
    public String getSummary() {
        int errorCount = getErrors().size();
        int warningCount = getWarnings().size();
        int infoCount = getInfos().size();
        
        return String.format("Operation: %s, Errors: %d, Warnings: %d, Info: %d",
                operationType, errorCount, warningCount, infoCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append("\n");
        
        if (!items.isEmpty()) {
            sb.append("Details:\n");
            for (ResultItem item : items) {
                sb.append("  ").append(item.toString()).append("\n");
            }
        }
        
        return sb.toString();
    }
}
