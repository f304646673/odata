using Lesson3.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OData.Deltas;
using Microsoft.AspNetCore.OData.Routing.Controllers;

namespace Lesson3.Controllers
{
    public class CompanyController : ODataController
    {
        private static Company company;

        static CompanyController()
        {
            company = new HoldingCompany
            {
                Id = 13,
                Name = "Company LLC",
                NumberOfSubsidiaries = 7
            };
        }

        public ActionResult<Company> Get()
        {
            return company;
        }

        public ActionResult<HoldingCompany> GetFromHoldingCompany()
        {
            if (!(company is HoldingCompany holdingCompany))
            {
                return NotFound();
            }

            return holdingCompany;
        }

        public ActionResult Put([FromBody] Company updated)
        {
            if (updated == null)
            {
                return BadRequest("The request body is null or invalid.");
            }
            company.Name = updated.Name;

            return Ok();
        }

        public ActionResult PutFromHoldingCompany([FromBody] HoldingCompany updated)
        {
            if (updated == null)
            {
                return BadRequest("The request body is null or invalid.");
            }

            if (!(company is HoldingCompany holdingCompany))
            {
                return NotFound();
            }

            holdingCompany.Name = updated.Name;
            holdingCompany.NumberOfSubsidiaries = updated.NumberOfSubsidiaries;

            return Ok();
        }

        public ActionResult Patch([FromBody] Delta<Company> delta)
        {
            if (delta == null)
            {
                return BadRequest("The request body is null or invalid.");
            }
            delta.Patch(company);

            return Ok();
        }

        public ActionResult PatchFromHoldingCompany([FromBody] Delta<HoldingCompany> delta)
        {
            if (delta == null)
            {
                return BadRequest("The request body is null or invalid.");
            }

            if (!(company is HoldingCompany holdingCompany))
            {
                return NotFound();
            }

            delta.Patch(holdingCompany);

            return Ok();
        }
    }
}
