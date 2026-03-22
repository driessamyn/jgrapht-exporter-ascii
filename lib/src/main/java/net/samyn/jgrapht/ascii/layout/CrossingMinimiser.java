package net.samyn.jgrapht.ascii.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;

/**
 * Reduces edge crossings between adjacent layers using the barycenter heuristic with iterative
 * sweeping. Vertices within each layer are reordered so that each vertex is placed at the average
 * (barycentre) position of its neighbours in the adjacent fixed layer.
 *
 * <p>The algorithm performs alternating top-down and bottom-up sweeps, repeating until no further
 * improvement is found or the maximum number of iterations is reached.
 *
 * <p><b>Performance note:</b> Crossing counting uses pairwise edge comparison (O(m&sup2;) per layer
 * pair) and is recomputed on each iteration. This is adequate for the intended use case of ASCII
 * terminal rendering, where layers typically contain fewer than 20 vertices and edges per layer
 * pair are in the low hundreds at most. For significantly larger graphs, the crossing count could
 * be replaced with an O(m log m) algorithm based on merge-sort inversion counting.
 */
public class CrossingMinimiser<V, E> {

  private static final int MAX_ITERATIONS = 24;

  /**
   * Reorders vertices within each layer to minimise edge crossings.
   *
   * @param graph the directed graph; must not be {@code null}
   * @param layers a map from each vertex to its assigned layer (0-indexed); must not be {@code
   *     null}. All edges must span exactly one layer (i.e. {@code layer(target) == layer(source) +
   *     1}). Long edges that skip layers must be split into single-layer segments before calling
   *     this method.
   * @return an ordered list of layers, where each layer is an ordered list of vertices
   * @throws IllegalArgumentException if {@code graph} or {@code layers} is {@code null}, or if any
   *     edge spans more than one layer
   */
  public List<List<V>> minimiseCrossings(Graph<V, E> graph, Map<V, Integer> layers) {
    if (graph == null) {
      throw new IllegalArgumentException("Input graph must not be null.");
    }
    if (layers == null) {
      throw new IllegalArgumentException("Layer assignment map must not be null.");
    }
    if (layers.isEmpty()) {
      return List.of();
    }

    validateAdjacentLayerEdges(graph, layers);

    int layerCount = layers.values().stream().mapToInt(i -> i).max().orElse(0) + 1;
    List<List<V>> orderedLayers = new ArrayList<>();
    for (int i = 0; i < layerCount; i++) {
      orderedLayers.add(new ArrayList<>());
    }
    for (Map.Entry<V, Integer> entry : layers.entrySet()) {
      orderedLayers.get(entry.getValue()).add(entry.getKey());
    }

    // Sort each layer initially by natural order for deterministic output
    for (List<V> layer : orderedLayers) {
      layer.sort(Comparator.comparing(Object::toString));
    }

    int bestCrossings = countAllCrossings(graph, orderedLayers);
    List<List<V>> bestOrder = deepCopy(orderedLayers);

    for (int iter = 0; iter < MAX_ITERATIONS && bestCrossings > 0; iter++) {
      // Top-down sweep: fix layer i, reorder layer i+1
      for (int i = 0; i < layerCount - 1; i++) {
        reorderByBarycentre(graph, orderedLayers, i, i + 1, true);
      }

      // Bottom-up sweep: fix layer i, reorder layer i-1
      for (int i = layerCount - 1; i > 0; i--) {
        reorderByBarycentre(graph, orderedLayers, i, i - 1, false);
      }

      int currentCrossings = countAllCrossings(graph, orderedLayers);
      if (currentCrossings < bestCrossings) {
        bestCrossings = currentCrossings;
        bestOrder = deepCopy(orderedLayers);
      } else {
        // No improvement — restore best and stop
        orderedLayers = deepCopy(bestOrder);
        break;
      }
    }

    return bestOrder;
  }

