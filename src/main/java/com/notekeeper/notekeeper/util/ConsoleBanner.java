package com.notekeeper.notekeeper.util;

/**
 * Writes the sign-in PIN to the server console only, as one plain line. The PIN is never part of
 * an HTTP response and never shown on a screen: people receive it by e-mail. The console line is
 * a development aid for when e-mail is not set up; remove the calls before production.
 */
public final class ConsoleBanner {

    private ConsoleBanner() {
    }

    /** Builds the line that {@link #printPin} writes. Separate so it can be tested. */
    public static String pinLine(String label, String code) {
        return "[2FA] PIN " + label + ": " + code;
    }

    public static void printPin(String label, String code) {
        System.out.println(pinLine(label, code));
    }
}
