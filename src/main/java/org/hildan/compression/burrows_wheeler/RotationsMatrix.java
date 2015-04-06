package org.hildan.compression.burrows_wheeler;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Represents the rotations matrix used by Burrows-Wheeler transform algorithm.
 * <p>
 * All the methods needed by the algorithm to manipulate this matrix are provided here as well. Here
 * is an example of a rotation matrix (for the source text "Brave") before being sorted:
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
 * <h1>Implementation details</h1>
 * <h2>Representation of the rotations</h2>
 *
 * Storing all rotations as {@code String}s or as a matrix of {@code Character}s would be a total
 * waste of space since the rotations of the source block contain the exact same characters.
 * Therefore, the source block is stored only once (as the original {@code String}), and each
 * rotation is represented by an integer defined as follows: the rotation i is obtained by rotating
 * the source block i times to the left (or i characters to the left). For instance, "lloHe" is the
 * rotation 2 of "Hello", and the source block is always the rotation 0.
 *
 * <h2>Representation of the matrix</h2>
 *
 * The matrix can then be represented by an array of integer, whose indices are the rows of the
 * matrix, and whose values are the rotation number (that we have just defined) corresponding to
 * each row. For example, here is the matrix that we have seen for the source "Brave" (before
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
 * This matrix would be represented by the array {@code [0, 1, 2, 3, 4]}. Indeed, for all i, the
 * rotation i is the row i of the matrix. And after being lexicographically sorted: *
 *
 * <pre>
 * aveBr (i=2)
 * Brave (i=0, source block)
 * eBrav (i=4)
 * raveB (i=1)
 * veBra (i=3)
 * </pre>
 *
 * This matrix would now be represented by the array {@code [2, 0, 4, 1, 3]}.
 */
class RotationsMatrix {

    private final char[] sourceBlock;

    /**
     * The matrix is represented by the source block and this array of integer, whose indices are
     * the rows of the matrix, and whose values are the rotation number corresponding to each row.
     * The rotation i is obtained by rotating the source block i times to the left (or i characters
     * to the left). For instance, "lloHe" is the rotation 2 of "Hello", and the source block is
     * always the rotation 0.
     */
    private final Integer[] rotations;

    /**
     * Creates the rotations matrix associated to the given source block.
     *
     * @param block
     *            The source block to create the matrix for.
     */
    public RotationsMatrix(String block) {
        sourceBlock = block.toCharArray();
        rotations = new Integer[sourceBlock.length];
        for (int i = 0; i < rotations.length; i++) {
            rotations[i] = i;
        }
    }

    /**
     * Returns the character at position {@code i} in the given {@code rotation}.
     *
     * @param rotation
     *            The number of the rotation, as explained in this class description.
     * @param i
     *            The position of the desired character in the rotation.
     * @return The character at position {@code i} in the given {@code rotation}.
     */
    private char getRotationCharAt(int rotation, int i) {
        return sourceBlock[(i + rotation) % sourceBlock.length];
    }

    /**
     * Returns the last column of this matrix as a {@code String}.
     *
     * @return The last column of this matrix.
     */
    public String getLastColumn() {
        final StringBuilder sb = new StringBuilder();
        for (final int rot : rotations) {
            sb.append(getRotationCharAt(rot, sourceBlock.length - 1));
        }
        return sb.toString();
    }

    /**
     * Returns the number of the row corresponding to the original block.
     *
     * @return The row number of the original block.
     */
    public int getSourceBlockLineNum() {
        for (int i = 0; i < sourceBlock.length; i++) {
            if (rotations[i] == 0) {
                // 0 is the rotation number of the source block
                return i;
            }
        }
        // cannot happen
        throw new RuntimeException("source block not found among rotations");
    }

    /**
     * Sorts this rotations matrix lexicographically.
     */
    public void sort() {
        // creation of a comparator that understands the integers as rotations and
        // compares the characters of the 2 rotations
        final Comparator<Integer> comparator = (rot1, rot2) -> {
            for (int i = 0; i < sourceBlock.length; i++) {
                final char c1 = getRotationCharAt(rot1, i);
                final char c2 = getRotationCharAt(rot2, i);
                if (c1 < c2) {
                    return -1;
                } else if (c1 > c2) {
                    return 1;
                }
            }
            return 0;
        };
        // easy sort thanks to java utils
        Arrays.sort(rotations, comparator);
    }

    /**
     * For testing purpose, to be used only with short strings.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final int rot : rotations) {
            for (int i = 0; i < sourceBlock.length; i++) {
                sb.append(getRotationCharAt(rot, i));
            }
        }
        return sb.toString();
    }
}
