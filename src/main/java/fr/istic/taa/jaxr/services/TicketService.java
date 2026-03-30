package fr.istic.taa.jaxr.services;

import java.time.LocalDateTime;
import java.util.List;

import fr.istic.taa.jaxr.dto.TicketCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ClientDao;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.TicketDao;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Ticket;
import fr.istic.taa.jaxrs.domain.TicketStatus;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;

public class TicketService {

    private final ConcertDao concertDao = new ConcertDao();
    private final TicketDao ticketDao = new TicketDao();
    private final ClientDao clientDao = new ClientDao();

    public Ticket findOne(Long id) {
        return ticketDao.findOne(id);
    }

    public List<Ticket> findAll() {
        return ticketDao.findAll();
    }

    public List<Ticket> findByConcert(Long concertId) {
        Concert concert = concertDao.findOne(concertId);
        if (concert == null) {
            throw new NotFoundException("Concert non trouvé");
        }
        return ticketDao.findByConcert(concert);
    }

    public void annuler(Long ticketId) {
        Ticket ticket = ticketDao.findOne(ticketId);
        if (ticket == null) {
            throw new NotFoundException("Ticket non trouvé");
        }
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new BadRequestException("Seul un ticket ACTIVE peut être annulé");
        }
        ticket.setStatus(TicketStatus.ANNULE);
        // Restituer la place au concert
        Concert concert = ticket.getConcert();
        concert.setCapacite(concert.getCapacite() + 1);
        ticketDao.update(ticket);
        concertDao.update(concert);
    }

    public long create(final TicketCreateDTO dto) throws ClientErrorException {
        var client = clientDao.findOne(dto.getUtilisateurId());
        if (client == null) {
            throw new BadRequestException("Utilisateur non trouvé");
        }

        var concert = concertDao.findOne(dto.getConcertId());
        if (concert == null) {
            throw new NotFoundException("Le concert n'existe pas");
        }

        if (!concert.getDate().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Le concert a déjà eu lieu");
        }

        long placesVendues = ticketDao.countByConcert(concert);
        if (placesVendues >= concert.getCapaciteMax()) {
            throw new ConflictException("Le concert est complet");
        }

        if (ticketDao.existsByConcertAndPlace(dto.getNumeroPlace(), concert)) {
            throw new ConflictException("La place " + dto.getNumeroPlace() + " n'est plus disponible");
        }

        Ticket ticket = new Ticket();
        ticket.setConcert(concert);
        ticket.setClient(client);
        ticket.setDate_achat(LocalDateTime.now());
        ticket.setStatus(TicketStatus.ACTIVE);
        ticket.setPrixUnitaire(calculPrixUnitaire(concert, dto.getNumeroPlace()));
        ticket.setNumeroPlace(dto.getNumeroPlace());
        concert.setCapacite(concert.getCapacite() - 1);

        ticketDao.save(ticket);
        concertDao.update(concert);
        return ticket.getTicketId();
    }

    private double calculPrixUnitaire(Concert concert, int numeroPlace) {
        // Prix de base : 42€ (logique métier simplifiée)
        return 42.0d;
    }
}
