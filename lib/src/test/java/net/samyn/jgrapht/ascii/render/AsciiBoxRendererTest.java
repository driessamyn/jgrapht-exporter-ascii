package net.samyn.jgrapht.ascii.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.GridEdge;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.junit.jupiter.api.Test;

class AsciiBoxRendererTest {

  private final CanvasRenderer renderer = new AsciiBoxRenderer();

  @Test
  void rendersSingleVertexBox() {
    var canvas = new Canvas(20, 10);
    var vertex = new GridVertex<>("x", "Hello", 0, 0);
    renderer.renderVertex(canvas, vertex);

    var expected =
        """
                +-------+
                | Hello |
                +-------+""";
    assertEquals(expected, canvas.toString());
  }

  @Test
  void rendersShortLabel() {
    var canvas = new Canvas(20, 10);
    var vertex = new GridVertex<>("x", "A", 0, 0);
    renderer.renderVertex(canvas, vertex);

    var expected =
        """
                +---+
                | A |
                +---+""";
    assertEquals(expected, canvas.toString());
  }

  @Test
  void rendersStraightVerticalEdge() {
    var canvas = new Canvas(20, 15);
    var sourceGv = new GridVertex<>("A", "A", 0, 0);
    var targetGv = new GridVertex<>("B", "B", 0, 0);
    var edge =
        new GridEdge<>("A", "B", sourceGv, targetGv, List.of(new int[] {2, 2}, new int[] {2, 4}));
    renderer.renderEdge(canvas, edge);

    assertEquals('|', canvas.charAt(2, 3));
    assertEquals('v', canvas.charAt(2, 4));
  }

  @Test
  void rendersStraightEdgeWithJunction() {
    var canvas = new Canvas(20, 15);
    var vertex = new GridVertex<>("A", "A", 0, 0);
    renderer.renderVertex(canvas, vertex);

    var sourceGv = new GridVertex<>("A", "A", 0, 0);
    var targetGv = new GridVertex<>("B", "B", 0, 0);
    var edge =
        new GridEdge<>("A", "B", sourceGv, targetGv, List.of(new int[] {2, 2}, new int[] {2, 4}));
    renderer.renderEdge(canvas, edge);

    // Junction on bottom border
    assertEquals('+', canvas.charAt(2, 2));
    assertEquals('|', canvas.charAt(2, 3));
    assertEquals('v', canvas.charAt(2, 4));
  }

  @Test
  void rendersBentEdge() {
    var canvas = new Canvas(20, 15);
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

    assertEquals('|', canvas.charAt(2, 2));
    assertEquals('+', canvas.charAt(2, 3)); // corner
    assertEquals('-', canvas.charAt(3, 3));
    assertEquals('+', canvas.charAt(8, 3)); // corner
    assertEquals('v', canvas.charAt(8, 4));
  }
}
