using Lesson5.Models;
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
    employeeEntityType.Collection.Function("GetHighestRating").Returns<int>();
    employeeEntityType.Function("GetRating").Returns<int>();


    var managerEntityType = modelBuilder.EntityType<Manager>();
    managerEntityType.Collection.Function("GetHighestBonus").Returns<decimal>();
    managerEntityType.Function("GetBonus").Returns<decimal>();

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