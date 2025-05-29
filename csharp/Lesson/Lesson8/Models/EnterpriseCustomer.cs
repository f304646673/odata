using System.Net;

namespace Lesson8.Models
{
    using System.Collections.Generic;
    public class EnterpriseCustomer : Customer
    {
        public decimal? CreditLimit { get; set; }
        public Address? RegisteredAddress { get; set; }
        public List<Address> ShippingAddresses { get; set; } = new List<Address>();
    }
}