package net.samyn.jgrapht.ascii.render;

import static org.junit.jupiter.api.Assertions.*;

import net.samyn.jgrapht.ascii.model.Canvas;
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
