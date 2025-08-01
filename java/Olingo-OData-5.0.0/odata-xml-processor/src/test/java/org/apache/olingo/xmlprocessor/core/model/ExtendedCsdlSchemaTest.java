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
public class ExtendedCsdlSchemaTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlSchema schema = new ExtendedCsdlSchema();
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

        ExtendedCsdlSchema extended = ExtendedCsdlSchema.fromCsdlSchema(source);

        assertNotNull(extended);
        assertEquals("TestNamespace", extended.getNamespace());
        assertEquals("TestAlias", extended.getAlias());
        assertEquals(1, extended.getExtendedEntityTypes().size());
        assertEquals("TestEntity", extended.getExtendedEntityTypes().get(0).getName());
    }

    @Test
    public void testFromCsdlSchemaNull() {
        ExtendedCsdlSchema extended = ExtendedCsdlSchema.fromCsdlSchema(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlSchema schema = new ExtendedCsdlSchema()
            .setNamespace("FluentNamespace")
            .setAlias("FluentAlias");

        assertEquals("FluentNamespace", schema.getNamespace());
        assertEquals("FluentAlias", schema.getAlias());
    }

    @Test
    public void testEntityTypeManagement() {
        ExtendedCsdlSchema schema = new ExtendedCsdlSchema()
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
        ExtendedCsdlSchema schema = new ExtendedCsdlSchema()
            .setNamespace("TestNamespace");

        ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType()
            .setName("TestComplexType");

        schema.addExtendedComplexType(complexType);

        assertEquals(1, schema.getExtendedComplexTypes().size());
        assertEquals("TestComplexType", schema.getExtendedComplexTypes().get(0).getName());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlSchema schema = new ExtendedCsdlSchema()
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
        ExtendedCsdlSchema schema = new ExtendedCsdlSchema()
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
        ExtendedCsdlSchema schema = new ExtendedCsdlSchema()
            .setNamespace("TestNamespace")
            .setAlias("TestAlias");

        String toString = schema.toString();
        assertTrue(toString.contains("TestNamespace"));
        assertTrue(toString.contains("TestAlias"));
    }
}
