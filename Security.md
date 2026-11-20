# Code Security and Analysis Strategy

This document summarizes the automated security analysis, dependency governance, secret protection, and CI/CD quality gates configured for **Spring Petclinic** (`FabrizioCozzolino/spring-petclinic`).

## 1. Code Scanning Configuration (CodeQL)

Static Application Security Testing (SAST) is configured via a dedicated GitHub Actions workflow (`.github/workflows/codeql-analysis.yml`) using **GitHub CodeQL Action v3**:
- **Languages Analyzed**: `java` (compiled on Temurin JDK 21 with `./mvnw clean compile -DskipTests`, keeping custom JMH `1.37-extended` artifacts isolated in the `benchmark` Maven profile).
- **Query Suite**: `security-and-quality` to detect both security vulnerabilities (CWEs) and maintainability/reliability defects.
- **Triggers**: Every `push` and `pull_request` targeting `main`, plus a weekly scheduled scan (`cron: '30 2 * * 1'`).
- **SARIF Category**: `/language:java` (separated from `/language:java-ci` used in the main CI workflow).

## 2. Secret Scanning & Push Protection

GitHub **Secret Scanning** and **Push Protection** are enabled in repository settings (`Settings > Code security`):
- **Continuous Scanning**: Automatically scans commits and repository history for exposed API keys, tokens, and credentials across partner and generic patterns.
- **Push Protection**: Blocks `git push` operations containing detected secrets before they reach the remote repository. During project setup, push protection proactively blocked an accidental push of a Docker Personal Access Token (PAT) in `.env`, enforcing immediate removal and token hygiene before committing.
- **Configuration Hygiene**: Environment files (`.env`) are excluded via `.gitignore`, and CI credentials (`CODECOV_TOKEN`, `GITHUB_TOKEN`) are injected exclusively through encrypted GitHub Actions Secrets.

## 3. Dependabot Configuration

Automated dependency monitoring and updates are configured via `.github/dependabot.yml` alongside **Dependabot Alerts** and **Dependabot Security Updates**:
- **Maven Ecosystem (`maven`)**: Checked on a `daily` schedule (`open-pull-requests-limit: 10`), commit prefix `deps`, assigned to `FabrizioCozzolino`.
- **GitHub Actions Ecosystem (`github-actions`)**: Checked on a `weekly` schedule (`open-pull-requests-limit: 5`), commit prefix `ci(deps)`, assigned to `FabrizioCozzolino`.
- **SBOM & HTTP Enforcement**: CycloneDX (`cyclonedx-maven-plugin`) generates an application SBOM (`application.cdx.json`) at build time, while `nohttp-checkstyle` blocks insecure plain-HTTP dependency or repository URLs during Maven `validate`.

## 4. CI/CD Security Integration & Quality Gates

Security checks are integrated into the primary pipeline (`.github/workflows/ci.yml`) inside the `security-checks` job (`needs: build-and-test`):
1. **SAST Gate**: Runs CodeQL (`security-and-quality`) on every push and pull request to `main`/`master`.
2. **Dependency Alert Gate**: Queries the GitHub REST API (`gh api repos/${{ github.repository }}/dependabot/alerts?state=open`) and fails the CI build (`exit 1`) if any open Dependabot security alerts exist.
3. **Code Quality & Coverage Gates**: Enforced alongside security checks via `spring-javaformat:validate`, `nohttp-checkstyle`, JaCoCo (80% line, 75% branch), and PITest (80% mutation threshold).

## 5. Security Policy

- **Vulnerability Prioritization & Remediation**: Critical and High severity alerts are triaged immediately (target SLA < 24 hours); Medium/Low alerts within 7 days; Note/informational findings are reviewed per sprint and either fixed in code or documented as accepted false positives.
- **Dependabot PR Workflow**: Automated Dependabot PRs trigger the full `Java CI Pipeline` (unit tests, JaCoCo, PITest, CodeQL). PRs are merged only after all CI checks pass.
- **Secret Management & Rotation**: No plaintext secrets are permitted in source control. Any exposed credential is immediately revoked, rotated at the provider, and purged from local configuration.

## 6. Security Metrics

| Tool / Category | Critical | High | Medium | Low | Note | Open | Resolved / Dismissed |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Code Scanning (CodeQL)** | 0 | 0 | 0 | 0 | 3 | 0 | 3 (2 fixed in code, 1 dismissed) |
| **Secret Scanning** | 0 | 0 | 0 | 0 | 0 | 0 | 1 blocked pre-push via Push Protection |
| **Dependabot Alerts** | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

- **Mean Time to Remediate (MTTR)**: ~10 minutes for initial CodeQL findings (`OwnerRepository.java:60` missing `@Override` and `PetController.java:101` unused parameter fixed in commit `6cca010`; `Vets.java:37` `Note` dismissed as required by JAXB `@XmlElement` binding).