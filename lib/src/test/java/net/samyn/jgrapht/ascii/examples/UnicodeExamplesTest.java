package net.samyn.jgrapht.ascii.examples;

import static org.junit.jupiter.api.Assertions.*;

import net.samyn.jgrapht.ascii.AsciiExporter;
import net.samyn.jgrapht.ascii.testutils.TestUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * End-to-end examples using the Unicode renderer (default). Each test exercises the full pipeline:
 * layout, edge routing, and rendering. Output is printed to the console for documentation purposes.
 */
class UnicodeExamplesTest {

  @Test
  void singleVertex() {
    var result = exportAndPrint("Single vertex", ExampleGraphs.singleVertexDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
┌───┐
│ A │
└───┘
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
┌───┐
│ A │
└─┬─┘
  │
  │
  │
  v
┌───┐
│ B │
└───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void threeVertexLinearChain() {
    var result = exportAndPrint("Linear chain (A->B->C)", ExampleGraphs.threeVerticesLinearDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
┌───┐
│ A │
└─┬─┘
  │
  │
  │
  v
┌───┐
│ B │
└─┬─┘
  │
  │
  │
  v
┌───┐
│ C │
└───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void diamondDag() {
    var result = exportAndPrint("Diamond DAG (A->{B,C}->D)", ExampleGraphs.diamondDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
   ┌───┐
   │ A │
   └─┬─┘
  ┌──┘
  │  └───┐
  │      │
  v      v
┌───┐  ┌───┐
│ B │  │ C │
└─┬─┘  └─┬─┘
  └──┐   │
     ┌───┘
     │
     v
   ┌───┐
   │ D │
   └───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void widerLabels() {
    var result = exportAndPrint("Wider labels (Start->End)", ExampleGraphs.widerLabelsDag());
    String expectedWider =
"""
┌───────┐
│ Start │
└───┬───┘
    │
    │
    │
    v
 ┌─────┐
 │ End │
 └─────┘
""";
    assertEquals(
        TestUtils.normaliseStringForComparison(expectedWider),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void differentLengthLabels() {
    var exporter =
        AsciiExporter.<String, DefaultEdge>builder()
            .labelProvider(v -> v.equals("X") ? "Short" : "A Much Longer Label")
            .build();
    var result = exporter.export(ExampleGraphs.differentLengthDag());
    System.out.println("--- Different length labels ---");
    System.out.println(result);
    System.out.println();
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
       ┌───────┐
       │ Short │
       └───┬───┘
           │
           │
           │
           v
┌─────────────────────┐
│ A Much Longer Label │
└─────────────────────┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void multipleRoots() {
    var result = exportAndPrint("Multiple roots ({A,B}->C)", ExampleGraphs.multipleRootsDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
┌───┐  ┌───┐
│ A │  │ B │
└─┬─┘  └─┬─┘
  └──┐   │
     ┌───┘
     │
     v
   ┌───┐
   │ C │
   └───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void deepChain_fiveVertices() {
    var result = exportAndPrint("Deep chain (1->2->3->4->5)", ExampleGraphs.deepChainDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
┌───┐
│ 1 │
└─┬─┘
  │
  │
  │
  v
┌───┐
│ 2 │
└─┬─┘
  │
  │
  │
  v
┌───┐
│ 3 │
└─┬─┘
  │
  │
  │
  v
┌───┐
│ 4 │
└─┬─┘
  │
  │
  │
  v
┌───┐
│ 5 │
└───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void bypassEdge_threeLayerSkip() {
    var result = exportAndPrint("Bypass edge (A->B->C + A->C)", ExampleGraphs.skipLayerDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
┌───┐
│ A │
└─┬─┘
  │
  │
  │
  └──┐
┌───┐│
│ B ││
└─┬─┘│
  ┌──┘
  │
  │
  v
┌───┐
│ C │
└───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void wideDag_manyVerticesInOneLayer() {
    var result = exportAndPrint("Fan-out (A->{B,C,D})", ExampleGraphs.wideDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
       ┌───┐
       │ A │
       └─┬─┘
  ┌──────┘
  │      └──────┐
  │      │      │
  v      v      v
┌───┐  ┌───┐  ┌───┐
│ B │  │ C │  │ D │
└───┘  └───┘  └───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void fanIn_multipleSourcesToOneTarget() {
    var result = exportAndPrint("Fan-in ({A,B,C}->D)", ExampleGraphs.multipleSourcesDag());
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
┌───┐  ┌───┐  ┌───┐
│ A │  │ B │  │ C │
└─┬─┘  └─┬─┘  └─┬─┘
  └──────┐      │
         ┌──────┘
         │
         v
       ┌───┐
       │ D │
       └───┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void isolatedVertices_noEdges() {
    var result =
        exportAndPrint("Isolated vertices (no edges)", ExampleGraphs.isolatedVerticesDag());
    String expectedIsolated =
        """
    ┌───┐  ┌───┐
    │ X │  │ Y │
    └───┘  └───┘
    """;
    assertEquals(
        TestUtils.normaliseStringForComparison(expectedIsolated),
        TestUtils.normaliseStringForComparison(result));
  }

  @Test
  void filmProductionWorkflow() {
    var result = exportAndPrint("Film Production Workflow", ExampleGraphs.filmProductionDag());
    // Assertion will be added after running the test and capturing its output
    assertEquals(
        TestUtils.normaliseStringForComparison(
"""
              ┌────────┐  ┌─────────┐  ┌────────┐  ┌─────────┐
              │ Budget │  │ Casting │  │ Script │  │ Concept │
              └────┬───┘  └────┬────┘  └────┬───┘  └────┬────┘
            ┌──────┘       ┌───┼────────────┘           └────┐
            ┌──────────────┼───┘            ┌───────────┘    │
            │              ┌───┼────────────┼───────────┘    │
            v──────────────┼───└───┐────────┘                v
      ┌──────────┐  ┌─────────────┐│ ┌────────────┐  ┌───────────────┐
      │ Schedule │  │ Storyboards ││ │ VFX Previs │  │ Merchandising │
      └─────┬────┘  └──────┬──────┘│ └──────┬─────┘  └───────────────┘
            └──────────────┐   ┌───┘       ┌┘
                           ┌───┘           │
                           │   └───────────┐
                           v               v
                       ┌───────┐  ┌────────────────┐
                       │ Shoot │  │ Motion Capture │
                       └───┬───┘  └────────┬───────┘
                           └──┐            │
                           └──┼────────────┼─┐
        ┌─────────────────────┼────────────┘ │
        │                     v              v
        │              ┌────────────┐  ┌───────────┐
        │              │ On-set VFX │  │ Rough Cut │
        │              └──────┬─────┘  └─────┬─────┘
        ┌─────────────────────┘              └┐
        │                  ┌─────────────────┘│
        │                  │                 └┼───────────────────┐
        v──────────────────┼─────────┌───────┘v                   v
┌───────────────┐  ┌───────────────┐ │┌──────────────┐  ┌──────────────────┐
│ CGI Rendering │  │ Musical Score │ ││ Sound Design │  │ Trailer Campaign │
└───────┬───────┘  └───────┬───────┘ │└───────┬──────┘  └──────────────────┘
        │                  │         └┌──────┐┘
        │                  └──────────┐      │
        └─────────────────────────────┐      │
                                      v──────┘
                               ┌────────────┐
                               │ Final Edit │
                               └──────┬─────┘
                                     ┌┘
                                     │
                                     │
                                     v
                             ┌───────────────┐
                             │ Color Grading │
                             └───────┬───────┘
                                     └┐
                                      │
                                      │
                                      v
                           ┌────────────────────┐
                           │ Theatrical Release │
                           └──────────┬─────────┘
                                     ┌┘
                                     │
                                     │
                                     v
                           ┌───────────────────┐
                           │ Streaming Release │
                           └─────────┬─────────┘
                                     │
                                     │
                                     │
                                     v
                            ┌─────────────────┐
                            │ Blu-ray Release │
                            └─────────────────┘
"""),
        TestUtils.normaliseStringForComparison(result));
  }

  private String exportAndPrint(String label, DefaultDirectedGraph<String, DefaultEdge> graph) {
    var result = new AsciiExporter<String, DefaultEdge>().export(graph);
    System.out.println("--- " + label + " ---");
    System.out.println(result);
    System.out.println();
    return result;
  }
}
