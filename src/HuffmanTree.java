/**
 * HuffmanTest tree realization.
 *
 * @author Toshko Todorov
 * @since 1.0
 */
abstract class HuffmanTree implements Comparable<HuffmanTree> {
    /**
     * The frequency of this tree.
     */
    final int frequency;

    HuffmanTree(int freq) {
        frequency = freq;
    }

    /**
     * Compares on the frequency
     *
     * @param tree to be compared
     * @return the frequency of meeting this tree
     * @since 1.0
     */
    public int compareTo(HuffmanTree tree) {
        return frequency - tree.frequency;
    }
}