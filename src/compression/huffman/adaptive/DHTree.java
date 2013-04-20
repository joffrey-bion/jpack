package compression.huffman.adaptive;

public class DHTree {

    private int weight;
    private int number;
    private DHTree parent;

    // node attributes
    private DHTree zero; // son corresponding to code 0
    private DHTree one; // son corresponding to code 1

    // leaf attribute
    private Character c;

    /** Creates a new NYT root (starting tree). */
    public DHTree() {
        this.parent = null;
        this.weight = 0;
        this.number = Character.MAX_VALUE;
        this.zero = null;
        this.one = null;
        this.c = null;
    }

    /** Creates a new NYT leaf. */
    private DHTree(DHTree parent) {
        this.parent = parent;
        this.weight = 0;
        this.number = parent.number - 2;
        this.zero = null;
        this.one = null;
        this.c = null;
    }

    /** Creates a new character leaf. */
    private DHTree(Character c, DHTree parent) {
        this.parent = parent;
        this.weight = 1;
        this.number = parent.number - 1;
        this.zero = null;
        this.one = null;
        this.c = c;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        // either a character leaf or NYT
        return c != null || weight == 0;
    }

    public Character getChar() {
        return c;
    }
    
    public String getCode(Character c) {
        return getCode(c, "");
    }
    
    private String getCode(Character c, String code) {
        if (isLeaf())
            return this.c == c ? code : "";
        else
            return zero.getCode(c, code + "0") + one.getCode(c, code + "1");
    }

    public void oneMore(Character c) {
        DHTree node = getLeaf(c);
        if (node == null) {
            node = getLeaf(null); // gets NYT
            node.zero = new DHTree(node); // new NYT
            node.one = new DHTree(c, node); // new node for c
        }
        node.update();
    }

    private void update() {
        DHTree max = getMaxInBlock();
        if (max != this && max != this.parent && !max.isRoot())
            swapWith(max);
        weight++;
        if (!isRoot())
            parent.update();
    }

    private DHTree getLeaf(Character c) {
        if (isLeaf()) {
            return (this.c == c) ? this : null;
        } else {
            DHTree node = zero.getLeaf(c);
            return (node == null) ? one.getLeaf(c) : node;
        }
    }

    private DHTree getMaxInBlock() {
        if (parent.weight == weight)
            return parent.getMaxInBlock();
        else if (parent.zero == this)
            return parent.one.weight == weight ? parent.one : this;
        else
            return this;
        // TODO check if needed to go farther to the right
    }

    private void swapWith(DHTree tree) {
        if (parent.zero == this) {
            parent.zero = tree;
            tree.parent.zero = this;
        } else {
            parent.one = tree;
            tree.parent.one = this;
        }
        DHTree treeParent = tree.parent;
        tree.parent = parent;
        parent = treeParent;
        int treeNumber = tree.number;
        tree.number = number;
        number = treeNumber;
    }

    /*
     * Fancy printing methods
     */

    private static final char vline = Character.toChars(Integer.parseInt("2502", 16))[0];
    private static final char hline = Character.toChars(Integer.parseInt("2500", 16))[0];
    private static final char midBranch = Character.toChars(Integer.parseInt("251c", 16))[0];
    private static final char endBranch = Character.toChars(Integer.parseInt("2514", 16))[0];

    public String toString(String indent) {
        String newIndent = indent.replace(endBranch, ' ').replace(hline, ' ')
                .replace(midBranch, vline);
        String indL = newIndent + " " + midBranch + hline;
        String indR = newIndent + " " + endBranch + hline;
        String res = indent;
        if (isLeaf()) {
            res += "<" + c + ">";
        } else {
            res += "[#]\n";
            if (zero == null)
                res += indL + "null";
            else
                res += zero.toString(indL);
            res += "\n";
            if (one == null)
                res += indR + "null";
            else
                res += one.toString(indR);
        }
        return res;
    }

    public String toString() {
        return toString("");
    }
}
