package fr.istic.taa.jaxrs.services;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.istic.taa.jaxr.dto.ConcertCreateDTO;
import fr.istic.taa.jaxr.services.ConcertService;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.EntityManagerHelper;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Organiser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Tests du ConcertService — couche métier.
 *
 * Ce sont toujours des tests d'intégration (pas de mocks) car le service
 * instancie ses DAOs en interne. On teste ici la logique métier :
 *   - validation des paramètres (organisateur inconnu, date passée, capacité ≤ 0)
 *   - comportement correct en cas de succès
 *   - méthodes de recherche (findUpcoming, findByLieu, findByOrganiseur)
 */
public class ConcertServiceTest {

    private final ConcertService service = new ConcertService();
    private final OrganisateurDao organisateurDao = new OrganisateurDao();
    private final ConcertDao concertDao = new ConcertDao();
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

    // Helper pour créer un organiser directement en DB (sans passer par ConcertService)
    private Organiser creerOrganiseur(String nom) {
        Organiser o = new Organiser();
        o.setName(nom);
        o.setFirstname("Prenom");
        o.setEmail(nom + "@test.com");
        o.setPassword("pwd");
        organisateurDao.save(o);
        return o;
    }

    // ── Tests de validation métier ────────────────────────────────────────────

    /**
     * Cas : l'ID d'organisateur fourni n'existe pas en base.
     * Attendu : BadRequestException ("Organisateur non trouvé")
     */
    @Test(expected = BadRequestException.class)
    public void testCreate_organiseurInconnu_doitLeverBadRequest() {
        ConcertCreateDTO dto = new ConcertCreateDTO();
        dto.setOrganisateurId(9999L);   // ID inexistant
        dto.setLieu("Zénith");
        dto.setCapacite(500L);
        dto.setDateTime(LocalDateTime.now().plusDays(10));
        dto.setPopularite(3);

        service.create(dto); // doit lever BadRequestException
    }

    /**
     * Cas : la capacité est ≤ 0.
     * Attendu : BadRequestException
     */
    @Test(expected = BadRequestException.class)
    public void testCreate_capaciteNegative_doitLeverBadRequest() {
        Organiser org = creerOrganiseur("Dupont");

        ConcertCreateDTO dto = new ConcertCreateDTO();
        dto.setOrganisateurId(org.getUserId());
        dto.setLieu("Salle");
        dto.setCapacite(-10L);           // capacité invalide
        dto.setDateTime(LocalDateTime.now().plusDays(5));
        dto.setPopularite(2);

        service.create(dto);
    }

    /**
     * Cas : la date du concert est dans le passé.
     * Attendu : BadRequestException ("date future")
     */
    @Test(expected = BadRequestException.class)
    public void testCreate_datePassee_doitLeverBadRequest() {
        Organiser org = creerOrganiseur("Martin");

        ConcertCreateDTO dto = new ConcertCreateDTO();
        dto.setOrganisateurId(org.getUserId());
        dto.setLieu("Salle");
        dto.setCapacite(200L);
        dto.setDateTime(LocalDateTime.now().minusDays(1)); // date passée
        dto.setPopularite(4);

        service.create(dto);
    }

    /**
     * Cas nominal : toutes les données sont valides.
     * Attendu : concert créé en base, ID retourné > 0
     */
    @Test
    public void testCreate_nominal_concertCreeEnBase() {
        Organiser org = creerOrganiseur("Leroy");

        ConcertCreateDTO dto = new ConcertCreateDTO();
        dto.setOrganisateurId(org.getUserId());
        dto.setLieu("L'Olympia");
        dto.setCapacite(1000L);
        dto.setDescription("Grand concert");
        dto.setDateTime(LocalDateTime.now().plusDays(30));
        dto.setPopularite(5);

        // WHEN
        long id = service.create(dto);

        // THEN : ID positif et concert trouvable en base
        assertTrue("L'ID doit être positif", id > 0);
        em.clear();
        Concert cree = concertDao.findOne(id);
        assertNotNull("Concert doit être en base", cree);
        assertEquals("L'Olympia", cree.getLieu());
        assertEquals(org.getUserId(), cree.getOrganiser().getUserId());
    }

    // ── Tests des méthodes de recherche métier ────────────────────────────────

    /**
     * findUpcoming doit retourner uniquement les concerts dont la date est future,
     * triés par date croissante.
     */
    @Test
    public void testFindUpcoming_exclutLesConcentsPasses() {
        Organiser org = creerOrganiseur("Org");

        // Créer directement en base pour contrôler la date
        Concert passe = new Concert();
        passe.setLieu("Passé");
        passe.setCapaciteMax(50);
        passe.setDate(LocalDateTime.now().minusDays(3));
        passe.setOrganiser(org);
        concertDao.save(passe);

        Concert futur1 = new Concert();
        futur1.setLieu("Futur 1");
        futur1.setCapaciteMax(100);
        futur1.setDate(LocalDateTime.now().plusDays(5));
        futur1.setOrganiser(org);
        concertDao.save(futur1);

        Concert futur2 = new Concert();
        futur2.setLieu("Futur 2");
        futur2.setCapaciteMax(200);
        futur2.setDate(LocalDateTime.now().plusDays(15));
        futur2.setOrganiser(org);
        concertDao.save(futur2);

        // WHEN
        em.clear();
        List<Concert> aVenir = service.findUpcoming();

        // THEN
        assertEquals("2 concerts à venir seulement", 2, aVenir.size());
        assertTrue("Trié par date : futur1 avant futur2",
            aVenir.get(0).getDate().isBefore(aVenir.get(1).getDate()));
    }

    /**
     * findByLieu utilise la @NamedQuery "Concert.findByLieu".
     */
    @Test
    public void testFindByLieu_retourneSeulementCeConcert() {
        Organiser org = creerOrganiseur("Org");

        Concert c1 = new Concert();
        c1.setLieu("Zénith");
        c1.setCapaciteMax(100);
        c1.setDate(LocalDateTime.now().plusDays(5));
        c1.setOrganiser(org);
        concertDao.save(c1);

        Concert c2 = new Concert();
        c2.setLieu("Autre Salle");
        c2.setCapaciteMax(50);
        c2.setDate(LocalDateTime.now().plusDays(8));
        c2.setOrganiser(org);
        concertDao.save(c2);

        // WHEN
        em.clear();
        List<Concert> result = service.findByLieu("Zénith");

        // THEN
        assertEquals("1 seul concert au Zénith", 1, result.size());
        assertEquals("Zénith", result.get(0).getLieu());
    }

    /**
     * findOne sur un ID inexistant doit lever NotFoundException.
     */
    @Test(expected = NotFoundException.class)
    public void testFindOne_idInexistant_doitLeverNotFoundException() {
        service.findOne(99999L);
    }

    /**
     * delete sur un ID inexistant doit lever NotFoundException.
     */
    @Test(expected = NotFoundException.class)
    public void testDelete_idInexistant_doitLeverNotFoundException() {
        service.delete(99999L);
    }
}
