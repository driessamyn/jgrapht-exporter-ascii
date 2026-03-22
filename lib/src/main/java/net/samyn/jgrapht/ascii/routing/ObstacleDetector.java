package net.samyn.jgrapht.ascii.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.samyn.jgrapht.ascii.model.GridVertex;

/**
 * Detects collisions between edge path segments and vertex boxes, and computes detour columns to
 * avoid obstacles. Separated from path construction for testability.
 *
 * @param <V> the graph vertex type
 */
class ObstacleDetector<V> {

  private static final int BOX_HEIGHT = GridVertex.BOX_HEIGHT;

  private final List<GridVertex<V>> obstacles;

  ObstacleDetector(List<GridVertex<V>> obstacles) {
    this.obstacles = List.copyOf(obstacles);
  }

  /** Returns all obstacles whose box intersects the vertical segment, sorted by Y ascending. */
  List<GridVertex<V>> findVerticalCollisions(int x, int startY, int endY) {
    int segMinY = Math.min(startY, endY);
    int segMaxY = Math.max(startY, endY);

    List<GridVertex<V>> collisions = new ArrayList<>();
    for (GridVertex<V> obst : obstacles) {
      int obstMinX = obst.x();
      int obstMaxX = obst.x() + obst.width() - 1;
      int obstMinY = obst.y();
      int obstMaxY = obst.y() + BOX_HEIGHT - 1;

      if (x >= obstMinX && x <= obstMaxX && segMaxY >= obstMinY && segMinY <= obstMaxY) {
        collisions.add(obst);
      }
    }

    collisions.sort(Comparator.comparingInt(GridVertex::y));
    return collisions;
  }

  /** Returns all obstacles whose box intersects the horizontal segment, sorted by X. */
  List<GridVertex<V>> findHorizontalCollisions(int y, int startX, int endX) {
    int segMinX = Math.min(startX, endX);
    int segMaxX = Math.max(startX, endX);

    List<GridVertex<V>> collisions = new ArrayList<>();
    for (GridVertex<V> obst : obstacles) {
      int obstMinX = obst.x();
      int obstMaxX = obst.x() + obst.width() - 1;
      int obstMinY = obst.y();
      int obstMaxY = obst.y() + BOX_HEIGHT - 1;

      if (y >= obstMinY && y <= obstMaxY && segMaxX >= obstMinX && segMinX <= obstMaxX) {
        collisions.add(obst);
      }
    }

    collisions.sort(Comparator.comparingInt(GridVertex::x));
    return collisions;
  }

  /** Returns true if the column x is free of all obstacles between startY and endY. */
  boolean isColumnFree(int x, int startY, int endY) {
    return findVerticalCollisions(x, startY, endY).isEmpty();
  }

  /**
   * Picks the best detour column (left or right of obstacle), preferring the side closer to
   * targetX. Validates the column is free in the given Y-range. Widens search if both adjacent
   * columns are blocked.
   */
  int pickDetourColumn(GridVertex<V> obstacle, int targetX, int yStart, int yEnd) {
    int leftX = obstacle.x() - 1;
    int rightX = obstacle.x() + obstacle.width();

    // Determine preferred order based on target position, skipping negative candidates
    int firstChoice;
    int secondChoice;
    if (leftX < 0) {
      firstChoice = rightX;
      secondChoice = -1;
    } else if (Math.abs(targetX - rightX) <= Math.abs(targetX - leftX)) {
      firstChoice = rightX;
      secondChoice = leftX;
    } else {
      firstChoice = leftX;
      secondChoice = rightX;
    }

    if (isColumnFree(firstChoice, yStart, yEnd)) {
      return firstChoice;
    }
    if (secondChoice >= 0 && isColumnFree(secondChoice, yStart, yEnd)) {
      return secondChoice;
    }

    // Both adjacent columns blocked — widen search outward
    for (int offset = 2; offset < 50; offset++) {
      int tryRight = obstacle.x() + obstacle.width() - 1 + offset;
      if (isColumnFree(tryRight, yStart, yEnd)) {
        return tryRight;
      }
      int tryLeft = obstacle.x() - offset;
      if (tryLeft >= 0 && isColumnFree(tryLeft, yStart, yEnd)) {
        return tryLeft;
      }
    }

    // Fallback — should not happen in well-formed layouts
    return rightX;
  }

  /** Returns true if the segment from (x1,y1) to (x2,y2) collides with any obstacle. */
  boolean isSegmentBlocked(int x1, int y1, int x2, int y2) {
    if (x1 == x2) {
      // Vertical segment
      return !findVerticalCollisions(x1, y1, y2).isEmpty();
    } else if (y1 == y2) {
      // Horizontal segment
      return !findHorizontalCollisions(y1, x1, x2).isEmpty();
    }
    return false;
  }

  /**
   * Validates that no segment in the path collides with any obstacle. Throws IllegalStateException
   * on failure.
   */
  void validatePath(List<int[]> path) {
    for (int i = 0; i < path.size() - 1; i++) {
      int[] from = path.get(i);
      int[] to = path.get(i + 1);
      if (isSegmentBlocked(from[0], from[1], to[0], to[1])) {
        throw new IllegalStateException(
            "Path segment ("
                + from[0]
                + ","
                + from[1]
                + ")->("
                + to[0]
                + ","
                + to[1]
                + ") collides with an obstacle.");
      }
    }
  }
}
