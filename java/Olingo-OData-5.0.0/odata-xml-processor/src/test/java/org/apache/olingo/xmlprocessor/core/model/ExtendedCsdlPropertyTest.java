package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlPropertyRefactored单元测试
 * 验证重构后的基类继承功能
 */
public class ExtendedCsdlPropertyTest {

    @Test
    void testBasicConstruction() {
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        assertNotNull(extProperty);
        assertNotNull(extProperty.getWrappedElement());
        assertNotNull(extProperty.getExtendedAnnotations());
    }

    @Test
    void testFromCsdlProperty() {
        CsdlProperty csdlProperty = new CsdlProperty();
        csdlProperty.setName("TestProperty");
        csdlProperty.setType("Edm.String");
        csdlProperty.setCollection(false);
        csdlProperty.setNullable(true);
        csdlProperty.setMaxLength(100);

        ExtendedCsdlProperty extended = ExtendedCsdlProperty.fromCsdlProperty(csdlProperty);
        
        assertNotNull(extended);
        assertEquals("TestProperty", extended.getName());
        assertEquals("Edm.String", extended.getType());
        assertFalse(extended.isCollection());
        assertTrue(extended.isNullable());
        assertEquals(Integer.valueOf(100), extended.getMaxLength());
    }

    @Test
    void testAnnotationsInheritanceFromBaseClass() {
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        
        // 测试基类提供的注解功能
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("test.property.annotation");
        
        extProperty.addExtendedAnnotation(annotation);
        
        assertEquals(1, extProperty.getExtendedAnnotations().size());
        assertEquals("test.property.annotation", extProperty.getExtendedAnnotations().get(0).getTerm());
        
        // 验证同步到原始对象
        assertNotNull(extProperty.asCsdlProperty().getAnnotations());
        assertEquals(1, extProperty.asCsdlProperty().getAnnotations().size());
    }

    @Test
    void testFluentApiFromBaseClass() {
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        
        // 测试基类提供的流式API
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("fluent.property");
        
        ExtendedCsdlProperty result = extProperty
            .setName("FluentProperty")
            .setType("Edm.Int32")
            .setNullable(false)
            .addExtendedAnnotation(annotation);
        
        // 验证流式API返回相同实例
        assertSame(extProperty, result);
        assertEquals("FluentProperty", result.getName());
        assertEquals("Edm.Int32", result.getType());
        assertFalse(result.isNullable());
        assertEquals(1, result.getExtendedAnnotations().size());
    }

    @Test
    void testPropertyAttributes() {
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        
        // 测试各种属性设置
        extProperty
            .setName("ComplexProperty")
            .setType(new FullQualifiedName("Test.Namespace", "CustomType"))
            .setCollection(true)
            .setNullable(false)
            .setMaxLength(255)
            .setPrecision(10)
            .setScale(2)
            .setUnicode(true)
            .setDefaultValue("defaultValue")
            .setSrid(SRID.valueOf("4326"));
        
        assertEquals("ComplexProperty", extProperty.getName());
        assertTrue(extProperty.isCollection());
        assertFalse(extProperty.isNullable());
        assertEquals(Integer.valueOf(255), extProperty.getMaxLength());
        assertEquals(Integer.valueOf(10), extProperty.getPrecision());
        assertEquals(Integer.valueOf(2), extProperty.getScale());
        assertTrue(extProperty.isUnicode());
        assertEquals("defaultValue", extProperty.getDefaultValue());
        assertEquals(SRID.valueOf("4326"), extProperty.getSrid());
    }

    @Test
    void testNullSafety() {
        ExtendedCsdlProperty extended = ExtendedCsdlProperty.fromCsdlProperty(null);
        assertNull(extended);
    }

    @Test
    void testComplexFromCsdlConversion() {
        CsdlProperty csdlProperty = new CsdlProperty();
        csdlProperty.setName("ComplexProperty");
        csdlProperty.setType("Edm.Decimal");
        csdlProperty.setCollection(true);
        csdlProperty.setNullable(false);
        csdlProperty.setPrecision(18);
        csdlProperty.setScale(4);
        
        // 添加注解
        List<CsdlAnnotation> annotations = new ArrayList<>();
        CsdlAnnotation annotation = new CsdlAnnotation();
        annotation.setTerm("test.complex.property");
        annotations.add(annotation);
        csdlProperty.setAnnotations(annotations);

        ExtendedCsdlProperty extended = ExtendedCsdlProperty.fromCsdlProperty(csdlProperty);
        
        assertNotNull(extended);
        assertEquals("ComplexProperty", extended.getName());
        assertEquals("Edm.Decimal", extended.getType());
        assertTrue(extended.isCollection());
        assertFalse(extended.isNullable());
        assertEquals(Integer.valueOf(18), extended.getPrecision());
        assertEquals(Integer.valueOf(4), extended.getScale());
        
        // 验证注解转换
        assertEquals(1, extended.getExtendedAnnotations().size());
        assertEquals("test.complex.property", extended.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    void testElementInterface() {
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        extProperty.setName("TestProperty");
        extProperty.setNamespace("test.namespace");
        
        assertEquals("TestProperty", extProperty.getElementId());
        assertEquals("TestProperty", extProperty.getElementPropertyName());
        assertEquals("test.namespace", extProperty.getNamespace());
        
        assertNotNull(extProperty.getElementFullyQualifiedName());
        assertEquals("test.namespace.TestProperty", extProperty.getElementFullyQualifiedName().getFullQualifiedNameAsString());
    }

    @Test
    void testElementIdFallback() {
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        
        // 没有名称时，应该返回基于hashCode的ID
        String elementId = extProperty.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("Property_"));
    }

    @Test
    void testToString() {
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        extProperty.setName("TestProperty");
        extProperty.setType("Edm.String");
        extProperty.setCollection(false);
        extProperty.setNullable(true);
        
        String toString = extProperty.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestProperty"));
        assertTrue(toString.contains("Edm.String"));
    }
}
