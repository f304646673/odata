using Lesson9.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Formatter;
using Microsoft.AspNetCore.OData.Query;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson9.Controllers
{
    public class BooksController : ODataController
    {

        private static List<Book> books = [
            new Book { Id = "1", Title = "Book 1", Author = "Author 1", ForKids = true },
            new Book { Id = "2", Title = "Book 2", Author = "Author 2", ForKids = false },
            new Book { Id = "3", Title = "Book 3", Author = "Author 3", ForKids = true },
            new Book { Id = "4", Title = "Book 4", Author = "Author 4", ForKids = false }
        ];

        [EnableQuery]
        public IActionResult Get()
        {
            return Ok(books);
        }

        [HttpGet("odata/Books/mostRecent()")]
        public IActionResult MostRecent()
        {
            var maxBookId = books.Max(x => x.Id);
            return Ok(maxBookId);
        }

        [HttpGet("odata/ReturnAllForKidsBooks")]
        public IActionResult ReturnAllForKidsBooks()
        {
            var forKidsBooks = books.Where(m => m.ForKids == true);
            return Ok(forKidsBooks);
        }

        [HttpPost("odata/Books({key})/Rate")]
        public IActionResult Rate([FromODataUri] string key, ODataActionParameters parameters)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest();
            }

            int rating = (int)parameters["rating"];

            if (rating < 0)
            {
                return BadRequest();
            }

            return Ok(new BookRating() { BookID = key, Rating = rating });
        }

        [HttpPost("odata/incrementBookYear")]
        public IActionResult IncrementBookYear(ODataActionParameters parameters)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest();
            }

            int increment = (int)parameters["increment"];
            string bookId = (string)parameters["id"];

            var book = books.Where(m => m.Id == bookId).FirstOrDefault();

            if (book != null)
            {
                book.Year = book.Year + increment;
            }

            return Ok(book);
        }
    }
}
