package me.shvaich.rcguard.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class StringUtil {
    public static String toLowerCase(String str) {
        return isEmpty(str) ? "" : str.toLowerCase(Locale.ROOT);
    }

    public static String toUpperCase(String str) { return isEmpty(str) ? "" : str.toUpperCase(Locale.ENGLISH); }

    public static String defaultIfEmpty(String str, String fallback) {
        return isEmpty(str) ? fallback : str;
    }

    public static String getIntWithSign(int value, boolean withZeroSign) {
        if (value == 0) return withZeroSign ? "+0" : "0";
        return (value > 0 ? "+" : "") + value;
    }

    public static boolean isEmpty(String str) { return str == null || str.isEmpty(); }

    public static String capitalize(String str) {
        if (isEmpty(str)) return "";
        char firstChar = str.charAt(0);
        if (firstChar < 'a' || firstChar > 'z')
            return str;

        final char[] chars = str.toCharArray();
        chars[0] = (char) (firstChar - ('a' - 'A'));
        return new String(chars);
    }

    public static String repeat(char ch, int n) {
        final char[] chars = new char[n];
        Arrays.fill(chars, ch);
        return new String(chars);
    }

    public static String joinIgnoreEmpty(String delimiter, CharSequence... elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringBuilder builder = null;
        for (final CharSequence cs : elements) {
            if (cs != null && cs.length() > 0) {
                (builder == null ? (builder = new StringBuilder()) : builder.append(delimiter)).append(cs);
            }
        }
        return builder == null ? "" : builder.toString();
    }

    public static boolean isColorCode(char ch) {
        return (ch >= '0' && ch <= '9')
                || (ch >= 'a' && ch <= 'f')
                || (ch >= 'A' && ch <= 'F');
    }

    public static boolean isFormatCode(char ch) {
        if (isColorCode(ch)) return true;
        return (ch >= 'k' && ch <= 'o') || (ch >= 'K' && ch <= 'O') || ch == 'r' || ch == 'R';
    }
}