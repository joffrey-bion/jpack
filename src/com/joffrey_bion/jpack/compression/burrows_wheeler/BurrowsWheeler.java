package com.joffrey_bion.jpack.compression.burrows_wheeler;

/**
 * This static class provides 2 methods to apply both the transform and the reverse
 * transform of Burrows-Wheeler.
 * <p>
 * The goal of this transform is to increase the length and number of runs in a text
 * block, and more generally, local correlations. This transform uses the fact that
 * in most languages, a letter is usually preceded by the same set of letters.
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
public class BurrowsWheeler {

    /**
     * Applies Burrows Wheeler transform to the given text block.
     * 
     * @param block
     *            A block of text to transform, of any length.
     * @return A BWBlock containing the transformed block (same length as original
     *         block) and the index of the source block among its sorted rotations.
     */
    public static BWBlock transform(String block) {
        BWRotationsMatrix rotations = new BWRotationsMatrix(block);
        rotations.sort();
        String lastColumn = rotations.getLastColumn();
        int index = rotations.getSourceBlockLineNum();
        return new BWBlock(lastColumn, index);
    }

    /**
     * Applies Burrows Wheeler reverse transform to the given transformed text block.
     * 
     * @param block
     *            A {@link BWBlock} text block previously transformed via
     *            {@link #transform(String)}.
     * @return The original text block.
     */
    public static String reverse(BWBlock block) {
        BWStatsTable stats = new BWStatsTable(block.content);
        StringBuilder sb = new StringBuilder();
        // number of characters to decode
        int nbLeft = block.content.length();
        // build the decoded string, starting by the end
        int pos = block.index; // the last letter is given by the index
        while (nbLeft > 0) {
            char c = block.content.charAt(pos);
            sb.append(c);
            // previous letter position (next to decode)
            pos = stats.getNbCharsLessThan(c) + stats.getNbPreviousMatches(pos);
            nbLeft--;
        }
        // reverse the string to return, to retrieve the original order
        return sb.reverse().toString();
    }
}
