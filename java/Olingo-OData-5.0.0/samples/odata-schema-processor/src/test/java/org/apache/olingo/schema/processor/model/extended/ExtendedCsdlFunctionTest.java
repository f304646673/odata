package org.apache.olingo.schema.processor.model.extended;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 ExtendedCsdlFunction
 */
public class ExtendedCsdlFunctionTest {

    private ExtendedCsdlFunction function;
    private ExtendedCsdlFunction functionWithId;

    @BeforeEach
    public void setUp() {
        function = new ExtendedCsdlFunction();
        functionWithId = new ExtendedCsdlFunction("testFunction");
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlFunction f = new ExtendedCsdlFunction();
        assertNotNull(f);
        assertNull(f.getName());
        assertNull(f.getNamespace());
        assertFalse(f.isBound());
    }

    @Test
    public void testConstructorWithElementId() {
        String elementId = "testFunction";
        ExtendedCsdlFunction f = new ExtendedCsdlFunction(elementId);
        assertNotNull(f);
        assertEquals(elementId, f.getElementId());
    }

    @Test
    public void testGetElementIdWithProvidedId() {
        assertEquals("testFunction", functionWithId.getElementId());
    }

    @Test
    public void testGetElementIdWithName() {
        function.setName("MyFunction");
        assertEquals("MyFunction", function.getElementId());
    }

