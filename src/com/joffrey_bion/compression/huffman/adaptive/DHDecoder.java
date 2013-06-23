package com.joffrey_bion.compression.huffman.adaptive;

import java.io.IOException;

import com.joffrey_bion.binary_io.BinFileReader;

public class DHDecoder {

    private BinFileReader input;
    private DHTree tree;
    
    public DHDecoder(BinFileReader input) {
        this.input = input;
        this.tree = new DHTree();
    }

    public char decode() throws IOException {
        DHTree node = readNextLeaf();
        char c;
        if (node.isNYT()) {
            c = input.readChar();
        } else {
            c = node.getChar();
        }
        tree.oneMore(c);
        return c;
    }

    private DHTree readNextLeaf() throws IOException {
        DHTree node = tree;
        while (!node.isLeaf()) {
            if (input.readBit()) {
                node = node.getOne();
            } else {
                node = node.getZero();
            }
        }
        return node;
    }
}
