package net.samyn.jgrapht.ascii.render;

import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridVertex;

/** Strategy interface for rendering graph elements onto a {@link Canvas}. */
public interface CanvasRenderer {

  /** Renders a vertex box onto the canvas at the vertex's grid position. */
  void renderVertex(Canvas canvas, GridVertex<?> vertex);

  /** Renders an edge route onto the canvas using the edge's waypoint path. */
  void renderEdge(Canvas canvas, GridEdge<?> edge);
}
