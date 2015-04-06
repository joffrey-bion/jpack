package org.hildan.compression.huffman.adaptive.vitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.hildan.utils.io.binary.BitInputStream;
import org.hildan.utils.io.binary.BitOutputStream;
import org.junit.Test;

public class VitterTest {

    private static final String TEST_FILES_LOCATION = "texts-test";

    private static final String TEMP_FILES_LOCATION = "C:/Windows/Temp";

    private static final String TEST_FILE = TEST_FILES_LOCATION + "/abra.txt";

    private static final String TEMP_BINARY_FILE = TEMP_FILES_LOCATION + "/temp-compressed";

    private static final String TEMP_TEXT_FILE = TEMP_FILES_LOCATION + "/temp.txt";

    @Test
    public void testVitter() throws IOException {
        try (BufferedReader input = new BufferedReader(new FileReader(TEST_FILE));
                BitOutputStream bos = new BitOutputStream(new FileOutputStream(TEMP_BINARY_FILE))) {

            System.out.println("Compressing file " + TEST_FILE + " to " + TEMP_BINARY_FILE);
            ProceduralVitter vitter = new ProceduralVitter();
            int b;
            int count = 0;
            while ((b = input.read()) != -1) {
                vitter.encodeAndTransmit((char) b, bos);
                if (count % 5 == 0) {
                    //System.out.print(".");
                }
            }
            System.out.println();
        }

        try (BitInputStream bis = new BitInputStream(new FileInputStream(TEMP_BINARY_FILE));
                BufferedWriter output = new BufferedWriter(new FileWriter(TEMP_TEXT_FILE))) {
            System.out.println("Decompressing file " + TEMP_BINARY_FILE + " to " + TEMP_TEXT_FILE);
            ProceduralVitter vitter = new ProceduralVitter();
            Character c;
            int count = 0;
            while ((c = vitter.receiveAndDecode(bis)) != null) {
                output.write(c);
                if (count % 5 == 0) {
                    //System.out.print(".");
                }
            }
            System.out.println();
        }

        assertEqualFiles(TEST_FILE, TEMP_TEXT_FILE);
    }

    private static void assertEqualFiles(String filename1, String filename2) throws IOException {
        System.out.println("Comparing files " + filename1 + " to " + filename2);
        try (BufferedReader reader1 = new BufferedReader(new FileReader(filename1));
                BufferedReader reader2 = new BufferedReader(new FileReader(filename2))) {
            while (true) {
                String line1 = reader1.readLine();
                String line2 = reader2.readLine();
                if (line1 == null) {
                    assertNull(line2);
                } else {
                    assertNotNull(line2);
                    assertEquals(line1, line2);
                }
            }
        }
    }

}
