<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# test: net.samyn.jgrapht.ascii

## Purpose
Test suite for the library. Mirrors the main source structure with unit tests for each class, plus integration-level example tests that verify full pipeline output.

## Key Files

| File | Description |
|------|-------------|
| `AsciiExporterTest.java` | Tests for the public API: null handling, empty graphs, builder, writer export |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `layout/` | Tests for layout algorithm components (LayerAssigner, CrossingMinimiser, LongEdgeSplitter, SugiyamaLayoutAlgorithm) |
| `model/` | Tests for data model classes (Canvas, GridVertex, GridEdge, DisplayWidth) |
| `render/` | Tests for Unicode and ASCII box renderers |
| `routing/` | Tests for edge routing (OrthogonalEdgeRouter, ObstacleDetector, LaneTracker) |
| `examples/` | Integration tests with example graphs producing full rendered output |
| `testutils/` | Shared test utilities |

## For AI Agents

### Working In This Directory
- Tests use JUnit 5 with `@Test` annotations
- Classes run in parallel (configured in convention plugin); individual tests within a class run on the same thread
- Example tests in `examples/` verify complete rendered output as multi-line strings
- `testutils/TestUtils.java` contains shared helpers

### Testing Requirements
- Every new class should have a corresponding test class
- Follow naming convention: `FooTest.java` for `Foo.java`
- Use descriptive `@Test` method names indicating the scenario tested
- Verify edge cases: null inputs, empty graphs, single vertices, cycles (should throw)

### Common Patterns
- Arrange-Act-Assert structure
- JGraphT `DefaultDirectedGraph` with `DefaultEdge` for test graph construction
- String comparison for rendered output verification

## Dependencies

### Internal
- All production packages under `net.samyn.jgrapht.ascii`

### External
- `org.junit.jupiter:junit-jupiter` - Test framework

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
