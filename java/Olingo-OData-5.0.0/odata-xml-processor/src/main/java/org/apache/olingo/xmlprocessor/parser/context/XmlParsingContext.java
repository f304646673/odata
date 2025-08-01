package org.apache.olingo.xmlprocessor.parser.context;

import org.apache.olingo.xmlprocessor.core.dependency.DependencyManager;
import org.apache.olingo.xmlprocessor.core.dependency.impl.CsdlDependencyManagerImpl;

/**
 * XML解析上下文
 * 包含解析过程中需要的各种依赖管理器和配置信息
 */
public class XmlParsingContext {
    
    private final DependencyManager dependencyManager;
    
    /**
     * 构造函数 - 使用默认的依赖管理器
     */
    public XmlParsingContext() {
        this.dependencyManager = new CsdlDependencyManagerImpl();
    }
    
    /**
     * 构造函数 - 使用指定的依赖管理器
     * @param dependencyManager 依赖管理器
     */
    public XmlParsingContext(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager != null ? dependencyManager : new CsdlDependencyManagerImpl();
    }
    
    /**
     * 获取依赖管理器
     * @return 依赖管理器
     */
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }
    
    /**
     * 清理上下文
     */
    public void clear() {
        dependencyManager.clear();
    }
}
