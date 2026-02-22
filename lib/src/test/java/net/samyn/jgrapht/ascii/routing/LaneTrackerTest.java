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
    assertEquals(5, tracker.findFreeRow(3, 6, 0, 10));
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
  void adjacentRangesDoNotOverlap() {
    var tracker = new LaneTracker();
    tracker.claim(3, 0, 5);
    // Range [5, 10] touches at boundary — should count as overlap (inclusive)
    assertTrue(tracker.isOccupied(3, 5, 10));
    // Range [6, 10] is truly separate
    assertFalse(tracker.isOccupied(3, 6, 10));
  }
}
