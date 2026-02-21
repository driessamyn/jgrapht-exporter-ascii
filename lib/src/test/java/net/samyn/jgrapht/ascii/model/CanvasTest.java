package net.samyn.jgrapht.ascii.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanvasTest {

    @Test
    void createCanvasWithDimensions() {
        var canvas = new Canvas(10, 5);
        assertEquals(10, canvas.getWidth());
        assertEquals(5, canvas.getHeight());
    }

    @Test
    void newCanvasFilledWithSpaces() {
        var canvas = new Canvas(3, 2);
        assertEquals(' ', canvas.charAt(0, 0));
        assertEquals(' ', canvas.charAt(2, 1));
    }

    @Test
    void writeAndReadCharacter() {
        var canvas = new Canvas(5, 5);
        canvas.putChar(2, 3, 'X');
        assertEquals('X', canvas.charAt(2, 3));
    }

    @Test
    void writeHorizontalString() {
        var canvas = new Canvas(10, 3);
        canvas.putString(1, 1, "hello");
        assertEquals('h', canvas.charAt(1, 1));
        assertEquals('e', canvas.charAt(2, 1));
        assertEquals('o', canvas.charAt(5, 1));
    }

    @Test
    void writeVerticalString() {
        var canvas = new Canvas(3, 10);
        canvas.putVerticalString(1, 1, "hi");
        assertEquals('h', canvas.charAt(1, 1));
        assertEquals('i', canvas.charAt(1, 2));
    }

    @Test
    void toStringOutputsGrid() {
        var canvas = new Canvas(3, 2);
        canvas.putChar(0, 0, 'A');
        canvas.putChar(2, 1, 'B');
        assertEquals("A\n  B", canvas.toString());
    }

    @Test
    void toStringTrimsTrailingBlankLines() {
        var canvas = new Canvas(3, 5);
        canvas.putChar(0, 0, 'A');
        // Only 1 line of content, trailing blank lines should be trimmed
        assertEquals("A", canvas.toString());
    }

    @Test
    void autoExpandOnWriteBeyondWidth() {
        var canvas = new Canvas(2, 2);
        canvas.putChar(5, 0, 'X');
        assertTrue(canvas.getWidth() > 5);
        assertEquals('X', canvas.charAt(5, 0));
    }

    @Test
    void autoExpandOnWriteBeyondHeight() {
        var canvas = new Canvas(2, 2);
        canvas.putChar(0, 5, 'Y');
        assertTrue(canvas.getHeight() > 5);
        assertEquals('Y', canvas.charAt(0, 5));
    }

    @Test
    void putStringAutoExpands() {
        var canvas = new Canvas(2, 1);
        canvas.putString(0, 0, "longtext");
        assertEquals("longtext", canvas.toString());
    }
}
