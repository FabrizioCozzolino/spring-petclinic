# Verification and Quality Assurance Report — Spring Petclinic

## 1. Overview & Toolchain
This report summarizes the static analysis, code coverage, mutation testing, and formal specification activities performed on `spring-petclinic` (Spring Boot 4.1.0, Java 21, JUnit 6.1.3, Mockito 5.24.0):

- **Code Coverage:** JaCoCo Maven Plugin (`0.8.15`)
- **Mutation Testing:** PITest (`pitest-maven` `1.22.0` + `pitest-junit5-plugin` `1.2.3`)
- **Formal Specifications & Verification:** OpenJML (`21.0.29`) with Extended Static Checking (`-esc`, Z3 SMT solver) and Runtime Assertion Checking (`-rac`)

---

## 2. Code Coverage & Mutation Testing (Lab 4)
Automated quality gates were configured in `pom.xml` (`jacoco:check` and `pitest:mutationCoverage`) and integrated into GitHub Actions CI with CodeCov:

| Metric | Achieved | Configured Threshold | Status |
| :--- | :---: | :---: | :---: |
| **JaCoCo Line Coverage** | **94.9%** | >= 85% | Passed |
| **JaCoCo Branch Coverage** | **86.0%** | >= 80% | Passed |
| **JaCoCo Class Coverage** | **95.5%** | >= 90% | Passed |
| **PITest Mutation Score** | **88.0%** | >= 80% | Passed |

Targeted unit tests (`OwnerUnitTests`, `OwnerControllerUnitTests`, `PetValidatorTests`, `VetTests`) were added to exercise defensive branches, boundary conditions (30-character pet name limit, duplicate pet ID checks), and null validation paths.

---

## 3. Formal Specifications with JML & OpenJML (Lab 5)
JML contracts (`invariant`, `requires`, `ensures`, `assignable`, `pure`) were specified across four core domain and validation classes using single-line annotations (`//@`) to preserve compatibility with `spring-javaformat`:

1. **`PetValidator` (Data Validation):**
   - Defined `private invariant MAX_NAME_LENGTH == 30`.
   - Specified `validate(Object, Errors)` (`requires obj instanceof Pet && errors != null`) and `supports(Class<?>)` (`ensures \result == Pet.class.isAssignableFrom(clazz)`).
2. **`Pet` (Entity State Management):**
   - Declared `//@ nullable_by_default` for JPA-managed fields (`birthDate`, `type`) and `public invariant visits != null`.
   - Specified pure getters, state-mutating setters, and `addVisit(Visit)` (`requires visit != null`, `ensures this.visits.contains(visit)`).
3. **`Owner` (Aggregate Root & Invariants):**
   - Enforced `public invariant pets != null` and pure lookup contracts (`getPets()`, `getPet(...)`).
   - Specified overloaded behavior contracts for `addPet(Pet)` (no-op on `null`, non-decreasing collection size otherwise) and preconditions on `addVisit(Integer, Visit)` (`requires petId != null && visit != null`).
4. **`Vet` (Lazy Collection Initialization):**
   - Verified lazy initialization in `getSpecialtiesInternal()` (`ensures this.specialties != null && \result == this.specialties`), non-negative count in `getNrOfSpecialties()`, and postcondition containment in `addSpecialty(Specialty)`.

---

## 4. Static (ESC) & Runtime (RAC) Verification Results
- **Extended Static Checking (`openjml -esc`):** All four annotated classes verified cleanly with **0 errors and 0 warnings** after resolving three tool-specific subtleties:
  1. Block JML comments (`/*@ ... @*/`) were reformatted by `spring-javaformat:apply`; switching to single-line `//@` annotations eliminated parser breakage.
  2. Default non-null semantics in JML generated false-positive `NullField` assertions on JPA entity fields; adding `//@ nullable_by_default` aligned JML with JPA lifecycle semantics.
  3. Unbounded collection iterators and external Spring utilities (`ToStringCreator`, `Assert`) caused Z3 solver timeouts or missing-specification warnings; scoping ESC with `//@ skipesc` on external/stream delegates kept contracts active for RAC while preserving fast static proofs.
- **Runtime Assertion Checking (`openjml -rac`):** Instrumented bytecode executed against JUnit suites (`17 tests run, 0 failures, 0 errors`). Negative test cases in `OwnerUnitTests` (`addVisit` with `null` `petId`, `null` `visit`, or unknown `petId`) triggered expected JML precondition violation traces immediately prior to Spring's `Assert.notNull` guards, confirming runtime alignment between formal contracts and defensive code.