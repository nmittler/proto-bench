package com.google.protobench;

import java.util.Random;

/**
 * Utilities for benchmarking UTF-8.
 */
final class Utf8Utils {
  private Utf8Utils() {
  }

  static class MaxCodePoint {
    final int value;

    /**
     * Convert the input string to a code point.  Accepts regular decimal numerals, hex strings, and
     * some symbolic names meaningful to humans.
     */
    private static int decode(String userFriendly) {
      try {
        return Integer.decode(userFriendly);
      } catch (NumberFormatException ignored) {
        if (userFriendly.matches("(?i)(?:American|English|ASCII)")) {
          // 1-byte UTF-8 sequences - "American" ASCII text
          return 0x80;
        } else if (userFriendly.matches("(?i)(?:Danish|Latin|Western.*European)")) {
          // Mostly 1-byte UTF-8 sequences, mixed with occasional 2-byte
          // sequences - "Western European" text
          return 0x90;
        } else if (userFriendly.matches("(?i)(?:Greek|Cyrillic|European|ISO.?8859)")) {
          // Mostly 2-byte UTF-8 sequences - "European" text
          return 0x800;
        } else if (userFriendly.matches("(?i)(?:Chinese|Han|Asian|BMP)")) {
          // Mostly 3-byte UTF-8 sequences - "Asian" text
          return Character.MIN_SUPPLEMENTARY_CODE_POINT;
        } else if (userFriendly.matches("(?i)(?:Cuneiform|rare|exotic|supplementary.*)")) {
          // Mostly 4-byte UTF-8 sequences - "rare exotic" text
          return Character.MAX_CODE_POINT;
        } else {
          throw new IllegalArgumentException("Can't decode codepoint " + userFriendly);
        }
      }
    }

    public static MaxCodePoint valueOf(String userFriendly) {
      return new MaxCodePoint(userFriendly);
    }

    public MaxCodePoint(String userFriendly) {
      value = decode(userFriendly);
    }
  }

  /**
   * Creates an array of random strings.
   *
   * @param stringCount  the number of strings to be created.
   * @param charCount    the number of characters per string.
   * @param maxCodePoint the maximum code point for the characters in the strings.
   * @return an array of random strings.
   */
  static String[] randomStrings(int stringCount, int charCount, MaxCodePoint maxCodePoint) {
    final long seed = 99;
    final Random rnd = new Random(seed);
    String[] strings = new String[stringCount];
    for (int i = 0; i < stringCount; i++) {
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < charCount; j++) {
        int codePoint;
        do {
          codePoint = rnd.nextInt(maxCodePoint.value);
        } while (Utf8Utils.isSurrogate(codePoint));
        sb.appendCodePoint(codePoint);
      }
      strings[i] = sb.toString();
    }
    return strings;
  }

  /**
   * Character.isSurrogate was added in Java SE 7.
   */
  static boolean isSurrogate(int c) {
    return Character.MIN_HIGH_SURROGATE <= c && c <= Character.MAX_LOW_SURROGATE;
  }
}

