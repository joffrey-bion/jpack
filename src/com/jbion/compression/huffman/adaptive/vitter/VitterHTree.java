package com.jbion.compression.huffman.adaptive.vitter;

import java.util.ArrayList;
import java.util.LinkedList;

public class VitterHTree {

	private final ArrayList<Node> internalNodes;
	private final ArrayList<Node> leaves;
	private final LinkedList<Block> blocks;

	public VitterHTree() {
		internalNodes = new ArrayList<>();
		leaves = new ArrayList<>();
		blocks = new LinkedList<>();
	}

	private void update(char c) {
		Node q = getLeaf(c);
		Leaf toIncrement = null;
		if (q.isZeroNode()) {
			// special case #1
			toIncrement = new Leaf(c);
			setChildren(q, new Leaf(), toIncrement);
		} else {
			// special case #2
			final Node leaderOfQ = findBlockLeader(q);
			swap(leaderOfQ, q);
			if (isSiblingOfNYT(q)) {
				toIncrement = (Leaf) q;
				q = getParent(q);
			}
		}
		while (!isRoot(q)) {
			// q must be the leader of its block
			q = slideAndIncrement(q);
		}
		if (toIncrement != null) {
			// handle the 2 special cases
			slideAndIncrement(toIncrement);
		}
	}

	private Node slideAndIncrement(Node p) {
		final int w = p.getWeight();
		final Block b = getNextBLock(getBlock(p));
		if (p.isLeaf() && !isLeafBlock(b) || !p.isLeaf() && isLeafBlock(b)) {
			final Node formerParent = getParent(p);
			final Node newParent = slideAheadOf(b, p);
			p.incrementWeight();
			if (p.isLeaf()) {
				return newParent;
			} else {
				return formerParent;
			}
		}
		return null;
	}

    private Node findBlockLeader(Node node) {
        // TODO
        return null;
    }

	private boolean isLeafBlock(Block b) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Slides {@code p} ahead of the nodes of {@code b} in implicit ordering.
	 * 
	 * @param b
	 *            The {@link Block} to slide ahead of
	 * @param p
	 *            The {@link Node} to move
	 * @return The new parent of {@code p}.
	 */
	private Node slideAheadOf(Block b, Node p) {
		// TODO Auto-generated method stub
		return null;

	}

	private Block getNextBLock(Object block) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object getBlock(Node p) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the leaf node corresponding to {@code c}
	 * 
	 * @param c
	 *            The character to get the leaf for
	 * @return the leaf {@link Node} corresponding to {@code c}, or the NYT node if
	 *         {@code c} had not been seen yet.
	 */
	private Node getLeaf(char c) {
		// TODO
		return null;
	}

	/**
	 * Returns the parent of {@code q}, or {@code null} if {@code q} is the root.
	 * 
	 * @param q
	 *            The node to get the parent for
	 * @return the parent of {@code q}, or {@code null} if {@code q} is the root.
	 */
	private Node getParent(Node q) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isRoot(Node q) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isSiblingOfNYT(Node q) {
		// TODO Auto-generated method stub
		return false;
	}

	private void swap(Node leaderOfQ, Node q) {
		// TODO
	}

	private void incrementLeaderWeight(Node node) {
		// TODO
	}

	private void setChildren(Node parent, Node left, Node right) {

	}
}
