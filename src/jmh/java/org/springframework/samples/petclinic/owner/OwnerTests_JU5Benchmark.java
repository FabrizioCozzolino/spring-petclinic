package org.springframework.samples.petclinic.owner;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.DynamicHalt;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@DynamicHalt(model = "oscnn")
public class OwnerTests_JU5Benchmark {

	@Benchmark
	public void addPetAddsPersistedPet(Blackhole bh) {
		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setId(5);
		pet.setName("Buddy");
		owner.addPet(pet);
		bh.consume(owner.getPets().contains(pet));
		bh.consume(owner.getPets().size());
	}

	@Benchmark
	public void addPetDoesNotAddDuplicatePet(Blackhole bh) {
		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setId(5);
		pet.setName("Buddy");
		owner.addPet(pet);
		owner.addPet(pet);
		bh.consume(owner.getPets().size());
	}

}