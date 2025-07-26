package org.apache.olingo.schema.processor.exporter;

import java.io.IOException;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Container导出器接口
 * 用于将OData容器中的所有元素导出到XML文件
 */
public interface ContainerExporter {
    
    /**
     * 将容器导出为XML文件
     * @param container 要导出的容器
     * @param outputPath 输出文件路径
     * @param containerNamespace 容器所属的命名空间
     * @return 导出结果
     * @throws IOException 文件操作异常
     */
    ContainerExportResult exportContainer(CsdlEntityContainer container, String outputPath, String containerNamespace) throws IOException;
    
    /**
     * 从XML文件加载容器并导出为新的XML文件
     * @param inputXmlPath 输入XML文件路径
     * @param outputPath 输出文件路径
     * @param containerName 要导出的容器名称（如果为null则导出第一个容器）
     * @return 导出结果
     * @throws IOException 文件操作异常
     */
    ContainerExportResult exportContainerFromXml(String inputXmlPath, String outputPath, String containerName) throws IOException;
    
    /**
     * 动态创建容器并导出
     * @param containerBuilder 容器构建器
     * @param outputPath 输出文件路径
     * @return 导出结果
     * @throws IOException 文件操作异常
     */
    ContainerExportResult exportDynamicContainer(ContainerBuilder containerBuilder, String outputPath) throws IOException;
    
    /**
     * 将多个Schema中的容器合并导出
     * @param schemas Schema列表
     * @param outputPath 输出文件路径
     * @param targetNamespace 目标命名空间
     * @return 导出结果
     * @throws IOException 文件操作异常
     */
    ContainerExportResult exportMergedContainers(List<CsdlSchema> schemas, String outputPath, String targetNamespace) throws IOException;
    
    /**
     * 容器导出结果
     */
    class ContainerExportResult {
        private final boolean success;
        private final String outputPath;
        private final int exportedElementCount;
        private final List<String> exportedElements;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ContainerExportResult(boolean success, String outputPath, int exportedElementCount, 
                                   List<String> exportedElements, List<String> errors, List<String> warnings) {
            this.success = success;
            this.outputPath = outputPath;
            this.exportedElementCount = exportedElementCount;
            this.exportedElements = exportedElements;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isSuccess() { return success; }
        public String getOutputPath() { return outputPath; }
        public int getExportedElementCount() { return exportedElementCount; }
        public List<String> getExportedElements() { return exportedElements; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        @Override
        public String toString() {
            return "ContainerExportResult{" +
                "success=" + success +
                ", outputPath='" + outputPath + '\'' +
                ", exportedElementCount=" + exportedElementCount +
                ", errors=" + errors.size() +
                ", warnings=" + warnings.size() +
                '}';
        }
    }
    
    /**
     * 容器构建器接口
     */
    interface ContainerBuilder {
        /**
         * 构建容器
         * @return 构建的容器和Schema
         */
        ContainerBuildResult build();
        
        /**
         * 容器构建结果
         */
        class ContainerBuildResult {
            private final CsdlEntityContainer container;
            private final CsdlSchema schema;
            private final String namespace;
            
            public ContainerBuildResult(CsdlEntityContainer container, CsdlSchema schema, String namespace) {
                this.container = container;
                this.schema = schema;
                this.namespace = namespace;
            }
            
            public CsdlEntityContainer getContainer() { return container; }
            public CsdlSchema getSchema() { return schema; }
            public String getNamespace() { return namespace; }
        }
    }
}
