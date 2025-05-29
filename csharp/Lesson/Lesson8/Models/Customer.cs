using System.Net;

namespace Lesson8.Models
{
    using System.Collections.Generic;
    public class Customer
    {
        public int Id { get; set; }
        public string? Name { get; set; }
        public Address? BillingAddress { get; set; }
        public List<string> ContactPhones { get; set; } = [];
    }
}