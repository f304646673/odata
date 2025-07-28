package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 ExtendedCsdlParameter
 */
public class ExtendedCsdlParameterTest {

    private ExtendedCsdlParameter parameter;
    private ExtendedCsdlParameter parameterWithId;

    @BeforeEach
    public void setUp() {
        parameter = new ExtendedCsdlParameter();
        parameterWithId = new ExtendedCsdlParameter("testParameter");
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlParameter p = new ExtendedCsdlParameter();
        assertNotNull(p);
        assertNull(p.getName());
        assertNull(p.getType());
        assertNull(p.getNamespace());
    }

    @Test
    public void testConstructorWithElementId() {
        String elementId = "testParameter";
        ExtendedCsdlParameter p = new ExtendedCsdlParameter(elementId);
        assertNotNull(p);
        assertEquals(elementId, p.getElementId());
    }

    @Test
    public void testGetElementIdWithProvidedId() {
        assertEquals("testParameter", parameterWithId.getElementId());
    }

    @Test
    public void testGetElementIdWithName() {
        parameter.setName("MyParameter");
        assertEquals("MyParameter", parameter.getElementId());
    }

    @Test
    public void testGetElementIdWithoutNameOrId() {
        String elementId = parameter.getElementId();
        assertNotNull(elementId);
        assertTrue(elementId.startsWith("Parameter_"));
    }

    @Test
    public void testSetAndGetNamespace() {
        String namespace = "com.example";
        ExtendedCsdlParameter result = parameter.setNamespace(namespace);
        
        assertSame(parameter, result); // 验证流式接口
        assertEquals(namespace, parameter.getNamespace());
    }

    @Test
    public void testSetAndGetParentName() {
        String parentName = "MyFunction";
        parameter.setParentName(parentName);
        assertEquals(parentName, parameter.getParentName());
    }

