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

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

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

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

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

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

    assertEquals(2, edges.size());
  }

  @Test
  void nullGraph_throwsIllegalArgumentException() {
    var model = new GridModel<String>(List.of());
    assertThrows(IllegalArgumentException.class, () -> router.routeEdges(null, model, List.of()));
  }

  @Test
  void nullModel_throwsIllegalArgumentException() {
    var graph = directedGraph();
    assertThrows(IllegalArgumentException.class, () -> router.routeEdges(graph, null, List.of()));
  }

  @Test
  void emptyGraph_returnsEmptyList() {
    var graph = directedGraph();
    var model = new GridModel<String>(List.of());

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

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

    assertThrows(IllegalStateException.class, () -> router.routeEdges(graph, model, List.of()));
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

    assertThrows(IllegalStateException.class, () -> router.routeEdges(graph, model, List.of()));
  }

  @Test
  void graphWithNoEdges_returnsEmptyList() {
    var graph = directedGraph();
    graph.addVertex("A");

    var a = new GridVertex<>("A", "A", 0, 0);
    var model = new GridModel<>(List.of(a));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

    assertTrue(edges.isEmpty());
  }

  // --- Obstacle avoidance tests ---

  @Test
  void straightVertical_singleObstacle_detoursAround() {
    // A at (0,0) layer 0, B (obstacle) at (0,5) layer 1, C at (0,10) layer 2
    // Edge A->C must detour around B
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "C");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 0, 5);
    var c = new GridVertex<>("C", "C", 0, 10);
    var model = new GridModel<>(List.of(a, b, c));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of(a, b, c));

    assertEquals(1, edges.size());
    List<int[]> path = edges.get(0).path();

    // The path must not pass through B's box (x=0..4, y=5..7)
    for (int i = 0; i < path.size() - 1; i++) {
      int[] from = path.get(i);
      int[] to = path.get(i + 1);
      // Vertical segments through x=0..4 in y=5..7 would collide
      if (from[0] == to[0]) { // vertical segment
        int x = from[0];
        int minY = Math.min(from[1], to[1]);
        int maxY = Math.max(from[1], to[1]);
        if (x >= 0 && x <= 4) {
          // Must not overlap y=5..7
          assertTrue(
              maxY < 5 || minY > 7,
              "Vertical segment at x=" + x + " y=" + minY + ".." + maxY + " passes through B");
        }
      }
    }
  }

  @Test
  void straightVertical_multipleObstacles_detoursAroundAll() {
    // A at (0,0), B at (0,5), C at (0,10), D at (0,15)
    // Edge A->D must detour around both B and C
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "D");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 0, 5);
    var c = new GridVertex<>("C", "C", 0, 10);
    var d = new GridVertex<>("D", "D", 0, 15);
    var model = new GridModel<>(List.of(a, b, c, d));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of(a, b, c, d));

    assertEquals(1, edges.size());
    List<int[]> path = edges.get(0).path();

    // Path must not collide with B (x=0..4, y=5..7) or C (x=0..4, y=10..12)
    for (int i = 0; i < path.size() - 1; i++) {
      int[] from = path.get(i);
      int[] to = path.get(i + 1);
      if (from[0] == to[0]) {
        int x = from[0];
        int minY = Math.min(from[1], to[1]);
        int maxY = Math.max(from[1], to[1]);
        if (x >= 0 && x <= 4) {
          assertTrue(maxY < 5 || minY > 7, "Vertical segment passes through B at x=" + x);
          assertTrue(maxY < 10 || minY > 12, "Vertical segment passes through C at x=" + x);
        }
      }
    }
  }

  @Test
  void bentEdge_obstacleOnHorizontalSegment_pushesBendDown() {
    // A at (0,0), target C at (12,10). Obstacle B sits in the horizontal path
    // B at (6,0) — same layer as A, blocking horizontal segment at y=3
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "C");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 6, 0);
    var c = new GridVertex<>("C", "C", 12, 10);
    var model = new GridModel<>(List.of(a, b, c));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of(a, b, c));

    assertEquals(1, edges.size());
    List<int[]> path = edges.get(0).path();

    // The horizontal segment must not pass through B's box (x=6..10, y=0..2)
    for (int i = 0; i < path.size() - 1; i++) {
      int[] from = path.get(i);
      int[] to = path.get(i + 1);
      if (from[1] == to[1]) { // horizontal segment
        int y = from[1];
        int minX = Math.min(from[0], to[0]);
        int maxX = Math.max(from[0], to[0]);
        if (y >= 0 && y <= 2) {
          // Must not overlap x=6..10
          assertTrue(
              maxX < 6 || minX > 10,
              "Horizontal segment at y=" + y + " x=" + minX + ".." + maxX + " passes through B");
        }
      }
    }
  }

  @Test
  void bentEdge_obstacleOnVerticalSegment_detours() {
    // A at (0,0), C at (0,10), obstacle B at (0,5)
    // Edge A->C with B blocking the entry-side vertical
    // This is effectively the same as straightVertical but ensures bent logic also handles it
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "C");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 0, 5);
    var c = new GridVertex<>("C", "C", 6, 10);
    var model = new GridModel<>(List.of(a, b, c));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of(a, b, c));

    assertEquals(1, edges.size());
    List<int[]> path = edges.get(0).path();

    // Path must not collide with B (x=0..4, y=5..7)
    for (int i = 0; i < path.size() - 1; i++) {
      int[] from = path.get(i);
      int[] to = path.get(i + 1);
      if (from[0] == to[0] && from[0] >= 0 && from[0] <= 4) {
        int minY = Math.min(from[1], to[1]);
        int maxY = Math.max(from[1], to[1]);
        assertTrue(maxY < 5 || minY > 7, "Vertical segment passes through B");
      }
    }
  }

  private DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }
}