    @Test
    public void testGetElementIdWithoutNameOrId() {
        String elementId = function.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("Function_"));
    }

    @Test
    public void testSetAndGetNamespace() {
        String namespace = "com.example";
        ExtendedCsdlFunction result = function.setNamespace(namespace);
        
        assertSame(function, result); // 验证流式接口
        assertEquals(namespace, function.getNamespace());
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        function.setNamespace("com.example");
        function.setName("TestFunction");
        
        FullQualifiedName fqn = function.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("com.example", fqn.getNamespace());
        assertEquals("TestFunction", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedNameWithNullValues() {
        FullQualifiedName fqn = function.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertNull(fqn.getNamespace());
        assertNull(fqn.getName());
    }

    @Test
    public void testGetElementDependencyType() {
        assertEquals(CsdlDependencyNode.DependencyType.FUNCTION, function.getElementDependencyType());
        assertEquals(CsdlDependencyNode.DependencyType.FUNCTION, functionWithId.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        assertNull(function.getElementPropertyName());
        assertNull(functionWithId.getElementPropertyName());
    }

    @Test
    public void testRegisterElement() {
        ExtendedCsdlFunction result = function.registerElement();
        
        assertSame(function, result); // 验证流式接口
        // 注意：registerElement的具体行为依赖于接口的默认实现
    }

    @Test
    public void testSetAndGetName() {
        String name = "MyFunction";
        function.setName(name);
        assertEquals(name, function.getName());
    }

    @Test
    public void testSetAndGetBound() {
        assertFalse(function.isBound()); // 默认
        
        function.setBound(true);
        assertTrue(function.isBound());
        
        function.setBound(false);
        assertFalse(function.isBound());
    }

    @Test
    public void testSetAndGetComposable() {
        assertFalse(function.isComposable()); // 默认
        
        function.setComposable(true);
        assertTrue(function.isComposable());
        
        function.setComposable(false);
        assertFalse(function.isComposable());
    }

    @Test
    public void testSetAndGetParameters() {
        CsdlParameter param1 = new CsdlParameter();
        param1.setName("param1");
        param1.setType("Edm.String");
        
        CsdlParameter param2 = new CsdlParameter();
        param2.setName("param2");
        param2.setType("Edm.Int32");
        
        function.setParameters(Arrays.asList(param1, param2));
        
        assertNotNull(function.getParameters());
        assertEquals(2, function.getParameters().size());
        assertEquals("param1", function.getParameters().get(0).getName());
        assertEquals("param2", function.getParameters().get(1).getName());
    }

    @Test
    public void testSetAndGetReturnType() {
        CsdlReturnType returnType = new CsdlReturnType();
        returnType.setType("Edm.String");
        
        function.setReturnType(returnType);
        
        assertNotNull(function.getReturnType());
        assertEquals("Edm.String", function.getReturnType().getType());
    }

    @Test
    public void testToString() {
        function.setName("TestFunction");
        function.setBound(true);
        
        CsdlParameter param = new CsdlParameter();
        param.setName("testParam");
        function.setParameters(Arrays.asList(param));
        
        String result = function.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestFunction"));
        assertTrue(result.contains("bound=true"));
        assertTrue(result.contains("parameters=1"));
        assertTrue(result.contains("ExtendedCsdlFunction"));
    }

    @Test
    public void testToStringWithNullValues() {
        String result = function.toString();
        assertNotNull(result);
        assertTrue(result.contains("ExtendedCsdlFunction"));
        assertTrue(result.contains("bound=false"));
        assertTrue(result.contains("parameters=0"));
    }

    @Test
    public void testToStringWithEmptyParameters() {
        function.setName("EmptyParamsFunction");
        function.setParameters(Arrays.asList());
        
        String result = function.toString();
        assertTrue(result.contains("parameters=0"));
    }

    @Test
    public void testInheritanceFromCsdlFunction() {
        assertTrue(function instanceof org.apache.olingo.commons.api.edm.provider.CsdlFunction);
    }

    @Test
    public void testImplementsExtendedCsdlElement() {
        assertTrue(function instanceof ExtendedCsdlElement);
    }

    @Test
    public void testFluentInterface() {
        function.setNamespace("com.example");
        function.setName("FluentFunction");
        function.setBound(true);
        function.setComposable(true);
        
        assertEquals("com.example", function.getNamespace());
        assertEquals("FluentFunction", function.getName());
        assertTrue(function.isBound());
        assertTrue(function.isComposable());
    }

    @Test
    public void testComplexFunctionSetup() {
        // 测试复杂的Function设置
        CsdlParameter param1 = new CsdlParameter();
        param1.setName("entityParam");
        param1.setType("com.example.EntityType");
        
        CsdlParameter param2 = new CsdlParameter();
        param2.setName("primitiveParam");
        param2.setType("Edm.String");
        
        CsdlReturnType returnType = new CsdlReturnType();
        returnType.setType("Collection(com.example.ReturnType)");
        
        function.setNamespace("com.example.functions");
        function.setName("ComplexFunction");
        function.setBound(true);
        function.setComposable(false);
        function.setParameters(Arrays.asList(param1, param2));
        function.setReturnType(returnType);
        
        assertEquals("com.example.functions", function.getNamespace());
        assertEquals("ComplexFunction", function.getName());
        assertTrue(function.isBound());
        assertFalse(function.isComposable());
        assertEquals(2, function.getParameters().size());
        assertNotNull(function.getReturnType());
        
        FullQualifiedName fqn = function.getElementFullyQualifiedName();
        assertEquals("com.example.functions", fqn.getNamespace());
        assertEquals("ComplexFunction", fqn.getName());
    }

    @Test
    public void testElementIdConsistency() {
        // 测试elementId在不同情况下的一致
        
        // 1. 有提供的elementId
        assertEquals("testFunction", functionWithId.getElementId());
        
        // 2. 没有elementId但有name
        function.setName("MyFunction");
        assertEquals("MyFunction", function.getElementId());
        
        // 3. 既没有elementId也没有name时，应该生成一个基于hashCode的ID
        ExtendedCsdlFunction emptyFunction = new ExtendedCsdlFunction();
        String generatedId = emptyFunction.getElementId();
        assertNotNull(generatedId);
        assertTrue(generatedId.startsWith("Function_"));
        
        // 同一个对象多次调用应该返回相同的ID
        assertEquals(generatedId, emptyFunction.getElementId());
    }
}
