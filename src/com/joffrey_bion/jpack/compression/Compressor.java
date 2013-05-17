package com.joffrey_bion.jpack.compression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.joffrey_bion.jpack.compression.huffman.semi_adaptive.StaticHuffman;


/**
 * Provides the compression and decompression methods to manipulate the files given
 * in the constructor.
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
public class Compressor {

    private static final boolean HUFFMAN = true;
    private static final boolean BLOCK = true;

    private static final String TEMP_FILENAME1 = "temp1.txt";
    private static final String TEMP_FILENAME2 = "temp2.txt";

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
        String huffmanSource;
        // block transformations
        if (BLOCK) {
            try {
                BlockCompressor bcomp = new BlockCompressor(sourceName, HUFFMAN ? TEMP_FILENAME1 : destName);
                bcomp.compress();
            } catch (FileNotFoundException e) {
                System.err.println("File source '" + sourceName + "' not found.");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            huffmanSource = TEMP_FILENAME1;
        } else {
            huffmanSource = sourceName;
        }
        // huffman encoding
        if (HUFFMAN)
            try {
                StaticHuffman.encode(huffmanSource, destName);
            } catch (IOException e) {
                System.err.println("I/O error in Huffman compression: " + e.getMessage());
            }
        new File(TEMP_FILENAME1).delete();
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
        String blockSource;
        // huffman decoding
        if (HUFFMAN) {
            try {
                StaticHuffman.decode(sourceName, BLOCK ? TEMP_FILENAME2 : destName);
            } catch (IOException e) {
                System.err.println("I/O error in Huffman decompression: " + e.getMessage());
                return;
            }
            blockSource = TEMP_FILENAME2;
        } else {
            blockSource = sourceName;
        }
        // block reverse transformations
        if (BLOCK)
            try {
                BlockCompressor bcomp = new BlockCompressor(blockSource, destName);
                bcomp.uncompress();
            } catch (FileNotFoundException e) {
                System.err.println("File source '" + blockSource + "' not found.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        new File(TEMP_FILENAME2).delete();
    }
}
