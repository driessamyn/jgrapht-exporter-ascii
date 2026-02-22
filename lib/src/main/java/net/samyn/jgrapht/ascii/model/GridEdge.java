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
  private final GridVertex<V> sourceGridVertex;
  private final GridVertex<V> targetGridVertex;
  private final List<int[]> path;

  /**
   * Creates a new grid edge.
   *
   * @param source the source vertex (from original graph)
   * @param target the target vertex (from original graph)
   * @param sourceGridVertex the GridVertex object for the source
   * @param targetGridVertex the GridVertex object for the target
   * @param path ordered sequence of (x, y) waypoints forming the edge route
   */
  public GridEdge(
      V source,
      V target,
      GridVertex<V> sourceGridVertex,
      GridVertex<V> targetGridVertex,
      List<int[]> path) {
    this.source = Objects.requireNonNull(source, "Source must not be null.");
    this.target = Objects.requireNonNull(target, "Target must not be null.");
    this.sourceGridVertex =
        Objects.requireNonNull(
            sourceGridVertex, "Source GridVertex must not be null."); // Init new field
    this.targetGridVertex =
        Objects.requireNonNull(
            targetGridVertex, "Target GridVertex must not be null."); // Init new field
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

  /** Returns the source vertex from the original graph. */
  public V source() {
    return source;
  }

  /** Returns the target vertex from the original graph. */
  public V target() {
    return target;
  }

  /** Returns the GridVertex object for the source. */
  public GridVertex<V> sourceGridVertex() {
    return sourceGridVertex;
  }

  /** Returns the GridVertex object for the target. */
  public GridVertex<V> targetGridVertex() {
    return targetGridVertex;
  }

  /** Returns the ordered waypoints forming the edge route. Each waypoint is an {x, y} pair. */
  public List<int[]> path() {
    return path;
  }
}
