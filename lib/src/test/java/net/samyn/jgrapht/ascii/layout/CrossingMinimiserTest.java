package net.samyn.jgrapht.ascii.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

class CrossingMinimiserTest {

  private final CrossingMinimiser<String, DefaultEdge> minimiser = new CrossingMinimiser<>();

  @Test
  void noCrossings_orderUnchanged() {
    // A -> C, B -> D (no crossings when ordered A,B and C,D)
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "C");
    graph.addEdge("B", "D");

    Map<String, Integer> layers = Map.of("A", 0, "B", 0, "C", 1, "D", 1);
    List<List<String>> orderedLayers = minimiser.minimiseCrossings(graph, layers);

    assertEquals(2, orderedLayers.size());
    // A should come before B, C should come before D (no crossings)
    assertEquals(List.of("A", "B"), orderedLayers.get(0));
    assertEquals(List.of("C", "D"), orderedLayers.get(1));
  }

  @Test
  void simpleCrossing_reduced() {
    // A -> D, B -> C creates a crossing when ordered A,B and C,D
    // After minimisation, the order should reduce crossings
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "D");
    graph.addEdge("B", "C");

    Map<String, Integer> layers = Map.of("A", 0, "B", 0, "C", 1, "D", 1);
    List<List<String>> orderedLayers = minimiser.minimiseCrossings(graph, layers);

    assertEquals(2, orderedLayers.size());
    // After minimisation: either swap layer 0 to B,A or layer 1 to D,C
    int crossings = countCrossings(graph, orderedLayers);
    assertEquals(0, crossings, "Crossings should be eliminated");
  }

  @Test
  void multiLayerSweep() {
    // Three layers: {A,B} -> {C,D} -> {E,F}
    // A->D, B->C (crossing in layer 0->1)
    // C->F, D->E (crossing in layer 1->2)
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addVertex("E");
    graph.addVertex("F");
    graph.addEdge("A", "D");
    graph.addEdge("B", "C");
    graph.addEdge("C", "F");
    graph.addEdge("D", "E");

    Map<String, Integer> layers = Map.of("A", 0, "B", 0, "C", 1, "D", 1, "E", 2, "F", 2);
    List<List<String>> orderedLayers = minimiser.minimiseCrossings(graph, layers);

    assertEquals(3, orderedLayers.size());
    int crossings = countCrossings(graph, orderedLayers);
    assertEquals(0, crossings, "All crossings should be eliminated after multi-layer sweep");
  }

  @Test
  void singleVertexPerLayer_noCrossings() {
    // Linear chain: A -> B -> C
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");

    Map<String, Integer> layers = Map.of("A", 0, "B", 1, "C", 2);
    List<List<String>> orderedLayers = minimiser.minimiseCrossings(graph, layers);

    assertEquals(3, orderedLayers.size());
    assertEquals(List.of("A"), orderedLayers.get(0));
    assertEquals(List.of("B"), orderedLayers.get(1));
    assertEquals(List.of("C"), orderedLayers.get(2));
  }

  @Test
  void diamondGraph_noCrossings() {
    // A -> B, A -> C, B -> D, C -> D
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");
    graph.addEdge("B", "D");
    graph.addEdge("C", "D");

    Map<String, Integer> layers = Map.of("A", 0, "B", 1, "C", 1, "D", 2);
    List<List<String>> orderedLayers = minimiser.minimiseCrossings(graph, layers);

    assertEquals(3, orderedLayers.size());
    int crossings = countCrossings(graph, orderedLayers);
    assertEquals(0, crossings);
  }

  @Test
  void emptyGraph_returnsEmptyLayers() {
    var graph = directedGraph();
    Map<String, Integer> layers = Map.of();
    List<List<String>> orderedLayers = minimiser.minimiseCrossings(graph, layers);

    assertTrue(orderedLayers.isEmpty());
  }

  @Test
  void nullGraph_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> minimiser.minimiseCrossings(null, Map.of()));
  }

  @Test
  void nullLayers_throwsIllegalArgumentException() {
    var graph = directedGraph();
    assertThrows(IllegalArgumentException.class, () -> minimiser.minimiseCrossings(graph, null));
  }

  @Test
  void longEdge_throwsIllegalArgumentException() {
    // Edge A->C spans 2 layers (0 -> 2), which is not allowed
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");

    Map<String, Integer> layers = Map.of("A", 0, "B", 1, "C", 2);

    var exception =
        assertThrows(
            IllegalArgumentException.class, () -> minimiser.minimiseCrossings(graph, layers));
    assertTrue(exception.getMessage().contains("spans"));
    assertTrue(exception.getMessage().contains("single-layer segments"));
  }

  @Test
  void vertexWithoutLayerAssignment_throwsIllegalArgumentException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    // Only A has a layer assignment, B is missing
    Map<String, Integer> layers = Map.of("A", 0);

    var exception =
        assertThrows(
            IllegalArgumentException.class, () -> minimiser.minimiseCrossings(graph, layers));
    assertTrue(exception.getMessage().contains("without a layer assignment"));
  }

  private DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }

  /**
   * Counts the number of edge crossings between adjacent layers. Two edges (u1->v1) and (u2->v2)
   * cross if u1 is before u2 in their layer but v1 is after v2 in theirs (or vice versa).
   */
  private int countCrossings(Graph<String, DefaultEdge> graph, List<List<String>> orderedLayers) {
    int totalCrossings = 0;
    for (int i = 0; i < orderedLayers.size() - 1; i++) {
      List<String> upperLayer = orderedLayers.get(i);
      List<String> lowerLayer = orderedLayers.get(i + 1);

      // Collect edges between these two layers
      List<int[]> edges = new java.util.ArrayList<>();
      for (DefaultEdge edge : graph.edgeSet()) {
        String source = graph.getEdgeSource(edge);
        String target = graph.getEdgeTarget(edge);
        int sourceIdx = upperLayer.indexOf(source);
        int targetIdx = lowerLayer.indexOf(target);
        if (sourceIdx >= 0 && targetIdx >= 0) {
          edges.add(new int[] {sourceIdx, targetIdx});
        }
      }

      // Count crossings
      for (int a = 0; a < edges.size(); a++) {
        for (int b = a + 1; b < edges.size(); b++) {
          int[] e1 = edges.get(a);
          int[] e2 = edges.get(b);
          if ((e1[0] < e2[0] && e1[1] > e2[1]) || (e1[0] > e2[0] && e1[1] < e2[1])) {
            totalCrossings++;
          }
        }
      }
    }
    return totalCrossings;
  }
}
