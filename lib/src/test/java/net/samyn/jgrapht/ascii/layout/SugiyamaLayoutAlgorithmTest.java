package net.samyn.jgrapht.ascii.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Function;
import net.samyn.jgrapht.ascii.model.GridModel;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

class SugiyamaLayoutAlgorithmTest {

  @Test
  void singleVertex_centredAtOrigin() {
    var graph = directedGraph();
    graph.addVertex("A");

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    assertEquals(1, result.vertices().size());
    GridVertex<String> v = result.vertices().get(0);
    assertEquals("A", v.label());
    assertEquals("A", v.vertex());
    assertEquals(0, v.x());
    assertEquals(0, v.y());
  }

  @Test
  void linearChain_verticallyStacked() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    assertEquals(3, result.vertices().size());
    List<GridVertex<String>> sorted = result.verticesByLayer();

    // All on the same x (single vertex per layer, centred)
    assertEquals(sorted.get(0).x(), sorted.get(1).x());
    assertEquals(sorted.get(1).x(), sorted.get(2).x());

    // Increasing y positions with gaps for edge routing
    assertTrue(sorted.get(0).y() < sorted.get(1).y());
    assertTrue(sorted.get(1).y() < sorted.get(2).y());
  }

  @Test
  void diamond_properlySpaced() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");
    graph.addEdge("B", "D");
    graph.addEdge("C", "D");

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    assertEquals(4, result.vertices().size());
    List<GridVertex<String>> sorted = result.verticesByLayer();

    // A at layer 0, B and C at layer 1, D at layer 2
    // B and C should not overlap horizontally
    GridVertex<String> a = findByLabel(sorted, "A");
    GridVertex<String> b = findByLabel(sorted, "B");
    GridVertex<String> c = findByLabel(sorted, "C");
    GridVertex<String> d = findByLabel(sorted, "D");

    // Layer 1 vertices should not overlap
    assertTrue(
        b.x() + b.width() <= c.x() || c.x() + c.width() <= b.x(),
        "B and C should not overlap horizontally");

    // Y positions: layer 0 < layer 1 < layer 2
    assertTrue(a.y() < b.y());
    assertEquals(b.y(), c.y());
    assertTrue(b.y() < d.y());
  }

  @Test
  void coordinateAssignment_considersLabelWidths() {
    var graph = directedGraph();
    graph.addVertex("X");
    graph.addVertex("Y");
    graph.addEdge("X", "Y");

    // Use a custom label provider that gives different-length labels
    Function<String, String> labelProvider = v -> v.equals("X") ? "Short" : "A Much Longer Label";
    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(labelProvider);
    GridModel<String> result = layout.layout(graph);

    GridVertex<String> shortVertex = findByLabel(result.vertices(), "Short");
    GridVertex<String> longVertex = findByLabel(result.vertices(), "A Much Longer Label");

    assertNotNull(shortVertex);
    assertNotNull(longVertex);
    // The longer label should produce a wider box
    assertTrue(longVertex.width() > shortVertex.width());
  }

  @Test
  void nullGraph_throwsIllegalArgumentException() {
    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    assertThrows(IllegalArgumentException.class, () -> layout.layout(null));
  }

  @Test
  void emptyGraph_returnsEmptyModel() {
    var graph = directedGraph();
    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    assertTrue(result.vertices().isEmpty());
  }

  @Test
  void cyclicGraph_throwsIllegalArgumentException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");
    graph.addEdge("B", "A");

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    assertThrows(IllegalArgumentException.class, () -> layout.layout(graph));
  }

  @Test
  void duplicateLabels_preserveVertexIdentity() {
    var graph = directedGraph();
    graph.addVertex("v1");
    graph.addVertex("v2");
    graph.addEdge("v1", "v2");

    // Both vertices get the same display label
    Function<String, String> labelProvider = v -> "Same";
    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(labelProvider);
    GridModel<String> result = layout.layout(graph);

    assertEquals(2, result.vertices().size());

    // Both have the same label but different original vertices
    GridVertex<String> first = result.vertices().get(0);
    GridVertex<String> second = result.vertices().get(1);
    assertEquals("Same", first.label());
    assertEquals("Same", second.label());
    assertNotEquals(first.vertex(), second.vertex());
    assertTrue(
        ("v1".equals(first.vertex()) && "v2".equals(second.vertex()))
            || ("v2".equals(first.vertex()) && "v1".equals(second.vertex())));
  }

  @Test
  void linearChainWithBypass_succeeds() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("A", "C"); // bypass: spans 2 layers

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    assertEquals(3, result.vertices().size());
    List<GridVertex<String>> sorted = result.verticesByLayer();

    // A at layer 0, B at layer 1, C at layer 2
    assertTrue(sorted.get(0).y() < sorted.get(1).y());
    assertTrue(sorted.get(1).y() < sorted.get(2).y());
  }

  @Test
  void multiLevelBypass_succeeds() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("C", "D");
    graph.addEdge("A", "D"); // bypass: spans 3 layers

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    assertEquals(4, result.vertices().size());
  }

  @Test
  void dummyVertices_doNotAppearInOutput() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("A", "C"); // bypass: creates a dummy

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    // Only original vertices appear
    for (GridVertex<String> gv : result.vertices()) {
      assertTrue(
          "A".equals(gv.vertex()) || "B".equals(gv.vertex()) || "C".equals(gv.vertex()),
          "Unexpected vertex in output: " + gv.vertex());
    }
  }

  @Test
  void bypassEdge_doesNotShiftVertexPositions() {
    // A linear chain without bypass
    var graphWithout = directedGraph();
    graphWithout.addVertex("A");
    graphWithout.addVertex("B");
    graphWithout.addVertex("C");
    graphWithout.addEdge("A", "B");
    graphWithout.addEdge("B", "C");

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> withoutBypass = layout.layout(graphWithout);

    // Same chain with an additional bypass edge A->C
    var graphWith = directedGraph();
    graphWith.addVertex("A");
    graphWith.addVertex("B");
    graphWith.addVertex("C");
    graphWith.addEdge("A", "B");
    graphWith.addEdge("B", "C");
    graphWith.addEdge("A", "C");

    GridModel<String> withBypass = layout.layout(graphWith);

    // Dummy vertices should not affect real vertex positions
    for (String name : List.of("A", "B", "C")) {
      GridVertex<String> expected = findByLabel(withoutBypass.vertices(), name);
      GridVertex<String> actual = findByLabel(withBypass.vertices(), name);
      assertEquals(expected.x(), actual.x(), "x position of " + name + " should not drift");
      assertEquals(expected.y(), actual.y(), "y position of " + name + " should not drift");
    }
  }

  @Test
  void vertexIdentity_preservedAfterSplitting() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("A", "C");

    var layout = new SugiyamaLayoutAlgorithm<String, DefaultEdge>(Object::toString);
    GridModel<String> result = layout.layout(graph);

    // Output vertices are the original String instances, not Object wrappers
    for (GridVertex<String> gv : result.vertices()) {
      assertSame(String.class, gv.vertex().getClass());
    }
  }

  private DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }

  private GridVertex<String> findByLabel(List<GridVertex<String>> vertices, String label) {
    return vertices.stream().filter(v -> v.label().equals(label)).findFirst().orElse(null);
  }
}
