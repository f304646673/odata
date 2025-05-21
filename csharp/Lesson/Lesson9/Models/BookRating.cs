namespace Lesson9.Models
{
    public class BookRating
    {
        public string? Id { get; set; }
        public int Rating { get; set; }
        public required string BookID { get; set; }
    }
}
