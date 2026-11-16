package org.springframework.samples.petclinic.owner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Isolated unit tests for {@link Owner} domain logic following FIRST principles.
 */
class OwnerUnitTests {

	private Owner owner;

	@BeforeEach
	void setUp() {
		this.owner = new Owner();
		this.owner.setId(1);
		this.owner.setFirstName("George");
		this.owner.setLastName("Franklin");
		this.owner.setAddress("110 W. Liberty St.");
		this.owner.setCity("Madison");
		this.owner.setTelephone("6085551023");
	}

	@Test
	void addPetShouldIgnoreNullAndDuplicates() {
		Pet pet = new Pet();
		pet.setName("Leo");

		this.owner.addPet(null);
		assertTrue(this.owner.getPets().isEmpty());

		this.owner.addPet(pet);
		this.owner.addPet(pet);
		assertEquals(1, this.owner.getPets().size());
	}

	@Test
	void addPetShouldIgnoreDifferentInstanceWithSamePersistedId() {
		Pet firstInstance = new Pet();
		firstInstance.setId(10);
		firstInstance.setName("Leo");

		Pet duplicateIdInstance = new Pet();
		duplicateIdInstance.setId(10);
		duplicateIdInstance.setName("Leo");

		this.owner.addPet(firstInstance);
		this.owner.addPet(duplicateIdInstance);

		assertEquals(1, this.owner.getPets().size());
	}

	@Test
	void getPetByNameShouldBeCaseInsensitiveAndRespectIgnoreNewFlag() {
		Pet newPet = new Pet();
		newPet.setName("Max");

		Pet savedPet = new Pet();
		savedPet.setId(5);
		savedPet.setName("Bella");

		this.owner.addPet(newPet);
		this.owner.addPet(savedPet);

		assertEquals(newPet, this.owner.getPet("MAX"));
		assertNull(this.owner.getPet("max", true));
		assertEquals(savedPet, this.owner.getPet("BELLA", true));
		assertNull(this.owner.getPet("Unknown"));
	}

	@Test
	void getPetByIdShouldReturnOnlyPersistedMatchingPet() {
		Pet unsavedPet = new Pet();
		unsavedPet.setName("Max");

		Pet savedPet = new Pet();
		savedPet.setId(7);
		savedPet.setName("Charlie");

		this.owner.addPet(unsavedPet);
		this.owner.addPet(savedPet);

		assertNull(this.owner.getPet((Integer) null));
		assertNull(this.owner.getPet(99));
		assertEquals(savedPet, this.owner.getPet(7));
	}

	@Test
	void addVisitShouldAttachVisitToExistingPet() {
		Pet pet = new Pet();
		pet.setId(3);
		pet.setName("Rosy");
		this.owner.addPet(pet);

		Visit visit = new Visit();
		visit.setDescription("Annual checkup");

		this.owner.addVisit(3, visit);

		assertEquals(1, pet.getVisits().size());
		assertEquals("Annual checkup", pet.getVisits().iterator().next().getDescription());
	}

	@Test
	void addVisitShouldThrowExceptionForInvalidArguments() {
		Visit visit = new Visit();
		visit.setDescription("Rabies shot");

		assertThrows(IllegalArgumentException.class, () -> this.owner.addVisit(null, visit));
		assertThrows(IllegalArgumentException.class, () -> this.owner.addVisit(1, null));
		assertThrows(IllegalArgumentException.class, () -> this.owner.addVisit(999, visit));
	}

	@Test
	void toStringShouldIncludeAllOwnerAttributes() {
		String output = this.owner.toString();
		assertNotNull(output);
		assertTrue(output.contains("George"));
		assertTrue(output.contains("Franklin"));
		assertTrue(output.contains("Madison"));
		assertTrue(output.contains("6085551023"));
	}

}