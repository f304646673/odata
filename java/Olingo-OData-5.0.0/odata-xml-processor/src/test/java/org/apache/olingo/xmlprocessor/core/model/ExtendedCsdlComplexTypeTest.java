package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlComplexTypeRefactored单元测试
 * 验证重构后的基类继承功能
 */
public class ExtendedCsdlComplexTypeTest {

    @Test
    void testBasicConstruction() {
        ExtendedCsdlComplexType extComplexType = new ExtendedCsdlComplexType();
        assertNotNull(extComplexType);
        assertNotNull(extComplexType.getWrappedElement());
        assertNotNull(extComplexType.getExtendedAnnotations());
    }

    @Test
    void testConstructionWithElementId() {
        ExtendedCsdlComplexType extComplexType = new ExtendedCsdlComplexType("test-element-id");
        assertEquals("test-element-id", extComplexType.getElementId());
    }

    @Test
    void testFromCsdlComplexType() {
        CsdlComplexType csdlComplexType = new CsdlComplexType();
        csdlComplexType.setName("Address");
        csdlComplexType.setAbstract(false);
        csdlComplexType.setOpenType(true);

        ExtendedCsdlComplexType extended = ExtendedCsdlComplexType.fromCsdlComplexType(csdlComplexType);
        
        assertNotNull(extended);
        assertEquals("Address", extended.getName());
        assertFalse(extended.isAbstract());
        assertTrue(extended.isOpenType());
    }

    @Test
    void testAnnotationsInheritanceFromBaseClass() {
        ExtendedCsdlComplexType extComplexType = new ExtendedCsdlComplexType();
        
        // 测试基类提供的注解功能
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("test.complex.annotation");
        
        extComplexType.addExtendedAnnotation(annotation);
        
        assertEquals(1, extComplexType.getExtendedAnnotations().size());
        assertEquals("test.complex.annotation", extComplexType.getExtendedAnnotations().get(0).getTerm());
        
        // 验证同步到原始对象
        assertNotNull(extComplexType.asCsdlComplexType().getAnnotations());
        assertEquals(1, extComplexType.asCsdlComplexType().getAnnotations().size());
    }

    @Test
    void testFluentApiFromBaseClass() {
        ExtendedCsdlComplexType extComplexType = new ExtendedCsdlComplexType();
        
        // 测试基类提供的流式API
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("fluent.complex");
        
        ExtendedCsdlComplexType result = extComplexType
            .setName("FluentComplexType")
            .setAbstract(true)
            .addExtendedAnnotation(annotation);
        
        // 验证流式API返回相同实例
        assertSame(extComplexType, result);
        assertEquals("FluentComplexType", result.getName());
        assertTrue(result.isAbstract());
        assertEquals(1, result.getExtendedAnnotations().size());
    }

    @Test
    void testNullSafety() {
        ExtendedCsdlComplexType extended = ExtendedCsdlComplexType.fromCsdlComplexType(null);
        assertNull(extended);
        
        ExtendedCsdlComplexType extComplexType = new ExtendedCsdlComplexType();
        
        // 测试空baseType设置
        extComplexType.setBaseType((String)null);
        assertNull(extComplexType.getBaseType());
        
        extComplexType.setBaseType("");
        assertNull(extComplexType.getBaseType());
        
        extComplexType.setBaseType("  ");
        assertNull(extComplexType.getBaseType());
    }

    @Test
    void testComplexFromCsdlConversion() {
        CsdlComplexType csdlComplexType = new CsdlComplexType();
        csdlComplexType.setName("ComplexAddress");
        csdlComplexType.setAbstract(true);
        csdlComplexType.setOpenType(false);
        csdlComplexType.setBaseType("base.ComplexType");
        
        // 添加注解
        List<CsdlAnnotation> annotations = new ArrayList<>();
        CsdlAnnotation annotation = new CsdlAnnotation();
        annotation.setTerm("test.complex.annotation");
        annotations.add(annotation);
        csdlComplexType.setAnnotations(annotations);
        
        // 添加属性
        List<CsdlProperty> properties = new ArrayList<>();
        CsdlProperty property = new CsdlProperty();
        property.setName("Street");
        properties.add(property);
        csdlComplexType.setProperties(properties);

        ExtendedCsdlComplexType extended = ExtendedCsdlComplexType.fromCsdlComplexType(csdlComplexType);
        
        assertNotNull(extended);
        assertEquals("ComplexAddress", extended.getName());
        assertTrue(extended.isAbstract());
        assertFalse(extended.isOpenType());
        assertEquals("base.ComplexType", extended.getBaseType());
        
        // 验证注解转换
        assertEquals(1, extended.getExtendedAnnotations().size());
        assertEquals("test.complex.annotation", extended.getExtendedAnnotations().get(0).getTerm());
        
        // 验证属性
        assertEquals(1, extended.getProperties().size());
        assertEquals("Street", extended.getProperties().get(0).getName());
    }

    @Test
    void testElementInterface() {
        ExtendedCsdlComplexType extComplexType = new ExtendedCsdlComplexType();
        extComplexType.setName("TestComplexType");
        extComplexType.setNamespace("test.namespace");
        
        assertEquals("TestComplexType", extComplexType.getElementId());
        assertEquals("TestComplexType", extComplexType.getElementPropertyName());
        assertEquals("test.namespace", extComplexType.getNamespace());
        
        assertNotNull(extComplexType.getElementFullyQualifiedName());
        assertEquals("test.namespace.TestComplexType", extComplexType.getElementFullyQualifiedName().getFullQualifiedNameAsString());
    }

    @Test
    void testElementIdFallback() {
        ExtendedCsdlComplexType extComplexType = new ExtendedCsdlComplexType();
        
        // 没有名称时，应该返回基于hashCode的ID
        String elementId = extComplexType.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("ComplexType_"));
    }
}
