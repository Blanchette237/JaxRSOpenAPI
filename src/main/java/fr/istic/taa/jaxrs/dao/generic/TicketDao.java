package fr.istic.taa.jaxrs.dao.generic;

import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Ticket;

public class TicketDao extends AbstractJpaDao<Long, Ticket> {
    public TicketDao() {
        super(Ticket.class);
    }

    public boolean existsByConcertAndPlace(String place, Concert concert) {
        return entityManager
                .createQuery("select count(t) > 0 from Ticket t where t.concert = :concert and t.numeroPlace = :place", Boolean.class)
                .setParameter("place", place)
                .setParameter("concert", concert)
                .getSingleResult();
    }

    public long countByConcert(Concert concert) {
        return entityManager.createQuery("select count(t) from Ticket t where t.concert = :concert", Long.class)
                .setParameter("concert", concert)
                .getSingleResult();
    }
}