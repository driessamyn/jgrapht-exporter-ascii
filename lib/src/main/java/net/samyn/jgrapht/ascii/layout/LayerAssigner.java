package net.samyn.jgrapht.ascii.layout;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;

/**
 * Assigns each vertex in a directed acyclic graph (DAG) to an integer layer using a longest-path
 * algorithm. Source vertices (in-degree 0) are placed at layer 0, and each subsequent vertex is
 * placed at one plus the maximum layer of its predecessors.
 *
 * <p>The algorithm uses a modified Kahn's topological sort (BFS with in-degree tracking) to
 * propagate layer values through the graph in a single pass.
 */
public class LayerAssigner<V, E> {

  /**
   * Assigns layers to all vertices in the given graph.
   *
   * @param graph a directed acyclic graph; must not be {@code null}
   * @return a map from each vertex to its assigned layer (0-indexed from sources)
   * @throws IllegalArgumentException if the graph is {@code null}, undirected, or contains cycles
   */
  public Map<V, Integer> assignLayers(Graph<V, E> graph) {
    Map<V, Integer> layers = new HashMap<>();
    if (graph == null) {
      throw new IllegalArgumentException("Input graph cannot be null.");
    }

    if (!graph.getType().isDirected()) {
      throw new IllegalArgumentException(
          "Graph must be directed. Undirected or mixed graphs are not supported.");
    }

    if (graph.vertexSet().isEmpty()) {
      return layers;
    }

    Set<V> cycleVertices = new CycleDetector<>(graph).findCycles();
    if (!cycleVertices.isEmpty()) {
      throw new IllegalArgumentException(
          "Graph contains cycles. Layer assignment is only supported for DAGs. "
              + "Vertices involved in cycles: "
              + cycleVertices);
    }

    Map<V, Integer> inDegrees = new HashMap<>();
    for (V vertex : graph.vertexSet()) {
      inDegrees.put(vertex, graph.inDegreeOf(vertex));
      layers.put(vertex, 0); // Initialize all layers to 0 for proper max calculation
    }

    Deque<V> queue = new ArrayDeque<>();
    // Add all source nodes (in-degree 0) to the queue and set their layer to 0
    for (V vertex : graph.vertexSet()) {
      if (inDegrees.get(vertex) == 0) {
        queue.addLast(vertex);
      }
    }

    while (!queue.isEmpty()) {
      V u = queue.removeFirst();
      int uLayer = layers.get(u);

      for (E edge : graph.outgoingEdgesOf(u)) {
        V v = graph.getEdgeTarget(edge);

        // Update layer of v if a longer path is found
        // The layer of v is the maximum of (layer of predecessor + 1)
        layers.put(v, Math.max(layers.get(v), uLayer + 1));

        // Decrement in-degree of v. If it becomes 0, all its predecessors have been processed.
        inDegrees.put(v, inDegrees.get(v) - 1);
        if (inDegrees.get(v) == 0) {
          queue.addLast(v);
        }
      }
    }

    return layers;
  }
}
