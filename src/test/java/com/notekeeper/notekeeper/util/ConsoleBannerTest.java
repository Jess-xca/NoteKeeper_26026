package com.notekeeper.notekeeper.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConsoleBannerTest {

    @Test
    void pinIsOneplainLineWithTheLabelAndTheCode() {
        String line = ConsoleBanner.pinLine("for ada@example.com", "482913");

        assertEquals("[2FA] PIN for ada@example.com: 482913", line);
        assertFalse(line.contains("\u001B"), "no colour or bold escape codes");
        assertFalse(line.contains("\n"), "a single line");
    }
}
