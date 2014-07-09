package com.jbion.compression.huffman.adaptive.vitter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import com.jbion.utils.io.binary.BitInputStream;
import com.jbion.utils.io.binary.BitOutputStream;

public class VitterTest {
    
    private static final String TEST_FILES_LOCATION = "../texts-test";
    private static final String TEMP_BINARY_FILE = "C:/temp/temp";
    
    private BufferedReader in;
    
    @Before
    public void init() throws FileNotFoundException {
        in = new BufferedReader(new FileReader(TEST_FILES_LOCATION + "/abra.txt"));
    }

    @Test
    public void testVitter() throws IOException {
        BitOutputStream bos = new BitOutputStream(new FileOutputStream(TEMP_BINARY_FILE));
        ProceduralVitter vitter = new ProceduralVitter();
        int b;
        while ((b = in.read()) != -1) {
            vitter.encodeAndTransmit((char) b, bos);
        }
        BitInputStream bis = new BitInputStream(new FileInputStream(TEMP_BINARY_FILE));
        vitter = new ProceduralVitter();
        while (true) {
            vitter.receiveAndDecode(bis);
            // TODO complete
        }
    }
    
}
