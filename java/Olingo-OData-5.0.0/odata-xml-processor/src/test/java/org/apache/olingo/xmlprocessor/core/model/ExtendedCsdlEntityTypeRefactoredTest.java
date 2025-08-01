package org.apache.olingo.xmlprocessor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlEntityTypeRefactored单元测试
 * 验证重构后的基类继承功能
 */
public class ExtendedCsdlEntityTypeRefactoredTest {

    @Test
    void testBasicConstruction() {
        ExtendedCsdlEntityTypeRefactored extEntityType = new ExtendedCsdlEntityTypeRefactored();
        assertNotNull(extEntityType);
        assertNotNull(extEntityType.getWrappedElement());
        assertNotNull(extEntityType.getExtendedAnnotations());
    }

    @Test
    void testFromCsdlEntityType() {
        CsdlEntityType csdlEntityType = new CsdlEntityType();
        csdlEntityType.setName("Customer");
        csdlEntityType.setAbstract(true);

        ExtendedCsdlEntityTypeRefactored extended = ExtendedCsdlEntityTypeRefactored.fromCsdlEntityType(csdlEntityType);
        
        assertNotNull(extended);
        assertEquals("Customer", extended.getName());
        assertTrue(extended.isAbstract());
    }

    @Test
    void testAnnotationsInheritanceFromBaseClass() {
        ExtendedCsdlEntityTypeRefactored extEntityType = new ExtendedCsdlEntityTypeRefactored();
        
        // 测试基类提供的注解功能
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("test.annotation");
        
        extEntityType.addExtendedAnnotation(annotation);
        
        assertEquals(1, extEntityType.getExtendedAnnotations().size());
        assertEquals("test.annotation", extEntityType.getExtendedAnnotations().get(0).getTerm());
        
        // 验证同步到原始对象
        assertNotNull(extEntityType.asCsdlEntityType().getAnnotations());
        assertEquals(1, extEntityType.asCsdlEntityType().getAnnotations().size());
    }

    @Test
    void testFluentApiFromBaseClass() {
        ExtendedCsdlEntityTypeRefactored extEntityType = new ExtendedCsdlEntityTypeRefactored();
        
        // 测试基类提供的流式API
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("fluent.test");
        
        ExtendedCsdlEntityTypeRefactored result = extEntityType
            .setName("FluentTest")
            .addExtendedAnnotation(annotation);
        
        // 验证流式API返回相同实例
        assertSame(extEntityType, result);
        assertEquals("FluentTest", result.getName());
        assertEquals(1, result.getExtendedAnnotations().size());
    }

    @Test
    void testExtendedPropertiesSync() {
        ExtendedCsdlEntityTypeRefactored extEntityType = new ExtendedCsdlEntityTypeRefactored();
        
        ExtendedCsdlProperty extProperty = new ExtendedCsdlProperty();
        extProperty.setName("TestProperty");
        
        extEntityType.addExtendedProperty(extProperty);
        
        assertEquals(1, extEntityType.getExtendedProperties().size());
        assertEquals("TestProperty", extEntityType.getExtendedProperty("TestProperty").getName());
        
        // 验证同步到原始数据
        assertNotNull(extEntityType.asCsdlEntityType().getProperties());
        assertEquals(1, extEntityType.asCsdlEntityType().getProperties().size());
    }

    @Test
    void testNullSafety() {
        ExtendedCsdlEntityTypeRefactored extended = ExtendedCsdlEntityTypeRefactored.fromCsdlEntityType(null);
        assertNull(extended);
        
        ExtendedCsdlEntityTypeRefactored extEntityType = new ExtendedCsdlEntityTypeRefactored();
        
        // 测试空baseType设置
        extEntityType.setBaseType((String)null);
        assertNull(extEntityType.getBaseType());
        
        extEntityType.setBaseType("");
        assertNull(extEntityType.getBaseType());
        
        extEntityType.setBaseType("  ");
        assertNull(extEntityType.getBaseType());
    }

    @Test
    void testComplexFromCsdlConversion() {
        CsdlEntityType csdlEntityType = new CsdlEntityType();
        csdlEntityType.setName("ComplexEntity");
        csdlEntityType.setAbstract(false);
        csdlEntityType.setHasStream(true);
        csdlEntityType.setOpenType(true);
        
        // 添加注解
        List<CsdlAnnotation> annotations = new ArrayList<>();
        CsdlAnnotation annotation = new CsdlAnnotation();
        annotation.setTerm("test.complex");
        annotations.add(annotation);
        csdlEntityType.setAnnotations(annotations);
        
        // 添加属性
        List<CsdlProperty> properties = new ArrayList<>();
        CsdlProperty property = new CsdlProperty();
        property.setName("ComplexProperty");
        properties.add(property);
        csdlEntityType.setProperties(properties);

        ExtendedCsdlEntityTypeRefactored extended = ExtendedCsdlEntityTypeRefactored.fromCsdlEntityType(csdlEntityType);
        
        assertNotNull(extended);
        assertEquals("ComplexEntity", extended.getName());
        assertFalse(extended.isAbstract());
        assertTrue(extended.hasStream());
        assertTrue(extended.isOpenType());
        
        // 验证注解转换
        assertEquals(1, extended.getExtendedAnnotations().size());
        assertEquals("test.complex", extended.getExtendedAnnotations().get(0).getTerm());
        
        // 验证属性转换
        assertEquals(1, extended.getExtendedProperties().size());
        assertEquals("ComplexProperty", extended.getExtendedProperties().get(0).getName());
    }

    @Test
    void testElementInterface() {
        ExtendedCsdlEntityTypeRefactored extEntityType = new ExtendedCsdlEntityTypeRefactored();
        extEntityType.setName("TestEntity");
        extEntityType.setNamespace("test.namespace");
        
        assertEquals("TestEntity", extEntityType.getElementId());
        assertEquals("TestEntity", extEntityType.getElementPropertyName());
        assertEquals("test.namespace", extEntityType.getNamespace());
        
        assertNotNull(extEntityType.getElementFullyQualifiedName());
        assertEquals("test.namespace.TestEntity", extEntityType.getElementFullyQualifiedName().getFullQualifiedNameAsString());
    }
}
