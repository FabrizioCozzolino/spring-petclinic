package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.HashMap;
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
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@DynamicHalt(model = "oscnn")
public class PetValidatorTests_JU5Benchmark {

	private static final String PET_NAME = "Buddy";

	private static final String PET_TYPE_NAME = "Dog";

	private static final LocalDate PET_BIRTH_DATE = LocalDate.of(1990, 1, 1);

	private PetValidator petValidator;

	private Pet pet;

	private PetType petType;

	private Errors errors;

	@Setup(Level.Invocation)
	public void setUp() {
		petValidator = new PetValidator();
		pet = new Pet();
		petType = new PetType();
		errors = new MapBindingResult(new HashMap<>(), "pet");
	}

	@Benchmark
	public void validate(Blackhole bh) {
		petType.setName(PET_TYPE_NAME);
		pet.setName(PET_NAME);
		pet.setType(petType);
		pet.setBirthDate(PET_BIRTH_DATE);
		petValidator.validate(pet, errors);
		bh.consume(errors.hasErrors());
	}

	@Benchmark
	public void validateWithLongPetName(Blackhole bh) {
		petType.setName(PET_TYPE_NAME);
		pet.setName("A".repeat(31));
		pet.setType(petType);
		pet.setBirthDate(PET_BIRTH_DATE);
		petValidator.validate(pet, errors);
		bh.consume(errors.hasFieldErrors("name"));
	}

}