package com.joffrey_bion.jpack.binary_io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinFileWriter {

    private BufferedOutputStream writer;
    private String buffer;

    /**
     * Creates a new writer for the given file.
     * 
     * @param filename
     *            The relative path to the file to write. Creates the file if it does
     *            not exist, overwrite it otherwise.
     * @throws IOException
     *             If any error occurs while writing to the file.
     */
    public BinFileWriter(String filename) throws IOException {
        writer = new BufferedOutputStream(new FileOutputStream(filename));
        buffer = "";
    }

    /** Writes the given binary string to a buffer that will be written byte by byte. */
    public void write(String bin) throws IOException {
        buffer += bin;
        // write the excess byte by byte
        while (buffer.length() >= 8) {
            writeByte(buffer.substring(0, 8));
            buffer = buffer.substring(8);
        }
    }

    /**
     * Writes the given long to the file, with leading zeros to reach
     * {@link Long#SIZE}.
     */
    public void writeLong(long value) throws IOException {
        writeValue(value, Long.SIZE);
    }

    /**
     * Writes the given integer to the file, with leading zeros to reach
     * {@link Integer#SIZE}.
     */
    public void writeInteger(int value) throws IOException {
        writeValue(value, Integer.SIZE);
    }

    /**
     * Writes the given character's code to the file, with leading zeros to reach
     * {@link Character#SIZE}.
     */
    public void writeCharacter(char value) throws IOException {
        writeValue(value, Character.SIZE);
    }

    /**
     * Writes the given value to the file, with leading zeros to reach {@code size}.
     * 
     * @param value
     *            The value to write to the binary file.
     * @param size
     *            The number of bits to use to write the value.
     */
    private void writeValue(long value, int size) throws IOException {
        String binStr = BinHelper.addLeadingZeros(Long.toBinaryString(value), size);
        write(binStr);
    }

    /**
     * Writes the given int value to the file, preceded by 5 bits indicating the number of
     * bits used to write it.
     */
    public void writeIntegerWithLength(int value) throws IOException {
        writeWithLength(value, 5);
    }

    /**
     * Writes the given long value to the file, preceded by 6 bits indicating the number of
     * bits used to write it.
     */
    public void writeLongWithLength(long value) throws IOException {
        writeWithLength(value, 6);
    }

    /**
     * Writes the given {@code value} to the file, preceded by
     * {@code magnitudeLength} bits indicating the number of bits used to write
     * {@code value}.
     * 
     * @param value
     *            The value to write to the binary file
     * @param magnitudeLength
     *            The number of bits used to indicate the number of bits used for
     *            {@code value} (beware of the number-of-bit-ception!)
     */
    private void writeWithLength(long value, int magnitudeLength) throws IOException {
        String binStr = Long.toBinaryString(value);
        String magnitudeStr = Integer.toBinaryString(binStr.length() - 1);
        magnitudeStr = BinHelper.addLeadingZeros(magnitudeStr, magnitudeLength);
        write(magnitudeStr);
        write(binStr);
    }

    /** Only call this method from {@link #write(String)}. */
    private void writeByte(String byteStr) throws IOException {
        if (byteStr.length() != 8) {
            throw new IllegalArgumentException("Wrong length (" + byteStr.length()
                    + ") for a byte, 8 expected");
        }
        try {
            int b = Integer.parseInt(byteStr, 2);
            writer.write(b); // TODO test with an integer > 255
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Binary string expected, got '" + byteStr + "'");
        }

    }

    /** Closes the file, flushing the buffer. */
    public void close() {
        try {
            if (buffer.length() != 0) {
                writeByte(completeByte(buffer));
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Completes a binary {@code String} shorter than 8 bits to a full byte. */
    private static String completeByte(String bin) {
        int length = bin.length();
        if (length > 8) {
            throw new IllegalArgumentException("The string must be shorter than a byte.");
        }
        String res = bin;
        for (int i = 8; i > length; i--) {
            res += "0";
        }
        return res;
    }
}