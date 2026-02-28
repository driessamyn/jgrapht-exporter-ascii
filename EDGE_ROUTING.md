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
  │
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
  │
  └──┐
     v
   ┌───┐
   │ B │
   └───┘
```

## Edge Routing Order

Edges are sorted before routing so that short, local edges are processed first and long, spanning
edges are processed last. This ensures short edges claim the lanes closest to their vertices while
long edges use whatever rows remain.

The sort criteria, in priority order:

1. **Vertical span** (ascending) — `|target.y() - source.y()|`. Shorter edges first.
2. **Horizontal span** (ascending) — `|target.centreX() - source.centreX()|`. Among edges with
   the same vertical span, narrower edges first. Uses box centres (not origins) since that is
   where edges attach.
3. **Coordinate tie-breaking** — deterministic fallback by source y, source x, target y, then
   target x. This ensures no two distinct edges compare as equal.

## Lane Assignment

When multiple edges share the same inter-layer gap, their horizontal segments can visually merge into a single line,
making it hard to trace individual connections. The `LaneTracker` addresses this by spreading horizontal segments
across different rows within each gap.

The `LAYER_GAP` between vertex boxes is 4 rows, providing space for up to 4 horizontal edge segments on separate rows.
As each edge is routed, the tracker records which (row, x-range) combinations are already claimed. Before placing a
new horizontal segment, the router calls `findFreeRow` to locate the first unclaimed row in the gap, avoiding overlap
with previously routed edges.

### How it works

1. A single `LaneTracker` instance is created per routing pass and shared across all edges.
2. For bent paths, after computing the initial bend row, the router queries `findFreeRow(bendY, maxY, minX, maxX)`
   to find a free row for the horizontal segment.
3. For vertical paths with obstacle detours, the tracker similarly assigns free rows for the horizontal detour segments.
4. After each path is constructed, all its horizontal segments are registered with `claim(y, minX, maxX)`.

This ensures that in dense graphs — such as the film production workflow — each horizontal edge segment occupies its
own row, making the output easier to read.

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
