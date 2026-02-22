package net.samyn.jgrapht.ascii.routing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridModel;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

class OrthogonalEdgeRouterTest {

  private final EdgeRouter router = new OrthogonalEdgeRouter();

  @Test
  void straightVerticalEdge_alignedVertices() {
    // A at (0,0), B at (0,5) — both centred, same x
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 0, 5);
    var model = new GridModel<>(List.of(a, b));

    List<GridEdge<String>> edges = router.routeEdges(graph, model);

    assertEquals(1, edges.size());
    GridEdge<String> edge = edges.get(0);
    assertEquals("A", edge.source());
    assertEquals("B", edge.target());

    // A has width 5 (1 char + 2 padding + 2 border), centre = 2
    // exitX = 0 + 5/2 = 2, exitY = 0 + 2 = 2 (bottom border)
    // entryX = 0 + 5/2 = 2, entryY = 5 (top border)
    // Straight: path = (2, 2) → (2, 4) — starts at border row
    List<int[]> path = edge.path();
    assertEquals(2, path.size());
    assertArrayEquals(new int[] {2, 2}, path.get(0));
    assertArrayEquals(new int[] {2, 4}, path.get(1));
  }

  @Test
  void bentEdge_offsetVertices() {
    // A at (0,0), B at (6,5) — different x positions
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 6, 5);
    var model = new GridModel<>(List.of(a, b));

    List<GridEdge<String>> edges = router.routeEdges(graph, model);

    assertEquals(1, edges.size());
    GridEdge<String> edge = edges.get(0);

    // A width=5, centre=2 → exitX=2, exitY=2
    // B width=5, centre=8 → entryX=8, entryY=5
    // Bent: bendY = 2 + 1 = 3
    // Path: (2,2) → (2,3) → (8,3) → (8,4) — starts at border row, all distinct
    List<int[]> path = edge.path();
    assertEquals(4, path.size());
    assertArrayEquals(new int[] {2, 2}, path.get(0));
    assertArrayEquals(new int[] {2, 3}, path.get(1));
    assertArrayEquals(new int[] {8, 3}, path.get(2));
    assertArrayEquals(new int[] {8, 4}, path.get(3));
  }

  @Test
  void multipleEdgesFromSameVertex() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");

    var a = new GridVertex<>("A", "A", 3, 0);
    var b = new GridVertex<>("B", "B", 0, 5);
    var c = new GridVertex<>("C", "C", 8, 5);
    var model = new GridModel<>(List.of(a, b, c));

    List<GridEdge<String>> edges = router.routeEdges(graph, model);

    assertEquals(2, edges.size());
  }

  @Test
  void nullGraph_throwsIllegalArgumentException() {
    var model = new GridModel<String>(List.of());
    assertThrows(IllegalArgumentException.class, () -> router.routeEdges(null, model));
  }

  @Test
  void nullModel_throwsIllegalArgumentException() {
    var graph = directedGraph();
    assertThrows(IllegalArgumentException.class, () -> router.routeEdges(graph, null));
  }

  @Test
  void emptyGraph_returnsEmptyList() {
    var graph = directedGraph();
    var model = new GridModel<String>(List.of());

    List<GridEdge<String>> edges = router.routeEdges(graph, model);

    assertTrue(edges.isEmpty());
  }

  @Test
  void missingSourceVertex_throwsIllegalStateException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    // Model only contains B, not A
    var b = new GridVertex<>("B", "B", 0, 5);
    var model = new GridModel<>(List.of(b));

    assertThrows(IllegalStateException.class, () -> router.routeEdges(graph, model));
  }

  @Test
  void missingTargetVertex_throwsIllegalStateException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    // Model only contains A, not B
    var a = new GridVertex<>("A", "A", 0, 0);
    var model = new GridModel<>(List.of(a));

    assertThrows(IllegalStateException.class, () -> router.routeEdges(graph, model));
  }

  @Test
  void graphWithNoEdges_returnsEmptyList() {
    var graph = directedGraph();
    graph.addVertex("A");

    var a = new GridVertex<>("A", "A", 0, 0);
    var model = new GridModel<>(List.of(a));

    List<GridEdge<String>> edges = router.routeEdges(graph, model);

    assertTrue(edges.isEmpty());
  }

  private DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }
}
