package org.apache.olingo.xmlprocessor.core.model;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlActionRefactored的单元测试
 */
class ExtendedCsdlActionTest {

    @Test
    void testConstructor() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();
        assertNotNull(action);
        assertNotNull(action.asCsdlAction());
    }

    @Test
    void testFromCsdlAction() {
        // 准备测试数据
        CsdlAction source = new CsdlAction();
        source.setName("TestAction");
        source.setBound(true);
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
        ExtendedCsdlAction extended = ExtendedCsdlAction.fromCsdlAction(source);

        // 验证结果
        assertNotNull(extended);
        assertEquals("TestAction", extended.getName());
        assertTrue(extended.isBound());
        assertEquals("Entities", extended.getEntitySetPath());
        assertEquals(2, extended.getExtendedParameters().size());
        assertNotNull(extended.getExtendedReturnType());
    }

    @Test
    void testFromCsdlActionWithNull() {
        ExtendedCsdlAction extended = ExtendedCsdlAction.fromCsdlAction(null);
        assertNull(extended);
    }

    @Test
    void testBasicProperties() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();

        // 测试Name
        action.setName("TestAction");
        assertEquals("TestAction", action.getName());

        // 测试Bound
        action.setBound(true);
        assertTrue(action.isBound());

        // 测试EntitySetPath
        action.setEntitySetPath("Entities");
        assertEquals("Entities", action.getEntitySetPath());
    }

    @Test
    void testFluentAPI() {
        ExtendedCsdlAction action = new ExtendedCsdlAction()
                .setName("FluentAction")
                .setBound(false)
                .setEntitySetPath("TestEntities");

        assertEquals("FluentAction", action.getName());
        assertFalse(action.isBound());
        assertEquals("TestEntities", action.getEntitySetPath());
    }

    @Test
    void testParametersManagement() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();

        // 添加Extended参数
        ExtendedCsdlParameter param1 = new ExtendedCsdlParameter();
        param1.setName("param1");
        param1.setType("Edm.String");

        ExtendedCsdlParameter param2 = new ExtendedCsdlParameter();
        param2.setName("param2");
        param2.setType("Edm.Int32");

        action.addExtendedParameter(param1);
        action.addExtendedParameter(param2);

        // 验证Extended参数
        assertEquals(2, action.getExtendedParameters().size());
        assertEquals("param1", action.getExtendedParameters().get(0).getName());
        assertEquals("param2", action.getExtendedParameters().get(1).getName());

        // 验证同步到底层对象
        assertEquals(2, action.getParameters().size());
        assertEquals("param1", action.getParameters().get(0).getName());
        assertEquals("param2", action.getParameters().get(1).getName());
    }

    @Test
    void testReturnTypeManagement() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();

        // 设置Extended返回类型
        ExtendedCsdlReturnType returnType = new ExtendedCsdlReturnType();
        returnType.setType("Edm.String");
        returnType.setNullable(false);

        action.setExtendedReturnType(returnType);

        // 验证Extended返回类型
        assertNotNull(action.getExtendedReturnType());
        assertEquals("Edm.String", action.getExtendedReturnType().getType());
        assertFalse(action.getExtendedReturnType().isNullable());

        // 验证同步到底层对象
        assertNotNull(action.getReturnType());
        assertEquals("Edm.String", action.getReturnType().getType());
    }

    @Test
    void testAnnotationsViaBaseClass() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();

        // 创建Extended注解 - 简化测试，不设置复杂表达式
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("Core.Description");

        // 使用基类方法添加注解
        action.addExtendedAnnotation(annotation);

        // 验证Extended注解
        assertEquals(1, action.getExtendedAnnotations().size());
        assertEquals("Core.Description", action.getExtendedAnnotations().get(0).getTerm());

        // 验证同步到底层对象
        assertEquals(1, action.asCsdlAction().getAnnotations().size());
        assertEquals("Core.Description", action.asCsdlAction().getAnnotations().get(0).getTerm());
    }

    @Test
    void testElementInterfaceMethods() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();
        action.setName("TestAction");
        action.setNamespace("test.namespace");

        // 测试ElementId
        assertEquals("TestAction", action.getElementId());

        // 测试ElementFullyQualifiedName
        assertNotNull(action.getElementFullyQualifiedName());
        assertEquals("test.namespace", action.getElementFullyQualifiedName().getNamespace());
        assertEquals("TestAction", action.getElementFullyQualifiedName().getName());

        // 测试ElementDependencyType
        assertEquals(org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode.DependencyType.ACTION_REFERENCE, 
                     action.getElementDependencyType());

        // 测试ElementPropertyName
        assertEquals("TestAction", action.getElementPropertyName());
    }

    @Test
    void testToString() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();
        action.setName("TestAction");
        action.setNamespace("test.namespace");
        action.setBound(true);

        String result = action.toString();
        assertTrue(result.contains("TestAction"));
        assertTrue(result.contains("test.namespace"));
        assertTrue(result.contains("isBound=true"));
    }

    @Test
    void testDataSynchronization() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();

        // 通过标准方法设置参数
        CsdlParameter param = new CsdlParameter();
        param.setName("standardParam");
        param.setType("Edm.String");

        action.setParameters(Arrays.asList(param));

        // 验证同步到Extended集合
        assertEquals(1, action.getExtendedParameters().size());
        assertEquals("standardParam", action.getExtendedParameters().get(0).getName());

        // 清除并通过Extended方法添加
        ExtendedCsdlParameter extParam = new ExtendedCsdlParameter();
        extParam.setName("extendedParam");
        extParam.setType("Edm.Int32");

        action.setExtendedParameters(Arrays.asList(extParam));

        // 验证同步到底层对象
        assertEquals(1, action.getParameters().size());
        assertEquals("extendedParam", action.getParameters().get(0).getName());
    }

    @Test
    void testNullHandling() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();

        // 测试添加null参数
        action.addExtendedParameter(null);
        assertTrue(action.getExtendedParameters().isEmpty());

        // 测试设置null返回类型
        action.setExtendedReturnType(null);
        assertNull(action.getExtendedReturnType());
        assertNull(action.getReturnType());

        // 测试设置null参数列表
        action.setExtendedParameters(null);
        assertTrue(action.getExtendedParameters().isEmpty());
    }
}
