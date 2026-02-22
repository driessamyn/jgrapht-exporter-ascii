# Long Edge Splitting in jgrapht-exporter-ascii

This document explains the long edge splitting algorithm implemented in the `LongEdgeSplitter` class,
which ensures every edge in the graph spans exactly one layer before crossing minimisation.

## Purpose

After [layer assignment](LAYERING.md), the longest-path algorithm can produce edges that skip layers. For example,
given edges A→B, B→C, A→C, the layer assignment is A=0, B=1, C=2 — making the edge A→C span 2 layers.

The [crossing minimisation](CROSSING_MINIMISATION.md) algorithm requires all edges to span exactly one layer.
Long edge splitting resolves this by inserting **dummy vertices** at intermediate layers, breaking each long edge
into a chain of single-layer segments.

## Algorithm

The `LongEdgeSplitter` sits in the Sugiyama pipeline between layer assignment and crossing minimisation:

```
Graph<V,E>
    │
    ▼
┌───────────────────────┐
│  1. Layer Assignment  │  → Map<V, Integer>
│     (LayerAssigner)   │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  2. Long Edge Split   │  → SplitResult (augmented graph + layers + dummy set)
│     (LongEdgeSplitter)│
└──────────┬────────────┘
           │
           ▼
┌───────────────────────────┐
│  3. Crossing Minimisation │  → List<List<Object>> (ordered layers)
│     (CrossingMinimiser)   │
└──────────┬────────────────┘
           │
           ▼
┌───────────────────────────┐
│  4. Coordinate Assignment │  → GridModel (positioned vertices)
└───────────────────────────┘
```

### Processing each edge

For each edge in the original graph, the splitter computes the **span** (`targetLayer - sourceLayer`):

- **Span = 1:** The edge is short — it is added directly to the augmented graph.
- **Span > 1:** The edge is long — `(span - 1)` dummy vertices are inserted at intermediate layers, forming a chain.

### Example: splitting a 3-layer edge

Given edge A→D where A is at layer 0 and D is at layer 3:

```
Before:                      After:

Layer 0:  A ─────────┐      Layer 0:  A
          │           │                │
Layer 1:  B           │      Layer 1:  B    d0
          │           │                │     │
Layer 2:  C           │      Layer 2:  C    d1
          │           │                │     │
Layer 3:  D ◄─────────┘      Layer 3:  D ◄──┘

Original edge A→D             Chain: A→d0→d1→D
(spans 3 layers)              (each segment spans 1 layer)
```

Dummy vertices `d0` and `d1` are inserted at layers 1 and 2 respectively, and the original long edge A→D
is replaced by three single-layer edges: A→d0, d0→d1, d1→D.

## Key design decisions

### Vertex type in the augmented graph

The augmented graph uses `Object` as its vertex type. Original vertices (type `V`) are added as-is, while
dummy vertices are instances of the package-private `DummyVertex` class. This avoids a complex wrapper type
while keeping the implementation simple. `DummyVertex` uses identity-based equality, so each instance is
inherently unique.

### Filtering dummies from the output

Dummy vertices participate in crossing minimisation and coordinate assignment (they occupy space on the grid)
but are **excluded** from the final `GridModel<V>` output. The `SplitResult.isDummy()` method identifies which
vertices to skip during coordinate assignment.

Dummy vertex positions serve as bend points for orthogonal edge routes — see [Edge Routing](EDGE_ROUTING.md).

### Defence in depth

The `CrossingMinimiser` retains its validation that all edges span exactly one layer. After splitting,
this should always pass — but keeping the check provides a safety net against bugs in the splitter.

## Output

The `splitLongEdges` method returns a `SplitResult` containing:

- `graph()` — the augmented `Graph<Object, DefaultEdge>` with dummy vertices and single-layer edges
- `layers()` — the updated `Map<Object, Integer>` layer assignment including dummy vertices
- `dummyVertices()` — the `Set<Object>` of all inserted dummy vertices
- `isDummy(vertex)` — convenience method to check if a vertex is a dummy
