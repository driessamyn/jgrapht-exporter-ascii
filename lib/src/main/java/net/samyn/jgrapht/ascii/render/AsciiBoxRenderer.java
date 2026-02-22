package net.samyn.jgrapht.ascii.render;

import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.GridVertex;

/**
 * Renders vertex boxes using plain ASCII characters.
 *
 * <pre>
 * +-------+
 * | label |
 * +-------+
 * </pre>
 */
public class AsciiBoxRenderer implements CanvasRenderer {

  @Override
  public void renderVertex(Canvas canvas, GridVertex<?> vertex) {
    int x = vertex.x();
    int y = vertex.y();
    int innerWidth = vertex.width() - 2;

    // Top border: +---+
    canvas.putChar(x, y, '+');
    for (int i = 0; i < innerWidth; i++) {
      canvas.putChar(x + 1 + i, y, '-');
    }
    canvas.putChar(x + innerWidth + 1, y, '+');

    // Label row: | label |
    canvas.putChar(x, y + 1, '|');
    canvas.putChar(x + 1, y + 1, ' ');
    canvas.putString(x + 2, y + 1, vertex.label());
    canvas.putChar(x + 2 + vertex.label().length(), y + 1, ' ');
    canvas.putChar(x + innerWidth + 1, y + 1, '|');

    // Bottom border: +---+
    canvas.putChar(x, y + 2, '+');
    for (int i = 0; i < innerWidth; i++) {
      canvas.putChar(x + 1 + i, y + 2, '-');
    }
    canvas.putChar(x + innerWidth + 1, y + 2, '+');
  }
}
