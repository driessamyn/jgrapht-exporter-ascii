package net.samyn.jgrapht.ascii.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Holds the positioned vertices on the grid after layout computation. Each vertex has been assigned
 * concrete (x, y) canvas coordinates and carries the original graph vertex.
 *
 * @param <V> the graph vertex type
 */
public final class GridModel<V> {

  private final List<GridVertex<V>> vertices;
  private final List<GridEdge<V>> edges;

  public GridModel(List<GridVertex<V>> vertices) {
    this(vertices, List.of());
  }

  public GridModel(List<GridVertex<V>> vertices, List<GridEdge<V>> edges) {
    this.vertices = vertices != null ? List.copyOf(vertices) : List.of();
    this.edges = edges != null ? List.copyOf(edges) : List.of();
  }

  /** Returns the positioned vertices. */
  public List<GridVertex<V>> vertices() {
    return vertices;
  }

  /** Returns the routed edges. */
  public List<GridEdge<V>> edges() {
    return edges;
  }

  /** Returns vertices sorted by layer (y position), then by x position within each layer. */
  public List<GridVertex<V>> verticesByLayer() {
    List<GridVertex<V>> sorted = new ArrayList<>(vertices);
    sorted.sort(Comparator.comparingInt(GridVertex<V>::y).thenComparingInt(GridVertex::x));
    return sorted;
  }
}
