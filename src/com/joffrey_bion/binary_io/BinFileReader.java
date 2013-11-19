package com.joffrey_bion.binary_io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BinFileReader {

    private BufferedInputStream reader;
    private String buffer;

    /**
     * Creates a new reader for the given binary file.
     * 
     * @param filename
     *            The relative path to the binary file to read.
     * @throws FileNotFoundException
     *             If the given file does not exist.
     */
    public BinFileReader(String filename) throws FileNotFoundException {
        reader = new BufferedInputStream(new FileInputStream(filename));
        buffer = "";
    }

    /**
     * Reads the specified number of bits.
     * 
     * @param length
     *            The number of bits to read.
     * @return A binary {@code String} representing the bits read.
     * @throws IOException
     *             If any I/O error occurs.
     */
    public String readBits(int length) throws IOException {
        int octet;
        while (buffer.length() < length && (octet = reader.read()) != -1) {
            buffer += BinHelper.addLeadingZeros(Integer.toBinaryString(octet), 8);
        }
        if (buffer.length() < length)
            throw new IOException("No more bits to read!");
        String res = buffer.substring(0, length);
        buffer = buffer.substring(length);
        return res;
    }

    /**
     * Reads a bit as a {@link Boolean} from the file.
     * 
     * @return {@code true} for a 1 and {@code false} for a 0.
     * @throws IOException
     *             If any I/O error occurs.
     */
    public boolean readBit() throws IOException {
        String bit = readBits(1);
        if ("0".equals(bit))
            return false;
        else if ("1".equals(bit))
            return true;
        else
            throw new RuntimeException("Non binary string read.");
    }

    /**
     * Reads an {@code int} from the file.
     * 
     * @return The read {@code int}.
     * @throws IOException
     *             If any I/O error occurs.
     */
    public int readInt() throws IOException {
        return (int) readValue(Integer.SIZE);
    }

    /**
     * Reads a {@code long} from the file.
     * 
     * @return The read {@code long}.
     * @throws IOException
     *             If any I/O error occurs.
     */
    public long readLong() throws IOException {
        return readValue(Long.SIZE);
    }

    /**
     * Reads a {@code char} from the file.
     * 
     * @return The read {@code char}.
     * @throws IOException
     *             If any I/O error occurs.
     */
    public char readChar() throws IOException {
        return (char) readValue(Character.SIZE);
    }

    /**
     * Reads a numeric value from the next {@code length} bits.
     * 
     * @param length
     *            the number of bits to convert to a long value.
     * @return The value represented by the next {@code length} bits.
     * @throws IOException
     *             If any I/O error occurs.
     */
    private long readValue(int length) throws IOException {
        String binStr = readBits(length);
        return Long.parseLong(binStr, 2);
    }

    /**
     * Reads a long value preceded by 6 bits indicating the number of bits used to
     * write it. As an example, 000000 means that 1 bit follows, 111111 meaning that
     * 64 bits follow.
     * 
     * @return The read {@code Long}.
     * @throws IOException
     *             If any I/O error occurs.
     */
    public Long readLongWithLength() throws IOException {
        int length = (int) readValue(6) + 1;
        return readValue(length);
    }

    /**
     * Closes the file.
     */
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
