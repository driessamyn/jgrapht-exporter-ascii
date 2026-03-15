<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# net.samyn.jgrapht.ascii

## Purpose
Root package of the library. Contains the public entry point `AsciiExporter` and orchestrates the layout-route-render pipeline. Subpackages handle the individual pipeline stages.

## Key Files

| File | Description |
|------|-------------|
| `AsciiExporter.java` | Public API entry point; accepts a JGraphT `Graph`, runs the Sugiyama layout pipeline, and returns rendered ASCII/Unicode text. Supports builder pattern for custom label providers and renderers. |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `layout/` | Sugiyama layout algorithm and helpers (see `layout/AGENTS.md`) |
| `model/` | Grid data model: vertices, edges, canvas (see `model/AGENTS.md`) |
| `render/` | Canvas renderers: Unicode and ASCII box drawing (see `render/AGENTS.md`) |
| `routing/` | Orthogonal edge routing with obstacle avoidance (see `routing/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- `AsciiExporter` is the only public-facing class most users interact with
- It wires together layout, routing, and rendering via constructor injection
- The builder pattern allows customising `labelProvider` (vertex-to-string) and `renderer` (Unicode vs ASCII)
- Default configuration: `UnicodeBoxRenderer` + `Object::toString`

### Pipeline Flow
```
Graph -> SugiyamaLayoutAlgorithm.layout() -> GridModel (vertices)
      -> OrthogonalEdgeRouter.routeEdges() -> GridModel (vertices + edges)
      -> CanvasRenderer.render*()           -> Canvas
      -> Canvas.toString()                  -> String
```

### Common Patterns
- Strategy pattern for `CanvasRenderer` and `LayoutAlgorithm`
- Immutable data model (`GridModel`, `GridVertex`, `GridEdge` use defensive copies)
- Null validation with `IllegalArgumentException` at public boundaries

## Dependencies

### Internal
- `layout/` - Sugiyama layout algorithm
- `model/` - Grid data structures and canvas
- `render/` - Box and edge rendering
- `routing/` - Edge path computation

### External
- `org.jgrapht:jgrapht-core` - `Graph` interface and graph types

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
