package net.samyn.jgrapht.ascii.model;

import java.util.Objects;

/**
 * A vertex positioned on the grid with its label and computed box dimensions. Carries the original
 * graph vertex so that callers can map grid positions back to graph vertices unambiguously, even
 * when multiple vertices share the same label.
 *
 * @param <V> the graph vertex type
 */
public final class GridVertex<V> {

  private static final int HORIZONTAL_PADDING = 2;
  private static final int BORDER_WIDTH = 2;

  /** Height of a vertex box (top border + label row + bottom border). */
  public static final int BOX_HEIGHT = 3;

  private final V vertex;
  private final String label;
  private final int x;
  private final int y;
  private final int cachedWidth;

  public GridVertex(V vertex, String label, int x, int y) {
    this.vertex = vertex;
    // convert null label to empty string for the purpose of rendering
    this.label = Objects.requireNonNullElse(label, "");
    this.x = x;
    this.y = y;
    this.cachedWidth = DisplayWidth.width(this.label) + HORIZONTAL_PADDING + BORDER_WIDTH;
  }

  /** Returns the original graph vertex. */
  public V vertex() {
    return vertex;
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
    return cachedWidth;
  }

  /** Horizontal centre of the rendered box (the column where edges attach). */
  public int centreX() {
    return x + width() / 2;
  }

  /** Total height of the rendered box (top border, label row, bottom border). */
  public int height() {
    return BOX_HEIGHT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GridVertex)) return false;
    GridVertex<?> that = (GridVertex<?>) o;
    return x == that.x
        && y == that.y
        && Objects.equals(vertex, that.vertex)
        && Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vertex, label, x, y);
  }

  @Override
  public String toString() {
    return "GridVertex[vertex=" + vertex + ", label=" + label + ", x=" + x + ", y=" + y + "]";
  }
}
