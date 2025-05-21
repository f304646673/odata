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

static IEdmModel GetEdmModel()
{
    EdmModel model = new EdmModel();
    addEnumType(model);
    addComplexType(model);
    addDerivedComplexType(model);
    addAbstractComplexType(model);
    addEntityType(model);

    var container = new EdmEntityContainer("Lesson10", "DefaultContainer");
    model.AddElement(container);

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