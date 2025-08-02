package org.apache.olingo.compliance.validator;

import java.io.File;
import java.nio.file.Path;

import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;

/**
 * 统一的合规性验证器接口，支持文件和目录的验证
 *
 * 该接口提供了统一的API来验证：
 * 1. 单个XML文件
 * 2. 整个目录及其子目录中的所有XML文件
 * 3. XML内容字符串
 *
 * 所有验证方法都要求传入SchemaRegistry以支持跨文件引用验证
 */
public interface ComplianceValidator {

    /**
     * 验证单个XML文件
     *
     * @param xmlFile 要验证的XML文件
     * @param registry Schema注册表，包含已知类型定义用于跨文件引用验证
     * @return 验证结果，包含单个文件的验证信息
     */
    ComplianceResult validateFile(File xmlFile, SchemaRegistry registry);

    /**
     * 验证单个XML文件
     *
     * @param xmlPath 要验证的XML文件路径
     * @param registry Schema注册表，包含已知类型定义用于跨文件引用验证
     * @return 验证结果，包含单个文件的验证信息
     */
    ComplianceResult validateFile(Path xmlPath, SchemaRegistry registry);

    /**
     * 验证XML内容字符串
     *
     * @param xmlContent XML内容字符串
     * @param fileName 可选的文件名，用于错误报告
     * @param registry Schema注册表，包含已知类型定义用于跨文件引用验证
     * @return 验证结果，包含内容验证信息
     */
    ComplianceResult validateContent(String xmlContent, String fileName, SchemaRegistry registry);

    /**
     * 验证整个目录及其子目录中的所有XML文件
     *
     * @param directoryPath 要验证的目录路径
     * @param registry Schema注册表，包含已知类型定义用于跨文件引用验证
     * @return 验证结果，包含目录中所有文件的聚合验证信息
     */
    ComplianceResult validateDirectory(String directoryPath, SchemaRegistry registry);

    /**
     * 验证整个目录及其子目录中的所有XML文件
     *
     * @param directoryPath 要验证的目录路径
     * @param registry Schema注册表，包含已知类型定义用于跨文件引用验证
     * @return 验证结果，包含目录中所有文件的聚合验证信息
     */
    ComplianceResult validateDirectory(Path directoryPath, SchemaRegistry registry);

    /**
     * 验证整个目录及其子目录中的所有XML文件，支持启用/禁用跨文件验证
     *
     * @param directoryPath 要验证的目录路径
     * @param registry Schema注册表，包含已知类型定义用于跨文件引用验证
     * @param enableCrossFileValidation 是否启用跨文件验证功能
     * @return 验证结果，包含目录中所有文件的聚合验证信息
     */
    ComplianceResult validateDirectory(String directoryPath, SchemaRegistry registry, boolean enableCrossFileValidation);

    /**
     * 验证整个目录及其子目录中的所有XML文件，支持启用/禁用跨文件验证
     *
     * @param directoryPath 要验证的目录路径
     * @param registry Schema注册表，包含已知类型定义用于跨文件引用验证
     * @param enableCrossFileValidation 是否启用跨文件验证功能
     * @return 验证结果，包含目录中所有文件的聚合验证信息
     */
    ComplianceResult validateDirectory(Path directoryPath, SchemaRegistry registry, boolean enableCrossFileValidation);
}
