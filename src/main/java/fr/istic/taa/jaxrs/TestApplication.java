package fr.istic.taa.jaxrs;

import java.util.HashSet;
import java.util.Set;

import fr.istic.taa.jaxrs.rest.ClientResource;
import fr.istic.taa.jaxrs.rest.ConcertRessource;
import fr.istic.taa.jaxrs.rest.CorsFilter;
import fr.istic.taa.jaxrs.rest.OrganiserResource;
import fr.istic.taa.jaxrs.rest.PetResource;
import fr.istic.taa.jaxrs.rest.TicketRessource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class TestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> clazzes = new HashSet<>();

        // Documentation OpenAPI (Swagger UI accessible via /openapi.json)
        clazzes.add(OpenApiResource.class);

        // Filtre CORS (doit être enregistré pour que les frontends Angular puissent appeler l'API)
        clazzes.add(CorsFilter.class);

        // Ressources REST
        clazzes.add(ConcertRessource.class);
        clazzes.add(TicketRessource.class);
        clazzes.add(ClientResource.class);
        clazzes.add(OrganiserResource.class);
        clazzes.add(PetResource.class);

        return clazzes;
    }
}
