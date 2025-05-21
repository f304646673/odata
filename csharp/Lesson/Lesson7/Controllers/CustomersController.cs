using Lesson7.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Query;
using Microsoft.AspNetCore.OData.Routing.Controllers;
using Microsoft.OData.Edm;
using Microsoft.OData.UriParser;
using System;
using System.Collections.Generic;
using Microsoft.AspNetCore.OData.Extensions;
using System.Linq;

namespace Lesson7.Controllers
{
    public class CustomersControllerODataController : ODataController
    {
        private static Random random = new Random();
        private static List<Customer> customers = new List<Customer>
        {
            new Customer
            {
                Id = 1,
                Name = "Customer 1",
                Orders = new List<Order> { new Order { Id = 1, Amount = 80 }, new Order { Id = 2, Amount = 40 } }
            },
            new EnterpriseCustomer
            {
                Id = 2,
                Name = "Customer 2",
                Orders = new List<Order> { new Order { Id = 3, Amount = 50 }, new Order { Id = 4, Amount = 65 } },
                RelationshipManagers = new List<Employee> { new Employee { Id = 1, Name = "Employee 1" } }
            }
        };


        public ActionResult CreateRefToOrders([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            if (customer.Orders.SingleOrDefault(d => d.Id.Equals(relatedKey)) == null)
            {
                // Quick, lazy and dirty
                customer.Orders.Add(new Order { Id = relatedKey, Amount = random.Next(1, 9) * 10 });
            }

            return NoContent();
        }

        public ActionResult CreateRefToRelationshipManagersFromEnterpriseCustomer([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var customer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            if (customer.RelationshipManagers.SingleOrDefault(d => d.Id == relatedKey) == null)
            {
                customer.RelationshipManagers.Add(new Employee { Id = relatedKey, Name = $"Employee {relatedKey}" });
            }

            return NoContent();
        }


        public ActionResult DeleteRefToOrders([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            var relatedOrder = customer.Orders.SingleOrDefault(d => d.Id.Equals(relatedKey));
            if (relatedOrder != null)
            {
                customer.Orders.Remove(relatedOrder);
            }

            return NoContent();
        }

        public ActionResult DeleteRefToRelationshipManagersFromEnterpriseCustomer([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var customer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            var relatedEmployee = customer.RelationshipManagers.SingleOrDefault(d => d.Id == relatedKey);
            if (relatedEmployee != null)
            {
                customer.RelationshipManagers.Remove(relatedEmployee);
            }

            return NoContent();
        }
    }
}
