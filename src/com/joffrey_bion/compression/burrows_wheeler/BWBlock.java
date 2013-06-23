package com.joffrey_bion.compression.burrows_wheeler;

/**
 * Represents a block of text transformed via Burrows-Wheeler transform. It contains
 * both the transformed block and the index of the source block in the list of
 * rotations, as specified by the algorithm paper.
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
public class BWBlock {

    /** The transformed text bloc */
    public final String content;

    /** The index of the source bloc in the sorted list of rotations */
    public final int index;

    /**
     * Creates a Burrows-Wheeler transformed block.
     * 
     * @param block
     *            A block transformed via Burrows-Wheeler transform
     * @param index
     *            The index of the source bloc in the sorted list of rotations
     */
    public BWBlock(String block, int index) {
        this.content = block;
        this.index = index;
    }

    @Override
    public String toString() {
        return index + ", " + content;
    }
}
