package net.samyn.jgrapht.ascii.model;

/** Unicode box-drawing character constants used for rendering vertex boxes and edge paths. */
public final class BoxDrawing {

  private BoxDrawing() {}

  // Lines
  public static final char HORIZONTAL = '\u2500'; // ─
  public static final char VERTICAL = '\u2502'; // │
  public static final char HORIZONTAL_BOLD = '\u2501'; // ━
  public static final char VERTICAL_BOLD = '\u2503'; // ┃

  // Corners
  public static final char TOP_LEFT = '\u250C'; // ┌
  public static final char TOP_RIGHT = '\u2510'; // ┐
  public static final char BOTTOM_LEFT = '\u2514'; // └
  public static final char BOTTOM_RIGHT = '\u2518'; // ┘

  // T-junctions
  public static final char TEE_DOWN = '\u252C'; // ┬
  public static final char TEE_UP = '\u2534'; // ┴
  public static final char TEE_RIGHT = '\u251C'; // ├
  public static final char TEE_LEFT = '\u2524'; // ┤

  // Cross
  public static final char CROSS = '\u253C'; // ┼
}
