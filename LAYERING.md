# Graph Layering in jgrapht-exporter-ascii

This document explains the graph layering algorithm implemented in the `LayerAssigner` class, which is a crucial step in
preparing a directed acyclic graph (DAG) for ASCII visualization.

## Purpose

Graph layering is the process of assigning each vertex in a graph to a specific "layer" or "rank" such that if there is
a directed edge from vertex `u` to vertex `v`, then `u` is in a layer numerically smaller than `v`.
This creates a hierarchical structure that is essential for drawing graphs in a readable, top-to-bottom (or
left-to-right) fashion, especially for layered drawing algorithms like Sugiyama.
For ASCII art, layers directly correspond to rows in the output.

### Examples

Here are some examples illustrating how vertices are assigned to layers:

#### Single Vertex

A graph with a single vertex will be assigned to Layer 0:

```
A (Layer 0)
```

#### Linear Chain

A simple linear chain `A -> B -> C` will result in increasing layers:

```
A (Layer 0)
|
v
B (Layer 1)
|
v
C (Layer 2)
```

#### Cyclic Graph

If a graph contains a cycle, such as `A <--> B`, an `IllegalArgumentException` will be thrown, and no layers will be
assigned:

```
A <--> B  (Cycle Detected! throws IllegalArgumentException)
```

## Algorithm: Longest-Path Layering from Sources

The `LayerAssigner` utilizes a longest-path layering algorithm, which is a common technique for DAGs.
This algorithm ensures that the layer of a vertex reflects the length of the longest path from any source node to that
vertex.

### Key Steps

1. **Cycle Detection:**
    * Before any layering begins, the algorithm first checks if the input graph contains any cycles using JGraphT's
      `org.jgrapht.alg.cycle.CycleDetector`.
    * If a cycle is detected, an `IllegalArgumentException` is thrown, as layering (and the broader Sugiyama-inspired
      approach) is only well-defined for DAGs.

2. **Initialization:**
    * For each vertex `v` in the graph:
        * Its `in-degree` (number of incoming edges) is calculated and stored.
        * Its initial `layer` is set to `0`. This acts as a baseline for `Math.max` calculations later.

3. **Identify Source Nodes:**
    * All vertices with an `in-degree` of `0` (i.e., source nodes) are identified and added to a processing queue (a
      `Deque`).
      These nodes naturally belong to layer 0.

4. **Breadth-First Search (BFS) Traversal:**
   The algorithm proceeds with a BFS-like traversal, processing nodes in a topological order facilitated by tracking
   in-degrees:

    * Initialize a queue with all source nodes (in-degree 0), setting their layer to 0.
    * While the queue is not empty:
        * Dequeue a vertex `u`.
        * Let `uLayer` be the layer assigned to `u`.
        * For each outgoing edge from `u` to a successor `v`:
            * **Layer Update:** The layer of `v` is updated to `max(currentLayer[v], uLayer + 1)`. This is the core of
              the longest-path assignment, ensuring `v` is placed on the deepest possible layer relative to its
              predecessors.
            * **In-degree Decrement:** The `in-degree` of `v` is decremented.
            * **Queue Addition:** If `v`'s `in-degree` becomes `0`, it means all of `v`'s predecessors have been
              processed, and `v` is now ready to be added to the queue.

**Example Walkthrough: Diamond DAG (A→B, A→C, B→D, C→D)**

Let's trace the layering for a diamond DAG:
```
 A (Layer 0)
/ \
v   v
B (L1) C (L1)
\ /
 v
 D (Layer 2)
```

1. **Initial State:**
    * `inDegrees`: A=0, B=1, C=1, D=2
    * `layers`: All initialized to 0.
    * `Queue`: `[A]` (only A has in-degree 0)

2. **Process A (Layer 0):**
    * Successors B and C are processed. Their layers become 1 (`max(0, 0+1)`).
    * `inDegrees` for B and C become 0.
    * `Queue`: `[B, C]`

3. **Process B (Layer 1):**
    * Successor D is processed. Its layer becomes 2 (`max(0, 1+1)`).
    * `inDegrees` for D becomes 1.
    * `Queue`: `[C]`

4. **Process C (Layer 1):**
    * Successor D is processed. Its layer is already 2. `max(2, 1+1)` remains 2.
    * `inDegrees` for D becomes 0.
    * `Queue`: `[D]`

5. **Process D (Layer 2):**
    * D has no successors.
    * `Queue`: `[]`

**Final Layers:** A=0, B=1, C=1, D=2, matching the visualization.

## Output

The `assignLayers` method returns a `Map<V, Integer>`, where `V` is a vertex from the input graph and `Integer` is the
assigned layer number for that vertex.
Layer numbers start from `0`.

## Constraints

* The `LayerAssigner` currently **only supports Directed Acyclic Graphs (DAGs)**.
  Graphs containing cycles will result in an `IllegalArgumentException`.
