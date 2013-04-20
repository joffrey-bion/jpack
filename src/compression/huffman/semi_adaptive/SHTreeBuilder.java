package compression.huffman.semi_adaptive;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.TreeMap;

import binary_io.UnicodeReader;

/**
 * Builds a Huffman tree according to the frequencies of each character in the given
 * file.
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
class SHTreeBuilder {

    /** Maps each character to its frequency in the source file. */
    private TreeMap<Character, Integer> frequencies;
    /**
     * A priority queue containing all the subtrees during the tree construction,
     * sorted as defined by {@link SHTree#compareTo(SHTree)}.
     */
    private PriorityQueue<SHTree> treesPool;

    /**
     * Creates a new Huffman tree builder.
     * 
     * @param filename
     *            The relative path to the source text file whose Huffman tree is
     *            desired.
     * @throws IOException
     *             If any exception occurs while reading the source file.
     */
    public SHTreeBuilder(String filename) throws IOException {
        super();
        frequencies = new TreeMap<>();
        UnicodeReader reader = new UnicodeReader(filename);
        // count the frequency of each character in the whole file
        int character;
        while ((character = reader.read()) != -1) {
            Integer count = frequencies.get((char) character);
            if (count == null) {
                // the character is not in the map, we have to initialize count
                count = 0;
            }
            // increments the frequency of the character
            frequencies.put((char) character, count + 1);
        }
        reader.close();
    }

    /**
     * Uses the frequencies read by the constructor to build a Huffman tree for the
     * source file.
     * 
     * @return The Huffman tree corresponding to the frequencies of the characters in
     *         the source file. If the file is empty, {@code null} is returned.
     */
    public SHTree buildTree() {
        treesPool = new PriorityQueue<>();
        // adds each character as a single-leaf tree in the queue
        for (char c : frequencies.keySet()) {
            treesPool.add(new SHTree(c, frequencies.get(c)));
        }
        // merges the 2 lowest-frequency trees until only one is left
        while (treesPool.size() > 1) {
            SHTree zero = treesPool.poll();
            SHTree one = treesPool.poll();
            treesPool.add(new SHTree(zero, one));
            // size decreases by one at each iteration
        }
        if (treesPool.size() == 0) {
            // the pool was empty in the first place: no character in the file
            return null;
        } else {
            // the only resulting element of the queue is the Huffman tree
            return treesPool.peek();
        }
    }
}
