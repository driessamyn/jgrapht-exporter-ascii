# Crossing Minimisation in jgrapht-exporter-ascii

This document explains the crossing minimisation algorithm implemented in the `CrossingMinimiser` class, which reorders
vertices within layers to reduce edge crossings in the ASCII visualisation.

## Purpose

After [layer assignment](LAYERING.md), each vertex has been placed on a layer, but the order of vertices within each
layer is arbitrary. A poor ordering leads to many edge crossings, making the graph hard to read:

```
Layer 0:   A     B              Layer 0:   B     A
            \   /                           |     |
             \ /                            |     |
              X   <-- crossing              |     |
             / \                            |     |
            /   \                           |     |
Layer 1:   D     C              Layer 1:   C     D
                                        (no crossings)
```

Crossing minimisation reorders vertices within their layers so that connected vertices are placed closer together,
reducing or eliminating crossings.

## Algorithm: Barycenter Heuristic with Iterative Sweeping

The `CrossingMinimiser` uses the **barycenter heuristic**, one of the most widely used methods for crossing reduction in
layered graph drawing. The core idea is simple: place each vertex at the average position of its neighbours in the
adjacent layer.

### Key Steps

1. **Initialisation:**
    * Vertices are grouped by their assigned layer.
    * Each layer is sorted alphabetically for deterministic initial ordering.

2. **Barycenter Calculation:**
    * For a given "free" layer being reordered, each vertex computes its **barycentre** — the average position of its
      neighbours in the adjacent "fixed" layer.
    * For example, if vertex `C` in layer 1 has neighbours at positions 0 and 2 in layer 0, its barycentre
      is `(0 + 2) / 2 = 1.0`.
    * Vertices with no neighbours in the fixed layer keep their current position.

3. **Reordering:**
    * Vertices in the free layer are sorted by their barycentre values.
    * This pulls each vertex toward the average position of its connections, naturally reducing crossings.

4. **Iterative Sweeping:**
    * A single pass in one direction may not find the optimal ordering, so the algorithm alternates:
        * **Top-down sweep:** Fix each layer in turn and reorder the layer below it.
        * **Bottom-up sweep:** Fix each layer in turn and reorder the layer above it.
    * After each full sweep pair, the total number of crossings is counted.
    * If the crossing count improved, the algorithm continues; otherwise, it restores the best ordering found and stops.
    * A maximum of 24 iterations prevents excessive computation on large graphs.

### Example Walkthrough

Consider a three-layer graph with crossings:

```
Layer 0:  A    B        Edges: A->D, B->C, C->F, D->E
Layer 1:  C    D
Layer 2:  E    F
```

**Initial crossings:** A connects to D (position 1) but A is at position 0. B connects to C (position 0) but B is at
position 1. The edges cross.

**Top-down sweep, reorder layer 1:**
* C's barycentre from layer 0: B is at position 1 → barycentre = 1.0
* D's barycentre from layer 0: A is at position 0 → barycentre = 0.0
* Reorder: D (0.0), C (1.0) → layer 1 becomes `[D, C]`

**Top-down sweep, reorder layer 2:**
* E's barycentre from layer 1: D is at position 0 → barycentre = 0.0
* F's barycentre from layer 1: C is at position 1 → barycentre = 1.0
* Reorder: E (0.0), F (1.0) → layer 2 becomes `[E, F]`

**Result:**

```
Layer 0:  A    B
          |    |
Layer 1:  D    C
          |    |
Layer 2:  E    F
```

All crossings eliminated in a single top-down sweep.

## Counting Crossings

Two edges `(u1→v1)` and `(u2→v2)` between adjacent layers cross if and only if `u1` is before `u2` in their layer but
`v1` is after `v2` in the next layer (or vice versa). The algorithm counts all such pairs to evaluate the quality of an
ordering.

## Output

The `minimiseCrossings` method returns a `List<List<V>>`, where each inner list contains the vertices of one layer in
their optimised order. Layer 0 is the first element, layer 1 the second, and so on.

## Constraints

* The algorithm is a **heuristic** — it does not guarantee the globally optimal ordering, but produces good results in
  practice for typical DAGs.
* The input must be a DAG with valid layer assignments (as produced by `LayerAssigner`).
* All edges must span exactly one layer. Long edges (where the target is more than one layer below the source) must be
  split into single-layer segments before calling `minimiseCrossings`.

## Performance

Crossing counting uses pairwise edge comparison — O(m²) per layer pair — and is recomputed on each iteration. This is
adequate for the intended use case of ASCII terminal rendering, where layers typically contain fewer than 20 vertices
and edges per layer pair are in the low hundreds at most. For a typical terminal graph (~200 vertices, ~15 layers),
the worst-case total work per iteration is around 2.4 million comparisons, well under a millisecond on modern hardware.

For significantly larger graphs, the crossing count could be replaced with an O(m log m) algorithm based on merge-sort
inversion counting.
