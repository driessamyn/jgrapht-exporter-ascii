<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# gradle

## Purpose
Gradle wrapper distribution and version catalogue for centralised dependency management.

## Key Files

| File | Description |
|------|-------------|
| `libs.versions.toml` | Version catalogue: defines JGraphT 1.5.2, JUnit Jupiter 5.11.4, Kover plugin versions |
| `wrapper/gradle-wrapper.properties` | Gradle wrapper configuration (Gradle 9.0.0) |
| `wrapper/gradle-wrapper.jar` | Gradle wrapper bootstrap JAR |

## For AI Agents

### Working In This Directory
- Add new dependencies to `libs.versions.toml` using the version catalogue format
- Reference dependencies in build scripts as `libs.<name>` (e.g., `libs.jgrapht.core`)
- Do not modify `gradle-wrapper.jar` directly; use `./gradlew wrapper --gradle-version=<version>` to upgrade

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