  /**
   * Validates that every edge in the graph spans exactly one layer. Long edges (where the target is
   * more than one layer below the source) are not supported and must be split into single-layer
   * segments before crossing minimisation.
   */
  private void validateAdjacentLayerEdges(Graph<V, E> graph, Map<V, Integer> layers) {
    for (E edge : graph.edgeSet()) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);
      Integer sourceLayer = layers.get(source);
      Integer targetLayer = layers.get(target);
      if (sourceLayer == null || targetLayer == null) {
        throw new IllegalArgumentException(
            "Edge "
                + source
                + " -> "
                + target
                + " references a vertex without a layer assignment.");
      }
      int span = targetLayer - sourceLayer;
      if (span != 1) {
        throw new IllegalArgumentException(
            "Edge "
                + source
                + " (layer "
                + sourceLayer
                + ") -> "
                + target
                + " (layer "
                + targetLayer
                + ") spans "
                + span
                + " layers. All edges must span exactly one layer. "
                + "Long edges must be split into single-layer segments before crossing minimisation.");
      }
    }
  }

  /**
   * Reorders the free layer based on barycentre values computed from the fixed layer. For a
   * top-down sweep, the fixed layer is the upper layer and we look at incoming edges. For a
   * bottom-up sweep, the fixed layer is the lower layer and we look at outgoing edges.
   */
  private void reorderByBarycentre(
      Graph<V, E> graph,
      List<List<V>> orderedLayers,
      int fixedLayerIdx,
      int freeLayerIdx,
      boolean topDown) {
    List<V> fixedLayer = orderedLayers.get(fixedLayerIdx);
    List<V> freeLayer = orderedLayers.get(freeLayerIdx);

    Map<V, Integer> fixedPositions = new HashMap<>();
    for (int i = 0; i < fixedLayer.size(); i++) {
      fixedPositions.put(fixedLayer.get(i), i);
    }

    Map<V, Integer> freePositions = new HashMap<>();
    for (int i = 0; i < freeLayer.size(); i++) {
      freePositions.put(freeLayer.get(i), i);
    }

    Map<V, Double> barycentres = new HashMap<>();
    for (V vertex : freeLayer) {
      List<Integer> neighbourPositions = new ArrayList<>();

      if (topDown) {
        // Free layer is below fixed layer — look at incoming edges from fixed layer
        for (E edge : graph.incomingEdgesOf(vertex)) {
          V source = graph.getEdgeSource(edge);
          if (fixedPositions.containsKey(source)) {
            neighbourPositions.add(fixedPositions.get(source));
          }
        }
      } else {
        // Free layer is above fixed layer — look at outgoing edges to fixed layer
        for (E edge : graph.outgoingEdgesOf(vertex)) {
          V target = graph.getEdgeTarget(edge);
          if (fixedPositions.containsKey(target)) {
            neighbourPositions.add(fixedPositions.get(target));
          }
        }
      }

      if (neighbourPositions.isEmpty()) {
        // No neighbours in fixed layer — keep current position
        barycentres.put(vertex, (double) freePositions.get(vertex));
      } else {
        double avg = neighbourPositions.stream().mapToInt(i -> i).average().orElse(0);
        barycentres.put(vertex, avg);
      }
    }

    freeLayer.sort(Comparator.comparingDouble(barycentres::get));
  }

  private int countAllCrossings(Graph<V, E> graph, List<List<V>> orderedLayers) {
    int total = 0;
    for (int i = 0; i < orderedLayers.size() - 1; i++) {
      total += countLayerCrossings(graph, orderedLayers.get(i), orderedLayers.get(i + 1));
    }
    return total;
  }

  private int countLayerCrossings(Graph<V, E> graph, List<V> upperLayer, List<V> lowerLayer) {
    Map<V, Integer> upperPos = new HashMap<>();
    for (int i = 0; i < upperLayer.size(); i++) {
      upperPos.put(upperLayer.get(i), i);
    }
    Map<V, Integer> lowerPos = new HashMap<>();
    for (int i = 0; i < lowerLayer.size(); i++) {
      lowerPos.put(lowerLayer.get(i), i);
    }

    List<int[]> edges = new ArrayList<>();
    for (E edge : graph.edgeSet()) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);
      if (upperPos.containsKey(source) && lowerPos.containsKey(target)) {
        edges.add(new int[] {upperPos.get(source), lowerPos.get(target)});
      }
    }

    int crossings = 0;
    for (int a = 0; a < edges.size(); a++) {
      for (int b = a + 1; b < edges.size(); b++) {
        int[] e1 = edges.get(a);
        int[] e2 = edges.get(b);
        if ((e1[0] < e2[0] && e1[1] > e2[1]) || (e1[0] > e2[0] && e1[1] < e2[1])) {
          crossings++;
        }
      }
    }
    return crossings;
  }

  private List<List<V>> deepCopy(List<List<V>> layers) {
    List<List<V>> copy = new ArrayList<>();
    for (List<V> layer : layers) {
      copy.add(new ArrayList<>(layer));
    }
    return copy;
  }
}
