package net.samyn.jgrapht.ascii.routing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LaneTrackerTest {

  @Test
  void newTrackerHasNoOccupiedRows() {
    var tracker = new LaneTracker();
    assertFalse(tracker.isOccupied(5, 0, 10));
  }

  @Test
  void claimMakesRowOccupied() {
    var tracker = new LaneTracker();
    tracker.claim(3, 2, 8);
    assertTrue(tracker.isOccupied(3, 2, 8));
  }

  @Test
  void occupiedDetectsOverlapOnLeft() {
    var tracker = new LaneTracker();
    tracker.claim(3, 4, 10);
    // Query range [2, 6] overlaps with [4, 10]
    assertTrue(tracker.isOccupied(3, 2, 6));
  }

  @Test
  void occupiedDetectsOverlapOnRight() {
    var tracker = new LaneTracker();
    tracker.claim(3, 2, 6);
    // Query range [4, 10] overlaps with [2, 6]
    assertTrue(tracker.isOccupied(3, 4, 10));
  }

  @Test
  void occupiedDetectsContainedRange() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 20);
    // Query range [5, 10] is fully inside [0, 20]
    assertTrue(tracker.isOccupied(3, 5, 10));
  }

  @Test
  void occupiedReturnsFalseForNonOverlappingRange() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 5);
    // Query range [6, 10] does not overlap [0, 5]
    assertFalse(tracker.isOccupied(3, 6, 10));
  }

  @Test
  void occupiedReturnsFalseForDifferentRow() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 10);
    assertFalse(tracker.isOccupied(4, 0, 10));
  }

  @Test
  void multipleClaimsOnSameRow() {
    var tracker = new LaneTracker();
    tracker.claim(5, 0, 3);
    tracker.claim(5, 7, 12);
    assertTrue(tracker.isOccupied(5, 0, 3));
    assertTrue(tracker.isOccupied(5, 7, 12));
    // Gap between claims is free
    assertFalse(tracker.isOccupied(5, 4, 6));
  }

  @Test
  void findFreeRowReturnsCandidateWhenFree() {
    var tracker = new LaneTracker();
    assertEquals(3, tracker.findFreeRow(3, 6, 0, 10));
  }

  @Test
  void findFreeRowSkipsOccupiedRows() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 10);
    tracker.claim(4, 0, 10);
    // Row 5 is adjacent to claimed row 4 — spacing pushes to row 6
    assertEquals(6, tracker.findFreeRow(3, 8, 0, 10));
  }

  @Test
  void findFreeRowReturnsCandidateWhenNoOverlap() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 5);
    // Looking for free row in x-range [7, 12] — no overlap with claim at [0, 5]
    assertEquals(3, tracker.findFreeRow(3, 6, 7, 12));
  }

  @Test
  void findFreeRowReturnsCandidateWhenMaxYReached() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 10);
    tracker.claim(4, 0, 10);
    tracker.claim(5, 0, 10);
    // All rows 3-5 occupied, maxY is 5 — returns candidate (3) as fallback
    assertEquals(3, tracker.findFreeRow(3, 5, 0, 10));
  }

  @Test
  void findFreeRowSkipsRowAdjacentToClaimedRow() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 10);
    // Row 4 is free but adjacent to claimed row 3 — should skip to row 5
    assertEquals(5, tracker.findFreeRow(4, 8, 0, 10));
  }

  @Test
  void findFreeRowEnforcesGapBetweenConsecutiveClaims() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 10);
    // First free row with gap: row 5 (row 4 is adjacent to 3)
    int row1 = tracker.findFreeRow(3, 10, 0, 10);
    assertEquals(5, row1);
    tracker.claim(row1, 0, 10);
    // Next free row with gap: row 7 (row 6 is adjacent to 5)
    int row2 = tracker.findFreeRow(3, 10, 0, 10);
    assertEquals(7, row2);
  }

  @Test
  void findFreeRowFallsBackToUnspacedRowWhenFull() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 10);
    tracker.claim(5, 0, 10);
    // Rows 3,5 claimed. With spacing: row 4 adj to 3 and 5, row 6 adj to 5, row 7 is free.
    // But if maxY is 6, spacing can't be satisfied — fallback to row 7? No, maxY=6.
    // Row 4: adj to 3 and 5. Row 6: adj to 5. All spaced options exhausted.
    // Fallback pass: row 4 is free (unoccupied), so return 4.
    assertEquals(4, tracker.findFreeRow(3, 6, 0, 10));
  }

  @Test
  void findFreeRowSpacingOnlyAppliesToOverlappingXRange() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 5);
    // Row 4 in x-range [7, 12] — no overlap with claim at row 3 [0, 5]
    // So adjacency check also passes (different x-range)
    assertEquals(4, tracker.findFreeRow(4, 8, 7, 12));
  }

  @Test
  void adjacentRangesDoNotOverlap() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 5);
    // Range [5, 10] touches at boundary — should count as overlap (inclusive)
    assertTrue(tracker.isOccupied(3, 5, 10));
    // Range [6, 10] is truly separate
    assertFalse(tracker.isOccupied(3, 6, 10));
  }
}
