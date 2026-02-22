package net.samyn.jgrapht.ascii.model;

import java.util.List;
import java.util.Objects;

/**
 * An edge positioned on the grid with an ordered sequence of waypoints forming the route between
 * source and target vertices. The first waypoint is the exit point (centre bottom of the source
 * box) and the last waypoint is the entry point (centre top of the target box). Intermediate
 * waypoints are bend points.
 *
 * @param <V> the graph vertex type
 */
public final class GridEdge<V> {

  private final V source;
  private final V target;
  private final List<int[]> path;

  /**
   * Creates a new grid edge.
   *
   * @param source the source vertex
   * @param target the target vertex
   * @param path ordered sequence of (x, y) waypoints forming the edge route
   */
  public GridEdge(V source, V target, List<int[]> path) {
    this.source = Objects.requireNonNull(source, "Source must not be null.");
    this.target = Objects.requireNonNull(target, "Target must not be null.");
    Objects.requireNonNull(path, "Path must not be null.");
    for (int i = 0; i < path.size(); i++) {
      int[] waypoint = path.get(i);
      if (waypoint == null || waypoint.length != 2) {
        throw new IllegalArgumentException(
            "Waypoint at index " + i + " must be a non-null {x, y} pair.");
      }
    }
    this.path = List.copyOf(path);
  }

  /** Returns the source vertex. */
  public V source() {
    return source;
  }

  /** Returns the target vertex. */
  public V target() {
    return target;
  }

  /** Returns the ordered waypoints forming the edge route. Each waypoint is an {x, y} pair. */
  public List<int[]> path() {
    return path;
  }
}
