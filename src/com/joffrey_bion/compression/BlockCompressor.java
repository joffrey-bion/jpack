package com.joffrey_bion.compression;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.joffrey_bion.binary_io.BinHelper;
import com.joffrey_bion.binary_io.UnicodeReader;
import com.joffrey_bion.compression.burrows_wheeler.BWBlock;
import com.joffrey_bion.compression.burrows_wheeler.BurrowsWheeler;
import com.joffrey_bion.compression.move_to_front.MoveToFront;

/**
 * Part of the {@link Compressor} that uses only algorithms that work on blocks of
 * data (especially <b>not</b> Huffman, that works on a whole file).
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
class BlockCompressor {

    private static final int BLOCK_SIZE = 4096;
    private static final int BLOCK_HEADER_SIZE = 3;

    private UnicodeReader reader;
    private BufferedWriter writer;

    private MoveToFront mtf;

    /**
     * Creates a new {@code BlockCompressor} for the given source and destination
     * files.
     * 
     * @param sourceName
     *            The relative path to the source file.
     * @param destName
     *            The relative path to the destination file. The file will be created
     *            if it does not exist, overwritten otherwise.
     * @throws IOException
     *             If any problem occurs while reading or writing in the given files.
     */
    public BlockCompressor(String sourceName, String destName) throws IOException {
        reader = new UnicodeReader(sourceName, "UTF-8");
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destName), "UTF-8"));
        mtf = new MoveToFront();
    }

    /**
     * Compresses the source file block by block, writing each block in the
     * destination file.
     * 
     * @throws IOException
     *             If any problem occurs while reading or writing in the given files.
     */
    public void compress() throws IOException {
        String block;
        while ((block = readString(BLOCK_SIZE)) != null) {
            System.out.println("Block: " + block);
            writeCBlock(transformBlock(block));
        }
        reader.close();
        writer.close();
    }

    /**
     * Uncompresses the source file block by block, writing each block in the
     * destination file.
     * 
     * @throws IOException
     *             If any problem occurs while reading or writing in the given files.
     */
    public void uncompress() throws IOException {
        CBlock block;
        while ((block = readCBlock()) != null) {
            writer.write(reverseBlock(block));
        }
        reader.close();
        writer.close();
    }

    private void writeCBlock(CBlock block) throws IOException {
        writer.write(block.header + block.content);
    }

    private CBlock readCBlock() throws IOException {
        CBlock block = new CBlock();
        block.header = readString(BLOCK_HEADER_SIZE);
        if (block.header == null)
            return null;
        block.content = readString(BLOCK_SIZE);
        if (block.content == null)
            throw new RuntimeException("Block header not followed by any content");
        return block;
    }

    private String readString(int length) throws IOException {
        char[] str = new char[length];
        int n = reader.read(str);
        if (n == -1)
            return null;
        System.out.println(n + " characters read");
        return String.valueOf(str).substring(0, n);
    }

    private CBlock transformBlock(String sourceBlock) {
        BWBlock bwOut = BurrowsWheeler.transform(sourceBlock);
        CBlock block = new CBlock();
        copyBWIntoCBlock(bwOut, block);
        System.out.println("Block bw: " + block);
        block.content = mtf.transform(block.content);
        System.out.println("Block mtf: " + block);
        return block;
    }

    private String reverseBlock(CBlock block) {
        System.out.println("Block mtf: " + block);
        block.content = mtf.reverse(block.content);
        System.out.println("Block bw: " + block);
        BWBlock bwb = toBWBlock(block);
        return BurrowsWheeler.reverse(bwb);
    }

    private static void copyBWIntoCBlock(BWBlock bwb, CBlock block) {
        String hexa = Integer.toHexString(bwb.index);
        block.header = BinHelper.addLeadingZeros(hexa, BLOCK_HEADER_SIZE);
        block.content = bwb.content;
    }

    private static BWBlock toBWBlock(CBlock block) {
        return new BWBlock(block.content, Integer.parseInt(block.header, 16));
    }
}
