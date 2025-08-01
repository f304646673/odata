package org.apache.olingo.xmlprocessor.core.model;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlEntitySetRefactored的单元测试
 */
class ExtendedCsdlEntitySetRefactoredTest {

    @Test
    void testConstructor() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();
        assertNotNull(entitySet);
        assertNotNull(entitySet.asCsdlEntitySet());
    }

    @Test
    void testFromCsdlEntitySet() {
        // 准备测试数据
        CsdlEntitySet source = new CsdlEntitySet();
        source.setName("TestEntitySet");
        source.setType("TestNamespace.TestEntity");
        source.setIncludeInServiceDocument(true);

        // 添加导航属性绑定
        CsdlNavigationPropertyBinding binding1 = new CsdlNavigationPropertyBinding();
        binding1.setPath("nav1");
        binding1.setTarget("EntitySet1");

        CsdlNavigationPropertyBinding binding2 = new CsdlNavigationPropertyBinding();
        binding2.setPath("nav2");
        binding2.setTarget("EntitySet2");

        source.setNavigationPropertyBindings(Arrays.asList(binding1, binding2));

        // 执行转换
        ExtendedCsdlEntitySetRefactored extended = ExtendedCsdlEntitySetRefactored.fromCsdlEntitySet(source);

        // 验证结果
        assertNotNull(extended);
        assertEquals("TestEntitySet", extended.getName());
        assertEquals("TestNamespace.TestEntity", extended.getType());
        assertTrue(extended.isIncludeInServiceDocument());
        assertEquals(2, extended.getExtendedNavigationPropertyBindings().size());
    }

    @Test
    void testFromCsdlEntitySetWithNull() {
        ExtendedCsdlEntitySetRefactored extended = ExtendedCsdlEntitySetRefactored.fromCsdlEntitySet(null);
        assertNull(extended);
    }

    @Test
    void testBasicProperties() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();

        // 测试Name
        entitySet.setName("TestEntitySet");
        assertEquals("TestEntitySet", entitySet.getName());

        // 测试Type
        entitySet.setType("TestNamespace.TestEntity");
        assertEquals("TestNamespace.TestEntity", entitySet.getType());

        // 测试IncludeInServiceDocument
        entitySet.setIncludeInServiceDocument(false);
        assertFalse(entitySet.isIncludeInServiceDocument());
    }

    @Test
    void testFluentAPI() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored()
                .setName("FluentEntitySet")
                .setType("FluentNamespace.FluentEntity")
                .setIncludeInServiceDocument(true);

        assertEquals("FluentEntitySet", entitySet.getName());
        assertEquals("FluentNamespace.FluentEntity", entitySet.getType());
        assertTrue(entitySet.isIncludeInServiceDocument());
    }

    @Test
    void testNavigationPropertyBindingsManagement() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();

        // 添加Extended导航属性绑定
        ExtendedCsdlNavigationPropertyBinding binding1 = new ExtendedCsdlNavigationPropertyBinding();
        binding1.setPath("nav1");
        binding1.setTarget("EntitySet1");

        ExtendedCsdlNavigationPropertyBinding binding2 = new ExtendedCsdlNavigationPropertyBinding();
        binding2.setPath("nav2");
        binding2.setTarget("EntitySet2");

        entitySet.addExtendedNavigationPropertyBinding(binding1);
        entitySet.addExtendedNavigationPropertyBinding(binding2);

        // 验证Extended导航属性绑定
        assertEquals(2, entitySet.getExtendedNavigationPropertyBindings().size());
        assertEquals("nav1", entitySet.getExtendedNavigationPropertyBindings().get(0).getPath());
        assertEquals("nav2", entitySet.getExtendedNavigationPropertyBindings().get(1).getPath());

        // 验证同步到底层对象
        assertEquals(2, entitySet.getNavigationPropertyBindings().size());
        assertEquals("nav1", entitySet.getNavigationPropertyBindings().get(0).getPath());
        assertEquals("nav2", entitySet.getNavigationPropertyBindings().get(1).getPath());
    }

    @Test
    void testAnnotationsViaBaseClass() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();

        // 创建Extended注解
        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation();
        annotation.setTerm("Core.Description");

        // 使用基类方法添加注解
        entitySet.addExtendedAnnotation(annotation);

        // 验证Extended注解
        assertEquals(1, entitySet.getExtendedAnnotations().size());
        assertEquals("Core.Description", entitySet.getExtendedAnnotations().get(0).getTerm());

        // 验证同步到底层对象
        assertEquals(1, entitySet.asCsdlEntitySet().getAnnotations().size());
        assertEquals("Core.Description", entitySet.asCsdlEntitySet().getAnnotations().get(0).getTerm());
    }

    @Test
    void testElementInterfaceMethods() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();
        entitySet.setName("TestEntitySet");
        entitySet.setNamespace("test.namespace");

        // 测试ElementId
        assertEquals("TestEntitySet", entitySet.getElementId());

        // 测试ElementFullyQualifiedName
        assertNotNull(entitySet.getElementFullyQualifiedName());
        assertEquals("test.namespace", entitySet.getElementFullyQualifiedName().getNamespace());
        assertEquals("TestEntitySet", entitySet.getElementFullyQualifiedName().getName());

        // 测试ElementDependencyType
        assertEquals(org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode.DependencyType.ENTITY_SET_REFERENCE, 
                     entitySet.getElementDependencyType());

        // 测试ElementPropertyName
        assertEquals("TestEntitySet", entitySet.getElementPropertyName());
    }

    @Test
    void testToString() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();
        entitySet.setName("TestEntitySet");
        entitySet.setNamespace("test.namespace");
        entitySet.setType("TestNamespace.TestEntity");
        entitySet.setIncludeInServiceDocument(true);

        String result = entitySet.toString();
        assertTrue(result.contains("TestEntitySet"));
        assertTrue(result.contains("test.namespace"));
        assertTrue(result.contains("TestNamespace.TestEntity"));
        assertTrue(result.contains("includeInServiceDocument=true"));
    }

    @Test
    void testDataSynchronization() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();

        // 通过标准方法设置导航属性绑定
        CsdlNavigationPropertyBinding binding = new CsdlNavigationPropertyBinding();
        binding.setPath("standardNav");
        binding.setTarget("StandardTarget");

        entitySet.setNavigationPropertyBindings(Arrays.asList(binding));

        // 验证同步到Extended集合
        assertEquals(1, entitySet.getExtendedNavigationPropertyBindings().size());
        assertEquals("standardNav", entitySet.getExtendedNavigationPropertyBindings().get(0).getPath());

        // 清除并通过Extended方法添加
        ExtendedCsdlNavigationPropertyBinding extBinding = new ExtendedCsdlNavigationPropertyBinding();
        extBinding.setPath("extendedNav");
        extBinding.setTarget("ExtendedTarget");

        entitySet.setExtendedNavigationPropertyBindings(Arrays.asList(extBinding));

        // 验证同步到底层对象
        assertEquals(1, entitySet.getNavigationPropertyBindings().size());
        assertEquals("extendedNav", entitySet.getNavigationPropertyBindings().get(0).getPath());
    }

    @Test
    void testNullHandling() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();

        // 测试添加null导航属性绑定
        entitySet.addExtendedNavigationPropertyBinding(null);
        assertTrue(entitySet.getExtendedNavigationPropertyBindings().isEmpty());

        // 测试设置null导航属性绑定列表
        entitySet.setExtendedNavigationPropertyBindings(null);
        assertTrue(entitySet.getExtendedNavigationPropertyBindings().isEmpty());
    }

    @Test
    void testServiceDocumentInclusion() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();
        
        // 先设置必要的属性避免NullPointerException
        entitySet.setName("ServiceEntitySet");
        entitySet.setType("TestNamespace.TestEntity");
        
        // 默认情况下应该包含在服务文档中
        entitySet.setIncludeInServiceDocument(true);
        assertTrue(entitySet.isIncludeInServiceDocument());
        
        // 可以设置为不包含
        entitySet.setIncludeInServiceDocument(false);
        assertFalse(entitySet.isIncludeInServiceDocument());
        
        // 验证toString包含相关信息
        String result = entitySet.toString();
        assertTrue(result.contains("includeInServiceDocument=false"));
    }

    @Test
    void testEntityTypeReference() {
        ExtendedCsdlEntitySetRefactored entitySet = new ExtendedCsdlEntitySetRefactored();
        
        // 设置实体类型引用
        String entityTypeReference = "TestNamespace.Customer";
        entitySet.setType(entityTypeReference);
        
        assertEquals(entityTypeReference, entitySet.getType());
        
        // 验证toString包含类型信息
        entitySet.setName("Customers");
        String result = entitySet.toString();
        assertTrue(result.contains(entityTypeReference));
    }
}
