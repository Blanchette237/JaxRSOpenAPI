package fr.istic.taa.jaxrs.services;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.istic.taa.jaxr.dto.ClientCreateDTO;
import fr.istic.taa.jaxr.services.ClientService;
import fr.istic.taa.jaxrs.dao.generic.ClientDao;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.EntityManagerHelper;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.dao.generic.TicketDao;
import fr.istic.taa.jaxrs.domain.Client;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Organiser;
import fr.istic.taa.jaxrs.domain.Ticket;
import fr.istic.taa.jaxrs.domain.TicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.ws.rs.NotFoundException;

/**
 * Tests du ClientService.
 * Focus sur la relation bidirectionnelle Client ↔ Ticket (mappedBy = "client").
 */
public class ClientServiceTest {

    private final ClientService service = new ClientService();
    private final ClientDao clientDao = new ClientDao();
    private final ConcertDao concertDao = new ConcertDao();
    private final OrganisateurDao organisateurDao = new OrganisateurDao();
    private final TicketDao ticketDao = new TicketDao();
    private EntityManager em;

    @Before
    public void setUp() {
        em = EntityManagerHelper.getEntityManager();
        purge();
    }

    @After
    public void tearDown() {
        purge();
        em.clear();
    }

    private void purge() {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) tx.begin();
        em.createQuery("DELETE FROM Ticket t").executeUpdate();
        em.createQuery("DELETE FROM Concert c").executeUpdate();
        em.createQuery("DELETE FROM Client c").executeUpdate();
        em.createQuery("DELETE FROM Organiser o").executeUpdate();
        tx.commit();
    }

    // ── CRUD basique ──────────────────────────────────────────────────────────

    @Test
    public void testCreate_etFindOne() {
        // GIVEN
        ClientCreateDTO dto = new ClientCreateDTO();
        dto.setName("Dupont");
        dto.setFirstname("Jean");
        dto.setEmail("jean@test.com");
        dto.setPassword("motdepasse");

        // WHEN
        long id = service.create(dto);
        em.clear();
        Client trouve = service.findOne(id);

        // THEN
        assertNotNull(trouve);
        assertEquals("Dupont", trouve.getName());
        assertEquals("jean@test.com", trouve.getEmail());
    }

    @Test(expected = NotFoundException.class)
    public void testFindOne_idInexistant() {
        service.findOne(99999L);
    }

    @Test
    public void testFindAll() {
        // GIVEN : 3 clients
        for (int i = 1; i <= 3; i++) {
            Client c = new Client();
            c.setName("Client" + i);
            c.setFirstname("Prenom");
            c.setEmail("c" + i + "@test.com");
            c.setPassword("pwd");
            clientDao.save(c);
        }

        // WHEN / THEN
        assertEquals(3, service.findAll().size());
    }

    @Test(expected = NotFoundException.class)
    public void testDelete_idInexistant() {
        service.delete(99999L);
    }

    // ── Relation Client ↔ Ticket (bidirectionnelle, mappedBy = "client") ──────

    /**
     * findTicketsByClient exploite le lien Client → Ticket.
     * Vérifie que la méthode retourne UNIQUEMENT les tickets du client demandé.
     */
    @Test
    public void testFindTicketsByClient_retourneSeulementSesTicekts() {
        // GIVEN : setup de données réaliste
        Organiser org = new Organiser();
        org.setName("Org"); org.setFirstname("F"); org.setEmail("o@t.com"); org.setPassword("p");
        organisateurDao.save(org);

        Concert c = new Concert();
        c.setLieu("Salle"); c.setCapaciteMax(100);
        c.setDate(LocalDateTime.now().plusDays(10));
        c.setOrganiser(org);
        concertDao.save(c);

        Client alice = new Client();
        alice.setName("Alice"); alice.setFirstname("A"); alice.setEmail("a@t.com"); alice.setPassword("p");
        clientDao.save(alice);

        Client bob = new Client();
        bob.setName("Bob"); bob.setFirstname("B"); bob.setEmail("b@t.com"); bob.setPassword("p");
        clientDao.save(bob);

        // Alice a 2 tickets, Bob en a 1
        creerTicket(alice, c, 1, TicketStatus.ACTIVE);
        creerTicket(alice, c, 2, TicketStatus.ANNULE);
        creerTicket(bob,   c, 3, TicketStatus.ACTIVE);

        // WHEN
        em.clear();
        List<Ticket> ticketsAlice = service.findTicketsByClient(alice.getUserId());

        // THEN
        assertEquals("Alice doit avoir 2 tickets", 2, ticketsAlice.size());
        ticketsAlice.forEach(t ->
            assertEquals("Tous les tickets appartiennent à Alice",
                         alice.getUserId(), t.getClient().getUserId())
        );
    }

    /**
     * countActiveTickets ne compte que les tickets ACTIVE (pas ANNULE ni UTILISE).
     */
    @Test
    public void testCountActiveTickets() {
        Organiser org = new Organiser();
        org.setName("O"); org.setFirstname("O"); org.setEmail("o@t.com"); org.setPassword("p");
        organisateurDao.save(org);

        Concert c = new Concert();
        c.setLieu("S"); c.setCapaciteMax(50);
        c.setDate(LocalDateTime.now().plusDays(5));
        c.setOrganiser(org);
        concertDao.save(c);

        Client cli = new Client();
        cli.setName("Cli"); cli.setFirstname("C"); cli.setEmail("cli@t.com"); cli.setPassword("p");
        clientDao.save(cli);

        creerTicket(cli, c, 1, TicketStatus.ACTIVE);
        creerTicket(cli, c, 2, TicketStatus.ACTIVE);
        creerTicket(cli, c, 3, TicketStatus.ANNULE);   // ne doit pas être compté
        creerTicket(cli, c, 4, TicketStatus.UTILISE);  // ne doit pas être compté

        // WHEN
        em.clear();
        long nbActifs = service.countActiveTickets(cli.getUserId());

        // THEN
        assertEquals("Seulement 2 tickets ACTIVE", 2L, nbActifs);
    }

    @Test
    public void testFindTicketsByClient_sansTickets_retourneListeVide() {
        Client cli = new Client();
        cli.setName("SansTicket"); cli.setFirstname("S"); cli.setEmail("s@t.com"); cli.setPassword("p");
        clientDao.save(cli);

        List<Ticket> result = service.findTicketsByClient(cli.getUserId());
        assertTrue("Liste vide pour un client sans ticket", result.isEmpty());
    }

    private void creerTicket(Client client, Concert concert, int place, TicketStatus statut) {
        Ticket t = new Ticket();
        t.setClient(client);
        t.setConcert(concert);
        t.setNumeroPlace(place);
        t.setStatus(statut);
        t.setPrixUnitaire(42.0);
        t.setDate_achat(LocalDateTime.now());
        ticketDao.save(t);
    }
}
