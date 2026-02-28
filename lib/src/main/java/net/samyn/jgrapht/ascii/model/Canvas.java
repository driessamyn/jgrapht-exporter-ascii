package net.samyn.jgrapht.ascii.model;

import java.util.Arrays;

/**
 * A mutable 2D character grid used for rendering ASCII/Unicode graph output. Automatically expands
 * when writing beyond current bounds.
 */
public class Canvas {
  private char[][] grid;
  private int width;
  private int height;

  public Canvas(int width, int height) {
    this.width = width;
    this.height = height;
    this.grid = new char[height][width];
    for (char[] row : grid) {
      Arrays.fill(row, ' ');
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public char charAt(int x, int y) {
    return grid[y][x];
  }

  public void putChar(int x, int y, char c) {
    ensureCapacity(x + 1, y + 1);
    grid[y][x] = c;
  }

  public void putString(int x, int y, String s) {
    ensureCapacity(x + s.length(), y + 1);
    for (int i = 0; i < s.length(); i++) {
      grid[y][x + i] = s.charAt(i);
    }
  }

  public void putVerticalString(int x, int y, String s) {
    ensureCapacity(x + 1, y + s.length());
    for (int i = 0; i < s.length(); i++) {
      grid[y + i][x] = s.charAt(i);
    }
  }

  public void putCharWithPrecedence(int x, int y, char newChar) {
    ensureCapacity(x + 1, y + 1);
    char existingChar = grid[y][x];

    if (newChar == ' ') { // New char is empty space, do not overwrite
      return;
    }
    if (existingChar == ' ') { // Existing is empty space, always draw new char
      grid[y][x] = newChar;
      return;
    }

    // --- Arrow protection: arrows are never overwritten ---
    if (isArrow(existingChar)) {
      return;
    }

    // --- Line crossing line to form a cross ---
    if ((isUnicodeHorizontalLine(existingChar) && isUnicodeVerticalLine(newChar))
        || (isUnicodeVerticalLine(existingChar) && isUnicodeHorizontalLine(newChar))) {
      grid[y][x] = '\u253C'; // ┼
      return;
    }
    if ((isAsciiHorizontalLine(existingChar) && isAsciiVerticalLine(newChar))
        || (isAsciiVerticalLine(existingChar) && isAsciiHorizontalLine(newChar))) {
      grid[y][x] = '+'; // ASCII Cross
      return;
    }

    // --- Precedence: Junctions/Corners over Lines ---
    // If new char is a line and existing is a stronger junction/corner, preserve existing
    if (isLine(newChar) && isJunctionOrCorner(existingChar)) {
      return;
    }
    // If new char is a junction/corner and existing is a line, overwrite with new char
    if (isJunctionOrCorner(newChar) && isLine(existingChar)) {
      grid[y][x] = newChar;
      return;
    }

    // --- Arrows ---
    if (isArrow(newChar)) {
      if (!isJunctionOrCorner(existingChar)) { // Overwrite if not a strong junction/corner
        grid[y][x] = newChar;
      }
      return;
    }

    // Default: overwrite (e.g., Junction over Junction, Corner over Corner, etc.)
    grid[y][x] = newChar;
  }

  private static boolean isUnicodeHorizontalLine(char c) {
    return c == '\u2500' || c == '\u2501'; // ─ or ━
  }

  private static boolean isUnicodeVerticalLine(char c) {
    return c == '\u2502' || c == '\u2503'; // │ or ┃
  }

  private static boolean isAsciiHorizontalLine(char c) {
    return c == '-';
  }

  private static boolean isAsciiVerticalLine(char c) {
    return c == '|';
  }

  private static boolean isLine(char c) { // General line check (Unicode or ASCII)
    return isUnicodeHorizontalLine(c)
        || isUnicodeVerticalLine(c)
        || isAsciiHorizontalLine(c)
        || isAsciiVerticalLine(c);
  }

  private static boolean isUnicodeJunction(char c) {
    return c == '\u252C' || c == '\u2534' || c == '\u251C' || c == '\u2524' || c == '\u253C';
  }

  private static boolean isAsciiJunction(char c) {
    return c == '+'; // ASCII '+' acts as junction/cross/corner
  }

  private static boolean isJunction(char c) { // General junction check
    return isUnicodeJunction(c) || isAsciiJunction(c);
  }

  private static boolean isUnicodeCorner(char c) {
    return c == '\u250C' || c == '\u2510' || c == '\u2514' || c == '\u2518';
  }

  private static boolean isAsciiCorner(char c) {
    return c == '+'; // ASCII '+' acts as junction/cross/corner
  }

  private static boolean isCorner(char c) { // General corner check
    return isUnicodeCorner(c) || isAsciiCorner(c);
  }

  private static boolean isJunctionOrCorner(char c) { // General check
    return isJunction(c) || isCorner(c);
  }

  private static boolean isArrow(char c) {
    return c == 'v' || c == '^' || c == '<' || c == '>';
  }

  @Override
  public String toString() {
    // Find last non-blank line
    int lastRow = height - 1;
    while (lastRow > 0 && isBlankRow(lastRow)) {
      lastRow--;
    }

    var sb = new StringBuilder();
    for (int y = 0; y <= lastRow; y++) {
      if (y > 0) {
        sb.append('\n');
      }
      // Trim trailing spaces on each line
      int lastCol = width - 1;
      while (lastCol > 0 && grid[y][lastCol] == ' ') {
        lastCol--;
      }
      // If the entire row is spaces, append nothing for this line
      if (lastCol == 0 && grid[y][0] == ' ') {
        continue;
      }
      for (int x = 0; x <= lastCol; x++) {
        sb.append(grid[y][x]);
      }
    }
    return sb.toString();
  }

  private boolean isBlankRow(int y) {
    for (int x = 0; x < width; x++) {
      if (grid[y][x] != ' ') {
        return false;
      }
    }
    return true;
  }

  private void ensureCapacity(int requiredWidth, int requiredHeight) {
    if (requiredWidth <= width && requiredHeight <= height) {
      return;
    }
    int newWidth = Math.max(width, requiredWidth);
    int newHeight = Math.max(height, requiredHeight);
    char[][] newGrid = new char[newHeight][newWidth];
    for (char[] row : newGrid) {
      Arrays.fill(row, ' ');
    }
    for (int y = 0; y < height; y++) {
      System.arraycopy(grid[y], 0, newGrid[y], 0, width);
    }
    grid = newGrid;
    width = newWidth;
    height = newHeight;
  }
}
