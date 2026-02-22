package net.samyn.jgrapht.ascii.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Splits long edges (spanning more than one layer) into chains of single-layer edges by inserting
 * dummy vertices at intermediate layers. This is a standard step in the Sugiyama framework,
 * required before crossing minimisation.
 *
 * @param <V> the original vertex type
 * @param <E> the original edge type
 */
final class LongEdgeSplitter<V, E> {

  /**
   * Builds an augmented graph where every edge spans exactly one layer.
   *
   * @param graph the original directed graph; must not be {@code null}
   * @param layers a map from each vertex to its assigned layer; must not be {@code null} and must
   *     contain an entry for every vertex in the graph
   * @return a {@link SplitResult} containing the augmented graph, layer map, and dummy vertex set
   * @throws IllegalArgumentException if {@code graph} or {@code layers} is {@code null}, if any
   *     vertex lacks a layer assignment, or if any edge has a non-positive layer span
   */
  SplitResult<V> splitLongEdges(Graph<V, E> graph, Map<V, Integer> layers) {
    if (graph == null) {
      throw new IllegalArgumentException("Input graph must not be null.");
    }
    if (layers == null) {
      throw new IllegalArgumentException("Layer assignment map must not be null.");
    }

    Graph<Object, DefaultEdge> augmented = new DefaultDirectedGraph<>(DefaultEdge.class);
    Map<Object, Integer> augmentedLayers = new HashMap<>();
    Set<Object> dummyVertices = new HashSet<>();

    // Add all original vertices, validating layer assignments
    for (V vertex : graph.vertexSet()) {
      Integer layer = layers.get(vertex);
      if (layer == null) {
        throw new IllegalArgumentException(
            "Vertex " + vertex + " has no layer assignment in the provided map.");
      }
      augmented.addVertex(vertex);
      augmentedLayers.put(vertex, layer);
    }

    // Sort edges deterministically so dummy vertex IDs are stable across runs
    List<E> sortedEdges = new ArrayList<>(graph.edgeSet());
    sortedEdges.sort(
        Comparator.comparingInt((E e) -> layers.get(graph.getEdgeSource(e)))
            .thenComparingInt(e -> layers.get(graph.getEdgeTarget(e)))
            .thenComparing(e -> graph.getEdgeSource(e).toString())
            .thenComparing(e -> graph.getEdgeTarget(e).toString()));

    // Process each edge
    for (E edge : sortedEdges) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);
      int sourceLayer = layers.get(source);
      int targetLayer = layers.get(target);
      int span = targetLayer - sourceLayer;

      if (span <= 0) {
        throw new IllegalArgumentException(
            "Edge "
                + source
                + " (layer "
                + sourceLayer
                + ") -> "
                + target
                + " (layer "
                + targetLayer
                + ") has non-positive span "
                + span
                + ". Target layer must be greater than source layer.");
      }

      if (span == 1) {
        // Short edge — add directly
        augmented.addEdge(source, target);
      } else {
        // Long edge — insert dummy vertices at intermediate layers
        Object previous = source;
        for (int layer = sourceLayer + 1; layer < targetLayer; layer++) {
          DummyVertex dummy = new DummyVertex(source.toString(), target.toString(), layer);
          augmented.addVertex(dummy);
          augmentedLayers.put(dummy, layer);
          dummyVertices.add(dummy);
          augmented.addEdge(previous, dummy);
          previous = dummy;
        }
        augmented.addEdge(previous, target);
      }
    }

    return new SplitResult<>(augmented, augmentedLayers, dummyVertices);
  }
}
