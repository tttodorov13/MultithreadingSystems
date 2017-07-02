/**
 * HuffmanTest tree's node.
 *
 * @author Toshko Todorov
 * @since 1.0
 */
class HuffmanNode extends HuffmanTree {
    final HuffmanTree left, right; // subtrees

    HuffmanNode(HuffmanTree l, HuffmanTree r) {
        super(l.frequency + r.frequency);
        left = l;
        right = r;
    }
}