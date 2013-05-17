package com.joffrey_bion.jpack.compression.burrows_wheeler;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Burrows-Wheeler algorithm requires a conceptual matrix containing all the
 * rotations of the source block to transform. This is what this class represents.
 * All the methods needed by the algorithm to manipulate this matrix are provided
 * here as well. Here is an example of a rotation matrix (for the source text
 * "Brave") before being sorted:
 * 
 * <pre>
 *    0 1 2 3 4
 * 0  B r a v e
 * 1  r a v e B 
 * 2  a v e B r
 * 3  v e B r a
 * 4  e B r a v
 * </pre>
 * 
 * <p>
 * <b>Implementation details:</b>
 * </p>
 * <p>
 * <b>Representation of the rotations:</b> Storing all rotations as {@code String}s
 * or as a matrix of {@code Character}s would be a total waste of space since the
 * rotations of the source block contain the exact same characters. Therefore, the
 * source block is stored only once (as the original {@code String}), and each
 * rotation is represented by an integer defined as follows: the rotation i is
 * obtained by rotating the source block i times to the left (or i characters to the
 * left). For instance, "lloHe" is the rotation 2 of "Hello", and the source block is
 * always the rotation 0.
 * </p>
 * <p>
 * <b>Representation of the matrix:</b> The matrix can then be represented by an
 * array of integer, whose indices are the rows of the matrix, and whose values are
 * the rotation number (that we have just defined) corresponding to each row. For
 * example, here is the matrix that we have seen for the source "Brave" (before
 * sorting):
 * 
 * <pre>
 * Brave (i=0, source block)
 * raveB (i=1)
 * aveBr (i=2)
 * veBra (i=3)
 * eBrav (i=4)
 * </pre>
 * 
 * This matrix would be represented by the following array :
 * <code>{0, 1, 2, 3, 4}</code>. Indeed, for all i, the rotation i is at row i of the
 * matrix. And after being lexicographically sorted:
 * 
 * <pre>
 * aveBr (i=2)
 * Brave (i=0, source block)
 * eBrav (i=4)
 * raveB (i=1)
 * veBra (i=3)
 * </pre>
 * 
 * This matrix would now be represented by the following array :
 * <code>{2, 0, 4, 1, 3}</code>.
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
class BWRotationsMatrix {

    private final String sourceBlock;
    private final int length;
    private Integer[] rotations;

    /** Creates the rotations matrix associated to the given source block. */
    public BWRotationsMatrix(String block) {
        sourceBlock = block;
        length = block.length();
        rotations = new Integer[length];
        for (int i = 0; i < rotations.length; i++) {
            rotations[i] = i;
        }
    }

    /** Returns the character at position i in the given rotation. */
    private char getRotationCharAt(int rotation, int i) {
        return sourceBlock.charAt((i + rotation) % length);
    }

    /** Returns the last column of this matrix as a {@code String}. */
    public String getLastColumn() {
        StringBuilder sb = new StringBuilder();
        for (int rot : rotations) {
            sb.append(getRotationCharAt(rot, length - 1));
        }
        return sb.toString();
    }

    /** Returns the number of the row corresponding to the original block. */
    public int getSourceBlockLineNum() {
        for (int i = 0; i < length; i++) {
            if (rotations[i] == 0) {
                // 0 is the rotation number of the source block
                return i;
            }
        }
        // cannot happen
        throw new RuntimeException("source block not found among rotations");
    }

    /** Sort this rotations matrix lexicographically. */
    public void sort() {
        // creation of a comparator that understands the integers as rotations and
        // compares the characters of the 2 rotations
        Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer rot1, Integer rot2) {
                for (int i = 0; i < length; i++) {
                    char c1 = getRotationCharAt(rot1, i);
                    char c2 = getRotationCharAt(rot2, i);
                    if (c1 < c2) {
                        return -1;
                    } else if (c1 > c2) {
                        return 1;
                    }
                }
                return 0;
            }
        };
        // easy sort thanks to java utils
        Arrays.sort(rotations, comparator);
    }

    /** For testing purpose only, with short strings! */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int rot : rotations)
            for (int i = 0; i < length; i++)
                sb.append(getRotationCharAt(rot, i));
        return sb.toString();
    }
}
