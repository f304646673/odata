using Lesson5.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson5.Controllers
{
    public class EmployeesController : ODataController
    {
        private static List<Employee> employees = new()
        {
            new Employee { Id = 1, Name = "Employee 1", PerfRating = 8 },
            new Employee { Id = 2, Name = "Employee 2", PerfRating = 7 },
            new Employee { Id = 3, Name = "Employee 3", PerfRating = 5 },
            new Employee { Id = 4, Name = "Employee 4", PerfRating = 3 },
            new Manager { Id = 5, Name = "Employee 5", PerfRating = 7, Bonus = 2900 },
            new Manager { Id = 6, Name = "Employee 6", PerfRating = 9, Bonus = 3700 }
        };

        [HttpGet]
        public ActionResult<decimal> GetHighestRating()
        {
            if (employees.Count < 1)
            {
                return NoContent();
            }

            return Ok(employees.Select(d => d.PerfRating).OrderByDescending(d => d).First());
        }


        [HttpGet]
        public ActionResult<decimal> GetRating([FromRoute] int key)
        {
            var employee = employees.SingleOrDefault(d => d.Id.Equals(key));

            if (employee == null)
            {
                return NotFound();
            }

            return Ok(employee.PerfRating);
        }

        [HttpGet]
        public ActionResult<decimal> GetHighestBonusOnCollectionOfManager()
        {
            var managers = employees.OfType<Manager>().ToArray();

            if (managers.Length < 1)
            {
                return NoContent();
            }

            return managers.Select(d => d.Bonus).OrderByDescending(d => d).First();
        }

        [HttpGet]
        public ActionResult<decimal> GetBonusOnManager([FromRoute] int key)
        {
            var manager = employees.OfType<Manager>().SingleOrDefault(d => d.Id.Equals(key));

            if (manager == null)
            {
                return NotFound();
            }

            return Ok(manager.Bonus);
        }

    }
}