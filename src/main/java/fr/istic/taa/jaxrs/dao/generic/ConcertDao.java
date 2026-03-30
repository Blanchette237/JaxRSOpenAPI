package fr.istic.taa.jaxrs.dao.generic;

import java.time.LocalDateTime;
import java.util.List;

import fr.istic.taa.jaxrs.domain.Concert;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class ConcertDao extends AbstractJpaDao<Long, Concert> {

    public ConcertDao() {
        super(Concert.class);
    }

    // Requête JPQL : concerts d'un organisateur
    public List<Concert> findByOrganiseur(Long organiseurId) {
        return entityManager
                .createQuery("SELECT c FROM Concert c WHERE c.organiser.UserId = :id", Concert.class)
                .setParameter("id", organiseurId)
                .getResultList();
    }

    // Requête nommée (définie dans Concert.java) : concerts par lieu
    public List<Concert> findByLieu(String lieu) {
        return entityManager
                .createNamedQuery("Concert.findByLieu", Concert.class)
                .setParameter("lieu", lieu)
                .getResultList();
    }

    // Criteria query : concerts à venir (date future), triés par date
    public List<Concert> findUpcoming() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Concert> cq = cb.createQuery(Concert.class);
        Root<Concert> root = cq.from(Concert.class);
        cq.where(cb.greaterThan(root.<LocalDateTime>get("date"), LocalDateTime.now()));
        cq.orderBy(cb.asc(root.get("date")));
        return entityManager.createQuery(cq).getResultList();
    }
}
