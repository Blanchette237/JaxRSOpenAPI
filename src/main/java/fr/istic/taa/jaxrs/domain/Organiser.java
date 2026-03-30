package fr.istic.taa.jaxrs.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "oganiser")

public class Organiser extends User implements Serializable  {
	public Organiser(Long userId, String name, String firstname, String password, String email) {
		super(userId, name, firstname, password, email);
	}
	
	@OneToMany(mappedBy = "organiser", cascade = CascadeType.ALL)
    private List<Concert> concerts = new ArrayList<>();

	public List<Concert> getConcerts() {
		return concerts;
	}

	public void setConcerts(List<Concert> concerts) {
		this.concerts = concerts;
	}
	
	

}
