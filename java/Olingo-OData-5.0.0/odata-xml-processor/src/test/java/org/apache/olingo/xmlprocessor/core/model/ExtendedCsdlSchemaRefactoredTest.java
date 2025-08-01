package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlSchemaRefactored测试类
 */
public class ExtendedCsdlSchemaRefactoredTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlSchemaRefactored schema = new ExtendedCsdlSchemaRefactored();
        assertNotNull(schema);
        assertNotNull(schema.asCsdlSchema());
    }

    @Test
    public void testFromCsdlSchema() {
        CsdlSchema source = new CsdlSchema()
            .setNamespace("TestNamespace")
            .setAlias("TestAlias");

        // 添加一个Entity Type
        CsdlEntityType entityType = new CsdlEntityType().setName("TestEntity");
        source.getEntityTypes().add(entityType);

        ExtendedCsdlSchemaRefactored extended = ExtendedCsdlSchemaRefactored.fromCsdlSchema(source);

        assertNotNull(extended);
        assertEquals("TestNamespace", extended.getNamespace());
        assertEquals("TestAlias", extended.getAlias());
        assertEquals(1, extended.getExtendedEntityTypes().size());
        assertEquals("TestEntity", extended.getExtendedEntityTypes().get(0).getName());
    }

    @Test
    public void testFromCsdlSchemaNull() {
        ExtendedCsdlSchemaRefactored extended = ExtendedCsdlSchemaRefactored.fromCsdlSchema(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlSchemaRefactored schema = new ExtendedCsdlSchemaRefactored()
            .setNamespace("FluentNamespace")
            .setAlias("FluentAlias");

        assertEquals("FluentNamespace", schema.getNamespace());
        assertEquals("FluentAlias", schema.getAlias());
    }

    @Test
    public void testEntityTypeManagement() {
        ExtendedCsdlSchemaRefactored schema = new ExtendedCsdlSchemaRefactored()
            .setNamespace("TestNamespace");

        ExtendedCsdlEntityType entityType = new ExtendedCsdlEntityType()
            .setName("TestEntity");

        schema.addExtendedEntityType(entityType);

        assertEquals(1, schema.getExtendedEntityTypes().size());
        assertEquals("TestEntity", schema.getExtendedEntityTypes().get(0).getName());
        
        // 验证同步到原始数据
        assertEquals(1, schema.getEntityTypes().size());
        assertEquals("TestEntity", schema.getEntityTypes().get(0).getName());
    }

    @Test
    public void testComplexTypeManagement() {
        ExtendedCsdlSchemaRefactored schema = new ExtendedCsdlSchemaRefactored()
            .setNamespace("TestNamespace");

        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType()
            .setName("TestComplexType");

        schema.addExtendedComplexType(complexType);

        assertEquals(1, schema.getExtendedComplexTypes().size());
        assertEquals("TestComplexType", schema.getExtendedComplexTypes().get(0).getName());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlSchemaRefactored schema = new ExtendedCsdlSchemaRefactored()
            .setNamespace("TestNamespace");

        assertEquals("TestNamespace", schema.getElementId());
        assertEquals("TestNamespace", schema.getElementPropertyName());

        FullQualifiedName fqn = schema.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("Schema", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlSchemaRefactored schema = new ExtendedCsdlSchemaRefactored()
            .setNamespace("AnnotatedNamespace");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        schema.addExtendedAnnotation(annotation);

        assertEquals(1, schema.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", schema.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlSchemaRefactored schema = new ExtendedCsdlSchemaRefactored()
            .setNamespace("TestNamespace")
            .setAlias("TestAlias");

        String toString = schema.toString();
        assertTrue(toString.contains("TestNamespace"));
        assertTrue(toString.contains("TestAlias"));
    }
}
