package com.joffrey_bion.compression.huffman.adaptive;


import java.io.IOException;

import com.joffrey_bion.binary_io.BinFileWriter;

public class DHEncoder {

    private BinFileWriter output;
    private DHTree tree;
    
    public DHEncoder(BinFileWriter output) {
        this.output = output;
        this.tree = new DHTree();
    }

    public void encode(Character c) throws IOException {
        output.write(tree.getCode(c));
        tree.oneMore(c);
    }
}
