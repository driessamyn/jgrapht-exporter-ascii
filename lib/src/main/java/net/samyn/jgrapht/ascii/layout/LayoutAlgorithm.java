package net.samyn.jgrapht.ascii.layout;

import net.samyn.jgrapht.ascii.model.GridModel;
import org.jgrapht.Graph;

/**
 * Strategy interface for computing a grid layout from a graph.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface LayoutAlgorithm<V, E> {

  /**
   * Computes grid positions for all vertices in the graph.
   *
   * @param graph the input graph; must not be {@code null}
   * @return a {@link GridModel} containing positioned vertices with original vertex identity
   * @throws IllegalArgumentException if the graph is {@code null} or unsuitable for this algorithm
   */
  GridModel<V> layout(Graph<V, E> graph);
}
