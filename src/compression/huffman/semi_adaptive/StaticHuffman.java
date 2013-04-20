package compression.huffman.semi_adaptive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import binary_io.BinReader;
import binary_io.BinWriter;

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
        // read the file and build a huffman tree according to charactesr frequencies
        SHTree huffmanTree = new SHTreeBuilder(sourceName).buildTree();
        System.out.println(huffmanTree);
        // build a code table corresponding to the tree
        SHCodeTable table = new SHCodeTable(huffmanTree);
        BinWriter writer = new BinWriter(destName);
        // write the size of the original file
        writer.writeLongWithLength(new File(sourceName).length());
        // encode the tree in the file
        writeTree(writer, huffmanTree);
        // encode each character of the original file into the output file
        BufferedReader reader = new BufferedReader(new FileReader(sourceName));
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
        BufferedWriter writer = new BufferedWriter(new FileWriter(destName));
        while (nbChars > 0) {
            writer.write(decodeChar(reader, huffmanTree));
            nbChars--;
        }
        reader.close();
        writer.close();
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
     *            The {@link BinWriter} to use to write in the file.
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
