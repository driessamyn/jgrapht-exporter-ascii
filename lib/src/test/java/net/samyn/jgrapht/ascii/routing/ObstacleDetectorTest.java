package net.samyn.jgrapht.ascii.routing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.samyn.jgrapht.ascii.model.GridVertex;
import org.junit.jupiter.api.Test;

class ObstacleDetectorTest {

  // BOX_HEIGHT = 3 throughout: each vertex occupies y..(y+2)

  @Test
  void findVerticalCollisions_noObstacles_returnsEmpty() {
    var detector = new ObstacleDetector<String>(List.of());

    List<GridVertex<String>> result = detector.findVerticalCollisions(5, 0, 10);

    assertTrue(result.isEmpty());
  }

  @Test
  void findVerticalCollisions_oneObstacle_returnsIt() {
    // Obstacle at (3,5) with label "B" -> width=5, occupies x=3..7, y=5..7
    var obstacle = new GridVertex<>("B", "B", 3, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Vertical segment at x=5 from y=0 to y=10 — passes through obstacle
    List<GridVertex<String>> result = detector.findVerticalCollisions(5, 0, 10);

    assertEquals(1, result.size());
    assertEquals("B", result.get(0).vertex());
  }

  @Test
  void findVerticalCollisions_multipleObstacles_sortedByY() {
    var lower = new GridVertex<>("Lower", "Lower", 0, 10);
    var upper = new GridVertex<>("Upper", "Upper", 0, 5);
    var detector = new ObstacleDetector<>(List.of(lower, upper));

    // x=4 is within both obstacles' x-ranges (Upper: width=9, x=0..8; Lower: width=9, x=0..8)
    List<GridVertex<String>> result = detector.findVerticalCollisions(4, 0, 15);

    assertEquals(2, result.size());
    assertEquals("Upper", result.get(0).vertex());
    assertEquals("Lower", result.get(1).vertex());
  }

  @Test
  void findVerticalCollisions_obstacleOutsideRange_excluded() {
    // Obstacle at y=20, well below the segment range
    var obstacle = new GridVertex<>("Far", "Far", 0, 20);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    List<GridVertex<String>> result = detector.findVerticalCollisions(2, 0, 10);

    assertTrue(result.isEmpty());
  }

  @Test
  void findVerticalCollisions_obstacleOutsideXRange_excluded() {
    // Obstacle at x=20, segment at x=5 — no x overlap
    var obstacle = new GridVertex<>("Far", "Far", 20, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    List<GridVertex<String>> result = detector.findVerticalCollisions(5, 0, 10);

    assertTrue(result.isEmpty());
  }

  @Test
  void findHorizontalCollisions_returnsColliding() {
    // Obstacle at (5,3) -> width=5, occupies x=5..9, y=3..5
    var obstacle = new GridVertex<>("X", "X", 5, 3);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Horizontal segment at y=4 from x=0 to x=15 — passes through obstacle
    List<GridVertex<String>> result = detector.findHorizontalCollisions(4, 0, 15);

    assertEquals(1, result.size());
    assertEquals("X", result.get(0).vertex());
  }

  @Test
  void findHorizontalCollisions_noOverlap_returnsEmpty() {
    var obstacle = new GridVertex<>("X", "X", 5, 3);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Horizontal at y=10 — well below obstacle (y=3..5)
    List<GridVertex<String>> result = detector.findHorizontalCollisions(10, 0, 15);

    assertTrue(result.isEmpty());
  }

  @Test
  void isColumnFree_freeColumn_returnsTrue() {
    // Obstacle at x=10..14, column 5 is outside
    var obstacle = new GridVertex<>("X", "X", 10, 0);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    assertTrue(detector.isColumnFree(5, 0, 10));
  }

  @Test
  void isColumnFree_blockedColumn_returnsFalse() {
    // Obstacle at (3,5) occupies x=3..7, y=5..7 — column 5 is blocked at y=5..7
    var obstacle = new GridVertex<>("B", "B", 3, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    assertFalse(detector.isColumnFree(5, 0, 10));
  }

  @Test
  void pickDetourColumn_prefersCloserSideToTarget() {
    // Obstacle at (10,5) width=5, occupies x=10..14
    // Target is at x=20 (to the right)
    // Right side: x=15, Left side: x=9
    // Should prefer right (closer to target x=20)
    var obstacle = new GridVertex<>("O", "O", 10, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    int detourX = detector.pickDetourColumn(obstacle, 20, 5, 7);

    assertEquals(15, detourX); // one column right of obstacle
  }

  @Test
  void pickDetourColumn_prefersLeftWhenTargetIsLeft() {
    // Obstacle at (10,5) width=5, occupies x=10..14
    // Target is at x=0 (to the left)
    var obstacle = new GridVertex<>("O", "O", 10, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    int detourX = detector.pickDetourColumn(obstacle, 0, 5, 7);

    assertEquals(9, detourX); // one column left of obstacle
  }

  @Test
  void pickDetourColumn_blockedSide_choosesOtherSide() {
    // Obstacle at (10,5), target at x=20 (prefers right)
    // But another obstacle blocks the right side at (15,5)
    var obstacle = new GridVertex<>("O", "O", 10, 5);
    var blocker = new GridVertex<>("B", "B", 15, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle, blocker));

    int detourX = detector.pickDetourColumn(obstacle, 20, 5, 7);

    assertEquals(9, detourX); // falls back to left side
  }

  @Test
  void pickDetourColumn_obstacleAtLeftEdge_neverReturnsNegative() {
    // Obstacle at x=0 — leftX would be -1, which is invalid
    // Target is to the left (x=-5), so left side is preferred but must be skipped
    var obstacle = new GridVertex<>("O", "O", 0, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    int detourX = detector.pickDetourColumn(obstacle, -5, 5, 7);

    assertTrue(detourX >= 0, "Detour column must not be negative, but was " + detourX);
    assertEquals(obstacle.width(), detourX); // should fall back to right side
  }

  @Test
  void isSegmentBlocked_verticalThroughObstacle_returnsTrue() {
    var obstacle = new GridVertex<>("B", "B", 3, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Vertical segment at x=5 from y=0 to y=10 through obstacle at x=3..7, y=5..7
    assertTrue(detector.isSegmentBlocked(5, 0, 5, 10));
  }

  @Test
  void isSegmentBlocked_clearSegment_returnsFalse() {
    var obstacle = new GridVertex<>("B", "B", 10, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Vertical segment at x=2 — obstacle at x=10..14, no overlap
    assertFalse(detector.isSegmentBlocked(2, 0, 2, 10));
  }

  @Test
  void isSegmentBlocked_horizontalThroughObstacle_returnsTrue() {
    var obstacle = new GridVertex<>("B", "B", 5, 3);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Horizontal at y=4 from x=0 to x=20 — through obstacle at x=5..9, y=3..5
    assertTrue(detector.isSegmentBlocked(0, 4, 20, 4));
  }

  @Test
  void validatePath_cleanPath_noException() {
    var obstacle = new GridVertex<>("B", "B", 10, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Path well away from obstacle
    List<int[]> path = List.of(new int[] {2, 0}, new int[] {2, 4}, new int[] {2, 9});

    assertDoesNotThrow(() -> detector.validatePath(path));
  }

  @Test
  void validatePath_collidingPath_throwsException() {
    var obstacle = new GridVertex<>("B", "B", 3, 5);
    var detector = new ObstacleDetector<>(List.of(obstacle));

    // Vertical path at x=5 through obstacle at x=3..7, y=5..7
    List<int[]> path = List.of(new int[] {5, 0}, new int[] {5, 10});

    assertThrows(IllegalStateException.class, () -> detector.validatePath(path));
  }
}
