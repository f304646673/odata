namespace Lesson4.Models
{
    public class Employee
    {
        public int Id { get; set; }
        public required string Name { get; set; }
        public Employee? Supervisor { get; set; }
        public List<Employee>? Peers { get; set; } = [];
    }
}
