package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.util.Assert;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "owners")
// @ nullable_by_default
public class Owner extends Person {

	@Column
	@NotBlank
	@Size(max = 255)
	// @ spec_public
	private String address;

	@Column
	@NotBlank
	@Size(max = 80)
	// @ spec_public
	private String city;

	@Column
	@NotBlank
	@Pattern(regexp = "\\d{10}", message = "{telephone.invalid}")
	// @ spec_public
	private String telephone;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id")
	@OrderBy("name")
	// @ spec_public
	private final List<Pet> pets = new ArrayList<>();

	// @ public invariant pets != null;

	// @ public normal_behavior
	// @ assignable \nothing;
	// @ ensures \result == this.address;
	// @ pure
	public String getAddress() {
		return this.address;
	}

	// @ public normal_behavior
	// @ assignable this.address;
	// @ ensures this.address == address;
	public void setAddress(String address) {
		this.address = address;
	}

	// @ public normal_behavior
	// @ assignable \nothing;
	// @ ensures \result == this.city;
	// @ pure
	public String getCity() {
		return this.city;
	}

	// @ public normal_behavior
	// @ assignable this.city;
	// @ ensures this.city == city;
	public void setCity(String city) {
		this.city = city;
	}

	// @ public normal_behavior
	// @ assignable \nothing;
	// @ ensures \result == this.telephone;
	// @ pure
	public String getTelephone() {
		return this.telephone;
	}

	// @ public normal_behavior
	// @ assignable this.telephone;
	// @ ensures this.telephone == telephone;
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	// @ public normal_behavior
	// @ assignable \nothing;
	// @ ensures \result == this.pets;
	// @ ensures \result != null;
	// @ pure
	public List<Pet> getPets() {
		return this.pets;
	}

	// @ public normal_behavior
	// @ requires pet == null;
	// @ assignable \nothing;
	// @ ensures this.pets.size() == \old(this.pets.size());
	// @ also
	// @ public behavior
	// @ requires pet != null;
	// @ assignable \everything;
	// @ ensures this.pets.size() >= \old(this.pets.size());
	// @ skipesc
	public void addPet(Pet pet) {
		if (pet == null) {
			return;
		}
		if (getPets().contains(pet)) {
			return;
		}
		for (Pet existingPet : getPets()) {
			if (!existingPet.isNew() && !pet.isNew() && Objects.equals(existingPet.getId(), pet.getId())) {
				return;
			}
		}
		getPets().add(pet);
	}

	// @ pure
	public Pet getPet(String name) {
		return getPet(name, false);
	}

	// @ pure
	// @ skipesc
	public Pet getPet(Integer id) {
		for (Pet pet : getPets()) {
			if (!pet.isNew()) {
				Integer compId = pet.getId();
				if (Objects.equals(compId, id)) {
					return pet;
				}
			}
		}
		return null;
	}

	// @ pure
	// @ skipesc
	public Pet getPet(String name, boolean ignoreNew) {
		for (Pet pet : getPets()) {
			String compName = pet.getName();
			if (compName != null && compName.equalsIgnoreCase(name)) {
				if (!ignoreNew || !pet.isNew()) {
					return pet;
				}
			}
		}
		return null;
	}

	@Override
	// @ skipesc
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("new", this.isNew())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.append("address", this.address)
			.append("city", this.city)
			.append("telephone", this.telephone)
			.toString();
	}

	// @ public behavior
	// @ requires petId != null && visit != null;
	// @ assignable \everything;
	public void addVisit(Integer petId, Visit visit) {

		Assert.notNull(petId, "Pet identifier must not be null!");
		Assert.notNull(visit, "Visit must not be null!");

		Pet pet = getPet(petId);
		// @ assume pet != null;

		Assert.notNull(pet, "Invalid Pet identifier!");

		pet.addVisit(visit);
	}

}