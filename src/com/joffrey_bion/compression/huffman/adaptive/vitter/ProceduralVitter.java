package com.joffrey_bion.compression.huffman.adaptive.vitter;

public class ProceduralVitter {

    /*
     * Arrays indexed by letter numbers (1..n)
     */
    private int[] rep;
    
    /*
     * Arrays indexed by node numbers (1..n)
     */
    private int[] alpha;
    private int[] block;
    
    /*
     * Arrays indexed by block numbers (1..2n-1)
     */
    private int[] weight;
    private int[] parent;
    private int[] parity;
    private int[] rightChild;
    private int[] first;
    private int[] last;
    private int[] prevBlock;
    private int[] nextBlock;
    
    private int availBlock;
    
    private int[] stack;
    
    
}
