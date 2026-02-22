package net.samyn.jgrapht.ascii.routing;

import java.util.List;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridModel;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.jgrapht.Graph;

/** Strategy interface for computing edge routes on a positioned grid model. */
public interface EdgeRouter {

  /**
   * Computes routes for all edges in the graph.
   *
   * @param <V> the vertex type
   * @param graph the original graph containing edge information
   * @param model the positioned grid model containing vertex coordinates
   * @return a list of routed edges with waypoint paths
   */
  <V> List<GridEdge<V>> routeEdges(
      Graph<V, ?> graph, GridModel<V> model, List<GridVertex<V>> obstacles);
}
