package net.samyn.jgrapht.ascii.testutils;

public class TestUtils {

  public static String normaliseStringForComparison(String s) {
    if (s == null) {
      return null;
    }
    // Replace all Windows-style newlines with Unix-style newlines
    String normalised = s.replace("\r\n", "\n");
    // Remove all trailing whitespace (including newlines)
    normalised = normalised.stripTrailing();
    // Ensure no trailing newline remains
    while (normalised.endsWith("\n")) {
      normalised = normalised.substring(0, normalised.length() - 1);
    }
    return normalised;
  }
}
