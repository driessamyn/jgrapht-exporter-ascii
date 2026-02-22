# Grid Coordinate Assignment in jgrapht-exporter-ascii

This document explains the grid coordinate assignment algorithm implemented in the `SugiyamaLayoutAlgorithm` class,
which converts abstract layer and ordering information into concrete (x, y) canvas positions for each vertex.

## Purpose

After [layer assignment](LAYERING.md), [long edge splitting](LONG_EDGE_SPLITTING.md), and
[crossing minimisation](CROSSING_MINIMISATION.md), we know:
- Which layer each vertex belongs to (its row in the output)
- The order of vertices within each layer (minimising edge crossings)

What we don't yet know is where each vertex should be drawn on the character grid. Coordinate assignment translates the
abstract layer/position information into pixel-level (character-level) coordinates, taking into account:
- Vertex label widths (different labels produce different box sizes)
- Horizontal spacing between vertices in the same layer
- Vertical spacing between layers (leaving room for edge routing)
- Centring of narrower layers within the overall canvas width

## Algorithm

The `SugiyamaLayoutAlgorithm` orchestrates the full Sugiyama-inspired pipeline:

```
Graph<V,E>
    │
    ▼
┌───────────────────────┐
│  1. Layer Assignment  │  → Map<V, Integer> (vertex → layer number)
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

### Step 1: Compute Vertex Dimensions

Before placing vertices, we need to know how large each box will be. A vertex box looks like:

```
┌───────┐
│ label │
└───────┘
```

The box width is: `displayWidth(label) + 2 (padding) + 2 (borders)`
The box height is always 3: top border, label row, bottom border.

Label width uses `DisplayWidth.width()` which correctly handles Unicode characters, including wide East Asian characters
and emoji.

### Step 2: Find the Maximum Layer Width

To centre all layers, we first compute the total width of each layer (sum of vertex widths plus gaps between them) and
find the maximum:

```
Layer 0:  [  A  ]                          width = 5
Layer 1:  [  B  ]  [  C  ]                 width = 5 + 2 + 5 = 12  ← max
Layer 2:  [  D  ]                          width = 5
```

### Step 3: Place Vertices

For each layer, we centre it horizontally within the maximum layer width:

```
startX = (maxLayerWidth - thisLayerWidth) / 2
```

Vertices are placed left to right within each layer, separated by a horizontal gap (2 characters). Layers are stacked
top to bottom, separated by a vertical gap (2 characters) to leave room for edge routing.

### Example Walkthrough: Diamond DAG

Given the diamond graph A→B, A→C, B→D, C→D with layers A=0, {B,C}=1, D=2:

**Vertex dimensions** (assuming single-character labels):
- Each box is 5 characters wide (1 + 2 + 2), 3 characters tall

**Layer widths:**
- Layer 0 (A only): 5
- Layer 1 (B, C): 5 + 2 + 5 = 12
- Layer 2 (D only): 5

**Max layer width:** 12

**Coordinate assignment:**

| Vertex | Layer | startX            | x | y  |
|--------|-------|-------------------|---|----|
| A      | 0     | (12 - 5) / 2 = 3  | 3 | 0  |
| B      | 1     | (12 - 12) / 2 = 0 | 0 | 5  |
| C      | 1     | (after B)         | 7 | 5  |
| D      | 2     | (12 - 5) / 2 = 3  | 3 | 10 |

**Y spacing:** Each layer starts at `previousY + boxHeight + gap` = `previousY + 3 + 2 = previousY + 5`.

**Result on the canvas:**

```
   ┌───┐
   │ A │
   └───┘

┌───┐ ┌───┐
│ B │ │ C │
└───┘ └───┘

   ┌───┐
   │ D │
   └───┘
```

## Configuration

The algorithm uses two spacing constants:

| Constant     | Value | Purpose                                           |
|--------------|-------|---------------------------------------------------|
| `LAYER_GAP`  | 2     | Vertical gap between layers (for edge routing)    |
| `VERTEX_GAP` | 2     | Horizontal gap between vertices in the same layer |

## Output

The `layout` method returns a `GridModel` containing a list of `GridVertex` objects, each with:
- `label()` — the display text
- `x()` — the left column position on the canvas
- `y()` — the top row position on the canvas
- `width()` — the total box width (computed from the label)
- `height()` — the total box height (always 3)

## Related

- [Long Edge Splitting](LONG_EDGE_SPLITTING.md) — how edges spanning multiple layers are handled before
  crossing minimisation
