# SOFTWARE DEPENDABILITY PROJECT - FABRIZIO COZZOLINO

[![Java CI Pipeline](https://github.com/FabrizioCozzolino/spring-petclinic/actions/workflows/ci.yml/badge.svg)](https://github.com/FabrizioCozzolino/spring-petclinic/actions/workflows/ci.yml)
[![Code Coverage](https://codecov.io/gh/FabrizioCozzolino/spring-petclinic/branch/main/graph/badge.svg)](https://codecov.io/gh/FabrizioCozzolino/spring-petclinic)
[![Mutation Score](https://img.shields.io/badge/PITest%20Mutation%20Score-88%25-brightgreen)](mutation.md)

## Software Dependability Reports

- [Lab 1: CI/CD Pipeline Documentation](CICD.md)
- [Lab 2a: Containerization & Docker Report](Docker.md)
- [Lab 3: Unit Testing Report (JUnit 6 & Mockito 5)](testing.md)
- [Lab 4a: Code Coverage Report (JaCoCo)](coverage.md)
- [Lab 4b: Mutation Testing Report (PITest)](mutation.md)
- [Lab 5: Microbenchmarking Report (JMH, JU2JMH & AMBER)](benchmarking.md)

## Testing, Code Coverage & Mutation Analysis

Unit tests are implemented using **JUnit 6.1.3** and **Mockito 5.24.0** following the **FIRST** principles (Fast, Independent, Repeatable, Self-validating, Thorough), with automated quality gates enforced via **JaCoCo 0.8.15** and **PITest 1.17.4**:

- Run unit tests and enforce JaCoCo quality gates (`Line >= 80%`, `Branch >= 75%`, `Class = 100%`):
  ```bash
  ./mvnw clean verify -Dtest="!*IntegrationTests"