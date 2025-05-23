using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson5.Controllers
{
    public class DefaultController : ODataController
    {

        [HttpGet("odata/GetSalary(hourlyRate={hourlyRate},hoursWorked={hoursWorked})")]
        public ActionResult<decimal> GetSalary(decimal hourlyRate, int hoursWorked)
        {
            return hourlyRate * hoursWorked;
        }
    }
}
