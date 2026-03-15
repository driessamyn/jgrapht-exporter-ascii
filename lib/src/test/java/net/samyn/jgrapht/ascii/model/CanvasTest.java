package net.samyn.jgrapht.ascii.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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

  // --- Arrow protection tests ---

  @Test
  void putCharWithPrecedence_horizontalLineDoesNotOverwriteArrow() {
    var canvas = new Canvas(3, 1);
    canvas.putChar(1, 0, 'v');
    canvas.putCharWithPrecedence(1, 0, '\u2500'); // ─
    assertEquals('v', canvas.charAt(1, 0));
  }

  @Test
  void putCharWithPrecedence_verticalLineDoesNotOverwriteArrow() {
    var canvas = new Canvas(3, 1);
    canvas.putChar(1, 0, 'v');
    canvas.putCharWithPrecedence(1, 0, '\u2502'); // │
    assertEquals('v', canvas.charAt(1, 0));
  }

  @Test
  void putCharWithPrecedence_asciiHorizontalLineDoesNotOverwriteArrow() {
    var canvas = new Canvas(3, 1);
    canvas.putChar(1, 0, 'v');
    canvas.putCharWithPrecedence(1, 0, '-');
    assertEquals('v', canvas.charAt(1, 0));
  }

  @Test
  void putCharWithPrecedence_asciiVerticalLineDoesNotOverwriteArrow() {
    var canvas = new Canvas(3, 1);
    canvas.putChar(1, 0, 'v');
    canvas.putCharWithPrecedence(1, 0, '|');
    assertEquals('v', canvas.charAt(1, 0));
  }

  // --- Corner + line → junction merge tests (Unicode) ---

  @Test
  void putCharWithPrecedence_topLeftCornerOnVerticalLineProducesLeftTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2502'); // │
    canvas.putCharWithPrecedence(1, 0, '\u250C'); // ┌
    assertEquals('\u251C', canvas.charAt(1, 0)); // ├
  }

  @Test
  void putCharWithPrecedence_topRightCornerOnVerticalLineProducesRightTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2502'); // │
    canvas.putCharWithPrecedence(1, 0, '\u2510'); // ┐
    assertEquals('\u2524', canvas.charAt(1, 0)); // ┤
  }

  @Test
  void putCharWithPrecedence_bottomLeftCornerOnVerticalLineProducesLeftTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2502'); // │
    canvas.putCharWithPrecedence(1, 0, '\u2514'); // └
    assertEquals('\u251C', canvas.charAt(1, 0)); // ├
  }

  @Test
  void putCharWithPrecedence_bottomRightCornerOnVerticalLineProducesRightTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2502'); // │
    canvas.putCharWithPrecedence(1, 0, '\u2518'); // ┘
    assertEquals('\u2524', canvas.charAt(1, 0)); // ┤
  }

  @Test
  void putCharWithPrecedence_topLeftCornerOnHorizontalLineProducesDownTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2500'); // ─
    canvas.putCharWithPrecedence(1, 0, '\u250C'); // ┌
    assertEquals('\u252C', canvas.charAt(1, 0)); // ┬
  }

  @Test
  void putCharWithPrecedence_topRightCornerOnHorizontalLineProducesDownTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2500'); // ─
    canvas.putCharWithPrecedence(1, 0, '\u2510'); // ┐
    assertEquals('\u252C', canvas.charAt(1, 0)); // ┬
  }

  @Test
  void putCharWithPrecedence_bottomLeftCornerOnHorizontalLineProducesUpTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2500'); // ─
    canvas.putCharWithPrecedence(1, 0, '\u2514'); // └
    assertEquals('\u2534', canvas.charAt(1, 0)); // ┴
  }

  @Test
  void putCharWithPrecedence_bottomRightCornerOnHorizontalLineProducesUpTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2500'); // ─
    canvas.putCharWithPrecedence(1, 0, '\u2518'); // ┘
    assertEquals('\u2534', canvas.charAt(1, 0)); // ┴
  }

  // --- Line on existing corner → junction merge tests ---

  @Test
  void putCharWithPrecedence_verticalLineOnTopLeftCornerProducesLeftTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u250C'); // ┌
    canvas.putCharWithPrecedence(1, 0, '\u2502'); // │
    assertEquals('\u251C', canvas.charAt(1, 0)); // ├
  }

  @Test
  void putCharWithPrecedence_verticalLineOnTopRightCornerProducesRightTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2510'); // ┐
    canvas.putCharWithPrecedence(1, 0, '\u2502'); // │
    assertEquals('\u2524', canvas.charAt(1, 0)); // ┤
  }

  @Test
  void putCharWithPrecedence_horizontalLineOnTopLeftCornerProducesDownTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u250C'); // ┌
    canvas.putCharWithPrecedence(1, 0, '\u2500'); // ─
    assertEquals('\u252C', canvas.charAt(1, 0)); // ┬
  }

  @Test
  void putCharWithPrecedence_horizontalLineOnBottomLeftCornerProducesUpTee() {
    var canvas = new Canvas(3, 1);
    canvas.putCharWithPrecedence(1, 0, '\u2514'); // └
    canvas.putCharWithPrecedence(1, 0, '\u2500'); // ─
    assertEquals('\u2534', canvas.charAt(1, 0)); // ┴
  }

  @Test
  void putCharWithPrecedence_cornerDoesNotOverwriteArrow() {
    var canvas = new Canvas(3, 1);
    canvas.putChar(1, 0, 'v');
    canvas.putCharWithPrecedence(1, 0, '\u2510'); // ┐
    assertEquals('v', canvas.charAt(1, 0));
  }

  @Test
  void putCharWithPrecedence_junctionDoesNotOverwriteArrow() {
    var canvas = new Canvas(3, 1);
    canvas.putChar(1, 0, 'v');
    canvas.putCharWithPrecedence(1, 0, '\u252C'); // ┬
    assertEquals('v', canvas.charAt(1, 0));
  }
}
