using Lesson4.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Deltas;
using Microsoft.AspNetCore.OData.Query;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson4.Controllers
{
    public class EmployeesController : ODataController
    {
        private static IList<Employee> employees = GetEmployees();

        private static List<Employee> GetEmployees()
        {
            var employee5 = new Manager { Id = 5, Name = "Employee 5" };
            var employee1 = new Employee { Id = 1, Name = "Employee 1", Supervisor = employee5 };
            var employee2 = new Employee { Id = 2, Name = "Employee 2", Supervisor = employee5 };
            var employee3 = new Employee { Id = 3, Name = "Employee 3", Supervisor = employee5 };
            var employee4 = new Employee { Id = 4, Name = "Employee 4" }; // No Supervisor
            var employee6 = new Manager { Id = 6, Name = "Employee 6" };

            employee5.DirectReports = new List<Employee> { employee1, employee2, employee3 };
            employee5.PersonalAssistant = employee3;

            return new List<Employee> { employee1, employee2, employee3, employee4, employee5, employee6 };
        }

        [EnableQuery]
        public ActionResult<IEnumerable<Employee>> Get()
        {
            return Ok(employees);
        }

        public ActionResult<Employee> GetSupervisor([FromRoute] int key)
        {
            var employee = employees.SingleOrDefault(d => d.Id.Equals(key));

            if (employee == null)
            {
                return NotFound();
            }

            if (employee.Supervisor == null)
            {
                return NotFound("The employee does not have a supervisor.");
            }

            return employee.Supervisor;
        }

        [EnableQuery]
        public ActionResult<IEnumerable<Employee>> GetDirectReportsFromManager([FromRoute] int key)
        {
            var manager = employees.OfType<Manager>().SingleOrDefault(d => d.Id.Equals(key));

            if (manager == null)
            {
                return NotFound();
            }

            return manager.DirectReports;
        }

        public ActionResult PostToPeers([FromRoute] int key, [FromBody] Employee peer)
        {
            var employee = employees.SingleOrDefault(d => d.Id.Equals(key));

            if (employee == null)
            {
                return NotFound();
            }

            employees.Add(peer);
            if (employee.Peers == null)
            {
                employee.Peers = new List<Employee>();
            }
            employee.Peers.Add(peer);
            return Created(peer);
        }

        public ActionResult PostToDirectReportsFromManager([FromRoute] int key, [FromBody] Employee employee)
        {
            var manager = employees.OfType<Manager>().SingleOrDefault(d => d.Id.Equals(key));

            if (manager == null)
            {
                return NotFound();
            }

            employees.Add(employee);
            manager.DirectReports.Add(employee);

            return Created(employee);
        }

        public ActionResult PutToSupervisor([FromRoute] int key, [FromBody] Employee supervisor)
        {
            var employee = employees.SingleOrDefault(d => d.Id.Equals(key));

            if (employee == null)
            {
                return NotFound();
            }

            employee.Supervisor = supervisor;

            return Ok();
        }

        public ActionResult PutToPersonalAssistantFromManager([FromRoute] int key, [FromBody] Employee personalAssistant)
        {
            var manager = employees.OfType<Manager>().SingleOrDefault(d => d.Id.Equals(key));

            if (manager == null)
            {
                return NotFound();
            }

            manager.PersonalAssistant = personalAssistant;

            return Ok();
        }

        public ActionResult PatchToSupervisor([FromRoute] int key, [FromBody] Delta<Employee> delta)
        {
            var employee = employees.SingleOrDefault(d => d.Id.Equals(key));

            if (employee == null)
            {
                return NotFound();
            }

            if (employee.Supervisor != null)
            {
                delta.Patch(employee.Supervisor);
            }

            return Ok();
        }

        public ActionResult PatchToPersonalAssistantFromManager([FromRoute] int key, [FromBody] Delta<Employee> delta)
        {
            var manager = employees.OfType<Manager>().SingleOrDefault(d => d.Id.Equals(key));

            if (manager == null)
            {
                return NotFound();
            }

            if (manager.PersonalAssistant != null)
            {
                delta.Patch(manager.PersonalAssistant);
            }

            return Ok();
        }
    }
}