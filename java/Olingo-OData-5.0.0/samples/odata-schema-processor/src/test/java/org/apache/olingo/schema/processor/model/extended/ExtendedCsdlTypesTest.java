package org.apache.olingo.schema.processor.model.extended;

import org.apache.olingo.schema.processor.model.dependency.GlobalDependencyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 扩展CSDL类型的综合测试
 */
public class ExtendedCsdlTypesTest {
    
    private GlobalDependencyManager manager;
    
    @BeforeEach
    public void setUp() {
        manager = GlobalDependencyManager.getInstance();
        manager.clear();
    }
    
    @AfterEach
    public void tearDown() {
        manager.clear();
    }
    
    @Test
    public void testExtendedCsdlComplexTypeBasics() {
        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType("complexType1");
        complexType.setName("Address");
        complexType.setNamespace("com.example");
        
        // 注册到全局管理
        complexType.registerElement();
        
        // 测试基本功能
        assertEquals("complexType1", complexType.getElementId());
        assertEquals("Address", complexType.getName());
        assertEquals("com.example", complexType.getNamespace());
        assertEquals(0, complexType.getDependencyCount());
        assertFalse(complexType.hasDependency("test"));
        
        // 测试添加依赖
        ExtendedCsdlComplexType complexType2 = new ExtendedCsdlComplexType("complexType2");
        complexType2.setName("Person");
        complexType2.setNamespace("com.example");
        complexType2.registerElement();
        
        assertTrue(complexType.addDependency("complexType2"));
        assertEquals(1, complexType.getDependencyCount());
        assertTrue(complexType.hasDependency("complexType2"));
        
        // 测试移除依赖
        assertTrue(complexType.removeDependency("complexType2"));
        assertEquals(0, complexType.getDependencyCount());
        assertFalse(complexType.hasDependency("complexType2"));
    }
    
    @Test 
    public void testExtendedCsdlActionBasics() {
        ExtendedCsdlAction action = new ExtendedCsdlAction("action1");
        action.setName("CreateEmployee");
        action.setNamespace("com.example");
        
        // 注册到全局管理
        action.registerElement();
        
        // 测试基本功能
        assertEquals("action1", action.getElementId());
        assertEquals("CreateEmployee", action.getName());
        assertEquals(0, action.getDependencyCount());
        
        // 测试添加依赖
        ExtendedCsdlAction action2 = new ExtendedCsdlAction("action2");
        action2.setName("UpdateEmployee");
        action2.setNamespace("com.example");
        action2.registerElement();
        
        assertTrue(action.addDependency("action2"));
        assertEquals(1, action.getDependencyCount());
        assertTrue(action.hasDependency("action2"));
        
        // 测试移除依赖
        assertTrue(action.removeDependency("action2"));
        assertEquals(0, action.getDependencyCount());
    }
    
    @Test
    public void testExtendedCsdlParameterBasics() {
        ExtendedCsdlParameter parameter = new ExtendedCsdlParameter("parameter1");
        parameter.setName("employeeId");
        parameter.setNamespace("com.example");
        parameter.setParentName("CreateEmployee");
        
        // 注册到全局管理
        parameter.registerElement();
        
        // 测试基本功能
        assertEquals("parameter1", parameter.getElementId());
        assertEquals("employeeId", parameter.getName());
        assertEquals("CreateEmployee", parameter.getParentName());
        assertEquals(0, parameter.getDependencyCount());
        
        // 测试添加依赖
        ExtendedCsdlParameter parameter2 = new ExtendedCsdlParameter("parameter2");
        parameter2.setName("departmentId");
        parameter2.setNamespace("com.example");
        parameter2.registerElement();
        
        assertTrue(parameter.addDependency("parameter2"));
        assertEquals(1, parameter.getDependencyCount());
        assertTrue(parameter.hasDependency("parameter2"));
    }
    
    @Test
    public void testFluentInterfaces() {
        // 测试ExtendedCsdlComplexType的流式接口
        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType()
            .setNamespace("com.example");
        complexType.setName("TestType");
        complexType.setAbstract(true);
        
        assertEquals("TestType", complexType.getName());
        assertEquals("com.example", complexType.getNamespace());
        assertTrue(complexType.isAbstract());
        
        // 测试ExtendedCsdlAction的流式接口
        ExtendedCsdlAction action = new ExtendedCsdlAction()
            .setNamespace("com.example");
        action.setName("TestAction");
        action.setBound(true);
        
        assertEquals("TestAction", action.getName());
        assertEquals("com.example", action.getNamespace());
        assertTrue(action.isBound());
    }
    
    @Test
    public void testDependencyManagement() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction("function1");
        function.setName("GetEmployees");
        function.setNamespace("com.example");
        function.registerElement();
        
        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType("complexType1");
        complexType.setName("Employee");
        complexType.setNamespace("com.example");
        complexType.registerElement();
        
        // 建立依赖关系: function -> complexType
        assertTrue(function.addDependency("complexType1"));
        
        // 验证依赖关系
        assertTrue(function.hasDependency("complexType1"));
        assertEquals(1, function.getDependencyCount());
        assertEquals(1, complexType.getDependentCount());
        
        // 验证全局查询功能
        assertEquals(2, manager.getAllElements().size());
        assertEquals(1, manager.getElementsByType(function.getElementDependencyType()).size());
        assertEquals(1, manager.getElementsByType(complexType.getElementDependencyType()).size());
    }
    
    @Test
    public void testComplexDependencyChain() {
        // 创建复杂的依赖链: Function -> Action -> ComplexType -> Parameter
        ExtendedCsdlFunction function = new ExtendedCsdlFunction("function1");
        function.setName("ProcessEmployee");
        function.setNamespace("com.example");
        function.registerElement();
        
        ExtendedCsdlAction action = new ExtendedCsdlAction("action1");
        action.setName("ValidateEmployee");
        action.setNamespace("com.example");
        action.registerElement();
        
        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType("complexType1");
        complexType.setName("Employee");
        complexType.setNamespace("com.example");
        complexType.registerElement();
        
        ExtendedCsdlParameter parameter = new ExtendedCsdlParameter("parameter1");
        parameter.setName("id");
        parameter.setNamespace("com.example");
        parameter.registerElement();
        
        // 建立依赖
        function.addDependency("action1");
        action.addDependency("complexType1");
        complexType.addDependency("parameter1");
        
        // 验证链式依赖
        assertTrue(function.hasDependencyPath("parameter1"));
        assertFalse(parameter.hasDependencyPath("function1"));
        
        // 验证拓扑排序
        assertEquals(4, manager.getTopologicalOrder().size());
    }
}
