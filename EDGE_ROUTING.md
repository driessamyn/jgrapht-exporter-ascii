# Edge Routing in jgrapht-exporter-ascii

This document explains the orthogonal edge routing algorithm implemented in the `OrthogonalEdgeRouter` class,
which computes waypoint-based paths between connected vertices and renders them as box-drawing characters on the canvas.

## Purpose

After [coordinate assignment](COORDINATE_ASSIGNMENT.md), vertices have concrete positions on the character grid but
there is no visual indication of graph structure. Edge routing computes paths between connected vertices so that
the rendered output shows the full graph topology.

## Pipeline Position

```
Graph<V,E>
    │
    ▼
┌───────────────────────┐
│  1. Layer Assignment  │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  2. Long Edge Split   │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────────┐
│  3. Crossing Minimisation │
└──────────┬────────────────┘
           │
           ▼
┌───────────────────────────┐
│  4. Coordinate Assignment │
└──────────┬────────────────┘
           │
           ▼
┌───────────────────────────┐
│  5. Edge Routing          │  ← this phase
│     (OrthogonalEdgeRouter)│
└───────────────────────────┘
```

## Data Model

### GridEdge

Each routed edge is represented as a `GridEdge<V>` containing:

- `source()` — the source vertex
- `target()` — the target vertex
- `path()` — an ordered list of `int[]` waypoints, each an `{x, y}` pair

The first waypoint is the **exit point** (centre of the source vertex's bottom border). The last waypoint is the
**entry point** (one row above the target vertex's top border, where the arrow is drawn). Intermediate waypoints
are bend points.

The constructor validates that every waypoint is a non-null 2-element array, failing fast with an
`IllegalArgumentException` if the contract is violated.

### GridModel

The `GridModel` now carries both vertices and edges:

```java
GridModel<V> model = layout.layout(graph);
model.vertices();  // List<GridVertex<V>>
model.edges();     // List<GridEdge<V>>
```

The single-argument `GridModel(vertices)` constructor remains for backward compatibility, defaulting edges to an
empty list.

## Algorithm

For each edge in the original graph, the router looks up the source and target positions in the `GridModel` and
computes an orthogonal path.

### Attachment Points

```
exitX  = source.x() + source.width() / 2     (centre of bottom border)
exitY  = source.y() + 2                       (bottom border row)
entryX = target.x() + target.width() / 2     (centre of top border)
entryY = target.y()                           (top border row)
```

### Case 1: Straight Edge (exitX == entryX)

When source and target are horizontally aligned, the path is a simple vertical line:

```
Path: (exitX, exitY) → (exitX, entryY - 1)
```

Rendered example:

```
┌───┐
│ A │
└─┬─┘
  │
  v
┌───┐
│ B │
└───┘
```

### Case 2: Bent Edge (exitX != entryX)

When source and target are offset, the path includes a horizontal segment one row below the source:

```
bendY = exitY + 1
Path: (exitX, exitY) → (exitX, bendY) → (entryX, bendY) → (entryX, entryY - 1)
```

Rendered example:

```
┌───┐
│ A │
└─┬─┘
  │
  └──┐
     v
   ┌───┐
   │ B │
   └───┘
```

## Rendering

The `CanvasRenderer` interface includes a `renderEdge(Canvas, GridEdge<?>)` method implemented by both renderers.

### Unicode (`UnicodeBoxRenderer`)

| Element                          | Character | Unicode |
|----------------------------------|-----------|---------|
| Vertical segment                 | │         | U+2502  |
| Horizontal segment               | ─         | U+2500  |
| Corner (from left, going down)   | ┐         | U+2510  |
| Corner (from above, going right) | └         | U+2514  |
| Corner (from above, going left)  | ┘         | U+2518  |
| Corner (from right, going down)  | ┌         | U+250C  |
| Border junction                  | ┬         | U+252C  |
| Arrow (downward)                 | v         | ASCII   |

### ASCII (`AsciiBoxRenderer`)

| Element            | Character |
|--------------------|-----------|
| Vertical segment   | \|        |
| Horizontal segment | -         |
| Corner / junction  | +         |
| Arrow (downward)   | v         |

### Junction Characters

When an edge exits from a vertex's bottom border, the renderer checks the existing canvas character at the exit
point. If it finds a border character (`─` or `-`), it replaces it with a junction character (`┬` or `+`),
creating a visual connection between the box and the edge.

### Rendering Order

Vertices should be rendered before edges. This ensures border characters are present when edges are drawn, allowing
the renderer to detect borders and replace them with junction characters (`┬` / `+`). Edge segments in the gap
between layers are drawn after vertices, so they are not overwritten.

## Related

- [Coordinate Assignment](COORDINATE_ASSIGNMENT.md) — how vertices are positioned before edge routing
- [Long Edge Splitting](LONG_EDGE_SPLITTING.md) — how edges spanning multiple layers are split, providing
  intermediate positions for edge routing
