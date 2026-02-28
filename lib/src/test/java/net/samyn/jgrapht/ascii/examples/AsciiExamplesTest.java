package net.samyn.jgrapht.ascii.examples;

import static org.junit.jupiter.api.Assertions.*;

import net.samyn.jgrapht.ascii.AsciiExporter;
import net.samyn.jgrapht.ascii.render.AsciiBoxRenderer;
import net.samyn.jgrapht.ascii.testutils.TestUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * End-to-end examples using the ASCII renderer. Each test exercises the full pipeline with plain
 * ASCII characters instead of Unicode box-drawing. Output is printed to the console for
 * documentation purposes.
 */
class AsciiExamplesTest {

  @Test
  void singleVertex() {
    var result = exportAndPrint("Single vertex", ExampleGraphs.singleVertexDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
+---+
| A |
+---+
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void twoVerticesWithEdge() {
    var result =
        exportAndPrint("Two vertices with edge (A->B)", ExampleGraphs.twoVerticesWithEdgeDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
+---+
| A |
+-+-+
  |
  |
  |
  v
+---+
| B |
+---+
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void threeVertexLinearChain() {
    var result = exportAndPrint("Linear chain (A->B->C)", ExampleGraphs.threeVerticesLinearDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
+---+
| A |
+-+-+
  |
  |
  |
  v
+---+
| B |
+-+-+
  |
  |
  |
  v
+---+
| C |
+---+
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void diamondDag() {
    var result = exportAndPrint("Diamond DAG (A->{B,C}->D)", ExampleGraphs.diamondDag());
    String expectedDiamond =
"""
   +---+
   | A |
   +-+-+
  +--+
  |  +---+
  |      |
  v      v
+---+  +---+
| B |  | C |
+-+-+  +-+-+
  +--+   |
     +---+
     |
     v
   +---+
   | D |
   +---+
""";
    assertEquals(
        TestUtils.normaliseStringForComparison(expectedDiamond),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void bypassEdge() {
    var result = exportAndPrint("Bypass edge (A->B->C + A->C)", ExampleGraphs.skipLayerDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
+---+
| A |
+-+-+
  |
  |
  |
  +--+
+---+|
| B ||
+-+-+|
  +--+
  |
  |
  v
+---+
| C |
+---+
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void filmProductionWorkflow() {
    var result = exportAndPrint("Bypass edge (A->B->C + A->C)", ExampleGraphs.filmProductionDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
              +--------+  +---------+  +--------+  +---------+
              | Budget |  | Casting |  | Script |  | Concept |
              +----+---+  +----+----+  +----+---+  +----+----+
            +------+-------+---+------------+-----------+
            +--------------+---+            +-----------+
            |              |   +-----------+|           +----+
            v              +-------++------+v                v
      +----------+  +-------------+||+------------+  +---------------+
      | Schedule |  | Storyboards |||| VFX Previs |  | Merchandising |
      +-----+----+  +------+------+||+------+-----+  +---------------+
            |              +-------++------+|
            +--------------+               ++
                           |               |
                           v               v
                       +-------+  +----------------+
                       | Shoot |  | Motion Capture |
                       +---+---+  +--------+-------+
                           +--+            |
                           +--+------------+-+
        +---------------------+------------+ |
        |                     v              v
        |              +------------+  +-----------+
        |              | On-set VFX |  | Rough Cut |
        |              +------+-----+  +-----+-----+
        +---------------------+              ++
        +------------------------------------+|
        |                  +-----------------+|
        v                  v         +-------+--------------------v
+---------------+  +---------------+ |+--------------+  +------------------+
| CGI Rendering |  | Musical Score | || Sound Design |  | Trailer Campaign |
+-------+-------+  +-------+-------+ |+-------+------+  +------------------+
        |                  |         ++-------|
        +------------------+----------+       |
                           +----------+       |
                                      v-------+
                               +------------+
                               | Final Edit |
                               +------+-----+
                                     ++
                                     |
                                     |
                                     v
                             +---------------+
                             | Color Grading |
                             +-------+-------+
                                     ++
                                      |
                                      |
                                      v
                           +--------------------+
                           | Theatrical Release |
                           +----------+---------+
                                     ++
                                     |
                                     |
                                     v
                           +-------------------+
                           | Streaming Release |
                           +---------+---------+
                                     |
                                     |
                                     |
                                     v
                            +-----------------+
                            | Blu-ray Release |
                            +-----------------+
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  private String exportAndPrint(String label, DefaultDirectedGraph<String, DefaultEdge> graph) {
    var result =
        AsciiExporter.<String, DefaultEdge>builder()
            .renderer(new AsciiBoxRenderer())
            .build()
            .export(graph);
    System.out.println("--- " + label + " (ASCII) ---");
    System.out.println(result);
    System.out.println();
    return result;
  }
}
