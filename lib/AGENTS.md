<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# lib (jgrapht-exporter-ascii)

## Purpose
The sole library module containing all production and test source code. Applies the `jgrapht-ascii.library-conventions` convention plugin and declares the JGraphT and JUnit dependencies.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle.kts` | Module build script; applies convention plugin, declares dependencies |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `src/main/java/net/samyn/jgrapht/ascii/` | Production source code (see `src/main/java/net/samyn/jgrapht/ascii/AGENTS.md`) |
| `src/test/java/net/samyn/jgrapht/ascii/` | Test source code (see `src/test/java/net/samyn/jgrapht/ascii/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- This module is renamed to `jgrapht-exporter-ascii` in `settings.gradle.kts`
- Dependencies: `api(libs.jgrapht.core)` for production, `testImplementation(libs.junit.jupiter)` for tests
- The convention plugin handles formatting, compilation, testing, and coverage

### Testing Requirements
- Run `./gradlew test` from the project root
- Tests are in `src/test/` mirroring the main source structure
- Coverage verified automatically via Kover on `check`

## Dependencies

### External
- `org.jgrapht:jgrapht-core:1.5.2` - API dependency
- `org.junit.jupiter:junit-jupiter:5.11.4` - Test dependency

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
