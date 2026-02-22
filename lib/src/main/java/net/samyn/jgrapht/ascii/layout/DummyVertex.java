package net.samyn.jgrapht.ascii.layout;

/**
 * A synthetic vertex inserted on intermediate layers to split long edges into single-layer
 * segments. Identity-based equality ensures each dummy is unique.
 *
 * <p>The {@code toString()} encodes the original edge endpoints and layer for deterministic
 * ordering in {@link CrossingMinimiser}, independent of edge iteration order.
 */
final class DummyVertex {

  private final String sourceLabel;
  private final String targetLabel;
  private final int layer;

  DummyVertex(String sourceLabel, String targetLabel, int layer) {
    this.sourceLabel = sourceLabel;
    this.targetLabel = targetLabel;
    this.layer = layer;
  }

  @Override
  public String toString() {
    return "dummy[" + sourceLabel + "->" + targetLabel + "@" + layer + "]";
  }
}
