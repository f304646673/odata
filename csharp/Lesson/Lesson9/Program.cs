using Lesson9.Models;
using Microsoft.AspNetCore.OData;
using Microsoft.OData.Edm;
using Microsoft.OData.ModelBuilder;
using System;

var builder = WebApplication.CreateBuilder(args);

// 提取 OData EDM 模型构建为方法，便于维护和扩展
static IEdmModel GetEdmModel()
{
    var modelBuilder = new ODataConventionModelBuilder();
    modelBuilder.EntitySet<Book>("books");

    modelBuilder.EntityType<Book>().Collection.Function("mostRecent").Returns<string>();
    modelBuilder.Function("returnAllForKidsBooks").ReturnsFromEntitySet<Book>("books");

    modelBuilder.EntityType<Book>().Action("rate").Parameter<int>("rating");
    var action = modelBuilder.Action("incrementBookYear").ReturnsFromEntitySet<Book>("books");
    action.Parameter<int>("increment");
    action.Parameter<string>("id");

    return modelBuilder.GetEdmModel();
}

// 添加 OData 服务和配置
builder.Services.AddControllers().AddOData(options =>
    {
        options.AddRouteComponents("odata", GetEdmModel()).Count().OrderBy().Filter().Select().Expand();
        options.RouteOptions.EnableNonParenthesisForEmptyParameterFunction = true;
    }
    
);

var app = builder.Build();

app.UseRouting();

app.MapControllers();

app.Run();