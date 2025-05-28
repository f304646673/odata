using System.IO;

namespace Lesson7.Models
{
    public class ThirdpartyPaymentOrder : Order
    {
        public Customer? PaidByCustomer { get; set; } // 代付的客户
    }
}