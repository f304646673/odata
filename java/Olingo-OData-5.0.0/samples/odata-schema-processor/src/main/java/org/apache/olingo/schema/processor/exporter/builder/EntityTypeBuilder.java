package org.apache.olingo.schema.processor.exporter.builder;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

/**
 * 实体类型构建器接口
 */
@FunctionalInterface
public interface EntityTypeBuilder {
    /**
     * 添加属性
     */
    default void addProperty(String name, String type, Boolean nullable, Integer maxLength) {
        // 默认实现 - 由具体实现覆盖
    }
    
    /**
     * 设置主键
     */
    default void setKey(String... keyNames) {
        // 默认实现 - 由具体实现覆盖
    }
    
    /**
     * 构建实体类型
     */
    void build(EntityTypeBuilderContext context);
    
    /**
     * 实体类型构建上下文
     */
    interface EntityTypeBuilderContext {
        void addProperty(String name, String type, Boolean nullable, Integer maxLength);
        void setKey(String... keyNames);
        List<CsdlProperty> getProperties();
        List<CsdlPropertyRef> getKeys();
    }
}
