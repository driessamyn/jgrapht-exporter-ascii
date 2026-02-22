package net.samyn.jgrapht.ascii.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.junit.jupiter.api.Test;

class UnicodeBoxRendererTest {

  private final CanvasRenderer renderer = new UnicodeBoxRenderer();

  @Test
  void rendersSingleVertexBox() {
    var canvas = new Canvas(20, 10);
    var vertex = new GridVertex<>("x", "Hello", 0, 0);
    renderer.renderVertex(canvas, vertex);

    var expected =
        """
                ┌───────┐
                │ Hello │
                └───────┘""";
    assertEquals(expected, canvas.toString());
  }

  @Test
  void rendersVertexAtOffset() {
    var canvas = new Canvas(20, 10);
    var vertex = new GridVertex<>("x", "Hi", 3, 2);
    renderer.renderVertex(canvas, vertex);

    assertEquals('┌', canvas.charAt(3, 2));
    assertEquals('│', canvas.charAt(3, 3));
    assertEquals('└', canvas.charAt(3, 4));
    assertEquals('H', canvas.charAt(5, 3));
    assertEquals('i', canvas.charAt(6, 3));
  }

  @Test
  void rendersStraightVerticalEdge() {
    var canvas = new Canvas(20, 15);
    // Straight edge: starts at border row (2), ends at arrow row (4)
    var edge = new GridEdge<>("A", "B", List.of(new int[] {2, 2}, new int[] {2, 4}));
    renderer.renderEdge(canvas, edge);

    assertEquals('\u2502', canvas.charAt(2, 3)); // │ in the gap
    assertEquals('v', canvas.charAt(2, 4)); // arrow at last waypoint
  }

  @Test
  void rendersStraightEdgeWithJunction() {
    var canvas = new Canvas(20, 15);
    // Render a vertex first so the border is present
    var vertex = new GridVertex<>("A", "A", 0, 0);
    renderer.renderVertex(canvas, vertex);

    // Edge exits from centre of bottom border (x=2, y=2)
    var edge = new GridEdge<>("A", "B", List.of(new int[] {2, 2}, new int[] {2, 4}));
    renderer.renderEdge(canvas, edge);

    // Junction on bottom border
    assertEquals('\u252C', canvas.charAt(2, 2)); // ┬
    assertEquals('\u2502', canvas.charAt(2, 3)); // │
    assertEquals('v', canvas.charAt(2, 4));
  }

  @Test
  void rendersBentEdge() {
    var canvas = new Canvas(20, 15);
    // Bent edge: border → down → right → down
    var edge =
        new GridEdge<>(
            "A",
            "B",
            List.of(new int[] {2, 2}, new int[] {2, 3}, new int[] {8, 3}, new int[] {8, 4}));
    renderer.renderEdge(canvas, edge);

    // Vertical segment from border
    assertEquals('\u2502', canvas.charAt(2, 2)); // │ at start (no border to junction with)
    // Corner at first bend (vertical to horizontal)
    assertEquals('\u2514', canvas.charAt(2, 3)); // └ (from above, going right)
    // Horizontal segment
    assertEquals('\u2500', canvas.charAt(3, 3)); // ─
    assertEquals('\u2500', canvas.charAt(7, 3)); // ─
    // Corner at second bend (horizontal to vertical)
    assertEquals('\u2510', canvas.charAt(8, 3)); // ┐ (from left, going down)
    // Arrow
    assertEquals('v', canvas.charAt(8, 4));
  }

  @Test
  void rendersShortLabel() {
    var canvas = new Canvas(20, 10);
    var vertex = new GridVertex<>("x", "A", 0, 0);
    renderer.renderVertex(canvas, vertex);

    var expected =
        """
                ┌───┐
                │ A │
                └───┘""";
    assertEquals(expected, canvas.toString());
  }

  @Test
  void rendersEmptyLabel() {
    var canvas = new Canvas(20, 10);
    var vertex = new GridVertex<>("x", "", 0, 0);
    renderer.renderVertex(canvas, vertex);

    var expected =
        """
                ┌──┐
                │  │
                └──┘""";
    assertEquals(expected, canvas.toString());
  }
}
