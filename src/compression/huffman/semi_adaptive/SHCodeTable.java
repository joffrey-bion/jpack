package compression.huffman.semi_adaptive;

import java.util.TreeMap;

/**
 * Represents a Huffman table, mapping each character of the source file to its
 * Huffman code.
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
class SHCodeTable {

    private TreeMap<Character, String> table;

    /**
     * Builds a new Huffman table based on the given Huffman tree.
     * 
     * @param tree
     *            The Huffman tree corresponding to the source file about to be
     *            encoded. May be {@code null}, in the case of an empty source file.
     */
    public SHCodeTable(SHTree tree) {
        table = new TreeMap<>();
        if (tree != null)
            buildCodes(tree, ""); // empty code for the root node
    }

    /**
     * Fills the table with the Huffman codes by iterating through the given tree
     * recursively.
     * 
     * @param tree
     *            The current subtree to iterate through.
     * @param code
     *            The current code corresponding to the path from the root to the
     *            given subtree.
     */
    private void buildCodes(SHTree tree, String code) {
        if (tree.isLeaf()) {
            table.put(tree.getChar(), code);
        } else {
            buildCodes(tree.getZero(), code + "0");
            buildCodes(tree.getOne(), code + "1");
        }
    }

    /**
     * Returns the Huffman code corresponding to the given {@code Character} in this
     * table.
     * 
     * @param c
     *            The Character to encode.
     * @return The Huffman code of {@code c}, as a binary {@code String}. If
     *         {@code c} was not in the source file, it is not in this table, so
     *         {@code null} is returned. If {@code c} is the only node in the tree,
     *         then its Huffman code is the empty {@code String}: "". Therefore, the
     *         decoder has to know the number of characters to decode.
     */
    public String getCode(Character c) {
        return table.get(c);
    }
}
