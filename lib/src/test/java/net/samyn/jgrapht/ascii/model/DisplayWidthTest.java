package net.samyn.jgrapht.ascii.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplayWidthTest {

    // ASCII
    @Test
    void asciiCharactersHaveWidthOne() {
        assertEquals(5, DisplayWidth.width("hello"));
    }
    @Test void asciiWithSpaces() {
        assertEquals(11, DisplayWidth.width("hello world"));
    }

    // Empty / trivial
    @Test
    void emptyStringHasZeroWidth() {
        assertEquals(0, DisplayWidth.width(""));
    }
    @Test
    void nullCharacterHasZeroWidth() {
        assertEquals(0, DisplayWidth.width("\0"));
    }

    // Control characters
    @Test
    void controlCharactersAreZeroWidth() {
        assertEquals(3, DisplayWidth.width("a\nb\tc"));
    }

    // Surrogate pairs / emoji
    @Test
    void emojiCountsAsTwoColumns() {
        assertEquals(2, DisplayWidth.width("🙂"));
    }
    @Test
    void multipleEmoji() {
        assertEquals(4, DisplayWidth.width("🙂🙂"));
    }

    // Combining marks
    @Test
    void combiningAccentDoesNotIncreaseWidth() {
        String composed = "e\u0301";
        // e + combining acute
        assertEquals(1, DisplayWidth.width(composed));
    }
    @Test
    void combiningSequenceInWord() {
        String s = "Cafe\u0301";
        // Café
        assertEquals(4, DisplayWidth.width(s));
    }

    // Wide CJK characters
    @Test
    void cjkCharacterIsDoubleWidth() {
        assertEquals(2, DisplayWidth.width("漢"));
    }
    @Test
    void multipleCjkCharacters() {
        assertEquals(4, DisplayWidth.width("漢字"));
    }

    // Mixed content
    @Test
    void mixedAsciiAndCjk() {
        assertEquals(6, DisplayWidth.width("ab漢cd"));
    }
    @Test
    void mixedAsciiEmojiAndCombining() {
        String s = "A🙂e\u0301Z";
        // A(1) + 🙂(2) + é(1) + Z(1)
        assertEquals(5, DisplayWidth.width(s));
    }

    // Format / ZWJ
    @Test
    void zeroWidthJoinerIsIgnored() {
        String s = "A\u200DB";
        assertEquals(2, DisplayWidth.width(s));
    }

    @Test
    void nullWidth() {
        assertEquals(0, DisplayWidth.width(null));
    }
}
