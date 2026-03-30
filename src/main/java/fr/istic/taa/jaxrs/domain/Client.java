package fr.istic.taa.jaxrs.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "client" )
public class Client extends User implements Serializable {
	
	public Client(Long userId, String name, String firstname, String password, String email) {
		super(userId, name, firstname, password, email);
	}
 
	@OneToMany(mappedBy = "client")
    private List<Ticket> ticketsAchetes = new ArrayList<>();
	
	public List<Ticket> getTicketsAchetes() {
		return ticketsAchetes;
	}

	public void setTicketsAchetes(List<Ticket> ticketsAchetes) {
		this.ticketsAchetes = ticketsAchetes;
	}
	
	

}
