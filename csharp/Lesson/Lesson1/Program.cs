using Lesson1.Models;
using Microsoft.AspNetCore.OData;
using Microsoft.OData.Edm;
using Microsoft.OData.ModelBuilder;

var builder = WebApplication.CreateBuilder(args);

// ��ȡ OData EDM ģ�͹���Ϊ����������ά������չ
static IEdmModel GetEdmModel()
{
    var modelBuilder = new ODataConventionModelBuilder();
    modelBuilder.EntityType<Order>();
    modelBuilder.EntitySet<Customer>("Customers");
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