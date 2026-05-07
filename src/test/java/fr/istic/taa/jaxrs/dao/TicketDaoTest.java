package fr.istic.taa.jaxrs.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fr.istic.taa.jaxrs.dao.generic.TicketDao;
import fr.istic.taa.jaxrs.domain.Client;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Organiser;
import fr.istic.taa.jaxrs.domain.Ticket;
import fr.istic.taa.jaxrs.domain.TicketStatus;

/**
 * Tests du TicketDao.
 * Couvre le lien bidirectionnel Concert ↔ Ticket ET Client ↔ Ticket.
 */
public class TicketDaoTest extends AbstractDaoTest {

    private final TicketDao dao = new TicketDao();

    // ── Lien Concert ↔ Ticket ─────────────────────────────────────────────────

    @Test
    public void testFindByConcert() {
        // GIVEN : 2 concerts, tickets répartis sur les 2
        Organiser org = creerOrganiseur("OrgTest");
        Concert c1 = creerConcert("Salle 1", org, 5);
        Concert c2 = creerConcert("Salle 2", org, 10);
        Client cli = creerClient("ClientTest");

        creerTicket(cli, c1, 1, TicketStatus.ACTIVE);
        creerTicket(cli, c1, 2, TicketStatus.ACTIVE);
        creerTicket(cli, c2, 1, TicketStatus.ACTIVE);

        // WHEN
        em.clear();
        List<Ticket> ticketsC1 = dao.findByConcert(c1);

        // THEN : seulement les 2 tickets du concert 1
        assertEquals("Concert 1 a 2 tickets", 2, ticketsC1.size());
        ticketsC1.forEach(t ->
            assertEquals("Chaque ticket appartient au concert 1",
                         c1.getConcertId(), t.getConcert().getConcertId())
        );
    }

    @Test
    public void testCountByConcert() {
        // GIVEN
        Organiser org = creerOrganiseur("Org");
        Concert c = creerConcert("Scène", org, 5);
        Client cli = creerClient("Cli");
        creerTicket(cli, c, 1, TicketStatus.ACTIVE);
        creerTicket(cli, c, 2, TicketStatus.ACTIVE);
        creerTicket(cli, c, 3, TicketStatus.ANNULE);

        // WHEN
        em.clear();
        long count = dao.countByConcert(c);

        // THEN : TOUS les tickets (peu importe le statut)
        assertEquals(3L, count);
    }

    @Test
    public void testExistsByConcertAndPlace_placeOccupee() {
        // GIVEN : place 5 déjà prise
        Organiser org = creerOrganiseur("Org");
        Concert c = creerConcert("Salle", org, 5);
        Client cli = creerClient("Cli");
        creerTicket(cli, c, 5, TicketStatus.ACTIVE);

        // WHEN
        em.clear();
        boolean occupe = dao.existsByConcertAndPlace(5, c);

        // THEN
        assertTrue("La place 5 est occupée", occupe);
    }

    @Test
    public void testExistsByConcertAndPlace_placeLibre() {
        // GIVEN : aucun ticket pour la place 42
        Organiser org = creerOrganiseur("Org");
        Concert c = creerConcert("Salle", org, 5);

        // WHEN
        boolean libre = dao.existsByConcertAndPlace(42, c);

        // THEN
        assertFalse("La place 42 est libre", libre);
    }

    // ── Lien Client ↔ Ticket (relation bidirectionnelle mappedBy = "client") ──

    @Test
    public void testFindByClient() {
        // GIVEN : 2 clients, tickets répartis
        Organiser org = creerOrganiseur("Org");
        Concert c = creerConcert("Salle", org, 5);
        Client alice = creerClient("Alice");
        Client bob   = creerClient("Bob");

        creerTicket(alice, c, 1, TicketStatus.ACTIVE);
        creerTicket(alice, c, 2, TicketStatus.ANNULE);
        creerTicket(bob,   c, 3, TicketStatus.ACTIVE);

        // WHEN : on récupère les tickets d'Alice
        em.clear();
        List<Ticket> ticketsAlice = dao.findByClient(alice);

        // THEN : Alice a 2 tickets (ACTIVE + ANNULE)
        assertEquals("Alice a 2 tickets", 2, ticketsAlice.size());
        ticketsAlice.forEach(t ->
            assertEquals("Tickets appartiennent à Alice",
                         alice.getUserId(), t.getClient().getUserId())
        );
    }

    @Test
    public void testFindByClient_aucunTicket() {
        // GIVEN : client sans ticket
        Client cli = creerClient("SansTicket");

        // WHEN
        List<Ticket> tickets = dao.findByClient(cli);

        // THEN
        assertTrue("Aucun ticket pour ce client", tickets.isEmpty());
    }

    @Test
    public void testCountActiveByClient() {
        // GIVEN : client avec 2 ACTIVE et 1 ANNULE
        Organiser org = creerOrganiseur("Org");
        Concert c = creerConcert("Salle", org, 5);
        Client cli = creerClient("Cli");
        creerTicket(cli, c, 1, TicketStatus.ACTIVE);
        creerTicket(cli, c, 2, TicketStatus.ACTIVE);
        creerTicket(cli, c, 3, TicketStatus.ANNULE);

        // WHEN
        em.clear();
        long nbActifs = dao.countActiveByClient(cli);

        // THEN : seulement les 2 ACTIVE
        assertEquals("2 tickets actifs", 2L, nbActifs);
    }

    // ── Annulation d'un ticket (statut ANNULE) ────────────────────────────────

    @Test
    public void testUpdate_annulationTicket() {
        // GIVEN : ticket ACTIVE
        Organiser org = creerOrganiseur("Org");
        Concert c = creerConcert("Salle", org, 5);
        Client cli = creerClient("Cli");
        Ticket t = creerTicket(cli, c, 7, TicketStatus.ACTIVE);

        // WHEN : on passe le statut à ANNULE
        t.setStatus(TicketStatus.ANNULE);
        dao.update(t);
        em.clear();

        // THEN
        Ticket reloaded = dao.findOne(t.getTicketId());
        assertEquals(TicketStatus.ANNULE, reloaded.getStatus());
    }
}
