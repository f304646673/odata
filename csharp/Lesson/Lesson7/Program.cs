using Lesson7.Models;
using Microsoft.AspNetCore.OData;
using Microsoft.OData.ModelBuilder;
using Microsoft.OData.Edm;

var builder = WebApplication.CreateBuilder(args);

// ��ȡ OData EDM ģ�͹���Ϊ����������ά������չ
static IEdmModel GetEdmModel()
{
    var modelBuilder = new ODataConventionModelBuilder();
    modelBuilder.EntitySet<Customer>("Customers");
    modelBuilder.EntitySet<Order>("Orders");
    modelBuilder.EntitySet<Employee>("Employees");

    return modelBuilder.GetEdmModel();
}

// ��� OData ���������
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