    @Test
    public void testGetElementFullyQualifiedName() {
        parameter.setNamespace("com.example");
        parameter.setParentName("MyFunction");
        parameter.setName("TestParameter");
        
        FullQualifiedName fqn = parameter.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("com.example", fqn.getNamespace());
        assertEquals("MyFunction.TestParameter", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedNameWithoutParent() {
        parameter.setNamespace("com.example");
        parameter.setName("TestParameter");
        
        FullQualifiedName fqn = parameter.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("com.example", fqn.getNamespace());
        assertEquals("TestParameter", fqn.getName());
    }

    @Test
    public void testGetElementFullyQualifiedNameWithNullValues() {
        FullQualifiedName fqn = parameter.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertNull(fqn.getNamespace());
        assertNull(fqn.getName());
    }

    @Test
    public void testGetElementDependencyType() {
        assertEquals(CsdlDependencyNode.DependencyType.PARAMETER, parameter.getElementDependencyType());
        assertEquals(CsdlDependencyNode.DependencyType.PARAMETER, parameterWithId.getElementDependencyType());
    }

    @Test
    public void testGetElementPropertyName() {
        assertEquals("type", parameter.getElementPropertyName());
        assertEquals("type", parameterWithId.getElementPropertyName());
    }

    @Test
    public void testRegisterElement() {
        ExtendedCsdlParameter result = parameter.registerElement();
        
        assertSame(parameter, result); // 验证流式接口
        // 注意：registerElement的具体行为依赖于接口的默认实现
    }

    @Test
    public void testSetAndGetName() {
        String name = "MyParameter";
        parameter.setName(name);
        assertEquals(name, parameter.getName());
    }

    @Test
    public void testSetAndGetType() {
        String type = "Edm.String";
        parameter.setType(type);
        assertEquals(type, parameter.getType());
    }

    @Test
    public void testSetAndGetNullable() {
        assertEquals(Boolean.TRUE, parameter.isNullable()); // 默认值是true
        
        parameter.setNullable(true);
        assertEquals(Boolean.TRUE, parameter.isNullable());
        
        parameter.setNullable(false);
        assertEquals(Boolean.FALSE, parameter.isNullable());
    }

    @Test
    public void testSetAndGetMaxLength() {
        assertNull(parameter.getMaxLength()); // 默认
        
        parameter.setMaxLength(100);
        assertEquals(Integer.valueOf(100), parameter.getMaxLength());
    }

    @Test
    public void testSetAndGetPrecision() {
        assertNull(parameter.getPrecision()); // 默认
        
        parameter.setPrecision(10);
        assertEquals(Integer.valueOf(10), parameter.getPrecision());
    }

    @Test
    public void testSetAndGetScale() {
        assertNull(parameter.getScale()); // 默认
        
        parameter.setScale(2);
        assertEquals(Integer.valueOf(2), parameter.getScale());
    }

    @Test
    public void testToString() {
        parameter.setName("TestParameter");
        parameter.setType("Edm.String");
        parameter.setParentName("TestFunction");
        
        String result = parameter.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestParameter"));
        assertTrue(result.contains("Edm.String"));
        assertTrue(result.contains("TestFunction"));
        assertTrue(result.contains("ExtendedCsdlParameter"));
    }

    @Test
    public void testToStringWithNullValues() {
        String result = parameter.toString();
        assertNotNull(result);
        assertTrue(result.contains("ExtendedCsdlParameter"));
    }

    @Test
    public void testInheritanceFromCsdlParameter() {
        assertTrue(parameter instanceof org.apache.olingo.commons.api.edm.provider.CsdlParameter);
    }

    @Test
    public void testImplementsExtendedCsdlElement() {
        assertTrue(parameter instanceof ExtendedCsdlElement);
    }

    @Test
    public void testFluentInterface() {
        parameter.setNamespace("com.example")
                 .setName("FluentParameter")
                 .setType("Edm.Int32");
        parameter.setNullable(false);
        parameter.setMaxLength(50);
        
        assertEquals("com.example", parameter.getNamespace());
        assertEquals("FluentParameter", parameter.getName());
        assertEquals("Edm.Int32", parameter.getType());
        assertEquals(Boolean.FALSE, parameter.isNullable());
        assertEquals(Integer.valueOf(50), parameter.getMaxLength());
    }

    @Test
    public void testComplexParameterSetup() {
        // 测试复杂的Parameter设置
        parameter.setNamespace("com.example.functions");
        parameter.setParentName("ComplexFunction");
        parameter.setName("EntityParameter");
        parameter.setType("com.example.EntityType");
        parameter.setNullable(true);
        parameter.setPrecision(10);
        parameter.setScale(2);
        
        assertEquals("com.example.functions", parameter.getNamespace());
        assertEquals("ComplexFunction", parameter.getParentName());
        assertEquals("EntityParameter", parameter.getName());
        assertEquals("com.example.EntityType", parameter.getType());
        assertEquals(Boolean.TRUE, parameter.isNullable());
        assertEquals(Integer.valueOf(10), parameter.getPrecision());
        assertEquals(Integer.valueOf(2), parameter.getScale());
        
        FullQualifiedName fqn = parameter.getElementFullyQualifiedName();
        assertEquals("com.example.functions", fqn.getNamespace());
        assertEquals("ComplexFunction.EntityParameter", fqn.getName());
    }

    @Test
    public void testElementIdConsistency() {
        // 测试elementId在不同情况下的一致
        
        // 1. 有提供的elementId
        assertEquals("testParameter", parameterWithId.getElementId());
        
        // 2. 没有elementId但有name
        parameter.setName("MyParameter");
        assertEquals("MyParameter", parameter.getElementId());
        
        // 3. 既没有elementId也没有name时，应该生成一个基于hashCode的ID
        ExtendedCsdlParameter emptyParameter = new ExtendedCsdlParameter();
        String generatedId = emptyParameter.getElementId();
        assertNotNull(generatedId);
        assertTrue(generatedId.startsWith("Parameter_"));
        
        // 同一个对象多次调用应该返回相同的ID
        assertEquals(generatedId, emptyParameter.getElementId());
    }

    @Test
    public void testFullyQualifiedNameVariations() {
        // 测试不同情况下的FQN生成
        
        // 情况1：有namespace、parent和name
        parameter.setNamespace("ns1")
                 .setParentName("parent1")
                 .setName("param1");
        FullQualifiedName fqn1 = parameter.getElementFullyQualifiedName();
        assertEquals("ns1", fqn1.getNamespace());
        assertEquals("parent1.param1", fqn1.getName());
        
        // 情况2：有namespace和name，但没有parent
        parameter.setParentName(null);
        FullQualifiedName fqn2 = parameter.getElementFullyQualifiedName();
        assertEquals("ns1", fqn2.getNamespace());
        assertEquals("param1", fqn2.getName());
        
        // 情况3：只有name
        parameter.setNamespace(null);
        FullQualifiedName fqn3 = parameter.getElementFullyQualifiedName();
        assertNull(fqn3.getNamespace());
        assertEquals("param1", fqn3.getName());
    }
}
