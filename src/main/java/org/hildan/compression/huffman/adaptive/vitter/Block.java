package org.hildan.compression.huffman.adaptive.vitter;

public class Block {

	public int weight;
	public int parent;
	public boolean parity;
	public int rightChild;
	public int first;
	public int last;

	public boolean isLeaderALeftChild() {
		return parity;
	}

	public int getLeaderNum() {
		return first;
	}
}
