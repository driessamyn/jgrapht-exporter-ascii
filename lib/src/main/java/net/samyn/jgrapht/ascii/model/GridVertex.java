package net.samyn.jgrapht.ascii.model;

import java.util.Objects;

/** A vertex positioned on the grid with its label and computed box dimensions. */
public final class GridVertex {

  private static final int HORIZONTAL_PADDING = 2;
  private static final int BORDER_WIDTH = 2;
  private static final int BOX_HEIGHT = 3;

  private final String label;
  private final int x;
  private final int y;

  public GridVertex(String label, int x, int y) {
    // convert null label to empty string for the purpose of rendering
    this.label = Objects.requireNonNullElse(label, "");
    this.x = x;
    this.y = y;
  }

  public String label() {
    return label;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }

  /** Total width of the rendered box including borders and padding. */
  public int width() {
    return DisplayWidth.width(label) + HORIZONTAL_PADDING + BORDER_WIDTH;
  }

  /** Total height of the rendered box (top border, label row, bottom border). */
  public int height() {
    return BOX_HEIGHT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GridVertex)) return false;
    GridVertex that = (GridVertex) o;
    return x == that.x && y == that.y && Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, x, y);
  }

  @Override
  public String toString() {
    return "GridVertex[label=" + label + ", x=" + x + ", y=" + y + "]";
  }
}
