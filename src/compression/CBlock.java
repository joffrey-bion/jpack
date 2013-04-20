package compression;

/**
 * Represents a compressed block of text.
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey Bion</a>
 */
class CBlock {

    /**
     * Header of the block, that can be used to store necessary information for
     * decoding.
     */
    public String header = null;
    /**
     * Encoded stream representing the source data.
     */
    public String content = null;

    @Override
    public String toString() {
        String h = header;
        if (h == null)
            h = "---";
        return h + " " + content;
    }
}
