package net.samyn.jgrapht.ascii.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class GridEdgeTest {

  @Test
  void validWaypoints_constructsSuccessfully() {
    var edge = new GridEdge<>("A", "B", List.of(new int[] {0, 0}, new int[] {0, 5}));
    assertEquals("A", edge.source());
    assertEquals("B", edge.target());
    assertEquals(2, edge.path().size());
  }

  @Test
  void nullSource_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class, () -> new GridEdge<>(null, "B", List.of(new int[] {0, 0})));
  }

  @Test
  void nullTarget_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class, () -> new GridEdge<>("A", null, List.of(new int[] {0, 0})));
  }

  @Test
  void nullPath_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new GridEdge<>("A", "B", null));
  }

  @Test
  void waypointWithWrongLength_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new GridEdge<>("A", "B", List.of(new int[] {1, 2, 3})));
  }

  @Test
  void emptyWaypoint_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class, () -> new GridEdge<>("A", "B", List.of(new int[] {})));
  }

  @Test
  void nullWaypointInList_throwsIllegalArgumentException() {
    List<int[]> path = new java.util.ArrayList<>();
    path.add(new int[] {0, 0});
    path.add(null);
    assertThrows(IllegalArgumentException.class, () -> new GridEdge<>("A", "B", path));
  }
}
