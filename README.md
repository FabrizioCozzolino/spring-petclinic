# SOFTWARE DEPENDABILITY PROJECT - FABRIZIO COZZOLINO

[![Java CI Pipeline](https://github.com/FabrizioCozzolino/spring-petclinic/actions/workflows/ci.yml/badge.svg)](https://github.com/FabrizioCozzolino/spring-petclinic/actions/workflows/ci.yml)

- **Software Dependability Reports:** [CI/CD Pipeline Documentation](CICD.md)

---

### Aggiornamento del `README.md`

Aggiungi il link a `Docker.md` accanto a quello del Lab 1 nel tuo **`README.md`**:

```markdown
- **Software Dependability Reports:**
  - [Lab 1: CI/CD Pipeline Documentation](CICD.md)
  - [Lab 2a: Containerization & Docker Report](Docker.md)

## Testing

Unit tests are implemented using **JUnit 6.1.3** and **Mockito 5.24.0** following the **FIRST** principles (Fast, Independent, Repeatable, Self-validating, Thorough).

- Run fast unit tests locally:
  ```bash
  ./mvnw test -Dtest="!*IntegrationTests"  