package com.jbion.compression.huffman.adaptive.vitter;

public class Leaf extends Node {

    private Character c;

    public Leaf() {
        this(null);
    }
    
    public Leaf(Character c) {
        super();
        this.c = c;
    }
    
    char getChar() {
        return c;
    }
    
    @Override
    boolean isZeroNode() {
        return c == null;
    }

    @Override
    boolean isLeaf() {
        return true;
    }
}
