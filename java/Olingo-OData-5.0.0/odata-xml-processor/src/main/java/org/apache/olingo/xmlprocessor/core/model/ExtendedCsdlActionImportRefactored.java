package org.apache.olingo.xmlprocessor.core.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;

/**
 * 扩展的 CsdlActionImport，采用组合模式并继承通用基类
 * 使用组合模式包装CsdlActionImport，保持内部数据联动
 */
public class ExtendedCsdlActionImportRefactored 
        extends AbstractExtendedCsdlElement<CsdlActionImport, ExtendedCsdlActionImportRefactored> {
    
    public ExtendedCsdlActionImportRefactored() {
        super(new CsdlActionImport());
    }
    
    public ExtendedCsdlActionImportRefactored(CsdlActionImport csdlActionImport) {
        super(csdlActionImport != null ? csdlActionImport : new CsdlActionImport());
    }

    /**
     * 从标准CsdlActionImport创建ExtendedCsdlActionImport
     */
    public static ExtendedCsdlActionImportRefactored fromCsdlActionImport(CsdlActionImport source) {
        if (source == null) {
            return null;
        }

        ExtendedCsdlActionImportRefactored extended = new ExtendedCsdlActionImportRefactored();

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

    public ExtendedCsdlActionImportRefactored setName(String name) {
        wrappedElement.setName(name);
        return this;
    }

    public String getAction() {
        return wrappedElement.getAction();
    }

    public ExtendedCsdlActionImportRefactored setAction(String action) {
        wrappedElement.setAction(action);
        return this;
    }

    public String getEntitySet() {
        return wrappedElement.getEntitySet();
    }

    public ExtendedCsdlActionImportRefactored setEntitySet(String entitySet) {
        wrappedElement.setEntitySet(entitySet);
        return this;
    }

    public List<CsdlAnnotation> getAnnotations() {
        return wrappedElement.getAnnotations();
    }

    public ExtendedCsdlActionImportRefactored setAnnotations(List<CsdlAnnotation> annotations) {
        // 使用基类的通用处理方法
        return handleSetAnnotations(annotations);
    }

    // ==================== 抽象方法实现 ====================

    @Override
    protected List<CsdlAnnotation> getOriginalAnnotations() {
        return wrappedElement.getAnnotations();
    }

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
