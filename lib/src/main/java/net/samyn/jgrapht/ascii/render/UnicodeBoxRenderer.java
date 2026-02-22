package net.samyn.jgrapht.ascii.render;

import java.util.List;
import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.DisplayWidth;
import net.samyn.jgrapht.ascii.model.GridEdge;
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

  @Override
  public void renderEdge(Canvas canvas, GridEdge<?> edge) {
    List<int[]> path = edge.path();
    if (path.size() < 2) {
      return;
    }

    // Draw the first waypoint as a junction on the source bottom border
    int[] first = path.get(0);
    char existingAtFirstWaypoint = canvas.charAt(first[0], first[1]);
    boolean drewJunction = false;
    // If current char is a horizontal line, convert to a T-junction.
    // If it's already a junction or cross, consider it drawn.
    if (existingAtFirstWaypoint == '\u2500') { // ─ (horizontal border)
      canvas.putChar(first[0], first[1], '\u252C'); // ┬ (T-junction down)
      drewJunction = true;
    } else if (existingAtFirstWaypoint == '\u252C' // ┬ (T-junction down)
        || existingAtFirstWaypoint == '\u253C') { // ┼ (Cross junction)
      // Already a junction that a vertical line can pass through
      drewJunction = true;
    }

    for (int i = 0; i < path.size() - 1; i++) {
      int x1 = path.get(i)[0];
      int y1 = path.get(i)[1];
      int x2 = path.get(i + 1)[0];
      int y2 = path.get(i + 1)[1];

      if (x1 == x2) {
        // Vertical segment
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        for (int y = minY; y <= maxY; y++) {
          if (drewJunction && i == 0 && y == y1) {
            continue; // skip — already drawn as junction
          }
          canvas.putCharWithPrecedence(x1, y, '\u2502'); // │
        }
      } else if (y1 == y2) {
        // Horizontal segment
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        for (int cx = minX; cx <= maxX; cx++) {
          if (drewJunction && i == 0 && cx == x1) {
            continue; // skip — already drawn as junction
          }
          canvas.putCharWithPrecedence(cx, y1, '\u2500'); // ─
        }
      }
    }

    // Draw corners at bend points (where direction changes)
    for (int i = 1; i < path.size() - 1; i++) {
      int prevX = path.get(i - 1)[0];
      int prevY = path.get(i - 1)[1];
      int curX = path.get(i)[0];
      int curY = path.get(i)[1];
      int nextX = path.get(i + 1)[0];
      int nextY = path.get(i + 1)[1];

      // Only draw a corner if direction actually changes
      boolean prevHorizontal = (prevY == curY);
      boolean nextHorizontal = (curY == nextY);
      if (prevHorizontal == nextHorizontal) {
        continue;
      }

      if (prevX < curX && nextY > curY) {
        canvas.putCharWithPrecedence(curX, curY, '\u2510'); // ┐ (came from left, going down)
      } else if (prevX > curX && nextY > curY) {
        canvas.putCharWithPrecedence(curX, curY, '\u250C'); // ┌ (came from right, going down)
      } else if (prevY < curY && nextX > curX) {
        canvas.putCharWithPrecedence(curX, curY, '\u2514'); // └ (came from above, going right)
      } else if (prevY < curY && nextX < curX) {
        canvas.putCharWithPrecedence(curX, curY, '\u2518'); // ┘ (came from above, going left)
      } else if (prevY > curY && nextX > curX) {
        canvas.putCharWithPrecedence(curX, curY, '\u250C'); // ┌ (came from below, going right)
      } else if (prevY > curY && nextX < curX) {
        canvas.putCharWithPrecedence(curX, curY, '\u2510'); // ┐ (came from below, going left)
      }
    }

    // Arrow at the last waypoint
    int[] last = path.get(path.size() - 1);
    canvas.putCharWithPrecedence(last[0], last[1], 'v');
  }
}
