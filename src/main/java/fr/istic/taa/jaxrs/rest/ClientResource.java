package fr.istic.taa.jaxrs.rest;

import java.net.URI;
import java.util.List;

import fr.istic.taa.jaxr.dto.ClientCreateDTO;
import fr.istic.taa.jaxr.services.ClientService;
import fr.istic.taa.jaxrs.domain.Client;
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

@Path("clients")
@Produces(MediaType.APPLICATION_JSON)
public class ClientResource {

    private final ClientService service = new ClientService();

    @GET
    public Response getAllClients() {
        List<Client> clients = service.findAll();
        return Response.ok(clients).build();
    }

    @GET
    @Path("/{id}")
    public Response getClientById(@PathParam("id") Long id) {
        Client client = service.findOne(id);
        return Response.ok(client).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createClient(ClientCreateDTO dto) {
        long id = service.create(dto);
        return Response.created(URI.create("/clients/" + id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteClient(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    // Endpoint métier : exploite la relation bidirectionnelle Client ↔ Ticket (mappedBy = "client")
    @GET
    @Path("/{id}/tickets")
    public Response getTicketsByClient(@PathParam("id") Long id) {
        List<Ticket> tickets = service.findTicketsByClient(id);
        return Response.ok(tickets).build();
    }

    // Endpoint métier : nombre de tickets actifs d'un client
    @GET
    @Path("/{id}/tickets/count-active")
    public Response countActiveTickets(@PathParam("id") Long id) {
        long count = service.countActiveTickets(id);
        return Response.ok(count).build();
    }
}
