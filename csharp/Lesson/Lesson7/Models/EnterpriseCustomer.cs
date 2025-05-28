namespace Lesson7.Models
{
    public class EnterpriseCustomer : Customer
    {
        public List<Employee> RelationshipManagers { get; set; } = new List<Employee>();
    }
}