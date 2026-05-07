package fr.istic.taa.jaxrs.dao;

import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;

import fr.istic.taa.jaxrs.dao.generic.EntityManagerHelper;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.dao.generic.ClientDao;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.TicketDao;
import fr.istic.taa.jaxrs.domain.Client;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Organiser;
import fr.istic.taa.jaxrs.domain.Ticket;
import fr.istic.taa.jaxrs.domain.TicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Classe de base partagée par tous les tests DAO/Service.
 *
 * Stratégie de test choisie : tests d'INTÉGRATION avec HSQL en mémoire.
 * Pourquoi pas des mocks (Mockito) ?
 *   - Les DAOs sont instanciés avec "new" dans les services → pas d'injection → difficile à mocker
 *   - Les tests d'intégration vérifient VRAIMENT que le JPQL / Criteria / Named Query fonctionnent
 *   - HSQL en mémoire = rapide, sans dépendance externe, recréé à chaque run
 *
 * Isolation entre tests :
 *   @Before  → prépare les données de test (via les DAOs)
 *   @After   → supprime TOUTES les données pour que chaque test parte d'une base vide
 */
public abstract class AbstractDaoTest {

    // EM accessible aux sous-classes pour les assertions directes si besoin
    protected EntityManager em;

    @Before
    public void setUp() {
        em = EntityManagerHelper.getEntityManager();
        // Nettoyer d'éventuelles données d'un test précédent
        purgeDatabase();
    }

    @After
    public void tearDown() {
        purgeDatabase();
        // Vider le cache L1 de Hibernate pour éviter les faux positifs
        em.clear();
    }

    // Supprime dans l'ordre inverse des FK : Ticket → Concert → Client, Organiser
    private void purgeDatabase() {
        em = EntityManagerHelper.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) tx.begin();
        em.createQuery("DELETE FROM Ticket t").executeUpdate();
        em.createQuery("DELETE FROM Concert c").executeUpdate();
        em.createQuery("DELETE FROM Client c").executeUpdate();
        em.createQuery("DELETE FROM Organiser o").executeUpdate();
        tx.commit();
    }

    // ── Helpers pour créer des entités de test ────────────────────────────────

    protected Organiser creerOrganiseur(String nom) {
        Organiser o = new Organiser();
        o.setName(nom);
        o.setFirstname("Prénom");
        o.setEmail(nom + "@test.com");
        o.setPassword("pwd");
        new OrganisateurDao().save(o);
        return o;
    }

    protected Client creerClient(String nom) {
        Client c = new Client();
        c.setName(nom);
        c.setFirstname("Prénom");
        c.setEmail(nom + "@test.com");
        c.setPassword("pwd");
        new ClientDao().save(c);
        return c;
    }

    /**
     * Crée un concert dans le futur (dans +nbJours jours).
     */
    protected Concert creerConcert(String lieu, Organiser organiseur, int nbJoursDansLeFutur) {
        Concert c = new Concert();
        c.setLieu(lieu);
        c.setDescription("Un concert de test");
        c.setCapaciteMax(100);
        c.setDate(LocalDateTime.now().plusDays(nbJoursDansLeFutur));
        c.setOrganiser(organiseur);
        new ConcertDao().save(c);
        return c;
    }

    /**
     * Crée un concert dans le PASSÉ (utile pour tester les filtres "à venir").
     */
    protected Concert creerConcertPasse(String lieu, Organiser organiseur) {
        Concert c = new Concert();
        c.setLieu(lieu);
        c.setDescription("Concert passé");
        c.setCapaciteMax(50);
        c.setDate(LocalDateTime.now().minusDays(5));
        c.setOrganiser(organiseur);
        new ConcertDao().save(c);
        return c;
    }

    protected Ticket creerTicket(Client client, Concert concert, int numeroPlace, TicketStatus statut) {
        Ticket t = new Ticket();
        t.setClient(client);
        t.setConcert(concert);
        t.setNumeroPlace(numeroPlace);
        t.setStatus(statut);
        t.setPrixUnitaire(42.0);
        t.setDate_achat(LocalDateTime.now());
        new TicketDao().save(t);
        return t;
    }
}
