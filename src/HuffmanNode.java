/**
 * HuffmanTest tree's node.
 *
 * @author Toshko Todorov
 * @since 1.0
 */
class HuffmanNode extends HuffmanTree {

    /**
     * The subtrees.
     */
    final HuffmanTree left, right;

    HuffmanNode(HuffmanTree l, HuffmanTree r) {
        super(l.frequency + r.frequency);
        left = l;
        right = r;
    }
}