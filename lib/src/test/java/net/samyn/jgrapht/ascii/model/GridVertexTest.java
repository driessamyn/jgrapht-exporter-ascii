package net.samyn.jgrapht.ascii.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridVertexTest {

    @Test
    void holdsLabelAndPosition() {
        var vertex = new GridVertex("Hello", 2, 3);
        assertEquals("Hello", vertex.label());
        assertEquals(2, vertex.x());
        assertEquals(3, vertex.y());
    }

    @Test
    void computesWidthFromLabel() {
        // Box: ┌─────┐
        //      │ Hello │
        //      └─────┘
        // Width = label.length + 2 (padding) + 2 (borders) = 5 + 2 + 2 = 9
        var vertex = new GridVertex("Hello", 0, 0);
        assertEquals(9, vertex.width());
    }

    @Test
    void computesFixedHeight() {
        // Box is always 3 rows: top border, label, bottom border
        var vertex = new GridVertex("Hello", 0, 0);
        assertEquals(3, vertex.height());
    }

    @Test
    void shortLabelWidth() {
        var vertex = new GridVertex("A", 0, 0);
        // Width = 1 + 2 + 2 = 5
        assertEquals(5, vertex.width());
    }

    @Test
    void emptyLabelWidth() {
        var vertex = new GridVertex("", 0, 0);
        // Width = 0 + 2 + 2 = 4
        assertEquals(4, vertex.width());
    }

    @Test
    void nullLabelWidth() {
        var vertex = new GridVertex(null, 0, 0);
        // Width = 0 + 2 + 2 = 4
        assertEquals(4, vertex.width());
    }
}
