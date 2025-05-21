using Lesson6.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Formatter;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson6.Controllers
{
    public class EmployeesController : ODataController
    {
        private static List<Employee> employees =
        [
            new Employee { Id = 1, Name = "Employee 1" },
            new Employee { Id = 2, Name = "Employee 2" },
            new Employee { Id = 3, Name = "Employee 3" },
            new Employee { Id = 4, Name = "Employee 4" },
            new Manager { Id = 5, Name = "Employee 5" },
            new Manager { Id = 6, Name = "Employee 6" }
        ];

        [HttpPost]
        public ActionResult ConferSwagGifts(ODataActionParameters parameters)
        {
            if (parameters != null && parameters.TryGetValue("SwagGift", out object? swag) && swag != null)
            {
                foreach (var employee in employees)
                {
                    employee.SwagGift = swag.ToString();
                }
            }
            else
            {
                return BadRequest();
            }

            return Ok();
        }

        [HttpPost]
        public ActionResult ConferSwagGift([FromRoute] int key, ODataActionParameters parameters)
        {
            if (parameters != null && parameters.TryGetValue("SwagGift", out object? swag) && swag != null)
            {
                var employee = employees.SingleOrDefault(d => d.Id.Equals(key));

                if (employee == null)
                {
                    return NotFound();
                }

                employee.SwagGift = swag.ToString();
            }
            else
            {
                return BadRequest();
            }

            return Ok();
        }

        [HttpPost]
        public ActionResult ConferBonusesOnCollectionOfManager(ODataActionParameters parameters)
        {
            if (parameters != null && parameters.TryGetValue("Bonus", out object? bonus) && bonus != null)
            {
                var managers = employees.OfType<Manager>();

                foreach (var manager in managers)
                {
                    manager.Bonus = Convert.ToDecimal(bonus);
                }
            }
            else
            {
                return BadRequest();
            }

            return Ok();
        }

        [HttpPost]
        public ActionResult ConferBonusOnManager([FromRoute] int key, ODataActionParameters parameters)
        {
            if (parameters != null && parameters.TryGetValue("Bonus", out object? bonus) && bonus != null)
            {
                var manager = employees.OfType<Manager>().SingleOrDefault(d => d.Id.Equals(key));

                if (manager == null)
                {
                    return NotFound();
                }

                manager.Bonus = Convert.ToDecimal(bonus);
            }
            else
            {
                return BadRequest();
            }

            return Ok();
        }


        [HttpPost("odata/ComputeSalary")]
        public ActionResult<decimal> ComputeSalary(ODataActionParameters parameters)
        {
            object? hourlyRateAsObject, hoursWorkedAsObject;
            decimal hourlyRate;
            int hoursWorked;

            if (parameters == null
                || !parameters.TryGetValue("hourlyRate", out hourlyRateAsObject) || hourlyRateAsObject == null
                || !decimal.TryParse(hourlyRateAsObject.ToString(), out hourlyRate)
                || !parameters.TryGetValue("hoursWorked", out hoursWorkedAsObject) || hoursWorkedAsObject == null
                || !int.TryParse(hoursWorkedAsObject.ToString(), out hoursWorked))
            {
                return BadRequest();
            }

            return hourlyRate * hoursWorked;
        }
    }
}
