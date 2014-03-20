package com.jbion.compression.move_to_front;

/**
 * A class used to apply the move-to-front transform to characters in a stream, or to
 * a block of characters (given as a {@code String}).
 * <p>
 * <b>Important:</b> All characters in the same message have to be encoded (or
 * decoded) by the same {@code MoveToFront} object. However, the list has to be
 * reinitialized (or a new object created) between encoding and decoding processes.
 * </p>
 * <p>
 * Characters can be encoded using the original transform or the adapted transform.
 * <ul>
 * <li>The original transform represents a character by its index in the list of
 * recent used symbols, as an {@code int}. This transform is applied via the methods
 * {@link #rawTransform(char)} and {@link #rawReverse(int)}. The drawback of this
 * direct encoding is that an {@code int} binary representation is twice as big as a
 * {@code char}.</li>
 * <li>The adapted transform gives a {@code char} representation of the index, so
 * that it can actually be used for compression, without making the file bigger. This
 * adapted transform is applied via the methods {@link #encode(char)} and
 * {@link #decode(char)}, and is also used in the block transforming methods.</li>
 * </ul>
 * </p>
 */
public class MoveToFront {

    private MTFList list;

    /**
     * Creates a new {@code MoveToFront} encoder or decoder.
     */
    public MoveToFront() {
        list = new MTFList();
    }

    /**
     * Transforms a single character.
     * 
     * @param c
     *            The character to transform via MTF.
     * @return The index of c in the MTF list.
     * @see #encode(char)
     */
    public int rawTransform(char c) {
        int index = list.indexOf(c);
        list.moveToFront(index, c);
        return index;
    }

    /**
     * Retrieves a single character.
     * 
     * @param index
     *            An index in the MTF list.
     * @return The character located at the given index in the MTF list.
     * @see #decode(char)
     */
    public char rawReverse(int index) {
        char c = list.get(index);
        list.moveToFront(index, c);
        return c;
    }

    /**
     * Transforms a single character.
     * 
     * @param c
     *            The character to transform via MTF.
     * @return A character representation of the index of c in the MTF list.
     * @see #rawTransform(char)
     */
    public char encode(char c) {
        // does not use rawTransform() to be more efficient
        int index = list.indexOf(c);
        list.moveToFront(index, c);
        return MTFCharShift.intToChar(index);
    }

    /**
     * Retrieves the character corresponding to the given index (transformed
     * character).
     * 
     * @param i
     *            The character representation of an index in the MTF list.
     * @return The character located at the given index in the MTF list.
     * @see #rawReverse(int)
     */
    public char decode(char i) {
        // does not use rawReverse() to be more efficient
        int index = MTFCharShift.charToInt(i);
        char c = list.get(index);
        list.moveToFront(index, c);
        return c;
    }

    /**
     * Transforms a block of characters.
     * 
     * @param block
     *            The block to transform via MTF.
     * @return A {@code String} representation of the indexes of the block's
     *         characters in the MTF list, as if all characters were transformed one
     *         by one via {@link #encode(char)}.
     * @see #encode(char)
     */
    public String encodeBlock(String block) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < block.length(); i++) {
            sb.append(encode(block.charAt(i)));
        }
        return sb.toString();
    }

    /**
     * Retrieves the block corresponding to the given transformed block.
     * 
     * @param block
     *            A {@code String} whose characters are the representations of
     *            indexes in the MTF list.
     * @return The reversed blocked as if all characters were reversed via
     *         {@link #decode(char)}.
     * @see #decode(char)
     */
    public String decodeBlock(String block) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < block.length(); i++) {
            sb.append(decode(block.charAt(i)));
        }
        return sb.toString();
    }

    /**
     * Resets the list of characters to the lexicographical order. This method has to
     * be called between 2 encoding or decoding operations.
     */
    public void reset() {
        list.reset();
    }
}
