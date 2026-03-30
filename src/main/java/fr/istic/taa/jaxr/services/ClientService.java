package fr.istic.taa.jaxr.services;

import java.util.List;

import fr.istic.taa.jaxr.dto.ClientCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ClientDao;
import fr.istic.taa.jaxrs.domain.Client;
import jakarta.ws.rs.NotFoundException;

public class ClientService {

    private final ClientDao clientDao = new ClientDao();

    public List<Client> findAll() {
        return clientDao.findAll();
    }

    public Client findOne(Long id) {
        Client client = clientDao.findOne(id);
        if (client == null) {
            throw new NotFoundException("Client non trouvé");
        }
        return client;
    }

    public long create(ClientCreateDTO dto) {
        Client client = new Client();
        client.setName(dto.getName());
        client.setFirstname(dto.getFirstname());
        client.setEmail(dto.getEmail());
        client.setPassword(dto.getPassword());
        clientDao.save(client);
        return client.getUserId();
    }

    public void delete(Long id) {
        Client client = clientDao.findOne(id);
        if (client == null) {
            throw new NotFoundException("Client non trouvé");
        }
        clientDao.delete(client);
    }
}
