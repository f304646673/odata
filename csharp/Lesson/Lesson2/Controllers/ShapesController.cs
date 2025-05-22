using Lesson2.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Deltas;
using Microsoft.AspNetCore.OData.Query;
using Microsoft.AspNetCore.OData.Routing.Controllers;
using System.Reflection;

namespace Lesson2.Controllers
{

    public class ShapesController : ODataController
    {
        private static List<Shape> shapes =
            [
                new Shape { Id = 1, Area = 28 },
                new Circle { Id = 2, Radius = 3.5, Area = 38.5 },
                new Rectangle { Id = 3, Length = 8, Width = 5, Area = 40 }
            ];

        public ActionResult Post([FromBody] Shape shape)
        {
            shapes.Add(shape);

            return Created(shape);
        }

        public ActionResult PostFromCircle([FromBody] Circle circle)
        {
            shapes.Add(circle);

            return Created(circle);
        }

        public ActionResult PostFromRectangle([FromBody] Rectangle rectangle)
        {
            shapes.Add(rectangle);

            return Created(rectangle);
        }

        [EnableQuery]
        public ActionResult<IEnumerable<Shape>> Get()
        {
            return Ok(shapes);
        }

        [EnableQuery]
        public ActionResult<IEnumerable<Shape>> GetFromShape()
        {
            return Ok(shapes.OfType<Shape>().ToList());
        }

        [EnableQuery]
        public ActionResult<IEnumerable<Rectangle>> GetFromRectangle()
        {
            return Ok(shapes.OfType<Rectangle>().ToList());
        }

        [EnableQuery]
        public ActionResult<IEnumerable<Circle>> GetFromCircle()
        {
            return Ok(shapes.OfType<Circle>().ToList());
        }

        public ActionResult<Shape> Get([FromRoute] int key)
        {
            var shape = shapes.SingleOrDefault(d => d.Id.Equals(key));

            if (shape == null)
            {
                return NotFound();
            }

            return Ok(shape);
        }

        public ActionResult<Circle> GetCircle([FromRoute] int key)
        {
            var circle = shapes.OfType<Circle>().SingleOrDefault(d => d.Id.Equals(key));

            if (circle == null)
            {
                return NotFound();
            }

            return Ok(circle);
        }

        public ActionResult Put([FromRoute] int key, [FromBody] Shape shape)
        {
            var item = shapes.SingleOrDefault(d => d.Id.Equals(key));

            if (shape == null)
            {
                return BadRequest("Shape cannot be null.");
            }
            else if (item == null)
            {
                return NotFound();
            }
            else if (!item.GetType().Equals(shape.GetType()))
            {
                return BadRequest();
            }

            // Update properties using reflection
            foreach (var propertyInfo in shape.GetType().GetProperties(
                BindingFlags.Public | BindingFlags.Instance))
            {
                var itemPropertyInfo = item.GetType().GetProperty(
                    propertyInfo.Name,
                    BindingFlags.Public | BindingFlags.Instance);

                // Ensure itemPropertyInfo is not null before accessing CanWrite
                if (itemPropertyInfo != null && itemPropertyInfo.CanWrite)
                {
                    itemPropertyInfo.SetValue(item, propertyInfo.GetValue(shape));
                }
            }

            return Ok();
        }

        public ActionResult PutCircle([FromRoute] int key, [FromBody] Circle circle)
        {
            var item = shapes.OfType<Circle>().SingleOrDefault(d => d.Id.Equals(key));

            if (item == null)
            {
                return NotFound();
            }

            item.Id = circle.Id;
            item.Radius = circle.Radius;
            item.Area = circle.Area;

            return Ok();
        }

        public ActionResult Patch([FromRoute] int key, [FromBody] Delta<Shape> delta)
        {
            var shape = shapes.SingleOrDefault(d => d.Id.Equals(key));

            if (shape == null)
            {
                return NotFound();
            }
            else if (!shape.GetType().Equals(delta.StructuredType))
            {
                return BadRequest();
            }

            delta.Patch(shape);

            return Ok();
        }

        public ActionResult PatchCircle([FromRoute] int key, [FromBody] Delta<Circle> delta)
        {
            var shape = shapes.OfType<Circle>().SingleOrDefault(d => d.Id.Equals(key));

            if (shape == null)
            {
                return NotFound();
            }

            delta.Patch(shape);

            return Ok();
        }

        public ActionResult Patch([FromBody] DeltaSet<Shape> deltaSet)
        {
            foreach (Delta<Shape> delta in deltaSet)
            {
                if (delta.TryGetPropertyValue("Id", out object idAsObject))
                {
                    var shape = shapes.SingleOrDefault(d => d.Id.Equals(idAsObject));
                    if (shape == null)
                    {
                        return NotFound();
                    }
                    else if (!shape.GetType().Equals(delta.StructuredType))
                    {
                        return BadRequest();
                    }
                    delta.Patch(shape);
                }
            }

            return Ok();
        }

        public ActionResult DeleteShape([FromRoute] int key)
        {
            var shape = shapes.SingleOrDefault(d => d.Id.Equals(key));

            if (shape == null)
            {
                return NotFound();
            }

            shapes.Remove(shape);

            return NoContent();
        }

        public ActionResult DeleteCircle([FromRoute] int key)
        {
            var shape = shapes.OfType<Circle>().SingleOrDefault(d => d.Id.Equals(key));

            if (shape == null)
            {
                return NotFound();
            }

            shapes.Remove(shape);

            return NoContent();
        }
    }
}
