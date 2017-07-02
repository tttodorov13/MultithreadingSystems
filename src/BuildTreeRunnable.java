import java.util.HashMap;
import java.util.Map;

/**
 * Implement Huffman build tree algorithm.
 *
 * @author Toshko Todorov
 * @since 1.0.
 */
class BuildTreeRunnable implements Runnable {

    /**
     * Declare a hash map to store c frequencies.
     */
    static Map<Character, Integer> charFreqsMap = new HashMap<>();

    /**
     * Declare a string to explore for character frequencies.
     */
    private String content;

    BuildTreeRunnable(String content) {
        this.content = content;
    }

    @Override
    public void run() {
        // Read each character and record its frequency.
        for (char c : content.toCharArray()) {
            if (charFreqsMap.containsKey(c)) {
                charFreqsMap.put(c, charFreqsMap.get(c) + 1);
            } else {
                charFreqsMap.put(c, 1);
            }
        }
    }
}
