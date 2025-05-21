namespace Lesson4.Models
{
    public class Manager : Employee
    {
        public Employee? PersonalAssistant { get; set; }
        public List<Employee> DirectReports { get; set; } = [];
    }
}
