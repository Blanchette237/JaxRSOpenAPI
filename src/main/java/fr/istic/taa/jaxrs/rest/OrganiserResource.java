package fr.istic.taa.jaxrs.rest;

import java.net.URI;
import java.util.List;

import fr.istic.taa.jaxr.dto.OrganiserCreateDTO;
import fr.istic.taa.jaxr.services.OrganiserService;
import fr.istic.taa.jaxrs.domain.Organiser;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("organiseurs")
@Produces(MediaType.APPLICATION_JSON)
public class OrganiserResource {

    private final OrganiserService service = new OrganiserService();

    @GET
    public Response getAllOrganiseurs() {
        List<Organiser> organiseurs = service.findAll();
        return Response.ok(organiseurs).build();
    }

    @GET
    @Path("/{id}")
    public Response getOrganiseurById(@PathParam("id") Long id) {
        Organiser organiser = service.findOne(id);
        return Response.ok(organiser).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrganiseur(OrganiserCreateDTO dto) {
        long id = service.create(dto);
        return Response.created(URI.create("/organiseurs/" + id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrganiseur(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
