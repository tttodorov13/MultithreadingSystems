/**
 * HuffmanTest tree's leaf.
 *
 * @author Toshko Todorov
 * @since 1.0
 */
class HuffmanLeaf extends HuffmanTree {

    /**
     * The character this leaf represents.
     */
    final char value;

    HuffmanLeaf(int freq, char val) {
        super(freq);
        value = val;
    }
}