package com.jbion.compression.huffman.adaptive.vitter;

import java.io.IOException;

import com.jbion.utils.io.binary.BitInputStream;
import com.jbion.utils.io.binary.BitOutputStream;

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
 * <p>
 * Other definitions:
 * </p>
 * <dl>
 * <dt>leaf block</dt>
 * <dd>all the leaves of a given weight</dd>
 * <dt>internal block</dt>
 * <dd>all the internal nodes of a given weight</dd>
 * <dt>leader of a block</dt>
 * <dd>largest numbered node in the block</dd>
 * </dl>
 * <p>
 * Invariants of the algorithm:
 * </p>
 * <ul>
 * <li>Blocks are linked together in increasing order by weight.</li>
 * <li>The leaf block of a given weight always precedes the internal block of same
 * weight.</li>
 * </ul>
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
    private final int[] rep;

    /*
     * Arrays indexed by leaf node numbers (1..n)
     */
    /** {@code alpha[q]} = the letter represented by the node q */
    private final char[] alpha;

    /*
     * Arrays indexed by node numbers (M..n | M+n..2n-1)
     */
    /** {@code block[q]} = block number of node q */
    private final int[] block;
    
    /*
     * Arrays indexed by block numbers (1..2n-1)
     */    
    /** {@code weight[b]} = weight of each node in block b */
    private final long[] weight;
    /**
     * {@code parent[b]} = the parent node of the leader of block b, if it exists;
     * and 0 otherwise
     */
    private final int[] parent;
    /**
     * {@code parity[b]} = 0 if the leader of block b is a left child or the root of
     * the Huffman tree; and 1 otherwise
     */
    private final int[] parity;
    /**
     * {@code rtChild[b]} = the right child of the leader of the block {@code b} (if
     * {@code b} is a block of internal nodes).
     */
    private final int[] rtChild;
    /** {@code first[b]} = q if node q is the leader of block b */
    private final int[] first;
    /** {@code last[b]} = q if node q is the smallest numbered node in block b */
    private final int[] last;
    /** {@code prevBlock[b]} = previous block on the circularly linked list of blocks */
    private final int[] prevBlock;
    /** {@code nextBlock[b]} = next block on the circularly linked list of blocks */
    private final int[] nextBlock;

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
    private final byte[] stack;

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
     * dirty global variables (original paper)
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
        System.out.println("Vitter initialization...");
        rep = new int[n + 1];
        alpha = new char[n + 1];
        
        block = new int[Z + 1];

        weight = new long[Z + 1];
        parent = new int[Z + 1];
        parity = new int[Z + 1];
        rtChild = new int[Z + 1];
        first = new int[Z + 1];
        last = new int[Z + 1];
        prevBlock = new int[Z + 1];
        nextBlock = new int[Z + 1];

        stack = new byte[n + 1];
        M = 0;
        E = 0;
        R = -1;
        for (int c = 0; c < n; c++) {
            M++;
            R++;
            if (2 * R == M) {
                E++;
                R = 0;
            }
            alpha[c] = (char) c;
            rep[c] = c;
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
        System.out.println("Initialization done.");
    }

    public void encodeAndTransmit(char j, BitOutputStream writer) throws IOException {
        System.out.println();
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
            System.out.print("encode letter of zero weight: ");
            for (int ii = 1; ii <= t; ii++) {
                i++;
                stack[i] = (byte) (node % 2);
                node = node / 2;
                System.out.print(stack[i]);
            }
            System.out.println();
            node = M;
        }
        int root = M == n ? n : Z;
        System.out.print("traverse up the tree: node = " + node);
        while (node != root) {
            // traverse up the tree
            i++;
            int bl = block[node];
            stack[i] = (byte) ((first[bl] - node + parity[bl]) % 2);
            System.out.print("[b="+ bl+" p=" + parent[bl] + "]");
            node = parent[bl] - (first[bl] - node + 1 - parity[bl]) / 2;
            System.out.print(", " + node);
        }
        System.out.println(" (root)");
        System.out.print("  => write bits: ");
        for (int ii = i; ii >= 1; ii--) {
            System.out.print(stack[ii]);
            writer.writeBit(stack[ii]);
        }
        System.out.println();
        update(j);
    }

    public Character receiveAndDecode(BitInputStream stream) throws IOException {
        try {
            int node = M == n ? n : Z;
            while (node > n) {
                // traverse down the tree
                node = findChild(node, (int) stream.readBits(1));
            }
            if (node == M) {
                // decode 0-node
                node = (int) stream.readBits(E);
                node = node < R ? 2 * node + ((int) stream.readBits(1)) : node + R;
                node++;
            }
            final char c = alpha[node];
            update(c);
            return c;
        } catch (IllegalStateException e) {
            return null;
        }
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
        final char temp = alpha[e1];
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
                || (q > n && first[nbq] <= n && weight[nbq] == weight[bq] + 1)) {
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
            // Adjust parent pointers for block nbq
            parent[nbq] = parent[nbq] - 1 + parity[nbq];
            parity[nbq] = 1 - parity[nbq];
            nbq = nextBlock[nbq];
        } else {
            slide = false;
        }
        if ((q <= n && first[nbq] <= n || q > n && first[nbq] > n) && weight[nbq] == weight[bq] + 1) {
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
                R = M / 2;
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
