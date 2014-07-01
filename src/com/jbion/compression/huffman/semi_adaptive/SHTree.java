package com.jbion.compression.huffman.semi_adaptive;

import java.util.Map;
import java.util.PriorityQueue;

/**
 * Represents a static Huffman tree.
 */
public class SHTree implements Comparable<SHTree> {

	/**
	 * The frequency of the represented {@code Character} if this tree is a leaf, the
	 * sum of the frequencies of its sons otherwise.
	 */
	private final int frequency;
	/**
	 * The left son of this tree, corresponding to the code "0", or {@code null } if
	 * this is a leaf.
	 */
	private final SHTree zero;
	/**
	 * The right son of this tree, corresponding to the code "1", or {@code null } if
	 * this is a leaf.
	 */
	private final SHTree one;
	/**
	 * The {@code Character} represented by this tree if this is a leaf, {@code null}
	 * otherwise.
	 */
	private final Character character;

	/**
	 * Creates a new leaf for the given {@code Character}.
	 * 
	 * @param c
	 *            The {@code Character} corresponding to this leaf.
	 * @param frequency
	 *            The number of occurrences of {@code c} in the text.
	 */
	private SHTree(Character c, int frequency) {
		this.frequency = frequency;
		this.zero = null;
		this.one = null;
		this.character = c;
	}

	/**
	 * Creates a new leaf, with an unspecified frequency (which is not needed when
	 * decoding).
	 * 
	 * @param c
	 *            The {@code Character} corresponding to this leaf.
	 */
	public SHTree(Character c) {
		this(c, 0);
	}

	/**
	 * Creates a new node with the given sons.
	 * 
	 * @param zero
	 *            The left son of this tree, corresponding to the code "0"
	 * @param one
	 *            The right son of this tree, corresponding to the code "1"
	 */
	public SHTree(SHTree zero, SHTree one) {
		this.frequency = zero.frequency + one.frequency;
		this.zero = zero;
		this.one = one;
		this.character = null;
	}

	/**
	 * Returns whether this tree is a leaf.
	 * 
	 * @return {@code true} if this is a leaf.
	 */
	public boolean isLeaf() {
		return character != null;
	}

	/**
	 * Returns the "0" son of this tree.
	 * 
	 * @return this tree's son corresponding to code "0", or {@code null} if this is
	 *         a leaf.
	 */
	public SHTree getZero() {
		return zero;
	}

	/**
	 * Returns the "1" son of this tree.
	 * 
	 * @return this tree's son corresponding to code "1", or {@code null} if this is
	 *         a leaf.
	 */
	public SHTree getOne() {
		return one;
	}

	/**
	 * Returns the {@code Character} represented by this leaf.
	 * 
	 * @return the {@code Character} represented by this leaf, or {@code null} if
	 *         this is an internal node.
	 */
	public Character getChar() {
		return character;
	}

	/**
	 * Returns the total number of characters in the text represented by this tree.
	 * 
	 * @return the total number of characters in the text represented by this tree.
	 */
	public long getNbCharactersRead() {
		return frequency;
	}

	/**
	 * Compares this tree to the specified tree using their frequencies:
	 * <ul>
	 * <li>The frequency of a leaf is the frequency of the corresponding character.</li>
	 * <li>The frequency of an internal node is the sum of the frequencies of its
	 * sons.</li>
	 * </ul>
	 * 
	 * @param tree
	 *            The {@link SHTree} to compare this tree with.
	 * @return A negative integer, 0 or a positive integer whether this tree is
	 *         respectively less than, equal or greater than the specified tree.
	 */
	@Override
	public int compareTo(SHTree tree) {
		return frequency - tree.frequency;
	}

	/**
	 * Uses the given character frequencies to build a Huffman tree.
	 * 
	 * @param frequencies
	 *            A {@link Map} between the characters of a text and their number of
	 *            occurrences in the text.
	 * 
	 * @return The Huffman tree corresponding to the frequencies of the characters in
	 *         the specified {@code Map}, or {@code null} if the specified
	 *         {@code Map} is empty.
	 */
	public static SHTree buildTree(Map<Character, Integer> frequencies) {
		// will store all the subtrees during the tree construction
		final PriorityQueue<SHTree> treesPool = new PriorityQueue<>();
		// add each character as a single-leaf tree in the queue
		for (final char c : frequencies.keySet()) {
			treesPool.add(new SHTree(c, frequencies.get(c)));
		}
		// merge the 2 lowest-frequency trees until only one is left
		while (treesPool.size() > 1) {
			final SHTree zero = treesPool.poll();
			final SHTree one = treesPool.poll();
			treesPool.add(new SHTree(zero, one));
			// size decreases by one at each iteration
		}
		// the only resulting element of the queue is the Huffman tree
		// peek() will return null if the map was empty
		return treesPool.peek();
	}

	/*
	 * Fancy printing methods
	 */

	/** Vertical line UTF-8 symbol. */
	private static final char vline = Character.toChars(Integer.parseInt("2502", 16))[0];
	/** Horizontal line UTF-8 symbol. */
	private static final char hline = Character.toChars(Integer.parseInt("2500", 16))[0];
	/** Vertical line with right branch UTF-8 symbol. */
	private static final char midBranch = Character.toChars(Integer.parseInt("251c", 16))[0];
	/** End branch (top and right line) UTF-8 symbol. */
	private static final char endBranch = Character.toChars(Integer.parseInt("2514", 16))[0];

	/**
	 * Returns a {@code String} representation of this tree, with fancy UTF-8 lines.
	 * 
	 * @param indent
	 *            The indentation to append before each line. May contain vertical
	 *            lines, or branches. Middle branches will be turned into vertical
	 *            lines (to continue the other branches), end branches will be
	 *            replaced by blanks (because the branch is finished).
	 * @return a {@code String} representation of this tree, with fancy UTF-8 lines.
	 */
	public String toString(String indent) {
		final String newIndent = indent.replace(endBranch, ' ').replace(hline, ' ').replace(midBranch, vline);
		final String indL = newIndent + " " + midBranch + hline;
		final String indR = newIndent + " " + endBranch + hline;
		String res = indent;
		if (isLeaf()) {
			res += "<" + character + ">";
		} else {
			res += "[#]\n";
			if (zero == null) {
				res += indL + "null";
			} else {
				res += zero.toString(indL);
			}
			res += "\n";
			if (one == null) {
				res += indR + "null";
			} else {
				res += one.toString(indR);
			}
		}
		return res;
	}

	@Override
	public String toString() {
		return toString("");
	}
}
