package org.apache.olingo.schema.processor.model.extended;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 扩展CSDL类型的综合测试
 */
public class ExtendedCsdlTypesTest {
    
    @Test
    public void testExtendedCsdlComplexTypeBasics() {
        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType();
        
        // 测试基本功能
        assertEquals(0, complexType.getDependencyCount());
        assertFalse(complexType.hasDependency("test"));
        
        // 测试添加依赖
        complexType.addDependency("com.example");
        assertEquals(1, complexType.getDependencyCount());
        assertTrue(complexType.hasDependency("com.example"));
        
        // 测试清除依赖
        complexType.clearDependencies();
        assertEquals(0, complexType.getDependencyCount());
    }
    
    @Test 
    public void testExtendedCsdlActionBasics() {
        ExtendedCsdlAction action = new ExtendedCsdlAction();
        
        // 测试基本功能
        assertEquals(0, action.getDependencyCount());
        
        // 测试添加依赖
        action.addDependency("com.example");
        assertEquals(1, action.getDependencyCount());
        assertTrue(action.hasDependency("com.example"));
        
        // 测试移除依赖
        assertTrue(action.removeDependency("com.example"));
        assertEquals(0, action.getDependencyCount());
    }
    
    @Test
    public void testExtendedCsdlParameterBasics() {
        ExtendedCsdlParameter parameter = new ExtendedCsdlParameter();
        
        // 测试基本功能
        assertEquals(0, parameter.getDependencyCount());
        
        // 测试添加依赖
        parameter.addDependency("com.example");
        assertEquals(1, parameter.getDependencyCount());
        assertTrue(parameter.hasDependency("com.example"));
    }
    
    @Test
    public void testFluentInterfaces() {
        // 测试ExtendedCsdlComplexType的流式接口
        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType()
            .setName("TestType")
            .setAbstract(true);
        
        assertEquals("TestType", complexType.getName());
        assertTrue(complexType.isAbstract());
        
        // 测试ExtendedCsdlAction的流式接口
        ExtendedCsdlAction action = new ExtendedCsdlAction()
            .setName("TestAction")
            .setBound(true);
        
        assertEquals("TestAction", action.getName());
        assertTrue(action.isBound());
    }
    
    @Test
    public void testDependencyManagement() {
        ExtendedCsdlFunction function = new ExtendedCsdlFunction();
        
        // 测试空状态
        assertTrue(function.getDependencies().isEmpty());
        assertEquals(0, function.getDependencyCount());
        
        // 测试添加多个依赖
        function.addDependency("com.example");
        function.addDependency("com.test");
        function.addDependency("com.example"); // 重复添加
        
        assertEquals(2, function.getDependencyCount()); // 应该是2，不是3
        assertTrue(function.hasDependency("com.example"));
        assertTrue(function.hasDependency("com.test"));
        
        // 测试清除
        function.clearDependencies();
        assertEquals(0, function.getDependencyCount());
    }
    
    @Test
    public void testNullAndEmptyHandling() {
        ExtendedCsdlEntitySet entitySet = new ExtendedCsdlEntitySet();
        
        // 测试null和空字符串处理
        entitySet.addDependency(null);
        entitySet.addDependency("");
        entitySet.addDependency("   ");
        
        assertEquals(0, entitySet.getDependencyCount()); // 应该忽略无效值
        
        // 测试有效值
        entitySet.addDependency("com.example");
        assertEquals(1, entitySet.getDependencyCount());
    }
}
