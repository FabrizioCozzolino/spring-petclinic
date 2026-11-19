package org.springframework.samples.petclinic.benchmarks;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.DynamicHalt;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetValidator;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@DynamicHalt(model = "oscnn")
public class PetclinicDomainBenchmark {

	private Owner ownerWithManyPets;

	private Map<String, Pet> petIndexByName;

	private Pet validPet;

	private PetValidator petValidator;

	private Vet vetWithSpecialties;

	@Setup(Level.Trial)
	public void setUp() {
		ownerWithManyPets = new Owner();
		ownerWithManyPets.setFirstName("George");
		ownerWithManyPets.setLastName("Franklin");

		petIndexByName = new HashMap<>();
		PetType dogType = new PetType();
		dogType.setId(1);
		dogType.setName("dog");

		for (int i = 0; i < 50; i++) {
			Pet p = new Pet();
			p.setId(i + 1);
			p.setName("PetNumber" + i);
			p.setType(dogType);
			p.setBirthDate(LocalDate.of(2020, 1, 15));
			ownerWithManyPets.addPet(p);
			petIndexByName.put(p.getName().toLowerCase(), p);
		}

		validPet = new Pet();
		validPet.setName("Leo");
		validPet.setType(dogType);
		validPet.setBirthDate(LocalDate.of(2021, 6, 10));
		petValidator = new PetValidator();

		vetWithSpecialties = new Vet();
		vetWithSpecialties.setFirstName("James");
		vetWithSpecialties.setLastName("Carter");
		String[] specNames = { "radiology", "surgery", "dentistry", "cardiology", "dermatology", "oncology" };
		for (int i = 0; i < specNames.length; i++) {
			Specialty s = new Specialty();
			s.setId(i + 1);
			s.setName(specNames[i]);
			vetWithSpecialties.addSpecialty(s);
		}
	}

	@Benchmark
	public void benchmarkOwnerGetPetLinearSearch(Blackhole bh) {
		bh.consume(ownerWithManyPets.getPet("petnumber49", false));
	}

	@Benchmark
	public void benchmarkOwnerGetPetMapLookupBaseline(Blackhole bh) {
		bh.consume(petIndexByName.get("petnumber49"));
	}

	@Benchmark
	public void benchmarkPetValidatorValidate(Blackhole bh) {
		Errors errors = new BeanPropertyBindingResult(validPet, "pet");
		petValidator.validate(validPet, errors);
		bh.consume(errors.hasErrors());
	}

	@Benchmark
	public void benchmarkVetGetSpecialtiesSorting(Blackhole bh) {
		List<Specialty> sorted = vetWithSpecialties.getSpecialties();
		bh.consume(sorted);
	}

}