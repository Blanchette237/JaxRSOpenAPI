package fr.istic.taa.jaxrs.dao.generic;

import java.util.List;

import fr.istic.taa.jaxrs.domain.Client;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Ticket;

public class TicketDao extends AbstractJpaDao<Long, Ticket> {
    public TicketDao() {
        super(Ticket.class);
    }

    public boolean existsByConcertAndPlace(int place, Concert concert) {
        Long count = entityManager
                .createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.concert = :concert AND t.NumeroPlace = :place", Long.class)
                .setParameter("place", place)
                .setParameter("concert", concert)
                .getSingleResult();
        return count > 0;
    }

    public List<Ticket> findByConcert(Concert concert) {
        return entityManager
                .createQuery("SELECT t FROM Ticket t WHERE t.concert = :concert", Ticket.class)
                .setParameter("concert", concert)
                .getResultList();
    }

    public long countByConcert(Concert concert) {
        return entityManager.createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.concert = :concert", Long.class)
                .setParameter("concert", concert)
                .getSingleResult();
    }

    // Lien Client ↔ Ticket : retourne tous les tickets d'un client donné
    public List<Ticket> findByClient(Client client) {
        return entityManager
                .createQuery("SELECT t FROM Ticket t WHERE t.client = :client", Ticket.class)
                .setParameter("client", client)
                .getResultList();
    }

    // Nombre de tickets actifs d'un client (exemple de méthode métier)
    public long countActiveByClient(Client client) {
        return entityManager
                .createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.client = :client AND t.status = fr.istic.taa.jaxrs.domain.TicketStatus.ACTIVE", Long.class)
                .setParameter("client", client)
                .getSingleResult();
    }
}