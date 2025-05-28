using Lesson7.Models;
using Microsoft.AspNetCore.OData;
using Microsoft.OData.ModelBuilder;
using Microsoft.OData.Edm;

var builder = WebApplication.CreateBuilder(args);

// 提取 OData EDM 模型构建为方法，便于维护和扩展
static IEdmModel GetEdmModel()
{
    var modelBuilder = new ODataConventionModelBuilder();
    modelBuilder.EntitySet<Customer>("Customers");
    modelBuilder.EntitySet<Order>("Orders");
    modelBuilder.EntitySet<Employee>("Employees");

    return modelBuilder.GetEdmModel();
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