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
    public class CustomersController : ODataController
    {

        private static List<Order> orders =
        [
            new Order { Id = 1, Amount = 80 },
            new Order { Id = 2, Amount = 40 },
            new Order { Id = 3, Amount = 50 },
            new Order { Id = 4, Amount = 65 }
        ];

        private static List<Employee> employees =
        [
            new Employee { Id = 1, Name = "Employee 1" },
            new Employee { Id = 2, Name = "Employee 2" }
        ];

        private static List<Customer> customers =
        [
            new Customer
            {
                Id = 1,
                Name = "Customer 1",
                Orders = new List<Order>
                {
                    orders.SingleOrDefault(d => d.Id == 1) ?? throw new InvalidOperationException("Order with Id 1 not found."),
                    orders.SingleOrDefault(d => d.Id == 2) ?? throw new InvalidOperationException("Order with Id 2 not found.")
                },
            },
            new EnterpriseCustomer
            {
                Id = 2,
                Name = "Customer 2",
                Orders = new List<Order>
                {
                    orders.SingleOrDefault(d => d.Id == 3) ?? throw new InvalidOperationException("Order with Id 3 not found."),
                    orders.SingleOrDefault(d => d.Id == 4) ?? throw new InvalidOperationException("Order with Id 4 not found.")
                },
                RelationshipManagers = new List<Employee> {
                    employees.SingleOrDefault(d => d.Id == 1) ?? throw new InvalidOperationException("Employee with Id 1 not found."),
                    employees.SingleOrDefault(d => d.Id == 2) ?? throw new InvalidOperationException("Employee with Id 2 not found.")
                }
            }
        ];

        [EnableQuery]
        public ActionResult<Order> GetRefToOrders([FromRoute] int key)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));
            if (customer == null)
            {
                return NotFound();
            }
            return Ok(customer.Orders);
        }

        [EnableQuery]
        public ActionResult<Order> GetRefToOrders([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));
            if (customer == null)
            {
                return NotFound();
            }
            var relatedOrder = customer.Orders.SingleOrDefault(d => d.Id.Equals(relatedKey));
            if (relatedOrder == null)
            {
                return NotFound();
            }
            return Ok(relatedOrder);
        }

        [EnableQuery]
        public ActionResult<Employee> GetRefToRelationshipManagersFromEnterpriseCustomer([FromRoute] int key) 
        {
            var customer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));
            if (customer == null)
            {
                return NotFound();
            }
            return Ok(customer.RelationshipManagers);
        }

        [EnableQuery]
        public ActionResult<Employee> GetRefToRelationshipManagersFromEnterpriseCustomer([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var customer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));
            if (customer == null)
            {
                return NotFound();
            }
            var relatedEmployee = customer.RelationshipManagers.SingleOrDefault(d => d.Id == relatedKey);
            if (relatedEmployee == null)
            {
                return NotFound();
            }
            return Ok(relatedEmployee);
        }


        public ActionResult CreateRefToOrders([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            var relatedOrder = customer.Orders.SingleOrDefault(d => d.Id.Equals(relatedKey));

            if (relatedOrder == null)
            {
                relatedOrder = new Order { Id = relatedKey, Amount = relatedKey * 10 };
                orders.Add(relatedOrder);
            }

            if (!customer.Orders.Any(d => d.Id.Equals(relatedOrder.Id)))
            {
                customer.Orders.Add(relatedOrder);
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

            var relatedEmployee = customer.RelationshipManagers.SingleOrDefault(d => d.Id == relatedKey);
            if (relatedEmployee == null)
            {
                relatedEmployee = new Employee { Id = relatedKey, Name = $"Employee {relatedKey}" };
                employees.Add(relatedEmployee);
            }

            if (!customer.RelationshipManagers.Any(d => d.Id == relatedKey))
            {
                // Add the employee to the relationship managers
                customer.RelationshipManagers.Add(relatedEmployee);
            }
            
            return NoContent();
        }

        public ActionResult DeleteRefToOrders([FromRoute] int key)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            customer.Orders.Clear();

            return NoContent();
        }

        public ActionResult DeleteRefToRelationshipManagersFromEnterpriseCustomer([FromRoute] int key)
        {
            var customer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            customer.RelationshipManagers.Clear();

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

        public ActionResult CreateRefToOrders([FromRoute] int key, [FromBody] Uri link)
        {
            if (!TryParseRelatedKey(link, out int relatedKey))
            {
                return BadRequest("Invalid related key.");
            }

            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));
            if (customer == null)
            {
                return NotFound();
            }

            var order = orders.SingleOrDefault(o => o.Id == relatedKey);
            if (order == null)
            {
                order = new Order { Id = relatedKey, Amount = relatedKey * 10 };
                orders.Add(order);
                customer.Orders.Add(order);

                return Ok(order);
            }

            if (!customer.Orders.Any(o => o.Id == relatedKey))
            {
                // Add the order to the customer's orders
                customer.Orders.Add(order);
            }

            // Quick, lazy and dirty
            customer.Orders.Add(order);

            return Ok(order);
        }

        public ActionResult CreateRefToRelationshipManagersFromEnterpriseCustomer([FromRoute] int key, [FromBody] Uri link)
        {
            if (!TryParseRelatedKey(link, out int relatedKey))
            {
                return BadRequest("Invalid related key.");
            }

            var customer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));
            if (customer == null)
            {
                return NotFound();
            }

            var employee = employees.SingleOrDefault(e => e.Id == relatedKey);
            if (employee == null)
            {
                employee = new Employee { Id = relatedKey, Name = $"Employee {relatedKey}" };
                employees.Add(employee);
            }

            if (!customer.RelationshipManagers.Any(e => e.Id == relatedKey))
            {
                customer.RelationshipManagers.Add(employee);
            }

            return Ok(employee);
        }

        private bool TryParseRelatedKey(Uri link, out int relatedKey)
        {
            relatedKey = 0;

            var model = Request.GetRouteServices().GetService(typeof(IEdmModel)) as IEdmModel;
            var serviceRoot = Request.CreateODataLink();

            if (link == null)
            {
                return false;
            }

            var uriParser = new ODataUriParser(model, new Uri(serviceRoot), link);
            // NOTE: ParsePath may throw exceptions for various reasons
            ODataPath parsedODataPath = uriParser.ParsePath(); // Renamed variable to avoid conflict

            KeySegment? keySegment = parsedODataPath.OfType<KeySegment>().LastOrDefault();

            if (keySegment == null || !int.TryParse(keySegment.Keys.First().Value?.ToString(), out relatedKey))
            {
                return false;
            }

            return true;
        }
    }
}
