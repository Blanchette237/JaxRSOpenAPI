package fr.istic.taa.jaxrs.dao.generic;

import fr.istic.taa.jaxrs.domain.Client;

public class ClientDao extends AbstractJpaDao<Long, Client> {

	public ClientDao(Class<Client> clazz) {
		super(Client.class);
	}
	

}
