# Code Coverage Report (JaCoCo)

## 1. Coverage Configuration

Code coverage analysis is configured in `pom.xml` using **`jacoco-maven-plugin` v0.8.15** (compatible with Java 21 bytecode). The plugin defines three Maven executions:

1. **`prepare-agent`**: Instruments compiled classes during test execution to record runtime execution data into `target/jacoco.exec`.
2. **`report` (phase: `test`)**: Generates HTML (`target/site/jacoco/index.html`) and XML (`target/site/jacoco/jacoco.xml`) coverage reports immediately after unit tests run.
3. **`check` (phase: `verify`)**: Enforces automated **Quality Gates** at package level (`<element>PACKAGE</element>`), failing the build if any subpackage drops below:
   - **Line Coverage (`LINE`)**: $\ge 80\%$ (`0.80`)
   - **Branch Coverage (`BRANCH`)**: $\ge 75\%$ (`0.75`)
   - **Class Coverage (`CLASS`)**: $100\%$ (`1.00`)

The root package `org.springframework.samples.petclinic` is excluded from the package rule because it only contains bootstrap/AOT classes (`PetClinicApplication` and `PetClinicRuntimeHints`) that are not exercised in isolated unit tests.

---

## 2. Coverage Analysis

Running `./mvnw clean verify -Dtest="!*IntegrationTests"` executed **91 unit tests** across **22 classes** with **0 failures** and satisfied all JaCoCo quality gates.

### Global Metrics
- **Instruction Coverage**: **91%** (1,205 / 1,319 instructions covered; 114 missed)
- **Line Coverage**: **94.9%** (316 / 333 lines covered; 17 missed)
- **Branch Coverage**: **86%** (106 / 122 branches covered; 16 missed)
- **Class Coverage**: **95.5%** overall (21 / 22 classes; **100%** across all domain subpackages)

### Breakdown by Package

| Package | Instruction Cov. | Branch Cov. | Covered Lines | Missed Methods |
| :--- | :---: | :---: | :---: | :---: |
| `org.springframework.samples.petclinic.vet` | **100%** | **100%** | 36 / 36 | 0 / 13 |
| `org.springframework.samples.petclinic.model` | **100%** | **100%** | 18 / 18 | 0 / 13 |
| `org.springframework.samples.petclinic.owner` | **96%** | **85%** | 246 / 251 | 3 / 68 |
| `org.springframework.samples.petclinic.system` | **77%** | n/a | 15 / 16 | 2 / 12 |
| `org.springframework.samples.petclinic` | **4%** | n/a | 1 / 12 | 6 / 7 |

### Lowest-Coverage Classes & Uncovered Methods
1. **`PetClinicRuntimeHints`** (`org.springframework.samples.petclinic`): **0% coverage** (9/9 lines missed; 5/5 methods uncovered, including `registerHints(...)` and private resource-registration helpers used strictly during GraalVM native compilation).
2. **`CacheConfiguration`** (`org.springframework.samples.petclinic.system`): **31% instruction coverage** (1/3 lines missed; 2/4 methods uncovered — specifically the `JCacheManagerCustomizer` lambda configuring the `"vets"` cache).
3. **`PetClinicApplication`** (`org.springframework.samples.petclinic`): **37% instruction coverage** (2/3 lines missed — `main(String[] args)` bootstrap method).
4. **`org.springframework.samples.petclinic.owner` edge branches**: 5 lines and 16 conditional branches remain uncovered across controller pagination/binding edge cases and defensive null checks in `Owner` / `PetController` / `VisitController`.

---

## 3. Coverage Improvement Plan

1. **Test `CacheConfiguration` directly**: Add a focused unit test invoking `petclinicCacheConfigurationCustomizer()` with a mocked `javax.cache.CacheManager` to verify that the `"vets"` cache is registered with `MutableConfiguration` and statistics enabled, bringing `.system` coverage to **100%**.
2. **Cover remaining `owner` conditional branches**: Add parameterized JUnit 6 tests for boundary inputs in `OwnerController` (empty search results, multi-page pagination boundaries) and `PetController` / `VisitController` invalid-ID/binding error paths to raise branch coverage from **85%** to **>92%**.
3. **Separate AOT/Bootstrap exclusions**: Explicitly exclude `*RuntimeHints` and `*Application` from JaCoCo instrumentation/reports so metrics accurately reflect business and web-layer test coverage.