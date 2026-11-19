# Mutation Testing Report (PITest)

## 1. Mutation Testing Configuration

Mutation testing is configured in `pom.xml` via **`pitest-maven` v1.17.4** with **`pitest-junit5-plugin` v1.2.2** to evaluate the fault-detection capability of the JUnit 6 / Mockito 5 unit test suite:

- **Target Classes (`<targetClasses>`)**: `org.springframework.samples.petclinic.*`
- **Target Tests (`<targetTests>`)**: `org.springframework.samples.petclinic.*`
- **Exclusions**: `PetClinicApplication` (`<excludedClasses>`) and `*IntegrationTests` (`<excludedTestClasses>`) to avoid spinning up Docker/database containers per mutant.
- **Quality Gates**:
  - **Mutation Score Threshold (`<mutationThreshold>`)**: **80%**
  - **Line Coverage Threshold (`<coverageThreshold>`)**: **80%**
- **Execution & Output**: `timeoutConstant = 5000 ms`, `timeoutFactor = 1.5`, generating `HTML` (`target/pit-reports/index.html`) and `XML` reports using PITest's default mutators (`DEFAULTS` group).

---

## 2. Mutation Analysis

Executing `./mvnw pitest:mutationCoverage` analyzed **17 production classes**, examined **47 unit test methods** (393 test executions total, 2.25 tests/mutant), and generated **175 mutants**, passing all quality gates:

### Global Metrics
- **Mutation Score (Mutation Coverage)**: **88%** (**154 / 175** mutants killed)
- **Test Strength** *(killed mutants on covered lines)*: **91%** (**154 / 169**)
- **PITest Line Coverage**: **98%** (**311 / 317** lines)
- **Mutant Status Breakdown**: **154 Killed**, **15 Survived**, **6 No Coverage**

### Breakdown by Package

| Package | Classes | Line Coverage | Mutation Score | Test Strength |
| :--- | :---: | :---: | :---: | :---: |
| `org.springframework.samples.petclinic.model` | 3 | **100%** (18/18) | **100%** (8/8) | **100%** (8/8) |
| `org.springframework.samples.petclinic.vet` | 3 | **100%** (35/35) | **94%** (15/16) | **94%** (15/16) |
| `org.springframework.samples.petclinic.owner` | 8 | **98%** (245/250) | **87%** (125/144) | **90%** (125/139) |
| `org.springframework.samples.petclinic.system` | 3 | **93%** (13/14) | **86%** (6/7) | **100%** (6/6) |

### Breakdown by Mutation Operator & Survivor Patterns
Most mutators achieved near-perfect kill rates: `EmptyObjectReturnValsMutator` (**45/45**, 100%), `ConditionalsBoundaryMutator` (**5/5**, 100%), `BooleanTrueReturnValsMutator` (**3/3**, 100%), `BooleanFalseReturnValsMutator` (**1/1**, 100%), `PrimitiveReturnsMutator` (**1/1**, 100%), `NegateConditionalsMutator` (**56/57**, 98%), and `NullReturnValsMutator` (**21/26**, 81%; 1 survived, 4 uncovered).

The **15 surviving mutants** concentrate in two classes (`VisitController` at **64%** [9/14] and `PetController` at **80%** [41/51]) and follow three distinct patterns:
1. **`VoidMethodCallMutator` (22/35 killed — 11 survived, 2 uncovered)**: Removal of `model.put(...)`, `redirectAttributes.addFlashAttribute(...)`, or `owner.addVisit(...)` survived when web tests checked HTTP status and view names without asserting model/flash attributes or entity state mutations.
2. **`MathMutator` (0/2 killed — 2 survived)**: Arithmetic index/offset mutations survived where pagination or collection-indexing assertions only checked non-empty results rather than exact page indices.
3. **`NegateConditionalsMutator` (1 survived) & `NullReturnValsMutator` (1 survived)**: Secondary guard branches and nullable return paths where tests verified the redirect path without asserting exact `BindingResult` field errors.

---

## 3. Mutation Score Improvement Plan

1. **Strengthen `VisitControllerTests` (64% $\rightarrow$ >90%)**: Assert `model().attributeExists("visit", "pet", "owner")`, verify flash messages on `redirectAttributes`, and assert that `owner.getPets()` contains the persisted `Visit` before `ownerRepository.save(...)` is called to kill `VoidMethodCallMutator` survivors.
2. **Strengthen `PetControllerTests` (80% $\rightarrow$ >92%)**: Add assertions for flash attributes on creation/update redirects and exact `BindingResult` error codes on duplicate pet names.
3. **Kill `MathMutator` survivors**: Add boundary pagination tests in `OwnerControllerTests` asserting exact page numbers and offset calculations.