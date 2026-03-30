package fr.istic.taa.jaxrs.dao.generic;

import java.util.List;

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
        return entityManager.createQuery("select count(t) from Ticket t where t.concert = :concert", Long.class)
                .setParameter("concert", concert)
                .getSingleResult();
    }
}