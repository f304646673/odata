package org.apache.olingo.schema.repository.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 测试用的Schema XML生成器
 * 提供各种测试场景的Schema XML内容
 */
public class TestSchemaGenerator {
    
    /**
     * 生成简单的EntityType Schema
     */
    public static String generateSimpleEntityTypeSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"TestNamespace\" Alias=\"TN\">\n" +
            "      <EntityType Name=\"Person\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "        <Property Name=\"Age\" Type=\"Edm.Int32\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityContainer Name=\"TestContainer\">\n" +
            "        <EntitySet Name=\"People\" EntityType=\"TestNamespace.Person\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成具有继承关系的Schema
     */
    public static String generateInheritanceSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"InheritanceNamespace\" Alias=\"IN\">\n" +
            "      <EntityType Name=\"Animal\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityType Name=\"Dog\" BaseType=\"InheritanceNamespace.Animal\">\n" +
            "        <Property Name=\"Breed\" Type=\"Edm.String\" MaxLength=\"50\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityType Name=\"Cat\" BaseType=\"InheritanceNamespace.Animal\">\n" +
            "        <Property Name=\"Color\" Type=\"Edm.String\" MaxLength=\"30\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityContainer Name=\"AnimalContainer\">\n" +
            "        <EntitySet Name=\"Animals\" EntityType=\"InheritanceNamespace.Animal\"/>\n" +
            "        <EntitySet Name=\"Dogs\" EntityType=\"InheritanceNamespace.Dog\"/>\n" +
            "        <EntitySet Name=\"Cats\" EntityType=\"InheritanceNamespace.Cat\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成具有ComplexType的Schema
     */
    public static String generateComplexTypeSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"ComplexNamespace\" Alias=\"CN\">\n" +
            "      <ComplexType Name=\"Address\">\n" +
            "        <Property Name=\"Street\" Type=\"Edm.String\" MaxLength=\"200\"/>\n" +
            "        <Property Name=\"City\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "        <Property Name=\"Country\" Type=\"Edm.String\" MaxLength=\"50\"/>\n" +
            "      </ComplexType>\n" +
            "      \n" +
            "      <EntityType Name=\"Company\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "        <Property Name=\"HeadOffice\" Type=\"ComplexNamespace.Address\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityContainer Name=\"BusinessContainer\">\n" +
            "        <EntitySet Name=\"Companies\" EntityType=\"ComplexNamespace.Company\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成具有NavigationProperty的Schema
     */
    public static String generateNavigationSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"NavigationNamespace\" Alias=\"NN\">\n" +
            "      <EntityType Name=\"Customer\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "        <NavigationProperty Name=\"Orders\" Type=\"Collection(NavigationNamespace.Order)\" \n" +
            "                           Partner=\"Customer\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityType Name=\"Order\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"OrderDate\" Type=\"Edm.DateTimeOffset\"/>\n" +
            "        <Property Name=\"CustomerID\" Type=\"Edm.Int32\"/>\n" +
            "        <NavigationProperty Name=\"Customer\" Type=\"NavigationNamespace.Customer\" \n" +
            "                           Partner=\"Orders\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityContainer Name=\"SalesContainer\">\n" +
            "        <EntitySet Name=\"Customers\" EntityType=\"NavigationNamespace.Customer\">\n" +
            "          <NavigationPropertyBinding Path=\"Orders\" Target=\"Orders\"/>\n" +
            "        </EntitySet>\n" +
            "        <EntitySet Name=\"Orders\" EntityType=\"NavigationNamespace.Order\">\n" +
            "          <NavigationPropertyBinding Path=\"Customer\" Target=\"Customers\"/>\n" +
            "        </EntitySet>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成具有Actions和Functions的Schema
     */
    public static String generateActionFunctionSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"ActionFunctionNamespace\" Alias=\"AFN\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "        <Property Name=\"Price\" Type=\"Edm.Decimal\" Precision=\"10\" Scale=\"2\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <Action Name=\"DiscountProduct\">\n" +
            "        <Parameter Name=\"Product\" Type=\"ActionFunctionNamespace.Product\"/>\n" +
            "        <Parameter Name=\"Percentage\" Type=\"Edm.Decimal\"/>\n" +
            "        <ReturnType Type=\"ActionFunctionNamespace.Product\"/>\n" +
            "      </Action>\n" +
            "      \n" +
            "      <Function Name=\"GetProductsByPrice\">\n" +
            "        <Parameter Name=\"MinPrice\" Type=\"Edm.Decimal\"/>\n" +
            "        <Parameter Name=\"MaxPrice\" Type=\"Edm.Decimal\"/>\n" +
            "        <ReturnType Type=\"Collection(ActionFunctionNamespace.Product)\"/>\n" +
            "      </Function>\n" +
            "      \n" +
            "      <EntityContainer Name=\"ProductContainer\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"ActionFunctionNamespace.Product\"/>\n" +
            "        <ActionImport Name=\"DiscountProduct\" Action=\"ActionFunctionNamespace.DiscountProduct\"/>\n" +
            "        <FunctionImport Name=\"GetProductsByPrice\" Function=\"ActionFunctionNamespace.GetProductsByPrice\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成具有循环依赖的Schema
     */
    public static String generateCircularDependencySchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"CircularNamespace\" Alias=\"CIN\">\n" +
            "      <EntityType Name=\"TypeA\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <NavigationProperty Name=\"RelatedB\" Type=\"CircularNamespace.TypeB\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityType Name=\"TypeB\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <NavigationProperty Name=\"RelatedA\" Type=\"CircularNamespace.TypeA\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityContainer Name=\"CircularContainer\">\n" +
            "        <EntitySet Name=\"TypeAs\" EntityType=\"CircularNamespace.TypeA\">\n" +
            "          <NavigationPropertyBinding Path=\"RelatedB\" Target=\"TypeBs\"/>\n" +
            "        </EntitySet>\n" +
            "        <EntitySet Name=\"TypeBs\" EntityType=\"CircularNamespace.TypeB\">\n" +
            "          <NavigationPropertyBinding Path=\"RelatedA\" Target=\"TypeAs\"/>\n" +
            "        </EntitySet>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成跨namespace依赖的第一个Schema
     */
    public static String generateCrossNamespaceDependencySchema1() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"CrossNamespace1\" Alias=\"CN1\">\n" +
            "      <EntityType Name=\"BaseEntity\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <ComplexType Name=\"SharedType\">\n" +
            "        <Property Name=\"Value\" Type=\"Edm.String\"/>\n" +
            "      </ComplexType>\n" +
            "      \n" +
            "      <EntityContainer Name=\"BaseContainer\">\n" +
            "        <EntitySet Name=\"BaseEntities\" EntityType=\"CrossNamespace1.BaseEntity\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成跨namespace依赖的第二个Schema（依赖第一个）
     */
    public static String generateCrossNamespaceDependencySchema2() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" \n" +
            "            Namespace=\"CrossNamespace2\" Alias=\"CN2\">\n" +
            "      <EntityType Name=\"DerivedEntity\" BaseType=\"CrossNamespace1.BaseEntity\">\n" +
            "        <Property Name=\"ExtendedField\" Type=\"CrossNamespace1.SharedType\"/>\n" +
            "      </EntityType>\n" +
            "      \n" +
            "      <EntityContainer Name=\"ExtendedContainer\">\n" +
            "        <EntitySet Name=\"DerivedEntities\" EntityType=\"CrossNamespace2.DerivedEntity\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 生成无效的Schema（缺少namespace）
     */
    public static String generateInvalidSchema() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
            "      <EntityType Name=\"InvalidEntity\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "      </EntityType>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
    
    /**
     * 将字符串转换为InputStream
     */
    public static InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
