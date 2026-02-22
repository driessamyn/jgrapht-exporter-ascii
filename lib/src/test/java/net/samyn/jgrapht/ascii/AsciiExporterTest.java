package net.samyn.jgrapht.ascii;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import net.samyn.jgrapht.ascii.render.AsciiBoxRenderer;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

class AsciiExporterTest {

  @Test
  void singleVertex_exportsBox() {
    var graph = directedGraph();
    graph.addVertex("A");

    var exporter = new AsciiExporter<String, DefaultEdge>();
    String result = exporter.export(graph);

    var expected =
        """
        \u250C\u2500\u2500\u2500\u2510
        \u2502 A \u2502
        \u2514\u2500\u2500\u2500\u2518""";
    assertEquals(expected, result);
  }

  @Test
  void linearChain_exportsWithEdges() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");

    var exporter = new AsciiExporter<String, DefaultEdge>();
    String result = exporter.export(graph);

    // Should contain both vertex boxes and an edge between them
    assertTrue(result.contains("A"), "Output should contain vertex A");
    assertTrue(result.contains("B"), "Output should contain vertex B");
    assertTrue(result.contains("v"), "Output should contain a downward arrow");
  }

  @Test
  void export_writesToWriter() {
    var graph = directedGraph();
    graph.addVertex("A");

    var exporter = new AsciiExporter<String, DefaultEdge>();
    var writer = new StringWriter();
    exporter.export(graph, writer);

    assertTrue(writer.toString().contains("A"));
  }

  @Test
  void customLabelProvider() {
    var graph = directedGraph();
    graph.addVertex("node1");

    var exporter =
        AsciiExporter.<String, DefaultEdge>builder().labelProvider(v -> "Label:" + v).build();
    String result = exporter.export(graph);

    assertTrue(result.contains("Label:node1"));
  }

  @Test
  void asciiRendererFallback() {
    var graph = directedGraph();
    graph.addVertex("A");

    var exporter =
        AsciiExporter.<String, DefaultEdge>builder().renderer(new AsciiBoxRenderer()).build();
    String result = exporter.export(graph);

    var expected =
        """
        +---+
        | A |
        +---+""";
    assertEquals(expected, result);
  }

  @Test
  void nullGraph_throwsIllegalArgumentException() {
    var exporter = new AsciiExporter<String, DefaultEdge>();
    assertThrows(
        IllegalArgumentException.class,
        () -> exporter.export((org.jgrapht.Graph<String, DefaultEdge>) null));
  }

  @Test
  void nullWriter_throwsIllegalArgumentException() {
    var graph = directedGraph();
    graph.addVertex("A");

    var exporter = new AsciiExporter<String, DefaultEdge>();
    assertThrows(IllegalArgumentException.class, () -> exporter.export(graph, null));
  }

  @Test
  void cyclicGraph_throwsIllegalArgumentException() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");
    graph.addEdge("B", "A");

    var exporter = new AsciiExporter<String, DefaultEdge>();
    assertThrows(IllegalArgumentException.class, () -> exporter.export(graph));
  }

  @Test
  void emptyGraph_returnsEmptyString() {
    var graph = directedGraph();

    var exporter = new AsciiExporter<String, DefaultEdge>();
    String result = exporter.export(graph);

    assertEquals("", result);
  }

  @Test
  void diamondDag_exportsAllVerticesAndEdges() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");
    graph.addEdge("B", "D");
    graph.addEdge("C", "D");

    var exporter = new AsciiExporter<String, DefaultEdge>();
    String result = exporter.export(graph);

    assertTrue(result.contains("A"));
    assertTrue(result.contains("B"));
    assertTrue(result.contains("C"));
    assertTrue(result.contains("D"));
  }

  private DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }
}
