package com.jbion.compression.blocks;

/**
 * Represents a compressed block of text.
 */
class CBlock {

	/**
	 * Header of the block, that can be used to store necessary information for
	 * decoding.
	 */
	public String header = null;
	/**
	 * Encoded stream representing the source data.
	 */
	public String content = null;

	@Override
	public String toString() {
		String h = header;
		if (h == null) {
			h = "---";
		}
		return h + " " + content;
	}
}
