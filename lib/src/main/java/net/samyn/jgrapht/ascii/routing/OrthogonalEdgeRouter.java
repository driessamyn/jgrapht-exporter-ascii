package net.samyn.jgrapht.ascii.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridModel;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.jgrapht.Graph;

/**
 * Computes orthogonal edge routes between connected vertices. Edges exit from the centre of the
 * source vertex's bottom border and enter at the centre of the target vertex's top border.
 *
 * <p>Straight edges (aligned vertices) produce a simple vertical path. Offset edges include a
 * horizontal bend one row below the source vertex.
 *
 * <p>When obstacles block a path segment, the router detours through the horizontal and vertical
 * channels that exist between vertex boxes in the Sugiyama layout grid.
 */
public class OrthogonalEdgeRouter implements EdgeRouter {

  /** Height of a vertex box (top border + label row + bottom border). */
  private static final int BOX_HEIGHT = 3;

  @Override
  public <V> List<GridEdge<V>> routeEdges(
      Graph<V, ?> graph, GridModel<V> model, List<GridVertex<V>> obstacles) {
    if (graph == null) {
      throw new IllegalArgumentException("Graph must not be null.");
    }
    if (model == null) {
      throw new IllegalArgumentException("Model must not be null.");
    }
    if (obstacles == null) {
      throw new IllegalArgumentException("Obstacles must not be null.");
    }

    return doRouteEdges(graph, model, obstacles);
  }

  private <V, E> List<GridEdge<V>> doRouteEdges(
      Graph<V, E> graph, GridModel<V> model, List<GridVertex<V>> obstacles) {
    Map<V, GridVertex<V>> vertexMap = new HashMap<>();
    for (GridVertex<V> gv : model.vertices()) {
      vertexMap.put(gv.vertex(), gv);
    }

    // Sort edges by source then target coordinates for stable, deterministic routing order.
    // This ensures lane assignment is consistent regardless of graph.edgeSet() iteration order.
    List<E> sortedEdges = new ArrayList<>(graph.edgeSet());
    sortedEdges.sort(
        (a, b) -> {
          GridVertex<V> srcA = vertexMap.get(graph.getEdgeSource(a));
          GridVertex<V> srcB = vertexMap.get(graph.getEdgeSource(b));
          GridVertex<V> tgtA = vertexMap.get(graph.getEdgeTarget(a));
          GridVertex<V> tgtB = vertexMap.get(graph.getEdgeTarget(b));
          int cmp = Integer.compare(srcA.y(), srcB.y());
          if (cmp != 0) return cmp;
          cmp = Integer.compare(srcA.x(), srcB.x());
          if (cmp != 0) return cmp;
          cmp = Integer.compare(tgtA.y(), tgtB.y());
          if (cmp != 0) return cmp;
          return Integer.compare(tgtA.x(), tgtB.x());
        });

    LaneTracker laneTracker = new LaneTracker();
    List<GridEdge<V>> edges = new ArrayList<>();
    for (E edge : sortedEdges) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);

      GridVertex<V> sourceVertex = vertexMap.get(source);
      GridVertex<V> targetVertex = vertexMap.get(target);
      if (sourceVertex == null) {
        throw new IllegalStateException("Source vertex '" + source + "' not found in grid model.");
      }
      if (targetVertex == null) {
        throw new IllegalStateException("Target vertex '" + target + "' not found in grid model.");
      }

      int exitX = sourceVertex.x() + sourceVertex.width() / 2;
      int exitY = sourceVertex.y() + 2; // bottom border row
      int entryX = targetVertex.x() + targetVertex.width() / 2;
      int entryY = targetVertex.y(); // top border row

      // Filter obstacles to exclude source and target of the current edge
      List<GridVertex<V>> relevantObstacles = new ArrayList<>();
      for (GridVertex<V> obst : obstacles) {
        if (!obst.vertex().equals(source) && !obst.vertex().equals(target)) {
          relevantObstacles.add(obst);
        }
      }

