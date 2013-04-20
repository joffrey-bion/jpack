package compression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import compression.huffman.semi_adaptive.StaticHuffman;

/**
 * Provides the compression and decompression methods to manipulate the files given
 * in the constructor.
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
public class Compressor {

    private static final String TEMP_FILENAME = "temp.txt";

    /**
     * Compresses the given source file into the destination file
     * 
     * @param sourceName
     *            The relative path to a text file to compress.
     * @param destName
     *            The relative path to the destination binary file to be created or
     *            overwritten.
     */
    public static void compress(String sourceName, String destName) {
        // block transformations
        try {
            BlockCompressor bcomp = new BlockCompressor(sourceName, TEMP_FILENAME);
            bcomp.compress();
        } catch (FileNotFoundException e) {
            System.err.println("File source '" + sourceName + "' not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // huffman encoding
        try {
            StaticHuffman.encode(TEMP_FILENAME, destName);
        } catch (IOException e) {
            System.err.println("I/O error in Huffman compression: " + e.getMessage());
        }
        deleteTempFile();
    }

    /**
     * Uncompresses the given source file into the destination file
     * 
     * @param sourceName
     *            The relative path to a binary file previously produced by
     *            {@link Compressor#compress(String, String)}.
     * @param destName
     *            The relative path to the destination text file to be created or
     *            overwritten.
     */
    public static void uncompress(String sourceName, String destName) {
        // huffman decoding
        try {
            StaticHuffman.decode(sourceName, TEMP_FILENAME);
        } catch (IOException e) {
            System.err.println("I/O error in Huffman decompression: " + e.getMessage());
        }
        // block reverse transformations
        try {
            BlockCompressor bcomp = new BlockCompressor(TEMP_FILENAME, destName);
            bcomp.uncompress();
        } catch (FileNotFoundException e) {
            System.err.println("File source '" + sourceName + "' not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteTempFile();
    }

    /**
     * Deletes the temporary file that was created between the block compression and
     * semi-adaptive Huffman.
     */
    private static void deleteTempFile() {
        // temporary file deletion
        if (new File(TEMP_FILENAME).delete())
            System.out.println("Temp file deleted");
        else
            System.out.println("Error deleting temp file");
    }
}
