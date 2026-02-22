package net.samyn.jgrapht.ascii;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import net.samyn.jgrapht.ascii.layout.SugiyamaLayoutAlgorithm;
import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridModel;
import net.samyn.jgrapht.ascii.render.CanvasRenderer;
import net.samyn.jgrapht.ascii.render.UnicodeBoxRenderer;
import org.jgrapht.Graph;

/**
 * Exports a JGraphT directed acyclic graph to ASCII/Unicode text art. Uses a Sugiyama-inspired
 * layered layout algorithm with orthogonal edge routing.
 *
 * <p>Default configuration uses Unicode box-drawing characters and {@code Object::toString} for
 * vertex labels. Use the {@link #builder()} for custom configuration.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class AsciiExporter<V, E> {

  private final Function<V, String> labelProvider;
  private final CanvasRenderer renderer;

  /** Creates an exporter with default settings (Unicode rendering, toString labels). */
  public AsciiExporter() {
    this(Object::toString, new UnicodeBoxRenderer());
  }

  private AsciiExporter(Function<V, String> labelProvider, CanvasRenderer renderer) {
    this.labelProvider = labelProvider;
    this.renderer = renderer;
  }

  /**
   * Exports the graph to a string.
   *
   * @param graph the directed acyclic graph to export
   * @return the rendered ASCII/Unicode text
   * @throws IllegalArgumentException if the graph is {@code null} or contains cycles
   */
  public String export(Graph<V, E> graph) {
    if (graph == null) {
      throw new IllegalArgumentException("Graph must not be null.");
    }

    var layout = new SugiyamaLayoutAlgorithm<V, E>(labelProvider);
    GridModel<V> model = layout.layout(graph);

    if (model.vertices().isEmpty()) {
      return "";
    }

    // Compute canvas dimensions from vertex positions
    int maxX = 0;
    int maxY = 0;
    for (var vertex : model.vertices()) {
      maxX = Math.max(maxX, vertex.x() + vertex.width());
      maxY = Math.max(maxY, vertex.y() + vertex.height());
    }

    var canvas = new Canvas(maxX, maxY);

    // Render vertices first, then edges (so edge junctions overwrite border characters)
    for (var vertex : model.vertices()) {
      renderer.renderVertex(canvas, vertex);
    }
    List<GridEdge<V>> sortedEdges = new ArrayList<>(model.edges());
    sortedEdges.sort(
        Comparator.<GridEdge<V>>comparingInt(edge -> edge.sourceGridVertex().y())
            .thenComparingInt(edge -> edge.sourceGridVertex().x())
            .thenComparingInt(edge -> edge.targetGridVertex().y())
            .thenComparingInt(edge -> edge.targetGridVertex().x()));

    for (var edge : sortedEdges) {
      renderer.renderEdge(canvas, edge);
    }

    return canvas.toString();
  }

  /**
   * Exports the graph to a writer.
   *
   * @param graph the directed acyclic graph to export
   * @param writer the writer to write the output to
   * @throws IllegalArgumentException if the graph or writer is {@code null}
   */
  public void export(Graph<V, E> graph, Writer writer) {
    if (writer == null) {
      throw new IllegalArgumentException("Writer must not be null.");
    }
    String result = export(graph);
    try {
      writer.write(result);
    } catch (java.io.IOException e) {
      throw new RuntimeException("Failed to write graph output.", e);
    }
  }

  /** Returns a new builder for configuring an {@link AsciiExporter}. */
  public static <V, E> Builder<V, E> builder() {
    return new Builder<>();
  }

  /**
   * Builder for configuring an {@link AsciiExporter}.
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   */
  public static class Builder<V, E> {
    private Function<V, String> labelProvider;
    private CanvasRenderer renderer;

    private Builder() {}

    /** Sets the function used to convert vertices to display labels. */
    public Builder<V, E> labelProvider(Function<V, String> labelProvider) {
      this.labelProvider = labelProvider;
      return this;
    }

    /** Sets the renderer used for drawing vertices and edges. */
    public Builder<V, E> renderer(CanvasRenderer renderer) {
      this.renderer = renderer;
      return this;
    }

    /** Builds the exporter with the configured settings. */
    public AsciiExporter<V, E> build() {
      Function<V, String> lp = labelProvider != null ? labelProvider : Object::toString;
      CanvasRenderer r = renderer != null ? renderer : new UnicodeBoxRenderer();
      return new AsciiExporter<>(lp, r);
    }
  }
}
