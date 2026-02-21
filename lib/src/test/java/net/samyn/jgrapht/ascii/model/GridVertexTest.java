package net.samyn.jgrapht.ascii.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GridVertexTest {

  @Test
  void holdsVertexLabelAndPosition() {
    var vertex = new GridVertex<>("v1", "Hello", 2, 3);
    assertEquals("v1", vertex.vertex());
    assertEquals("Hello", vertex.label());
    assertEquals(2, vertex.x());
    assertEquals(3, vertex.y());
  }

  @Test
  void computesWidthFromLabel() {
    // Box: ┌───────┐
    //      │ Hello │
    //      └───────┘
    // Width = label.length + 2 (padding) + 2 (borders) = 5 + 2 + 2 = 9
    var vertex = new GridVertex<>("v", "Hello", 0, 0);
    assertEquals(9, vertex.width());
  }

  @Test
  void computesFixedHeight() {
    // Box is always 3 rows: top border, label, bottom border
    var vertex = new GridVertex<>("v", "Hello", 0, 0);
    assertEquals(3, vertex.height());
  }

  @Test
  void shortLabelWidth() {
    var vertex = new GridVertex<>("v", "A", 0, 0);
    // Width = 1 + 2 + 2 = 5
    assertEquals(5, vertex.width());
  }

  @Test
  void emptyLabelWidth() {
    var vertex = new GridVertex<>("v", "", 0, 0);
    // Width = 0 + 2 + 2 = 4
    assertEquals(4, vertex.width());
  }

  @Test
  void nullLabelWidth() {
    var vertex = new GridVertex<>("v", null, 0, 0);
    // Width = 0 + 2 + 2 = 4
    assertEquals(4, vertex.width());
  }

  @Test
  void preservesVertexIdentityWithDuplicateLabels() {
    var gv1 = new GridVertex<>(1, "Same", 0, 0);
    var gv2 = new GridVertex<>(2, "Same", 5, 0);

    assertEquals("Same", gv1.label());
    assertEquals("Same", gv2.label());
    // Even though labels match, original vertex identity is preserved
    assertEquals(1, gv1.vertex());
    assertEquals(2, gv2.vertex());
    assertNotEquals(gv1, gv2);
  }

  @Test
  void nullVertexIsPermitted() {
    var vertex = new GridVertex<>(null, "label", 0, 0);
    assertNull(vertex.vertex());
    assertEquals("label", vertex.label());
  }
}
