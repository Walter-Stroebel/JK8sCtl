/*
 *  Copyright (c) 2017 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.basicutils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class Utils {

    /**
     * Just compareNatural with dutch as language.
     */
    public static final Comparator<String> COMPARE_DUTCH_WITH_NUMBERS = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return compareNatural(dutch, o1, o2);
        }
    };
    /**
     * Simple implementation to sort natural names like (title givenname or
     * initials surname) as surname, (rest).
     */
    public static final Comparator<String> COMPARE_LAST_FIRST = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return compareLastFirst(o1, o2);
        }
    };
    /**
     * Simple implementation to sort, ignoring case, natural names like (title
     * givenname or initials surname) as surname, (rest).
     */
    public static final Comparator<String> COMPARE_LAST_FIRST_IGNORE_CASE = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return compareLastFirstIgnoreCase(o1, o2);
        }
    };
    /**
     * Actually French but does not matter for sorting words and numbers
     */
    private static Collator dutch = Collator.getInstance(Locale.FRANCE);
    /**
     * <p>
     * A string comparator that does case insensitive comparisons and handles
     * embedded numbers correctly.
     * </p>
     * <p>
     * <b>Do not use</b> if your app might ever run on any locale that uses more
     * than 7-bit ascii characters.
     * </p>
     */
    public static final Comparator<String> IGNORE_CASE_NATURAL_COMPARATOR_ASCII = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return compareNaturalIgnoreCaseAscii(o1, o2);
        }
    };
    /**
     * <p>
     * A string comparator that does case sensitive comparisons and handles
     * embedded numbers correctly.
     * </p>
     * <p>
     * <b>Do not use</b> if your app might ever run on any locale that uses more
     * than 7-bit ascii characters.
     * </p>
     */
    public static final Comparator<String> NATURAL_COMPARATOR_ASCII = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return compareNaturalAscii(o1, o2);
        }
    };

    public static String nbsp(int count) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < count; i++) {
            ret.append("&nbsp;");
        }
        return ret.toString();
    }

    /**
     * Utility function.
     *
     * @param out PrintWriter.
     * @param text Text with potential &lt;,&gt;, " or &amp; to encode.
     * @throws IOException If PrintWriter does.
     */
    public static void html(PrintWriter out, String text) throws IOException {
        out.print(html(text));
    }

    /**
     * Converts a string to a HTML entity encoded string, preserving any
     * existing entities. This method properly encodes a string like
     * &lt;&amp;EURO;&gt; to &amp;lt;&amp;EURO;&amp;gt;.
     *
     * @param text Text with potential &lt;,&gt;, " or &amp; to encode.
     * @return The text with any &lt;,&gt;, " or &amp; converted to &amp;lt;,
     * &amp;gt;, &amp;quot; and &amp;amp; while preserving any occurrences of
     * &amp;any;.
     */
    public static String html(String text) {
        if (text == null) {
            return "";
        }
        int amp = text.indexOf('&');
        if (amp >= 0) {
            int semi = text.indexOf(';', amp);
            if (semi > amp && semi - amp < 7) { // seems a valid html entity
                StringBuilder sb = new StringBuilder();
                if (amp > 0) {
                    sb.append(html(text.substring(0, amp)));
                }
                sb.append(text.substring(amp, semi));
                if (semi < text.length() - 1) {
                    sb.append(html(text.substring(semi + 1)));
                }
                return sb.toString();
            }
        }
        StringBuilder ret = new StringBuilder();
        for (char c : text.toCharArray()) {
            ret.append(htmlChar(c));
        }
        return ret.toString();
    }

    /**
     * Replaces any ASCII double quotes with &amp;quot;.
     *
     * @param text text to replace quotes in.
     * @return text with any quotes replaced.
     */
    public static String quote(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\"", "&quot;");
    }

    /**
     * Translates needed characters to entities.
     *
     * @param c Possibly dangerous character.
     * @return The character as a safe string.
     */
    public static String htmlChar(char c) {
        switch (c) {
            case '"':
                return ("&quot;");
            case '&':
                return ("&amp;");
            case '<':
                return ("&lt;");
            case '>':
                return ("&gt;");
            case '€':
                return ("&euro;");
            default:
                return (Character.toString(c));
        }
    }

    /**
     * CompareIgnoreCase that can handle null values.
     *
     * @param s1 String or null one
     * @param s2 String or null two
     * @return -1, 0, 1 where null &lt; something and null == null
     */
    public static int compareIgnoreCaseNull(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null && s2 != null) {
            return -1;
        }
        if (s1 != null && s2 == null) {
            return 1;
        }
        return s1.compareToIgnoreCase(s2);
    }

    /**
     * Simple implementation to sort natural names (title given name or initials
     * surname) as surname, (rest).
     *
     * @param o1 First name
     * @param o2 Second name
     * @return Ordering based on the last word, then any prior words.
     */
    public static int compareLastFirst(String o1, String o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null && o2 != null) {
            return -1;
        }
        if (o1 != null && o2 == null) {
            return 1;
        }
        if (o1.isEmpty() && o2.isEmpty()) {
            return 0;
        }
        if (o1.isEmpty() && !o2.isEmpty()) {
            return -1;
        }
        if (!o1.isEmpty() && o2.isEmpty()) {
            return 1;
        }
        int l1 = 0;
        int l2 = 0;
        if (o1.contains(" ")) {
            l1 = o1.lastIndexOf(' ');
        }
        if (o2.contains(" ")) {
            l2 = o2.lastIndexOf(' ');
        }
        int c = compareNatural(dutch, o1.substring(l1), o2.substring(l2));
        if (c != 0) {
            return c;
        }
        if (l1 == 0 && l2 == 0) {
            return 0;
        }
        if (l1 == 0 && l2 != 0) {
            return -1;
        }
        if (l1 != 0 && l2 == 0) {
            return 1;
        }
        return compareNatural(dutch, o1, o2);
    }

    /**
     * Simple implementation, ignoring case, to sort natural names (title given
     * name or initials surname) as surname, (rest).
     *
     * @param o1 First name
     * @param o2 Second name
     * @return Ordering based on the last word ignoring case, then any prior
     * words.
     */
    public static int compareLastFirstIgnoreCase(String o1, String o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null && o2 != null) {
            return -1;
        }
        if (o1 != null && o2 == null) {
            return 1;
        }
        if (o1.isEmpty() && o2.isEmpty()) {
            return 0;
        }
        if (o1.isEmpty() && !o2.isEmpty()) {
            return -1;
        }
        if (!o1.isEmpty() && o2.isEmpty()) {
            return 1;
        }
        return compareLastFirst(o1.toLowerCase(), o2.toLowerCase());
    }

    /**
     * <p>
     * Compares two strings using the given collator and comparing contained
     * numbers based on their numeric values.
     * </p>
     *
     * @param collator Which one to use.
     * @param s first string
     * @param t second string
     * @return zero if <code>s</code> and <code>t</code> are equal, a value less
     * than zero if <code>s</code> lexicographically precedes <code>t</code> and
     * a value larger than zero if <code>s</code> lexicographically follows
     * <code>t</code>
     */
    public static int compareNatural(Collator collator, String s, String t) {
        return compareNatural(s, t, true, collator);
    }

    /**
     * <p>
     * Compares two strings using the current locale's rules and comparing
     * contained numbers based on their numeric values.
     * </p>
     * <p>
     * This is probably the best default comparison to use.
     * </p>
     * <p>
     * If you know that the texts to be compared are in a certain language that
     * differs from the default locale's langage, then get a collator for the
     * desired locale ({@link java.text.Collator#getInstance(java.util.Locale)})
     * and pass it to
     * {@link #compareNatural(java.text.Collator, String, String)}
     * </p>
     *
     * @param s first string
     * @param t second string
     * @return zero if <code>s</code> and <code>t</code> are equal, a value less
     * than zero if <code>s</code> lexicographically precedes <code>t</code> and
     * a value larger than zero if <code>s</code> lexicographically follows
     * <code>t</code>
     */
    public static int compareNatural(String s, String t) {
        return compareNatural(s, t, false, Collator.getInstance());
    }

    /**
     * @param s first string
     * @param t second string
     * @param caseSensitive treat characters differing in case only as equal -
     * will be ignored if a collator is given
     * @param collator used to compare subwords that aren't numbers - if null,
     * characters will be compared individually based on their Unicode value
     * @return zero if <code>s</code> and <code>t</code> are equal, a value less
     * than zero if <code>s</code> lexicographically precedes <code>t</code> and
     * a value larger than zero if <code>s</code> lexicographically follows
     * <code>t</code>
     */
    private static int compareNatural(String s, String t, boolean caseSensitive, Collator collator) {
        int sIndex = 0;
        int tIndex = 0;
        int sLength = s.length();
        int tLength = t.length();
        while (true) {
            // both character indices are after a subword (or at zero)
            // Check if one string is at end
            if (sIndex == sLength && tIndex == tLength) {
                return 0;
            }
            if (sIndex == sLength) {
                return -1;
            }
            if (tIndex == tLength) {
                return 1;
            }
            // Compare sub word
            char sChar = s.charAt(sIndex);
            char tChar = t.charAt(tIndex);
            boolean sCharIsDigit = Character.isDigit(sChar);
            boolean tCharIsDigit = Character.isDigit(tChar);
            if (sCharIsDigit && tCharIsDigit) {
                // Compare numbers
                // skip leading 0s
                int sLeadingZeroCount = 0;
                while (sChar == '0') {
                    ++sLeadingZeroCount;
                    ++sIndex;
                    if (sIndex == sLength) {
                        break;
                    }
                    sChar = s.charAt(sIndex);
                }
                int tLeadingZeroCount = 0;
                while (tChar == '0') {
                    ++tLeadingZeroCount;
                    ++tIndex;
                    if (tIndex == tLength) {
                        break;
                    }
                    tChar = t.charAt(tIndex);
                }
                boolean sAllZero = sIndex == sLength || !Character.isDigit(sChar);
                boolean tAllZero = tIndex == tLength || !Character.isDigit(tChar);
                if (sAllZero && tAllZero) {
                    continue;
                }
                if (sAllZero && !tAllZero) {
                    return -1;
                }
                if (tAllZero) {
                    return 1;
                }
                int diff = 0;
                do {
                    if (diff == 0) {
                        diff = sChar - tChar;
                    }
                    ++sIndex;
                    ++tIndex;
                    if (sIndex == sLength && tIndex == tLength) {
                        return diff != 0 ? diff : sLeadingZeroCount - tLeadingZeroCount;
                    }
                    if (sIndex == sLength) {
                        if (diff == 0) {
                            return -1;
                        }
                        return Character.isDigit(t.charAt(tIndex)) ? -1 : diff;
                    }
                    if (tIndex == tLength) {
                        if (diff == 0) {
                            return 1;
                        }
                        return Character.isDigit(s.charAt(sIndex)) ? 1 : diff;
                    }
                    sChar = s.charAt(sIndex);
                    tChar = t.charAt(tIndex);
                    sCharIsDigit = Character.isDigit(sChar);
                    tCharIsDigit = Character.isDigit(tChar);
                    if (!sCharIsDigit && !tCharIsDigit) {
                        // both number sub words have the same length
                        if (diff != 0) {
                            return diff;
                        }
                        break;
                    }
                    if (!sCharIsDigit) {
                        return -1;
                    }
                    if (!tCharIsDigit) {
                        return 1;
                    }
                } while (true);
            } else {
                // Compare words
                if (collator != null) {
                    // To use the collator the whole subwords have to be compared - character-by-character comparision
                    // is not possible. So find the two subwords first
                    int aw = sIndex;
                    int bw = tIndex;
                    do {
                        ++sIndex;
                    } while (sIndex < sLength && !Character.isDigit(s.charAt(sIndex)));
                    do {
                        ++tIndex;
                    } while (tIndex < tLength && !Character.isDigit(t.charAt(tIndex)));
                    String as = s.substring(aw, sIndex);
                    String bs = t.substring(bw, tIndex);
                    int subwordResult = collator.compare(as, bs);
                    if (subwordResult != 0) {
                        return subwordResult;
                    }
                } else {
                    // No collator specified. All characters should be ascii only. Compare character-by-character.
                    do {
                        if (sChar != tChar) {
                            if (caseSensitive) {
                                return sChar - tChar;
                            }
                            sChar = Character.toUpperCase(sChar);
                            tChar = Character.toUpperCase(tChar);
                            if (sChar != tChar) {
                                sChar = Character.toLowerCase(sChar);
                                tChar = Character.toLowerCase(tChar);
                                if (sChar != tChar) {
                                    return sChar - tChar;
                                }
                            }
                        }
                        ++sIndex;
                        ++tIndex;
                        if (sIndex == sLength && tIndex == tLength) {
                            return 0;
                        }
                        if (sIndex == sLength) {
                            return -1;
                        }
                        if (tIndex == tLength) {
                            return 1;
                        }
                        sChar = s.charAt(sIndex);
                        tChar = t.charAt(tIndex);
                        sCharIsDigit = Character.isDigit(sChar);
                        tCharIsDigit = Character.isDigit(tChar);
                    } while (!sCharIsDigit && !tCharIsDigit);
                }
            }
        }
    }

    /**
     * <p>
     * Compares two strings using each character's Unicode value for non-digit
     * characters and the numeric values off any contained numbers.
     * </p>
     * <p>
     * (This will probably make sense only for strings containing 7-bit ascii
     * characters only.)
     * </p>
     *
     * @param s String one
     * @param t String two
     * @return zero if <code>s</code> and <code>t</code> are equal, a value less
     * than zero if <code>s</code> lexicographically precedes <code>t</code> and
     * a value larger than zero if <code>s</code> lexicographically follows
     * <code>t</code>
     */
    public static int compareNaturalAscii(String s, String t) {
        return compareNatural(s, t, true, null);
    }

    /**
     * <p>
     * Compares two strings using each character's Unicode value - ignoring
     * upper/lower case - for non-digit characters and the numeric values of any
     * contained numbers.
     * </p>
     * <p>
     * (This will probably make sense only for strings containing 7-bit ascii
     * characters only.)
     * </p>
     *
     * @param s String one
     * @param t String two
     * @return zero if <code>s</code> and <code>t</code> are equal, a value less
     * than zero if <code>s</code> lexicographically precedes <code>t</code> and
     * a value larger than zero if <code>s</code> lexicographically follows
     * <code>t</code>
     */
    public static int compareNaturalIgnoreCaseAscii(String s, String t) {
        return compareNatural(s, t, false, null);
    }

    /**
     * Returns a comparator that compares contained numbers based on their
     * numeric values and compares other parts using the current locale's order
     * rules.
     * <p>
     * For example in German locale this will be a comparator that handles
     * umlauts correctly and ignores upper/lower case differences.
     * </p>
     *
     * @return
     * <p>
     * A string comparator that uses the current locale's order rules and
     * handles embedded numbers correctly.
     * </p>
     * @see #getNaturalComparator(java.text.Collator)
     */
    public static Comparator<String> getNaturalComparator() {
        Collator collator = Collator.getInstance();
        return getNaturalComparator(collator);
    }

    /**
     * Returns a comparator that compares contained numbers based on their
     * numeric values and compares other parts using the given collator.
     *
     * @param collator used for locale specific comparison of text (non-number)
     * subwords - must not be null
     * @return
     * <p>
     * A string comparator that uses the given Collator to compare subwords and
     * handles embedded numbers correctly.
     * </p>
     * @see #getNaturalComparator()
     */
    public static Comparator<String> getNaturalComparator(final Collator collator) {
        if (collator == null) {
            // it's important to explicitly handle this here - else the bug will manifest anytime later in possibly
            // unrelated code that tries to use the comparator
            throw new NullPointerException("collator must not be null");
        }
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return compareNatural(collator, o1, o2);
            }
        };
    }

    /**
     * Returns a comparator that compares contained numbers based on their
     * numeric values and compares other parts based on each character's Unicode
     * value.
     *
     * @return
     * <p>
     * a string comparator that does case sensitive comparisons on pure ascii
     * strings and handles embedded numbers correctly.
     * </p>
     * <b>Do not use</b> if your app might ever run on any locale that uses more
     * than 7-bit ascii characters.
     * @see #getNaturalComparator()
     * @see #getNaturalComparator(java.text.Collator)
     */
    public static Comparator<String> getNaturalComparatorAscii() {
        return NATURAL_COMPARATOR_ASCII;
    }

    /**
     * Returns a comparator that compares contained numbers based on their
     * numeric values and compares other parts based on each character's Unicode
     * value while ignore upper/lower case differences. <b>Do not use</b> if
     * your app might ever run on any locale that uses more than 7-bit ascii
     * characters.
     *
     * @return
     * <p>
     * a string comparator that does case insensitive comparisons on pure ascii
     * strings and handles embedded numbers correctly.
     * </p>
     * @see #getNaturalComparator()
     * @see #getNaturalComparator(java.text.Collator)
     */
    public static Comparator<String> getNaturalComparatorIgnoreCaseAscii() {
        return IGNORE_CASE_NATURAL_COMPARATOR_ASCII;
    }

    public static String enc(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            // no fail
        }
        return value;
    }

}
