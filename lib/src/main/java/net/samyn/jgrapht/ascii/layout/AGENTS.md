<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# layout

## Purpose
Implements the Sugiyama-inspired layered layout algorithm. Transforms a JGraphT DAG into a `GridModel` with positioned vertices by running the four-phase Sugiyama pipeline: layer assignment, long edge splitting, crossing minimisation, and coordinate assignment.

## Key Files

| File | Description |
|------|-------------|
| `LayoutAlgorithm.java` | Strategy interface: `layout(Graph) -> GridModel` |
| `SugiyamaLayoutAlgorithm.java` | Main orchestrator; runs the full pipeline and assigns (x, y) coordinates to vertices |
| `LayerAssigner.java` | Assigns vertices to layers using longest-path BFS (modified Kahn's algorithm); detects cycles |
| `LongEdgeSplitter.java` | Splits edges spanning multiple layers by inserting `DummyVertex` instances at intermediate layers |
| `CrossingMinimiser.java` | Reduces edge crossings via barycenter heuristic with alternating top-down/bottom-up sweeps |
| `DummyVertex.java` | Synthetic vertex for split edges; identity-based equality, deterministic `toString()` |
| `SplitResult.java` | Value object holding the augmented graph, layer map, and dummy vertex set after splitting |

## For AI Agents

### Working In This Directory
- `SugiyamaLayoutAlgorithm` is the main entry point; it calls `LayerAssigner`, `LongEdgeSplitter`, `CrossingMinimiser` in sequence
- Layout constants: `BOX_HEIGHT = 3`, `LAYER_GAP = 4`, `VERTEX_GAP = 2`
- `LongEdgeSplitter` and `SplitResult` are package-private
- `DummyVertex` is package-private; it uses identity equality (no `equals`/`hashCode` override)

### Testing Requirements
- Each class has a corresponding test in the test `layout/` package
- Test `LayerAssigner` with cycles (should throw), empty graphs, and multi-path DAGs
- Test `CrossingMinimiser` with known-crossing configurations
- Test `LongEdgeSplitter` with edges spanning 2+ layers

### Common Patterns
- Single-responsibility: each pipeline step is a separate class
- Validation at method entry (null checks, cycle detection, edge span checks)
- `CrossingMinimiser` uses O(m^2) crossing count, adequate for terminal-sized graphs

## Dependencies

### Internal
- `model/GridModel`, `model/GridVertex`, `model/GridEdge` - output data structures
- `routing/OrthogonalEdgeRouter` - called by `SugiyamaLayoutAlgorithm` after coordinate assignment

### External
- `org.jgrapht:jgrapht-core` - `Graph`, `CycleDetector`, `DefaultDirectedGraph`, `DefaultEdge`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
