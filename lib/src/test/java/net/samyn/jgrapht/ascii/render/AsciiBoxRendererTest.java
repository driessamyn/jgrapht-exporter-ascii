package net.samyn.jgrapht.ascii.render;

import static org.junit.jupiter.api.Assertions.*;

import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.junit.jupiter.api.Test;

class AsciiBoxRendererTest {

  private final CanvasRenderer renderer = new AsciiBoxRenderer();

  @Test
  void rendersSingleVertexBox() {
    var canvas = new Canvas(20, 10);
    var vertex = new GridVertex("Hello", 0, 0);
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
    var vertex = new GridVertex("A", 0, 0);
    renderer.renderVertex(canvas, vertex);

    var expected =
        """
                +---+
                | A |
                +---+""";
    assertEquals(expected, canvas.toString());
  }
}
