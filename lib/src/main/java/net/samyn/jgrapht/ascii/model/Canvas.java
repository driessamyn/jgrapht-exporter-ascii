package net.samyn.jgrapht.ascii.model;

import java.util.Arrays;

/**
 * A mutable 2D character grid used for rendering ASCII/Unicode graph output.
 * Automatically expands when writing beyond current bounds.
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
