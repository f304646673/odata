namespace Lesson9.Models
{
    public class Book
    {
        public required string Id { get; set; }
        public required string Title { get; set; }
        public required string Author { get; set; }
        public required bool ForKids { get; set; }
        public int? Year { get; set; } = 0;
    }
}
