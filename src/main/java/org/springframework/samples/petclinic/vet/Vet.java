package org.springframework.samples.petclinic.vet;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Person;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlElement;

@Entity
@Table(name = "vets")
// @ nullable_by_default
public class Vet extends Person {

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"),
			inverseJoinColumns = @JoinColumn(name = "specialty_id"))
	// @ spec_public
	private Set<Specialty> specialties;

	// @ protected normal_behavior
	// @ assignable this.specialties;
	// @ ensures this.specialties != null;
	// @ ensures \result == this.specialties;
	protected Set<Specialty> getSpecialtiesInternal() {
		if (this.specialties == null) {
			this.specialties = new HashSet<>();
		}
		return this.specialties;
	}

	@XmlElement
	// @ skipesc
	public List<Specialty> getSpecialties() {
		return getSpecialtiesInternal().stream()
			.sorted(Comparator.comparing(specialty -> specialty.getName()))
			.collect(Collectors.toList());
	}

	// @ public normal_behavior
	// @ assignable this.specialties;
	// @ ensures this.specialties != null;
	// @ ensures \result >= 0;
	public int getNrOfSpecialties() {
		Set<Specialty> internal = getSpecialtiesInternal();
		// @ assume internal.size() >= 0;
		return internal.size();
	}

	// @ public behavior
	// @ requires specialty != null;
	// @ assignable \everything;
	// @ ensures this.specialties != null;
	// @ ensures this.specialties.contains(specialty);
	public void addSpecialty(Specialty specialty) {
		getSpecialtiesInternal().add(specialty);
	}

}