package net.samyn.jgrapht.ascii.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.junit.jupiter.api.Test;

public class LayerAssignerTest {

  @Test
  void assignLayers_emptyGraph_returnsEmptyMap() {
    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    Map<String, Integer> layers = assigner.assignLayers(graph);

    assertNotNull(layers);
    assertEquals(0, layers.size());
  }

  @Test
  void assignLayers_singleVertexGraph_assignsToLayerZero() {
    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    String v1 = "V1";
    graph.addVertex(v1);

    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    Map<String, Integer> layers = assigner.assignLayers(graph);

    assertNotNull(layers);
    assertEquals(1, layers.size());
    assertEquals(0, layers.get(v1));
  }

  @Test
  void assignLayers_linearChainGraph_assignsCorrectLayers() {
    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    String a = "A";
    String b = "B";
    String c = "C";
    graph.addVertex(a);
    graph.addVertex(b);
    graph.addVertex(c);
    graph.addEdge(a, b);
    graph.addEdge(b, c);

    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    Map<String, Integer> layers = assigner.assignLayers(graph);

    assertNotNull(layers);
    assertEquals(3, layers.size());
    assertEquals(0, layers.get(a));
    assertEquals(1, layers.get(b));
    assertEquals(2, layers.get(c));
  }

  @Test
  void assignLayers_diamondGraph_assignsCorrectLayers() {
    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    String a = "A";
    String b = "B";
    String c = "C";
    String d = "D";
    graph.addVertex(a);
    graph.addVertex(b);
    graph.addVertex(c);
    graph.addVertex(d);
    graph.addEdge(a, b);
    graph.addEdge(a, c);
    graph.addEdge(b, d);
    graph.addEdge(c, d);

    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    Map<String, Integer> layers = assigner.assignLayers(graph);

    assertNotNull(layers);
    assertEquals(4, layers.size());
    assertEquals(0, layers.get(a));
    assertEquals(1, layers.get(b));
    assertEquals(1, layers.get(c));
    assertEquals(2, layers.get(d));
  }

  @Test
  void assignLayers_wideDagWithMultipleRoots_assignsCorrectLayers() {
    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    String r1 = "R1";
    String r2 = "R2";
    String a = "A";
    String b = "B";
    String c = "C";
    graph.addVertex(r1);
    graph.addVertex(r2);
    graph.addVertex(a);
    graph.addVertex(b);
    graph.addVertex(c);
    graph.addEdge(r1, a);
    graph.addEdge(r2, b);
    graph.addEdge(a, c);
    graph.addEdge(b, c);

    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    Map<String, Integer> layers = assigner.assignLayers(graph);

    assertNotNull(layers);
    assertEquals(5, layers.size()); // R1, R2, A, B, C
    assertEquals(0, layers.get(r1));
    assertEquals(0, layers.get(r2));
    assertEquals(1, layers.get(a));
    assertEquals(1, layers.get(b));
    assertEquals(2, layers.get(c));
  }

  @Test
  void assignLayers_cyclicGraph_throwsIllegalArgumentException() {
    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    String a = "A";
    String b = "B";
    graph.addVertex(a);
    graph.addVertex(b);
    graph.addEdge(a, b);
    graph.addEdge(b, a); // Creates a cycle A -> B -> A

    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> assigner.assignLayers(graph));
    String expectedMessagePart1 =
        "Graph contains cycles. Layer assignment is only supported for DAGs. Vertices involved in cycles: ";
    String fullMessage = exception.getMessage();

    assertTrue(fullMessage.contains(expectedMessagePart1));
    assertTrue(fullMessage.contains("A"));
    assertTrue(fullMessage.contains("B"));
  }

  @Test
  void assignLayers_nullGraph_throwsIllegalArgumentException() {
    Graph<String, DefaultEdge> nullGraph = null;
    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    assertThrows(IllegalArgumentException.class, () -> assigner.assignLayers(nullGraph));
  }

  @Test
  void assignLayers_undirectedGraph_throwsIllegalArgumentException() {
    Graph<String, DefaultEdge> undirectedGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
    undirectedGraph.addVertex("A");
    undirectedGraph.addVertex("B");
    undirectedGraph.addEdge("A", "B");

    LayerAssigner<String, DefaultEdge> assigner = new LayerAssigner<>();

    assertThrows(IllegalArgumentException.class, () -> assigner.assignLayers(undirectedGraph));
  }
}
