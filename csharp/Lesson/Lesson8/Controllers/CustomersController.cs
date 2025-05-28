using Lesson8.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Deltas;
using Microsoft.AspNetCore.OData.Query;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson8.Controllers
{
    public class CustomersController : ODataController
    {
        private static List<Customer> customers = new List<Customer>
        {
            new Customer
            {
                Id = 1,
                Name = "Customer 1",
                ContactPhones = new List<string> { "761-116-1865" },
                BillingAddress = new Address { Street = "Street 1A" }
            },
            new Customer
            {
                Id = 2,
                Name = "Customer 2",
                ContactPhones = new List<string> { "835-791-8257" },
                BillingAddress = new PostalAddress { Street = "2A", PostalCode = "14030" }
            },
            new EnterpriseCustomer
            {
                Id = 3,
                Name = "Customer 3",
                ContactPhones = new List<string> { "157-575-6005" },
                BillingAddress = new Address { Street = "Street 3A" },
                CreditLimit = 4200,
                RegisteredAddress = new Address { Street = "Street 3B" },
                ShippingAddresses = new List<Address>
                {
                    new Address { Street = "Street 3C" }
                }
            },
            new EnterpriseCustomer
            {
                Id = 4,
                Name = "Customer 4",
                ContactPhones = new List<string> { "724-096-6719" },
                BillingAddress = new Address { Street = "Street 4A" },
                CreditLimit = 3700,
                RegisteredAddress = new PostalAddress { Street = "Street 4B", PostalCode = "22109" },
                ShippingAddresses = new List<Address>
                {
                    new Address { Street = "Street 4C" }
                }
            }
        };

        [EnableQuery]
        public ActionResult<string> GetName([FromRoute] int key)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            return customer.Name;
        }

        [EnableQuery]
        public ActionResult<decimal> GetCreditLimit([FromRoute] int key)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            return enterpriseCustomer.CreditLimit;
        }


        [EnableQuery]
        public ActionResult<IEnumerable<string>> GetContactPhones([FromRoute] int key)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            return customer.ContactPhones;
        }

        [EnableQuery]
        public ActionResult<IEnumerable<Address>> GetShippingAddressesFromEnterpriseCustomer([FromRoute] int key)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            return enterpriseCustomer.ShippingAddresses;
        }


        public ActionResult<Address> GetBillingAddress([FromRoute] int key)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            return customer.BillingAddress;
        }

        public ActionResult<PostalAddress> GetBillingAddressOfPostalAddress([FromRoute] int key)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (!(customer?.BillingAddress is PostalAddress billingAddress))
            {
                return NotFound();
            }

            return billingAddress;
        }

        public ActionResult<Address> GetRegisteredAddressFromEnterpriseCustomer([FromRoute] int key)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            return enterpriseCustomer.RegisteredAddress;
        }

        public ActionResult<PostalAddress> GetRegisteredAddressOfPostalAddressFromEnterpriseCustomer([FromRoute] int key)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (!(enterpriseCustomer?.RegisteredAddress is PostalAddress registeredAddress))
            {
                return NotFound();
            }

            return registeredAddress;
        }

        public ActionResult<decimal> GetCreditLimitFromEnterpriseCustomer([FromRoute] int key)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            return enterpriseCustomer.CreditLimit;
        }


        public ActionResult PostToContactPhones([FromRoute] int key, [FromBody] string contactPhone)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            customer.ContactPhones.Add(contactPhone);

            return Accepted();
        }

        public ActionResult PostToShippingAddressesFromEnterpriseCustomer([FromRoute] int key, [FromBody] Address address)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            enterpriseCustomer.ShippingAddresses.Add(address);

            return Accepted();
        }

        public ActionResult PutToName([FromRoute] int key, [FromBody] string name)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            customer.Name = name;

            return Ok();
        }

        public ActionResult PutToBillingAddress([FromRoute] int key, [FromBody] Address address)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            customer.BillingAddress = address;

            return Ok();
        }

        public ActionResult PutToContactPhones([FromRoute] int key, [FromBody] IEnumerable<string> contactPhones)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            // Ensure contactPhones is not null before assigning
            customer.ContactPhones = contactPhones?.ToList() ?? [];

            return Ok();
        }

        public ActionResult PutToRegisteredAddressFromEnterpriseCustomer([FromRoute] int key, [FromBody] Address address)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            enterpriseCustomer.RegisteredAddress = address;

            return Ok();
        }

        public ActionResult PutToRegisteredAddressOfPostalAddressFromEnterpriseCustomer([FromRoute] int key, [FromBody] PostalAddress registeredAddress)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            enterpriseCustomer.RegisteredAddress = registeredAddress;

            return Ok();
        }

        public ActionResult PatchToBillingAddress([FromRoute] int key, [FromBody] Delta<Address> delta)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            if (delta == null)
            {
                return BadRequest("Invalid request body.");
            }

            delta.Patch(customer.BillingAddress);

            return Ok();
        }

        public ActionResult PatchToBillingAddressOfPostalAddress([FromRoute] int key, [FromBody] Delta<PostalAddress> delta)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (!(customer?.BillingAddress is PostalAddress billingAddress))
            {
                return NotFound();
            }

            delta.Patch(billingAddress);

            return Ok();
        }

        public ActionResult PatchToRegisteredAddressFromEnterpriseCustomer([FromRoute] int key, [FromBody] Delta<Address> delta)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            if (delta == null)
            {
                return BadRequest("Invalid request body.");
            }

            delta.Patch(enterpriseCustomer.RegisteredAddress);

            return Ok();
        }

        public ActionResult PatchToRegisteredAddressOfPostalAddressFromEnterpriseCustomer([FromRoute] int key, [FromBody] Delta<PostalAddress> delta)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (!(enterpriseCustomer?.RegisteredAddress is PostalAddress registeredAddress))
            {
                return NotFound();
            }

            delta.Patch(registeredAddress);

            return Ok();
        }

        public ActionResult DeleteToBillingAddress([FromRoute] int key)
        {
            var customer = customers.SingleOrDefault(d => d.Id.Equals(key));

            if (customer == null)
            {
                return NotFound();
            }

            customer.BillingAddress = null!; // Use null-forgiving operator to suppress the warning

            return NoContent();
        }

        public ActionResult DeleteToRegisteredAddressFromEnterpriseCustomer([FromRoute] int key)
        {
            var enterpriseCustomer = customers.OfType<EnterpriseCustomer>().SingleOrDefault(d => d.Id.Equals(key));

            if (enterpriseCustomer == null)
            {
                return NotFound();
            }

            enterpriseCustomer.RegisteredAddress = null!; // Use null-forgiving operator to suppress the warning

            return NoContent();
        }


    }
}
