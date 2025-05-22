using Microsoft.AspNetCore.OData;
using Microsoft.OData.Edm;
using System.Xml.Serialization;

var builder = WebApplication.CreateBuilder(args);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 添加枚举类型
static void addEnumType(EdmModel model)
{
    EdmEnumType color = new("Lesson10", "Color");
    color.AddMember(new EdmEnumMember(color, "Red", new EdmEnumMemberValue(0)));
    color.AddMember(new EdmEnumMember(color, "Blue", new EdmEnumMemberValue(1)));
    color.AddMember(new EdmEnumMember(color, "Green", new EdmEnumMemberValue(2)));
    model.AddElement(color);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 添加复杂类型
static void addComplexType(EdmModel model)
{
    EdmComplexType address = new("Lesson10", "Address");
    address.AddStructuralProperty("Street", EdmPrimitiveTypeKind.String);
    address.AddStructuralProperty("City", EdmPrimitiveTypeKind.String);
    address.AddStructuralProperty("State", EdmPrimitiveTypeKind.String);
    address.AddStructuralProperty("Country", EdmPrimitiveTypeKind.String);
    model.AddElement(address);
}

static void addDerivedComplexType(EdmModel model)
{
    EdmComplexType derivedAddress = new("Lesson10", "PostalAddress", baseType : model.FindType("Lesson10.Address") as EdmComplexType);
    derivedAddress.AddStructuralProperty("PostalCode", EdmPrimitiveTypeKind.String);
    model.AddElement(derivedAddress);
}

static void addAbstractComplexType(EdmModel model)
{
    EdmComplexType addressAbstract = new("Lesson10", "AddressAbstract", baseType: null, isAbstract: true);
    model.AddElement(addressAbstract);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 添加实体类型
static void addEntityType(EdmModel model)
{
    EdmEntityType customer = new("Lesson10", "Customer");
    customer.AddKeys(customer.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32)); 
    customer.AddStructuralProperty("Name", EdmPrimitiveTypeKind.String);
    var postalAddressType = model.FindType("Lesson10.PostalAddress") as IEdmComplexType;
    if (postalAddressType != null)
    {
        customer.AddStructuralProperty("PostalAddress", new EdmComplexTypeReference(postalAddressType, true));
    }
    model.AddElement(customer);
}

static void addDerivedEntityType(EdmModel model)
{
    EdmEntityType vipCustomer = new("Lesson10", "VipCustomer", baseType: model.FindType("Lesson10.Customer") as IEdmEntityType);
    vipCustomer.AddStructuralProperty("VipLevel", EdmPrimitiveTypeKind.String);
    model.AddElement(vipCustomer);
}

static void addAbstractEntityType(EdmModel model)
{
    EdmEntityType orderAbstract = new("Lesson10", "OrderAbstract", baseType: null, isAbstract: true, isOpen: false);
    orderAbstract.AddKeys(orderAbstract.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32));
    model.AddElement(orderAbstract);
}

static void addDerivedAbstractEntityType(EdmModel model)
{
    EdmEntityType order = new("Lesson10", "Order", baseType: model.FindType("Lesson10.OrderAbstract") as IEdmEntityType);
    order.AddStructuralProperty("Amount", EdmPrimitiveTypeKind.Decimal);
    model.AddElement(order);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 添加单实体类型
static void addSingleEntityType(EdmModel model)
{
    EdmEntityType singleCustomer = new("Lesson10", "SingleCustomer", baseType: null, isAbstract: false, isOpen: false);
    singleCustomer.AddKeys(singleCustomer.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32));
    singleCustomer.AddStructuralProperty("Name", EdmPrimitiveTypeKind.String);
    model.AddElement(singleCustomer);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 添加默认容器
static void addDefaultContainer(EdmModel model)
{
    EdmEntityContainer container = new("Lesson10", "DefaultContainer");
    model.AddElement(container);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 添加导航属性
static void addNavigationProperty(EdmModel model)
{
    EdmEntityType? customer = model.FindType("Lesson10.Customer") as EdmEntityType;
    EdmEntityType? order = model.FindType("Lesson10.Order") as EdmEntityType;
    if (customer != null && order != null)
    {
        customer.AddUnidirectionalNavigation(new EdmNavigationPropertyInfo
        {
            Name = "Orders",
            TargetMultiplicity = EdmMultiplicity.Many,
            Target = order,
            ContainsTarget = false
        });
        order.AddUnidirectionalNavigation(new EdmNavigationPropertyInfo
        {
            Name = "Customer",
            TargetMultiplicity = EdmMultiplicity.One,
            Target = customer,
            ContainsTarget = false
        });
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

static void addUnboundFunction(EdmModel model)
{
    EdmFunction function = new("Lesson10", "GetCustomerById", new EdmEntityTypeReference(model.FindType("Lesson10.Customer") as IEdmEntityType, true), isBound: false, entitySetPathExpression: null, isComposable: false);
    function.AddParameter("key", EdmCoreModel.Instance.GetInt32(false));
    model.AddElement(function);
}

static void addBoundFunction(EdmModel model)
{
    EdmFunction function = new("Lesson10", "GetCustomerName", EdmCoreModel.Instance.GetString(false), isBound: true, entitySetPathExpression: null, isComposable: false);
    function.AddParameter("customer", new EdmEntityTypeReference(model.FindType("Lesson10.Customer") as IEdmEntityType, false));
    model.AddElement(function);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

static void addUnboundAction(EdmModel model)
{
    EdmAction action = new("Lesson10", "UpdateCustomer", EdmCoreModel.Instance.GetString(false), isBound: false, entitySetPathExpression: null);
    action.AddParameter("customer", new EdmEntityTypeReference(model.FindType("Lesson10.Customer") as IEdmEntityType, false));
    model.AddElement(action);
}

static void addBoundAction(EdmModel model)
{
    EdmAction action = new("Lesson10", "UpdateCustomerName", EdmCoreModel.Instance.GetString(false), isBound: true, entitySetPathExpression: null);
    action.AddParameter("customer", new EdmEntityTypeReference(model.FindType("Lesson10.Customer") as IEdmEntityType, false));
    action.AddParameter("name", EdmCoreModel.Instance.GetString(false));
    model.AddElement(action);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

static IEdmModel GetEdmModel()
{
    EdmModel model = new EdmModel();

    addEnumType(model);

    addComplexType(model);
    addDerivedComplexType(model);
    addAbstractComplexType(model);

    addEntityType(model);
    addDerivedEntityType(model);
    addAbstractEntityType(model);
    addDerivedAbstractEntityType(model);

    addSingleEntityType(model);

    addNavigationProperty(model);

    addUnboundFunction(model);
    addBoundFunction(model);

    addUnboundAction(model);
    addBoundAction(model);

    addDefaultContainer(model);

    return model;
}

// 添加 OData 服务和配置
builder.Services.AddControllers().AddOData(options =>
    options.Select()
       .Filter()
       .OrderBy()
       .Expand()
       .Count()
       .SetMaxTop(null)
       .AddRouteComponents("odata", GetEdmModel())
);

var app = builder.Build();

app.UseRouting();

app.MapControllers();

app.Run();