<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# jgrapht-exporter-ascii

## Purpose
A Java library that exports JGraphT directed acyclic graphs (DAGs) to ASCII/Unicode text art. Uses a Sugiyama-inspired layered layout algorithm with orthogonal edge routing to produce readable box-and-arrow diagrams in the terminal.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle.kts` | Root build script; applies Kover for aggregate coverage |
| `settings.gradle.kts` | Gradle settings; includes the `lib` subproject (renamed to `jgrapht-exporter-ascii`) |
| `gradle.properties` | Project properties (version, group) |
| `.gitignore` | Git ignore rules |
| `LICENSE` | Project licence |
| `gradlew` / `gradlew.bat` | Gradle wrapper scripts |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `lib/` | Main library module containing all source and test code (see `lib/AGENTS.md`) |
| `buildSrc/` | Gradle convention plugins (see `buildSrc/AGENTS.md`) |
| `gradle/` | Gradle wrapper and version catalogue (see `gradle/AGENTS.md`) |
| `.github/` | CI/CD workflows (see `.github/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- This is a **single-module Gradle project** with the library in `lib/`
- Use British English in all code, comments, documentation, and commit messages
- Follow conventional commits: `feat:`, `fix:`, `chore:`, `refactor:`, `docs:`, `test:`
- TDD workflow: write tests first, then implementation

### Build Commands
- `./gradlew build` - full build with tests and coverage verification
- `./gradlew test` - run tests only
- `./gradlew koverHtmlReport` - generate coverage report
- `./gradlew spotlessApply` - format code (auto-runs before compilation)
- `./gradlew check` - runs tests + coverage verification (90% minimum)

### Testing Requirements
- JUnit 5 with parallel class execution
- Kover coverage minimum: 90% instruction coverage
- Tests mirror the main source structure under `lib/src/test/`

### Architecture Overview
The export pipeline follows the Sugiyama framework:
1. **Layer assignment** - longest-path from sources (`layout/LayerAssigner`)
2. **Long edge splitting** - insert dummy vertices (`layout/LongEdgeSplitter`)
3. **Crossing minimisation** - barycenter heuristic (`layout/CrossingMinimiser`)
4. **Coordinate assignment** - centre vertices within layers (`layout/SugiyamaLayoutAlgorithm`)
5. **Edge routing** - orthogonal paths with obstacle avoidance (`routing/OrthogonalEdgeRouter`)
6. **Rendering** - draw boxes and edges onto a character canvas (`render/`)

## Dependencies

### External
- `org.jgrapht:jgrapht-core:1.5.2` - Graph data structures and algorithms
- `org.junit.jupiter:junit-jupiter:5.11.4` - Testing framework
- Gradle 9.0.0 with JDK 21 toolchain, Java 11 bytecode target

## Contribution Guidelines

- **TDD** — always write or update tests first; ensure high test coverage
- **SOLID principles** — all code must adhere to SOLID
- **Javadoc** — all public API must have Javadoc comments
- **Minimal comments** — code should be self-explanatory where possible; when comments are needed, focus on "why", not "what"
- **No test ceremony** — tests should never have given/when/then or arrange/act/assert comments; the structure should be obvious from the code
- **No extra dependencies** — do not introduce any third-party dependencies beyond the JGraphT library
- **British English** — everywhere: code, comments, documentation, commit messages

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
