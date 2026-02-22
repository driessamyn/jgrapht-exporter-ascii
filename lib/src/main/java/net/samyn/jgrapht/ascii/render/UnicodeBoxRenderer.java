package net.samyn.jgrapht.ascii.render;

import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.DisplayWidth;
import net.samyn.jgrapht.ascii.model.GridVertex;

/**
 * Renders vertex boxes using Unicode box-drawing characters.
 *
 * <pre>
 * ┌───────┐
 * │ label │
 * └───────┘
 * </pre>
 */
public class UnicodeBoxRenderer implements CanvasRenderer {

  @Override
  public void renderVertex(Canvas canvas, GridVertex<?> vertex) {
    int x = vertex.x();
    int y = vertex.y();
    int innerWidth = vertex.width() - 2;

    // Top border: ┌───┐
    canvas.putChar(x, y, '\u250C');
    for (int i = 0; i < innerWidth; i++) {
      canvas.putChar(x + 1 + i, y, '\u2500');
    }
    canvas.putChar(x + innerWidth + 1, y, '\u2510');

    // Label row: │ label │
    canvas.putChar(x, y + 1, '\u2502');
    canvas.putChar(x + 1, y + 1, ' ');
    canvas.putString(x + 2, y + 1, vertex.label());
    canvas.putChar(x + 2 + DisplayWidth.width(vertex.label()), y + 1, ' ');
    canvas.putChar(x + innerWidth + 1, y + 1, '\u2502');

    // Bottom border: └───┘
    canvas.putChar(x, y + 2, '\u2514');
    for (int i = 0; i < innerWidth; i++) {
      canvas.putChar(x + 1 + i, y + 2, '\u2500');
    }
    canvas.putChar(x + innerWidth + 1, y + 2, '\u2518');
  }
}
