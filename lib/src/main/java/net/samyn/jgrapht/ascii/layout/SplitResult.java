package net.samyn.jgrapht.ascii.layout;

import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Result of splitting long edges into single-layer segments. Contains the augmented graph (with
 * dummy vertices inserted), the updated layer map, and the set of dummy vertices.
 *
 * @param <V> the original vertex type
 */
final class SplitResult<V> {

  private final Graph<Object, DefaultEdge> graph;
  private final Map<Object, Integer> layers;
  private final Set<Object> dummyVertices;

  SplitResult(
      Graph<Object, DefaultEdge> graph, Map<Object, Integer> layers, Set<Object> dummyVertices) {
    this.graph = graph;
    this.layers = layers;
    this.dummyVertices = dummyVertices;
  }

  Graph<Object, DefaultEdge> graph() {
    return graph;
  }

  Map<Object, Integer> layers() {
    return layers;
  }

  Set<Object> dummyVertices() {
    return dummyVertices;
  }

  boolean isDummy(Object vertex) {
    return dummyVertices.contains(vertex);
  }
}
