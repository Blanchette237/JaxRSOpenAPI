package fr.istic.taa.jaxrs.rest;

import java.net.URI;
import java.util.List;

import fr.istic.taa.jaxr.dto.ConcertCreateDTO;
import fr.istic.taa.jaxr.services.ConcertService;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Ticket;
import fr.istic.taa.jaxr.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("concerts")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Concerts", description = "Gestion des concerts : création, consultation et suppression")
public class ConcertRessource {

    private final ConcertService concertService = new ConcertService();
    private final TicketService ticketService = new TicketService();

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GET
    @Operation(
        summary = "Lister tous les concerts",
        description = "Retourne la liste complète de tous les concerts enregistrés.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Liste des concerts",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Concert.class)))
            )
        }
    )
    public Response getAllConcerts() {
        List<Concert> concerts = concertService.findAll();
        return Response.ok(concerts).build();
    }

    @GET
    @Path("/{id}")
    @Operation(
        summary = "Obtenir un concert par son identifiant",
        description = "Retourne le détail d'un concert à partir de son ID.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Concert trouvé",
                content = @Content(schema = @Schema(implementation = Concert.class))
            ),
            @ApiResponse(responseCode = "404", description = "Concert non trouvé")
        }
    )
    public Response getConcertById(
            @Parameter(description = "Identifiant du concert", required = true)
            @PathParam("id") Long id) {
        Concert concert = concertService.findOne(id);
        return Response.ok(concert).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Créer un nouveau concert",
        description = "Crée un concert à partir du DTO fourni. L'organisateur doit exister, la capacité doit être positive et la date doit être dans le futur.",
        requestBody = @RequestBody(
            description = "Données du concert à créer",
            required = true,
            content = @Content(schema = @Schema(implementation = ConcertCreateDTO.class))
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Concert créé, retourne l'URL de la ressource"),
            @ApiResponse(responseCode = "400", description = "Données invalides (organisateur inconnu, date passée, capacité nulle)")
        }
    )
    public Response createConcert(ConcertCreateDTO dto) {
        long id = concertService.create(dto);
        return Response.created(URI.create("/concerts/" + id)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Supprimer un concert",
        description = "Supprime un concert existant par son ID.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Concert supprimé"),
            @ApiResponse(responseCode = "404", description = "Concert non trouvé")
        }
    )
    public Response deleteConcert(
            @Parameter(description = "Identifiant du concert", required = true)
            @PathParam("id") Long id) {
        concertService.delete(id);
        return Response.noContent().build();
    }

    // ── Endpoints métiers ─────────────────────────────────────────────────────

    @GET
    @Path("/upcoming")
    @Operation(
        summary = "Concerts à venir",
        description = "Retourne les concerts dont la date est dans le futur, triés par date croissante. Utilise une Criteria Query JPA.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Liste des concerts à venir",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Concert.class)))
            )
        }
    )
    public Response getUpcomingConcerts() {
        List<Concert> concerts = concertService.findUpcoming();
        return Response.ok(concerts).build();
    }

    @GET
    @Path("/lieu/{lieu}")
    @Operation(
        summary = "Concerts par lieu",
        description = "Retourne les concerts se tenant dans le lieu spécifié. Utilise une Named Query JPA.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Liste des concerts dans ce lieu",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Concert.class)))
            )
        }
    )
    public Response getConcertsByLieu(
            @Parameter(description = "Nom du lieu", required = true)
            @PathParam("lieu") String lieu) {
        List<Concert> concerts = concertService.findByLieu(lieu);
        return Response.ok(concerts).build();
    }

    @GET
    @Path("/organiseur/{organiseurId}")
    @Operation(
        summary = "Concerts d'un organisateur",
        description = "Retourne tous les concerts créés par un organisateur donné. Utilise une requête JPQL.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Liste des concerts de l'organisateur",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Concert.class)))
            )
        }
    )
    public Response getConcertsByOrganiseur(
            @Parameter(description = "Identifiant de l'organisateur", required = true)
            @PathParam("organiseurId") Long organiseurId) {
        List<Concert> concerts = concertService.findByOrganiseur(organiseurId);
        return Response.ok(concerts).build();
    }

    @GET
    @Path("/{id}/tickets")
    @Operation(
        summary = "Tickets vendus pour un concert",
        description = "Retourne la liste de tous les tickets vendus pour le concert spécifié.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Liste des tickets",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Ticket.class)))
            ),
            @ApiResponse(responseCode = "404", description = "Concert non trouvé")
        }
    )
    public Response getTicketsByConcert(
            @Parameter(description = "Identifiant du concert", required = true)
            @PathParam("id") Long id) {
        List<Ticket> tickets = ticketService.findByConcert(id);
        return Response.ok(tickets).build();
    }
}
