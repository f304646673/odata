package org.apache.olingo.xmlprocessor.core.model;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * ExtendedCsdlEnumTypeRefactored测试类
 */
public class ExtendedCsdlEnumTypeTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedCsdlEnumType enumType = new ExtendedCsdlEnumType();
        assertNotNull(enumType);
        assertNotNull(enumType.asCsdlEnumType());
    }

    @Test
    public void testFromCsdlEnumType() {
        CsdlEnumType source = new CsdlEnumType()
            .setName("TestEnumType")
            .setUnderlyingType("Edm.Int32")
            .setFlags(true);

        // 添加枚举成员
        CsdlEnumMember member1 = new CsdlEnumMember().setName("Value1").setValue("1");
        CsdlEnumMember member2 = new CsdlEnumMember().setName("Value2").setValue("2");
        source.setMembers(Arrays.asList(member1, member2));

        ExtendedCsdlEnumType extended = ExtendedCsdlEnumType.fromCsdlEnumType(source);

        assertNotNull(extended);
        assertEquals("TestEnumType", extended.getName());
        assertEquals("Edm.Int32", extended.getUnderlyingType());
        assertTrue(extended.isFlags());
        assertEquals(2, extended.getMembers().size());
    }

    @Test
    public void testFromCsdlEnumTypeNull() {
        ExtendedCsdlEnumType extended = ExtendedCsdlEnumType.fromCsdlEnumType(null);
        assertNull(extended);
    }

    @Test
    public void testFluentAPI() {
        ExtendedCsdlEnumType enumType = new ExtendedCsdlEnumType()
            .setName("FluentEnumType")
            .setUnderlyingType("Edm.String")
            .setFlags(false)
            .setNamespace("TestNamespace");

        assertEquals("FluentEnumType", enumType.getName());
        assertEquals("Edm.String", enumType.getUnderlyingType());
        assertFalse(enumType.isFlags());
        assertEquals("TestNamespace", enumType.getNamespace());
    }

    @Test
    public void testMemberManagement() {
        CsdlEnumMember member1 = new CsdlEnumMember().setName("Value1").setValue("1");
        CsdlEnumMember member2 = new CsdlEnumMember().setName("Value2").setValue("2");

        ExtendedCsdlEnumType enumType = new ExtendedCsdlEnumType()
            .setName("TestEnum")
            .setMembers(Arrays.asList(member1, member2));

        assertEquals(2, enumType.getMembers().size());
        assertEquals("Value1", enumType.getMembers().get(0).getName());
        assertEquals("Value2", enumType.getMembers().get(1).getName());
    }

    @Test
    public void testExtendedCsdlElementMethods() {
        ExtendedCsdlEnumType enumType = new ExtendedCsdlEnumType()
            .setName("TestEnumType")
            .setNamespace("TestNamespace");

        assertEquals("TestEnumType", enumType.getElementId());
        assertEquals("TestEnumType", enumType.getElementPropertyName());

        FullQualifiedName fqn = enumType.getElementFullyQualifiedName();
        assertNotNull(fqn);
        assertEquals("TestNamespace", fqn.getNamespace());
        assertEquals("TestEnumType", fqn.getName());
    }

    @Test
    public void testAnnotationManagement() {
        ExtendedCsdlEnumType enumType = new ExtendedCsdlEnumType()
            .setName("AnnotatedEnumType");

        ExtendedCsdlAnnotation annotation = new ExtendedCsdlAnnotation()
            .setTerm("TestAnnotation")
            .setQualifier("Qualifier1");

        enumType.addExtendedAnnotation(annotation);

        assertEquals(1, enumType.getExtendedAnnotations().size());
        assertEquals("TestAnnotation", enumType.getExtendedAnnotations().get(0).getTerm());
    }

    @Test
    public void testToString() {
        ExtendedCsdlEnumType enumType = new ExtendedCsdlEnumType()
            .setName("TestEnumType")
            .setUnderlyingType("Edm.Int32")
            .setFlags(true)
            .setNamespace("TestNamespace");

        String toString = enumType.toString();
        assertTrue(toString.contains("TestEnumType"));
        assertTrue(toString.contains("TestNamespace"));
        assertTrue(toString.contains("Edm.Int32"));
        assertTrue(toString.contains("true"));
    }
}
