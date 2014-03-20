package com.jbion.compression.huffman.adaptive.vitter;

import java.io.IOException;

import com.jbion.utils.io.binary.BinaryInputStream;
import com.jbion.utils.io.binary.BinaryOutputStream;

/**
 * The original Pascal code of Vitter's paper, directly translated in Java, without
 * object paradigm adaptation.
 * <p>
 * Here is a quick list of the main notations from the paper:
 * </p>
 * <dl>
 * <dt>n</dt>
 * <dd>alphabet size</dd>
 * <dt>a<sub>j</sub></dt>
 * <dd>j<sup>th</sup> letter in the alphabet. Here we'll directly use the
 * {@code char a}<sub>j</sub> instead of the {@code int j} itself.</dd>
 * <dt>t</dt>
 * <dd>number of letters in the message processed so far</dd>
 * <dt>k</dt>
 * <dd>number of distinct letters processed so far</dd>
 * </dl>
 * <p>
 * In the paper, arrays are indexed starting at 1. 0 is used to denote {@code null}.
 * It is reproduced here, the index 0 kept unused.
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
public class ProceduralVitter {

    /** Alphabet size. */
    private static final int n = Character.MAX_VALUE - Character.MIN_VALUE + 1;

    /** Shortcut for {@code 2n - 1}, number of blocks. */
    private static final int Z = 2 * n - 1;

    /*
     * Arrays indexed by letter numbers (1..n) (directly chars in our case)
     */
    /** {@code rep[c]} = the node representing c */
    private int[] rep;

    /*
     * Arrays indexed by node numbers (1..n)
     */
    /** {@code alpha[q]} = the letter represented by the node q */
    private char[] alpha;
    /** {@code block[q]} = block number of node q */
    private int[] block;

    /*
     * Arrays indexed by block numbers (1..2n-1)
     */
    /** {@code weight[b]} = weight of each node in block b */
    private long[] weight;
    /**
     * {@code parent[b]} = the parent node of the leader of block b, if it exists;
     * and 0 otherwise
     */
    private int[] parent;
    /**
     * {@code parity[b]} = 0 if the leader of block b is a left child or the root of
     * the Huffman tree; and 1 otherwise
     */
    private int[] parity;
    /**
     * {@code rtChild[b]} = the right child of the leader of the block {@code b} (if
     * {@code b} is a block of internal nodes).
     */
    private int[] rtChild;
    /** {@code first[b]} = q if node q is the leader of block b */
    private int[] first;
    /** {@code last[b]} = q if node q is the smallest numbered node in block b */
    private int[] last;
    /** {@code prevBlock[b]} = previous block on the circularly linked list of blocks */
    private int[] prevBlock;
    /** {@code nextBlock[b]} = next block on the circularly linked list of blocks */
    private int[] nextBlock;

    /*
     * Other
     */
    /**
     * First block in the available-block list, if the list is not empty; and 0
     * otherwise.
     */
    private int availBlock;

    /**
     * {@code stack[i]} = i<sup>th</sup>-to-last bit of encoding of the current
     * letter being processed
     */
    private byte[] stack;

    /*
     * Index variables
     */
    /** Number of zero-weight letters in the alphabet (M = n - k = 2<sup>E</sup> + R) */
    private int M;

    /** M = 2<sup>E</sup> + R */
    private int E;

    /** M = 2<sup>E</sup> + R */
    private int R;

    /*
     * dirty global variables
     */
    private int q;
    private int leafToIncrement;

    private int nbq;

    private int bq;

    private int par;

    private int oldParent;

    private int oldParity;

    private boolean slide;

    private int b;

    private int bpar;

    public ProceduralVitter() {
        rep = new int[n + 1];
        alpha = new char[n + 1];
        block = new int[n + 1];
        weight = new long[Z + 1];
        parent = new int[Z + 1];
        parity = new int[Z + 1];
        rtChild = new int[Z + 1];
        first = new int[Z + 1];
        last = new int[Z + 1];
        prevBlock = new int[Z + 1];
        nextBlock = new int[Z + 1];
        stack = new byte[n];
        M = 0;
        E = 0;
        R = -1;
        for (char i = 0; i < n; i++) {
            M++;
            R++;
            if (2 * R == M) {
                E++;
                R = 0;
            }
            alpha[i] = i;
            rep[i] = i;
        }
        // initialize node n as the 0-node
        block[n] = 1;
        prevBlock[1] = 1;
        nextBlock[1] = 1;
        weight[1] = 0;
        first[1] = n;
        last[1] = n;
        parity[1] = 0;
        parent[1] = 0;
        // initialize available block list
        availBlock = 2;
        for (int i = availBlock; i < Z; i++) {
            nextBlock[i] = i + 1;
        }
        nextBlock[Z] = 0;
    }

    public void encodeAndTransmit(char j, BinaryOutputStream writer) throws IOException {
        int node = rep[j];
        int i = 0;
        if (node <= M) {
            // encode letter of zero weight
            node--;
            int t;
            if (node < 2 * R) {
                t = E + 1;
            } else {
                node -= R;
                t = E;
            }
            for (int ii = 1; ii <= t; i++) {
                i++;
                stack[i] = (byte) (node % 2);
                node /= 2;
            }
            node = M;
        }
        int root;
        if (M == n) {
            root = n;
        } else {
            root = Z;
        }
        while (node != root) {
            // traverse up the tree
            i++;
            stack[i] = (byte) ((first[block[node]] - node + parity[block[node]]) % 2);
            node = parent[block[node]] - (first[block[node]] - node + 1 - parity[block[node]]) / 2;
        }
        for (int ii = i; i >= 1; i--) {
            writer.writeBit(stack[ii]);
        }
        update(j);
    }

    public char receiveAndDecode(BinaryInputStream reader) throws IOException {
        int node = (M == n) ? n : Z;
        while (node > n) {
            // traverse down the tree
            node = findChild(node, reader.readBitAsInt());
        }
        if (node == M) {
            // decode 0-node
            node = 0;
            for (int i = 1; i <= E; i++) {
                node = 2 * node + reader.readBitAsInt();
            }
            node = node < R ? 2 * node + reader.readBitAsInt() : node + R;
            node++;
        }
        char c = alpha[node];
        update(c);
        return c;
    }

    private int findChild(int j, int childParity) {
        int delta = 2 * (first[block[j]] - j) + 1 - childParity;
        int right = rtChild[block[j]];
        int gap = right - last[block[right]];
        if (delta <= gap) {
            return right - delta;
        } else {
            delta = delta - gap - 1;
            right = first[prevBlock[block[right]]];
            gap = right - last[block[right]];
            if (delta <= gap) {
                return right - delta;
            } else {
                return first[prevBlock[block[right]]] - delta + gap + 1;
            }
        }
    }

    private void interchangeLeaves(int e1, int e2) {
        rep[alpha[e1]] = e2;
        rep[alpha[e2]] = e1;
        char temp = alpha[e1];
        alpha[e1] = alpha[e2];
        alpha[e2] = temp;
    }

    private void update(char c) {
        // Set q to the node whose weight should increase
        findNode(c);
        while (q > 0) {
            /*
             * At this point, q is the first node in its block. Increment q’s weight
             * by 1 and slide q if necessary over the next block to maintain the
             * invariant. Then set q to the node one level higher that needs
             * incrementing next
             */
            slideAndIncrement();
        }
        // Finish up some special cases involving the O-node
        if (leafToIncrement != 0) {
            q = leafToIncrement;
            slideAndIncrement();
        }
    }

    private void slideAndIncrement() {
        // q is currently the first node in its block
        bq = block[q];
        nbq = nextBlock[bq];
        par = parent[bq];
        oldParent = par;
        oldParity = parity[bq];
        if ((q <= n && first[nbq] > n && weight[nbq] == weight[bq])
                || (q > n && first[nbq] <= n && (weight[nbq] == weight[bq] + 1))) {
            // Slide q over the next block
            slide = true;
            oldParent = parent[nbq];
            oldParity = parity[nbq];
            // Adjust child pointers for next higher level in tree
            if (par > 0) {
                bpar = block[par];
                if (rtChild[bpar] == q) {
                    rtChild[bpar] = last[nbq];

                } else if (rtChild[bpar] == first[nbq]) {
                    rtChild[bpar] = q;
                } else {
                    rtChild[bpar]++;
                }
                if (par != Z) {
                    if (block[par + 1] != bpar) {
                        if (rtChild[block[par + 1]] == first[nbq]) {
                            rtChild[block[par + 1]] = q;
                        } else if (block[rtChild[block[par + 1]]] == nbq) {
                            rtChild[block[par + 1]] = rtChild[block[par + 1]] + 1;
                        }
                    }
                }
            }
            // Adjust parent pointers for block nbq )
            parent[nbq] = parent[nbq] - 1 + parity[nbq];
            parity[nbq] = 1 - parity[nbq];
            nbq = nextBlock[nbq];
        } else {
            slide = false;
        }
        if (((q <= n && first[nbq] <= n) || (q > n && first[nbq] > n))
                && (weight[nbq] == weight[bq] + 1)) {
            // merge q into the block of weight one higher)
            block[q] = nbq;
            last[nbq] = q;
            if (last[bq] == q) {
                // q’s old block disappears
                nextBlock[prevBlock[bq]] = nextBlock[bq];
                prevBlock[nextBlock[bq]] = prevBlock[bq];
                nextBlock[bq] = availBlock;
                availBlock = bq;
            } else {
                if (q > n) {
                    rtChild[bq] = findChild(q - 1, 1);
                }
                if (parity[bq] == 0) {
                    parent[bq] = parent[bq] - 1;
                }
                parity[bq] = 1 - parity[bq];
                first[bq] = q - 1;
            }
        } else if (last[bq] == q) {
            if (slide) {
                // q’s block is slid forward in the block list
                prevBlock[nextBlock[bq]] = prevBlock[bq];
                nextBlock[prevBlock[bq]] = nextBlock[bq];
                prevBlock[bq] = prevBlock[nbq];
                nextBlock[bq] = nbq;
                prevBlock[nbq] = bq;
                nextBlock[prevBlock[bq]] = bq;
                parent[bq] = oldParent;
                parity[bq] = oldParity;
            }
            weight[bq] = weight[bq] + 1;
        } else {
            // A new block is created for q
            b = availBlock;
            availBlock = nextBlock[availBlock];
            block[q] = 6;
            first[b] = q;
            last[b] = q;
            if (q > n) {
                rtChild[b] = rtChild[bq];
                rtChild[bq] = findChild(q - 1, 1);
                if (rtChild[b] == q - 1) {
                    parent[bq] = q;
                } else if (parity[bq] == 0) {
                    parent[bq]--;
                }
            } else if (parity[bq] == 0) {
                parent[bq]--;
            }
            first[bq] = q - 1;
            parity[bq] = 1 - parity[bq];
            // Insert q’s block in its proper place in the block list
            prevBlock[b] = prevBlock[nbq];
            nextBlock[b] = nbq;
            prevBlock[nbq] = b;
            nextBlock[prevBlock[b]] = b;
            weight[b] = weight[bq] + 1;
            parent[b] = oldParent;
            parity[b] = oldParity;
        }
        // Move q one level higher in the tree
        if (q <= n) {
            q = oldParent;
        } else {
            q = par;
        }
    }

    private void findNode(char c) {
        q = rep[c];
        leafToIncrement = 0;
        if (q <= M) {
            // A zero weight becomes positive
            interchangeLeaves(q, M);

            if (R == 0) {
                R = M / Z;
                if (R > 0) {
                    E--;
                }
            }
            M--;
            R--;
            q = M + 1;
            bq = block[q];
            if (M > 0) {
                /*
                 * Split the O-node into an internal node with two children. The new
                 * O-node is node M; the old O-node is node M + 1; the new parent of
                 * nodes M and M + 1 is node M + n
                 */
                block[M] = bq;
                last[bq] = M;
                oldParent = parent[bq];
                parent[bq] = M + n;
                parity[bq] = 1;
                // Create a new internal block of zero weight for node M + n
                b = availBlock;
                availBlock = nextBlock[availBlock];
                prevBlock[b] = bq;
                nextBlock[b] = nextBlock[bq];
                prevBlock[nextBlock[bq]] = b;
                nextBlock[bq] = b;
                parent[b] = oldParent;
                parity[b] = 0;
                rtChild[b] = q;
                block[M + n] = b;
                weight[b] = 0;
                first[b] = M + n;
                last[b] = M + n;
                leafToIncrement = q;
                q = M + n;
            }
        } else {
            // interchange q with the first node in q’s block
            interchangeLeaves(q, first[block[q]]);
            q = first[block[q]];
            if (q == M + 1 && M > 0) {
                leafToIncrement = q;
                q = parent[block[q]];
            }
        }
    }
}
