<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# model

## Purpose
Data model classes representing the grid layout and rendering surface. Contains positioned vertices, routed edges, the character canvas, and Unicode display width calculation.

## Key Files

| File | Description |
|------|-------------|
| `GridModel.java` | Immutable container for positioned `GridVertex` and `GridEdge` lists; provides `verticesByLayer()` sorting |
| `GridVertex.java` | A vertex with (x, y) position, label, computed box dimensions (width = label display width + padding + borders, height = 3) |
| `GridEdge.java` | An edge with source/target vertices and an ordered waypoint path (list of `{x, y}` pairs) |
| `Canvas.java` | Mutable 2D character grid for rendering; auto-expands, supports precedence-based character overwriting for line/junction/corner/arrow compositing |
| `DisplayWidth.java` | Unicode-aware display width calculator; handles wide (East Asian), combining marks, zero-width characters |

## For AI Agents

### Working In This Directory
- `GridVertex` computes box dimensions from label width: `width = DisplayWidth.width(label) + 4` (2 padding + 2 border)
- `GridModel` uses `List.copyOf()` for immutability
- `Canvas.putCharWithPrecedence()` implements character compositing rules (lines < corners/junctions < arrows)
- `Canvas.toString()` trims trailing spaces and blank rows
- `DisplayWidth` approximates `wcwidth()` for terminal rendering

### Testing Requirements
- Test `Canvas` character precedence rules (line crossing, junction overwriting)
- Test `DisplayWidth` with ASCII, CJK, emoji, combining marks, zero-width characters
- Test `GridVertex` dimension calculations with various label lengths

### Common Patterns
- Value semantics: `GridVertex` implements `equals`/`hashCode`; `GridEdge` does not (path contains mutable arrays)
- `Canvas` is the only mutable class; everything else is effectively immutable after construction

## Dependencies

### Internal
- No internal dependencies (leaf package)

### External
- None (pure Java)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
