package net.samyn.jgrapht.ascii.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks which horizontal lanes (row + x-range) have been claimed by edge segments. Used by the
 * edge router to spread horizontal segments across different rows within inter-layer gaps, avoiding
 * visual overlap.
 */
class LaneTracker {

  private final Map<Integer, List<int[]>> claimed = new HashMap<>();

  /** Claim a horizontal segment at the given row across [minX, maxX] (inclusive). */
  void claim(int y, int minX, int maxX) {
    claimed.computeIfAbsent(y, k -> new ArrayList<>()).add(new int[] {minX, maxX});
  }

  /** Returns {@code true} if any claimed segment at the given row overlaps [minX, maxX]. */
  boolean isOccupied(int y, int minX, int maxX) {
    List<int[]> segments = claimed.get(y);
    if (segments == null) {
      return false;
    }
    for (int[] seg : segments) {
      if (seg[0] <= maxX && minX <= seg[1]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Starting from {@code candidateY}, search downward (up to {@code maxY}) for the first row not
   * occupied in [minX, maxX]. Returns {@code candidateY} if already free, or {@code candidateY} as
   * a fallback if all rows up to {@code maxY} are occupied.
   */
  int findFreeRow(int candidateY, int maxY, int minX, int maxX) {
    for (int y = candidateY; y <= maxY; y++) {
      if (!isOccupied(y, minX, maxX)) {
        return y;
      }
    }
    return candidateY;
  }
}
