<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# render

## Purpose
Canvas renderers that draw vertex boxes and edge routes onto a `Canvas`. Provides two implementations: Unicode box-drawing characters (default) and plain ASCII.

## Key Files

| File | Description |
|------|-------------|
| `CanvasRenderer.java` | Strategy interface with `renderVertex(Canvas, GridVertex)` and `renderEdge(Canvas, GridEdge)` |
| `UnicodeBoxRenderer.java` | Renders boxes with Unicode characters (`┌─┐│└─┘`), edges with `│─` and corner characters (`┐┌└┘`), T-junctions (`┬`), arrows (`v`) |
| `AsciiBoxRenderer.java` | Renders boxes with `+`, `-`, `|`; edges with `|`, `-`, `+`; arrows with `v` |

## For AI Agents

### Working In This Directory
- Both renderers follow the same algorithm: draw box borders, label, then edge segments with corners
- Edge rendering: first waypoint gets a T-junction on the source bottom border, intermediate bends get corner characters, last waypoint gets a `v` arrow
- `UnicodeBoxRenderer` uses `DisplayWidth.width()` for label positioning; `AsciiBoxRenderer` uses `String.length()`
- `Canvas.putCharWithPrecedence()` handles overlapping edge/border characters

### Testing Requirements
- Test both renderers produce correct output for simple and multi-edge graphs
- Verify Unicode corner characters are placed correctly at bend points
- Test edge junction handling when multiple edges exit the same vertex

### Common Patterns
- Strategy pattern: swap renderers via `AsciiExporter.builder().renderer()`
- Rendering is stateless; renderers can be shared across threads

## Dependencies

### Internal
- `model/Canvas` - rendering target
- `model/GridVertex`, `model/GridEdge` - input data
- `model/DisplayWidth` - used by `UnicodeBoxRenderer` for label width

### External
- None (pure Java)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