      List<int[]> path =
          findPath(
              exitX, exitY, entryX, entryY, new ObstacleDetector<>(relevantObstacles), laneTracker);
      claimHorizontalSegments(laneTracker, path);
      edges.add(new GridEdge<>(source, target, sourceVertex, targetVertex, path));
    }

    return edges;
  }

  private <V> List<int[]> findPath(
      int startX,
      int startY,
      int endX,
      int endY,
      ObstacleDetector<V> detector,
      LaneTracker laneTracker) {

    List<int[]> path = new ArrayList<>();
    path.add(new int[] {startX, startY});

    int adjustedEndY = endY - 1;

    if (startX == endX) {
      // Straight vertical path — walk top-down, detouring around each obstacle
      buildVerticalPath(path, startX, startY, adjustedEndY, endX, detector, laneTracker);
    } else {
      // Bent path — horizontal bend between source and target columns
      buildBentPath(path, startX, startY, endX, adjustedEndY, detector, laneTracker);
    }

    return path;
  }

  /** Registers all horizontal segments in the path with the lane tracker. */
  private void claimHorizontalSegments(LaneTracker tracker, List<int[]> path) {
    for (int i = 0; i < path.size() - 1; i++) {
      int[] a = path.get(i);
      int[] b = path.get(i + 1);
      if (a[1] == b[1] && a[0] != b[0]) {
        int minX = Math.min(a[0], b[0]);
        int maxX = Math.max(a[0], b[0]);
        tracker.claim(a[1], minX, maxX);
      }
    }
  }

  /**
   * Builds a vertical path from currentY down to targetY at column x, detouring around each
   * obstacle encountered along the way.
   */
  private <V> void buildVerticalPath(
      List<int[]> path,
      int x,
      int startY,
      int targetY,
      int finalX,
      ObstacleDetector<V> detector,
      LaneTracker laneTracker) {

    List<GridVertex<V>> collisions = detector.findVerticalCollisions(x, startY, targetY);

    if (collisions.isEmpty()) {
      path.add(new int[] {x, targetY});
      return;
    }

    int currentY = startY;
    for (GridVertex<V> obstacle : collisions) {
      int channelAbove = obstacle.y() - 1;
      int channelBelow = obstacle.y() + BOX_HEIGHT;

      // Move down to the channel row above the obstacle
      if (channelAbove > currentY) {
        path.add(new int[] {x, channelAbove});
      }

      // Pick a detour column to go around the obstacle
      int detourX = detector.pickDetourColumn(obstacle, finalX, channelAbove, channelBelow);
      int minDetourX = Math.min(x, detourX);
      int maxDetourX = Math.max(x, detourX);

      // Use lane tracker to find free rows for the horizontal detour segments
      channelAbove =
          laneTracker.findFreeRow(channelAbove, obstacle.y() - 1, minDetourX, maxDetourX);
      channelBelow =
          laneTracker.findFreeRow(
              channelBelow, channelBelow + BOX_HEIGHT - 1, minDetourX, maxDetourX);

      // Horizontal move to detour column
      path.add(new int[] {detourX, channelAbove});
      // Vertical move past the obstacle
      path.add(new int[] {detourX, channelBelow});
      // Horizontal move back to original column
      path.add(new int[] {x, channelBelow});

      currentY = channelBelow;
    }

    // Continue down to the target
    if (targetY > currentY) {
      path.add(new int[] {x, targetY});
    }
  }

  /**
   * Builds a bent path: vertical from source, horizontal bend, then vertical to target. Pushes the
   * bend row down if it collides with obstacles, and detours vertical segments around obstacles.
   */
  private <V> void buildBentPath(
      List<int[]> path,
      int startX,
      int startY,
      int endX,
      int adjustedEndY,
      ObstacleDetector<V> detector,
      LaneTracker laneTracker) {

    int bendY = startY + 1;

    // Check if horizontal segment at bendY collides with any obstacle, and push down if so
    List<GridVertex<V>> hCollisions = detector.findHorizontalCollisions(bendY, startX, endX);
    while (!hCollisions.isEmpty()) {
      // Push bendY below the lowest colliding obstacle
      int maxObstBottom = 0;
      for (GridVertex<V> obst : hCollisions) {
        maxObstBottom = Math.max(maxObstBottom, obst.y() + BOX_HEIGHT);
      }
      bendY = maxObstBottom;
      hCollisions = detector.findHorizontalCollisions(bendY, startX, endX);
    }

    // Ensure bendY doesn't exceed the target
    if (bendY >= adjustedEndY) {
      bendY = adjustedEndY;
    }

    // Use lane tracker to spread horizontal segments across different rows.
    // Re-check obstacle collisions after lane assignment to avoid placing the bend
    // inside a vertex box when findFreeRow searches past the current inter-layer gap.
    int minBendX = Math.min(startX, endX);
    int maxBendX = Math.max(startX, endX);
    bendY = laneTracker.findFreeRow(bendY, adjustedEndY, minBendX, maxBendX);
    List<GridVertex<V>> postLaneCollisions = detector.findHorizontalCollisions(bendY, startX, endX);
    while (!postLaneCollisions.isEmpty()) {
      int maxObstBottom = 0;
      for (GridVertex<V> obst : postLaneCollisions) {
        maxObstBottom = Math.max(maxObstBottom, obst.y() + BOX_HEIGHT);
      }
      bendY = laneTracker.findFreeRow(maxObstBottom, adjustedEndY, minBendX, maxBendX);
      postLaneCollisions = detector.findHorizontalCollisions(bendY, startX, endX);
    }

    // Build the exit-side vertical segment (startX, startY) -> (startX, bendY)
    List<GridVertex<V>> exitCollisions = detector.findVerticalCollisions(startX, startY, bendY);
    if (!exitCollisions.isEmpty()) {
      // Detour around obstacles on the exit-side vertical
      for (GridVertex<V> obstacle : exitCollisions) {
        int channelAbove = obstacle.y() - 1;
        int channelBelow = obstacle.y() + BOX_HEIGHT;

        if (channelAbove > startY) {
          path.add(new int[] {startX, channelAbove});
        }

        int detourX = detector.pickDetourColumn(obstacle, endX, channelAbove, channelBelow);
        path.add(new int[] {detourX, channelAbove});
        path.add(new int[] {detourX, channelBelow});
        path.add(new int[] {startX, channelBelow});
      }
      // After detours, the last point is at startX, below the last obstacle
      // Add bend point if we haven't reached it yet
      int lastY = path.get(path.size() - 1)[1];
      if (lastY < bendY) {
        path.add(new int[] {startX, bendY});
      }
    } else {
      path.add(new int[] {startX, bendY});
    }

    // Horizontal segment to endX
    path.add(new int[] {endX, bendY});

    // Build the entry-side vertical segment (endX, bendY) -> (endX, adjustedEndY)
    if (bendY < adjustedEndY) {
      List<GridVertex<V>> entryCollisions =
          detector.findVerticalCollisions(endX, bendY, adjustedEndY);
      if (!entryCollisions.isEmpty()) {
        int currentY = bendY;
        for (GridVertex<V> obstacle : entryCollisions) {
          int channelAbove = obstacle.y() - 1;
          int channelBelow = obstacle.y() + BOX_HEIGHT;

          if (channelAbove > currentY) {
            path.add(new int[] {endX, channelAbove});
          }

          int detourX = detector.pickDetourColumn(obstacle, startX, channelAbove, channelBelow);
          path.add(new int[] {detourX, channelAbove});
          path.add(new int[] {detourX, channelBelow});
          path.add(new int[] {endX, channelBelow});

          currentY = channelBelow;
        }
        if (adjustedEndY > currentY) {
          path.add(new int[] {endX, adjustedEndY});
        }
      } else {
        path.add(new int[] {endX, adjustedEndY});
      }
    }
  }
}
