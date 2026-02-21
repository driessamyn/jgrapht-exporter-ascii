package net.samyn.jgrapht.ascii.model;

/**
 * Utility class to measure displayWidth in Unicode characters
 */
public final class DisplayWidth {

    public static int width(String s) {
        int w = 0;

        if(null == s) return w;

        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            w += wcwidth(cp);
            i += Character.charCount(cp);
        }
        return w;
    }

    private static int wcwidth(int cp) {

        // Control characters
        if (cp == 0) return 0;
        if (cp < 32 || (cp >= 0x7f && cp < 0xa0))
            return 0;

        int type = Character.getType(cp);

        // Combining marks
        if (type == Character.NON_SPACING_MARK ||
                type == Character.ENCLOSING_MARK ||
                type == Character.COMBINING_SPACING_MARK)
            return 0;

        // Zero-width format chars (ZWJ etc.) ----
        if (type == Character.FORMAT)
            return 0;

        // Wide characters (East Asian) ----
        if (isWide(cp))
            return 2;

        return 1;
    }

    // Wide-range detection
    private static boolean isWide(int cp) {
        return
                (cp >= 0x1100 && cp <= 0x115F) ||  // Hangul Jamo
                        (cp >= 0x2329 && cp <= 0x232A) ||
                        (cp >= 0x2E80 && cp <= 0xA4CF && cp != 0x303F) ||
                        (cp >= 0xAC00 && cp <= 0xD7A3) ||  // Hangul
                        (cp >= 0xF900 && cp <= 0xFAFF) ||
                        (cp >= 0xFE10 && cp <= 0xFE19) ||
                        (cp >= 0xFE30 && cp <= 0xFE6F) ||
                        (cp >= 0xFF00 && cp <= 0xFF60) ||
                        (cp >= 0xFFE0 && cp <= 0xFFE6) ||
                        (cp >= 0x1F300 && cp <= 0x1F64F) || // Emoji blocks
                        (cp >= 0x1F900 && cp <= 0x1F9FF);
    }
}
