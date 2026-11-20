# Microbenchmarking Strategy with JMH, JU2JMH, and AMBER

## 1. Benchmarked Methods & Selection Rationale

Microbenchmarking was performed on **Spring Petclinic** (JDK 21.0.11, Spring Boot 4.1.0) targeting performance-critical domain operations and converted JUnit 5 test suites:

1. **`Owner.getPet(String name)` (`benchmarkOwnerGetPetLinearSearch`)**: Performs an $O(n)$ case-insensitive linear scan over an owner's pet list. Chosen because it is invoked repeatedly during form binding, visit registration, and duplicate validation.
2. **`Map<String, Pet>` Lookup (`benchmarkOwnerGetPetMapLookupBaseline`)**: An $O(1)$ hash-based baseline implemented to quantify the overhead of linear traversal vs. indexed lookup.
3. **`PetValidator.validate(Object, Errors)` (`benchmarkPetValidatorValidate`)**: Executes bean validation rules (required name, pet type, birth date). Selected as a hot path executed on every pet creation/update HTTP request.
4. **`Vet.getSpecialties()` (`benchmarkVetGetSpecialtiesSorting`)**: Copies the internal `Set<Specialty>` into a `List` and sorts elements alphabetically via `PropertyComparator` on every call, representing a classic defensive-copy + repeated-sorting bottleneck.
5. **JU2JMH Converted Benchmarks (`OwnerTests_JU5Benchmark`, `PetValidatorTests_JU5Benchmark`)**: Four benchmarks automatically generated via **JU2JMH** (`addPetAddsPersistedPet`, `addPetDoesNotAddDuplicatePet`, `validate`, `validateWithLongPetName`) to evaluate automated test-to-benchmark conversion against hand-crafted benchmarks.

---

## 2. Warm-up, Measurement & AMBER Configuration

Benchmarks are isolated under the Maven `benchmark` profile (`mvn clean package -DskipTests -Pbenchmark`) using `jmh-core` `1.37-extended` and executed locally in `AverageTime` mode (`ns/op`), 1 thread, 1 fork (`-f 1 -w 500ms -r 500ms`).

To prevent arbitrary fixed warm-up durations, we integrated **AMBER** (`@DynamicHalt(model = "oscnn")`) backed by the local Docker JPT service (`localhost:5001`). The **OS-CNN** (Online Statistical Control Chart Neural Network) classifier evaluates a sliding window of `100` iterations (`500 ms` each) and halts warm-up dynamically once JVM JIT compilation reaches steady-state.

---

## 3. Performance Results & Analysis

| Benchmark | Source | Score (ns/op) | Error (±99.9%) | Iterations |
| :--- | :--- | :---: | :---: | :---: |
| `PetclinicDomainBenchmark.benchmarkOwnerGetPetLinearSearch` | Hand-crafted | 655.301 | ± 23.571 | 100 |
| `PetclinicDomainBenchmark.benchmarkOwnerGetPetMapLookupBaseline` | Hand-crafted | 12.582 | ± 1.012 | 100 |
| `PetclinicDomainBenchmark.benchmarkPetValidatorValidate` | Hand-crafted | 75.255 | ± 2.450 | 100 |
| `PetclinicDomainBenchmark.benchmarkVetGetSpecialtiesSorting` | Hand-crafted | 295.415 | ± 33.626 | 100 |
| `OwnerTests_JU5Benchmark.addPetAddsPersistedPet` | JU2JMH | 0.525 | ± 0.015 | 100 |
| `OwnerTests_JU5Benchmark.addPetDoesNotAddDuplicatePet` | JU2JMH | 0.509 | ± 0.037 | 100 |
| `PetValidatorTests_JU5Benchmark.validate` | JU2JMH | 216.430 | ± 15.503 | 100 |
| `PetValidatorTests_JU5Benchmark.validateWithLongPetName` | JU2JMH | 631.323 | ± 88.219 | 100 |

### Key Findings
- **Bottleneck Identification (`Owner.getPet`)**: Linear search averages **655.301 ns/op**, whereas the `Map` lookup baseline executes in **12.582 ns/op**—a **52× speedup** (~98.1% latency reduction).
- **Repeated Sorting Overhead (`Vet.getSpecialties`)**: Defensive copying and sorting on every read costs **295.415 ns/op**, making it a strong candidate for lazy caching invalidated on mutation.
- **Hand-crafted vs. JU2JMH Benchmarks**: Hand-crafted `PetValidator` validation isolates pure validation logic (**75.255 ns/op**), whereas JU2JMH conversions (`216.430`–`631.323 ns/op`) include per-invocation fixture allocation (`Pet`, `BeanPropertyBindingResult`). Conversely, `OwnerTests_JU5Benchmark` (`~0.51 ns/op`) illustrates HotSpot C2 Dead Code Elimination (DCE) when generated benchmarks lack `Blackhole` consumption.
- **AMBER Steady-State Detection**: Across all 8 benchmarks, `oscnn` detected steady-state stability right at the `100`-iteration window boundary, avoiding redundant benchmark execution.

---

## 4. Performance Improvements & CI/CD Integration

- **Architectural Improvement**: Replacing linear pet scans in `Owner` with a normalized lowercase `Map<String, Pet>` index (or caching sorted specialties in `Vet`) eliminates $O(n)$ and $O(n \log n)$ read overhead.
- **CI/CD Strategy**: Because microbenchmarks are sensitive to noisy cloud runners, full AMBER runs are executed locally, while a scheduled/manual GitHub Actions workflow (`.github/workflows/benchmarks.yml`) is configured to detect regressions and archive JSON artifacts.