package net.samyn.jgrapht.ascii.render;

import net.samyn.jgrapht.ascii.model.Canvas;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnicodeBoxRendererTest {

    private final CanvasRenderer renderer = new UnicodeBoxRenderer();

    @Test
    void rendersSingleVertexBox() {
        var canvas = new Canvas(20, 10);
        var vertex = new GridVertex("Hello", 0, 0);
        renderer.renderVertex(canvas, vertex);

        var expected = """
                ┌───────┐
                │ Hello │
                └───────┘""";
        assertEquals(expected, canvas.toString());
    }

    @Test
    void rendersVertexAtOffset() {
        var canvas = new Canvas(20, 10);
        var vertex = new GridVertex("Hi", 3, 2);
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
        var vertex = new GridVertex("A", 0, 0);
        renderer.renderVertex(canvas, vertex);

        var expected = """
                ┌───┐
                │ A │
                └───┘""";
        assertEquals(expected, canvas.toString());
    }

    @Test
    void rendersEmptyLabel() {
        var canvas = new Canvas(20, 10);
        var vertex = new GridVertex("", 0, 0);
        renderer.renderVertex(canvas, vertex);

        var expected = """
                ┌──┐
                │  │
                └──┘""";
        assertEquals(expected, canvas.toString());
    }
}
