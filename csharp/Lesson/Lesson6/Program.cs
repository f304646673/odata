using Lesson6.Models;
using Microsoft.AspNetCore.OData;
using Microsoft.OData.ModelBuilder;
using Microsoft.OData.Edm;

var builder = WebApplication.CreateBuilder(args);

// ��ȡ OData EDM ģ�͹���Ϊ����������ά������չ
static IEdmModel GetEdmModel()
{
    var modelBuilder = new ODataConventionModelBuilder();
    modelBuilder.EntitySet<Employee>("Employees");
    var employeeEntityType = modelBuilder.EntitySet<Employee>("Employees").EntityType;
    var managerEntityType = modelBuilder.EntityType<Manager>();

    employeeEntityType.Collection.Action("ConferSwagGifts")
        .Parameter<string>("SwagGift");
    employeeEntityType.Action("ConferSwagGift")
        .Parameter<string>("SwagGift");
    managerEntityType.Collection.Action("ConferBonuses")
        .Parameter<decimal>("Bonus");
    managerEntityType.Action("ConferBonus")
        .Parameter<decimal>("Bonus");

    var getSalaryFunction = modelBuilder.Function("GetSalary");
    getSalaryFunction.Parameter<decimal>("hourlyRate");
    getSalaryFunction.Parameter<int>("hoursWorked");
    getSalaryFunction.Returns<decimal>();

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