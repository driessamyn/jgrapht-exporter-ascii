package net.samyn.jgrapht.ascii.render;

import static net.samyn.jgrapht.ascii.model.BoxDrawing.*;
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
    var sourceGv = new GridVertex<>("A", "A", 0, 0);
    var targetGv = new GridVertex<>("B", "B", 0, 0);
    var edge =
        new GridEdge<>("A", "B", sourceGv, targetGv, List.of(new int[] {2, 2}, new int[] {2, 4}));
    renderer.renderEdge(canvas, edge);

    assertEquals(VERTICAL, canvas.charAt(2, 3));
    assertEquals('v', canvas.charAt(2, 4)); // arrow at last waypoint
  }

  @Test
  void rendersStraightEdgeWithJunction() {
    var canvas = new Canvas(20, 15);
    // Render a vertex first so the border is present
    var vertex = new GridVertex<>("A", "A", 0, 0);
    renderer.renderVertex(canvas, vertex);

    // Edge exits from centre of bottom border (x=2, y=2)
    var sourceGv = new GridVertex<>("A", "A", 0, 0);
    var targetGv = new GridVertex<>("B", "B", 0, 0);
    var edge =
        new GridEdge<>("A", "B", sourceGv, targetGv, List.of(new int[] {2, 2}, new int[] {2, 4}));
    renderer.renderEdge(canvas, edge);

    // Junction on bottom border
    assertEquals(TEE_DOWN, canvas.charAt(2, 2));
    assertEquals(VERTICAL, canvas.charAt(2, 3));
    assertEquals('v', canvas.charAt(2, 4));
  }

  @Test
  void rendersBentEdge() {
    var canvas = new Canvas(20, 15);
    // Bent edge: border → down → right → down
    var sourceGv = new GridVertex<>("A", "A", 0, 0);
    var targetGv = new GridVertex<>("B", "B", 0, 0);
    var edge =
        new GridEdge<>(
            "A",
            "B",
            sourceGv,
            targetGv,
            List.of(new int[] {2, 2}, new int[] {2, 3}, new int[] {8, 3}, new int[] {8, 4}));
    renderer.renderEdge(canvas, edge);

    // Vertical segment from border
    assertEquals(VERTICAL, canvas.charAt(2, 2));
    // Corner at first bend (vertical to horizontal)
    assertEquals(BOTTOM_LEFT, canvas.charAt(2, 3));
    // Horizontal segment
    assertEquals(HORIZONTAL, canvas.charAt(3, 3));
    assertEquals(HORIZONTAL, canvas.charAt(7, 3));
    // Corner at second bend (horizontal to vertical)
    assertEquals(TOP_RIGHT, canvas.charAt(8, 3));
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
