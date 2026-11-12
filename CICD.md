# CI/CD Pipeline Documentation & Self-Audit

## 1. Overview
This document describes the Continuous Integration (CI) pipeline configured for the **Spring PetClinic** web application (`FabrizioCozzolino/spring-petclinic`) as part of the Software Dependability course project. The workflow is defined in `.github/workflows/ci.yml` and runs automatically on GitHub Actions.

---

## 2. Pipeline Stages & Trigger Rules

### Trigger Rules (`on`)
The pipeline is triggered automatically on:
- **`push`** events to the `main` or `master` branches.
- **`pull_request`** events targeting the `main` or `master` branches.

This ensures that every direct commit and every proposed change via Pull Request is verified before being integrated into the primary codebase.

### Job: `build-and-test`
The pipeline executes on a clean, ephemeral **`ubuntu-latest`** virtual machine and consists of the following sequential steps:

1. **Checkout Repository (`actions/checkout@v4`):** Clones the exact commit SHA that triggered the workflow onto the runner.
2. **Java Environment Setup (`actions/setup-java@v4`):** Installs **Eclipse Temurin JDK 21** and enables dependency caching (`cache: 'maven'`) for `~/.m2/repository` to speed up subsequent builds while maintaining deterministic dependency resolution.
3. **Maven Wrapper Permissions (`chmod +x mvnw`):** Ensures the project-bundled Maven Wrapper script has execution permissions on Linux runners.
4. **Build and Automated Testing (`./mvnw clean test`):** Compiles the application from a clean target directory and runs the complete JUnit 5 test suite (unit tests, persistence layer tests, and Spring MVC controller tests).

---

## 3. Build Reproducibility & Code Quality Assurance

### Build Reproducibility
- **Ephemeral Runners:** Every workflow run starts from a fresh `ubuntu-latest` container, eliminating state pollution or hidden local dependencies ("works on my machine" issues).
- **Pinned Runtime & Build Tool:** By explicitly specifying `java-version: '21'` (`temurin`) and invoking the repository's own Maven Wrapper (`./mvnw` instead of a global `mvn` installation), both the compiler version and the Maven build engine version are locked across all local and remote environments.
- **Clean Lifecycle Execution:** Running `clean test` forces Maven to discard any previously compiled bytecode (`target/`) and rebuild all classes from source before running tests.

### Code Quality & Dependability
- **Automated Regression Gate:** The entire JUnit test suite executes automatically on every push and pull request. If a single unit or integration test fails, the job exits with a non-zero status code and marks the build as failed, preventing broken code from being merged silently.
- **Fast Feedback Loop:** Caching Maven dependencies (`cache: 'maven'`) keeps execution times low, providing developers with immediate feedback on code correctness and build integrity.