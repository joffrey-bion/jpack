package compression.move_to_front;

/**
 * This helper class performs a circular rotation, shifting character codes, and
 * provides static methods to convert a character into its code and vice-versa.
 * <p>
 * The purpose of such an operation is only to change the set of characters that will
 * be used the most in the MTF encoded stream. Indeed, we know that the lowest codes
 * will be used often, while the highest codes won't be used often (this is the very
 * goal of MTF encoding). Since the lowest codes in the charset are control
 * characters, the encoded stream is not really convenient to view and to check
 * (while testing). That's why the charset is shifted to start with more readable
 * values such as the numbers or the letters.
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 * @see MoveToFront
 */
class MTFCharShift {

    private static final char MOST_RECURRENT_CHARS_START = 'A';

    /** Gives the character corresponding to the given code. */
    public static char intToChar(int i) {
        return (char) rangeModulo(i + MOST_RECURRENT_CHARS_START, Character.MIN_VALUE,
                Character.MAX_VALUE);
    }

    /** Gives the code of the given character. */
    public static int charToInt(char c) {
        return rangeModulo(c - MOST_RECURRENT_CHARS_START, Character.MIN_VALUE,
                Character.MAX_VALUE);
    }

    /**
     * Performs a modulo operation that brings {@code value} between {@code min} and
     * {@code max}.
     * 
     * @param value
     *            The value from which to take the modulo.
     * @param min
     *            The minimum value of the target range.
     * @param max
     *            The maximum value of the target range.
     */
    private static int rangeModulo(int value, int min, int max) {
        int rangeLength = max + 1 - min;
        int valueShifted = (value - min) % rangeLength;
        // Java accepts negative modulo (-1 % 2 == -1 instead of 1), this is corrected here
        return (valueShifted < 0 ? valueShifted + rangeLength : valueShifted) + min;
    }

}
