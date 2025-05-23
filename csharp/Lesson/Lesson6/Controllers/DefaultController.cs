using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Formatter;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson6.Controllers
{
    public class DefaultController : ODataController
    {
        [HttpPost("odata/ComputeSalary")]
        public ActionResult<decimal> ComputeSalary(ODataActionParameters parameters)
        {
            if (parameters == null)
            {
                return BadRequest();
            }

            if (!parameters.TryGetValue("hourlyRate", out var hourlyRateAsObject)
                || hourlyRateAsObject == null
                || !decimal.TryParse(hourlyRateAsObject.ToString(), out var hourlyRate))
            {
                return BadRequest();
            }

            if (!parameters.TryGetValue("hoursWorked", out var hoursWorkedAsObject)
                || hoursWorkedAsObject == null
                || !int.TryParse(hoursWorkedAsObject.ToString(), out var hoursWorked))
            {
                return BadRequest();
            }

            return hourlyRate * hoursWorked;
        }
    }
}
