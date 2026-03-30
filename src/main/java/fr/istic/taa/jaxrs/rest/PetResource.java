package fr.istic.taa.jaxrs.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Endpoint de vérification de l'état du serveur.
 */
@Path("health")
@Produces(MediaType.TEXT_PLAIN)
public class PetResource {

    @GET
    public Response health() {
        return Response.ok("Service opérationnel").build();
    }
}
