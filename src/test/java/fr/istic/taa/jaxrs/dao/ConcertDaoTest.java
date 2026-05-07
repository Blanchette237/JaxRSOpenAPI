package fr.istic.taa.jaxrs.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Organiser;

/**
 * Tests du ConcertDao.
 * Chaque méthode @Test correspond à une fonctionnalité précise du DAO.
 * Le setUp/tearDown (hérité) garantit une base vide avant et après chaque test.
 */
public class ConcertDaoTest extends AbstractDaoTest {

    private final ConcertDao dao = new ConcertDao();

    // ── CRUD de base ──────────────────────────────────────────────────────────

    @Test
    public void testSave_etFindOne() {
        // GIVEN : un organisateur et un concert
        Organiser org = creerOrganiseur("Dupont");
        Concert concert = creerConcert("Zénith Rennes", org, 10);

        // WHEN : on cherche le concert par son ID
        em.clear(); // vide le cache L1 pour forcer la lecture en base
        Concert trouve = dao.findOne(concert.getConcertId());

        // THEN
        assertNotNull("Le concert doit être trouvé en base", trouve);
        assertEquals("Zénith Rennes", trouve.getLieu());
        assertNotNull("L'organisateur doit être chargé", trouve.getOrganiser());
    }

    @Test
    public void testFindAll() {
        // GIVEN : 2 concerts créés
        Organiser org = creerOrganiseur("Martin");
        creerConcert("Salle A", org, 5);
        creerConcert("Salle B", org, 15);

        // WHEN
        List<Concert> tous = dao.findAll();

        // THEN
        assertEquals("Il doit y avoir exactement 2 concerts", 2, tous.size());
    }

    @Test
    public void testDelete() {
        // GIVEN
        Organiser org = creerOrganiseur("Leroy");
        Concert c = creerConcert("Olympia", org, 30);
        Long id = c.getConcertId();

        // WHEN
        dao.deleteById(id);
        em.clear();

        // THEN
        assertNull("Le concert supprimé ne doit plus être trouvé", dao.findOne(id));
    }

    @Test
    public void testUpdate() {
        // GIVEN
        Organiser org = creerOrganiseur("Blanc");
        Concert c = creerConcert("Ancien Lieu", org, 7);

        // WHEN : on modifie le lieu
        c.setLieu("Nouveau Lieu");
        dao.update(c);
        em.clear();

        // THEN
        Concert updated = dao.findOne(c.getConcertId());
        assertEquals("Nouveau Lieu", updated.getLieu());
    }

    // ── Requête JPQL : findByOrganiseur ───────────────────────────────────────
    //    "SELECT c FROM Concert c WHERE c.organiser.UserId = :id"
    //    → accède à une propriété de l'entité liée (Organiser) via JPQL

    @Test
    public void testFindByOrganiseur_JPQL() {
        // GIVEN : 2 organisateurs, chacun avec ses concerts
        Organiser orgA = creerOrganiseur("OrgA");
        Organiser orgB = creerOrganiseur("OrgB");
        creerConcert("Lieu1", orgA, 5);
        creerConcert("Lieu2", orgA, 10);
        creerConcert("Lieu3", orgB, 3);

        // WHEN
        em.clear();
        List<Concert> concertsOrgA = dao.findByOrganiseur(orgA.getUserId());

        // THEN : seuls les 2 concerts de OrgA doivent être retournés
        assertEquals("OrgA a 2 concerts", 2, concertsOrgA.size());
        concertsOrgA.forEach(c ->
            assertEquals("Tous les concerts doivent appartenir à OrgA",
                         orgA.getUserId(), c.getOrganiser().getUserId())
        );
    }

    // ── Named Query : Concert.findByLieu ──────────────────────────────────────
    //    @NamedQuery définie sur l'entité Concert :
    //    "SELECT c FROM Concert c WHERE c.lieu = :lieu"
    //    → requête nommée, définie à la compilation, réutilisable

    @Test
    public void testFindByLieu_NamedQuery() {
        // GIVEN
        Organiser org = creerOrganiseur("NomOrg");
        creerConcert("Zénith", org, 5);
        creerConcert("Zénith", org, 20);  // 2 concerts au même lieu
        creerConcert("Autre Salle", org, 8);

        // WHEN
        em.clear();
        List<Concert> auZenith = dao.findByLieu("Zénith");

        // THEN
        assertEquals("2 concerts au Zénith", 2, auZenith.size());
        auZenith.forEach(c -> assertEquals("Zénith", c.getLieu()));
    }

    @Test
    public void testFindByLieu_NamedQuery_aucunResultat() {
        // GIVEN : aucun concert créé
        // WHEN
        List<Concert> result = dao.findByLieu("Lieu Inexistant");
        // THEN
        assertTrue("Liste vide attendue", result.isEmpty());
    }

    // ── Criteria Query : findUpcoming ─────────────────────────────────────────
    //    Construit dynamiquement la requête avec CriteriaBuilder :
    //    WHERE date > now()  ORDER BY date ASC
    //    → utile quand les conditions varient à l'exécution

    @Test
    public void testFindUpcoming_CriteriaQuery() {
        // GIVEN : 1 concert dans le passé, 2 dans le futur
        Organiser org = creerOrganiseur("TstOrg");
        creerConcertPasse("Salle Passée", org);         // doit être EXCLU
        creerConcert("Salle Future 1", org, 3);          // doit être INCLUS
        creerConcert("Salle Future 2", org, 10);         // doit être INCLUS

        // WHEN
        em.clear();
        List<Concert> aVenir = dao.findUpcoming();

        // THEN : seuls les 2 concerts futurs
        assertEquals("2 concerts à venir", 2, aVenir.size());
        // Vérifier le tri par date croissante
        assertTrue("Trié par date : le 1er doit précéder le 2e",
            aVenir.get(0).getDate().isBefore(aVenir.get(1).getDate()));
    }

    @Test
    public void testFindUpcoming_baseVide() {
        List<Concert> aVenir = dao.findUpcoming();
        assertTrue("Aucun concert à venir sur base vide", aVenir.isEmpty());
    }
}
