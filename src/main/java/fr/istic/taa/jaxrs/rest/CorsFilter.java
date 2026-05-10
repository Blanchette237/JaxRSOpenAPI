package fr.istic.taa.jaxrs.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtre CORS — autorise les requêtes depuis les frontends Angular (port 4200 et 4201).
 * Gère aussi les pré-requêtes OPTIONS envoyées par le navigateur avant chaque POST/DELETE.
 */
@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Le navigateur envoie d'abord une requête OPTIONS ("preflight") pour vérifier les droits CORS.
        // On répond immédiatement 200 sans aller plus loin dans la chaîne.
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(Response.ok().build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin",  "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
        responseContext.getHeaders().add("Access-Control-Max-Age",       "3600");
    }
}
