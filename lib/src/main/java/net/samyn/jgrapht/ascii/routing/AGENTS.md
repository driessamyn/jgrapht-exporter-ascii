<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-02-28 | Updated: 2026-02-28 -->

# routing

## Purpose
Orthogonal edge routing that computes waypoint paths between connected vertices, avoiding obstacle vertex boxes. Produces `GridEdge` instances with ordered (x, y) waypoints for the renderer to draw.

## Key Files

| File | Description |
|------|-------------|
| `EdgeRouter.java` | Strategy interface: `routeEdges(Graph, GridModel, obstacles) -> List<GridEdge>` |
| `OrthogonalEdgeRouter.java` | Main router; exits from source bottom-centre, enters at target top-centre. Handles straight vertical paths and bent paths with horizontal segments. Detours around obstacles using channel routing. |
| `ObstacleDetector.java` | Collision detection between path segments and vertex boxes; computes detour columns. Package-private, separated for testability. |
| `LaneTracker.java` | Tracks claimed horizontal lanes (row + x-range) to spread parallel edge segments across different rows, avoiding visual overlap. Package-private. |

## For AI Agents

### Working In This Directory
- `OrthogonalEdgeRouter` is the only public class; `ObstacleDetector` and `LaneTracker` are package-private
- Edges are sorted deterministically before routing for stable output
- Routing algorithm:
  1. Compute exit/entry points (centre of source bottom / target top)
  2. If aligned: straight vertical path with obstacle detours
  3. If offset: bent path (vertical + horizontal + vertical) with obstacle/lane awareness
- `LaneTracker.findFreeRow()` searches downward for an unclaimed row in a given range
- `ObstacleDetector.pickDetourColumn()` prefers the side closer to the target, widening search if blocked

### Testing Requirements
- Test straight and bent paths separately
- Test obstacle avoidance with vertices blocking direct paths
- Test `LaneTracker` lane claiming and free row search
- Test `ObstacleDetector` collision detection and detour column selection

### Common Patterns
- Strategy pattern for `EdgeRouter`
- Collision detection separated from path construction for testability
- Deterministic edge ordering for reproducible output

## Dependencies

### Internal
- `model/GridModel`, `model/GridVertex`, `model/GridEdge` - input/output data structures

### External
- `org.jgrapht:jgrapht-core` - `Graph` interface for edge iteration

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
