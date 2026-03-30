package fr.istic.taa.jaxrs.rest;

import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Pet;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("concert")
@Produces({"application/json"})
public class ConcertRessource {
	
	@GET
	  @Path("/{ConcertId}")
	  public Concert getPetById(@PathParam("ConcertId") Long ConcertId)  {
		Concert c = new Concert();
	      return c;
	  }
	
	@POST
	  @Consumes("application/json")
	  public Response addConcert(
	      @Parameter(description = "Concert object that needs to be added to the store", required = true) Concert concert) {
	    // add concert
	    return Response.ok().entity("SUCCESS").build();
	  }

}
