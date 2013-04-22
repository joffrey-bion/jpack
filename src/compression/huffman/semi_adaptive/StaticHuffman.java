package compression.huffman.semi_adaptive;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.TreeMap;

import binary_io.BinReader;
import binary_io.BinWriter;
import binary_io.UnicodeReader;

/**
 * This static class uses Huffman algorithm to encode a text file into a binary file,
 * or decode a binary file (previously encoded via this encoder) into a text file.
 * <p>
 * The encoded file is written as follows:
 * <ul>
 * <li>The size of the original source file in bytes, as a {@code Long}.</li>
 * <li>A binary representation of the Huffman tree used to encode the file (or
 * nothing if the source file is empty).</li>
 * <li>The encoded stream representing the characters of the source file (or nothing
 * if the source file is empty).</li>
 * <li>If the last byte was incomplete, it is completed by as many 0 as necessary.</li>
 * </ul>
 * </p>
 * <p>
 * The encoding of the Huffman tree is done via a pre-order enumeration: for each
 * internal node, a 0 is written, followed by the left subtree (0-subtree) then the
 * right subtree (1-subtree). Each leaf is encoded by a 1 followed by the code of the
 * character it represents (as a {@code Character}).
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
public class StaticHuffman {

    /**
     * Encodes the given text file via Huffman algorithm.
     * 
     * @param sourceName
     *            The relative path to the source text file.
     * @param destName
     *            The relative path to the destination binary file to
     *            create/overwrite.
     */
    public static void encode(String sourceName, String destName) throws IOException {
        // read the file and build a huffman tree according to characters frequencies
        SHTree huffmanTree = buildTree(sourceName);
        // build a code table corresponding to the tree
        SHCodeTable table = new SHCodeTable(huffmanTree);
        // write the size of the original file
        BinWriter writer = new BinWriter(destName);
        writer.writeLongWithLength(huffmanTree.getNbCharactersRead());
        // encode the tree in the file
        writeTree(writer, huffmanTree);
        // encode each character of the original file into the output file
        UnicodeReader reader = new UnicodeReader(sourceName);
        int character;
        while ((character = reader.read()) != -1) {
            writer.write(table.getCode((char) character));
        }
        reader.close();
        writer.close();
    }

    /**
     * Decodes the given binary file via Huffman algorithm.
     * 
     * @param sourceName
     *            The relative path to the source binary file.
     * @param destName
     *            The relative path to the destination text file to create/overwrite.
     */
    public static void decode(String sourceName, String destName) throws IOException {
        BinReader reader = new BinReader(sourceName);
        // read the original file size
        long nbChars = reader.readLongWithLength();
        // decode the huffman tree
        SHTree huffmanTree = null;
        if (nbChars > 0) // no tree to read if the original file is empty
            huffmanTree = readTree(reader);
        // decode each character in the file with the tree
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destName), "UTF-8"));
        while (nbChars > 0) {
            writer.write(decodeChar(reader, huffmanTree));
            nbChars--;
        }
        reader.close();
        writer.close();
    }

    /**
     * Creates the Huffman tree corresponding to the frequencies of the characters in
     * the specified file.
     * 
     * @param filename
     *            The relative path to the source text file whose Huffman tree is
     *            desired.
     * @return The Huffman tree corresponding to the frequencies of the characters in
     *         the specified file.
     * @throws IOException
     *             If any exception occurs while reading the source file.
     */
    private static SHTree buildTree(String filename) throws IOException {
        TreeMap<Character, Integer> frequencies = new TreeMap<>();
        UnicodeReader reader = new UnicodeReader(filename);
        // count the frequency of each character in the whole file
        int character;
        while ((character = reader.read()) != -1) {
            Integer count = frequencies.get((char) character);
            // if the character is not in the map, initialize count
            if (count == null) {
                count = 0;
            }
            // increments the frequency of the character
            frequencies.put((char) character, count + 1);
        }
        reader.close();
        return SHTree.buildTree(frequencies);
    }

    /**
     * Encodes the given tree into the binary destination file, via a pre-order
     * enumeration. See this class description for details.
     * 
     * @param writer
     *            The {@link BinWriter} to use to write in the file.
     * @param huffmanTree
     *            The tree to encode.
     * @see StaticHuffman
     */
    private static void writeTree(BinWriter writer, SHTree huffmanTree) throws IOException {
        if (huffmanTree == null)
            return; // in case of empty file, no tree written
        if (huffmanTree.isLeaf()) {
            // when a leaf is reached, write 1 followed by the character code
            writer.write("1");
            writer.writeCharacter(huffmanTree.getChar());
        } else {
            // when we are at a node, write 0 followed by the 0 son then the 1 son
            writer.write("0");
            writeTree(writer, huffmanTree.getZero());
            writeTree(writer, huffmanTree.getOne());
        }
    }

    /**
     * Decodes the tree from the encoded binary file. See this class description for
     * details.
     * 
     * @param reader
     *            The {@link BinWriter} to use to write in the file.
     * @return The decoded Huffman tree.
     * @see StaticHuffman
     */
    private static SHTree readTree(BinReader reader) throws IOException {
        if (reader.readBit()) {
            // 1 means this is a leaf, and is followed by the character code
            return new SHTree(reader.readChar());
        } else {
            // 0 means this is a node, and is followed by the '0' son then the '1'
            // son
            SHTree zero = readTree(reader);
            SHTree one = readTree(reader);
            return new SHTree(zero, one);
        }
    }

    /**
     * Decodes one character from the encoded file.
     * 
     * @param reader
     *            The {@link BinReader} to use to read the file.
     * @return The decoded {@code Character}.
     */
    private static char decodeChar(BinReader reader, SHTree huffmanTree) throws IOException {
        SHTree tree = huffmanTree;
        // read bits to browse the tree until a leaf is reached
        while (!tree.isLeaf()) {
            if (reader.readBit())
                tree = tree.getOne();
            else
                tree = tree.getZero();
        }
        return tree.getChar();
    }
}
