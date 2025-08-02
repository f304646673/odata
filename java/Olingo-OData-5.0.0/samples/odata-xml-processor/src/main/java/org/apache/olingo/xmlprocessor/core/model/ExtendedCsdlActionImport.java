package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement.impl.AbstractExtendedCsdlElement;

/**
 * 扩展的 CsdlActionImport，采用组合模式并继承通用基类
 * 使用组合模式包装CsdlActionImport，保持内部数据联动
 */
public class ExtendedCsdlActionImport
        extends AbstractExtendedCsdlElement<CsdlActionImport, ExtendedCsdlActionImport> {
    
    public ExtendedCsdlActionImport() {
        super(new CsdlActionImport());
    }
    
    public ExtendedCsdlActionImport(CsdlActionImport csdlActionImport) {
        super(csdlActionImport != null ? csdlActionImport : new CsdlActionImport());
    }

    /**
     * 从标准CsdlActionImport创建ExtendedCsdlActionImport
     */
    public static ExtendedCsdlActionImport fromCsdlActionImport(CsdlActionImport source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlActionImport extended = new ExtendedCsdlActionImport();

        // 复制基本属性
        extended.setName(source.getName());
        extended.setAction(source.getAction());
        extended.setEntitySet(source.getEntitySet());

        // 使用基类的工具方法复制Annotations
        extended.copyAnnotationsFrom(source.getAnnotations());

        return extended;
    }
    
    // 获取内部包装的对象
    public CsdlActionImport asCsdlActionImport() {
        return wrappedElement;
    }
    
    // ==================== CsdlActionImport 方法委托 ====================
    
    public String getName() {
        return wrappedElement.getName();
    }

    public ExtendedCsdlActionImport setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getAction() {
        return wrappedElement.getAction();
    }

    public ExtendedCsdlActionImport setAction(String action) {
        wrappedElement.setAction(action);
        return this;
    }

    public String getEntitySet() {
        return wrappedElement.getEntitySet();
    }

    public ExtendedCsdlActionImport setEntitySet(String entitySet) {
        wrappedElement.setEntitySet(entitySet);
        return this;
    }

    @Deprecated
    public List<CsdlAnnotation> getAnnotations() {
        return wrappedElement.getAnnotations();
    }

    public ExtendedCsdlActionImport setAnnotations(List<CsdlAnnotation> annotations) {
        // 使用基类的通用处理方法
        return handleSetAnnotations(annotations);
    }

    // ==================== 抽象方法实现 ====================

    @Deprecated
    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        return wrappedElement.getAnnotations();
    }

    @Deprecated
    @Override
    protected void setOriginalAnnotations(List<CsdlAnnotation> annotations) {
        wrappedElement.setAnnotations(annotations);
    }

    // ==================== ExtendedCsdlElement 接口实现 ====================

    @Override
    public String getElementId() {
        if (getName() != null) {
            return getName();
        }
        return "ActionImport_" + hashCode();
    }

    @Override
    public FullQualifiedName getElementFullyQualifiedName() {
        return new FullQualifiedName(getNamespace(), getName());
    }

    @Override
    public CsdlDependencyNode.DependencyType getElementDependencyType() {
        return CsdlDependencyNode.DependencyType.ACTION_IMPORT_REFERENCE;
    }

    @Override
    public String getElementPropertyName() {
        return getName();
    }
}
