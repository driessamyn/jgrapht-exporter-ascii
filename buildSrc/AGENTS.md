<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# buildSrc

## Purpose
Gradle convention plugins that define shared build configuration for the library module. Contains a single convention plugin that configures Java compilation, testing, formatting, and coverage.

## Key Files

| File | Description |
|------|-------------|
| `settings.gradle.kts` | buildSrc settings (applies Kover plugin dependency) |
| `src/main/kotlin/jgrapht-ascii.library-conventions.gradle.kts` | Convention plugin: Java 21 toolchain, Java 11 target, Spotless formatting, Kover 90% coverage, JUnit 5 parallel tests |

## For AI Agents

### Working In This Directory
- The convention plugin `jgrapht-ascii.library-conventions` is applied by `lib/build.gradle.kts`
- Changes here affect all modules that apply the convention plugin
- Key settings configured:
  - **Toolchain**: JDK 21
  - **Bytecode target**: Java 11 (`options.release.set(11)`)
  - **Formatting**: Google Java Format via Spotless (auto-applied before compilation)
  - **Coverage**: Kover with 90% minimum instruction coverage
  - **Testing**: JUnit 5 with parallel class execution

### Common Patterns
- Convention plugins use Kotlin DSL (`.gradle.kts`)
- Plugin dependencies are declared in the buildSrc `settings.gradle.kts` via the version catalogue

## Dependencies

### External
- `org.jetbrains.kotlinx:kover-gradle-plugin:0.9.4` - Code coverage
- `com.diffplug.spotless` - Code formatting

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
