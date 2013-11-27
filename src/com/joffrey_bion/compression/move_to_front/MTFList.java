package com.joffrey_bion.compression.move_to_front;

import java.util.ArrayList;

/**
 * An {@code MTFList} is basically an {@link ArrayList} which is initialized with the
 * BMP set of characters in the constructor, and provides the
 * {@link #moveToFront(int, char)} method for the algorithm purpose.
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
@SuppressWarnings("serial")
class MTFList extends ArrayList<Character> {

    /**
     * Creates a new {@code MTFList} with all characters (in the Basic Multilingual
     * Pane) in lexicographical order.
     */
    public MTFList() {
        super();
        reset();
    }

    /**
     * Resets this {@code MTFList} to the lexicographical order.
     */
    public void reset() {
        clear();
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            add(MTFCharShift.intToChar(i));
        }
    }

    /**
     * Moves the specified character (which is at the given position) to the front of
     * the list.
     * 
     * @param position
     *            The current position of the character to move to the front.
     * @param c
     *            The character to move to the front. It is not really needed here,
     *            because it could be retrieved. However, for an efficiency purpose,
     *            it is faster to give it as a parameter since the caller always have
     *            it already.
     */
    public void moveToFront(int position, char c) {
        for (int i = position; i > 0; i--)
            set(i, get(i - 1));
        set(0, c);
    }

}
