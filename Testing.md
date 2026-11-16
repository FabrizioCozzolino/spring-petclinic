# Testing Strategy & Unit Test Report

## 1. Overview & Tooling

This project adopts an automated unit testing strategy built on **JUnit 6.1.3** (`junit-jupiter`) and **Mockito 5.24.0** (`mockito-junit-jupiter`), integrated with **Maven Surefire**, **JaCoCo** for code coverage reporting, and **Spring JavaFormat / Checkstyle** for static code quality enforcement.

While Spring Petclinic includes Spring Boot slice and integration tests (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`), this suite introduces fast, completely isolated unit tests for core domain logic and web controller workflows without starting an application context or database engine.

## 2. Alignment with FIRST Principles

Every unit test class in this repository is designed to satisfy the five **FIRST** principles of Test-Driven Development:

- **Fast:** Pure JUnit and Mockito tests (`OwnerUnitTests`, `OwnerControllerUnitTests`) execute in milliseconds/few seconds without Spring context initialization or container startup overhead.
- **Independent:** Each test method initializes fresh fixtures via `@BeforeEach` and mocks external collaborators (`OwnerRepository`, `BindingResult`, `Model`, `RedirectAttributes`), preventing shared mutable state or execution-order dependencies.
- **Repeatable:** Tests rely exclusively on deterministic in-memory inputs and stubbed repository responses, producing identical outcomes across local Windows/Linux environments and GitHub Actions runners.
- **Self-Validating:** All test cases use explicit JUnit assertions (`assertEquals`, `assertNull`, `assertThrows`, `assertTrue`) and Mockito behavioral verifications (`verify`, `never`) with zero manual inspection required.
- **Thorough:** Beyond happy paths, the suite targets boundary conditions, invalid inputs, validation failures, pagination edge cases, and security/integrity checks.

## 3. Tested Components & Critical Scenarios

### `OwnerUnitTests` (Domain Layer)
Tests pure business rules inside `org.springframework.samples.petclinic.owner.Owner`:
- **Pet collection management:** Verifies `addPet()` ignores `null` references, duplicate object references, and distinct instances sharing the same persisted identifier.
- **Case-insensitive lookup & `ignoreNew` filtering:** Validates `getPet(String, boolean)` across uppercase/lowercase queries, missing names, and unsaved entities (`id == null`).
- **Visit registration & defensive assertions:** Verifies `addVisit()` attaches visits to persisted pets and throws `IllegalArgumentException` when `petId` or `visit` is `null` or unknown.

### `OwnerControllerUnitTests` (Controller Layer with Mockito 5)
Isolates `OwnerController` from persistence and servlet infrastructure using `@ExtendWith(MockitoExtension.class)`, `@Mock`, and `@InjectMocks`:
- **Creation workflow (`processCreationForm`):** Verifies that validation errors prevent `OwnerRepository.save()` calls and return the form view, whereas valid submissions persist the entity and redirect with flash feedback.
- **Search & pagination (`processFindForm`):** Covers empty search results (`notFound` field error), single-match direct redirect, multi-match pagination model population, and out-of-bounds page redirects.
- **Update integrity (`processUpdateOwnerForm`):** Verifies protection against URL/form ID mismatch before persisting changes.
- **Retrieval error handling (`showOwner`, `findOwner`):** Confirms `IllegalArgumentException` is raised when querying non-existent owner IDs.

## 4. CI/CD Integration

Unit tests run automatically on every `push` and `pull_request` targeting `main` via GitHub Actions (`.github/workflows/ci.yml`), executing `mvnw test` and archiving Surefire XML/TXT reports as workflow artifacts.