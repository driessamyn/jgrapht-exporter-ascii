<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# workflows

## Purpose
GitHub Actions workflow definitions for CI/CD: build verification and package publishing.

## Key Files

| File | Description |
|------|-------------|
| `build-and-test.yml` | CI workflow: builds and tests on push/PR |
| `gh-publish.yml` | Publishes to GitHub Packages |
| `mc-publish.yml` | Publishes to Maven Central |

## For AI Agents

### Working In This Directory
- Workflows use Gradle wrapper (`./gradlew`) for all build steps
- JDK 21 is the toolchain version used in CI
- Ensure any new workflows follow the existing naming pattern

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
