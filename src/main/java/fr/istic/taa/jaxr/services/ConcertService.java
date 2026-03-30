package fr.istic.taa.jaxr.services;

import java.time.LocalDateTime;
import java.util.List;

import fr.istic.taa.jaxr.dto.ConcertCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Organiser;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

public class ConcertService {

    private final ConcertDao concertDao = new ConcertDao();
    private final OrganisateurDao organisateurDao = new OrganisateurDao();

    public List<Concert> findAll() {
        return concertDao.findAll();
    }

    public Concert findOne(Long id) {
        Concert concert = concertDao.findOne(id);
        if (concert == null) {
            throw new NotFoundException("Concert non trouvé");
        }
        return concert;
    }

    public List<Concert> findUpcoming() {
        return concertDao.findUpcoming();
    }

    public List<Concert> findByLieu(String lieu) {
        return concertDao.findByLieu(lieu);
    }

    public List<Concert> findByOrganiseur(Long organiseurId) {
        return concertDao.findByOrganiseur(organiseurId);
    }

    public long create(ConcertCreateDTO dto) {
        Organiser organiser = organisateurDao.findOne(dto.getOrganisateurId());
        if (organiser == null) {
            throw new BadRequestException("Organisateur non trouvé");
        }

        if (dto.getCapacite() <= 0) {
            throw new BadRequestException("La capacité doit être positive");
        }

        if (!dto.getDateTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Le concert doit se tenir à une date future");
        }

        Concert concert = new Concert();
        concert.setLieu(dto.getLieu());
        concert.setDescription(dto.getDescription());
        concert.setCapaciteMax(dto.getCapacite());
        concert.setDate(dto.getDateTime());
        concert.setOrganiser(organiser);

        concertDao.save(concert);
        return concert.getConcertId();
    }

    public void delete(Long id) {
        Concert concert = concertDao.findOne(id);
        if (concert == null) {
            throw new NotFoundException("Concert non trouvé");
        }
        concertDao.delete(concert);
    }
}
