package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;

/**
 * 扩展的 CsdlNavigationPropertyBinding，采用组合模式
 * 使用组合模式包装CsdlNavigationPropertyBinding，保持内部数据联动
 */
public class ExtendedCsdlNavigationPropertyBinding {
    
    private final CsdlNavigationPropertyBinding wrappedBinding;
    
    public ExtendedCsdlNavigationPropertyBinding() {
        this.wrappedBinding = new CsdlNavigationPropertyBinding();
    }
    
    public ExtendedCsdlNavigationPropertyBinding(CsdlNavigationPropertyBinding csdlBinding) {
        this.wrappedBinding = csdlBinding != null ? csdlBinding : new CsdlNavigationPropertyBinding();
    }

    /**
     * 从标准CsdlNavigationPropertyBinding创建ExtendedCsdlNavigationPropertyBinding
     */
    public static ExtendedCsdlNavigationPropertyBinding fromCsdlNavigationPropertyBinding(CsdlNavigationPropertyBinding source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlNavigationPropertyBinding extended = new ExtendedCsdlNavigationPropertyBinding();

        // 复制基本属性
        extended.setPath(source.getPath());
        extended.setTarget(source.getTarget());

        return extended;
    }
    
    // 获取内部包装的对象
    public CsdlNavigationPropertyBinding asCsdlNavigationPropertyBinding() {
        return wrappedBinding;
    }
    
    // ==================== CsdlNavigationPropertyBinding 方法委托 ====================
    
    public String getPath() {
        return wrappedBinding.getPath();
    }

    public ExtendedCsdlNavigationPropertyBinding setPath(String path) {
        wrappedBinding.setPath(path);
        return this;
    }

    public String getTarget() {
        return wrappedBinding.getTarget();
    }

    public ExtendedCsdlNavigationPropertyBinding setTarget(String target) {
        wrappedBinding.setTarget(target);
        return this;
    }
}
