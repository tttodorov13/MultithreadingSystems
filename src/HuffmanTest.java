import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Class to test Huffman algorithm.
 *
 * @author Toshko Todorov
 * @since 1.0
 */
public class HuffmanTest {

    /**
     * Declare an int to store c number of threads used.
     */
    private static int numThreads = 1;

    /**
     * Declare a boolean to store c whether it is quiet.
     */
    private static boolean quiet;

    /**
     * Declare a hash map to store c frequencies.
     */
    private static Map<Character, Integer> charFreqsMap = new HashMap<>();

    /**
     * Declare a list of string to store results.
     */
    private static List<String> result = new ArrayList<>();

    /**
     * Input is an map of frequencies, keyed by character code
     *
     * @param charFreqs the frequencies of the chars
     * @return Huffman tree
     * @since 1.0.
     */
    private static HuffmanTree buildTree(Map<Character, Integer> charFreqs) {
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<>();
        // Initially, there is a forest of leaves
        // one for each non-empty character
        for (Map.Entry<Character, Integer> entry : charFreqs.entrySet()) {
            trees.offer(new HuffmanLeaf(entry.getValue(), entry.getKey()));
        }

        assert trees.size() > 0;
        // loop until there is only one tree left
        while (trees.size() > 1) {
            // two trees with least frequency
            HuffmanTree a = trees.poll();
            HuffmanTree b = trees.poll();

            // put into new node and re-insert into queue
            trees.offer(new HuffmanNode(a, b));
        }
        return trees.poll();
    }

    /**
     * Implement Huffman algorithm.
     *
     * @param tree   the tree to be checked
     * @param prefix the current char prefix
     * @since 1.0.
     */
    private static void encode(HuffmanTree tree, StringBuffer prefix) {
        assert tree != null;

        if (tree instanceof HuffmanLeaf) {
            HuffmanLeaf leaf = (HuffmanLeaf) tree;

            // put in character, frequency, and code for this leaf (which is just the prefix)
            result.add(String.format("%c\t\t%d\t\t%s", leaf.value, leaf.frequency, prefix.toString()));
        } else if (tree instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode) tree;

            // traverse left
            prefix.append('0');
            encode(node.left, prefix);
            prefix.deleteCharAt(prefix.length() - 1);

            // traverse right
            prefix.append('1');
            encode(node.right, prefix);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    private static void runDistributed(Path pathRead, Path pathWrite) {
        // Record the start time for execution.
        long startTime = System.currentTimeMillis();

        // Set the content read from the input file.
        StringBuffer content = new StringBuffer();

        try {
            // Append the content read from the input file.
            Files.lines(pathRead).forEach(s -> content.append(s));
        } catch (IOException e) {
            // Print a stack trace of input-output exception.
            e.printStackTrace();
        }

        // Distribute task on multiple threads.contentList
        Thread jobs[] = new Thread[getNumThreads()];

        // Set thread usage counter.
        int jobNumber = 0;

        // Create a content list to be distributed among the threads.
        List<String> contentList = new ArrayList<>();

        // Optimize split content into parts.
        int contentListDivide = (content.length() % getNumThreads()) * getNumThreads();
        int contentListElementSize;

        // Split content into parts.
        if (content.length() > getNumThreads()) {
            if (content.length() % getNumThreads() == 0) {
                contentListElementSize = content.length() / getNumThreads();
                contentList.addAll(Arrays.asList(content.toString().split("(?<=\\G.{" + contentListElementSize + "})")));
            } else {
                contentListElementSize = (content.length() - content.length() % getNumThreads()) / getNumThreads();
                contentList.addAll(Arrays.asList(content.substring(0, contentListDivide).split("(?<=\\G.{" + contentListElementSize + "})")));
                String first = contentList.get(0).concat(content.substring(contentListDivide));
                contentList.set(0, first);
            }
        } else {
            for (Character c : content.toString().toCharArray()) {
                contentList.add(String.valueOf(c));
            }
        }

        for (String s : contentList) {
            BuildTreeRunnable r = new BuildTreeRunnable(s);
            Thread t = new Thread(r);
            t.start();
            jobs[jobNumber] = t;
            if (!isQuiet()) {
                System.out.format("Thread-%d started.\n", ++jobNumber);
            }
        }

        // Join all threads.
        for (int i = 0; i < jobNumber; i++) {
            try {
                jobs[i].join();
                if (!isQuiet()) {
                    System.out.format("Thread-%d stopped.\n", i + 1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!isQuiet()) {
            System.out.format("Threads used in current run: %d\n", jobNumber);
        }

        // Build a tree.
        HuffmanTree tree = buildTree(BuildTreeRunnable.charFreqsMap);

        // Add the result to be printed into the output file.
        result.add(content.toString());
        result.add(String.format("SYMBOL\tWEIGHT\tCODE"));

        // Build encoding language.
        encode(tree, new StringBuffer());

        // Record the end time for execution.
        long stopTime = System.currentTimeMillis();

        // Calculate the time for execution.
        long elapsedTime = stopTime - startTime;

        // Print out the time for execution to console.
        System.out.format("Total execution time for current run (millis): %d", elapsedTime);

        try {
            // Print out the results.
            Files.write(pathWrite, result, Charset.forName("UTF-8"));
        } catch (IOException e) {
            // Print a stack trace of input-output exception.
            e.printStackTrace();
        }
    }

    /**
     * Execute the program implementing Huffman algorithm.
     *
     * @since 1.0.
     */
    public static void main(String[] args) {
        // Print out a leading message to introduce the user to the program.
        System.out.format("This is an example for Huffman encoding.\nEnter file name and threads' number in format: -f String -t int\n");

        // Set a respond in case of bad request.
        final String WRONG_REQUEST = "Wrong request format!";

        // Read the input from the console.
        Scanner scan = new Scanner(System.in);
        List<String> request = Arrays.asList(scan.nextLine().split("\\s+"));

        // Test for bad input.
        if ((request.size() != 4 && request.size() != 5) || !"-f".equals(request.get(0)) || !"-t".equals(request.get(2))) {
            System.out.println(WRONG_REQUEST);
            return;
        }

        // Set the file for input and output file.
        Path pathRead = Paths.get(request.get(1));
        Path pathWrite = Paths.get("result.dat");

        // Test for bad number of threads.
        try {
            setNumThreads(Integer.valueOf(request.get(3)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (getNumThreads() <= 0) {
            System.out.println(WRONG_REQUEST);
            return;
        }

        // Set quiet mode
        if (request.size() == 5 && "-q".equals(request.get(4))) {
            setQuiet(true);
        }

        // Do the magic.
        runDistributed(pathRead, pathWrite);
    }

    private static int getNumThreads() {
        return numThreads;
    }

    private static void setNumThreads(int numThreads) {
        HuffmanTest.numThreads = numThreads;
    }

    private static boolean isQuiet() {
        return quiet;
    }

    private static void setQuiet(boolean quiet) {
        HuffmanTest.quiet = quiet;
    }
}