package net.samyn.jgrapht.ascii.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

class LongEdgeSplitterTest {

  @Test
  void singleEdgeSpanningOneLayer_noDummiesInserted() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    Map<String, Integer> layers = Map.of("A", 0, "B", 1);

    var result = new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers);

    assertEquals(2, result.graph().vertexSet().size());
    assertEquals(1, result.graph().edgeSet().size());
    assertTrue(result.dummyVertices().isEmpty());
    assertTrue(result.graph().containsEdge("A", "B"));
  }

  @Test
  void singleEdgeSpanningTwoLayers_oneDummyInserted() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    Map<String, Integer> layers = Map.of("A", 0, "B", 2);

    var result = new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers);

    assertEquals(3, result.graph().vertexSet().size());
    assertEquals(2, result.graph().edgeSet().size());
    assertEquals(1, result.dummyVertices().size());

    // The dummy vertex should be at layer 1
    Object dummy = result.dummyVertices().iterator().next();
    assertEquals(1, result.layers().get(dummy));
    assertTrue(result.isDummy(dummy));
    assertFalse(result.isDummy("A"));
    assertFalse(result.isDummy("B"));

    // Chain: A -> dummy -> B
    assertTrue(result.graph().containsEdge("A", dummy));
    assertTrue(result.graph().containsEdge(dummy, "B"));
    assertFalse(result.graph().containsEdge("A", "B"));
  }

  @Test
  void singleEdgeSpanningThreeLayers_twoDummiesInsertedInChain() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    Map<String, Integer> layers = Map.of("A", 0, "B", 3);

    var result = new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers);

    assertEquals(4, result.graph().vertexSet().size());
    assertEquals(3, result.graph().edgeSet().size());
    assertEquals(2, result.dummyVertices().size());

    // Verify chain: each edge spans exactly one layer
    for (DefaultEdge edge : result.graph().edgeSet()) {
      Object source = result.graph().getEdgeSource(edge);
      Object target = result.graph().getEdgeTarget(edge);
      assertEquals(1, result.layers().get(target) - result.layers().get(source));
    }
  }

  @Test
  void mixedGraph_shortAndLongEdges() {
    // A -> B (span 1), B -> C (span 1), A -> C (span 2)
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("A", "C");

    Map<String, Integer> layers = Map.of("A", 0, "B", 1, "C", 2);

    var result = new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers);

    // 3 original + 1 dummy for A->C
    assertEquals(4, result.graph().vertexSet().size());
    assertEquals(1, result.dummyVertices().size());

    // All edges should span exactly one layer
    for (DefaultEdge edge : result.graph().edgeSet()) {
      Object source = result.graph().getEdgeSource(edge);
      Object target = result.graph().getEdgeTarget(edge);
      assertEquals(
          1,
          result.layers().get(target) - result.layers().get(source),
          "Edge " + source + " -> " + target + " should span exactly one layer");
    }

    // Original short edges preserved
    assertTrue(result.graph().containsEdge("A", "B"));
    assertTrue(result.graph().containsEdge("B", "C"));
  }

  @Test
  void nullGraph_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(null, Map.of()));
  }

  @Test
  void nullLayers_throwsIllegalArgumentException() {
    var graph = directedGraph();
    assertThrows(
        IllegalArgumentException.class,
        () -> new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, null));
  }

  @Test
  void missingLayerForVertex_throwsIllegalArgumentException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    // Only A has a layer — B is missing
    Map<String, Integer> layers = new HashMap<>();
    layers.put("A", 0);

    var ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers));
    assertTrue(ex.getMessage().contains("B"), "Message should mention the missing vertex");
  }

  @Test
  void nonPositiveSpan_throwsIllegalArgumentException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    // Target layer is not greater than source layer
    Map<String, Integer> layers = Map.of("A", 1, "B", 0);

    var ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers));
    assertTrue(ex.getMessage().contains("non-positive span"));
  }

  @Test
  void sameLayer_throwsIllegalArgumentException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    Map<String, Integer> layers = Map.of("A", 0, "B", 0);

    assertThrows(
        IllegalArgumentException.class,
        () -> new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers));
  }

  @Test
  void allAugmentedEdgesSpanExactlyOneLayer() {
    // Multi-level bypass: A->B, B->C, C->D, A->D (span 3)
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("C", "D");
    graph.addEdge("A", "D");

    Map<String, Integer> layers = Map.of("A", 0, "B", 1, "C", 2, "D", 3);

    var result = new LongEdgeSplitter<String, DefaultEdge>().splitLongEdges(graph, layers);

    // 2 dummies for A->D (layers 1 and 2)
    assertEquals(2, result.dummyVertices().size());

    // Verify the augmented graph can be processed by CrossingMinimiser
    @SuppressWarnings("unchecked")
    Graph<Object, DefaultEdge> augGraph = result.graph();
    Map<Object, Integer> augLayers = result.layers();

    // This would throw if any edge spans more than one layer
    assertDoesNotThrow(
        () -> new CrossingMinimiser<Object, DefaultEdge>().minimiseCrossings(augGraph, augLayers));
  }

  private DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }
}
