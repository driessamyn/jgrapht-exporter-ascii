package net.samyn.jgrapht.ascii.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.samyn.jgrapht.ascii.model.GridModel;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Sugiyama-inspired layout algorithm that assigns grid coordinates to vertices in a DAG. The
 * pipeline is:
 *
 * <ol>
 *   <li>Layer assignment (longest-path from sources)
 *   <li>Long edge splitting (insert dummy vertices so every edge spans one layer)
 *   <li>Crossing minimisation (barycenter heuristic)
 *   <li>Coordinate assignment (centre vertices within layers, space layers vertically)
 * </ol>
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class SugiyamaLayoutAlgorithm<V, E> implements LayoutAlgorithm<V, E> {

  /** Height of a vertex box (top border + label row + bottom border). */
  private static final int BOX_HEIGHT = 3;

  /** Vertical gap between the bottom of one vertex box and the top of the next layer. */
  private static final int LAYER_GAP = 2;

  /** Horizontal gap between adjacent vertex boxes within a layer. */
  private static final int VERTEX_GAP = 2;

  private final Function<V, String> labelProvider;

  /**
   * Creates a new layout algorithm.
   *
   * @param labelProvider a function that maps each vertex to its display label; must not be {@code
   *     null}
   */
  public SugiyamaLayoutAlgorithm(Function<V, String> labelProvider) {
    if (labelProvider == null) {
      throw new IllegalArgumentException("Label provider must not be null.");
    }
    this.labelProvider = labelProvider;
  }

  @Override
  public GridModel<V> layout(Graph<V, E> graph) {
    if (graph == null) {
      throw new IllegalArgumentException("Input graph must not be null.");
    }
    if (graph.vertexSet().isEmpty()) {
      return new GridModel<>(List.of());
    }

    // Step 1: Layer assignment
    Map<V, Integer> layerMap = new LayerAssigner<V, E>().assignLayers(graph);

    // Step 2: Split long edges into single-layer segments
    SplitResult<V> splitResult = new LongEdgeSplitter<V, E>().splitLongEdges(graph, layerMap);

    // Step 3: Crossing minimisation (on augmented graph)
    List<List<Object>> orderedLayers =
        new CrossingMinimiser<Object, DefaultEdge>()
            .minimiseCrossings(splitResult.graph(), splitResult.layers());

    // Step 4: Compute labels and dimensions for each real vertex
    Map<V, String> labels = new HashMap<>();
    Map<V, GridVertex<V>> vertexDimensions = new HashMap<>();
    for (V vertex : graph.vertexSet()) {
      String label = labelProvider.apply(vertex);
      labels.put(vertex, label);
      vertexDimensions.put(vertex, new GridVertex<>(vertex, label, 0, 0));
    }

    // Step 5: Assign coordinates using only real vertices for spacing
    // Filter each layer to only real vertices, preserving relative order
    List<List<V>> realLayers = new ArrayList<>();
    for (List<Object> layer : orderedLayers) {
      List<V> realLayer = new ArrayList<>();
      for (Object vertex : layer) {
        if (!splitResult.isDummy(vertex)) {
          @SuppressWarnings("unchecked")
          V original = (V) vertex;
          realLayer.add(original);
        }
      }
      realLayers.add(realLayer);
    }

    // Pre-compute the maximum layer width for centring
    int maxLayerWidth = 0;
    for (List<V> layer : realLayers) {
      int w = 0;
      for (int i = 0; i < layer.size(); i++) {
        w += vertexDimensions.get(layer.get(i)).width();
        if (i > 0) {
          w += VERTEX_GAP;
        }
      }
      maxLayerWidth = Math.max(maxLayerWidth, w);
    }

    List<GridVertex<V>> positioned = new ArrayList<>();
    int currentY = 0;

    for (List<V> layer : realLayers) {
      // Compute the total width of this layer
      int layerWidth = 0;
      for (int i = 0; i < layer.size(); i++) {
        layerWidth += vertexDimensions.get(layer.get(i)).width();
        if (i > 0) {
          layerWidth += VERTEX_GAP;
        }
      }

      // Centre this layer within the max width
      int startX = (maxLayerWidth - layerWidth) / 2;
      int currentX = startX;

      for (V vertex : layer) {
        String label = labels.get(vertex);
        positioned.add(new GridVertex<>(vertex, label, currentX, currentY));
        currentX += vertexDimensions.get(vertex).width() + VERTEX_GAP;
      }

      // Move to next layer: vertex box height + gap for edge routing
      currentY += BOX_HEIGHT + LAYER_GAP;
    }

    return new GridModel<>(positioned);
  }
}
