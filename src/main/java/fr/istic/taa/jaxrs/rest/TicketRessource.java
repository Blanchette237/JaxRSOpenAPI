package fr.istic.taa.jaxrs.rest;

import fr.istic.taa.jaxr.services.TicketService;
import fr.istic.taa.jaxrs.domain.Ticket;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("pet")
@Produces({"application/json"})
public class TicketRessource {
	
	private final TicketService service = new TicketService();
	@GET
	  @Path("/{ticketId}")
	public Ticket getTicketById(@PathParam("ticketId") Long ticketId) {
		
		return service.findOne(ticketId);
	}

}
