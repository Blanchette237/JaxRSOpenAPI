package fr.istic.taa.jaxrs.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "concert")
@NamedQuery(name = "Concert.findByLieu", query = "SELECT c FROM Concert c WHERE c.lieu = :lieu")
public class Concert implements Serializable{
	@Id
	@GeneratedValue
	private Long ConcertId;
	private String lieu;
	private String Description;
	private LocalDateTime date;
	
	private double capaciteMax;// nombre de places
	@ManyToOne
    @JoinColumn(name = "UserId", nullable = false)
    private Organiser organiser;
	
	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL)
    private List<Ticket> ticketsVendus = new ArrayList<>();
	
	public Long getConcertId() {
		return ConcertId;
	}
	public void setConcertId(Long concertId) {
		this.ConcertId = concertId;
	}
	public List<Ticket> getTicketsVendus() {
		return ticketsVendus;
	}
	public void setTicketsVendus(List<Ticket> ticketsVendus) {
		this.ticketsVendus = ticketsVendus;
	}
	public Organiser getOrganiser() {
		return organiser;
	}
	public void setOrganiser(Organiser organiser) {
		this.organiser = organiser;
	}
	public double getCapacite() {
		return capaciteMax;
	}
	
	public void setCapacite(double capaciteMax ) {
		this.capaciteMax = capaciteMax;
	}
	public String getLieu() {
		return lieu;
	}
	public void setLieu(String lieu) {
		this.lieu = lieu;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public double getCapaciteMax() {
		return capaciteMax;
	}
	public void setCapaciteMax(double capaciteMax) {
		this.capaciteMax = capaciteMax;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	


}
