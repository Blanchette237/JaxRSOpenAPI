package fr.istic.taa.jaxr.services;

import java.util.List;

import fr.istic.taa.jaxr.dto.OrganiserCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Organiser;
import jakarta.ws.rs.NotFoundException;

public class OrganiserService {

    private final OrganisateurDao organisateurDao = new OrganisateurDao();

    public List<Organiser> findAll() {
        return organisateurDao.findAll();
    }

    public Organiser findOne(Long id) {
        Organiser organiser = organisateurDao.findOne(id);
        if (organiser == null) {
            throw new NotFoundException("Organisateur non trouvé");
        }
        return organiser;
    }

    public List<Organiser> findAll(Long id) {
        return organisateurDao.findAll();
    }

    public long create(OrganiserCreateDTO dto) {
        Organiser organiser = new Organiser();
        organiser.setName(dto.getName());
        organiser.setFirstname(dto.getFirstname());
        organiser.setEmail(dto.getEmail());
        organiser.setPassword(dto.getPassword());
        organisateurDao.save(organiser);
        return organiser.getUserId();
    }

    public void delete(Long id) {
        Organiser organiser = organisateurDao.findOne(id);
        if (organiser == null) {
            throw new NotFoundException("Organisateur non trouvé");
        }
        organisateurDao.delete(organiser);
    }
}
