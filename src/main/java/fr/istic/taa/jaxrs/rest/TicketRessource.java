package fr.istic.taa.jaxrs.rest;

import java.net.URI;
import java.util.List;

import fr.istic.taa.jaxr.dto.TicketCreateDTO;
import fr.istic.taa.jaxr.services.TicketService;
import fr.istic.taa.jaxrs.domain.Ticket;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("tickets")
@Produces(MediaType.APPLICATION_JSON)
public class TicketRessource {

    private final TicketService service = new TicketService();

    @GET
    public Response getAllTickets() {
        List<Ticket> tickets = service.findAll();
        return Response.ok(tickets).build();
    }

    @GET
    @Path("/{id}")
    public Response getTicketById(@PathParam("id") Long id) {
        Ticket ticket = service.findOne(id);
        return Response.ok(ticket).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTicket(TicketCreateDTO dto) {
        long id = service.create(dto);
        return Response.created(URI.create("/tickets/" + id)).build();
    }

    // Endpoint métier : annuler un ticket (statut ACTIF → ANNULÉ, restitue la place)
    @DELETE
    @Path("/{id}/annuler")
    public Response annulerTicket(@PathParam("id") Long id) {
        service.annuler(id);
        return Response.noContent().build();
    }
}
