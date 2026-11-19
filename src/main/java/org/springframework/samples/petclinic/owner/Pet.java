package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.NamedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "pets")
// @ nullable_by_default
public class Pet extends NamedEntity {

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	// @ spec_public
	private LocalDate birthDate;

	@ManyToOne
	@JoinColumn(name = "type_id")
	// @ spec_public
	private PetType type;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "pet_id")
	@OrderBy("date ASC")
	// @ spec_public
	private final Set<Visit> visits = new LinkedHashSet<>();

	// @ public invariant visits != null;

	// @ public normal_behavior
	// @ assignable this.birthDate;
	// @ ensures this.birthDate == birthDate;
	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	// @ public normal_behavior
	// @ assignable \nothing;
	// @ ensures \result == this.birthDate;
	// @ pure
	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	// @ public normal_behavior
	// @ assignable \nothing;
	// @ ensures \result == this.type;
	// @ pure
	public PetType getType() {
		return this.type;
	}

	// @ public normal_behavior
	// @ assignable this.type;
	// @ ensures this.type == type;
	public void setType(PetType type) {
		this.type = type;
	}

	// @ public normal_behavior
	// @ assignable \nothing;
	// @ ensures \result == this.visits;
	// @ ensures \result != null;
	// @ pure
	public Collection<Visit> getVisits() {
		return this.visits;
	}

	// @ public behavior
	// @ requires visit != null;
	// @ assignable \everything;
	// @ ensures this.visits.contains(visit);
	public void addVisit(Visit visit) {
		getVisits().add(visit);
	}

}