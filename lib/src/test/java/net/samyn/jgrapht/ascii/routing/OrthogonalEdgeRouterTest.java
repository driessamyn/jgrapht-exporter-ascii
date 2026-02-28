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
  void nullObstacles_throwsIllegalArgumentException() {
    var graph = directedGraph();
    var model = new GridModel<String>(List.of());
    assertThrows(IllegalArgumentException.class, () -> router.routeEdges(graph, model, null));
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

  @Test
  void bentEdge_laneSearchDoesNotEnterObstacleBox() {
    // Layer 0: S1(0,0) S2(10,0) S3(20,0) S4(30,0) S5(40,0) — five sources
    // Layer 1: OBS(20,7) — obstacle vertex in the middle
    // Layer 2: T(20,14) — single target
    // All five sources connect to T, producing bent edges with horizontal segments
    // in the gap (rows 3-6). With only 4 gap rows, the 5th edge's findFreeRow
    // must NOT place its bend in OBS's box (rows 7-9).
    var graph = directedGraph();
    for (int i = 1; i <= 5; i++) graph.addVertex("S" + i);
    graph.addVertex("OBS");
    graph.addVertex("T");
    for (int i = 1; i <= 5; i++) graph.addEdge("S" + i, "T");

    var s1 = new GridVertex<>("S1", "S1", 0, 0);
    var s2 = new GridVertex<>("S2", "S2", 10, 0);
    var s3 = new GridVertex<>("S3", "S3", 20, 0);
    var s4 = new GridVertex<>("S4", "S4", 30, 0);
    var s5 = new GridVertex<>("S5", "S5", 40, 0);
    var obs = new GridVertex<>("OBS", "OBS", 20, 7);
    var t = new GridVertex<>("T", "T", 20, 14);
    var all = List.of(s1, s2, s3, s4, s5, obs, t);
    var model = new GridModel<>(all);

    List<GridEdge<String>> edges = router.routeEdges(graph, model, all);

    // Verify no horizontal segment from any edge intersects OBS's box (rows 7-9)
    for (GridEdge<String> edge : edges) {
      for (int i = 0; i < edge.path().size() - 1; i++) {
        int[] from = edge.path().get(i);
        int[] to = edge.path().get(i + 1);
        if (from[1] == to[1] && from[0] != to[0]) {
          int y = from[1];
          assertTrue(
              y < 7 || y > 9,
              "Edge "
                  + edge.source()
                  + "->"
                  + edge.target()
                  + " has horizontal segment at y="
                  + y
                  + " inside OBS box (rows 7-9)");
        }
      }
    }
  }

  @Test
  void verticalDetour_tightGap_doesNotEnterNextVertexBox() {
    // Layout: gap of 2 rows between layers 1 and 2 (rows 8-9 are the channel).
    //   Layer 0: A(0,0)
    //   Layer 1: B(0,5), D(10,5), E(20,5)
    //   Layer 2: C(0,10), F(1,10), G(2,10)
    //
    // D→F and E→G are short-span bent edges (span=5) that route first.
    // Their horizontal segments claim rows 8 and 9 in the x-range overlapping
    // the detour corridor for A→C.
    //
    // A→C (span=10) routes last. It detours around obstacle B. The channelBelow
    // findFreeRow must NOT return row 10 (C's top border) even though rows 8-9
    // are occupied.
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addVertex("E");
    graph.addVertex("F");
    graph.addVertex("G");
    graph.addEdge("D", "F");
    graph.addEdge("E", "G");
    graph.addEdge("A", "C");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 0, 5);
    var c = new GridVertex<>("C", "C", 0, 10);
    var d = new GridVertex<>("D", "D", 10, 5);
    var e = new GridVertex<>("E", "E", 20, 5);
    var f = new GridVertex<>("F", "F", 1, 10);
    var g = new GridVertex<>("G", "G", 2, 10);
    var all = List.of(a, b, c, d, e, f, g);
    var model = new GridModel<>(all);

    List<GridEdge<String>> edges = router.routeEdges(graph, model, all);

    // Verify no horizontal detour segment from A→C lands inside C's box (y=10..12)
    GridEdge<String> acEdge =
        edges.stream()
            .filter(edge -> "A".equals(edge.source()) && "C".equals(edge.target()))
            .findFirst()
            .orElseThrow();
    List<int[]> path = acEdge.path();
    for (int i = 0; i < path.size() - 1; i++) {
      int[] from = path.get(i);
      int[] to = path.get(i + 1);
      if (from[1] == to[1] && from[0] != to[0]) {
        // Horizontal segment — must not be inside C's box rows
        int y = from[1];
        assertTrue(
            y < 10 || y > 12,
            "A→C detour has horizontal segment at y=" + y + " inside C's box (rows 10-12)");
      }
    }
  }

  // --- Edge routing order tests (Proposal C) ---

  @Test
  void routeEdges_sortsShortEdgesBeforeLongEdges() {
    // Long edge: A(0,0) → D(10,14) spans 14 rows
    // Short edge: B(10,0) → C(10,7) spans 7 rows
    // Short edge should be routed first (appear first in result)
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "D");
    graph.addEdge("B", "C");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 10, 0);
    var c = new GridVertex<>("C", "C", 10, 7);
    var d = new GridVertex<>("D", "D", 10, 14);
    var model = new GridModel<>(List.of(a, b, c, d));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

    assertEquals(2, edges.size());
    assertEquals("B", edges.get(0).source());
    assertEquals("C", edges.get(0).target());
    assertEquals("A", edges.get(1).source());
    assertEquals("D", edges.get(1).target());
  }

  @Test
  void routeEdges_sameSpan_sortsNarrowerHorizontalFirst() {
    // Both edges span the same vertical distance (7 rows) but differ in horizontal span.
    // Wide: A(0,0) → C(20,7) horizontal span = 20
    // Narrow: B(10,0) → D(15,7) horizontal span = 5
    // Narrower should be routed first.
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "C");
    graph.addEdge("B", "D");

    var a = new GridVertex<>("A", "A", 0, 0);
    var b = new GridVertex<>("B", "B", 10, 0);
    var c = new GridVertex<>("C", "C", 20, 7);
    var d = new GridVertex<>("D", "D", 15, 7);
    var model = new GridModel<>(List.of(a, b, c, d));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

    assertEquals(2, edges.size());
    assertEquals("B", edges.get(0).source());
    assertEquals("D", edges.get(0).target());
    assertEquals("A", edges.get(1).source());
    assertEquals("C", edges.get(1).target());
  }

  @Test
  void routeEdges_sameSpanAndSource_breaksTieByTargetCoordinates() {
    // Two edges from the same source with the same vertical and horizontal span.
    // The comparator must break the tie using target coordinates for deterministic order.
    // A(5,0) → B(0,7) and A(5,0) → C(10,7). Both have vertical span 7, horizontal
    // centre span 5 (A centreX=7, B centreX=2, C centreX=12). Source is identical.
    // Without target tie-breaking the comparator returns 0 → nondeterministic.
    // With target tie-breaking: B(y=7,x=0) < C(y=7,x=10) → A→B first.
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");

    var a = new GridVertex<>("A", "A", 5, 0);
    var b = new GridVertex<>("B", "B", 0, 7);
    var c = new GridVertex<>("C", "C", 10, 7);
    var model = new GridModel<>(List.of(a, b, c));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

    assertEquals(2, edges.size());
    assertEquals("B", edges.get(0).target());
    assertEquals("C", edges.get(1).target());
  }

  @Test
  void routeEdges_horizontalSpanUsesBoxCentres_notOrigins() {
    // Edge A→C: A has a wide label ("Wide") at x=0, width=10, centreX=5.
    //           C at x=18, width=5, centreX=20. Centre span = |20-5| = 15.
    // Edge B→D: B has a narrow label ("N") at x=12, width=5, centreX=14.
    //           D at x=0, width=5, centreX=2. Centre span = |2-14| = 12.
    //
    // Origin-based span: A→C = |18-0| = 18, B→D = |0-12| = 12 → B→D first (correct by accident?).
    // But flip: make B→D wider by origins yet narrower by centres.
    //
    // Edge A→C: A("N") at x=20, width=5, centreX=22.
    //           C("N") at x=5, width=5, centreX=7. Origin span=15, centre span=15.
    // Edge B→D: B("VeryWideLabel") at x=0, width=20, centreX=10.
    //           D("N") at x=30, width=5, centreX=32. Origin span=30, centre span=22.
    //
    // With origin-based: A→C span=15, B→D span=30 → A→C first.
    // With centre-based: A→C span=15, B→D span=22 → A→C first. Still same...
    //
    // Better scenario: make origins closer but centres farther apart.
    // Edge A→C: A("VeryVeryWideName") at x=0, width=24, centreX=12.
    //           C("N") at x=10, width=5, centreX=12. Origin span=10, centre span=0.
    // Edge B→D: B("N") at x=3, width=5, centreX=5.
    //           D("N") at x=8, width=5, centreX=10. Origin span=5, centre span=5.
    //
    // Origin-based: A→C=10, B→D=5 → B→D first (narrower by origin).
    // Centre-based: A→C=0, B→D=5 → A→C first (narrower by centre). ← correct for routing.
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "C");
    graph.addEdge("B", "D");

    // A has a very wide label so its centre is far from its origin
    var a = new GridVertex<>("A", "VeryVeryWideName!!", 0, 0); // width=24, centreX=12
    var c = new GridVertex<>("C", "N", 10, 7); // width=5, centreX=12
    // B and D are narrow, close by origin but farther by centre
    var b = new GridVertex<>("B", "N", 3, 0); // width=5, centreX=5
    var d = new GridVertex<>("D", "N", 8, 7); // width=5, centreX=10
    var model = new GridModel<>(List.of(a, b, c, d));

    List<GridEdge<String>> edges = router.routeEdges(graph, model, List.of());

    assertEquals(2, edges.size());
    // A→C has centre span 0 (both centreX=12), so it should route first
    assertEquals("A", edges.get(0).source());
    assertEquals("C", edges.get(0).target());
    assertEquals("B", edges.get(1).source());
    assertEquals("D", edges.get(1).target());
  }

  private DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }
}
