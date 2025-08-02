package org.apache.olingo.xmlprocessor.core.model;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlFunctionRefactored的单元测试
 */
class ExtendedCsdlFunctionTest {

    @Test
    void testConstructor() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();
        assertNotNull(function);
        assertNotNull(function.asCsdlFunction());
    }

    @Test
    void testFromCsdlFunction() {
        // 准备测试数据
        CsdlFunction source = new CsdlFunction();
        source.setName("TestFunction");
        source.setBound(true);
        source.setComposable(false);
        source.setEntitySetPath("Entities");

        // 添加参数
        CsdlParameter param1 = new CsdlParameter();
        param1.setName("param1");
        param1.setType("Edm.String");

        CsdlParameter param2 = new CsdlParameter();
        param2.setName("param2");
        param2.setType("Edm.Int32");

        source.setParameters(Arrays.asList(param1, param2));

        // 添加返回类型
        CsdlReturnType returnType = new CsdlReturnType();
        returnType.setType("Edm.String");
        source.setReturnType(returnType);

        // 执行转换
        ExtendedCsdlFunction extended = ExtendedCsdlFunction.fromCsdlFunction(source);

        // 验证结果
        assertNotNull(extended);
        assertEquals("TestFunction", extended.getName());
        assertTrue(extended.isBound());
        assertFalse(extended.isComposable());
        assertEquals("Entities", extended.getEntitySetPath());
        assertEquals(2, extended.getExtendedParameters().size());
        assertNotNull(extended.getExtendedReturnType());
    }

    @Test
    void testFromCsdlFunctionWithNull() {
        ExtendedCsdlFunction extended = ExtendedCsdlFunction.fromCsdlFunction(null);
        assertNull(extended);
    }

    @Test
    void testBasicProperties() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();

        // 测试Name
        function.setName("TestFunction");
        assertEquals("TestFunction", function.getName());

        // 测试Bound
        function.setBound(true);
        assertTrue(function.isBound());

        // 测试Composable
        function.setComposable(false);
        assertFalse(function.isComposable());

        // 测试EntitySetPath
        function.setEntitySetPath("Entities");
        assertEquals("Entities", function.getEntitySetPath());
    }

    @Test
    void testFluentAPI() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction()
                .setName("FluentFunction")
                .setBound(false)
                .setComposable(true)
                .setEntitySetPath("TestEntities");

        assertEquals("FluentFunction", function.getName());
        assertFalse(function.isBound());
        assertTrue(function.isComposable());
        assertEquals("TestEntities", function.getEntitySetPath());
    }

    @Test
    void testParametersManagement() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();

        // 添加Extended参数
        ExtendedCsdlParameter param1 = new ExtendedCsdlParameter();
        param1.setName("param1");
        param1.setType("Edm.String");

        ExtendedCsdlParameter param2 = new ExtendedCsdlParameter();
        param2.setName("param2");
        param2.setType("Edm.Int32");

        function.addExtendedParameter(param1);
        function.addExtendedParameter(param2);

        // 验证Extended参数
        assertEquals(2, function.getExtendedParameters().size());
        assertEquals("param1", function.getExtendedParameters().get(0).getName());
        assertEquals("param2", function.getExtendedParameters().get(1).getName());

        // 验证同步到底层对象
        assertEquals(2, function.getParameters().size());
        assertEquals("param1", function.getParameters().get(0).getName());
        assertEquals("param2", function.getParameters().get(1).getName());
    }

    @Test
    void testReturnTypeManagement() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();

        // 设置Extended返回类型
        ExtendedCsdlReturnType returnType = new ExtendedCsdlReturnType();
        returnType.setType("Edm.String");
        returnType.setNullable(false);

        function.setExtendedReturnType(returnType);

        // 验证Extended返回类型
        assertNotNull(function.getExtendedReturnType());
        assertEquals("Edm.String", function.getExtendedReturnType().getType());
        assertFalse(function.getExtendedReturnType().isNullable());

        // 验证同步到底层对象
        assertNotNull(function.getReturnType());
        assertEquals("Edm.String", function.getReturnType().getType());
    }

    @Test
    void testAnnotationsViaBaseClass() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();

        // 创建Extended注解
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("Core.Description");

        // 使用基类方法添加注解
        function.addExtendedAnnotation(annotation);

        // 验证Extended注解
        assertEquals(1, function.getExtendedAnnotations().size());
        assertEquals("Core.Description", function.getExtendedAnnotations().get(0).getTerm());

        // 验证同步到底层对象
        assertEquals(1, function.asCsdlFunction().getAnnotations().size());
        assertEquals("Core.Description", function.asCsdlFunction().getAnnotations().get(0).getTerm());
    }

    @Test
    void testElementInterfaceMethods() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();
        function.setName("TestFunction");
        function.setNamespace("test.namespace");

        // 测试ElementId
        assertEquals("TestFunction", function.getElementId());

        // 测试ElementFullyQualifiedName
        assertNotNull(function.getElementFullyQualifiedName());
        assertEquals("test.namespace", function.getElementFullyQualifiedName().getNamespace());
        assertEquals("TestFunction", function.getElementFullyQualifiedName().getName());

        // 测试ElementDependencyType
        assertEquals(CsdlDependencyNode.DependencyType.FUNCTION_REFERENCE,
                     function.getElementDependencyType());

        // 测试ElementPropertyName
        assertEquals("TestFunction", function.getElementPropertyName());
    }

    @Test
    void testToString() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();
        function.setName("TestFunction");
        function.setNamespace("test.namespace");
        function.setBound(true);
        function.setComposable(false);

        String result = function.toString();
        assertTrue(result.contains("TestFunction"));
        assertTrue(result.contains("test.namespace"));
        assertTrue(result.contains("isBound=true"));
        assertTrue(result.contains("isComposable=false"));
    }

    @Test
    void testDataSynchronization() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();

        // 通过标准方法设置参数
        CsdlParameter param = new CsdlParameter();
        param.setName("standardParam");
        param.setType("Edm.String");

        function.setParameters(Arrays.asList(param));

        // 验证同步到Extended集合
        assertEquals(1, function.getExtendedParameters().size());
        assertEquals("standardParam", function.getExtendedParameters().get(0).getName());

        // 清除并通过Extended方法添加
        ExtendedCsdlParameter extParam = new ExtendedCsdlParameter();
        extParam.setName("extendedParam");
        extParam.setType("Edm.Int32");

        function.setExtendedParameters(Arrays.asList(extParam));

        // 验证同步到底层对象
        assertEquals(1, function.getParameters().size());
        assertEquals("extendedParam", function.getParameters().get(0).getName());
    }

    @Test
    void testNullHandling() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();

        // 测试添加null参数
        function.addExtendedParameter(null);
        assertTrue(function.getExtendedParameters().isEmpty());

        // 测试设置null返回类型
        function.setExtendedReturnType(null);
        assertNull(function.getExtendedReturnType());
        assertNull(function.getReturnType());

        // 测试设置null参数列表
        function.setExtendedParameters(null);
        assertTrue(function.getExtendedParameters().isEmpty());
    }

    @Test
    void testComposableFunctionSpecificBehavior() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();
        
        // 测试可组合函数的特殊行为
        function.setComposable(true);
        assertTrue(function.isComposable());
        
        // 可组合函数通常不绑定
        function.setBound(false);
        assertFalse(function.isBound());
        
        // 验证可组合函数的toString包含相关信息
        function.setName("ComposableFunction");
        String result = function.toString();
        assertTrue(result.contains("isComposable=true"));
    }
}
