package net.samyn.jgrapht.ascii.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridModel;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.jgrapht.Graph;

/**
 * Computes orthogonal edge routes between connected vertices. Edges exit from the centre of the
 * source vertex's bottom border and enter at the centre of the target vertex's top border.
 *
 * <p>Straight edges (aligned vertices) produce a simple vertical path. Offset edges include a
 * horizontal bend one row below the source vertex.
 */
public class OrthogonalEdgeRouter implements EdgeRouter {

  @Override
  public <V> List<GridEdge<V>> routeEdges(Graph<V, ?> graph, GridModel<V> model) {
    if (graph == null) {
      throw new IllegalArgumentException("Graph must not be null.");
    }
    if (model == null) {
      throw new IllegalArgumentException("Model must not be null.");
    }

    return doRouteEdges(graph, model);
  }

  private <V, E> List<GridEdge<V>> doRouteEdges(Graph<V, E> graph, GridModel<V> model) {
    // Build a lookup from vertex to its grid position
    Map<V, GridVertex<V>> vertexMap = new HashMap<>();
    for (GridVertex<V> gv : model.vertices()) {
      vertexMap.put(gv.vertex(), gv);
    }

    List<GridEdge<V>> edges = new ArrayList<>();
    for (E edge : graph.edgeSet()) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);

      GridVertex<V> sourceVertex = vertexMap.get(source);
      GridVertex<V> targetVertex = vertexMap.get(target);
      if (sourceVertex == null) {
        throw new IllegalStateException("Source vertex '" + source + "' not found in grid model.");
      }
      if (targetVertex == null) {
        throw new IllegalStateException("Target vertex '" + target + "' not found in grid model.");
      }

      int exitX = sourceVertex.x() + sourceVertex.width() / 2;
      int exitY = sourceVertex.y() + 2; // bottom border row
      int entryX = targetVertex.x() + targetVertex.width() / 2;
      int entryY = targetVertex.y(); // top border row

      List<int[]> path = new ArrayList<>();

      if (exitX == entryX) {
        // Straight vertical path: start at border, end one row above target
        path.add(new int[] {exitX, exitY});
        path.add(new int[] {exitX, entryY - 1});
      } else {
        // Bent path: border → one row below → horizontal across → down to target
        int bendY = exitY + 1;
        path.add(new int[] {exitX, exitY});
        path.add(new int[] {exitX, bendY});
        path.add(new int[] {entryX, bendY});
        path.add(new int[] {entryX, entryY - 1});
      }

      edges.add(new GridEdge<>(source, target, sourceVertex, targetVertex, path));
    }

    return edges;
  }
}
