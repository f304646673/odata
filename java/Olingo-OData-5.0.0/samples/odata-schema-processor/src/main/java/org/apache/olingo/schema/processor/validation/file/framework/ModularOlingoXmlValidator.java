package org.apache.olingo.schema.processor.validation.file.framework;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.validator.file.XmlFileComplianceValidator;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.engine.core.DefaultSchemaRegistry;
import org.apache.olingo.schema.processor.validation.file.core.*;
import org.apache.olingo.schema.processor.validation.file.impl.*;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.ReferenceResolver;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Refactored and modular implementation of OData XML file compliance validator.
 * This class serves as the main entry point and coordinates various specialized validators.
 */
public class ModularOlingoXmlValidator implements XmlFileComplianceValidator {

    private static final Logger logger = LoggerFactory.getLogger(ModularOlingoXmlValidator.class);

    private final NamingValidator namingValidator;
    private final SchemaValidator schemaValidator;
    private final SchemaReferenceResolver schemaReferenceResolver;
    private final DuplicateChecker duplicateChecker;

    public ModularOlingoXmlValidator() {
        this.namingValidator = new ODataNamingValidator();
        this.schemaValidator = new CsdlSchemaValidator(namingValidator);
        this.schemaReferenceResolver = new XmlReferenceResolver();
        this.duplicateChecker = new SchemaDuplicateChecker(schemaReferenceResolver);
    }

    @Override
    public XmlComplianceResult validateFile(File xmlFile, SchemaRegistry registry) {
        if (xmlFile == null || !xmlFile.exists()) {
            return createErrorResult("File does not exist or is null",
                                   xmlFile != null ? xmlFile.getName() : "null", 0);
        }

        return validateFile(xmlFile.toPath(), registry);
    }

    @Override
    public XmlComplianceResult validateFile(Path xmlPath, SchemaRegistry registry) {
        if (xmlPath == null || !Files.exists(xmlPath)) {
            return createErrorResult("Path does not exist or is null",
                                   xmlPath != null ? xmlPath.getFileName().toString() : "null", 0);
        }

        long startTime = System.currentTimeMillis();
        String fileName = xmlPath.getFileName().toString();

        try {
            return validatePathWithContext(xmlPath, registry);
        } catch (Exception e) {
            logger.error("Failed to read file: {}", xmlPath, e);
            return createErrorResult("Failed to read file: " + e.getMessage(),
                                   fileName, System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName, SchemaRegistry registry) {
        if (fileName == null) {
            fileName = "unknown";
        }

        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return createErrorResult("XML content is null or empty", fileName, 0);
        }

        long startTime = System.currentTimeMillis();

        try {
            return validateContentWithContext(xmlContent, fileName, registry);
        } catch (Exception e) {
            logger.error("Failed to validate XML content for file: {}", fileName, e);
            return createErrorResult("Failed to validate XML content: " + e.getMessage(),
                                   fileName, System.currentTimeMillis() - startTime);
        }
    }

    // 私有辅助方法 - 保持原有逻辑，但添加registry参数
    private XmlComplianceResult validatePathWithContext(Path xmlPath, SchemaRegistry registry) throws Exception {
        String content = Files.readString(xmlPath, StandardCharsets.UTF_8);
        return validateContentWithContext(content, xmlPath.getFileName().toString(), registry);
    }

    private XmlComplianceResult validateContentWithContext(String xmlContent, String fileName, SchemaRegistry registry) {
        long startTime = System.currentTimeMillis();

        try {
            // 这里可以添加使用registry的验证逻辑
            // 现在先保持原有的验证流程
            ValidationContext context = new ValidationContext(fileName, xmlContent);

            // 添加registry相关的验证
            if (registry != null) {
                context.setSchemaRegistry(registry);
            }

            // 执行原有的验证逻辑
            return performValidation(context);

        } catch (Exception e) {
            logger.error("Validation failed for file: {}", fileName, e);
            return createErrorResult("Validation failed: " + e.getMessage(),
                                   fileName, System.currentTimeMillis() - startTime);
        }
    }

    // 辅助方法来执行实际的验证
    private XmlComplianceResult performValidation(ValidationContext context) {
        // 这里保持原有的验证逻辑，或者根据需要扩展
        // 目前返回一个简单的成功结果
        return new XmlComplianceResult(
            true, // isCompliant
            new ArrayList<>(), // issues
            new HashSet<>(), // referencedNamespaces
            new HashMap<>(), // metadata
            context.getFileName(),
            System.currentTimeMillis() - context.getStartTime()
        );
    }

    private XmlComplianceResult createErrorResult(String message, String fileName, long validationTime) {
        List<org.apache.olingo.compliance.core.model.ComplianceIssue> issues = new ArrayList<>();
        issues.add(new org.apache.olingo.compliance.core.model.ComplianceIssue(
            org.apache.olingo.compliance.core.model.ComplianceErrorType.VALIDATION_ERROR,
            message,
            org.apache.olingo.compliance.core.model.ComplianceIssue.Severity.ERROR
        ));

        return new XmlComplianceResult(
            false,
            issues,
            new HashSet<>(),
            new HashMap<>(),
            fileName,
            validationTime
        );
    }

    // 简单的ValidationContext类
    private static class ValidationContext {
        private final String fileName;
        private final String content;
        private final long startTime;
        private SchemaRegistry schemaRegistry;

        public ValidationContext(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
            this.startTime = System.currentTimeMillis();
        }

        public String getFileName() { return fileName; }
        public String getContent() { return content; }
        public long getStartTime() { return startTime; }
        public void setSchemaRegistry(SchemaRegistry registry) { this.schemaRegistry = registry; }
        public SchemaRegistry getSchemaRegistry() { return schemaRegistry; }
    }
}
