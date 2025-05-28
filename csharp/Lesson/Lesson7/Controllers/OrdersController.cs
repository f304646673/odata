using Lesson7.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Extensions;
using Microsoft.AspNetCore.OData.Routing.Controllers;
using Microsoft.OData.Edm;
using Microsoft.OData.UriParser;

namespace Lesson7.Controllers
{
    public class OrdersController : ODataController
    {
        private static List<Customer> customers = new List<Customer>
        {
            new Customer { Id = 1, Name = "Customer 1" },
            new Customer { Id = 2, Name = "Customer 2" },
        };

        private static List<Order> orders = new List<Order>
        {
            new Order { Id = 1, Amount = 80, Customer = customers.SingleOrDefault(c => c.Id == 1) },
            new ThirdpartyPaymentOrder { Id = 2, Amount = 40, Customer = customers.SingleOrDefault(c => c.Id == 1), PaidByCustomer = customers.SingleOrDefault(c => c.Id == 2) },
        };

        public ActionResult<Customer> GetRefToCustomer([FromRoute] int key)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));
            if (order == null)
            {
                return NotFound();
            }
            return Ok(order.Customer);
        }

        public ActionResult<Customer> GetRefToPaidByCustomerFromThirdpartyPaymentOrder([FromRoute] int key)
        {
            var order = orders.OfType<ThirdpartyPaymentOrder>().SingleOrDefault(d => d.Id.Equals(key));
            if (order == null)
            {
                return NotFound();
            }
            return Ok(order.PaidByCustomer);
        }

        public ActionResult CreateRefToCustomer([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));
            if (order == null)
            {
                return NotFound();
            }

            var customer = customers.SingleOrDefault(c => c.Id == relatedKey);
            if (customer == null)
            {
                customer = new Customer { Id = relatedKey, Name = $"Customer {relatedKey}" };
                customers.Add(customer);
                order.Customer = customer;

                return Ok(customer);
            }

            // Quick, lazy and dirty
            order.Customer = customer;

            return Ok(customer);
        }

        public ActionResult CreateRefToPaidByCustomerFromThirdpartyPaymentOrder([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var order = orders.OfType<ThirdpartyPaymentOrder>().SingleOrDefault(d => d.Id.Equals(key));
            if (order == null)
            {
                return NotFound();
            }

            var customer = customers.SingleOrDefault(c => c.Id == relatedKey);
            if (customer == null)
            {
                customer = new Customer { Id = relatedKey, Name = $"Customer {relatedKey}" };
                customers.Add(customer);
                order.PaidByCustomer = customer;

                return Ok(customer);
            }

            // Quick, lazy and dirty
            order.PaidByCustomer = customer;

            return Ok(customer);
        }

        public ActionResult DeleteRefToCustomer([FromRoute] int key)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            order.Customer = null!; // Use null-forgiving operator to explicitly allow null assignment.

            return NoContent();
        }

        public ActionResult DeleteRefToPaidByCustomerFromThirdpartyPaymentOrder([FromRoute] int key)
        {
            var order = orders.OfType<ThirdpartyPaymentOrder>().SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            order.PaidByCustomer = null!; // Use null-forgiving operator to explicitly allow null assignment.

            return NoContent();
        }

        public ActionResult CreateRef([FromRoute] int key, [FromRoute] int relatedKey, [FromRoute] string navigationProperty)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            switch (navigationProperty)
            {
                case "Customer":
                    // Quick, lazy and dirty
                    order.Customer = new Customer { Id = relatedKey, Name = $"Customer {relatedKey}" };
                    break;
                default:
                    return BadRequest();
            }

            return NoContent();
        }


        public ActionResult CreateRefToCustomer([FromRoute] int key, [FromBody] Uri link)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            int relatedKey;
            if (!TryParseRelatedKey(link, out relatedKey))
            {
                return BadRequest();
            }

            // Quick, lazy and dirty
            order.Customer = new Customer { Id = relatedKey, Name = $"Customer {relatedKey}" };

            return Created(order.Customer);
        }

        public ActionResult CreateRefToPaidByCustomerFromThirdpartyPaymentOrder([FromRoute] int key, [FromBody] Uri link)
        {
            var order = orders.OfType<ThirdpartyPaymentOrder>().SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            int relatedKey;
            if (!TryParseRelatedKey(link, out relatedKey))
            {
                return BadRequest();
            }

            var customer = customers.SingleOrDefault(c => c.Id == relatedKey);
            if (customer == null)
            {
                customer = new Customer { Id = relatedKey, Name = $"Customer {relatedKey}" };
                customers.Add(customer);
            }


            // Quick, lazy and dirty
            order.PaidByCustomer = customer;

            return Created(order.PaidByCustomer);
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
