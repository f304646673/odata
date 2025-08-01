package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlParameterRefactored和ExtendedCsdlReturnTypeRefactored的联合单元测试
 */
class ExtendedCsdlParameterAndReturnTypeRefactoredTest {

    @Test
    void testParameterConstructor() {
        ExtendedCsdlParameterRefactored parameter = new ExtendedCsdlParameterRefactored();
        assertNotNull(parameter);
        assertNotNull(parameter.asCsdlParameter());
    }

    @Test
    void testParameterFromCsdlParameter() {
        // 准备测试数据
        CsdlParameter source = new CsdlParameter();
        source.setName("testParam");
        source.setType("Edm.String");
        source.setCollection(true);
        source.setNullable(false);
        source.setMaxLength(255);

        // 执行转换
        ExtendedCsdlParameterRefactored extended = ExtendedCsdlParameterRefactored.fromCsdlParameter(source);

        // 验证结果
        assertNotNull(extended);
        assertEquals("testParam", extended.getName());
        assertEquals("Edm.String", extended.getType());
        assertTrue(extended.isCollection());
        assertFalse(extended.isNullable());
        assertEquals(255, extended.getMaxLength());
    }

    @Test
    void testParameterFluentAPI() {
        ExtendedCsdlParameterRefactored parameter = new ExtendedCsdlParameterRefactored()
                .setName("fluentParam")
                .setType("Edm.Int32")
                .setCollection(false)
                .setNullable(true)
                .setPrecision(10)
                .setScale(2);

        assertEquals("fluentParam", parameter.getName());
        assertEquals("Edm.Int32", parameter.getType());
        assertFalse(parameter.isCollection());
        assertTrue(parameter.isNullable());
        assertEquals(10, parameter.getPrecision());
        assertEquals(2, parameter.getScale());
    }

    @Test
    void testReturnTypeConstructor() {
        ExtendedCsdlReturnTypeRefactored returnType = new ExtendedCsdlReturnTypeRefactored();
        assertNotNull(returnType);
        assertNotNull(returnType.asCsdlReturnType());
    }

    @Test
    void testReturnTypeFromCsdlReturnType() {
        // 准备测试数据
        CsdlReturnType source = new CsdlReturnType();
        source.setType("Edm.String");
        source.setCollection(true);
        source.setNullable(false);
        source.setMaxLength(100);

        // 执行转换
        ExtendedCsdlReturnTypeRefactored extended = ExtendedCsdlReturnTypeRefactored.fromCsdlReturnType(source);

        // 验证结果
        assertNotNull(extended);
        assertEquals("Edm.String", extended.getType());
        assertTrue(extended.isCollection());
        assertFalse(extended.isNullable());
        assertEquals(100, extended.getMaxLength());
    }

    @Test
    void testReturnTypeFluentAPI() {
        ExtendedCsdlReturnTypeRefactored returnType = new ExtendedCsdlReturnTypeRefactored()
                .setType("Edm.Decimal")
                .setCollection(false)
                .setNullable(true)
                .setPrecision(18)
                .setScale(4);

        assertEquals("Edm.Decimal", returnType.getType());
        assertFalse(returnType.isCollection());
        assertTrue(returnType.isNullable());
        assertEquals(18, returnType.getPrecision());
        assertEquals(4, returnType.getScale());
    }

    @Test
    void testParameterElementInterface() {
        ExtendedCsdlParameterRefactored parameter = new ExtendedCsdlParameterRefactored();
        parameter.setName("testParam");
        parameter.setNamespace("test.namespace");

        assertEquals("testParam", parameter.getElementId());
        assertNotNull(parameter.getElementFullyQualifiedName());
        assertEquals("test.namespace", parameter.getElementFullyQualifiedName().getNamespace());
        assertEquals("testParam", parameter.getElementFullyQualifiedName().getName());
    }

    @Test
    void testReturnTypeElementInterface() {
        ExtendedCsdlReturnTypeRefactored returnType = new ExtendedCsdlReturnTypeRefactored();
        returnType.setType("Edm.String");
        returnType.setNamespace("test.namespace");

        assertTrue(returnType.getElementId().contains("ReturnType_"));
        assertNotNull(returnType.getElementFullyQualifiedName());
        assertEquals("test.namespace", returnType.getElementFullyQualifiedName().getNamespace());
    }

    @Test
    void testParameterAnnotationsViaBaseClass() {
        ExtendedCsdlParameterRefactored parameter = new ExtendedCsdlParameterRefactored();

        // 创建Extended注解
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("Core.Description");

        // 使用基类方法添加注解
        parameter.addExtendedAnnotation(annotation);

        // 验证Extended注解
        assertEquals(1, parameter.getExtendedAnnotations().size());
        assertEquals("Core.Description", parameter.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    void testReturnTypeToString() {
        ExtendedCsdlReturnTypeRefactored returnType = new ExtendedCsdlReturnTypeRefactored();
        returnType.setType("Edm.String");
        returnType.setCollection(true);
        returnType.setNamespace("test.namespace");

        String result = returnType.toString();
        assertTrue(result.contains("Edm.String"));
        assertTrue(result.contains("test.namespace"));
        assertTrue(result.contains("isCollection=true"));
    }

    @Test
    void testParameterParentName() {
        ExtendedCsdlParameterRefactored parameter = new ExtendedCsdlParameterRefactored();
        parameter.setParentName("TestAction");

        assertEquals("TestAction", parameter.getParentName());

        String result = parameter.toString();
        assertTrue(result.contains("parentName='TestAction'"));
    }

    @Test
    void testReturnTypeParentName() {
        ExtendedCsdlReturnTypeRefactored returnType = new ExtendedCsdlReturnTypeRefactored();
        returnType.setParentName("TestFunction");

        assertEquals("TestFunction", returnType.getParentName());

        String result = returnType.toString();
        assertTrue(result.contains("parentName='TestFunction'"));
    }

    @Test
    void testNullHandling() {
        // 测试Parameter的null处理
        ExtendedCsdlParameterRefactored paramFromNull = ExtendedCsdlParameterRefactored.fromCsdlParameter(null);
        assertNull(paramFromNull);

        // 测试ReturnType的null处理
        ExtendedCsdlReturnTypeRefactored returnFromNull = ExtendedCsdlReturnTypeRefactored.fromCsdlReturnType(null);
        assertNull(returnFromNull);
    }

    @Test
    void testParameterTypeFQN() {
        ExtendedCsdlParameterRefactored parameter = new ExtendedCsdlParameterRefactored();
        parameter.setType("TestNamespace.CustomType");

        assertEquals("TestNamespace.CustomType", parameter.getType());
        
        // 测试FullQualifiedName设置
        org.apache.olingo.commons.api.edm.FullQualifiedName fqn = 
            new org.apache.olingo.commons.api.edm.FullQualifiedName("TestNamespace", "CustomType");
        parameter.setType(fqn);
        
        assertNotNull(parameter.getTypeFQN());
    }

    @Test
    void testReturnTypeTypeFQN() {
        ExtendedCsdlReturnTypeRefactored returnType = new ExtendedCsdlReturnTypeRefactored();
        returnType.setType("TestNamespace.CustomType");

        assertEquals("TestNamespace.CustomType", returnType.getType());
        
        // 测试FullQualifiedName设置
        org.apache.olingo.commons.api.edm.FullQualifiedName fqn = 
            new org.apache.olingo.commons.api.edm.FullQualifiedName("TestNamespace", "CustomType");
        returnType.setType(fqn);
        
        assertNotNull(returnType.getTypeFQN());
    }
}
