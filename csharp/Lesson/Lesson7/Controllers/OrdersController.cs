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
        private static List<Order> orders = GetOrders();

        private static List<Order> GetOrders()
        {
            var customer1 = new Customer { Id = 1, Name = "Customer 1" };
            var customer2 = new EnterpriseCustomer
            {
                Id = 2,
                Name = "Customer 2",
                RelationshipManagers = [new() { Id = 1, Name = "Employee 1" }]
            };

            return
            [
                new Order { Id = 1, Amount = 80, Customer = customer1 },
                new Order { Id = 2, Amount = 40, Customer = customer1 },
                new Order { Id = 3, Amount = 50, Customer = customer2 },
                new Order { Id = 4, Amount = 65, Customer = customer2 },
                new Order { Id = 5, Amount = 35 }
            ];
        }

        public ActionResult CreateRefToCustomer([FromRoute] int key, [FromRoute] int relatedKey)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            // Quick, lazy and dirty
            order.Customer = new Customer { Id = relatedKey, Name = $"Customer {relatedKey}" };

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


        public ActionResult CreateRefToCustomer([FromRoute] int key, [FromBody] Uri link)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            int relatedKey;
            // The code for TryParseRelatedKey is shown a little further below
            if (!TryParseRelatedKey(link, out relatedKey))
            {
                return BadRequest();
            }

            // Quick, lazy and dirty
            order.Customer = new Customer { Id = relatedKey, Name = $"Customer {relatedKey}" };

            return NoContent();
        }

        public ActionResult CreateRef([FromRoute] int key, [FromRoute] string navigationProperty, [FromBody] Uri link)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));

            if (order == null)
            {
                return NotFound();
            }

            int relatedKey;
            // The code for TryParseRelatedKey is shown a little further below
            if (!TryParseRelatedKey(link, out relatedKey))
            {
                return BadRequest();
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

        public ActionResult GetRefToCustomer([FromRoute] int key)
        {
            var order = orders.SingleOrDefault(d => d.Id.Equals(key));
            if (order == null)
            {
                return NotFound();
            }
            return Ok(order.Customer);
        }
    }
}
