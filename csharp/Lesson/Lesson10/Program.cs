using Microsoft.AspNetCore.OData;
using Microsoft.OData.Edm;
using System.Xml.Serialization;

var builder = WebApplication.CreateBuilder(args);

static void addEnumType(EdmModel model)
{
    EdmEnumType color = new("Lesson10", "Color");
    color.AddMember(new EdmEnumMember(color, "Red", new EdmEnumMemberValue(0)));
    color.AddMember(new EdmEnumMember(color, "Blue", new EdmEnumMemberValue(1)));
    color.AddMember(new EdmEnumMember(color, "Green", new EdmEnumMemberValue(2)));
    model.AddElement(color);
}

static void addComplexType(EdmModel model)
{
    EdmComplexType employee = new("Lesson10", "Employee");
    employee.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32);
    employee.AddStructuralProperty("Name", EdmPrimitiveTypeKind.String);
    employee.AddStructuralProperty("PerfRating", EdmPrimitiveTypeKind.Decimal);
    model.AddElement(employee);
}

static void addDerivedComplexType(EdmModel model)
{
    EdmComplexType address = new("Lesson10", "Address");
    address.AddStructuralProperty("Street", EdmPrimitiveTypeKind.String);
    address.AddStructuralProperty("City", EdmPrimitiveTypeKind.String);
    address.AddStructuralProperty("State", EdmPrimitiveTypeKind.String);
    address.AddStructuralProperty("ZipCode", EdmPrimitiveTypeKind.String);
    model.AddElement(address);
    EdmComplexType derivedAddress = new("Lesson10", "DerivedAddress", address);
    derivedAddress.AddStructuralProperty("Country", EdmPrimitiveTypeKind.String);
    model.AddElement(derivedAddress);
}

static void addAbstractComplexType(EdmModel model)
{
    EdmComplexType person = new("Lesson10", "Person", baseType: null, isAbstract: true);
    person.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32);
    person.AddStructuralProperty("FirstName", EdmPrimitiveTypeKind.String);
    person.AddStructuralProperty("LastName", EdmPrimitiveTypeKind.String);
    model.AddElement(person);
}

static void addEntityType(EdmModel model)
{
    EdmEntityType customer = new("Lesson10", "Customer");
    customer.AddKeys(customer.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32)); 
    customer.AddStructuralProperty("Name", EdmPrimitiveTypeKind.String);
    var derivedAddressType = model.FindType("Lesson10.DerivedAddress") as IEdmComplexType;
    if (derivedAddressType != null)
    {
        customer.AddStructuralProperty("Address", new EdmComplexTypeReference(derivedAddressType, true));
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
    EdmEntityType peopele = new("Lesson10", "Peopele", baseType: null, isAbstract: true, isOpen: false);
    peopele.AddKeys(peopele.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32));
    model.AddElement(peopele);
}

static void addSingleEntityType(EdmModel model)
{
    EdmEntityType singleCustomer = new("Lesson10", "SingleCustomer", baseType: null, isAbstract: false, isOpen: false);
    singleCustomer.AddKeys(singleCustomer.AddStructuralProperty("Id", EdmPrimitiveTypeKind.Int32));
    singleCustomer.AddStructuralProperty("Name", EdmPrimitiveTypeKind.String);
    model.AddElement(singleCustomer);
}

static void addDefaultContainer(EdmModel model)
{
    EdmEntityContainer container = new("Lesson10", "DefaultContainer");
    model.AddElement(container);
}

static void addNavigationProperty(EdmModel model)
{
    EdmEntityType customer = model.FindType("Lesson10.Customer") as EdmEntityType;
    EdmEntityType order = model.FindType("Lesson10.Order") as EdmEntityType;
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
    addSingleEntityType(model);
    addNavigationProperty(model);

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