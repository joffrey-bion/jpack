package com.jbion.compression.burrows_wheeler;

import java.util.TreeMap;

/**
 * A table containing statistics about occurrences of the characters in a
 * Burrows-Wheeler transformed block. It contains information that is used to decode
 * such a block:
 * <ul>
 * <li>For each character {@code c}, the number of characters in the block that are
 * lexicographically less than {@code c}, each character counted as many times as it
 * appears in the transformed block.</li>
 * <li>For each position {@code i} in the block, the number of characters that are
 * identical to the character at position {@code i} and are located before position
 * {@code i} in the transformed block.</li>
 * </ul>
 */
class StatsTable {

	/** @see #getNbPreviousMatches(int) */
	private int[] prevMatch;

	/** @see #getNbCharsLessThan(char) */
	private TreeMap<Character, Integer> nbLessThan;

	/**
	 * Creates a new statistics table for the given transformed block.
	 *
	 * @param transformedBlock
	 *            A block previously transformed via
	 *            {@link BurrowsWheeler#transform(String)}.
	 */
	public StatsTable(String transformedBlock) {
		final TreeMap<Character, Integer> frequencies = fillPrevMatch(transformedBlock);
		fillNbLessThan(frequencies);
	}

	/**
	 * Fills {@code prevMatch} while counting the number of occurrences of each
	 * character in the block.
	 *
	 * @param block
	 *            The source block.
	 * @return A mapping of each character to its number of occurrences (frequency).
	 */
	private TreeMap<Character, Integer> fillPrevMatch(String block) {
		prevMatch = new int[block.length()];
		final TreeMap<Character, Integer> frequencies = new TreeMap<>();
		for (int i = 0; i < block.length(); i++) {
			final char c = block.charAt(i);
			Integer n = frequencies.get(c);
			if (n == null) {
				// first time this char is seen
				n = 0;
			}
			// number of previous matches is the current frequency of c
			prevMatch[i] = n;
			// we increment the frequency of c since we met one more
			frequencies.put(c, n + 1);
		}
		return frequencies;
	}

	/**
	 * Fills {@code nbLessThan} using the frequency of each character in the block.
	 *
	 * @param frequencies
	 *            A mapping of each character to its number of occurrences
	 *            (frequency).
	 */
	private void fillNbLessThan(TreeMap<Character, Integer> frequencies) {
		nbLessThan = new TreeMap<>();
		for (final Character c1 : frequencies.keySet()) {
			// initialization at 0
			nbLessThan.put(c1, 0);
			for (final Character c2 : frequencies.keySet()) {
				if (c2 < c1) {
					// add the frequency of each inferior character
					nbLessThan.put(c1, nbLessThan.get(c1) + frequencies.get(c2));
				}
			}
		}
	}

	/**
	 * Returns the number of characters in the block that are lexicographically less
	 * than {@code c}. Each character is counted as many times as it appears in the
	 * block.
	 *
	 * @param c
	 *            The character referred to in the description of this method.
	 * @return The number of characters in the block that are lexicographically less
	 *         than {@code c}.
	 */
	public int getNbCharsLessThan(char c) {
		return nbLessThan.get(c);
	}

	/**
	 * Returns the number of characters that are identical to the character at the
	 * specified {@code position} and are located before {@code position} in the
	 * transformed block.
	 *
	 * @param position
	 *            The position in the transformed block that is considered in this
	 *            method's description.
	 * @return The number of characters that are identical to the character at
	 *         position {@code position} and are located before {@code position} in
	 *         the transformed block.
	 */
	public int getNbPreviousMatches(int position) {
		return prevMatch[position];
	}

}
